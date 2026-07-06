package com.hardrockunion.business.merchant.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.merchant.domain.entity.MerchantOrder;
import com.hardrockunion.business.merchant.domain.entity.MerchantOrderItem;
import com.hardrockunion.business.merchant.domain.entity.MerchantProduct;
import com.hardrockunion.business.merchant.domain.entity.MerchantQuotation;
import com.hardrockunion.business.merchant.domain.entity.MerchantQuotationItem;
import com.hardrockunion.business.merchant.dto.MerchantOrderCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantOrderItemCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantOrderItemResponse;
import com.hardrockunion.business.merchant.dto.MerchantOrderQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantOrderResponse;
import com.hardrockunion.business.merchant.dto.MerchantOrderShipRequest;
import com.hardrockunion.business.merchant.dto.MerchantOrderStatusUpdateRequest;
import com.hardrockunion.business.merchant.mapper.MerchantOrderItemMapper;
import com.hardrockunion.business.merchant.mapper.MerchantOrderMapper;
import com.hardrockunion.business.merchant.mapper.MerchantProductMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 商城订单服务。
 *
 * <p>这里负责 商户订单主链路：创建订单、报价转订单、接单、发货、收货和完成。
 * 当前先把履约收口在商家订单内，后续再从真实履约需求中抽出仓储和物流能力。
 */
@Service
public class MerchantOrderService {

    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MerchantOrderMapper orderMapper;
    private final MerchantOrderItemMapper orderItemMapper;
    private final MerchantProductMapper productMapper;
    private final MerchantAccessGuard merchantAccessGuard;
    private final AppRegistryQueryService appRegistryQueryService;
    private final TenantRegistryService tenantRegistryService;

    public MerchantOrderService(MerchantOrderMapper orderMapper,
                            MerchantOrderItemMapper orderItemMapper,
                            MerchantProductMapper productMapper,
                            MerchantAccessGuard merchantAccessGuard,
                            AppRegistryQueryService appRegistryQueryService,
                            TenantRegistryService tenantRegistryService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.merchantAccessGuard = merchantAccessGuard;
        this.appRegistryQueryService = appRegistryQueryService;
        this.tenantRegistryService = tenantRegistryService;
    }

    /**
     * 查询当前租户下的订单，并支持按状态、时间、交易目标和仓配字段筛选。
     */
    public PageResponse<MerchantOrderResponse> list(MerchantOrderQueryRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantOrderQueryRequest query = request == null ? new MerchantOrderQueryRequest() : request;
        validateQueryRange(query);

        LambdaQueryWrapper<MerchantOrder> wrapper = new LambdaQueryWrapper<MerchantOrder>()
            .eq(MerchantOrder::getTenantId, loginUser.getTenantId())
            .eq(MerchantOrder::getDeleted, 0);

        if (StringUtils.isNotBlank(query.getOrderStatus())) {
            wrapper.eq(MerchantOrder::getOrderStatus, StringUtils.upperCase(StringUtils.trim(query.getOrderStatus())));
        }
        if (StringUtils.isNotBlank(query.getOrderKeyword())) {
            wrapper.like(MerchantOrder::getOrderNo, StringUtils.trim(query.getOrderKeyword()));
        }
        if (StringUtils.isNotBlank(query.getCustomerKeyword())) {
            String keyword = StringUtils.trim(query.getCustomerKeyword());
            wrapper.and(w -> w.like(MerchantOrder::getCustomerName, keyword)
                .or()
                .like(MerchantOrder::getCustomerPhone, keyword));
        }
        if (query.getTargetTenantId() != null) {
            wrapper.eq(MerchantOrder::getTargetTenantId, query.getTargetTenantId());
        }
        if (StringUtils.isNotBlank(query.getTargetProjectName())) {
            wrapper.like(MerchantOrder::getTargetProjectName, StringUtils.trim(query.getTargetProjectName()));
        }
        if (StringUtils.isNotBlank(query.getTargetSiteName())) {
            wrapper.like(MerchantOrder::getTargetSiteName, StringUtils.trim(query.getTargetSiteName()));
        }
        if (StringUtils.isNotBlank(query.getTargetUserName())) {
            wrapper.like(MerchantOrder::getTargetUserName, StringUtils.trim(query.getTargetUserName()));
        }
        if (query.getWarehouseId() != null) {
            wrapper.eq(MerchantOrder::getWarehouseId, query.getWarehouseId());
        }
        if (StringUtils.isNotBlank(query.getWarehouseName())) {
            wrapper.like(MerchantOrder::getWarehouseName, StringUtils.trim(query.getWarehouseName()));
        }
        if (StringUtils.isNotBlank(query.getLogisticsCompany())) {
            wrapper.like(MerchantOrder::getLogisticsCompany, StringUtils.trim(query.getLogisticsCompany()));
        }
        if (StringUtils.isNotBlank(query.getTrackingNo())) {
            wrapper.like(MerchantOrder::getTrackingNo, StringUtils.trim(query.getTrackingNo()));
        }
        if (query.getCreatedFrom() != null) {
            wrapper.ge(MerchantOrder::getCreatedAt, query.getCreatedFrom());
        }
        if (query.getCreatedTo() != null) {
            wrapper.le(MerchantOrder::getCreatedAt, query.getCreatedTo());
        }

        Page<MerchantOrder> page = orderMapper.selectPage(
            Page.of(query.getPageNum(), query.getPageSize()),
            wrapper.orderByDesc(MerchantOrder::getId));
        var responsePage = page.convert(this::toResponse);
        return PageResponse.from(responsePage);
    }

    public MerchantOrderResponse getById(Long id, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        return toResponse(loadOrder(id, loginUser.getTenantId()));
    }

    /**
     * 创建订单。
     *
     * <p>订单创建阶段只固化交易事实和成交价格，不提前绑定仓库，也不提前扣减库存。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderResponse create(MerchantOrderCreateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("订单明细不能为空");
        }
        String targetAppCode = normalizeTargetAppCode(request.getTargetAppCode());
        String targetUserName = resolveTargetUserName(request.getTargetUserName(), request.getCustomerName());
        String targetUserPhone = resolveTargetUserPhone(request.getTargetUserPhone(), request.getCustomerPhone());
        if (StringUtils.isBlank(targetUserName)) {
            throw new BusinessException("targetUserName 不能为空");
        }

        TenantRegistryResponse merchantTenant = tenantRegistryService.getById(loginUser.getTenantId());

        return createOrder(loginUser, merchantTenant,
            targetAppCode,
            request.getTargetTenantId(),
            StringUtils.trimToNull(request.getTargetProjectName()),
            StringUtils.trimToNull(request.getTargetSiteName()),
            targetUserName,
            targetUserPhone,
            StringUtils.trimToNull(request.getDeliveryAddress()),
            request.getWarehouseId(),
            StringUtils.trimToNull(request.getRemark()),
            request.getItems());
    }

    /**
     * 由报价单生成订单。
     *
     * <p>这里会把报价明细转换成订单明细，并显式保留报价价格，确保订单金额与报价金额一致。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderResponse createFromQuotation(MerchantQuotation quotation,
                                                 List<MerchantQuotationItem> quotationItems,
                                                 LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (quotation == null || quotation.getId() == null) {
            throw new BusinessException("报价单不存在，无法转订单");
        }
        if (quotationItems == null || quotationItems.isEmpty()) {
            throw new BusinessException("报价单明细为空，无法转订单");
        }

        TenantRegistryResponse merchantTenant = tenantRegistryService.getById(loginUser.getTenantId());

        List<MerchantOrderItemCreateRequest> items = quotationItems.stream()
            .map(item -> {
                MerchantOrderItemCreateRequest request = new MerchantOrderItemCreateRequest();
                request.setProductId(item.getProductId());
                request.setQuantity(item.getQuantity());
                request.setSalePrice(item.getQuotationPrice());
                return request;
            })
            .toList();

        String remark = StringUtils.defaultIfBlank(quotation.getRemark(), "")
            + (StringUtils.isBlank(quotation.getRemark()) ? "" : "；")
            + "来源报价单:" + quotation.getQuotationNo();

        return createOrder(loginUser, merchantTenant,
            normalizeTargetAppCode(quotation.getTargetAppCode()),
            quotation.getTargetTenantId(),
            quotation.getTargetProjectName(),
            quotation.getTargetSiteName(),
            StringUtils.defaultIfBlank(quotation.getTargetUserName(), quotation.getCustomerName()),
            StringUtils.defaultIfBlank(quotation.getTargetUserPhone(), quotation.getCustomerPhone()),
            null,
            quotation.getWarehouseId(),
            remark,
            items);
    }

    private MerchantOrderResponse createOrder(LoginUser loginUser,
                                          TenantRegistryResponse merchantTenant,
                                          String targetAppCode,
                                          Long targetTenantId,
                                          String targetProjectName,
                                          String targetSiteName,
                                          String targetUserName,
                                          String targetUserPhone,
                                          String deliveryAddress,
                                          Long warehouseId,
                                          String remark,
                                          List<MerchantOrderItemCreateRequest> itemRequests) {
        // 所有订单入口最终收口到这里，避免“手工下单”和“报价转单”出现两套库存与金额规则。
        if (StringUtils.isBlank(targetUserName)) {
            throw new BusinessException("targetUserName 不能为空");
        }
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new BusinessException("订单明细不能为空");
        }

        String normalizedTargetAppCode = normalizeTargetAppCode(targetAppCode);
        validateTargetTenant(normalizedTargetAppCode, targetTenantId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;
        MerchantOrder order = new MerchantOrder();
        order.setTenantId(loginUser.getTenantId());
        order.setOrderNo(generateOrderNo());
        order.setMerchantId(merchantTenant.getId());
        order.setMerchantName(merchantTenant.getTenantName());
        order.setTargetAppId(resolveAppId(normalizedTargetAppCode));
        order.setTargetAppCode(normalizedTargetAppCode);
        order.setTargetTenantId(targetTenantId);
        order.setTargetProjectName(StringUtils.trimToNull(targetProjectName));
        order.setTargetSiteName(StringUtils.trimToNull(targetSiteName));
        order.setTargetUserName(StringUtils.trim(targetUserName));
        order.setTargetUserPhone(StringUtils.trimToNull(targetUserPhone));
        order.setCustomerName(StringUtils.trim(targetUserName));
        order.setCustomerPhone(StringUtils.trimToNull(targetUserPhone));
        order.setDeliveryAddress(StringUtils.trimToNull(deliveryAddress));
        order.setWarehouseId(warehouseId);
        order.setOrderStatus("CREATED");
        order.setRemark(StringUtils.trimToNull(remark));
        order.setCreatedBy(loginUser.getUserId());
        order.setDeleted(0);
        order.setItemCount(0);
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);

        for (MerchantOrderItemCreateRequest itemRequest : itemRequests) {
            if (itemRequest == null || itemRequest.getProductId() == null || itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new BusinessException("订单明细缺少 productId 或 quantity 非法");
            }
            MerchantProduct product = productMapper.selectOne(new LambdaQueryWrapper<MerchantProduct>()
                .eq(MerchantProduct::getId, itemRequest.getProductId())
                .eq(MerchantProduct::getTenantId, loginUser.getTenantId())
                .eq(MerchantProduct::getDeleted, 0)
                .eq(MerchantProduct::getStatus, 1)
                .last("limit 1"));
            if (product == null) {
                throw new BusinessException("存在不可下单商品");
            }
            BigDecimal salePrice = itemRequest.getSalePrice() == null
                ? (product.getSalePrice() == null ? BigDecimal.ZERO : product.getSalePrice())
                : itemRequest.getSalePrice();
            if (salePrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("salePrice 不能小于 0");
            }
            BigDecimal lineAmount = salePrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            MerchantOrderItem item = new MerchantOrderItem();
            item.setTenantId(loginUser.getTenantId());
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getProductName());
            item.setSkuCode(product.getSkuCode());
            item.setUnit(product.getUnit());
            item.setQuantity(itemRequest.getQuantity());
            item.setSalePrice(salePrice);
            item.setLineAmount(lineAmount);
            item.setDeleted(0);
            orderItemMapper.insert(item);

            itemCount += itemRequest.getQuantity();
            totalAmount = totalAmount.add(lineAmount);
        }

        order.setItemCount(itemCount);
        order.setTotalAmount(totalAmount);
        orderMapper.updateById(order);
        return getById(order.getId(), loginUser);
    }

    /**
     * 更新订单状态。
     *
     * <p>目前只支持 merchant 主链路状态机；取消只允许发生在发货前。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderResponse updateStatus(Long id, MerchantOrderStatusUpdateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getTargetStatus())) {
            throw new BusinessException("targetStatus 不能为空");
        }

        MerchantOrder order = loadOrder(id, loginUser.getTenantId());
        String currentStatus = StringUtils.trimToEmpty(order.getOrderStatus()).toUpperCase();
        String targetStatus = StringUtils.trimToEmpty(request.getTargetStatus()).toUpperCase();

        if (StringUtils.equals(currentStatus, targetStatus)) {
            return toResponse(order);
        }
        if (!isAllowedTransition(currentStatus, targetStatus)) {
            throw new BusinessException("订单状态不允许从 " + currentStatus + " 流转到 " + targetStatus);
        }
        order.setOrderStatus(targetStatus);
        orderMapper.updateById(order);
        return getById(order.getId(), loginUser);
    }

    /**
     * 录入发货信息并把订单状态推进到已发货。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderResponse ship(Long id, MerchantOrderShipRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isAnyBlank(request.getLogisticsCompany(), request.getTrackingNo())) {
            throw new BusinessException("logisticsCompany、trackingNo 不能为空");
        }

        MerchantOrder order = loadOrder(id, loginUser.getTenantId());
        String currentStatus = StringUtils.trimToEmpty(order.getOrderStatus()).toUpperCase();
        if (!"ACCEPTED".equals(currentStatus)) {
            throw new BusinessException("当前订单状态不允许发货");
        }
        Long warehouseId = request.getWarehouseId() != null ? request.getWarehouseId() : order.getWarehouseId();
        List<MerchantOrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<MerchantOrderItem>()
            .eq(MerchantOrderItem::getOrderId, order.getId())
            .eq(MerchantOrderItem::getDeleted, 0)
            .orderByAsc(MerchantOrderItem::getId));
        for (MerchantOrderItem item : items) {
            deductProductStock(item, loginUser.getTenantId());
        }

        order.setWarehouseId(warehouseId);
        order.setLogisticsCompany(StringUtils.trim(request.getLogisticsCompany()));
        order.setTrackingNo(StringUtils.trim(request.getTrackingNo()));
        order.setShippedAt(request.getShippedAt() == null ? LocalDateTime.now() : request.getShippedAt());
        order.setShippingRemark(StringUtils.trimToNull(request.getShippingRemark()));
        order.setOrderStatus("SHIPPED");
        orderMapper.updateById(order);
        return getById(order.getId(), loginUser);
    }

    private boolean isAllowedTransition(String currentStatus, String targetStatus) {
        return switch (currentStatus) {
            case "CREATED" -> "ACCEPTED".equals(targetStatus) || "CANCELLED".equals(targetStatus);
            case "ACCEPTED" -> "CANCELLED".equals(targetStatus);
            case "SHIPPED" -> "RECEIVED".equals(targetStatus) || "AFTER_SALE".equals(targetStatus);
            case "RECEIVED" -> "COMPLETED".equals(targetStatus) || "AFTER_SALE".equals(targetStatus);
            default -> false;
        };
    }

    private void validateQueryRange(MerchantOrderQueryRequest query) {
        if (query.getCreatedFrom() != null && query.getCreatedTo() != null
            && query.getCreatedFrom().isAfter(query.getCreatedTo())) {
            throw new BusinessException("createdFrom 不能晚于 createdTo");
        }
    }

    private void deductProductStock(MerchantOrderItem item, Long tenantId) {
        MerchantProduct product = productMapper.selectOne(new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getId, item.getProductId())
            .eq(MerchantProduct::getTenantId, tenantId)
            .eq(MerchantProduct::getDeleted, 0)
            .last("limit 1"));
        if (product == null) {
            throw new BusinessException("订单商品不存在: " + item.getProductName());
        }
        int currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        if (currentStock < item.getQuantity()) {
            throw new BusinessException("商品库存不足: " + product.getProductName());
        }
        product.setStockQuantity(currentStock - item.getQuantity());
        productMapper.updateById(product);
    }

    private MerchantOrder loadOrder(Long id, Long tenantId) {
        MerchantOrder order = orderMapper.selectOne(new LambdaQueryWrapper<MerchantOrder>()
            .eq(MerchantOrder::getId, id)
            .eq(MerchantOrder::getTenantId, tenantId)
            .eq(MerchantOrder::getDeleted, 0)
            .last("limit 1"));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private MerchantOrderResponse toResponse(MerchantOrder order) {
        MerchantOrderResponse response = new MerchantOrderResponse();
        response.setId(order.getId());
        response.setTenantId(order.getTenantId());
        response.setOrderNo(order.getOrderNo());
        response.setMerchantId(order.getMerchantId());
        response.setMerchantName(order.getMerchantName());
        response.setTargetAppId(order.getTargetAppId());
        response.setTargetAppCode(order.getTargetAppCode());
        response.setTargetTenantId(order.getTargetTenantId());
        response.setTargetProjectName(order.getTargetProjectName());
        response.setTargetSiteName(order.getTargetSiteName());
        response.setTargetUserName(order.getTargetUserName());
        response.setTargetUserPhone(order.getTargetUserPhone());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerPhone(order.getCustomerPhone());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setWarehouseId(order.getWarehouseId());
        response.setWarehouseName(order.getWarehouseName());
        response.setLogisticsCompany(order.getLogisticsCompany());
        response.setTrackingNo(order.getTrackingNo());
        response.setShippedAt(order.getShippedAt());
        response.setShippingRemark(order.getShippingRemark());
        response.setOrderStatus(order.getOrderStatus());
        response.setItemCount(order.getItemCount());
        response.setTotalAmount(order.getTotalAmount());
        response.setRemark(order.getRemark());
        response.setItems(orderItemMapper.selectList(new LambdaQueryWrapper<MerchantOrderItem>()
                .eq(MerchantOrderItem::getOrderId, order.getId())
                .eq(MerchantOrderItem::getDeleted, 0)
                .orderByAsc(MerchantOrderItem::getId))
            .stream()
            .map(this::toItemResponse)
            .toList());
        return response;
    }

    private MerchantOrderItemResponse toItemResponse(MerchantOrderItem item) {
        MerchantOrderItemResponse response = new MerchantOrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setSkuCode(item.getSkuCode());
        response.setUnit(item.getUnit());
        response.setQuantity(item.getQuantity());
        response.setSalePrice(item.getSalePrice());
        response.setLineAmount(item.getLineAmount());
        return response;
    }

    private String generateOrderNo() {
        return "GD" + LocalDateTime.now().format(ORDER_TIME_FORMATTER)
            + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    /**
     * 没有显式指定目标应用时，当前商城默认把交易对象视为 `nexis` 侧对象。
     */
    private String normalizeTargetAppCode(String targetAppCode) {
        String normalized = StringUtils.upperCase(StringUtils.trimToEmpty(targetAppCode));
        String appCode = StringUtils.defaultIfBlank(normalized, "NEXIS");
        if (!StringUtils.equals(appCode, "NEXIS")) {
            throw new BusinessException("targetAppCode 当前仅支持 NEXIS");
        }
        return appCode;
    }

    private Long resolveAppId(String appCode) {
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        return app.getId();
    }

    private void validateTargetTenant(String targetAppCode, Long targetTenantId) {
        if (targetTenantId == null) {
            return;
        }
        TenantRegistryResponse targetTenant = tenantRegistryService.getByAppAndId(targetAppCode, targetTenantId);
        if (targetTenant.getStatus() == null || targetTenant.getStatus() != 1) {
            throw new BusinessException("目标租户不可用");
        }
    }

    /**
     * 兼容旧字段 customerName，逐步向 targetUserName 统一。
     */
    private String resolveTargetUserName(String targetUserName, String customerName) {
        return StringUtils.trimToNull(StringUtils.defaultIfBlank(targetUserName, customerName));
    }

    /**
     * 兼容旧字段 customerPhone，避免接口升级时一次性影响全部调用方。
     */
    private String resolveTargetUserPhone(String targetUserPhone, String customerPhone) {
        return StringUtils.trimToNull(StringUtils.defaultIfBlank(targetUserPhone, customerPhone));
    }
}

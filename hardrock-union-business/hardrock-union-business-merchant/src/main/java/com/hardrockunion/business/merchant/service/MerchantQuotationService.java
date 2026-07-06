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
import com.hardrockunion.business.merchant.domain.entity.MerchantProduct;
import com.hardrockunion.business.merchant.domain.entity.MerchantQuotation;
import com.hardrockunion.business.merchant.domain.entity.MerchantQuotationItem;
import com.hardrockunion.business.merchant.dto.MerchantQuotationCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantQuotationItemCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantQuotationItemResponse;
import com.hardrockunion.business.merchant.dto.MerchantQuotationQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantQuotationResponse;
import com.hardrockunion.business.merchant.dto.MerchantQuotationStatusUpdateRequest;
import com.hardrockunion.business.merchant.mapper.MerchantProductMapper;
import com.hardrockunion.business.merchant.mapper.MerchantQuotationItemMapper;
import com.hardrockunion.business.merchant.mapper.MerchantQuotationMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;
import com.hardrockunion.infrastructure.db.page.PageResponse;

/**
 * 商城报价单服务。
 *
 * <p>这里负责 商户侧的报价单主链路，包括：
 * 报价单创建、按交易目标筛选、详情查询，以及把报价单转换成订单。
 * 报价单面向的交易对象不是本地“客户主数据”，而是后续会逐步对接到 `pmhub` 的项目、工地和联系人。
 */
@Service
public class MerchantQuotationService {

    private static final DateTimeFormatter QUOTATION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MerchantQuotationMapper quotationMapper;
    private final MerchantQuotationItemMapper quotationItemMapper;
    private final MerchantProductMapper productMapper;
    private final MerchantOrderService merchantOrderService;
    private final MerchantAccessGuard merchantAccessGuard;
    private final AppRegistryQueryService appRegistryQueryService;
    private final TenantRegistryService tenantRegistryService;

    public MerchantQuotationService(MerchantQuotationMapper quotationMapper,
                                MerchantQuotationItemMapper quotationItemMapper,
                                MerchantProductMapper productMapper,
                                MerchantOrderService merchantOrderService,
                                MerchantAccessGuard merchantAccessGuard,
                                AppRegistryQueryService appRegistryQueryService,
                                TenantRegistryService tenantRegistryService) {
        this.quotationMapper = quotationMapper;
        this.quotationItemMapper = quotationItemMapper;
        this.productMapper = productMapper;
        this.merchantOrderService = merchantOrderService;
        this.merchantAccessGuard = merchantAccessGuard;
        this.appRegistryQueryService = appRegistryQueryService;
        this.tenantRegistryService = tenantRegistryService;
    }

    /**
     * 按当前登录租户查询报价单列表，并支持按交易目标和计划履约信息做业务筛选。
     */
    public PageResponse<MerchantQuotationResponse> list(MerchantQuotationQueryRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantQuotationQueryRequest query = request == null ? new MerchantQuotationQueryRequest() : request;
        validateQueryRange(query);

        LambdaQueryWrapper<MerchantQuotation> wrapper = new LambdaQueryWrapper<MerchantQuotation>()
            .eq(MerchantQuotation::getTenantId, loginUser.getTenantId())
            .eq(MerchantQuotation::getDeleted, 0);

        if (StringUtils.isNotBlank(query.getQuotationStatus())) {
            wrapper.eq(MerchantQuotation::getQuotationStatus, StringUtils.upperCase(StringUtils.trim(query.getQuotationStatus())));
        }
        if (StringUtils.isNotBlank(query.getQuotationKeyword())) {
            String keyword = StringUtils.trim(query.getQuotationKeyword());
            wrapper.and(w -> w.like(MerchantQuotation::getQuotationNo, keyword)
                .or()
                .like(MerchantQuotation::getTargetUserName, keyword)
                .or()
                .like(MerchantQuotation::getTargetUserPhone, keyword));
        }
        if (query.getTargetTenantId() != null) {
            wrapper.eq(MerchantQuotation::getTargetTenantId, query.getTargetTenantId());
        }
        if (StringUtils.isNotBlank(query.getTargetProjectName())) {
            wrapper.like(MerchantQuotation::getTargetProjectName, StringUtils.trim(query.getTargetProjectName()));
        }
        if (StringUtils.isNotBlank(query.getTargetSiteName())) {
            wrapper.like(MerchantQuotation::getTargetSiteName, StringUtils.trim(query.getTargetSiteName()));
        }
        if (StringUtils.isNotBlank(query.getTargetUserName())) {
            wrapper.like(MerchantQuotation::getTargetUserName, StringUtils.trim(query.getTargetUserName()));
        }
        if (query.getWarehouseId() != null) {
            wrapper.eq(MerchantQuotation::getWarehouseId, query.getWarehouseId());
        }
        if (StringUtils.isNotBlank(query.getWarehouseName())) {
            wrapper.like(MerchantQuotation::getWarehouseName, StringUtils.trim(query.getWarehouseName()));
        }
        if (StringUtils.isNotBlank(query.getLogisticsCompany())) {
            wrapper.like(MerchantQuotation::getLogisticsCompany, StringUtils.trim(query.getLogisticsCompany()));
        }
        if (query.getValidUntilFrom() != null) {
            wrapper.ge(MerchantQuotation::getValidUntil, query.getValidUntilFrom());
        }
        if (query.getValidUntilTo() != null) {
            wrapper.le(MerchantQuotation::getValidUntil, query.getValidUntilTo());
        }
        if (Boolean.TRUE.equals(query.getExpired())) {
            wrapper.isNotNull(MerchantQuotation::getValidUntil)
                .lt(MerchantQuotation::getValidUntil, LocalDateTime.now());
        } else if (Boolean.FALSE.equals(query.getExpired())) {
            wrapper.and(w -> w.isNull(MerchantQuotation::getValidUntil)
                .or()
                .ge(MerchantQuotation::getValidUntil, LocalDateTime.now()));
        }
        if (query.getCreatedFrom() != null) {
            wrapper.ge(MerchantQuotation::getCreatedAt, query.getCreatedFrom());
        }
        if (query.getCreatedTo() != null) {
            wrapper.le(MerchantQuotation::getCreatedAt, query.getCreatedTo());
        }

        Page<MerchantQuotation> page = quotationMapper.selectPage(
            Page.of(query.getPageNum(), query.getPageSize()),
            wrapper.orderByDesc(MerchantQuotation::getId));
        var responsePage = page.convert(this::toResponse);
        return PageResponse.from(responsePage);
    }

    public MerchantQuotationResponse getById(Long id, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantQuotation quotation = quotationMapper.selectOne(new LambdaQueryWrapper<MerchantQuotation>()
            .eq(MerchantQuotation::getId, id)
            .eq(MerchantQuotation::getTenantId, loginUser.getTenantId())
            .eq(MerchantQuotation::getDeleted, 0)
            .last("limit 1"));
        if (quotation == null) {
            throw new BusinessException("报价单不存在");
        }
        return toResponse(quotation);
    }

    /**
     * 推进报价单状态流转。
     *
     * <p>当前先保持最小状态机：
     * ISSUED 可作废为 CANCELLED；
     * CANCELLED 可重新恢复为 ISSUED；
     * CONVERTED 为终态，只能由“转订单”动作推进。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantQuotationResponse updateStatus(Long id, MerchantQuotationStatusUpdateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getTargetStatus())) {
            throw new BusinessException("targetStatus 不能为空");
        }

        MerchantQuotation quotation = loadQuotation(id, loginUser.getTenantId());
        String currentStatus = StringUtils.trimToEmpty(quotation.getQuotationStatus()).toUpperCase();
        String targetStatus = StringUtils.trimToEmpty(request.getTargetStatus()).toUpperCase();

        if (StringUtils.equals(currentStatus, targetStatus)) {
            return toResponse(quotation);
        }
        if (!isAllowedStatusTransition(currentStatus, targetStatus)) {
            throw new BusinessException("报价单状态不允许从 " + currentStatus + " 流转到 " + targetStatus);
        }

        quotation.setQuotationStatus(targetStatus);
        quotationMapper.updateById(quotation);
        return getById(quotation.getId(), loginUser);
    }

    /**
     * 创建报价单。
     *
     * <p>这里会先锁定当前商户租户，再逐条校验报价商品是否合法。
     * 报价阶段只记录报价价格和金额，不绑定真实仓储能力；后续由订单履约自然反推仓储边界。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantQuotationResponse create(MerchantQuotationCreateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null) {
            throw new BusinessException("请求不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("报价明细不能为空");
        }
        String targetAppCode = normalizeTargetAppCode(request.getTargetAppCode());
        String targetUserName = resolveTargetUserName(request.getTargetUserName(), request.getCustomerName());
        String targetUserPhone = resolveTargetUserPhone(request.getTargetUserPhone(), request.getCustomerPhone());
        if (StringUtils.isBlank(targetUserName)) {
            throw new BusinessException("targetUserName 不能为空");
        }

        TenantRegistryResponse merchantTenant = tenantRegistryService.getById(loginUser.getTenantId());
        validateTargetTenant(targetAppCode, request.getTargetTenantId());
        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;

        MerchantQuotation quotation = new MerchantQuotation();
        quotation.setTenantId(loginUser.getTenantId());
        quotation.setQuotationNo(generateQuotationNo());
        quotation.setMerchantId(merchantTenant.getId());
        quotation.setMerchantName(merchantTenant.getTenantName());
        quotation.setTargetAppId(resolveAppId(targetAppCode));
        quotation.setTargetAppCode(targetAppCode);
        quotation.setTargetTenantId(request.getTargetTenantId());
        quotation.setTargetProjectName(StringUtils.trimToNull(request.getTargetProjectName()));
        quotation.setTargetSiteName(StringUtils.trimToNull(request.getTargetSiteName()));
        quotation.setTargetUserName(targetUserName);
        quotation.setTargetUserPhone(targetUserPhone);
        quotation.setCustomerName(targetUserName);
        quotation.setCustomerPhone(targetUserPhone);
        quotation.setWarehouseId(request.getWarehouseId());
        quotation.setLogisticsCompany(StringUtils.trimToNull(request.getLogisticsCompany()));
        quotation.setQuotationStatus("ISSUED");
        quotation.setValidUntil(request.getValidUntil());
        quotation.setRemark(StringUtils.trimToNull(request.getRemark()));
        quotation.setCreatedBy(loginUser.getUserId());
        quotation.setDeleted(0);
        quotation.setItemCount(0);
        quotation.setTotalAmount(BigDecimal.ZERO);
        quotationMapper.insert(quotation);

        for (MerchantQuotationItemCreateRequest itemRequest : request.getItems()) {
            if (itemRequest == null || itemRequest.getProductId() == null || itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new BusinessException("报价明细缺少 productId 或 quantity 非法");
            }
            MerchantProduct product = productMapper.selectOne(new LambdaQueryWrapper<MerchantProduct>()
                .eq(MerchantProduct::getId, itemRequest.getProductId())
                .eq(MerchantProduct::getTenantId, loginUser.getTenantId())
                .eq(MerchantProduct::getDeleted, 0)
                .eq(MerchantProduct::getStatus, 1)
                .last("limit 1"));
            if (product == null) {
                throw new BusinessException("存在不可报价商品");
            }

            BigDecimal quotationPrice = itemRequest.getQuotationPrice() == null
                ? (product.getSalePrice() == null ? BigDecimal.ZERO : product.getSalePrice())
                : itemRequest.getQuotationPrice();
            if (quotationPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("quotationPrice 不能小于 0");
            }
            BigDecimal lineAmount = quotationPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            MerchantQuotationItem item = new MerchantQuotationItem();
            item.setTenantId(loginUser.getTenantId());
            item.setQuotationId(quotation.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getProductName());
            item.setSkuCode(product.getSkuCode());
            item.setUnit(product.getUnit());
            item.setQuantity(itemRequest.getQuantity());
            item.setQuotationPrice(quotationPrice);
            item.setLineAmount(lineAmount);
            item.setDeleted(0);
            quotationItemMapper.insert(item);

            itemCount += itemRequest.getQuantity();
            totalAmount = totalAmount.add(lineAmount);
        }

        quotation.setItemCount(itemCount);
        quotation.setTotalAmount(totalAmount);
        quotationMapper.updateById(quotation);
        return getById(quotation.getId(), loginUser);
    }

    /**
     * 把已签发且未过期的报价单转换成订单。
     *
     * <p>转换时会沿用报价单上的交易目标信息和报价价格，避免转单后又回退成商品当前销售价。
     */
    @Transactional(rollbackFor = Exception.class)
    public com.hardrockunion.business.merchant.dto.MerchantOrderResponse convertToOrder(Long id, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantQuotation quotation = loadQuotation(id, loginUser.getTenantId());
        if (!StringUtils.equals("ISSUED", StringUtils.upperCase(StringUtils.trimToEmpty(quotation.getQuotationStatus())))) {
            throw new BusinessException("当前报价单状态不允许转订单");
        }
        if (quotation.getValidUntil() != null && quotation.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new BusinessException("报价单已过期，无法转订单");
        }

        List<MerchantQuotationItem> items = quotationItemMapper.selectList(new LambdaQueryWrapper<MerchantQuotationItem>()
            .eq(MerchantQuotationItem::getQuotationId, quotation.getId())
            .eq(MerchantQuotationItem::getDeleted, 0)
            .orderByAsc(MerchantQuotationItem::getId));
        if (items.isEmpty()) {
            throw new BusinessException("报价单明细为空，无法转订单");
        }

        com.hardrockunion.business.merchant.dto.MerchantOrderResponse order = merchantOrderService.createFromQuotation(quotation, items, loginUser);
        quotation.setQuotationStatus("CONVERTED");
        quotationMapper.updateById(quotation);
        return order;
    }

    private boolean isAllowedStatusTransition(String currentStatus, String targetStatus) {
        return switch (currentStatus) {
            case "ISSUED" -> "CANCELLED".equals(targetStatus);
            case "CANCELLED" -> "ISSUED".equals(targetStatus);
            default -> false;
        };
    }

    private MerchantQuotation loadQuotation(Long id, Long tenantId) {
        MerchantQuotation quotation = quotationMapper.selectOne(new LambdaQueryWrapper<MerchantQuotation>()
            .eq(MerchantQuotation::getId, id)
            .eq(MerchantQuotation::getTenantId, tenantId)
            .eq(MerchantQuotation::getDeleted, 0)
            .last("limit 1"));
        if (quotation == null) {
            throw new BusinessException("报价单不存在");
        }
        return quotation;
    }

    private MerchantQuotationResponse toResponse(MerchantQuotation quotation) {
        MerchantQuotationResponse response = new MerchantQuotationResponse();
        response.setId(quotation.getId());
        response.setTenantId(quotation.getTenantId());
        response.setQuotationNo(quotation.getQuotationNo());
        response.setMerchantId(quotation.getMerchantId());
        response.setMerchantName(quotation.getMerchantName());
        response.setTargetAppId(quotation.getTargetAppId());
        response.setTargetAppCode(quotation.getTargetAppCode());
        response.setTargetTenantId(quotation.getTargetTenantId());
        response.setTargetProjectName(quotation.getTargetProjectName());
        response.setTargetSiteName(quotation.getTargetSiteName());
        response.setTargetUserName(quotation.getTargetUserName());
        response.setTargetUserPhone(quotation.getTargetUserPhone());
        response.setCustomerName(quotation.getCustomerName());
        response.setCustomerPhone(quotation.getCustomerPhone());
        response.setWarehouseId(quotation.getWarehouseId());
        response.setWarehouseName(quotation.getWarehouseName());
        response.setLogisticsCompany(quotation.getLogisticsCompany());
        response.setQuotationStatus(quotation.getQuotationStatus());
        response.setValidUntil(quotation.getValidUntil());
        response.setItemCount(quotation.getItemCount());
        response.setTotalAmount(quotation.getTotalAmount());
        response.setRemark(quotation.getRemark());
        response.setItems(quotationItemMapper.selectList(new LambdaQueryWrapper<MerchantQuotationItem>()
                .eq(MerchantQuotationItem::getQuotationId, quotation.getId())
                .eq(MerchantQuotationItem::getDeleted, 0)
                .orderByAsc(MerchantQuotationItem::getId))
            .stream()
            .map(this::toItemResponse)
            .toList());
        return response;
    }

    private MerchantQuotationItemResponse toItemResponse(MerchantQuotationItem item) {
        MerchantQuotationItemResponse response = new MerchantQuotationItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setSkuCode(item.getSkuCode());
        response.setUnit(item.getUnit());
        response.setQuantity(item.getQuantity());
        response.setQuotationPrice(item.getQuotationPrice());
        response.setLineAmount(item.getLineAmount());
        return response;
    }

    private String generateQuotationNo() {
        return "BJ" + LocalDateTime.now().format(QUOTATION_TIME_FORMATTER)
            + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    /**
     * 列表查询允许按时间区间筛选，但要保证开始时间不晚于结束时间。
     */
    private void validateQueryRange(MerchantQuotationQueryRequest query) {
        if (query.getCreatedFrom() != null && query.getCreatedTo() != null
            && query.getCreatedFrom().isAfter(query.getCreatedTo())) {
            throw new BusinessException("createdFrom 不能晚于 createdTo");
        }
        if (query.getValidUntilFrom() != null && query.getValidUntilTo() != null
            && query.getValidUntilFrom().isAfter(query.getValidUntilTo())) {
            throw new BusinessException("validUntilFrom 不能晚于 validUntilTo");
        }
    }

    private String normalizeTargetAppCode(String targetAppCode) {
        String normalized = StringUtils.upperCase(StringUtils.trimToEmpty(targetAppCode));
        String appCode = StringUtils.defaultIfBlank(normalized, "PMHUB");
        if (!StringUtils.equals(appCode, "PMHUB")) {
            throw new BusinessException("targetAppCode 当前仅支持 PMHUB");
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
     * 兼容旧入参里的 customerName，逐步把单据交易对象统一收口到 targetUserName。
     */
    private String resolveTargetUserName(String targetUserName, String customerName) {
        return StringUtils.trimToNull(StringUtils.defaultIfBlank(targetUserName, customerName));
    }

    /**
     * 兼容旧入参里的 customerPhone，避免接口演进期间前端一次性大改。
     */
    private String resolveTargetUserPhone(String targetUserPhone, String customerPhone) {
        return StringUtils.trimToNull(StringUtils.defaultIfBlank(targetUserPhone, customerPhone));
    }
}

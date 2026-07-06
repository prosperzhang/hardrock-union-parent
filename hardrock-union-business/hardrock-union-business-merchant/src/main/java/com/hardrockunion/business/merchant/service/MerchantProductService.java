package com.hardrockunion.business.merchant.service;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.business.merchant.domain.entity.MerchantProduct;
import com.hardrockunion.business.merchant.domain.entity.MerchantProductRegionPrice;
import com.hardrockunion.business.merchant.dto.MerchantProductMarketplaceQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductCreateRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductQueryRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductRegionPriceResponse;
import com.hardrockunion.business.merchant.dto.MerchantProductRegionPriceUpsertRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductResponse;
import com.hardrockunion.business.merchant.dto.MerchantProductStockAdjustRequest;
import com.hardrockunion.business.merchant.dto.MerchantProductUpdateRequest;
import com.hardrockunion.business.merchant.mapper.MerchantProductMapper;
import com.hardrockunion.business.merchant.mapper.MerchantProductRegionPriceMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;

import jakarta.annotation.PostConstruct;

@Service
public class MerchantProductService {

    private static final String PRIMELOAD_MARKETPLACE_APP_CODE = "PRIMELOAD-MARKETPLACE";
    private static final String PRIMELOAD_SELF_OPERATED = "PRIMELOAD_SELF_OPERATED";
    private static final String MERCHANT = "MERCHANT";
    private static final String SELF_OPERATED_MERCHANT = "SELF_OPERATED_MERCHANT";

    private final MerchantProductMapper productMapper;
    private final MerchantProductRegionPriceMapper regionPriceMapper;
    private final MerchantCategoryService merchantCategoryService;
    private final MerchantAccessGuard merchantAccessGuard;
    private final TenantRegistryService tenantRegistryService;
    private final JdbcTemplate jdbcTemplate;

    public MerchantProductService(MerchantProductMapper productMapper,
                              MerchantProductRegionPriceMapper regionPriceMapper,
                              MerchantCategoryService merchantCategoryService,
                              MerchantAccessGuard merchantAccessGuard,
                              TenantRegistryService tenantRegistryService,
                              JdbcTemplate jdbcTemplate) {
        this.productMapper = productMapper;
        this.regionPriceMapper = regionPriceMapper;
        this.merchantCategoryService = merchantCategoryService;
        this.merchantAccessGuard = merchantAccessGuard;
        this.tenantRegistryService = tenantRegistryService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initSchema() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS merchant_product_region_price (
                id BIGINT PRIMARY KEY COMMENT '主键',
                tenant_id BIGINT NOT NULL COMMENT '商户租户ID',
                product_id BIGINT NOT NULL COMMENT '商品ID',
                region_level TINYINT NOT NULL COMMENT '区域级别 1省 2市 3区县',
                region_code VARCHAR(32) NOT NULL COMMENT '生效行政区编码',
                region_name VARCHAR(64) NOT NULL COMMENT '生效行政区名称',
                province_code VARCHAR(32) DEFAULT NULL COMMENT '省级行政区编码',
                province_name VARCHAR(64) DEFAULT NULL COMMENT '省级行政区名称',
                city_code VARCHAR(32) DEFAULT NULL COMMENT '市级行政区编码',
                city_name VARCHAR(64) DEFAULT NULL COMMENT '市级行政区名称',
                district_code VARCHAR(32) DEFAULT NULL COMMENT '区县行政区编码',
                district_name VARCHAR(64) DEFAULT NULL COMMENT '区县行政区名称',
                sale_price DECIMAL(18,2) NOT NULL COMMENT '区域销售价',
                status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
                deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
                created_by BIGINT DEFAULT NULL COMMENT '创建人',
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                UNIQUE KEY uk_merchant_product_region_price_scope (tenant_id, product_id, region_level, region_code),
                KEY idx_merchant_product_region_price_product (product_id),
                KEY idx_merchant_product_region_price_region (region_level, region_code)
            ) COMMENT='商户商品区域价表'
            """);
    }

    public PageResponse<MerchantProductResponse> list(MerchantProductQueryRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantProductQueryRequest query = request == null ? new MerchantProductQueryRequest() : request;
        LambdaQueryWrapper<MerchantProduct> wrapper = new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getTenantId, loginUser.getTenantId())
            .eq(MerchantProduct::getDeleted, 0);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            wrapper.and(w -> w.like(MerchantProduct::getProductName, keyword)
                .or()
                .like(MerchantProduct::getSkuCode, keyword)
                .or()
                .like(MerchantProduct::getBrandName, keyword)
                .or()
                .like(MerchantProduct::getSpecModel, keyword));
        }
        if (StringUtils.isNotBlank(query.getCategoryCode())) {
            wrapper.eq(MerchantProduct::getCategoryCode, StringUtils.upperCase(StringUtils.trim(query.getCategoryCode())));
        }
        if (query.getStatus() != null) {
            ensureStatus(query.getStatus());
            wrapper.eq(MerchantProduct::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(MerchantProduct::getId);
        Page<MerchantProduct> page = productMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        var responsePage = page.convert(this::toResponse);
        return PageResponse.from(responsePage);
    }

    public MerchantProductResponse getById(Long id, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantProduct product = productMapper.selectOne(new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getId, id)
            .eq(MerchantProduct::getTenantId, loginUser.getTenantId())
            .eq(MerchantProduct::getDeleted, 0)
            .last("limit 1"));
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return toResponse(product);
    }

    public PageResponse<MerchantProductResponse> listMarketplace(MerchantProductMarketplaceQueryRequest request,
                                                                 LoginUser loginUser) {
        TenantRegistryResponse targetTenant = ensureTargetTenant(loginUser);
        MerchantProductMarketplaceQueryRequest query = request == null ? new MerchantProductMarketplaceQueryRequest() : request;
        Long merchantTenantId = resolveMarketplaceMerchantTenantId(query.getMerchantTenantId());

        LambdaQueryWrapper<MerchantProduct> wrapper = new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getTenantId, merchantTenantId)
            .eq(MerchantProduct::getStatus, 1)
            .eq(MerchantProduct::getDeleted, 0);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            wrapper.and(w -> w.like(MerchantProduct::getProductName, keyword)
                .or()
                .like(MerchantProduct::getSkuCode, keyword)
                .or()
                .like(MerchantProduct::getBrandName, keyword)
                .or()
                .like(MerchantProduct::getSpecModel, keyword));
        }
        if (StringUtils.isNotBlank(query.getCategoryCode())) {
            wrapper.eq(MerchantProduct::getCategoryCode, StringUtils.upperCase(StringUtils.trim(query.getCategoryCode())));
        }
        wrapper.orderByDesc(MerchantProduct::getId);
        Page<MerchantProduct> page = productMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        var responsePage = page.convert(product -> toMarketplaceResponse(product, targetTenant));
        return PageResponse.from(responsePage);
    }

    public MerchantProductResponse create(MerchantProductCreateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getProductName())) {
            throw new BusinessException("productName 不能为空");
        }

        MerchantProduct product = new MerchantProduct();
        product.setTenantId(loginUser.getTenantId());
        product.setProductName(normalizeAndEnsureProductName(loginUser.getTenantId(), request.getProductName(), null));
        product.setCategoryCode(normalizeAndEnsureCategory(loginUser.getTenantId(), request.getCategoryCode()));
        product.setSkuCode(normalizeAndEnsureSku(loginUser.getTenantId(), request.getSkuCode(), null));
        product.setMainImageUrl(StringUtils.trimToNull(request.getMainImageUrl()));
        product.setBrandName(StringUtils.trimToNull(request.getBrandName()));
        product.setSpecModel(StringUtils.trimToNull(request.getSpecModel()));
        product.setMaterial(StringUtils.trimToNull(request.getMaterial()));
        product.setProductDescription(StringUtils.trimToNull(request.getProductDescription()));
        product.setUnit(StringUtils.defaultIfBlank(StringUtils.trimToNull(request.getUnit()), "件"));
        product.setSalePrice(normalizeSalePrice(request.getSalePrice()));
        product.setStockQuantity(normalizeInitialStockQuantity(request.getStockQuantity()));
        product.setStatus(1);
        product.setDeleted(0);
        product.setCreatedBy(loginUser.getUserId());
        productMapper.insert(product);
        return getById(product.getId(), loginUser);
    }

    public MerchantProductResponse update(Long id, MerchantProductUpdateRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getProductName())) {
            throw new BusinessException("productName 不能为空");
        }

        MerchantProduct product = loadEntity(id, loginUser.getTenantId());
        product.setProductName(normalizeAndEnsureProductName(loginUser.getTenantId(), request.getProductName(), product.getId()));
        product.setCategoryCode(normalizeAndEnsureCategory(loginUser.getTenantId(), request.getCategoryCode()));
        product.setSkuCode(normalizeAndEnsureSku(loginUser.getTenantId(), request.getSkuCode(), product.getId()));
        product.setMainImageUrl(StringUtils.trimToNull(request.getMainImageUrl()));
        product.setBrandName(StringUtils.trimToNull(request.getBrandName()));
        product.setSpecModel(StringUtils.trimToNull(request.getSpecModel()));
        product.setMaterial(StringUtils.trimToNull(request.getMaterial()));
        product.setProductDescription(StringUtils.trimToNull(request.getProductDescription()));
        product.setUnit(StringUtils.defaultIfBlank(StringUtils.trimToNull(request.getUnit()), "件"));
        product.setSalePrice(normalizeSalePrice(request.getSalePrice()));
        if (request.getStatus() != null) {
            ensureStatus(request.getStatus());
            product.setStatus(request.getStatus());
        }
        productMapper.updateById(product);
        return getById(product.getId(), loginUser);
    }

    /**
     * 手工调整库存。
     *
     * <p>这里用于日常补货、盘亏等轻量库存维护。完整库存流水后续从 warehouse 阶段再抽出。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantProductResponse adjustStock(Long id, MerchantProductStockAdjustRequest request, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null || StringUtils.isBlank(request.getAdjustType()) || request.getQuantity() == null) {
            throw new BusinessException("adjustType、quantity 不能为空");
        }
        if (request.getQuantity() <= 0) {
            throw new BusinessException("quantity 必须大于 0");
        }

        MerchantProduct product = loadEntity(id, loginUser.getTenantId());
        String adjustType = StringUtils.upperCase(StringUtils.trim(request.getAdjustType()));
        int beforeQuantity = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int afterQuantity;

        switch (adjustType) {
            case "IN" -> {
                afterQuantity = beforeQuantity + request.getQuantity();
            }
            case "OUT" -> {
                if (beforeQuantity < request.getQuantity()) {
                    throw new BusinessException("库存不足，无法手工出库");
                }
                afterQuantity = beforeQuantity - request.getQuantity();
            }
            default -> throw new BusinessException("adjustType 仅支持 IN 或 OUT");
        }

        product.setStockQuantity(afterQuantity);
        productMapper.updateById(product);
        return getById(product.getId(), loginUser);
    }

    public void remove(Long id, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantProduct product = loadEntity(id, loginUser.getTenantId());
        productMapper.deleteById(product.getId());
    }

    public List<MerchantProductRegionPriceResponse> listRegionPrices(Long productId, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantProduct product = loadEntity(productId, loginUser.getTenantId());
        return regionPriceMapper.selectList(new LambdaQueryWrapper<MerchantProductRegionPrice>()
                .eq(MerchantProductRegionPrice::getTenantId, product.getTenantId())
                .eq(MerchantProductRegionPrice::getProductId, product.getId())
                .eq(MerchantProductRegionPrice::getDeleted, 0)
                .orderByAsc(MerchantProductRegionPrice::getRegionLevel)
                .orderByAsc(MerchantProductRegionPrice::getRegionCode))
            .stream()
            .map(this::toRegionPriceResponse)
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public MerchantProductRegionPriceResponse upsertRegionPrice(Long productId,
                                                                MerchantProductRegionPriceUpsertRequest request,
                                                                LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        if (request == null || request.getSalePrice() == null) {
            throw new BusinessException("salePrice 不能为空");
        }
        if (request.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("salePrice 不能小于 0");
        }
        MerchantProduct product = loadEntity(productId, loginUser.getTenantId());
        Integer regionLevel = normalizeRegionLevel(request);
        String regionCode = resolveRegionCode(regionLevel, request);
        String regionName = resolveRegionName(regionLevel, request);
        if (StringUtils.isAnyBlank(regionCode, regionName)) {
            throw new BusinessException("区域编码和名称不能为空");
        }
        Integer status = request.getStatus() == null ? 1 : request.getStatus();
        ensureStatus(status);

        MerchantProductRegionPrice regionPrice = regionPriceMapper.selectOne(new LambdaQueryWrapper<MerchantProductRegionPrice>()
            .eq(MerchantProductRegionPrice::getTenantId, product.getTenantId())
            .eq(MerchantProductRegionPrice::getProductId, product.getId())
            .eq(MerchantProductRegionPrice::getRegionLevel, regionLevel)
            .eq(MerchantProductRegionPrice::getRegionCode, regionCode)
            .eq(MerchantProductRegionPrice::getDeleted, 0)
            .last("limit 1"));
        boolean isNew = regionPrice == null;
        if (isNew) {
            regionPrice = new MerchantProductRegionPrice();
            regionPrice.setTenantId(product.getTenantId());
            regionPrice.setProductId(product.getId());
            regionPrice.setRegionLevel(regionLevel);
            regionPrice.setRegionCode(regionCode);
            regionPrice.setCreatedBy(loginUser.getUserId());
            regionPrice.setDeleted(0);
        }
        regionPrice.setRegionName(regionName);
        regionPrice.setProvinceCode(StringUtils.trimToNull(request.getProvinceCode()));
        regionPrice.setProvinceName(StringUtils.trimToNull(request.getProvinceName()));
        regionPrice.setCityCode(StringUtils.trimToNull(request.getCityCode()));
        regionPrice.setCityName(StringUtils.trimToNull(request.getCityName()));
        regionPrice.setDistrictCode(StringUtils.trimToNull(request.getDistrictCode()));
        regionPrice.setDistrictName(StringUtils.trimToNull(request.getDistrictName()));
        regionPrice.setSalePrice(request.getSalePrice());
        regionPrice.setStatus(status);
        if (isNew) {
            regionPriceMapper.insert(regionPrice);
        } else {
            regionPriceMapper.updateById(regionPrice);
        }
        return toRegionPriceResponse(regionPrice);
    }

    public void removeRegionPrice(Long productId, Long priceId, LoginUser loginUser) {
        merchantAccessGuard.ensureLogin(loginUser);
        MerchantProduct product = loadEntity(productId, loginUser.getTenantId());
        MerchantProductRegionPrice regionPrice = regionPriceMapper.selectOne(new LambdaQueryWrapper<MerchantProductRegionPrice>()
            .eq(MerchantProductRegionPrice::getId, priceId)
            .eq(MerchantProductRegionPrice::getTenantId, product.getTenantId())
            .eq(MerchantProductRegionPrice::getProductId, product.getId())
            .eq(MerchantProductRegionPrice::getDeleted, 0)
            .last("limit 1"));
        if (regionPrice == null) {
            throw new BusinessException("区域价不存在");
        }
        regionPriceMapper.deleteById(regionPrice.getId());
    }

    private MerchantProduct loadEntity(Long id, Long tenantId) {
        MerchantProduct product = productMapper.selectOne(new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getId, id)
            .eq(MerchantProduct::getTenantId, tenantId)
            .eq(MerchantProduct::getDeleted, 0)
            .last("limit 1"));
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    private String normalizeAndEnsureCategory(Long tenantId, String categoryCode) {
        String normalizedCategoryCode = StringUtils.upperCase(StringUtils.trimToNull(categoryCode));
        if (normalizedCategoryCode == null) {
            throw new BusinessException("categoryCode 不能为空");
        }
        if (merchantCategoryService.findEnabledByCode(tenantId, normalizedCategoryCode) == null) {
            throw new BusinessException("商品分类不存在或已停用");
        }
        return normalizedCategoryCode;
    }

    private String normalizeAndEnsureProductName(Long tenantId, String productName, Long currentProductId) {
        String normalizedProductName = StringUtils.trimToNull(productName);
        if (normalizedProductName == null) {
            throw new BusinessException("productName 不能为空");
        }
        LambdaQueryWrapper<MerchantProduct> wrapper = new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getTenantId, tenantId)
            .eq(MerchantProduct::getProductName, normalizedProductName)
            .eq(MerchantProduct::getDeleted, 0)
            .last("limit 1");
        if (currentProductId != null) {
            wrapper.ne(MerchantProduct::getId, currentProductId);
        }
        MerchantProduct existed = productMapper.selectOne(wrapper);
        if (existed != null) {
            throw new BusinessException("商品名称已存在");
        }
        return normalizedProductName;
    }

    private String normalizeAndEnsureSku(Long tenantId, String skuCode, Long currentProductId) {
        String normalizedSkuCode = StringUtils.upperCase(StringUtils.trimToNull(skuCode));
        if (normalizedSkuCode == null) {
            return null;
        }
        LambdaQueryWrapper<MerchantProduct> wrapper = new LambdaQueryWrapper<MerchantProduct>()
            .eq(MerchantProduct::getTenantId, tenantId)
            .eq(MerchantProduct::getSkuCode, normalizedSkuCode)
            .eq(MerchantProduct::getDeleted, 0)
            .last("limit 1");
        if (currentProductId != null) {
            wrapper.ne(MerchantProduct::getId, currentProductId);
        }
        MerchantProduct existed = productMapper.selectOne(wrapper);
        if (existed != null) {
            throw new BusinessException("SKU编码已存在");
        }
        return normalizedSkuCode;
    }

    private BigDecimal normalizeSalePrice(BigDecimal salePrice) {
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("salePrice 不能小于 0");
        }
        return salePrice;
    }

    private Integer normalizeInitialStockQuantity(Integer stockQuantity) {
        if (stockQuantity == null) {
            return 0;
        }
        if (stockQuantity < 0) {
            throw new BusinessException("stockQuantity 不能小于 0");
        }
        return stockQuantity;
    }

    private void ensureStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("status 仅支持 0 或 1");
        }
    }

    private TenantRegistryResponse ensureTargetTenant(LoginUser loginUser) {
        if (loginUser == null || StringUtils.isBlank(loginUser.getAppCode()) || loginUser.getTenantId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        return tenantRegistryService.getByAppAndId(loginUser.getAppCode(), loginUser.getTenantId());
    }

    private Long resolveMarketplaceMerchantTenantId(Long merchantTenantId) {
        if (merchantTenantId != null) {
            TenantRegistryResponse tenant = tenantRegistryService.getByAppAndId(PRIMELOAD_MARKETPLACE_APP_CODE, merchantTenantId);
            if (!StringUtils.equalsIgnoreCase(MERCHANT, tenant.getTenantType())
                && !StringUtils.equalsIgnoreCase(SELF_OPERATED_MERCHANT, tenant.getTenantType())) {
                throw new BusinessException("商城租户不是经营主体");
            }
            return tenant.getId();
        }
        TenantRegistryResponse selfOperated = tenantRegistryService.findFirstByTenantSource(
            PRIMELOAD_MARKETPLACE_APP_CODE, PRIMELOAD_SELF_OPERATED);
        if (selfOperated == null) {
            throw new BusinessException("好料自营租户不存在");
        }
        return selfOperated.getId();
    }

    private Integer normalizeRegionLevel(MerchantProductRegionPriceUpsertRequest request) {
        Integer regionLevel = request.getRegionLevel();
        if (regionLevel == null) {
            if (StringUtils.isNotBlank(request.getDistrictCode())) {
                regionLevel = 3;
            } else if (StringUtils.isNotBlank(request.getCityCode())) {
                regionLevel = 2;
            } else if (StringUtils.isNotBlank(request.getProvinceCode())) {
                regionLevel = 1;
            }
        }
        if (regionLevel == null || regionLevel < 1 || regionLevel > 3) {
            throw new BusinessException("regionLevel 仅支持 1省、2市、3区县");
        }
        return regionLevel;
    }

    private String resolveRegionCode(Integer regionLevel, MerchantProductRegionPriceUpsertRequest request) {
        return switch (regionLevel) {
            case 1 -> StringUtils.trimToNull(request.getProvinceCode());
            case 2 -> StringUtils.trimToNull(request.getCityCode());
            case 3 -> StringUtils.trimToNull(request.getDistrictCode());
            default -> null;
        };
    }

    private String resolveRegionName(Integer regionLevel, MerchantProductRegionPriceUpsertRequest request) {
        return switch (regionLevel) {
            case 1 -> StringUtils.trimToNull(request.getProvinceName());
            case 2 -> StringUtils.trimToNull(request.getCityName());
            case 3 -> StringUtils.trimToNull(request.getDistrictName());
            default -> null;
        };
    }

    private MerchantProductResponse toResponse(MerchantProduct product) {
        MerchantProductResponse response = new MerchantProductResponse();
        response.setId(product.getId());
        response.setTenantId(product.getTenantId());
        response.setProductName(product.getProductName());
        response.setCategoryCode(product.getCategoryCode());
        response.setSkuCode(product.getSkuCode());
        response.setMainImageUrl(product.getMainImageUrl());
        response.setBrandName(product.getBrandName());
        response.setSpecModel(product.getSpecModel());
        response.setMaterial(product.getMaterial());
        response.setProductDescription(product.getProductDescription());
        response.setUnit(product.getUnit());
        response.setSalePrice(product.getSalePrice());
        response.setEffectiveSalePrice(product.getSalePrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setStatus(product.getStatus());
        return response;
    }

    private MerchantProductResponse toMarketplaceResponse(MerchantProduct product, TenantRegistryResponse targetTenant) {
        MerchantProductResponse response = toResponse(product);
        MerchantProductRegionPrice regionPrice = findEffectiveRegionPrice(product, targetTenant);
        if (regionPrice != null) {
            response.setEffectiveSalePrice(regionPrice.getSalePrice());
            response.setPriceRegionLevel(regionPrice.getRegionLevel());
            response.setPriceRegionCode(regionPrice.getRegionCode());
            response.setPriceRegionName(regionPrice.getRegionName());
        }
        return response;
    }

    private MerchantProductRegionPrice findEffectiveRegionPrice(MerchantProduct product, TenantRegistryResponse targetTenant) {
        MerchantProductRegionPrice districtPrice = findEnabledRegionPrice(product, 3, targetTenant.getDistrictCode());
        if (districtPrice != null) {
            return districtPrice;
        }
        MerchantProductRegionPrice cityPrice = findEnabledRegionPrice(product, 2, targetTenant.getCityCode());
        if (cityPrice != null) {
            return cityPrice;
        }
        return findEnabledRegionPrice(product, 1, targetTenant.getProvinceCode());
    }

    private MerchantProductRegionPrice findEnabledRegionPrice(MerchantProduct product, Integer regionLevel, String regionCode) {
        String normalizedRegionCode = StringUtils.trimToNull(regionCode);
        if (normalizedRegionCode == null) {
            return null;
        }
        return regionPriceMapper.selectOne(new LambdaQueryWrapper<MerchantProductRegionPrice>()
            .eq(MerchantProductRegionPrice::getTenantId, product.getTenantId())
            .eq(MerchantProductRegionPrice::getProductId, product.getId())
            .eq(MerchantProductRegionPrice::getRegionLevel, regionLevel)
            .eq(MerchantProductRegionPrice::getRegionCode, normalizedRegionCode)
            .eq(MerchantProductRegionPrice::getStatus, 1)
            .eq(MerchantProductRegionPrice::getDeleted, 0)
            .last("limit 1"));
    }

    private MerchantProductRegionPriceResponse toRegionPriceResponse(MerchantProductRegionPrice regionPrice) {
        MerchantProductRegionPriceResponse response = new MerchantProductRegionPriceResponse();
        response.setId(regionPrice.getId());
        response.setTenantId(regionPrice.getTenantId());
        response.setProductId(regionPrice.getProductId());
        response.setRegionLevel(regionPrice.getRegionLevel());
        response.setRegionCode(regionPrice.getRegionCode());
        response.setRegionName(regionPrice.getRegionName());
        response.setProvinceCode(regionPrice.getProvinceCode());
        response.setProvinceName(regionPrice.getProvinceName());
        response.setCityCode(regionPrice.getCityCode());
        response.setCityName(regionPrice.getCityName());
        response.setDistrictCode(regionPrice.getDistrictCode());
        response.setDistrictName(regionPrice.getDistrictName());
        response.setSalePrice(regionPrice.getSalePrice());
        response.setStatus(regionPrice.getStatus());
        return response;
    }
}

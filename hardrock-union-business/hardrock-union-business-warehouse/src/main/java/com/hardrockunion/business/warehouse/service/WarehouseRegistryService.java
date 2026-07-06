package com.hardrockunion.business.warehouse.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.business.warehouse.domain.entity.Warehouse;
import com.hardrockunion.business.warehouse.dto.WarehouseCreateRequest;
import com.hardrockunion.business.warehouse.dto.WarehouseResponse;
import com.hardrockunion.business.warehouse.mapper.WarehouseMapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.service.IamRoleQueryService;
import com.hardrockunion.platform.tenant.event.TenantCreatedEvent;

import jakarta.annotation.PostConstruct;

@Service
public class WarehouseRegistryService {

    private static final String PROJECT = "PROJECT";
    private static final String MERCHANT = "MERCHANT";
    private static final String SELF_OPERATED_MERCHANT = "SELF_OPERATED_MERCHANT";
    private static final String PRIMELOAD_SELF_OPERATED = "PRIMELOAD_SELF_OPERATED";
    private static final String WSGM = "WSGM";
    private static final String DEFAULT_CODE = "DEFAULT";

    private final WarehouseMapper warehouseMapper;
    private final JdbcTemplate jdbcTemplate;
    private final IamRoleQueryService iamRoleQueryService;

    public WarehouseRegistryService(WarehouseMapper warehouseMapper,
                                    JdbcTemplate jdbcTemplate,
                                    IamRoleQueryService iamRoleQueryService) {
        this.warehouseMapper = warehouseMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.iamRoleQueryService = iamRoleQueryService;
    }

    @PostConstruct
    public void init() {
        ensureRegionColumns();
    }

    @EventListener
    public void handleTenantCreated(TenantCreatedEvent event) {
        createDefaultWarehouse(
            event.appCode(),
            event.tenantType(),
            event.tenantId(),
            event.tenantName(),
            event.tenantSource(),
            event.projectAddress(),
            event.provinceCode(),
            event.provinceName(),
            event.cityCode(),
            event.cityName(),
            event.districtCode(),
            event.districtName(),
            event.managerName(),
            event.managerPhone(),
            event.createdByAppCode(),
            event.createdBy()
        );
    }

    public WarehouseResponse createDefaultWarehouse(String appCode,
                                                    String tenantType,
                                                    Long tenantId,
                                                    String tenantName,
                                                    String tenantSource,
                                                    String address,
                                                    String provinceCode,
                                                    String provinceName,
                                                    String cityCode,
                                                    String cityName,
                                                    String districtCode,
                                                    String districtName,
                                                    String contactName,
                                                    String contactPhone,
                                                    String createdByAppCode,
                                                    Long createdBy) {
        if (tenantId == null || StringUtils.isAnyBlank(appCode, tenantType)) {
            throw new BusinessException("默认仓库初始化参数不完整");
        }
        String normalizedAppCode = StringUtils.upperCase(StringUtils.trim(appCode));
        String normalizedTenantType = StringUtils.upperCase(StringUtils.trim(tenantType));
        if (isSelfOperatedMerchant(normalizedTenantType, tenantSource)) {
            return null;
        }
        if (!shouldCreateDefaultWarehouse(normalizedTenantType)) {
            return null;
        }
        Warehouse existed = warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>()
            .eq(Warehouse::getAppCode, normalizedAppCode)
            .eq(Warehouse::getTenantId, tenantId)
            .eq(Warehouse::getDefaultFlag, 1)
            .eq(Warehouse::getDeleted, 0)
            .last("limit 1"));
        if (existed != null) {
            return toResponse(existed);
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setAppCode(normalizedAppCode);
        warehouse.setTenantId(tenantId);
        warehouse.setWarehouseCode(DEFAULT_CODE);
        warehouse.setWarehouseName(defaultWarehouseName(normalizedTenantType, tenantName));
        warehouse.setWarehouseType(defaultWarehouseType(normalizedTenantType));
        warehouse.setDefaultFlag(1);
        warehouse.setOwnerType(normalizedTenantType);
        warehouse.setOwnerAppCode(normalizedAppCode);
        warehouse.setOwnerTenantId(tenantId);
        warehouse.setContactName(StringUtils.trimToNull(contactName));
        warehouse.setContactPhone(StringUtils.trimToNull(contactPhone));
        warehouse.setWarehouseAddress(StringUtils.trimToNull(address));
        warehouse.setProvinceCode(StringUtils.trimToNull(provinceCode));
        warehouse.setProvinceName(StringUtils.trimToNull(provinceName));
        warehouse.setCityCode(StringUtils.trimToNull(cityCode));
        warehouse.setCityName(StringUtils.trimToNull(cityName));
        warehouse.setDistrictCode(StringUtils.trimToNull(districtCode));
        warehouse.setDistrictName(StringUtils.trimToNull(districtName));
        warehouse.setStatus(1);
        warehouse.setDeleted(0);
        warehouse.setCreatedBy(createdBy);
        warehouseMapper.insert(warehouse);
        return toResponse(warehouse);
    }

    public WarehouseResponse createWarehouse(String appCode, WarehouseCreateRequest request, LoginUser loginUser) {
        if (loginUser == null || loginUser.getTenantId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        if (request == null || StringUtils.isBlank(request.getWarehouseName())) {
            throw new BusinessException("仓库名称不能为空");
        }
        String normalizedAppCode = StringUtils.upperCase(StringUtils.trimToEmpty(appCode));
        Warehouse warehouse = new Warehouse();
        warehouse.setAppCode(normalizedAppCode);
        warehouse.setTenantId(loginUser.getTenantId());
        warehouse.setWarehouseCode(StringUtils.defaultIfBlank(
            StringUtils.upperCase(StringUtils.trimToNull(request.getWarehouseCode())),
            nextWarehouseCode()));
        warehouse.setWarehouseName(StringUtils.trim(request.getWarehouseName()));
        warehouse.setWarehouseType(StringUtils.defaultIfBlank(
            StringUtils.upperCase(StringUtils.trimToNull(request.getWarehouseType())),
            defaultManualWarehouseType(normalizedAppCode)));
        warehouse.setDefaultFlag(request.getDefaultFlag() != null && request.getDefaultFlag() == 1 ? 1 : 0);
        warehouse.setOwnerType(StringUtils.defaultIfBlank(
            StringUtils.upperCase(StringUtils.trimToNull(request.getOwnerType())),
            defaultManualOwnerType(normalizedAppCode)));
        warehouse.setOwnerAppCode(StringUtils.defaultIfBlank(
            StringUtils.upperCase(StringUtils.trimToNull(request.getOwnerAppCode())),
            normalizedAppCode));
        warehouse.setOwnerTenantId(request.getOwnerTenantId() == null ? loginUser.getTenantId() : request.getOwnerTenantId());
        warehouse.setContactName(StringUtils.trimToNull(request.getContactName()));
        warehouse.setContactPhone(StringUtils.trimToNull(request.getContactPhone()));
        warehouse.setWarehouseAddress(StringUtils.trimToNull(request.getWarehouseAddress()));
        warehouse.setProvinceCode(StringUtils.trimToNull(request.getProvinceCode()));
        warehouse.setProvinceName(StringUtils.trimToNull(request.getProvinceName()));
        warehouse.setCityCode(StringUtils.trimToNull(request.getCityCode()));
        warehouse.setCityName(StringUtils.trimToNull(request.getCityName()));
        warehouse.setDistrictCode(StringUtils.trimToNull(request.getDistrictCode()));
        warehouse.setDistrictName(StringUtils.trimToNull(request.getDistrictName()));
        warehouse.setStatus(1);
        warehouse.setDeleted(0);
        warehouse.setCreatedBy(loginUser.getUserId());
        warehouseMapper.insert(warehouse);
        return toResponse(warehouse);
    }

    public List<WarehouseResponse> listCurrentTenantWarehouses(String appCode, LoginUser loginUser) {
        if (loginUser == null || loginUser.getTenantId() == null) {
            return List.of();
        }
        String normalizedAppCode = StringUtils.upperCase(StringUtils.trimToEmpty(appCode));
        return warehouseMapper.selectList(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getAppCode, normalizedAppCode)
                .eq(Warehouse::getTenantId, loginUser.getTenantId())
                .eq(Warehouse::getDeleted, 0)
                .orderByDesc(Warehouse::getDefaultFlag)
                .orderByDesc(Warehouse::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public Warehouse loadEntity(String appCode, Long tenantId, Long warehouseId) {
        if (tenantId == null || warehouseId == null) {
            throw new BusinessException("tenantId、warehouseId 不能为空");
        }
        String normalizedAppCode = StringUtils.upperCase(StringUtils.trimToEmpty(appCode));
        Warehouse warehouse = warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>()
            .eq(Warehouse::getAppCode, normalizedAppCode)
            .eq(Warehouse::getTenantId, tenantId)
            .eq(Warehouse::getId, warehouseId)
            .eq(Warehouse::getDeleted, 0)
            .last("limit 1"));
        if (warehouse == null) {
            throw new BusinessException("仓库不存在");
        }
        return warehouse;
    }

    private String defaultWarehouseName(String tenantType, String tenantName) {
        if (MERCHANT.equals(tenantType)) {
            return "商家默认仓";
        }
        if (PROJECT.equals(tenantType)) {
            return "项目默认仓";
        }
        return StringUtils.defaultIfBlank(StringUtils.trimToNull(tenantName), "默认") + "默认仓";
    }

    private String defaultWarehouseType(String tenantType) {
        if (MERCHANT.equals(tenantType)) {
            return "MERCHANT_WAREHOUSE";
        }
        if (PROJECT.equals(tenantType)) {
            return "PROJECT_WAREHOUSE";
        }
        return "TENANT_WAREHOUSE";
    }

    private boolean shouldCreateDefaultWarehouse(String tenantType) {
        return MERCHANT.equals(tenantType)
            || PROJECT.equals(tenantType);
    }

    private String defaultManualWarehouseType(String appCode) {
        if (WSGM.equals(appCode)) {
            return "COMPREHENSIVE_WAREHOUSE";
        }
        return "TENANT_WAREHOUSE";
    }

    private String defaultManualOwnerType(String appCode) {
        if (WSGM.equals(appCode)) {
            return "COMPREHENSIVE_WAREHOUSE";
        }
        return "TENANT";
    }

    private String nextWarehouseCode() {
        return "WH-" + StringUtils.upperCase(Long.toString(System.currentTimeMillis(), 36));
    }

    private boolean isSelfOperatedMerchant(String tenantType, String tenantSource) {
        return (MERCHANT.equals(tenantType) || SELF_OPERATED_MERCHANT.equals(tenantType))
            && PRIMELOAD_SELF_OPERATED.equals(StringUtils.upperCase(StringUtils.trimToEmpty(tenantSource)));
    }

    private void ensureRegionColumns() {
        addColumnIfMissing("province_code", "ALTER TABLE warehouse_registry ADD COLUMN province_code VARCHAR(32) DEFAULT NULL COMMENT '省级行政区编码' AFTER warehouse_address");
        addColumnIfMissing("province_name", "ALTER TABLE warehouse_registry ADD COLUMN province_name VARCHAR(64) DEFAULT NULL COMMENT '省级行政区名称' AFTER province_code");
        addColumnIfMissing("city_code", "ALTER TABLE warehouse_registry ADD COLUMN city_code VARCHAR(32) DEFAULT NULL COMMENT '市级行政区编码' AFTER province_name");
        addColumnIfMissing("city_name", "ALTER TABLE warehouse_registry ADD COLUMN city_name VARCHAR(64) DEFAULT NULL COMMENT '市级行政区名称' AFTER city_code");
        addColumnIfMissing("district_code", "ALTER TABLE warehouse_registry ADD COLUMN district_code VARCHAR(32) DEFAULT NULL COMMENT '区县行政区编码' AFTER city_name");
        addColumnIfMissing("district_name", "ALTER TABLE warehouse_registry ADD COLUMN district_name VARCHAR(64) DEFAULT NULL COMMENT '区县行政区名称' AFTER district_code");
    }

    private void addColumnIfMissing(String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'warehouse_registry'
              AND COLUMN_NAME = ?
            """, Integer.class, columnName);
        if (count == null || count == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        WarehouseResponse response = new WarehouseResponse();
        response.setId(warehouse.getId());
        response.setAppCode(warehouse.getAppCode());
        response.setTenantId(warehouse.getTenantId());
        response.setWarehouseCode(warehouse.getWarehouseCode());
        response.setWarehouseName(warehouse.getWarehouseName());
        response.setWarehouseType(warehouse.getWarehouseType());
        response.setDefaultFlag(warehouse.getDefaultFlag());
        response.setOwnerType(warehouse.getOwnerType());
        response.setOwnerAppCode(warehouse.getOwnerAppCode());
        response.setOwnerTenantId(warehouse.getOwnerTenantId());
        response.setContactName(warehouse.getContactName());
        response.setContactPhone(warehouse.getContactPhone());
        response.setWarehouseAddress(warehouse.getWarehouseAddress());
        response.setProvinceCode(warehouse.getProvinceCode());
        response.setProvinceName(warehouse.getProvinceName());
        response.setCityCode(warehouse.getCityCode());
        response.setCityName(warehouse.getCityName());
        response.setDistrictCode(warehouse.getDistrictCode());
        response.setDistrictName(warehouse.getDistrictName());
        response.setStatus(warehouse.getStatus());
        return response;
    }
}

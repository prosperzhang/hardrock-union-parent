package com.hardrockunion.platform.tenant.service;

import java.util.UUID;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.tenant.domain.entity.TenantRegistry;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.mapper.TenantRegistryMapper;

import jakarta.annotation.PostConstruct;

@Service
public class TenantRegistryService {

    private static final String WSGM_APP_CODE = "WSGM";
    private static final Long WSGM_TENANT_ID = 1001L;
    private static final String OLD_WSGM_SELF_OPERATED = "WSGM_SELF_OPERATED";
    private static final String PRIMELOAD_SELF_OPERATED = "PRIMELOAD_SELF_OPERATED";
    private static final String SELF_OPERATED_MERCHANT = "SELF_OPERATED_MERCHANT";
    private static final String PRIMELOAD_SELF_OPERATED_TENANT_CODE = "PRIMELOAD-MARKETPLACE-SELF-OPERATED";
    private static final String SELF_OPERATED_TENANT_NAME = "好料自营";
    private static final String TENANT_TYPE_GROUP = "GROUP";
    private static final String TENANT_TYPE_COMPANY = "COMPANY";
    private static final String TENANT_TYPE_PROJECT = "PROJECT";

    private final TenantRegistryMapper tenantRegistryMapper;
    private final AppRegistryQueryService appRegistryQueryService;
    private final JdbcTemplate jdbcTemplate;

    public TenantRegistryService(TenantRegistryMapper tenantRegistryMapper,
                                 AppRegistryQueryService appRegistryQueryService,
                                 JdbcTemplate jdbcTemplate) {
        this.tenantRegistryMapper = tenantRegistryMapper;
        this.appRegistryQueryService = appRegistryQueryService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        ensureRegionColumns();
        ensureHierarchyColumns();
        migrateSelfOperatedTenant();
        ensureSelfOperatedTenantUniqueIndex();
    }

    public TenantRegistryResponse getById(Long tenantId) {
        if (tenantId == null) {
            throw new BusinessException("tenantId 不能为空");
        }
        TenantRegistry tenant = tenantRegistryMapper.selectOne(new LambdaQueryWrapper<TenantRegistry>()
            .eq(TenantRegistry::getId, tenantId)
            .eq(TenantRegistry::getDeleted, 0)
            .last("limit 1"));
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        return toResponse(tenant);
    }

    public TenantRegistryResponse getByAppAndId(String appCode, Long tenantId) {
        return toResponse(loadByAppAndId(appCode, tenantId));
    }

    public TenantRegistryResponse updateBasicInfo(String appCode,
                                                  Long tenantId,
                                                  String tenantName,
                                                  String projectAddress,
                                                  String provinceCode,
                                                  String provinceName,
                                                  String cityCode,
                                                  String cityName,
                                                  String districtCode,
                                                  String districtName,
                                                  String managerName,
                                                  String managerPhone) {
        ensureRegionColumns();
        TenantRegistry tenant = loadByAppAndId(appCode, tenantId);
        String normalizedTenantName = StringUtils.trimToNull(tenantName);
        if (normalizedTenantName == null) {
            throw new BusinessException("tenantName 不能为空");
        }
        tenant.setTenantName(normalizedTenantName);
        tenant.setProjectAddress(StringUtils.trimToNull(projectAddress));
        tenant.setProvinceCode(StringUtils.trimToNull(provinceCode));
        tenant.setProvinceName(StringUtils.trimToNull(provinceName));
        tenant.setCityCode(StringUtils.trimToNull(cityCode));
        tenant.setCityName(StringUtils.trimToNull(cityName));
        tenant.setDistrictCode(StringUtils.trimToNull(districtCode));
        tenant.setDistrictName(StringUtils.trimToNull(districtName));
        tenant.setManagerName(StringUtils.trimToNull(managerName));
        tenant.setManagerPhone(StringUtils.trimToNull(managerPhone));
        tenantRegistryMapper.updateById(tenant);
        return toResponse(tenant);
    }

    public List<TenantRegistryResponse> listEnabled(String appCode) {
        LambdaQueryWrapper<TenantRegistry> queryWrapper = new LambdaQueryWrapper<TenantRegistry>()
            .eq(TenantRegistry::getDeleted, 0)
            .eq(TenantRegistry::getStatus, 1);
        if (StringUtils.isNotBlank(appCode)) {
            AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
            queryWrapper.and(wrapper -> wrapper
                .eq(TenantRegistry::getAppId, app.getId())
                .or()
                .eq(TenantRegistry::getAppCode, app.getAppCode()));
        }
        return tenantRegistryMapper.selectList(queryWrapper.orderByAsc(TenantRegistry::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private TenantRegistry loadByAppAndId(String appCode, Long tenantId) {
        if (tenantId == null) {
            throw new BusinessException("tenantId 不能为空");
        }
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        ensureWsgmTenantId(app.getAppCode(), tenantId);
        TenantRegistry tenant = tenantRegistryMapper.selectOne(new LambdaQueryWrapper<TenantRegistry>()
            .and(wrapper -> wrapper
                .eq(TenantRegistry::getAppId, app.getId())
                .or()
                .eq(TenantRegistry::getAppCode, app.getAppCode()))
            .eq(TenantRegistry::getId, tenantId)
            .eq(TenantRegistry::getDeleted, 0)
            .last("limit 1"));
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        return tenant;
    }

    public List<TenantRegistryResponse> listEnabledByApp(String appCode) {
        return listEnabled(normalizeAppCode(appCode));
    }

    public List<TenantRegistryResponse> listEnabled() {
        return listEnabled(null);
    }

    public TenantRegistryResponse findFirstByTenantSource(String appCode, String tenantSource) {
        String normalizedTenantSource = StringUtils.trimToNull(tenantSource);
        if (normalizedTenantSource == null) {
            throw new BusinessException("tenantSource 不能为空");
        }
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        TenantRegistry tenant = tenantRegistryMapper.selectOne(new LambdaQueryWrapper<TenantRegistry>()
            .and(wrapper -> wrapper
                .eq(TenantRegistry::getAppId, app.getId())
                .or()
                .eq(TenantRegistry::getAppCode, app.getAppCode()))
            .eq(TenantRegistry::getTenantSource, normalizedTenantSource)
            .eq(TenantRegistry::getDeleted, 0)
            .eq(TenantRegistry::getStatus, 1)
            .orderByAsc(TenantRegistry::getId)
            .last("limit 1"));
        return tenant == null ? null : toResponse(tenant);
    }

    public List<TenantRegistryResponse> findProjectTenantsByKeyword(String appCode, String keyword, Long tenantId) {
        return findTenantsByKeyword(appCode, "PROJECT", keyword, tenantId);
    }

    public List<TenantRegistryResponse> findTenantsByKeyword(String appCode, String tenantType, String keyword, Long tenantId) {
        String normalizedKeyword = StringUtils.trimToNull(keyword);
        if (normalizedKeyword == null) {
            return List.of();
        }
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        LambdaQueryWrapper<TenantRegistry> queryWrapper = new LambdaQueryWrapper<TenantRegistry>()
            .eq(TenantRegistry::getAppId, app.getId())
            .eq(TenantRegistry::getTenantType, tenantType)
            .eq(TenantRegistry::getDeleted, 0)
            .and(wrapper -> wrapper.eq(TenantRegistry::getTenantCode, normalizedKeyword)
                .or()
                .eq(TenantRegistry::getTenantName, normalizedKeyword)
                .or()
                .like(TenantRegistry::getTenantCode, normalizedKeyword)
                .or()
                .like(TenantRegistry::getTenantName, normalizedKeyword));
        if (tenantId != null) {
            queryWrapper.eq(TenantRegistry::getId, tenantId);
        }
        return tenantRegistryMapper.selectList(queryWrapper.orderByDesc(TenantRegistry::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public TenantRegistryResponse createProjectTenant(String appCode,
                                                      String tenantCode,
                                                      String tenantName,
                                                      String projectAddress,
                                                      String provinceCode,
                                                      String provinceName,
                                                      String cityCode,
                                                      String cityName,
                                                      String districtCode,
                                                      String districtName,
                                                      String managerName,
                                                      String managerPhone) {
        return createTenant(appCode, "PROJECT", "NEXIS", tenantCode, tenantName, projectAddress,
            provinceCode, provinceName, cityCode, cityName, districtCode, districtName, managerName, managerPhone);
    }

    public TenantRegistryResponse createTenant(String appCode,
                                               String tenantType,
                                               String tenantCodePrefix,
                                               String tenantCode,
                                               String tenantName,
                                               String projectAddress,
                                               String provinceCode,
                                               String provinceName,
                                               String cityCode,
                                               String cityName,
                                               String districtCode,
                                               String districtName,
                                               String managerName,
                                               String managerPhone) {
        return createTenant(appCode, tenantType, tenantCodePrefix, tenantCode, tenantName, null, null, projectAddress,
            provinceCode, provinceName, cityCode, cityName, districtCode, districtName, managerName, managerPhone);
    }

    public TenantRegistryResponse createTenant(String appCode,
                                               String tenantType,
                                               String tenantCodePrefix,
                                               String tenantCode,
                                               String tenantName,
                                               String tenantSource,
                                               Long parentTenantId,
                                               String projectAddress,
                                               String provinceCode,
                                               String provinceName,
                                               String cityCode,
                                               String cityName,
                                               String districtCode,
                                               String districtName,
                                               String managerName,
                                               String managerPhone) {
        return createTenantInternal(appCode, tenantType, tenantCodePrefix, tenantCode, tenantName, tenantSource,
            parentTenantId,
            projectAddress, provinceCode, provinceName, cityCode, cityName, districtCode, districtName, managerName,
            managerPhone);
    }

    public TenantRegistryResponse updateParentTenant(String appCode, Long tenantId, Long parentTenantId) {
        ensureHierarchyColumns();
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        TenantRegistry tenant = loadByAppAndId(app.getAppCode(), tenantId);
        if (!StringUtils.equals(TENANT_TYPE_PROJECT, tenant.getTenantType())) {
            throw new BusinessException("只有项目租户可以调整公司/集团归属");
        }
        TenantRegistry parentTenant = validateParentTenant(app.getId(), parentTenantId, tenant.getTenantType());
        tenant.setParentTenantId(parentTenant == null ? null : parentTenant.getId());
        tenantRegistryMapper.updateById(tenant);
        return toResponse(tenant);
    }

    public TenantRegistryResponse createTenant(String appCode,
                                               String tenantType,
                                               String tenantCodePrefix,
                                               String tenantCode,
                                               String tenantName,
                                               String tenantSource,
                                               String projectAddress,
                                               String provinceCode,
                                               String provinceName,
                                               String cityCode,
                                               String cityName,
                                               String districtCode,
                                               String districtName,
                                               String managerName,
                                               String managerPhone) {
        return createTenant(appCode, tenantType, tenantCodePrefix, tenantCode, tenantName, tenantSource, null,
            projectAddress, provinceCode, provinceName, cityCode, cityName, districtCode, districtName, managerName,
            managerPhone);
    }

    private TenantRegistryResponse createTenantInternal(String appCode,
                                                       String tenantType,
                                                       String tenantCodePrefix,
                                                       String tenantCode,
                                                       String tenantName,
                                                       String tenantSource,
                                                       Long parentTenantId,
                                                       String projectAddress,
                                                       String provinceCode,
                                                       String provinceName,
                                                       String cityCode,
                                                       String cityName,
                                                       String districtCode,
                                                       String districtName,
                                                       String managerName,
                                                       String managerPhone) {
        ensureRegionColumns();
        ensureHierarchyColumns();
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        ensureWsgmTenantCanBeCreated(app.getAppCode());
        String normalizedTenantName = StringUtils.trimToNull(tenantName);
        if (normalizedTenantName == null) {
            throw new BusinessException("tenantName 不能为空");
        }
        String normalizedTenantType = normalizeTenantType(tenantType);
        TenantRegistry parentTenant = validateParentTenant(app.getId(), parentTenantId, normalizedTenantType);
        String normalizedTenantSource = StringUtils.trimToNull(tenantSource);
        ensureSelfOperatedTenantCanBeCreated(app.getId(), normalizedTenantSource);
        String normalizedTenantCode = StringUtils.upperCase(StringUtils.trimToNull(tenantCode));
        if (normalizedTenantCode == null && StringUtils.equalsIgnoreCase(PRIMELOAD_SELF_OPERATED, normalizedTenantSource)) {
            normalizedTenantCode = PRIMELOAD_SELF_OPERATED_TENANT_CODE;
        }
        if (normalizedTenantCode == null) {
            normalizedTenantCode = generateTenantCode(tenantCodePrefix, normalizedTenantName, normalizedTenantType);
        }
        Long count = tenantRegistryMapper.selectCount(new LambdaQueryWrapper<TenantRegistry>()
            .eq(TenantRegistry::getAppId, app.getId())
            .eq(TenantRegistry::getTenantCode, normalizedTenantCode)
            .eq(TenantRegistry::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BusinessException("租户编码已存在");
        }

        TenantRegistry tenant = new TenantRegistry();
        tenant.setAppId(app.getId());
        tenant.setAppCode(app.getAppCode());
        tenant.setParentTenantId(parentTenant == null ? null : parentTenant.getId());
        tenant.setTenantCode(normalizedTenantCode);
        tenant.setTenantName(normalizedTenantName);
        tenant.setTenantType(normalizedTenantType);
        tenant.setTenantSource(normalizedTenantSource);
        tenant.setProjectAddress(StringUtils.trimToNull(projectAddress));
        tenant.setProvinceCode(StringUtils.trimToNull(provinceCode));
        tenant.setProvinceName(StringUtils.trimToNull(provinceName));
        tenant.setCityCode(StringUtils.trimToNull(cityCode));
        tenant.setCityName(StringUtils.trimToNull(cityName));
        tenant.setDistrictCode(StringUtils.trimToNull(districtCode));
        tenant.setDistrictName(StringUtils.trimToNull(districtName));
        tenant.setManagerName(StringUtils.trimToNull(managerName));
        tenant.setManagerPhone(StringUtils.trimToNull(managerPhone));
        tenant.setStatus(1);
        tenant.setDeleted(0);
        tenantRegistryMapper.insert(tenant);
        return toResponse(tenant);
    }

    private String normalizeTenantType(String tenantType) {
        String normalizedTenantType = StringUtils.upperCase(StringUtils.trimToNull(tenantType));
        if (normalizedTenantType == null) {
            throw new BusinessException("tenantType 不能为空");
        }
        return normalizedTenantType;
    }

    private TenantRegistry validateParentTenant(Long appId, Long parentTenantId, String tenantType) {
        if (parentTenantId == null) {
            return null;
        }
        TenantRegistry parentTenant = tenantRegistryMapper.selectOne(new LambdaQueryWrapper<TenantRegistry>()
            .eq(TenantRegistry::getAppId, appId)
            .eq(TenantRegistry::getId, parentTenantId)
            .eq(TenantRegistry::getDeleted, 0)
            .eq(TenantRegistry::getStatus, 1)
            .last("limit 1"));
        if (parentTenant == null) {
            throw new BusinessException("父级租户不存在或已停用");
        }
        if (parentTenant.getId().equals(parentTenant.getParentTenantId())) {
            throw new BusinessException("父级租户层级异常");
        }
        if (StringUtils.equals(TENANT_TYPE_GROUP, tenantType)) {
            throw new BusinessException("集团租户不能挂到其他租户下");
        }
        if (StringUtils.equals(TENANT_TYPE_COMPANY, tenantType)
            && !StringUtils.equals(TENANT_TYPE_GROUP, parentTenant.getTenantType())) {
            throw new BusinessException("公司租户只能挂到集团租户下");
        }
        if (StringUtils.equals(TENANT_TYPE_PROJECT, tenantType)
            && !StringUtils.equalsAny(parentTenant.getTenantType(), TENANT_TYPE_GROUP, TENANT_TYPE_COMPANY)) {
            throw new BusinessException("项目只能挂到公司或集团租户下");
        }
        return parentTenant;
    }

    private void ensureWsgmTenantId(String appCode, Long tenantId) {
        if (StringUtils.equalsIgnoreCase(WSGM_APP_CODE, appCode)
            && !WSGM_TENANT_ID.equals(tenantId)) {
            throw new BusinessException("顽石工盟固定为 1001");
        }
    }

    private void ensureWsgmTenantCanBeCreated(String appCode) {
        if (!StringUtils.equalsIgnoreCase(WSGM_APP_CODE, appCode)) {
            return;
        }
        Long count = tenantRegistryMapper.selectCount(new LambdaQueryWrapper<TenantRegistry>()
            .eq(TenantRegistry::getAppCode, WSGM_APP_CODE)
            .eq(TenantRegistry::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BusinessException("顽石工盟租户只能有一个");
        }
    }

    private void ensureSelfOperatedTenantCanBeCreated(Long appId, String tenantSource) {
        if (!StringUtils.equalsIgnoreCase(PRIMELOAD_SELF_OPERATED, tenantSource)) {
            return;
        }
        Long count = tenantRegistryMapper.selectCount(new LambdaQueryWrapper<TenantRegistry>()
            .eq(TenantRegistry::getAppId, appId)
            .eq(TenantRegistry::getTenantSource, PRIMELOAD_SELF_OPERATED)
            .eq(TenantRegistry::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BusinessException("好料自营租户已存在");
        }
    }

    private String normalizeAppCode(String appCode) {
        return appRegistryQueryService.normalizeAppCode(appCode);
    }

    private TenantRegistryResponse toResponse(TenantRegistry tenant) {
        TenantRegistryResponse response = new TenantRegistryResponse();
        response.setId(tenant.getId());
        response.setAppId(tenant.getAppId());
        response.setAppCode(tenant.getAppCode());
        response.setParentTenantId(tenant.getParentTenantId());
        fillParentTenant(response, tenant.getParentTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setTenantName(tenant.getTenantName());
        response.setTenantType(tenant.getTenantType());
        response.setTenantSource(tenant.getTenantSource());
        response.setStatus(tenant.getStatus());
        response.setProjectAddress(tenant.getProjectAddress());
        response.setProvinceCode(tenant.getProvinceCode());
        response.setProvinceName(tenant.getProvinceName());
        response.setCityCode(tenant.getCityCode());
        response.setCityName(tenant.getCityName());
        response.setDistrictCode(tenant.getDistrictCode());
        response.setDistrictName(tenant.getDistrictName());
        response.setManagerName(tenant.getManagerName());
        response.setManagerPhone(tenant.getManagerPhone());
        return response;
    }

    private void fillParentTenant(TenantRegistryResponse response, Long parentTenantId) {
        if (parentTenantId == null) {
            return;
        }
        TenantRegistry parentTenant = tenantRegistryMapper.selectOne(new LambdaQueryWrapper<TenantRegistry>()
            .eq(TenantRegistry::getId, parentTenantId)
            .eq(TenantRegistry::getDeleted, 0)
            .last("limit 1"));
        if (parentTenant == null) {
            return;
        }
        response.setParentTenantCode(parentTenant.getTenantCode());
        response.setParentTenantName(parentTenant.getTenantName());
    }

    private String generateTenantCode(String tenantCodePrefix, String tenantName, String tenantType) {
        String prefix = StringUtils.defaultIfBlank(StringUtils.upperCase(StringUtils.trimToNull(tenantCodePrefix)), "TENANT");
        String base = StringUtils.upperCase(StringUtils.defaultIfBlank(tenantName, "PROJECT"))
            .replaceAll("[^A-Z0-9]+", "-");
        base = StringUtils.strip(base, "-");
        if (StringUtils.isBlank(base)) {
            base = StringUtils.defaultIfBlank(tenantType, "TENANT");
        }
        if (StringUtils.equals(base, prefix)) {
            base = StringUtils.defaultIfBlank(tenantType, "TENANT");
        } else if (StringUtils.startsWith(base, prefix + "-")) {
            base = StringUtils.removeStart(base, prefix + "-");
        }
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return prefix + "-" + base + "-" + suffix;
    }

    private void ensureRegionColumns() {
        addColumnIfMissing("tenant_source", "ALTER TABLE tenant_registry ADD COLUMN tenant_source VARCHAR(32) DEFAULT NULL COMMENT '租户来源，例如 PRIMELOAD_SELF_OPERATED' AFTER tenant_type");
        addColumnIfMissing("province_code", "ALTER TABLE tenant_registry ADD COLUMN province_code VARCHAR(32) DEFAULT NULL COMMENT '省级行政区编码' AFTER project_address");
        addColumnIfMissing("province_name", "ALTER TABLE tenant_registry ADD COLUMN province_name VARCHAR(64) DEFAULT NULL COMMENT '省级行政区名称' AFTER province_code");
        addColumnIfMissing("city_code", "ALTER TABLE tenant_registry ADD COLUMN city_code VARCHAR(32) DEFAULT NULL COMMENT '市级行政区编码' AFTER province_name");
        addColumnIfMissing("city_name", "ALTER TABLE tenant_registry ADD COLUMN city_name VARCHAR(64) DEFAULT NULL COMMENT '市级行政区名称' AFTER city_code");
        addColumnIfMissing("district_code", "ALTER TABLE tenant_registry ADD COLUMN district_code VARCHAR(32) DEFAULT NULL COMMENT '区县行政区编码' AFTER city_name");
        addColumnIfMissing("district_name", "ALTER TABLE tenant_registry ADD COLUMN district_name VARCHAR(64) DEFAULT NULL COMMENT '区县行政区名称' AFTER district_code");
    }

    private void ensureHierarchyColumns() {
        addColumnIfMissing("parent_tenant_id", "ALTER TABLE tenant_registry ADD COLUMN parent_tenant_id BIGINT DEFAULT NULL COMMENT '父级租户ID。NEXIS 中项目可挂到公司或集团租户下' AFTER app_code");
        addIndexIfMissing("idx_tenant_registry_parent", "ALTER TABLE tenant_registry ADD INDEX idx_tenant_registry_parent (parent_tenant_id)");
    }

    private void addColumnIfMissing(String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'tenant_registry'
              AND COLUMN_NAME = ?
            """, Integer.class, columnName);
        if (count == null || count == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private void ensureSelfOperatedTenantUniqueIndex() {
        dropIndexIfExists("uk_tenant_registry_app_source_deleted");
        dropIndexIfExists("uk_tenant_registry_self_operated");
        dropColumnIfExists("self_operated_unique_key", "ALTER TABLE tenant_registry DROP COLUMN self_operated_unique_key");
        addColumnIfMissing("self_operated_unique_key", """
            ALTER TABLE tenant_registry
            ADD COLUMN self_operated_unique_key BIGINT
            GENERATED ALWAYS AS (
                CASE
                    WHEN tenant_source = 'PRIMELOAD_SELF_OPERATED' AND deleted = 0 THEN app_id
                    ELSE NULL
                END
            ) STORED COMMENT '好料自营唯一约束键'
            """);
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'tenant_registry'
              AND INDEX_NAME = 'uk_tenant_registry_self_operated'
            """, Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.execute("""
                ALTER TABLE tenant_registry
                ADD UNIQUE KEY uk_tenant_registry_self_operated (self_operated_unique_key)
                """);
        }
    }

    private void dropIndexIfExists(String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'tenant_registry'
              AND INDEX_NAME = ?
            """, Integer.class, indexName);
        if (count != null && count > 0) {
            jdbcTemplate.execute("ALTER TABLE tenant_registry DROP INDEX " + indexName);
        }
    }

    private void addIndexIfMissing(String indexName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'tenant_registry'
              AND INDEX_NAME = ?
            """, Integer.class, indexName);
        if (count == null || count == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private void dropColumnIfExists(String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'tenant_registry'
              AND COLUMN_NAME = ?
            """, Integer.class, columnName);
        if (count != null && count > 0) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private void migrateSelfOperatedTenant() {
        jdbcTemplate.update("""
            UPDATE tenant_registry
            SET tenant_name = ?,
                tenant_type = ?,
                tenant_source = ?
            WHERE tenant_source IN (?, ?)
              AND deleted = 0
            """, SELF_OPERATED_TENANT_NAME, SELF_OPERATED_MERCHANT, PRIMELOAD_SELF_OPERATED,
            OLD_WSGM_SELF_OPERATED, PRIMELOAD_SELF_OPERATED);
    }
}

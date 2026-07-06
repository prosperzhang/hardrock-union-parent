package com.hardrockunion.platform.tenant.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.hardrockunion.framework.core.exception.BusinessException;

@Component
public class TenantFlowPolicy {

    public AppTenantPolicy resolve(String appCode) {
        String normalizedAppCode = StringUtils.upperCase(StringUtils.trimToEmpty(appCode));
        return switch (normalizedAppCode) {
            case "WSGM" -> new AppTenantPolicy(
                "WSGM",
                "HEADQUARTERS",
                "总部",
                "管理总部系统",
                "WSGM_ROOT",
                List.of("WSGM_SUPER_ADMIN"),
                "WSGM"
            );
            case "NEXIS" -> new AppTenantPolicy(
                "NEXIS",
                "PROJECT",
                "项目",
                "创建项目或加入项目",
                "NEXIS_DECISION_DEPT",
                List.of("NEXIS_DECISION_LEADER"),
                "NEXIS"
            );
            case "PRIMELOAD-MARKETPLACE" -> new AppTenantPolicy(
                "PRIMELOAD-MARKETPLACE",
                "MERCHANT",
                "商户",
                "创建商户或加入商户",
                "PRIMELOAD_MARKETPLACE_DECISION_DEPT",
                List.of("PRIMELOAD_MARKETPLACE_DECISION_LEADER"),
                "PRIMELOAD-MARKETPLACE"
            );
            case "PRIMELOAD-LOGISTICS" -> new AppTenantPolicy(
                "PRIMELOAD-LOGISTICS",
                "LOGISTICS_OPERATOR",
                "物流公司",
                "创建物流公司或加入物流公司",
                "PRIMELOAD_LOGISTICS_DECISION_DEPT",
                List.of("PRIMELOAD_LOGISTICS_DECISION_LEADER"),
                "PRIMELOAD-LOGISTICS"
            );
            case "PRIMELOAD-CLOUD-WAREHOUSE" -> new AppTenantPolicy(
                "PRIMELOAD-CLOUD-WAREHOUSE",
                "CLOUD_WAREHOUSE",
                "云仓",
                "创建云仓或加入云仓",
                "PRIMELOAD_CLOUD_WAREHOUSE_DECISION_DEPT",
                List.of("PRIMELOAD_CLOUD_WAREHOUSE_DECISION_LEADER"),
                "PRIMELOAD-CLOUD-WAREHOUSE"
            );
            case "PRIMELOAD-DELIVERY" -> new AppTenantPolicy(
                "PRIMELOAD-DELIVERY",
                "DELIVERY_PROVIDER",
                "配送方",
                "创建配送方或加入配送方",
                "PRIMELOAD_DELIVERY_DECISION_DEPT",
                List.of("PRIMELOAD_DELIVERY_DECISION_LEADER"),
                "PRIMELOAD-DELIVERY"
            );
            default -> throw new BusinessException("当前暂不支持该 app 的租户流程");
        };
    }

    public record AppTenantPolicy(String appCode,
                                  String tenantType,
                                  String tenantLabel,
                                  String onboardingActionLabel,
                                  String defaultDepartmentCode,
                                  List<String> defaultAdminRoleCodes,
                                  String tenantCodePrefix) {
    }
}

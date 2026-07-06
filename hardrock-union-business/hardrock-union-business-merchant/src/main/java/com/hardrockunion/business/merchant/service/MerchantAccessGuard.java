package com.hardrockunion.business.merchant.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;

@Component
public class MerchantAccessGuard {

    private static final String APP_CODE = "PRIMELOAD-MARKETPLACE";
    private static final String TENANT_TYPE = "MERCHANT";
    private static final String SELF_OPERATED_TENANT_TYPE = "SELF_OPERATED_MERCHANT";

    private final TenantRegistryService tenantRegistryService;

    public MerchantAccessGuard(TenantRegistryService tenantRegistryService) {
        this.tenantRegistryService = tenantRegistryService;
    }

    public void ensureLogin(LoginUser loginUser) {
        ensureMerchantLogin(loginUser);
    }

    public TenantRegistryResponse ensureMerchantLogin(LoginUser loginUser) {
        if (loginUser == null || loginUser.getTenantId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        if (!StringUtils.equalsIgnoreCase(APP_CODE, loginUser.getAppCode())) {
            throw new BusinessException("当前账号不是 PRIMELOAD-MARKETPLACE 商户登录上下文");
        }
        TenantRegistryResponse tenant = tenantRegistryService.getByAppAndId(APP_CODE, loginUser.getTenantId());
        if (!StringUtils.equalsIgnoreCase(TENANT_TYPE, tenant.getTenantType())
            && !StringUtils.equalsIgnoreCase(SELF_OPERATED_TENANT_TYPE, tenant.getTenantType())) {
            throw new BusinessException("当前租户不是商户租户");
        }
        return tenant;
    }
}

package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.service.IamPermissionQueryService;
import com.hardrockunion.platform.iam.service.IamRoleQueryService;
import com.hardrockunion.platform.tenant.service.TenantRegistryService;

@Component
public class NexisAccessGuard {

    private final IamPermissionQueryService iamPermissionQueryService;
    private final IamRoleQueryService iamRoleQueryService;
    private final TenantRegistryService tenantRegistryService;

    public NexisAccessGuard(IamPermissionQueryService iamPermissionQueryService,
                            IamRoleQueryService iamRoleQueryService,
                            TenantRegistryService tenantRegistryService) {
        this.iamPermissionQueryService = iamPermissionQueryService;
        this.iamRoleQueryService = iamRoleQueryService;
        this.tenantRegistryService = tenantRegistryService;
    }

    public void ensureLogin(LoginUser loginUser) {
        if (loginUser == null || loginUser.getTenantId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        String normalizedAppCode = StringUtils.trimToEmpty(loginUser.getAppCode()).toUpperCase();
        if (!StringUtils.equals(normalizedAppCode, "NEXIS")) {
            throw new BusinessException("当前登录态不属于 NEXIS");
        }
        String tenantType = tenantRegistryService.getByAppAndId("NEXIS", loginUser.getTenantId()).getTenantType();
        if (!StringUtils.equalsIgnoreCase("PROJECT", tenantType)) {
            throw new BusinessException("当前空间不是项目，请先进入项目空间");
        }
    }

    public void ensureAuthenticated(LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        String normalizedAppCode = StringUtils.trimToEmpty(loginUser.getAppCode()).toUpperCase();
        if (!StringUtils.equals(normalizedAppCode, "NEXIS")) {
            throw new BusinessException("当前登录态不属于 NEXIS");
        }
    }

    public void ensurePermission(LoginUser loginUser, String permissionCode) {
        ensureLogin(loginUser);
        String normalizedPermissionCode = StringUtils.upperCase(StringUtils.trimToEmpty(permissionCode));
        if (StringUtils.isBlank(normalizedPermissionCode)) {
            throw new BusinessException("权限编码不能为空");
        }
        List<String> permissions = iamPermissionQueryService.listPermissionCodesByUser(
            loginUser.getUserId(),
            loginUser.getAppCode(),
            loginUser.getTenantId());
        if (permissions.contains("*:*:*") || permissions.contains(normalizedPermissionCode)) {
            return;
        }
        throw new BusinessException("当前账号没有权限：" + normalizedPermissionCode);
    }

    public boolean hasRole(LoginUser loginUser, String roleCode) {
        ensureLogin(loginUser);
        return iamRoleQueryService.listRoleEntitiesByUser(
                loginUser.getUserId(), loginUser.getAppCode(), loginUser.getTenantId())
            .stream()
            .anyMatch(role -> StringUtils.equalsIgnoreCase(role.getRoleCode(), roleCode));
    }
}

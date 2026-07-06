package com.hardrockunion.business.project.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.service.IamPermissionQueryService;

@Component
public class PmhubAccessGuard {

    private final IamPermissionQueryService iamPermissionQueryService;

    public PmhubAccessGuard(IamPermissionQueryService iamPermissionQueryService) {
        this.iamPermissionQueryService = iamPermissionQueryService;
    }

    public void ensureLogin(LoginUser loginUser) {
        if (loginUser == null || loginUser.getTenantId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        String normalizedAppCode = StringUtils.trimToEmpty(loginUser.getAppCode()).toUpperCase();
        if (!StringUtils.equals(normalizedAppCode, "PMHUB")) {
            throw new BusinessException("当前登录态不属于 PMHUB");
        }
    }

    public void ensureAuthenticated(LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        String normalizedAppCode = StringUtils.trimToEmpty(loginUser.getAppCode()).toUpperCase();
        if (!StringUtils.equals(normalizedAppCode, "PMHUB")) {
            throw new BusinessException("当前登录态不属于 PMHUB");
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
}

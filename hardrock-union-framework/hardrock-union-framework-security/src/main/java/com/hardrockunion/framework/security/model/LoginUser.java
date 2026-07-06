package com.hardrockunion.framework.security.model;

public class LoginUser {

    private final Long userId;
    private final String appCode;
    private final Long tenantId;
    private final String username;

    public LoginUser(Long userId, String appCode, Long tenantId, String username) {
        this.userId = userId;
        this.appCode = appCode;
        this.tenantId = tenantId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAppCode() {
        return appCode;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getUsername() {
        return username;
    }
}

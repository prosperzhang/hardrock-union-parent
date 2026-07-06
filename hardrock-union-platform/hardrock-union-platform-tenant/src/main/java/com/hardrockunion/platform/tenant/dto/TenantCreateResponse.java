package com.hardrockunion.platform.tenant.dto;

import com.hardrockunion.platform.iam.dto.LoginResponse;

public class TenantCreateResponse {

    private TenantSummaryResponse tenant;
    private LoginResponse login;

    public TenantSummaryResponse getTenantRegistry() {
        return tenant;
    }

    public void setTenantRegistry(TenantSummaryResponse tenant) {
        this.tenant = tenant;
    }

    public LoginResponse getLogin() {
        return login;
    }

    public void setLogin(LoginResponse login) {
        this.login = login;
    }
}

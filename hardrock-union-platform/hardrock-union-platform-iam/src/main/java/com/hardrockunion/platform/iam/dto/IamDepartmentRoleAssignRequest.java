package com.hardrockunion.platform.iam.dto;

import java.util.List;

public class IamDepartmentRoleAssignRequest {

    private List<String> roleCodes;

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}

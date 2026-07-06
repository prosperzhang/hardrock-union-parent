package com.hardrockunion.platform.iam.dto;

import java.util.List;

/**
 * IAM 用户角色分配请求。
 *
 * <p>用于给已有用户重置角色集合，当前采用“整组覆盖”语义，而不是增量追加。
 */
public class IamUserRoleAssignRequest {

    private List<String> roleCodes;

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}

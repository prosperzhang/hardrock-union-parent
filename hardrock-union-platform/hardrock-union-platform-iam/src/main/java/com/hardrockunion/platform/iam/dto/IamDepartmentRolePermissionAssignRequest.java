package com.hardrockunion.platform.iam.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "部门角色权限分配请求。")
public class IamDepartmentRolePermissionAssignRequest {

    @Schema(description = "权限编码列表")
    private List<String> permissionCodes;

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }
}

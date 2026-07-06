package com.hardrockunion.platform.tenant.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "租户成员部门角色分配请求。")
public class TenantMemberAssignRequest {

    @Schema(description = "部门ID", example = "72217422662279195")
    private Long departmentId;
    @Schema(description = "角色编码列表", example = "[\"PMHUB_CONSTRUCTION_OFFICER\"]")
    private List<String> roleCodes;
    @Schema(description = "多部门角色分配列表。传该字段时，departmentId/roleCodes 作为旧单部门格式忽略。")
    private List<TenantMemberDepartmentRoleRequest> departmentRoles;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<TenantMemberDepartmentRoleRequest> getDepartmentRoles() {
        return departmentRoles;
    }

    public void setDepartmentRoles(List<TenantMemberDepartmentRoleRequest> departmentRoles) {
        this.departmentRoles = departmentRoles;
    }
}

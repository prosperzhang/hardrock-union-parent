package com.hardrockunion.platform.tenant.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "租户成员单个部门下的角色分配。")
public class TenantMemberDepartmentRoleRequest {

    @Schema(description = "部门ID", example = "72217422662279197")
    private Long departmentId;

    @Schema(description = "该部门下的角色编码列表", example = "[\"NEXIS_CONSTRUCTION_OFFICER\"]")
    private List<String> roleCodes;

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
}

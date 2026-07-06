package com.hardrockunion.platform.tenant.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "租户成员单个部门下的角色响应。")
public class TenantMemberDepartmentRoleResponse {

    @Schema(description = "部门ID", example = "72217422662279197")
    private Long departmentId;

    @Schema(description = "部门编码", example = "PMHUB_ENGINEERING_DEPT")
    private String departmentCode;

    @Schema(description = "部门名称", example = "工程部")
    private String departmentName;

    @Schema(description = "角色编码列表", example = "[\"PMHUB_CONSTRUCTION_OFFICER\"]")
    private List<String> roleCodes;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}

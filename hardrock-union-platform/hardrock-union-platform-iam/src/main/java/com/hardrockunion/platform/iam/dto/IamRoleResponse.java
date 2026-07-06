package com.hardrockunion.platform.iam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "共享角色响应。")
public class IamRoleResponse {

    @Schema(description = "角色ID", example = "66613534017052709")
    private Long id;
    @Schema(description = "应用编码", example = "PRIMELOAD-MARKETPLACE")
    private String appCode;
    @Schema(description = "角色编码", example = "PRIMELOAD_MARKETPLACE_MERCHANT_ADMIN")
    private String roleCode;
    @Schema(description = "角色名称", example = "商城管理员")
    private String roleName;
    @Schema(description = "状态，1启用，0停用", example = "1")
    private Integer status;
    @Schema(description = "是否可分配，1可分配，0不可分配", example = "1")
    private Integer assignable;
    @Schema(description = "是否管理员角色，1是，0否", example = "1")
    private Integer adminRole;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getAssignable() {
        return assignable;
    }

    public void setAssignable(Integer assignable) {
        this.assignable = assignable;
    }

    public Integer getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(Integer adminRole) {
        this.adminRole = adminRole;
    }
}

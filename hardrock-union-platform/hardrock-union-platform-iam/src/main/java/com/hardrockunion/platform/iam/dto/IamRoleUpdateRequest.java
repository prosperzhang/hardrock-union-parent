package com.hardrockunion.platform.iam.dto;

/**
 * 共享角色更新请求。
 */
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "共享角色更新请求。只有 WSGM 允许更新 WSGM、PMHUB、PRIMELOAD-MARKETPLACE 三端角色。")
public class IamRoleUpdateRequest {

    @Schema(description = "角色名称", example = "采购内勤")
    private String roleName;
    @Schema(description = "状态，1启用，0停用", example = "1", allowableValues = {"0", "1"})
    private Integer status;
    @Schema(description = "是否可分配，1可分配，0不可分配", example = "1", allowableValues = {"0", "1"})
    private Integer assignable;
    @Schema(description = "是否管理员角色，1是，0否", example = "0", allowableValues = {"0", "1"})
    private Integer adminRole;

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

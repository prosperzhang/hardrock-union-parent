package com.hardrockunion.platform.tenant.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "租户成员响应。")
public class TenantMemberResponse {

    @Schema(description = "成员ID", example = "75669421022519299")
    private Long id;
    @Schema(description = "项目租户ID", example = "75668854082945026")
    private Long tenantId;
    @Schema(description = "项目租户名称", example = "张栋俊测试项目A")
    private String tenantName;
    @Schema(description = "用户ID", example = "75669421022519297")
    private Long userId;
    @Schema(description = "用户名", example = "pmhub_join_migrate_20260422_2156")
    private String username;
    @Schema(description = "昵称", example = "李四")
    private String nickName;
    @Schema(description = "头像地址", example = "https://example.com/avatar.png")
    private String avatarUrl;
    @Schema(description = "成员状态", example = "ACTIVE")
    private String memberStatus;
    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;
    @Schema(description = "部门ID", example = "72217422662279195")
    private Long departmentId;
    @Schema(description = "部门编码", example = "PMHUB_ENGINEERING_DEPT")
    private String departmentCode;
    @Schema(description = "部门名称", example = "工程部")
    private String departmentName;
    @Schema(description = "角色编码列表", example = "[\"PMHUB_CONSTRUCTION_OFFICER\"]")
    private List<String> roleCodes;
    @Schema(description = "按部门分组的角色列表。一个成员可同时属于多个部门，每个部门可有多个角色。")
    private List<TenantMemberDepartmentRoleResponse> departmentRoles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

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

    public List<TenantMemberDepartmentRoleResponse> getDepartmentRoles() {
        return departmentRoles;
    }

    public void setDepartmentRoles(List<TenantMemberDepartmentRoleResponse> departmentRoles) {
        this.departmentRoles = departmentRoles;
    }
}

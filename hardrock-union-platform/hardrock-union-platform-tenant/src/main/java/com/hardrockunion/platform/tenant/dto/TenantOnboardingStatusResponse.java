package com.hardrockunion.platform.tenant.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "当前用户租户空间入驻状态响应。")
public class TenantOnboardingStatusResponse {

    @Schema(description = "引导状态", example = "WAITING_ROLE_ASSIGNMENT")
    private String status;
    @Schema(description = "租户空间ID", example = "75668854082945026")
    private Long tenantId;
    @Schema(description = "租户空间名称", example = "张栋俊测试项目A")
    private String tenantName;
    @Schema(description = "加入申请ID", example = "75669421022519298")
    private Long joinRequestId;
    @Schema(description = "加入申请状态", example = "PENDING")
    private String joinRequestStatus;
    @Schema(description = "成员ID", example = "75669421022519299")
    private Long memberId;
    @Schema(description = "成员状态", example = "ACTIVE")
    private String memberStatus;
    @Schema(description = "已分配部门ID", example = "72217422662279197")
    private Long departmentId;
    @Schema(description = "已分配部门编码", example = "PMHUB_ENGINEERING_DEPT")
    private String departmentCode;
    @Schema(description = "已分配部门名称", example = "工程部")
    private String departmentName;
    @Schema(description = "已分配部门简称", example = "工程")
    private String departmentShortName;
    @Schema(description = "角色编码列表", example = "[\"PMHUB_CONSTRUCTION_OFFICER\"]")
    private List<String> roleCodes;
    @Schema(description = "状态说明", example = "已加入租户空间，正在等待管理员分配部门和角色。")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Long getJoinRequestId() {
        return joinRequestId;
    }

    public void setJoinRequestId(Long joinRequestId) {
        this.joinRequestId = joinRequestId;
    }

    public String getJoinRequestStatus() {
        return joinRequestStatus;
    }

    public void setJoinRequestStatus(String joinRequestStatus) {
        this.joinRequestStatus = joinRequestStatus;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
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

    public String getDepartmentShortName() {
        return departmentShortName;
    }

    public void setDepartmentShortName(String departmentShortName) {
        this.departmentShortName = departmentShortName;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

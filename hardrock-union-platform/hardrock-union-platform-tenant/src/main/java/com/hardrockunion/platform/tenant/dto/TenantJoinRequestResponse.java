package com.hardrockunion.platform.tenant.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "加入项目租户申请响应。")
public class TenantJoinRequestResponse {

    @Schema(description = "申请ID", example = "75669421022519298")
    private Long id;
    @Schema(description = "目标项目租户ID", example = "75668854082945026")
    private Long tenantId;
    @Schema(description = "目标项目租户名称", example = "张栋俊测试项目A")
    private String tenantName;
    @Schema(description = "申请用户ID", example = "75669421022519297")
    private Long userId;
    @Schema(description = "申请状态", example = "PENDING")
    private String requestStatus;
    @Schema(description = "申请说明", example = "我是工程部施工员，请审批加入项目。")
    private String applyMessage;
    @Schema(description = "审批人用户ID", example = "75668854082945024")
    private Long reviewedBy;
    @Schema(description = "审批时间")
    private LocalDateTime reviewedAt;
    @Schema(description = "审批备注", example = "同意加入")
    private String reviewRemark;

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

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getApplyMessage() {
        return applyMessage;
    }

    public void setApplyMessage(String applyMessage) {
        this.applyMessage = applyMessage;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewRemark() {
        return reviewRemark;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }
}

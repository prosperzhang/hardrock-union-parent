package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "外部上级项目关联审核")
public class NexisExternalProjectLinkReviewRequest {

    @Schema(description = "是否通过", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean approved;
    @Schema(description = "通过时选定的正式上级项目租户ID")
    private Long targetProjectTenantId;
    @Schema(description = "数据共享范围，当前支持 SUMMARY", example = "SUMMARY")
    private String shareScope;
    @Schema(description = "审核备注")
    private String remark;

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public Long getTargetProjectTenantId() { return targetProjectTenantId; }
    public void setTargetProjectTenantId(Long targetProjectTenantId) { this.targetProjectTenantId = targetProjectTenantId; }
    public String getShareScope() { return shareScope; }
    public void setShareScope(String shareScope) { this.shareScope = shareScope; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

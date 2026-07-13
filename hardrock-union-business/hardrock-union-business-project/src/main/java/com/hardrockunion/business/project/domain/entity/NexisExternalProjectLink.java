package com.hardrockunion.business.project.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("project_external_link")
public class NexisExternalProjectLink extends BaseEntity {

    private Long sourceProjectTenantId;
    private Long sourceOrganizationTenantId;
    private Long externalParticipantCompanyId;
    private Long targetOrganizationTenantId;
    private Long targetProjectTenantId;
    private String externalOwnerNameSnapshot;
    private String externalProjectNameSnapshot;
    private String contractScopeNameSnapshot;
    private String linkStatus;
    private String shareScope;
    private Long requestedBy;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
    private Long unlinkedBy;
    private LocalDateTime unlinkedAt;

    public Long getSourceProjectTenantId() { return sourceProjectTenantId; }
    public void setSourceProjectTenantId(Long sourceProjectTenantId) { this.sourceProjectTenantId = sourceProjectTenantId; }
    public Long getSourceOrganizationTenantId() { return sourceOrganizationTenantId; }
    public void setSourceOrganizationTenantId(Long sourceOrganizationTenantId) { this.sourceOrganizationTenantId = sourceOrganizationTenantId; }
    public Long getExternalParticipantCompanyId() { return externalParticipantCompanyId; }
    public void setExternalParticipantCompanyId(Long externalParticipantCompanyId) { this.externalParticipantCompanyId = externalParticipantCompanyId; }
    public Long getTargetOrganizationTenantId() { return targetOrganizationTenantId; }
    public void setTargetOrganizationTenantId(Long targetOrganizationTenantId) { this.targetOrganizationTenantId = targetOrganizationTenantId; }
    public Long getTargetProjectTenantId() { return targetProjectTenantId; }
    public void setTargetProjectTenantId(Long targetProjectTenantId) { this.targetProjectTenantId = targetProjectTenantId; }
    public String getExternalOwnerNameSnapshot() { return externalOwnerNameSnapshot; }
    public void setExternalOwnerNameSnapshot(String externalOwnerNameSnapshot) { this.externalOwnerNameSnapshot = externalOwnerNameSnapshot; }
    public String getExternalProjectNameSnapshot() { return externalProjectNameSnapshot; }
    public void setExternalProjectNameSnapshot(String externalProjectNameSnapshot) { this.externalProjectNameSnapshot = externalProjectNameSnapshot; }
    public String getContractScopeNameSnapshot() { return contractScopeNameSnapshot; }
    public void setContractScopeNameSnapshot(String contractScopeNameSnapshot) { this.contractScopeNameSnapshot = contractScopeNameSnapshot; }
    public String getLinkStatus() { return linkStatus; }
    public void setLinkStatus(String linkStatus) { this.linkStatus = linkStatus; }
    public String getShareScope() { return shareScope; }
    public void setShareScope(String shareScope) { this.shareScope = shareScope; }
    public Long getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Long requestedBy) { this.requestedBy = requestedBy; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public Long getUnlinkedBy() { return unlinkedBy; }
    public void setUnlinkedBy(Long unlinkedBy) { this.unlinkedBy = unlinkedBy; }
    public LocalDateTime getUnlinkedAt() { return unlinkedAt; }
    public void setUnlinkedAt(LocalDateTime unlinkedAt) { this.unlinkedAt = unlinkedAt; }
}

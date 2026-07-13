package com.hardrockunion.business.project.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "外部上级项目认领与承包段关联")
public class NexisExternalProjectLinkResponse {

    private Long id;
    private Long sourceProjectTenantId;
    private String sourceProjectTenantName;
    private Long sourceOrganizationTenantId;
    private String sourceOrganizationTenantName;
    private Long externalParticipantCompanyId;
    private String externalParticipantCompanyName;
    private Long targetOrganizationTenantId;
    private String targetOrganizationTenantName;
    private Long targetProjectTenantId;
    private String targetProjectTenantName;
    private String externalOwnerName;
    private String externalProjectName;
    private String contractScopeName;
    private String linkStatus;
    private String linkStatusLabel;
    private String shareScope;
    private String reviewRemark;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime unlinkedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSourceProjectTenantId() { return sourceProjectTenantId; }
    public void setSourceProjectTenantId(Long sourceProjectTenantId) { this.sourceProjectTenantId = sourceProjectTenantId; }
    public String getSourceProjectTenantName() { return sourceProjectTenantName; }
    public void setSourceProjectTenantName(String sourceProjectTenantName) { this.sourceProjectTenantName = sourceProjectTenantName; }
    public Long getSourceOrganizationTenantId() { return sourceOrganizationTenantId; }
    public void setSourceOrganizationTenantId(Long sourceOrganizationTenantId) { this.sourceOrganizationTenantId = sourceOrganizationTenantId; }
    public String getSourceOrganizationTenantName() { return sourceOrganizationTenantName; }
    public void setSourceOrganizationTenantName(String sourceOrganizationTenantName) { this.sourceOrganizationTenantName = sourceOrganizationTenantName; }
    public Long getExternalParticipantCompanyId() { return externalParticipantCompanyId; }
    public void setExternalParticipantCompanyId(Long externalParticipantCompanyId) { this.externalParticipantCompanyId = externalParticipantCompanyId; }
    public String getExternalParticipantCompanyName() { return externalParticipantCompanyName; }
    public void setExternalParticipantCompanyName(String externalParticipantCompanyName) { this.externalParticipantCompanyName = externalParticipantCompanyName; }
    public Long getTargetOrganizationTenantId() { return targetOrganizationTenantId; }
    public void setTargetOrganizationTenantId(Long targetOrganizationTenantId) { this.targetOrganizationTenantId = targetOrganizationTenantId; }
    public String getTargetOrganizationTenantName() { return targetOrganizationTenantName; }
    public void setTargetOrganizationTenantName(String targetOrganizationTenantName) { this.targetOrganizationTenantName = targetOrganizationTenantName; }
    public Long getTargetProjectTenantId() { return targetProjectTenantId; }
    public void setTargetProjectTenantId(Long targetProjectTenantId) { this.targetProjectTenantId = targetProjectTenantId; }
    public String getTargetProjectTenantName() { return targetProjectTenantName; }
    public void setTargetProjectTenantName(String targetProjectTenantName) { this.targetProjectTenantName = targetProjectTenantName; }
    public String getExternalOwnerName() { return externalOwnerName; }
    public void setExternalOwnerName(String externalOwnerName) { this.externalOwnerName = externalOwnerName; }
    public String getExternalProjectName() { return externalProjectName; }
    public void setExternalProjectName(String externalProjectName) { this.externalProjectName = externalProjectName; }
    public String getContractScopeName() { return contractScopeName; }
    public void setContractScopeName(String contractScopeName) { this.contractScopeName = contractScopeName; }
    public String getLinkStatus() { return linkStatus; }
    public void setLinkStatus(String linkStatus) { this.linkStatus = linkStatus; }
    public String getLinkStatusLabel() { return linkStatusLabel; }
    public void setLinkStatusLabel(String linkStatusLabel) { this.linkStatusLabel = linkStatusLabel; }
    public String getShareScope() { return shareScope; }
    public void setShareScope(String shareScope) { this.shareScope = shareScope; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getUnlinkedAt() { return unlinkedAt; }
    public void setUnlinkedAt(LocalDateTime unlinkedAt) { this.unlinkedAt = unlinkedAt; }
}

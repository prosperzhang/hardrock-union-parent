package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubSiteWorkScopeResponse", description = "pmhub 标段施工范围响应")
public class PmhubSiteWorkScopeResponse {

    @Schema(description = "施工范围 ID", example = "68262140034686979")
    private Long id;
    @Schema(description = "所属 pmhub 租户 ID", example = "2001")
    private Long tenantId;
    @Schema(description = "项目 ID", example = "20010001")
    private Long projectId;
    @Schema(description = "项目名称", example = "园区一期改造项目")
    private String projectName;
    @Schema(description = "标段/工地 ID", example = "2001001")
    private Long siteId;
    @Schema(description = "标段/工地名称", example = "苏州工业园区一期工地")
    private String siteName;
    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;
    @Schema(description = "参建单位名称", example = "甲劳务公司")
    private String participantCompanyName;
    @Schema(description = "范围类型", example = "CODE_RANGE")
    private String scopeType;
    @Schema(description = "范围类型名称", example = "编码区间")
    private String scopeTypeLabel;
    @Schema(description = "范围名称", example = "A段劳务范围")
    private String scopeName;
    @Schema(description = "范围起点编码", example = "A-001")
    private String scopeStartCode;
    @Schema(description = "范围终点编码", example = "A-5000")
    private String scopeEndCode;
    @Schema(description = "范围说明", example = "甲劳务公司负责 A 段起止编码")
    private String scopeRemark;
    @Schema(description = "状态 1启用 0停用", example = "1")
    private Integer status;
    @Schema(description = "状态名称", example = "启用")
    private String statusLabel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public String getParticipantCompanyName() { return participantCompanyName; }
    public void setParticipantCompanyName(String participantCompanyName) { this.participantCompanyName = participantCompanyName; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getScopeTypeLabel() { return scopeTypeLabel; }
    public void setScopeTypeLabel(String scopeTypeLabel) { this.scopeTypeLabel = scopeTypeLabel; }
    public String getScopeName() { return scopeName; }
    public void setScopeName(String scopeName) { this.scopeName = scopeName; }
    public String getScopeStartCode() { return scopeStartCode; }
    public void setScopeStartCode(String scopeStartCode) { this.scopeStartCode = scopeStartCode; }
    public String getScopeEndCode() { return scopeEndCode; }
    public void setScopeEndCode(String scopeEndCode) { this.scopeEndCode = scopeEndCode; }
    public String getScopeRemark() { return scopeRemark; }
    public void setScopeRemark(String scopeRemark) { this.scopeRemark = scopeRemark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
}

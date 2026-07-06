package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubTeamResponse", description = "pmhub 班组响应")
public class PmhubTeamResponse {

    @Schema(description = "班组 ID", example = "68270000000000001")
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
    @Schema(description = "施工范围 ID", example = "68262140034686979")
    private Long workScopeId;
    @Schema(description = "施工范围名称", example = "A段劳务范围")
    private String workScopeName;
    @Schema(description = "班组名称", example = "甲劳务 A 段一班")
    private String teamName;
    @Schema(description = "班组编码", example = "TEAM-A-001")
    private String teamCode;
    @Schema(description = "班组长姓名", example = "李班长")
    private String leaderName;
    @Schema(description = "班组长手机号", example = "13800000002")
    private String leaderPhone;
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
    public Long getWorkScopeId() { return workScopeId; }
    public void setWorkScopeId(Long workScopeId) { this.workScopeId = workScopeId; }
    public String getWorkScopeName() { return workScopeName; }
    public void setWorkScopeName(String workScopeName) { this.workScopeName = workScopeName; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamCode() { return teamCode; }
    public void setTeamCode(String teamCode) { this.teamCode = teamCode; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public String getLeaderPhone() { return leaderPhone; }
    public void setLeaderPhone(String leaderPhone) { this.leaderPhone = leaderPhone; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
}

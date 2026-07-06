package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubWorkerResponse", description = "pmhub 工人响应")
public class PmhubWorkerResponse {

    @Schema(description = "工人 ID", example = "68270000000000002")
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
    @Schema(description = "班组 ID", example = "68270000000000001")
    private Long teamId;
    @Schema(description = "班组名称", example = "甲劳务 A 段一班")
    private String teamName;
    @Schema(description = "工人姓名", example = "赵师傅")
    private String workerName;
    @Schema(description = "工人手机号", example = "13800000003")
    private String workerPhone;
    @Schema(description = "身份证号", example = "320101199001010011")
    private String idCardNo;
    @Schema(description = "工种编码", example = "PIPE_INSTALLER")
    private String jobType;
    @Schema(description = "工种名称", example = "管道安装工")
    private String jobName;
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
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getWorkerPhone() { return workerPhone; }
    public void setWorkerPhone(String workerPhone) { this.workerPhone = workerPhone; }
    public String getIdCardNo() { return idCardNo; }
    public void setIdCardNo(String idCardNo) { this.idCardNo = idCardNo; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
}

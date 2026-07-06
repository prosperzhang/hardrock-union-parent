package com.hardrockunion.business.project.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubWorkerEntryResponse", description = "pmhub 工人实名进场响应")
public class PmhubWorkerEntryResponse {

    @Schema(description = "实名进场记录 ID", example = "68280000000000001")
    private Long id;
    @Schema(description = "所属 pmhub 租户 ID", example = "2001")
    private Long tenantId;
    @Schema(description = "实名进场单号", example = "ENTRY68280000000000001")
    private String entryNo;
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
    @Schema(description = "工人 ID", example = "68270000000000002")
    private Long workerId;
    @Schema(description = "工人姓名", example = "赵师傅")
    private String workerName;
    @Schema(description = "工人手机号", example = "13800000003")
    private String workerPhone;
    @Schema(description = "身份证号", example = "320101199001010011")
    private String idCardNo;
    @Schema(description = "实名状态", example = "VERIFIED")
    private String realNameStatus;
    @Schema(description = "实名状态名称", example = "已实名")
    private String realNameStatusLabel;
    @Schema(description = "进场状态", example = "ENTERED")
    private String entryStatus;
    @Schema(description = "进场状态名称", example = "已进场")
    private String entryStatusLabel;
    @Schema(description = "实际进场时间", example = "2026-04-06T13:30:00")
    private LocalDateTime enteredAt;
    @Schema(description = "实际退场时间", example = "2026-04-06T18:30:00")
    private LocalDateTime exitedAt;
    @Schema(description = "备注", example = "门禁已核验通过")
    private String remark;
    @Schema(description = "状态 1启用 0停用", example = "1")
    private Integer status;
    @Schema(description = "状态名称", example = "启用")
    private String statusLabel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getEntryNo() { return entryNo; }
    public void setEntryNo(String entryNo) { this.entryNo = entryNo; }
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
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getWorkerPhone() { return workerPhone; }
    public void setWorkerPhone(String workerPhone) { this.workerPhone = workerPhone; }
    public String getIdCardNo() { return idCardNo; }
    public void setIdCardNo(String idCardNo) { this.idCardNo = idCardNo; }
    public String getRealNameStatus() { return realNameStatus; }
    public void setRealNameStatus(String realNameStatus) { this.realNameStatus = realNameStatus; }
    public String getRealNameStatusLabel() { return realNameStatusLabel; }
    public void setRealNameStatusLabel(String realNameStatusLabel) { this.realNameStatusLabel = realNameStatusLabel; }
    public String getEntryStatus() { return entryStatus; }
    public void setEntryStatus(String entryStatus) { this.entryStatus = entryStatus; }
    public String getEntryStatusLabel() { return entryStatusLabel; }
    public void setEntryStatusLabel(String entryStatusLabel) { this.entryStatusLabel = entryStatusLabel; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
    public void setEnteredAt(LocalDateTime enteredAt) { this.enteredAt = enteredAt; }
    public LocalDateTime getExitedAt() { return exitedAt; }
    public void setExitedAt(LocalDateTime exitedAt) { this.exitedAt = exitedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
}

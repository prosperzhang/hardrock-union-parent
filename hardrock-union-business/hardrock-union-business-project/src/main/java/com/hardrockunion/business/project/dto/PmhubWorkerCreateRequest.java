package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubWorkerCreateRequest", description = "pmhub 工人创建请求")
public class PmhubWorkerCreateRequest {

    @Schema(description = "项目 ID", example = "20010001")
    private Long projectId;
    @Schema(description = "标段/工地 ID", example = "2001001")
    private Long siteId;
    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;
    @Schema(description = "施工范围 ID，可为空", example = "68262140034686979")
    private Long workScopeId;
    @Schema(description = "班组 ID，可为空", example = "68270000000000001")
    private Long teamId;
    @Schema(description = "工人姓名", example = "赵师傅")
    private String workerName;
    @Schema(description = "工人手机号", example = "13800000003")
    private String workerPhone;
    @Schema(description = "身份证号，用于实名进场和实名校验", example = "320101199001010011")
    private String idCardNo;
    @Schema(description = "工种编码", example = "PIPE_INSTALLER")
    private String jobType;
    @Schema(description = "工种名称", example = "管道安装工")
    private String jobName;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public Long getWorkScopeId() { return workScopeId; }
    public void setWorkScopeId(Long workScopeId) { this.workScopeId = workScopeId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
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
}

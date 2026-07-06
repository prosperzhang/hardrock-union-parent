package com.hardrockunion.business.project.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisWorkerEntryQueryRequest", description = "nexis 工人实名进场分页查询条件")
public class NexisWorkerEntryQueryRequest extends PageRequest {

    @Schema(description = "项目 ID", example = "20010001")
    private Long projectId;
    @Schema(description = "标段/工地 ID", example = "2001001")
    private Long siteId;
    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;
    @Schema(description = "施工范围 ID", example = "68262140034686979")
    private Long workScopeId;
    @Schema(description = "班组 ID", example = "68270000000000001")
    private Long teamId;
    @Schema(description = "工人 ID", example = "68270000000000002")
    private Long workerId;
    @Schema(description = "进场状态", example = "ENTERED", allowableValues = { "REGISTERED", "ENTERED", "EXITED" })
    private String entryStatus;
    @Schema(description = "实名状态", example = "VERIFIED", allowableValues = { "VERIFIED", "UNVERIFIED" })
    private String realNameStatus;
    @Schema(description = "关键词，匹配进场单号、工人姓名、手机号、身份证号和班组名称", example = "赵师傅")
    private String keyword;

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
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public String getEntryStatus() { return entryStatus; }
    public void setEntryStatus(String entryStatus) { this.entryStatus = entryStatus; }
    public String getRealNameStatus() { return realNameStatus; }
    public void setRealNameStatus(String realNameStatus) { this.realNameStatus = realNameStatus; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}

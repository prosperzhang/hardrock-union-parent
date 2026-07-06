package com.hardrockunion.business.project.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubWorkerQueryRequest", description = "pmhub 工人分页查询条件")
public class PmhubWorkerQueryRequest extends PageRequest {

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
    @Schema(description = "关键词，匹配工人姓名、手机号、身份证号、工种编码和工种名称", example = "赵师傅")
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
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}

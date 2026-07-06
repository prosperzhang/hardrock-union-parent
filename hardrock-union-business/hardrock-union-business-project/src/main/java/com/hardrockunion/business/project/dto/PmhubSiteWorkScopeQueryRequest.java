package com.hardrockunion.business.project.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubSiteWorkScopeQueryRequest", description = "pmhub 标段施工范围分页查询条件")
public class PmhubSiteWorkScopeQueryRequest extends PageRequest {

    @Schema(description = "项目 ID", example = "20010001")
    private Long projectId;

    @Schema(description = "标段/工地 ID", example = "2001001")
    private Long siteId;

    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;

    @Schema(description = "范围类型筛选，可通过 /api/pmhub/dictionaries/site-work-scope-types 获取字典", example = "CODE_RANGE",
        allowableValues = {"CODE_RANGE", "MILESTONE_RANGE", "AREA_RANGE", "PIPE_SEGMENT", "BUILDING_FLOOR_RANGE"})
    private String scopeType;

    @Schema(description = "关键词，匹配范围名称、起止编码、标段名称和参建单位名称/编码", example = "A-001")
    private String keyword;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}

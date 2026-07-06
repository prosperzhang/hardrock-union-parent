package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisSiteWorkScopeCreateRequest", description = "nexis 标段施工范围创建请求")
public class NexisSiteWorkScopeCreateRequest {

    @Schema(description = "项目 ID；标段未挂项目时可显式传入", example = "20010001")
    private Long projectId;
    @Schema(description = "标段/工地 ID", example = "2001001")
    private Long siteId;
    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;
    @Schema(description = "范围类型，可通过 /api/nexis/dictionaries/site-work-scope-types 获取字典", example = "CODE_RANGE",
        allowableValues = {"CODE_RANGE", "MILESTONE_RANGE", "AREA_RANGE", "PIPE_SEGMENT", "BUILDING_FLOOR_RANGE"})
    private String scopeType;
    @Schema(description = "范围名称", example = "A段劳务范围")
    private String scopeName;
    @Schema(description = "范围起点编码", example = "A-001")
    private String scopeStartCode;
    @Schema(description = "范围终点编码", example = "A-5000")
    private String scopeEndCode;
    @Schema(description = "范围说明", example = "甲劳务公司负责 A 段起止编码")
    private String scopeRemark;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getScopeName() { return scopeName; }
    public void setScopeName(String scopeName) { this.scopeName = scopeName; }
    public String getScopeStartCode() { return scopeStartCode; }
    public void setScopeStartCode(String scopeStartCode) { this.scopeStartCode = scopeStartCode; }
    public String getScopeEndCode() { return scopeEndCode; }
    public void setScopeEndCode(String scopeEndCode) { this.scopeEndCode = scopeEndCode; }
    public String getScopeRemark() { return scopeRemark; }
    public void setScopeRemark(String scopeRemark) { this.scopeRemark = scopeRemark; }
}

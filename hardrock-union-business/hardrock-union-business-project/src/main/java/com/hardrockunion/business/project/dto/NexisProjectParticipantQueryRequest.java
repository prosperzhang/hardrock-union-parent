package com.hardrockunion.business.project.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisProjectParticipantQueryRequest", description = "nexis 项目参建关系分页查询条件")
public class NexisProjectParticipantQueryRequest extends PageRequest {

    @Schema(description = "项目 ID", example = "20010001")
    private Long projectId;

    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;

    @Schema(description = "项目角色筛选，可通过 /api/nexis/dictionaries/participant-roles 获取字典", example = "LABOR_CONTRACTOR",
        allowableValues = {"GENERAL_CONTRACTOR", "SPECIALTY_CONTRACTOR", "LABOR_CONTRACTOR", "SUPPLIER"})
    private String participantRole;

    @Schema(description = "关键词，匹配项目名称/编码和参建单位名称/编码", example = "园区一期")
    private String keyword;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public String getParticipantRole() { return participantRole; }
    public void setParticipantRole(String participantRole) { this.participantRole = participantRole; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}

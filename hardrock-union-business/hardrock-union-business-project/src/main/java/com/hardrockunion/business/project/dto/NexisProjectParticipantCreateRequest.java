package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisProjectParticipantCreateRequest", description = "nexis 项目参建关系创建请求")
public class NexisProjectParticipantCreateRequest {

    @Schema(description = "项目 ID", example = "20010001")
    private Long projectId;
    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;
    @Schema(description = "项目角色，可通过 /api/nexis/dictionaries/participant-roles 获取字典", example = "LABOR_CONTRACTOR",
        allowableValues = {"GENERAL_CONTRACTOR", "SPECIALTY_CONTRACTOR", "LABOR_CONTRACTOR", "SUPPLIER"})
    private String participantRole;
    @Schema(description = "是否主责单位 1是 0否", example = "0")
    private Integer isLead;
    @Schema(description = "备注", example = "参与园区一期项目")
    private String remark;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public String getParticipantRole() { return participantRole; }
    public void setParticipantRole(String participantRole) { this.participantRole = participantRole; }
    public Integer getIsLead() { return isLead; }
    public void setIsLead(Integer isLead) { this.isLead = isLead; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

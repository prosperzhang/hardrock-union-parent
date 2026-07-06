package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubProjectParticipantResponse", description = "pmhub 项目参建关系响应")
public class PmhubProjectParticipantResponse {

    @Schema(description = "项目参建关系 ID", example = "68262140034686977")
    private Long id;
    @Schema(description = "所属 pmhub 租户 ID", example = "2001")
    private Long tenantId;
    @Schema(description = "项目 ID", example = "20010001")
    private Long projectId;
    @Schema(description = "项目名称", example = "园区一期改造项目")
    private String projectName;
    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long participantCompanyId;
    @Schema(description = "参建单位名称", example = "甲劳务公司")
    private String participantCompanyName;
    @Schema(description = "项目角色", example = "LABOR_CONTRACTOR")
    private String participantRole;
    @Schema(description = "项目角色名称", example = "劳务分包")
    private String participantRoleLabel;
    @Schema(description = "是否主责单位 1是 0否", example = "0")
    private Integer isLead;
    @Schema(description = "状态 1启用 0停用", example = "1")
    private Integer status;
    @Schema(description = "状态名称", example = "启用")
    private String statusLabel;
    @Schema(description = "备注", example = "参与园区一期项目")
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public String getParticipantCompanyName() { return participantCompanyName; }
    public void setParticipantCompanyName(String participantCompanyName) { this.participantCompanyName = participantCompanyName; }
    public String getParticipantRole() { return participantRole; }
    public void setParticipantRole(String participantRole) { this.participantRole = participantRole; }
    public String getParticipantRoleLabel() { return participantRoleLabel; }
    public void setParticipantRoleLabel(String participantRoleLabel) { this.participantRoleLabel = participantRoleLabel; }
    public Integer getIsLead() { return isLead; }
    public void setIsLead(Integer isLead) { this.isLead = isLead; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

package com.hardrockunion.business.project.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("project_site_participant")
public class NexisSiteParticipant extends BaseEntity {

    private Long tenantId;
    private Long projectId;
    private Long siteId;
    private Long participantCompanyId;
    private String participantRole;
    private Integer status;
    private String remark;
    private Long createdBy;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }
    public Long getParticipantCompanyId() { return participantCompanyId; }
    public void setParticipantCompanyId(Long participantCompanyId) { this.participantCompanyId = participantCompanyId; }
    public String getParticipantRole() { return participantRole; }
    public void setParticipantRole(String participantRole) { this.participantRole = participantRole; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

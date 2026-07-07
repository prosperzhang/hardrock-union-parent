package com.hardrockunion.business.project.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("project_site_work_scope")
public class NexisSiteWorkScope extends BaseEntity {

    private Long tenantId;
    private Long projectId;
    private Long siteId;
    private Long participantCompanyId;
    private String scopeType;
    private String scopeName;
    private String scopeStartCode;
    private String scopeEndCode;
    private String scopeRemark;
    private Integer status;
    private Long createdBy;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

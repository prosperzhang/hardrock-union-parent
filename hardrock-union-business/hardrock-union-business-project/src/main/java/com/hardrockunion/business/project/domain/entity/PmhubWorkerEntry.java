package com.hardrockunion.business.project.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("project_worker_entry")
public class PmhubWorkerEntry extends BaseEntity {

    private Long tenantId;
    private String entryNo;
    private Long projectId;
    private Long siteId;
    private Long participantCompanyId;
    private Long workScopeId;
    private Long teamId;
    private Long workerId;
    private String realNameStatus;
    private String entryStatus;
    private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
    private String remark;
    private Integer status;
    private Long createdBy;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getEntryNo() { return entryNo; }
    public void setEntryNo(String entryNo) { this.entryNo = entryNo; }
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
    public String getRealNameStatus() { return realNameStatus; }
    public void setRealNameStatus(String realNameStatus) { this.realNameStatus = realNameStatus; }
    public String getEntryStatus() { return entryStatus; }
    public void setEntryStatus(String entryStatus) { this.entryStatus = entryStatus; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
    public void setEnteredAt(LocalDateTime enteredAt) { this.enteredAt = enteredAt; }
    public LocalDateTime getExitedAt() { return exitedAt; }
    public void setExitedAt(LocalDateTime exitedAt) { this.exitedAt = exitedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

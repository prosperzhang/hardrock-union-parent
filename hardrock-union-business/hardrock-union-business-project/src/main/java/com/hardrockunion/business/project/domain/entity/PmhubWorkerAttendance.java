package com.hardrockunion.business.project.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("project_worker_attendance")
public class PmhubWorkerAttendance extends BaseEntity {

    private Long tenantId;
    private String attendanceNo;
    private LocalDate attendanceDate;
    private Long projectId;
    private Long siteId;
    private Long participantCompanyId;
    private Long workScopeId;
    private Long teamId;
    private Long workerId;
    private Long entryId;
    private String attendanceStatus;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private String remark;
    private Integer status;
    private Long createdBy;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getAttendanceNo() { return attendanceNo; }
    public void setAttendanceNo(String attendanceNo) { this.attendanceNo = attendanceNo; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
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
    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public LocalDateTime getCheckInAt() { return checkInAt; }
    public void setCheckInAt(LocalDateTime checkInAt) { this.checkInAt = checkInAt; }
    public LocalDateTime getCheckOutAt() { return checkOutAt; }
    public void setCheckOutAt(LocalDateTime checkOutAt) { this.checkOutAt = checkOutAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

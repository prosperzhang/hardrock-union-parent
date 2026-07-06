package com.hardrockunion.solution.pmhub.dashboard.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PmhubAttendanceSnapshotResponse {

    private Long id;
    private String attendanceNo;
    private LocalDate attendanceDate;
    private String siteName;
    private String teamName;
    private String workerName;
    private String attendanceStatus;
    private String attendanceStatusLabel;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAttendanceNo() { return attendanceNo; }
    public void setAttendanceNo(String attendanceNo) { this.attendanceNo = attendanceNo; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public String getAttendanceStatusLabel() { return attendanceStatusLabel; }
    public void setAttendanceStatusLabel(String attendanceStatusLabel) { this.attendanceStatusLabel = attendanceStatusLabel; }
    public LocalDateTime getCheckInAt() { return checkInAt; }
    public void setCheckInAt(LocalDateTime checkInAt) { this.checkInAt = checkInAt; }
    public LocalDateTime getCheckOutAt() { return checkOutAt; }
    public void setCheckOutAt(LocalDateTime checkOutAt) { this.checkOutAt = checkOutAt; }
}

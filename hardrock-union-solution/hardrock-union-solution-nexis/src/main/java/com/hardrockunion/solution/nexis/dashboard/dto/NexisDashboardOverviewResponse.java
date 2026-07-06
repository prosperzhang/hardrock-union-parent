package com.hardrockunion.solution.nexis.dashboard.dto;

import java.util.List;

public class NexisDashboardOverviewResponse {

    private Long tenantId;
    private long siteCount;
    private long activeSiteCount;
    private long managerCount;
    private long teamCount;
    private long workerCount;
    private long currentEntryCount;
    private long todayAttendanceCount;
    private long todayCheckedInCount;
    private long todayCheckedOutCount;
    private List<NexisSiteSnapshotResponse> latestSites;
    private List<NexisAttendanceSnapshotResponse> latestAttendances;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public long getSiteCount() { return siteCount; }
    public void setSiteCount(long siteCount) { this.siteCount = siteCount; }
    public long getActiveSiteCount() { return activeSiteCount; }
    public void setActiveSiteCount(long activeSiteCount) { this.activeSiteCount = activeSiteCount; }
    public long getManagerCount() { return managerCount; }
    public void setManagerCount(long managerCount) { this.managerCount = managerCount; }
    public long getTeamCount() { return teamCount; }
    public void setTeamCount(long teamCount) { this.teamCount = teamCount; }
    public long getWorkerCount() { return workerCount; }
    public void setWorkerCount(long workerCount) { this.workerCount = workerCount; }
    public long getCurrentEntryCount() { return currentEntryCount; }
    public void setCurrentEntryCount(long currentEntryCount) { this.currentEntryCount = currentEntryCount; }
    public long getTodayAttendanceCount() { return todayAttendanceCount; }
    public void setTodayAttendanceCount(long todayAttendanceCount) { this.todayAttendanceCount = todayAttendanceCount; }
    public long getTodayCheckedInCount() { return todayCheckedInCount; }
    public void setTodayCheckedInCount(long todayCheckedInCount) { this.todayCheckedInCount = todayCheckedInCount; }
    public long getTodayCheckedOutCount() { return todayCheckedOutCount; }
    public void setTodayCheckedOutCount(long todayCheckedOutCount) { this.todayCheckedOutCount = todayCheckedOutCount; }
    public List<NexisSiteSnapshotResponse> getLatestSites() { return latestSites; }
    public void setLatestSites(List<NexisSiteSnapshotResponse> latestSites) { this.latestSites = latestSites; }
    public List<NexisAttendanceSnapshotResponse> getLatestAttendances() { return latestAttendances; }
    public void setLatestAttendances(List<NexisAttendanceSnapshotResponse> latestAttendances) { this.latestAttendances = latestAttendances; }
}

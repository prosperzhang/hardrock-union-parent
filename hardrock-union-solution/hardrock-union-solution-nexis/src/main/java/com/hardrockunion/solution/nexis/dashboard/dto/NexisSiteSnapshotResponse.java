package com.hardrockunion.solution.nexis.dashboard.dto;

public class NexisSiteSnapshotResponse {

    private Long id;
    private String siteName;
    private String projectName;
    private String managerName;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

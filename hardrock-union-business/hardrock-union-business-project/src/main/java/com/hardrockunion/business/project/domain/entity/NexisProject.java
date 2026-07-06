package com.hardrockunion.business.project.domain.entity;

public class NexisProject {

    private Long id;
    private Long tenantId;
    private String projectName;
    private String projectCode;
    private String projectAddress;
    private String managerName;
    private String managerPhone;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public String getProjectAddress() { return projectAddress; }
    public void setProjectAddress(String projectAddress) { this.projectAddress = projectAddress; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getManagerPhone() { return managerPhone; }
    public void setManagerPhone(String managerPhone) { this.managerPhone = managerPhone; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

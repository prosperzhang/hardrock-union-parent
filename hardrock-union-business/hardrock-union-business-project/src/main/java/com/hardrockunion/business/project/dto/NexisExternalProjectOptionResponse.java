package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "可承接外部关联的正式项目选项")
public class NexisExternalProjectOptionResponse {

    private Long tenantId;
    private Long parentTenantId;
    private String tenantCode;
    private String tenantName;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getParentTenantId() { return parentTenantId; }
    public void setParentTenantId(Long parentTenantId) { this.parentTenantId = parentTenantId; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
}

package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisParticipantCompanyResponse", description = "nexis 参建单位响应")
public class NexisParticipantCompanyResponse {

    @Schema(description = "参建单位 ID", example = "68262140034686976")
    private Long id;
    @Schema(description = "所属 nexis 租户 ID", example = "2001")
    private Long tenantId;
    @Schema(description = "绑定的平台租户 ID；为空表示外部单位", example = "2001")
    private Long bindTenantId;
    @Schema(description = "参建单位名称", example = "甲劳务公司")
    private String companyName;
    @Schema(description = "参建单位编码", example = "LAB-JIA-001")
    private String companyCode;
    @Schema(description = "单位类型", example = "LABOR_CONTRACTOR")
    private String companyType;
    @Schema(description = "单位类型名称", example = "劳务分包单位")
    private String companyTypeLabel;
    @Schema(description = "联系人", example = "张工")
    private String contactName;
    @Schema(description = "联系电话", example = "13800000001")
    private String contactPhone;
    @Schema(description = "状态 1启用 0停用", example = "1")
    private Integer status;
    @Schema(description = "状态名称", example = "启用")
    private String statusLabel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getBindTenantId() { return bindTenantId; }
    public void setBindTenantId(Long bindTenantId) { this.bindTenantId = bindTenantId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }
    public String getCompanyTypeLabel() { return companyTypeLabel; }
    public void setCompanyTypeLabel(String companyTypeLabel) { this.companyTypeLabel = companyTypeLabel; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
}

package com.hardrockunion.business.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NexisParticipantCompanyCreateRequest", description = "nexis 参建单位创建请求")
public class NexisParticipantCompanyCreateRequest {

    @Schema(description = "绑定的平台租户 ID；外部单位可不传", example = "2001")
    private Long bindTenantId;
    @Schema(description = "参建单位名称", example = "甲劳务公司")
    private String companyName;
    @Schema(description = "参建单位编码", example = "LAB-JIA-001")
    private String companyCode;
    @Schema(description = "单位类型，可通过 /api/nexis/dictionaries/participant-company-types 获取字典", example = "LABOR_CONTRACTOR",
        allowableValues = {"GENERAL_CONTRACTOR", "SPECIALTY_CONTRACTOR", "LABOR_CONTRACTOR", "SUPPLIER", "OWNER", "SUPERVISOR"})
    private String companyType;
    @Schema(description = "联系人", example = "张工")
    private String contactName;
    @Schema(description = "联系电话", example = "13800000001")
    private String contactPhone;

    public Long getBindTenantId() { return bindTenantId; }
    public void setBindTenantId(Long bindTenantId) { this.bindTenantId = bindTenantId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}

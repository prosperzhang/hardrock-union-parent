package com.hardrockunion.business.project.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PmhubParticipantCompanyQueryRequest", description = "pmhub 参建单位分页查询条件")
public class PmhubParticipantCompanyQueryRequest extends PageRequest {

    @Schema(description = "关键词，匹配单位名称、单位编码、联系人和联系电话", example = "甲劳务")
    private String keyword;

    @Schema(description = "单位类型筛选，可通过 /api/pmhub/dictionaries/participant-company-types 获取字典", example = "LABOR_CONTRACTOR",
        allowableValues = {"GENERAL_CONTRACTOR", "SPECIALTY_CONTRACTOR", "LABOR_CONTRACTOR", "SUPPLIER", "OWNER", "SUPERVISOR"})
    private String companyType;

    @Schema(description = "绑定的平台租户 ID", example = "2001")
    private Long bindTenantId;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }
    public Long getBindTenantId() { return bindTenantId; }
    public void setBindTenantId(Long bindTenantId) { this.bindTenantId = bindTenantId; }
}

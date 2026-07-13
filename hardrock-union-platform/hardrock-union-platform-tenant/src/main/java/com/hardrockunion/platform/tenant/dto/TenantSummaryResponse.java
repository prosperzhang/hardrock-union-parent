package com.hardrockunion.platform.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "项目租户响应。")
public class TenantSummaryResponse {

    @Schema(description = "项目租户ID", example = "75668854082945026")
    private Long tenantId;
    @Schema(description = "父级租户ID。NEXIS 项目可挂到公司下", example = "75668854082945026")
    private Long parentTenantId;
    @Schema(description = "父级租户名称", example = "陕西某某建设有限公司")
    private String parentTenantName;
    @Schema(description = "父级租户编码", example = "NEXIS-COMPANY-ABC12345")
    private String parentTenantCode;
    @Schema(description = "租户类型。NEXIS 支持 COMPANY、PROJECT", example = "PROJECT")
    private String tenantType;
    @Schema(description = "项目租户名称", example = "张栋俊测试项目A")
    private String tenantName;
    @Schema(description = "项目租户编码", example = "NEXIS-20260423120000")
    private String tenantCode;
    @Schema(description = "租户来源，例如 PRIMELOAD_SELF_OPERATED 表示一车好料商城自营", example = "PRIMELOAD_SELF_OPERATED")
    private String tenantSource;
    @Schema(description = "项目地址", example = "苏州工业园区星湖街 99 号")
    private String projectAddress;
    @Schema(description = "省级行政区编码", example = "610000")
    private String provinceCode;
    @Schema(description = "省级行政区名称", example = "陕西省")
    private String provinceName;
    @Schema(description = "市级行政区编码", example = "610500")
    private String cityCode;
    @Schema(description = "市级行政区名称", example = "渭南市")
    private String cityName;
    @Schema(description = "区县行政区编码", example = "610523")
    private String districtCode;
    @Schema(description = "区县行政区名称", example = "大荔县")
    private String districtName;
    @Schema(description = "项目负责人姓名", example = "张栋俊")
    private String managerName;
    @Schema(description = "项目负责人手机号", example = "13800138000")
    private String managerPhone;
    @Schema(description = "外部上级单位名称。用于 A 公司未入驻 Nexis 时记录上级单位", example = "A公司")
    private String externalOwnerName;
    @Schema(description = "外部上级项目名称。用于记录对方真实大项目名称", example = "星河湾一期总承包项目")
    private String externalProjectName;
    @Schema(description = "我的承包范围/施工段名称", example = "1#楼主体结构劳务")
    private String contractScopeName;
    @Schema(description = "状态，1启用，0停用", example = "1")
    private Integer status;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getParentTenantId() {
        return parentTenantId;
    }

    public void setParentTenantId(Long parentTenantId) {
        this.parentTenantId = parentTenantId;
    }

    public String getParentTenantName() {
        return parentTenantName;
    }

    public void setParentTenantName(String parentTenantName) {
        this.parentTenantName = parentTenantName;
    }

    public String getParentTenantCode() {
        return parentTenantCode;
    }

    public void setParentTenantCode(String parentTenantCode) {
        this.parentTenantCode = parentTenantCode;
    }

    public String getTenantType() {
        return tenantType;
    }

    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getTenantSource() {
        return tenantSource;
    }

    public void setTenantSource(String tenantSource) {
        this.tenantSource = tenantSource;
    }

    public String getProjectAddress() {
        return projectAddress;
    }

    public void setProjectAddress(String projectAddress) {
        this.projectAddress = projectAddress;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    public String getExternalOwnerName() {
        return externalOwnerName;
    }

    public void setExternalOwnerName(String externalOwnerName) {
        this.externalOwnerName = externalOwnerName;
    }

    public String getExternalProjectName() {
        return externalProjectName;
    }

    public void setExternalProjectName(String externalProjectName) {
        this.externalProjectName = externalProjectName;
    }

    public String getContractScopeName() {
        return contractScopeName;
    }

    public void setContractScopeName(String contractScopeName) {
        this.contractScopeName = contractScopeName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

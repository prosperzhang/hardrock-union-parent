package com.hardrockunion.platform.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "创建租户空间请求。")
public class TenantCreateRequest {

    @Schema(description = "租户类型。NEXIS 支持 GROUP、COMPANY、PROJECT；不传时按当前 app 默认类型创建。", example = "PROJECT")
    private String tenantType;
    @Schema(description = "父级租户ID。NEXIS 项目可填写所属公司或集团租户ID。", example = "75668854082945026")
    private Long parentTenantId;
    @Schema(description = "租户空间名称。NEXIS 为项目名称，PRIMELOAD-MARKETPLACE 为商户名称。", example = "张栋俊测试项目A")
    private String tenantName;
    @Schema(description = "租户空间编码。不传时系统按 app 自动生成。", example = "NEXIS-20260423120000")
    private String tenantCode;
    @Schema(description = "地址。NEXIS 为项目地址，PRIMELOAD-MARKETPLACE 可作为商户经营地址。", example = "苏州工业园区星湖街 99 号")
    private String projectAddress;
    @Schema(description = "省级行政区编码", example = "610000")
    private String provinceCode;
    @Schema(description = "省级行政区名称", example = "陕西省")
    private String provinceName;
    @Schema(description = "市级行政区编码。直辖市可与 provinceCode 相同。", example = "610500")
    private String cityCode;
    @Schema(description = "市级行政区名称。直辖市可与 provinceName 相同。", example = "渭南市")
    private String cityName;
    @Schema(description = "区县行政区编码", example = "610523")
    private String districtCode;
    @Schema(description = "区县行政区名称", example = "大荔县")
    private String districtName;
    @Schema(description = "负责人姓名", example = "张栋俊")
    private String managerName;
    @Schema(description = "负责人手机号", example = "13800138000")
    private String managerPhone;

    public String getTenantType() {
        return tenantType;
    }

    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }

    public Long getParentTenantId() {
        return parentTenantId;
    }

    public void setParentTenantId(Long parentTenantId) {
        this.parentTenantId = parentTenantId;
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
}

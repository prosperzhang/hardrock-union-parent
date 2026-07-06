package com.hardrockunion.business.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商户资料响应。商户主档来自 tenant_registry。")
public class MerchantProfileResponse {

    @Schema(description = "商户租户ID", example = "79693762425421831")
    private Long tenantId;

    @Schema(description = "商户编码", example = "PRIMELOAD-MARKETPLACE-20260503-181350-2280F1C3")
    private String merchantCode;

    @Schema(description = "商户名称", example = "苏州某某五金商行")
    private String merchantName;

    @Schema(description = "商户类型", example = "MERCHANT")
    private String merchantType;

    @Schema(description = "经营地址", example = "苏州工业园区星湖街 99 号")
    private String businessAddress;

    @Schema(description = "负责人姓名", example = "张栋俊")
    private String managerName;

    @Schema(description = "负责人手机号", example = "13800138000")
    private String managerPhone;

    @Schema(description = "状态 1启用 0停用", example = "1")
    private Integer status;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

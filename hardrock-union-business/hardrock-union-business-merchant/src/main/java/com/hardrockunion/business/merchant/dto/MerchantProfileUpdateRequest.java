package com.hardrockunion.business.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商户资料更新请求。商户身份仍由 tenant_registry 表达。")
public class MerchantProfileUpdateRequest {

    @Schema(description = "商户名称", example = "苏州某某五金商行")
    private String merchantName;

    @Schema(description = "经营地址", example = "苏州工业园区星湖街 99 号")
    private String businessAddress;

    @Schema(description = "负责人姓名", example = "张栋俊")
    private String managerName;

    @Schema(description = "负责人手机号", example = "13800138000")
    private String managerPhone;

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
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
}

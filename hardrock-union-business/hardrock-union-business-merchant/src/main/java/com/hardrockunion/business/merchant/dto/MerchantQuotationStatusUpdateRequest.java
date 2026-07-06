package com.hardrockunion.business.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 报价单状态流转请求。
 */
@Schema(name = "MerchantQuotationStatusUpdateRequest", description = "报价单状态流转请求")
public class MerchantQuotationStatusUpdateRequest {

    // 目标状态，当前支持 ISSUED、CANCELLED。
    @Schema(description = "目标状态", example = "CANCELLED")
    private String targetStatus;

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }
}

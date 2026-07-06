package com.hardrockunion.business.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 订单状态流转请求。
 */
@Schema(name = "MerchantOrderStatusUpdateRequest", description = "订单状态流转请求")
public class MerchantOrderStatusUpdateRequest {

    // 目标状态，例如 ACCEPTED、RECEIVED、COMPLETED、CANCELLED、AFTER_SALE。
    @Schema(description = "目标状态", example = "ACCEPTED")
    private String targetStatus;

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }
}

package com.hardrockunion.business.merchant.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 订单发货请求。
 */
@Schema(name = "MerchantOrderShipRequest", description = "订单发货请求")
public class MerchantOrderShipRequest {

    // 发货仓库；不传时沿用订单上已指定的仓库。
    @Schema(description = "发货仓库 ID；不传时沿用订单中已指定的仓库", example = "3100001")
    private Long warehouseId;
    // 物流公司名称。
    @Schema(description = "物流公司名称", example = "顺丰速运")
    private String logisticsCompany;
    // 运单号。
    @Schema(description = "运单号", example = "SF123456789CN")
    private String trackingNo;
    // 发货时间；不传时默认取当前时间。
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发货时间，格式 yyyy-MM-dd HH:mm:ss", example = "2026-03-29 14:30:00")
    private LocalDateTime shippedAt;
    // 发货备注。
    @Schema(description = "发货备注", example = "今天下午装车发出")
    private String shippingRemark;

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    public String getShippingRemark() { return shippingRemark; }
    public void setShippingRemark(String shippingRemark) { this.shippingRemark = shippingRemark; }
}

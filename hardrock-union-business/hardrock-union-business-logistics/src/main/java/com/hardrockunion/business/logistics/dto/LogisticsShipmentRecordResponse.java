package com.hardrockunion.business.logistics.dto;

import java.time.LocalDateTime;

/**
 * 供应链发货记录返回模型。
 */
public class LogisticsShipmentRecordResponse {

    private Long id;
    private Long businessAppId;
    private String businessAppCode;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private String logisticsCompany;
    private String trackingNo;
    private LocalDateTime shippedAt;
    private String shippingRemark;
    private String shipmentStatus;
    private LocalDateTime invalidatedAt;
    private String invalidatedRemark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessAppId() { return businessAppId; }
    public void setBusinessAppId(Long businessAppId) { this.businessAppId = businessAppId; }
    public String getBusinessAppCode() { return businessAppCode; }
    public void setBusinessAppCode(String businessAppCode) { this.businessAppCode = businessAppCode; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    public String getShippingRemark() { return shippingRemark; }
    public void setShippingRemark(String shippingRemark) { this.shippingRemark = shippingRemark; }
    public String getShipmentStatus() { return shipmentStatus; }
    public void setShipmentStatus(String shipmentStatus) { this.shipmentStatus = shipmentStatus; }
    public LocalDateTime getInvalidatedAt() { return invalidatedAt; }
    public void setInvalidatedAt(LocalDateTime invalidatedAt) { this.invalidatedAt = invalidatedAt; }
    public String getInvalidatedRemark() { return invalidatedRemark; }
    public void setInvalidatedRemark(String invalidatedRemark) { this.invalidatedRemark = invalidatedRemark; }
}

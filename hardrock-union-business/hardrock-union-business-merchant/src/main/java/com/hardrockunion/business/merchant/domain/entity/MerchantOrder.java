package com.hardrockunion.business.merchant.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("merchant_order")
public class MerchantOrder extends BaseEntity {

    private Long tenantId;
    private String orderNo;
    private Long merchantId;
    private String merchantName;
    private Long targetAppId;
    private String targetAppCode;
    private Long targetTenantId;
    private String targetProjectName;
    private String targetSiteName;
    private String targetUserName;
    private String targetUserPhone;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private Long warehouseId;
    private String warehouseName;
    private String logisticsCompany;
    private String trackingNo;
    private LocalDateTime shippedAt;
    private String shippingRemark;
    private String orderStatus;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private String remark;
    private Long createdBy;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public Long getTargetAppId() { return targetAppId; }
    public void setTargetAppId(Long targetAppId) { this.targetAppId = targetAppId; }
    public String getTargetAppCode() { return targetAppCode; }
    public void setTargetAppCode(String targetAppCode) { this.targetAppCode = targetAppCode; }
    public Long getTargetTenantId() { return targetTenantId; }
    public void setTargetTenantId(Long targetTenantId) { this.targetTenantId = targetTenantId; }
    public String getTargetProjectName() { return targetProjectName; }
    public void setTargetProjectName(String targetProjectName) { this.targetProjectName = targetProjectName; }
    public String getTargetSiteName() { return targetSiteName; }
    public void setTargetSiteName(String targetSiteName) { this.targetSiteName = targetSiteName; }
    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
    public String getTargetUserPhone() { return targetUserPhone; }
    public void setTargetUserPhone(String targetUserPhone) { this.targetUserPhone = targetUserPhone; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    public String getShippingRemark() { return shippingRemark; }
    public void setShippingRemark(String shippingRemark) { this.shippingRemark = shippingRemark; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

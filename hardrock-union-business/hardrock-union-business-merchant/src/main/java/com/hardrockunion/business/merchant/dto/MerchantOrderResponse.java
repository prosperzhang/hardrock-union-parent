package com.hardrockunion.business.merchant.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单返回模型。
 *
 * <p>除了订单自身信息，也会带回交易目标、发货信息和订单明细。
 */
public class MerchantOrderResponse {

    // 订单主键。
    private Long id;
    // 当前商户所在租户。
    private Long tenantId;
    // 订单号。
    private String orderNo;
    // 商户租户 ID。
    private Long merchantId;
    // 商户租户名称。
    private String merchantName;
    // 目标应用 ID。
    private Long targetAppId;
    // 目标应用，当前默认是 PMHUB。
    private String targetAppCode;
    // 目标租户。
    private Long targetTenantId;
    // 目标项目名称。
    private String targetProjectName;
    // 目标工地名称。
    private String targetSiteName;
    // 目标联系人姓名。
    private String targetUserName;
    // 目标联系人手机号。
    private String targetUserPhone;
    // 兼容旧接口字段，内部语义等同于 targetUserName。
    private String customerName;
    // 兼容旧接口字段，内部语义等同于 targetUserPhone。
    private String customerPhone;
    // 收货地址。
    private String deliveryAddress;
    // 发货仓库 ID。
    private Long warehouseId;
    // 发货仓库名称。
    private String warehouseName;
    // 物流公司。
    private String logisticsCompany;
    // 运单号。
    private String trackingNo;
    // 发货时间。
    private LocalDateTime shippedAt;
    // 发货备注。
    private String shippingRemark;
    // 订单状态。
    private String orderStatus;
    // 订单总件数。
    private Integer itemCount;
    // 订单总金额。
    private BigDecimal totalAmount;
    // 备注。
    private String remark;
    // 订单明细。
    private List<MerchantOrderItemResponse> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public List<MerchantOrderItemResponse> getItems() { return items; }
    public void setItems(List<MerchantOrderItemResponse> items) { this.items = items; }
}

package com.hardrockunion.business.merchant.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报价单返回模型。
 *
 * <p>除了报价单自身信息，也会带回指向 `pmhub` 的交易对象信息和报价明细。
 */
public class MerchantQuotationResponse {

    // 报价单主键。
    private Long id;
    // 当前商户所在租户。
    private Long tenantId;
    // 报价单号。
    private String quotationNo;
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
    // 计划发货仓库 ID。
    private Long warehouseId;
    // 计划发货仓库名称。
    private String warehouseName;
    // 计划物流公司。
    private String logisticsCompany;
    // 报价单状态。
    private String quotationStatus;
    // 报价有效期。
    private LocalDateTime validUntil;
    // 报价单总件数。
    private Integer itemCount;
    // 报价总金额。
    private BigDecimal totalAmount;
    // 备注。
    private String remark;
    // 报价明细。
    private List<MerchantQuotationItemResponse> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getQuotationNo() { return quotationNo; }
    public void setQuotationNo(String quotationNo) { this.quotationNo = quotationNo; }
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
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getQuotationStatus() { return quotationStatus; }
    public void setQuotationStatus(String quotationStatus) { this.quotationStatus = quotationStatus; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<MerchantQuotationItemResponse> getItems() { return items; }
    public void setItems(List<MerchantQuotationItemResponse> items) { this.items = items; }
}

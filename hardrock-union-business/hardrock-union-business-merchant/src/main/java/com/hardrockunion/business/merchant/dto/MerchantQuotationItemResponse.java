package com.hardrockunion.business.merchant.dto;

import java.math.BigDecimal;

/**
 * 报价单明细返回模型。
 */
public class MerchantQuotationItemResponse {

    // 明细主键。
    private Long id;
    // 商品 ID。
    private Long productId;
    // 商品名称。
    private String productName;
    // SKU 编码。
    private String skuCode;
    // 销售单位。
    private String unit;
    // 报价数量。
    private Integer quantity;
    // 报价单价。
    private BigDecimal quotationPrice;
    // 当前明细金额。
    private BigDecimal lineAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getQuotationPrice() { return quotationPrice; }
    public void setQuotationPrice(BigDecimal quotationPrice) { this.quotationPrice = quotationPrice; }
    public BigDecimal getLineAmount() { return lineAmount; }
    public void setLineAmount(BigDecimal lineAmount) { this.lineAmount = lineAmount; }
}

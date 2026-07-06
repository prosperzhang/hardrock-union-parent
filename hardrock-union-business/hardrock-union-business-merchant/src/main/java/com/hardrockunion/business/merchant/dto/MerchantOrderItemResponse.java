package com.hardrockunion.business.merchant.dto;

import java.math.BigDecimal;

/**
 * 订单明细返回模型。
 */
public class MerchantOrderItemResponse {

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
    // 下单数量。
    private Integer quantity;
    // 成交单价。
    private BigDecimal salePrice;
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
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public BigDecimal getLineAmount() { return lineAmount; }
    public void setLineAmount(BigDecimal lineAmount) { this.lineAmount = lineAmount; }
}

package com.hardrockunion.business.merchant.dto;

import java.math.BigDecimal;

/**
 * 报价单明细创建请求。
 */
public class MerchantQuotationItemCreateRequest {

    // 报价商品 ID。
    private Long productId;
    // 报价数量。
    private Integer quantity;
    // 报价单价；不传时会回落到商品当前销售价。
    private BigDecimal quotationPrice;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getQuotationPrice() { return quotationPrice; }
    public void setQuotationPrice(BigDecimal quotationPrice) { this.quotationPrice = quotationPrice; }
}

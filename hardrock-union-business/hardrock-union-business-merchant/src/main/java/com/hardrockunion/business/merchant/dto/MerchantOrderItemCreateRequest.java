package com.hardrockunion.business.merchant.dto;

import java.math.BigDecimal;

/**
 * 订单明细创建请求。
 */
public class MerchantOrderItemCreateRequest {

    // 下单商品 ID。
    private Long productId;
    // 下单数量。
    private Integer quantity;
    // 下单成交单价；报价转订单时会显式带入报价价。
    private BigDecimal salePrice;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
}

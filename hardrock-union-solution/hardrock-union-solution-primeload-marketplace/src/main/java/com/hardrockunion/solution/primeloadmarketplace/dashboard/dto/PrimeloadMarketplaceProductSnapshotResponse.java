package com.hardrockunion.solution.primeloadmarketplace.dashboard.dto;

import java.math.BigDecimal;

public class PrimeloadMarketplaceProductSnapshotResponse {

    private Long id;
    private String productName;
    private String skuCode;
    private BigDecimal salePrice;
    private Integer stockQuantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}

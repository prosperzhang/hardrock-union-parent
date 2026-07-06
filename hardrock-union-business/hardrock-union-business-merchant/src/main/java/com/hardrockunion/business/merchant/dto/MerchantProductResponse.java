package com.hardrockunion.business.merchant.dto;

import java.math.BigDecimal;

public class MerchantProductResponse {

    private Long id;
    private Long tenantId;
    private String productName;
    private String categoryCode;
    private String skuCode;
    private String mainImageUrl;
    private String brandName;
    private String specModel;
    private String material;
    private String productDescription;
    private String unit;
    private BigDecimal salePrice;
    private BigDecimal effectiveSalePrice;
    private Integer priceRegionLevel;
    private String priceRegionCode;
    private String priceRegionName;
    private Integer stockQuantity;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public BigDecimal getEffectiveSalePrice() { return effectiveSalePrice; }
    public void setEffectiveSalePrice(BigDecimal effectiveSalePrice) { this.effectiveSalePrice = effectiveSalePrice; }
    public Integer getPriceRegionLevel() { return priceRegionLevel; }
    public void setPriceRegionLevel(Integer priceRegionLevel) { this.priceRegionLevel = priceRegionLevel; }
    public String getPriceRegionCode() { return priceRegionCode; }
    public void setPriceRegionCode(String priceRegionCode) { this.priceRegionCode = priceRegionCode; }
    public String getPriceRegionName() { return priceRegionName; }
    public void setPriceRegionName(String priceRegionName) { this.priceRegionName = priceRegionName; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

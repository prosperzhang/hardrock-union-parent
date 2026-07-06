package com.hardrockunion.business.warehouse.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("warehouse_stock")
public class WarehouseStock extends BaseEntity {

    private String appCode;
    private Long tenantId;
    private Long warehouseId;
    private Long productId;
    private String productName;
    private String skuCode;
    private String unit;
    private Integer stockQuantity;
    private Long createdBy;

    public String getAppCode() { return appCode; }
    public void setAppCode(String appCode) { this.appCode = appCode; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

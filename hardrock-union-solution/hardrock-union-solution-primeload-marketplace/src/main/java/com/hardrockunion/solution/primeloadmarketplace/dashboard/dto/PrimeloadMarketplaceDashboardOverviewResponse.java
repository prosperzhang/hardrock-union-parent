package com.hardrockunion.solution.primeloadmarketplace.dashboard.dto;

import java.util.List;

public class PrimeloadMarketplaceDashboardOverviewResponse {

    private Long tenantId;
    private String merchantName;
    private long productCount;
    private long activeProductCount;
    private long lowStockProductCount;
    private long quotationCount;
    private long issuedQuotationCount;
    private long expiredQuotationCount;
    private long orderCount;
    private long pendingShipmentOrderCount;
    private long shippedOrderCount;
    private long completedOrderCount;
    private long warehouseCount;
    private long activeWarehouseCount;
    private long warehouseStockQuantity;
    private List<PrimeloadMarketplaceProductSnapshotResponse> latestProducts;
    private List<PrimeloadMarketplaceQuotationSnapshotResponse> latestQuotations;
    private List<PrimeloadMarketplaceOrderSnapshotResponse> latestOrders;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public long getProductCount() { return productCount; }
    public void setProductCount(long productCount) { this.productCount = productCount; }
    public long getActiveProductCount() { return activeProductCount; }
    public void setActiveProductCount(long activeProductCount) { this.activeProductCount = activeProductCount; }
    public long getLowStockProductCount() { return lowStockProductCount; }
    public void setLowStockProductCount(long lowStockProductCount) { this.lowStockProductCount = lowStockProductCount; }
    public long getQuotationCount() { return quotationCount; }
    public void setQuotationCount(long quotationCount) { this.quotationCount = quotationCount; }
    public long getIssuedQuotationCount() { return issuedQuotationCount; }
    public void setIssuedQuotationCount(long issuedQuotationCount) { this.issuedQuotationCount = issuedQuotationCount; }
    public long getExpiredQuotationCount() { return expiredQuotationCount; }
    public void setExpiredQuotationCount(long expiredQuotationCount) { this.expiredQuotationCount = expiredQuotationCount; }
    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    public long getPendingShipmentOrderCount() { return pendingShipmentOrderCount; }
    public void setPendingShipmentOrderCount(long pendingShipmentOrderCount) { this.pendingShipmentOrderCount = pendingShipmentOrderCount; }
    public long getShippedOrderCount() { return shippedOrderCount; }
    public void setShippedOrderCount(long shippedOrderCount) { this.shippedOrderCount = shippedOrderCount; }
    public long getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(long completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public long getWarehouseCount() { return warehouseCount; }
    public void setWarehouseCount(long warehouseCount) { this.warehouseCount = warehouseCount; }
    public long getActiveWarehouseCount() { return activeWarehouseCount; }
    public void setActiveWarehouseCount(long activeWarehouseCount) { this.activeWarehouseCount = activeWarehouseCount; }
    public long getWarehouseStockQuantity() { return warehouseStockQuantity; }
    public void setWarehouseStockQuantity(long warehouseStockQuantity) { this.warehouseStockQuantity = warehouseStockQuantity; }
    public List<PrimeloadMarketplaceProductSnapshotResponse> getLatestProducts() { return latestProducts; }
    public void setLatestProducts(List<PrimeloadMarketplaceProductSnapshotResponse> latestProducts) { this.latestProducts = latestProducts; }
    public List<PrimeloadMarketplaceQuotationSnapshotResponse> getLatestQuotations() { return latestQuotations; }
    public void setLatestQuotations(List<PrimeloadMarketplaceQuotationSnapshotResponse> latestQuotations) { this.latestQuotations = latestQuotations; }
    public List<PrimeloadMarketplaceOrderSnapshotResponse> getLatestOrders() { return latestOrders; }
    public void setLatestOrders(List<PrimeloadMarketplaceOrderSnapshotResponse> latestOrders) { this.latestOrders = latestOrders; }
}

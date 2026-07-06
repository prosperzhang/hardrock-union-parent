package com.hardrockunion.solution.primeloadmarketplace.dashboard.dto;

import java.math.BigDecimal;

public class PrimeloadMarketplaceOrderSnapshotResponse {

    private Long id;
    private String orderNo;
    private String orderStatus;
    private BigDecimal totalAmount;
    private String targetProjectName;
    private String targetUserName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getTargetProjectName() { return targetProjectName; }
    public void setTargetProjectName(String targetProjectName) { this.targetProjectName = targetProjectName; }
    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
}

package com.hardrockunion.solution.primeloadmarketplace.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PrimeloadMarketplaceQuotationSnapshotResponse {

    private Long id;
    private String quotationNo;
    private String quotationStatus;
    private BigDecimal totalAmount;
    private LocalDateTime validUntil;
    private String targetProjectName;
    private String targetUserName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuotationNo() { return quotationNo; }
    public void setQuotationNo(String quotationNo) { this.quotationNo = quotationNo; }
    public String getQuotationStatus() { return quotationStatus; }
    public void setQuotationStatus(String quotationStatus) { this.quotationStatus = quotationStatus; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public String getTargetProjectName() { return targetProjectName; }
    public void setTargetProjectName(String targetProjectName) { this.targetProjectName = targetProjectName; }
    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
}

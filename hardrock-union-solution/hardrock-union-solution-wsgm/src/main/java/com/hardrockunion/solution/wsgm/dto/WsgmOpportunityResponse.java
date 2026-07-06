package com.hardrockunion.solution.wsgm.dto;

import java.math.BigDecimal;

public class WsgmOpportunityResponse {

    private Long id;
    private Long tenantId;
    private Long customerId;
    private String opportunityName;
    private String stageCode;
    private BigDecimal expectedAmount;
    private String expectedSignDate;
    private String remark;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public String getStageCode() { return stageCode; }
    public void setStageCode(String stageCode) { this.stageCode = stageCode; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
    public String getExpectedSignDate() { return expectedSignDate; }
    public void setExpectedSignDate(String expectedSignDate) { this.expectedSignDate = expectedSignDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

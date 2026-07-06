package com.hardrockunion.solution.wsgm.dto;

import java.math.BigDecimal;

public class WsgmOpportunityCreateRequest {

    private String opportunityName;
    private String stageCode;
    private BigDecimal expectedAmount;
    private String expectedSignDate;
    private String remark;

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
}

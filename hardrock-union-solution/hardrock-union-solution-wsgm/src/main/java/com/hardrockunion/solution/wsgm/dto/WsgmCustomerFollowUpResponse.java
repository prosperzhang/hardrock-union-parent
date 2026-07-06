package com.hardrockunion.solution.wsgm.dto;

public class WsgmCustomerFollowUpResponse {

    private Long id;
    private Long customerId;
    private String followUpType;
    private String followUpContent;
    private String nextAction;
    private String nextFollowUpAt;
    private Long createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getFollowUpType() { return followUpType; }
    public void setFollowUpType(String followUpType) { this.followUpType = followUpType; }
    public String getFollowUpContent() { return followUpContent; }
    public void setFollowUpContent(String followUpContent) { this.followUpContent = followUpContent; }
    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
    public String getNextFollowUpAt() { return nextFollowUpAt; }
    public void setNextFollowUpAt(String nextFollowUpAt) { this.nextFollowUpAt = nextFollowUpAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

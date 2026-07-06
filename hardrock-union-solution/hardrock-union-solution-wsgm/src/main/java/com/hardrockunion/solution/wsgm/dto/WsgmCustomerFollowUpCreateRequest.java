package com.hardrockunion.solution.wsgm.dto;

public class WsgmCustomerFollowUpCreateRequest {

    private String followUpType;
    private String followUpContent;
    private String nextAction;
    private String nextFollowUpAt;

    public String getFollowUpType() { return followUpType; }
    public void setFollowUpType(String followUpType) { this.followUpType = followUpType; }
    public String getFollowUpContent() { return followUpContent; }
    public void setFollowUpContent(String followUpContent) { this.followUpContent = followUpContent; }
    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
    public String getNextFollowUpAt() { return nextFollowUpAt; }
    public void setNextFollowUpAt(String nextFollowUpAt) { this.nextFollowUpAt = nextFollowUpAt; }
}

package com.hardrockunion.solution.wsgm.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("wsgm_customer_follow_up")
public class WsgmCustomerFollowUp extends BaseEntity {

    private Long tenantId;
    private Long customerId;
    private String followUpType;
    private String followUpContent;
    private String nextAction;
    private LocalDateTime nextFollowUpAt;
    private Long createdBy;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getFollowUpType() { return followUpType; }
    public void setFollowUpType(String followUpType) { this.followUpType = followUpType; }
    public String getFollowUpContent() { return followUpContent; }
    public void setFollowUpContent(String followUpContent) { this.followUpContent = followUpContent; }
    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
    public LocalDateTime getNextFollowUpAt() { return nextFollowUpAt; }
    public void setNextFollowUpAt(LocalDateTime nextFollowUpAt) { this.nextFollowUpAt = nextFollowUpAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

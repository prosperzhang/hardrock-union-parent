package com.hardrockunion.platform.message.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("message_recipient")
public class MessageRecipient extends BaseEntity {

    private Long appId;
    private String appCode;
    private Long tenantId;
    private Long threadId;
    private Long recordId;
    private Long receiverUserId;
    private Integer readFlag;
    private LocalDateTime readAt;
    private String recipientStatus;

    public Long getAppId() { return appId; }
    public void setAppId(Long appId) { this.appId = appId; }
    public String getAppCode() { return appCode; }
    public void setAppCode(String appCode) { this.appCode = appCode; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getThreadId() { return threadId; }
    public void setThreadId(Long threadId) { this.threadId = threadId; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(Long receiverUserId) { this.receiverUserId = receiverUserId; }
    public Integer getReadFlag() { return readFlag; }
    public void setReadFlag(Integer readFlag) { this.readFlag = readFlag; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public String getRecipientStatus() { return recipientStatus; }
    public void setRecipientStatus(String recipientStatus) { this.recipientStatus = recipientStatus; }
}

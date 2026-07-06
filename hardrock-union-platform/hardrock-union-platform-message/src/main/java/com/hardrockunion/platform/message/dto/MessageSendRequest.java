package com.hardrockunion.platform.message.dto;

import java.util.List;

public class MessageSendRequest {

    private Long tenantId;
    private String threadType;
    private String messageType;
    private List<Long> receiverUserIds;
    private String title;
    private String content;
    private String sourceType;
    private Long sourceId;
    private String actionUrl;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getThreadType() { return threadType; }
    public void setThreadType(String threadType) { this.threadType = threadType; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public List<Long> getReceiverUserIds() { return receiverUserIds; }
    public void setReceiverUserIds(List<Long> receiverUserIds) { this.receiverUserIds = receiverUserIds; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
}

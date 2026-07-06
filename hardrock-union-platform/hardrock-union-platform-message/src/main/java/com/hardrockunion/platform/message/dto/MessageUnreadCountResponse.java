package com.hardrockunion.platform.message.dto;

public class MessageUnreadCountResponse {

    private long unreadCount;

    public MessageUnreadCountResponse() {
    }

    public MessageUnreadCountResponse(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
}

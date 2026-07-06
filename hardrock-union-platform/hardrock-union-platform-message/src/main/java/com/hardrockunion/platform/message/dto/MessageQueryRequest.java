package com.hardrockunion.platform.message.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

public class MessageQueryRequest extends PageRequest {

    private String messageType;
    private Integer readFlag;
    private String keyword;

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public Integer getReadFlag() { return readFlag; }
    public void setReadFlag(Integer readFlag) { this.readFlag = readFlag; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}

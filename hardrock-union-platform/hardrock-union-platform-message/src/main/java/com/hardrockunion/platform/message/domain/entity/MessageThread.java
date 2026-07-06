package com.hardrockunion.platform.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hardrockunion.infrastructure.db.entity.BaseEntity;

@TableName("message_thread")
public class MessageThread extends BaseEntity {

    private Long appId;
    private String appCode;
    private Long tenantId;
    private String threadType;
    private String sourceType;
    private Long sourceId;
    private String title;
    private String threadStatus;
    private Long createdBy;

    public Long getAppId() { return appId; }
    public void setAppId(Long appId) { this.appId = appId; }
    public String getAppCode() { return appCode; }
    public void setAppCode(String appCode) { this.appCode = appCode; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getThreadType() { return threadType; }
    public void setThreadType(String threadType) { this.threadType = threadType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getThreadStatus() { return threadStatus; }
    public void setThreadStatus(String threadStatus) { this.threadStatus = threadStatus; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}

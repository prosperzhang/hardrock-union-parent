package com.hardrockunion.platform.workflow.event;

public class WorkflowTaskEvent {

    private final String eventType;
    private final String appCode;
    private final Long tenantId;
    private final Long taskId;
    private final String taskTitle;
    private final String taskContent;
    private final String sourceType;
    private final Long sourceId;
    private final Long applicantUserId;
    private final Long assigneeUserId;
    private final Long operatorUserId;
    private final String actionUrl;

    public WorkflowTaskEvent(String eventType,
                             String appCode,
                             Long tenantId,
                             Long taskId,
                             String taskTitle,
                             String taskContent,
                             String sourceType,
                             Long sourceId,
                             Long applicantUserId,
                             Long assigneeUserId,
                             Long operatorUserId,
                             String actionUrl) {
        this.eventType = eventType;
        this.appCode = appCode;
        this.tenantId = tenantId;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskContent = taskContent;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.applicantUserId = applicantUserId;
        this.assigneeUserId = assigneeUserId;
        this.operatorUserId = operatorUserId;
        this.actionUrl = actionUrl;
    }

    public String getEventType() { return eventType; }
    public String getAppCode() { return appCode; }
    public Long getTenantId() { return tenantId; }
    public Long getTaskId() { return taskId; }
    public String getTaskTitle() { return taskTitle; }
    public String getTaskContent() { return taskContent; }
    public String getSourceType() { return sourceType; }
    public Long getSourceId() { return sourceId; }
    public Long getApplicantUserId() { return applicantUserId; }
    public Long getAssigneeUserId() { return assigneeUserId; }
    public Long getOperatorUserId() { return operatorUserId; }
    public String getActionUrl() { return actionUrl; }
}

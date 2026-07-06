package com.hardrockunion.platform.workflow.dto;

import com.hardrockunion.infrastructure.db.page.PageRequest;

public class WorkflowTaskQueryRequest extends PageRequest {

    private String taskStatus;
    private String workflowType;
    private String keyword;

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public String getWorkflowType() { return workflowType; }
    public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}

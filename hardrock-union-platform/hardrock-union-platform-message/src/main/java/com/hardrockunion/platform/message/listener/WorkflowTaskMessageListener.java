package com.hardrockunion.platform.message.listener;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hardrockunion.platform.message.dto.MessageSendRequest;
import com.hardrockunion.platform.message.service.MessageService;
import com.hardrockunion.platform.workflow.event.WorkflowTaskEvent;

@Component
public class WorkflowTaskMessageListener {

    private final MessageService messageService;

    public WorkflowTaskMessageListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @EventListener
    public void onWorkflowTaskEvent(WorkflowTaskEvent event) {
        Long receiverUserId = switch (event.getEventType()) {
            case "TASK_CREATED" -> event.getAssigneeUserId();
            case "TASK_APPROVED", "TASK_REJECTED" -> event.getApplicantUserId();
            default -> null;
        };
        if (receiverUserId == null) {
            return;
        }
        MessageSendRequest request = new MessageSendRequest();
        request.setTenantId(event.getTenantId());
        request.setThreadType("APPROVAL");
        request.setMessageType("APPROVAL");
        request.setReceiverUserIds(List.of(receiverUserId));
        request.setTitle(title(event));
        request.setContent(content(event));
        request.setSourceType("WORKFLOW_TASK");
        request.setSourceId(event.getTaskId());
        request.setActionUrl(event.getActionUrl());
        messageService.sendSystem(event.getAppCode(), request);
    }

    private String title(WorkflowTaskEvent event) {
        return switch (event.getEventType()) {
            case "TASK_CREATED" -> "新的审批任务：" + event.getTaskTitle();
            case "TASK_APPROVED" -> "审批已通过：" + event.getTaskTitle();
            case "TASK_REJECTED" -> "审批已驳回：" + event.getTaskTitle();
            default -> event.getTaskTitle();
        };
    }

    private String content(WorkflowTaskEvent event) {
        String content = event.getTaskContent();
        return content == null || content.isBlank() ? title(event) : content;
    }
}

package com.hardrockunion.platform.workflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;
import com.hardrockunion.platform.workflow.dto.WorkflowTaskCreateRequest;
import com.hardrockunion.platform.workflow.dto.WorkflowTaskQueryRequest;
import com.hardrockunion.platform.workflow.dto.WorkflowTaskResponse;
import com.hardrockunion.platform.workflow.dto.WorkflowTaskReviewRequest;
import com.hardrockunion.platform.workflow.service.WorkflowTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/{appCode}/workflow/tasks")
@Tag(name = "工作流任务", description = "跨 app、跨租户复用的审批任务。")
public class WorkflowTaskController {

    private final WorkflowTaskService workflowTaskService;

    public WorkflowTaskController(WorkflowTaskService workflowTaskService) {
        this.workflowTaskService = workflowTaskService;
    }

    @Operation(summary = "创建审批任务", description = "在当前租户内创建一个审批任务，并通知处理人。")
    @PostMapping
    public Result<WorkflowTaskResponse> create(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                               @PathVariable("appCode") String appCode,
                                               @RequestBody WorkflowTaskCreateRequest request,
                                               LoginUser loginUser) {
        return Result.success(workflowTaskService.create(appCode, request, loginUser));
    }

    @Operation(summary = "我的待办", description = "查询当前登录人的审批任务。")
    @GetMapping("/mine")
    public Result<PageResponse<WorkflowTaskResponse>> myTasks(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                              @PathVariable("appCode") String appCode,
                                                              WorkflowTaskQueryRequest request,
                                                              LoginUser loginUser) {
        return Result.success(workflowTaskService.myTasks(appCode, request, loginUser));
    }

    @Operation(summary = "我的申请", description = "查询当前登录人提交的审批任务。")
    @GetMapping("/applications")
    public Result<PageResponse<WorkflowTaskResponse>> myApplications(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                                     @PathVariable("appCode") String appCode,
                                                                     WorkflowTaskQueryRequest request,
                                                                     LoginUser loginUser) {
        return Result.success(workflowTaskService.myApplications(appCode, request, loginUser));
    }

    @Operation(summary = "审批通过", description = "把当前登录人负责的待办任务审批通过，并通知申请人。")
    @PostMapping("/{taskId}/approve")
    public Result<WorkflowTaskResponse> approve(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                                @PathVariable("appCode") String appCode,
                                                @PathVariable("taskId") Long taskId,
                                                @RequestBody WorkflowTaskReviewRequest request,
                                                LoginUser loginUser) {
        return Result.success(workflowTaskService.approve(appCode, taskId, request, loginUser));
    }

    @Operation(summary = "审批驳回", description = "把当前登录人负责的待办任务驳回，并通知申请人。")
    @PostMapping("/{taskId}/reject")
    public Result<WorkflowTaskResponse> reject(@Parameter(description = "应用编码，例如 NEXIS、PRIMELOAD-MARKETPLACE")
                                               @PathVariable("appCode") String appCode,
                                               @PathVariable("taskId") Long taskId,
                                               @RequestBody WorkflowTaskReviewRequest request,
                                               LoginUser loginUser) {
        return Result.success(workflowTaskService.reject(appCode, taskId, request, loginUser));
    }
}

package com.hardrockunion.platform.workflow.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.infrastructure.db.page.PageResponse;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;
import com.hardrockunion.platform.iam.mapper.IamTenantMemberMapper;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.workflow.domain.entity.WorkflowTask;
import com.hardrockunion.platform.workflow.dto.WorkflowTaskCreateRequest;
import com.hardrockunion.platform.workflow.dto.WorkflowTaskQueryRequest;
import com.hardrockunion.platform.workflow.dto.WorkflowTaskResponse;
import com.hardrockunion.platform.workflow.dto.WorkflowTaskReviewRequest;
import com.hardrockunion.platform.workflow.event.WorkflowTaskEvent;
import com.hardrockunion.platform.workflow.mapper.WorkflowTaskMapper;

@Service
public class WorkflowTaskService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final AppRegistryQueryService appRegistryQueryService;
    private final IamTenantMemberMapper iamTenantMemberMapper;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final ApplicationEventPublisher eventPublisher;

    public WorkflowTaskService(AppRegistryQueryService appRegistryQueryService,
                               IamTenantMemberMapper iamTenantMemberMapper,
                               WorkflowTaskMapper workflowTaskMapper,
                               ApplicationEventPublisher eventPublisher) {
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamTenantMemberMapper = iamTenantMemberMapper;
        this.workflowTaskMapper = workflowTaskMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskResponse create(String appCode, WorkflowTaskCreateRequest request, LoginUser loginUser) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        if (request == null) {
            throw new BusinessException("工作流任务请求不能为空");
        }
        Long tenantId = request.getTenantId() == null ? loginUser.getTenantId() : request.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("tenantId 不能为空");
        }
        if (!tenantId.equals(loginUser.getTenantId())) {
            throw new BusinessException("不能在当前租户之外创建工作流任务");
        }
        if (request.getAssigneeUserId() == null) {
            throw new BusinessException("assigneeUserId 不能为空");
        }
        ensureTenantMember(app.getId(), tenantId, request.getAssigneeUserId());
        if (StringUtils.isBlank(request.getTaskTitle())) {
            throw new BusinessException("taskTitle 不能为空");
        }

        WorkflowTask task = new WorkflowTask();
        task.setAppId(app.getId());
        task.setAppCode(app.getAppCode());
        task.setTenantId(tenantId);
        task.setWorkflowType(StringUtils.upperCase(StringUtils.defaultIfBlank(StringUtils.trimToNull(request.getWorkflowType()), "GENERAL_APPROVAL")));
        task.setTaskTitle(StringUtils.trim(request.getTaskTitle()));
        task.setTaskContent(StringUtils.trimToNull(request.getTaskContent()));
        task.setSourceType(StringUtils.upperCase(StringUtils.trimToNull(request.getSourceType())));
        task.setSourceId(request.getSourceId());
        task.setApplicantUserId(loginUser.getUserId());
        task.setAssigneeUserId(request.getAssigneeUserId());
        task.setTaskStatus(STATUS_PENDING);
        task.setActionUrl(StringUtils.trimToNull(request.getActionUrl()));
        task.setDeleted(0);
        workflowTaskMapper.insert(task);
        publish("TASK_CREATED", task, loginUser.getUserId());
        return toResponse(task);
    }

    public PageResponse<WorkflowTaskResponse> myTasks(String appCode, WorkflowTaskQueryRequest request, LoginUser loginUser) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        WorkflowTaskQueryRequest query = request == null ? new WorkflowTaskQueryRequest() : request;
        LambdaQueryWrapper<WorkflowTask> wrapper = new LambdaQueryWrapper<WorkflowTask>()
            .eq(WorkflowTask::getAppId, app.getId())
            .eq(WorkflowTask::getTenantId, loginUser.getTenantId())
            .eq(WorkflowTask::getAssigneeUserId, loginUser.getUserId())
            .eq(WorkflowTask::getDeleted, 0);
        applyQuery(wrapper, query);
        wrapper.orderByDesc(WorkflowTask::getId);
        Page<WorkflowTask> page = workflowTaskMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(this::toResponse));
    }

    public PageResponse<WorkflowTaskResponse> myApplications(String appCode, WorkflowTaskQueryRequest request, LoginUser loginUser) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        WorkflowTaskQueryRequest query = request == null ? new WorkflowTaskQueryRequest() : request;
        LambdaQueryWrapper<WorkflowTask> wrapper = new LambdaQueryWrapper<WorkflowTask>()
            .eq(WorkflowTask::getAppId, app.getId())
            .eq(WorkflowTask::getTenantId, loginUser.getTenantId())
            .eq(WorkflowTask::getApplicantUserId, loginUser.getUserId())
            .eq(WorkflowTask::getDeleted, 0);
        applyQuery(wrapper, query);
        wrapper.orderByDesc(WorkflowTask::getId);
        Page<WorkflowTask> page = workflowTaskMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResponse.from(page.convert(this::toResponse));
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskResponse approve(String appCode, Long taskId, WorkflowTaskReviewRequest request, LoginUser loginUser) {
        return review(appCode, taskId, request, loginUser, STATUS_APPROVED, "TASK_APPROVED");
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskResponse reject(String appCode, Long taskId, WorkflowTaskReviewRequest request, LoginUser loginUser) {
        return review(appCode, taskId, request, loginUser, STATUS_REJECTED, "TASK_REJECTED");
    }

    private WorkflowTaskResponse review(String appCode,
                                        Long taskId,
                                        WorkflowTaskReviewRequest request,
                                        LoginUser loginUser,
                                        String targetStatus,
                                        String eventType) {
        AppRegistry app = ensureAppLogin(appCode, loginUser);
        WorkflowTask task = workflowTaskMapper.selectOne(new LambdaQueryWrapper<WorkflowTask>()
            .eq(WorkflowTask::getAppId, app.getId())
            .eq(WorkflowTask::getTenantId, loginUser.getTenantId())
            .eq(WorkflowTask::getId, taskId)
            .eq(WorkflowTask::getAssigneeUserId, loginUser.getUserId())
            .eq(WorkflowTask::getDeleted, 0)
            .last("limit 1"));
        if (task == null) {
            throw new BusinessException("工作流任务不存在");
        }
        if (!STATUS_PENDING.equals(task.getTaskStatus())) {
            throw new BusinessException("当前任务已处理");
        }
        task.setTaskStatus(targetStatus);
        task.setReviewedBy(loginUser.getUserId());
        task.setReviewedAt(LocalDateTime.now());
        task.setReviewRemark(request == null ? null : StringUtils.trimToNull(request.getReviewRemark()));
        workflowTaskMapper.updateById(task);
        publish(eventType, task, loginUser.getUserId());
        return toResponse(task);
    }

    private AppRegistry ensureAppLogin(String appCode, LoginUser loginUser) {
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        if (loginUser == null || loginUser.getUserId() == null || loginUser.getTenantId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        if (!StringUtils.equalsIgnoreCase(app.getAppCode(), loginUser.getAppCode())) {
            throw new BusinessException("当前登录态不属于该 app");
        }
        return app;
    }

    private void ensureTenantMember(Long appId, Long tenantId, Long userId) {
        IamTenantMember member = iamTenantMemberMapper.selectOne(new LambdaQueryWrapper<IamTenantMember>()
            .eq(IamTenantMember::getAppId, appId)
            .eq(IamTenantMember::getTenantId, tenantId)
            .eq(IamTenantMember::getUserId, userId)
            .eq(IamTenantMember::getMemberStatus, "ACTIVE")
            .eq(IamTenantMember::getDeleted, 0)
            .last("limit 1"));
        if (member == null) {
            throw new BusinessException("处理人不属于当前租户");
        }
    }

    private void applyQuery(LambdaQueryWrapper<WorkflowTask> wrapper, WorkflowTaskQueryRequest query) {
        if (StringUtils.isNotBlank(query.getTaskStatus())) {
            wrapper.eq(WorkflowTask::getTaskStatus, StringUtils.upperCase(StringUtils.trim(query.getTaskStatus())));
        }
        if (StringUtils.isNotBlank(query.getWorkflowType())) {
            wrapper.eq(WorkflowTask::getWorkflowType, StringUtils.upperCase(StringUtils.trim(query.getWorkflowType())));
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = StringUtils.trim(query.getKeyword());
            wrapper.and(w -> w.like(WorkflowTask::getTaskTitle, keyword)
                .or()
                .like(WorkflowTask::getTaskContent, keyword));
        }
    }

    private void publish(String eventType, WorkflowTask task, Long operatorUserId) {
        eventPublisher.publishEvent(new WorkflowTaskEvent(
            eventType,
            task.getAppCode(),
            task.getTenantId(),
            task.getId(),
            task.getTaskTitle(),
            task.getTaskContent(),
            task.getSourceType(),
            task.getSourceId(),
            task.getApplicantUserId(),
            task.getAssigneeUserId(),
            operatorUserId,
            task.getActionUrl()
        ));
    }

    private WorkflowTaskResponse toResponse(WorkflowTask task) {
        WorkflowTaskResponse response = new WorkflowTaskResponse();
        response.setId(task.getId());
        response.setTenantId(task.getTenantId());
        response.setWorkflowType(task.getWorkflowType());
        response.setTaskTitle(task.getTaskTitle());
        response.setTaskContent(task.getTaskContent());
        response.setSourceType(task.getSourceType());
        response.setSourceId(task.getSourceId());
        response.setApplicantUserId(task.getApplicantUserId());
        response.setAssigneeUserId(task.getAssigneeUserId());
        response.setTaskStatus(task.getTaskStatus());
        response.setReviewedBy(task.getReviewedBy());
        response.setReviewedAt(task.getReviewedAt());
        response.setReviewRemark(task.getReviewRemark());
        response.setActionUrl(task.getActionUrl());
        response.setCreatedAt(task.getCreatedAt());
        return response;
    }
}

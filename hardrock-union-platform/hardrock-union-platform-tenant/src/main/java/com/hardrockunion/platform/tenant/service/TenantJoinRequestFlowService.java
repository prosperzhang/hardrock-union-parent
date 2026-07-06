package com.hardrockunion.platform.tenant.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.IamTenantJoinRequest;
import com.hardrockunion.platform.iam.mapper.IamTenantJoinRequestMapper;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.iam.service.IamRoleQueryService;
import com.hardrockunion.platform.tenant.dto.TenantJoinRequestCreateRequest;
import com.hardrockunion.platform.tenant.dto.TenantJoinRequestResponse;
import com.hardrockunion.platform.tenant.dto.TenantJoinRequestReviewRequest;
import com.hardrockunion.platform.tenant.dto.TenantSummaryResponse;

@Service
public class TenantJoinRequestFlowService {

    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";
    private static final String CANCELLED = "CANCELLED";

    private final IamTenantJoinRequestMapper joinRequestMapper;
    private final TenantWorkspaceFlowService tenantWorkspaceFlowService;
    private final TenantMemberFlowService tenantMemberFlowService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamRoleQueryService iamRoleQueryService;

    public TenantJoinRequestFlowService(IamTenantJoinRequestMapper joinRequestMapper,
                                        TenantWorkspaceFlowService tenantWorkspaceFlowService,
                                        TenantMemberFlowService tenantMemberFlowService,
                                        AppRegistryQueryService appRegistryQueryService,
                                        IamRoleQueryService iamRoleQueryService) {
        this.joinRequestMapper = joinRequestMapper;
        this.tenantWorkspaceFlowService = tenantWorkspaceFlowService;
        this.tenantMemberFlowService = tenantMemberFlowService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamRoleQueryService = iamRoleQueryService;
    }

    @Transactional(rollbackFor = Exception.class)
    public TenantJoinRequestResponse create(String appCode, TenantJoinRequestCreateRequest request, LoginUser loginUser) {
        TenantFlowPolicy.AppTenantPolicy policy = tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        ensureAuthenticated(appCode, loginUser);
        if (request == null) {
            throw new BusinessException("加入" + policy.tenantLabel() + "信息不能为空");
        }
        TenantSummaryResponse tenant = resolveTenantRegistry(appCode, request, loginUser.getTenantId());
        Long tenantId = tenant.getTenantId();
        if (tenantMemberFlowService.findActiveMember(appCode, tenantId, loginUser.getUserId()) != null) {
            throw new BusinessException("当前账号已加入" + policy.tenantLabel() + "，无需重复申请");
        }
        ensureNoPendingRequest(appCode, tenantId, loginUser.getUserId());
        IamTenantJoinRequest joinRequest = new IamTenantJoinRequest();
        joinRequest.setAppId(resolveAppId(appCode));
        joinRequest.setTenantId(tenantId);
        joinRequest.setUserId(loginUser.getUserId());
        joinRequest.setApplyMessage(StringUtils.trimToNull(request.getApplyMessage()));
        joinRequest.setRequestStatus(PENDING);
        joinRequest.setDeleted(0);
        joinRequestMapper.insert(joinRequest);
        return toResponse(joinRequest, tenant);
    }

    public TenantJoinRequestResponse cancel(String appCode, Long requestId, LoginUser loginUser) {
        tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        ensureAuthenticated(appCode, loginUser);
        IamTenantJoinRequest joinRequest = joinRequestMapper.selectOne(new LambdaQueryWrapper<IamTenantJoinRequest>()
            .eq(IamTenantJoinRequest::getAppId, resolveAppId(appCode))
            .eq(IamTenantJoinRequest::getId, requestId)
            .eq(IamTenantJoinRequest::getUserId, loginUser.getUserId())
            .eq(IamTenantJoinRequest::getDeleted, 0)
            .last("limit 1"));
        if (joinRequest == null) {
            throw new BusinessException("加入申请不存在");
        }
        if (!PENDING.equals(joinRequest.getRequestStatus())) {
            throw new BusinessException("当前加入申请不允许撤销");
        }
        joinRequest.setRequestStatus(CANCELLED);
        joinRequestMapper.updateById(joinRequest);
        return toResponse(joinRequest, tenantWorkspaceFlowService.loadTenantRegistry(appCode, joinRequest.getTenantId(), joinRequest.getTenantId()));
    }

    public List<TenantJoinRequestResponse> listByTenantRegistry(String appCode, Long tenantId, LoginUser loginUser) {
        tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        tenantMemberFlowService.ensureTenantRoleAdmin(appCode, tenantId, loginUser);
        TenantSummaryResponse tenant = tenantWorkspaceFlowService.loadTenantRegistry(appCode, tenantId, loginUser.getTenantId());
        return joinRequestMapper.selectList(new LambdaQueryWrapper<IamTenantJoinRequest>()
                .eq(IamTenantJoinRequest::getAppId, resolveAppId(appCode))
                .eq(IamTenantJoinRequest::getTenantId, tenantId)
                .eq(IamTenantJoinRequest::getDeleted, 0)
                .orderByDesc(IamTenantJoinRequest::getId))
            .stream()
            .map(item -> toResponse(item, tenant))
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public TenantJoinRequestResponse approve(String appCode,
                                             Long tenantId,
                                             Long requestId,
                                             TenantJoinRequestReviewRequest request,
                                             LoginUser loginUser) {
        tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        tenantMemberFlowService.ensureTenantRoleAdmin(appCode, tenantId, loginUser);
        TenantSummaryResponse tenant = tenantWorkspaceFlowService.loadTenantRegistry(appCode, tenantId, loginUser.getTenantId());
        IamTenantJoinRequest joinRequest = loadEntity(appCode, requestId, tenantId);
        if (!PENDING.equals(joinRequest.getRequestStatus())) {
            throw new BusinessException("当前加入申请不允许重复审批");
        }
        joinRequest.setRequestStatus(APPROVED);
        joinRequest.setReviewedBy(loginUser.getUserId());
        joinRequest.setReviewedAt(LocalDateTime.now());
        joinRequest.setReviewRemark(StringUtils.trimToNull(request == null ? null : request.getReviewRemark()));
        joinRequestMapper.updateById(joinRequest);
        tenantMemberFlowService.activateMember(appCode, tenantId, joinRequest.getUserId());
        return toResponse(joinRequest, tenant);
    }

    @Transactional(rollbackFor = Exception.class)
    public TenantJoinRequestResponse reject(String appCode,
                                            Long tenantId,
                                            Long requestId,
                                            TenantJoinRequestReviewRequest request,
                                            LoginUser loginUser) {
        tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        tenantMemberFlowService.ensureTenantRoleAdmin(appCode, tenantId, loginUser);
        TenantSummaryResponse tenant = tenantWorkspaceFlowService.loadTenantRegistry(appCode, tenantId, loginUser.getTenantId());
        IamTenantJoinRequest joinRequest = loadEntity(appCode, requestId, tenantId);
        if (!PENDING.equals(joinRequest.getRequestStatus())) {
            throw new BusinessException("当前加入申请不允许重复审批");
        }
        joinRequest.setRequestStatus(REJECTED);
        joinRequest.setReviewedBy(loginUser.getUserId());
        joinRequest.setReviewedAt(LocalDateTime.now());
        joinRequest.setReviewRemark(StringUtils.trimToNull(request == null ? null : request.getReviewRemark()));
        joinRequestMapper.updateById(joinRequest);
        return toResponse(joinRequest, tenant);
    }

    public IamTenantJoinRequest findLatestPending(String appCode, Long tenantId, Long userId) {
        tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        if (tenantId == null) {
            return findLatestPendingByUser(appCode, userId);
        }
        return joinRequestMapper.selectOne(new LambdaQueryWrapper<IamTenantJoinRequest>()
            .eq(IamTenantJoinRequest::getAppId, resolveAppId(appCode))
            .eq(IamTenantJoinRequest::getTenantId, tenantId)
            .eq(IamTenantJoinRequest::getUserId, userId)
            .eq(IamTenantJoinRequest::getDeleted, 0)
            .eq(IamTenantJoinRequest::getRequestStatus, PENDING)
            .orderByDesc(IamTenantJoinRequest::getId)
            .last("limit 1"));
    }

    public IamTenantJoinRequest findLatestPendingByUser(String appCode, Long userId) {
        tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        if (userId == null) {
            return null;
        }
        return joinRequestMapper.selectOne(new LambdaQueryWrapper<IamTenantJoinRequest>()
            .eq(IamTenantJoinRequest::getAppId, resolveAppId(appCode))
            .eq(IamTenantJoinRequest::getUserId, userId)
            .eq(IamTenantJoinRequest::getDeleted, 0)
            .eq(IamTenantJoinRequest::getRequestStatus, PENDING)
            .orderByDesc(IamTenantJoinRequest::getId)
            .last("limit 1"));
    }

    private TenantSummaryResponse resolveTenantRegistry(String appCode, TenantJoinRequestCreateRequest request, Long currentTenantId) {
        if (request.getTenantId() != null) {
            return tenantWorkspaceFlowService.loadTenantRegistry(appCode, request.getTenantId(), currentTenantId);
        }
        if (StringUtils.isBlank(request.getTenantKeyword())) {
            throw new BusinessException("tenantId 或 tenantKeyword 至少填写一个");
        }
        return tenantWorkspaceFlowService.loadTenantByKeyword(appCode, request.getTenantKeyword(), currentTenantId);
    }

    private void ensureNoPendingRequest(String appCode, Long tenantId, Long userId) {
        Long count = joinRequestMapper.selectCount(new LambdaQueryWrapper<IamTenantJoinRequest>()
            .eq(IamTenantJoinRequest::getAppId, resolveAppId(appCode))
            .eq(IamTenantJoinRequest::getTenantId, tenantId)
            .eq(IamTenantJoinRequest::getUserId, userId)
            .eq(IamTenantJoinRequest::getDeleted, 0)
            .eq(IamTenantJoinRequest::getRequestStatus, PENDING));
        if (count != null && count > 0) {
            throw new BusinessException("当前租户已存在待审批的加入申请");
        }
    }

    private IamTenantJoinRequest loadEntity(String appCode, Long requestId, Long tenantId) {
        IamTenantJoinRequest joinRequest = joinRequestMapper.selectOne(new LambdaQueryWrapper<IamTenantJoinRequest>()
            .eq(IamTenantJoinRequest::getAppId, resolveAppId(appCode))
            .eq(IamTenantJoinRequest::getTenantId, tenantId)
            .eq(IamTenantJoinRequest::getId, requestId)
            .eq(IamTenantJoinRequest::getDeleted, 0)
            .last("limit 1"));
        if (joinRequest == null) {
            throw new BusinessException("加入申请不存在");
        }
        return joinRequest;
    }

    private Long resolveAppId(String appCode) {
        return appRegistryQueryService.getEnabledAppByCode(appCode).getId();
    }

    private void ensureAuthenticated(String appCode, LoginUser loginUser) {
        tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
    }

    private TenantJoinRequestResponse toResponse(IamTenantJoinRequest joinRequest, TenantSummaryResponse tenant) {
        TenantJoinRequestResponse response = new TenantJoinRequestResponse();
        response.setId(joinRequest.getId());
        response.setTenantId(joinRequest.getTenantId());
        response.setTenantName(tenant == null ? null : tenant.getTenantName());
        response.setUserId(joinRequest.getUserId());
        response.setRequestStatus(joinRequest.getRequestStatus());
        response.setApplyMessage(joinRequest.getApplyMessage());
        response.setReviewedBy(joinRequest.getReviewedBy());
        response.setReviewedAt(joinRequest.getReviewedAt());
        response.setReviewRemark(joinRequest.getReviewRemark());
        return response;
    }
}

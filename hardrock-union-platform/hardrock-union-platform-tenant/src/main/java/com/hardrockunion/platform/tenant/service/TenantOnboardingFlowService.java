package com.hardrockunion.platform.tenant.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.IamDepartment;
import com.hardrockunion.platform.iam.domain.entity.IamTenantJoinRequest;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.iam.service.IamDepartmentQueryService;
import com.hardrockunion.platform.iam.service.IamRoleQueryService;
import com.hardrockunion.platform.iam.service.IamTenantMemberService;
import com.hardrockunion.platform.tenant.dto.TenantOnboardingStatusResponse;
import com.hardrockunion.platform.tenant.dto.TenantSummaryResponse;

@Service
public class TenantOnboardingFlowService {

    private static final String NEED_CREATE_OR_JOIN = "NEED_CREATE_OR_JOIN";
    private static final String WAITING_APPROVAL = "WAITING_APPROVAL";
    private static final String WAITING_ROLE_ASSIGNMENT = "WAITING_ROLE_ASSIGNMENT";
    private static final String READY = "READY";

    private final TenantMemberFlowService tenantMemberFlowService;
    private final TenantJoinRequestFlowService tenantJoinRequestFlowService;
    private final TenantWorkspaceFlowService tenantWorkspaceFlowService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamRoleQueryService iamRoleQueryService;
    private final IamTenantMemberService iamTenantMemberService;
    private final IamDepartmentQueryService iamDepartmentQueryService;

    public TenantOnboardingFlowService(TenantMemberFlowService tenantMemberFlowService,
                                       TenantJoinRequestFlowService tenantJoinRequestFlowService,
                                       TenantWorkspaceFlowService tenantWorkspaceFlowService,
                                       AppRegistryQueryService appRegistryQueryService,
                                       IamRoleQueryService iamRoleQueryService,
                                       IamTenantMemberService iamTenantMemberService,
                                       IamDepartmentQueryService iamDepartmentQueryService) {
        this.tenantMemberFlowService = tenantMemberFlowService;
        this.tenantJoinRequestFlowService = tenantJoinRequestFlowService;
        this.tenantWorkspaceFlowService = tenantWorkspaceFlowService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamRoleQueryService = iamRoleQueryService;
        this.iamTenantMemberService = iamTenantMemberService;
        this.iamDepartmentQueryService = iamDepartmentQueryService;
    }

    public TenantOnboardingStatusResponse getStatus(String appCode, LoginUser loginUser) {
        TenantFlowPolicy.AppTenantPolicy policy = tenantWorkspaceFlowService.ensureSupportedApp(appCode);
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        Long appId = appRegistryQueryService.getEnabledAppByCode(policy.appCode()).getId();
        if (loginUser.getTenantId() != null) {
            IamTenantMember member = tenantMemberFlowService.findActiveMember(appCode, loginUser.getTenantId(), loginUser.getUserId());
            if (member != null) {
                List<String> roleCodes = tenantMemberFlowService.listRoleCodes(appCode, loginUser.getTenantId(), member.getUserId());
                if (!roleCodes.isEmpty()) {
                    return buildReady(policy, tenantWorkspaceFlowService.loadTenantRegistry(appCode, loginUser.getTenantId(), loginUser.getTenantId()), member, roleCodes);
                }
                return buildWaitingRoleAssignment(policy, tenantWorkspaceFlowService.loadTenantRegistry(appCode, loginUser.getTenantId(), loginUser.getTenantId()), member);
            }

            IamTenantJoinRequest joinRequest = tenantJoinRequestFlowService.findLatestPending(appCode, loginUser.getTenantId(), loginUser.getUserId());
            if (joinRequest != null) {
                return buildWaitingApproval(policy, tenantWorkspaceFlowService.loadTenantRegistry(appCode, joinRequest.getTenantId(), loginUser.getTenantId()), joinRequest);
            }
            return buildNeedCreateOrJoin(policy);
        }

        List<IamTenantMember> tenantMembers = iamTenantMemberService.listActiveMembersByUser(appId, loginUser.getUserId());
        if (!tenantMembers.isEmpty()) {
            IamTenantMember member = tenantMembers.getFirst();
            List<String> roleCodes = tenantMemberFlowService.listRoleCodes(appCode, member.getTenantId(), member.getUserId());
            TenantSummaryResponse tenant = tenantWorkspaceFlowService.loadTenantRegistry(appCode, member.getTenantId(), member.getTenantId());
            if (!roleCodes.isEmpty()) {
                return buildReady(policy, tenant, member, roleCodes);
            }
            return buildWaitingRoleAssignment(policy, tenant, member);
        }

        IamTenantJoinRequest joinRequest = tenantJoinRequestFlowService.findLatestPendingByUser(appCode, loginUser.getUserId());
        if (joinRequest != null) {
            return buildWaitingApproval(policy, tenantWorkspaceFlowService.loadTenantRegistry(appCode, joinRequest.getTenantId(), joinRequest.getTenantId()), joinRequest);
        }
        return buildNeedCreateOrJoin(policy);
    }

    private TenantOnboardingStatusResponse buildReady(TenantFlowPolicy.AppTenantPolicy policy,
                                                      TenantSummaryResponse tenant,
                                                      IamTenantMember member,
                                                      List<String> roleCodes) {
        TenantOnboardingStatusResponse response = new TenantOnboardingStatusResponse();
        response.setStatus(READY);
        response.setTenantId(tenant.getTenantId());
        response.setTenantName(tenant.getTenantName());
        response.setMemberId(member.getId());
        response.setMemberStatus(member.getMemberStatus());
        IamDepartment department = iamDepartmentQueryService.getPrimaryDepartmentByUser(member.getUserId(), policy.appCode(), tenant.getTenantId());
        if (department != null) {
            response.setDepartmentId(department.getId());
            response.setDepartmentCode(department.getDeptCode());
            response.setDepartmentName(department.getDeptName());
            response.setDepartmentShortName(department.getDeptShortName());
        }
        response.setRoleCodes(roleCodes);
        response.setMessage("已加入" + policy.tenantLabel() + "并完成部门角色分配，可以进入首页。");
        return response;
    }

    private TenantOnboardingStatusResponse buildWaitingRoleAssignment(TenantFlowPolicy.AppTenantPolicy policy,
                                                                      TenantSummaryResponse tenant,
                                                                      IamTenantMember member) {
        TenantOnboardingStatusResponse response = new TenantOnboardingStatusResponse();
        response.setStatus(WAITING_ROLE_ASSIGNMENT);
        response.setTenantId(tenant.getTenantId());
        response.setTenantName(tenant.getTenantName());
        response.setMemberId(member.getId());
        response.setMemberStatus(member.getMemberStatus());
        response.setRoleCodes(List.of());
        response.setMessage("已加入" + policy.tenantLabel() + "，正在等待管理员分配部门和角色。");
        return response;
    }

    private TenantOnboardingStatusResponse buildWaitingApproval(TenantFlowPolicy.AppTenantPolicy policy,
                                                                TenantSummaryResponse tenant,
                                                                IamTenantJoinRequest joinRequest) {
        TenantOnboardingStatusResponse response = new TenantOnboardingStatusResponse();
        response.setStatus(WAITING_APPROVAL);
        response.setTenantId(tenant.getTenantId());
        response.setTenantName(tenant.getTenantName());
        response.setJoinRequestId(joinRequest.getId());
        response.setJoinRequestStatus(joinRequest.getRequestStatus());
        response.setRoleCodes(List.of());
        response.setMessage("已提交加入申请，请等待" + policy.tenantLabel() + "管理员审批。");
        return response;
    }

    private TenantOnboardingStatusResponse buildNeedCreateOrJoin(TenantFlowPolicy.AppTenantPolicy policy) {
        TenantOnboardingStatusResponse response = new TenantOnboardingStatusResponse();
        response.setStatus(NEED_CREATE_OR_JOIN);
        response.setRoleCodes(List.of());
        response.setMessage("当前账号还没有" + policy.tenantLabel() + "归属，请先" + policy.onboardingActionLabel() + "。");
        return response;
    }
}

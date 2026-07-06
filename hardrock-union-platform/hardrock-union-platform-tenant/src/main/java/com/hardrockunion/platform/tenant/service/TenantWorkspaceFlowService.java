package com.hardrockunion.platform.tenant.service;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.dto.IamTenantMemberSwitchRequest;
import com.hardrockunion.platform.iam.service.IamAuthService;
import com.hardrockunion.platform.iam.service.IamRoleQueryService;
import com.hardrockunion.platform.tenant.dto.TenantCreateRequest;
import com.hardrockunion.platform.tenant.dto.TenantCreateResponse;
import com.hardrockunion.platform.tenant.dto.TenantSummaryResponse;
import com.hardrockunion.platform.tenant.dto.TenantRegistryResponse;
import com.hardrockunion.platform.tenant.event.TenantCreatedEvent;

@Service
public class TenantWorkspaceFlowService {

    private static final String PRIMELOAD_MARKETPLACE = "PRIMELOAD-MARKETPLACE";
    private static final String PMHUB = "PMHUB";
    private static final String SELF_OPERATED_MERCHANT = "SELF_OPERATED_MERCHANT";
    private static final String PMHUB_GROUP = "GROUP";
    private static final String PMHUB_COMPANY = "COMPANY";
    private static final String PMHUB_PROJECT = "PROJECT";

    private final TenantRegistryService tenantRegistryService;
    private final TenantMemberFlowService tenantMemberFlowService;
    private final IamAuthService iamAuthService;
    private final IamRoleQueryService iamRoleQueryService;
    private final TenantFlowPolicy tenantFlowPolicy;
    private final ApplicationEventPublisher eventPublisher;

    public TenantWorkspaceFlowService(TenantRegistryService tenantRegistryService,
                                      TenantMemberFlowService tenantMemberFlowService,
                                      IamAuthService iamAuthService,
                                      IamRoleQueryService iamRoleQueryService,
                                      TenantFlowPolicy tenantFlowPolicy,
                                      ApplicationEventPublisher eventPublisher) {
        this.tenantRegistryService = tenantRegistryService;
        this.tenantMemberFlowService = tenantMemberFlowService;
        this.iamAuthService = iamAuthService;
        this.iamRoleQueryService = iamRoleQueryService;
        this.tenantFlowPolicy = tenantFlowPolicy;
        this.eventPublisher = eventPublisher;
    }

    public List<TenantSummaryResponse> list(String appCode, LoginUser loginUser) {
        TenantFlowPolicy.AppTenantPolicy policy = ensureSupportedApp(appCode);
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        return tenantRegistryService.listEnabledByApp(policy.appCode()).stream()
            .filter(tenant -> isPolicyTenant(policy, tenant))
            .filter(tenant -> isVisibleTenant(policy, tenant, loginUser.getTenantId()))
            .map(this::toTenantRegistry)
            .toList();
    }

    public TenantSummaryResponse getById(String appCode, Long tenantId, LoginUser loginUser) {
        ensureSupportedApp(appCode);
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        return loadTenantRegistry(appCode, tenantId, loginUser.getTenantId());
    }

    @Transactional(rollbackFor = Exception.class)
    public TenantCreateResponse create(String appCode, TenantCreateRequest request, LoginUser loginUser) {
        TenantFlowPolicy.AppTenantPolicy policy = ensureSupportedApp(appCode);
        ensureCanCreateTenant(policy, loginUser);
        if (request == null || StringUtils.isBlank(request.getTenantName())) {
            throw new BusinessException("tenantName 不能为空");
        }
        String tenantName = StringUtils.trim(request.getTenantName());
        String tenantCode = StringUtils.trimToNull(request.getTenantCode());
        String tenantSource = resolveTenantSource(policy, loginUser);
        String tenantType = resolveTenantType(policy, request, loginUser);
        var tenant = tenantRegistryService.createTenant(
            policy.appCode(),
            tenantType,
            policy.tenantCodePrefix(),
            tenantCode,
            tenantName,
            tenantSource,
            request.getParentTenantId(),
            request.getProjectAddress(),
            request.getProvinceCode(),
            request.getProvinceName(),
            request.getCityCode(),
            request.getCityName(),
            request.getDistrictCode(),
            request.getDistrictName(),
            request.getManagerName(),
            request.getManagerPhone()
        );
        tenantMemberFlowService.createTenantAdminMember(policy.appCode(), tenant.getId(), loginUser);
        eventPublisher.publishEvent(new TenantCreatedEvent(
            policy.appCode(),
            tenant.getTenantType(),
            tenant.getId(),
            tenant.getTenantName(),
            tenant.getTenantSource(),
            tenant.getProjectAddress(),
            tenant.getProvinceCode(),
            tenant.getProvinceName(),
            tenant.getCityCode(),
            tenant.getCityName(),
            tenant.getDistrictCode(),
            tenant.getDistrictName(),
            tenant.getManagerName(),
            tenant.getManagerPhone(),
            loginUser.getAppCode(),
            loginUser.getUserId()
        ));
        IamTenantMemberSwitchRequest switchRequest = new IamTenantMemberSwitchRequest();
        switchRequest.setTenantId(tenant.getId());
        TenantCreateResponse response = new TenantCreateResponse();
        response.setTenantRegistry(toTenantRegistry(tenant));
        if (isSameAppLogin(policy, loginUser)) {
            response.setLogin(iamAuthService.switchCurrentUserTenant(policy.appCode(), loginUser.getUserId(), switchRequest));
        }
        return response;
    }

    public TenantSummaryResponse loadTenantRegistry(String appCode, Long tenantId, Long currentTenantId) {
        TenantFlowPolicy.AppTenantPolicy policy = ensureSupportedApp(appCode);
        var tenant = tenantRegistryService.getByAppAndId(policy.appCode(), tenantId);
        if (!isPolicyTenant(policy, tenant)) {
            throw new BusinessException(policy.tenantLabel() + "不存在");
        }
        if (currentTenantId != null && !currentTenantId.equals(tenant.getId())) {
            if (!isVisibleTenant(policy, tenant, currentTenantId)) {
                throw new BusinessException(policy.tenantLabel() + "不存在");
            }
        }
        return toTenantRegistry(tenant);
    }

    public List<Long> findTenantIdsByKeyword(String appCode, String keyword, Long currentTenantId) {
        TenantFlowPolicy.AppTenantPolicy policy = ensureSupportedApp(appCode);
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }
        String trimmedKeyword = StringUtils.trim(keyword);
        Stream<Long> tenantIds = tenantRegistryService.findTenantsByKeyword(policy.appCode(), policy.tenantType(), trimmedKeyword, currentTenantId)
            .stream()
            .map(TenantRegistryResponse::getId);
        Stream<Long> visibleTenantIds = tenantRegistryService.listEnabledByApp(policy.appCode()).stream()
            .filter(tenant -> isPolicyTenant(policy, tenant))
            .filter(tenant -> isVisibleTenant(policy, tenant, currentTenantId))
            .filter(tenant -> StringUtils.containsIgnoreCase(tenant.getTenantName(), trimmedKeyword)
                || StringUtils.containsIgnoreCase(tenant.getTenantCode(), trimmedKeyword)
                || StringUtils.containsIgnoreCase(tenant.getManagerName(), trimmedKeyword)
                || StringUtils.containsIgnoreCase(tenant.getManagerPhone(), trimmedKeyword))
            .map(TenantRegistryResponse::getId);
        return Stream.concat(tenantIds, visibleTenantIds).distinct().toList();
    }

    public TenantSummaryResponse loadTenantByKeyword(String appCode, String keyword, Long currentTenantId) {
        TenantFlowPolicy.AppTenantPolicy policy = ensureSupportedApp(appCode);
        String trimmedKeyword = StringUtils.trimToNull(keyword);
        if (trimmedKeyword == null) {
            throw new BusinessException("tenantKeyword 不能为空");
        }
        List<TenantRegistryResponse> tenants = tenantRegistryService.listEnabledByApp(policy.appCode()).stream()
            .filter(tenant -> isPolicyTenant(policy, tenant))
            .filter(tenant -> isVisibleTenant(policy, tenant, currentTenantId))
            .filter(tenant -> StringUtils.equalsIgnoreCase(tenant.getTenantCode(), trimmedKeyword)
                || StringUtils.equalsIgnoreCase(tenant.getTenantName(), trimmedKeyword)
                || StringUtils.containsIgnoreCase(tenant.getTenantCode(), trimmedKeyword)
                || StringUtils.containsIgnoreCase(tenant.getTenantName(), trimmedKeyword))
            .toList();
        if (tenants.isEmpty()) {
            throw new BusinessException("未找到匹配的" + policy.tenantLabel());
        }
        if (tenants.size() > 1) {
            throw new BusinessException("匹配到多个" + policy.tenantLabel() + "，请改用 tenantId 提交申请");
        }
        return loadTenantRegistry(policy.appCode(), tenants.getFirst().getId(), currentTenantId);
    }

    public TenantFlowPolicy.AppTenantPolicy ensureSupportedApp(String appCode) {
        return tenantFlowPolicy.resolve(appCode);
    }

    private void ensureAuthenticated(LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null || StringUtils.isBlank(loginUser.getAppCode())) {
            throw new BusinessException("未登录或登录已失效");
        }
        iamRoleQueryService.ensureAppLogin(loginUser.getAppCode(), loginUser);
    }

    private void ensureCanCreateTenant(TenantFlowPolicy.AppTenantPolicy policy, LoginUser loginUser) {
        ensureAuthenticated(loginUser);
        if (isSameAppLogin(policy, loginUser)) {
            return;
        }
        throw new BusinessException("当前登录态无权创建该租户");
    }

    private String resolveTenantSource(TenantFlowPolicy.AppTenantPolicy policy, LoginUser loginUser) {
        return null;
    }

    private String resolveTenantType(TenantFlowPolicy.AppTenantPolicy policy, TenantCreateRequest request, LoginUser loginUser) {
        String requestedTenantType = StringUtils.upperCase(StringUtils.trimToNull(request.getTenantType()));
        if (requestedTenantType == null) {
            return policy.tenantType();
        }
        if (!StringUtils.equalsIgnoreCase(PMHUB, policy.appCode())) {
            if (StringUtils.equalsIgnoreCase(policy.tenantType(), requestedTenantType)) {
                return policy.tenantType();
            }
            throw new BusinessException("当前 app 不支持自定义租户类型");
        }
        if (StringUtils.equalsAny(requestedTenantType, PMHUB_GROUP, PMHUB_COMPANY, PMHUB_PROJECT)) {
            return requestedTenantType;
        }
        throw new BusinessException("PMHub tenantType 仅支持 GROUP、COMPANY、PROJECT");
    }

    private boolean isSameAppLogin(TenantFlowPolicy.AppTenantPolicy policy, LoginUser loginUser) {
        return StringUtils.equalsIgnoreCase(policy.appCode(), loginUser.getAppCode());
    }

    private boolean isPolicyTenant(TenantFlowPolicy.AppTenantPolicy policy, TenantRegistryResponse tenant) {
        if (StringUtils.equalsIgnoreCase(PMHUB, policy.appCode())) {
            return StringUtils.equalsAnyIgnoreCase(tenant.getTenantType(), PMHUB_GROUP, PMHUB_COMPANY, PMHUB_PROJECT);
        }
        if (StringUtils.equalsIgnoreCase(policy.tenantType(), tenant.getTenantType())) {
            return true;
        }
        return StringUtils.equalsIgnoreCase(PRIMELOAD_MARKETPLACE, policy.appCode())
            && StringUtils.equalsIgnoreCase(SELF_OPERATED_MERCHANT, tenant.getTenantType());
    }

    private boolean isVisibleTenant(TenantFlowPolicy.AppTenantPolicy policy, TenantRegistryResponse tenant, Long currentTenantId) {
        if (currentTenantId == null) {
            return true;
        }
        if (currentTenantId.equals(tenant.getId())) {
            return true;
        }
        if (!StringUtils.equalsIgnoreCase(PMHUB, policy.appCode())) {
            return false;
        }
        return currentTenantId.equals(tenant.getParentTenantId());
    }

    private TenantSummaryResponse toTenantRegistry(TenantRegistryResponse tenant) {
        TenantSummaryResponse response = new TenantSummaryResponse();
        response.setTenantId(tenant.getId());
        response.setParentTenantId(tenant.getParentTenantId());
        response.setParentTenantName(tenant.getParentTenantName());
        response.setParentTenantCode(tenant.getParentTenantCode());
        response.setTenantType(tenant.getTenantType());
        response.setTenantName(tenant.getTenantName());
        response.setTenantCode(tenant.getTenantCode());
        response.setTenantSource(tenant.getTenantSource());
        response.setProjectAddress(tenant.getProjectAddress());
        response.setProvinceCode(tenant.getProvinceCode());
        response.setProvinceName(tenant.getProvinceName());
        response.setCityCode(tenant.getCityCode());
        response.setCityName(tenant.getCityName());
        response.setDistrictCode(tenant.getDistrictCode());
        response.setDistrictName(tenant.getDistrictName());
        response.setManagerName(tenant.getManagerName());
        response.setManagerPhone(tenant.getManagerPhone());
        response.setStatus(tenant.getStatus());
        return response;
    }
}

package com.hardrockunion.platform.iam.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.domain.entity.IamUserInfo;
import com.hardrockunion.platform.iam.domain.entity.IamUser;
import com.hardrockunion.platform.iam.domain.entity.IamDepartment;
import com.hardrockunion.platform.iam.dto.IamRoleResponse;
import com.hardrockunion.platform.iam.dto.IamUserCreateRequest;
import com.hardrockunion.platform.iam.dto.IamUserRoleAssignRequest;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentAssignRequest;
import com.hardrockunion.platform.iam.dto.IamUserResponse;
import com.hardrockunion.platform.iam.mapper.IamUserMapper;

/**
 * IAM 用户管理服务。
 *
 * <p>当前主要服务于 app 级租户后台的员工管理场景，例如 `primeload-marketplace` 商户员工新增与角色绑定。
 * 这里复用统一 IAM 账号体系，不再为每个 app 单独造一套账号模型。
 */
@Service
public class IamUserManageService {

    private final IamUserMapper iamUserMapper;
    private final IamRoleQueryService iamRoleQueryService;
    private final IamDepartmentQueryService iamDepartmentQueryService;
    private final IamDepartmentManageService iamDepartmentManageService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamTenantMemberService iamTenantMemberService;
    private final IamUserInfoService iamUserInfoService;
    private final IamTenantDepartmentRoleService iamTenantDepartmentRoleService;

    public IamUserManageService(IamUserMapper iamUserMapper,
                                IamRoleQueryService iamRoleQueryService,
                                IamDepartmentQueryService iamDepartmentQueryService,
                                IamDepartmentManageService iamDepartmentManageService,
                                AppRegistryQueryService appRegistryQueryService,
                                IamTenantMemberService iamTenantMemberService,
                                IamUserInfoService iamUserInfoService,
                                IamTenantDepartmentRoleService iamTenantDepartmentRoleService) {
        this.iamUserMapper = iamUserMapper;
        this.iamRoleQueryService = iamRoleQueryService;
        this.iamDepartmentQueryService = iamDepartmentQueryService;
        this.iamDepartmentManageService = iamDepartmentManageService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamTenantMemberService = iamTenantMemberService;
        this.iamUserInfoService = iamUserInfoService;
        this.iamTenantDepartmentRoleService = iamTenantDepartmentRoleService;
    }

    /**
     * 查询当前 app、当前租户下的员工列表。
     */
    public List<IamUserResponse> listUsers(String appCode, LoginUser loginUser) {
        ensureAdmin(appCode, loginUser);
        Long appId = resolveAppId(appCode);
        List<Long> memberUserIds = iamTenantMemberService.listActiveUserIds(appId, loginUser.getTenantId());
        if (memberUserIds.isEmpty()) {
            return List.of();
        }
        return iamUserMapper.selectList(new LambdaQueryWrapper<IamUser>()
                .eq(IamUser::getAppId, appId)
                .in(IamUser::getId, memberUserIds)
                .eq(IamUser::getDeleted, 0)
                .orderByAsc(IamUser::getId))
            .stream()
            .map(user -> toResponse(user, loginUser.getTenantId()))
            .toList();
    }

    /**
     * 查询指定用户当前已绑定的角色。
     */
    public List<IamRoleResponse> listUserRoles(String appCode, Long userId, LoginUser loginUser) {
        IamUser user = getManagedUser(appCode, userId, loginUser);
        Long appId = user.getAppId() == null ? resolveAppId(user.getAppCode()) : user.getAppId();
        return iamRoleQueryService.listRolesByUser(user.getId(), appId, loginUser.getTenantId());
    }

    /**
     * 创建员工账号并绑定角色。
     *
     * <p>账号创建阶段只落用户主数据，昵称和头像统一落到 iam_user_info。
     * 如果前端已经填写了资料，则顺手写入；否则后续再通过资料补全接口完善。
     * 角色分配严格限制在当前 app、当前租户的可用角色池内，避免跨 app 或跨租户串数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public IamUserResponse createUser(String appCode, IamUserCreateRequest request, LoginUser loginUser) {
        ensureAdmin(appCode, loginUser);
        if (request == null || StringUtils.isAnyBlank(request.getUsername(), request.getPassword())) {
            throw new BusinessException("username、password 不能为空");
        }
        Long appId = resolveAppId(loginUser.getAppCode());
        List<String> roleCodes = request.getRoleCodes() == null ? List.of() : request.getRoleCodes().stream()
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::trim)
            .map(StringUtils::upperCase)
            .distinct()
            .toList();
        if (roleCodes.isEmpty() && request.getDepartmentId() == null) {
            throw new BusinessException("至少选择一个角色或选择一个部门");
        }

        IamUser existed = iamUserMapper.selectOne(new LambdaQueryWrapper<IamUser>()
            .eq(IamUser::getAppId, appId)
            .eq(IamUser::getUsername, StringUtils.trim(request.getUsername()))
            .eq(IamUser::getDeleted, 0)
            .last("limit 1"));
        if (existed != null) {
            throw new BusinessException("账号已存在");
        }

        List<IamRole> roles = iamRoleQueryService.listEnabledRolesByCodes(appId, roleCodes);
        if (roles.size() != roleCodes.size()) {
            throw new BusinessException("角色不存在或已停用");
        }
        validateNexisAssignment(loginUser.getAppCode(), loginUser.getTenantId(), request.getDepartmentId(), roles);

        IamUser user = new IamUser();
        user.setAppId(appId);
        user.setAppCode(loginUser.getAppCode());
        user.setUsername(StringUtils.trim(request.getUsername()));
        user.setPasswordHash(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()));
        user.setStatus(1);
        user.setDeleted(0);
        iamUserMapper.insert(user);
        iamTenantMemberService.upsertActiveMember(appId, loginUser.getTenantId(), user.getId(), true);
        iamUserInfoService.upsertProfile(appId, user.getId(), request.getNickName(), request.getAvatarUrl());

        if (request.getDepartmentId() != null) {
            IamUserDepartmentAssignRequest assignRequest = new IamUserDepartmentAssignRequest();
            assignRequest.setDepartmentId(request.getDepartmentId());
            iamDepartmentManageService.assignUserDepartment(loginUser.getAppCode(), user.getId(), assignRequest, loginUser);
        }
        saveUserRoles(user, loginUser.getTenantId(), roles);
        return toResponse(user, loginUser.getTenantId());
    }

    /**
     * 重置指定用户的角色集合。
     *
     * <p>当前采用整组覆盖语义，传入的 roleCodes 会成为用户最终生效的全部角色。
     */
    @Transactional(rollbackFor = Exception.class)
    public IamUserResponse assignUserRoles(String appCode,
                                           Long userId,
                                           IamUserRoleAssignRequest request,
                                           LoginUser loginUser) {
        IamUser user = getManagedUser(appCode, userId, loginUser);
        List<String> roleCodes = normalizeRoleCodes(request == null ? null : request.getRoleCodes());
        if (roleCodes.isEmpty()) {
            throw new BusinessException("至少选择一个角色");
        }
        Long appId = user.getAppId() == null ? resolveAppId(user.getAppCode()) : user.getAppId();
        List<IamRole> roles = iamRoleQueryService.listEnabledRolesByCodes(appId, roleCodes);
        if (roles.size() != roleCodes.size()) {
            throw new BusinessException("角色不存在或已停用");
        }
        IamDepartment primaryDepartment = iamDepartmentQueryService.getPrimaryDepartmentByUser(
            user.getId(), appId, loginUser.getTenantId());
        validateNexisAssignment(appCode, loginUser.getTenantId(),
            primaryDepartment == null ? null : primaryDepartment.getId(), roles);
        saveUserRoles(user, loginUser.getTenantId(), roles);
        return toResponse(user, loginUser.getTenantId());
    }

    /**
     * 只有具备管理员角色的登录人，才允许管理当前租户员工。
     */
    private void ensureAdmin(String appCode, LoginUser loginUser) {
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        Long appId = resolveAppId(appCode);
        boolean hasAdminRole = iamRoleQueryService.listRoleEntitiesByUser(loginUser.getUserId(), appId, loginUser.getTenantId())
            .stream()
            .anyMatch(iamRoleQueryService::isAdminRole);
        if (!hasAdminRole) {
            throw new BusinessException("当前账号没有管理员权限");
        }
    }

    private IamUser getManagedUser(String appCode, Long userId, LoginUser loginUser) {
        ensureAdmin(appCode, loginUser);
        if (userId == null) {
            throw new BusinessException("userId 不能为空");
        }
        IamUser user = iamUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户不存在");
        }
        Long appId = resolveAppId(appCode);
        Long userAppId = user.getAppId() == null ? resolveAppId(user.getAppCode()) : user.getAppId();
        if (!appId.equals(userAppId) || iamTenantMemberService.getActiveMember(appId, loginUser.getTenantId(), user.getId()) == null) {
            throw new BusinessException("用户不属于当前 app 或当前租户");
        }
        return user;
    }

    private List<String> normalizeRoleCodes(List<String> roleCodes) {
        return roleCodes == null ? List.of() : roleCodes.stream()
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::trim)
            .map(StringUtils::upperCase)
            .distinct()
            .toList();
    }

    private void saveUserRoles(IamUser user, Long tenantId, List<IamRole> roles) {
        Long departmentId = null;
        IamDepartment primaryDepartment = iamDepartmentQueryService.getPrimaryDepartmentByUser(
            user.getId(),
            user.getAppId() == null ? resolveAppId(user.getAppCode()) : user.getAppId(),
            tenantId
        );
        if (primaryDepartment != null) {
            departmentId = primaryDepartment.getId();
        }
        iamTenantDepartmentRoleService.replaceRoles(
            user.getAppCode(),
            tenantId,
            user.getId(),
            departmentId,
            roles.stream().map(IamRole::getId).toList()
        );
    }

    private void validateNexisAssignment(String appCode,
                                         Long tenantId,
                                         Long departmentId,
                                         List<IamRole> roles) {
        if (!StringUtils.equalsIgnoreCase("NEXIS", appCode)) {
            return;
        }
        if (departmentId == null) {
            throw new BusinessException("Nexis 成员必须选择当前空间的部门");
        }
        if (roles.stream().anyMatch(iamRoleQueryService::isAdminRole)) {
            throw new BusinessException("空间负责人角色不能通过成员页面分配");
        }
        IamDepartment department = iamDepartmentQueryService.getDepartmentEntity(resolveAppId(appCode), departmentId);
        iamDepartmentQueryService.ensureDepartmentAssignableToTenant(appCode, tenantId, department);
        iamDepartmentQueryService.ensureRolesAssignableToDepartment(departmentId, roles);
    }

    private Long resolveAppId(String appCode) {
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        return app.getId();
    }

    private IamUserResponse toResponse(IamUser user, Long tenantId) {
        IamUserResponse response = new IamUserResponse();
        response.setId(user.getId());
        response.setAppCode(user.getAppCode());
        response.setTenantId(tenantId);
        Long appId = user.getAppId() == null ? resolveAppId(user.getAppCode()) : user.getAppId();
        IamDepartment primaryDepartment = iamDepartmentQueryService.getPrimaryDepartmentByUser(user.getId(), user.getAppCode(), tenantId);
        if (primaryDepartment != null) {
            response.setDepartmentId(primaryDepartment.getId());
            response.setDepartmentCode(primaryDepartment.getDeptCode());
            response.setDepartmentName(primaryDepartment.getDeptName());
        }
        response.setUsername(user.getUsername());
        IamUserInfo profile = iamUserInfoService.getActiveUserInfo(appId, user.getId());
        response.setNickName(profile == null ? null : profile.getNickName());
        response.setAvatarUrl(profile == null ? null : profile.getAvatarUrl());
        response.setStatus(user.getStatus());
        response.setRoleCodes(iamRoleQueryService.listRoleEntitiesByUser(user.getId(), appId, tenantId)
            .stream()
            .map(IamRole::getRoleCode)
            .toList());
        return response;
    }
}

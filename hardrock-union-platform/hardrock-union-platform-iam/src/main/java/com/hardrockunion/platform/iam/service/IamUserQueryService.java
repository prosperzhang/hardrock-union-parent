package com.hardrockunion.platform.iam.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamDepartment;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;
import com.hardrockunion.platform.iam.domain.entity.IamUserInfo;
import com.hardrockunion.platform.iam.domain.entity.IamUser;
import com.hardrockunion.platform.iam.dto.CurrentUserResponse;
import com.hardrockunion.platform.iam.mapper.IamUserMapper;

@Service
public class IamUserQueryService {

    private final IamUserMapper iamUserMapper;
    private final IamRoleQueryService iamRoleQueryService;
    private final IamDepartmentQueryService iamDepartmentQueryService;
    private final IamPermissionQueryService iamPermissionQueryService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamTenantMemberService iamTenantMemberService;
    private final IamUserInfoService iamUserInfoService;

    public IamUserQueryService(IamUserMapper iamUserMapper,
                               IamRoleQueryService iamRoleQueryService,
                               IamDepartmentQueryService iamDepartmentQueryService,
                               IamPermissionQueryService iamPermissionQueryService,
                               AppRegistryQueryService appRegistryQueryService,
                               IamTenantMemberService iamTenantMemberService,
                               IamUserInfoService iamUserInfoService) {
        this.iamUserMapper = iamUserMapper;
        this.iamRoleQueryService = iamRoleQueryService;
        this.iamDepartmentQueryService = iamDepartmentQueryService;
        this.iamPermissionQueryService = iamPermissionQueryService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamTenantMemberService = iamTenantMemberService;
        this.iamUserInfoService = iamUserInfoService;
    }

    public CurrentUserResponse currentUser(String appCode, LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        String normalizedAppCode = app.getAppCode();
        String normalizedLoginAppCode = appRegistryQueryService.getAppByCode(loginUser.getAppCode()).getAppCode();
        if (!StringUtils.equals(normalizedAppCode, normalizedLoginAppCode)) {
            throw new BusinessException("当前登录态与访问入口不一致");
        }

        IamUser user = iamUserMapper.selectById(loginUser.getUserId());
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("当前用户不存在");
        }
        if (!StringUtils.equals(normalizedAppCode, appRegistryQueryService.getAppByCode(user.getAppCode()).getAppCode())) {
            throw new BusinessException("当前用户不属于该 app");
        }

        Long appId = user.getAppId() != null ? user.getAppId() : app.getId();
        CurrentUserResponse response = new CurrentUserResponse();
        response.setUserId(user.getId());
        response.setAppCode(user.getAppCode());
        response.setUsername(user.getUsername());
        IamUserInfo profile = iamUserInfoService.getActiveUserInfo(appId, user.getId());
        response.setNickName(profile == null ? null : profile.getNickName());
        response.setAvatarUrl(profile == null ? null : profile.getAvatarUrl());
        if (loginUser.getTenantId() == null) {
            List<IamTenantMember> members = iamTenantMemberService.listActiveMembersByUser(appId, user.getId());
            if (members.isEmpty()) {
                ensureAppAccessible(app, user.getId(), null, List.of());
                response.setRoles(List.of());
                response.setPermissions(List.of());
                return response;
            }
            IamTenantMember member = members.getFirst();
            response.setTenantId(member.getTenantId());
            IamDepartment primaryDepartment = iamDepartmentQueryService.getPrimaryDepartmentByUser(user.getId(), appId, member.getTenantId());
            if (primaryDepartment != null) {
                response.setDepartmentId(primaryDepartment.getId());
                response.setDepartmentCode(primaryDepartment.getDeptCode());
                response.setDepartmentName(primaryDepartment.getDeptName());
                response.setDepartmentShortName(primaryDepartment.getDeptShortName());
            }
            List<IamRole> roles = iamRoleQueryService.listRoleEntitiesByUser(user.getId(), appId, member.getTenantId());
            ensureAppAccessible(app, user.getId(), member.getTenantId(), roles);
            response.setRoles(roles.stream().map(IamRole::getRoleCode).toList());
            response.setPermissions(iamPermissionQueryService.listPermissionCodesByUser(user.getId(), appId, member.getTenantId()));
            response.setMenus(iamPermissionQueryService.listMenuTreeByUser(user.getId(), appId, member.getTenantId()));
            return response;
        }

        IamTenantMember member = iamTenantMemberService.ensureActiveMember(user, appId, loginUser.getTenantId(), true);
        if (member == null || !"ACTIVE".equalsIgnoreCase(member.getMemberStatus())) {
            throw new BusinessException("当前用户不属于该租户");
        }

        response.setTenantId(member.getTenantId());
        IamDepartment primaryDepartment = iamDepartmentQueryService.getPrimaryDepartmentByUser(user.getId(), appId, member.getTenantId());
        if (primaryDepartment != null) {
            response.setDepartmentId(primaryDepartment.getId());
            response.setDepartmentCode(primaryDepartment.getDeptCode());
            response.setDepartmentName(primaryDepartment.getDeptName());
            response.setDepartmentShortName(primaryDepartment.getDeptShortName());
        }
        List<IamRole> roles = iamRoleQueryService.listRoleEntitiesByUser(user.getId(), appId, member.getTenantId());
        ensureAppAccessible(app, user.getId(), member.getTenantId(), roles);
        response.setRoles(roles.stream().map(IamRole::getRoleCode).toList());
        response.setPermissions(iamPermissionQueryService.listPermissionCodesByUser(user.getId(), appId, member.getTenantId()));
        response.setMenus(iamPermissionQueryService.listMenuTreeByUser(user.getId(), appId, member.getTenantId()));
        return response;
    }

    private void ensureAppAccessible(AppRegistry app, Long userId, Long tenantId, List<IamRole> roles) {
        if (app == null || Integer.valueOf(1).equals(app.getStatus())) {
            return;
        }
        if (StringUtils.equalsIgnoreCase("WSGM", app.getAppCode())
            && roles.stream().anyMatch(role -> StringUtils.equalsIgnoreCase("WSGM_SUPER_ADMIN", role.getRoleCode()))) {
            return;
        }
        throw new BusinessException("应用已禁用");
    }
}

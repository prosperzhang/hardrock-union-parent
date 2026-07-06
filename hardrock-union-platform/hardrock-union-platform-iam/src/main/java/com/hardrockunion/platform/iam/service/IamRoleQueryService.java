package com.hardrockunion.platform.iam.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.dto.IamRoleResponse;
import com.hardrockunion.platform.iam.mapper.IamRoleMapper;

@Service
public class IamRoleQueryService {

    private static final String WSGM_APP_CODE = "WSGM";
    private static final String WSGM_SUPER_ADMIN_ROLE_CODE = "WSGM_SUPER_ADMIN";

    private final IamRoleMapper iamRoleMapper;
    private final IamTenantDepartmentRoleService iamTenantDepartmentRoleService;
    private final AppRegistryQueryService appRegistryQueryService;

    public IamRoleQueryService(IamRoleMapper iamRoleMapper,
                               IamTenantDepartmentRoleService iamTenantDepartmentRoleService,
                               AppRegistryQueryService appRegistryQueryService) {
        this.iamRoleMapper = iamRoleMapper;
        this.iamTenantDepartmentRoleService = iamTenantDepartmentRoleService;
        this.appRegistryQueryService = appRegistryQueryService;
    }

    public List<IamRoleResponse> listRoles(String appCode, LoginUser loginUser) {
        ensureAppLogin(appCode, loginUser);
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return listRolesByUser(loginUser.getUserId(), appId, loginUser.getTenantId());
    }

    /**
     * 返回当前 app 下的共享角色定义。
     *
     * <p>角色定义已经从“租户复制一份”收口成“每个 app 共享一套”，
     * 所以这里不再按 tenantId 过滤业务含义，只取共享角色记录。
     */
    public List<IamRoleResponse> listSharedRoles(String appCode, LoginUser loginUser) {
        ensureAppLogin(appCode, loginUser);
        String normalizedAppCode = normalizeAppCode(appCode);
        Long appId = resolveAppId(normalizedAppCode);
        QueryWrapper<IamRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId)
            .eq("deleted", 0)
            .eq("status", 1)
            .eq("assignable", 1)
            .orderByAsc("id");
        return iamRoleMapper.selectList(queryWrapper)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<IamRole> listRoleEntitiesByUser(Long userId, String appCode, Long tenantId) {
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return listRoleEntitiesByUser(userId, appId, tenantId);
    }

    public List<IamRole> listRoleEntitiesByUser(Long userId, Long appId, Long tenantId) {
        if (userId == null || appId == null || tenantId == null) {
            return List.of();
        }
        AppRegistry app = appRegistryQueryService.getAppById(appId);
        List<Long> directRoleIds = iamTenantDepartmentRoleService.listActiveRoleIds(app.getAppCode(), tenantId, userId);
        if (directRoleIds.isEmpty()) {
            return List.of();
        }
        return iamRoleMapper.selectList(new LambdaQueryWrapper<IamRole>()
            .eq(IamRole::getAppId, appId)
            .in(IamRole::getId, directRoleIds)
            .eq(IamRole::getDeleted, 0)
            .eq(IamRole::getStatus, 1)
            .orderByAsc(IamRole::getId));
    }

    public List<IamRoleResponse> listRolesByUser(Long userId, String appCode, Long tenantId) {
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return listRolesByUser(userId, appId, tenantId);
    }

    public List<IamRoleResponse> listRolesByUser(Long userId, Long appId, Long tenantId) {
        return listRoleEntitiesByUser(userId, appId, tenantId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<IamRoleResponse> listManageRoles(String appCode, LoginUser loginUser) {
        ensureWsgmRoleAdmin(loginUser);
        String normalizedAppCode = normalizeAppCode(appCode);
        Long appId = resolveAppId(normalizedAppCode);
        return iamRoleMapper.selectList(new LambdaQueryWrapper<IamRole>()
                .eq(IamRole::getAppId, appId)
                .eq(IamRole::getDeleted, 0)
                .orderByAsc(IamRole::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public IamRoleResponse getRoleDetail(String appCode, Long roleId, LoginUser loginUser) {
        ensureWsgmRoleAdmin(loginUser);
        return toResponse(getRoleEntity(appCode, roleId));
    }

    public List<IamRole> listEnabledRolesByCodes(String appCode, List<String> roleCodes) {
        if (StringUtils.isBlank(appCode) || roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        String normalizedAppCode = normalizeAppCode(appCode);
        List<String> normalizedRoleCodes = roleCodes.stream()
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::trim)
            .map(StringUtils::upperCase)
            .distinct()
            .toList();
        if (normalizedRoleCodes.isEmpty()) {
            return List.of();
        }
        Long appId = resolveAppId(normalizedAppCode);
        QueryWrapper<IamRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId)
            .in("role_code", normalizedRoleCodes)
            .eq("deleted", 0)
            .eq("status", 1)
            .eq("assignable", 1)
            .orderByAsc("id");
        return iamRoleMapper.selectList(queryWrapper);
    }

    public List<IamRole> listEnabledRolesByCodes(Long appId, List<String> roleCodes) {
        if (appId == null || roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        List<String> normalizedRoleCodes = roleCodes.stream()
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::trim)
            .map(StringUtils::upperCase)
            .distinct()
            .toList();
        if (normalizedRoleCodes.isEmpty()) {
            return List.of();
        }
        QueryWrapper<IamRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId)
            .in("role_code", normalizedRoleCodes)
            .eq("deleted", 0)
            .eq("status", 1)
            .eq("assignable", 1)
            .orderByAsc("id");
        return iamRoleMapper.selectList(queryWrapper);
    }

    public boolean isAdminRole(IamRole role) {
        return role != null && Integer.valueOf(1).equals(role.getAdminRole());
    }

    public void ensureAdmin(String appCode, LoginUser loginUser) {
        ensureAppLogin(appCode, loginUser);
        Long appId = resolveAppId(normalizeAppCode(appCode));
        boolean hasAdminRole = listRoleEntitiesByUser(loginUser.getUserId(), appId, loginUser.getTenantId())
            .stream()
            .anyMatch(this::isAdminRole);
        if (!hasAdminRole) {
            throw new BusinessException("当前账号没有管理员权限");
        }
    }

    public void ensureWsgmRoleAdmin(LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        if (!StringUtils.equalsIgnoreCase(WSGM_APP_CODE, loginUser.getAppCode())) {
            throw new BusinessException("只有 WSGM 可以管理角色");
        }
        AppRegistry app = appRegistryQueryService.getAppByCode(loginUser.getAppCode());
        Long appId = app.getId();
        List<IamRole> roles = listRoleEntitiesByUser(loginUser.getUserId(), appId, loginUser.getTenantId());
        if (!Integer.valueOf(1).equals(app.getStatus())) {
            if (roles.stream().noneMatch(this::isWsgmSuperAdminRole)) {
                throw new BusinessException("顽石工盟已禁用，仅总部超级管理员可操作");
            }
            return;
        }
        if (roles.stream().noneMatch(this::isAdminRole)) {
            throw new BusinessException("当前账号没有角色管理权限");
        }
    }

    public IamRole getRoleEntity(String appCode, Long roleId) {
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return getRoleEntity(appId, roleId);
    }

    public IamRole getRoleEntity(Long appId, Long roleId) {
        if (roleId == null) {
            throw new BusinessException("角色ID不能为空");
        }
        IamRole role = iamRoleMapper.selectById(roleId);
        if (role == null || Integer.valueOf(1).equals(role.getDeleted())) {
            throw new BusinessException("角色不存在");
        }
        boolean appMatches = role.getAppId() != null && role.getAppId().equals(appId);
        if (!appMatches) {
            throw new BusinessException("角色不属于当前 app");
        }
        return role;
    }

    public void ensureAppLogin(String appCode, LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        AppRegistry loginApp = appRegistryQueryService.getAppByCode(loginUser.getAppCode());
        String normalizedAppCode = app.getAppCode();
        String normalizedLoginAppCode = loginApp.getAppCode();
        if (!StringUtils.equals(normalizedAppCode, normalizedLoginAppCode)) {
            throw new BusinessException("当前登录态与访问入口不一致");
        }
        if (Integer.valueOf(1).equals(app.getStatus())) {
            return;
        }
        if (StringUtils.equalsIgnoreCase(WSGM_APP_CODE, app.getAppCode())
            && listRoleEntitiesByUser(loginUser.getUserId(), app.getId(), loginUser.getTenantId()).stream()
                .anyMatch(this::isWsgmSuperAdminRole)) {
            return;
        }
        throw new BusinessException("应用已禁用");
    }

    public String normalizeAppCode(String appCode) {
        return appRegistryQueryService.normalizeRegisteredAppCode(appCode);
    }

    private Long resolveAppId(String appCode) {
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        return app.getId();
    }

    private boolean isWsgmSuperAdminRole(IamRole role) {
        return role != null
            && StringUtils.equalsIgnoreCase(WSGM_APP_CODE, role.getAppCode())
            && StringUtils.equalsIgnoreCase(WSGM_SUPER_ADMIN_ROLE_CODE, role.getRoleCode());
    }

    private IamRoleResponse toResponse(IamRole role) {
        IamRoleResponse response = new IamRoleResponse();
        response.setId(role.getId());
        response.setAppCode(role.getAppCode());
        response.setRoleCode(role.getRoleCode());
        response.setRoleName(role.getRoleName());
        response.setStatus(role.getStatus());
        response.setAssignable(role.getAssignable());
        response.setAdminRole(role.getAdminRole());
        return response;
    }
}

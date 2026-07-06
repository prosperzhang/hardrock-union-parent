package com.hardrockunion.platform.iam.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamDepartmentRolePermission;
import com.hardrockunion.platform.iam.domain.entity.IamPermission;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.domain.model.IamTenantDepartmentRoleBinding;
import com.hardrockunion.platform.iam.dto.IamPermissionResponse;
import com.hardrockunion.platform.iam.mapper.IamDepartmentRolePermissionMapper;
import com.hardrockunion.platform.iam.mapper.IamPermissionMapper;

@Service
public class IamPermissionQueryService {

    private static final String MENU = "MENU";

    private final IamPermissionMapper iamPermissionMapper;
    private final IamDepartmentRolePermissionMapper iamDepartmentRolePermissionMapper;
    private final IamRoleQueryService iamRoleQueryService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamTenantDepartmentRoleService iamTenantDepartmentRoleService;

    public IamPermissionQueryService(IamPermissionMapper iamPermissionMapper,
                                     IamDepartmentRolePermissionMapper iamDepartmentRolePermissionMapper,
                                     IamRoleQueryService iamRoleQueryService,
                                     AppRegistryQueryService appRegistryQueryService,
                                     IamTenantDepartmentRoleService iamTenantDepartmentRoleService) {
        this.iamPermissionMapper = iamPermissionMapper;
        this.iamDepartmentRolePermissionMapper = iamDepartmentRolePermissionMapper;
        this.iamRoleQueryService = iamRoleQueryService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamTenantDepartmentRoleService = iamTenantDepartmentRoleService;
    }

    public List<IamPermissionResponse> listPermissions(String appCode, LoginUser loginUser) {
        ensureWsgmPermissionAdmin(loginUser);
        Long appId = resolveAppId(iamRoleQueryService.normalizeAppCode(appCode));
        return listPermissions(appId);
    }

    public List<IamPermissionResponse> listPermissions(Long appId) {
        if (appId == null) {
            return List.of();
        }
        return iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermission>()
                .eq(IamPermission::getAppId, appId)
                .eq(IamPermission::getDeleted, 0)
                .orderByAsc(IamPermission::getSortNo)
                .orderByAsc(IamPermission::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<IamPermissionResponse> listPermissionTree(String appCode, LoginUser loginUser) {
        ensureWsgmPermissionAdmin(loginUser);
        Long appId = resolveAppId(iamRoleQueryService.normalizeAppCode(appCode));
        return listPermissionTree(listPermissions(appId));
    }

    public List<IamPermissionResponse> listPermissionTree(Long appId) {
        return listPermissionTree(listPermissions(appId));
    }

    public List<IamPermissionResponse> listMenuTree(Long appId) {
        return listPermissionTree(listEnabledMenus(appId));
    }

    public List<IamPermissionResponse> listMenuTreeByUser(Long userId, Long appId, Long tenantId) {
        if (userId == null || appId == null || tenantId == null) {
            return List.of();
        }
        List<IamRole> roles = iamRoleQueryService.listRoleEntitiesByUser(userId, appId, tenantId);
        if (roles.stream().anyMatch(iamRoleQueryService::isAdminRole)) {
            return listMenuTree(appId);
        }
        List<Long> permissionIds = listPermissionIdsByUserBindings(userId, appId, tenantId);
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = includeAncestorPermissionIds(appId, permissionIds);
        List<IamPermissionResponse> menus = iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermission>()
                .eq(IamPermission::getAppId, appId)
                .in(IamPermission::getId, menuIds)
                .eq(IamPermission::getPermissionType, MENU)
                .eq(IamPermission::getDeleted, 0)
                .eq(IamPermission::getStatus, 1)
                .orderByAsc(IamPermission::getSortNo)
                .orderByAsc(IamPermission::getId))
            .stream()
            .map(this::toResponse)
            .toList();
        return listPermissionTree(menus);
    }

    private List<Long> includeAncestorPermissionIds(Long appId, List<Long> permissionIds) {
        List<Long> result = new ArrayList<>();
        if (appId == null || permissionIds == null || permissionIds.isEmpty()) {
            return result;
        }
        for (Long permissionId : permissionIds.stream().filter(Objects::nonNull).distinct().toList()) {
            appendPermissionAndAncestors(appId, permissionId, result);
        }
        return result.stream().distinct().toList();
    }

    private void appendPermissionAndAncestors(Long appId, Long permissionId, List<Long> result) {
        if (permissionId == null || result.contains(permissionId)) {
            return;
        }
        IamPermission permission = iamPermissionMapper.selectById(permissionId);
        if (permission == null
            || !appId.equals(permission.getAppId())
            || Integer.valueOf(1).equals(permission.getDeleted())
            || !Integer.valueOf(1).equals(permission.getStatus())) {
            return;
        }
        result.add(permission.getId());
        Long parentId = permission.getParentId();
        if (parentId != null && parentId > 0) {
            appendPermissionAndAncestors(appId, parentId, result);
        }
    }

    public IamPermission getPermissionEntity(String appCode, Long permissionId) {
        Long appId = resolveAppId(iamRoleQueryService.normalizeAppCode(appCode));
        return getPermissionEntity(appId, permissionId);
    }

    public IamPermission getPermissionEntity(Long appId, Long permissionId) {
        if (permissionId == null) {
            throw new BusinessException("权限ID不能为空");
        }
        IamPermission permission = iamPermissionMapper.selectById(permissionId);
        if (permission == null || Integer.valueOf(1).equals(permission.getDeleted())) {
            throw new BusinessException("权限不存在");
        }
        boolean appMatches = permission.getAppId() != null && permission.getAppId().equals(appId);
        if (!appMatches) {
            throw new BusinessException("权限不属于当前 app");
        }
        return permission;
    }

    public IamPermissionResponse getPermissionDetail(String appCode, Long permissionId, LoginUser loginUser) {
        ensureWsgmPermissionAdmin(loginUser);
        Long appId = resolveAppId(iamRoleQueryService.normalizeAppCode(appCode));
        return toResponse(getPermissionEntity(appId, permissionId));
    }

    public IamPermissionResponse getPermissionDetail(Long appId, Long permissionId, LoginUser loginUser) {
        ensureWsgmPermissionAdmin(loginUser);
        return toResponse(getPermissionEntity(appId, permissionId));
    }

    public List<IamPermission> listEnabledPermissionsByCodes(String appCode, List<String> permissionCodes) {
        if (StringUtils.isBlank(appCode) || permissionCodes == null || permissionCodes.isEmpty()) {
            return List.of();
        }
        String normalizedAppCode = iamRoleQueryService.normalizeAppCode(appCode);
        return listEnabledPermissionsByCodes(resolveAppId(normalizedAppCode), permissionCodes);
    }

    public List<IamPermission> listEnabledPermissionsByCodes(Long appId, List<String> permissionCodes) {
        if (appId == null || permissionCodes == null || permissionCodes.isEmpty()) {
            return List.of();
        }
        List<String> normalizedPermissionCodes = permissionCodes.stream()
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::trim)
            .map(StringUtils::upperCase)
            .distinct()
            .toList();
        if (normalizedPermissionCodes.isEmpty()) {
            return List.of();
        }
        return iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermission>()
                .eq(IamPermission::getAppId, appId)
                .in(IamPermission::getPermissionCode, normalizedPermissionCodes)
                .eq(IamPermission::getDeleted, 0)
                .eq(IamPermission::getStatus, 1)
                .orderByAsc(IamPermission::getSortNo)
                .orderByAsc(IamPermission::getId));
    }

    public List<IamPermissionResponse> listPermissionsByDepartmentRole(String appCode,
                                                                       Long departmentId,
                                                                       Long roleId,
                                                                       LoginUser loginUser) {
        ensureWsgmPermissionAdmin(loginUser);
        Long appId = resolveAppId(iamRoleQueryService.normalizeAppCode(appCode));
        return listPermissionEntitiesByDepartmentRole(appId, departmentId, roleId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<IamPermission> listPermissionEntitiesByDepartmentRole(Long appId, Long departmentId, Long roleId) {
        if (appId == null || departmentId == null || roleId == null) {
            return List.of();
        }
        List<Long> permissionIds = iamDepartmentRolePermissionMapper.selectList(new LambdaQueryWrapper<IamDepartmentRolePermission>()
                .eq(IamDepartmentRolePermission::getAppId, appId)
                .eq(IamDepartmentRolePermission::getDepartmentId, departmentId)
                .eq(IamDepartmentRolePermission::getRoleId, roleId)
                .eq(IamDepartmentRolePermission::getDeleted, 0))
            .stream()
            .map(IamDepartmentRolePermission::getPermissionId)
            .distinct()
            .toList();
        return listEnabledPermissionsByIds(appId, permissionIds);
    }

    public List<String> listPermissionCodesByUser(Long userId, String appCode, Long tenantId) {
        Long appId = resolveAppId(iamRoleQueryService.normalizeAppCode(appCode));
        return listPermissionCodesByUser(userId, appId, tenantId);
    }

    public List<String> listPermissionCodesByUser(Long userId, Long appId, Long tenantId) {
        if (userId == null || appId == null || tenantId == null) {
            return List.of();
        }
        List<IamRole> roles = iamRoleQueryService.listRoleEntitiesByUser(userId, appId, tenantId);
        if (roles.stream().anyMatch(iamRoleQueryService::isAdminRole)) {
            return listEnabledPermissionCodes(appId);
        }
        List<Long> permissionIds = listPermissionIdsByUserBindings(userId, appId, tenantId);
        return listEnabledPermissionsByIds(appId, permissionIds).stream()
            .map(IamPermission::getPermissionCode)
            .distinct()
            .toList();
    }

    public void ensureWsgmPermissionAdmin(LoginUser loginUser) {
        iamRoleQueryService.ensureWsgmRoleAdmin(loginUser);
    }

    private List<Long> listPermissionIdsByUserBindings(Long userId, Long appId, Long tenantId) {
        AppRegistry app = appRegistryQueryService.getAppById(appId);
        List<IamTenantDepartmentRoleBinding> bindings = iamTenantDepartmentRoleService.listActiveBindings(app.getAppCode(), tenantId, userId)
            .stream()
            .filter(binding -> binding.getDepartmentId() != null && binding.getDepartmentId() > 0)
            .filter(binding -> binding.getRoleId() != null && binding.getRoleId() > 0)
            .toList();
        if (bindings.isEmpty()) {
            return List.of();
        }
        List<Long> permissionIds = new ArrayList<>();
        for (IamTenantDepartmentRoleBinding binding : bindings) {
            iamDepartmentRolePermissionMapper.selectList(new LambdaQueryWrapper<IamDepartmentRolePermission>()
                    .eq(IamDepartmentRolePermission::getAppId, appId)
                    .eq(IamDepartmentRolePermission::getDepartmentId, binding.getDepartmentId())
                    .eq(IamDepartmentRolePermission::getRoleId, binding.getRoleId())
                    .eq(IamDepartmentRolePermission::getDeleted, 0))
                .stream()
                .map(IamDepartmentRolePermission::getPermissionId)
                .filter(Objects::nonNull)
                .forEach(permissionIds::add);
        }
        return permissionIds.stream().distinct().toList();
    }

    private List<IamPermission> listEnabledPermissionsByIds(Long appId, List<Long> permissionIds) {
        if (appId == null || permissionIds == null || permissionIds.isEmpty()) {
            return List.of();
        }
        return iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermission>()
            .eq(IamPermission::getAppId, appId)
            .in(IamPermission::getId, permissionIds.stream().filter(Objects::nonNull).distinct().toList())
            .eq(IamPermission::getDeleted, 0)
            .eq(IamPermission::getStatus, 1)
            .orderByAsc(IamPermission::getSortNo)
            .orderByAsc(IamPermission::getId));
    }

    private List<String> listEnabledPermissionCodes(Long appId) {
        if (appId == null) {
            return List.of();
        }
        return iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermission>()
                .eq(IamPermission::getAppId, appId)
                .eq(IamPermission::getDeleted, 0)
                .eq(IamPermission::getStatus, 1)
                .orderByAsc(IamPermission::getSortNo)
                .orderByAsc(IamPermission::getId))
            .stream()
            .map(IamPermission::getPermissionCode)
            .distinct()
            .toList();
    }

    private List<IamPermissionResponse> listPermissionTree(List<IamPermissionResponse> flatPermissions) {
        Map<Long, IamPermissionResponse> byId = new LinkedHashMap<>();
        for (IamPermissionResponse permission : flatPermissions) {
            permission.setChildren(new ArrayList<>());
            byId.put(permission.getId(), permission);
        }
        List<IamPermissionResponse> roots = new ArrayList<>();
        for (IamPermissionResponse permission : flatPermissions) {
            Long parentId = permission.getParentId();
            if (parentId == null || parentId <= 0 || !byId.containsKey(parentId)) {
                roots.add(permission);
                continue;
            }
            IamPermissionResponse parent = byId.get(parentId);
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(permission);
        }
        sortTree(roots);
        return roots;
    }

    private IamPermissionResponse toResponse(IamPermission permission) {
        IamPermissionResponse response = new IamPermissionResponse();
        response.setId(permission.getId());
        response.setAppCode(permission.getAppCode());
        response.setPermissionCode(permission.getPermissionCode());
        response.setPermissionName(permission.getPermissionName());
        response.setPermissionType(permission.getPermissionType());
        response.setParentId(permission.getParentId());
        response.setPermissionPath(permission.getPermissionPath());
        response.setHttpMethod(permission.getHttpMethod());
        response.setComponent(permission.getComponent());
        response.setStatus(permission.getStatus());
        response.setSortNo(permission.getSortNo());
        return response;
    }

    private Long resolveAppId(String appCode) {
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        return app.getId();
    }

    private List<IamPermissionResponse> listEnabledMenus(Long appId) {
        return iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermission>()
                .eq(IamPermission::getAppId, appId)
                .eq(IamPermission::getPermissionType, MENU)
            .eq(IamPermission::getDeleted, 0)
            .eq(IamPermission::getStatus, 1)
            .orderByAsc(IamPermission::getSortNo)
            .orderByAsc(IamPermission::getId))
            .stream()
            .filter(permission -> StringUtils.endsWithIgnoreCase(permission.getPermissionCode(), "_MENU"))
            .map(this::toResponse)
            .toList();
    }

    private void sortTree(List<IamPermissionResponse> nodes) {
        nodes.sort(Comparator.comparing(IamPermissionResponse::getSortNo, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(IamPermissionResponse::getId, Comparator.nullsLast(Long::compareTo)));
        for (IamPermissionResponse node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }
}

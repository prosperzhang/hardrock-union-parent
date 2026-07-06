package com.hardrockunion.platform.iam.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamDepartment;
import com.hardrockunion.platform.iam.domain.entity.IamDepartmentRoleOption;
import com.hardrockunion.platform.iam.domain.entity.IamDepartmentRolePermission;
import com.hardrockunion.platform.iam.domain.entity.IamPermission;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.dto.IamDepartmentRolePermissionAssignRequest;
import com.hardrockunion.platform.iam.dto.IamPermissionCreateRequest;
import com.hardrockunion.platform.iam.dto.IamPermissionResponse;
import com.hardrockunion.platform.iam.dto.IamPermissionUpdateRequest;
import com.hardrockunion.platform.iam.mapper.IamDepartmentMapper;
import com.hardrockunion.platform.iam.mapper.IamDepartmentRoleOptionMapper;
import com.hardrockunion.platform.iam.mapper.IamDepartmentRolePermissionMapper;
import com.hardrockunion.platform.iam.mapper.IamPermissionMapper;

@Service
public class IamPermissionManageService {

    private static final List<String> ALLOWED_PERMISSION_TYPES = List.of("MENU", "BUTTON", "API", "DATA");

    private final IamPermissionMapper iamPermissionMapper;
    private final IamDepartmentMapper iamDepartmentMapper;
    private final IamDepartmentRoleOptionMapper iamDepartmentRoleOptionMapper;
    private final IamDepartmentRolePermissionMapper iamDepartmentRolePermissionMapper;
    private final IamPermissionQueryService iamPermissionQueryService;
    private final IamRoleQueryService iamRoleQueryService;
    private final AppRegistryQueryService appRegistryQueryService;

    public IamPermissionManageService(IamPermissionMapper iamPermissionMapper,
                                      IamDepartmentMapper iamDepartmentMapper,
                                      IamDepartmentRoleOptionMapper iamDepartmentRoleOptionMapper,
                                      IamDepartmentRolePermissionMapper iamDepartmentRolePermissionMapper,
                                      IamPermissionQueryService iamPermissionQueryService,
                                      IamRoleQueryService iamRoleQueryService,
                                      AppRegistryQueryService appRegistryQueryService) {
        this.iamPermissionMapper = iamPermissionMapper;
        this.iamDepartmentMapper = iamDepartmentMapper;
        this.iamDepartmentRoleOptionMapper = iamDepartmentRoleOptionMapper;
        this.iamDepartmentRolePermissionMapper = iamDepartmentRolePermissionMapper;
        this.iamPermissionQueryService = iamPermissionQueryService;
        this.iamRoleQueryService = iamRoleQueryService;
        this.appRegistryQueryService = appRegistryQueryService;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamPermissionResponse createPermission(String appCode, IamPermissionCreateRequest request, LoginUser loginUser) {
        ensureManagePermissionAdmin(loginUser);
        String normalizedAppCode = iamRoleQueryService.normalizeAppCode(appCode);
        Long appId = resolveAppId(normalizedAppCode);
        IamPermission permission = buildPermission(normalizedAppCode, request, null);
        IamPermission existed = iamPermissionMapper.selectOne(new LambdaQueryWrapper<IamPermission>()
            .eq(IamPermission::getAppId, appId)
            .eq(IamPermission::getPermissionCode, permission.getPermissionCode())
            .eq(IamPermission::getDeleted, 0)
            .last("limit 1"));
        if (existed != null) {
            throw new BusinessException("权限编码已存在");
        }
        validateParent(normalizedAppCode, permission.getParentId(), null);
        iamPermissionMapper.insert(permission);
        return iamPermissionQueryService.getPermissionDetail(appId, permission.getId(), loginUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamPermissionResponse updatePermission(String appCode, Long permissionId, IamPermissionUpdateRequest request, LoginUser loginUser) {
        ensureManagePermissionAdmin(loginUser);
        IamPermission permission = iamPermissionQueryService.getPermissionEntity(appCode, permissionId);
        if (request == null || StringUtils.isBlank(request.getPermissionName())) {
            throw new BusinessException("permissionName 不能为空");
        }
        IamPermission updated = buildPermission(permission.getAppCode(), request, permission);
        validateParent(permission.getAppCode(), updated.getParentId(), permission.getId());
        permission.setPermissionName(updated.getPermissionName());
        permission.setPermissionType(updated.getPermissionType());
        permission.setParentId(updated.getParentId());
        permission.setPermissionPath(updated.getPermissionPath());
        permission.setHttpMethod(updated.getHttpMethod());
        permission.setComponent(updated.getComponent());
        permission.setStatus(updated.getStatus());
        permission.setSortNo(updated.getSortNo());
        iamPermissionMapper.updateById(permission);
        return iamPermissionQueryService.getPermissionDetail(permission.getAppId(), permissionId, loginUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(String appCode, Long permissionId, LoginUser loginUser) {
        ensureManagePermissionAdmin(loginUser);
        IamPermission permission = iamPermissionQueryService.getPermissionEntity(appCode, permissionId);
        Long appId = resolveAppId(permission.getAppCode());
        long childCount = iamPermissionMapper.selectCount(new LambdaQueryWrapper<IamPermission>()
            .eq(IamPermission::getAppId, appId)
            .eq(IamPermission::getParentId, permission.getId())
            .eq(IamPermission::getDeleted, 0));
        if (childCount > 0) {
            throw new BusinessException("该权限下还有子权限，不能删除");
        }
        long bindingCount = iamDepartmentRolePermissionMapper.selectCount(new LambdaQueryWrapper<IamDepartmentRolePermission>()
            .eq(IamDepartmentRolePermission::getAppId, appId)
            .eq(IamDepartmentRolePermission::getPermissionId, permission.getId())
            .eq(IamDepartmentRolePermission::getDeleted, 0));
        if (bindingCount > 0) {
            throw new BusinessException("该权限已分配给部门角色，不能删除");
        }
        permission.setDeleted(1);
        iamPermissionMapper.updateById(permission);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<IamPermissionResponse> assignDepartmentRolePermissions(String appCode,
                                                                       Long departmentId,
                                                                       Long roleId,
                                                                       IamDepartmentRolePermissionAssignRequest request,
                                                                       LoginUser loginUser) {
        ensureManagePermissionAdmin(loginUser);
        String normalizedAppCode = iamRoleQueryService.normalizeAppCode(appCode);
        Long appId = resolveAppId(normalizedAppCode);
        IamDepartment department = iamDepartmentMapper.selectById(departmentId);
        if (department == null || Integer.valueOf(1).equals(department.getDeleted()) || !appId.equals(department.getAppId())) {
            throw new BusinessException("部门不存在");
        }
        IamRole role = iamRoleQueryService.getRoleEntity(appCode, roleId);
        ensureDepartmentRoleExists(appId, department.getId(), role.getId());
        List<String> permissionCodes = normalizePermissionCodes(request == null ? null : request.getPermissionCodes());
        List<IamPermission> permissions = iamPermissionQueryService.listEnabledPermissionsByCodes(appId, permissionCodes);
        if (permissions.size() != permissionCodes.size()) {
            throw new BusinessException("权限不存在或已停用");
        }

        List<IamDepartmentRolePermission> existingRelations = iamDepartmentRolePermissionMapper.selectList(new LambdaQueryWrapper<IamDepartmentRolePermission>()
            .eq(IamDepartmentRolePermission::getAppId, appId)
            .eq(IamDepartmentRolePermission::getDepartmentId, department.getId())
            .eq(IamDepartmentRolePermission::getRoleId, role.getId()));
        Map<Long, IamDepartmentRolePermission> existingByPermissionId = existingRelations.stream()
            .collect(java.util.stream.Collectors.toMap(IamDepartmentRolePermission::getPermissionId, Function.identity(), (left, right) -> left, HashMap::new));
        List<Long> targetPermissionIds = permissions.stream()
            .map(IamPermission::getId)
            .toList();

        for (IamDepartmentRolePermission existingRelation : existingRelations) {
            if (!targetPermissionIds.contains(existingRelation.getPermissionId()) && Integer.valueOf(0).equals(existingRelation.getDeleted())) {
                existingRelation.setAppId(appId);
                existingRelation.setDeleted(1);
                iamDepartmentRolePermissionMapper.updateById(existingRelation);
            }
        }

        for (IamPermission permission : permissions) {
            IamDepartmentRolePermission existingRelation = existingByPermissionId.get(permission.getId());
            if (existingRelation != null) {
                existingRelation.setAppId(appId);
                if (Integer.valueOf(1).equals(existingRelation.getDeleted())) {
                    existingRelation.setDeleted(0);
                    iamDepartmentRolePermissionMapper.updateById(existingRelation);
                }
                continue;
            }
            IamDepartmentRolePermission rolePermission = new IamDepartmentRolePermission();
            rolePermission.setAppId(appId);
            rolePermission.setAppCode(role.getAppCode());
            rolePermission.setDepartmentId(department.getId());
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermission.setDeleted(0);
            iamDepartmentRolePermissionMapper.insert(rolePermission);
        }
        return iamPermissionQueryService.listPermissionsByDepartmentRole(appCode, department.getId(), roleId, loginUser);
    }

    private IamPermission buildPermission(String appCode, IamPermissionCreateRequest request, IamPermission current) {
        if (request == null || StringUtils.isAnyBlank(request.getPermissionCode(), request.getPermissionName())) {
            throw new BusinessException("permissionCode、permissionName 不能为空");
        }
        String normalizedPermissionCode = StringUtils.upperCase(StringUtils.trim(request.getPermissionCode()));
        if (!StringUtils.startsWith(normalizedPermissionCode, appCode + "_")) {
            throw new BusinessException("权限编码必须以当前 app 编码开头");
        }
        String permissionType = normalizePermissionType(request.getPermissionType(), current == null ? "API" : current.getPermissionType());
        IamPermission permission = new IamPermission();
        permission.setAppId(resolveAppId(appCode));
        permission.setAppCode(appCode);
        permission.setPermissionCode(normalizedPermissionCode);
        permission.setPermissionName(StringUtils.trim(request.getPermissionName()));
        permission.setPermissionType(permissionType);
        permission.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        permission.setPermissionPath(StringUtils.trimToNull(request.getPermissionPath()));
        permission.setHttpMethod(StringUtils.upperCase(StringUtils.trimToNull(request.getHttpMethod())));
        permission.setComponent(StringUtils.trimToNull(request.getComponent()));
        permission.setStatus(normalizeFlag(request.getStatus(), current == null ? 1 : current.getStatus(), "status"));
        permission.setSortNo(request.getSortNo() == null ? (current == null ? 0 : current.getSortNo()) : request.getSortNo());
        return permission;
    }

    private IamPermission buildPermission(String appCode, IamPermissionUpdateRequest request, IamPermission current) {
        if (request == null || StringUtils.isBlank(request.getPermissionName())) {
            throw new BusinessException("permissionName 不能为空");
        }
        String permissionType = normalizePermissionType(request.getPermissionType(), current == null ? "API" : current.getPermissionType());
        IamPermission permission = new IamPermission();
        permission.setAppCode(appCode);
        permission.setPermissionCode(current == null ? null : current.getPermissionCode());
        permission.setPermissionName(StringUtils.trim(request.getPermissionName()));
        permission.setPermissionType(permissionType);
        permission.setParentId(request.getParentId() == null ? (current == null ? 0L : current.getParentId()) : request.getParentId());
        permission.setPermissionPath(StringUtils.trimToNull(request.getPermissionPath()) == null ? (current == null ? null : current.getPermissionPath()) : StringUtils.trimToNull(request.getPermissionPath()));
        permission.setHttpMethod(StringUtils.upperCase(StringUtils.trimToNull(request.getHttpMethod())) == null ? (current == null ? null : current.getHttpMethod()) : StringUtils.upperCase(StringUtils.trimToNull(request.getHttpMethod())));
        permission.setComponent(StringUtils.trimToNull(request.getComponent()) == null ? (current == null ? null : current.getComponent()) : StringUtils.trimToNull(request.getComponent()));
        permission.setStatus(normalizeFlag(request.getStatus(), current == null ? 1 : current.getStatus(), "status"));
        permission.setSortNo(request.getSortNo() == null ? (current == null ? 0 : current.getSortNo()) : request.getSortNo());
        return permission;
    }

    private void validateParent(String appCode, Long parentId, Long selfId) {
        if (parentId == null || parentId <= 0) {
            return;
        }
        if (selfId != null && parentId.equals(selfId)) {
            throw new BusinessException("上级权限不能是自己");
        }
        IamPermission parent = iamPermissionQueryService.getPermissionEntity(appCode, parentId);
        if (parent == null) {
            throw new BusinessException("上级权限不存在");
        }
    }

    private List<String> normalizePermissionCodes(List<String> permissionCodes) {
        return permissionCodes == null ? List.of() : permissionCodes.stream()
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::trim)
            .map(StringUtils::upperCase)
            .distinct()
            .toList();
    }

    private String normalizePermissionType(String permissionType, String defaultValue) {
        String normalized = StringUtils.defaultIfBlank(permissionType, defaultValue);
        normalized = StringUtils.upperCase(StringUtils.trim(normalized));
        if (!ALLOWED_PERMISSION_TYPES.contains(normalized)) {
            throw new BusinessException("permissionType 只能是 MENU、BUTTON、API、DATA");
        }
        return normalized;
    }

    private Integer normalizeFlag(Integer value, Integer defaultValue, String fieldName) {
        Integer normalizedValue = value == null ? defaultValue : value;
        if (!Integer.valueOf(0).equals(normalizedValue) && !Integer.valueOf(1).equals(normalizedValue)) {
            throw new BusinessException(fieldName + " 只能是 0 或 1");
        }
        return normalizedValue;
    }

    private void ensureManagePermissionAdmin(LoginUser loginUser) {
        iamPermissionQueryService.ensureWsgmPermissionAdmin(loginUser);
    }

    private void ensureDepartmentRoleExists(Long appId, Long departmentId, Long roleId) {
        Long count = iamDepartmentRoleOptionMapper.selectCount(new LambdaQueryWrapper<IamDepartmentRoleOption>()
            .eq(IamDepartmentRoleOption::getAppId, appId)
            .eq(IamDepartmentRoleOption::getDepartmentId, departmentId)
            .eq(IamDepartmentRoleOption::getRoleId, roleId)
            .eq(IamDepartmentRoleOption::getDeleted, 0));
        if (count == null || count <= 0) {
            throw new BusinessException("该角色不属于当前部门，不能分配权限");
        }
    }

    private Long resolveAppId(String appCode) {
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appCode);
        return app.getId();
    }
}

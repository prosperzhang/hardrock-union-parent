package com.hardrockunion.platform.iam.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamDepartment;
import com.hardrockunion.platform.iam.domain.entity.IamDepartmentRoleOption;
import com.hardrockunion.platform.iam.domain.entity.IamDepartmentRolePermission;
import com.hardrockunion.platform.iam.domain.entity.IamPermission;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.dto.IamPermissionMatrixResponse;
import com.hardrockunion.platform.iam.dto.IamPermissionResponse;
import com.hardrockunion.platform.iam.mapper.IamDepartmentMapper;
import com.hardrockunion.platform.iam.mapper.IamDepartmentRoleOptionMapper;
import com.hardrockunion.platform.iam.mapper.IamDepartmentRolePermissionMapper;
import com.hardrockunion.platform.iam.mapper.IamPermissionMapper;
import com.hardrockunion.platform.iam.mapper.IamRoleMapper;

@Service
public class IamPermissionMatrixQueryService {

    private static final String MENU = "MENU";

    private final AppRegistryQueryService appRegistryQueryService;
    private final IamPermissionQueryService iamPermissionQueryService;
    private final IamDepartmentMapper iamDepartmentMapper;
    private final IamDepartmentRoleOptionMapper iamDepartmentRoleOptionMapper;
    private final IamDepartmentRolePermissionMapper iamDepartmentRolePermissionMapper;
    private final IamPermissionMapper iamPermissionMapper;
    private final IamRoleMapper iamRoleMapper;

    public IamPermissionMatrixQueryService(AppRegistryQueryService appRegistryQueryService,
                                           IamPermissionQueryService iamPermissionQueryService,
                                           IamDepartmentMapper iamDepartmentMapper,
                                           IamDepartmentRoleOptionMapper iamDepartmentRoleOptionMapper,
                                           IamDepartmentRolePermissionMapper iamDepartmentRolePermissionMapper,
                                           IamPermissionMapper iamPermissionMapper,
                                           IamRoleMapper iamRoleMapper) {
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamPermissionQueryService = iamPermissionQueryService;
        this.iamDepartmentMapper = iamDepartmentMapper;
        this.iamDepartmentRoleOptionMapper = iamDepartmentRoleOptionMapper;
        this.iamDepartmentRolePermissionMapper = iamDepartmentRolePermissionMapper;
        this.iamPermissionMapper = iamPermissionMapper;
        this.iamRoleMapper = iamRoleMapper;
    }

    public IamPermissionMatrixResponse getPermissionMatrix(String appCode, LoginUser loginUser) {
        iamPermissionQueryService.ensureWsgmPermissionAdmin(loginUser);
        AppRegistry app = appRegistryQueryService.getEnabledAppByCode(appRegistryQueryService.normalizeAppCode(appCode));

        IamPermissionMatrixResponse response = new IamPermissionMatrixResponse();
        response.setAppCode(app.getAppCode());
        response.setPermissionTree(iamPermissionQueryService.listMenuTree(app.getId()));
        response.setDepartments(buildDepartmentTree(app));
        return response;
    }

    private List<IamPermissionMatrixResponse.DepartmentNode> buildDepartmentTree(AppRegistry app) {
        List<IamPermissionMatrixResponse.DepartmentNode> flatDepartments = iamDepartmentMapper.selectList(new LambdaQueryWrapper<IamDepartment>()
                .eq(IamDepartment::getAppId, app.getId())
                .eq(IamDepartment::getDeleted, 0)
                .orderByAsc(IamDepartment::getSortNo)
                .orderByAsc(IamDepartment::getId))
            .stream()
            .map(department -> toDepartmentNode(app, department))
            .toList();
        Map<Long, IamPermissionMatrixResponse.DepartmentNode> byId = new LinkedHashMap<>();
        for (IamPermissionMatrixResponse.DepartmentNode department : flatDepartments) {
            department.setChildren(new ArrayList<>());
            byId.put(department.getDepartmentId(), department);
        }
        List<IamPermissionMatrixResponse.DepartmentNode> roots = new ArrayList<>();
        for (IamPermissionMatrixResponse.DepartmentNode department : flatDepartments) {
            Long parentId = department.getParentId();
            if (parentId == null || parentId <= 0 || !byId.containsKey(parentId)) {
                roots.add(department);
                continue;
            }
            byId.get(parentId).getChildren().add(department);
        }
        sortDepartments(roots);
        return roots;
    }

    private IamPermissionMatrixResponse.DepartmentNode toDepartmentNode(AppRegistry app, IamDepartment department) {
        IamPermissionMatrixResponse.DepartmentNode node = new IamPermissionMatrixResponse.DepartmentNode();
        node.setDepartmentId(department.getId());
        node.setDeptCode(department.getDeptCode());
        node.setDeptName(department.getDeptName());
        node.setParentId(department.getParentId());
        node.setDeptType(department.getDeptType());
        node.setStatus(department.getStatus());
        node.setSortNo(department.getSortNo());
        node.setRoles(listRolePermissionNodes(app, department));
        return node;
    }

    private List<IamPermissionMatrixResponse.RolePermissionNode> listRolePermissionNodes(AppRegistry app, IamDepartment department) {
        List<Long> roleIds = iamDepartmentRoleOptionMapper.selectList(new LambdaQueryWrapper<IamDepartmentRoleOption>()
                .eq(IamDepartmentRoleOption::getAppId, app.getId())
                .eq(IamDepartmentRoleOption::getDepartmentId, department.getId())
                .eq(IamDepartmentRoleOption::getDeleted, 0))
            .stream()
            .map(IamDepartmentRoleOption::getRoleId)
            .distinct()
            .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return iamRoleMapper.selectList(new LambdaQueryWrapper<IamRole>()
                .in(IamRole::getId, roleIds)
                .eq(IamRole::getAppId, app.getId())
                .eq(IamRole::getDeleted, 0)
                .orderByAsc(IamRole::getId))
            .stream()
            .map(role -> toRolePermissionNode(app, department, role))
            .toList();
    }

    private IamPermissionMatrixResponse.RolePermissionNode toRolePermissionNode(AppRegistry app,
                                                                                IamDepartment department,
                                                                                IamRole role) {
        List<IamPermission> permissions = listRolePermissions(app, department, role);
        IamPermissionMatrixResponse.RolePermissionNode node = new IamPermissionMatrixResponse.RolePermissionNode();
        node.setRoleId(role.getId());
        node.setRoleCode(role.getRoleCode());
        node.setRoleName(role.getRoleName());
        node.setStatus(role.getStatus());
        node.setAssignable(role.getAssignable());
        node.setAdminRole(role.getAdminRole());
        node.setPermissionCodes(permissions.stream().map(IamPermission::getPermissionCode).distinct().toList());
        node.setPermissionIds(permissions.stream().map(IamPermission::getId).distinct().toList());
        return node;
    }

    private List<IamPermission> listRolePermissions(AppRegistry app, IamDepartment department, IamRole role) {
        List<Long> permissionIds = iamDepartmentRolePermissionMapper.selectList(new LambdaQueryWrapper<IamDepartmentRolePermission>()
                .eq(IamDepartmentRolePermission::getAppId, app.getId())
                .eq(IamDepartmentRolePermission::getDepartmentId, department.getId())
                .eq(IamDepartmentRolePermission::getRoleId, role.getId())
                .eq(IamDepartmentRolePermission::getDeleted, 0))
            .stream()
            .map(IamDepartmentRolePermission::getPermissionId)
            .distinct()
            .toList();
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return iamPermissionMapper.selectList(new LambdaQueryWrapper<IamPermission>()
            .eq(IamPermission::getAppId, app.getId())
            .in(IamPermission::getId, permissionIds)
            .eq(IamPermission::getPermissionType, MENU)
            .eq(IamPermission::getDeleted, 0)
            .eq(IamPermission::getStatus, 1)
            .orderByAsc(IamPermission::getSortNo)
            .orderByAsc(IamPermission::getId));
    }

    private void sortDepartments(List<IamPermissionMatrixResponse.DepartmentNode> nodes) {
        nodes.sort(Comparator.comparing(IamPermissionMatrixResponse.DepartmentNode::getSortNo, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(IamPermissionMatrixResponse.DepartmentNode::getDepartmentId, Comparator.nullsLast(Long::compareTo)));
        for (IamPermissionMatrixResponse.DepartmentNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortDepartments(node.getChildren());
            }
        }
    }
}

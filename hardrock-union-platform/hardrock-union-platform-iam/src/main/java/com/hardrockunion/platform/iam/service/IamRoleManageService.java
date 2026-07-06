package com.hardrockunion.platform.iam.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.dto.IamRoleCreateRequest;
import com.hardrockunion.platform.iam.dto.IamRoleResponse;
import com.hardrockunion.platform.iam.dto.IamRoleUpdateRequest;
import com.hardrockunion.platform.iam.mapper.IamRoleMapper;

/**
 * app 级共享角色管理服务。
 */
@Service
public class IamRoleManageService {

    private static final String WSGM_APP_CODE = "WSGM";
    private static final String WSGM_SUPER_ADMIN_ROLE_CODE = "WSGM_SUPER_ADMIN";

    private final IamRoleMapper iamRoleMapper;
    private final IamRoleQueryService iamRoleQueryService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamTenantDepartmentRoleService iamTenantDepartmentRoleService;

    public IamRoleManageService(IamRoleMapper iamRoleMapper,
                                IamRoleQueryService iamRoleQueryService,
                                AppRegistryQueryService appRegistryQueryService,
                                IamTenantDepartmentRoleService iamTenantDepartmentRoleService) {
        this.iamRoleMapper = iamRoleMapper;
        this.iamRoleQueryService = iamRoleQueryService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamTenantDepartmentRoleService = iamTenantDepartmentRoleService;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamRoleResponse createRole(String appCode, IamRoleCreateRequest request, LoginUser loginUser) {
        iamRoleQueryService.ensureWsgmRoleAdmin(loginUser);
        if (request == null || StringUtils.isAnyBlank(request.getRoleCode(), request.getRoleName())) {
            throw new BusinessException("roleCode、roleName 不能为空");
        }

        String normalizedAppCode = iamRoleQueryService.normalizeAppCode(appCode);
        Long appId = resolveAppId(normalizedAppCode);
        String normalizedRoleCode = StringUtils.upperCase(StringUtils.trim(request.getRoleCode()));
        if (!StringUtils.startsWith(normalizedRoleCode, normalizedAppCode + "_")) {
            throw new BusinessException("角色编码必须以当前 app 编码开头");
        }

        IamRole existed = iamRoleMapper.selectOne(new LambdaQueryWrapper<IamRole>()
            .and(wrapper -> wrapper.eq(IamRole::getAppId, appId)
                .or()
                .eq(IamRole::getAppCode, normalizedAppCode))
            .eq(IamRole::getRoleCode, normalizedRoleCode)
            .eq(IamRole::getDeleted, 0)
            .last("limit 1"));
        if (existed != null) {
            throw new BusinessException("角色编码已存在");
        }

        IamRole role = new IamRole();
        role.setAppId(resolveAppId(normalizedAppCode));
        role.setAppCode(normalizedAppCode);
        role.setRoleCode(normalizedRoleCode);
        role.setRoleName(StringUtils.trim(request.getRoleName()));
        role.setStatus(normalizeFlag(request.getStatus(), 1, "status"));
        role.setAssignable(normalizeFlag(request.getAssignable(), 1, "assignable"));
        role.setAdminRole(normalizeFlag(request.getAdminRole(), 0, "adminRole"));
        role.setDeleted(0);
        iamRoleMapper.insert(role);
        return iamRoleQueryService.getRoleDetail(appCode, role.getId(), loginUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamRoleResponse updateRole(String appCode, Long roleId, IamRoleUpdateRequest request, LoginUser loginUser) {
        iamRoleQueryService.ensureWsgmRoleAdmin(loginUser);
        if (request == null || StringUtils.isBlank(request.getRoleName())) {
            throw new BusinessException("roleName 不能为空");
        }

        IamRole role = iamRoleQueryService.getRoleEntity(appCode, roleId);
        role.setRoleName(StringUtils.trim(request.getRoleName()));
        Integer status = normalizeFlag(request.getStatus(), role.getStatus(), "status");
        Integer assignable = normalizeFlag(request.getAssignable(), role.getAssignable(), "assignable");
        Integer adminRole = normalizeFlag(request.getAdminRole(), role.getAdminRole(), "adminRole");
        if (isWsgmSuperAdmin(role)
            && (!Integer.valueOf(1).equals(status)
                || !Integer.valueOf(1).equals(assignable)
                || !Integer.valueOf(1).equals(adminRole))) {
            throw new BusinessException("WSGM_SUPER_ADMIN 是系统唯一超管角色，不能停用或改为不可分配");
        }
        role.setStatus(status);
        role.setAssignable(assignable);
        role.setAdminRole(adminRole);
        iamRoleMapper.updateById(role);
        return iamRoleQueryService.getRoleDetail(appCode, roleId, loginUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String appCode, Long roleId, LoginUser loginUser) {
        iamRoleQueryService.ensureWsgmRoleAdmin(loginUser);
        IamRole role = iamRoleQueryService.getRoleEntity(appCode, roleId);
        if (isWsgmSuperAdmin(role)) {
            throw new BusinessException("WSGM_SUPER_ADMIN 是系统唯一超管角色，不能删除");
        }
        Long activeBindingCount = iamTenantDepartmentRoleService.countActiveBindingsByRole(role.getAppCode(), role.getId());
        if (activeBindingCount != null && activeBindingCount > 0) {
            throw new BusinessException("该角色已分配给用户，不能删除");
        }
        iamRoleMapper.deleteById(role.getId());
    }

    private Integer normalizeFlag(Integer value, Integer defaultValue, String fieldName) {
        Integer normalizedValue = value == null ? defaultValue : value;
        if (!Integer.valueOf(0).equals(normalizedValue) && !Integer.valueOf(1).equals(normalizedValue)) {
            throw new BusinessException(fieldName + " 只能是 0 或 1");
        }
        return normalizedValue;
    }

    private Long resolveAppId(String appCode) {
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        return app.getId();
    }

    private boolean isWsgmSuperAdmin(IamRole role) {
        return role != null
            && StringUtils.equalsIgnoreCase(WSGM_APP_CODE, role.getAppCode())
            && StringUtils.equalsIgnoreCase(WSGM_SUPER_ADMIN_ROLE_CODE, role.getRoleCode());
    }
}

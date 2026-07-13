package com.hardrockunion.platform.iam.service;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamDepartment;
import com.hardrockunion.platform.iam.domain.entity.IamDepartmentRoleOption;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.domain.entity.IamUser;
import com.hardrockunion.platform.iam.dto.IamDepartmentCreateRequest;
import com.hardrockunion.platform.iam.dto.IamDepartmentResponse;
import com.hardrockunion.platform.iam.dto.IamDepartmentRoleAssignRequest;
import com.hardrockunion.platform.iam.dto.IamDepartmentUpdateRequest;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentAssignRequest;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentSwitchRequest;
import com.hardrockunion.platform.iam.mapper.IamDepartmentMapper;
import com.hardrockunion.platform.iam.mapper.IamDepartmentRoleOptionMapper;
import com.hardrockunion.platform.iam.mapper.IamUserMapper;

@Service
public class IamDepartmentManageService {

    private static final String WSGM_APP_CODE = "WSGM";
    private static final String WSGM_ROOT_DEPT_CODE = "WSGM_ROOT";
    private static final String WSGM_SUPER_ADMIN_ROLE_CODE = "WSGM_SUPER_ADMIN";
    private static final Set<String> WORKSPACE_SCOPES = Set.of("ALL", "GROUP", "COMPANY", "ORGANIZATION", "PROJECT");

    private final IamDepartmentMapper iamDepartmentMapper;
    private final IamDepartmentRoleOptionMapper iamDepartmentRoleOptionMapper;
    private final IamTenantDepartmentRoleService iamTenantDepartmentRoleService;
    private final IamUserMapper iamUserMapper;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamRoleQueryService iamRoleQueryService;
    private final IamDepartmentQueryService iamDepartmentQueryService;
    private final IamTenantMemberService iamTenantMemberService;
    private final JdbcTemplate jdbcTemplate;

    public IamDepartmentManageService(IamDepartmentMapper iamDepartmentMapper,
                                      IamDepartmentRoleOptionMapper iamDepartmentRoleOptionMapper,
                                      IamTenantDepartmentRoleService iamTenantDepartmentRoleService,
                                      IamUserMapper iamUserMapper,
                                      AppRegistryQueryService appRegistryQueryService,
                                      IamRoleQueryService iamRoleQueryService,
                                      IamDepartmentQueryService iamDepartmentQueryService,
                                      IamTenantMemberService iamTenantMemberService,
                                      JdbcTemplate jdbcTemplate) {
        this.iamDepartmentMapper = iamDepartmentMapper;
        this.iamDepartmentRoleOptionMapper = iamDepartmentRoleOptionMapper;
        this.iamTenantDepartmentRoleService = iamTenantDepartmentRoleService;
        this.iamUserMapper = iamUserMapper;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamRoleQueryService = iamRoleQueryService;
        this.iamDepartmentQueryService = iamDepartmentQueryService;
        this.iamTenantMemberService = iamTenantMemberService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDepartmentResponse createDepartment(String appCode, IamDepartmentCreateRequest request, LoginUser loginUser) {
        ensureAdmin(appCode, loginUser);
        if (request == null || StringUtils.isAnyBlank(request.getDeptCode(), request.getDeptName())) {
            throw new BusinessException("deptCode、deptName 不能为空");
        }
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        String normalizedAppCode = iamRoleQueryService.normalizeAppCode(appCode);
        String normalizedDeptCode = StringUtils.upperCase(StringUtils.trim(request.getDeptCode()));
        IamDepartment existed = iamDepartmentMapper.selectOne(new LambdaQueryWrapper<IamDepartment>()
            .eq(IamDepartment::getAppId, app.getId())
            .eq(IamDepartment::getDeptCode, normalizedDeptCode)
            .eq(IamDepartment::getDeleted, 0)
            .last("limit 1"));
        if (existed != null) {
            throw new BusinessException("部门编码已存在");
        }
        if (request.getParentId() != null && request.getParentId() > 0) {
            IamDepartment parent = iamDepartmentQueryService.getDepartmentById(request.getParentId(), app.getId());
            if (parent == null) {
                throw new BusinessException("上级部门不存在");
            }
        }
        IamDepartment department = new IamDepartment();
        department.setAppId(app.getId());
        department.setAppCode(normalizedAppCode);
        department.setDeptCode(normalizedDeptCode);
        department.setDeptName(StringUtils.trim(request.getDeptName()));
        department.setDeptShortName(StringUtils.trimToNull(request.getDeptShortName()));
        department.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        department.setDeptType(StringUtils.defaultIfBlank(request.getDeptType(), "GENERAL"));
        department.setWorkspaceScope(normalizeWorkspaceScope(request.getWorkspaceScope(), "ALL"));
        department.setStatus(normalizeFlag(request.getStatus(), 1, "status"));
        department.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        department.setDeleted(0);
        iamDepartmentMapper.insert(department);
        return iamDepartmentQueryService.getDepartmentDetail(appCode, department.getId(), loginUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDepartmentResponse updateDepartment(String appCode, Long departmentId, IamDepartmentUpdateRequest request, LoginUser loginUser) {
        ensureAdmin(appCode, loginUser);
        IamDepartment department = iamDepartmentQueryService.getDepartmentEntity(resolveAppId(appCode), departmentId);
        if (request == null || StringUtils.isBlank(request.getDeptName())) {
            throw new BusinessException("deptName 不能为空");
        }
        if (request.getParentId() != null && request.getParentId() > 0) {
            IamDepartment parent = iamDepartmentQueryService.getDepartmentById(request.getParentId(), department.getAppId());
            if (parent == null) {
                throw new BusinessException("上级部门不存在");
            }
        }
        department.setDeptName(StringUtils.trim(request.getDeptName()));
        department.setDeptShortName(StringUtils.trimToNull(request.getDeptShortName()));
        department.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        department.setDeptType(StringUtils.defaultIfBlank(request.getDeptType(), department.getDeptType()));
        department.setWorkspaceScope(normalizeWorkspaceScope(request.getWorkspaceScope(), department.getWorkspaceScope()));
        department.setStatus(normalizeFlag(request.getStatus(), department.getStatus(), "status"));
        department.setSortNo(request.getSortNo() == null ? department.getSortNo() : request.getSortNo());
        iamDepartmentMapper.updateById(department);
        return iamDepartmentQueryService.getDepartmentDetail(appCode, departmentId, loginUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(String appCode, Long departmentId, LoginUser loginUser) {
        ensureAdmin(appCode, loginUser);
        IamDepartment department = iamDepartmentQueryService.getDepartmentEntity(resolveAppId(appCode), departmentId);
        ensureCoreDepartmentCanBeDeleted(department);
        long activeMemberCount = iamTenantDepartmentRoleService.countActiveBindingsByDepartment(department.getAppCode(), department.getId());
        if (activeMemberCount > 0) {
            throw new BusinessException("部门下还有成员，不能删除");
        }
        department.setDeleted(1);
        iamDepartmentMapper.updateById(department);
        iamDepartmentRoleOptionMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<IamDepartmentRoleOption>()
            .eq(IamDepartmentRoleOption::getDepartmentId, department.getId())
            .eq(IamDepartmentRoleOption::getAppId, department.getAppId())
            .eq(IamDepartmentRoleOption::getDeleted, 0)
            .set(IamDepartmentRoleOption::getDeleted, 1));
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDepartmentResponse assignDepartmentRoles(String appCode,
                                                      Long departmentId,
                                                      IamDepartmentRoleAssignRequest request,
                                                      LoginUser loginUser) {
        ensureAdmin(appCode, loginUser);
        IamDepartment department = iamDepartmentQueryService.getDepartmentEntity(resolveAppId(appCode), departmentId);
        List<String> roleCodes = request == null ? List.of() : request.getRoleCodes();
        List<IamRole> roles = iamRoleQueryService.listEnabledRolesByCodes(department.getAppId(), roleCodes);
        long targetRoleCount = roleCodes == null ? 0 : roleCodes.stream()
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::trim)
            .map(StringUtils::upperCase)
            .distinct()
            .count();
        if (roles.size() != targetRoleCount) {
            throw new BusinessException("角色不存在或已停用");
        }

        List<IamDepartmentRoleOption> existingRelations = iamDepartmentRoleOptionMapper.selectList(new LambdaQueryWrapper<IamDepartmentRoleOption>()
            .eq(IamDepartmentRoleOption::getAppId, department.getAppId())
            .eq(IamDepartmentRoleOption::getDepartmentId, department.getId()));
        List<Long> targetRoleIds = roles.stream().map(IamRole::getId).toList();
        ensureCoreDepartmentRolesCanBeUpdated(department, roles);
        for (IamDepartmentRoleOption existingRelation : existingRelations) {
            if (!targetRoleIds.contains(existingRelation.getRoleId()) && Integer.valueOf(0).equals(existingRelation.getDeleted())) {
                iamDepartmentRoleOptionMapper.deleteById(existingRelation.getId());
            }
        }
        for (IamRole role : roles) {
            upsertDepartmentRoleOption(department, role);
        }
        return iamDepartmentQueryService.getDepartmentDetail(appCode, departmentId, loginUser);
    }

    private void upsertDepartmentRoleOption(IamDepartment department, IamRole role) {
        jdbcTemplate.update("""
            INSERT INTO iam_department_role_option (app_id, app_code, department_id, role_id, deleted)
            VALUES (?, ?, ?, ?, 0)
            ON DUPLICATE KEY UPDATE app_code = VALUES(app_code), deleted = 0, updated_at = CURRENT_TIMESTAMP
            """, department.getAppId(), department.getAppCode(), department.getId(), role.getId());
    }

    private void ensureCoreDepartmentCanBeDeleted(IamDepartment department) {
        if (isWsgmRootDepartment(department)) {
            throw new BusinessException("WSGM_ROOT 归属 WSGM_SUPER_ADMIN，不能删除");
        }
    }

    private void ensureCoreDepartmentRolesCanBeUpdated(IamDepartment department, List<IamRole> roles) {
        if (!isWsgmRootDepartment(department)) {
            return;
        }
        boolean hasSuperAdmin = roles.stream()
            .anyMatch(role -> StringUtils.equalsIgnoreCase(role.getRoleCode(), WSGM_SUPER_ADMIN_ROLE_CODE));
        if (!hasSuperAdmin) {
            throw new BusinessException("WSGM_ROOT 必须保留 WSGM_SUPER_ADMIN");
        }
    }

    private boolean isWsgmRootDepartment(IamDepartment department) {
        return department != null
            && StringUtils.equalsIgnoreCase(department.getAppCode(), WSGM_APP_CODE)
            && StringUtils.equalsIgnoreCase(department.getDeptCode(), WSGM_ROOT_DEPT_CODE);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDepartmentResponse assignUserDepartment(String appCode,
                                                     Long userId,
                                                     IamUserDepartmentAssignRequest request,
                                                     LoginUser loginUser) {
        return setPrimaryDepartment(appCode, loginUser == null ? null : loginUser.getTenantId(), userId, request == null ? null : request.getDepartmentId(), loginUser, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDepartmentResponse assignUserDepartment(String appCode,
                                                     Long tenantId,
                                                     Long userId,
                                                     IamUserDepartmentAssignRequest request,
                                                     LoginUser loginUser) {
        return setPrimaryDepartment(appCode, tenantId, userId, request == null ? null : request.getDepartmentId(), loginUser, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public IamDepartmentResponse switchUserDepartment(String appCode,
                                                      Long userId,
                                                      IamUserDepartmentSwitchRequest request,
                                                      LoginUser loginUser) {
        return setPrimaryDepartment(appCode, loginUser == null ? null : loginUser.getTenantId(), userId, request == null ? null : request.getDepartmentId(), loginUser, false);
    }

    private void ensureAdmin(String appCode, LoginUser loginUser) {
        iamRoleQueryService.ensureWsgmRoleAdmin(loginUser);
    }

    private Integer normalizeFlag(Integer value, Integer defaultValue, String fieldName) {
        Integer normalizedValue = value == null ? defaultValue : value;
        if (!Integer.valueOf(0).equals(normalizedValue) && !Integer.valueOf(1).equals(normalizedValue)) {
            throw new BusinessException(fieldName + " 只能是 0 或 1");
        }
        return normalizedValue;
    }

    private IamDepartmentResponse setPrimaryDepartment(String appCode,
                                                       Long tenantId,
                                                       Long userId,
                                                       Long departmentId,
                                                       LoginUser loginUser,
                                                       boolean allowCreateRelation) {
        if (userId == null) {
            throw new BusinessException("userId 不能为空");
        }
        if (departmentId == null) {
            throw new BusinessException("departmentId 不能为空");
        }
        ensureSelfOrAdmin(appCode, userId, loginUser);
        IamDepartment department = iamDepartmentQueryService.getDepartmentEntity(resolveAppId(appCode), departmentId);
        iamDepartmentQueryService.ensureDepartmentAssignableToTenant(appCode, tenantId, department);
        IamUser user = iamUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户不存在");
        }
        if (!StringUtils.equalsIgnoreCase(loginUser.getAppCode(), user.getAppCode())
            || iamTenantMemberService.getActiveMember(department.getAppId(), tenantId, user.getId()) == null) {
            throw new BusinessException("用户不属于当前 app 或当前租户");
        }

        iamTenantDepartmentRoleService.setPrimaryDepartment(department.getAppCode(), tenantId, user.getId(), department.getId(), allowCreateRelation);

        return iamDepartmentQueryService.getDepartmentDetail(appCode, department.getId(), loginUser);
    }

    private String normalizeWorkspaceScope(String value, String defaultValue) {
        String scope = StringUtils.upperCase(StringUtils.defaultIfBlank(value, defaultValue));
        if (!WORKSPACE_SCOPES.contains(scope)) {
            throw new BusinessException("workspaceScope 仅支持 ALL、GROUP、COMPANY、ORGANIZATION、PROJECT");
        }
        return scope;
    }

    private void ensureSelfOrAdmin(String appCode, Long userId, LoginUser loginUser) {
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        if (loginUser.getUserId().equals(userId)) {
            return;
        }
        iamRoleQueryService.ensureAdmin(appCode, loginUser);
    }

    private Long resolveAppId(String appCode) {
        return appRegistryQueryService.getAppByCode(iamRoleQueryService.normalizeAppCode(appCode)).getId();
    }
}

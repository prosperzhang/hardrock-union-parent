package com.hardrockunion.platform.iam.service;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.AppRegistry;
import com.hardrockunion.platform.iam.domain.entity.IamDepartment;
import com.hardrockunion.platform.iam.domain.entity.IamDepartmentRoleOption;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.domain.entity.IamUser;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;
import com.hardrockunion.platform.iam.domain.entity.IamUserInfo;
import com.hardrockunion.platform.iam.domain.model.IamTenantDepartmentRoleBinding;
import com.hardrockunion.platform.iam.dto.IamDepartmentResponse;
import com.hardrockunion.platform.iam.dto.IamRoleResponse;
import com.hardrockunion.platform.iam.dto.IamUserDepartmentResponse;
import com.hardrockunion.platform.iam.dto.IamUserResponse;
import com.hardrockunion.platform.iam.mapper.IamDepartmentMapper;
import com.hardrockunion.platform.iam.mapper.IamDepartmentRoleOptionMapper;
import com.hardrockunion.platform.iam.mapper.IamRoleMapper;
import com.hardrockunion.platform.iam.mapper.IamUserMapper;

@Service
public class IamDepartmentQueryService {

    private static final String NEXIS_APP_CODE = "NEXIS";
    private static final String SCOPE_ALL = "ALL";
    private static final String SCOPE_ORGANIZATION = "ORGANIZATION";

    private final IamDepartmentMapper iamDepartmentMapper;
    private final IamDepartmentRoleOptionMapper iamDepartmentRoleOptionMapper;
    private final IamTenantDepartmentRoleService iamTenantDepartmentRoleService;
    private final IamUserMapper iamUserMapper;
    private final IamRoleMapper iamRoleMapper;
    private final IamRoleQueryService iamRoleQueryService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final IamTenantMemberService iamTenantMemberService;
    private final IamUserInfoService iamUserInfoService;
    private final JdbcTemplate jdbcTemplate;

    public IamDepartmentQueryService(IamDepartmentMapper iamDepartmentMapper,
                                     IamDepartmentRoleOptionMapper iamDepartmentRoleOptionMapper,
                                     IamTenantDepartmentRoleService iamTenantDepartmentRoleService,
                                     IamUserMapper iamUserMapper,
                                     IamRoleMapper iamRoleMapper,
                                     IamRoleQueryService iamRoleQueryService,
                                     AppRegistryQueryService appRegistryQueryService,
                                     IamTenantMemberService iamTenantMemberService,
                                     IamUserInfoService iamUserInfoService,
                                     JdbcTemplate jdbcTemplate) {
        this.iamDepartmentMapper = iamDepartmentMapper;
        this.iamDepartmentRoleOptionMapper = iamDepartmentRoleOptionMapper;
        this.iamTenantDepartmentRoleService = iamTenantDepartmentRoleService;
        this.iamUserMapper = iamUserMapper;
        this.iamRoleMapper = iamRoleMapper;
        this.iamRoleQueryService = iamRoleQueryService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.iamTenantMemberService = iamTenantMemberService;
        this.iamUserInfoService = iamUserInfoService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<IamDepartmentResponse> listDepartments(String appCode, LoginUser loginUser) {
        ensureAppLoginOrWsgmAdmin(appCode, loginUser);
        String normalizedAppCode = normalizeAppCode(appCode);
        Long appId = resolveAppId(normalizedAppCode);
        List<IamDepartmentResponse> departments = listDepartments(appId);
        if (!StringUtils.equalsIgnoreCase(NEXIS_APP_CODE, normalizedAppCode)
            || !StringUtils.equalsIgnoreCase(normalizedAppCode, loginUser.getAppCode())) {
            return departments;
        }
        String tenantType = resolveTenantType(appId, loginUser.getTenantId());
        return departments.stream()
            .filter(department -> Integer.valueOf(1).equals(department.getStatus()))
            .filter(department -> isScopeApplicable(department.getWorkspaceScope(), tenantType))
            .toList();
    }

    public List<IamDepartmentResponse> listDepartments(Long appId) {
        if (appId == null) {
            return List.of();
        }
        return iamDepartmentMapper.selectList(new LambdaQueryWrapper<IamDepartment>()
                .eq(IamDepartment::getAppId, appId)
                .eq(IamDepartment::getDeleted, 0)
                .orderByAsc(IamDepartment::getSortNo)
                .orderByAsc(IamDepartment::getId))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public IamDepartmentResponse getDepartmentDetail(String appCode, Long departmentId, LoginUser loginUser) {
        ensureAppLoginOrWsgmAdmin(appCode, loginUser);
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return toResponse(getDepartmentEntity(appId, departmentId));
    }

    public IamDepartment getDepartmentEntity(String appCode, Long departmentId, LoginUser loginUser) {
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return getDepartmentEntity(appId, departmentId);
    }

    public IamDepartment getDepartmentEntity(Long appId, Long departmentId) {
        if (departmentId == null) {
            throw new BusinessException("部门ID不能为空");
        }
        IamDepartment department = iamDepartmentMapper.selectById(departmentId);
        if (department == null || Integer.valueOf(1).equals(department.getDeleted())) {
            throw new BusinessException("部门不存在");
        }
        if (department.getAppId() == null || !appId.equals(department.getAppId())) {
            throw new BusinessException("部门不属于当前 app");
        }
        return department;
    }

    public void ensureDepartmentAssignableToTenant(String appCode, Long tenantId, IamDepartment department) {
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        if (!StringUtils.equalsIgnoreCase(NEXIS_APP_CODE, appCode)) {
            return;
        }
        String tenantType = resolveTenantType(department.getAppId(), tenantId);
        if (!isScopeApplicable(department.getWorkspaceScope(), tenantType)) {
            throw new BusinessException("该部门不适用于当前" + tenantTypeLabel(tenantType) + "空间");
        }
    }

    public void ensureRolesAssignableToDepartment(Long departmentId, List<IamRole> roles) {
        List<Long> optionRoleIds = listRoleEntitiesByDepartment(
            departmentId,
            roles == null || roles.isEmpty() ? null : roles.getFirst().getAppId()
        ).stream().map(IamRole::getId).toList();
        if (roles == null || roles.isEmpty() || roles.stream().anyMatch(role -> !optionRoleIds.contains(role.getId()))) {
            throw new BusinessException("所选角色不属于目标部门");
        }
    }

    public List<IamRole> listRoleEntitiesByDepartment(Long departmentId, String appCode) {
        if (departmentId == null || StringUtils.isBlank(appCode)) {
            return List.of();
        }
        String normalizedAppCode = normalizeAppCode(appCode);
        Long appId = resolveAppId(normalizedAppCode);
        return listRoleEntitiesByDepartment(departmentId, appId);
    }

    public List<IamRole> listRoleEntitiesByDepartment(Long departmentId, Long appId) {
        if (departmentId == null || appId == null) {
            return List.of();
        }
        List<Long> roleIds = iamDepartmentRoleOptionMapper.selectList(new LambdaQueryWrapper<IamDepartmentRoleOption>()
                .eq(IamDepartmentRoleOption::getDepartmentId, departmentId)
                .eq(IamDepartmentRoleOption::getAppId, appId)
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
            .eq(IamRole::getDeleted, 0)
            .eq(IamRole::getStatus, 1)
            .orderByAsc(IamRole::getId));
    }

    public List<IamRoleResponse> listRolesByDepartment(Long departmentId, String appCode) {
        if (departmentId == null || StringUtils.isBlank(appCode)) {
            return List.of();
        }
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return listRoleEntitiesByDepartment(departmentId, appId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<IamUserResponse> listMembersByDepartment(String appCode, Long departmentId, LoginUser loginUser) {
        ensureAppLoginOrWsgmAdmin(appCode, loginUser);
        Long appId = resolveAppId(normalizeAppCode(appCode));
        IamDepartment department = getDepartmentEntity(appId, departmentId);
        return iamTenantDepartmentRoleService.listActiveBindingsByDepartment(department.getAppCode(), loginUser.getTenantId(), department.getId())
            .stream()
            .map(IamTenantDepartmentRoleBinding::getUserId)
            .distinct()
            .map(userId -> iamUserMapper.selectById(userId))
            .filter(user -> user != null && Integer.valueOf(0).equals(user.getDeleted()))
            .map(user -> toUserResponse(user, department, loginUser.getTenantId()))
            .toList();
    }

    public List<IamUserDepartmentResponse> listUserDepartments(Long userId, String appCode, LoginUser loginUser) {
        if (userId == null) {
            throw new BusinessException("userId 不能为空");
        }
        ensureAppLogin(appCode, loginUser);
        Long appId = resolveAppId(normalizeAppCode(appCode));
        Long tenantId = resolveTenantId(appId, userId, loginUser);
        boolean selfQuery = loginUser.getUserId().equals(userId);
        if (!selfQuery) {
            iamRoleQueryService.ensureAdmin(appCode, loginUser);
        }
        IamUser user = iamUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户不存在");
        }
        if (!StringUtils.equals(normalizeAppCode(appCode), normalizeAppCode(user.getAppCode()))
            || iamTenantMemberService.getActiveMember(appId, tenantId, user.getId()) == null) {
            throw new BusinessException("用户不属于当前 app 或当前租户");
        }
        return iamTenantDepartmentRoleService.listActiveBindings(normalizeAppCode(appCode), tenantId, userId)
            .stream()
            .filter(relation -> relation.getDepartmentId() != null && relation.getDepartmentId() > 0)
            .collect(java.util.stream.Collectors.toMap(
                IamTenantDepartmentRoleBinding::getDepartmentId,
                relation -> relation,
                (left, right) -> Integer.valueOf(1).equals(left.getPrimaryFlag()) ? left : right,
                java.util.LinkedHashMap::new
            ))
            .values()
            .stream()
            .map(relation -> toUserDepartmentResponse(user, relation))
            .filter(Objects::nonNull)
            .toList();
    }

    public IamDepartment getPrimaryDepartmentByUser(Long userId, String appCode, Long tenantId) {
        if (userId == null || StringUtils.isBlank(appCode) || tenantId == null) {
            return null;
        }
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return getPrimaryDepartmentByUser(userId, appId, tenantId);
    }

    public IamDepartment getPrimaryDepartmentByUser(Long userId, Long appId, Long tenantId) {
        if (userId == null || appId == null || tenantId == null) {
            return null;
        }
        AppRegistry app = appRegistryQueryService.getAppById(appId);
        Long departmentId = iamTenantDepartmentRoleService.getPrimaryDepartmentId(app.getAppCode(), tenantId, userId);
        if (departmentId == null) {
            return null;
        }
        return getDepartmentById(departmentId, appId);
    }

    public List<IamDepartment> listDepartmentsByUser(Long userId, String appCode, Long tenantId) {
        if (userId == null || StringUtils.isBlank(appCode) || tenantId == null) {
            return List.of();
        }
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return listDepartmentsByUser(userId, appId, tenantId);
    }

    public List<IamDepartment> listDepartmentsByUser(Long userId, Long appId, Long tenantId) {
        if (userId == null || appId == null || tenantId == null) {
            return List.of();
        }
        AppRegistry app = appRegistryQueryService.getAppById(appId);
        List<Long> departmentIds = iamTenantDepartmentRoleService.listActiveDepartmentIds(app.getAppCode(), tenantId, userId);
        if (departmentIds.isEmpty()) {
            return List.of();
        }
        return iamDepartmentMapper.selectList(new LambdaQueryWrapper<IamDepartment>()
            .in(IamDepartment::getId, departmentIds)
            .eq(IamDepartment::getAppId, appId)
            .eq(IamDepartment::getDeleted, 0)
            .eq(IamDepartment::getStatus, 1)
            .orderByAsc(IamDepartment::getSortNo)
            .orderByAsc(IamDepartment::getId));
    }

    public List<Long> listDepartmentIdsByUser(Long userId, String appCode, Long tenantId) {
        return listDepartmentsByUser(userId, appCode, tenantId).stream()
            .map(IamDepartment::getId)
            .toList();
    }

    public IamDepartment getDepartmentById(Long departmentId, String appCode) {
        Long appId = resolveAppId(normalizeAppCode(appCode));
        return getDepartmentById(departmentId, appId);
    }

    public IamDepartment getDepartmentById(Long departmentId, Long appId) {
        if (departmentId == null) {
            return null;
        }
        IamDepartment department = iamDepartmentMapper.selectById(departmentId);
        if (department == null || Integer.valueOf(1).equals(department.getDeleted())) {
            return null;
        }
        if (department.getAppId() == null || !appId.equals(department.getAppId())) {
            return null;
        }
        return department;
    }

    public IamDepartment getDepartmentByCode(Long appId, String deptCode) {
        if (appId == null || StringUtils.isBlank(deptCode)) {
            return null;
        }
        String normalizedDeptCode = StringUtils.upperCase(StringUtils.trim(deptCode));
        IamDepartment department = iamDepartmentMapper.selectOne(new LambdaQueryWrapper<IamDepartment>()
            .eq(IamDepartment::getAppId, appId)
            .eq(IamDepartment::getDeptCode, normalizedDeptCode)
            .eq(IamDepartment::getDeleted, 0)
            .last("limit 1"));
        if (department == null) {
            return null;
        }
        if (department.getStatus() == null || Integer.valueOf(0).equals(department.getStatus())) {
            return null;
        }
        return department;
    }

    private IamDepartmentResponse toResponse(IamDepartment department) {
        IamDepartmentResponse response = new IamDepartmentResponse();
        response.setId(department.getId());
        response.setAppId(department.getAppId());
        response.setAppCode(department.getAppCode());
        response.setDeptCode(department.getDeptCode());
        response.setDeptName(department.getDeptName());
        response.setDeptShortName(department.getDeptShortName());
        response.setParentId(department.getParentId());
        response.setDeptType(department.getDeptType());
        response.setWorkspaceScope(StringUtils.defaultIfBlank(department.getWorkspaceScope(), SCOPE_ALL));
        response.setStatus(department.getStatus());
        response.setSortNo(department.getSortNo());
        response.setRoleCodes(listRoleEntitiesByDepartment(department.getId(), department.getAppCode())
            .stream()
            .map(IamRole::getRoleCode)
            .toList());
        return response;
    }

    private String resolveTenantType(Long appId, Long tenantId) {
        if (tenantId == null) {
            throw new BusinessException("当前登录态缺少租户信息");
        }
        List<String> tenantTypes = jdbcTemplate.queryForList("""
            SELECT tenant_type FROM tenant_registry
            WHERE app_id = ? AND id = ? AND deleted = 0 AND status = 1
            LIMIT 1
            """, String.class, appId, tenantId);
        if (tenantTypes.isEmpty()) {
            throw new BusinessException("当前租户不存在或已停用");
        }
        return StringUtils.upperCase(StringUtils.trim(tenantTypes.getFirst()));
    }

    private boolean isScopeApplicable(String workspaceScope, String tenantType) {
        String scope = StringUtils.upperCase(StringUtils.defaultIfBlank(workspaceScope, SCOPE_ALL));
        if (StringUtils.equals(scope, SCOPE_ALL) || StringUtils.equals(scope, tenantType)) {
            return true;
        }
        return StringUtils.equals(scope, SCOPE_ORGANIZATION)
            && StringUtils.equalsAny(tenantType, "GROUP", "COMPANY");
    }

    private String tenantTypeLabel(String tenantType) {
        return switch (tenantType) {
            case "GROUP" -> "集团";
            case "COMPANY" -> "公司";
            case "PROJECT" -> "项目";
            default -> "当前";
        };
    }

    private void ensureAppLogin(String appCode, LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        String normalizedAppCode = normalizeAppCode(appCode);
        String normalizedLoginAppCode = normalizeAppCode(loginUser.getAppCode());
        if (!StringUtils.equals(normalizedAppCode, normalizedLoginAppCode)) {
            throw new BusinessException("当前登录态与访问入口不一致");
        }
    }

    private void ensureAppLoginOrWsgmAdmin(String appCode, LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException("未登录或登录已失效");
        }
        String normalizedAppCode = normalizeAppCode(appCode);
        String normalizedLoginAppCode = normalizeAppCode(loginUser.getAppCode());
        if (StringUtils.equals(normalizedAppCode, normalizedLoginAppCode)) {
            return;
        }
        iamRoleQueryService.ensureWsgmRoleAdmin(loginUser);
    }

    private String normalizeAppCode(String appCode) {
        return appRegistryQueryService.normalizeRegisteredAppCode(appCode);
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

    private IamUserResponse toUserResponse(IamUser user, IamDepartment department, Long tenantId) {
        IamUserResponse response = new IamUserResponse();
        response.setId(user.getId());
        response.setAppCode(user.getAppCode());
        response.setTenantId(tenantId);
        response.setDepartmentId(department.getId());
        response.setDepartmentCode(department.getDeptCode());
        response.setDepartmentName(department.getDeptName());
        response.setDepartmentShortName(department.getDeptShortName());
        response.setUsername(user.getUsername());
        IamUserInfo profile = iamUserInfoService.getActiveUserInfo(user.getAppId(), user.getId());
        response.setNickName(profile == null ? null : profile.getNickName());
        response.setAvatarUrl(profile == null ? null : profile.getAvatarUrl());
        response.setStatus(user.getStatus());
        response.setRoleCodes(iamRoleQueryService.listRoleEntitiesByUser(user.getId(), user.getAppCode(), tenantId)
            .stream()
            .map(IamRole::getRoleCode)
            .toList());
        return response;
    }

    private IamUserDepartmentResponse toUserDepartmentResponse(IamUser user, IamTenantDepartmentRoleBinding relation) {
        IamDepartment department = getDepartmentById(relation.getDepartmentId(), user.getAppCode());
        if (department == null) {
            return null;
        }
        IamUserDepartmentResponse response = new IamUserDepartmentResponse();
        response.setDepartmentId(department.getId());
        response.setAppCode(department.getAppCode());
        response.setTenantId(relation.getTenantId());
        response.setDeptCode(department.getDeptCode());
        response.setDeptName(department.getDeptName());
        response.setDeptShortName(department.getDeptShortName());
        response.setParentId(department.getParentId());
        response.setDeptType(department.getDeptType());
        response.setDepartmentStatus(department.getStatus());
        response.setSortNo(department.getSortNo());
        response.setPrimaryFlag(relation.getPrimaryFlag());
        response.setRoleCodes(listRoleEntitiesByDepartment(department.getId(), department.getAppCode())
            .stream()
            .map(IamRole::getRoleCode)
            .toList());
        return response;
    }

    private Long resolveAppId(String appCode) {
        AppRegistry app = appRegistryQueryService.getAppByCode(appCode);
        return app.getId();
    }

    private Long resolveTenantId(Long appId, Long userId, LoginUser loginUser) {
        if (loginUser != null && loginUser.getTenantId() != null) {
            return loginUser.getTenantId();
        }
        List<IamTenantMember> activeMembers = iamTenantMemberService.listActiveMembersByUser(appId, userId);
        if (activeMembers.isEmpty()) {
            return null;
        }
        return activeMembers.stream()
            .filter(member -> Integer.valueOf(1).equals(member.getIsPrimary()))
            .findFirst()
            .orElse(activeMembers.getFirst())
            .getTenantId();
    }
}

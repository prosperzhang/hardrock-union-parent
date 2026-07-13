package com.hardrockunion.platform.tenant.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.platform.iam.domain.entity.IamDepartment;
import com.hardrockunion.platform.iam.domain.entity.IamRole;
import com.hardrockunion.platform.iam.domain.entity.IamTenantMember;
import com.hardrockunion.platform.iam.domain.entity.IamUser;
import com.hardrockunion.platform.iam.domain.entity.IamUserInfo;
import com.hardrockunion.platform.iam.domain.model.IamTenantDepartmentRoleBinding;
import com.hardrockunion.platform.iam.mapper.IamRoleMapper;
import com.hardrockunion.platform.iam.mapper.IamUserMapper;
import com.hardrockunion.platform.iam.service.AppRegistryQueryService;
import com.hardrockunion.platform.iam.service.IamDepartmentQueryService;
import com.hardrockunion.platform.iam.service.IamRoleQueryService;
import com.hardrockunion.platform.iam.service.IamTenantDepartmentRoleService;
import com.hardrockunion.platform.iam.service.IamTenantMemberService;
import com.hardrockunion.platform.iam.service.IamUserInfoService;
import com.hardrockunion.platform.tenant.dto.TenantMemberAssignRequest;
import com.hardrockunion.platform.tenant.dto.TenantMemberDepartmentRoleRequest;
import com.hardrockunion.platform.tenant.dto.TenantMemberDepartmentRoleResponse;
import com.hardrockunion.platform.tenant.dto.TenantMemberResponse;

@Service
public class TenantMemberFlowService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String REMOVED_STATUS = "REMOVED";

    private final IamRoleMapper iamRoleMapper;
    private final IamUserMapper iamUserMapper;
    private final IamUserInfoService iamUserInfoService;
    private final IamRoleQueryService iamRoleQueryService;
    private final IamDepartmentQueryService iamDepartmentQueryService;
    private final IamTenantMemberService iamTenantMemberService;
    private final IamTenantDepartmentRoleService iamTenantDepartmentRoleService;
    private final AppRegistryQueryService appRegistryQueryService;
    private final TenantRegistryService tenantRegistryService;
    private final TenantFlowPolicy tenantFlowPolicy;

    public TenantMemberFlowService(IamRoleMapper iamRoleMapper,
                                   IamUserMapper iamUserMapper,
                                   IamUserInfoService iamUserInfoService,
                                   IamRoleQueryService iamRoleQueryService,
                                   IamDepartmentQueryService iamDepartmentQueryService,
                                   IamTenantMemberService iamTenantMemberService,
                                   IamTenantDepartmentRoleService iamTenantDepartmentRoleService,
                                   AppRegistryQueryService appRegistryQueryService,
                                   TenantRegistryService tenantRegistryService,
                                   TenantFlowPolicy tenantFlowPolicy) {
        this.iamRoleMapper = iamRoleMapper;
        this.iamUserMapper = iamUserMapper;
        this.iamUserInfoService = iamUserInfoService;
        this.iamRoleQueryService = iamRoleQueryService;
        this.iamDepartmentQueryService = iamDepartmentQueryService;
        this.iamTenantMemberService = iamTenantMemberService;
        this.iamTenantDepartmentRoleService = iamTenantDepartmentRoleService;
        this.appRegistryQueryService = appRegistryQueryService;
        this.tenantRegistryService = tenantRegistryService;
        this.tenantFlowPolicy = tenantFlowPolicy;
    }

    public void createTenantAdminMember(String appCode, Long tenantId, LoginUser loginUser) {
        TenantFlowPolicy.AppTenantPolicy policy = ensureSupportedApp(appCode);
        if (tenantId == null || loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException(policy.tenantLabel() + "管理员初始化参数不完整");
        }
        Long appId = resolveAppId(appCode);
        Long userId = loginUser.getUserId();
        iamTenantMemberService.upsertMember(appId, tenantId, userId, ACTIVE_STATUS, true, LocalDateTime.now(), null);
        TenantAdminIdentity identity = resolveTenantAdminIdentity(policy, tenantId);
        IamDepartment adminDepartment = loadDepartmentByCode(policy.appCode(), identity.departmentCode());
        Long roleId = loadRoleByCode(policy.appCode(), identity.roleCode()).getId();
        iamTenantDepartmentRoleService.replaceDepartmentRoles(
            appCode, tenantId, userId, adminDepartment.getId(), List.of(roleId));
    }

    public IamTenantMember findActiveMember(String appCode, Long tenantId, Long userId) {
        ensureSupportedApp(appCode);
        return iamTenantMemberService.getActiveMember(resolveAppId(appCode), tenantId, userId);
    }

    public List<TenantMemberResponse> listMembers(String appCode, Long tenantId, LoginUser loginUser) {
        ensureSupportedApp(appCode);
        ensureTenantRoleAdmin(appCode, tenantId, loginUser);
        return iamTenantMemberService.listMembersByTenant(resolveAppId(appCode), tenantId).stream()
            .map(member -> toResponse(appCode, member, tenantId))
            .toList();
    }

    public TenantMemberResponse activateMember(String appCode, Long tenantId, Long userId) {
        ensureSupportedApp(appCode);
        IamTenantMember member = iamTenantMemberService.upsertMember(resolveAppId(appCode), tenantId, userId, ACTIVE_STATUS, false, LocalDateTime.now(), null);
        return toResponse(appCode, member, tenantId);
    }

    public TenantMemberResponse assignDepartmentRoles(String appCode,
                                                      Long tenantId,
                                                      Long memberId,
                                                      TenantMemberAssignRequest request,
                                                      LoginUser loginUser) {
        ensureSupportedApp(appCode);
        ensureTenantRoleAdmin(appCode, tenantId, loginUser);
        IamTenantMember member = loadActiveMemberById(appCode, tenantId, memberId);
        List<TenantMemberDepartmentRoleRequest> assignments = normalizeAssignments(request);
        List<IamTenantDepartmentRoleService.DepartmentRoleGroup> groups = new ArrayList<>();
        for (TenantMemberDepartmentRoleRequest assignment : assignments) {
            IamDepartment department = loadAssignableDepartment(appCode, tenantId, assignment.getDepartmentId());
            List<IamRole> roles = loadAssignableRoles(appCode, assignment.getRoleCodes());
            iamDepartmentQueryService.ensureRolesAssignableToDepartment(department.getId(), roles);
            groups.add(new IamTenantDepartmentRoleService.DepartmentRoleGroup(
                department.getId(),
                roles.stream().map(IamRole::getId).distinct().toList()
            ));
        }
        iamTenantDepartmentRoleService.replaceDepartmentRoleGroups(appCode, tenantId, member.getUserId(), groups);
        return toResponse(appCode, member, tenantId);
    }

    public TenantMemberResponse removeMember(String appCode, Long tenantId, Long memberId, String remark, LoginUser loginUser) {
        TenantFlowPolicy.AppTenantPolicy policy = ensureSupportedApp(appCode);
        ensureTenantRoleAdmin(appCode, tenantId, loginUser);
        IamTenantMember member = iamTenantMemberService.getMemberById(resolveAppId(appCode), tenantId, memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }
        if (REMOVED_STATUS.equals(member.getMemberStatus())) {
            throw new BusinessException("该成员已被移出" + policy.tenantLabel());
        }
        if (listRoleCodes(appCode, tenantId, member.getUserId()).stream().anyMatch(policy.defaultAdminRoleCodes()::contains)) {
            throw new BusinessException("当前版本暂不支持直接移出" + policy.tenantLabel() + "管理员");
        }
        member.setMemberStatus(REMOVED_STATUS);
        member.setRemark(StringUtils.trimToNull(remark));
        iamTenantMemberService.save(member);
        iamTenantDepartmentRoleService.deactivateAllBindings(appCode, tenantId, member.getUserId());
        return toResponse(appCode, member, tenantId);
    }

    public List<String> listRoleCodes(String appCode, Long tenantId, Long userId) {
        ensureSupportedApp(appCode);
        if (tenantId == null || userId == null) {
            return List.of();
        }
        List<Long> roleIds = iamTenantDepartmentRoleService.listActiveRoleIds(appCode, tenantId, userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return iamRoleMapper.selectList(new LambdaQueryWrapper<IamRole>()
                .in(IamRole::getId, roleIds)
                .eq(IamRole::getDeleted, 0)
                .eq(IamRole::getStatus, 1)
                .orderByAsc(IamRole::getId))
            .stream()
            .map(IamRole::getRoleCode)
            .toList();
    }

    public void ensureTenantRoleAdmin(String appCode, Long tenantId, LoginUser loginUser) {
        if (!isTenantRoleAdmin(appCode, tenantId, loginUser)) {
            throw new BusinessException("当前账号没有成员管理权限");
        }
    }

    public boolean isTenantRoleAdmin(String appCode, Long tenantId, LoginUser loginUser) {
        TenantFlowPolicy.AppTenantPolicy policy = ensureSupportedApp(appCode);
        if (loginUser == null || tenantId == null) {
            return false;
        }
        if (hasPlatformAdmin(loginUser)) {
            return true;
        }
        iamRoleQueryService.ensureAppLogin(appCode, loginUser);
        if (hasTenantAdminRole(appCode, tenantId, loginUser.getUserId(), policy.defaultAdminRoleCodes())) {
            return true;
        }
        if (!StringUtils.equalsIgnoreCase("NEXIS", policy.appCode())) {
            return false;
        }
        Long parentTenantId = tenantRegistryService.getByAppAndId(appCode, tenantId).getParentTenantId();
        for (int depth = 0; parentTenantId != null && depth < 2; depth++) {
            var parent = tenantRegistryService.getByAppAndId(appCode, parentTenantId);
            String inheritedAdminRole = switch (StringUtils.upperCase(parent.getTenantType())) {
                case "GROUP" -> "NEXIS_GROUP_ADMIN";
                case "COMPANY" -> "NEXIS_COMPANY_ADMIN";
                default -> null;
            };
            if (inheritedAdminRole != null
                && hasTenantAdminRole(appCode, parentTenantId, loginUser.getUserId(), List.of(inheritedAdminRole))) {
                return true;
            }
            parentTenantId = parent.getParentTenantId();
        }
        return false;
    }

    public TenantFlowPolicy.AppTenantPolicy ensureSupportedApp(String appCode) {
        return tenantFlowPolicy.resolve(appCode);
    }

    private boolean hasPlatformAdmin(LoginUser loginUser) {
        if (!StringUtils.equalsIgnoreCase("WSGM", loginUser.getAppCode())) {
            return false;
        }
        return iamRoleQueryService.listRoleEntitiesByUser(loginUser.getUserId(), loginUser.getAppCode(), loginUser.getTenantId())
            .stream()
            .anyMatch(role -> StringUtils.equalsIgnoreCase("WSGM_SUPER_ADMIN", role.getRoleCode()));
    }

    private boolean hasTenantAdminRole(String appCode,
                                       Long tenantId,
                                       Long userId,
                                       List<String> adminRoleCodes) {
        IamTenantMember member = findActiveMember(appCode, tenantId, userId);
        return member != null
            && listRoleCodes(appCode, tenantId, userId).stream().anyMatch(adminRoleCodes::contains);
    }

    private Long resolveAppId(String appCode) {
        return appRegistryQueryService.getEnabledAppByCode(appCode).getId();
    }

    private IamTenantMember loadActiveMemberById(String appCode, Long tenantId, Long memberId) {
        IamTenantMember member = iamTenantMemberService.getMemberById(resolveAppId(appCode), tenantId, memberId);
        if (member == null || !ACTIVE_STATUS.equals(member.getMemberStatus())) {
            throw new BusinessException("成员不存在");
        }
        return member;
    }

    private IamDepartment loadDepartmentByCode(String appCode, String departmentCode) {
        IamDepartment department = iamDepartmentQueryService.getDepartmentByCode(resolveAppId(appCode), departmentCode);
        if (department == null) {
            throw new BusinessException("未找到" + appCode + "默认管理部门");
        }
        return department;
    }

    private IamDepartment loadAssignableDepartment(String appCode, Long tenantId, Long departmentId) {
        if (departmentId == null) {
            throw new BusinessException("分配角色时必须同时指定部门");
        }
        IamDepartment department = iamDepartmentQueryService.getDepartmentEntity(resolveAppId(appCode), departmentId);
        if (!Integer.valueOf(1).equals(department.getStatus())) {
            throw new BusinessException("目标部门已停用");
        }
        iamDepartmentQueryService.ensureDepartmentAssignableToTenant(appCode, tenantId, department);
        return department;
    }

    private TenantAdminIdentity resolveTenantAdminIdentity(TenantFlowPolicy.AppTenantPolicy policy, Long tenantId) {
        if (!StringUtils.equalsIgnoreCase("NEXIS", policy.appCode())) {
            return new TenantAdminIdentity(policy.defaultDepartmentCode(), policy.defaultAdminRoleCodes().getFirst());
        }
        String tenantType = tenantRegistryService.getByAppAndId(policy.appCode(), tenantId).getTenantType();
        return switch (StringUtils.upperCase(StringUtils.trimToEmpty(tenantType))) {
            case "GROUP" -> new TenantAdminIdentity("NEXIS_GROUP_MANAGEMENT_DEPT", "NEXIS_GROUP_ADMIN");
            case "COMPANY" -> new TenantAdminIdentity("NEXIS_COMPANY_MANAGEMENT_DEPT", "NEXIS_COMPANY_ADMIN");
            case "PROJECT" -> new TenantAdminIdentity("NEXIS_PROJECT_DEPT", "NEXIS_PROJECT_ADMIN");
            default -> throw new BusinessException("不支持的 Nexis 空间类型: " + tenantType);
        };
    }

    private record TenantAdminIdentity(String departmentCode, String roleCode) {
    }

    private IamRole loadRoleByCode(String appCode, String roleCode) {
        IamRole role = iamRoleMapper.selectOne(new LambdaQueryWrapper<IamRole>()
            .eq(IamRole::getAppCode, appCode)
            .eq(IamRole::getRoleCode, StringUtils.upperCase(StringUtils.trim(roleCode)))
            .eq(IamRole::getDeleted, 0)
            .eq(IamRole::getStatus, 1)
            .last("limit 1"));
        if (role == null) {
            throw new BusinessException("未找到角色定义: " + roleCode);
        }
        return role;
    }

    private List<IamRole> loadAssignableRoles(String appCode, List<String> roleCodes) {
        List<String> normalizedRoleCodes = roleCodes == null ? List.of() : roleCodes.stream()
            .filter(StringUtils::isNotBlank)
            .map(StringUtils::trim)
            .map(StringUtils::upperCase)
            .distinct()
            .toList();
        if (normalizedRoleCodes.isEmpty()) {
            throw new BusinessException("至少分配一个角色");
        }
        LambdaQueryWrapper<IamRole> queryWrapper = new LambdaQueryWrapper<IamRole>()
            .eq(IamRole::getAppCode, appCode)
            .in(IamRole::getRoleCode, normalizedRoleCodes)
            .eq(IamRole::getDeleted, 0)
            .eq(IamRole::getStatus, 1)
            .eq(IamRole::getAssignable, 1)
            .orderByAsc(IamRole::getId);
        if (!StringUtils.equalsIgnoreCase("WSGM", appCode)) {
            queryWrapper.eq(IamRole::getAdminRole, 0);
        }
        List<IamRole> roles = iamRoleMapper.selectList(queryWrapper);
        if (roles.size() != normalizedRoleCodes.size()) {
            throw new BusinessException("角色不存在、已停用，或不允许分配平台管理员角色");
        }
        return roles;
    }

    private List<TenantMemberDepartmentRoleRequest> normalizeAssignments(TenantMemberAssignRequest request) {
        if (request == null) {
            throw new BusinessException("部门角色分配不能为空");
        }
        if (request.getDepartmentRoles() != null && !request.getDepartmentRoles().isEmpty()) {
            return request.getDepartmentRoles();
        }
        TenantMemberDepartmentRoleRequest assignment = new TenantMemberDepartmentRoleRequest();
        assignment.setDepartmentId(request.getDepartmentId());
        assignment.setRoleCodes(request.getRoleCodes());
        return List.of(assignment);
    }

    private TenantMemberResponse toResponse(String appCode, IamTenantMember member, Long tenantId) {
        var tenant = tenantRegistryService.getByAppAndId(appCode, tenantId);
        IamUser user = iamUserMapper.selectById(member.getUserId());
        IamUserInfo userInfo = iamUserInfoService.getActiveUserInfo(resolveAppId(appCode), member.getUserId());
        IamDepartment primaryDepartment = iamDepartmentQueryService.getPrimaryDepartmentByUser(member.getUserId(), appCode, tenantId);
        List<String> roleCodes = listRoleCodes(appCode, member.getTenantId(), member.getUserId());
        List<TenantMemberDepartmentRoleResponse> departmentRoles = listDepartmentRoles(appCode, member.getTenantId(), member.getUserId());
        TenantMemberResponse response = new TenantMemberResponse();
        response.setId(member.getId());
        response.setTenantId(member.getTenantId());
        response.setTenantName(tenant.getTenantName());
        response.setUserId(member.getUserId());
        response.setUsername(user == null ? null : user.getUsername());
        response.setNickName(userInfo == null ? null : userInfo.getNickName());
        response.setAvatarUrl(userInfo == null ? null : userInfo.getAvatarUrl());
        response.setMemberStatus(member.getMemberStatus());
        response.setJoinedAt(member.getJoinedAt());
        response.setDepartmentId(primaryDepartment == null ? null : primaryDepartment.getId());
        response.setDepartmentCode(primaryDepartment == null ? null : primaryDepartment.getDeptCode());
        response.setDepartmentName(primaryDepartment == null ? null : primaryDepartment.getDeptName());
        response.setRoleCodes(roleCodes);
        response.setDepartmentRoles(departmentRoles);
        return response;
    }

    private List<TenantMemberDepartmentRoleResponse> listDepartmentRoles(String appCode, Long tenantId, Long userId) {
        List<IamTenantDepartmentRoleBinding> bindings = iamTenantDepartmentRoleService.listActiveBindings(appCode, tenantId, userId);
        Map<Long, List<Long>> roleIdsByDepartment = new LinkedHashMap<>();
        for (IamTenantDepartmentRoleBinding binding : bindings) {
            Long departmentId = binding.getDepartmentId();
            Long roleId = binding.getRoleId();
            if (departmentId == null || departmentId <= 0 || roleId == null || roleId <= 0) {
                continue;
            }
            roleIdsByDepartment.computeIfAbsent(departmentId, ignored -> new ArrayList<>()).add(roleId);
        }
        if (roleIdsByDepartment.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = roleIdsByDepartment.values().stream()
            .flatMap(List::stream)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, String> roleCodeById = iamRoleMapper.selectBatchIds(roleIds).stream()
            .collect(LinkedHashMap::new, (map, role) -> map.put(role.getId(), role.getRoleCode()), Map::putAll);
        Long appId = resolveAppId(appCode);
        return roleIdsByDepartment.entrySet().stream()
            .map(entry -> {
                IamDepartment department = iamDepartmentQueryService.getDepartmentEntity(appId, entry.getKey());
                TenantMemberDepartmentRoleResponse response = new TenantMemberDepartmentRoleResponse();
                response.setDepartmentId(entry.getKey());
                response.setDepartmentCode(department.getDeptCode());
                response.setDepartmentName(department.getDeptName());
                response.setRoleCodes(entry.getValue().stream()
                    .map(roleCodeById::get)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .toList());
                return response;
            })
            .toList();
    }
}

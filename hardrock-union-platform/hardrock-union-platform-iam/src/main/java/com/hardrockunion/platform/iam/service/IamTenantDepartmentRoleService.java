package com.hardrockunion.platform.iam.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hardrockunion.framework.core.exception.BusinessException;
import com.hardrockunion.platform.iam.domain.model.IamTenantDepartmentRoleBinding;

@Service
@DS("master")
public class IamTenantDepartmentRoleService {

    private static final String TABLE_NAME = "iam_tenant_member_department_role";
    private static final List<String> TENANT_UNIQUE_ROLE_CODES = List.of(
        "WSGM_SUPER_ADMIN",
        "NEXIS_DECISION_LEADER",
        "PRIMELOAD_MARKETPLACE_DECISION_LEADER",
        "PRIMELOAD_LOGISTICS_DECISION_LEADER",
        "PRIMELOAD_CLOUD_WAREHOUSE_DECISION_LEADER",
        "PRIMELOAD_DELIVERY_DECISION_LEADER"
    );
    private static final String WSGM_SUPER_ADMIN_ROLE_CODE = "WSGM_SUPER_ADMIN";
    private static final Long NO_DEPARTMENT_ID = 0L;
    private static final Long NO_ROLE_ID = 0L;
    private final JdbcTemplate jdbcTemplate;
    private final AppRegistryQueryService appRegistryQueryService;

    public IamTenantDepartmentRoleService(JdbcTemplate jdbcTemplate,
                                          AppRegistryQueryService appRegistryQueryService) {
        this.jdbcTemplate = jdbcTemplate;
        this.appRegistryQueryService = appRegistryQueryService;
    }

    public List<IamTenantDepartmentRoleBinding> listActiveBindings(String appCode, Long tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return List.of();
        }
        Long appId = resolveAppId(appCode);
        String sql = "SELECT id, app_id, tenant_id, user_id, department_id, role_id, primary_flag, status, deleted "
            + "FROM " + TABLE_NAME + " "
            + "WHERE app_id = ? AND tenant_id = ? AND user_id = ? AND deleted = 0 AND status = 1 "
            + "ORDER BY primary_flag DESC, id ASC";
        return jdbcTemplate.query(sql, ROW_MAPPER, appId, tenantId, userId);
    }

    public List<IamTenantDepartmentRoleBinding> listActiveBindingsByDepartment(String appCode, Long tenantId, Long departmentId) {
        if (tenantId == null || departmentId == null) {
            return List.of();
        }
        Long appId = resolveAppId(appCode);
        String sql = "SELECT id, app_id, tenant_id, user_id, department_id, role_id, primary_flag, status, deleted "
            + "FROM " + TABLE_NAME + " "
            + "WHERE app_id = ? AND tenant_id = ? AND department_id = ? AND deleted = 0 AND status = 1 "
            + "ORDER BY primary_flag DESC, id ASC";
        return jdbcTemplate.query(sql, ROW_MAPPER, appId, tenantId, departmentId);
    }

    public long countActiveBindingsByDepartment(String appCode, Long departmentId) {
        if (departmentId == null) {
            return 0L;
        }
        Long appId = resolveAppId(appCode);
        String sql = "SELECT COUNT(DISTINCT user_id) FROM " + TABLE_NAME
            + " WHERE app_id = ? AND department_id = ? AND deleted = 0 AND status = 1";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, appId, departmentId);
        return count == null ? 0L : count;
    }

    public long countActiveBindingsByRole(String appCode, Long roleId) {
        if (roleId == null) {
            return 0L;
        }
        Long appId = resolveAppId(appCode);
        String sql = "SELECT COUNT(1) FROM " + TABLE_NAME
            + " WHERE app_id = ? AND role_id = ? AND deleted = 0 AND status = 1";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, appId, roleId);
        return count == null ? 0L : count;
    }

    public List<Long> listActiveRoleIds(String appCode, Long tenantId, Long userId) {
        return listActiveBindings(appCode, tenantId, userId).stream()
            .map(IamTenantDepartmentRoleBinding::getRoleId)
            .filter(Objects::nonNull)
            .filter(roleId -> roleId > 0)
            .distinct()
            .toList();
    }

    public List<Long> listActiveDepartmentIds(String appCode, Long tenantId, Long userId) {
        return listActiveBindings(appCode, tenantId, userId).stream()
            .map(IamTenantDepartmentRoleBinding::getDepartmentId)
            .filter(Objects::nonNull)
            .filter(departmentId -> departmentId > 0)
            .distinct()
            .toList();
    }

    public Long getPrimaryDepartmentId(String appCode, Long tenantId, Long userId) {
        List<IamTenantDepartmentRoleBinding> bindings = listActiveBindings(appCode, tenantId, userId);
        return bindings.stream()
            .filter(binding -> Integer.valueOf(1).equals(binding.getPrimaryFlag()))
            .map(IamTenantDepartmentRoleBinding::getDepartmentId)
            .filter(Objects::nonNull)
            .filter(departmentId -> departmentId > 0)
            .findFirst()
            .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setPrimaryDepartment(String appCode,
                                     Long tenantId,
                                     Long userId,
                                     Long departmentId,
                                     boolean allowCreate) {
        if (tenantId == null || userId == null || departmentId == null) {
            throw new BusinessException("tenantId、userId、departmentId 不能为空");
        }
        Long appId = resolveAppId(appCode);
        Long normalizedDepartmentId = normalizeDepartmentId(departmentId);
        List<IamTenantDepartmentRoleBinding> activeBindings = listActiveBindings(appCode, tenantId, userId);
        boolean departmentExists = activeBindings.stream()
            .anyMatch(binding -> normalizedDepartmentId.equals(binding.getDepartmentId()));
        if (!departmentExists) {
            if (!allowCreate) {
                throw new BusinessException("用户尚未加入该部门");
            }
            upsertBinding(appId, tenantId, userId, normalizedDepartmentId, NO_ROLE_ID, 1);
            activeBindings = listActiveBindings(appCode, tenantId, userId);
        }

        jdbcTemplate.update(
            "UPDATE " + TABLE_NAME + " SET primary_flag = CASE WHEN department_id = ? THEN 1 ELSE 0 END "
                + "WHERE app_id = ? AND tenant_id = ? AND user_id = ? AND deleted = 0 AND status = 1",
            normalizedDepartmentId, appId, tenantId, userId
        );

        boolean hasBaseDepartmentRow = activeBindings.stream()
            .anyMatch(binding -> normalizedDepartmentId.equals(binding.getDepartmentId()) && NO_ROLE_ID.equals(binding.getRoleId()));
        if (!hasBaseDepartmentRow) {
            upsertBinding(appId, tenantId, userId, normalizedDepartmentId, NO_ROLE_ID, 1);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceRoles(String appCode,
                             Long tenantId,
                             Long userId,
                             Long departmentId,
                             List<Long> roleIds) {
        if (tenantId == null || userId == null) {
            throw new BusinessException("tenantId、userId 不能为空");
        }
        Long appId = resolveAppId(appCode);
        Long targetDepartmentId = normalizeDepartmentId(departmentId);
        Long primaryDepartmentId = getPrimaryDepartmentId(appCode, tenantId, userId);
        if (targetDepartmentId <= 0 && primaryDepartmentId != null) {
            targetDepartmentId = primaryDepartmentId;
        }
        List<Long> normalizedRoleIds = roleIds == null ? List.of() : roleIds.stream()
            .filter(Objects::nonNull)
            .filter(roleId -> roleId > 0)
            .distinct()
            .toList();
        ensureTenantUniqueRoleNotRemoved(appId, tenantId, userId, normalizedRoleIds);

        if (normalizedRoleIds.isEmpty()) {
            jdbcTemplate.update(
                "UPDATE " + TABLE_NAME + " SET deleted = 1 WHERE app_id = ? AND tenant_id = ? AND user_id = ? AND deleted = 0",
                appId, tenantId, userId
            );
            return;
        }
        transferTenantUniqueRoles(appId, tenantId, userId, normalizedRoleIds);
        jdbcTemplate.update(
            "UPDATE " + TABLE_NAME + " SET deleted = 1 WHERE app_id = ? AND tenant_id = ? AND user_id = ? AND deleted = 0",
            appId, tenantId, userId
        );

        Integer primaryFlag = targetDepartmentId > 0 ? 1 : 0;
        for (Long roleId : normalizedRoleIds) {
            IamTenantDepartmentRoleBinding existingRoleBinding = findBinding(appId, tenantId, userId, targetDepartmentId, roleId);
            if (existingRoleBinding == null) {
                jdbcTemplate.update(
                    "INSERT INTO " + TABLE_NAME + " (app_id, tenant_id, user_id, department_id, role_id, primary_flag, status, deleted) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 1, 0)",
                    appId, tenantId, userId, targetDepartmentId, roleId, primaryFlag
                );
                continue;
            }
            jdbcTemplate.update(
                "UPDATE " + TABLE_NAME + " SET primary_flag = ?, status = 1, deleted = 0 WHERE id = ?",
                primaryFlag, existingRoleBinding.getId()
            );
        }
        ensureUniqueRoleInvariant(appId, tenantId, normalizedRoleIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceDepartmentRoles(String appCode,
                                       Long tenantId,
                                       Long userId,
                                       Long departmentId,
                                       List<Long> roleIds) {
        if (tenantId == null || userId == null) {
            throw new BusinessException("tenantId、userId 不能为空");
        }
        Long normalizedDepartmentId = normalizeDepartmentId(departmentId);
        if (normalizedDepartmentId <= 0) {
            throw new BusinessException("departmentId 不能为空");
        }
        List<Long> normalizedRoleIds = roleIds == null ? List.of() : roleIds.stream()
            .filter(Objects::nonNull)
            .filter(roleId -> roleId > 0)
            .distinct()
            .toList();
        if (normalizedRoleIds.isEmpty()) {
            throw new BusinessException("至少分配一个角色");
        }
        Long appId = resolveAppId(appCode);
        ensureTenantUniqueRoleNotRemoved(appId, tenantId, userId, normalizedRoleIds);
        transferTenantUniqueRoles(appId, tenantId, userId, normalizedRoleIds);
        jdbcTemplate.update(
            "UPDATE " + TABLE_NAME + " SET deleted = 1 WHERE app_id = ? AND tenant_id = ? AND user_id = ? AND deleted = 0",
            appId, tenantId, userId
        );
        for (Long roleId : normalizedRoleIds) {
            upsertBinding(appId, tenantId, userId, normalizedDepartmentId, roleId, 1);
        }
        ensureUniqueRoleInvariant(appId, tenantId, normalizedRoleIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceDepartmentRoleGroups(String appCode,
                                            Long tenantId,
                                            Long userId,
                                            List<DepartmentRoleGroup> groups) {
        if (tenantId == null || userId == null) {
            throw new BusinessException("tenantId、userId 不能为空");
        }
        if (groups == null || groups.isEmpty()) {
            throw new BusinessException("至少分配一个部门角色");
        }
        Long appId = resolveAppId(appCode);
        List<Long> allRoleIds = groups.stream()
            .filter(Objects::nonNull)
            .flatMap(group -> group.roleIds() == null ? List.<Long>of().stream() : group.roleIds().stream())
            .filter(Objects::nonNull)
            .filter(roleId -> roleId > 0)
            .distinct()
            .toList();
        ensureTenantUniqueRoleNotRemoved(appId, tenantId, userId, allRoleIds);
        transferTenantUniqueRoles(appId, tenantId, userId, allRoleIds);
        jdbcTemplate.update(
            "UPDATE " + TABLE_NAME + " SET deleted = 1 WHERE app_id = ? AND tenant_id = ? AND user_id = ? AND deleted = 0",
            appId, tenantId, userId
        );

        boolean primaryAssigned = false;
        for (DepartmentRoleGroup group : groups) {
            Long normalizedDepartmentId = normalizeDepartmentId(group.departmentId());
            if (normalizedDepartmentId <= 0) {
                throw new BusinessException("departmentId 不能为空");
            }
            List<Long> normalizedRoleIds = group.roleIds() == null ? List.of() : group.roleIds().stream()
                .filter(Objects::nonNull)
                .filter(roleId -> roleId > 0)
                .distinct()
                .toList();
            if (normalizedRoleIds.isEmpty()) {
                throw new BusinessException("每个部门至少分配一个角色");
            }
            Integer primaryFlag = primaryAssigned ? 0 : 1;
            primaryAssigned = true;
            for (Long roleId : normalizedRoleIds) {
                upsertBinding(appId, tenantId, userId, normalizedDepartmentId, roleId, primaryFlag);
            }
        }
        ensureUniqueRoleInvariant(appId, tenantId, allRoleIds);
    }

    public record DepartmentRoleGroup(Long departmentId, List<Long> roleIds) {
    }

    public void activateRole(String appCode,
                             Long tenantId,
                             Long userId,
                             Long departmentId,
                             Long roleId) {
        if (roleId == null || roleId <= 0) {
            return;
        }
        replaceRoles(appCode, tenantId, userId, departmentId, List.of(roleId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deactivateAllBindings(String appCode, Long tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return;
        }
        Long appId = resolveAppId(appCode);
        ensureTenantUniqueRoleNotRemoved(appId, tenantId, userId, List.of());
        jdbcTemplate.update(
            "UPDATE " + TABLE_NAME + " SET deleted = 1 WHERE app_id = ? AND tenant_id = ? AND user_id = ? AND deleted = 0",
            appId, tenantId, userId
        );
    }

    private List<IamTenantDepartmentRoleBinding> listAllBindings(Long appId, Long tenantId, Long userId) {
        String sql = "SELECT id, app_id, tenant_id, user_id, department_id, role_id, primary_flag, status, deleted "
            + "FROM " + TABLE_NAME + " WHERE app_id = ? AND tenant_id = ? AND user_id = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, ROW_MAPPER, appId, tenantId, userId);
    }

    private IamTenantDepartmentRoleBinding findBinding(Long appId,
                                                       Long tenantId,
                                                       Long userId,
                                                       Long departmentId,
                                                       Long roleId) {
        List<IamTenantDepartmentRoleBinding> bindings = jdbcTemplate.query(
            "SELECT id, app_id, tenant_id, user_id, department_id, role_id, primary_flag, status, deleted "
                + "FROM " + TABLE_NAME + " WHERE app_id = ? AND tenant_id = ? AND user_id = ? AND department_id = ? AND role_id = ? LIMIT 1",
            ROW_MAPPER,
            appId, tenantId, userId, departmentId, roleId
        );
        return bindings.isEmpty() ? null : bindings.getFirst();
    }

    private void upsertBinding(Long appId,
                               Long tenantId,
                               Long userId,
                               Long departmentId,
                               Long roleId,
                               Integer primaryFlag) {
        IamTenantDepartmentRoleBinding binding = findBinding(appId, tenantId, userId, departmentId, roleId);
        if (binding == null) {
            jdbcTemplate.update(
                "INSERT INTO " + TABLE_NAME + " (app_id, tenant_id, user_id, department_id, role_id, primary_flag, status, deleted) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 1, 0)",
                appId, tenantId, userId, departmentId, roleId, primaryFlag
            );
            return;
        }
        jdbcTemplate.update(
            "UPDATE " + TABLE_NAME + " SET primary_flag = ?, status = 1, deleted = 0 WHERE id = ?",
            primaryFlag, binding.getId()
        );
    }

    private Long normalizeDepartmentId(Long departmentId) {
        return departmentId == null || departmentId < 0 ? NO_DEPARTMENT_ID : departmentId;
    }

    private Long resolveAppId(String appCode) {
        return appRegistryQueryService.getAppByCode(appCode).getId();
    }

    private void transferTenantUniqueRoles(Long appId, Long tenantId, Long userId, List<Long> roleIds) {
        if (appId == null || tenantId == null || userId == null || roleIds == null || roleIds.isEmpty()) {
            return;
        }
        String sql = "SELECT r.role_code FROM iam_role r "
            + "WHERE r.app_id = ? AND r.id = ? AND r.deleted = 0 AND r.status = 1";
        for (Long roleId : roleIds) {
            List<String> roleCodes = jdbcTemplate.queryForList(sql, String.class, appId, roleId);
            if (roleCodes.isEmpty() || !TENANT_UNIQUE_ROLE_CODES.contains(roleCodes.getFirst())) {
                continue;
            }
            jdbcTemplate.update(
                "UPDATE " + TABLE_NAME + " rel SET rel.deleted = 1 "
                    + "WHERE rel.app_id = ? AND rel.tenant_id = ? AND rel.role_id = ? "
                    + "AND rel.user_id <> ? AND rel.deleted = 0 AND rel.status = 1",
                appId, tenantId, roleId, userId
            );
        }
    }

    private void ensureUniqueRoleInvariant(Long appId, Long tenantId, List<Long> roleIds) {
        if (appId == null || roleIds == null || roleIds.isEmpty()) {
            return;
        }
        String roleCodeSql = "SELECT role_code FROM iam_role WHERE app_id = ? AND id = ? AND deleted = 0 AND status = 1";
        for (Long roleId : roleIds.stream().filter(Objects::nonNull).filter(id -> id > 0).distinct().toList()) {
            List<String> roleCodes = jdbcTemplate.queryForList(roleCodeSql, String.class, appId, roleId);
            if (roleCodes.isEmpty() || !TENANT_UNIQUE_ROLE_CODES.contains(roleCodes.getFirst())) {
                continue;
            }
            String roleCode = roleCodes.getFirst();
            Long holderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT user_id) FROM " + TABLE_NAME
                    + " WHERE app_id = ? AND tenant_id = ? AND role_id = ? AND deleted = 0 AND status = 1",
                Long.class,
                appId, tenantId, roleId
            );
            if (holderCount != null && holderCount > 1) {
                throw new BusinessException(roleCode + " 只能唯一持有，请先转让后再分配");
            }
        }
    }

    private void ensureTenantUniqueRoleNotRemoved(Long appId, Long tenantId, Long userId, List<Long> nextRoleIds) {
        if (appId == null || tenantId == null || userId == null) {
            return;
        }
        List<UniqueRole> currentUniqueRoles = listActiveTenantUniqueRoles(appId, tenantId, userId);
        if (currentUniqueRoles.isEmpty()) {
            return;
        }
        for (UniqueRole role : currentUniqueRoles) {
            boolean keepsRole = nextRoleIds != null && nextRoleIds.contains(role.roleId());
            if (!keepsRole) {
                throw new BusinessException(role.roleCode() + " 只能在当前租户内转让，不能删除");
            }
        }
    }

    private List<UniqueRole> listActiveTenantUniqueRoles(Long appId, Long tenantId, Long userId) {
        return jdbcTemplate.query(
            "SELECT rel.role_id, r.role_code FROM " + TABLE_NAME + " rel "
                + "JOIN iam_role r ON r.id = rel.role_id AND r.app_id = rel.app_id "
                + "WHERE rel.app_id = ? AND rel.tenant_id = ? AND rel.user_id = ? "
                + "AND rel.deleted = 0 AND rel.status = 1 "
                + "AND r.deleted = 0 AND r.status = 1",
            (rs, rowNum) -> new UniqueRole(rs.getLong("role_id"), rs.getString("role_code")),
            appId, tenantId, userId
        ).stream()
            .filter(role -> role.roleId() != null && role.roleId() > 0)
            .filter(role -> TENANT_UNIQUE_ROLE_CODES.contains(role.roleCode()))
            .distinct()
            .toList();
    }

    private record UniqueRole(Long roleId, String roleCode) {
    }

    private Long findRoleId(Long appId, String roleCode) {
        List<Long> roleIds = jdbcTemplate.queryForList(
            "SELECT id FROM iam_role WHERE app_id = ? AND role_code = ? AND deleted = 0 LIMIT 1",
            Long.class,
            appId, roleCode
        );
        return roleIds.isEmpty() ? null : roleIds.getFirst();
    }

    public boolean isWsgmSuperAdminRole(Long appId, Long roleId) {
        Long superAdminRoleId = findRoleId(appId, WSGM_SUPER_ADMIN_ROLE_CODE);
        return superAdminRoleId != null && superAdminRoleId.equals(roleId);
    }

    private static final RowMapper<IamTenantDepartmentRoleBinding> ROW_MAPPER = new RowMapper<>() {
        @Override
        public IamTenantDepartmentRoleBinding mapRow(ResultSet rs, int rowNum) throws SQLException {
            IamTenantDepartmentRoleBinding binding = new IamTenantDepartmentRoleBinding();
            binding.setId(rs.getLong("id"));
            binding.setAppId(rs.getLong("app_id"));
            binding.setTenantId(rs.getLong("tenant_id"));
            binding.setUserId(rs.getLong("user_id"));
            binding.setDepartmentId(rs.getLong("department_id"));
            binding.setRoleId(rs.getLong("role_id"));
            binding.setPrimaryFlag(rs.getInt("primary_flag"));
            binding.setStatus(rs.getInt("status"));
            binding.setDeleted(rs.getInt("deleted"));
            return binding;
        }
    };
}

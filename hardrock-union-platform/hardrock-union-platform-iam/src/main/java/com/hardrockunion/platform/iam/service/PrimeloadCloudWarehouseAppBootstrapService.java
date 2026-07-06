package com.hardrockunion.platform.iam.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class PrimeloadCloudWarehouseAppBootstrapService {

    private static final String APP_CODE = "PRIMELOAD-CLOUD-WAREHOUSE";
    private static final String APP_NAME = "一车好料云仓";
    private static final String DECISION_DEPT_CODE = "PRIMELOAD_CLOUD_WAREHOUSE_DECISION_DEPT";
    private static final String DECISION_ROLE_CODE = "PRIMELOAD_CLOUD_WAREHOUSE_DECISION_LEADER";

    private final JdbcTemplate jdbcTemplate;

    public PrimeloadCloudWarehouseAppBootstrapService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void bootstrap() {
        if (!tableExists("app_registry")) {
            return;
        }
        ensureApp();
        Long appId = findAppId();
        if (appId == null) {
            return;
        }
        ensureDepartment(appId);
        ensureRole(appId);
        ensureDepartmentRoleOption(appId);
    }

    private void ensureApp() {
        if (findAppId() == null) {
            jdbcTemplate.update("""
                INSERT INTO app_registry (app_code, app_name, app_type, home_path, login_path, icon, sort_no, status, description, deleted)
                VALUES (?, ?, 'WAREHOUSE_PLATFORM', '/primeload-cloud-warehouse', '/primeload-cloud-warehouse/login', NULL, 50, 1, ?, 0)
                """, APP_CODE, APP_NAME, "一车好料云仓应用");
            return;
        }
        jdbcTemplate.update("""
            UPDATE app_registry
            SET app_name = ?,
                app_type = COALESCE(app_type, 'WAREHOUSE_PLATFORM'),
                home_path = COALESCE(home_path, '/primeload-cloud-warehouse'),
                login_path = COALESCE(login_path, '/primeload-cloud-warehouse/login'),
                description = COALESCE(description, ?),
                sort_no = CASE WHEN sort_no = 0 THEN 50 ELSE sort_no END,
                status = 1,
                deleted = 0
            WHERE app_code = ?
            """, APP_NAME, "一车好料云仓应用", APP_CODE);
    }

    private void ensureDepartment(Long appId) {
        if (!tableExists("iam_department") || exists("iam_department", "app_id", appId, "dept_code", DECISION_DEPT_CODE)) {
            return;
        }
        jdbcTemplate.update("""
            INSERT INTO iam_department (app_id, app_code, dept_code, dept_name, dept_short_name, parent_id, dept_type, status, sort_no, deleted)
            VALUES (?, ?, ?, '决策部', '决策部', 0, 'DECISION', 1, 10, 0)
            """, appId, APP_CODE, DECISION_DEPT_CODE);
    }

    private void ensureRole(Long appId) {
        if (!tableExists("iam_role") || exists("iam_role", "app_id", appId, "role_code", DECISION_ROLE_CODE)) {
            return;
        }
        jdbcTemplate.update("""
            INSERT INTO iam_role (app_id, app_code, role_code, role_name, status, assignable, admin_role, deleted)
            VALUES (?, ?, ?, '决策部负责人', 1, 1, 1, 0)
            """, appId, APP_CODE, DECISION_ROLE_CODE);
    }

    private void ensureDepartmentRoleOption(Long appId) {
        if (!tableExists("iam_department_role_option")) {
            return;
        }
        Long departmentId = findId("iam_department", "app_id", appId, "dept_code", DECISION_DEPT_CODE);
        Long roleId = findId("iam_role", "app_id", appId, "role_code", DECISION_ROLE_CODE);
        if (departmentId == null || roleId == null) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM iam_department_role_option
            WHERE app_id = ?
              AND department_id = ?
              AND role_id = ?
              AND deleted = 0
            """, Integer.class, appId, departmentId, roleId);
        if (count == null || count == 0) {
            jdbcTemplate.update("""
                INSERT INTO iam_department_role_option (app_id, app_code, department_id, role_id, deleted)
                VALUES (?, ?, ?, ?, 0)
                """, appId, APP_CODE, departmentId, roleId);
        }
    }

    private Long findAppId() {
        return jdbcTemplate.query("""
            SELECT id
            FROM app_registry
            WHERE app_code = ?
              AND deleted = 0
            LIMIT 1
            """, rs -> rs.next() ? rs.getLong("id") : null, APP_CODE);
    }

    private Long findId(String tableName, String appIdColumn, Long appId, String codeColumn, String codeValue) {
        if (!tableExists(tableName)) {
            return null;
        }
        return jdbcTemplate.query("SELECT id FROM `" + tableName + "` WHERE `" + appIdColumn + "` = ? AND `" + codeColumn + "` = ? AND deleted = 0 LIMIT 1",
            rs -> rs.next() ? rs.getLong("id") : null, appId, codeValue);
    }

    private boolean exists(String tableName, String appIdColumn, Long appId, String codeColumn, String codeValue) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `" + tableName + "` WHERE `" + appIdColumn + "` = ? AND `" + codeColumn + "` = ? AND deleted = 0",
            Integer.class, appId, codeValue);
        return count != null && count > 0;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = ?
            """, Integer.class, tableName);
        return count != null && count > 0;
    }
}

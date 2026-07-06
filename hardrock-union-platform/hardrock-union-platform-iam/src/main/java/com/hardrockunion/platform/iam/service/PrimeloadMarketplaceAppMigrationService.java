package com.hardrockunion.platform.iam.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class PrimeloadMarketplaceAppMigrationService {

    private static final String OLD_APP_CODE = "G" + "DWL";
    private static final String NEW_APP_CODE = "PRIMELOAD-MARKETPLACE";
    private static final String NEW_APP_NAME = "一车好料商城";
    private static final String OLD_APP_PATH = "/" + "g" + "dwl";
    private static final String OLD_CODE_PREFIX = "G" + "DWL_";
    private static final String OLD_TENANT_PREFIX = "G" + "DWL-";

    private final JdbcTemplate jdbcTemplate;

    public PrimeloadMarketplaceAppMigrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        if (!tableExists("app_registry")) {
            return;
        }
        migrateAppRegistry();
        replaceAppCodeColumns();
        replaceCodePrefixes();
        replacePaths();
    }

    private void migrateAppRegistry() {
        Long oldAppId = findAppId(OLD_APP_CODE);
        Long newAppId = findAppId(NEW_APP_CODE);
        if (oldAppId != null && newAppId == null) {
            jdbcTemplate.update("""
                UPDATE app_registry
                SET app_code = ?,
                    app_name = ?,
                    home_path = REPLACE(COALESCE(home_path, '/primeload-marketplace'), ?, '/primeload-marketplace'),
                    login_path = REPLACE(COALESCE(login_path, '/primeload-marketplace/login'), ?, '/primeload-marketplace'),
                    description = ?,
                    status = 1,
                    deleted = 0
                WHERE id = ?
                """, NEW_APP_CODE, NEW_APP_NAME, OLD_APP_PATH, OLD_APP_PATH, "一车好料商城应用", oldAppId);
            return;
        }
        if (newAppId == null) {
            jdbcTemplate.update("""
                INSERT INTO app_registry (app_code, app_name, app_type, home_path, login_path, icon, sort_no, status, description, deleted)
                VALUES (?, ?, 'SAAS_PLATFORM', '/primeload-marketplace', '/primeload-marketplace/login', NULL, 30, 1, ?, 0)
                """, NEW_APP_CODE, NEW_APP_NAME, "一车好料商城应用");
            return;
        }
        jdbcTemplate.update("""
            UPDATE app_registry
            SET app_name = ?,
                home_path = COALESCE(home_path, '/primeload-marketplace'),
                login_path = COALESCE(login_path, '/primeload-marketplace/login'),
                description = COALESCE(description, ?),
                status = 1,
                deleted = 0
            WHERE id = ?
            """, NEW_APP_NAME, "一车好料商城应用", newAppId);
        if (oldAppId != null && !oldAppId.equals(newAppId)) {
            migrateAppIdReferences(oldAppId, newAppId);
            jdbcTemplate.update("""
                UPDATE app_registry
                SET app_code = CONCAT('LEGACY_APP_', id),
                    deleted = 1,
                    status = 0
                WHERE id = ?
                """, oldAppId);
        }
    }

    private void migrateAppIdReferences(Long oldAppId, Long newAppId) {
        replaceId("iam_user", "app_id", oldAppId, newAppId);
        replaceId("iam_user_info", "app_id", oldAppId, newAppId);
        replaceId("iam_role", "app_id", oldAppId, newAppId);
        replaceId("iam_department", "app_id", oldAppId, newAppId);
        replaceId("iam_department_role_option", "app_id", oldAppId, newAppId);
        replaceId("iam_permission", "app_id", oldAppId, newAppId);
        replaceId("iam_department_role_permission", "app_id", oldAppId, newAppId);
        replaceId("iam_tenant_member", "app_id", oldAppId, newAppId);
        replaceId("iam_tenant_join_request", "app_id", oldAppId, newAppId);
        replaceId("iam_tenant_member_department_role", "app_id", oldAppId, newAppId);
        replaceId("tenant_registry", "app_id", oldAppId, newAppId);
        replaceId("workflow_task", "app_id", oldAppId, newAppId);
        replaceId("message_thread", "app_id", oldAppId, newAppId);
        replaceId("message_record", "app_id", oldAppId, newAppId);
        replaceId("message_recipient", "app_id", oldAppId, newAppId);
        replaceId("logistics_shipment_record", "business_app_id", oldAppId, newAppId);
    }

    private void replaceAppCodeColumns() {
        replaceValue("iam_user", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("iam_role", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("iam_department", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("iam_department_role_option", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("iam_permission", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("iam_department_role_permission", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("tenant_registry", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("workflow_task", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("message_thread", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("message_record", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("message_recipient", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("warehouse_registry", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("warehouse_registry", "owner_app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("warehouse_stock", "app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("logistics_shipment_record", "business_app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("merchant_quotation", "target_app_code", OLD_APP_CODE, NEW_APP_CODE);
        replaceValue("merchant_order", "target_app_code", OLD_APP_CODE, NEW_APP_CODE);
    }

    private void replaceCodePrefixes() {
        replacePrefix("iam_role", "role_code", OLD_CODE_PREFIX, "PRIMELOAD_MARKETPLACE_");
        replacePrefix("iam_department", "dept_code", OLD_CODE_PREFIX, "PRIMELOAD_MARKETPLACE_");
        replacePrefix("iam_permission", "permission_code", OLD_CODE_PREFIX, "PRIMELOAD_MARKETPLACE_");
        replacePrefix("tenant_registry", "tenant_code", OLD_TENANT_PREFIX, "PRIMELOAD-MARKETPLACE-");
        replacePrefix("warehouse_registry", "warehouse_code", OLD_TENANT_PREFIX, "PRIMELOAD-MARKETPLACE-");
    }

    private void replacePaths() {
        replaceText("iam_permission", "permission_path", "/api" + OLD_APP_PATH, "/api/primeload-marketplace");
        replaceText("workflow_task", "action_url", "/api" + OLD_APP_PATH, "/api/primeload-marketplace");
        replaceText("message_record", "action_url", "/api" + OLD_APP_PATH, "/api/primeload-marketplace");
        replaceText("app_registry", "home_path", OLD_APP_PATH, "/primeload-marketplace");
        replaceText("app_registry", "login_path", OLD_APP_PATH, "/primeload-marketplace");
    }

    private Long findAppId(String appCode) {
        return jdbcTemplate.query("""
            SELECT id
            FROM app_registry
            WHERE app_code = ?
              AND deleted = 0
            LIMIT 1
            """, rs -> rs.next() ? rs.getLong("id") : null, appCode);
    }

    private void replaceId(String tableName, String columnName, Long oldValue, Long newValue) {
        if (tableExists(tableName) && columnExists(tableName, columnName)) {
            jdbcTemplate.update("UPDATE `" + tableName + "` SET `" + columnName + "` = ? WHERE `" + columnName + "` = ?", newValue, oldValue);
        }
    }

    private void replaceValue(String tableName, String columnName, String oldValue, String newValue) {
        if (tableExists(tableName) && columnExists(tableName, columnName)) {
            jdbcTemplate.update("UPDATE `" + tableName + "` SET `" + columnName + "` = ? WHERE `" + columnName + "` = ?", newValue, oldValue);
        }
    }

    private void replacePrefix(String tableName, String columnName, String oldValue, String newValue) {
        if (tableExists(tableName) && columnExists(tableName, columnName)) {
            jdbcTemplate.update("UPDATE `" + tableName + "` SET `" + columnName + "` = REPLACE(`" + columnName + "`, ?, ?) WHERE `" + columnName + "` LIKE ?", oldValue, newValue, oldValue + "%");
        }
    }

    private void replaceText(String tableName, String columnName, String oldValue, String newValue) {
        if (tableExists(tableName) && columnExists(tableName, columnName)) {
            jdbcTemplate.update("UPDATE `" + tableName + "` SET `" + columnName + "` = REPLACE(`" + columnName + "`, ?, ?) WHERE `" + columnName + "` LIKE ?", oldValue, newValue, "%" + oldValue + "%");
        }
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

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = ?
              AND COLUMN_NAME = ?
            """, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }
}

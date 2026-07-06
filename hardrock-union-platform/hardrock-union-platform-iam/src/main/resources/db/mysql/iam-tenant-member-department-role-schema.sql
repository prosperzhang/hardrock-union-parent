CREATE TABLE IF NOT EXISTS iam_tenant_member_department_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    department_id BIGINT NOT NULL DEFAULT 0 COMMENT '部门ID 0表示未指定部门',
    role_id BIGINT NOT NULL DEFAULT 0 COMMENT '角色ID 0表示仅部门关系',
    primary_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否主部门 1是 0否',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_iam_tenant_member_department_role (app_id, tenant_id, user_id, department_id, role_id),
    KEY idx_iam_tenant_member_department_role_user (app_id, tenant_id, user_id),
    KEY idx_iam_tenant_member_department_role_department (app_id, tenant_id, department_id),
    KEY idx_iam_tenant_member_department_role_role (app_id, tenant_id, role_id)
) COMMENT='IAM租户成员-部门-角色关系表';

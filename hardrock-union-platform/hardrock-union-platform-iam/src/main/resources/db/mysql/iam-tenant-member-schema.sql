CREATE TABLE IF NOT EXISTS iam_tenant_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    app_id BIGINT NOT NULL DEFAULT 0 COMMENT '应用ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    member_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '成员状态',
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主租户 1是 0否',
    joined_at DATETIME DEFAULT NULL COMMENT '加入时间',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_iam_tenant_member (app_id, tenant_id, user_id),
    KEY idx_iam_tenant_member_app_user (app_id, user_id, member_status),
    KEY idx_iam_tenant_member_app_tenant (app_id, tenant_id, member_status),
    KEY idx_iam_tenant_member_app_user_primary (app_id, user_id, is_primary)
) COMMENT='IAM租户成员表';

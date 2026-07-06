CREATE TABLE IF NOT EXISTS iam_tenant_join_request (
    id BIGINT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL DEFAULT 0 COMMENT '应用ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    user_id BIGINT NOT NULL COMMENT '申请用户ID',
    request_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态 PENDING/APPROVED/REJECTED/CANCELLED',
    apply_message VARCHAR(255) DEFAULT NULL COMMENT '申请说明',
    reviewed_by BIGINT DEFAULT NULL COMMENT '审批人',
    reviewed_at DATETIME DEFAULT NULL COMMENT '审批时间',
    review_remark VARCHAR(255) DEFAULT NULL COMMENT '审批意见',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_iam_tenant_join_request_tenant (app_id, tenant_id, request_status),
    KEY idx_iam_tenant_join_request_user (app_id, user_id, request_status)
) COMMENT='IAM租户加入申请表';

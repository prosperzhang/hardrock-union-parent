CREATE TABLE IF NOT EXISTS wsgm_customer_follow_up (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    follow_up_type VARCHAR(32) NOT NULL DEFAULT 'PHONE' COMMENT '跟进方式',
    follow_up_content VARCHAR(1000) NOT NULL COMMENT '跟进内容',
    next_action VARCHAR(255) NULL COMMENT '下一步动作',
    next_follow_up_at DATETIME NULL COMMENT '下次跟进时间',
    created_by BIGINT NULL COMMENT '创建人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_wsgm_follow_up_customer (customer_id),
    KEY idx_wsgm_follow_up_tenant (tenant_id)
) COMMENT='总部客户跟进记录表';

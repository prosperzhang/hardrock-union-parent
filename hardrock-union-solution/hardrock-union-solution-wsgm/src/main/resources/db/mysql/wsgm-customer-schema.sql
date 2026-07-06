CREATE TABLE IF NOT EXISTS wsgm_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    customer_name VARCHAR(128) NOT NULL COMMENT '客户名称',
    contact_name VARCHAR(64) NULL COMMENT '联系人',
    contact_phone VARCHAR(32) NULL COMMENT '联系电话',
    level_code VARCHAR(32) NOT NULL DEFAULT 'A' COMMENT '客户等级',
    source_code VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT '客户来源',
    remark VARCHAR(500) NULL COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_by BIGINT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_wsgm_customer_tenant_name_phone (tenant_id, customer_name, contact_phone),
    KEY idx_wsgm_customer_tenant (tenant_id),
    KEY idx_wsgm_customer_name (customer_name)
) COMMENT='总部客户表';

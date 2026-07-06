CREATE TABLE IF NOT EXISTS wsgm_opportunity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    opportunity_name VARCHAR(128) NOT NULL COMMENT '商机名称',
    stage_code VARCHAR(32) NOT NULL DEFAULT 'INITIAL' COMMENT '商机阶段',
    expected_amount DECIMAL(18,2) NULL COMMENT '预计金额',
    expected_sign_date DATE NULL COMMENT '预计签约日期',
    remark VARCHAR(500) NULL COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1有效 0关闭',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    created_by BIGINT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_wsgm_opportunity_customer (customer_id),
    KEY idx_wsgm_opportunity_tenant (tenant_id)
) COMMENT='总部客户商机表';

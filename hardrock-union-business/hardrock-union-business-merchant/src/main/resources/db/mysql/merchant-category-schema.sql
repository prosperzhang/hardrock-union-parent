CREATE TABLE IF NOT EXISTS merchant_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    category_code VARCHAR(64) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级分类ID',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_merchant_category_tenant_code (tenant_id, category_code)
) COMMENT='商户商品分类表';

CREATE TABLE IF NOT EXISTS nexis_site (
    id BIGINT PRIMARY KEY COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    site_name VARCHAR(128) NOT NULL COMMENT '工地名称',
    project_name VARCHAR(128) DEFAULT NULL COMMENT '项目名称',
    site_address VARCHAR(255) DEFAULT NULL COMMENT '工地地址',
    manager_name VARCHAR(64) DEFAULT NULL COMMENT '负责人',
    manager_phone VARCHAR(32) DEFAULT NULL COMMENT '负责人电话',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_nexis_site_tenant_name (tenant_id, site_name)
) COMMENT='Nexis工地表';

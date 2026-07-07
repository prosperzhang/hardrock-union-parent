CREATE TABLE IF NOT EXISTS project_participant_company (
    id BIGINT PRIMARY KEY COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '所属租户ID',
    bind_tenant_id BIGINT DEFAULT NULL COMMENT '绑定平台租户ID，为空表示外部单位',
    company_name VARCHAR(128) NOT NULL COMMENT '参建单位名称',
    company_code VARCHAR(64) DEFAULT NULL COMMENT '参建单位编码',
    company_type VARCHAR(32) NOT NULL COMMENT '单位类型 GENERAL_CONTRACTOR/SPECIALTY_CONTRACTOR/LABOR_CONTRACTOR/SUPPLIER/OWNER/SUPERVISOR',
    contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_project_participant_company_tenant_name (tenant_id, company_name),
    KEY idx_project_participant_company_bind_tenant (bind_tenant_id),
    KEY idx_project_participant_company_type (tenant_id, company_type, status)
) COMMENT='Nexis参建单位表';

CREATE TABLE IF NOT EXISTS project_participant (
    id BIGINT PRIMARY KEY COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '所属租户ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    participant_company_id BIGINT NOT NULL COMMENT '参建单位ID',
    participant_role VARCHAR(32) NOT NULL COMMENT '项目角色 GENERAL_CONTRACTOR/SPECIALTY_CONTRACTOR/LABOR_CONTRACTOR/SUPPLIER',
    is_lead TINYINT NOT NULL DEFAULT 0 COMMENT '是否主责单位 1是 0否',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_project_participant_unique (project_id, participant_company_id, participant_role),
    KEY idx_project_participant_tenant_project (tenant_id, project_id, status),
    KEY idx_project_participant_company (participant_company_id)
) COMMENT='Nexis项目参建单位关系表';

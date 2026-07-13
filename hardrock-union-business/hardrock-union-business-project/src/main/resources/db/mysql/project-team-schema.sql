CREATE TABLE IF NOT EXISTS project_team (
    id BIGINT PRIMARY KEY COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '所属租户ID',
    site_id BIGINT DEFAULT NULL COMMENT '标段/工地ID，可为空',
    participant_company_id BIGINT NOT NULL COMMENT '参建单位ID',
    work_scope_id BIGINT DEFAULT NULL COMMENT '施工范围ID',
    team_name VARCHAR(128) NOT NULL COMMENT '班组名称',
    team_code VARCHAR(64) DEFAULT NULL COMMENT '班组编码',
    leader_name VARCHAR(64) DEFAULT NULL COMMENT '班组长姓名',
    leader_phone VARCHAR(32) DEFAULT NULL COMMENT '班组长手机号',
    leader_user_id BIGINT DEFAULT NULL COMMENT '班组长 Nexis 用户ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_project_team_project_company_name (tenant_id, participant_company_id, team_name),
    KEY idx_project_team_tenant_site (tenant_id, site_id, status),
    KEY idx_project_team_company (participant_company_id),
    KEY idx_project_team_scope (work_scope_id),
    KEY idx_project_team_leader_user (tenant_id, leader_user_id)
) COMMENT='Nexis班组表';

ALTER TABLE project_team
    ADD COLUMN IF NOT EXISTS leader_user_id BIGINT DEFAULT NULL COMMENT '班组长 Nexis 用户ID' AFTER leader_phone;

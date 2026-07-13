CREATE TABLE IF NOT EXISTS iam_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    app_id BIGINT NOT NULL DEFAULT 0 COMMENT '应用ID',
    app_code VARCHAR(32) NOT NULL DEFAULT 'WSGM' COMMENT '应用编码',
    dept_code VARCHAR(64) NOT NULL COMMENT '部门编码',
    dept_name VARCHAR(64) NOT NULL COMMENT '部门名称',
    dept_short_name VARCHAR(32) NULL COMMENT '部门简称',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级部门ID',
    dept_type VARCHAR(32) NOT NULL DEFAULT 'GENERAL' COMMENT '部门类型',
    workspace_scope VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '适用空间：ALL/GROUP/COMPANY/ORGANIZATION/PROJECT',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_iam_department_app_code (app_id, dept_code),
    KEY idx_iam_department_app_parent (app_id, parent_id)
) COMMENT='IAM部门表';

ALTER TABLE iam_department
    ADD COLUMN IF NOT EXISTS workspace_scope VARCHAR(32) NOT NULL DEFAULT 'ALL'
    COMMENT '适用空间：ALL/GROUP/COMPANY/ORGANIZATION/PROJECT' AFTER dept_type;

CREATE TABLE IF NOT EXISTS iam_department_role_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    app_id BIGINT NOT NULL DEFAULT 0 COMMENT '应用ID',
    app_code VARCHAR(32) NOT NULL DEFAULT 'WSGM' COMMENT '应用编码',
    department_id BIGINT NOT NULL COMMENT '部门ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_iam_department_role_option_app_dept_role (app_id, department_id, role_id),
    KEY idx_iam_department_role_option_app_dept (app_id, department_id)
) COMMENT='IAM部门角色选项表';

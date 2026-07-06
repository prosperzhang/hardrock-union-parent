CREATE TABLE IF NOT EXISTS iam_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    app_id BIGINT NOT NULL DEFAULT 0 COMMENT '应用ID',
    app_code VARCHAR(32) NOT NULL DEFAULT 'WSGM' COMMENT '应用编码',
    permission_code VARCHAR(128) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(32) NOT NULL DEFAULT 'API' COMMENT '权限类型 MENU/BUTTON/API/DATA',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级权限ID',
    permission_path VARCHAR(255) DEFAULT NULL COMMENT '前端路径或资源路径',
    http_method VARCHAR(16) DEFAULT NULL COMMENT 'HTTP方法',
    component VARCHAR(255) DEFAULT NULL COMMENT '前端组件标识',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_iam_permission_app_code (app_id, permission_code),
    KEY idx_iam_permission_app_parent (app_id, parent_id),
    KEY idx_iam_permission_app_type (app_id, permission_type)
) COMMENT='IAM权限表';

CREATE TABLE IF NOT EXISTS iam_department_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    app_id BIGINT NOT NULL DEFAULT 0 COMMENT '应用ID',
    app_code VARCHAR(32) NOT NULL DEFAULT 'WSGM' COMMENT '应用编码',
    department_id BIGINT NOT NULL COMMENT '部门ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_iam_dept_role_permission (app_id, department_id, role_id, permission_id),
    KEY idx_iam_dept_role_permission_role (app_id, department_id, role_id)
) COMMENT='IAM部门角色权限关系表';

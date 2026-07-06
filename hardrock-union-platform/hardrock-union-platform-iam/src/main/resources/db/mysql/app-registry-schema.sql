CREATE TABLE IF NOT EXISTS app_registry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    app_code VARCHAR(32) NOT NULL COMMENT '应用编码',
    app_name VARCHAR(64) NOT NULL COMMENT '应用名称',
    app_type VARCHAR(32) NOT NULL DEFAULT 'SAAS_PLATFORM' COMMENT '应用类型',
    home_path VARCHAR(128) DEFAULT NULL COMMENT '首页路径',
    login_path VARCHAR(128) DEFAULT NULL COMMENT '登录路径',
    icon VARCHAR(128) DEFAULT NULL COMMENT '图标',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_app_registry_code (app_code),
    KEY idx_app_registry_status_sort (status, sort_no)
) COMMENT='App注册表';

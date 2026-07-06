CREATE TABLE IF NOT EXISTS sys_region (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    code VARCHAR(20) NOT NULL COMMENT '行政区划编码',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    parent_code VARCHAR(20) NOT NULL DEFAULT '0' COMMENT '父级行政区划编码',
    level TINYINT NOT NULL COMMENT '级别 1省级 2市级 3区县级',
    full_name VARCHAR(255) DEFAULT NULL COMMENT '完整名称',
    latitude DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    longitude DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_sys_region_code (code),
    KEY idx_sys_region_parent_code (parent_code),
    KEY idx_sys_region_level (level)
) COMMENT='行政区域表';

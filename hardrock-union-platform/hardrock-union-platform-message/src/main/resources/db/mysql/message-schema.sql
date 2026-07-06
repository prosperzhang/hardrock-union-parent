CREATE TABLE IF NOT EXISTS message_thread (
    id BIGINT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    app_code VARCHAR(32) NOT NULL COMMENT '应用编码',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    thread_type VARCHAR(32) NOT NULL COMMENT '主题类型 SYSTEM/BUSINESS/APPROVAL/CHAT/COMMENT',
    source_type VARCHAR(64) DEFAULT NULL COMMENT '来源业务类型',
    source_id BIGINT DEFAULT NULL COMMENT '来源业务ID',
    title VARCHAR(128) NOT NULL COMMENT '主题标题',
    thread_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '主题状态',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_message_thread_tenant (app_id, tenant_id, thread_type, id),
    KEY idx_message_thread_source (app_id, tenant_id, source_type, source_id)
) COMMENT='消息主题表';

CREATE TABLE IF NOT EXISTS message_record (
    id BIGINT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    app_code VARCHAR(32) NOT NULL COMMENT '应用编码',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    thread_id BIGINT NOT NULL COMMENT '消息主题ID',
    message_type VARCHAR(32) NOT NULL COMMENT '消息类型 SYSTEM/BUSINESS/APPROVAL/CHAT/COMMENT',
    sender_user_id BIGINT DEFAULT NULL COMMENT '发送人ID',
    sender_name VARCHAR(64) DEFAULT NULL COMMENT '发送人名称',
    title VARCHAR(128) NOT NULL COMMENT '消息标题',
    content VARCHAR(1000) DEFAULT NULL COMMENT '消息内容',
    source_type VARCHAR(64) DEFAULT NULL COMMENT '来源业务类型',
    source_id BIGINT DEFAULT NULL COMMENT '来源业务ID',
    action_url VARCHAR(255) DEFAULT NULL COMMENT '跳转地址',
    record_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '消息状态',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_message_record_thread (app_id, tenant_id, thread_id, id),
    KEY idx_message_record_source (app_id, tenant_id, source_type, source_id),
    KEY idx_message_record_created (app_id, tenant_id, created_at)
) COMMENT='消息明细表';

CREATE TABLE IF NOT EXISTS message_recipient (
    id BIGINT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    app_code VARCHAR(32) NOT NULL COMMENT '应用编码',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    thread_id BIGINT NOT NULL COMMENT '消息主题ID',
    record_id BIGINT NOT NULL COMMENT '消息明细ID',
    receiver_user_id BIGINT NOT NULL COMMENT '接收人ID',
    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读 1是 0否',
    read_at DATETIME DEFAULT NULL COMMENT '已读时间',
    recipient_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '接收状态',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_message_recipient_record_user (app_id, record_id, receiver_user_id),
    KEY idx_message_recipient_inbox (app_id, tenant_id, receiver_user_id, read_flag, id),
    KEY idx_message_recipient_thread (app_id, tenant_id, thread_id, receiver_user_id)
) COMMENT='消息接收人表';

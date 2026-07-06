CREATE TABLE IF NOT EXISTS warehouse_stock (
    id BIGINT PRIMARY KEY COMMENT '主键',
    app_code VARCHAR(32) NOT NULL COMMENT '应用编码 NEXIS/PRIMELOAD-MARKETPLACE',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    warehouse_id BIGINT NOT NULL COMMENT '数字仓库ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(128) NOT NULL COMMENT '产品名称',
    sku_code VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    unit VARCHAR(32) DEFAULT NULL COMMENT '单位',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_warehouse_stock_product (app_code, tenant_id, warehouse_id, product_id),
    KEY idx_warehouse_stock_warehouse (app_code, tenant_id, warehouse_id),
    KEY idx_warehouse_stock_product (app_code, tenant_id, product_id)
) COMMENT='数字仓库产品库存表';

CREATE TABLE IF NOT EXISTS warehouse_stock_io_order (
    id BIGINT PRIMARY KEY COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    warehouse_id BIGINT NOT NULL COMMENT '数字仓库ID',
    warehouse_name VARCHAR(128) NOT NULL COMMENT '仓库名称',
    order_no VARCHAR(64) NOT NULL COMMENT '出入库单号',
    order_type VARCHAR(16) NOT NULL COMMENT '单据类型 IN/OUT',
    order_status VARCHAR(32) NOT NULL COMMENT '单据状态',
    item_count INT NOT NULL DEFAULT 0 COMMENT '明细数量',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_warehouse_stock_io_order_no (tenant_id, order_no),
    KEY idx_warehouse_stock_io_order_warehouse (tenant_id, warehouse_id),
    KEY idx_warehouse_stock_io_order_status (tenant_id, order_status)
) COMMENT='仓储出入库单表';

CREATE TABLE IF NOT EXISTS warehouse_stock_io_order_item (
    id BIGINT PRIMARY KEY COMMENT '主键',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    order_id BIGINT NOT NULL COMMENT '出入库单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    quantity INT NOT NULL COMMENT '数量',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0未删 1已删',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_warehouse_stock_io_order_item_order (tenant_id, order_id),
    KEY idx_warehouse_stock_io_order_item_product (tenant_id, product_id)
) COMMENT='仓储出入库单明细表';

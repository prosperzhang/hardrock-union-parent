# Nexis 当前数据库表清单

盘点时间：2026-07-11
数据库：`hardrock_db`
数据来源：MySQL 主库 `information_schema` 与实时 `COUNT(*)`
总表数：44

## 一、平台与 IAM（14 张）

| 数据表 | 行数 | 用途 | Nexis 关系 |
|---|---:|---|---|
| `app_registry` | 6 | 注册 NEXIS、WSGM、商城、物流等应用 | 平台共享 |
| `ark_uid_worker_node` | 8 | 分布式 ID 生成器节点记录 | 基础设施 |
| `iam_department` | 35 | 应用级部门模板及空间适用范围 | Nexis 组织核心 |
| `iam_department_role_option` | 74 | 部门允许分配哪些角色 | Nexis 组织核心 |
| `iam_department_role_permission` | 153 | 部门角色与权限的绑定 | Nexis 权限核心 |
| `iam_permission` | 66 | 菜单和 API 权限定义 | 平台共享 |
| `iam_role` | 74 | 各应用角色定义 | 平台共享 |
| `iam_tenant_join_request` | 0 | 用户申请加入公司或项目 | Nexis 成员流程 |
| `iam_tenant_member` | 28 | 用户与租户空间的成员关系 | Nexis 成员核心 |
| `iam_tenant_member_department_role` | 29 | 成员在某空间内的部门和角色 | Nexis 权限核心 |
| `iam_user` | 6 | 登录账号 | 平台共享 |
| `iam_user_info` | 12 | 用户昵称、头像等个人资料 | 平台共享 |
| `sys_region` | 5814 | 省市区行政区域字典 | 平台共享 |
| `tenant_registry` | 27 | 公司、项目空间及公司项目关系 | Nexis 组织核心 |

## 二、Nexis 项目业务（10 张）

| 数据表 | 行数 | 用途 | 当前层级 |
|---|---:|---|---|
| `project_external_link` | 2 | 外部上级单位、外部项目认领和正式关联 | 项目 |
| `project_participant` | 1 | 项目与参建单位的关系记录 | 项目 |
| `project_participant_company` | 9 | 总包、专业分包、劳务分包等参建单位主数据 | 项目 |
| `project_site` | 1 | 项目下标段或工地 | 项目 |
| `project_site_participant` | 0 | 标段与参建单位关系 | 标段 |
| `project_site_work_scope` | 0 | 参建单位在标段内的施工范围 | 标段/参建单位 |
| `project_team` | 8 | 班组及班组长账号绑定 | 班组 |
| `project_worker` | 5 | 工人实名和班组归属 | 工人 |
| `project_worker_entry` | 2 | 工人实名进场、进场、退场记录 | 工人 |
| `project_worker_attendance` | 2 | 工人签到、签退和考勤记录 | 工人 |

当前项目业务链路：

```text
tenant_registry(PROJECT)
  -> project_participant_company
  -> project_site
  -> project_site_work_scope
  -> project_team
  -> project_worker
  -> project_worker_entry
  -> project_worker_attendance
```

## 三、消息与工作流（4 张）

| 数据表 | 行数 | 用途 | Nexis 关系 |
|---|---:|---|---|
| `message_thread` | 0 | 消息主题或会话 | 平台共享，尚未使用 |
| `message_record` | 0 | 消息正文 | 平台共享，尚未使用 |
| `message_recipient` | 0 | 消息接收人与已读状态 | 平台共享，尚未使用 |
| `workflow_task` | 0 | 审批、待办和工作流任务 | Nexis 未来审批，尚未使用 |

## 四、商城与物流（8 张）

| 数据表 | 行数 | 用途 | Nexis 关系 |
|---|---:|---|---|
| `logistics_shipment_record` | 4 | 供应链发货记录 | 非 Nexis 项目核心 |
| `merchant_category` | 0 | 商城商品分类 | 一车好料/商城 |
| `merchant_product` | 0 | 商城物料商品 | 一车好料/商城 |
| `merchant_product_region_price` | 0 | 商品区域价格 | 一车好料/商城 |
| `merchant_quotation` | 0 | 报价单主表 | 一车好料/商城 |
| `merchant_quotation_item` | 0 | 报价单明细 | 一车好料/商城 |
| `merchant_order` | 0 | 商城订单主表 | 一车好料/商城 |
| `merchant_order_item` | 0 | 商城订单明细 | 一车好料/商城 |

## 五、仓储（5 张）

| 数据表 | 行数 | 用途 | Nexis 关系 |
|---|---:|---|---|
| `warehouse_registry` | 15 | 数字仓库注册信息 | 仓储应用 |
| `warehouse_stock` | 0 | 仓库商品库存 | 仓储应用 |
| `warehouse_inventory_record` | 0 | 库存流水 | 仓储应用 |
| `warehouse_stock_io_order` | 2 | 出入库单主表 | 仓储应用 |
| `warehouse_stock_io_order_item` | 2 | 出入库单明细 | 仓储应用 |

## 六、WSGM 总部 CRM（3 张）

| 数据表 | 行数 | 用途 | Nexis 关系 |
|---|---:|---|---|
| `wsgm_customer` | 0 | 总部客户 | 非 Nexis |
| `wsgm_customer_follow_up` | 0 | 客户跟进记录 | 非 Nexis |
| `wsgm_opportunity` | 0 | 客户商机 | 非 Nexis |

## 当前结论

1. Nexis 直接依赖平台/IAM 14 张表中的大部分，以及 `project_*` 10 张表。
2. 商城、物流、仓储、WSGM 共 16 张业务表与 Nexis 项目管理并非同一业务域。
3. 消息和工作流 4 张表目前均为空，只是预留基础设施。
4. `hardrock_db` 当前是多应用共享数据库，不是纯 Nexis 数据库。
5. 下一步应按“平台共享、Nexis、商城供应链、WSGM”建立清晰的数据归属边界，再决定是否物理拆库。

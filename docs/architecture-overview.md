# Hardrock Union 架构总览

## 1. 系统定位

`hardrock-union` 不是一个“单体 SaaS + 几个功能模块”的系统，而是一组面向不同业务场景的 app 产品族。

当前核心 app：

- `wsgm`：集团总部内部使用的管理系统。
- `pmhub`：面向施工企业、项目部、项目现场的项目管理 SaaS。
- `primeload-marketplace`：面向五金建材商家的商城/交易 SaaS。

当前租户定位：

- `pmhub` 是多租户 SaaS，当前核心模型是：一个项目就是一个租户。
- `primeload-marketplace` 是多租户 SaaS，当前核心模型是：一个商家就是一个租户。
- `wsgm` 是集团自营内部系统，可以先按总部默认组织理解，后续再扩展内部组织模型。

架构上要始终记住一件事：系统不是只服务一个 app，而是服务多个 app。用户、租户、部门、角色、权限、仓储、物流都要为未来多 app 复用留出位置。

## 2. 分层模型

项目按四层理解：

- `framework`：通用应用框架能力，例如统一返回、异常处理、安全上下文、Web 配置。
- `infrastructure`：技术基础设施能力，例如数据库基础层、ID、缓存、文件存储、MQ。
- `platform`：跨 app 复用的业务平台能力，例如 IAM、租户、文件、流程、日志。
- `business`：可被多个 app 复用的业务域能力，例如项目、商家、仓储、物流。
- `solution`：具体 app 的组装层，例如 `wsgm`、`pmhub`、`primeload-marketplace`。

推荐依赖方向：

```text
solution -> business -> platform -> infrastructure -> framework
```

不推荐反向依赖。比如 `warehouse` 不应该依赖 `merchant` 或 `project`，否则以后会很容易形成循环依赖。

## 3. 模块职责

### 3.1 framework

- `hardrock-union-framework-core`：基础领域对象、统一返回、基础异常。
- `hardrock-union-framework-web`：Web 异常处理、MVC 通用配置。
- `hardrock-union-framework-security`：JWT 解析、登录用户上下文、请求拦截、参数注入。

### 3.2 infrastructure

- `hardrock-union-infra-db`：MyBatis Plus 配置、基础实体、自动填充、分页、逻辑删除约定。
- `hardrock-union-infra-id`：ID 生成抽象和实现。
- `hardrock-union-infra-cache`：Redis key 约定和缓存工具。
- `hardrock-union-infra-storage`：文件存储抽象。
- `hardrock-union-infra-mq`：事件发布和消息集成抽象。

### 3.3 platform

- `hardrock-union-platform-iam`：用户、登录、app、部门、角色、权限、租户成员关系。
- `hardrock-union-platform-tenant`：app 级租户注册、租户生命周期、租户状态、租户隔离元数据。
- `hardrock-union-platform-file`：跨 app 复用的文件元数据能力。
- `hardrock-union-platform-workflow`：跨 app 复用的流程能力。
- `hardrock-union-platform-log`：操作日志、审计日志。
- `hardrock-union-platform-message`：跨 app 复用的消息中心，包括即时聊天、系统通知、审批提醒、业务状态通知和协作评论。

### 3.4 business

- `hardrock-union-business-project`：项目、工地、施工协作等可复用项目域能力。
- `hardrock-union-business-merchant`：商家、商品、报价、订单、交易履约等商家域能力。
- `hardrock-union-business-warehouse`：仓库、库存、锁库、出库、入库、库存流水等仓储域能力。
- `hardrock-union-business-logistics`：发货、运单、物流轨迹、签收、物流异常等物流域能力。

### 3.5 solution

- `hardrock-union-solution-wsgm`：集团总部 app 组装层。
- `hardrock-union-solution-pmhub`：PMHub app 组装层，组合 IAM、租户、项目业务能力。
- `hardrock-union-solution-primeload-marketplace`：PRIMELOAD-MARKETPLACE app 组装层，组合 IAM、租户、商家业务能力。

`solution-*` 应该尽量轻，主要负责 app 入口、聚合接口、看板、编排，不应该沉淀大量可复用核心业务。

## 4. App 和租户模型

租户模型必须是 app-aware，也就是所有租户都要知道自己属于哪个 app。

核心概念：

- `app`：应用系统身份，例如 `wsgm`、`pmhub`、`primeload-marketplace`，未来也可以有 `warehouse`、`logistics`。
- `tenant`：某个 app 下的组织、项目、商户或业务主体。
- `user`：登录账号。用户可以通过成员关系加入多个租户。

当前主表方向：

- `app_registry`：app 主数据表，保存 app 名称、编码、类型、入口、状态等。
- `tenant_registry`：租户主表，保存某个 app 下的租户。
- `iam_user`：账号表，只表达登录账号本身。
- `iam_user_info`：用户资料表，例如昵称、头像等。
- `iam_tenant_member`：用户属于哪个租户的成员关系表。

关系约定：

- 一个 app 有多个租户。
- 一个租户有多个成员。
- 一个用户可以通过 `iam_tenant_member` 加入多个租户。
- 用户不应该被强行塞进默认租户。注册账号成功后，如果没有租户，只表示账号可登录但还不能进入具体业务系统。

当前例子：

- `pmhub`：`tenant = project`，也就是项目即租户。
- `primeload-marketplace`：`tenant = merchant`，也就是商家即租户。
- `wsgm`：可以先有总部默认租户，后续再细化组织模型。

## 5. 租户数据方向

`tenant_registry` 是 app-aware 的租户注册表。租户编码应该在同一个 app 内唯一，不要求全平台唯一。

建议核心字段：

- `id`
- `app_id`
- `tenant_code`
- `tenant_name`
- `tenant_type`
- `status`
- `contact_name`
- `contact_phone`
- `admin_user_id`
- `deleted`
- `created_at`
- `updated_at`

建议 app：

- `WSGM`
- `PMHUB`
- `PRIMELOAD-MARKETPLACE`
- `WAREHOUSE`
- `LOGISTICS`

建议租户类型：

- `HEADQUARTERS`：总部。
- `PROJECT`：项目。
- `MERCHANT`：商家。
- `WAREHOUSE_OPERATOR`：仓储运营方。
- `LOGISTICS_OPERATOR`：物流运营方。

## 6. IAM 方向

`iam` 是跨 app 的身份和权限中心，不应该只为 PMHub 或 PRIMELOAD-MARKETPLACE 服务。

当前方向：

- app 由 `app_registry` 表统一管理，不再只靠硬编码字符串。
- 用户账号和租户成员关系分离。
- 用户可以通过 `iam_tenant_member` 加入多个租户。
- 部门和角色通过 `iam_tenant_member_department_role` 表形成用户在某个租户内的有效组织身份。
- 部门角色目录由 `iam_department_role` 管理。
- 权限应该成为角色下面的具体能力层，而不是继续依赖硬编码字符串。
- 消息、通知、提醒、评论属于 `platform-message`，按 `app + tenant` 隔离和分发。

推荐 IAM 层级：

```text
app
tenant
department
user
role
permission
```

推荐登录理解：

```text
app + username + password -> 账号登录成功
账号选择或创建 tenant -> 进入具体业务系统
tenant member + department role + permission -> 决定能访问什么
```

PMHub 当前运行模型：

- `tenant_registry`：项目主表，当前 `tenant = project`。
- `iam_tenant_member`：项目成员关系。
- `iam_tenant_member_department_role`：成员在项目内的部门和角色。
- `iam_department_role`：部门角色目录。
- `iam_tenant_join_request`：加入项目申请。

PMHub 主流程中已经废弃：

- `pmhub_project`
- `pmhub_project_member`
- `pmhub_project_member_role`
- `pmhub_project_join_request`
- `iam_user_role`
- `iam_user_department`

## 7. 消息中心方向

`hardrock-union-platform-message` 是跨 app 的消息中心，不属于某一个具体业务域。

它要支持的场景：

- 同一个 `app + tenant` 内的成员即时聊天。
- 不同 `app + tenant` 的业务参与方，围绕询价、订单、发货、售后等业务单据即时聊天。
- 系统通知。
- 审批提醒。
- 项目协作评论。
- 订单状态通知。
- 物流状态通知。

消息中心必须支持两类会话：

```text
INTERNAL
  租户内部会话。
  同一个 app + 同一个 tenant 内的成员，可以互相通信和接收该租户内的消息。

BIZ_CROSS_APP
  跨 app 业务会话。
  不同 app、不同 tenant 的用户，只要他们是同一个业务单据的参与方，就可以围绕这个业务单据通信。
```

例子：

- 一个用户创建了 `pmhub` 项目租户，他是决策部负责人。后续 20 个人加入这个项目后，这 21 个人可以在该项目租户内聊天、收项目通知、审批提醒和协作评论。
- 一个用户创建了 `primeload-marketplace` 商家租户，他是决策部负责人。后续 20 个人加入这个商家后，这 21 个人可以在该商家租户内聊天、收订单状态通知和物流状态通知。
- 一个 `pmhub` 项目租户里的物资部采购员，可以和一个 `primeload-marketplace` 商家租户里的客服部客服员，围绕询价、订单、发货或售后单据发送即时消息。

消息中心不要写死“这是项目还是商家”，但必须保存参与方和业务上下文：

```text
conversation_type

source_app_id
source_tenant_id
source_member_id / source_user_id

target_app_id
target_tenant_id
target_member_id / target_user_id

sender_user_id
receiver_user_id / receiver_member_id

message_type
biz_type
biz_id
```

其中：

- `INTERNAL` 会话主要依赖 `app_id + tenant_id` 做租户内隔离。
- `BIZ_CROSS_APP` 会话主要依赖 `source_app_id/source_tenant_id + target_app_id/target_tenant_id + biz_type/biz_id` 形成业务关系边界。
- 跨 app 消息不是全平台随便互聊，必须有业务上下文，例如询价单、订单、发货单、售后单。
- 用户是否能进入跨 app 会话，后续应该结合 IAM 权限和业务参与方关系判断。

后续可拆成三类核心能力：

- 会话消息：会话、会话成员、聊天消息、已读未读。
- 业务通知：系统通知、审批提醒、订单状态通知、物流状态通知。
- 业务评论：项目协作评论、订单评论、审批评论、物流异常评论。

## 8. 业务边界

### 8.1 WSGM

定位：集团总部内部 CRM 和运营管理系统。

典型领域：

- 客户。
- 跟进。
- 商机。
- 报价。
- 合同。
- 订单协调。
- 结算和对账。

推荐归属：

- 总部强定制能力放在 `hardrock-union-solution-wsgm`。
- 能沉淀为通用能力的内容再抽到 `business-*`。

### 8.2 PMHub

定位：施工项目管理 SaaS。

商业定价方向详见：[PMHub Pricing Strategy](./pmhub-pricing-strategy.md)。

公司/集团与项目层级方向详见：[PMHub Company And Project Model](./pmhub-company-project-model.md)。

典型领域：

- 项目。
- 工地。
- 班组和工人。
- 进度。
- 安全检查。
- 施工过程协作。
- 现场物资和项目仓。

当前边界：

- 项目身份、成员、加入申请、初始化、部门角色分配，归 `platform-tenant` 和 `platform-iam`。
- `hardrock-union-business-project` 聚焦项目业务本身，例如工地、人员、考勤、施工协作、项目侧采购需求。
- 项目侧需要使用 `warehouse` 和 `logistics`，但不应该直接改仓储和物流底层表。

### 8.3 PRIMELOAD-MARKETPLACE / Merchant

定位：五金建材商家 SaaS。

典型领域：

- 商家资料。
- 商品和目录。
- 报价。
- 订单。
- 交易履约。
- 商家仓。
- 发货。

当前边界：

- 商家身份由 `tenant_registry` 表达，当前 `tenant = merchant`。
- `hardrock-union-business-merchant` 聚焦商家、商品、报价、订单和交易履约。
- 商家侧需要使用 `warehouse` 扣库存、出库，也需要使用 `logistics` 发货。

入口流程详见：[PRIMELOAD-MARKETPLACE Merchant Onboarding Flow](./primeload-marketplace-merchant-onboarding-flow.md)。

Merchant 第一阶段推进清单详见：[PRIMELOAD-MARKETPLACE Merchant Phase 1 Plan](./primeload-marketplace-merchant-phase-1-plan.md)。

## 9. Warehouse 和 Logistics 设计原则

`warehouse` 和 `logistics` 不是 `merchant` 的附属模块，也不是 `project` 的附属模块。它们应该是可独立成 app 的业务域能力。

当前可以先作为 `business` 模块被 `project` 和 `merchant` 调用。未来可以继续演进为：

- `hardrock-union-solution-warehouse`
- `hardrock-union-solution-logistics`

核心原则：

- `warehouse` 负责仓库、库存、锁库、出库、入库、库存流水。
- `logistics` 负责发货、运单、运输轨迹、签收、异常、作废。
- `warehouse` 和 `logistics` 不应该写死 `primeload-marketplace`、`pmhub`、`merchant`、`project` 这类上层 app 语义。
- `merchant` 和 `project` 可以调用 `warehouse`、`logistics`，但不能反向依赖。
- `merchant` 不直接改库存表。
- `project` 不直接改物流表。

仓库归属不是固定属于商家或项目，而是属于“某个 app 下的某个租户”。

推荐仓库归属模型：

```text
warehouse
owner_app_id
owner_tenant_id
```

这样可以表达：

- `merchant app + 商家租户 = 商家仓`
- `pmhub/project app + 项目租户 = 项目仓`
- `warehouse app + 仓储租户 = 第三方仓`

推荐后续核心表方向：

```text
warehouse
  id
  owner_app_id
  owner_tenant_id
  warehouse_code
  warehouse_name
  warehouse_type
  address
  status

warehouse_stock
  id
  warehouse_id
  owner_app_id
  owner_tenant_id
  item_id
  item_type
  quantity_available
  quantity_locked

warehouse_stock_order
  id
  order_type
  owner_app_id
  owner_tenant_id
  warehouse_id
  source_app_id
  source_tenant_id
  source_type
  source_id
  status
```

其中 `owner_app_id + owner_tenant_id` 表示仓库或库存属于谁；`source_app_id + source_tenant_id + source_type + source_id` 表示这次库存变化来自哪个业务单据。

## 10. 跨 App 下单和履约链路

我们当前确认的目标流程：

```text
project 在 merchant 下单
merchant 接单
merchant 在 warehouse 扣库存/出库
warehouse 调用 logistics 发货
logistics 把货送到 project 的 warehouse
project 的 warehouse 入库
```

更完整的状态流可以理解为：

```text
merchant_order: CREATED
warehouse_outbound: LOCKED -> OUTBOUND
logistics_shipment: CREATED -> SHIPPED -> DELIVERED
warehouse_inbound: PENDING -> RECEIVED
merchant_order: COMPLETED
```

这个流程里各模块职责：

- `project`：提出采购需求、下单、收货、形成项目仓库存。
- `merchant`：管理商品、报价、订单、交易履约。
- `warehouse`：校验库存、锁库、扣库存、出库、入库、库存流水。
- `logistics`：创建发货单、生成运单、记录运输状态、签收或异常。

这个链路的关键不是“谁直接改谁的表”，而是“谁负责哪个业务事实”。

推荐调用规则：

```text
project -> merchant：下单
merchant -> warehouse：锁库、扣库存、创建出库
warehouse -> logistics：创建发货
logistics -> warehouse：送达后触发目标仓入库
warehouse -> project：项目仓库存已入库，可用于项目侧业务
```

## 11. 当前代码状态

当前已经对齐的方向：

- `framework` 基础路径可用。
- `infra-db` 已经有基础形态。
- `platform-iam` 和 `platform-tenant` 已经可以作为初始平台底座。
- `app_registry` 已经进入平台基础模型。
- 租户成员关系由 `iam_tenant_member` 表达。
- 租户内部门角色由 `iam_tenant_member_department_role` 表达。
- `solution-pmhub` 和 `solution-primeload-marketplace` 应保持轻量，主要做 app 入口和编排。
- PMHub 的项目身份已经向 `tenant = project` 收口。
- PRIMELOAD-MARKETPLACE 的商家身份应该继续向 `tenant = merchant` 收口。
- `business-supply` 已拆分为 `business-warehouse` 和 `business-logistics`。
- `business-chat` 已调整为 `platform-message`，作为跨 app 的消息中心。

## 12. 后续重构顺序

推荐顺序：

1. 继续保持 `tenant_registry` 作为 app-aware 的租户注册表。
2. 继续保持 IAM 成员、部门、角色、权限统一落在 `platform-iam`。
3. 让 `solution-*` 继续轻量化，只负责 app 编排和入口。
4. 继续把 PMHub/PRIMELOAD-MARKETPLACE 中可复用的业务沉淀到 `business-*`。
5. 重构 `warehouse`，让仓库支持 `owner_app_id + owner_tenant_id`。
6. 重构 `logistics`，让发货支持来源方和目标方。
7. 再考虑新增独立的 `solution-warehouse` 和 `solution-logistics`。
8. 设计 `platform-message` 的会话、通知、评论基础表。
9. 清理历史兼容代码和过时文档。

## 13. 近期下一步

近期更适合先做：

- 梳理权限模型，把 `menu`、`api` 等权限真正接到业务接口。
- 梳理 `warehouse` 基础表，把“商家仓”和“项目仓”统一到同一套模型。
- 梳理 `logistics` 基础表，让发货能表达从谁发、发给谁、送到哪个目标仓。

不要急着把所有 app 都做满。先把底层身份、租户、权限、仓储、物流的模型打稳，后面的业务扩展会轻很多。

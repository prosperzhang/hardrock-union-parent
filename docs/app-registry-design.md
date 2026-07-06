# App Registry 设计

## 1. 为什么叫 `app_registry`

系统里已经有 `tenant_registry`，所以 app 主数据也应该进入同一套“注册表”命名体系。

`app_registry` 表示系统里有哪些 app，例如：

- `WSGM`
- `PMHUB`
- `PRIMELOAD-MARKETPLACE`
- 未来的 `WAREHOUSE`
- 未来的 `LOGISTICS`

它不是 IAM 自己的内部表，所以不应该叫 `iam_app`。
它也不是 platform 专属概念，所以不应该叫 `platform_app`。

## 2. 核心关系

推荐主链路：

- `app_registry`：定义系统有哪些 app
- `tenant_registry`：定义某个 app 下有哪些租户
- `iam_tenant_member`：定义用户属于哪个 app、哪个 tenant
- `iam_tenant_member_department_role`：定义用户在租户下的部门和角色
- `iam_department_role_permission`：定义部门角色拥有的权限

也就是说：

`app_registry.id`

-> `tenant_registry.app_id`

-> `iam_tenant_member.app_id + tenant_id`

-> `iam_tenant_member_department_role.app_id + tenant_id + user_id`

-> `iam_department_role_permission.app_id + department_id + role_id`

## 3. 表结构

### 3.1 `app_registry`

字段：

- `id`：app ID
- `app_code`：app 编码
- `app_name`：app 名称
- `app_type`：app 类型
- `home_path`：默认首页路径
- `login_path`：默认登录路径
- `icon`：图标
- `sort_no`：排序
- `status`：启用状态
- `description`：描述
- `deleted`：删除标记
- `created_at`：创建时间
- `updated_at`：更新时间

唯一约束：

- `app_code`

## 4. 命名边界

保留字段名：

- `app_id`
- `app_code`

原因是字段表达的是“属于哪个 app”，不是表名。即使主表叫 `app_registry`，其他表通过 `app_id` 指向 app 仍然是清楚的。

不再使用：

- `iam_app`
- `platform_app`

## 5. 接口

App 注册表查询接口：

- `GET /api/app-registry`
- `GET /api/app-registry/{appCode}`

这些接口只负责查询 app 注册信息，不负责租户创建、登录、部门角色、权限分配。

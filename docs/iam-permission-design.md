# IAM 权限设计

## 1. 为什么要做权限

现在部门、角色、租户成员关系已经成型，下一层就应该是权限。

权限用来描述“某个部门下的某个角色到底能看到什么、能做什么”。当前阶段先落 `MENU`，也就是前端菜单权限；后续再逐步扩展 `BUTTON`、`API`、`DATA`。

核心链路固定为：

- `iam_tenant_member`：用户属于哪个租户
- `iam_tenant_member_department_role`：用户在该租户下属于哪个部门、哪个角色
- `iam_department_role_permission`：这个部门角色绑定了哪些权限
- `iam_permission`：权限本身，当前主要是菜单

也就是说，权限不是直接挂在 `role` 上，而是挂在 `department + role` 上。

## 2. 为什么不是 `iam_role_permission`

单独的 `role -> permission` 不适合现在的业务。

原因是同一个角色名称或角色能力，在不同部门下含义可能不同。比如“负责人”不能脱离部门理解，`决策部 + 负责人` 才是完整身份。

所以旧的 `iam_role_permission` 已经不作为业务表使用，改为：

- `iam_department_role_permission`

这张表表达的是：

- 某个 app 下
- 某个部门
- 某个角色
- 拥有哪些权限

## 3. 表结构

### 3.1 `iam_permission`

权限主表。

主要字段：

- `id`：权限 ID
- `app_id`：应用 ID
- `app_code`：应用编码
- `permission_code`：权限编码
- `permission_name`：权限名称
- `permission_type`：权限类型，当前先用 `MENU`
- `parent_id`：上级权限 ID，用来组织菜单树
- `permission_path`：前端路由或资源路径
- `http_method`：API 权限预留字段
- `component`：前端组件标识
- `status`：启用状态
- `sort_no`：排序
- `deleted`：删除标记

### 3.2 `iam_department_role_permission`

部门角色权限关系表。

主要字段：

- `id`：关系 ID
- `app_id`：应用 ID
- `app_code`：应用编码
- `department_id`：部门 ID
- `role_id`：角色 ID
- `permission_id`：权限 ID
- `deleted`：删除标记

唯一约束：

- `app_id + department_id + role_id + permission_id`

这能保证同一个 app 下，同一个部门角色不会重复绑定同一个权限。

## 4. 当前用户权限解析

用户登录后，`auth/me` 应该按下面逻辑生成权限：

1. 根据 `tenant_id` 找到用户所在租户。
2. 从 `iam_tenant_member_department_role` 找到用户在该租户下的部门角色。
3. 用 `department_id + role_id` 去 `iam_department_role_permission` 找权限 ID。
4. 到 `iam_permission` 查询启用且未删除的权限。
5. `permission_type = MENU` 的权限组装成菜单树返回给前端。

管理员角色可以走特殊逻辑：如果是平台管理员或应用管理员，可以返回当前 app 下全部启用菜单。

## 5. 当前接口

权限主数据：

- `GET /api/{appCode}/permissions`
- `GET /api/{appCode}/permissions/tree`
- `GET /api/{appCode}/permissions/{permissionId}`
- `POST /api/{appCode}/permissions`
- `PUT /api/{appCode}/permissions/{permissionId}`
- `DELETE /api/{appCode}/permissions/{permissionId}`

部门角色权限绑定：

- `GET /api/{appCode}/departments/{departmentId}/roles/{roleId}/permissions`
- `PUT /api/{appCode}/departments/{departmentId}/roles/{roleId}/permissions`

当前用户：

- `GET /api/{appCode}/auth/me`

`auth/me` 返回：

- `permissions`：当前用户聚合后的权限编码
- `menus`：当前用户可见菜单树

## 6. 当前阶段边界

当前先只认真做 `MENU`。

`BUTTON`、`API`、`DATA` 先保留类型和字段，但不急着落业务拦截。这样前端菜单可以先跑起来，同时不会把后续按钮权限、接口权限、数据权限的路堵死。

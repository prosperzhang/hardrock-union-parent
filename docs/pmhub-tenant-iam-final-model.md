# PMHub TenantRegistry/IAM Final Model

## 1. Core Conclusion

PMHub now uses the unified `tenant + iam` model.

- `tenant = project`
- `tenant_registry` is the project master record
- `iam_tenant_member` is the project membership table
- `iam_tenant_member_department_role` is the effective department-and-role assignment table
- `iam_department_role` is the department role catalog
- `iam_tenant_join_request` is the join-request table

PMHub business code no longer owns project membership, project join request, or project role assignment.

## 2. Final Tables

### 2.1 `iam_user`

Account master table.

Main responsibility:

- account identity
- username
- password hash
- account status

Typical fields:

- `id`
- `app_id`
- `app_code`
- `username`
- `password_hash`
- `status`
- `deleted`

### 2.2 `iam_user_info`

User profile table.

Main responsibility:

- nickname
- avatar

Typical fields:

- `user_id`
- `nick_name`
- `avatar_url`
- `deleted`

### 2.3 `tenant_registry`

Project master table.

Main responsibility:

- project tenant identity
- project basic information

In PMHub:

- `tenant = project`

Typical fields:

- `id`
- `app_id`
- `app_code`
- `tenant_code`
- `tenant_name`
- `tenant_type`
- `status`
- `project_address`
- `manager_name`
- `manager_phone`

### 2.4 `iam_tenant_member`

Project membership table.

Main responsibility:

- who belongs to which project tenant
- whether member is active or removed

Typical fields:

- `id`
- `app_id`
- `tenant_id`
- `user_id`
- `member_status`
- `is_primary`
- `remark`
- `joined_at`
- `deleted`

### 2.5 `iam_tenant_member_department_role`

Effective department-and-role result table.

Main responsibility:

- member primary department
- member effective roles inside one tenant

Typical fields:

- `id`
- `app_id`
- `tenant_id`
- `user_id`
- `department_id`
- `role_id`
- `primary_flag`
- `deleted`
- `created_at`
- `updated_at`

### 2.6 `iam_department_role`

Department role catalog table.

Main responsibility:

- which roles are assignable under one department

Important note:

- this is not the final user assignment table
- this is only the role catalog for department-based selection

### 2.7 `iam_tenant_join_request`

Join-request table.

Main responsibility:

- who requests to join which project tenant
- request review result

Typical fields:

- `id`
- `app_id`
- `tenant_id`
- `user_id`
- `apply_message`
- `request_status`
- `reviewed_by`
- `reviewed_at`
- `review_remark`
- `deleted`

## 3. Final Business Flow

### 3.1 Register

Writes:

- `iam_user`
- `iam_user_info`

No tenant is created at this stage.

### 3.2 Create Project

Writes:

- `tenant_registry`
- `iam_tenant_member`
- `iam_tenant_member_department_role`

Default result:

- creator becomes tenant member
- creator enters `PMHUB_DECISION_DEPT`
- creator gets `PMHUB_DECISION_LEADER`

### 3.3 Join Project

Writes:

- `iam_tenant_join_request`

No formal membership or role is created yet.

### 3.4 Approve Join Request

Writes:

- update `iam_tenant_join_request`
- create or activate `iam_tenant_member`

At this stage the user is a member, but still may have no department-role assignment.

### 3.5 Assign Department And Roles

Writes:

- `iam_tenant_member_department_role`

Recommended product flow:

1. admin selects member
2. admin selects department
3. system loads assignable roles from `iam_department_role`
4. admin selects one or more roles
5. system writes final result into `iam_tenant_member_department_role`

## 4. Onboarding Status

Current aggregated onboarding states:

- `NEED_CREATE_OR_JOIN`
- `WAITING_APPROVAL`
- `WAITING_ROLE_ASSIGNMENT`
- `READY`

Recommended decision order:

1. no active `iam_tenant_member` and no pending request
   -> `NEED_CREATE_OR_JOIN`
2. has pending `iam_tenant_join_request`
   -> `WAITING_APPROVAL`
3. has active `iam_tenant_member` but no active `iam_tenant_member_department_role`
   -> `WAITING_ROLE_ASSIGNMENT`
4. has active `iam_tenant_member` and at least one active `iam_tenant_member_department_role`
   -> `READY`

## 5. Retired Tables

The following tables are no longer part of the runtime main flow:

- `pmhub_project`
- `pmhub_project_member`
- `pmhub_project_member_role`
- `pmhub_project_join_request`
- `iam_user_role`
- `iam_user_department`

Some may still exist only for historical migration purposes, but they are no longer the active model.

## 6. Final Boundary

### TenantRegistry/IAM responsibility

- project creation
- project membership
- join requests
- department assignment
- role assignment
- onboarding status

### PMHub business responsibility

- sites
- workers
- entries
- attendance
- other pure PMHub business data

## 7. Final Summary

The final PMHub model is:

- register creates account only
- create-project creates tenant and admin assignment
- join-project creates join request
- approval creates tenant membership
- department-role assignment completes access
- user enters the system only after tenant membership and department-role assignment are ready

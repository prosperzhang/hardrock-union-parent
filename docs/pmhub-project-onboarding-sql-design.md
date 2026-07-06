# PMHub Onboarding SQL Design

## 1. Current Storage Model

PMHub onboarding no longer uses:

- `pmhub_project_member`
- `pmhub_project_member_role`
- `pmhub_project_join_request`

The current storage model is:

- `tenant_registry`
  project master record
- `iam_tenant_member`
  tenant membership
- `iam_tenant_member_department_role`
  department-and-role assignment
- `iam_tenant_join_request`
  join request

## 2. Create Project Writes

When the user creates a project:

1. insert `tenant_registry`
2. insert `iam_tenant_member`
3. insert `iam_tenant_member_department_role`

The creator becomes:

- tenant member
- `PMHUB_DECISION_DEPT`
- `PMHUB_DECISION_LEADER`

## 3. Join Project Writes

When the user submits a join request:

1. insert `iam_tenant_join_request`
2. status = `PENDING`

When admin approves:

1. update `iam_tenant_join_request`
2. insert or activate `iam_tenant_member`

When admin assigns department and roles:

1. replace active bindings in `iam_tenant_member_department_role`

## 4. Onboarding Status Projection

Recommended response fields:

- `status`
- `tenantId`
- `tenantName`
- `joinRequestId`
- `joinRequestStatus`
- `memberId`
- `memberStatus`
- `roleCodes`
- `message`

Recommended rules:

1. active `iam_tenant_member` + active `iam_tenant_member_department_role`
   -> `READY`
2. active `iam_tenant_member` only
   -> `WAITING_ROLE_ASSIGNMENT`
3. pending `iam_tenant_join_request`
   -> `WAITING_APPROVAL`
4. none of the above
   -> `NEED_CREATE_OR_JOIN`

## 5. Uniqueness Rules

Suggested membership uniqueness:

- `UNIQUE KEY (tenant_id, user_id)`

Suggested role-binding uniqueness:

- one active primary department per user in one tenant
- no duplicate `(tenant_id, user_id, department_id, role_id)` active binding

## 6. Final Note

The SQL design has already moved to the unified tenant-and-IAM model. Any future schema change should continue from:

- `tenant_registry`
- `iam_tenant_member`
- `iam_tenant_member_department_role`
- `iam_tenant_join_request`

and should not reintroduce PMHub-specific membership tables.

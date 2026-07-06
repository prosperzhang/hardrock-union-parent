# PMHub Onboarding And Membership Design

## 1. Current Model

PMHub now uses the unified `tenant + iam` model.

- `tenant = project`
- `tenant_registry` is the project master record
- `iam_tenant_member` is the project membership table
- `iam_tenant_member_department_role` is the department-and-role assignment table
- `iam_tenant_join_request` is the join-request table

PMHub business code no longer owns project membership, project join request, or project role assignment.

## 2. Registration Flow

### 2.1 Register

- `POST /api/pmhub/auth/register`

Request:

- `username`
- `password`

Effects:

- create `iam_user`
- create empty `iam_user_info`
- return token with `tenantId = null`

### 2.2 Complete Profile

- `PUT /api/pmhub/auth/me/profile`

Request:

- `nickName`
- `avatarUrl` optional

Effects:

- update `iam_user_info`

## 3. Create Project Flow

- `POST /api/pmhub/tenants/projects`

Request:

- `tenantName`
- `tenantCode`
- `projectAddress`
- `managerName`
- `managerPhone`

Effects:

1. create `tenant_registry`
2. create `iam_tenant_member`
3. create `iam_tenant_member_department_role`
4. creator enters `PMHUB_DECISION_DEPT`
5. creator gets `PMHUB_DECISION_LEADER`
6. return a new login token with the new `tenantId`

## 4. Join Project Flow

- `POST /api/pmhub/tenant-join-requests`

Request:

- `tenantId`
  or
- `tenantKeyword`
- `applyMessage` optional

Effects:

1. create `iam_tenant_join_request`
2. status becomes `PENDING`
3. do not create formal membership yet
4. do not assign department or role yet

## 5. Review Flow

### 5.1 List Requests

- `GET /api/pmhub/tenant-join-requests/tenants/{tenantId}`

Only tenant admins can view.

### 5.2 Approve

- `POST /api/pmhub/tenant-join-requests/tenants/{tenantId}/{requestId}/approve`

Effects:

1. mark join request as `APPROVED`
2. create or activate `iam_tenant_member`
3. onboarding enters `WAITING_ROLE_ASSIGNMENT`

### 5.3 Reject

- `POST /api/pmhub/tenant-join-requests/tenants/{tenantId}/{requestId}/reject`

Effects:

1. mark join request as `REJECTED`
2. do not create membership

## 6. Assign Department And Roles

- `PUT /api/pmhub/tenants/{tenantId}/members/{memberId}/department-roles`

Request:

- `departmentId`
- `roleCodes`

Rules:

1. admin chooses a member
2. admin chooses a department
3. system loads assignable roles from `iam_department_role`
4. admin chooses one or more roles under that department
5. system writes final result into `iam_tenant_member_department_role`

This means:

- `iam_department_role` = department role catalog
- `iam_tenant_member_department_role` = actual user assignment result

## 7. Onboarding Status

- `GET /api/pmhub/onboarding/status`

Suggested response fields:

- `status`
- `tenantId`
- `tenantName`
- `joinRequestId`
- `joinRequestStatus`
- `memberId`
- `memberStatus`
- `roleCodes`
- `message`

Supported status values:

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

## 8. Permission Boundary

PMHub business remains responsible for:

- sites
- workers
- entries
- attendance
- project business data

TenantRegistry and IAM are responsible for:

- project creation
- project membership
- join requests
- department assignment
- role assignment
- onboarding status

## 9. Final Conclusion

The current target model is:

- registration only creates account
- create-project creates tenant and admin assignment
- join-project creates join request
- approval creates tenant membership
- department-role assignment completes access
- user enters the system only after tenant membership and department-role assignment are ready

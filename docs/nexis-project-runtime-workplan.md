# Nexis Project Runtime Workplan

## Current Baseline

- Nexis uses `tenant = project`.
- Project master data lives in `tenant_registry`.
- Project membership, join requests, department assignment, and role assignment live in IAM tenant tables.
- `hardrock-union-business-project` owns only construction project business extensions:
  - sites
  - participant companies
  - project/site participant relations
  - work scopes
  - teams
  - workers
  - worker entries
  - attendance
- `hardrock-union-solution-nexis` is the Nexis app assembly layer.

The current code compiles with:

```bash
mvn -q -DskipTests compile
```

## Runtime Entry Points

### Account And Onboarding

- `POST /api/nexis/auth/register`
- `PUT /api/nexis/auth/me/profile`
- `GET /api/nexis/onboarding/status`

### Project Tenant Flow

- `GET /api/nexis/tenants/projects`
- `GET /api/nexis/tenants/projects/{tenantId}`
- `POST /api/nexis/tenants/projects`
- `POST /api/nexis/tenant-join-requests`
- `POST /api/nexis/tenant-join-requests/{requestId}/cancel`
- `GET /api/nexis/tenant-join-requests/tenants/{tenantId}`
- `POST /api/nexis/tenant-join-requests/tenants/{tenantId}/{requestId}/approve`
- `POST /api/nexis/tenant-join-requests/tenants/{tenantId}/{requestId}/reject`
- `GET /api/nexis/tenants/{tenantId}/members`
- `PUT /api/nexis/tenants/{tenantId}/members/{memberId}/department-roles`
- `POST /api/nexis/tenants/{tenantId}/members/{memberId}/remove`

### Construction Project Business

- `GET/POST /api/nexis/sites`
- `GET/POST /api/nexis/participant-companies`
- `GET/POST /api/nexis/project-participants`
- `GET/POST /api/nexis/site-participants`
- `GET/POST /api/nexis/site-work-scopes`
- `GET/POST /api/nexis/teams`
- `GET/POST /api/nexis/workers`
- `GET/POST /api/nexis/worker-entries`
- `GET/POST /api/nexis/worker-attendances`
- `GET /api/nexis/dashboard/overview`

## First Delivery Slice

1. Keep tenant/project onboarding clean.
   - Registration must not create a project.
   - Create-project must create tenant, creator membership, and default department-role binding.
   - Join-project must only create a pending join request.
   - Approval must create or activate membership, but not silently assign business roles.
   - Department-role assignment must complete readiness.

2. Keep project business data tenant-scoped.
   - Business tables must use current `tenant_id` as the project identity.
   - Any `project_id` field in business tables should refer to the same Nexis project tenant id unless a later sub-project concept is introduced explicitly.
   - Services should continue using `NexisProjectLookupService` as a tenant-registry projection, not a `nexis_project` table gateway.

3. Make Swagger/API semantics explicit.
   - Generic tenant endpoints should describe Nexis as project flow.
   - Nexis business endpoints should avoid implying that they manage IAM membership.

4. Verify end-to-end.
   - Register.
   - Complete profile.
   - Create project.
   - Check onboarding status is `READY`.
   - Create site, participant company, project participant, team, worker, entry, attendance.
   - Check dashboard overview reflects business data.

## Guardrails

- Do not reintroduce `nexis_project` as the project master table.
- Do not add Nexis-specific member, member-role, or join-request tables.
- Do not put project membership logic in `hardrock-union-business-project`.
- Treat `tenant_registry` as the source of truth for Nexis project identity.

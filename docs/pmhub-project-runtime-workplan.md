# PMHub Project Runtime Workplan

## Current Baseline

- PMHub uses `tenant = project`.
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
- `hardrock-union-solution-pmhub` is the PMHub app assembly layer.

The current code compiles with:

```bash
mvn -q -DskipTests compile
```

## Runtime Entry Points

### Account And Onboarding

- `POST /api/pmhub/auth/register`
- `PUT /api/pmhub/auth/me/profile`
- `GET /api/pmhub/onboarding/status`

### Project Tenant Flow

- `GET /api/pmhub/tenants/projects`
- `GET /api/pmhub/tenants/projects/{tenantId}`
- `POST /api/pmhub/tenants/projects`
- `POST /api/pmhub/tenant-join-requests`
- `POST /api/pmhub/tenant-join-requests/{requestId}/cancel`
- `GET /api/pmhub/tenant-join-requests/tenants/{tenantId}`
- `POST /api/pmhub/tenant-join-requests/tenants/{tenantId}/{requestId}/approve`
- `POST /api/pmhub/tenant-join-requests/tenants/{tenantId}/{requestId}/reject`
- `GET /api/pmhub/tenants/{tenantId}/members`
- `PUT /api/pmhub/tenants/{tenantId}/members/{memberId}/department-roles`
- `POST /api/pmhub/tenants/{tenantId}/members/{memberId}/remove`

### Construction Project Business

- `GET/POST /api/pmhub/sites`
- `GET/POST /api/pmhub/participant-companies`
- `GET/POST /api/pmhub/project-participants`
- `GET/POST /api/pmhub/site-participants`
- `GET/POST /api/pmhub/site-work-scopes`
- `GET/POST /api/pmhub/teams`
- `GET/POST /api/pmhub/workers`
- `GET/POST /api/pmhub/worker-entries`
- `GET/POST /api/pmhub/worker-attendances`
- `GET /api/pmhub/dashboard/overview`

## First Delivery Slice

1. Keep tenant/project onboarding clean.
   - Registration must not create a project.
   - Create-project must create tenant, creator membership, and default department-role binding.
   - Join-project must only create a pending join request.
   - Approval must create or activate membership, but not silently assign business roles.
   - Department-role assignment must complete readiness.

2. Keep project business data tenant-scoped.
   - Business tables must use current `tenant_id` as the project identity.
   - Any `project_id` field in business tables should refer to the same PMHub project tenant id unless a later sub-project concept is introduced explicitly.
   - Services should continue using `PmhubProjectLookupService` as a tenant-registry projection, not a `pmhub_project` table gateway.

3. Make Swagger/API semantics explicit.
   - Generic tenant endpoints should describe PMHub as project flow.
   - PMHub business endpoints should avoid implying that they manage IAM membership.

4. Verify end-to-end.
   - Register.
   - Complete profile.
   - Create project.
   - Check onboarding status is `READY`.
   - Create site, participant company, project participant, team, worker, entry, attendance.
   - Check dashboard overview reflects business data.

## Guardrails

- Do not reintroduce `pmhub_project` as the project master table.
- Do not add PMHub-specific member, member-role, or join-request tables.
- Do not put project membership logic in `hardrock-union-business-project`.
- Treat `tenant_registry` as the source of truth for PMHub project identity.

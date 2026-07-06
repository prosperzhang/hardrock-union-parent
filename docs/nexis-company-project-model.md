# Nexis Company And Project Model

## 1. Problem

The previous Nexis runtime model uses:

```text
tenant = project
```

This is still correct for project-level data isolation, but it is not enough for two real business scenarios:

1. A construction company may have 40 projects running at the same time. Someone at company level must coordinate progress, cost, procurement, risks, documents, and project managers.
2. A user may participate in multiple projects as an investor, partner, owner representative, or company executive. The user should not need multiple accounts.

Nexis therefore needs a company/group layer above project tenants.

## 2. Final Direction

Nexis should use a two-level business model:

```text
Nexis company/group workspace
  -> Nexis project tenant
```

Important rule:

- project remains the project business isolation unit
- company/group is the multi-project management unit
- one user account can join many company/project tenants with different roles

In storage terms, this becomes:

```text
tenant_registry
  tenant_type = GROUP / COMPANY / PROJECT
  parent_tenant_id = parent company or group tenant id
```

Typical hierarchy:

```text
GROUP
  -> COMPANY
       -> PROJECT
       -> PROJECT
       -> PROJECT
```

A simple customer can skip `GROUP` and use:

```text
COMPANY
  -> PROJECT
```

A very small trial customer can still use a standalone project:

```text
PROJECT
```

Standalone projects remain allowed for compatibility and for trial onboarding.

## 3. Company-Level Coordination

When a company has many projects, coordination should happen from a company workspace, not by forcing company executives to enter each project as normal project members.

Recommended company-level departments:

- executive office
- project management center
- engineering management department
- cost department
- procurement department
- finance department
- document/archive department

Recommended company-level roles:

- `NEXIS_COMPANY_OWNER`
- `NEXIS_COMPANY_ADMIN`
- `NEXIS_PROJECT_DIRECTOR`
- `NEXIS_ENGINEERING_DIRECTOR`
- `NEXIS_COST_DIRECTOR`
- `NEXIS_PROCUREMENT_DIRECTOR`
- `NEXIS_FINANCE_DIRECTOR`
- `NEXIS_DOCUMENT_MANAGER`

Company-level dashboards should aggregate:

- project progress
- project risk
- material requests
- arrival exceptions
- cost overruns
- missing invoices
- missing delivery notes
- supplier fulfillment ranking
- project manager performance
- multi-project procurement analysis

## 4. Multi-Project User Participation

One person must keep one account:

```text
iam_user
```

The user's participation in companies and projects is represented by tenant membership:

```text
iam_tenant_member
iam_tenant_member_department_role
```

Example:

```text
user A
  -> Company Alpha: NEXIS_COMPANY_OWNER
  -> Project 1: NEXIS_INVESTOR
  -> Project 2: NEXIS_INVESTOR
  -> Project 8: NEXIS_OWNER_REPRESENTATIVE
```

Investor roles should not be mixed with construction execution roles.

Recommended investor-side roles:

- `NEXIS_INVESTOR`
- `NEXIS_INVESTOR_REPRESENTATIVE`
- `NEXIS_PARTNER`
- `NEXIS_OWNER_REPRESENTATIVE`

Investor-visible information should focus on:

- project progress
- investment amount
- project cost report
- payment and collection summary
- contract and change summary
- risk reminders
- owner dashboard

Investor roles should not automatically see every internal execution detail, such as worker attendance, internal approval notes, or subcontractor management data.

## 5. Data Scope

Nexis should introduce data scope above basic role permission:

```text
PROJECT_SELF
COMPANY_SELECTED_PROJECTS
COMPANY_ALL_PROJECTS
GROUP_ALL_PROJECTS
```

Meaning:

- `PROJECT_SELF`: user can access only the current project tenant
- `COMPANY_SELECTED_PROJECTS`: user can access selected projects under a company
- `COMPANY_ALL_PROJECTS`: user can access all projects under a company
- `GROUP_ALL_PROJECTS`: user can access all projects under all companies in a group

The current implementation starts with parent tenant hierarchy in `tenant_registry`. Full data-scope enforcement should be added as a separate capability layer instead of hard-coding company permissions inside project services.

## 6. Runtime Flow

### Company Creation

```text
POST /api/nexis/tenants/workspaces
tenantType = COMPANY
```

Creates:

- company tenant in `tenant_registry`
- creator membership in `iam_tenant_member`
- creator default department-role binding

### Project Creation Under Company

```text
POST /api/nexis/tenants/workspaces
tenantType = PROJECT
parentTenantId = company tenant id
```

Creates:

- project tenant in `tenant_registry`
- `parent_tenant_id = company tenant id`
- creator membership in the project
- creator default department-role binding

### Standalone Project Creation

```text
POST /api/nexis/tenants/workspaces
tenantType = PROJECT
parentTenantId = null
```

Still allowed for:

- trial users
- very small project teams
- compatibility with the previous Nexis model

## 7. Implementation Rule

Do not reintroduce `nexis_project` as the project master table.

The source of truth remains:

```text
tenant_registry
```

Nexis project business tables continue to use the current `tenant_id` as the project identity.

Company/group hierarchy is expressed by:

```text
tenant_registry.parent_tenant_id
tenant_registry.tenant_type
```

This keeps the architecture aligned with the existing app-aware tenant model while making company-level Nexis possible.

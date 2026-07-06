# Nexis Project And Site SQL Design

## Status

This document is historical.

It was written before the current Nexis `tenant = project` storage model was finalized.

Do not use this document as the current SQL implementation guide.

## Current References

- [Nexis TenantRegistry/IAM Final Model](/Users/castor/Desktop/hardrock-union-parent/docs/nexis-tenant-iam-final-model.md)
- [Nexis Onboarding SQL Design](/Users/castor/Desktop/hardrock-union-parent/docs/nexis-project-onboarding-sql-design.md)
- [Architecture Overview](/Users/castor/Desktop/hardrock-union-parent/docs/architecture-overview.md)

## Current Storage Rule

Current Nexis project identity and onboarding storage lives in:

- `tenant_registry`
- `iam_tenant_member`
- `iam_tenant_member_department_role`
- `iam_tenant_join_request`

Retired from the runtime main flow:

- `nexis_project`
- `nexis_project_member`
- `nexis_project_member_role`
- `nexis_project_join_request`

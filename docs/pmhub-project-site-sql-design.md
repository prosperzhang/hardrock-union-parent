# PMHub Project And Site SQL Design

## Status

This document is historical.

It was written before the current PMHub `tenant = project` storage model was finalized.

Do not use this document as the current SQL implementation guide.

## Current References

- [PMHub TenantRegistry/IAM Final Model](/Users/castor/Desktop/hardrock-union-parent/docs/pmhub-tenant-iam-final-model.md)
- [PMHub Onboarding SQL Design](/Users/castor/Desktop/hardrock-union-parent/docs/pmhub-project-onboarding-sql-design.md)
- [Architecture Overview](/Users/castor/Desktop/hardrock-union-parent/docs/architecture-overview.md)

## Current Storage Rule

Current PMHub project identity and onboarding storage lives in:

- `tenant_registry`
- `iam_tenant_member`
- `iam_tenant_member_department_role`
- `iam_tenant_join_request`

Retired from the runtime main flow:

- `pmhub_project`
- `pmhub_project_member`
- `pmhub_project_member_role`
- `pmhub_project_join_request`

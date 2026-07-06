# PMHub Project And Site Domain Draft

## Status

This document is historical.

It was originally written before the current PMHub `tenant = project` model was finalized.

Do not use this document as the current project master-data design.

## Current References

- [PMHub TenantRegistry/IAM Final Model](/Users/castor/Desktop/hardrock-union-parent/docs/pmhub-tenant-iam-final-model.md)
- [PMHub Onboarding And Membership Design](/Users/castor/Desktop/hardrock-union-parent/docs/pmhub-project-onboarding-and-membership-design.md)
- [Architecture Overview](/Users/castor/Desktop/hardrock-union-parent/docs/architecture-overview.md)

## Current Rule

PMHub now uses:

- `tenant_registry` as the project master record
- `iam_tenant_member` as the project membership table
- `iam_tenant_member_department_role` as the effective department-and-role assignment table
- `iam_tenant_join_request` as the project join-request table

The PMHub business-project module should focus on project business extensions such as:

- site
- workers
- attendance
- construction collaboration

It should not reintroduce:

- `pmhub_project`
- `pmhub_project_member`
- `pmhub_project_member_role`
- `pmhub_project_join_request`

# Nexis Project And Site Domain Draft

## Status

This document is historical.

It was originally written before the current Nexis `tenant = project` model was finalized.

Do not use this document as the current project master-data design.

## Current References

- [Nexis TenantRegistry/IAM Final Model](/Users/castor/Desktop/hardrock-union-parent/docs/nexis-tenant-iam-final-model.md)
- [Nexis Onboarding And Membership Design](/Users/castor/Desktop/hardrock-union-parent/docs/nexis-project-onboarding-and-membership-design.md)
- [Architecture Overview](/Users/castor/Desktop/hardrock-union-parent/docs/architecture-overview.md)

## Current Rule

Nexis now uses:

- `tenant_registry` as the project master record
- `iam_tenant_member` as the project membership table
- `iam_tenant_member_department_role` as the effective department-and-role assignment table
- `iam_tenant_join_request` as the project join-request table

The Nexis business-project module should focus on project business extensions such as:

- site
- workers
- attendance
- construction collaboration

It should not reintroduce:

- `nexis_project`
- `nexis_project_member`
- `nexis_project_member_role`
- `nexis_project_join_request`

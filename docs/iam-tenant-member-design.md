# IAM TenantRegistry Member Design

## Status

This document is obsolete and retained only for historical reference.

It reflects an intermediate design that still treated:

- `iam_user_role`
- `iam_user_department`

as active runtime tables.

That is no longer true.

## Current Authoritative Documents

- [Nexis TenantRegistry/IAM Final Model](/Users/castor/Desktop/hardrock-union-parent/docs/nexis-tenant-iam-final-model.md)
- [Architecture Overview](/Users/castor/Desktop/hardrock-union-parent/docs/architecture-overview.md)

## Current Runtime Model

Membership:

- `iam_tenant_member`

Department-role result:

- `iam_tenant_member_department_role`

Department role catalog:

- `iam_department_role`

Join request:

- `iam_tenant_join_request`

Retired from the runtime main flow:

- `iam_user_role`
- `iam_user_department`

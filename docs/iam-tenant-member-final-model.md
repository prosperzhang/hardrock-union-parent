# IAM TenantRegistry Member Final Model

## Status

This document is superseded.

It described a stage where multi-tenant membership had already been introduced, but the runtime still depended on:

- `iam_user_role`
- `iam_user_department`

That is no longer the final model.

## Use These Documents Instead

- [Nexis TenantRegistry/IAM Final Model](/Users/castor/Desktop/hardrock-union-parent/docs/nexis-tenant-iam-final-model.md)
- [Architecture Overview](/Users/castor/Desktop/hardrock-union-parent/docs/architecture-overview.md)

## Current Final Rule

The current final assignment chain is:

- `tenant_registry`
- `iam_tenant_member`
- `iam_department_role`
- `iam_tenant_member_department_role`
- `iam_tenant_join_request`

Retired from the runtime main flow:

- `iam_user_role`
- `iam_user_department`

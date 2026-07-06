# IAM Department-Role Design

## Status

This document is kept only as a historical note.

It no longer describes the current runtime model.

The current authoritative model is:

- [Nexis TenantRegistry/IAM Final Model](/Users/castor/Desktop/hardrock-union-parent/docs/nexis-tenant-iam-final-model.md)
- [Architecture Overview](/Users/castor/Desktop/hardrock-union-parent/docs/architecture-overview.md)

## Historical Context

This document described an earlier transition design where:

- `iam_user_role` still existed as a compatibility layer
- `iam_user_department` still existed as a compatibility layer
- department-role inheritance was still being designed around those tables

That is no longer the active direction.

## Current Rule

The current effective assignment model is:

- `iam_tenant_member`
  membership inside one tenant
- `iam_department_role`
  department role catalog
- `iam_tenant_member_department_role`
  final effective department-and-role result

Retired from the runtime main flow:

- `iam_user_role`
- `iam_user_department`

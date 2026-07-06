# IAM TenantRegistry Design

## 1. Why TenantRegistry Must Be App-Aware

`tenant` is not a global shared bucket.

The platform already has three app identities:

- `WSGM`
- `NEXIS`
- `PRIMELOAD-MARKETPLACE`

Each app needs its own:

- tenant bootstrap
- login context
- dashboard entry
- department tree
- role and permission set

So tenant must be treated as app-aware master data.

## 2. Target Model

Recommended logical chain:

- one `app_registry` has many `tenants`
- one `tenant` belongs to one `app`
- one `user` can belong to many tenants
- one `tenant` has many tenant members

In short:

- `app` is the product identity
- `tenant` is the organization using the app
- `user` is the account master data
- `iam_tenant_member` is the membership between user and tenant

## 3. Recommended Table

### 3.1 `tenant_registry`

Recommended purpose:

- store app-aware tenant master data
- make tenant bootstrap explicit per app
- prevent cross-app tenant collisions

Recommended fields:

- `id`
- `app_code`
- `tenant_code`
- `tenant_name`
- `tenant_type`
- `status`
- `contact_name`
- `contact_phone`
- `admin_user_id`
- `deleted`
- `created_at`
- `updated_at`

Suggested field meaning:

- `app_code`
  app registry code, such as `WSGM`, `NEXIS`, `PRIMELOAD-MARKETPLACE`
- `tenant_code`
  tenant code unique inside one app
- `tenant_name`
  display name
- `tenant_type`
  tenant category, such as headquarters, company, merchant
- `admin_user_id`
  primary tenant admin

Recommended unique key:

- `(app_code, tenant_code)`

Recommended indexes:

- `(app_code, status)`

## 4. Relationship Rules

### 4.1 App to TenantRegistry

TenantRegistry belongs to one app.

That means:

- tenant queries must always know which app they belong to
- bootstrap data should be seeded per app
- a tenant code should not be globally unique anymore

### 4.2 TenantRegistry to Department

Departments should be app-scoped master data.

Long-term direction:

- tenant identifies the organization
- department identifies the internal structure of the app
- department master data should not be duplicated because a tenant changes

### 4.3 TenantRegistry to User

Users do not rely on `iam_user.tenant_id` as the long-term authority anymore.

The authoritative relationship should become:

- `iam_tenant_member`

Recommended future login shape:

- `appCode + tenantId + username + password`

The login request can still carry `tenantId`, but the backend should resolve membership through `iam_tenant_member`.

## 5. Migration Strategy

Recommended steps:

1. keep `tenant_registry` as the tenant registry
2. change uniqueness from global `tenant_code` to `(app_code, tenant_code)`
3. add `iam_tenant_member` as the authoritative membership table
4. backfill one membership row from existing `iam_user.tenant_id`
5. update login and authorization to resolve tenant membership through `iam_tenant_member`
6. keep `app_code` as the practical runtime reference
7. later, if needed, replace hard-coded app checks with `app_registry` lookups

Important:

- do not keep tenant code globally unique forever
- tenant code should only be unique inside an app boundary
- this is the key to making multi-app expansion safe

## 6. Seed Examples

Recommended tenant seeds:

- `WSGM`
  - `wsgm-hq`
- `NEXIS`
  - `nexis-default`
- `PRIMELOAD-MARKETPLACE`
  - `primeload-marketplace-default`

## 7. Impacted Areas

Once `tenant_registry` becomes app-aware, the following areas can be aligned:

- tenant bootstrap SQL
- login tenant selection
- app tenant lookup APIs
- department and role seed data per app
- app home and dashboard routing

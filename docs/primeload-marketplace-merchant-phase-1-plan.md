# PRIMELOAD-MARKETPLACE Merchant Phase 1 Plan

## 1. Goal

第一阶段先做商户进入系统后的主流程起点：

- 商户资料。
- 商品分类。
- 商品。
- 报价单和订单的 PRIMELOAD-MARKETPLACE 应用入口。

报价单和订单本轮只补 PRIMELOAD-MARKETPLACE 语义入口，底层仍复用 `business-merchant` 已有服务，不做大重构。
warehouse、logistics 暂不正式拆入履约体系。

## 2. Current Baseline

已经确认：

- PRIMELOAD-MARKETPLACE 注册登录由 IAM 提供。
- PRIMELOAD-MARKETPLACE 商户身份由 `tenant_registry` 表达。
- `tenant_registry.tenant_type = MERCHANT`。
- 创建商户 tenant 后，创建者只获得 `PRIMELOAD_MARKETPLACE_DECISION_LEADER`。
- 加入商户审批通过后，只激活成员关系，等待部门角色分配。

## 3. Merchant Profile

商户资料不新建 `MerchantMerchant`。

当前商户主档来自：

```text
tenant_registry
```

PRIMELOAD-MARKETPLACE 商户资料接口：

```http
GET /api/primeload-marketplace/merchant/profile
PUT /api/primeload-marketplace/merchant/profile
```

当前字段映射：

| API 字段 | 数据来源 |
| --- | --- |
| `tenantId` | `tenant_registry.id` |
| `merchantCode` | `tenant_registry.tenant_code` |
| `merchantName` | `tenant_registry.tenant_name` |
| `merchantType` | `tenant_registry.tenant_type` |
| `businessAddress` | `tenant_registry.project_address` |
| `managerName` | `tenant_registry.manager_name` |
| `managerPhone` | `tenant_registry.manager_phone` |
| `status` | `tenant_registry.status` |

后续如果需要营业执照、门店照片、经营类目、资质文件，再考虑扩展 `merchant_profile`。现在不提前建表。

## 4. Product

现有 `business-merchant` 已经有：

- `merchant_category`
- `merchant_product`

它们已经按 `tenant_id` 隔离，适合作为第一阶段的商品基础。

PRIMELOAD-MARKETPLACE app 语义入口：

```http
GET  /api/primeload-marketplace/product-categories
POST /api/primeload-marketplace/product-categories
PUT  /api/primeload-marketplace/product-categories/{id}
DELETE /api/primeload-marketplace/product-categories/{id}
GET  /api/primeload-marketplace/products
POST /api/primeload-marketplace/products
GET  /api/primeload-marketplace/products/{id}
PUT  /api/primeload-marketplace/products/{id}
DELETE /api/primeload-marketplace/products/{id}
POST /api/primeload-marketplace/products/{id}/stock-adjust
```

底层仍复用 `business-merchant` 的 service。旧的 `/api/merchant/...` 暂时保留，后续前端优先使用 `/api/primeload-marketplace/...`。

商品主图只保存 URL：

```text
platform-file 上传图片 -> 返回 URL -> merchant_product.main_image_url 保存引用
```

Merchant 不直接接 OSS。

商品基础资料字段：

- `productName` 必填；同一商户内不能重复。
- `categoryCode` 必填；必须是当前商户下已启用的分类。
- `skuCode` 可选；非空时在当前商户内唯一，并统一按大写保存。
- `mainImageUrl`
- `brandName`
- `specModel`
- `material`
- `productDescription`
- `unit`
- `salePrice` 可选；非空时不能小于 `0`。
- `stockQuantity` 创建时可选，默认 `0`；不能小于 `0`。
- `status` 支持 `0` 或 `1`，列表筛选也遵守同一规则。

商品分类字段：

- `categoryName` 必填；同一商户内不能重复。
- `categoryCode` 可选；不传时系统自动生成。
- `parentId` 可选，默认根分类；非根分类必须属于当前商户且处于启用状态，并且不能形成自引用或循环引用。
- `sortNo` 可选，默认 `0`；不能小于 `0`。
- `status` 创建时默认启用，更新时可改为 `0` 或 `1`，列表筛选也遵守同一规则；分类下有子分类或商品时不能停用。

## 5. Quotation And Order

报价单和订单已经存在于 `business-merchant`，本阶段先把应用入口从旧的 `/api/merchant/...`
对齐到 PRIMELOAD-MARKETPLACE：

```http
GET  /api/primeload-marketplace/quotations
POST /api/primeload-marketplace/quotations
GET  /api/primeload-marketplace/quotations/{id}
POST /api/primeload-marketplace/quotations/{id}/status
POST /api/primeload-marketplace/quotations/{id}/convert-to-order

GET  /api/primeload-marketplace/orders
POST /api/primeload-marketplace/orders
GET  /api/primeload-marketplace/orders/{id}
POST /api/primeload-marketplace/orders/{id}/status
POST /api/primeload-marketplace/orders/{id}/ship
```

当前交易对象默认面向 `NEXIS`：

- `targetAppCode` 默认 `NEXIS`，本阶段只支持 `NEXIS`。
- `targetTenantId` 表示 NEXIS 租户。
- `targetProjectName`、`targetSiteName`、`targetUserName`、`targetUserPhone` 先用文本承接。
- 如果传了 `targetTenantId`，系统会校验它必须属于 `targetAppCode` 对应应用，且目标租户状态可用。

旧的 `/api/merchant/quotations` 和 `/api/merchant/orders` 暂时保留，后续前端优先使用 `/api/primeload-marketplace/...`。

## 6. Guardrail

Merchant 业务入口必须满足：

- `loginUser.appCode = PRIMELOAD-MARKETPLACE`
- `loginUser.tenantId` 不为空
- 当前 tenant 必须属于 PRIMELOAD-MARKETPLACE
- 当前 tenant 的 `tenant_type = MERCHANT`

这能避免 NEXIS 或 WSGM token 误打到 merchant 业务。

## 7. Not In Phase 1

第一阶段不做：

- 独立商户注册。
- `MerchantMerchant`。
- 独立商户账号。
- 报价单大重构。
- 订单大重构。
- warehouse 正式库存扣减。
- logistics 正式发货链路。
- 支付。

这些等商户资料和商品主流程稳定后再推进。

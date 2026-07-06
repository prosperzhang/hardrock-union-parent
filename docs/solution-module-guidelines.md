# Solution Module Guidelines

## 1. Purpose

`hardrock-union-solution-*` modules are app composition layers.

They are not the primary home for reusable business domains.

Their job is to assemble:

- platform capabilities
- business capabilities
- app-specific entrypoints
- app-specific menus and views of shared capabilities

## 2. Module Position

Recommended understanding:

- `platform`
  shared business platform capabilities
- `business`
  reusable core business domains
- `solution`
  app shells and app orchestration

That means:

- `hardrock-union-solution-wsgm`
  can still carry stronger business meaning because `wsgm` is a headquarters-oriented internal system with high customization
- `hardrock-union-solution-pmhub`
  should mainly compose `platform + business-project`
- `hardrock-union-solution-primeload-marketplace`
  should mainly compose `platform + business-merchant`

## 3. What Solution Modules Should Contain

Suitable content for `solution-pmhub` and `solution-primeload-marketplace`:

- app startup aggregation dependencies
- app-specific menu tree and navigation metadata
- app-specific home/dashboard aggregation
- app-specific facade or BFF APIs
- app-specific permission grouping
- app-specific workflow assembly
- app-specific configuration overlays
- app-specific UI/API adaptation for mobile/web/client differences

Examples:

- `pmhub` homepage needs project statistics, site alerts, task summary
- `primeload-marketplace` homepage needs merchant profile summary, product inventory summary, quotation summary

These are app views of multiple business capabilities, so they belong in `solution`.

## 4. What Solution Modules Should Not Contain

The following should not primarily live in `solution-pmhub` or `solution-primeload-marketplace`:

- reusable core project domain models
- reusable merchant domain models
- reusable merchant, goods, order, inventory domain logic
- reusable site, inspection, task, schedule domain logic
- shared data persistence rules that may be used by more than one app

Examples:

- `site`, `inspection`, `task`
  should go to `hardrock-union-business-project`
- `merchant`, `goods`, `quotation`, `order`
  should go to `hardrock-union-business-merchant`

## 5. Recommended Mapping

Recommended module mapping:

- `hardrock-union-business-project`
  project company, project, site, inspection, task, reporting
- `hardrock-union-business-merchant`
  merchant profile, goods, catalog, quotation, order, fulfillment
- `hardrock-union-solution-pmhub`
  compose project business into PMHub app
- `hardrock-union-solution-primeload-marketplace`
  compose merchant business into PRIMELOAD-MARKETPLACE app

## 6. Suggested Package Structure

Suggested package layout for `solution-pmhub`:

- `com.hardrockunion.solution.pmhub.dashboard`
- `com.hardrockunion.solution.pmhub.facade`
- `com.hardrockunion.solution.pmhub.menu`
- `com.hardrockunion.solution.pmhub.config`
- `com.hardrockunion.solution.pmhub.integration`

Suggested package layout for `solution-primeload-marketplace`:

- `com.hardrockunion.solution.primeloadmarketplace.dashboard`
- `com.hardrockunion.solution.primeloadmarketplace.facade`
- `com.hardrockunion.solution.primeloadmarketplace.menu`
- `com.hardrockunion.solution.primeloadmarketplace.config`
- `com.hardrockunion.solution.primeloadmarketplace.integration`

Avoid putting reusable `domain/entity/service/mapper` packages here unless the logic is truly app-only and cannot reasonably belong in `business`.

## 7. Decision Rule

When adding new code, use this rule:

If the capability is reusable as a stable business concept, put it in `business`.

If the capability exists to present, assemble, tailor, or orchestrate business capabilities for one specific app, put it in `solution`.

## 8. Current Project Rule

For the current project, the working rule should be:

- `pmhub` core business goes to `hardrock-union-business-project`
- `primeload-marketplace` core business goes to `hardrock-union-business-merchant`
- `solution-pmhub` and `solution-primeload-marketplace` stay as app shells
- `wsgm` can temporarily keep stronger business weight in `solution-wsgm`

## 9. Immediate Follow-up

Recommended next steps for `solution-pmhub` and `solution-primeload-marketplace`:

1. Keep them lightweight and dependency-oriented
2. Add app dashboard/facade packages when needed
3. Do not reintroduce reusable business entities into these modules
4. Continue expanding core business in `business-project` and `business-merchant`

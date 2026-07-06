# PMHub Pricing Strategy

## 1. Pricing Position

PMHub should not be priced as a simple project collaboration tool.

The formal pricing position is:

- charge by project and company management value
- keep field users lightweight or free where possible
- use trials to prove the full workflow, not to create a permanent free tier
- let advanced AI, approval, cost, archive, procurement, and multi-project capabilities drive higher plans

The old `199 / 499` level can be used only as an early pilot price. It should not become the formal public price, otherwise PMHub will be anchored too low for construction project management.

## 2. Trial Plan

### Experience Plan

Price:

- `0 CNY`
- `14-30 days`

Limits:

- 1 project
- trial only, no long-term free usage

Includes:

- project ledger
- basic construction daily report
- basic material request
- basic arrival acceptance
- limited AI recognition quota

Purpose:

- let customers experience the complete project workflow
- help sales demonstrate material request, arrival acceptance, daily report, and AI recognition value
- avoid educating customers to expect PMHub as a free tool

## 3. Project Plans

### Project Basic

Price:

- `999 CNY / month / project`
- `9999 CNY / year / project`

Target customers:

- small construction teams
- small projects
- single-project professional subcontractors

Includes:

- project ledger
- construction daily report
- material request
- basic approval
- arrival acceptance
- delivery note archive
- basic material cost ledger
- site photos and attachments
- project member collaboration

Position:

- entry paid plan
- replaces low public pricing as the formal starting plan

### Project Professional

Price:

- `1999 CNY / month / project`
- `19999 CNY / year / project`

Target customers:

- standard PMHub paid customers
- projects that need AI and project cost visibility
- customers who care about missing materials, disputes, rework, reconciliation, and delivery note loss

Includes everything in Project Basic, plus:

- AI construction daily report
- AI material list recognition
- site measurement record
- simple quantity and material calculation
- material request approval
- purchase request approval
- arrival exception approval
- change and site instruction record
- project cost report
- invoice archive
- PRIMELOAD marketplace procurement entry
- owner project dashboard

Position:

- main recommended plan
- best balance between PMHub value and customer affordability

### Construction Control

Price:

- `3999 CNY / month / project`
- `39999 CNY / year / project`

Target customers:

- medium and large projects
- professional subcontracting companies
- decoration companies
- fire protection projects
- mechanical and electrical installation projects

Includes everything in Project Professional, plus:

- multi-level approval workflow
- complex permission control
- budget vs actual cost
- change and site instruction approval
- measurement and quantity calculation templates
- quantity confirmation
- material planning
- supplier reconciliation
- invoice ledger
- payment request
- project operation analysis
- data export
- project document archive

Position:

- enters project operation management
- no longer only project collaboration

## 4. Company Multi-Project Plans

The company plan should not be priced by user seat as the primary metric.

Field workers, foremen, receivers, and basic site participants should be free or lightly limited. PMHub should charge mainly for:

- project count
- management-level capabilities
- company-level analysis
- approval complexity
- procurement and cost data

Recommended tiers:

| Plan | Price | Included Projects |
| --- | ---: | ---: |
| Company Standard | `6999 CNY / month` | up to 5 projects |
| Company Professional | `12999 CNY / month` | up to 10 projects |
| Company Flagship | `19999 CNY / month` | up to 20 projects |

Yearly reference prices:

| Plan | Price |
| --- | ---: |
| Company Standard | `69999 CNY / year` |
| Company Professional | price by negotiation |
| Company Flagship | price by negotiation |

Includes:

- multi-project management
- owner cockpit
- company-level cost summary
- multi-project material procurement analysis
- multi-project approval workflow
- project manager performance
- material request aggregation
- supplier fulfillment ranking
- unified invoice, delivery note, and acceptance archive
- PRIMELOAD procurement data integration

## 5. Enterprise Plan

Price:

- `100000-500000 CNY / year` and above
- quote by project complexity

Target customers:

- medium and large construction enterprises
- professional subcontracting groups
- decoration companies
- fire protection and mechanical/electrical companies
- regional construction enterprises

Includes:

- multi-company support
- multi-department support
- multi-project support
- complex approval workflow
- custom forms
- custom cost subjects
- API
- ERP integration
- financial system integration
- WeCom, DingTalk, or Feishu integration
- AI + BIM module
- advanced permission system
- private deployment or dedicated deployment option
- dedicated customer success
- annual training

Rule:

- enterprise plan must not be sold cheaply
- once ERP, finance, BIM, API, or dedicated implementation is involved, pricing should be based on implementation complexity and annual account value

## 6. Product Packaging Rule

Recommended packaging ladder:

```text
Experience
  prove workflow

Project Basic
  project collaboration and basic material control

Project Professional
  AI + approval + cost visibility

Construction Control
  project operation management

Company Multi-Project
  company-level management and analysis

Enterprise
  integration, customization, deployment, and service
```

The most important public plans should be:

- Project Basic as the entry paid plan
- Project Professional as the main recommended plan
- Company Standard as the upgrade path for multi-project construction companies

## 7. Implementation Implications

The pricing model implies that PMHub capabilities should be tagged by plan.

Suggested internal capability groups:

- `PROJECT_LEDGER`
- `DAILY_REPORT_BASIC`
- `DAILY_REPORT_AI`
- `MATERIAL_REQUEST`
- `ARRIVAL_ACCEPTANCE`
- `APPROVAL_BASIC`
- `APPROVAL_ADVANCED`
- `MATERIAL_COST_LEDGER`
- `PROJECT_COST_REPORT`
- `CHANGE_RECORD`
- `INVOICE_ARCHIVE`
- `PROCUREMENT_ENTRY`
- `OWNER_DASHBOARD`
- `MULTI_PROJECT_DASHBOARD`
- `SUPPLIER_RECONCILIATION`
- `DATA_EXPORT`
- `CUSTOM_FORM`
- `API_INTEGRATION`
- `PRIVATE_DEPLOYMENT`

Future permission, menu, and subscription logic should avoid hard-coding price plans directly into business services. A separate plan capability layer should decide whether a tenant can use a feature.


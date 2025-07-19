# Manufacturing Service

## Overview

The Manufacturing Service manages production planning, work orders, and manufacturing operations across the Chiro ERP system.

## 🎯 Business Purpose

This service handles:

-   **Production Planning**: Master production schedule and capacity planning
-   **Work Order Management**: Manufacturing orders and job tracking
-   **Bill of Materials**: Product recipes and component requirements
-   **Shop Floor Control**: Production monitoring and quality control
-   **Resource Planning**: Equipment and labor scheduling
-   **Quality Management**: QC processes and defect tracking

## 🏗️ Architecture

### Domain Model

```
WorkOrder (Aggregate Root)
├── WorkOrderId (Identity)
├── ProductId (Reference)
├── BillOfMaterials (Entity Collection)
├── ProductionStatus (Value Object)
├── QualityChecks (Entity Collection)
└── ResourceAllocations (Entity Collection)
```

### API Endpoints

```
GET    /api/v1/manufacturing/work-orders  # List work orders
POST   /api/v1/manufacturing/work-orders  # Create work order
GET    /api/v1/manufacturing/bom          # Bill of materials
POST   /api/v1/manufacturing/production/start # Start production
```

### Domain Events Published

```kotlin
- WorkOrderCreatedEvent
- ProductionStartedEvent
- ProductionCompletedEvent
- QualityCheckFailedEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8091
-   Dev UI: http://localhost:8091/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Manufacturing Domain Team

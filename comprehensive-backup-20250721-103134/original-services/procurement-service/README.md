# Procurement Service

## Overview

The Procurement Service manages supplier relationships, purchase orders, and procurement processes across the Chiro ERP system.

## 🎯 Business Purpose

This service handles:

-   **Supplier Management**: Vendor profiles, qualifications, and performance
-   **Purchase Orders**: PO creation, approval workflows, and tracking
-   **RFQ Processing**: Request for quotations and vendor selection
-   **Contract Management**: Supplier contracts and terms negotiation
-   **Procurement Analytics**: Spend analysis and cost optimization
-   **Invoice Matching**: Three-way matching and approval workflows

## 🏗️ Architecture

### Domain Model

```
Supplier (Aggregate Root)
├── SupplierId (Identity)
├── SupplierInfo (Entity)
├── Contracts (Entity Collection)
├── PerformanceMetrics (Value Object)
└── PaymentTerms (Value Object)

PurchaseOrder (Aggregate Root)
├── PurchaseOrderId (Identity)
├── SupplierId (Reference)
├── Items (Entity Collection)
├── ApprovalStatus (Value Object)
└── DeliveryInfo (Value Object)
```

### API Endpoints

```
GET    /api/v1/procurement/suppliers      # List suppliers
POST   /api/v1/procurement/suppliers      # Create supplier
GET    /api/v1/procurement/purchase-orders # List purchase orders
POST   /api/v1/procurement/purchase-orders # Create purchase order
```

### Domain Events Published

```kotlin
- SupplierCreatedEvent
- PurchaseOrderCreatedEvent
- PurchaseOrderApprovedEvent
- GoodsReceivedEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8090
-   Dev UI: http://localhost:8090/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Procurement Domain Team

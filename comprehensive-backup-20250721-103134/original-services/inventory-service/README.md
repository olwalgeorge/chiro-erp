# Inventory Service

## Overview

The Inventory Service manages product catalog, stock levels, and inventory operations across the Chiro ERP system. It provides real-time inventory tracking, automated reordering, and integrates with sales, procurement, and manufacturing processes.

## 🎯 Business Purpose

This service handles:

-   **Product Catalog Management**: Product definitions, variants, and hierarchies
-   **Stock Level Tracking**: Real-time inventory quantities across locations
-   **Warehouse Management**: Multi-location inventory and transfers
-   **Automated Reordering**: Stock level alerts and automatic purchase requests
-   **Inventory Valuation**: FIFO/LIFO/Weighted average costing methods
-   **Cycle Counting**: Physical inventory reconciliation

## 🏗️ Architecture

### Domain Model

```
Product (Aggregate Root)
├── ProductId (Identity)
├── SKU (Value Object)
├── ProductInfo (Entity)
├── Variants (Entity Collection)
├── PricingTiers (Value Object)
└── Categories (Reference Collection)

InventoryItem (Aggregate Root)
├── InventoryItemId (Identity)
├── ProductId (Reference)
├── LocationId (Reference)
├── StockLevel (Value Object)
├── ReorderPoint (Value Object)
└── MovementHistory (Entity Collection)
```

### API Endpoints

#### Product Management

```
GET    /api/v1/inventory/products         # List products
POST   /api/v1/inventory/products         # Create product
GET    /api/v1/inventory/products/{id}    # Get product details
PUT    /api/v1/inventory/products/{id}    # Update product
DELETE /api/v1/inventory/products/{id}    # Deactivate product
```

#### Stock Management

```
GET    /api/v1/inventory/stock            # Get stock levels
POST   /api/v1/inventory/stock/adjust     # Stock adjustment
POST   /api/v1/inventory/stock/transfer   # Inter-location transfer
GET    /api/v1/inventory/stock/movements  # Stock movement history
```

### Domain Events Published

```kotlin
- ProductCreatedEvent
- ProductUpdatedEvent
- StockAdjustedEvent
- LowStockAlertEvent
- StockTransferredEvent
- ReorderPointReachedEvent
```

## 🚀 Getting Started

### Local Development Setup

-   Service: http://localhost:8084
-   Dev UI: http://localhost:8084/q/dev/

### Configuration

```yaml
inventory:
    default-reorder-point: 10
    low-stock-threshold: 5
    auto-reorder-enabled: true
    costing-method: "FIFO"
```

---

**Service Version**: 1.0.0
**Maintainer**: Inventory Domain Team

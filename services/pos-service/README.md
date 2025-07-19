# POS Service

## Overview

The POS (Point of Sale) Service manages retail transactions, payment processing, and in-store operations across the Chiro ERP system.

## 🎯 Business Purpose

This service handles:

-   **Transaction Processing**: Retail sales and payment processing
-   **Product Lookup**: Barcode scanning and product information
-   **Payment Integration**: Multiple payment method support
-   **Receipt Management**: Digital and printed receipt generation
-   **Discount Management**: Promotional pricing and coupon handling
-   **Cash Management**: Till operations and cash reconciliation

## 🏗️ Architecture

### Domain Model

```
Transaction (Aggregate Root)
├── TransactionId (Identity)
├── StoreId (Reference)
├── CashierId (Reference)
├── Items (Entity Collection)
├── Payments (Entity Collection)
├── Discounts (Value Object Collection)
└── Status (Value Object)
```

### API Endpoints

```
POST   /api/v1/pos/transactions           # Process transaction
GET    /api/v1/pos/products/{barcode}     # Product lookup
POST   /api/v1/pos/payments               # Process payment
GET    /api/v1/pos/receipts/{id}          # Get receipt
```

### Domain Events Published

```kotlin
- TransactionProcessedEvent
- PaymentProcessedEvent
- ReceiptGeneratedEvent
- RefundProcessedEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8097
-   Dev UI: http://localhost:8097/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Retail Domain Team

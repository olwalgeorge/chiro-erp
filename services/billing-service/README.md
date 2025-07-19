# Billing Service

## Overview

The Billing Service handles subscription billing, invoice generation, payment processing, and revenue recognition for the Chiro ERP system. It manages recurring billing cycles and integrates with payment gateways.

## 🎯 Business Purpose

This service handles:

-   **Subscription Billing**: Recurring billing for SaaS subscriptions
-   **Invoice Generation**: Automated invoice creation and delivery
-   **Payment Processing**: Integration with payment gateways
-   **Revenue Recognition**: Deferred revenue and accounting compliance
-   **Billing Analytics**: Revenue reporting and forecasting
-   **Dunning Management**: Automated collection processes

## 🏗️ Architecture

### Domain Model

```
Subscription (Aggregate Root)
├── SubscriptionId (Identity)
├── CustomerId (Reference)
├── Plan (Value Object)
├── BillingCycle (Value Object)
├── Status (Value Object)
└── Invoices (Entity Collection)

Bill (Aggregate Root)
├── BillId (Identity)
├── SubscriptionId (Reference)
├── Amount (Value Object)
├── DueDate (Value Object)
├── Status (Value Object)
└── PaymentHistory (Entity Collection)
```

### API Endpoints

#### Subscription Management

```
GET    /api/v1/billing/subscriptions      # List subscriptions
POST   /api/v1/billing/subscriptions      # Create subscription
GET    /api/v1/billing/subscriptions/{id} # Get subscription details
PUT    /api/v1/billing/subscriptions/{id} # Update subscription
POST   /api/v1/billing/subscriptions/{id}/cancel # Cancel subscription
```

#### Billing Operations

```
POST   /api/v1/billing/invoices/generate  # Generate invoices
GET    /api/v1/billing/invoices           # List invoices
POST   /api/v1/billing/payments/process   # Process payment
GET    /api/v1/billing/reports/revenue    # Revenue reports
```

### Domain Events Published

```kotlin
- SubscriptionCreatedEvent
- InvoiceGeneratedEvent
- PaymentProcessedEvent
- PaymentFailedEvent
- SubscriptionCancelledEvent
```

## 🚀 Getting Started

### Local Development Setup

-   Service: http://localhost:8087
-   Dev UI: http://localhost:8087/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Finance Domain Team

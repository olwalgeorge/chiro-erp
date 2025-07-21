# Finance Service

## Overview

The Finance Service manages financial transactions, accounting, reporting, and compliance across the Chiro ERP system. It provides comprehensive financial management including accounts receivable, accounts payable, and general ledger functionality.

## 🎯 Business Purpose

This service handles:

-   **General Ledger**: Chart of accounts and double-entry bookkeeping
-   **Accounts Receivable**: Customer invoices and payment tracking
-   **Accounts Payable**: Vendor bills and payment processing
-   **Financial Reporting**: Balance sheet, P&L, cash flow statements
-   **Tax Management**: Tax calculations and compliance reporting
-   **Budget Management**: Budget planning and variance analysis

## 🏗️ Architecture

### Domain Model

```
Account (Aggregate Root)
├── AccountId (Identity)
├── AccountCode (Value Object)
├── AccountType (Value Object)
├── Balance (Value Object)
└── Transactions (Entity Collection)

Invoice (Aggregate Root)
├── InvoiceId (Identity)
├── CustomerId (Reference)
├── InvoiceNumber (Value Object)
├── LineItems (Entity Collection)
├── PaymentTerms (Value Object)
└── PaymentStatus (Value Object)
```

### API Endpoints

#### General Ledger

```
GET    /api/v1/finance/accounts           # List accounts
POST   /api/v1/finance/accounts           # Create account
GET    /api/v1/finance/journal-entries    # List journal entries
POST   /api/v1/finance/journal-entries    # Create journal entry
```

#### Invoicing

```
GET    /api/v1/finance/invoices           # List invoices
POST   /api/v1/finance/invoices           # Create invoice
GET    /api/v1/finance/invoices/{id}      # Get invoice details
POST   /api/v1/finance/invoices/{id}/send # Send invoice to customer
POST   /api/v1/finance/payments           # Record payment
```

### Domain Events Published

```kotlin
- InvoiceCreatedEvent
- InvoiceSentEvent
- PaymentReceivedEvent
- JournalEntryCreatedEvent
- AccountBalanceUpdatedEvent
```

## 🚀 Getting Started

### Local Development Setup

-   Service: http://localhost:8086
-   Dev UI: http://localhost:8086/q/dev/

### Configuration

```yaml
finance:
    default-currency: "USD"
    fiscal-year-start: "01-01"
    auto-invoice-numbering: true
    tax-calculation-enabled: true
    multi-currency-enabled: false
```

---

**Service Version**: 1.0.0
**Maintainer**: Finance Domain Team

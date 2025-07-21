# CRM Service

## Overview

The CRM Service manages customer relationships, contacts, leads, and customer interactions across the Chiro ERP system. It provides comprehensive customer lifecycle management and sales pipeline support.

## 🎯 Business Purpose

This service handles:

-   **Customer Management**: Complete customer profiles and contact information
-   **Lead Management**: Lead capture, qualification, and nurturing
-   **Opportunity Tracking**: Sales pipeline and opportunity management
-   **Contact Management**: Multiple contacts per customer organization
-   **Activity Tracking**: Customer interaction history and notes
-   **Customer Segmentation**: Grouping customers for targeted marketing

## 🏗️ Architecture

### Domain Model

```
Customer (Aggregate Root)
├── CustomerId (Identity)
├── CustomerInfo (Entity)
├── Contacts (Entity Collection)
├── Addresses (Value Object Collection)
├── CustomerStatus (Value Object)
└── Preferences (Value Object)

Lead (Aggregate Root)
├── LeadId (Identity)
├── LeadInfo (Entity)
├── Source (Value Object)
├── Status (Value Object)
└── Activities (Entity Collection)
```

### API Endpoints

#### Customer Management

```
GET    /api/v1/crm/customers              # List customers
POST   /api/v1/crm/customers              # Create customer
GET    /api/v1/crm/customers/{id}         # Get customer details
PUT    /api/v1/crm/customers/{id}         # Update customer
DELETE /api/v1/crm/customers/{id}         # Deactivate customer
```

#### Lead Management

```
GET    /api/v1/crm/leads                  # List leads
POST   /api/v1/crm/leads                  # Create lead
GET    /api/v1/crm/leads/{id}             # Get lead details
PUT    /api/v1/crm/leads/{id}             # Update lead
POST   /api/v1/crm/leads/{id}/convert     # Convert lead to customer
```

### Domain Events Published

```kotlin
- CustomerCreatedEvent
- CustomerUpdatedEvent
- LeadCreatedEvent
- LeadQualifiedEvent
- LeadConvertedEvent
- ContactUpdatedEvent
```

## 🚀 Getting Started

### Local Development Setup

-   Service: http://localhost:8085
-   Dev UI: http://localhost:8085/q/dev/

### Configuration

```yaml
crm:
    lead-scoring-enabled: true
    auto-qualify-threshold: 75
    customer-merge-enabled: true
    duplicate-detection-enabled: true
```

---

**Service Version**: 1.0.0
**Maintainer**: CRM Domain Team

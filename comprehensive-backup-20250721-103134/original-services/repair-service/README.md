# Repair Service

## Overview

The Repair Service manages equipment repairs, RMA processes, and repair shop operations across the Chiro ERP system.

## 🎯 Business Purpose

This service handles:

-   **Repair Order Management**: Repair requests and work order tracking
-   **RMA Processing**: Return merchandise authorization workflows
-   **Repair Shop Operations**: Work center management and capacity planning
-   **Parts & Labor Tracking**: Repair costs and resource utilization
-   **Warranty Management**: Warranty claims and coverage validation
-   **Quality Control**: Repair validation and testing procedures

## 🏗️ Architecture

### Domain Model

```
RepairOrder (Aggregate Root)
├── RepairOrderId (Identity)
├── CustomerId (Reference)
├── ItemId (Reference)
├── RepairType (Value Object)
├── EstimatedCost (Value Object)
├── Status (Value Object)
└── PartsUsed (Entity Collection)
```

### API Endpoints

```
GET    /api/v1/repair/orders              # List repair orders
POST   /api/v1/repair/orders              # Create repair order
POST   /api/v1/repair/rma                 # Create RMA
GET    /api/v1/repair/warranty/{id}       # Check warranty status
```

### Domain Events Published

```kotlin
- RepairOrderCreatedEvent
- RMACreatedEvent
- RepairCompletedEvent
- WarrantyClaimProcessedEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8096
-   Dev UI: http://localhost:8096/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Service Domain Team

## Key Features

-   Repair order creation and management
-   Product diagnostic tracking and reporting
-   Parts and labor consumption recording
-   Warranty claim processing and validation
-   Repair cost calculation and billing integration
-   Integration with CRM, Product, Inventory, and Sales services

## Architecture

This service follows Clean Architecture (Hexagonal Architecture) with clear separation of concerns:

### Application Layer

-   **Commands**: Repair order creation, status updates, parts consumption, labor logging
-   **Queries**: Repair details, progress history, warranty claims, cost analysis
-   **Events**: Integration events from CRM, Product, Inventory, and Sales services
-   **Services**: Business logic orchestration for repair workflows

### Domain Layer

-   **Aggregates**: RepairOrder, WarrantyClaim
-   **Entities**: RepairActivity, RepairPart, RepairLaborEntry, DiagnosticReport, ReturnAuthorization
-   **Value Objects**: Serial numbers, repair statuses, warranty status, time durations
-   **Events**: Domain events for repair lifecycle changes
-   **Services**: Core business logic for cost calculation, warranty validation, and workflow management

### Infrastructure Layer

-   **REST API**: Repair endpoints for external system integration
-   **GraphQL API**: Flexible querying for complex repair data
-   **Kafka Integration**: Event-driven communication with other services
-   **Database**: PostgreSQL with JPA/Hibernate for persistence
-   **External Integrations**: Billing, CRM sync, and notification services

## Service Interactions

-   **Consumes Events From**: CRM (customer issues), Product (repairable items), Inventory (parts availability), Sales (warranty info)
-   **Publishes Events To**: Finance (repair costs), Inventory (parts consumption)
-   **Direct Integration**: External billing, CRM sync, and notification services

## Getting Started

1. Ensure PostgreSQL and Kafka are running
2. Configure database connection in `application.properties`
3. Run the service: `./gradlew quarkusDev`
4. Access API at: `http://localhost:8085`

## API Endpoints

-   GET `/repair/orders` - List repair orders
-   POST `/repair/orders` - Create repair order
-   GET `/repair/orders/{id}` - Get repair order details
-   POST `/repair/orders/{id}/activities` - Record repair activity
-   POST `/repair/orders/{id}/complete` - Complete repair order
-   GET `/repair/warranty-claims` - List warranty claims
-   POST `/repair/warranty-claims` - Submit warranty claim

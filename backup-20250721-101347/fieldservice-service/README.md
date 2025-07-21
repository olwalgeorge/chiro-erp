# Field Service

## Overview

The Field Service manages on-site service operations, technician scheduling, and mobile workforce management across the Chiro ERP system.

## 🎯 Business Purpose

This service handles:

-   **Service Request Management**: Customer service requests and work orders
-   **Technician Scheduling**: Field technician dispatch and route optimization
-   **Mobile Operations**: Mobile app support for field workers
-   **Asset Management**: Customer equipment and service history
-   **Parts Management**: Field inventory and parts ordering
-   **Service Analytics**: SLA compliance and performance metrics

## 🏗️ Architecture

### Domain Model

```
ServiceRequest (Aggregate Root)
├── ServiceRequestId (Identity)
├── CustomerId (Reference)
├── AssetId (Reference)
├── TechnicianId (Reference)
├── ServiceType (Value Object)
├── Schedule (Value Object)
└── Status (Value Object)
```

### API Endpoints

```
GET    /api/v1/fieldservice/requests      # List service requests
POST   /api/v1/fieldservice/requests      # Create service request
GET    /api/v1/fieldservice/schedule      # Technician schedules
POST   /api/v1/fieldservice/dispatch      # Dispatch technician
```

### Domain Events Published

```kotlin
- ServiceRequestCreatedEvent
- TechnicianDispatchedEvent
- ServiceCompletedEvent
- PartsUsedEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8095
-   Dev UI: http://localhost:8095/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Service Domain Team

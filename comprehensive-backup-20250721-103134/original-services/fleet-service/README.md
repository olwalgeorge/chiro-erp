# Fleet Service

## Overview

The Fleet Service manages vehicle fleet operations, maintenance scheduling, and driver management across the Chiro ERP system.

## 🎯 Business Purpose

This service handles:

-   **Vehicle Management**: Fleet inventory and vehicle lifecycle
-   **Maintenance Scheduling**: Preventive and corrective maintenance
-   **Driver Management**: Driver assignments and performance tracking
-   **Fuel Management**: Fuel consumption tracking and optimization
-   **Route Optimization**: Delivery route planning and optimization
-   **Compliance**: Vehicle registration, insurance, and regulatory compliance

## 🏗️ Architecture

### Domain Model

```
Vehicle (Aggregate Root)
├── VehicleId (Identity)
├── VehicleInfo (Entity)
├── MaintenanceHistory (Entity Collection)
├── FuelRecords (Entity Collection)
└── AssignedDriver (Reference)
```

### API Endpoints

```
GET    /api/v1/fleet/vehicles             # List vehicles
POST   /api/v1/fleet/vehicles             # Add vehicle
GET    /api/v1/fleet/maintenance          # Maintenance schedules
POST   /api/v1/fleet/fuel-logs            # Log fuel consumption
```

### Domain Events Published

```kotlin
- VehicleAddedEvent
- MaintenanceScheduledEvent
- MaintenanceCompletedEvent
- FuelRecordedEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8094
-   Dev UI: http://localhost:8094/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Operations Domain Team

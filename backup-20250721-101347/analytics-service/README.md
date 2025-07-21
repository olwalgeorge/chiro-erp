# Analytics Service

## Overview

The Analytics Service provides business intelligence, reporting, and data analytics across the Chiro ERP system. It aggregates data from all services to provide insights and decision-making support.

## 🎯 Business Purpose

This service handles:

-   **Business Intelligence**: KPI dashboards and performance metrics
-   **Data Aggregation**: Cross-service data collection and analysis
-   **Custom Reporting**: Ad-hoc reports and scheduled report generation
-   **Predictive Analytics**: Forecasting and trend analysis
-   **Real-time Analytics**: Live dashboards and monitoring
-   **Data Export**: Business data export and integration support

## 🏗️ Architecture

### Domain Model

```
Report (Aggregate Root)
├── ReportId (Identity)
├── ReportDefinition (Entity)
├── DataSources (Entity Collection)
├── Parameters (Value Object Collection)
├── Schedule (Value Object)
└── Recipients (Entity Collection)

Dashboard (Aggregate Root)
├── DashboardId (Identity)
├── Widgets (Entity Collection)
├── Layout (Value Object)
└── Permissions (Value Object)
```

### API Endpoints

```
GET    /api/v1/analytics/reports          # List reports
POST   /api/v1/analytics/reports          # Create report
GET    /api/v1/analytics/reports/{id}/run # Execute report
GET    /api/v1/analytics/dashboards       # List dashboards
POST   /api/v1/analytics/kpis             # Get KPI data
```

### Domain Events Published

```kotlin
- ReportGeneratedEvent
- DashboardCreatedEvent
- DataAnalysisCompletedEvent
- KPIThresholdExceededEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8093
-   Dev UI: http://localhost:8093/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Analytics Domain Team

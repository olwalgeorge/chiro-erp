# HR Service

## Overview

The HR Service manages human resources, payroll, employee lifecycle, and workforce management across the Chiro ERP system.

## 🎯 Business Purpose

This service handles:

-   **Employee Management**: Employee profiles, onboarding, and lifecycle
-   **Payroll Processing**: Salary calculations, deductions, and payments
-   **Time & Attendance**: Time tracking, leave management, and scheduling
-   **Performance Management**: Reviews, goals, and career development
-   **Benefits Administration**: Health insurance, retirement plans, and benefits
-   **Compliance**: Labor law compliance and reporting

## 🏗️ Architecture

### Domain Model

```
Employee (Aggregate Root)
├── EmployeeId (Identity)
├── PersonalInfo (Entity)
├── Employment (Entity)
├── Compensation (Value Object)
├── Benefits (Entity Collection)
└── PerformanceRecords (Entity Collection)
```

### API Endpoints

```
GET    /api/v1/hr/employees               # List employees
POST   /api/v1/hr/employees               # Create employee
GET    /api/v1/hr/payroll                 # Payroll processing
POST   /api/v1/hr/timesheet               # Submit timesheet
```

### Domain Events Published

```kotlin
- EmployeeHiredEvent
- EmployeeTerminatedEvent
- PayrollProcessedEvent
- LeaveRequestedEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8089
-   Dev UI: http://localhost:8089/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: HR Domain Team

# Project Service

## Overview

The Project Service manages project planning, resource allocation, task tracking, and project delivery across the Chiro ERP system.

## 🎯 Business Purpose

This service handles:

-   **Project Planning**: Project creation, milestone planning, and scheduling
-   **Task Management**: Work breakdown structure and task assignments
-   **Resource Allocation**: Team member assignment and capacity planning
-   **Time Tracking**: Project time logging and effort reporting
-   **Budget Management**: Project budgets and cost tracking
-   **Delivery Management**: Project deliverables and client communication

## 🏗️ Architecture

### Domain Model

```
Project (Aggregate Root)
├── ProjectId (Identity)
├── CustomerId (Reference)
├── ProjectInfo (Entity)
├── Tasks (Entity Collection)
├── Budget (Value Object)
├── Timeline (Value Object)
└── Resources (Entity Collection)
```

### API Endpoints

```
GET    /api/v1/projects                   # List projects
POST   /api/v1/projects                   # Create project
GET    /api/v1/projects/{id}/tasks        # List project tasks
POST   /api/v1/projects/{id}/tasks        # Create task
POST   /api/v1/projects/time-entries      # Log time entry
```

### Domain Events Published

```kotlin
- ProjectCreatedEvent
- TaskCreatedEvent
- TaskCompletedEvent
- TimeLoggedEvent
- ProjectCompletedEvent
```

## 🚀 Getting Started

-   Service: http://localhost:8092
-   Dev UI: http://localhost:8092/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Project Domain Team

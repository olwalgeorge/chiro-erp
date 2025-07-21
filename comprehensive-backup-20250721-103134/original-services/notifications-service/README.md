# Notifications Service

## Overview

The Notifications Service provides centralized communication management across the Chiro ERP system. It handles email, SMS, push notifications, and in-app messaging with template management and delivery tracking.

## 🎯 Business Purpose

This service handles:

-   **Multi-Channel Messaging**: Email, SMS, push notifications, in-app alerts
-   **Template Management**: Dynamic message templates with personalization
-   **Delivery Tracking**: Message status and engagement analytics
-   **User Preferences**: Channel preferences and notification settings
-   **Batch Processing**: Bulk messaging and campaign management
-   **Integration Support**: Third-party communication service integration

## 🏗️ Architecture

### Domain Model

```
Notification (Aggregate Root)
├── NotificationId (Identity)
├── RecipientId (Reference)
├── Channel (Value Object)
├── Template (Value Object)
├── Content (Value Object)
├── Status (Value Object)
└── DeliveryAttempts (Entity Collection)

NotificationTemplate (Aggregate Root)
├── TemplateId (Identity)
├── Name (Value Object)
├── Channel (Value Object)
├── Content (Value Object)
└── Variables (Value Object Collection)
```

### API Endpoints

#### Notification Management

```
POST   /api/v1/notifications/send         # Send notification
GET    /api/v1/notifications              # List notifications
GET    /api/v1/notifications/{id}         # Get notification status
POST   /api/v1/notifications/bulk         # Send bulk notifications
```

#### Template Management

```
GET    /api/v1/notifications/templates    # List templates
POST   /api/v1/notifications/templates    # Create template
GET    /api/v1/notifications/templates/{id} # Get template
PUT    /api/v1/notifications/templates/{id} # Update template
```

### Domain Events Published

```kotlin
- NotificationSentEvent
- NotificationDeliveredEvent
- NotificationFailedEvent
- TemplateCreatedEvent
- BulkNotificationCompletedEvent
```

## 🚀 Getting Started

### Local Development Setup

-   Service: http://localhost:8088
-   Dev UI: http://localhost:8088/q/dev/

---

**Service Version**: 1.0.0
**Maintainer**: Platform Team

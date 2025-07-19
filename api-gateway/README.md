# API Gateway

## Overview

The API Gateway serves as the single entry point for all external requests to the Chiro ERP system. It provides routing, authentication, rate limiting, and API management capabilities.

## 🎯 Business Purpose

This service handles:

-   **Request Routing**: Route requests to appropriate microservices
-   **Authentication & Authorization**: JWT validation and user context
-   **Rate Limiting**: API usage throttling and abuse prevention
-   **Load Balancing**: Distribute requests across service instances
-   **API Documentation**: Unified API documentation and testing
-   **Security**: SSL termination, CORS, and security headers

## 🏗️ Architecture

### Domain Model

```
Route (Configuration)
├── Path (Value Object)
├── Method (Value Object)
├── TargetService (Reference)
├── AuthRequired (Value Object)
└── RateLimit (Value Object)

ApiKey (Aggregate Root)
├── ApiKeyId (Identity)
├── ClientId (Reference)
├── Key (Value Object)
├── Permissions (Value Object Collection)
└── Usage (Entity Collection)
```

### API Endpoints

#### Core Routes

```
GET    /api/v1/health                     # Gateway health check
GET    /api/v1/metrics                    # Gateway metrics
GET    /api/v1/docs                       # API documentation
GET    /api/v1/swagger                    # Swagger UI
```

#### Service Proxying

-   `/api/v1/users/**` → User Management Service
-   `/api/v1/tenants/**` → Tenant Management Service
-   `/api/v1/sales/**` → Sales Service
-   `/api/v1/inventory/**` → Inventory Service
-   `/api/v1/crm/**` → CRM Service
-   `/api/v1/finance/**` → Finance Service
-   And all other services...

### Features

-   **Circuit Breaker**: Prevent cascade failures
-   **Request Logging**: Comprehensive access logs
-   **Response Caching**: Cache frequent requests
-   **API Versioning**: Support multiple API versions
-   **WebSocket Support**: Real-time communication proxy

## 🚀 Getting Started

### Local Development Setup

-   Service: http://localhost:8080
-   Dev UI: http://localhost:8080/q/dev/
-   Swagger UI: http://localhost:8080/swagger-ui/

### Configuration

```yaml
gateway:
    cors:
        allowed-origins: ["http://localhost:3000"]
        allowed-methods: ["GET", "POST", "PUT", "DELETE"]
    rate-limiting:
        requests-per-minute: 1000
        burst-size: 100
    services:
        user-management:
            url: "http://user-management-service:8080"
            health-check: "/q/health"
```

## 🔒 Security Features

### Authentication

-   JWT token validation
-   API key authentication for external integrations
-   OAuth 2.0/OIDC support

### Authorization

-   Role-based access control
-   Resource-level permissions
-   Tenant-based isolation

### Security Headers

-   HTTPS enforcement
-   CSRF protection
-   Security headers (HSTS, CSP, etc.)

---

**Service Version**: 1.0.0
**Maintainer**: Platform Team

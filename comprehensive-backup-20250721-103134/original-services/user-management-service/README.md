# User Management Service

## Overview

The User Management Service is a foundational microservice in the Chiro ERP system responsible for user authentication, authorization, and user lifecycle management. It provides centralized user identity management across all services in the system.

## 🎯 Business Purpose

This service handles:

-   **User Authentication**: JWT-based authentication and token management
-   **User Registration & Lifecycle**: User creation, profile management, deactivation
-   **Role-Based Access Control**: User roles and permissions management
-   **Multi-Tenant User Management**: User isolation and tenant-specific access
-   **Password Management**: Secure password storage, reset, and policy enforcement
-   **Audit Trail**: User activity logging and security event tracking

## 🏗️ Architecture

### Domain Model

```
User (Aggregate Root)
├── UserId (Identity)
├── TenantId (Multi-tenancy)
├── Email (Value Object)
├── Password (Value Object)
├── Profile (Entity)
├── Roles (Collection)
└── AuditLog (Entity)
```

### Key Aggregates

-   **User**: Core user entity with authentication credentials
-   **Role**: Permission sets and access control definitions
-   **UserSession**: Active user sessions and JWT token management

### Bounded Context

-   **Core Domain**: User identity and authentication
-   **Supporting Domain**: Authorization and access control
-   **Generic Domain**: Audit logging and user preferences

## 🔧 Technical Specifications

### Technology Stack

-   **Framework**: Quarkus 3.x with Kotlin
-   **Database**: PostgreSQL with Hibernate ORM + Panache
-   **Messaging**: Kafka for event publishing
-   **Security**: JWT tokens, BCrypt password hashing
-   **Testing**: JUnit 5, Testcontainers, MockK

### API Endpoints

#### Authentication

```
POST   /api/v1/auth/login           # User authentication
POST   /api/v1/auth/refresh         # Token refresh
POST   /api/v1/auth/logout          # User logout
POST   /api/v1/auth/forgot-password # Password reset request
POST   /api/v1/auth/reset-password  # Password reset confirmation
```

#### User Management

```
GET    /api/v1/users               # List users (paginated)
POST   /api/v1/users               # Create new user
GET    /api/v1/users/{id}          # Get user by ID
PUT    /api/v1/users/{id}          # Update user
DELETE /api/v1/users/{id}          # Deactivate user
GET    /api/v1/users/me            # Get current user profile
PUT    /api/v1/users/me            # Update current user profile
```

#### Role Management

```
GET    /api/v1/roles               # List roles
POST   /api/v1/roles               # Create role
GET    /api/v1/roles/{id}          # Get role details
PUT    /api/v1/roles/{id}          # Update role
DELETE /api/v1/roles/{id}          # Delete role
POST   /api/v1/users/{id}/roles    # Assign role to user
DELETE /api/v1/users/{id}/roles/{roleId} # Remove role from user
```

### Domain Events Published

```kotlin
- UserRegisteredEvent
- UserActivatedEvent
- UserDeactivatedEvent
- UserPasswordChangedEvent
- UserRoleAssignedEvent
- UserRoleRemovedEvent
- UserLoginEvent
- UserLogoutEvent
```

### External Dependencies

-   **Notifications Service**: For email notifications (password reset, welcome emails)
-   **Tenant Management Service**: For tenant validation and multi-tenancy
-   **Audit Service**: For centralized audit logging

## 🚀 Getting Started

### Prerequisites

-   JDK 21+
-   Docker & Docker Compose
-   Gradle 8.x

### Local Development Setup

1. **Start Dependencies**

```bash
# From project root
docker-compose up postgres kafka zookeeper
```

2. **Run Database Migrations**

```bash
./gradlew flywayMigrate
```

3. **Start Service in Dev Mode**

```bash
./gradlew quarkusDev
```

4. **Access Development UI**

-   Service: http://localhost:8081
-   Dev UI: http://localhost:8081/q/dev/
-   Health Check: http://localhost:8081/q/health
-   Metrics: http://localhost:8081/q/metrics

### Configuration

Key configuration properties in `application.yml`:

```yaml
quarkus:
    datasource:
        db-kind: postgresql
        jdbc:
            url: jdbc:postgresql://localhost:5432/chiro_erp
            username: chiro
            password: chiro123

    kafka:
        bootstrap-servers: localhost:9092

    smallrye-jwt:
        enabled: true

security:
    jwt:
        secret-key: ${JWT_SECRET_KEY:default-secret}
        expiry-hours: 24
    password:
        min-length: 8
        require-uppercase: true
        require-numbers: true
```

## 🧪 Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# All tests
./gradlew check
```

### Test Structure

-   **Unit Tests**: Domain logic, services, utilities
-   **Integration Tests**: Database interactions, Kafka publishing
-   **Contract Tests**: API contract verification
-   **Security Tests**: Authentication and authorization flows

### Test Data

Use the provided test data seeds:

```bash
./gradlew loadTestData
```

## 📊 Monitoring & Observability

### Health Checks

-   **Liveness**: `/q/health/live` - Service is running
-   **Readiness**: `/q/health/ready` - Service can accept traffic
-   **Database**: Connection pool status
-   **Kafka**: Producer connectivity

### Metrics

Key metrics exposed at `/q/metrics`:

-   `user_logins_total` - Total user login attempts
-   `user_registrations_total` - Total user registrations
-   `active_sessions_count` - Current active user sessions
-   `jwt_tokens_issued_total` - JWT tokens issued
-   `password_resets_total` - Password reset requests

### Logging

Structured JSON logging with correlation IDs:

```json
{
    "timestamp": "2025-07-19T10:30:00Z",
    "level": "INFO",
    "logger": "org.chiro.user.UserService",
    "message": "User login successful",
    "userId": "user123",
    "tenantId": "tenant456",
    "correlationId": "req789"
}
```

## 🔒 Security

### Authentication Flow

1. User submits credentials to `/api/v1/auth/login`
2. Service validates credentials against database
3. On success, JWT token is generated and returned
4. Client includes JWT in `Authorization: Bearer <token>` header
5. Service validates JWT on protected endpoints

### Security Features

-   **Password Hashing**: BCrypt with configurable rounds
-   **JWT Security**: RSA256 signing, configurable expiry
-   **Rate Limiting**: Login attempt throttling
-   **Account Lockout**: Temporary lockout after failed attempts
-   **Audit Logging**: All authentication events logged

### Multi-Tenancy

-   Tenant isolation at database level using `tenant_id` column
-   JWT tokens include tenant context
-   All queries automatically filtered by tenant

## 📚 API Documentation

### Interactive API Docs

-   **Swagger UI**: http://localhost:8081/q/swagger-ui/
-   **OpenAPI Spec**: http://localhost:8081/q/openapi

### Authentication Example

```bash
# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com", "password": "password123"}'

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "user": {
    "id": "123",
    "email": "admin@example.com",
    "roles": ["ADMIN"]
  }
}

# Use token in subsequent requests
curl -X GET http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

## 🚢 Deployment

### Docker Build

```bash
# Build container image
./gradlew build -Dquarkus.container-image.build=true

# Run container
docker run -p 8081:8080 chiro-erp/user-management-service:latest
```

### Kubernetes Deployment

```yaml
# k8s/deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
    name: user-management-service
spec:
    replicas: 3
    selector:
        matchLabels:
            app: user-management-service
    template:
        metadata:
            labels:
                app: user-management-service
        spec:
            containers:
                - name: user-management-service
                  image: chiro-erp/user-management-service:latest
                  ports:
                      - containerPort: 8080
                  env:
                      - name: DATABASE_URL
                        valueFrom:
                            secretKeyRef:
                                name: db-secret
                                key: url
```

## 📋 Database Schema

### Key Tables

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    permissions TEXT[], -- JSON array of permissions
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles junction table
CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id),
    role_id UUID REFERENCES roles(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);
```

## 🤝 Contributing

### Development Workflow

1. Create feature branch from `develop`
2. Implement changes with tests
3. Run quality checks: `./gradlew check`
4. Create pull request with description
5. Pass code review and CI checks
6. Merge to `develop`

### Code Style

-   Follow Kotlin coding conventions
-   Use ktlint for formatting
-   Maintain test coverage above 80%
-   Document public APIs with KDoc

## 📞 Support

### Troubleshooting

-   **Service won't start**: Check database connectivity and Kafka availability
-   **Authentication fails**: Verify JWT configuration and secret keys
-   **Database errors**: Ensure migrations have run successfully

### Contacts

-   **Team**: Platform Team
-   **Slack**: #user-management-service
-   **Email**: platform-team@chiro-erp.com

## 📄 License

Copyright (c) 2025 Chiro ERP. All rights reserved.

---

**Service Version**: 1.0.0
**Last Updated**: July 2025
**Maintainer**: Platform Team

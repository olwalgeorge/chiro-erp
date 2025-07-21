# Tenant Management Service

## Overview

The Tenant Management Service is a foundational microservice responsible for multi-tenancy support across the entire Chiro ERP system. It manages tenant lifecycle, configurations, and ensures proper data isolation between different organizations using the platform.

## 🎯 Business Purpose

This service handles:

-   **Tenant Onboarding**: New organization registration and setup
-   **Tenant Configuration**: Custom settings, branding, and feature toggles
-   **Data Isolation**: Ensuring secure separation between tenants
-   **Subscription Management**: Plan management and feature access control
-   **Tenant Analytics**: Usage metrics and reporting per tenant
-   **Compliance**: Data residency and regulatory requirements per tenant

## 🏗️ Architecture

### Domain Model

```
Tenant (Aggregate Root)
├── TenantId (Identity)
├── TenantInfo (Entity)
├── Subscription (Entity)
├── Configuration (Value Object)
├── DataResidency (Value Object)
└── UsageMetrics (Entity)
```

### Key Aggregates

-   **Tenant**: Core tenant entity with organizational details
-   **Subscription**: Billing plan and feature access management
-   **TenantConfiguration**: Custom settings and preferences per tenant

### Bounded Context

-   **Core Domain**: Multi-tenant architecture and data isolation
-   **Supporting Domain**: Subscription management and billing integration
-   **Generic Domain**: Configuration management and analytics

## 🔧 Technical Specifications

### Technology Stack

-   **Framework**: Quarkus 3.x with Kotlin
-   **Database**: PostgreSQL with Hibernate ORM + Panache
-   **Messaging**: Kafka for event publishing
-   **Caching**: Redis for tenant configuration caching
-   **Testing**: JUnit 5, Testcontainers, MockK

### API Endpoints

#### Tenant Management

```
GET    /api/v1/tenants              # List tenants (admin only)
POST   /api/v1/tenants              # Create new tenant
GET    /api/v1/tenants/{id}         # Get tenant details
PUT    /api/v1/tenants/{id}         # Update tenant
DELETE /api/v1/tenants/{id}         # Deactivate tenant
GET    /api/v1/tenants/current      # Get current tenant info
```

#### Configuration Management

```
GET    /api/v1/tenants/{id}/config  # Get tenant configuration
PUT    /api/v1/tenants/{id}/config  # Update configuration
GET    /api/v1/tenants/current/config # Get current tenant config
PUT    /api/v1/tenants/current/config # Update current tenant config
```

#### Subscription Management

```
GET    /api/v1/tenants/{id}/subscription    # Get subscription details
PUT    /api/v1/tenants/{id}/subscription    # Update subscription
POST   /api/v1/tenants/{id}/subscription/upgrade # Upgrade plan
POST   /api/v1/tenants/{id}/subscription/downgrade # Downgrade plan
```

### Domain Events Published

```kotlin
- TenantCreatedEvent
- TenantActivatedEvent
- TenantDeactivatedEvent
- TenantConfigurationUpdatedEvent
- TenantSubscriptionChangedEvent
- TenantUsageLimitExceededEvent
- TenantDataExportRequestedEvent
```

### External Dependencies

-   **Billing Service**: For subscription and payment processing
-   **User Management Service**: For tenant admin user creation
-   **Notifications Service**: For tenant communication

## 🚀 Getting Started

### Prerequisites

-   JDK 21+
-   Docker & Docker Compose
-   Gradle 8.x

### Local Development Setup

1. **Start Dependencies**

```bash
# From project root
docker-compose up postgres kafka zookeeper redis
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

-   Service: http://localhost:8082
-   Dev UI: http://localhost:8082/q/dev/
-   Health Check: http://localhost:8082/q/health
-   Metrics: http://localhost:8082/q/metrics

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

    redis:
        hosts: redis://localhost:6379

tenant:
    default-plan: "basic"
    max-users-per-tenant: 100
    data-retention-days: 365
    allowed-domains: [] # Empty means all domains allowed
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

-   **Unit Tests**: Domain logic, tenant validation, configuration management
-   **Integration Tests**: Database interactions, Redis caching, Kafka publishing
-   **Contract Tests**: API contract verification with dependent services
-   **Multi-tenancy Tests**: Data isolation and tenant context validation

## 📊 Monitoring & Observability

### Health Checks

-   **Liveness**: `/q/health/live` - Service is running
-   **Readiness**: `/q/health/ready` - Service can accept traffic
-   **Database**: Connection pool and tenant database accessibility
-   **Redis**: Cache connectivity and performance
-   **Kafka**: Event publishing capability

### Metrics

Key metrics exposed at `/q/metrics`:

-   `tenants_total` - Total number of active tenants
-   `tenant_creation_rate` - Rate of new tenant creation
-   `tenant_configuration_updates_total` - Configuration change frequency
-   `subscription_changes_total` - Plan upgrade/downgrade events
-   `tenant_usage_violations_total` - Usage limit violations

### Logging

Structured JSON logging with tenant context:

```json
{
    "timestamp": "2025-07-19T10:30:00Z",
    "level": "INFO",
    "logger": "org.chiro.tenant.TenantService",
    "message": "Tenant created successfully",
    "tenantId": "tenant456",
    "tenantName": "Acme Corp",
    "subscriptionPlan": "professional",
    "correlationId": "req789"
}
```

## 🔒 Security

### Multi-Tenancy Security

-   **Data Isolation**: Row-level security with tenant_id filtering
-   **API Security**: Tenant context validation in all requests
-   **Cross-Tenant Prevention**: Strict tenant boundary enforcement
-   **Admin Access**: Super-admin role for cross-tenant operations

### Configuration Security

-   **Sensitive Data**: Encrypted storage of tenant secrets
-   **Access Control**: Role-based configuration management
-   **Audit Trail**: All configuration changes logged and tracked

## 📚 API Documentation

### Interactive API Docs

-   **Swagger UI**: http://localhost:8082/q/swagger-ui/
-   **OpenAPI Spec**: http://localhost:8082/q/openapi

### Tenant Creation Example

```bash
# Create new tenant
curl -X POST http://localhost:8082/api/v1/tenants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "name": "Acme Corporation",
    "domain": "acme.com",
    "adminUser": {
      "email": "admin@acme.com",
      "firstName": "John",
      "lastName": "Admin"
    },
    "subscriptionPlan": "professional",
    "dataResidency": "US",
    "configuration": {
      "timezone": "America/New_York",
      "currency": "USD",
      "locale": "en_US"
    }
  }'

# Response
{
  "id": "tenant-123",
  "name": "Acme Corporation",
  "domain": "acme.com",
  "status": "ACTIVE",
  "subscriptionPlan": "professional",
  "createdAt": "2025-07-19T10:30:00Z",
  "configuration": {
    "timezone": "America/New_York",
    "currency": "USD",
    "locale": "en_US"
  }
}
```

## 🚢 Deployment

### Environment Variables

```bash
# Database
DATABASE_URL=jdbc:postgresql://postgres:5432/chiro_erp
DATABASE_USERNAME=chiro
DATABASE_PASSWORD=chiro123

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Tenant Configuration
DEFAULT_SUBSCRIPTION_PLAN=basic
MAX_TENANTS_PER_INSTANCE=1000
```

### Kubernetes Configuration

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
    name: tenant-management-service
spec:
    replicas: 2
    selector:
        matchLabels:
            app: tenant-management-service
    template:
        metadata:
            labels:
                app: tenant-management-service
        spec:
            containers:
                - name: tenant-management-service
                  image: chiro-erp/tenant-management-service:latest
                  ports:
                      - containerPort: 8080
                  env:
                      - name: DATABASE_URL
                        valueFrom:
                            secretKeyRef:
                                name: db-secret
                                key: url
                  resources:
                      requests:
                          memory: "256Mi"
                          cpu: "100m"
                      limits:
                          memory: "512Mi"
                          cpu: "500m"
```

## 📋 Database Schema

### Key Tables

```sql
-- Tenants table
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) UNIQUE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    subscription_plan VARCHAR(100) NOT NULL,
    data_residency VARCHAR(10) NOT NULL,
    max_users INTEGER DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tenant configurations
CREATE TABLE tenant_configurations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    is_encrypted BOOLEAN DEFAULT false,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, config_key)
);

-- Usage tracking
CREATE TABLE tenant_usage_metrics (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    metric_name VARCHAR(255) NOT NULL,
    metric_value BIGINT NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🤝 Contributing

### Multi-Tenancy Guidelines

-   Always include tenant context in database queries
-   Validate tenant access in all API endpoints
-   Test data isolation thoroughly
-   Document tenant-specific configurations

### Development Standards

-   Follow domain-driven design principles
-   Implement proper error handling for tenant operations
-   Maintain backwards compatibility for configuration changes
-   Use feature flags for tenant-specific features

## 📞 Support

### Common Issues

-   **Tenant Creation Fails**: Check domain uniqueness and admin user validation
-   **Configuration Not Applied**: Verify Redis cache invalidation
-   **Cross-Tenant Data Leak**: Review tenant filtering in queries

### Contacts

-   **Team**: Platform Team
-   **Slack**: #tenant-management-service
-   **Email**: platform-team@chiro-erp.com

---

**Service Version**: 1.0.0
**Last Updated**: July 2025
**Maintainer**: Platform Team

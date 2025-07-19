# System Architecture

## Overview

Chiro-ERP follows a microservices architecture based on Domain-Driven Design (DDD) principles, implementing Clean Architecture patterns for maintainability and scalability.

## Architectural Patterns

### 1. Clean Architecture (Hexagonal Architecture)

Each microservice is structured in three main layers:

```
src/main/kotlin/org/chiro/{service}/
├── application/           # Application Layer
│   ├── dto/              # Data Transfer Objects
│   ├── port/             # Ports (Interfaces)
│   │   ├── incoming/     # Driving Ports (Use Cases)
│   │   └── outgoing/     # Driven Ports (Repositories, External Services)
│   └── service/          # Application Services (Orchestration)
├── domain/               # Domain Layer (Business Logic)
│   ├── aggregate/        # Aggregates (Business Entities)
│   ├── entity/           # Entities
│   ├── event/            # Domain Events
│   ├── exception/        # Domain Exceptions
│   ├── repository/       # Repository Interfaces
│   ├── service/          # Domain Services
│   └── valueobject/      # Value Objects
└── infrastructure/       # Infrastructure Layer
    ├── adapter/          # Adapters (Implementation)
    │   ├── incoming/     # Controllers, Message Listeners
    │   └── outgoing/     # Repository Implementations, External Service Clients
    ├── configuration/    # Configuration Classes
    ├── exception/        # Infrastructure Exceptions
    └── util/             # Utilities
```

### 2. Domain-Driven Design (DDD)

#### Bounded Contexts

| Context                   | Services                              | Core Aggregates                      |
| ------------------------- | ------------------------------------- | ------------------------------------ |
| **Customer Management**   | CRM Service                           | Lead, Account, Opportunity           |
| **Sales Management**      | Sales Service                         | SalesOrder, SalesQuote               |
| **Inventory Management**  | Inventory Service                     | Product, Warehouse, Stock            |
| **Manufacturing**         | Manufacturing Service                 | ProductionOrder, BOM, WorkCenter     |
| **Procurement**           | Procurement Service                   | PurchaseOrder, Vendor                |
| **Financial Management**  | Finance, Billing Services             | Account, Invoice, Payment            |
| **Human Resources**       | HR Service                            | Employee, PayrollRun                 |
| **Project Management**    | Project Service                       | Project, Task, TimeSheet             |
| **Field Operations**      | Field Service, Repair, Fleet Services | ServiceRequest, RepairOrder, Vehicle |
| **Point of Sale**         | POS Service                           | POSTransaction, POSShift             |
| **System Management**     | User Management, Tenant Management    | User, Role, Tenant                   |
| **Communication**         | Notifications Service                 | Notification, Template               |
| **Business Intelligence** | Analytics Service                     | Dashboard, Report                    |

#### Aggregate Design Principles

-   **Single Aggregate Root**: Each aggregate has one root entity
-   **Consistency Boundaries**: Aggregates enforce business invariants
-   **Small Aggregates**: Keep aggregates focused and small
-   **Reference by ID**: Aggregates reference each other by ID only

### 3. Event-Driven Architecture

#### Domain Events

```kotlin
// Example Domain Event
data class CustomerCreatedEvent(
    val aggregateId: CustomerId,
    val tenantId: TenantId,
    val customerName: String,
    val email: String,
    val occurredOn: Instant
) : DomainEvent
```

#### Integration Events (Kafka)

```kotlin
// Example Integration Event
data class OrderPlacedIntegrationEvent(
    val orderId: OrderId,
    val customerId: CustomerId,
    val tenantId: TenantId,
    val orderItems: List<OrderItem>,
    val totalAmount: Money,
    val timestamp: Instant
) : IntegrationEvent
```

## System Components

### 1. API Gateway

-   **Technology**: Quarkus + Vert.x
-   **Responsibilities**:
    -   Request routing to appropriate services
    -   Authentication and authorization
    -   Rate limiting and throttling
    -   Request/response transformation
    -   Circuit breaker pattern

### 2. Service Registry & Discovery

-   **Technology**: Eureka Server (Planned)
-   **Responsibilities**:
    -   Service registration and health checks
    -   Load balancing
    -   Service discovery

### 3. Message Broker

-   **Technology**: Apache Kafka
-   **Topics Structure**:
    ```
    chiro.{tenant-id}.{service}.{aggregate}.{event-type}
    ```
    Example: `chiro.tenant-001.sales.order.placed`

### 4. Database Strategy

-   **Per-Service Database**: Each service has its own PostgreSQL database
-   **Multi-Tenancy**: Tenant isolation at application level
-   **CQRS**: Read/Write models separated where needed

### 5. Cross-Cutting Concerns

#### Security

-   **Authentication**: JWT tokens with OAuth 2.0
-   **Authorization**: RBAC + ABAC models
-   **Multi-tenancy**: Tenant context propagation
-   **Encryption**: Data at rest and in transit

#### Monitoring & Observability

-   **Metrics**: Micrometer + Prometheus
-   **Logging**: Structured logging with correlation IDs
-   **Tracing**: Jaeger for distributed tracing
-   **Health Checks**: Actuator endpoints

#### Data Consistency

-   **Eventual Consistency**: Between services via events
-   **Saga Pattern**: For distributed transactions
-   **Outbox Pattern**: Reliable event publishing

## Communication Patterns

### 1. Synchronous Communication

-   **REST APIs**: For real-time queries and commands
-   **GraphQL**: For complex data fetching (planned)
-   **gRPC**: For high-performance service-to-service calls (planned)

### 2. Asynchronous Communication

-   **Domain Events**: Within service boundaries
-   **Integration Events**: Across service boundaries
-   **Command Events**: For eventual consistency

## Deployment Architecture

### 1. Container Strategy

```dockerfile
# Multi-stage build for optimization
FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test

FROM openjdk:21-jre-slim
COPY --from=builder /app/build/quarkus-app/ /app/
EXPOSE 8080
CMD ["java", "-jar", "/app/quarkus-run.jar"]
```

### 2. Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
    name: { service-name }
spec:
    replicas: 3
    selector:
        matchLabels:
            app: { service-name }
    template:
        metadata:
            labels:
                app: { service-name }
        spec:
            containers:
                - name: { service-name }
                  image: chiro-erp/{service-name}:latest
                  ports:
                      - containerPort: 8080
                  env:
                      - name: DB_HOST
                        value: "postgres"
                      - name: KAFKA_BROKERS
                        value: "kafka:9092"
```

## Quality Attributes

### 1. Scalability

-   **Horizontal Scaling**: Stateless services with load balancing
-   **Database Sharding**: Tenant-based partitioning
-   **Caching Strategy**: Redis for session and data caching

### 2. Availability

-   **Circuit Breaker**: Prevent cascade failures
-   **Retry Mechanisms**: Exponential backoff
-   **Health Checks**: Kubernetes liveness/readiness probes

### 3. Performance

-   **Connection Pooling**: Database and HTTP connections
-   **Async Processing**: Non-blocking I/O with Reactive Streams
-   **Event Sourcing**: For audit and replay capabilities (planned)

### 4. Security

-   **Zero Trust**: No implicit trust between services
-   **API Security**: Rate limiting, input validation
-   **Data Protection**: Encryption, anonymization

## Architecture Decision Records (ADRs)

1. **ADR-001**: Microservices Architecture
2. **ADR-002**: Domain-Driven Design
3. **ADR-003**: Event-Driven Architecture
4. **ADR-004**: Kotlin + Quarkus Technology Stack
5. **ADR-005**: PostgreSQL Database Strategy
6. **ADR-006**: Apache Kafka Message Broker
7. **ADR-007**: Clean Architecture Pattern

---

_This architecture supports the core principles of modularity, scalability, maintainability, and business domain alignment._

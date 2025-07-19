# Technology Stack

## Core Technologies

### Runtime & Frameworks

| Technology  | Version | Purpose                 | Rationale                                                      |
| ----------- | ------- | ----------------------- | -------------------------------------------------------------- |
| **Java**    | 21 LTS  | Runtime Platform        | Latest LTS with modern features, performance improvements      |
| **Kotlin**  | 1.9+    | Primary Language        | Concise, null-safe, interoperable with Java, excellent for DDD |
| **Quarkus** | 3.0+    | Microservices Framework | Cloud-native, fast startup, low memory footprint, reactive     |
| **Gradle**  | 8.0+    | Build Tool              | Flexible, performant, great Kotlin DSL support                 |

### Data & Messaging

| Technology             | Version | Purpose          | Rationale                                                  |
| ---------------------- | ------- | ---------------- | ---------------------------------------------------------- |
| **PostgreSQL**         | 15      | Primary Database | ACID compliance, JSON support, scalability, reliability    |
| **Apache Kafka**       | 3.5+    | Message Broker   | High-throughput, fault-tolerant, event streaming           |
| **Redis**              | 7.0+    | Caching          | In-memory data store, session management, high performance |
| **Hibernate Reactive** | Latest  | ORM              | Reactive database access, Panache integration              |

### Container & Orchestration

| Technology         | Version | Purpose                 | Rationale                                      |
| ------------------ | ------- | ----------------------- | ---------------------------------------------- |
| **Docker**         | 24+     | Containerization        | Application packaging, environment consistency |
| **Docker Compose** | 2.0+    | Local Development       | Multi-container orchestration for development  |
| **Kubernetes**     | 1.28+   | Container Orchestration | Production deployment, scaling, service mesh   |
| **Helm**           | 3.0+    | K8s Package Manager     | Templating, versioning, deployment management  |

### Monitoring & Observability

| Technology     | Version | Purpose             | Rationale                             |
| -------------- | ------- | ------------------- | ------------------------------------- |
| **Prometheus** | Latest  | Metrics Collection  | Time-series monitoring, alerting      |
| **Grafana**    | Latest  | Visualization       | Dashboards, metrics visualization     |
| **Jaeger**     | Latest  | Distributed Tracing | Request tracing across services       |
| **ELK Stack**  | 8.0+    | Logging             | Centralized logging, search, analysis |

### Security

| Technology        | Version | Purpose             | Rationale                              |
| ----------------- | ------- | ------------------- | -------------------------------------- |
| **Keycloak**      | 22+     | Identity Management | OAuth2/OIDC, user federation, RBAC     |
| **Vault**         | 1.14+   | Secrets Management  | Secure secret storage, rotation        |
| **Let's Encrypt** | Latest  | SSL Certificates    | Free, automated certificate management |

## Architecture Patterns & Libraries

### Domain-Driven Design

```kotlin
// Domain modeling with Kotlin
data class CustomerId(val value: UUID) : Identifier
data class Money(val amount: BigDecimal, val currency: Currency) : ValueObject

abstract class AggregateRoot<ID : Identifier> {
    abstract val id: ID
    private val domainEvents = mutableListOf<DomainEvent>()

    protected fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun getUncommittedEvents(): List<DomainEvent> = domainEvents.toList()
    fun markEventsAsCommitted() = domainEvents.clear()
}
```

### Reactive Programming

```kotlin
// Quarkus Reactive with Mutiny
@ApplicationScoped
class CustomerService {

    @Inject
    lateinit var customerRepository: CustomerRepository

    fun createCustomer(command: CreateCustomerCommand): Uni<Customer> {
        return customerRepository
            .persist(Customer.create(command))
            .invoke { customer -> eventPublisher.publish(CustomerCreatedEvent(customer.id)) }
    }
}
```

### Event-Driven Architecture

```kotlin
// Kafka integration with Quarkus
@ApplicationScoped
class OrderEventHandler {

    @Incoming("orders")
    fun handleOrderEvent(event: OrderPlacedEvent): Uni<Void> {
        return inventoryService
            .reserveStock(event.orderItems)
            .invoke { logger.info("Stock reserved for order ${event.orderId}") }
            .replaceWithVoid()
    }
}
```

## Development Tools

### IDE & Development

| Tool              | Purpose            | Rationale                                          |
| ----------------- | ------------------ | -------------------------------------------------- |
| **IntelliJ IDEA** | Primary IDE        | Excellent Kotlin support, debugging, refactoring   |
| **VS Code**       | Lightweight Editor | Extensions, Docker integration, remote development |
| **Postman**       | API Testing        | REST API testing, automation, collaboration        |

### Code Quality

| Tool          | Purpose         | Rationale                              |
| ------------- | --------------- | -------------------------------------- |
| **Detekt**    | Static Analysis | Kotlin-specific code quality rules     |
| **Spotless**  | Code Formatting | Consistent code formatting across team |
| **JaCoCo**    | Code Coverage   | Test coverage reporting                |
| **SonarQube** | Code Quality    | Continuous inspection, technical debt  |

### Testing

| Tool               | Purpose             | Rationale                                    |
| ------------------ | ------------------- | -------------------------------------------- |
| **JUnit 5**        | Unit Testing        | Modern testing framework with Kotlin support |
| **Testcontainers** | Integration Testing | Real database/service integration tests      |
| **MockK**          | Mocking             | Kotlin-first mocking library                 |
| **Rest Assured**   | API Testing         | REST API testing with fluent interface       |

## Database Design

### Multi-Tenant Strategy

```sql
-- Tenant-aware tables
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Row Level Security
CREATE POLICY tenant_isolation ON customers
    FOR ALL TO application_user
    USING (tenant_id = current_setting('app.current_tenant_id')::UUID);
```

### Event Store (Planned)

```sql
-- Event sourcing support
CREATE TABLE event_store (
    id SERIAL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    metadata JSONB,
    sequence_number BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

## Message Schema Registry

### Kafka Topic Naming

```
Pattern: chiro.{environment}.{service}.{aggregate}.{event-type}
Examples:
- chiro.prod.sales.order.placed
- chiro.dev.inventory.product.updated
- chiro.test.crm.customer.created
```

### Avro Schema Example

```json
{
    "type": "record",
    "name": "CustomerCreatedEvent",
    "namespace": "org.chiro.crm.events",
    "fields": [
        { "name": "customerId", "type": "string" },
        { "name": "tenantId", "type": "string" },
        { "name": "customerName", "type": "string" },
        { "name": "email", "type": ["null", "string"], "default": null },
        {
            "name": "createdAt",
            "type": "long",
            "logicalType": "timestamp-millis"
        }
    ]
}
```

## Environment Configuration

### Development Environment

```yaml
# application-dev.yml
quarkus:
    datasource:
        jdbc:
            url: jdbc:postgresql://localhost:5432/chiro_erp_dev
            username: chiro
            password: chiro123
    kafka:
        bootstrap:
            servers: localhost:9092
    log:
        level: DEBUG
    dev-ui:
        enabled: true
```

### Production Environment

```yaml
# application-prod.yml
quarkus:
    datasource:
        jdbc:
            url: ${DATABASE_URL}
            username: ${DB_USERNAME}
            password: ${DB_PASSWORD}
    kafka:
        bootstrap:
            servers: ${KAFKA_BROKERS}
    log:
        level: INFO
    native:
        enabled: true
```

## Performance Considerations

### JVM Tuning

```bash
# Production JVM settings
-XX:+UseG1GC
-XX:MaxRAMPercentage=70.0
-XX:+UnlockExperimentalVMOptions
-XX:+UseZGC  # For low-latency requirements
```

### Database Optimization

```sql
-- Indexing strategy
CREATE INDEX CONCURRENTLY idx_customers_tenant_id ON customers(tenant_id);
CREATE INDEX CONCURRENTLY idx_orders_customer_date ON orders(customer_id, created_at DESC);

-- Partitioning for large tables
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    -- other columns
) PARTITION BY RANGE (created_at);
```

### Kafka Optimization

```properties
# Producer configuration
acks=all
retries=2147483647
max.in.flight.requests.per.connection=5
enable.idempotence=true
compression.type=snappy
```

## Security Configuration

### OAuth 2.0 / OIDC

```yaml
quarkus:
    oidc:
        auth-server-url: ${KEYCLOAK_URL}/realms/chiro-erp
        client-id: chiro-erp-backend
        credentials:
            secret: ${OIDC_CLIENT_SECRET}
    security:
        jaxrs:
            deny-unannotated-endpoints: true
```

### Database Security

```yaml
quarkus:
    datasource:
        jdbc:
            additional-jdbc-properties:
                sslmode: require
                sslcert: /path/to/client-cert.pem
                sslkey: /path/to/client-key.pem
                sslrootcert: /path/to/ca-cert.pem
```

## CI/CD Stack

### GitHub Actions

```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline
on:
    push:
        branches: [main, develop]
    pull_request:
        branches: [main]

jobs:
    test:
        runs-on: ubuntu-latest
        services:
            postgres:
                image: postgres:15
                env:
                    POSTGRES_PASSWORD: postgres
                options: >-
                    --health-cmd pg_isready
                    --health-interval 10s
                    --health-timeout 5s
                    --health-retries 5
        steps:
            - uses: actions/checkout@v4
            - uses: actions/setup-java@v4
              with:
                  java-version: "21"
                  distribution: "temurin"
            - name: Test
              run: ./gradlew test
```

### Deployment Tools

| Tool          | Purpose        | Configuration                     |
| ------------- | -------------- | --------------------------------- |
| **ArgoCD**    | GitOps         | Kubernetes application deployment |
| **Terraform** | Infrastructure | Cloud infrastructure provisioning |
| **Ansible**   | Configuration  | Server configuration management   |

---

_This technology stack provides a solid foundation for building a scalable, maintainable, and high-performance ERP system._

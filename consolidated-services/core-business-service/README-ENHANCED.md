# Core Business Service

## 🏗️ **ENHANCED PRODUCTION-READY ARCHITECTURE**

The Core Business Service is a **modern, production-grade microservice** that consolidates critical ERP business operations including Finance, Inventory, Sales, Manufacturing, and Procurement into a single, cohesive service following **Domain-Driven Design (DDD)** and **Event-Driven Architecture (EDA)** patterns.

---

## 📋 **Architecture Overview**

### **Multi-Module Monolith Design**

-   **Finance Module**: General ledger, accounts payable/receivable, financial reporting
-   **Inventory Module**: Product management, stock tracking, warehouse operations
-   **Sales Module**: Order management, customer relations, pricing
-   **Manufacturing Module**: Work orders, bill of materials, production planning
-   **Procurement Module**: Purchase orders, vendor management, requisitions

### **Domain-Driven Design (DDD) Implementation**

```
📦 src/main/kotlin/org/chiro/core_business_service/
├── 🔧 Application.kt                 # Enhanced application entry point
├── 🔗 shared/                        # Shared components across modules
│   ├── domain/                       # Domain layer (enterprise business logic)
│   │   ├── aggregate/                # Aggregate roots
│   │   ├── entity/                   # Domain entities
│   │   ├── valueobject/              # Value objects
│   │   ├── event/                    # Domain events
│   │   ├── service/                  # Domain services
│   │   ├── repository/               # Repository contracts
│   │   └── exception/                # Domain exceptions
│   ├── application/                  # Application layer (use cases)
│   │   ├── command/                  # Command handling (CQRS)
│   │   ├── query/                    # Query handling (CQRS)
│   │   ├── service/                  # Application services
│   │   ├── dto/                      # Data transfer objects
│   │   ├── port/                     # Ports (interfaces)
│   │   └── handler/                  # Command/query handlers
│   └── infrastructure/               # Infrastructure layer (technical details)
│       ├── adapter/                  # External system adapters
│       ├── persistence/              # Database implementations
│       ├── messaging/                # Event messaging
│       ├── rest/                     # REST controllers
│       ├── configuration/            # Configuration classes
│       └── exception/                # Infrastructure exceptions
├── 🔄 module/                        # Inter-module integration
│   ├── integration/                  # Integration events & handlers
│   └── facade/                       # Module facades for communication
└── ⚙️ config/                        # Application configuration
```

### **Event-Driven Architecture (EDA)**

-   **Domain Events**: Business events within bounded contexts
-   **Integration Events**: Cross-module communication events
-   **Event Store**: Complete audit trail and event sourcing capability
-   **CQRS**: Command-Query Responsibility Segregation for scalability

---

## 🚀 **Production Features**

### **✅ Enterprise-Grade Capabilities**

-   **Multi-tenancy ready** with configurable isolation
-   **Event sourcing** for complete audit trails
-   **CQRS** for read/write scalability
-   **Distributed transactions** across modules
-   **Comprehensive validation** with business rules
-   **Performance monitoring** and metrics
-   **Security integration** with JWT authentication
-   **Database migrations** with Liquibase
-   **Health checks** and observability

### **🔧 Technology Stack**

-   **Runtime**: Quarkus 3.24.4 (Cloud-native Java/Kotlin)
-   **Language**: Kotlin with coroutines for async processing
-   **Database**: PostgreSQL with Liquibase migrations
-   **Messaging**: SmallRye Reactive Messaging for events
-   **API**: REST with OpenAPI 3.0 documentation
-   **Testing**: JUnit 5 with TestContainers for integration tests
-   **Observability**: Micrometer metrics, health checks
-   **Security**: JWT with fine-grained authorization

---

## 🏭 **Module Details**

### **💰 Finance Module** (`modules/finance/`)

-   **Chart of Accounts** with hierarchical structure
-   **Double-entry accounting** with full audit trail
-   **Multi-currency support** with exchange rates
-   **Accounts Payable/Receivable** management
-   **Financial reporting** and trial balance
-   **Tax calculation** and compliance

### **📦 Inventory Module** (`modules/inventory/`)

-   **Product catalog** with SKU management
-   **Multi-location stock tracking** (FIFO/LIFO/Average)
-   **Stock reservations** and allocations
-   **Inventory movements** with complete traceability
-   **Low stock alerts** and reorder points
-   **Serial/lot tracking** for compliance

### **💼 Sales Module** (`modules/sales/`)

-   **Order management** lifecycle
-   **Customer relationship** management
-   **Pricing and discounting** engine
-   **Credit management** and limits
-   **Shipping and fulfillment** integration
-   **Sales reporting** and analytics

### **🏭 Manufacturing Module** (`modules/manufacturing/`)

-   **Bill of Materials** (BOM) management
-   **Work order** planning and execution
-   **Production scheduling** and capacity planning
-   **Material requirements** planning (MRP)
-   **Quality control** checkpoints
-   **Production costing** and variance analysis

### **🛒 Procurement Module** (`modules/procurement/`)

-   **Purchase order** management
-   **Vendor management** and evaluation
-   **Requisition workflow** with approvals
-   **Receiving and inspection** processes
-   **Vendor performance** tracking
-   **Contract management** and compliance

---

## 📊 **Database Architecture**

### **Schema Organization**

```sql
core_business/
├── finance_*          # Finance module tables
├── inventory_*        # Inventory module tables
├── sales_*           # Sales module tables
├── manufacturing_*    # Manufacturing module tables
├── procurement_*     # Procurement module tables
├── event_store       # Event sourcing store
├── event_snapshots   # Performance snapshots
└── projection_state  # CQRS read model state
```

### **Migration Strategy**

-   **Liquibase** for version-controlled schema changes
-   **Module-specific** migration files for isolation
-   **Rollback support** for safe deployments
-   **Cross-module** integration tables for relationships

---

## 🔄 **Inter-Module Communication**

### **Integration Events**

```kotlin
// Example: Sales order triggers inventory reservation
SalesOrderCreatedEvent → InventoryModule.reserveStock()
                      → FinanceModule.recordAccountsReceivable()

// Example: Work order completion updates inventory
WorkOrderCompletedEvent → InventoryModule.adjustStock()
                       → FinanceModule.recordProductionCosts()
```

### **Module Facades**

Clean interfaces for cross-module communication without tight coupling:

-   `FinanceModuleFacade`
-   `InventoryModuleFacade`
-   `SalesModuleFacade`
-   `ManufacturingModuleFacade`
-   `ProcurementModuleFacade`

---

## 🧪 **Testing Strategy**

### **Test Structure**

```
📦 src/test/kotlin/org/chiro/core_business_service/
├── unit/              # Unit tests for domain logic
├── integration/       # Integration tests for APIs
└── shared/           # Shared test utilities
```

### **Test Types**

-   **Unit Tests**: Domain logic, value objects, business rules
-   **Integration Tests**: REST APIs, database operations
-   **Contract Tests**: Module interface compliance
-   **Performance Tests**: Load testing and benchmarks

---

## ⚙️ **Configuration**

### **Environment Variables**

```yaml
# Database
DATABASE_URL: jdbc:postgresql://localhost:5432/chiro_core_business
DATABASE_USERNAME: core_business_user
DATABASE_PASSWORD: core_business_pass

# Features
EVENT_SOURCING_ENABLED: true
STRICT_VALIDATION: true
FILE_LOGGING_ENABLED: true

# Security
JWT_PUBLIC_KEY_LOCATION: classpath:publickey.pem
JWT_ISSUER: https://chiro-erp.com
```

### **Module Configuration**

Each module can be independently enabled/disabled:

```yaml
modules:
    finance:
        enabled: true
        base-path: /finance
        currency:
            default: USD
            supported: [USD, EUR, GBP, CAD, AUD]
    inventory:
        enabled: true
        stock-tracking:
            method: FIFO
        low-stock-threshold: 10
```

---

## 🚀 **Running the Service**

### **Development Mode**

```bash
./gradlew :consolidated-services:core-business-service:quarkusDev
```

### **Production Build**

```bash
./gradlew :consolidated-services:core-business-service:build
```

### **Docker Deployment**

```bash
# Build the service
./gradlew :consolidated-services:core-business-service:build

# Build Docker image
docker build -f consolidated-services/core-business-service/docker/Dockerfile \
  -t chiro/core-business-service:latest .

# Run container
docker run -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/chiro_core_business" \
  chiro/core-business-service:latest
```

---

## 📡 **API Documentation**

### **Health & Monitoring**

-   `GET /api/v1/health/status` - Service health status
-   `GET /api/v1/health/info` - Service information
-   `GET /q/health/live` - Liveness probe
-   `GET /q/health/ready` - Readiness probe
-   `GET /q/openapi` - OpenAPI specification
-   `GET /q/swagger-ui` - Swagger UI

### **Module APIs**

-   `GET /api/v1/finance/**` - Finance operations
-   `GET /api/v1/inventory/**` - Inventory operations
-   `GET /api/v1/sales/**` - Sales operations
-   `GET /api/v1/manufacturing/**` - Manufacturing operations
-   `GET /api/v1/procurement/**` - Procurement operations

---

## 🔍 **Monitoring & Observability**

### **Metrics**

-   **Business metrics**: Orders, transactions, inventory levels
-   **Technical metrics**: Response times, error rates, throughput
-   **Infrastructure metrics**: Database connections, memory usage

### **Logging**

-   **Structured logging** with correlation IDs
-   **Module-specific** log levels
-   **Security events** and audit trails
-   **Performance monitoring** and alerts

### **Health Checks**

-   **Deep health checks** for database connectivity
-   **Module health** status reporting
-   **Dependency health** monitoring
-   **Custom business** health indicators

---

## 🚦 **Deployment Pipeline**

### **CI/CD Integration**

1. **Build & Test**: Automated testing on all changes
2. **Quality Gates**: Code coverage, security scans
3. **Database Migration**: Automated schema updates
4. **Blue-Green Deployment**: Zero-downtime deployments
5. **Rollback Strategy**: Quick rollback capabilities

### **Environment Promotion**

-   **Development**: Feature development and testing
-   **Staging**: Integration and performance testing
-   **Production**: Live business operations

---

This enhanced Core Business Service provides a **production-ready foundation** for enterprise ERP operations with modern architecture patterns, comprehensive testing, and operational excellence built-in.

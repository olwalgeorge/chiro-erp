# Sales Service

## Overview

The Sales Service is a core business microservice responsible for managing the entire sales process from lead qualification to order fulfillment. It handles sales orders, quotations, pricing, and integrates with CRM, Inventory, and Finance services to provide a complete sales management solution.

## 🎯 Business Purpose

This service handles:

-   **Sales Order Management**: Create, modify, and track sales orders
-   **Quotation Management**: Generate and manage customer quotes
-   **Pricing Engine**: Dynamic pricing, discounts, and promotions
-   **Sales Pipeline**: Track opportunities through sales stages
-   **Commission Calculation**: Sales representative commission tracking
-   **Sales Analytics**: Revenue reporting and sales performance metrics

## 🏗️ Architecture

### Domain Model

```
SalesOrder (Aggregate Root)
├── SalesOrderId (Identity)
├── CustomerId (Reference)
├── TenantId (Multi-tenancy)
├── OrderItems (Entity Collection)
├── PricingDetails (Value Object)
├── ShippingInfo (Value Object)
└── OrderStatus (Value Object)

Quotation (Aggregate Root)
├── QuotationId (Identity)
├── CustomerId (Reference)
├── SalesRepId (Reference)
├── QuoteItems (Entity Collection)
├── Validity (Value Object)
└── QuoteStatus (Value Object)
```

### Key Aggregates

-   **SalesOrder**: Complete customer order with items and pricing
-   **Quotation**: Customer quote/proposal management
-   **SalesOpportunity**: Lead tracking through sales pipeline

### Bounded Context

-   **Core Domain**: Sales order processing and revenue management
-   **Supporting Domain**: Pricing and discount management
-   **Generic Domain**: Sales reporting and analytics

## 🔧 Technical Specifications

### Technology Stack

-   **Framework**: Quarkus 3.x with Kotlin
-   **Database**: PostgreSQL with Hibernate ORM + Panache
-   **Messaging**: Kafka for event publishing
-   **Caching**: Redis for pricing cache
-   **Testing**: JUnit 5, Testcontainers, MockK

### API Endpoints

#### Sales Orders

```
GET    /api/v1/sales/orders         # List sales orders (paginated)
POST   /api/v1/sales/orders         # Create new sales order
GET    /api/v1/sales/orders/{id}    # Get sales order details
PUT    /api/v1/sales/orders/{id}    # Update sales order
DELETE /api/v1/sales/orders/{id}    # Cancel sales order
POST   /api/v1/sales/orders/{id}/confirm # Confirm sales order
POST   /api/v1/sales/orders/{id}/ship    # Mark as shipped
```

#### Quotations

```
GET    /api/v1/sales/quotations     # List quotations
POST   /api/v1/sales/quotations     # Create quotation
GET    /api/v1/sales/quotations/{id} # Get quotation details
PUT    /api/v1/sales/quotations/{id} # Update quotation
POST   /api/v1/sales/quotations/{id}/convert # Convert to sales order
POST   /api/v1/sales/quotations/{id}/send    # Send to customer
```

#### Pricing

```
POST   /api/v1/sales/pricing/calculate # Calculate pricing for items
GET    /api/v1/sales/pricing/rules     # Get pricing rules
POST   /api/v1/sales/pricing/rules     # Create pricing rule
PUT    /api/v1/sales/pricing/rules/{id} # Update pricing rule
```

### Domain Events Published

```kotlin
- SalesOrderCreatedEvent
- SalesOrderConfirmedEvent
- SalesOrderCancelledEvent
- SalesOrderShippedEvent
- QuotationCreatedEvent
- QuotationSentEvent
- QuotationConvertedEvent
- PricingRuleUpdatedEvent
```

### Domain Events Consumed

```kotlin
- CustomerCreatedEvent (from CRM Service)
- ProductPriceUpdatedEvent (from Inventory Service)
- InventoryLowStockEvent (from Inventory Service)
- PaymentReceivedEvent (from Finance Service)
```

### External Dependencies

-   **CRM Service**: Customer information and relationship data
-   **Inventory Service**: Product availability and pricing
-   **Finance Service**: Payment processing and invoicing
-   **Notifications Service**: Customer communications

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

-   Service: http://localhost:8083
-   Dev UI: http://localhost:8083/q/dev/
-   Health Check: http://localhost:8083/q/health
-   Metrics: http://localhost:8083/q/metrics

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

sales:
    default-tax-rate: 0.08
    default-discount-limit: 0.15
    quote-validity-days: 30
    auto-confirm-threshold: 1000.00
    commission-rate: 0.05
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

-   **Unit Tests**: Domain logic, pricing calculations, order validation
-   **Integration Tests**: Database operations, event publishing
-   **Contract Tests**: API contracts with CRM, Inventory, Finance services
-   **Business Process Tests**: End-to-end order flow testing

## 📊 Monitoring & Observability

### Health Checks

-   **Liveness**: `/q/health/live` - Service is running
-   **Readiness**: `/q/health/ready` - Service can accept traffic
-   **Database**: Order processing capability
-   **External Services**: CRM, Inventory, Finance service connectivity

### Metrics

Key metrics exposed at `/q/metrics`:

-   `sales_orders_total` - Total sales orders created
-   `sales_revenue_total` - Total sales revenue
-   `quotations_sent_total` - Quotations sent to customers
-   `quote_conversion_rate` - Quote to order conversion rate
-   `average_order_value` - Average sales order value
-   `pricing_calculations_total` - Pricing engine usage

### Logging

Structured JSON logging with business context:

```json
{
    "timestamp": "2025-07-19T10:30:00Z",
    "level": "INFO",
    "logger": "org.chiro.sales.SalesOrderService",
    "message": "Sales order confirmed",
    "salesOrderId": "order-123",
    "customerId": "customer-456",
    "orderValue": 2500.0,
    "tenantId": "tenant-789",
    "correlationId": "req-abc"
}
```

## 🔒 Security

### Authorization

-   **Role-Based Access**: Sales reps can only access their orders
-   **Territory Management**: Geographic or product-based access control
-   **Approval Workflows**: Manager approval for large orders or discounts
-   **Data Privacy**: Customer data access logging and compliance

### Business Rules Validation

-   **Credit Limits**: Customer credit validation before order confirmation
-   **Pricing Authorization**: Discount approval workflows
-   **Inventory Validation**: Stock availability checking
-   **Territory Rules**: Sales rep territory enforcement

## 📚 API Documentation

### Interactive API Docs

-   **Swagger UI**: http://localhost:8083/q/swagger-ui/
-   **OpenAPI Spec**: http://localhost:8083/q/openapi

### Sales Order Creation Example

```bash
# Create sales order
curl -X POST http://localhost:8083/api/v1/sales/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "customerId": "customer-123",
    "salesRepId": "rep-456",
    "orderDate": "2025-07-19",
    "items": [
      {
        "productId": "product-789",
        "quantity": 10,
        "unitPrice": 100.00
      },
      {
        "productId": "product-101",
        "quantity": 5,
        "unitPrice": 200.00
      }
    ],
    "shippingAddress": {
      "street": "123 Business Ave",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "notes": "Rush order - needed by Friday"
  }'

# Response
{
  "id": "order-123",
  "orderNumber": "SO-2025-001234",
  "customerId": "customer-123",
  "salesRepId": "rep-456",
  "status": "PENDING",
  "orderDate": "2025-07-19",
  "totalAmount": 2000.00,
  "taxAmount": 160.00,
  "grandTotal": 2160.00,
  "items": [
    {
      "productId": "product-789",
      "productName": "Widget A",
      "quantity": 10,
      "unitPrice": 100.00,
      "totalPrice": 1000.00
    },
    {
      "productId": "product-101",
      "productName": "Widget B",
      "quantity": 5,
      "unitPrice": 200.00,
      "totalPrice": 1000.00
    }
  ]
}
```

## 🚢 Deployment

### Environment Variables

```bash
# Database
DATABASE_URL=jdbc:postgresql://postgres:5432/chiro_erp

# External Services
CRM_SERVICE_URL=http://crm-service:8080
INVENTORY_SERVICE_URL=http://inventory-service:8080
FINANCE_SERVICE_URL=http://finance-service:8080

# Business Configuration
DEFAULT_TAX_RATE=0.08
MAX_DISCOUNT_PERCENT=15
QUOTE_VALIDITY_DAYS=30
```

### Kubernetes Configuration

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
    name: sales-service
spec:
    replicas: 3
    selector:
        matchLabels:
            app: sales-service
    template:
        metadata:
            labels:
                app: sales-service
        spec:
            containers:
                - name: sales-service
                  image: chiro-erp/sales-service:latest
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
                          memory: "512Mi"
                          cpu: "200m"
                      limits:
                          memory: "1Gi"
                          cpu: "800m"
```

## 📋 Database Schema

### Key Tables

```sql
-- Sales orders
CREATE TABLE sales_orders (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    sales_rep_id UUID,
    order_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    shipping_address JSONB,
    billing_address JSONB,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order items
CREATE TABLE sales_order_items (
    id UUID PRIMARY KEY,
    sales_order_id UUID NOT NULL REFERENCES sales_orders(id),
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    total_price DECIMAL(12,2) NOT NULL
);

-- Quotations
CREATE TABLE quotations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    quote_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    sales_rep_id UUID,
    quote_date DATE NOT NULL,
    valid_until DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    converted_order_id UUID REFERENCES sales_orders(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🤝 Contributing

### Business Logic Guidelines

-   Validate all pricing calculations with multiple test scenarios
-   Ensure proper inventory checking before order confirmation
-   Implement proper error handling for external service failures
-   Maintain audit trail for all order modifications

### Development Standards

-   Follow domain-driven design principles
-   Implement saga pattern for distributed transactions
-   Use event sourcing for critical business events
-   Maintain high test coverage for business logic

## 📞 Support

### Common Issues

-   **Order Confirmation Fails**: Check inventory availability and customer credit limit
-   **Pricing Calculation Errors**: Verify pricing rules and product configurations
-   **Integration Failures**: Monitor external service health and circuit breakers

### Contacts

-   **Team**: Sales Domain Team
-   **Slack**: #sales-service
-   **Email**: sales-team@chiro-erp.com

---

**Service Version**: 1.0.0
**Last Updated**: July 2025
**Maintainer**: Sales Domain Team

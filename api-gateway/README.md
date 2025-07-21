# API Gateway Service

The API Gateway serves as the unified entry point for the Chiro ERP consolidated services architecture.

## 🎯 Purpose

This service handles:

-   **Request Routing**: Route requests to appropriate consolidated services
-   **Authentication & Authorization**: Validate and authorize incoming requests  
-   **Rate Limiting**: Prevent API abuse and ensure fair usage
-   **Load Balancing**: Distribute requests across service instances
-   **Circuit Breaking**: Prevent cascade failures
-   **API Documentation**: Aggregate OpenAPI specs from all services

## 🏗️ Architecture

### Consolidated Service Routing

The gateway routes requests to 5 consolidated services:

1. **core-business-service** (Port 8081)
   - /api/billing/** → Billing operations
   - /api/finance/** → Financial management
   - /api/sales/** → Sales operations
   - /api/inventory/** → Inventory management  
   - /api/procurement/** → Procurement operations
   - /api/manufacturing/** → Manufacturing operations

2. **operations-management-service** (Port 8082)
   - /api/project/** → Project management
   - /api/fleet/** → Fleet management
   - /api/pos/** → Point of sale
   - /api/fieldservice/** → Field service operations
   - /api/repair/** → Repair management

3. **customer-relations-service** (Port 8083)
   - /api/crm/** → Customer relationship management

4. **platform-services-service** (Port 8084)
   - /api/analytics/** → Analytics and reporting
   - /api/notifications/** → Notification services

5. **workforce-management-service** (Port 8085)
   - /api/hr/** → Human resources
   - /api/users/** → User management
   - /api/tenants/** → Multi-tenant management

## 🚀 Features

### Routing & Load Balancing
- Path-based routing to consolidated services
- Round-robin load balancing
- Health check integration
- Automatic failover

### Security
- JWT token validation
- API key authentication
- CORS configuration
- Rate limiting per client

### Monitoring & Observability
- Request/response logging
- Metrics collection (Prometheus)
- Distributed tracing
- Health check endpoints

### Fault Tolerance
- Circuit breaker pattern
- Request timeout handling
- Retry logic with backoff
- Graceful degradation

## 📋 API Endpoints

### Gateway Management
`
GET    /q/health                         # Gateway health check
GET    /q/metrics                        # Gateway metrics  
GET    /q/openapi                        # Aggregated OpenAPI spec
`

### Service Proxying
All /api/** requests are routed to appropriate consolidated services based on path prefix.

## ⚙️ Configuration

### Environment Variables
`ash
# Service URLs
GATEWAY_SERVICES_CORE_BUSINESS_URL=http://core-business-service:8081
GATEWAY_SERVICES_OPERATIONS_MANAGEMENT_URL=http://operations-management-service:8082
GATEWAY_SERVICES_CUSTOMER_RELATIONS_URL=http://customer-relations-service:8083
GATEWAY_SERVICES_PLATFORM_SERVICES_URL=http://platform-services-service:8084
GATEWAY_SERVICES_WORKFORCE_MANAGEMENT_URL=http://workforce-management-service:8085

# Rate Limiting
GATEWAY_RATE_LIMITING_REQUESTS_PER_MINUTE=1000
GATEWAY_RATE_LIMITING_BURST_SIZE=100

# Circuit Breaker
GATEWAY_CIRCUIT_BREAKER_FAILURE_THRESHOLD=5
GATEWAY_CIRCUIT_BREAKER_RECOVERY_TIMEOUT=30s
`

### Application Configuration
See src/main/resources/application.yml for complete configuration options.

## 🏃‍♂️ Running the Service

### Local Development
`ash
./gradlew quarkusDev
`

### Docker
`ash
docker build -t chiro-erp/api-gateway .
docker run -p 8080:8080 chiro-erp/api-gateway
`

### Kubernetes
`ash
kubectl apply -f kubernetes/services/api-gateway/
`

## 🧪 Testing

### Unit Tests
`ash
./gradlew test
`

### Integration Tests
`ash
./gradlew integrationTest
`

### Load Testing
`ash
# Use your preferred load testing tool against http://localhost:8080/api/
`

## 📊 Monitoring

### Health Checks
- Gateway: http://localhost:8080/q/health
- Individual services health checks are proxied through the gateway

### Metrics
- Prometheus metrics: http://localhost:8080/q/metrics
- Request rates, latencies, error rates
- Circuit breaker states
- Service health status

## 🔧 Development

### Adding New Routes
1. Update service mapping in ConsolidatedRequestRoutingService
2. Add configuration in pplication.yml
3. Update this README documentation

### Custom Filters
Implement custom filters by extending the routing logic in the request routing service.

## 📈 Performance

### Optimizations
- Connection pooling for service calls
- Response caching for static content
- Request batching where applicable
- Async/non-blocking I/O

### Scaling
- Horizontal scaling via Kubernetes
- Load balancer integration
- Service mesh compatibility (Istio)

---

This gateway is optimized for the consolidated monolithic services architecture while maintaining the flexibility to scale and evolve with your system needs.

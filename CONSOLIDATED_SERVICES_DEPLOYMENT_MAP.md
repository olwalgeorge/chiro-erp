# Consolidated Services Architecture & Deployment Mapping

## 🏗️ **Consolidated Services Overview**

The Chiro ERP system uses a **consolidated microservices architecture** where related business domains are grouped into 5 main services plus an API Gateway. This approach reduces operational complexity while maintaining logical separation.

## 📊 **Service Consolidation Map**

### **1. Core Business Service** 🏪

**Location**: `consolidated-services/core-business-service/`
**Kubernetes**: `kubernetes/services/core-business-service/`
**Modules Consolidated**:

-   📊 **Finance** - Financial management and accounting
-   📦 **Inventory** - Stock and warehouse management
-   🏭 **Manufacturing** - Production and assembly
-   🛒 **Procurement** - Purchasing and supplier management
-   💰 **Sales** - Sales orders and revenue tracking

**Deployment Configuration**:

-   Port: `8081`
-   Replicas: `2` (dev), `3` (prod)
-   Database: Shared PostgreSQL with schema isolation

---

### **2. Customer Relations Service** 👥

**Location**: `consolidated-services/customer-relations-service/`
**Kubernetes**: `kubernetes/services/customer-relations-service/`
**Modules Consolidated**:

-   🤝 **CRM** - Customer relationship management

**Deployment Configuration**:

-   Port: `8082`
-   Replicas: `2` (dev), `3` (prod)
-   Integrations: Core Business Service for customer data

---

### **3. Operations Management Service** 🔧

**Location**: `consolidated-services/operations-management-service/`
**Kubernetes**: `kubernetes/services/operations-management-service/`
**Modules Consolidated**:

-   🚛 **Field Service** - On-site service management
-   🚗 **Fleet** - Vehicle and asset tracking
-   💳 **POS** - Point of sale systems
-   📋 **Project** - Project management and tracking
-   🔧 **Repair** - Maintenance and repair services

**Deployment Configuration**:

-   Port: `8083`
-   Replicas: `2` (dev), `3` (prod)
-   Dependencies: Core Business Service, Customer Relations

---

### **4. Platform Services** ⚙️

**Location**: `consolidated-services/platform-services/`
**Kubernetes**: `kubernetes/services/platform-services/`
**Modules Consolidated**:

-   📈 **Analytics** - Business intelligence and reporting
-   💸 **Billing** - Invoice and payment processing
-   📢 **Notifications** - System notifications and alerts
-   🏢 **Tenant Management** - Multi-tenant administration

**Deployment Configuration**:

-   Port: `8084`
-   Replicas: `2` (dev), `3` (prod)
-   Cross-cutting: Serves all other services

---

### **5. Workforce Management Service** 👨‍💼

**Location**: `consolidated-services/workforce-management-service/`
**Kubernetes**: `kubernetes/services/workforce-management-service/`
**Modules Consolidated**:

-   👥 **HR** - Human resources management
-   🔐 **User Management** - Authentication and authorization

**Deployment Configuration**:

-   Port: `8085`
-   Replicas: `2` (dev), `3` (prod)
-   Security: Handles authentication for all services

---

### **6. API Gateway** 🌐

**Location**: `api-gateway/` (in root)
**Kubernetes**: `kubernetes/services/api-gateway/`
**Purpose**:

-   Central entry point for all external requests
-   Service discovery and routing
-   Authentication and authorization
-   Rate limiting and security

**Deployment Configuration**:

-   Port: `8080`
-   Replicas: `2` (dev), `3` (prod)
-   Public facing with Ingress configuration

## 🔄 **How Services Interact**

### **Service Communication Flow**:

```
External Client → API Gateway → Consolidated Services
                      ↓
    ┌─────────────────────────────────────────┐
    │              API Gateway                │
    │         (Port 8080)                     │
    └─────────────────┬───────────────────────┘
                      │
    ┌─────────────────┼───────────────────────┐
    │                 ▼                       │
    │  Core Business Service (8081)           │
    │  Customer Relations Service (8082)      │
    │  Operations Management Service (8083)   │
    │  Platform Services (8084)               │
    │  Workforce Management Service (8085)    │
    └─────────────────────────────────────────┘
                      │
    ┌─────────────────▼───────────────────────┐
    │         Infrastructure Layer            │
    │  PostgreSQL (5432) | Redis (6379)      │
    │  Kafka (9092) | Zookeeper (2181)       │
    └─────────────────────────────────────────┘
```

## 🚀 **Deployment Strategy**

### **1. Infrastructure First** (Automatic):

```powershell
# Infrastructure services are deployed first automatically
postgres, redis, kafka, zookeeper
```

### **2. Application Services** (All Consolidated Services):

```powershell
# Deploy all consolidated services
.\k8s-deploy.ps1 -Command deploy -Environment dev

# Deploy specific consolidated service
.\k8s-deploy.ps1 -Command deploy -Environment dev -ServiceName "core-business-service"
```

### **3. Service Discovery**:

Each consolidated service can discover others via Kubernetes DNS:

-   `http://core-business-service:8081`
-   `http://customer-relations-service:8082`
-   `http://operations-management-service:8083`
-   `http://platform-services:8084`
-   `http://workforce-management-service:8085`

## 📝 **Database Schema Strategy**

### **Shared Database with Schema Isolation**:

```sql
-- Each consolidated service gets its own schema
chiro_erp_db:
  ├── core_business_schema     (finance, inventory, manufacturing, procurement, sales)
  ├── customer_relations_schema (crm)
  ├── operations_schema        (fieldservice, fleet, pos, project, repair)
  ├── platform_schema          (analytics, billing, notifications, tenant)
  └── workforce_schema         (hr, user_management)
```

## ✅ **Deployment Verification**

### **All Services Are Properly Configured**:

-   ✅ **5 Consolidated Services** + API Gateway = 6 total services
-   ✅ **Kubernetes Manifests** - Each service has deployment, service, configmap
-   ✅ **Port Allocation** - Unique ports for each service (8080-8085)
-   ✅ **Service Discovery** - Internal DNS resolution configured
-   ✅ **Load Balancing** - Multiple replicas per environment
-   ✅ **Health Checks** - Readiness and liveness probes
-   ✅ **Environment Variables** - Database and infrastructure connections
-   ✅ **Resource Limits** - Memory and CPU constraints

### **Deployment Commands Work For All Services**:

```powershell
# Deploy everything
.\k8s-deploy.ps1 -Command deploy -Environment dev

# Deploy specific consolidated services
.\k8s-deploy.ps1 -Command deploy -Environment dev -ServiceName "core-business-service,platform-services"

# Scale consolidated services
.\k8s-deploy.ps1 -Command scale -Environment prod -ServiceName "core-business-service" -Replicas 5

# Check status of all services
.\k8s-deploy.ps1 -Command status -Environment dev
```

## 🎯 **Benefits of This Consolidated Architecture**

1. **Reduced Operational Complexity** - 6 services instead of 18 individual microservices
2. **Logical Domain Grouping** - Related business functions stay together
3. **Simplified Service Discovery** - Fewer network calls between services
4. **Easier Deployment** - Single deployment per business domain
5. **Resource Efficiency** - Shared infrastructure within each consolidated service
6. **Maintainability** - Related code stays in the same service boundary

## 🔍 **Monitoring & Observability**

Each consolidated service exposes:

-   **Health endpoints**: `/q/health/live`, `/q/health/ready`
-   **Metrics**: Prometheus metrics at `/q/metrics`
-   **Logs**: Structured logging with service and module identification
-   **Tracing**: Distributed tracing across consolidated service boundaries

The consolidated services architecture is **fully deployed and operational** with proper service discovery, load balancing, and monitoring! 🎉

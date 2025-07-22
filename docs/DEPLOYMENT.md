# 🚀 CHIRO ERP - DEPLOYMENT OVERVIEW

## 📋 Documentation Index

This deployment overview provides links to comprehensive deployment documentation:

-   **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)** - Complete deployment guide with Docker, Kubernetes, and native compilation
-   **[DEPENDENCY_ARCHITECTURE.md](./DEPENDENCY_ARCHITECTURE.md)** - Detailed dependency architecture documentation
-   **[README.md](./README.md)** - Quick start and development guide

## 🏗️ Current Architecture (Updated)

### Modern Microservices Stack

-   **Framework**: Quarkus 3.24.4 with new REST implementation
-   **Language**: Kotlin 2.1.21 with serialization support
-   **Database**: PostgreSQL with Hibernate Reactive Panache
-   **Build**: Gradle 8.14 with Kotlin DSL
-   **Java**: OpenJDK 21 LTS

### Service Architecture

-   **API Gateway** (`api-gateway`) - Unified entry point with security and routing
-   **Core Business Service** (`consolidated-services/core-business-service`) - Finance, Sales, Procurement, Manufacturing, Inventory
-   **Customer Relations Service** (`consolidated-services/customer-relations-service`) - CRM and Billing
-   **Operations Management Service** (`consolidated-services/operations-management-service`) - Field Service, Fleet, POS, Project Management, Repair
-   **Platform Services** (`consolidated-services/platform-services`) - Notifications and Tenant Management
-   **Workforce Management Service** (`consolidated-services/workforce-management-service`) - HR and User Management

### REST Communication

-   **REST Server**: `quarkus-rest` for exposing APIs
-   **REST Client**: `quarkus-rest-client` for inter-service communication
-   **Dual Serialization**: Kotlin for internal, Jackson for external APIs

## ⚡ Quick Start

### 1. Check Current Status

```powershell
.\scripts\deploy-final.ps1 -Action status
```

### 2. Deploy Everything (Infrastructure + Applications)

```powershell
.\scripts\deploy-final.ps1 -Action full
```

### 3. Deploy Only Infrastructure

```powershell
.\scripts\deploy-final.ps1 -Action infrastructure
```

### 4. Deploy Only Applications

```powershell
.\scripts\deploy-final.ps1 -Action applications
```

### 5. Clean Up Everything

```powershell
.\scripts\deploy-final.ps1 -Action cleanup
```

## 🔧 Manual Commands (If Needed)

### Build Individual Service

```powershell
docker build --build-arg SERVICE_NAME=core-business-service -t chiro-erp/core-business-service -f Dockerfile.consolidated .
```

### Start Infrastructure Only

```powershell
docker-compose -f docker-compose.consolidated.yml up -d postgres kafka zookeeper
```

### Run Individual Service

```powershell
docker run -d --name chiro-erp-core-business-service --network chiro-erp_default -p 8081:8080 -e "QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/chiro_erp" chiro-erp/core-business-service
```

## 🌐 Access Points

After successful deployment:

-   **Database**: `localhost:5432` (user: chiro_user, password: chiro_password, db: chiro_erp)
-   **Kafka**: `localhost:9092`
-   **Application Health Checks**: `http://localhost:808X/q/health` (where X is the service port)

## 📊 System Requirements

### Minimum Requirements

-   **RAM**: 8GB (4GB for Docker Desktop + 4GB for services)
-   **CPU**: 4 cores
-   **Disk**: 10GB free space
-   **Docker Desktop**: Latest version with WSL2 backend

### Recommended Requirements

-   **RAM**: 16GB
-   **CPU**: 8 cores
-   **Disk**: 20GB free space

## 🔍 Troubleshooting

### Common Issues

1. **Kotlin Compilation Fails**

    - The Dockerfile is optimized for resource constraints
    - Build uses single-threaded compilation with memory limits
    - If it still fails, try building one service at a time

2. **Out of Memory Errors**

    - Increase Docker Desktop memory allocation to 6GB+
    - Close other applications to free up RAM
    - Build services individually instead of all at once

3. **Port Conflicts**

    - Check if ports 5432, 9092, 8080-8085 are available
    - Stop other services using these ports

4. **Infrastructure Services Won't Start**
    - Run: `docker-compose -f docker-compose.consolidated.yml down -v`
    - Then restart with the deployment script

### Health Checks

Check service health:

```powershell
# Database
docker exec chiro-erp-postgres-1 pg_isready -h localhost

# Kafka
docker exec chiro-erp-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list

# Application services
curl http://localhost:8081/q/health
```

## 🗂️ File Structure

```
chiro-erp/
├── Dockerfile.consolidated     # ONLY Dockerfile needed
├── scripts/deploy-final.ps1           # ONLY deployment script needed
├── docker-compose.consolidated.yml  # ONLY compose file needed
├── docs/DEPLOYMENT.md              # This documentation
├── consolidated-services/     # Source code for all services
├── buildSrc/                 # Gradle build conventions
└── deployment-backup/        # Backup of old deployment files
```

## 🎯 Deployment Strategies

### Development Deployment

-   Use `scripts\deploy-final.ps1 -Action infrastructure` first
-   Build and test services individually
-   Good for development and testing

### Production Deployment

-   Use `scripts\deploy-final.ps1 -Action full`
-   Deploys everything in the correct order
-   Includes health checks and monitoring

### Troubleshooting Deployment

-   Use `scripts\deploy-final.ps1 -Action status` to check current state
-   Use `scripts\deploy-final.ps1 -Action cleanup` to start fresh
-   Check Docker Desktop logs for detailed error information

## 🔐 Security Notes

-   All services run as non-root users (UID 1001)
-   Database uses dedicated user credentials (not root)
-   Alpine Linux base images for minimal attack surface
-   Health checks ensure services are responding correctly

## 📈 Monitoring

### Built-in Health Endpoints

-   `/q/health` - Overall health status
-   `/q/health/ready` - Readiness probe
-   `/q/health/live` - Liveness probe

### Prometheus Metrics

Available at `/q/metrics` on each service

### Logs

View logs with:

```powershell
docker logs chiro-erp-<service-name>
```

## 🚀 Next Steps

After successful deployment:

1. **Verify all services are healthy**
2. **Test database connectivity**
3. **Verify Kafka message processing**
4. **Run integration tests**
5. **Set up monitoring and alerting**

---

**This is the complete deployment solution for Chiro ERP. No other deployment files are needed.**

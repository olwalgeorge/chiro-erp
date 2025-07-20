# Docker & Infrastructure Guide

## Overview

This guide covers the complete Docker and infrastructure setup for the Chiro ERP system, including development, testing, and production environments.

## 📋 Table of Contents

-   [Quick Start](#quick-start)
-   [Architecture Overview](#architecture-overview)
-   [Development Environment](#development-environment)
-   [Build Automation](#build-automation)
-   [Production Deployment](#production-deployment)
-   [Monitoring & Observability](#monitoring--observability)
-   [Best Practices](#best-practices)

## 🚀 Quick Start

### Prerequisites

-   **Docker Desktop** 4.0+ with Docker Compose
-   **PowerShell 7+** (Windows) or Bash (Linux/Mac)
-   **Java 21+** (for local development)
-   **Git** for version control

### 1. Initial Setup

```powershell
# Clone the repository
git clone https://github.com/olwalgeorge/chiro-erp.git
cd chiro-erp

# Copy and customize environment
cp .env.template .env
# Edit .env with your configuration

# Make scripts executable (Linux/Mac)
chmod +x scripts/*.sh
```

### 2. Start Development Environment

```powershell
# Using the development script
./scripts/dev.ps1 -Action start

# Or manually with Docker Compose
docker compose -f docker-compose.dev.yml up -d
```

### 3. Build Services

```powershell
# Build all services
./scripts/build-automation.ps1 -Action all

# Build specific service
./scripts/build-automation.ps1 -Action build -Service "api-gateway"
```

## 🏗️ Architecture Overview

### Container Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Load Balancer (Traefik)                 │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
   ┌─────────┐   ┌─────────┐   ┌─────────┐
   │   API   │   │  User   │   │ Tenant  │
   │Gateway  │   │  Mgmt   │   │  Mgmt   │
   └─────────┘   └─────────┘   └─────────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
   ┌─────────┐   ┌─────────┐   ┌─────────┐
   │PostgreSQL│   │  Kafka  │   │  Redis  │
   └─────────┘   └─────────┘   └─────────┘
```

### Service Layers

1. **Infrastructure Layer**

    - PostgreSQL (Primary database)
    - Apache Kafka (Event streaming)
    - Redis (Caching & sessions)

2. **Application Layer**

    - API Gateway (Routing, auth, rate limiting)
    - Microservices (Business logic)

3. **Observability Layer**
    - Jaeger (Distributed tracing)
    - Prometheus (Metrics collection)
    - Grafana (Dashboards)

## 🛠️ Development Environment

### Docker Compose Files

| File                      | Purpose                                    |
| ------------------------- | ------------------------------------------ |
| `docker-compose.dev.yml`  | Development with hot reload, debug ports   |
| `docker-compose.prod.yml` | Production-ready with clustering, security |
| `docker-compose.yml`      | Original basic setup                       |

### Development Services

```yaml
# Infrastructure
- PostgreSQL 15 (port 5432)
- Redis 7 (port 6379)
- Apache Kafka (port 9092)
- Zookeeper (port 2181)

# Observability
- Kafka UI (port 8090)
- Jaeger (port 16686)
- Prometheus (port 9090)
- Grafana (port 3000)

# Applications
- API Gateway (port 8080)
- User Management (port 8081)
- [Other services as developed]
```

### Environment Configuration

Copy `.env.template` to `.env` and customize:

```env
# Database
DB_NAME=chiro_erp_dev
DB_USERNAME=chiro_dev
DB_PASSWORD=secure_password_123

# Kafka
KAFKA_PORT=9092

# Service Ports
API_GATEWAY_PORT=8080
USER_MANAGEMENT_PORT=8081
```

## 🔧 Build Automation

### PowerShell Build Script

The `scripts/build-automation.ps1` script provides comprehensive build automation:

```powershell
# Build all services
./scripts/build-automation.ps1 -Action all

# Build specific service
./scripts/build-automation.ps1 -Action build -Service "api-gateway"

# Test only
./scripts/build-automation.ps1 -Action test -SkipTests

# Docker images only
./scripts/build-automation.ps1 -Action docker

# Clean everything
./scripts/build-automation.ps1 -Action clean
```

### Development Workflow Script

The `scripts/dev.ps1` script simplifies development workflows:

```powershell
# Start development environment
./scripts/dev.ps1 -Action start

# Stop everything
./scripts/dev.ps1 -Action stop

# Check status
./scripts/dev.ps1 -Action status

# View logs
./scripts/dev.ps1 -Action logs -Service kafka -Follow

# Build and test
./scripts/dev.ps1 -Action build
./scripts/dev.ps1 -Action test
```

### Dockerfile Strategies

We provide multiple Dockerfile variants:

1. **Dockerfile.enhanced** - Multi-stage with optimization
2. **src/main/docker/Dockerfile.jvm** - Quarkus JVM mode
3. **src/main/docker/Dockerfile.native** - Quarkus native mode
4. **src/main/docker/Dockerfile.legacy-jar** - Legacy JAR deployment

### Build Features

✅ **Multi-stage builds** for smaller images
✅ **Layer caching** for faster builds  
✅ **Security scanning** with Trivy
✅ **Health checks** for all services
✅ **Non-root user** for security
✅ **Resource limits** and monitoring

## 🚀 Production Deployment

### High Availability Setup

```yaml
# Load Balancer
- Traefik with SSL/TLS termination
- Automatic service discovery
- Health check routing

# Database Cluster
- PostgreSQL with streaming replication
- Automated failover
- Backup automation

# Caching Cluster
- Redis Cluster (3 nodes)
- High availability
- Data persistence

# Message Streaming
- Kafka cluster with replication
- Topic auto-creation disabled
- Retention policies
```

### Security Features

-   **TLS encryption** for all traffic
-   **Secrets management** via environment variables
-   **Network segmentation** with custom networks
-   **Resource limits** to prevent abuse
-   **Health checks** for automatic recovery
-   **Non-root containers** for security

### Monitoring Stack

```yaml
# Metrics
- Prometheus for metrics collection
- Grafana for visualization
- Custom dashboards per service

# Tracing
- Jaeger for distributed tracing
- Request flow visualization
- Performance bottleneck identification

# Logging
- Centralized logging (can add ELK stack)
- Log aggregation and search
- Alert management
```

## 📊 Monitoring & Observability

### Available Dashboards

| Service    | URL                    | Credentials |
| ---------- | ---------------------- | ----------- |
| Grafana    | http://localhost:3000  | admin/admin |
| Prometheus | http://localhost:9090  | -           |
| Jaeger     | http://localhost:16686 | -           |
| Kafka UI   | http://localhost:8090  | -           |

### Key Metrics

-   **Application Performance**: Response time, throughput, errors
-   **Infrastructure**: CPU, memory, disk, network
-   **Business Metrics**: User registrations, transactions, revenue

### Health Checks

All services include comprehensive health checks:

-   `/health/live` - Liveness probe
-   `/health/ready` - Readiness probe
-   `/q/metrics` - Prometheus metrics endpoint

## 🎯 Best Practices

### Development

1. **Use .env files** for local configuration
2. **Run infrastructure first**, then applications
3. **Use health checks** to verify service readiness
4. **Monitor logs** during development
5. **Clean up** unused images and volumes regularly

### Production

1. **Use secrets management** for sensitive data
2. **Enable resource limits** for all containers
3. **Configure log retention** policies
4. **Set up automated backups** for data
5. **Monitor service health** continuously
6. **Use blue-green deployments** for zero downtime

### Security

1. **Never use default passwords** in production
2. **Enable TLS/SSL** for all external traffic
3. **Use non-root containers**
4. **Scan images** for vulnerabilities
5. **Limit network access** with firewalls
6. **Rotate secrets** regularly

## 🔄 CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/ci-cd.yml`) provides:

1. **Automated Testing**

    - Unit and integration tests
    - Code coverage reporting
    - Security vulnerability scanning

2. **Image Building**

    - Multi-architecture builds (AMD64, ARM64)
    - Layer caching for faster builds
    - Automated tagging and versioning

3. **Deployment**
    - Staging environment deployment
    - Production deployment approval
    - Rollback capabilities

## 🚨 Troubleshooting

### Common Issues

| Issue               | Solution                                             |
| ------------------- | ---------------------------------------------------- |
| Out of memory       | Increase Docker Desktop memory limit                 |
| Port conflicts      | Check `.env` file port assignments                   |
| Service won't start | Check logs: `docker compose logs [service]`          |
| Build failures      | Clean and rebuild: `./scripts/dev.ps1 -Action clean` |

### Useful Commands

```powershell
# Check service status
docker compose -f docker-compose.dev.yml ps

# View logs
docker compose -f docker-compose.dev.yml logs -f [service]

# Execute into container
docker compose -f docker-compose.dev.yml exec [service] /bin/bash

# Check resource usage
docker stats

# Clean up
docker system prune -af --volumes
```

## 📚 Next Steps

1. **Complete API Gateway** implementation
2. **Add authentication** service
3. **Implement service discovery**
4. **Set up centralized logging** (ELK stack)
5. **Add backup automation**
6. **Configure auto-scaling**

---

This infrastructure setup provides a solid foundation for developing, testing, and deploying the Chiro ERP system. The automated build and deployment processes ensure consistency across environments while maintaining security and performance standards.

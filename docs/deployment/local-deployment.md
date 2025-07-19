# Local Deployment Guide

## Overview

This guide covers running the entire Chiro-ERP system locally using Docker Compose for development and testing purposes.

## Prerequisites

-   Docker 24+ with Docker Compose
-   Minimum 8GB RAM available for Docker
-   20GB free disk space

## Quick Start

### 1. Clone and Navigate

```bash
git clone https://github.com/your-org/chiro-erp.git
cd chiro-erp
```

### 2. Start Infrastructure

```bash
# Start core infrastructure services
docker-compose up -d postgres kafka zookeeper redis

# Verify services are healthy
docker-compose ps
```

### 3. Build Services

```bash
# Build all service images
./gradlew build
docker-compose build
```

### 4. Start All Services

```bash
# Start all services
docker-compose up -d

# Monitor startup logs
docker-compose logs -f
```

### 5. Verify Deployment

```bash
# Health check for API Gateway
curl http://localhost:8080/health

# Check all services
./scripts/health-check.sh
```

## Docker Compose Configuration

### Current docker-compose.yml Enhancement

Let's enhance your current docker-compose.yml to include all services:

```yaml
version: "3.8"

services:
    # Infrastructure Services
    postgres:
        image: postgres:15
        environment:
            POSTGRES_DB: chiro_erp
            POSTGRES_USER: chiro
            POSTGRES_PASSWORD: chiro123
        ports:
            - "5432:5432"
        volumes:
            - postgres_data:/var/lib/postgresql/data
            - ./scripts/init-databases.sql:/docker-entrypoint-initdb.d/init-databases.sql
        healthcheck:
            test: ["CMD-SHELL", "pg_isready -U chiro"]
            interval: 10s
            timeout: 5s
            retries: 5

    zookeeper:
        image: confluentinc/cp-zookeeper:latest
        environment:
            ZOOKEEPER_CLIENT_PORT: 2181
            ZOOKEEPER_TICK_TIME: 2000
        healthcheck:
            test: ["CMD", "bash", "-c", "echo 'ruok' | nc localhost 2181"]
            interval: 10s
            timeout: 5s
            retries: 5

    kafka:
        image: confluentinc/cp-kafka:latest
        depends_on:
            zookeeper:
                condition: service_healthy
        ports:
            - "9092:9092"
        environment:
            KAFKA_BROKER_ID: 1
            KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
            KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
            KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
        healthcheck:
            test:
                [
                    "CMD",
                    "kafka-broker-api-versions.sh",
                    "--bootstrap-server",
                    "localhost:9092",
                ]
            interval: 30s
            timeout: 10s
            retries: 5

    redis:
        image: redis:7-alpine
        ports:
            - "6379:6379"
        command: redis-server --appendonly yes
        volumes:
            - redis_data:/data
        healthcheck:
            test: ["CMD", "redis-cli", "ping"]
            interval: 10s
            timeout: 5s
            retries: 5

    # Core Services
    api-gateway:
        build:
            context: ./api-gateway
            dockerfile: Dockerfile
        ports:
            - "8080:8080"
        depends_on:
            kafka:
                condition: service_healthy
            redis:
                condition: service_healthy
        environment:
            - KAFKA_BROKERS=kafka:9092
            - REDIS_URL=redis://redis:6379
            - JWT_SECRET=dev-secret-key
        healthcheck:
            test: ["CMD", "curl", "-f", "http://localhost:8080/q/health"]
            interval: 30s
            timeout: 10s
            retries: 5

    user-management-service:
        build:
            context: ./services/user-management-service
            dockerfile: Dockerfile
        ports:
            - "8081:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_user_management
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    tenant-management-service:
        build:
            context: ./services/tenant-management-service
            dockerfile: Dockerfile
        ports:
            - "8082:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_tenant_management
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    crm-service:
        build:
            context: ./services/crm-service
            dockerfile: Dockerfile
        ports:
            - "8083:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_crm
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    sales-service:
        build:
            context: ./services/sales-service
            dockerfile: Dockerfile
        ports:
            - "8084:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_sales
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    inventory-service:
        build:
            context: ./services/inventory-service
            dockerfile: Dockerfile
        ports:
            - "8085:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_inventory
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    manufacturing-service:
        build:
            context: ./services/manufacturing-service
            dockerfile: Dockerfile
        ports:
            - "8086:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_manufacturing
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    procurement-service:
        build:
            context: ./services/procurement-service
            dockerfile: Dockerfile
        ports:
            - "8087:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_procurement
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    finance-service:
        build:
            context: ./services/finance-service
            dockerfile: Dockerfile
        ports:
            - "8088:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_finance
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    billing-service:
        build:
            context: ./services/billing-service
            dockerfile: Dockerfile
        ports:
            - "8089:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_billing
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    notifications-service:
        build:
            context: ./services/notifications-service
            dockerfile: Dockerfile
        ports:
            - "8096:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_notifications
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    analytics-service:
        build:
            context: ./services/analytics-service
            dockerfile: Dockerfile
        ports:
            - "8097:8080"
        depends_on:
            postgres:
                condition: service_healthy
            kafka:
                condition: service_healthy
        environment:
            - DB_URL=jdbc:postgresql://postgres:5432/chiro_analytics
            - DB_USER=chiro
            - DB_PASSWORD=chiro123
            - KAFKA_BROKERS=kafka:9092

    # Monitoring & Management
    prometheus:
        image: prom/prometheus:latest
        ports:
            - "9090:9090"
        volumes:
            - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
            - prometheus_data:/prometheus
        command:
            - "--config.file=/etc/prometheus/prometheus.yml"
            - "--storage.tsdb.path=/prometheus"
            - "--web.console.libraries=/usr/share/prometheus/console_libraries"
            - "--web.console.templates=/usr/share/prometheus/consoles"

    grafana:
        image: grafana/grafana:latest
        ports:
            - "3000:3000"
        environment:
            - GF_SECURITY_ADMIN_PASSWORD=admin
        volumes:
            - grafana_data:/var/lib/grafana
            - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
            - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources

volumes:
    postgres_data:
    redis_data:
    prometheus_data:
    grafana_data:
```

## Database Initialization

Create `scripts/init-databases.sql`:

```sql
-- Create databases for each service
CREATE DATABASE chiro_user_management;
CREATE DATABASE chiro_tenant_management;
CREATE DATABASE chiro_crm;
CREATE DATABASE chiro_sales;
CREATE DATABASE chiro_inventory;
CREATE DATABASE chiro_manufacturing;
CREATE DATABASE chiro_procurement;
CREATE DATABASE chiro_finance;
CREATE DATABASE chiro_billing;
CREATE DATABASE chiro_hr;
CREATE DATABASE chiro_project;
CREATE DATABASE chiro_fieldservice;
CREATE DATABASE chiro_repair;
CREATE DATABASE chiro_fleet;
CREATE DATABASE chiro_pos;
CREATE DATABASE chiro_notifications;
CREATE DATABASE chiro_analytics;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE chiro_user_management TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_tenant_management TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_crm TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_sales TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_inventory TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_manufacturing TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_procurement TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_finance TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_billing TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_hr TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_project TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_fieldservice TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_repair TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_fleet TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_pos TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_notifications TO chiro;
GRANT ALL PRIVILEGES ON DATABASE chiro_analytics TO chiro;
```

## Service Management Scripts

### Health Check Script

Create `scripts/health-check.sh`:

```bash
#!/bin/bash

services=(
    "api-gateway:8080"
    "user-management-service:8081"
    "tenant-management-service:8082"
    "crm-service:8083"
    "sales-service:8084"
    "inventory-service:8085"
    "manufacturing-service:8086"
    "procurement-service:8087"
    "finance-service:8088"
    "billing-service:8089"
    "notifications-service:8096"
    "analytics-service:8097"
)

echo "🏥 Checking service health..."

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"

    if curl -sf "http://localhost:${port}/q/health" > /dev/null; then
        echo "✅ $name (port $port) - Healthy"
    else
        echo "❌ $name (port $port) - Unhealthy"
    fi
done

echo "🏁 Health check completed"
```

### Service Starter Script

Create `scripts/start-services.sh`:

```bash
#!/bin/bash

echo "🚀 Starting Chiro-ERP services..."

# Start infrastructure first
echo "📡 Starting infrastructure services..."
docker-compose up -d postgres kafka zookeeper redis

# Wait for infrastructure to be ready
echo "⏳ Waiting for infrastructure services..."
sleep 30

# Start core services
echo "🔧 Starting core services..."
docker-compose up -d api-gateway user-management-service tenant-management-service

# Wait for core services
sleep 20

# Start business services
echo "💼 Starting business services..."
docker-compose up -d crm-service sales-service inventory-service manufacturing-service procurement-service finance-service billing-service

# Start operational services
echo "⚙️ Starting operational services..."
docker-compose up -d hr-service project-service fieldservice-service repair-service fleet-service pos-service

# Start support services
echo "📢 Starting support services..."
docker-compose up -d notifications-service analytics-service

# Start monitoring
echo "📊 Starting monitoring services..."
docker-compose up -d prometheus grafana

echo "✅ All services started!"
echo "🌐 Access points:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Grafana: http://localhost:3000 (admin/admin)"
echo "  - Prometheus: http://localhost:9090"
```

## Development Profiles

### Development Environment

Create `docker-compose.dev.yml`:

```yaml
version: "3.8"

services:
    api-gateway:
        environment:
            - QUARKUS_PROFILE=dev
            - QUARKUS_LOG_LEVEL=DEBUG
        volumes:
            - ./api-gateway/src:/app/src:cached
        command: ["./gradlew", "quarkusDev"]

    crm-service:
        environment:
            - QUARKUS_PROFILE=dev
            - QUARKUS_LOG_LEVEL=DEBUG
        volumes:
            - ./services/crm-service/src:/app/src:cached
        command: ["./gradlew", "quarkusDev"]
```

Usage:

```bash
# Start in development mode
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

### Testing Environment

Create `docker-compose.test.yml`:

```yaml
version: "3.8"

services:
    postgres-test:
        image: postgres:15
        environment:
            POSTGRES_DB: chiro_erp_test
            POSTGRES_USER: chiro_test
            POSTGRES_PASSWORD: chiro_test123
        ports:
            - "5433:5432"
        tmpfs: /var/lib/postgresql/data

    kafka-test:
        image: confluentinc/cp-kafka:latest
        environment:
            KAFKA_BROKER_ID: 2
            KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
            KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9093
            KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
        ports:
            - "9093:9092"
```

## Monitoring Setup

### Prometheus Configuration

Create `monitoring/prometheus.yml`:

```yaml
global:
    scrape_interval: 15s

scrape_configs:
    - job_name: "chiro-erp-services"
      static_configs:
          - targets:
                [
                    "api-gateway:8080",
                    "user-management-service:8080",
                    "crm-service:8080",
                    "sales-service:8080",
                    "inventory-service:8080",
                ]
      metrics_path: /q/metrics
```

### Grafana Dashboard

Create `monitoring/grafana/dashboards/chiro-erp-overview.json` with service metrics.

## Common Operations

### Start Specific Services

```bash
# Start only infrastructure
docker-compose up -d postgres kafka zookeeper redis

# Start specific service
docker-compose up -d crm-service

# Scale service
docker-compose up -d --scale crm-service=3
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f crm-service

# Last N lines
docker-compose logs --tail=100 crm-service
```

### Database Operations

```bash
# Connect to PostgreSQL
docker exec -it chiro-erp-postgres-1 psql -U chiro -d chiro_crm

# Backup database
docker exec chiro-erp-postgres-1 pg_dump -U chiro chiro_crm > backup.sql

# Restore database
docker exec -i chiro-erp-postgres-1 psql -U chiro -d chiro_crm < backup.sql
```

### Kafka Operations

```bash
# List topics
docker exec chiro-erp-kafka-1 kafka-topics.sh --list --bootstrap-server localhost:9092

# Create topic
docker exec chiro-erp-kafka-1 kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# Monitor messages
docker exec chiro-erp-kafka-1 kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic chiro.dev.crm.customer.created --from-beginning
```

### Cleanup

```bash
# Stop all services
docker-compose down

# Remove volumes (⚠️ Data loss)
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Complete cleanup
docker system prune -a
```

## Troubleshooting

### Port Conflicts

```bash
# Find process using port
lsof -i :8080
netstat -ano | findstr :8080  # Windows

# Change port mapping
docker-compose up -d --scale api-gateway=1 -p 8081:8080
```

### Memory Issues

```bash
# Increase Docker memory limit
# Docker Desktop → Settings → Resources → Memory → 8GB

# Check container resource usage
docker stats
```

### Service Dependencies

```bash
# Check service dependencies
docker-compose config --services

# Start with dependency order
docker-compose up --remove-orphans
```

### Network Issues

```bash
# Inspect networks
docker network ls
docker network inspect chiro-erp_default

# Recreate network
docker-compose down
docker network prune
docker-compose up -d
```

---

_This local deployment setup provides a complete development environment that mirrors production architecture while remaining easy to manage._

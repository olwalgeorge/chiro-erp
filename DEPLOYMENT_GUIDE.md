# Deployment Guide - Chiro ERP Platform

This guide covers deployment strategies and procedures for the Chiro ERP microservices platform.

## 🚀 Deployment Overview

### Platform Architecture

-   **6 Microservices**: 1 API Gateway + 5 Consolidated Services
-   **Technology Stack**: Quarkus 3.24.4, Kotlin 2.1.21, PostgreSQL
-   **Build Tool**: Gradle 8.14 with Kotlin DSL
-   **Java Runtime**: OpenJDK 21 LTS

### Deployment Modes

1. **Development**: Local development with live reload
2. **Staging**: Docker containers with PostgreSQL
3. **Production**: Kubernetes with native compilation
4. **Native**: GraalVM native executables for cloud deployment

## 🛠️ Pre-deployment Setup

### 1. Environment Validation

```powershell
# Validate dependencies and configurations
.\fix-dependencies.ps1
.\fix-database-config.ps1

# Run comprehensive build test
.\gradlew clean build
```

### 2. Database Setup

```sql
-- Create databases for each service
CREATE DATABASE chiro_core_business_service;
CREATE DATABASE chiro_customer_relations_service;
CREATE DATABASE chiro_operations_management_service;
CREATE DATABASE chiro_platform_services;
CREATE DATABASE chiro_workforce_management_service;

-- Create user with appropriate permissions
CREATE USER chiro_user WITH PASSWORD 'chiro_password';
GRANT ALL PRIVILEGES ON DATABASE chiro_core_business_service TO chiro_user;
GRANT ALL PRIVILEGES ON DATABASE chiro_customer_relations_service TO chiro_user;
GRANT ALL PRIVILEGES ON DATABASE chiro_operations_management_service TO chiro_user;
GRANT ALL PRIVILEGES ON DATABASE chiro_platform_services TO chiro_user;
GRANT ALL PRIVILEGES ON DATABASE chiro_workforce_management_service TO chiro_user;
```

## 🐳 Docker Deployment

### 1. Build Docker Images

```bash
# Build all services with Docker images
./gradlew build -Dquarkus.container-image.build=true

# Or build individual services
./gradlew :api-gateway:build -Dquarkus.container-image.build=true
./gradlew :consolidated-services:core-business-service:build -Dquarkus.container-image.build=true
```

### 2. Docker Compose Deployment

```yaml
# docker-compose.yml
version: "3.8"

services:
    postgres:
        image: postgres:15
        environment:
            POSTGRES_DB: chiro_platform
            POSTGRES_USER: chiro_user
            POSTGRES_PASSWORD: chiro_password
        ports:
            - "5432:5432"
        volumes:
            - postgres_data:/var/lib/postgresql/data

    api-gateway:
        image: chiro/api-gateway:latest
        ports:
            - "8080:8080"
        environment:
            QUARKUS_HTTP_PORT: 8080
            QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/chiro_platform
        depends_on:
            - postgres

    core-business-service:
        image: chiro/core-business-service:latest
        ports:
            - "8081:8081"
        environment:
            QUARKUS_HTTP_PORT: 8081
            QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/chiro_core_business_service
        depends_on:
            - postgres

volumes:
    postgres_data:
```

### 3. Deploy with Docker Compose

```bash
# Start the platform
docker-compose up -d

# Check service health
curl http://localhost:8080/q/health
curl http://localhost:8081/q/health

# View logs
docker-compose logs -f api-gateway
```

## ☸️ Kubernetes Deployment

### 1. Kubernetes Manifests

#### API Gateway Deployment

```yaml
# k8s/api-gateway-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
    name: api-gateway
spec:
    replicas: 3
    selector:
        matchLabels:
            app: api-gateway
    template:
        metadata:
            labels:
                app: api-gateway
        spec:
            containers:
                - name: api-gateway
                  image: chiro/api-gateway:latest
                  ports:
                      - containerPort: 8080
                  env:
                      - name: QUARKUS_HTTP_PORT
                        value: "8080"
                      - name: QUARKUS_DATASOURCE_JDBC_URL
                        value: "jdbc:postgresql://postgres-service:5432/chiro_platform"
                  resources:
                      limits:
                          memory: "512Mi"
                          cpu: "500m"
                      requests:
                          memory: "256Mi"
                          cpu: "250m"
                  readinessProbe:
                      httpGet:
                          path: /q/health/ready
                          port: 8080
                      initialDelaySeconds: 10
                      periodSeconds: 5
                  livenessProbe:
                      httpGet:
                          path: /q/health/live
                          port: 8080
                      initialDelaySeconds: 30
                      periodSeconds: 10
```

#### Service Configuration

```yaml
# k8s/api-gateway-service.yaml
apiVersion: v1
kind: Service
metadata:
    name: api-gateway-service
spec:
    selector:
        app: api-gateway
    ports:
        - port: 80
          targetPort: 8080
    type: LoadBalancer
```

### 2. Deploy to Kubernetes

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get deployments
kubectl get services
kubectl get pods

# Check service health
kubectl port-forward service/api-gateway-service 8080:80
curl http://localhost:8080/q/health
```

## 🚀 Native Compilation Deployment

### 1. Build Native Images

```bash
# Build native executables (requires GraalVM)
./gradlew build -Dquarkus.native.enabled=true

# Or use container-based native build
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

### 2. Native Docker Images

```dockerfile
# Dockerfile.native
FROM registry.access.redhat.com/ubi8/ubi-minimal:8.9
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root build/*-runner /work/application
EXPOSE 8080
USER 1001
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
```

### 3. Performance Benefits

-   **Startup Time**: ~0.1 seconds (vs ~2-3 seconds JVM)
-   **Memory Usage**: ~50MB (vs ~200MB JVM)
-   **Container Size**: ~100MB (vs ~300MB JVM)

## 🔧 Production Configuration

### 1. Environment Variables

```bash
# Database configuration
export QUARKUS_DATASOURCE_USERNAME=chiro_user
export QUARKUS_DATASOURCE_PASSWORD=chiro_password
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://prod-db:5432/chiro_db

# Service configuration
export QUARKUS_HTTP_PORT=8080
export QUARKUS_APPLICATION_NAME=chiro-erp
export QUARKUS_LOG_LEVEL=INFO

# Performance tuning
export QUARKUS_THREAD_POOL_MAX_THREADS=100
export QUARKUS_DATASOURCE_MAX_SIZE=20
```

### 2. JVM Tuning (Non-Native)

```bash
# JVM arguments for production
-Xmx2g -Xms1g
-XX:+UseG1GC
-XX:+UseStringDeduplication
-Djava.util.logging.manager=org.jboss.logmanager.LogManager
```

### 3. Security Configuration

```properties
# Security settings
quarkus.security.enabled=true
quarkus.oidc.auth-server-url=https://auth.company.com/auth/realms/chiro
quarkus.oidc.client-id=chiro-erp
quarkus.oidc.credentials.secret=${OIDC_CLIENT_SECRET}
```

## 📊 Monitoring & Observability

### 1. Health Checks

```bash
# Service health endpoints
curl http://api-gateway:8080/q/health
curl http://api-gateway:8080/q/health/ready
curl http://api-gateway:8080/q/health/live
```

### 2. Metrics Collection

```bash
# Prometheus metrics
curl http://api-gateway:8080/q/metrics
```

### 3. Logging Configuration

```properties
# Centralized logging
quarkus.log.console.enable=true
quarkus.log.console.format=%d{HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n
quarkus.log.category."org.chiro".level=DEBUG
```

## 🔄 Deployment Automation

### 1. PowerShell Deployment Script

```powershell
# deploy-final.ps1 Usage
.\deploy-final.ps1 -Action build          # Build all services
.\deploy-final.ps1 -Action docker         # Build and create Docker images
.\deploy-final.ps1 -Action kubernetes     # Deploy to Kubernetes
.\deploy-final.ps1 -Action native         # Build native executables
```

### 2. CI/CD Pipeline Integration

```yaml
# GitHub Actions workflow
name: Deploy Chiro ERP
on:
    push:
        branches: [main]

jobs:
    deploy:
        runs-on: ubuntu-latest
        steps:
            - uses: actions/checkout@v3
            - uses: actions/setup-java@v3
              with:
                  java-version: "21"
                  distribution: "graalvm"

            - name: Build and Test
              run: ./gradlew clean build

            - name: Build Native Images
              run: ./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true

            - name: Deploy to Kubernetes
              run: kubectl apply -f k8s/
```

## 🚨 Troubleshooting

### Common Deployment Issues

#### Database Connection Failures

```bash
# Check database connectivity
pg_isready -h localhost -p 5432 -U chiro_user

# Verify database configuration
kubectl exec -it api-gateway-pod -- env | grep QUARKUS_DATASOURCE
```

#### Service Discovery Issues

```bash
# Check service resolution
kubectl get services
kubectl describe service api-gateway-service

# Test internal connectivity
kubectl exec -it test-pod -- curl http://api-gateway-service/q/health
```

#### Memory Issues

```bash
# Monitor memory usage
kubectl top pods
docker stats

# Adjust resource limits
kubectl patch deployment api-gateway -p '{"spec":{"template":{"spec":{"containers":[{"name":"api-gateway","resources":{"limits":{"memory":"1Gi"}}}]}}}}'
```

### Performance Tuning

#### Database Performance

```sql
-- Optimize PostgreSQL for microservices
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
SELECT pg_reload_conf();
```

#### Application Performance

```properties
# Optimize Quarkus settings
quarkus.datasource.max-size=20
quarkus.datasource.min-size=5
quarkus.thread-pool.max-threads=100
quarkus.vertx.event-loops-pool-size=4
```

## 📈 Scaling Strategies

### Horizontal Scaling

```bash
# Scale API Gateway
kubectl scale deployment api-gateway --replicas=5

# Scale business services
kubectl scale deployment core-business-service --replicas=3
```

### Database Scaling

-   **Read Replicas**: For read-heavy workloads
-   **Sharding**: For data partitioning
-   **Connection Pooling**: PgBouncer for connection management

### Load Balancing

-   **Ingress Controller**: NGINX or Traefik for external traffic
-   **Service Mesh**: Istio for advanced traffic management
-   **Circuit Breakers**: Built-in Quarkus fault tolerance

## 🎯 Production Checklist

-   [ ] Database migrations completed
-   [ ] Environment variables configured
-   [ ] Security certificates installed
-   [ ] Monitoring dashboards configured
-   [ ] Log aggregation configured
-   [ ] Backup procedures tested
-   [ ] Disaster recovery plan validated
-   [ ] Performance benchmarks established
-   [ ] Health checks configured
-   [ ] Auto-scaling policies defined

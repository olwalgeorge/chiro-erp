# Dependency Architecture Documentation

This document describes the comprehensive dependency architecture implemented in the Chiro ERP microservices platform.

## 🏗️ Overall Architecture

### Microservices REST Communication Pattern

The Chiro ERP platform implements a modern REST-based microservices architecture using the new Quarkus REST implementation (not legacy JAX-RS). This architecture supports both synchronous REST communication and reactive patterns.

## 🔧 Core Dependencies

### Quarkus Platform

-   **Version**: 3.24.4 (Latest LTS)
-   **BOM**: `io.quarkus.platform:quarkus-bom:3.24.4`
-   **Enforced Platform**: All services use enforced platform for version consistency

### Kotlin Support

-   **Version**: 2.1.21 (Latest stable)
-   **Plugins**:
    -   `kotlin("jvm")` - Core Kotlin JVM support
    -   `kotlin("plugin.allopen")` - All-open plugin for frameworks
    -   `kotlin("plugin.serialization")` - Kotlin serialization support

### Java Target

-   **Version**: Java 21 LTS
-   **Source/Target Compatibility**: VERSION_21
-   **JVM Target**: JVM_21

## 🌐 REST Architecture

### REST Server Dependencies

```kotlin
// REST Server (new Quarkus REST for exposing APIs)
implementation("io.quarkus:quarkus-rest")
```

**Purpose**: Exposes RESTful APIs for each microservice
**Features**:

-   Reactive by default
-   Built-in JSON support
-   Automatic OpenAPI generation
-   Better performance than legacy JAX-RS

### REST Client Dependencies

```kotlin
// REST Client (new Quarkus REST for inter-service communication)
implementation("io.quarkus:quarkus-rest-client")
```

**Purpose**: Enables microservices to communicate with each other
**Features**:

-   Reactive REST client
-   Declarative client interfaces
-   Automatic service discovery integration
-   Circuit breaker support

## 📊 Serialization Strategy

### Dual Serialization Support

The platform implements a dual serialization strategy to optimize for different use cases:

#### Internal Communication (Kotlin Serialization)

```kotlin
implementation("io.quarkus:quarkus-rest-kotlin-serialization")
```

-   **Use Case**: Service-to-service internal communication
-   **Benefits**:
    -   Optimized for Kotlin data classes
    -   Better performance for internal APIs
    -   Type-safe serialization
    -   Smaller payload sizes

#### External Communication (Jackson)

```kotlin
implementation("io.quarkus:quarkus-rest-jackson")
```

-   **Use Case**: External client APIs and third-party integrations
-   **Benefits**:
    -   Industry standard JSON processing
    -   Better compatibility with external systems
    -   Rich annotation support
    -   Flexible data transformation

## 🗄️ Database Layer

### Reactive Database Stack

```kotlin
// Database dependencies
implementation("io.quarkus:quarkus-hibernate-reactive-panache-kotlin")
implementation("io.quarkus:quarkus-jdbc-postgresql")
```

**Features**:

-   **Hibernate Reactive Panache**: Reactive ORM with simplified APIs
-   **Kotlin Extensions**: Kotlin-specific extensions for better syntax
-   **PostgreSQL**: Production-ready relational database
-   **Non-blocking I/O**: Fully reactive database operations

## 📊 Observability & Configuration

### Common Observability Dependencies

```kotlin
// Configuration and observability
implementation("io.quarkus:quarkus-config-yaml")
implementation("io.quarkus:quarkus-micrometer")
implementation("io.quarkus:quarkus-smallrye-health")
```

**Features**:

-   **YAML Configuration**: Better configuration management
-   **Micrometer**: Metrics collection and monitoring
-   **Health Checks**: Service health monitoring and readiness probes

## 🚪 API Gateway Specific Dependencies

### Enhanced Gateway Features

```kotlin
// API Gateway specific features
implementation("io.quarkus:quarkus-smallrye-openapi")
implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
implementation("io.quarkus:quarkus-security")
implementation("io.quarkus:quarkus-oidc")
implementation("io.quarkus:quarkus-cache")
```

**Purpose**: API Gateway acts as the unified entry point
**Features**:

-   **OpenAPI**: Automatic API documentation generation
-   **Fault Tolerance**: Circuit breakers, retries, timeouts
-   **Security**: Authentication and authorization
-   **OIDC**: OpenID Connect integration
-   **Caching**: Performance optimization

## 🔍 Service Type Classification

### Business Services (Full Stack)

-   **Services**: core-business-service, customer-relations-service, operations-management-service
-   **Dependencies**: Full database + REST stack
-   **Use Case**: Core business logic with data persistence

### Simple Services (Lightweight)

-   **Services**: platform-services, workforce-management-service
-   **Dependencies**: REST only, minimal database dependencies
-   **Use Case**: Utility services, notification services, lightweight operations

### API Gateway (Enhanced)

-   **Service**: api-gateway
-   **Dependencies**: Enhanced REST + security + observability
-   **Use Case**: Unified API entry point with comprehensive gateway features

## 🧪 Testing Dependencies

### Test Stack

```kotlin
// Test dependencies
testImplementation("io.quarkus:quarkus-junit5")
testImplementation("io.rest-assured:rest-assured")
testImplementation("io.quarkus:quarkus-test-security") // API Gateway only
```

**Features**:

-   **JUnit 5**: Modern testing framework
-   **REST Assured**: REST API testing
-   **Test Security**: Security testing for API Gateway

## 🔄 Dependency Management Scripts

### Automated Standardization

The platform includes automation scripts for dependency management:

#### `fix-dependencies.ps1`

-   **Purpose**: Standardizes all Gradle build files
-   **Features**:
    -   Service-specific dependency templates
    -   Automatic repository configuration
    -   Version consistency enforcement
    -   Validation and error checking

#### `fix-database-config.ps1`

-   **Purpose**: Standardizes database configurations
-   **Features**:
    -   PostgreSQL configuration standardization
    -   Service-specific database naming
    -   Port management for multiple services

## 📈 Performance Characteristics

### Reactive Performance

-   **Startup Time**: Sub-second with proper configuration
-   **Memory Usage**: Optimized with reactive stack
-   **Throughput**: High throughput with non-blocking I/O
-   **Scalability**: Horizontal scaling with stateless services

### Native Compilation Ready

All dependencies are compatible with GraalVM native compilation for:

-   Faster startup times
-   Reduced memory footprint
-   Better cloud performance

## 🛠️ Development Workflow

### 1. Dependency Updates

```bash
# Run dependency standardization
.\fix-dependencies.ps1

# Validate changes
.\gradlew build
```

### 2. Database Configuration

```bash
# Standardize database configs
.\fix-database-config.ps1

# Test database connectivity
.\gradlew :consolidated-services:core-business-service:build
```

### 3. Service Testing

```bash
# Individual service testing
.\gradlew :api-gateway:build
.\gradlew :consolidated-services:core-business-service:build

# Full project testing
.\gradlew build
```

## 🚨 Common Issues & Solutions

### Build Failures

-   **Issue**: Missing or incorrect dependencies
-   **Solution**: Run `.\fix-dependencies.ps1` to standardize

### Database Connection Errors

-   **Issue**: H2 vs PostgreSQL configuration conflicts
-   **Solution**: Run `.\fix-database-config.ps1` to standardize

### Version Conflicts

-   **Issue**: Dependency version mismatches
-   **Solution**: Use enforced platform BOM for consistency

### REST Client Errors

-   **Issue**: Incorrect REST client dependency names
-   **Solution**: Use `quarkus-rest-client` (not `quarkus-rest-client-reactive`)

## 🎯 Future Enhancements

### Planned Improvements

1. **Service Mesh Integration**: Istio/Envoy for advanced traffic management
2. **Event Streaming**: Kafka integration for event-driven architecture
3. **Advanced Monitoring**: Distributed tracing with Jaeger
4. **Multi-tenancy**: Enhanced tenant isolation and management

### Migration Path

The architecture is designed to support gradual migration to:

-   Event-driven patterns
-   CQRS implementation
-   Advanced security models
-   Cloud-native deployments

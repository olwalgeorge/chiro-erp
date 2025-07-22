# Chiro ERP - Modern Microservices Platform

A comprehensive Enterprise Resource Planning (ERP) system built with modern microservices architecture using Quarkus 3.24.4, Kotlin 2.1.21, and the new Quarkus REST implementation.

## 📁 Project Organization

This workspace is now organized for better maintainability:

```
chiro-erp/
├── 📚 docs/                    # All documentation 
├── 🔧 scripts/                 # All automation scripts
├── 💼 consolidated-services/   # Business service implementations
├── 🚪 api-gateway/            # API Gateway service
├── ☸️ kubernetes/             # Kubernetes manifests
├── ⚙️ config/                  # Configuration files
└── 🐳 docker-compose.*.yml    # Docker composition files
```

## 🚀 Quick Start

### Deployment
```powershell
# Check system status
.\scripts\deploy-final.ps1 -Action status

# Deploy everything (recommended)
.\scripts\deploy-final.ps1 -Action full
```

### Development
```powershell
# Fix dependencies and validate structure
.\scripts\fix-dependencies.ps1
.\scripts\verify-service-structure-consistency.ps1

# Build and test
.\gradlew clean build
```

## 📚 Documentation

Comprehensive documentation is available in the `docs/` directory:

- **[docs/README.md](./docs/README.md)** - Documentation index and navigation
- **[docs/DEPLOYMENT.md](./docs/DEPLOYMENT.md)** - Quick deployment reference
- **[docs/DEPLOYMENT_GUIDE.md](./docs/DEPLOYMENT_GUIDE.md)** - Complete deployment guide
- **[docs/BOUNDED_CONTEXTS_ARCHITECTURE.md](./docs/BOUNDED_CONTEXTS_ARCHITECTURE.md)** - Domain-Driven Design architecture
- **[docs/DEPENDENCY_ARCHITECTURE.md](./docs/DEPENDENCY_ARCHITECTURE.md)** - Technical dependency documentation

## 🏗️ Architecture Overview

### Microservices Structure

-   **API Gateway** (`api-gateway`) - Unified entry point with routing, security, and fault tolerance
-   **Core Business Service** (`consolidated-services/core-business-service`) - Finance, Sales, Procurement, Manufacturing, Inventory
-   **Customer Relations Service** (`consolidated-services/customer-relations-service`) - CRM and Billing
-   **Operations Management Service** (`consolidated-services/operations-management-service`) - Field Service, Fleet, POS, Project Management, Repair
-   **Platform Services** (`consolidated-services/platform-services`) - Notifications and Tenant Management
-   **Workforce Management Service** (`consolidated-services/workforce-management-service`) - HR and User Management

### Technology Stack

-   **Framework**: Quarkus 3.24.4 (Supersonic Subatomic Java Framework)
-   **Language**: Kotlin 2.1.21 with serialization support
-   **REST API**: New Quarkus REST implementation (not legacy JAX-RS)
-   **Database**: PostgreSQL with Hibernate Reactive Panache
-   **Build Tool**: Gradle 8.14 with Kotlin DSL
-   **Java Version**: 21 (LTS)
-   **Serialization**: Dual strategy - Kotlin for internal, Jackson for external

### REST Communication Architecture

-   **REST Server**: `quarkus-rest` for exposing APIs
-   **REST Client**: `quarkus-rest-client` for inter-service communication
-   **Dual Serialization**:
    -   Kotlin serialization for internal service-to-service communication
    -   Jackson for external API compatibility and client integration

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## 🚀 Quick Start & Development

### Prerequisites

-   Java 21 (OpenJDK or Oracle)
-   PostgreSQL 13+ running locally
-   Gradle 8.14+ (wrapper included)

### Database Setup

Each service uses its own PostgreSQL database:

```bash
# Core Business Service
createdb chiro_core_business_service

# Customer Relations Service
createdb chiro_customer_relations_service

# Operations Management Service
createdb chiro_operations_management_service

# Platform Services
createdb chiro_platform_services

# Workforce Management Service
createdb chiro_workforce_management_service
```

### Building the Project

#### Full Project Build

```shell script
./gradlew build
```

#### Individual Service Build

```shell script
# API Gateway
./gradlew :api-gateway:build

# Specific consolidated service
./gradlew :consolidated-services:core-business-service:build
```

#### Clean Build

```shell script
./gradlew clean build
```

### Running in Development Mode

#### API Gateway (Entry Point)

```shell script
./gradlew :api-gateway:quarkusDev
```

Access at: http://localhost:8080

#### Individual Services

```shell script
# Core Business Service (Port 8081)
./gradlew :consolidated-services:core-business-service:quarkusDev

# Customer Relations Service (Port 8082)
./gradlew :consolidated-services:customer-relations-service:quarkusDev

# Operations Management Service (Port 8083)
./gradlew :consolidated-services:operations-management-service:quarkusDev

# Platform Services (Port 8084)
./gradlew :consolidated-services:platform-services:quarkusDev

# Workforce Management Service (Port 8085)
./gradlew :consolidated-services:workforce-management-service:quarkusDev
```

## 🔧 Development Tools

### Dependency Management

Use the comprehensive dependency standardization script:

```powershell
# Update all service dependencies
.\fix-dependencies.ps1

# Dry run to preview changes
.\fix-dependencies.ps1 -DryRun
```

### Database Configuration Management

Standardize all database configurations:

```powershell
# Update all application.properties to use PostgreSQL
.\fix-database-config.ps1

# Preview changes
.\fix-database-config.ps1 -DryRun
```

### File Protection System

Automated backup and git integration:

```powershell
# Setup file protection
.\setup-file-protection.ps1

# Manual backup creation
.\create-backup.ps1
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/chiro-erp-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

-   Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
-   JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

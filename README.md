# Chiro ERP - Modern Microservices Architecture

A modern ERP system built with **Quarkus 3.24.4**, **Kotlin 2.1.21**, and **Java 21**, featuring a consolidated microservices architecture that maintains Domain-Driven Design principles while improving operational efficiency.

## 🏗️ Architecture Overview

The system is organized into **6 consolidated services** that maintain bounded context isolation:

### 🌐 API Gateway (`port 8080`)

-   **Path**: `api-gateway/`
-   **Purpose**: Central entry point with routing, security, and observability
-   **Features**: CORS, health checks, metrics, fault tolerance

### 💼 Core Business Service (`port 8081`)

-   **Path**: `consolidated-services/core-business-service/`
-   **Bounded Contexts**: Finance, Inventory, Sales
-   **Database**: H2 (development), Hibernate ORM

### 🤝 Customer Relations Service (`port 8083`)

-   **Path**: `consolidated-services/customer-relations-service/`
-   **Bounded Contexts**: CRM, Support
-   **Type**: Lightweight REST service

### ⚙️ Operations Management Service (`port 8082`)

-   **Path**: `consolidated-services/operations-management-service/`
-   **Bounded Contexts**: Field Service, Manufacturing, Project Management
-   **Type**: Business process orchestration

### 🛠️ Platform Services (`port 8084`)

-   **Path**: `consolidated-services/platform-services/`
-   **Bounded Contexts**: User Management, Tenant Management, Notifications
-   **Database**: H2 (development), Hibernate ORM

### 👥 Workforce Management Service (`port 8085`)

-   **Path**: `consolidated-services/workforce-management-service/`
-   **Bounded Contexts**: HR, Fleet Management
-   **Database**: H2 (development), Hibernate ORM

## 🚀 Quick Start

### Prerequisites

-   **Java 21** or later
-   **Gradle** (included via wrapper)

### Running the Application

1. **Build all services:**

    ```bash
    ./gradlew clean build
    ```

2. **Run individual services in dev mode:**

    ```bash
    # API Gateway
    ./gradlew :api-gateway:quarkusDev

    # Core Business Service
    ./gradlew :consolidated-services:core-business-service:quarkusDev

    # Platform Services
    ./gradlew :consolidated-services:platform-services:quarkusDev
    ```

3. **Access services:**
    - API Gateway: http://localhost:8080
    - Core Business: http://localhost:8081
    - Operations: http://localhost:8082
    - Customer Relations: http://localhost:8083
    - Platform Services: http://localhost:8084
    - Workforce: http://localhost:8085

### Health Checks

All services provide health endpoints:

-   `http://localhost:{port}/q/health`
-   `http://localhost:{port}/q/health/live`
-   `http://localhost:{port}/q/health/ready`

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

## 🛠️ Development Tools

### PowerShell Scripts

The project includes comprehensive PowerShell automation scripts:

-   **`fix-dependencies.ps1`**: Standardizes dependencies and configurations across all services
-   **`validate-dependencies.ps1`**: Validates build configurations
-   **`deploy-comprehensive.ps1`**: Comprehensive deployment automation

### Running the Fix Script

```powershell
# Dry run to see what would be changed
.\fix-dependencies.ps1 -DryRun

# Apply fixes
.\fix-dependencies.ps1
```

## 🏛️ Technology Stack

-   **Runtime**: Java 21
-   **Language**: Kotlin 2.1.21
-   **Framework**: Quarkus 3.24.4
-   **Database**: H2 (development), Hibernate ORM
-   **Build Tool**: Gradle with Kotlin DSL
-   **Architecture**: REST-first microservices
-   **Serialization**: Kotlin Serialization
-   **Testing**: JUnit 5, REST Assured

## 📂 Project Structure

```
chiro-erp/
├── api-gateway/                           # API Gateway service
├── consolidated-services/                 # Business services
│   ├── core-business-service/            # Finance, Inventory, Sales
│   ├── customer-relations-service/       # CRM, Support
│   ├── operations-management-service/    # Field Service, Manufacturing
│   ├── platform-services/               # User/Tenant Management
│   └── workforce-management-service/     # HR, Fleet
├── kubernetes/                           # K8s deployment manifests
├── docs/                                # Architecture documentation
├── scripts/                             # Development automation
└── *.ps1                               # PowerShell automation scripts
```

## 🎯 Domain-Driven Design

The architecture maintains **bounded context isolation** within consolidated services through:

-   **Package-based separation** of domain models
-   **Independent application services** per context
-   **Separate infrastructure adapters** per context
-   **Context-specific REST endpoints**

See [BOUNDED_CONTEXTS_ARCHITECTURE.md](BOUNDED_CONTEXTS_ARCHITECTURE.md) for detailed information.

## 📋 Available Commands

### Build & Test

```bash
./gradlew clean build                    # Full build
./gradlew test                          # Run tests
./gradlew build -x test                 # Build without tests
```

### Development

```bash
./gradlew quarkusDev                    # Run with live reload
./gradlew quarkusUpdate                 # Update Quarkus dependencies
```

### Native Compilation

```bash
./gradlew build -Dquarkus.native.enabled=true                    # Native build
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true    # Container native build
```

## 📚 Documentation

-   [Bounded Contexts Architecture](BOUNDED_CONTEXTS_ARCHITECTURE.md)
-   [Deployment Guide](DEPLOYMENT.md)
-   [Development Productivity Tools](PRODUCTIVITY_TOOLS.md)
-   [Recent Optimizations](RECENT_OPTIMIZATIONS_SUMMARY.md)

## 🔧 Configuration

All services use standardized configuration patterns:

-   **Ports**: Sequential assignment (8080-8085)
-   **Database**: H2 for development, configurable for production
-   **Health checks**: Enabled on all services
-   **Logging**: Structured with DEBUG level for org.chiro packages

## 🚢 Deployment

The project supports multiple deployment strategies:

-   **Local development**: Individual service startup
-   **Docker**: Containerized deployment
-   **Kubernetes**: Production-ready manifests in `/kubernetes`

## 🤝 Contributing

1. Use the `fix-dependencies.ps1` script to maintain consistent configurations
2. Follow the established bounded context patterns
3. Ensure all services build successfully with `./gradlew clean build`
4. Add appropriate tests for new functionality

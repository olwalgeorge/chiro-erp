# Chiro ERP Consolidation Script

This repository contains a comprehensive PowerShell script to refactor the Chiro ERP microservices architecture into consolidated containerized monolithic services.

## Overview

The consolidation script transforms your existing microservices architecture into a more manageable set of domain-focused services while maintaining modularity and containerization capabilities.

## What It Does

### Service Consolidation Strategy

The script consolidates **17 original microservices** into **5 domain-focused consolidated services**:

#### 1. **core-business-service** (Port: 8080)

-   **Database**: `chiro_core_business`
-   **Consolidates**:
    -   finance-service
    -   inventory-service
    -   sales-service
    -   procurement-service
    -   pos-service

#### 2. **operations-management-service** (Port: 8081)

-   **Database**: `chiro_operations`
-   **Consolidates**:
    -   fleet-service
    -   project-service
    -   manufacturing-service
    -   fieldservice-service
    -   repair-service

#### 3. **customer-relations-service** (Port: 8082)

-   **Database**: `chiro_customer_relations`
-   **Consolidates**:
    -   crm-service
    -   analytics-service

#### 4. **platform-services** (Port: 8083)

-   **Database**: `chiro_platform`
-   **Consolidates**:
    -   tenant-management-service
    -   user-management-service
    -   notifications-service
    -   billing-service

#### 5. **workforce-management-service** (Port: 8084)

-   **Database**: `chiro_workforce`
-   **Consolidates**:
    -   hr-service

## Usage

### Prerequisites

-   PowerShell 5.1 or PowerShell Core 7+
-   Existing Chiro ERP microservices structure

### Running the Script

#### Dry Run (Recommended First)

```powershell
.\consolidate-to-monolithic-services.ps1 -DryRun
```

#### With Backup of Original Structure

```powershell
.\consolidate-to-monolithic-services.ps1 -BackupOriginal
```

#### Full Execution

```powershell
.\consolidate-to-monolithic-services.ps1
```

### Validation

After running the script, validate the structure:

```powershell
.\validate-consolidation.ps1
```

For detailed structure view:

```powershell
.\validate-consolidation.ps1 -Detailed
```

## Generated Structure

### Directory Layout

```
consolidated-services/
├── core-business-service/
│   ├── src/main/kotlin/org/chiro/core_business_service/
│   ├── src/main/resources/
│   ├── src/test/kotlin/org/chiro/core_business_service/
│   ├── modules/
│   │   ├── finance/
│   │   ├── inventory/
│   │   ├── sales/
│   │   ├── procurement/
│   │   └── pos/
│   ├── docker/
│   │   └── Dockerfile
│   ├── build.gradle.kts
│   └── README.md
├── operations-management-service/
│   └── ... (similar structure)
├── customer-relations-service/
│   └── ... (similar structure)
├── platform-services/
│   └── ... (similar structure)
└── workforce-management-service/
    └── ... (similar structure)
```

### Generated Files

#### For Each Consolidated Service:

-   **build.gradle.kts**: Complete Gradle build script with Quarkus dependencies
-   **Application.kt**: Main Quarkus application class
-   **application.properties**: Service-specific configuration
-   **Dockerfile**: Containerization configuration
-   **README.md**: Service documentation

#### Root Level:

-   **settings.gradle.kts**: Updated Gradle settings for consolidated services
-   **docker-compose.consolidated.yml**: Docker Compose for all services
-   **CONSOLIDATION_MIGRATION_GUIDE.md**: Comprehensive migration guide
-   **scripts/sql/init-databases.sql**: Database initialization script
-   **scripts/dev-consolidated.ps1**: Development utilities

## Architecture Benefits

### 1. **Reduced Operational Complexity**

-   Fewer services to deploy, monitor, and maintain
-   Simplified service mesh and networking
-   Reduced resource overhead

### 2. **Maintained Modularity**

-   Clear module boundaries within each service
-   Domain-driven service grouping
-   Easy to extract modules back to microservices if needed

### 3. **Improved Development Experience**

-   Simplified debugging across related features
-   Reduced context switching between services
-   Better IDE support for cross-module refactoring

### 4. **Enhanced Resource Utilization**

-   Shared JVM resources within consolidated services
-   Better database connection pooling
-   Reduced container overhead

### 5. **Flexible Deployment Options**

-   Can deploy as monolith or individual services
-   Supports gradual migration strategies
-   Maintains containerization benefits

## Module Structure

Each original microservice becomes a module within the appropriate consolidated service:

```
modules/
├── finance/
│   ├── src/main/kotlin/    # Original service code
│   ├── src/test/kotlin/    # Original tests
│   └── resources/          # Module-specific resources
└── inventory/
    ├── src/main/kotlin/
    ├── src/test/kotlin/
    └── resources/
```

## API Structure

Each module maintains its own API endpoints under a module-specific prefix:

-   **Original**: `http://finance-service:8080/api/v1/accounts`
-   **Consolidated**: `http://core-business-service:8080/finance/api/v1/accounts`

## Development Workflow

### Starting Services

```powershell
# Start all services
.\scripts\dev-consolidated.ps1 start

# Start specific service
.\scripts\dev-consolidated.ps1 start core-business-service

# View logs
.\scripts\dev-consolidated.ps1 logs core-business-service
```

### Building and Testing

```powershell
# Build all services
.\scripts\dev-consolidated.ps1 build

# Test specific service
.\scripts\dev-consolidated.ps1 test platform-services

# Clean all artifacts
.\scripts\dev-consolidated.ps1 clean
```

## Database Configuration

Each consolidated service uses its own database:

-   **core-business-service**: `chiro_core_business`
-   **operations-management-service**: `chiro_operations`
-   **customer-relations-service**: `chiro_customer_relations`
-   **platform-services**: `chiro_platform`
-   **workforce-management-service**: `chiro_workforce`

The script automatically generates initialization scripts for all databases.

## Migration Path

### Immediate Benefits

1. **Reduced Deployment Complexity**: Deploy 5 services instead of 17
2. **Simplified Monitoring**: Fewer endpoints to monitor
3. **Better Resource Usage**: Shared resources within domains

### Future Flexibility

1. **Service Extraction**: Easy to extract modules back to microservices
2. **Gradual Migration**: Can migrate one domain at a time
3. **Team Boundaries**: Clear ownership per consolidated service

## Monitoring and Observability

Each consolidated service provides:

-   **Health Checks**: `/q/health`
-   **Metrics**: `/q/metrics` (Prometheus format)
-   **Application Info**: `/q/info`

The generated Docker Compose includes:

-   **Prometheus**: Service metrics collection
-   **Grafana**: Dashboards and visualization

## Next Steps After Running the Script

1. **Review Generated Structure**: Check all consolidated services
2. **Read Migration Guide**: Follow `CONSOLIDATION_MIGRATION_GUIDE.md`
3. **Update CI/CD**: Modify pipelines for new service structure
4. **Test Services**: Use development scripts to verify functionality
5. **Update Documentation**: Reflect new architecture in docs
6. **Train Team**: Educate on new service boundaries and workflows

## Rollback Strategy

If needed, the original microservices structure is preserved:

1. **Backup Available**: If run with `-BackupOriginal` flag
2. **Original Services**: Remain in `services/` directory
3. **Gradle Settings**: Can be reverted to original configuration

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Ensure ports 8080-8084 are available
2. **Database Connections**: Verify PostgreSQL is running
3. **Kafka Dependencies**: Ensure Kafka is available for messaging

### Getting Help

-   Check the generated README files for each service
-   Review the migration guide for detailed instructions
-   Use the validation script to verify structure
-   Check Docker Compose logs for runtime issues

## Contributing

When adding new features to consolidated services:

1. Identify the appropriate consolidated service
2. Create or update the relevant module
3. Follow the established package structure
4. Update tests at the module level
5. Update the service's README with changes

This consolidation script provides a robust foundation for transitioning to a more manageable service architecture while preserving the benefits of modularity and containerization.

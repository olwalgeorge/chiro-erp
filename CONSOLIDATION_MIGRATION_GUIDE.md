# Migration Guide: Microservices to Consolidated Services

## Overview
This guide explains how to migrate from the original microservices architecture to the new consolidated monolithic services approach.

## What Changed

### Service Consolidation
The original 17 microservices have been consolidated into 5 domain-focused services:



## Benefits of Consolidation

1. **Reduced Operational Complexity**: Fewer services to deploy, monitor, and maintain
2. **Simplified Inter-Service Communication**: Related functionality is co-located
3. **Better Resource Utilization**: Reduced overhead from separate JVM instances
4. **Easier Development**: Simplified debugging and testing across related features
5. **Maintained Modularity**: Clear module boundaries within each service

## Migration Steps

### 1. Update Build Configuration
`ash
# The new settings.gradle.kts includes consolidated services
./gradlew clean build
`

### 2. Database Migration
`ash
# Run the database initialization script
docker-compose -f docker-compose.consolidated.yml up postgres
`

### 3. Start Consolidated Services
`ash
# Start all consolidated services
docker-compose -f docker-compose.consolidated.yml up
`

### 4. Update API Gateway Configuration
Update your API Gateway routing to point to the new consolidated services and their module-specific endpoints.

### 5. Update Client Applications
Update any client applications to use the new service endpoints:
- Original: http://finance-service:8080/api/v1/accounts
- New: http://core-business-service:8080/finance/api/v1/accounts

## Development Workflow

### Running Individual Services
`ash
# Run a specific consolidated service in dev mode
./gradlew :consolidated-services:core-business-service:quarkusDev

# Run all services
./gradlew quarkusDev
`

### Adding New Features
1. Identify the appropriate consolidated service
2. Create or update the relevant module
3. Add tests at the module level
4. Update API documentation

### Testing
`ash
# Test all consolidated services
./gradlew test

# Test a specific service
./gradlew :consolidated-services:core-business-service:test
`

## Rollback Strategy
If needed, the original microservices structure is preserved in the backup directory. To rollback:
1. Stop consolidated services
2. Restore from backup
3. Restart original microservices

## Monitoring and Observability
- Each consolidated service exposes metrics on /q/metrics
- Health checks available at /q/health
- Prometheus configuration updated for new service endpoints
- Grafana dashboards need to be updated for new service structure

## Next Steps
1. Update CI/CD pipelines for consolidated services
2. Update monitoring and alerting configurations
3. Update documentation and runbooks
4. Train team on new service structure
5. Consider further optimization based on usage patterns

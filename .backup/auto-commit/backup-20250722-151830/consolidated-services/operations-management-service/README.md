# operations-management-service

Operational services for fleet, projects, manufacturing, and field services

## Consolidated Services
This service consolidates the following original microservices:


## Architecture
This service follows a modular monolith pattern where each original service becomes a module within the consolidated service. Each module maintains its own:
- Domain models and entities
- Business logic and services  
- REST endpoints (with module-specific path prefixes)
- Database migrations (in separate schema/namespaces)

## Running the Service

### Development Mode
`ash
./gradlew :consolidated-services:operations-management-service:quarkusDev
`

### Building
`ash
./gradlew :consolidated-services:operations-management-service:build
`

### Docker
`ash
# Build the service
./gradlew :consolidated-services:operations-management-service:build

# Build Docker image
docker build -f consolidated-services/operations-management-service/docker/Dockerfile -t chiro/operations-management-service:latest .

# Run container
docker run -p 8081:8081 chiro/operations-management-service:latest
`

## Database
- Database: chiro_operations
- Port: 8081
- Migrations: src/main/resources/db/migration

## API Endpoints
Each module exposes its endpoints under a module-specific prefix:


## Module Structure
`
modules/

`

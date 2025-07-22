# core-business-service

Core ERP business operations including finance, inventory, sales, and procurement

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
./gradlew :consolidated-services:core-business-service:quarkusDev
`

### Building
`ash
./gradlew :consolidated-services:core-business-service:build
`

### Docker
`ash
# Build the service
./gradlew :consolidated-services:core-business-service:build

# Build Docker image
docker build -f consolidated-services/core-business-service/docker/Dockerfile -t chiro/core-business-service:latest .

# Run container
docker run -p 8080:8080 chiro/core-business-service:latest
`

## Database
- Database: chiro_core_business
- Port: 8080
- Migrations: src/main/resources/db/migration

## API Endpoints
Each module exposes its endpoints under a module-specific prefix:


## Module Structure
`
modules/

`

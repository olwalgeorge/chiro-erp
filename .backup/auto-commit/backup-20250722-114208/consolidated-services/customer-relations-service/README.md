# customer-relations-service

Customer relationship management and analytics

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
./gradlew :consolidated-services:customer-relations-service:quarkusDev
`

### Building
`ash
./gradlew :consolidated-services:customer-relations-service:build
`

### Docker
`ash
# Build the service
./gradlew :consolidated-services:customer-relations-service:build

# Build Docker image
docker build -f consolidated-services/customer-relations-service/docker/Dockerfile -t chiro/customer-relations-service:latest .

# Run container
docker run -p 8082:8082 chiro/customer-relations-service:latest
`

## Database
- Database: chiro_customer_relations
- Port: 8082
- Migrations: src/main/resources/db/migration

## API Endpoints
Each module exposes its endpoints under a module-specific prefix:


## Module Structure
`
modules/

`

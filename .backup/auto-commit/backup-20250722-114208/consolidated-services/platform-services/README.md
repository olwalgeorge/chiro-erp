# platform-services

Platform infrastructure including tenancy, users, notifications, and billing

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
./gradlew :consolidated-services:platform-services:quarkusDev
`

### Building
`ash
./gradlew :consolidated-services:platform-services:build
`

### Docker
`ash
# Build the service
./gradlew :consolidated-services:platform-services:build

# Build Docker image
docker build -f consolidated-services/platform-services/docker/Dockerfile -t chiro/platform-services:latest .

# Run container
docker run -p 8083:8083 chiro/platform-services:latest
`

## Database
- Database: chiro_platform
- Port: 8083
- Migrations: src/main/resources/db/migration

## API Endpoints
Each module exposes its endpoints under a module-specific prefix:


## Module Structure
`
modules/

`

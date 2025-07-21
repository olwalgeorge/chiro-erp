#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Consolidates the Chiro ERP microservices architecture into containerized monolithic services
    
.DESCRIPTION
    This script refactors the existing microservices into domain-based consolidated services
    while maintaining clear boundaries and containerization capabilities. It follows best 
    practices for modular monoliths with microservices-ready architecture.
    
.PARAMETER DryRun
    If specified, shows what would be done without making actual changes
    
.PARAMETER BackupOriginal
    If specified, creates backup of original structure before refactoring
    
.EXAMPLE
    .\consolidate-to-monolithic-services.ps1 -DryRun
    .\consolidate-to-monolithic-services.ps1 -BackupOriginal
#>

param(
    [switch]$DryRun,
    [switch]$BackupOriginal
)

# Set error handling
$ErrorActionPreference = "Stop"

# Define color output functions
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Info { param($Message) Write-Host "ℹ️  $Message" -ForegroundColor Cyan }
function Write-Warning { param($Message) Write-Host "⚠️  $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }

Write-Info "Starting Chiro ERP Consolidation to Monolithic Services"
Write-Info "Working Directory: $(Get-Location)"

# Define the consolidation mapping
$ConsolidationMap = @{
    "core-business-service"         = @{
        "description" = "Core ERP business operations including finance, inventory, sales, and procurement"
        "services"    = @("finance-service", "inventory-service", "sales-service", "procurement-service", "pos-service")
        "port"        = 8080
        "database"    = "chiro_core_business"
    }
    "operations-management-service" = @{
        "description" = "Operational services for fleet, projects, manufacturing, and field services"
        "services"    = @("fleet-service", "project-service", "manufacturing-service", "fieldservice-service", "repair-service")
        "port"        = 8081
        "database"    = "chiro_operations"
    }
    "customer-relations-service"    = @{
        "description" = "Customer relationship management and analytics"
        "services"    = @("crm-service", "analytics-service")
        "port"        = 8082
        "database"    = "chiro_customer_relations"
    }
    "platform-services"             = @{
        "description" = "Platform infrastructure including tenancy, users, notifications, and billing"
        "services"    = @("tenant-management-service", "user-management-service", "notifications-service", "billing-service")
        "port"        = 8083
        "database"    = "chiro_platform"
    }
    "workforce-management-service"  = @{
        "description" = "Human resources and workforce management"
        "services"    = @("hr-service")
        "port"        = 8084
        "database"    = "chiro_workforce"
    }
}

# Function to create directory if it doesn't exist
function New-DirectoryIfNotExists {
    param([string]$Path)
    if (-not (Test-Path $Path)) {
        if (-not $DryRun) {
            New-Item -ItemType Directory -Path $Path -Force | Out-Null
        }
        Write-Info "Created directory: $Path"
    }
    else {
        Write-Warning "Directory already exists: $Path"
    }
}

# Function to create file with content
function New-FileWithContent {
    param(
        [string]$Path,
        [string]$Content
    )
    if (-not $DryRun) {
        Set-Content -Path $Path -Value $Content -Encoding UTF8
    }
    Write-Info "Created file: $Path"
}

# Function to copy files and merge configurations
function Copy-ServiceFiles {
    param(
        [string]$SourceService,
        [string]$TargetService,
        [string]$TargetModule
    )
    
    $sourcePath = "services/$SourceService"
    $targetPath = "consolidated-services/$TargetService/modules/$TargetModule"
    
    if (Test-Path $sourcePath) {
        Write-Info "Copying $SourceService to $TargetModule module in $TargetService"
        
        if (-not $DryRun) {
            # Copy source files
            if (Test-Path "$sourcePath/src") {
                Copy-Item -Path "$sourcePath/src" -Destination "$targetPath" -Recurse -Force
            }
            
            # Copy configuration files
            if (Test-Path "$sourcePath/src/main/resources") {
                New-DirectoryIfNotExists "$targetPath/resources"
                Copy-Item -Path "$sourcePath/src/main/resources/*" -Destination "$targetPath/resources" -Recurse -Force
            }
        }
    }
    else {
        Write-Warning "Source service not found: $sourcePath"
    }
}

# Backup original structure if requested
if ($BackupOriginal -and -not $DryRun) {
    $backupPath = "backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Write-Info "Creating backup at: $backupPath"
    Copy-Item -Path "services" -Destination $backupPath -Recurse
    Write-Success "Backup created successfully"
}

# Create consolidated services directory structure
Write-Info "Creating consolidated services structure..."
New-DirectoryIfNotExists "consolidated-services"

foreach ($serviceName in $ConsolidationMap.Keys) {
    $serviceConfig = $ConsolidationMap[$serviceName]
    $servicePath = "consolidated-services/$serviceName"
    
    Write-Info "Creating consolidated service: $serviceName"
    Write-Info "  Description: $($serviceConfig.description)"
    Write-Info "  Consolidating: $($serviceConfig.services -join ', ')"
    
    # Create main service structure
    New-DirectoryIfNotExists $servicePath
    New-DirectoryIfNotExists "$servicePath/src/main/kotlin/org/chiro/$($serviceName.Replace('-', '_'))"
    New-DirectoryIfNotExists "$servicePath/src/main/resources"
    New-DirectoryIfNotExists "$servicePath/src/test/kotlin/org/chiro/$($serviceName.Replace('-', '_'))"
    New-DirectoryIfNotExists "$servicePath/modules"
    New-DirectoryIfNotExists "$servicePath/docker"
    
    # Create modular structure for each original service
    foreach ($originalService in $serviceConfig.services) {
        $moduleName = $originalService.Replace('-service', '')
        New-DirectoryIfNotExists "$servicePath/modules/$moduleName"
        New-DirectoryIfNotExists "$servicePath/modules/$moduleName/src/main/kotlin/org/chiro/$($serviceName.Replace('-', '_'))/$moduleName"
        New-DirectoryIfNotExists "$servicePath/modules/$moduleName/src/test/kotlin/org/chiro/$($serviceName.Replace('-', '_'))/$moduleName"
        
        # Copy existing service files
        Copy-ServiceFiles -SourceService $originalService -TargetService $serviceName -TargetModule $moduleName
    }
    
    # Create consolidated build.gradle.kts
    $buildGradleContent = @"
plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
    id("org.jetbrains.kotlin.plugin.jpa")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("`${quarkusPlatformGroupId}:`${quarkusPlatformArtifactId}:`${quarkusPlatformVersion}"))
    
    // Quarkus Core
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-jackson")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    
    // Database
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")
    
    // Messaging
    implementation("io.quarkus:quarkus-kafka-client")
    implementation("io.quarkus:quarkus-kafka-streams")
    
    // Monitoring & Health
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-health")
    implementation("io.quarkus:quarkus-info")
    
    // Security
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-security-jpa")
    
    // Validation
    implementation("io.quarkus:quarkus-hibernate-validator")
    
    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-test-h2")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        javaParameters = true
    }
}
"@
    
    New-FileWithContent -Path "$servicePath/build.gradle.kts" -Content $buildGradleContent
    
    # Create application.properties
    $applicationProperties = @"
# Application Configuration for $serviceName
quarkus.application.name=$serviceName
quarkus.http.port=$($serviceConfig.port)

# Database Configuration
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=chiro_user
quarkus.datasource.password=chiro_password
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/$($serviceConfig.database)

# Hibernate Configuration
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.sql-load-script=no-file

# Flyway Configuration
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=classpath:db/migration

# Kafka Configuration
kafka.bootstrap.servers=localhost:9092
mp.messaging.outgoing.events.connector=smallrye-kafka
mp.messaging.outgoing.events.topic=$($serviceName.Replace('-', '_'))_events

# Monitoring
quarkus.micrometer.export.prometheus.enabled=true
quarkus.health.extensions.enabled=true

# Logging
quarkus.log.level=INFO
quarkus.log.category."org.chiro".level=DEBUG

# Development
%dev.quarkus.log.console.enable=true
%dev.quarkus.hibernate-orm.log.sql=true
"@
    
    New-FileWithContent -Path "$servicePath/src/main/resources/application.properties" -Content $applicationProperties
    
    # Create main application class
    $className = ($serviceName.Split('-') | ForEach-Object { $_.Substring(0, 1).ToUpper() + $_.Substring(1) }) -join ''
    $mainClass = @"
package org.chiro.$($serviceName.Replace('-', '_'))

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.enterprise.context.ApplicationScoped

@QuarkusMain
class ${className}Application : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    Quarkus.run(${className}Application::class.java, *args)
}
"@
    
    New-FileWithContent -Path "$servicePath/src/main/kotlin/org/chiro/$($serviceName.Replace('-', '_'))/Application.kt" -Content $mainClass
    
    # Create Dockerfile for the service
    $dockerfileContent = @"
FROM registry.access.redhat.com/ubi8/openjdk-17:1.15

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 build/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 build/quarkus-app/*.jar /deployments/
COPY --chown=185 build/quarkus-app/app/ /deployments/app/
COPY --chown=185 build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE $($serviceConfig.port)
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
"@
    
    New-FileWithContent -Path "$servicePath/docker/Dockerfile" -Content $dockerfileContent
    
    # Create README for the service
    $readmeContent = @"
# $serviceName

$($serviceConfig.description)

## Consolidated Services
This service consolidates the following original microservices:
$($serviceConfig.services | ForEach-Object { "- $_" } | Join-String "`n")

## Architecture
This service follows a modular monolith pattern where each original service becomes a module within the consolidated service. Each module maintains its own:
- Domain models and entities
- Business logic and services  
- REST endpoints (with module-specific path prefixes)
- Database migrations (in separate schema/namespaces)

## Running the Service

### Development Mode
```bash
./gradlew :consolidated-services:$serviceName`:quarkusDev
```

### Building
```bash
./gradlew :consolidated-services:$serviceName`:build
```

### Docker
```bash
# Build the service
./gradlew :consolidated-services:$serviceName`:build

# Build Docker image
docker build -f consolidated-services/$serviceName/docker/Dockerfile -t chiro/$serviceName`:latest .

# Run container
docker run -p $($serviceConfig.port):$($serviceConfig.port) chiro/$serviceName`:latest
```

## Database
- Database: $($serviceConfig.database)
- Port: $($serviceConfig.port)
- Migrations: `src/main/resources/db/migration`

## API Endpoints
Each module exposes its endpoints under a module-specific prefix:
$($serviceConfig.services | ForEach-Object { 
    $module = $_.Replace('-service', '')
    "- /$module/* - $_ endpoints"
} | Join-String "`n")

## Module Structure
```
modules/
$($serviceConfig.services | ForEach-Object { 
    $module = $_.Replace('-service', '')
    "├── $module/`n│   ├── src/main/kotlin/`n│   ├── src/test/kotlin/`n│   └── resources/"
} | Join-String "`n")
```
"@
    
    New-FileWithContent -Path "$servicePath/README.md" -Content $readmeContent
}

# Update root settings.gradle.kts to include consolidated services
Write-Info "Updating root settings.gradle.kts..."

# Build the includes section
$includeStatements = $ConsolidationMap.Keys | ForEach-Object { "include(`"consolidated-services:$_`")" }
$projectNameStatements = $ConsolidationMap.Keys | ForEach-Object { "project(`":consolidated-services:$_`").name = `"$_`"" }

$settingsGradleContent = @"
rootProject.name = "chiro-erp"

// Include original API Gateway (remains separate)
include("api-gateway")

// Include consolidated services
$($includeStatements -join "`n")

// Configure project names for consolidated services  
$($projectNameStatements -join "`n")
"@

if (-not $DryRun) {
    New-FileWithContent -Path "settings.gradle.kts" -Content $settingsGradleContent
}

# Create new docker-compose for consolidated services
Write-Info "Creating docker-compose for consolidated services..."

# Build consolidated services section
$consolidatedServicesYaml = $ConsolidationMap.Keys | ForEach-Object {
    $serviceName = $_
    $serviceConfig = $ConsolidationMap[$serviceName]
    @"
  ${serviceName}:
    build:
      context: .
      dockerfile: consolidated-services/$serviceName/docker/Dockerfile
    ports:
      - "$($serviceConfig.port):$($serviceConfig.port)"
    environment:
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/$($serviceConfig.database)
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - postgres
      - kafka
"@
} | Join-String "`n`n"

# Build environment variables for API Gateway
$apiGatewayEnvVars = $ConsolidationMap.Keys | ForEach-Object {
    $serviceName = $_
    $serviceConfig = $ConsolidationMap[$serviceName]
    "      $($serviceName.ToUpper().Replace('-', '_'))_URL: http://${serviceName}:$($serviceConfig.port)"
} | Join-String "`n"

# Build depends_on for API Gateway
$apiGatewayDependsOn = $ConsolidationMap.Keys | ForEach-Object { "      - $_" } | Join-String "`n"

$dockerComposeContent = @"
version: '3.8'

services:
  # Database Services
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: chiro_erp
      POSTGRES_USER: chiro_user
      POSTGRES_PASSWORD: chiro_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/sql/init-databases.sql:/docker-entrypoint-initdb.d/init-databases.sql

  # Message Broker
  kafka:
    image: confluentinc/cp-kafka:7.4.0
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:29092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  # Consolidated Services
$consolidatedServicesYaml

  # API Gateway (remains separate)
  api-gateway:
    build:
      context: .
      dockerfile: api-gateway/docker/Dockerfile
    ports:
      - "8080:8080"
    environment:
      # Service discovery endpoints
$apiGatewayEnvVars
    depends_on:
$apiGatewayDependsOn

  # Monitoring
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./config/prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin

volumes:
  postgres_data:
"@

New-FileWithContent -Path "docker-compose.consolidated.yml" -Content $dockerComposeContent

# Create database initialization script
Write-Info "Creating database initialization script..."
$dbInitScript = @"
-- Initialize databases for consolidated services
$($ConsolidationMap.Keys | ForEach-Object {
    $serviceConfig = $ConsolidationMap[$_]
    "CREATE DATABASE $($serviceConfig.database);"
} | Join-String "`n")

-- Grant permissions
$($ConsolidationMap.Keys | ForEach-Object {
    $serviceConfig = $ConsolidationMap[$_]
    "GRANT ALL PRIVILEGES ON DATABASE $($serviceConfig.database) TO chiro_user;"
} | Join-String "`n")
"@

New-DirectoryIfNotExists "scripts/sql"
New-FileWithContent -Path "scripts/sql/init-databases.sql" -Content $dbInitScript

# Create migration guide
Write-Info "Creating migration guide..."

# Build service consolidation documentation
$serviceCount = ($ConsolidationMap.Values.services | ForEach-Object { $_ } | Sort-Object -Unique | Measure-Object).Count
$consolidatedServiceDocs = $ConsolidationMap.Keys | ForEach-Object {
    $serviceName = $_
    $serviceConfig = $ConsolidationMap[$serviceName]
    @"
### $serviceName
- **Description**: $($serviceConfig.description)
- **Port**: $($serviceConfig.port)
- **Database**: $($serviceConfig.database)
- **Consolidated Services**: $($serviceConfig.services -join ', ')
"@
} | Join-String "`n`n"

$migrationGuide = @"
# Migration Guide: Microservices to Consolidated Services

## Overview
This guide explains how to migrate from the original microservices architecture to the new consolidated monolithic services approach.

## What Changed

### Service Consolidation
The original $serviceCount microservices have been consolidated into $($ConsolidationMap.Count) domain-focused services:

$consolidatedServiceDocs

## Benefits of Consolidation

1. **Reduced Operational Complexity**: Fewer services to deploy, monitor, and maintain
2. **Simplified Inter-Service Communication**: Related functionality is co-located
3. **Better Resource Utilization**: Reduced overhead from separate JVM instances
4. **Easier Development**: Simplified debugging and testing across related features
5. **Maintained Modularity**: Clear module boundaries within each service

## Migration Steps

### 1. Update Build Configuration
```bash
# The new settings.gradle.kts includes consolidated services
./gradlew clean build
```

### 2. Database Migration
```bash
# Run the database initialization script
docker-compose -f docker-compose.consolidated.yml up postgres
```

### 3. Start Consolidated Services
```bash
# Start all consolidated services
docker-compose -f docker-compose.consolidated.yml up
```

### 4. Update API Gateway Configuration
Update your API Gateway routing to point to the new consolidated services and their module-specific endpoints.

### 5. Update Client Applications
Update any client applications to use the new service endpoints:
- Original: `http://finance-service:8080/api/v1/accounts`
- New: `http://core-business-service:8080/finance/api/v1/accounts`

## Development Workflow

### Running Individual Services
```bash
# Run a specific consolidated service in dev mode
./gradlew :consolidated-services:core-business-service:quarkusDev

# Run all services
./gradlew quarkusDev
```

### Adding New Features
1. Identify the appropriate consolidated service
2. Create or update the relevant module
3. Add tests at the module level
4. Update API documentation

### Testing
```bash
# Test all consolidated services
./gradlew test

# Test a specific service
./gradlew :consolidated-services:core-business-service:test
```

## Rollback Strategy
If needed, the original microservices structure is preserved in the backup directory. To rollback:
1. Stop consolidated services
2. Restore from backup
3. Restart original microservices

## Monitoring and Observability
- Each consolidated service exposes metrics on `/q/metrics`
- Health checks available at `/q/health`
- Prometheus configuration updated for new service endpoints
- Grafana dashboards need to be updated for new service structure

## Next Steps
1. Update CI/CD pipelines for consolidated services
2. Update monitoring and alerting configurations
3. Update documentation and runbooks
4. Train team on new service structure
5. Consider further optimization based on usage patterns
"@

New-FileWithContent -Path "CONSOLIDATION_MIGRATION_GUIDE.md" -Content $migrationGuide

# Create development scripts
Write-Info "Creating development scripts..."

# Build the ValidateSet for services
$serviceNames = $ConsolidationMap.Keys -join '", "'

$devScript = @"
#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Development utilities for consolidated Chiro ERP services
#>

param(
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "restart", "logs", "build", "test", "clean")]
    [string]`$Action = "start",
    
    [Parameter(Position=1)]
    [ValidateSet("all", "$serviceNames")]
    [string]`$Service = "all"
)

function Start-Services {
    param([string]`$ServiceName)
    
    if (`$ServiceName -eq "all") {
        Write-Host "Starting all consolidated services..." -ForegroundColor Green
        docker-compose -f docker-compose.consolidated.yml up -d
    } else {
        Write-Host "Starting `$ServiceName..." -ForegroundColor Green
        docker-compose -f docker-compose.consolidated.yml up -d `$ServiceName postgres kafka zookeeper
    }
}

function Stop-Services {
    param([string]`$ServiceName)
    
    if (`$ServiceName -eq "all") {
        Write-Host "Stopping all services..." -ForegroundColor Yellow
        docker-compose -f docker-compose.consolidated.yml down
    } else {
        Write-Host "Stopping `$ServiceName..." -ForegroundColor Yellow
        docker-compose -f docker-compose.consolidated.yml stop `$ServiceName
    }
}

function Show-Logs {
    param([string]`$ServiceName)
    
    if (`$ServiceName -eq "all") {
        docker-compose -f docker-compose.consolidated.yml logs -f
    } else {
        docker-compose -f docker-compose.consolidated.yml logs -f `$ServiceName
    }
}

function Build-Services {
    param([string]`$ServiceName)
    
    if (`$ServiceName -eq "all") {
        Write-Host "Building all services..." -ForegroundColor Blue
        ./gradlew build
    } else {
        Write-Host "Building `$ServiceName..." -ForegroundColor Blue
        ./gradlew ":consolidated-services:`$ServiceName:build"
    }
}

function Test-Services {
    param([string]`$ServiceName)
    
    if (`$ServiceName -eq "all") {
        Write-Host "Testing all services..." -ForegroundColor Blue
        ./gradlew test
    } else {
        Write-Host "Testing `$ServiceName..." -ForegroundColor Blue
        ./gradlew ":consolidated-services:`$ServiceName:test"
    }
}

switch (`$Action) {
    "start" { Start-Services `$Service }
    "stop" { Stop-Services `$Service }
    "restart" { 
        Stop-Services `$Service
        Start-Sleep -Seconds 2
        Start-Services `$Service
    }
    "logs" { Show-Logs `$Service }
    "build" { Build-Services `$Service }
    "test" { Test-Services `$Service }
    "clean" {
        Write-Host "Cleaning build artifacts..." -ForegroundColor Yellow
        ./gradlew clean
        docker-compose -f docker-compose.consolidated.yml down --volumes
    }
}
"@

New-FileWithContent -Path "scripts/dev-consolidated.ps1" -Content $devScript

# Summary
Write-Success "Consolidation script completed!"
Write-Info ""
Write-Info "Summary of changes:"
Write-Info "✅ Created $($ConsolidationMap.Count) consolidated services from $serviceCount original microservices"
Write-Info "✅ Created modular structure maintaining domain boundaries"
Write-Info "✅ Generated Docker configurations for containerization"
Write-Info "✅ Created database initialization scripts"
Write-Info "✅ Generated comprehensive documentation"
Write-Info "✅ Created development utilities"
Write-Info ""
Write-Info "Next steps:"
Write-Info "1. Review the generated consolidated services structure"
Write-Info "2. Read CONSOLIDATION_MIGRATION_GUIDE.md for migration details"
Write-Info "3. Test the consolidated services: ./scripts/dev-consolidated.ps1 start"
Write-Info "4. Update your CI/CD pipelines for the new structure"
Write-Info ""
Write-Warning "Note: This script has created the structure but you may need to:"
Write-Warning "- Merge duplicate dependencies in build.gradle.kts files"
Write-Warning "- Resolve any package naming conflicts"
Write-Warning "- Update API Gateway routing configuration"
Write-Warning "- Migrate existing data if databases have been created"

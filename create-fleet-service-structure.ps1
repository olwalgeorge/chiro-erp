# PowerShell script to create fleet-service structure
# This script creates all folders and files for the fleet-service without any code content

$serviceName = "fleet-service"
$basePackagePath = "org/chiro/fleet"

# Create base service directory
$serviceDir = "services/$serviceName"
New-Item -ItemType Directory -Path $serviceDir -Force

# Create main source structure
$mainSrcDir = "$serviceDir/src/main"
$mainKotlinDir = "$mainSrcDir/kotlin/$basePackagePath"
$mainResourcesDir = "$mainSrcDir/resources"
$webappDir = "$mainSrcDir/webapp"

# Create test structure
$testDir = "$serviceDir/src/test"
$testKotlinDir = "$testDir/kotlin/$basePackagePath"
$testResourcesDir = "$testDir/resources"

Write-Host "Creating directory structure for $serviceName..." -ForegroundColor Green

# Create all main directories
$directories = @(
    # Main structure
    "$mainKotlinDir/application/dto/command",
    "$mainKotlinDir/application/dto/event",
    "$mainKotlinDir/application/dto/query",
    "$mainKotlinDir/application/dto/response",
    "$mainKotlinDir/application/port/incoming",
    "$mainKotlinDir/application/port/outgoing",
    "$mainKotlinDir/application/service",
    
    # Domain structure
    "$mainKotlinDir/domain/aggregate",
    "$mainKotlinDir/domain/entity",
    "$mainKotlinDir/domain/event",
    "$mainKotlinDir/domain/exception",
    "$mainKotlinDir/domain/repository",
    "$mainKotlinDir/domain/service",
    "$mainKotlinDir/domain/valueobject",
    
    # Infrastructure structure
    "$mainKotlinDir/infrastructure/adapter/incoming",
    "$mainKotlinDir/infrastructure/adapter/outgoing",
    "$mainKotlinDir/infrastructure/configuration",
    "$mainKotlinDir/infrastructure/exception",
    "$mainKotlinDir/infrastructure/util",
    
    # Resources structure
    "$mainResourcesDir/db/migration",
    "$mainResourcesDir/META-INF/resources",
    
    # Webapp
    "$webappDir",
    
    # Test structure
    "$testKotlinDir/application/dto",
    "$testKotlinDir/application/port",
    "$testKotlinDir/application/service",
    "$testKotlinDir/domain/aggregate",
    "$testKotlinDir/domain/entity",
    "$testKotlinDir/domain/event",
    "$testKotlinDir/domain/service",
    "$testKotlinDir/domain/valueobject",
    "$testKotlinDir/infrastructure/adapter",
    "$testKotlinDir/infrastructure/configuration",
    "$testKotlinDir/infrastructure/util",
    "$testResourcesDir"
)

# Create all directories
foreach ($dir in $directories) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
}

Write-Host "Creating application DTO command files..." -ForegroundColor Yellow

# Create Application DTO Command files
$commandFiles = @(
    "AssignDriverToVehicleCommand.kt",
    "CreateVehicleCommand.kt",
    "LogFuelConsumptionCommand.kt",
    "RecordMaintenanceCommand.kt",
    "ScheduleMaintenanceCommand.kt",
    "UpdateVehicleStatusCommand.kt",
    "DispatchVehicleCommand.kt"
)

foreach ($file in $commandFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/command/$file" -Force | Out-Null
}

Write-Host "Creating application DTO event files..." -ForegroundColor Yellow

# Create Application DTO Event files
$eventFiles = @(
    "HRDriverLicensedEventDTO.kt",
    "InventoryPartsConsumedEventDTO.kt",
    "SalesOrderShippedEventDTO.kt",
    "FieldServiceWorkOrderAssignedEventDTO.kt"
)

foreach ($file in $eventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/event/$file" -Force | Out-Null
}

Write-Host "Creating application DTO query files..." -ForegroundColor Yellow

# Create Application DTO Query files
$queryFiles = @(
    "VehicleDetailsDTO.kt",
    "VehicleMaintenanceHistoryDTO.kt",
    "VehicleUtilizationReportDTO.kt",
    "FuelConsumptionReportDTO.kt",
    "DriverAssignmentStatusDTO.kt"
)

foreach ($file in $queryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/query/$file" -Force | Out-Null
}

Write-Host "Creating application DTO response files..." -ForegroundColor Yellow

# Create Application DTO Response files
$responseFiles = @(
    "RouteOptimizationResponse.kt"
)

foreach ($file in $responseFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/response/$file" -Force | Out-Null
}

Write-Host "Creating application port files..." -ForegroundColor Yellow

# Create Application Port Incoming files
$incomingPortFiles = @(
    "FleetCommandPort.kt",
    "FleetQueryPort.kt",
    "HREventConsumerPort.kt",
    "InventoryEventConsumerPort.kt",
    "SalesEventConsumerPort.kt",
    "FieldServiceEventConsumerPort.kt"
)

foreach ($file in $incomingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/incoming/$file" -Force | Out-Null
}

# Create Application Port Outgoing files
$outgoingPortFiles = @(
    "FinanceEventPublisherPort.kt",
    "FleetEventPublisherPort.kt",
    "InventoryEventPublisherPort.kt",
    "HREventPublisherPort.kt",
    "TelematicsIntegrationPort.kt"
)

foreach ($file in $outgoingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating application service files..." -ForegroundColor Yellow

# Create Application Service files
$applicationServiceFiles = @(
    "VehicleManagementApplicationService.kt",
    "MaintenanceSchedulingApplicationService.kt",
    "DispatchAndRoutingApplicationService.kt",
    "FuelManagementApplicationService.kt",
    "DriverAssignmentApplicationService.kt"
)

foreach ($file in $applicationServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/service/$file" -Force | Out-Null
}

Write-Host "Creating domain aggregate files..." -ForegroundColor Yellow

# Create Domain Aggregate files
$aggregateFiles = @(
    "Vehicle.kt",
    "MaintenanceSchedule.kt",
    "Trip.kt"
)

foreach ($file in $aggregateFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/aggregate/$file" -Force | Out-Null
}

Write-Host "Creating domain entity files..." -ForegroundColor Yellow

# Create Domain Entity files
$entityFiles = @(
    "FuelLogEntry.kt",
    "MaintenanceRecord.kt",
    "VehicleLicense.kt",
    "DriverAssignment.kt",
    "RouteStop.kt"
)

foreach ($file in $entityFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/entity/$file" -Force | Out-Null
}

Write-Host "Creating domain event files..." -ForegroundColor Yellow

# Create Domain Event files
$domainEventFiles = @(
    "VehicleCreatedEvent.kt",
    "VehicleStatusUpdatedEvent.kt",
    "MaintenanceScheduledEvent.kt",
    "MaintenanceCompletedEvent.kt",
    "FuelLoggedEvent.kt",
    "VehicleDispatchedEvent.kt",
    "DriverAssignedToVehicleEvent.kt"
)

foreach ($file in $domainEventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/event/$file" -Force | Out-Null
}

Write-Host "Creating domain exception files..." -ForegroundColor Yellow

# Create Domain Exception files
$exceptionFiles = @(
    "VehicleNotFoundException.kt",
    "DriverNotAvailableException.kt",
    "InvalidVehicleStatusChangeException.kt",
    "MaintenanceRecordNotFoundException.kt"
)

foreach ($file in $exceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/exception/$file" -Force | Out-Null
}

Write-Host "Creating domain repository files..." -ForegroundColor Yellow

# Create Domain Repository files
$repositoryFiles = @(
    "VehicleRepository.kt",
    "MaintenanceScheduleRepository.kt",
    "TripRepository.kt"
)

foreach ($file in $repositoryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/repository/$file" -Force | Out-Null
}

Write-Host "Creating domain service files..." -ForegroundColor Yellow

# Create Domain Service files
$domainServiceFiles = @(
    "MaintenancePredictionService.kt",
    "RouteOptimizationService.kt",
    "VehicleAvailabilityService.kt"
)

foreach ($file in $domainServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/service/$file" -Force | Out-Null
}

Write-Host "Creating domain value object files..." -ForegroundColor Yellow

# Create Domain Value Object files
$valueObjectFiles = @(
    "DriverId.kt",
    "FinancialAmount.kt",
    "FuelType.kt",
    "Mileage.kt",
    "VehicleId.kt",
    "VehicleStatus.kt",
    "LicensePlate.kt",
    "LocationData.kt",
    "MaintenanceType.kt"
)

foreach ($file in $valueObjectFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/valueobject/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter incoming files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Incoming files
$incomingAdapterFiles = @(
    "FleetGraphQLResource.kt",
    "FleetResource.kt",
    "HRKafkaConsumer.kt",
    "InventoryKafkaConsumer.kt",
    "SalesKafkaConsumer.kt",
    "FieldServiceKafkaConsumer.kt"
)

foreach ($file in $incomingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/incoming/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter outgoing files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Outgoing files
$outgoingAdapterFiles = @(
    "JpaVehicleRepository.kt",
    "JpaMaintenanceScheduleRepository.kt",
    "JpaTripRepository.kt",
    "KafkaFinanceEventPublisher.kt",
    "KafkaFleetEventPublisher.kt",
    "KafkaInventoryEventPublisher.kt",
    "KafkaHREventPublisher.kt",
    "TelematicsIntegrationClient.kt"
)

foreach ($file in $outgoingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure configuration files..." -ForegroundColor Yellow

# Create Infrastructure Configuration files
$configFiles = @(
    "KafkaConfig.kt",
    "ObjectMapperConfig.kt",
    "SecurityConfig.kt",
    "TelematicsIntegrationConfig.kt",
    "TenantSchemaResolver.kt"
)

foreach ($file in $configFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/configuration/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure exception files..." -ForegroundColor Yellow

# Create Infrastructure Exception files
$infraExceptionFiles = @(
    "FleetExceptionMapper.kt"
)

foreach ($file in $infraExceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/exception/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure util files..." -ForegroundColor Yellow

# Create Infrastructure Util files
$utilFiles = @(
    "FleetMapper.kt"
)

foreach ($file in $utilFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/util/$file" -Force | Out-Null
}

Write-Host "Creating resource files..." -ForegroundColor Yellow

# Create Resource files
New-Item -ItemType File -Path "$mainResourcesDir/application.properties" -Force | Out-Null
New-Item -ItemType File -Path "$webappDir/index.html" -Force | Out-Null

Write-Host "Creating build and documentation files..." -ForegroundColor Yellow

# Create build and documentation files
New-Item -ItemType File -Path "$serviceDir/build.gradle.kts" -Force | Out-Null
New-Item -ItemType File -Path "$serviceDir/README.md" -Force | Out-Null

Write-Host "Creating test files..." -ForegroundColor Yellow

# Create placeholder test files for main directories
New-Item -ItemType File -Path "$testKotlinDir/application/service/FleetApplicationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/VehicleTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/MaintenanceScheduleTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/service/RouteOptimizationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/adapter/FleetResourceTest.kt" -Force | Out-Null

Write-Host ""
Write-Host "✅ Successfully created fleet-service structure!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Main source files: $(((Get-ChildItem -Path "$serviceDir/src/main" -Recurse -File).Count)) files"
Write-Host "   • Test files: $(((Get-ChildItem -Path "$serviceDir/src/test" -Recurse -File).Count)) files"
Write-Host "   • Total directories: $(((Get-ChildItem -Path $serviceDir -Recurse -Directory).Count)) directories"
Write-Host "   • Service location: $serviceDir"
Write-Host ""
Write-Host "🎯 Next steps:"
Write-Host "   1. Update build.gradle.kts with fleet-specific dependencies (Spring Boot, JPA, Kafka, etc.)"
Write-Host "   2. Configure application.properties with database and Kafka settings"
Write-Host "   3. Add database migration scripts for Vehicle, MaintenanceSchedule, Trip, etc."
Write-Host "   4. Implement the Kotlin classes with proper fleet domain logic"
Write-Host "   5. Configure telematics integration for real-time vehicle data"

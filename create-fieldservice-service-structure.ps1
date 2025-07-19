# PowerShell script to create fieldservice-service structure
# This script creates all folders and files for the fieldservice-service without any code content

$serviceName = "fieldservice-service"
$basePackagePath = "org/chiro/fieldservice"

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
    "$mainResourcesDir/data",
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
    "AssignTechnicianToWorkOrderCommand.kt",
    "CreateServiceRequestCommand.kt",
    "CreateWorkOrderCommand.kt",
    "LogFieldServiceTimeEntryCommand.kt",
    "RecordWorkOrderCompletionCommand.kt",
    "UpdateServiceRequestStatusCommand.kt",
    "ConsumePartsForWorkOrderCommand.kt",
    "CreateMaintenanceContractCommand.kt"
)

foreach ($file in $commandFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/command/$file" -Force | Out-Null
}

Write-Host "Creating application DTO event files..." -ForegroundColor Yellow

# Create Application DTO Event files
$eventFiles = @(
    "CRMCustomerServiceRequestCreatedEventDTO.kt",
    "ProductServiceableItemEventDTO.kt",
    "InventoryPartsAvailableEventDTO.kt",
    "HREmployeeAvailabilityEventDTO.kt"
)

foreach ($file in $eventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/event/$file" -Force | Out-Null
}

Write-Host "Creating application DTO query files..." -ForegroundColor Yellow

# Create Application DTO Query files
$queryFiles = @(
    "ServiceRequestDetailsDTO.kt",
    "WorkOrderDetailsDTO.kt",
    "TechnicianScheduleDTO.kt",
    "MaintenanceContractDetailsDTO.kt",
    "FieldServiceReportDTO.kt"
)

foreach ($file in $queryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/query/$file" -Force | Out-Null
}

Write-Host "Creating application DTO response files..." -ForegroundColor Yellow

# Create Application DTO Response files
$responseFiles = @(
    "WorkOrderCreationResponse.kt",
    "ServiceRequestAssignmentResponse.kt"
)

foreach ($file in $responseFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/response/$file" -Force | Out-Null
}

Write-Host "Creating application port files..." -ForegroundColor Yellow

# Create Application Port Incoming files
$incomingPortFiles = @(
    "FieldServiceCommandPort.kt",
    "FieldServiceQueryPort.kt",
    "CRMEventConsumerPort.kt",
    "ProductEventConsumerPort.kt",
    "InventoryEventConsumerPort.kt",
    "HREventConsumerPort.kt"
)

foreach ($file in $incomingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/incoming/$file" -Force | Out-Null
}

# Create Application Port Outgoing files
$outgoingPortFiles = @(
    "FinanceEventPublisherPort.kt",
    "InventoryEventPublisherPort.kt",
    "FieldServiceEventPublisherPort.kt",
    "BillingServiceIntegrationPort.kt",
    "CRMSyncIntegrationPort.kt",
    "NotificationServiceIntegrationPort.kt"
)

foreach ($file in $outgoingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating application service files..." -ForegroundColor Yellow

# Create Application Service files
$applicationServiceFiles = @(
    "ServiceRequestApplicationService.kt",
    "WorkOrderApplicationService.kt",
    "TechnicianSchedulingApplicationService.kt",
    "FieldServiceTimeTrackingApplicationService.kt",
    "PartsManagementApplicationService.kt",
    "MaintenanceContractApplicationService.kt"
)

foreach ($file in $applicationServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/service/$file" -Force | Out-Null
}

Write-Host "Creating domain aggregate files..." -ForegroundColor Yellow

# Create Domain Aggregate files
$aggregateFiles = @(
    "ServiceRequest.kt",
    "WorkOrder.kt",
    "MaintenanceContract.kt"
)

foreach ($file in $aggregateFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/aggregate/$file" -Force | Out-Null
}

Write-Host "Creating domain entity files..." -ForegroundColor Yellow

# Create Domain Entity files
$entityFiles = @(
    "WorkOrderTask.kt",
    "WorkOrderPart.kt",
    "TimeEntry.kt",
    "ServiceReport.kt",
    "ContractServiceItem.kt",
    "DispatchRoute.kt"
)

foreach ($file in $entityFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/entity/$file" -Force | Out-Null
}

Write-Host "Creating domain event files..." -ForegroundColor Yellow

# Create Domain Event files
$domainEventFiles = @(
    "ServiceRequestCreatedEvent.kt",
    "WorkOrderCreatedEvent.kt",
    "WorkOrderCompletedEvent.kt",
    "TechnicianAssignedEvent.kt",
    "PartsConsumedForWorkOrderEvent.kt",
    "ServiceRequestStatusUpdatedEvent.kt",
    "MaintenanceContractCreatedEvent.kt"
)

foreach ($file in $domainEventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/event/$file" -Force | Out-Null
}

Write-Host "Creating domain exception files..." -ForegroundColor Yellow

# Create Domain Exception files
$exceptionFiles = @(
    "ServiceRequestNotFoundException.kt",
    "WorkOrderNotFoundException.kt",
    "TechnicianNotAvailableException.kt",
    "InsufficientPartsException.kt",
    "InvalidWorkOrderStatusException.kt"
)

foreach ($file in $exceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/exception/$file" -Force | Out-Null
}

Write-Host "Creating domain repository files..." -ForegroundColor Yellow

# Create Domain Repository files
$repositoryFiles = @(
    "ServiceRequestRepository.kt",
    "WorkOrderRepository.kt",
    "MaintenanceContractRepository.kt"
)

foreach ($file in $repositoryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/repository/$file" -Force | Out-Null
}

Write-Host "Creating domain service files..." -ForegroundColor Yellow

# Create Domain Service files
$domainServiceFiles = @(
    "TechnicianAssignmentService.kt",
    "WorkOrderCompletionService.kt",
    "FieldServicePricingService.kt"
)

foreach ($file in $domainServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/service/$file" -Force | Out-Null
}

Write-Host "Creating domain value object files..." -ForegroundColor Yellow

# Create Domain Value Object files
$valueObjectFiles = @(
    "CustomerId.kt",
    "EmployeeId.kt",
    "FinancialAmount.kt",
    "ProductId.kt",
    "ServiceRequestId.kt",
    "ServiceRequestStatus.kt",
    "WorkOrderId.kt",
    "WorkOrderStatus.kt",
    "TimeDuration.kt",
    "ServiceLocation.kt",
    "ContractTerm.kt"
)

foreach ($file in $valueObjectFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/valueobject/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter incoming files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Incoming files
$incomingAdapterFiles = @(
    "FieldServiceGraphQLResource.kt",
    "FieldServiceResource.kt",
    "CRMKafkaConsumer.kt",
    "ProductKafkaConsumer.kt",
    "InventoryKafkaConsumer.kt",
    "HRKafkaConsumer.kt"
)

foreach ($file in $incomingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/incoming/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter outgoing files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Outgoing files
$outgoingAdapterFiles = @(
    "JpaServiceRequestRepository.kt",
    "JpaWorkOrderRepository.kt",
    "JpaMaintenanceContractRepository.kt",
    "KafkaFinanceEventPublisher.kt",
    "KafkaInventoryEventPublisher.kt",
    "KafkaFieldServiceEventPublisher.kt",
    "BillingServiceClient.kt",
    "CRMSyncServiceClient.kt",
    "NotificationServiceClient.kt"
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
    "TenantSchemaResolver.kt"
)

foreach ($file in $configFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/configuration/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure exception files..." -ForegroundColor Yellow

# Create Infrastructure Exception files
$infraExceptionFiles = @(
    "FieldServiceExceptionMapper.kt"
)

foreach ($file in $infraExceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/exception/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure util files..." -ForegroundColor Yellow

# Create Infrastructure Util files
$utilFiles = @(
    "FieldServiceMapper.kt"
)

foreach ($file in $utilFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/util/$file" -Force | Out-Null
}

Write-Host "Creating resource files..." -ForegroundColor Yellow

# Create Resource files
New-Item -ItemType File -Path "$mainResourcesDir/application.properties" -Force | Out-Null
New-Item -ItemType File -Path "$mainResourcesDir/data/default-service-templates.json" -Force | Out-Null
New-Item -ItemType File -Path "$webappDir/index.html" -Force | Out-Null

Write-Host "Creating build and documentation files..." -ForegroundColor Yellow

# Create build and documentation files
New-Item -ItemType File -Path "$serviceDir/build.gradle.kts" -Force | Out-Null
New-Item -ItemType File -Path "$serviceDir/README.md" -Force | Out-Null

Write-Host "Creating test files..." -ForegroundColor Yellow

# Create placeholder test files for main directories
New-Item -ItemType File -Path "$testKotlinDir/application/service/FieldServiceApplicationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/ServiceRequestTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/WorkOrderTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/MaintenanceContractTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/service/TechnicianAssignmentServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/service/FieldServicePricingServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/adapter/FieldServiceResourceTest.kt" -Force | Out-Null

Write-Host ""
Write-Host "✅ Successfully created fieldservice-service structure!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Main source files: $(((Get-ChildItem -Path "$serviceDir/src/main" -Recurse -File).Count)) files"
Write-Host "   • Test files: $(((Get-ChildItem -Path "$serviceDir/src/test" -Recurse -File).Count)) files"
Write-Host "   • Total directories: $(((Get-ChildItem -Path $serviceDir -Recurse -Directory).Count)) directories"
Write-Host "   • Service location: $serviceDir"
Write-Host ""
Write-Host "🎯 Next steps:"
Write-Host "   1. Update build.gradle.kts with field service dependencies (Spring Boot, JPA, Kafka, etc.)"
Write-Host "   2. Configure application.properties with database and Kafka settings"
Write-Host "   3. Add database migration scripts for ServiceRequest, WorkOrder, MaintenanceContract, etc."
Write-Host "   4. Implement the Kotlin classes with proper field service domain logic"
Write-Host "   5. Set up default-service-templates.json seed data for common service types"
Write-Host "   6. Configure technician routing and scheduling algorithms"

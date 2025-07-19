# PowerShell script to create project-service structure
# This script creates all folders and files for the project-service without any code content

$serviceName = "project-service"
$basePackagePath = "org/chiro/project"

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
    "AssignResourceToTaskCommand.kt",
    "CreateProjectCommand.kt",
    "CreateTaskCommand.kt",
    "LogTimeEntryCommand.kt",
    "UpdateProjectStatusCommand.kt",
    "RecordProjectExpenseCommand.kt"
)

foreach ($file in $commandFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/command/$file" -Force | Out-Null
}

Write-Host "Creating application DTO event files..." -ForegroundColor Yellow

# Create Application DTO Event files
$eventFiles = @(
    "SalesOpportunityWonEventDTO.kt",
    "HREmployeeAvailabilityEventDTO.kt",
    "ProcurementPurchaseOrderCreatedEventDTO.kt",
    "FinanceExpenseRecordedEventDTO.kt"
)

foreach ($file in $eventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/event/$file" -Force | Out-Null
}

Write-Host "Creating application DTO query files..." -ForegroundColor Yellow

# Create Application DTO Query files
$queryFiles = @(
    "ProjectDetailsDTO.kt",
    "ProjectGanttChartDTO.kt",
    "ProjectCostReportDTO.kt",
    "ResourceUtilizationReportDTO.kt",
    "TaskDetailsDTO.kt"
)

foreach ($file in $queryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/query/$file" -Force | Out-Null
}

Write-Host "Creating application DTO response files..." -ForegroundColor Yellow

# Create Application DTO Response files
$responseFiles = @(
    "ProjectCreationResponse.kt"
)

foreach ($file in $responseFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/response/$file" -Force | Out-Null
}

Write-Host "Creating application port files..." -ForegroundColor Yellow

# Create Application Port Incoming files
$incomingPortFiles = @(
    "ProjectCommandPort.kt",
    "ProjectQueryPort.kt",
    "CRMLifecycleEventConsumerPort.kt",
    "HREventConsumerPort.kt",
    "ProcurementEventConsumerPort.kt"
)

foreach ($file in $incomingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/incoming/$file" -Force | Out-Null
}

# Create Application Port Outgoing files
$outgoingPortFiles = @(
    "FinanceEventPublisherPort.kt",
    "ProjectEventPublisherPort.kt",
    "HREventPublisherPort.kt",
    "BillingServiceIntegrationPort.kt",
    "NotificationServiceIntegrationPort.kt"
)

foreach ($file in $outgoingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating application service files..." -ForegroundColor Yellow

# Create Application Service files
$applicationServiceFiles = @(
    "ProjectManagementApplicationService.kt",
    "TaskManagementApplicationService.kt",
    "ResourceAllocationApplicationService.kt",
    "TimeTrackingApplicationService.kt",
    "ProjectCostingApplicationService.kt",
    "ProjectBillingApplicationService.kt"
)

foreach ($file in $applicationServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/service/$file" -Force | Out-Null
}

Write-Host "Creating domain aggregate files..." -ForegroundColor Yellow

# Create Domain Aggregate files
$aggregateFiles = @(
    "Project.kt",
    "TimeSheet.kt"
)

foreach ($file in $aggregateFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/aggregate/$file" -Force | Out-Null
}

Write-Host "Creating domain entity files..." -ForegroundColor Yellow

# Create Domain Entity files
$entityFiles = @(
    "Task.kt",
    "ProjectResource.kt",
    "TimeEntry.kt",
    "ProjectExpense.kt",
    "BillingMilestone.kt"
)

foreach ($file in $entityFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/entity/$file" -Force | Out-Null
}

Write-Host "Creating domain event files..." -ForegroundColor Yellow

# Create Domain Event files
$domainEventFiles = @(
    "ProjectCreatedEvent.kt",
    "ProjectStatusUpdatedEvent.kt",
    "TaskCompletedEvent.kt",
    "ResourceAssignedToProjectEvent.kt",
    "TimeEntryLoggedEvent.kt",
    "ProjectExpenseRecordedEvent.kt",
    "BillingMilestoneAchievedEvent.kt"
)

foreach ($file in $domainEventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/event/$file" -Force | Out-Null
}

Write-Host "Creating domain exception files..." -ForegroundColor Yellow

# Create Domain Exception files
$exceptionFiles = @(
    "ProjectNotFoundException.kt",
    "TaskNotFoundException.kt",
    "ResourceNotAvailableException.kt",
    "InvalidProjectStatusException.kt",
    "TimeEntryValidationException.kt"
)

foreach ($file in $exceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/exception/$file" -Force | Out-Null
}

Write-Host "Creating domain repository files..." -ForegroundColor Yellow

# Create Domain Repository files
$repositoryFiles = @(
    "ProjectRepository.kt",
    "TimeSheetRepository.kt"
)

foreach ($file in $repositoryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/repository/$file" -Force | Out-Null
}

Write-Host "Creating domain service files..." -ForegroundColor Yellow

# Create Domain Service files
$domainServiceFiles = @(
    "ResourceAvailabilityService.kt",
    "ProjectSchedulingService.kt",
    "ProjectCostCalculationService.kt"
)

foreach ($file in $domainServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/service/$file" -Force | Out-Null
}

Write-Host "Creating domain value object files..." -ForegroundColor Yellow

# Create Domain Value Object files
$valueObjectFiles = @(
    "EmployeeId.kt",
    "FinancialAmount.kt",
    "ProjectId.kt",
    "ProjectStatus.kt",
    "TaskId.kt",
    "TaskStatus.kt",
    "TimeDuration.kt",
    "WorklogId.kt",
    "BillingMethod.kt"
)

foreach ($file in $valueObjectFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/valueobject/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter incoming files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Incoming files
$incomingAdapterFiles = @(
    "ProjectGraphQLResource.kt",
    "ProjectResource.kt",
    "CRMKafkaConsumer.kt",
    "HRKafkaConsumer.kt",
    "ProcurementKafkaConsumer.kt"
)

foreach ($file in $incomingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/incoming/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter outgoing files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Outgoing files
$outgoingAdapterFiles = @(
    "JpaProjectRepository.kt",
    "JpaTimeSheetRepository.kt",
    "KafkaFinanceEventPublisher.kt",
    "KafkaProjectEventPublisher.kt",
    "KafkaHREventPublisher.kt",
    "BillingServiceClient.kt",
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
    "ProjectExceptionMapper.kt"
)

foreach ($file in $infraExceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/exception/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure util files..." -ForegroundColor Yellow

# Create Infrastructure Util files
$utilFiles = @(
    "ProjectMapper.kt"
)

foreach ($file in $utilFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/util/$file" -Force | Out-Null
}

Write-Host "Creating resource files..." -ForegroundColor Yellow

# Create Resource files
New-Item -ItemType File -Path "$mainResourcesDir/application.properties" -Force | Out-Null
New-Item -ItemType File -Path "$mainResourcesDir/data/default-project-templates.json" -Force | Out-Null
New-Item -ItemType File -Path "$webappDir/index.html" -Force | Out-Null

Write-Host "Creating build and documentation files..." -ForegroundColor Yellow

# Create build and documentation files
New-Item -ItemType File -Path "$serviceDir/build.gradle.kts" -Force | Out-Null
New-Item -ItemType File -Path "$serviceDir/README.md" -Force | Out-Null

Write-Host "Creating test files..." -ForegroundColor Yellow

# Create placeholder test files for main directories
New-Item -ItemType File -Path "$testKotlinDir/application/service/ProjectApplicationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/ProjectTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/TimeSheetTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/service/ProjectSchedulingServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/adapter/ProjectResourceTest.kt" -Force | Out-Null

Write-Host ""
Write-Host "✅ Successfully created project-service structure!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Main source files: $(((Get-ChildItem -Path "$serviceDir/src/main" -Recurse -File).Count)) files"
Write-Host "   • Test files: $(((Get-ChildItem -Path "$serviceDir/src/test" -Recurse -File).Count)) files"
Write-Host "   • Total directories: $(((Get-ChildItem -Path $serviceDir -Recurse -Directory).Count)) directories"
Write-Host "   • Service location: $serviceDir"
Write-Host ""
Write-Host "🎯 Next steps:"
Write-Host "   1. Update build.gradle.kts with project-specific dependencies (Spring Boot, JPA, Kafka, etc.)"
Write-Host "   2. Configure application.properties with database and Kafka settings"
Write-Host "   3. Add database migration scripts for Project, Task, TimeSheet, etc."
Write-Host "   4. Implement the Kotlin classes with proper project domain logic"
Write-Host "   5. Set up default-project-templates.json seed data"

# PowerShell script to create billing-service structure
# This script creates all folders and files for the billing-service without any code content

$serviceName = "billing-service"
$basePackagePath = "org/chiro/billing"

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
    "CreateSubscriptionCommand.kt",
    "UpdateSubscriptionCommand.kt",
    "ProcessBillingCycleCommand.kt",
    "RecordPaymentCommand.kt",
    "GenerateAdHocInvoiceCommand.kt",
    "ApplyCreditMemoCommand.kt"
)

foreach ($file in $commandFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/command/$file" -Force | Out-Null
}

Write-Host "Creating application DTO event files..." -ForegroundColor Yellow

# Create Application DTO Event files
$eventFiles = @(
    "SalesNewSubscriptionEventDTO.kt",
    "ProjectBillingMilestoneAchievedEventDTO.kt",
    "FieldServiceWorkOrderCompletedEventDTO.kt",
    "RepairOrderCompletedEventDTO.kt",
    "FinancePaymentFailedEventDTO.kt",
    "TenantSubscriptionUpdateEventDTO.kt"
)

foreach ($file in $eventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/event/$file" -Force | Out-Null
}

Write-Host "Creating application DTO query files..." -ForegroundColor Yellow

# Create Application DTO Query files
$queryFiles = @(
    "InvoiceDetailsDTO.kt",
    "SubscriptionDetailsDTO.kt",
    "CustomerBillingHistoryDTO.kt",
    "DunningProcessStatusDTO.kt",
    "RevenueRecognitionScheduleDTO.kt"
)

foreach ($file in $queryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/query/$file" -Force | Out-Null
}

Write-Host "Creating application DTO response files..." -ForegroundColor Yellow

# Create Application DTO Response files
$responseFiles = @(
    "InvoiceGenerationResponse.kt",
    "PaymentProcessingResponse.kt"
)

foreach ($file in $responseFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/response/$file" -Force | Out-Null
}

Write-Host "Creating application port files..." -ForegroundColor Yellow

# Create Application Port Incoming files
$incomingPortFiles = @(
    "BillingCommandPort.kt",
    "BillingQueryPort.kt",
    "SalesEventConsumerPort.kt",
    "ProjectEventConsumerPort.kt",
    "FieldServiceEventConsumerPort.kt",
    "RepairEventConsumerPort.kt",
    "FinanceEventConsumerPort.kt",
    "TenantEventConsumerPort.kt"
)

foreach ($file in $incomingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/incoming/$file" -Force | Out-Null
}

# Create Application Port Outgoing files
$outgoingPortFiles = @(
    "FinanceEventPublisherPort.kt",
    "BillingEventPublisherPort.kt",
    "TenantManagementIntegrationPort.kt",
    "NotificationServiceIntegrationPort.kt",
    "PaymentGatewayIntegrationPort.kt"
)

foreach ($file in $outgoingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating application service files..." -ForegroundColor Yellow

# Create Application Service files
$applicationServiceFiles = @(
    "SubscriptionManagementApplicationService.kt",
    "InvoiceGenerationApplicationService.kt",
    "PaymentProcessingApplicationService.kt",
    "DunningManagementApplicationService.kt",
    "RevenueRecognitionApplicationService.kt"
)

foreach ($file in $applicationServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/service/$file" -Force | Out-Null
}

Write-Host "Creating domain aggregate files..." -ForegroundColor Yellow

# Create Domain Aggregate files
$aggregateFiles = @(
    "Subscription.kt",
    "Invoice.kt",
    "DunningCase.kt"
)

foreach ($file in $aggregateFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/aggregate/$file" -Force | Out-Null
}

Write-Host "Creating domain entity files..." -ForegroundColor Yellow

# Create Domain Entity files
$entityFiles = @(
    "SubscriptionPlan.kt",
    "InvoiceLineItem.kt",
    "PaymentRecord.kt",
    "CreditMemo.kt",
    "BillingCycle.kt",
    "RevenueScheduleEntry.kt"
)

foreach ($file in $entityFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/entity/$file" -Force | Out-Null
}

Write-Host "Creating domain event files..." -ForegroundColor Yellow

# Create Domain Event files
$domainEventFiles = @(
    "SubscriptionCreatedEvent.kt",
    "SubscriptionUpdatedEvent.kt",
    "InvoiceIssuedEvent.kt",
    "PaymentReceivedEvent.kt",
    "InvoiceOverdueEvent.kt",
    "DunningActionTakenEvent.kt",
    "RevenueRecognizedEvent.kt"
)

foreach ($file in $domainEventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/event/$file" -Force | Out-Null
}

Write-Host "Creating domain exception files..." -ForegroundColor Yellow

# Create Domain Exception files
$exceptionFiles = @(
    "SubscriptionNotFoundException.kt",
    "InvoiceNotFoundException.kt",
    "InvalidPaymentException.kt",
    "DunningProcessException.kt",
    "BillingCycleException.kt"
)

foreach ($file in $exceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/exception/$file" -Force | Out-Null
}

Write-Host "Creating domain repository files..." -ForegroundColor Yellow

# Create Domain Repository files
$repositoryFiles = @(
    "SubscriptionRepository.kt",
    "InvoiceRepository.kt",
    "DunningCaseRepository.kt"
)

foreach ($file in $repositoryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/repository/$file" -Force | Out-Null
}

Write-Host "Creating domain service files..." -ForegroundColor Yellow

# Create Domain Service files
$domainServiceFiles = @(
    "PricingCalculationService.kt",
    "TaxCalculationService.kt",
    "PaymentReconciliationService.kt",
    "DunningPolicyService.kt"
)

foreach ($file in $domainServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/service/$file" -Force | Out-Null
}

Write-Host "Creating domain value object files..." -ForegroundColor Yellow

# Create Domain Value Object files
$valueObjectFiles = @(
    "CustomerId.kt",
    "FinancialAmount.kt",
    "InvoiceId.kt",
    "InvoiceStatus.kt",
    "PaymentMethod.kt",
    "SubscriptionId.kt",
    "SubscriptionStatus.kt",
    "BillingPeriod.kt",
    "Currency.kt",
    "CreditMemoId.kt"
)

foreach ($file in $valueObjectFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/valueobject/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter incoming files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Incoming files
$incomingAdapterFiles = @(
    "BillingGraphQLResource.kt",
    "BillingResource.kt",
    "SalesKafkaConsumer.kt",
    "ProjectKafkaConsumer.kt",
    "FieldServiceKafkaConsumer.kt",
    "RepairKafkaConsumer.kt",
    "FinanceKafkaConsumer.kt",
    "TenantKafkaConsumer.kt"
)

foreach ($file in $incomingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/incoming/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter outgoing files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Outgoing files
$outgoingAdapterFiles = @(
    "JpaSubscriptionRepository.kt",
    "JpaInvoiceRepository.kt",
    "JpaDunningCaseRepository.kt",
    "KafkaFinanceEventPublisher.kt",
    "KafkaBillingEventPublisher.kt",
    "TenantManagementServiceClient.kt",
    "NotificationServiceClient.kt",
    "StripePaymentGatewayClient.kt"
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
    "PaymentGatewayConfig.kt",
    "TenantSchemaResolver.kt"
)

foreach ($file in $configFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/configuration/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure exception files..." -ForegroundColor Yellow

# Create Infrastructure Exception files
$infraExceptionFiles = @(
    "BillingExceptionMapper.kt"
)

foreach ($file in $infraExceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/exception/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure util files..." -ForegroundColor Yellow

# Create Infrastructure Util files
$utilFiles = @(
    "BillingMapper.kt"
)

foreach ($file in $utilFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/util/$file" -Force | Out-Null
}

Write-Host "Creating resource files..." -ForegroundColor Yellow

# Create Resource files
New-Item -ItemType File -Path "$mainResourcesDir/application.properties" -Force | Out-Null
New-Item -ItemType File -Path "$mainResourcesDir/data/default-subscription-plans.json" -Force | Out-Null
New-Item -ItemType File -Path "$webappDir/index.html" -Force | Out-Null

Write-Host "Creating build and documentation files..." -ForegroundColor Yellow

# Create build and documentation files
New-Item -ItemType File -Path "$serviceDir/build.gradle.kts" -Force | Out-Null
New-Item -ItemType File -Path "$serviceDir/README.md" -Force | Out-Null

Write-Host "Creating test files..." -ForegroundColor Yellow

# Create placeholder test files for main directories
New-Item -ItemType File -Path "$testKotlinDir/application/service/BillingApplicationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/SubscriptionTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/InvoiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/DunningCaseTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/service/PricingCalculationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/service/TaxCalculationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/service/PaymentReconciliationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/adapter/BillingResourceTest.kt" -Force | Out-Null

Write-Host ""
Write-Host "✅ Successfully created billing-service structure!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Main source files: $(((Get-ChildItem -Path "$serviceDir/src/main" -Recurse -File).Count)) files"
Write-Host "   • Test files: $(((Get-ChildItem -Path "$serviceDir/src/test" -Recurse -File).Count)) files"
Write-Host "   • Total directories: $(((Get-ChildItem -Path $serviceDir -Recurse -Directory).Count)) directories"
Write-Host "   • Service location: $serviceDir"
Write-Host ""
Write-Host "🎯 Next steps:"
Write-Host "   1. Update build.gradle.kts with billing service dependencies (Spring Boot, JPA, Kafka, payment gateways, etc.)"
Write-Host "   2. Configure application.properties with database, Kafka, and payment gateway settings"
Write-Host "   3. Add database migration scripts for Subscription, Invoice, DunningCase, etc."
Write-Host "   4. Implement the Kotlin classes with proper billing domain logic"
Write-Host "   5. Set up default-subscription-plans.json seed data for standard billing plans"
Write-Host "   6. Configure PaymentGatewayConfig for Stripe/PayPal/etc. integration"
Write-Host "   7. Set up tax calculation service integration for compliance"

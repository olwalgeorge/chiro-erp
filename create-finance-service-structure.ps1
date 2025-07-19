# PowerShell script to create finance-service structure
# This script creates all folders and files for the finance-service without any code content

$serviceName = "finance-service"
$basePackagePath = "org/chiro/finance"

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
    "AdjustCreditLimitCommand.kt",
    "ApplyCustomerPaymentCommand.kt",
    "CreateManualJournalEntryCommand.kt",
    "RecordVendorPaymentCommand.kt",
    "RunDunningProcessCommand.kt",
    "UpdateExchangeRateCommand.kt",
    "BulkProcessTransactionsCommand.kt",
    "CloseFiscalPeriodCommand.kt"
)

foreach ($file in $commandFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/command/$file" -Force | Out-Null
}

Write-Host "Creating application DTO event files..." -ForegroundColor Yellow

# Create Application DTO Event files
$eventFiles = @(
    "InventoryCostUpdatedEventDTO.kt",
    "ManufacturingCostCompletedEventDTO.kt",
    "SalesInvoiceCreatedEventDTO.kt",
    "ServiceCostRecordedEventDTO.kt",
    "CustomerCreatedEventDTO.kt",
    "VendorCreatedEventDTO.kt"
)

foreach ($file in $eventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/event/$file" -Force | Out-Null
}

Write-Host "Creating application DTO query files..." -ForegroundColor Yellow

# Create Application DTO Query files
$queryFiles = @(
    "AccountReceivableAgingDTO.kt",
    "CustomerCreditStatusDTO.kt",
    "ExchangeRateDTO.kt",
    "FinancialReportDTO.kt",
    "InvoiceSummaryDTO.kt",
    "LedgerDetailDTO.kt",
    "AuditLogEntryDTO.kt",
    "TrialBalanceDTO.kt"
)

foreach ($file in $queryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/query/$file" -Force | Out-Null
}

Write-Host "Creating application port files..." -ForegroundColor Yellow

# Create Application Port Incoming files
$incomingPortFiles = @(
    "BillingEventConsumerPort.kt",
    "FinanceCommandPort.kt",
    "FinanceQueryPort.kt",
    "InventoryEventConsumerPort.kt",
    "ManufacturingEventConsumerPort.kt",
    "SalesEventConsumerPort.kt",
    "ServiceEventConsumerPort.kt",
    "UserManagementEventConsumerPort.kt",
    "SystemEventConsumerPort.kt"
)

foreach ($file in $incomingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/incoming/$file" -Force | Out-Null
}

# Create Application Port Outgoing files
$outgoingPortFiles = @(
    "FinanceEventPublisherPort.kt",
    "NotificationServiceIntegrationPort.kt",
    "PaymentGatewayIntegrationPort.kt",
    "AuditLogPublisherPort.kt",
    "ReportingServiceIntegrationPort.kt"
)

foreach ($file in $outgoingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating application service files..." -ForegroundColor Yellow

# Create Application Service files
$applicationServiceFiles = @(
    "AccountsReceivableApplicationService.kt",
    "AccountsPayableApplicationService.kt",
    "CurrencyManagementApplicationService.kt",
    "DunningApplicationService.kt",
    "FinancialReportingApplicationService.kt",
    "JournalEntryApplicationService.kt",
    "CostAccountingApplicationService.kt",
    "ReconciliationApplicationService.kt",
    "FiscalPeriodApplicationService.kt"
)

foreach ($file in $applicationServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/service/$file" -Force | Out-Null
}

Write-Host "Creating domain aggregate files..." -ForegroundColor Yellow

# Create Domain Aggregate files
$aggregateFiles = @(
    "Account.kt",
    "CustomerInvoice.kt",
    "JournalEntry.kt",
    "LedgerAccountBalance.kt",
    "VendorBill.kt",
    "CreditProfile.kt",
    "FiscalPeriod.kt",
    "BankAccount.kt"
)

foreach ($file in $aggregateFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/aggregate/$file" -Force | Out-Null
}

Write-Host "Creating domain entity files..." -ForegroundColor Yellow

# Create Domain Entity files
$entityFiles = @(
    "CurrencyExchangeRate.kt",
    "InvoicePaymentApplication.kt",
    "PaymentReceipt.kt",
    "PaymentDisbursement.kt",
    "TransactionLine.kt",
    "CostComponent.kt",
    "AuditLogEntry.kt",
    "ReconciliationStatement.kt"
)

foreach ($file in $entityFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/entity/$file" -Force | Out-Null
}

Write-Host "Creating domain event files..." -ForegroundColor Yellow

# Create Domain Event files
$domainEventFiles = @(
    "AccountBalanceUpdatedEvent.kt",
    "CustomerCreditLimitAdjustedEvent.kt",
    "InvoiceCreatedEvent.kt",
    "InvoicePaymentAppliedEvent.kt",
    "JournalEntryPostedEvent.kt",
    "PeriodClosedEvent.kt",
    "UnappliedPaymentReceivedEvent.kt",
    "ExchangeRateUpdatedEvent.kt",
    "FinancialTransactionAuditedEvent.kt",
    "ReconciliationCompletedEvent.kt"
)

foreach ($file in $domainEventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/event/$file" -Force | Out-Null
}

Write-Host "Creating domain exception files..." -ForegroundColor Yellow

# Create Domain Exception files
$exceptionFiles = @(
    "CreditLimitExceededException.kt",
    "ExchangeRateNotFoundException.kt",
    "InvoiceAlreadyPaidException.kt",
    "InvalidPaymentAmountException.kt",
    "UnbalancedJournalEntryException.kt",
    "FiscalPeriodClosedException.kt",
    "AccountNotFoundException.kt",
    "CurrencyMismatchException.kt"
)

foreach ($file in $exceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/exception/$file" -Force | Out-Null
}

Write-Host "Creating domain repository files..." -ForegroundColor Yellow

# Create Domain Repository files
$repositoryFiles = @(
    "AccountRepository.kt",
    "CreditProfileRepository.kt",
    "CurrencyExchangeRateRepository.kt",
    "CustomerInvoiceRepository.kt",
    "JournalEntryRepository.kt",
    "LedgerAccountBalanceRepository.kt",
    "PaymentDisbursementRepository.kt",
    "PaymentReceiptRepository.kt",
    "VendorBillRepository.kt",
    "FiscalPeriodRepository.kt",
    "BankAccountRepository.kt",
    "AuditLogRepository.kt"
)

foreach ($file in $repositoryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/repository/$file" -Force | Out-Null
}

Write-Host "Creating domain service files..." -ForegroundColor Yellow

# Create Domain Service files - Create nested directory for CostCalculationService
New-Item -ItemType Directory -Path "$mainKotlinDir/domain/service/CostCalculationService" -Force | Out-Null

$domainServiceFiles = @(
    "FinancialPeriodService.kt",
    "MultiCurrencyConverterService.kt",
    "CostCalculationService.kt",
    "TaxCalculationService.kt",
    "BankReconciliationService.kt",
    "ChartOfAccountsService.kt"
)

foreach ($file in $domainServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/service/$file" -Force | Out-Null
}

# Create nested service files under CostCalculationService
$costCalculationServiceFiles = @(
    "InventoryCostingStrategy.kt",
    "ManufacturingCostingStrategy.kt",
    "ServiceCostingStrategy.kt"
)

foreach ($file in $costCalculationServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/service/CostCalculationService/$file" -Force | Out-Null
}

Write-Host "Creating domain value object files..." -ForegroundColor Yellow

# Create Domain Value Object files
$valueObjectFiles = @(
    "AccountId.kt",
    "AccountingPeriod.kt",
    "CostMethod.kt",
    "CreditLimit.kt",
    "Currency.kt",
    "CustomerId.kt",
    "FinancialAmount.kt",
    "InvoiceStatus.kt",
    "JournalEntryId.kt",
    "PaymentMethod.kt",
    "VendorId.kt",
    "PaymentTerm.kt",
    "FiscalPeriodStatus.kt",
    "TransactionType.kt"
)

foreach ($file in $valueObjectFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/valueobject/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter incoming files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Incoming files
$incomingAdapterFiles = @(
    "BillingKafkaConsumer.kt",
    "FinanceGraphQLResource.kt",
    "FinanceResource.kt",
    "InventoryKafkaConsumer.kt",
    "ManufacturingKafkaConsumer.kt",
    "SalesKafkaConsumer.kt",
    "ServiceKafkaConsumer.kt",
    "UserManagementKafkaConsumer.kt",
    "SystemKafkaConsumer.kt"
)

foreach ($file in $incomingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/incoming/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter outgoing files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Outgoing files
$outgoingAdapterFiles = @(
    "JpaAccountRepository.kt",
    "JpaCreditProfileRepository.kt",
    "JpaCurrencyExchangeRateRepository.kt",
    "JpaCustomerInvoiceRepository.kt",
    "JpaJournalEntryRepository.kt",
    "JpaLedgerAccountBalanceRepository.kt",
    "JpaPaymentDisbursementRepository.kt",
    "JpaPaymentReceiptRepository.kt",
    "JpaVendorBillRepository.kt",
    "JpaFiscalPeriodRepository.kt",
    "JpaBankAccountRepository.kt",
    "JpaAuditLogRepository.kt",
    "KafkaFinanceEventPublisher.kt",
    "NotificationServiceClient.kt",
    "ExternalTaxServiceAdapter.kt",
    "ReportingServiceClient.kt"
)

foreach ($file in $outgoingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure configuration files..." -ForegroundColor Yellow

# Create Infrastructure Configuration files
$configFiles = @(
    "KafkaConfig.kt",
    "ObjectMapperConfig.kt",
    "TenantSchemaResolver.kt",
    "SchedulingConfig.kt",
    "SecurityConfig.kt"
)

foreach ($file in $configFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/configuration/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure exception files..." -ForegroundColor Yellow

# Create Infrastructure Exception files
$infraExceptionFiles = @(
    "FinanceExceptionMapper.kt"
)

foreach ($file in $infraExceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/exception/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure util files..." -ForegroundColor Yellow

# Create Infrastructure Util files
$utilFiles = @(
    "FinanceMapper.kt"
)

foreach ($file in $utilFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/util/$file" -Force | Out-Null
}

Write-Host "Creating resource files..." -ForegroundColor Yellow

# Create Resource files
New-Item -ItemType File -Path "$mainResourcesDir/application.properties" -Force | Out-Null
New-Item -ItemType File -Path "$mainResourcesDir/data/chart-of-accounts.json" -Force | Out-Null
New-Item -ItemType File -Path "$webappDir/index.html" -Force | Out-Null

Write-Host "Creating build and documentation files..." -ForegroundColor Yellow

# Create build and documentation files
New-Item -ItemType File -Path "$serviceDir/build.gradle.kts" -Force | Out-Null
New-Item -ItemType File -Path "$serviceDir/README.md" -Force | Out-Null

Write-Host "Creating test files..." -ForegroundColor Yellow

# Create placeholder test files for main directories
New-Item -ItemType File -Path "$testKotlinDir/application/service/FinanceApplicationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/JournalEntryTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/CustomerInvoiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/service/CostCalculationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/adapter/FinanceResourceTest.kt" -Force | Out-Null

Write-Host ""
Write-Host "✅ Successfully created finance-service structure!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Main source files: $(((Get-ChildItem -Path "$serviceDir/src/main" -Recurse -File).Count)) files"
Write-Host "   • Test files: $(((Get-ChildItem -Path "$serviceDir/src/test" -Recurse -File).Count)) files"
Write-Host "   • Total directories: $(((Get-ChildItem -Path $serviceDir -Recurse -Directory).Count)) directories"
Write-Host "   • Service location: $serviceDir"
Write-Host ""
Write-Host "🎯 Next steps:"
Write-Host "   1. Update build.gradle.kts with finance-specific dependencies (Spring Boot, JPA, Kafka, etc.)"
Write-Host "   2. Configure application.properties with database and Kafka settings"
Write-Host "   3. Add database migration scripts for Chart of Accounts, Journal Entries, etc."
Write-Host "   4. Implement the Kotlin classes with proper financial domain logic"
Write-Host "   5. Set up chart-of-accounts.json seed data"

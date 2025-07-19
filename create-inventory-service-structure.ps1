# PowerShell script to create inventory-service structure
# This script creates all folders and files for the inventory-service without any code content

$serviceName = "inventory-service"
$basePackagePath = "org/chiro/inventory"

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
    "AddStockToLocationCommand.kt",
    "AdjustStockLevelCommand.kt",
    "AssembleKitCommand.kt",
    "CreateProductCommand.kt",
    "CreateProductVariantCommand.kt",
    "CreateWarehouseLocationCommand.kt",
    "DisassembleKitCommand.kt",
    "InitiatePhysicalInventoryCountCommand.kt",
    "RecordPhysicalInventoryCountCommand.kt",
    "MoveStockWithinWarehouseCommand.kt",
    "ReceivePurchaseOrderItemsCommand.kt",
    "ReserveStockAtLocationCommand.kt",
    "ShipSalesOrderItemsCommand.kt",
    "UpdateProductDetailsCommand.kt",
    "UpdateWarehouseLocationCommand.kt",
    "TransferStockBetweenWarehousesCommand.kt"
)

foreach ($file in $commandFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/command/$file" -Force | Out-Null
}

Write-Host "Creating application DTO event files..." -ForegroundColor Yellow

# Create Application DTO Event files
$eventFiles = @(
    "GoodsReceivedToLocationEventDTO.kt",
    "GoodsShippedFromLocationEventDTO.kt",
    "InventoryAdjustedEventDTO.kt",
    "InventoryCostUpdatedEventDTO.kt",
    "KitAssembledEventDTO.kt",
    "PhysicalInventoryCountCompletedEventDTO.kt",
    "ProductCreatedEventDTO.kt",
    "ProductVariantCreatedEventDTO.kt",
    "StockMovedEventDTO.kt",
    "StockReservedEventDTO.kt",
    "StockReleasedEventDTO.kt"
)

foreach ($file in $eventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/event/$file" -Force | Out-Null
}

Write-Host "Creating application DTO query files..." -ForegroundColor Yellow

# Create Application DTO Query files
$queryFiles = @(
    "InventoryAuditLogDTO.kt",
    "InventoryValuationReportDTO.kt",
    "PhysicalInventoryCountStatusDTO.kt",
    "ProductDetailDTO.kt",
    "ProductVariantDTO.kt",
    "StockHistoryDTO.kt",
    "StockLevelByLocationDTO.kt",
    "StockSummaryByProductDTO.kt",
    "UnitOfMeasureConversionDTO.kt",
    "WarehouseLocationDTO.kt"
)

foreach ($file in $queryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/query/$file" -Force | Out-Null
}

Write-Host "Creating application DTO response files..." -ForegroundColor Yellow

# Create Application DTO Response files
$responseFiles = @(
    "StockAllocationResponse.kt"
)

foreach ($file in $responseFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/response/$file" -Force | Out-Null
}

Write-Host "Creating application port files..." -ForegroundColor Yellow

# Create Application Port Incoming files
$incomingPortFiles = @(
    "InventoryCommandPort.kt",
    "InventoryQueryPort.kt",
    "ManufacturingOrderEventConsumerPort.kt",
    "ProcurementOrderEventConsumerPort.kt",
    "SalesOrderEventConsumerPort.kt",
    "SalesReturnEventConsumerPort.kt"
)

foreach ($file in $incomingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/incoming/$file" -Force | Out-Null
}

# Create Application Port Outgoing files
$outgoingPortFiles = @(
    "AuditLogPublisherPort.kt",
    "FinanceEventPublisherPort.kt",
    "InventoryEventPublisherPort.kt",
    "QualityControlIntegrationPort.kt",
    "ShippingServiceIntegrationPort.kt"
)

foreach ($file in $outgoingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating application service files..." -ForegroundColor Yellow

# Create Application Service files
$applicationServiceFiles = @(
    "InventoryAdjustmentApplicationService.kt",
    "InventoryCostingApplicationService.kt",
    "KitAndBundleApplicationService.kt",
    "PhysicalInventoryApplicationService.kt",
    "ProductMasterApplicationService.kt",
    "StockMovementApplicationService.kt",
    "WarehouseManagementApplicationService.kt"
)

foreach ($file in $applicationServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/service/$file" -Force | Out-Null
}

Write-Host "Creating domain aggregate files..." -ForegroundColor Yellow

# Create Domain Aggregate files
$aggregateFiles = @(
    "Product.kt",
    "Warehouse.kt",
    "StockReservation.kt",
    "PhysicalInventoryCount.kt"
)

foreach ($file in $aggregateFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/aggregate/$file" -Force | Out-Null
}

Write-Host "Creating domain entity files..." -ForegroundColor Yellow

# Create Domain Entity files
$entityFiles = @(
    "InventoryLocation.kt",
    "ProductVariant.kt",
    "ProductKitDefinition.kt",
    "ProductStock.kt",
    "StockMovement.kt",
    "StockAdjustment.kt",
    "CostLayer.kt",
    "CycleCountSchedule.kt",
    "InventoryAuditLog.kt"
)

foreach ($file in $entityFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/entity/$file" -Force | Out-Null
}

Write-Host "Creating domain event files..." -ForegroundColor Yellow

# Create Domain Event files
$domainEventFiles = @(
    "GoodsReceivedIntoLocationEvent.kt",
    "GoodsShippedFromLocationEvent.kt",
    "InventoryAdjustedEvent.kt",
    "InventoryCostUpdatedEvent.kt",
    "KitAssembledEvent.kt",
    "ProductCreatedEvent.kt",
    "ProductVariantCreatedEvent.kt",
    "PhysicalInventoryCountInitiatedEvent.kt",
    "PhysicalInventoryCountRecordedEvent.kt",
    "StockMovedEvent.kt",
    "StockReservedEvent.kt",
    "StockReleasedEvent.kt",
    "StockTransferredBetweenWarehousesEvent.kt",
    "InventoryPolicyViolationEvent.kt"
)

foreach ($file in $domainEventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/event/$file" -Force | Out-Null
}

Write-Host "Creating domain exception files..." -ForegroundColor Yellow

# Create Domain Exception files
$exceptionFiles = @(
    "InsufficientStockAtLocationException.kt",
    "InvalidProductStateException.kt",
    "InvalidUnitOfMeasureConversionException.kt",
    "KitAssemblyRuleViolationException.kt",
    "ProductNotFoundException.kt",
    "ProductVariantNotFoundException.kt",
    "StockReservationConflictException.kt",
    "WarehouseLocationNotFoundException.kt",
    "PhysicalInventoryCountInProgressException.kt"
)

foreach ($file in $exceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/exception/$file" -Force | Out-Null
}

Write-Host "Creating domain repository files..." -ForegroundColor Yellow

# Create Domain Repository files
$repositoryFiles = @(
    "ProductRepository.kt",
    "PhysicalInventoryCountRepository.kt",
    "StockAdjustmentRepository.kt",
    "StockMovementRepository.kt",
    "StockReservationRepository.kt",
    "WarehouseRepository.kt",
    "InventoryAuditLogRepository.kt"
)

foreach ($file in $repositoryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/repository/$file" -Force | Out-Null
}

Write-Host "Creating domain service files..." -ForegroundColor Yellow

# Create Domain Service files
$domainServiceFiles = @(
    "InventoryAllocationStrategy.kt",
    "InventoryPolicyService.kt",
    "InventoryTraceabilityService.kt",
    "InventoryValuationService.kt",
    "LocationPickerService.kt",
    "UnitOfMeasureConversionService.kt"
)

foreach ($file in $domainServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/service/$file" -Force | Out-Null
}

Write-Host "Creating domain value object files..." -ForegroundColor Yellow

# Create Domain Value Object files
$valueObjectFiles = @(
    "BatchId.kt",
    "BinId.kt",
    "CostLayerId.kt",
    "InventoryStatus.kt",
    "LocationType.kt",
    "LotNumber.kt",
    "ProductAttribute.kt",
    "ProductId.kt",
    "ProductSKU.kt",
    "Quantity.kt",
    "ReorderPoint.kt",
    "SafetyStock.kt",
    "SerialNumber.kt",
    "UnitOfMeasure.kt",
    "UoMConversionRule.kt",
    "WarehouseId.kt",
    "WarehouseLocationId.kt"
)

foreach ($file in $valueObjectFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/valueobject/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter incoming files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Incoming files
$incomingAdapterFiles = @(
    "InventoryGraphQLResource.kt",
    "InventoryResource.kt",
    "ManufacturingKafkaConsumer.kt",
    "ProcurementKafkaConsumer.kt",
    "SalesKafkaConsumer.kt",
    "SalesReturnKafkaConsumer.kt"
)

foreach ($file in $incomingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/incoming/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter outgoing files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Outgoing files
$outgoingAdapterFiles = @(
    "JpaCostLayerRepository.kt",
    "JpaInventoryAuditLogRepository.kt",
    "JpaPhysicalInventoryCountRepository.kt",
    "JpaProductRepository.kt",
    "JpaStockAdjustmentRepository.kt",
    "JpaStockMovementRepository.kt",
    "JpaStockReservationRepository.kt",
    "JpaWarehouseRepository.kt",
    "KafkaAuditLogPublisher.kt",
    "KafkaFinanceEventPublisher.kt",
    "KafkaInventoryEventPublisher.kt",
    "QualityControlServiceClient.kt",
    "ShippingServiceClient.kt"
)

foreach ($file in $outgoingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure configuration files..." -ForegroundColor Yellow

# Create Infrastructure Configuration files
$configFiles = @(
    "KafkaConfig.kt",
    "ObjectMapperConfig.kt",
    "SchedulingConfig.kt",
    "SecurityConfig.kt",
    "TenantSchemaResolver.kt"
)

foreach ($file in $configFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/configuration/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure exception files..." -ForegroundColor Yellow

# Create Infrastructure Exception files
$infraExceptionFiles = @(
    "InventoryExceptionMapper.kt"
)

foreach ($file in $infraExceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/exception/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure util files..." -ForegroundColor Yellow

# Create Infrastructure Util files
$utilFiles = @(
    "InventoryMapper.kt"
)

foreach ($file in $utilFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/util/$file" -Force | Out-Null
}

Write-Host "Creating resource files..." -ForegroundColor Yellow

# Create Resource files
New-Item -ItemType File -Path "$mainResourcesDir/application.properties" -Force | Out-Null
New-Item -ItemType File -Path "$mainResourcesDir/data/default-uom-conversions.json" -Force | Out-Null
New-Item -ItemType File -Path "$mainResourcesDir/data/initial-product-categories.json" -Force | Out-Null
New-Item -ItemType File -Path "$webappDir/index.html" -Force | Out-Null

Write-Host "Creating build and documentation files..." -ForegroundColor Yellow

# Create build and documentation files
New-Item -ItemType File -Path "$serviceDir/build.gradle.kts" -Force | Out-Null
New-Item -ItemType File -Path "$serviceDir/README.md" -Force | Out-Null

Write-Host "Creating test files..." -ForegroundColor Yellow

# Create placeholder test files for main directories
New-Item -ItemType File -Path "$testKotlinDir/application/service/InventoryApplicationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/ProductTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/aggregate/WarehouseTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/adapter/InventoryResourceTest.kt" -Force | Out-Null

Write-Host ""
Write-Host "✅ Successfully created inventory-service structure!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Main source files: $(((Get-ChildItem -Path "$serviceDir/src/main" -Recurse -File).Count)) files"
Write-Host "   • Test files: $(((Get-ChildItem -Path "$serviceDir/src/test" -Recurse -File).Count)) files"
Write-Host "   • Total directories: $(((Get-ChildItem -Path $serviceDir -Recurse -Directory).Count)) directories"
Write-Host "   • Service location: $serviceDir"
Write-Host ""
Write-Host "🎯 Next steps:"
Write-Host "   1. Update build.gradle.kts with dependencies"
Write-Host "   2. Configure application.properties"
Write-Host "   3. Add database migration scripts"
Write-Host "   4. Implement the Kotlin classes"

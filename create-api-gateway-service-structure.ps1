# PowerShell script to create api-gateway structure
# This script creates all folders and files for the api-gateway without any code content

$serviceName = "api-gateway"
$basePackagePath = "org/chiro/gateway"

# Create base service directory
$serviceDir = "$serviceName"
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
    "$mainKotlinDir/application/dto/request",
    "$mainKotlinDir/application/dto/response",
    "$mainKotlinDir/application/dto/event",
    "$mainKotlinDir/application/port/incoming",
    "$mainKotlinDir/application/port/outgoing",
    "$mainKotlinDir/application/service",
    
    # Domain structure
    "$mainKotlinDir/domain/model",
    "$mainKotlinDir/domain/event",
    "$mainKotlinDir/domain/exception",
    "$mainKotlinDir/domain/repository",
    "$mainKotlinDir/domain/valueobject",
    
    # Infrastructure structure
    "$mainKotlinDir/infrastructure/adapter/incoming",
    "$mainKotlinDir/infrastructure/adapter/outgoing",
    "$mainKotlinDir/infrastructure/configuration",
    "$mainKotlinDir/infrastructure/exception",
    "$mainKotlinDir/infrastructure/filter",
    
    # Resources structure
    "$mainResourcesDir/META-INF/resources",
    
    # Webapp
    "$webappDir",
    
    # Test structure
    "$testKotlinDir/application/dto",
    "$testKotlinDir/application/port",
    "$testKotlinDir/application/service",
    "$testKotlinDir/domain/model",
    "$testKotlinDir/domain/event",
    "$testKotlinDir/domain/exception",
    "$testKotlinDir/domain/repository",
    "$testKotlinDir/domain/valueobject",
    "$testKotlinDir/infrastructure/adapter",
    "$testKotlinDir/infrastructure/configuration",
    "$testKotlinDir/infrastructure/filter",
    "$testResourcesDir"
)

# Create all directories
foreach ($dir in $directories) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
}

Write-Host "Creating application DTO request files..." -ForegroundColor Yellow

# Create Application DTO Request files
$requestFiles = @(
    "AuthenticationRequest.kt",
    "AuthorizationRequest.kt"
)

foreach ($file in $requestFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/request/$file" -Force | Out-Null
}

Write-Host "Creating application DTO response files..." -ForegroundColor Yellow

# Create Application DTO Response files
$responseFiles = @(
    "AuthenticationResponse.kt",
    "AuthorizationResponse.kt"
)

foreach ($file in $responseFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/response/$file" -Force | Out-Null
}

Write-Host "Creating application DTO event files..." -ForegroundColor Yellow

# Create Application DTO Event files
$eventFiles = @(
    "TenantCreatedEventDTO.kt",
    "UserLoggedInEventDTO.kt"
)

foreach ($file in $eventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/dto/event/$file" -Force | Out-Null
}

Write-Host "Creating application port files..." -ForegroundColor Yellow

# Create Application Port Incoming files
$incomingPortFiles = @(
    "GatewayRequestPort.kt",
    "UserManagementEventConsumerPort.kt",
    "TenantEventConsumerPort.kt"
)

foreach ($file in $incomingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/incoming/$file" -Force | Out-Null
}

# Create Application Port Outgoing files
$outgoingPortFiles = @(
    "UserManagementServiceIntegrationPort.kt",
    "ServiceRoutingPort.kt",
    "GatewayEventPublisherPort.kt",
    "MetricsPublisherPort.kt"
)

foreach ($file in $outgoingPortFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/port/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating application service files..." -ForegroundColor Yellow

# Create Application Service files
$applicationServiceFiles = @(
    "AuthenticationService.kt",
    "AuthorizationService.kt",
    "RequestRoutingService.kt",
    "RateLimitingService.kt",
    "CircuitBreakerService.kt"
)

foreach ($file in $applicationServiceFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/application/service/$file" -Force | Out-Null
}

Write-Host "Creating domain model files..." -ForegroundColor Yellow

# Create Domain Model files
$modelFiles = @(
    "RouteDefinition.kt",
    "SecurityPolicy.kt",
    "RateLimitRule.kt",
    "CircuitBreakerState.kt"
)

foreach ($file in $modelFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/model/$file" -Force | Out-Null
}

Write-Host "Creating domain event files..." -ForegroundColor Yellow

# Create Domain Event files
$domainEventFiles = @(
    "RequestRoutedEvent.kt",
    "AuthenticationFailedEvent.kt",
    "AuthorizationFailedEvent.kt",
    "CircuitBreakerOpenedEvent.kt"
)

foreach ($file in $domainEventFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/event/$file" -Force | Out-Null
}

Write-Host "Creating domain exception files..." -ForegroundColor Yellow

# Create Domain Exception files
$exceptionFiles = @(
    "RouteNotFoundException.kt",
    "UnauthorizedGatewayException.kt",
    "ForbiddenGatewayException.kt",
    "RateLimitExceededException.kt",
    "ServiceUnavailableException.kt"
)

foreach ($file in $exceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/exception/$file" -Force | Out-Null
}

Write-Host "Creating domain repository files..." -ForegroundColor Yellow

# Create Domain Repository files
$repositoryFiles = @(
    "RouteDefinitionRepository.kt"
)

foreach ($file in $repositoryFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/repository/$file" -Force | Out-Null
}

Write-Host "Creating domain value object files..." -ForegroundColor Yellow

# Create Domain Value Object files
$valueObjectFiles = @(
    "ApiKey.kt",
    "Path.kt",
    "ServiceName.kt",
    "Token.kt",
    "ClientIP.kt"
)

foreach ($file in $valueObjectFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/domain/valueobject/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter incoming files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Incoming files
$incomingAdapterFiles = @(
    "SpringCloudGatewayConfig.kt",
    "GatewayController.kt",
    "UserManagementKafkaConsumer.kt",
    "TenantKafkaConsumer.kt"
)

foreach ($file in $incomingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/incoming/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure adapter outgoing files..." -ForegroundColor Yellow

# Create Infrastructure Adapter Outgoing files
$outgoingAdapterFiles = @(
    "RestUserManagementClient.kt",
    "KafkaGatewayEventPublisher.kt",
    "PrometheusMetricsPublisher.kt"
)

foreach ($file in $outgoingAdapterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/adapter/outgoing/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure configuration files..." -ForegroundColor Yellow

# Create Infrastructure Configuration files
$configFiles = @(
    "GatewayRoutesConfig.kt",
    "SecurityConfig.kt",
    "RateLimitingConfig.kt",
    "CircuitBreakerConfig.kt",
    "KafkaConfig.kt",
    "OpenApiConfig.kt"
)

foreach ($file in $configFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/configuration/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure exception files..." -ForegroundColor Yellow

# Create Infrastructure Exception files
$infraExceptionFiles = @(
    "GatewayExceptionHandler.kt"
)

foreach ($file in $infraExceptionFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/exception/$file" -Force | Out-Null
}

Write-Host "Creating infrastructure filter files..." -ForegroundColor Yellow

# Create Infrastructure Filter files
$filterFiles = @(
    "AuthenticationFilter.kt",
    "AuthorizationFilter.kt",
    "TenantResolutionFilter.kt",
    "LoggingFilter.kt",
    "RateLimitFilter.kt"
)

foreach ($file in $filterFiles) {
    New-Item -ItemType File -Path "$mainKotlinDir/infrastructure/filter/$file" -Force | Out-Null
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
New-Item -ItemType File -Path "$testKotlinDir/application/service/AuthenticationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/application/service/AuthorizationServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/application/service/RequestRoutingServiceTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/model/RouteDefinitionTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/domain/model/SecurityPolicyTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/filter/AuthenticationFilterTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/filter/AuthorizationFilterTest.kt" -Force | Out-Null
New-Item -ItemType File -Path "$testKotlinDir/infrastructure/filter/RateLimitFilterTest.kt" -Force | Out-Null

Write-Host ""
Write-Host "✅ Successfully created api-gateway structure!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Main source files: $(((Get-ChildItem -Path "$serviceDir/src/main" -Recurse -File).Count)) files"
Write-Host "   • Test files: $(((Get-ChildItem -Path "$serviceDir/src/test" -Recurse -File).Count)) files"
Write-Host "   • Total directories: $(((Get-ChildItem -Path $serviceDir -Recurse -Directory).Count)) directories"
Write-Host "   • Service location: $serviceDir"
Write-Host ""
Write-Host "🎯 Next steps:"
Write-Host "   1. Update build.gradle.kts with Spring Cloud Gateway dependencies"
Write-Host "   2. Configure application.properties with gateway routes and security settings"
Write-Host "   3. Implement Spring Cloud Gateway filters for authentication, authorization, and routing"
Write-Host "   4. Set up rate limiting and circuit breaker configurations"
Write-Host "   5. Configure Kafka consumers for tenant and user management events"
Write-Host "   6. Set up Prometheus metrics and health endpoints"
Write-Host "   7. Configure CORS and JWT validation for gateway-level security"

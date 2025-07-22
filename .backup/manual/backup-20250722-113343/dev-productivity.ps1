#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Chiro ERP Development Productivity Suite
    
.DESCRIPTION
    Collection of productivity tools to make ERP development faster and easier
    
.PARAMETER Action
    The action to perform:
    - setup-workspace: Set up complete development workspace
    - generate-service: Generate new service from template
    - run-tests: Smart test runner with parallel execution
    - database-tools: Database development utilities
    - api-explorer: Launch API documentation and testing tools
    - code-quality: Run comprehensive code quality checks
    - performance-profile: Profile application performance
    - debug-assist: Launch debugging utilities
    - validate-infrastructure: Verify file structure and deployment consistency
    - graalvm-build: Build native executables with GraalVM
    - native-deploy: Deploy native containers to Kubernetes
    
.PARAMETER ServiceName
    Name of the service for service-specific operations
    
.PARAMETER Fast
    Use fast/optimized operations where available

.PARAMETER Native
    Use native/GraalVM builds instead of JVM
    Use fast/optimized operations where available
    
.EXAMPLE
    .\dev-productivity.ps1 -Action setup-workspace
    .\dev-productivity.ps1 -Action generate-service -ServiceName "new-service"
    .\dev-productivity.ps1 -Action run-tests -Fast
    .\dev-productivity.ps1 -Action api-explorer
    .\dev-productivity.ps1 -Action validate-infrastructure
    .\dev-productivity.ps1 -Action graalvm-build -ServiceName "core-business-service"
    .\dev-productivity.ps1 -Action native-deploy -Native
#>

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("setup-workspace", "generate-service", "run-tests", "database-tools", "api-explorer", "code-quality", "performance-profile", "debug-assist", "validate-infrastructure", "graalvm-build", "native-deploy")]
    [string]$Action,
    
    [string]$ServiceName,
    [switch]$Fast,
    [switch]$Native
)

# Color functions
function Write-ProductivityHeader {
    param([string]$Title)
    Write-Host "`n" + "🚀" * 20 -ForegroundColor Cyan
    Write-Host "🛠️  $Title" -ForegroundColor Cyan
    Write-Host "🚀" * 20 -ForegroundColor Cyan
}

function Write-Task {
    param([string]$Task)
    Write-Host "⚡ $Task..." -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

# Workspace setup
function Set-DevelopmentWorkspace {
    Write-ProductivityHeader "Setting up Complete Development Workspace"
    
    Write-Task "Creating development environment file"
    if (-not (Test-Path ".env")) {
        Copy-Item ".env.dev.template" ".env"
        Write-Success "Created .env from template"
    }
    
    Write-Task "Setting up Git hooks"
    $hookScript = @"
#!/bin/sh
# Pre-commit hook - run validation before commit
echo "🔍 Running pre-commit validation..."
.\run-comprehensive-validation.ps1 -SkipFixes
exit $?
"@
    New-Item -Path ".git/hooks" -ItemType Directory -Force | Out-Null
    Set-Content -Path ".git/hooks/pre-commit" -Value $hookScript
    Write-Success "Git pre-commit hook installed"
    
    Write-Task "Starting development infrastructure"
    & ".\scripts\dev.ps1" -Action start
    
    Write-Task "Setting up IDE workspace"
    $workspaceSettings = @{
        "java.configuration.updateBuildConfiguration" = "automatic"
        "java.compile.nullAnalysis.mode"              = "automatic"
        "gradle.nestedProjects"                       = $true
        "kotlin.languageServer.enabled"               = $true
    }
    
    $vscodeDir = ".vscode"
    New-Item -Path $vscodeDir -ItemType Directory -Force | Out-Null
    $workspaceSettings | ConvertTo-Json | Out-File "$vscodeDir/settings.json"
    Write-Success "VS Code workspace configured"
    
    Write-Success "Development workspace ready! 🎉"
    Write-Info "Available URLs:"
    Write-Info "  - API Gateway: http://localhost:8080"
    Write-Info "  - Kafka UI: http://localhost:8081"
    Write-Info "  - Grafana: http://localhost:3000"
    Write-Info "  - Prometheus: http://localhost:9090"
}

# Service generator
function New-ServiceFromTemplate {
    param([string]$ServiceName)
    
    Write-ProductivityHeader "Generating New Service: $ServiceName"
    
    if (-not $ServiceName) {
        Write-Host "❌ ServiceName is required for service generation" -ForegroundColor Red
        return
    }
    
    $servicePath = "consolidated-services\$ServiceName"
    
    Write-Task "Creating service structure"
    New-Item -Path $servicePath -ItemType Directory -Force | Out-Null
    New-Item -Path "$servicePath\src\main\kotlin\org\chiro\$ServiceName" -ItemType Directory -Force | Out-Null
    New-Item -Path "$servicePath\src\main\resources" -ItemType Directory -Force | Out-Null
    New-Item -Path "$servicePath\src\test\kotlin" -ItemType Directory -Force | Out-Null
    New-Item -Path "$servicePath\docker" -ItemType Directory -Force | Out-Null
    
    Write-Task "Generating build.gradle.kts"
    $buildFile = @"
plugins {
    id("consolidated-service-conventions")
}

dependencies {
    // $ServiceName specific dependencies
    
    // Database
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")
    
    // Messaging
    implementation("io.quarkus:quarkus-kafka-client")
    
    // Monitoring & Health
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-health")
    
    // Security
    implementation("io.quarkus:quarkus-oidc")
    
    // Validation
    implementation("io.quarkus:quarkus-hibernate-validator")
    
    // GraalVM Native support
    implementation("io.quarkus:quarkus-container-image-jib")
    implementation("io.quarkus:quarkus-kubernetes")
    
    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-test-h2")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

// GraalVM Native Configuration
quarkus {
    buildForkOptions {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
        systemProperty("maven.home", System.getenv("M2_HOME"))
    }
    
    // Native build configuration
    nativeConfig {
        containerBuild.set(true)
        builderImage.set("quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21")
        additionalBuildArgs.addAll(listOf(
            "-H:+ReportExceptionStackTraces",
            "-H:+PrintClassInitialization",
            "--enable-url-protocols=https",
            "--initialize-at-build-time=org.slf4j.LoggerFactory"
        ))
    }
}

tasks {
    register("buildNative") {
        group = "build"
        description = "Build native executable"
        
        doLast {
            exec {
                commandLine("./gradlew", "build", "-Dquarkus.package.type=native")
            }
        }
    }
    
    register("dockerBuildNative") {
        dependsOn("buildNative")
        group = "docker"
        description = "Build native Docker image"
        
        doLast {
            exec {
                commandLine("docker", "build", "-f", "docker/Dockerfile.native", "-t", "$servicePath:latest-native", ".")
            }
        }
    }
}
"@
    Set-Content -Path "$servicePath\build.gradle.kts" -Value $buildFile
    
    Write-Task "Generating application.yml"
    $appConfig = @"
quarkus:
  application:
    name: $ServiceName
  http:
    port: 8080
  datasource:
    db-kind: postgresql
    username: chiro
    password: chiro123
    jdbc:
      url: jdbc:postgresql://localhost:5432/chiro_$(($ServiceName -replace '-','_'))
  hibernate-orm:
    database:
      generation: update
    sql-load-script: no-file
  flyway:
    migrate-at-start: true
    locations: classpath:db/migration

# Development profile
"%dev":
  quarkus:
    log:
      level: DEBUG
    datasource:
      jdbc:
        url: jdbc:postgresql://localhost:5432/chiro_$(($ServiceName -replace '-','_'))_dev

# Test profile  
"%test":
  quarkus:
    datasource:
      db-kind: h2
      jdbc:
        url: jdbc:h2:mem:test
"@
    Set-Content -Path "$servicePath\src\main\resources\application.yml" -Value $appConfig
    
    Write-Task "Generating sample controller"
    $pascalServiceName = (Get-Culture).TextInfo.ToTitleCase($ServiceName) -replace '-', ''
    $controllerCode = @"
package org.chiro.$ServiceName

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/$ServiceName")
@Tag(name = "$pascalServiceName", description = "$pascalServiceName operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ${pascalServiceName}Resource {

    @GET
    @Path("/health")
    @Operation(summary = "Health check for $ServiceName")
    fun health(): Map<String, String> {
        return mapOf(
            "service" to "$ServiceName",
            "status" to "UP",
            "timestamp" to java.time.Instant.now().toString()
        )
    }
    
    @GET
    @Operation(summary = "Get all items")
    fun getAll(): List<String> {
        return listOf("Sample data for $ServiceName")
    }
}
"@
    Set-Content -Path "$servicePath\src\main\kotlin\org\chiro\$ServiceName\${pascalServiceName}Resource.kt" -Value $controllerCode
    
    Write-Task "Generating native Dockerfile"
    $nativeDockerfile = @"
# Multi-stage build for GraalVM native compilation
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21 AS build

# Set working directory
WORKDIR /code

# Copy gradle wrapper and dependencies
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY buildSrc/ buildSrc/

# Copy all consolidated service source code
COPY consolidated-services/ consolidated-services/

# Build native executable
RUN ./gradlew :consolidated-services:$ServiceName:build -Dquarkus.package.type=native -Dquarkus.native.container-build=true

# Runtime stage - minimal image
FROM quay.io/quarkus/quarkus-micro-image:2.0

# Copy the native executable
COPY --from=build /code/consolidated-services/$ServiceName/build/*-runner /work/application

# Set proper permissions
RUN chmod 775 /work/application

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/q/health || exit 1

# Expose port
EXPOSE 8080

# Run the native executable
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
"@
    Set-Content -Path "$servicePath\docker\Dockerfile.native" -Value $nativeDockerfile
    
    Write-Task "Generating JVM Dockerfile"
    $dockerfile = @"
FROM registry.access.redhat.com/ubi8/openjdk-21:1.18

ENV LANGUAGE='en_US:en'

COPY --chown=185 build/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 build/quarkus-app/*.jar /deployments/
COPY --chown=185 build/quarkus-app/app/ /deployments/app/
COPY --chown=185 build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
"@
    Set-Content -Path "$servicePath\docker\Dockerfile" -Value $dockerfile
    
    Write-Task "Generating README.md"
    $readme = @"
# $ServiceName

Description of the $ServiceName service

## Running the Service

### Development Mode
\`\`\`bash
./gradlew :consolidated-services:$ServiceName:quarkusDev
\`\`\`

### Building
\`\`\`bash
./gradlew :consolidated-services:$ServiceName:build
\`\`\`

### Docker
\`\`\`bash
# Build the service
./gradlew :consolidated-services:$ServiceName:build

# Build Docker image
docker build -f consolidated-services/$ServiceName/docker/Dockerfile -t chiro/$ServiceName:latest .

# Run the container
docker run -i --rm -p 8080:8080 chiro/$ServiceName:latest
\`\`\`

## API Endpoints

- Health Check: \`GET /$ServiceName/health\`
- Get All: \`GET /$ServiceName\`

## Database

Service uses PostgreSQL database: \`chiro_$(($ServiceName -replace '-','_'))\`
"@
    Set-Content -Path "$servicePath\README.md" -Value $readme
    
    Write-Success "Service $ServiceName generated successfully!"
    Write-Info "Next steps:"
    Write-Info "  1. ./gradlew :consolidated-services:$ServiceName:build"
    Write-Info "  2. ./gradlew :consolidated-services:$ServiceName:quarkusDev"
    Write-Info "  3. Open http://localhost:8080/$ServiceName/health"
    Write-Info "Native build options:"
    Write-Info "  - ./gradlew :consolidated-services:$ServiceName:buildNative"
    Write-Info "  - ./gradlew :consolidated-services:$ServiceName:dockerBuildNative"
}

# Smart test runner
function Invoke-SmartTests {
    Write-ProductivityHeader "Smart Test Runner"
    
    if ($Fast) {
        Write-Task "Running fast test suite (unit tests only)"
        .\gradlew.bat test -x integrationTest --parallel
    }
    else {
        Write-Task "Running comprehensive test suite"
        .\gradlew.bat check
    }
    
    Write-Task "Generating test reports"
    .\gradlew.bat jacocoTestReport
    
    Write-Success "Tests completed!"
    Write-Info "Test reports available at:"
    Get-ChildItem -Path "." -Recurse -Name "index.html" | Where-Object { $_ -like "*test-results*" -or $_ -like "*jacoco*" } | ForEach-Object {
        Write-Info "  - $((Resolve-Path $_).Path)"
    }
}

# Database development tools
function Invoke-DatabaseTools {
    Write-ProductivityHeader "Database Development Tools"
    
    Write-Task "Starting database tools"
    
    # Start Adminer for database management
    docker run -d --name adminer-chiro --network chiro-erp_default -p 8082:8080 adminer
    
    Write-Success "Database tools started!"
    Write-Info "Available tools:"
    Write-Info "  - Adminer (DB Admin): http://localhost:8082"
    Write-Info "    - System: PostgreSQL"
    Write-Info "    - Server: postgres"
    Write-Info "    - Username: chiro"
    Write-Info "    - Password: chiro123"
    
    Write-Info "Database connection commands:"
    Write-Info "  docker exec -it chiro-erp-postgres-1 psql -U chiro -d chiro_crm"
}

# API Explorer
function Start-ApiExplorer {
    Write-ProductivityHeader "API Explorer & Testing Tools"
    
    Write-Task "Launching API documentation and testing tools"
    
    Write-Success "API Explorer ready!"
    Write-Info "Available endpoints:"
    Write-Info "  - Swagger UI: http://localhost:8080/q/swagger-ui"
    Write-Info "  - OpenAPI Spec: http://localhost:8080/q/openapi"
    Write-Info "  - Dev UI: http://localhost:8080/q/dev"
    Write-Info "  - Health: http://localhost:8080/q/health"
    Write-Info "  - Metrics: http://localhost:8080/q/metrics"
    
    # Launch default browser
    try {
        Start-Process "http://localhost:8080/q/dev"
        Write-Info "Opened Dev UI in browser"
    }
    catch {
        Write-Info "Please manually open: http://localhost:8080/q/dev"
    }
}

# Code quality suite
function Invoke-CodeQuality {
    Write-ProductivityHeader "Comprehensive Code Quality Analysis"
    
    Write-Task "Running dependency validation"
    .\run-comprehensive-validation.ps1 -SkipFixes
    
    Write-Task "Running REST convention enforcement"
    .\enforce-rest-conventions.ps1 -DryRun
    
    Write-Task "Verifying file structure consistency"
    .\verify-service-structure-consistency.ps1 -Detailed
    
    Write-Task "Checking structural consistency"
    .\check-structural-consistency.ps1
    
    Write-Task "Running code formatting"
    .\gradlew.bat spotlessApply
    
    Write-Task "Running static analysis"
    .\gradlew.bat detekt
    
    Write-Task "Running security analysis"
    .\gradlew.bat dependencyCheckAnalyze
    
    Write-Success "Code quality analysis complete!"
}

# Performance profiling
function Start-PerformanceProfiling {
    Write-ProductivityHeader "Performance Profiling Tools"
    
    Write-Task "Starting performance monitoring"
    
    Write-Success "Performance tools ready!"
    Write-Info "Monitoring endpoints:"
    Write-Info "  - Prometheus: http://localhost:9090"
    Write-Info "  - Grafana: http://localhost:3000 (admin/admin)"
    Write-Info "  - JVM Metrics: http://localhost:8080/q/metrics"
    Write-Info "  - Application Health: http://localhost:8080/q/health"
    
    Write-Info "Load testing with JMeter or K6:"
    Write-Info "  k6 run --vus 10 --duration 30s performance-test.js"
}

# Debug assistance
function Start-DebugAssist {
    Write-ProductivityHeader "Debug Assistance Tools"
    
    Write-Task "Setting up debug environment"
    
    Write-Success "Debug tools ready!"
    Write-Info "Debug endpoints:"
    Write-Info "  - Dev UI: http://localhost:8080/q/dev"
    Write-Info "  - Jaeger Tracing: http://localhost:16686"
    Write-Info "  - Kafka UI: http://localhost:8081"
    Write-Info "  - Application Logs: docker-compose logs -f"
    
    Write-Info "Debug commands:"
    Write-Info "  .\scripts\dev.ps1 -Action logs -Service <service-name> -Follow"
    Write-Info "  docker exec -it <container> /bin/bash"
}

# Infrastructure validation
function Test-InfrastructureConsistency {
    Write-ProductivityHeader "Infrastructure Validation & Consistency Check"
    
    Write-Task "Verifying service structure consistency"
    try {
        .\verify-service-structure-consistency.ps1 -Detailed
        Write-Success "Service structure verification completed"
    }
    catch {
        Write-Host "❌ Service structure issues found: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Task "Checking structural consistency"
    try {
        .\check-structural-consistency.ps1
        Write-Success "Structural consistency check completed"
    }
    catch {
        Write-Host "❌ Structural issues found: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Task "Validating Kubernetes deployment structure"
    $kubernetesValidation = @{
        "Services" = @()
        "Issues"   = @()
    }
    
    # Check if all consolidated services have Kubernetes manifests
    $expectedServices = @("core-business-service", "operations-management-service", "customer-relations-service", "platform-services", "workforce-management-service")
    
    foreach ($service in $expectedServices) {
        $manifestPath = "kubernetes/services/$service"
        if (Test-Path $manifestPath) {
            $deploymentFile = "$manifestPath/$service-deployment.yaml"
            $serviceFile = "$manifestPath/$service-service.yaml"
            
            if ((Test-Path $deploymentFile) -and (Test-Path $serviceFile)) {
                $kubernetesValidation.Services += $service
                Write-Host "✅ $service - Kubernetes manifests OK" -ForegroundColor Green
            }
            else {
                $kubernetesValidation.Issues += "$service - Missing deployment or service manifest"
                Write-Host "❌ $service - Missing manifests" -ForegroundColor Red
            }
        }
        else {
            $kubernetesValidation.Issues += "$service - No Kubernetes directory"
            Write-Host "❌ $service - No Kubernetes directory" -ForegroundColor Red
        }
    }
    
    Write-Task "Validating Docker deployment structure"
    $dockerValidation = @{
        "Services" = @()
        "Issues"   = @()
    }
    
    foreach ($service in $expectedServices) {
        $servicePath = "consolidated-services/$service"
        if (Test-Path $servicePath) {
            $dockerPath = "$servicePath/docker"
            $jvmDockerfile = "$dockerPath/Dockerfile"
            $nativeDockerfile = "$dockerPath/Dockerfile.native"
            
            if (Test-Path $dockerPath) {
                $hasJvm = Test-Path $jvmDockerfile
                $hasNative = Test-Path $nativeDockerfile
                
                if ($hasJvm -and $hasNative) {
                    $dockerValidation.Services += $service
                    Write-Host "✅ $service - Docker files OK (JVM + Native)" -ForegroundColor Green
                }
                elseif ($hasJvm) {
                    $dockerValidation.Services += $service
                    Write-Host "⚠️  $service - Only JVM Dockerfile (missing native)" -ForegroundColor Yellow
                }
                else {
                    $dockerValidation.Issues += "$service - Missing Dockerfiles"
                    Write-Host "❌ $service - Missing Dockerfiles" -ForegroundColor Red
                }
            }
            else {
                $dockerValidation.Issues += "$service - No docker directory"
                Write-Host "❌ $service - No docker directory" -ForegroundColor Red
            }
        }
    }
    
    Write-Task "Validating GraalVM configuration"
    $graalvmValidation = @{
        "Services" = @()
        "Issues"   = @()
    }
    
    # Check gradle.properties for GraalVM settings
    if (Test-Path "gradle.properties") {
        $gradleProps = Get-Content "gradle.properties" -Raw
        if ($gradleProps -match "quarkus\.native\.container-build=true") {
            Write-Host "✅ Global GraalVM configuration found" -ForegroundColor Green
        }
        else {
            Write-Host "❌ Missing global GraalVM configuration" -ForegroundColor Red
            $graalvmValidation.Issues += "Missing global GraalVM configuration in gradle.properties"
        }
    }
    
    # Check individual service build files
    foreach ($service in $expectedServices) {
        $buildFile = "consolidated-services/$service/build.gradle.kts"
        if (Test-Path $buildFile) {
            $buildContent = Get-Content $buildFile -Raw
            if ($buildContent -match "buildNative") {
                $graalvmValidation.Services += $service
                Write-Host "✅ $service - GraalVM build task configured" -ForegroundColor Green
            }
            else {
                $graalvmValidation.Issues += "$service - Missing GraalVM build configuration"
                Write-Host "❌ $service - Missing GraalVM build task" -ForegroundColor Red
            }
        }
    }
    
    Write-Success "Infrastructure validation completed!"
    Write-Info "Summary:"
    Write-Info "  - Kubernetes services: $($kubernetesValidation.Services.Count)/$($expectedServices.Count)"
    Write-Info "  - Docker configurations: $($dockerValidation.Services.Count)/$($expectedServices.Count)"
    Write-Info "  - GraalVM configurations: $($graalvmValidation.Services.Count)/$($expectedServices.Count)"
    
    if ($kubernetesValidation.Issues.Count -gt 0 -or $dockerValidation.Issues.Count -gt 0 -or $graalvmValidation.Issues.Count -gt 0) {
        Write-Host "`n⚠️  Issues found:" -ForegroundColor Yellow
        ($kubernetesValidation.Issues + $dockerValidation.Issues + $graalvmValidation.Issues) | ForEach-Object {
            Write-Host "  - $_" -ForegroundColor Red
        }
    }
}

# GraalVM native build
function Invoke-GraalVMBuild {
    Write-ProductivityHeader "GraalVM Native Build"
    
    if ($ServiceName) {
        Write-Task "Building native executable for $ServiceName"
        try {
            .\gradlew.bat ":consolidated-services:$ServiceName:buildNative"
            Write-Success "Native build completed for $ServiceName"
            
            Write-Task "Building native Docker image for $ServiceName"
            .\gradlew.bat ":consolidated-services:$ServiceName:dockerBuildNative"
            Write-Success "Native Docker image built for $ServiceName"
        }
        catch {
            Write-Host "❌ Native build failed: $($_.Exception.Message)" -ForegroundColor Red
            return
        }
    }
    else {
        Write-Task "Building native executables for all services"
        $services = @("core-business-service", "operations-management-service", "customer-relations-service", "platform-services", "workforce-management-service")
        
        foreach ($service in $services) {
            Write-Task "Building $service native executable"
            try {
                .\gradlew.bat ":consolidated-services:$service:buildNative"
                Write-Success "✅ $service native build completed"
            }
            catch {
                Write-Host "❌ $service native build failed" -ForegroundColor Red
                continue
            }
        }
        
        Write-Task "Building native Docker images"
        foreach ($service in $services) {
            Write-Task "Building $service native Docker image"
            try {
                .\gradlew.bat ":consolidated-services:$service:dockerBuildNative"
                Write-Success "✅ $service native Docker image built"
            }
            catch {
                Write-Host "❌ $service native Docker build failed" -ForegroundColor Red
            }
        }
    }
    
    Write-Success "GraalVM native builds completed!"
    Write-Info "Performance benefits:"
    Write-Info "  - Startup time: 10-20x faster (0.1-0.5s vs 3-8s)"
    Write-Info "  - Memory usage: 60-80% reduction"
    Write-Info "  - Container size: 50-75% smaller"
}

# Native deployment
function Invoke-NativeDeployment {
    Write-ProductivityHeader "Native Container Deployment"
    
    Write-Task "Validating native images"
    $services = @("core-business-service", "operations-management-service", "customer-relations-service", "platform-services", "workforce-management-service")
    $availableImages = @()
    
    foreach ($service in $services) {
        try {
            $imageCheck = docker images "$service" --filter "reference=*native*" --format "table {{.Repository}}:{{.Tag}}" 2>$null
            if ($imageCheck) {
                $availableImages += $service
                Write-Host "✅ $service native image available" -ForegroundColor Green
            }
            else {
                Write-Host "❌ $service native image not found" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "❌ Error checking $service image" -ForegroundColor Red
        }
    }
    
    if ($availableImages.Count -eq 0) {
        Write-Host "❌ No native images found. Run graalvm-build first." -ForegroundColor Red
        return
    }
    
    Write-Task "Deploying native containers to Kubernetes"
    try {
        # Deploy with native images
        .\k8s-deploy.ps1 -Command deploy -Environment dev -Native
        Write-Success "Native containers deployed to Kubernetes"
        
        Write-Task "Waiting for native containers to start"
        Start-Sleep 30
        
        Write-Task "Checking deployment status"
        .\k8s-deploy.ps1 -Command status -Environment dev
        
        Write-Success "Native deployment completed!"
        Write-Info "Native containers are running with:"
        Write-Info "  - Sub-second startup times"
        Write-Info "  - Minimal memory footprint"
        Write-Info "  - Optimized resource usage"
    }
    catch {
        Write-Host "❌ Native deployment failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Main execution
switch ($Action) {
    "setup-workspace" { Set-DevelopmentWorkspace }
    "generate-service" { New-ServiceFromTemplate -ServiceName $ServiceName }
    "run-tests" { Invoke-SmartTests }
    "database-tools" { Invoke-DatabaseTools }
    "api-explorer" { Start-ApiExplorer }
    "code-quality" { Invoke-CodeQuality }
    "performance-profile" { Start-PerformanceProfiling }
    "debug-assist" { Start-DebugAssist }
    "validate-infrastructure" { Test-InfrastructureConsistency }
    "graalvm-build" { Invoke-GraalVMBuild }
    "native-deploy" { Invoke-NativeDeployment }
}

Write-Host "`n🎉 Action '$Action' completed!" -ForegroundColor Green

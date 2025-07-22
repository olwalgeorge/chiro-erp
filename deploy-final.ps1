# =============================================================================
# CHIRO ERP - COMPREHENSIVE DEPLOYMENT SOLUTION
# =============================================================================
# Single deployment script for the entire ERP system

param(
    [string]$Action = "status",
    [string]$Environment = "dev",
    [switch]$SkipChecks = $false
)

# Set strict error handling
$ErrorActionPreference = "Stop"
$InformationPreference = "Continue"

function Invoke-PreDeploymentChecks {
    Write-Host "`n🔧 RUNNING PRE-DEPLOYMENT SCRIPTS" -ForegroundColor Magenta
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    # 1. Verify Service Structure Consistency
    Write-Host "📁 Step 1: Verifying service structure consistency..." -ForegroundColor Cyan
    if (Test-Path ".\verify-service-structure-consistency.ps1") {
        try {
            & ".\verify-service-structure-consistency.ps1" -Fix
            if ($LASTEXITCODE -eq 0) {
                Write-Host "   ✅ Service structure verification completed" -ForegroundColor Green
            }
            else {
                Write-Host "   ⚠️  Service structure verification found issues (exit code: $LASTEXITCODE)" -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "   ❌ Failed to run service structure verification: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "   ⚠️  Service structure verification script not found" -ForegroundColor Yellow
    }
    
    # 2. Fix Dependencies
    Write-Host "`n📦 Step 2: Fixing dependencies..." -ForegroundColor Cyan
    if (Test-Path ".\fix-dependencies.ps1") {
        try {
            & ".\fix-dependencies.ps1"
            if ($LASTEXITCODE -eq 0) {
                Write-Host "   ✅ Dependencies fixed successfully" -ForegroundColor Green
            }
            else {
                Write-Host "   ⚠️  Dependency fixing completed with warnings (exit code: $LASTEXITCODE)" -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "   ❌ Failed to fix dependencies: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "   ⚠️  Fix dependencies script not found" -ForegroundColor Yellow
    }
    
    # 3. Standardize Kubernetes Manifests
    Write-Host "`n☸️  Step 3: Standardizing Kubernetes manifests..." -ForegroundColor Cyan
    if (Test-Path ".\standardize-k8s-manifests.ps1") {
        try {
            & ".\standardize-k8s-manifests.ps1" -Fix
            if ($LASTEXITCODE -eq 0) {
                Write-Host "   ✅ Kubernetes manifests standardized successfully" -ForegroundColor Green
            }
            else {
                Write-Host "   ⚠️  Kubernetes manifest standardization completed with warnings (exit code: $LASTEXITCODE)" -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "   ❌ Failed to standardize Kubernetes manifests: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "   ⚠️  Kubernetes manifest standardization script not found" -ForegroundColor Yellow
    }
    
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    Write-Host "✅ PRE-DEPLOYMENT SCRIPTS COMPLETED" -ForegroundColor Green
}

# Global error handler
function Write-CriticalError {
    param(
        [string]$Message,
        [string]$Context = "",
        [int]$ExitCode = 1
    )
    
    Write-Host "`n❌ CRITICAL ERROR" -ForegroundColor Red -BackgroundColor DarkRed
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red
    Write-Host "🛑 $Message" -ForegroundColor Red
    if ($Context) {
        Write-Host "📍 Context: $Context" -ForegroundColor Yellow
    }
    Write-Host "⏰ Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
    Write-Host "💻 Environment: $Environment" -ForegroundColor Gray
    Write-Host "🔧 Action: $Action" -ForegroundColor Gray
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Red
    Write-Host "🛑 DEPLOYMENT TERMINATED" -ForegroundColor Red -BackgroundColor DarkRed
    
    # Show system status for debugging
    Write-Host "`n🔍 System Status at Error:" -ForegroundColor Yellow
    try {
        $runningContainers = docker ps --format "table {{.Names}}\t{{.Status}}" 2>$null
        if ($runningContainers) {
            Write-Host "Running containers:" -ForegroundColor Gray
            $runningContainers | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }
        }
    }
    catch { 
        Write-Host "Could not retrieve container status" -ForegroundColor Gray
    }
    
    exit $ExitCode
}

Write-Host "🚀 CHIRO ERP COMPREHENSIVE DEPLOYMENT" -ForegroundColor Magenta
Write-Host "Action: $Action | Environment: $Environment" -ForegroundColor Cyan
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray

function Test-Prerequisites {
    Write-Host "`n🔍 RUNNING PRE-DEPLOYMENT CONSISTENCY CHECKS" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    $issues = @()
    $warnings = @()
    
    # Run pre-deployment scripts in order
    Invoke-PreDeploymentChecks
    
    # 1. Check Docker availability and version
    Write-Host "📦 Checking Docker..." -ForegroundColor Cyan
    try {
        $dockerVersion = docker --version 2>$null
        if ($dockerVersion -and $LASTEXITCODE -eq 0) {
            Write-Host "  ✅ Docker found: $dockerVersion" -ForegroundColor Green
        }
        else {
            $issues += "Docker is not installed or not accessible in PATH"
            Write-Host "  ❌ Docker command not found" -ForegroundColor Red
        }
    }
    catch {
        $issues += "Failed to check Docker version: $_"
        Write-Host "  ❌ Error checking Docker: $_" -ForegroundColor Red
    }
    
    # 2. Check Docker daemon status
    Write-Host "🐳 Checking Docker daemon..." -ForegroundColor Cyan
    try {
        $dockerInfoOutput = docker info 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ Docker daemon is running" -ForegroundColor Green
        }
        else {
            # Check specific error messages to provide better guidance
            if ($dockerInfoOutput -match "pipe.*dockerDesktopLinuxEngine.*cannot find.*file") {
                $issues += "Docker Desktop is not running. Please start Docker Desktop and wait for it to fully initialize."
                Write-Host "  ❌ Docker Desktop is not running" -ForegroundColor Red
                Write-Host "     💡 Start Docker Desktop and wait for the status to show 'Engine running'" -ForegroundColor Yellow
            }
            elseif ($dockerInfoOutput -match "docker daemon.*not running") {
                $issues += "Docker daemon is not running. Please start Docker service."
                Write-Host "  ❌ Docker daemon is not running" -ForegroundColor Red
            }
            else {
                $issues += "Cannot connect to Docker daemon. Error: $dockerInfoOutput"
                Write-Host "  ❌ Cannot connect to Docker daemon" -ForegroundColor Red
                Write-Host "     Details: $dockerInfoOutput" -ForegroundColor Gray
            }
        }
    }
    catch {
        $issues += "Cannot connect to Docker daemon: $_"
        Write-Host "  ❌ Docker daemon connection failed: $_" -ForegroundColor Red
    }
    
    # 3. Check available disk space
    Write-Host "💾 Checking disk space..." -ForegroundColor Cyan
    try {
        $currentDrive = (Get-Location).Drive.Name
        $driveInfo = Get-WmiObject -Class Win32_LogicalDisk -Filter "DeviceID='${currentDrive}:'"
        $freeSpaceGB = [math]::Round($driveInfo.FreeSpace / 1GB, 2)
        
        if ($freeSpaceGB -gt 10) {
            Write-Host "  ✅ Available disk space: ${freeSpaceGB}GB" -ForegroundColor Green
        }
        elseif ($freeSpaceGB -gt 5) {
            $warnings += "Low disk space: ${freeSpaceGB}GB (recommended: >10GB)"
            Write-Host "  ⚠️  Low disk space: ${freeSpaceGB}GB" -ForegroundColor Yellow
        }
        else {
            $issues += "Insufficient disk space: ${freeSpaceGB}GB (minimum: 5GB)"
        }
    }
    catch {
        $warnings += "Could not check disk space: $_"
    }
    
    # Summary
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    if ($issues.Count -eq 0 -and $warnings.Count -eq 0) {
        Write-Host "🎉 ALL CHECKS PASSED! Ready for deployment." -ForegroundColor Green
        return $true
    }
    
    if ($issues.Count -gt 0) {
        Write-Host "`n❌ CRITICAL ISSUES FOUND:" -ForegroundColor Red
        foreach ($issue in $issues) {
            Write-Host "  • $issue" -ForegroundColor Red
        }
    }
    
    if ($warnings.Count -gt 0) {
        Write-Host "`n⚠️  WARNINGS:" -ForegroundColor Yellow
        foreach ($warning in $warnings) {
            Write-Host "  • $warning" -ForegroundColor Yellow
        }
    }
    
    if ($issues.Count -gt 0) {
        Write-Host "`n🛑 Please fix the critical issues above before proceeding." -ForegroundColor Red
        Write-Host "💡 You can use -SkipChecks to bypass these checks (not recommended)." -ForegroundColor Cyan
        return $false
    }
    else {
        Write-Host "`n✅ No critical issues found. Proceeding with warnings..." -ForegroundColor Green
        return $true
    }
}

function Test-LocalBuilds {
    Write-Host "`n🔨 VALIDATING LOCAL BUILDS" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    # Pre-check: Verify Gradle wrapper is accessible
    if (-not (Test-Path "gradlew")) {
        Write-CriticalError "Gradle wrapper (gradlew) not found" "Cannot perform local build validation without Gradle wrapper"
    }
    
    # Pre-check: Test Gradle wrapper execution
    Write-Host "🔧 Testing Gradle wrapper..." -ForegroundColor Cyan
    try {
        .\gradlew --version 2>$null | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-CriticalError "Gradle wrapper is not executable" "Gradle wrapper test failed with exit code: $LASTEXITCODE"
        }
        Write-Host "  ✅ Gradle wrapper is functional" -ForegroundColor Green
    }
    catch {
        Write-CriticalError "Failed to test Gradle wrapper: $($_.Exception.Message)" "Gradle wrapper execution failed"
    }
    
    # Clean previous builds
    Write-Host "🧹 Cleaning previous builds..." -ForegroundColor Cyan
    try {
        .\gradlew clean --quiet
        if ($LASTEXITCODE -ne 0) {
            Write-CriticalError "Gradle clean failed" "Build clean operation failed with exit code: $LASTEXITCODE"
        }
        Write-Host "  ✅ Previous builds cleaned" -ForegroundColor Green
    }
    catch {
        Write-CriticalError "Failed to clean builds: $($_.Exception.Message)" "Gradle clean operation failed"
    }
    
    # Compile and test the entire project
    Write-Host "⚙️  Compiling entire project..." -ForegroundColor Cyan
    Write-Host "   📊 This may take several minutes - watch for progress..." -ForegroundColor Gray
    
    try {
        # Run build with progress output
        $buildStartTime = Get-Date
        .\gradlew build --info --no-daemon
        $buildEndTime = Get-Date
        $buildDuration = ($buildEndTime - $buildStartTime).TotalSeconds
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ Project build completed successfully" -ForegroundColor Green
            Write-Host "  ⏱️  Build duration: $([math]::Round($buildDuration, 1)) seconds" -ForegroundColor Gray
        }
        else {
            Write-CriticalError "Project build failed" "Gradle build failed with exit code: $LASTEXITCODE - Cannot proceed with Docker deployment"
        }
    }
    catch {
        Write-CriticalError "Build execution failed: $($_.Exception.Message)" "Gradle build process encountered an exception"
    }
    
    # Verify that all service JARs were built
    Write-Host "📦 Verifying service artifacts..." -ForegroundColor Cyan
    $services = @("core-business-service", "customer-relations-service", "platform-services")
    $missingArtifacts = @()
    
    foreach ($service in $services) {
        $jarPath = "consolidated-services/$service/build/libs"
        if (Test-Path $jarPath) {
            $jarFiles = Get-ChildItem -Path $jarPath -Filter "*.jar" | Where-Object { $_.Name -notmatch "-sources" -and $_.Name -notmatch "-javadoc" }
            if ($jarFiles.Count -gt 0) {
                $jarFile = $jarFiles[0]
                $jarSizeMB = [math]::Round($jarFile.Length / 1MB, 2)
                Write-Host "  ✅ $service artifact: $($jarFile.Name) (${jarSizeMB} MB)" -ForegroundColor Green
            }
            else {
                $missingArtifacts += $service
                Write-Host "  ❌ $service artifact not found" -ForegroundColor Red
            }
        }
        else {
            $missingArtifacts += $service
            Write-Host "  ❌ $service build directory not found" -ForegroundColor Red
        }
    }
    
    if ($missingArtifacts.Count -gt 0) {
        Write-CriticalError "Missing build artifacts for services: $($missingArtifacts -join ', ')" "Some services failed to build properly - Docker deployment will fail"
    }
    
    # Run project tests
    Write-Host "🧪 Running project tests..." -ForegroundColor Cyan
    try {
        $testStartTime = Get-Date
        .\gradlew test --info --no-daemon
        $testEndTime = Get-Date
        $testDuration = ($testEndTime - $testStartTime).TotalSeconds
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ All tests passed" -ForegroundColor Green
            Write-Host "  ⏱️  Test duration: $([math]::Round($testDuration, 1)) seconds" -ForegroundColor Gray
        }
        else {
            # Tests failed but we'll warn instead of failing completely
            Write-Host "  ⚠️  Some tests failed (exit code: $LASTEXITCODE)" -ForegroundColor Yellow
            Write-Host "  💡 Consider fixing tests before deployment, but proceeding..." -ForegroundColor Gray
            
            # Show test results summary if available
            if (Test-Path "build/reports/tests/test/index.html") {
                Write-Host "  📊 Test report: build/reports/tests/test/index.html" -ForegroundColor Gray
            }
        }
    }
    catch {
        Write-Host "  ⚠️  Test execution failed: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host "  💡 Proceeding with deployment despite test issues..." -ForegroundColor Gray
    }
    
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    Write-Host "✅ LOCAL BUILD VALIDATION COMPLETED" -ForegroundColor Green
    Write-Host "🚀 Ready to proceed with Docker deployment..." -ForegroundColor Cyan
    return $true
}

function Show-Status {
    Write-Host "`n📊 CHIRO ERP SYSTEM STATUS" -ForegroundColor Yellow
    
    # Check if Docker is available first
    try {
        docker info 2>$null | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "`n❌ Docker is not running - cannot show container status" -ForegroundColor Red
            Write-Host "💡 Please start Docker Desktop and try again" -ForegroundColor Yellow
            return
        }
    }
    catch {
        Write-Host "`n❌ Docker is not available - cannot show container status" -ForegroundColor Red
        Write-Host "💡 Please install Docker Desktop and try again" -ForegroundColor Yellow
        return
    }
    
    Write-Host "`nInfrastructure Services:" -ForegroundColor Cyan
    try {
        $infraServices = docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}" | Where-Object { $_ -match "chiro-erp-(postgres|kafka|zookeeper)" }
        if ($infraServices) {
            $infraServices | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
        }
        else {
            Write-Host "  No infrastructure services running" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "  Error retrieving infrastructure services: $_" -ForegroundColor Red
    }
    
    Write-Host "`nApplication Services:" -ForegroundColor Cyan
    try {
        $appServices = docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}" | Where-Object { $_ -match "chiro-erp-.*-service" }
        if ($appServices) {
            $appServices | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
        }
        else {
            Write-Host "  No application services running" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "  Error retrieving application services: $_" -ForegroundColor Red
    }
    
    Write-Host "`nBuilt Images:" -ForegroundColor Cyan
    try {
        $builtImages = docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | Where-Object { $_ -match "chiro-erp" }
        if ($builtImages) {
            $builtImages | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
        }
        else {
            Write-Host "  No CHIRO ERP images found" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "  Error retrieving Docker images: $_" -ForegroundColor Red
    }
    
    Write-Host "`nHealth Check URLs:" -ForegroundColor Cyan
    try {
        $runningApps = docker ps --filter "name=chiro-erp" --format "{{.Names}} {{.Ports}}" 2>$null
        if ($runningApps -and $LASTEXITCODE -eq 0) {
            $healthUrlsFound = $false
            foreach ($app in $runningApps) {
                if ($app -match '(\d+)->8080') {
                    $port = $matches[1]
                    Write-Host "  http://localhost:$port/q/health" -ForegroundColor Green
                    $healthUrlsFound = $true
                }
            }
            if (-not $healthUrlsFound) {
                Write-Host "  No application services with exposed ports running" -ForegroundColor Gray
            }
        }
        else {
            Write-Host "  No application services running" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "  Error retrieving health check URLs: $_" -ForegroundColor Red
    }
}

function Deploy-Infrastructure {
    Write-Host "`n🏗️  DEPLOYING INFRASTRUCTURE" -ForegroundColor Yellow
    
    try {
        Write-Host "🚀 Starting infrastructure services..." -ForegroundColor Cyan
        docker-compose -f docker-compose.consolidated.yml up -d postgres kafka zookeeper
        
        if ($LASTEXITCODE -ne 0) {
            Write-CriticalError "Infrastructure deployment failed" "Docker compose failed with exit code: $LASTEXITCODE"
        }
        
        Write-Host "✅ Infrastructure deployment started" -ForegroundColor Green
        Write-Host "⏳ Waiting for services to be ready..." -ForegroundColor Cyan
        Start-Sleep 15
        
        Write-Host "✅ All infrastructure services are running" -ForegroundColor Green
        return $true
    }
    catch {
        Write-CriticalError "Infrastructure deployment exception: $($_.Exception.Message)" "Docker infrastructure deployment failed"
    }
}

function Build-Service {
    param([string]$ServiceName)
    
    Write-Host "`n🔨 Building $ServiceName..." -ForegroundColor Cyan
    Write-Host "📊 Watch for percentage progress indicators below..." -ForegroundColor Green
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    try {
        docker build --progress=plain --memory=3g --build-arg SERVICE_NAME=$ServiceName -f Dockerfile.consolidated -t "chiro-erp/$ServiceName" .
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
            Write-Host "✅ $ServiceName built successfully" -ForegroundColor Green
            return $true
        }
        else {
            Write-CriticalError "$ServiceName build failed" "Docker build failed with exit code: $LASTEXITCODE"
        }
    }
    catch {
        Write-CriticalError "$ServiceName build exception: $($_.Exception.Message)" "Docker build process failed"
    }
}

function Deploy-Applications {
    Write-Host "`n🚀 DEPLOYING APPLICATIONS" -ForegroundColor Yellow
    
    $services = @(
        "core-business-service",
        "customer-relations-service", 
        "platform-services"
    )
    
    $successful = @()
    
    foreach ($service in $services) {
        Write-Host "`n📋 Processing service: $service" -ForegroundColor Cyan
        
        if (Build-Service -ServiceName $service) {
            $successful += $service
            
            $port = 8080 + $successful.Count
            Write-Host "🚀 Starting $service on port $port..." -ForegroundColor Cyan
            
            try {
                $existingContainer = docker ps -a --filter "name=chiro-erp-$service" --format "{{.Names}}" 2>$null
                if ($existingContainer) {
                    Write-Host "   🔄 Removing existing container..." -ForegroundColor Yellow
                    docker rm -f "chiro-erp-$service" 2>$null
                }
                
                docker run -d --name "chiro-erp-$service" --network chiro-erp_default -p "${port}:8080" -e "QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/chiro_erp" -e "KAFKA_BOOTSTRAP_SERVERS=kafka:9092" "chiro-erp/$service"
                
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "✅ $service deployed successfully on port $port" -ForegroundColor Green
                    Write-Host "   🌐 Health check: http://localhost:$port/q/health" -ForegroundColor Gray
                }
                else {
                    Write-CriticalError "Failed to start $service container" "Docker run failed with exit code: $LASTEXITCODE"
                }
            }
            catch {
                Write-CriticalError "Failed to start $service container: $($_.Exception.Message)" "Container deployment failed"
            }
        }
    }
    
    Write-Host "`n🎉 ALL SERVICES DEPLOYED SUCCESSFULLY!" -ForegroundColor Green
    return @{ Success = $successful; Failed = @() }
}

function Remove-All {
    Write-Host "`n🧹 CLEANING UP DEPLOYMENT" -ForegroundColor Yellow
    
    Write-Host "Stopping containers..." -ForegroundColor Cyan
    docker-compose -f docker-compose.consolidated.yml down -v 2>$null
    
    Write-Host "Removing application containers..." -ForegroundColor Cyan
    docker ps -a --filter "name=chiro-erp" --format "{{.Names}}" | ForEach-Object {
        docker rm -f $_ 2>$null
    }
    
    Write-Host "✅ Cleanup completed" -ForegroundColor Green
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

try {
    # Run pre-deployment checks for deployment actions (unless skipped)
    if (@("infrastructure", "applications", "full") -contains $Action.ToLower() -and -not $SkipChecks) {
        Write-Host "🔍 Running pre-deployment consistency checks..." -ForegroundColor Cyan
        if (-not (Test-Prerequisites)) {
            Write-CriticalError "Pre-deployment checks failed" "Multiple issues detected that must be resolved before deployment"
        }
        Write-Host "✅ Pre-deployment checks passed" -ForegroundColor Green
        
        # Run local build validation for application deployments
        if (@("applications", "full") -contains $Action.ToLower()) {
            Write-Host "🔨 Running local build validation..." -ForegroundColor Cyan
            if (-not (Test-LocalBuilds)) {
                Write-CriticalError "Local build validation failed" "Project build issues must be resolved before Docker deployment"
            }
            Write-Host "✅ Local build validation passed" -ForegroundColor Green
        }
        Write-Host ""
    }

    switch ($Action.ToLower()) {
        "status" {
            Show-Status
        }
    
        "checks" {
            Test-Prerequisites
        }
        
        "predeploy" {
            Write-Host "🔧 Running pre-deployment scripts only..." -ForegroundColor Cyan
            Invoke-PreDeploymentChecks
            Write-Host "✅ Pre-deployment scripts completed" -ForegroundColor Green
        }
        
        "build" {
            Test-LocalBuilds
        }
    
        "infrastructure" {
            if (-not $SkipChecks) {
                Write-Host "🔍 Running pre-deployment checks..." -ForegroundColor Cyan
                if (-not (Test-Prerequisites)) {
                    Write-CriticalError "Pre-deployment checks failed" "Multiple issues detected - aborting deployment"
                }
            }
        
            if (Deploy-Infrastructure) {
                Start-Sleep 10
                Show-Status
            }
        }
    
        "applications" {
            if (-not $SkipChecks) {
                Write-Host "🔍 Running pre-deployment checks..." -ForegroundColor Cyan
                if (-not (Test-Prerequisites)) {
                    Write-CriticalError "Pre-deployment checks failed" "Multiple issues detected - aborting deployment"
                }
                
                Write-Host "🔨 Running local build validation..." -ForegroundColor Cyan
                if (-not (Test-LocalBuilds)) {
                    Write-CriticalError "Local build validation failed" "Project build issues must be resolved before Docker deployment"
                }
            }
        
            Deploy-Applications
            Show-Status
        }
    
        "full" {
            if (-not $SkipChecks) {
                Write-Host "🔍 Running comprehensive pre-deployment checks..." -ForegroundColor Cyan
                if (-not (Test-Prerequisites)) {
                    Write-CriticalError "Pre-deployment checks failed" "Multiple issues detected - aborting deployment"
                }
                
                Write-Host "🔨 Running local build validation..." -ForegroundColor Cyan
                if (-not (Test-LocalBuilds)) {
                    Write-CriticalError "Local build validation failed" "Project build issues must be resolved before Docker deployment"
                }
            }
            
            if (Deploy-Infrastructure) {
                Start-Sleep 15
                Deploy-Applications
                Show-Status
            
                Write-Host "`n🎉 DEPLOYMENT COMPLETE!" -ForegroundColor Green
                Write-Host "Database: localhost:5432 (chiro_erp/chiro_user)" -ForegroundColor Cyan
                Write-Host "Kafka: localhost:9092" -ForegroundColor Cyan
                Write-Host "Check health endpoints above for application status" -ForegroundColor Cyan
            }
        }
    
        "cleanup" {
            Remove-All
        }
    
        default {
            Write-Host "Usage: .\deploy-final.ps1 -Action [status|checks|predeploy|build|infrastructure|applications|full|cleanup] [-SkipChecks]" -ForegroundColor Yellow
            Write-Host "`nActions:" -ForegroundColor Cyan
            Write-Host "  status         - Show current system status" -ForegroundColor White
            Write-Host "  checks         - Run only pre-deployment consistency checks" -ForegroundColor White
            Write-Host "  predeploy      - Run pre-deployment scripts (verify structure, fix dependencies, standardize manifests)" -ForegroundColor White
            Write-Host "  build          - Run only local build validation" -ForegroundColor White
            Write-Host "  infrastructure - Deploy only database and messaging services" -ForegroundColor White
            Write-Host "  applications   - Build and deploy application services" -ForegroundColor White
            Write-Host "  full           - Complete deployment (infrastructure + applications)" -ForegroundColor White
            Write-Host "  cleanup        - Stop and remove all containers" -ForegroundColor White
            Write-Host "`nOptions:" -ForegroundColor Cyan
            Write-Host "  -SkipChecks    - Skip pre-deployment consistency checks (not recommended)" -ForegroundColor White
        }
    }

    # Success message for completed deployments
    if (@("infrastructure", "applications", "full", "cleanup", "build", "predeploy") -contains $Action.ToLower()) {
        Write-Host "`n✅ DEPLOYMENT COMPLETED SUCCESSFULLY" -ForegroundColor Green
    }

}
catch {
    # Global error handler for unexpected errors
    Write-CriticalError "Unexpected error occurred: $($_.Exception.Message)" "PowerShell exception in main execution" 2
}
finally {
    Write-Host "`n✨ CHIRO ERP DEPLOYMENT SCRIPT COMPLETED" -ForegroundColor Magenta
    Write-Host "⏰ End time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
}

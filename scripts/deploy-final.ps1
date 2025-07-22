# =============================================================================
# CHIRO ERP - COMPREHENSIVE DEPLOYMENT SOLUTION
# =============================================================================
# Single deployment script for the entire ERP system

param(
    [string]$Action = "status",
    [string]$Environment = "dev",
    [switch]$SkipChecks = $false,
    [switch]$Native = $false,
    [switch]$NonInteractive
)

# Set strict error handling
$ErrorActionPreference = "Stop"
$InformationPreference = "Continue"

# Interactive build type selection (only for build-related actions)
if (-not $NonInteractive -and @("infrastructure", "applications", "full", "build") -contains $Action.ToLower()) {
    Write-Host "`n🎯 BUILD TYPE SELECTION" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    Write-Host "Choose your build type:" -ForegroundColor Cyan
    Write-Host "  [1] 🚀 GraalVM Native - Faster startup, lower memory (production)" -ForegroundColor Green
    Write-Host "  [2] ☕ Standard JVM - Faster builds, easier debugging (development)" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    do {
        Write-Host "Please enter your choice [1-2]: " -NoNewline -ForegroundColor Yellow
        $choice = Read-Host
        
        switch ($choice) {
            "1" {
                $Native = $true
                Write-Host "✅ Selected: GraalVM Native compilation" -ForegroundColor Green
                Write-Host "💡 This will take longer to build but provides optimized runtime performance" -ForegroundColor Gray
                break
            }
            "2" {
                $Native = $false
                Write-Host "✅ Selected: Standard JVM compilation" -ForegroundColor Cyan
                Write-Host "💡 This will build faster and is ideal for development" -ForegroundColor Gray
                break
            }
            default {
                Write-Host "❌ Invalid choice. Please enter 1 or 2." -ForegroundColor Red
                continue
            }
        }
        break
    } while ($true)
    
    Write-Host ""
}

# Progress tracking functions
function Show-ProgressBar {
    param(
        [int]$PercentComplete,
        [string]$Activity,
        [string]$Status,
        [int]$Id = 1
    )
    Write-Progress -Id $Id -Activity $Activity -Status $Status -PercentComplete $PercentComplete
}

function Write-TimedStatus {
    param(
        [string]$Message,
        [string]$Color = "Cyan",
        [switch]$ShowElapsed
    )
    $timestamp = Get-Date -Format "HH:mm:ss"
    if ($ShowElapsed -and $script:StartTime) {
        $elapsed = (Get-Date) - $script:StartTime
        $elapsedStr = " [Elapsed: $($elapsed.ToString('mm\:ss'))]"
    } else {
        $elapsedStr = ""
    }
    Write-Host "[$timestamp]$elapsedStr $Message" -ForegroundColor $Color
}

function Start-Timer {
    $script:StartTime = Get-Date
    Write-TimedStatus "🚀 Starting deployment process..." "Green"
}

function Show-StepProgress {
    param(
        [int]$CurrentStep,
        [int]$TotalSteps,
        [string]$StepName,
        [string]$Details = ""
    )
    $percent = [math]::Round(($CurrentStep / $TotalSteps) * 100, 0)
    $progressBar = "█" * [math]::Round($percent / 5) + "░" * (20 - [math]::Round($percent / 5))
    Write-TimedStatus "[$progressBar] Step $CurrentStep/$TotalSteps ($percent%): $StepName" "Cyan" -ShowElapsed
    if ($Details) {
        Write-Host "    💡 $Details" -ForegroundColor Gray
    }
}

function Invoke-PreDeploymentChecks {
    Write-TimedStatus "🔧 RUNNING PRE-DEPLOYMENT SCRIPTS" "Magenta"
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    Show-StepProgress 1 3 "Verifying service structure consistency" "Checking project structure and consistency"
    
    # 1. Verify Service Structure Consistency
    if (Test-Path ".\verify-service-structure-consistency.ps1") {
        try {
            Write-TimedStatus "   📁 Running service structure verification..." "Cyan"
            & ".\verify-service-structure-consistency.ps1" -Fix
            if ($LASTEXITCODE -eq 0) {
                Write-TimedStatus "   ✅ Service structure verification completed" "Green"
            }
            else {
                Write-TimedStatus "   ⚠️  Service structure verification found issues (exit code: $LASTEXITCODE)" "Yellow"
            }
        }
        catch {
            Write-TimedStatus "   ❌ Failed to run service structure verification: $($_.Exception.Message)" "Red"
        }
    }
    else {
        Write-TimedStatus "   ⚠️  Service structure verification script not found" "Yellow"
    }
    
    Show-StepProgress 2 3 "Fixing dependencies" "Resolving and updating project dependencies"
    
    # 2. Fix Dependencies
    if (Test-Path ".\fix-dependencies.ps1") {
        try {
            Write-TimedStatus "   📦 Running dependency fixes..." "Cyan"
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
Write-Host "Action: $Action | Environment: $Environment | Native: $Native" -ForegroundColor Cyan
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray

# Initialize timer for full deployments
if (@("infrastructure", "applications", "full") -contains $Action.ToLower()) {
    Start-Timer
}

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
    Write-TimedStatus "🔨 VALIDATING LOCAL BUILDS" "Yellow"
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    
    Show-StepProgress 1 4 "Checking Gradle wrapper" "Verifying build system availability"
    
    # Pre-check: Verify Gradle wrapper is accessible
    if (-not (Test-Path "gradlew")) {
        Write-CriticalError "Gradle wrapper (gradlew) not found" "Cannot perform local build validation without Gradle wrapper"
    }
    
    # Pre-check: Test Gradle wrapper execution
    Write-TimedStatus "🔧 Testing Gradle wrapper..." "Cyan"
    try {
        .\gradlew --version 2>$null | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-CriticalError "Gradle wrapper is not executable" "Gradle wrapper test failed with exit code: $LASTEXITCODE"
        }
        Write-TimedStatus "  ✅ Gradle wrapper is functional" "Green"
    }
    catch {
        Write-CriticalError "Failed to test Gradle wrapper: $($_.Exception.Message)" "Gradle wrapper execution failed"
    }
    
    Show-StepProgress 2 4 "Cleaning previous builds" "Removing build artifacts and caches"
    
    # Clean previous builds
    Write-TimedStatus "🧹 Cleaning previous builds..." "Cyan"
    try {
        .\gradlew clean --quiet
        if ($LASTEXITCODE -ne 0) {
            Write-CriticalError "Gradle clean failed" "Build clean operation failed with exit code: $LASTEXITCODE"
        }
        Write-TimedStatus "  ✅ Previous builds cleaned" "Green"
    }
    catch {
        Write-CriticalError "Failed to clean builds: $($_.Exception.Message)" "Gradle clean operation failed"
    }
    
    Show-StepProgress 3 4 "Compiling project" "Building all services and dependencies"
    
    # Compile and test the entire project
    if ($Native) {
        Write-TimedStatus "⚡ Compiling entire project with GraalVM Native..." "Cyan"
        Write-TimedStatus "   🚀 GRAALVM NATIVE BUILD IN PROGRESS - This will take longer..." "Yellow"
        Write-TimedStatus "   💡 Native compilation provides faster startup and lower memory usage" "Gray"
    } else {
        Write-TimedStatus "⚙️  Compiling entire project..." "Cyan"
        Write-TimedStatus "   🔨 GRADLE BUILD IN PROGRESS - Please wait..." "Yellow"
    }
    
    try {
        # Run build with minimal output but show progress
        $buildStartTime = Get-Date
        
        # Show a simple progress indicator for Gradle build
        $gradleJob = Start-Job -ScriptBlock {
            param($workingDir, $useNative)
            Set-Location $workingDir
            if ($useNative) {
                .\gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true --quiet
            } else {
                .\gradlew build --quiet
            }
        } -ArgumentList (Get-Location).Path, $Native
        
        # Show progress while Gradle is building
        $dots = 0
        $buildType = if ($Native) { "NATIVE" } else { "JVM" }
        while ($gradleJob.State -eq "Running") {
            $elapsed = [math]::Round(((Get-Date) - $buildStartTime).TotalSeconds, 0)
            $dotString = "." * ($dots % 4)
            if ($Native) {
                Write-Host "`r   ⚡ GRAALVM BUILDING $buildType$dotString (${elapsed}s elapsed)   " -NoNewline -ForegroundColor Yellow
            } else {
                Write-Host "`r   🔨 GRADLE BUILDING $buildType$dotString (${elapsed}s elapsed)   " -NoNewline -ForegroundColor Yellow
            }
            Start-Sleep 3
            $dots++
        }
        
        Receive-Job $gradleJob | Out-Null  # Consume job output
        $buildEndTime = Get-Date
        $buildDuration = ($buildEndTime - $buildStartTime).TotalSeconds
        
        if ($gradleJob.State -eq "Completed" -and $LASTEXITCODE -eq 0) {
            if ($Native) {
                Write-Host "`r   ⚡ GRAALVM NATIVE BUILD COMPLETED ($([math]::Round($buildDuration, 1))s)                    " -ForegroundColor Green
                Write-TimedStatus "  🚀 All services compiled to native executables successfully" "Green"
            } else {
                Write-Host "`r   ✅ GRADLE BUILD COMPLETED ($([math]::Round($buildDuration, 1))s)                    " -ForegroundColor Green
                Write-TimedStatus "  📦 All services compiled successfully" "Green"
            }
        }
        else {
            if ($Native) {
                Write-Host "`r   ❌ GRAALVM NATIVE BUILD FAILED                                   " -ForegroundColor Red
                Write-CriticalError "Native compilation failed" "GraalVM native build failed - Cannot proceed with Docker deployment"
            } else {
                Write-Host "`r   ❌ GRADLE BUILD FAILED                                   " -ForegroundColor Red
                Write-CriticalError "Project build failed" "Gradle build failed - Cannot proceed with Docker deployment"
            }
        }
        
        Remove-Job $gradleJob -Force
    }
    catch {
        Write-CriticalError "Build execution failed: $($_.Exception.Message)" "Gradle build process encountered an exception"
    }
    
    Show-StepProgress 4 4 "Verifying artifacts" "Checking that all service JARs were built"
    
    # Verify that all service JARs were built
    Write-TimedStatus "📦 Verifying service artifacts..." "Cyan"
    $services = @("core-business-service", "customer-relations-service", "platform-services")
    $missingArtifacts = @()
    
    foreach ($service in $services) {
        if ($Native) {
            # For native builds, check for native executables
            $nativeExePath = "consolidated-services/$service/build"
            $nativeFiles = Get-ChildItem -Path $nativeExePath -Recurse -Filter "*-runner" -ErrorAction SilentlyContinue
            if ($nativeFiles.Count -gt 0) {
                $nativeFile = $nativeFiles[0]
                $nativeFileSizeMB = [math]::Round($nativeFile.Length / 1MB, 2)
                Write-TimedStatus "  ⚡ $service native executable: $($nativeFile.Name) (${nativeFileSizeMB} MB)" "Green"
            } else {
                $missingArtifacts += $service
                Write-TimedStatus "  ❌ $service native executable not found" "Red"
            }
        } else {
            # For JVM builds, check for JAR files
            $jarPath = "consolidated-services/$service/build/libs"
            if (Test-Path $jarPath) {
                $jarFiles = Get-ChildItem -Path $jarPath -Filter "*.jar" | Where-Object { $_.Name -notmatch "-sources" -and $_.Name -notmatch "-javadoc" }
                if ($jarFiles.Count -gt 0) {
                    $jarFile = $jarFiles[0]
                    $jarSizeMB = [math]::Round($jarFile.Length / 1MB, 2)
                    Write-TimedStatus "  ✅ $service artifact: $($jarFile.Name) (${jarSizeMB} MB)" "Green"
                }
                else {
                    $missingArtifacts += $service
                    Write-TimedStatus "  ❌ $service artifact not found" "Red"
                }
            }
            else {
                $missingArtifacts += $service
                Write-TimedStatus "  ❌ $service build directory not found" "Red"
            }
        }
    }
    
    if ($missingArtifacts.Count -gt 0) {
        Write-CriticalError "Missing build artifacts for services: $($missingArtifacts -join ', ')" "Some services failed to build properly - Docker deployment will fail"
    }
    
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
    Write-TimedStatus "✅ LOCAL BUILD VALIDATION COMPLETED" "Green"
    Write-TimedStatus "🚀 Ready to proceed with Docker deployment..." "Cyan"
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
    Write-TimedStatus "🏗️  DEPLOYING INFRASTRUCTURE" "Yellow"
    
    try {
        Write-TimedStatus "🚀 Starting database and messaging services..." "Cyan"
        Write-Host "   � Services: postgres, kafka, zookeeper" -ForegroundColor Gray
        
        docker-compose -f docker-compose.consolidated.yml up -d postgres kafka zookeeper 2>$null
        
        if ($LASTEXITCODE -ne 0) {
            Write-CriticalError "Infrastructure deployment failed" "Docker compose failed"
        }
        
        Write-TimedStatus "✅ Infrastructure services started" "Green"
        Write-TimedStatus "⏳ Waiting for initialization..." "Cyan"
        
        # Simple countdown
        for ($i = 15; $i -gt 0; $i--) {
            Write-Host "`r   ⏰ Initializing services... ${i}s remaining   " -NoNewline -ForegroundColor Yellow
            Start-Sleep 1
        }
        Write-Host "`r   ✅ Services ready                              " -ForegroundColor Green
        
        # Quick verification
        $runningServices = docker-compose -f docker-compose.consolidated.yml ps --services --filter "status=running" 2>$null
        if ($runningServices) {
            Write-TimedStatus "📊 Running: $($runningServices -join ', ')" "Green"
        }
        
        return $true
    }
    catch {
        Write-CriticalError "Infrastructure deployment exception: $($_.Exception.Message)" "Docker infrastructure deployment failed"
    }
}

function Build-Service {
    param([string]$ServiceName)
    
    Write-TimedStatus "🔨 Building Docker image for $ServiceName..." "Cyan"
    
    try {
        $buildStartTime = Get-Date
        Write-Host "   🐳 DOCKER BUILD IN PROGRESS: $ServiceName" -ForegroundColor Yellow
        
        # Run Docker build with minimal output
        $dockerJob = Start-Job -ScriptBlock {
            param($serviceName, $workingDir)
            Set-Location $workingDir
            docker build --quiet --memory=3g --build-arg SERVICE_NAME=$serviceName -f Dockerfile.consolidated -t "chiro-erp/$serviceName" .
        } -ArgumentList $ServiceName, (Get-Location).Path
        
        # Show progress while Docker is building
        $dots = 0
        while ($dockerJob.State -eq "Running") {
            $elapsed = [math]::Round(((Get-Date) - $buildStartTime).TotalSeconds, 0)
            $dotString = "." * ($dots % 4)
            Write-Host "`r   📦 DOCKER BUILDING $ServiceName$dotString (${elapsed}s elapsed)   " -NoNewline -ForegroundColor Yellow
            Start-Sleep 3
            $dots++
        }
        
        Receive-Job $dockerJob | Out-Null  # Consume job output
        $buildEndTime = Get-Date
        $buildDuration = ($buildEndTime - $buildStartTime).TotalSeconds
        
        if ($dockerJob.State -eq "Completed") {
            Write-Host "`r   ✅ DOCKER BUILD COMPLETED: $ServiceName ($([math]::Round($buildDuration, 1))s)                    " -ForegroundColor Green
            
            # Get image size
            try {
                $imageInfo = docker images "chiro-erp/$ServiceName" --format "{{.Size}}" | Select-Object -First 1
                if ($imageInfo) {
                    Write-TimedStatus "   📏 Image size: $imageInfo" "Gray"
                }
            }
            catch {
                Write-TimedStatus "   ⚠️  Could not retrieve image size" "Yellow"
            }
            
            Remove-Job $dockerJob -Force
            return $true
        }
        else {
            Write-Host "`r   ❌ DOCKER BUILD FAILED: $ServiceName                                   " -ForegroundColor Red
            Remove-Job $dockerJob -Force
            Write-CriticalError "$ServiceName Docker build failed" "Docker build process failed"
        }
    }
    catch {
        Write-CriticalError "$ServiceName build exception: $($_.Exception.Message)" "Docker build process failed"
    }
}

function Deploy-Applications {
    Write-TimedStatus "🚀 DEPLOYING APPLICATIONS" "Yellow"
    
    $services = @(
        "core-business-service",
        "customer-relations-service", 
        "platform-services"
    )
    
    $successful = @()
    $totalServices = $services.Count
    
    for ($i = 0; $i -lt $totalServices; $i++) {
        $service = $services[$i]
        $currentStep = $i + 1
        
        Write-TimedStatus "📋 [$currentStep/$totalServices] Processing: $service" "Cyan"
        
        if (Build-Service -ServiceName $service) {
            $successful += $service
            
            $port = 8080 + $successful.Count
            Write-TimedStatus "🚀 Starting container on port $port..." "Cyan"
            
            try {
                # Check and remove existing container
                $existingContainer = docker ps -a --filter "name=chiro-erp-$service" --format "{{.Names}}" 2>$null
                if ($existingContainer) {
                    Write-Host "   🔄 Removing existing container..." -ForegroundColor Yellow
                    docker rm -f "chiro-erp-$service" 2>$null
                }
                
                Write-Host "   📦 Starting container: $service -> localhost:$port" -ForegroundColor Gray
                
                docker run -d --name "chiro-erp-$service" --network chiro-erp_default -p "${port}:8080" -e "QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/chiro_erp" -e "KAFKA_BOOTSTRAP_SERVERS=kafka:9092" "chiro-erp/$service" 2>$null
                
                if ($LASTEXITCODE -eq 0) {
                    Write-TimedStatus "✅ $service deployed successfully" "Green"
                    Write-TimedStatus "   🌐 Health: http://localhost:$port/q/health" "Gray"
                    
                    # Quick container health check
                    Start-Sleep 2
                    $containerStatus = docker ps --filter "name=chiro-erp-$service" --format "{{.Status}}" 2>$null
                    if ($containerStatus -and $containerStatus -like "*Up*") {
                        Write-Host "   📊 Status: Running" -ForegroundColor Green
                    } else {
                        Write-Host "   ⚠️  Status: Unknown" -ForegroundColor Yellow
                    }
                }
                else {
                    Write-CriticalError "Failed to start $service container" "Docker run failed"
                }
            }
            catch {
                Write-CriticalError "Failed to start $service container: $($_.Exception.Message)" "Container deployment failed"
            }
        }
    }
    
    Write-TimedStatus "🎉 APPLICATION DEPLOYMENT COMPLETED" "Green"
    Write-TimedStatus "📊 Deployed: $($successful.Count)/$totalServices services" "Cyan"
    
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
            Write-TimedStatus "🚀 STARTING FULL DEPLOYMENT" "Magenta"
            Write-TimedStatus "📋 Deployment plan: Infrastructure → Applications → Verification" "Cyan"
            
            if (-not $SkipChecks) {
                Write-TimedStatus "🔍 Running comprehensive pre-deployment checks..." "Cyan"
                if (-not (Test-Prerequisites)) {
                    Write-CriticalError "Pre-deployment checks failed" "Multiple issues detected - aborting deployment"
                }
                
                Write-TimedStatus "🔨 Running local build validation..." "Cyan"
                if (-not (Test-LocalBuilds)) {
                    Write-CriticalError "Local build validation failed" "Project build issues must be resolved before Docker deployment"
                }
            }
            
            Show-StepProgress 1 3 "Deploying Infrastructure" "Setting up database and messaging services"
            if (Deploy-Infrastructure) {
                Write-TimedStatus "⏳ Allowing infrastructure to fully initialize..." "Cyan"
                Start-Sleep 15
                
                Show-StepProgress 2 3 "Deploying Applications" "Building and starting microservices"
                Deploy-Applications
                
                Show-StepProgress 3 3 "Final verification" "Checking deployment status and health"
                Show-Status
            
                Write-TimedStatus "🎉 DEPLOYMENT COMPLETE!" "Green"
                Write-TimedStatus "📊 Infrastructure endpoints:" "Cyan"
                Write-TimedStatus "   🐘 Database: localhost:5432 (chiro_erp/chiro_user)" "White"
                Write-TimedStatus "   📨 Kafka: localhost:9092" "White"
                Write-TimedStatus "💡 Check health endpoints above for application status" "Cyan"
                
                if ($script:StartTime) {
                    $totalDuration = (Get-Date) - $script:StartTime
                    Write-TimedStatus "⏱️  Total deployment time: $($totalDuration.ToString('mm\:ss'))" "Gray"
                }
            }
        }
    
        "cleanup" {
            Remove-All
        }
    
        default {
            Write-Host "Usage: .\deploy-final.ps1 -Action [status|checks|predeploy|build|infrastructure|applications|full|cleanup] [-SkipChecks] [-NonInteractive]" -ForegroundColor Yellow
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
            Write-Host "  -Native        - Force GraalVM native compilation (skips interactive selection)" -ForegroundColor White
            Write-Host "  -NonInteractive - Skip interactive prompts and use defaults" -ForegroundColor White
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

#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Complete build automation script for Chiro ERP system
.DESCRIPTION
    This script handles building, testing, and containerizing all services
.PARAMETER Action
    The action to perform: build, test, package, docker, deploy, clean, all
.PARAMETER Service
    Specific service to build (optional, defaults to all services)
.PARAMETER Environment
    Target environment: dev, staging, prod (default: dev)
.PARAMETER SkipTests
    Skip running tests during build
#>

param(
    [Parameter(Mandatory = $false)]
    [ValidateSet("build", "test", "package", "docker", "deploy", "clean", "all")]
    [string]$Action = "all",
    
    [Parameter(Mandatory = $false)]
    [string]$Service = "",
    
    [Parameter(Mandatory = $false)]
    [ValidateSet("dev", "staging", "prod")]
    [string]$Environment = "dev",
    
    [Parameter(Mandatory = $false)]
    [switch]$SkipTests,
    
    [Parameter(Mandatory = $false)]
    [switch]$Parallel
)

# =============================================================================
# CONFIGURATION
# =============================================================================

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

# Colors for output
$Colors = @{
    Success = "Green"
    Warning = "Yellow" 
    Error   = "Red"
    Info    = "Cyan"
    Header  = "Magenta"
}

# Service definitions
$Services = @(
    "api-gateway",
    "services/user-management-service",
    "services/tenant-management-service", 
    "services/sales-service",
    "services/inventory-service",
    "services/crm-service",
    "services/finance-service",
    "services/billing-service",
    "services/notifications-service",
    "services/hr-service",
    "services/procurement-service",
    "services/manufacturing-service",
    "services/project-service",
    "services/analytics-service",
    "services/fleet-service",
    "services/fieldservice-service",
    "services/repair-service",
    "services/pos-service"
)

# =============================================================================
# HELPER FUNCTIONS
# =============================================================================

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Colors[$Color]
}

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-ColorOutput "=" * 80 "Header"
    Write-ColorOutput $Message "Header"
    Write-ColorOutput "=" * 80 "Header"
    Write-Host ""
}

function Test-Prerequisites {
    Write-ColorOutput "Checking prerequisites..." "Info"
    
    # Check Docker
    try {
        docker --version | Out-Null
        Write-ColorOutput "✓ Docker is available" "Success"
    }
    catch {
        Write-ColorOutput "✗ Docker is not available or not in PATH" "Error"
        exit 1
    }
    
    # Check Docker Compose
    try {
        docker compose version | Out-Null
        Write-ColorOutput "✓ Docker Compose is available" "Success"
    }
    catch {
        Write-ColorOutput "✗ Docker Compose is not available" "Error"
        exit 1
    }
    
    # Check Gradle
    if (Test-Path "./gradlew.bat") {
        Write-ColorOutput "✓ Gradle wrapper is available" "Success"
    }
    else {
        Write-ColorOutput "✗ Gradle wrapper not found" "Error"
        exit 1
    }
    
    # Check Java
    try {
        java -version 2>&1 | Out-Null
        Write-ColorOutput "✓ Java is available" "Success"
    }
    catch {
        Write-ColorOutput "✗ Java is not available or not in PATH" "Error"
        exit 1
    }
}

function Invoke-GradleCommand {
    param([string]$Command, [string]$ServicePath = ".")
    
    Push-Location $ServicePath
    try {
        $commandArgs = $Command.Split(' ')
        if ($IsWindows -or $env:OS -eq "Windows_NT") {
            & ".\gradlew.bat" @commandArgs
        }
        else {
            & "./gradlew" @commandArgs
        }
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle command failed with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
}

function Build-Service {
    param([string]$ServicePath)
    
    $serviceName = Split-Path $ServicePath -Leaf
    Write-ColorOutput "Building service: $serviceName" "Info"
    
    if (!(Test-Path $ServicePath)) {
        Write-ColorOutput "Service path not found: $ServicePath" "Warning"
        return $false
    }
    
    try {
        $gradleArgs = "clean build"
        if ($SkipTests) {
            $gradleArgs += " -x test"
        }
        
        Invoke-GradleCommand $gradleArgs $ServicePath
        Write-ColorOutput "✓ Successfully built $serviceName" "Success"
        return $true
    }
    catch {
        Write-ColorOutput "✗ Failed to build $serviceName`: $_" "Error"
        return $false
    }
}

function Test-Service {
    param([string]$ServicePath)
    
    $serviceName = Split-Path $ServicePath -Leaf
    Write-ColorOutput "Testing service: $serviceName" "Info"
    
    if (!(Test-Path $ServicePath)) {
        Write-ColorOutput "Service path not found: $ServicePath" "Warning"
        return $false
    }
    
    try {
        Invoke-GradleCommand "test" $ServicePath
        Write-ColorOutput "✓ Tests passed for $serviceName" "Success"
        return $true
    }
    catch {
        Write-ColorOutput "✗ Tests failed for $serviceName`: $_" "Error"
        return $false
    }
}

function Build-DockerImage {
    param([string]$ServicePath)
    
    $serviceName = Split-Path $ServicePath -Leaf
    Write-ColorOutput "Building Docker image for: $serviceName" "Info"
    
    if (!(Test-Path $ServicePath)) {
        Write-ColorOutput "Service path not found: $ServicePath" "Warning"
        return $false
    }
    
    try {
        $imageName = "chiro-erp/$serviceName`:latest"
        
        Push-Location $ServicePath
        
        # Choose Dockerfile based on environment
        $dockerfile = switch ($Environment) {
            "prod" { "../Dockerfile.multi --target runtime-distroless" }
            "staging" { "../Dockerfile.multi --target runtime-alpine" }
            default { "../Dockerfile.enhanced" }
        }
        
        docker build -f $dockerfile -t $imageName .
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✓ Successfully built Docker image for $serviceName" "Success"
            
            # Security scan the image
            if ($Environment -eq "prod" -or $Environment -eq "staging") {
                Invoke-SecurityScan $imageName
            }
            
            return $true
        }
        else {
            Write-ColorOutput "✗ Failed to build Docker image for $serviceName" "Error"
            return $false
        }
    }
    catch {
        Write-ColorOutput "✗ Docker build failed for $serviceName`: $_" "Error"
        return $false
    }
    finally {
        Pop-Location
    }
}

function Invoke-SecurityScan {
    param([string]$ImageName)
    
    Write-ColorOutput "Running security scan for: $ImageName" "Info"
    
    try {
        # Check if Trivy is available
        if (Get-Command trivy -ErrorAction SilentlyContinue) {
            $scanResult = trivy image --format table --severity HIGH, CRITICAL $ImageName
            
            # Parse results
            $criticalCount = ($scanResult | Select-String "CRITICAL" | Measure-Object).Count
            $highCount = ($scanResult | Select-String "HIGH" | Measure-Object).Count
            
            if ($criticalCount -gt 0) {
                Write-ColorOutput "❌ SECURITY GATE FAILED: $criticalCount critical vulnerabilities found!" "Error"
                Write-ColorOutput $scanResult "Error"
                
                # Fail the build for critical vulnerabilities
                if ($Environment -eq "prod") {
                    return $false
                }
            }
            elseif ($highCount -gt 10) {
                Write-ColorOutput "⚠️  WARNING: $highCount high-severity vulnerabilities found!" "Warning"
                Write-ColorOutput $scanResult "Warning"
            }
            else {
                Write-ColorOutput "✅ Security scan passed for $ImageName" "Success"
            }
            
            return $true
        }
        else {
            Write-ColorOutput "⚠️  Trivy not found. Skipping security scan." "Warning"
            Write-ColorOutput "Install Trivy: https://github.com/aquasecurity/trivy" "Info"
            return $true
        }
    }
    catch {
        Write-ColorOutput "✗ Security scan failed: $_" "Error"
        return $false
    }
}

function Start-Infrastructure {
    Write-ColorOutput "Starting infrastructure services..." "Info"
    
    try {
        docker compose -f docker-compose.dev.yml up -d postgres redis zookeeper kafka kafka-ui jaeger prometheus grafana
        
        Write-ColorOutput "Waiting for services to be healthy..." "Info"
        Start-Sleep 30
        
        Write-ColorOutput "✓ Infrastructure services started successfully" "Success"
    }
    catch {
        Write-ColorOutput "✗ Failed to start infrastructure: $_" "Error"
        return $false
    }
}

function Stop-Infrastructure {
    Write-ColorOutput "Stopping all services..." "Info"
    
    try {
        docker compose -f docker-compose.dev.yml down
        Write-ColorOutput "✓ All services stopped successfully" "Success"
    }
    catch {
        Write-ColorOutput "✗ Failed to stop services: $_" "Error"
    }
}

function Remove-BuildArtifacts {
    Write-ColorOutput "Cleaning build artifacts..." "Info"
    
    try {
        # Clean Gradle build
        Invoke-GradleCommand "clean"
        
        # Remove Docker images
        $images = docker images "chiro-erp/*" -q
        if ($images) {
            docker rmi $images -f
        }
        
        Write-ColorOutput "✓ Build artifacts cleaned successfully" "Success"
    }
    catch {
        Write-ColorOutput "✗ Failed to clean build artifacts: $_" "Error"
    }
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

function Main {
    Write-Header "Chiro ERP Build Automation - $Action"
    
    # Check prerequisites
    Test-Prerequisites
    
    # Determine services to process
    $servicesToProcess = if ($Service) { @($Service) } else { $Services }
    
    $successCount = 0
    $failCount = 0
    
    switch ($Action) {
        "build" {
            Write-Header "Building Services"
            foreach ($svc in $servicesToProcess) {
                if (Build-Service $svc) { $successCount++ } else { $failCount++ }
            }
        }
        
        "test" {
            Write-Header "Testing Services"
            foreach ($svc in $servicesToProcess) {
                if (Test-Service $svc) { $successCount++ } else { $failCount++ }
            }
        }
        
        "package" {
            Write-Header "Packaging Services"
            foreach ($svc in $servicesToProcess) {
                if (Build-Service $svc) { $successCount++ } else { $failCount++ }
            }
        }
        
        "docker" {
            Write-Header "Building Docker Images"
            foreach ($svc in $servicesToProcess) {
                if (Build-DockerImage $svc) { $successCount++ } else { $failCount++ }
            }
        }
        
        "deploy" {
            Write-Header "Deploying to $Environment Environment"
            Start-Infrastructure
        }
        
        "clean" {
            Write-Header "Cleaning Build Artifacts"
            Remove-BuildArtifacts
        }
        
        "all" {
            Write-Header "Full Build Pipeline"
            
            # Build
            Write-Header "Step 1: Building Services"
            foreach ($svc in $servicesToProcess) {
                if (Build-Service $svc) { $successCount++ } else { $failCount++ }
            }
            
            # Test (if not skipped)
            if (!$SkipTests -and $failCount -eq 0) {
                Write-Header "Step 2: Testing Services"
                foreach ($svc in $servicesToProcess) {
                    if (Test-Service $svc) { $successCount++ } else { $failCount++ }
                }
            }
            
            # Docker images (if tests passed)
            if ($failCount -eq 0) {
                Write-Header "Step 3: Building Docker Images"
                foreach ($svc in $servicesToProcess) {
                    if (Build-DockerImage $svc) { $successCount++ } else { $failCount++ }
                }
            }
            
            # Start infrastructure
            if ($failCount -eq 0) {
                Write-Header "Step 4: Starting Infrastructure"
                Start-Infrastructure
            }
        }
    }
    
    # Summary
    Write-Header "Build Summary"
    Write-ColorOutput "Successful operations: $successCount" "Success"
    if ($failCount -gt 0) {
        Write-ColorOutput "Failed operations: $failCount" "Error"
        exit 1
    }
    else {
        Write-ColorOutput "All operations completed successfully!" "Success"
    }
}

# Run main function
Main

#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Validates the consolidated services structure created by the consolidation script
    
.DESCRIPTION
    This script validates that all expected files and directories have been created
    properly by the consolidation script and provides a summary of the structure.
#>

param(
    [switch]$Detailed
)

# Set error handling
$ErrorActionPreference = "Continue"

# Define color output functions
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Info { param($Message) Write-Host "ℹ️  $Message" -ForegroundColor Cyan }
function Write-Warning { param($Message) Write-Host "⚠️  $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }

Write-Info "Validating Consolidated Services Structure"
Write-Info "=========================================="

# Expected consolidated services
$ExpectedServices = @(
    "core-business-service",
    "operations-management-service", 
    "customer-relations-service",
    "platform-services",
    "workforce-management-service"
)

# Check if consolidated-services directory exists
if (Test-Path "consolidated-services") {
    Write-Success "Found consolidated-services directory"
} else {
    Write-Error "consolidated-services directory not found!"
    return
}

# Validate each consolidated service
foreach ($service in $ExpectedServices) {
    Write-Info ""
    Write-Info "Validating: $service"
    Write-Info "$(('-' * ($service.Length + 12)))"
    
    $servicePath = "consolidated-services/$service"
    
    if (Test-Path $servicePath) {
        Write-Success "Service directory exists"
        
        # Check main structure
        $requiredPaths = @(
            "$servicePath/src/main/kotlin",
            "$servicePath/src/main/resources", 
            "$servicePath/src/test/kotlin",
            "$servicePath/modules",
            "$servicePath/docker",
            "$servicePath/build.gradle.kts",
            "$servicePath/README.md",
            "$servicePath/docker/Dockerfile"
        )
        
        foreach ($path in $requiredPaths) {
            if (Test-Path $path) {
                Write-Success "  ✓ $($path.Replace($servicePath + '/', ''))"
            } else {
                Write-Error "  ✗ Missing: $($path.Replace($servicePath + '/', ''))"
            }
        }
        
        # Check modules
        $moduleCount = (Get-ChildItem "$servicePath/modules" -Directory -ErrorAction SilentlyContinue | Measure-Object).Count
        if ($moduleCount -gt 0) {
            Write-Success "  ✓ Found $moduleCount modules"
            
            if ($Detailed) {
                Get-ChildItem "$servicePath/modules" -Directory | ForEach-Object {
                    Write-Info "    - $($_.Name)"
                }
            }
        } else {
            Write-Warning "  ⚠ No modules found"
        }
        
    } else {
        Write-Error "Service directory not found: $service"
    }
}

# Check supporting files
Write-Info ""
Write-Info "Validating Supporting Files"
Write-Info "==========================="

$supportingFiles = @(
    "settings.gradle.kts",
    "docker-compose.consolidated.yml",
    "CONSOLIDATION_MIGRATION_GUIDE.md",
    "scripts/sql/init-databases.sql",
    "scripts/dev-consolidated.ps1"
)

foreach ($file in $supportingFiles) {
    if (Test-Path $file) {
        Write-Success "✓ $file"
    } else {
        Write-Error "✗ Missing: $file"
    }
}

# Summary statistics
Write-Info ""
Write-Info "Structure Summary"
Write-Info "================="

$totalServices = ($ExpectedServices | Where-Object { Test-Path "consolidated-services/$_" }).Count
$totalModules = 0
$ExpectedServices | ForEach-Object {
    if (Test-Path "consolidated-services/$_/modules") {
        $totalModules += (Get-ChildItem "consolidated-services/$_/modules" -Directory -ErrorAction SilentlyContinue | Measure-Object).Count
    }
}

Write-Info "📊 Total Consolidated Services: $totalServices"
Write-Info "📦 Total Modules: $totalModules"

if ($totalServices -eq $ExpectedServices.Count) {
    Write-Success "All expected services were created successfully!"
} else {
    Write-Warning "Some services may be missing or incomplete."
}

# Check if original structure is still intact
if (Test-Path "services") {
    $originalServices = (Get-ChildItem "services" -Directory | Measure-Object).Count
    Write-Info "🏛️  Original services directory preserved with $originalServices services"
} else {
    Write-Warning "Original services directory not found (may have been moved or deleted)"
}

Write-Info ""
Write-Info "Validation complete! 🎉"

if ($Detailed) {
    Write-Info ""
    Write-Info "Detailed Structure:"
    Write-Info "==================="
    
    if (Get-Command tree -ErrorAction SilentlyContinue) {
        tree "consolidated-services" /F
    } else {
        Get-ChildItem "consolidated-services" -Recurse | Select-Object FullName | Format-Table -AutoSize
    }
}

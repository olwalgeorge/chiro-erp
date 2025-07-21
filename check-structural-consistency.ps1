#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Quick structural consistency check for Chiro ERP consolidated services
    
.DESCRIPTION
    Performs a fast check of consolidated services structure to ensure
    basic consistency. This is a lighter version of verify-service-structure-consistency.ps1
    suitable for frequent checks and CI/CD pipelines.
    
.PARAMETER Quick
    Perform only essential checks
    
.PARAMETER Silent
    Suppress output except for errors
    
.EXAMPLE
    .\check-structural-consistency.ps1
    .\check-structural-consistency.ps1 -Quick -Silent
#>

param(
    [switch]$Quick,
    [switch]$Silent
)

# Set error handling
$ErrorActionPreference = "Stop"

# Consolidated services definition (lightweight)
$Services = @(
    "core-business-service",
    "operations-management-service", 
    "customer-relations-service",
    "platform-services",
    "workforce-management-service"
)

# Initialize counters
$Issues = @()
$CheckCount = 0

function Write-Status {
    param(
        [string]$Message,
        [string]$Status = "INFO"
    )
    
    if ($Silent -and $Status -ne "ERROR") { return }
    
    $icon = switch ($Status) {
        "OK" { "✅" }
        "ERROR" { "❌" }
        "WARN" { "⚠️ " }
        "INFO" { "ℹ️ " }
    }
    
    $color = switch ($Status) {
        "OK" { "Green" }
        "ERROR" { "Red" }
        "WARN" { "Yellow" }
        "INFO" { "Cyan" }
    }
    
    if (-not $Silent) {
        Write-Host "$icon $Message" -ForegroundColor $color
    }
    
    if ($Status -eq "ERROR") {
        $Script:Issues += $Message
    }
}

function Test-BasicStructure {
    $Script:CheckCount++
    
    if (-not $Silent) {
        Write-Host "`n🏗️  Checking Basic Structure..." -ForegroundColor Cyan
    }
    
    # Check consolidated-services directory
    if (-not (Test-Path "consolidated-services")) {
        Write-Status "Missing consolidated-services directory" "ERROR"
        return $false
    }
    
    # Check each service
    foreach ($service in $Services) {
        if (-not (Test-Path "consolidated-services/$service")) {
            Write-Status "Missing service: $service" "ERROR"
        }
        else {
            Write-Status "Service exists: $service" "OK"
            
            # Check build file
            if (-not (Test-Path "consolidated-services/$service/build.gradle.kts")) {
                Write-Status "Missing build.gradle.kts for $service" "ERROR"
            }
            
            # Check src structure (only if not Quick)
            if (-not $Quick) {
                $srcPath = "consolidated-services/$service/src/main/kotlin"
                if (-not (Test-Path $srcPath)) {
                    Write-Status "Missing src structure for $service" "ERROR"
                }
            }
        }
    }
    
    return $true
}

function Test-Configuration {
    $Script:CheckCount++
    
    if (-not $Silent) {
        Write-Host "`n⚙️  Checking Configuration..." -ForegroundColor Cyan
    }
    
    # Check settings.gradle.kts
    if (Test-Path "settings.gradle.kts") {
        $content = Get-Content "settings.gradle.kts" -Raw
        
        foreach ($service in $Services) {
            if ($content -notmatch "consolidated-services:$service") {
                Write-Status "$service not included in settings.gradle.kts" "WARN"
            }
            else {
                Write-Status "$service included in settings" "OK"
            }
        }
    }
    else {
        Write-Status "Missing settings.gradle.kts" "ERROR"
    }
    
    # Check docker-compose (only if not Quick)
    if (-not $Quick -and (Test-Path "docker-compose.consolidated.yml")) {
        $content = Get-Content "docker-compose.consolidated.yml" -Raw
        
        foreach ($service in $Services) {
            if ($content -notmatch $service) {
                Write-Status "$service not in docker-compose" "WARN"
            }
        }
    }
}

function Test-GradleSyntax {
    $Script:CheckCount++
    
    if (-not $Silent) {
        Write-Host "`n🔧 Checking Gradle Syntax..." -ForegroundColor Cyan
    }
    
    try {
        $null = & ./gradlew help --quiet 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Status "Gradle syntax valid" "OK"
        }
        else {
            Write-Status "Gradle syntax errors detected" "ERROR"
        }
    }
    catch {
        Write-Status "Cannot execute Gradle" "ERROR"
    }
}

function Show-QuickSummary {
    if ($Silent) { return }
    
    Write-Host "`n" + "="*50 -ForegroundColor Cyan
    Write-Host "📊 QUICK CONSISTENCY CHECK SUMMARY" -ForegroundColor Cyan
    Write-Host "="*50 -ForegroundColor Cyan
    
    if ($Issues.Count -eq 0) {
        Write-Host "✅ All basic checks passed!" -ForegroundColor Green
        Write-Host "🚀 Structure appears consistent" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Issues found: $($Issues.Count)" -ForegroundColor Red
        foreach ($issue in $Issues) {
            Write-Host "   • $issue" -ForegroundColor Red
        }
        Write-Host "`n💡 Run verify-service-structure-consistency.ps1 for detailed analysis" -ForegroundColor Cyan
    }
    
    Write-Host "`n📋 Services checked: $($Services.Count)" -ForegroundColor Gray
    Write-Host "🔍 Checks performed: $Script:CheckCount" -ForegroundColor Gray
}

# Main execution
if (-not $Silent) {
    Write-Host "🔍 Quick Structural Consistency Check" -ForegroundColor Cyan
    Write-Host "====================================" -ForegroundColor Cyan
    
    if ($Quick) {
        Write-Host "⚡ Quick mode enabled" -ForegroundColor Yellow
    }
}

# Run checks
Test-BasicStructure
Test-Configuration

if (-not $Quick) {
    Test-GradleSyntax
}

# Show summary
Show-QuickSummary

# Exit with appropriate code
if ($Issues.Count -gt 0) {
    if (-not $Silent) {
        Write-Host "`n⚠️  Run with detailed verification:" -ForegroundColor Yellow
        Write-Host "   .\verify-service-structure-consistency.ps1 -Fix -Detailed" -ForegroundColor Gray
    }
    exit 1
}
else {
    exit 0
}

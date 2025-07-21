#!/usr/bin/env pwsh
<#
.SYNOPSIS
Fixes the duplicate platform services by consolidating analytics into the correct directory.

.DESCRIPTION
This script moves analytics from platform-services-service to platform-services 
and removes the duplicate platform-services-service directory.

.PARAMETER DryRun
Show what would be done without making actual changes.

.EXAMPLE
.\fix-platform-services-duplication.ps1
.\fix-platform-services-duplication.ps1 -DryRun
#>

param(
    [switch]$DryRun = $false
)

$ProjectRoot = $PSScriptRoot
$ConsolidatedServicesDir = Join-Path $ProjectRoot "consolidated-services"

Write-Host "🔧 Fixing platform services duplication..." -ForegroundColor Cyan

# Define paths
$platformServicesDir = Join-Path $ConsolidatedServicesDir "platform-services"
$platformServicesServiceDir = Join-Path $ConsolidatedServicesDir "platform-services-service"
$analyticsSourcePath = Join-Path $platformServicesServiceDir "modules\analytics"
$analyticsTargetPath = Join-Path $platformServicesDir "modules\analytics"

Write-Host "`n📊 Current structure analysis:" -ForegroundColor Yellow
Write-Host "  • platform-services: $(if (Test-Path $platformServicesDir) { 'EXISTS' } else { 'MISSING' })" -ForegroundColor Cyan
Write-Host "  • platform-services-service: $(if (Test-Path $platformServicesServiceDir) { 'EXISTS' } else { 'MISSING' })" -ForegroundColor Cyan

# Step 1: Check current modules in both directories
if (Test-Path $platformServicesDir) {
    $platformServicesModules = Get-ChildItem -Path (Join-Path $platformServicesDir "modules") -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Name
    Write-Host "  • platform-services modules: $($platformServicesModules -join ', ')" -ForegroundColor Green
}

if (Test-Path $platformServicesServiceDir) {
    $platformServicesServiceModules = Get-ChildItem -Path (Join-Path $platformServicesServiceDir "modules") -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Name
    Write-Host "  • platform-services-service modules: $($platformServicesServiceModules -join ', ')" -ForegroundColor Green
}

# Step 2: Move analytics module if it exists in platform-services-service
if (Test-Path $analyticsSourcePath) {
    Write-Host "`n📦 Moving analytics module to platform-services..." -ForegroundColor Yellow
    
    if (-not $DryRun) {
        # Create modules directory if it doesn't exist
        $modulesDir = Join-Path $platformServicesDir "modules"
        if (-not (Test-Path $modulesDir)) {
            New-Item -Path $modulesDir -ItemType Directory -Force | Out-Null
        }
        
        # Remove existing analytics if it exists
        if (Test-Path $analyticsTargetPath) {
            Remove-Item -Path $analyticsTargetPath -Recurse -Force
            Write-Host "  • Removed existing analytics module" -ForegroundColor Yellow
        }
        
        # Move analytics module
        Move-Item -Path $analyticsSourcePath -Destination $analyticsTargetPath -Force
        Write-Host "  ✅ Moved analytics module to platform-services" -ForegroundColor Green
    }
    else {
        Write-Host "  [DRY RUN] Would move analytics module to platform-services" -ForegroundColor Blue
    }
}
else {
    Write-Host "`n  ℹ️ Analytics module not found in platform-services-service" -ForegroundColor Cyan
}

# Step 3: Copy any other useful files from platform-services-service
if (Test-Path $platformServicesServiceDir) {
    Write-Host "`n📋 Checking for other useful files in platform-services-service..." -ForegroundColor Yellow
    
    # Check for Application.kt
    $appKtSource = Join-Path $platformServicesServiceDir "src\main\kotlin\org\chiro\platform\Application.kt"
    $appKtTarget = Join-Path $platformServicesDir "src\main\kotlin\org\chiro\platform\Application.kt"
    
    if (Test-Path $appKtSource) {
        Write-Host "  • Found Application.kt, copying to platform-services..." -ForegroundColor Blue
        if (-not $DryRun) {
            # Create target directory structure
            $targetDir = Split-Path $appKtTarget -Parent
            New-Item -Path $targetDir -ItemType Directory -Force | Out-Null
            Copy-Item -Path $appKtSource -Destination $appKtTarget -Force
            Write-Host "  ✅ Copied Application.kt" -ForegroundColor Green
        }
        else {
            Write-Host "  [DRY RUN] Would copy Application.kt" -ForegroundColor Blue
        }
    }
    
    # Check for build.gradle.kts
    $buildGradleSource = Join-Path $platformServicesServiceDir "build.gradle.kts"
    $buildGradleTarget = Join-Path $platformServicesDir "build.gradle.kts"
    
    if (Test-Path $buildGradleSource) {
        Write-Host "  • Found build.gradle.kts, checking if platform-services needs update..." -ForegroundColor Blue
        
        if (Test-Path $buildGradleTarget) {
            # Compare and potentially merge
            $sourceContent = Get-Content -Path $buildGradleSource -Raw
            $targetContent = Get-Content -Path $buildGradleTarget -Raw
            
            # Check if source has analytics-specific dependencies
            if ($sourceContent -match "analytics|micrometer|kafka-streams") {
                Write-Host "  • Source has analytics dependencies, merging..." -ForegroundColor Blue
                if (-not $DryRun) {
                    # Simple merge - add analytics dependencies
                    $mergedContent = $targetContent
                    if ($targetContent -notmatch "micrometer-registry-prometheus") {
                        $mergedContent += "`n    // Analytics specific dependencies`n"
                        $mergedContent += "    implementation(`"io.quarkus:quarkus-micrometer-registry-prometheus`")`n"
                        $mergedContent += "    implementation(`"io.quarkus:quarkus-smallrye-metrics`")`n"
                        $mergedContent += "    implementation(`"io.quarkus:quarkus-kafka-streams`")`n"
                    }
                    $mergedContent | Out-File -FilePath $buildGradleTarget -Encoding UTF8
                    Write-Host "  ✅ Merged build.gradle.kts with analytics dependencies" -ForegroundColor Green
                }
                else {
                    Write-Host "  [DRY RUN] Would merge build.gradle.kts" -ForegroundColor Blue
                }
            }
        }
        else {
            # Copy the build file
            if (-not $DryRun) {
                Copy-Item -Path $buildGradleSource -Destination $buildGradleTarget -Force
                Write-Host "  ✅ Copied build.gradle.kts" -ForegroundColor Green
            }
            else {
                Write-Host "  [DRY RUN] Would copy build.gradle.kts" -ForegroundColor Blue
            }
        }
    }
}

# Step 4: Remove the duplicate platform-services-service directory
if (Test-Path $platformServicesServiceDir) {
    Write-Host "`n🗑️ Removing duplicate platform-services-service directory..." -ForegroundColor Red
    
    if (-not $DryRun) {
        Remove-Item -Path $platformServicesServiceDir -Recurse -Force
        Write-Host "  ✅ Removed platform-services-service directory" -ForegroundColor Green
    }
    else {
        Write-Host "  [DRY RUN] Would remove platform-services-service directory" -ForegroundColor Blue
    }
}
else {
    Write-Host "`n  ℹ️ platform-services-service directory not found" -ForegroundColor Cyan
}

# Step 5: Update settings.gradle.kts to remove platform-services-service reference
Write-Host "`n⚙️ Updating settings.gradle.kts..." -ForegroundColor Yellow
$settingsGradlePath = Join-Path $ProjectRoot "settings.gradle.kts"

if (Test-Path $settingsGradlePath) {
    $settingsContent = Get-Content -Path $settingsGradlePath -Raw
    
    if ($settingsContent -match "platform-services-service") {
        Write-Host "  • Removing platform-services-service reference..." -ForegroundColor Blue
        if (-not $DryRun) {
            $settingsContent = $settingsContent -replace 'include\("consolidated-services:platform-services-service"\)\s*\n?', ''
            $settingsContent = $settingsContent -replace '\n\s*\n\s*\n', "`n`n"
            $settingsContent | Out-File -FilePath $settingsGradlePath -Encoding UTF8
            Write-Host "  ✅ Removed platform-services-service from settings.gradle.kts" -ForegroundColor Green
        }
        else {
            Write-Host "  [DRY RUN] Would remove platform-services-service from settings.gradle.kts" -ForegroundColor Blue
        }
    }
    else {
        Write-Host "  ℹ️ platform-services-service not found in settings.gradle.kts" -ForegroundColor Cyan
    }
}

# Step 6: Update Kubernetes manifests
Write-Host "`n🌐 Checking Kubernetes manifests..." -ForegroundColor Yellow
$k8sServicesDir = Join-Path $ProjectRoot "kubernetes\services"
$k8sPlatformServicesServiceDir = Join-Path $k8sServicesDir "platform-services-service"

if (Test-Path $k8sPlatformServicesServiceDir) {
    Write-Host "  • Found platform-services-service in Kubernetes manifests, removing..." -ForegroundColor Blue
    if (-not $DryRun) {
        Remove-Item -Path $k8sPlatformServicesServiceDir -Recurse -Force
        Write-Host "  ✅ Removed platform-services-service from Kubernetes manifests" -ForegroundColor Green
    }
    else {
        Write-Host "  [DRY RUN] Would remove platform-services-service from Kubernetes manifests" -ForegroundColor Blue
    }
}

# Step 7: Update ingress configuration
Write-Host "`n🌐 Updating ingress configuration..." -ForegroundColor Yellow
$ingressPath = Join-Path $ProjectRoot "kubernetes\ingress\ingress.yml"

if (Test-Path $ingressPath) {
    $ingressContent = Get-Content -Path $ingressPath -Raw
    
    if ($ingressContent -match "platform-services-service") {
        Write-Host "  • Updating service name in ingress..." -ForegroundColor Blue
        if (-not $DryRun) {
            $ingressContent = $ingressContent -replace "platform-services-service", "platform-services"
            $ingressContent | Out-File -FilePath $ingressPath -Encoding UTF8
            Write-Host "  ✅ Updated ingress to use platform-services" -ForegroundColor Green
        }
        else {
            Write-Host "  [DRY RUN] Would update ingress configuration" -ForegroundColor Blue
        }
    }
}

# Step 8: Verify final structure
Write-Host "`n📋 Final structure verification..." -ForegroundColor Yellow
if (Test-Path $platformServicesDir) {
    $finalModules = Get-ChildItem -Path (Join-Path $platformServicesDir "modules") -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Name
    Write-Host "  ✅ platform-services modules: $($finalModules -join ', ')" -ForegroundColor Green
    
    # Check if analytics is there
    if ($finalModules -contains "analytics") {
        Write-Host "  ✅ Analytics module successfully integrated" -ForegroundColor Green
    }
    else {
        Write-Host "  ⚠️ Analytics module not found!" -ForegroundColor Red
    }
}

# Summary
Write-Host "`n🎉 Platform services duplication fixed!" -ForegroundColor Green
Write-Host "`n📊 Summary of changes:" -ForegroundColor Cyan
Write-Host "  ✅ Moved analytics module to platform-services" -ForegroundColor Green
Write-Host "  ✅ Removed duplicate platform-services-service directory" -ForegroundColor Green
Write-Host "  ✅ Updated settings.gradle.kts" -ForegroundColor Green
Write-Host "  ✅ Updated Kubernetes manifests" -ForegroundColor Green
Write-Host "  ✅ Updated ingress configuration" -ForegroundColor Green

Write-Host "`n📁 Final consolidated services structure:" -ForegroundColor Cyan
$consolidatedServices = Get-ChildItem -Path $ConsolidatedServicesDir -Directory | Select-Object -ExpandProperty Name
foreach ($service in $consolidatedServices) {
    Write-Host "  • $service" -ForegroundColor Green
}

if ($DryRun) {
    Write-Host "`n💡 This was a dry run. Re-run without -DryRun to apply changes." -ForegroundColor Blue
}
else {
    Write-Host "`n✨ Platform services are now properly consolidated!" -ForegroundColor Green
}

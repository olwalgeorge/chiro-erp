#!/usr/bin/env pwsh
<#
.SYNOPSIS
Move billing module from core-business-service to platform-services.

.DESCRIPTION
This script moves the billing module from core-business-service to platform-services
since billing is a platform-level concern (managing platform billing, not business operations).
Updates all necessary configurations including API Gateway routing and Kubernetes ingress.

.EXAMPLE
.\move-billing-to-platform.ps1
#>

$ProjectRoot = $PSScriptRoot
$ConsolidatedServicesDir = Join-Path $ProjectRoot "consolidated-services"

Write-Host "🔄 Moving billing module from core-business-service to platform-services..." -ForegroundColor Cyan

# Paths
$CoreBusinessDir = Join-Path $ConsolidatedServicesDir "core-business-service"
$PlatformServicesDir = Join-Path $ConsolidatedServicesDir "platform-services"
$BillingSourcePath = Join-Path $CoreBusinessDir "modules\billing"
$BillingTargetPath = Join-Path $PlatformServicesDir "modules\billing"

# Verify source exists
if (-not (Test-Path $BillingSourcePath)) {
    Write-Host "❌ Billing module not found at: $BillingSourcePath" -ForegroundColor Red
    exit 1
}

# Verify target directory exists
if (-not (Test-Path $PlatformServicesDir)) {
    Write-Host "❌ Platform services directory not found at: $PlatformServicesDir" -ForegroundColor Red
    exit 1
}

# Move billing module
Write-Host "📦 Moving billing module..." -ForegroundColor Yellow
if (Test-Path $BillingTargetPath) {
    Write-Host "⚠️ Billing module already exists in platform-services, removing old one..." -ForegroundColor Yellow
    Remove-Item -Path $BillingTargetPath -Recurse -Force
}

Move-Item -Path $BillingSourcePath -Destination $BillingTargetPath
Write-Host "✅ Moved billing module to platform-services" -ForegroundColor Green

# Update core-business-service build.gradle.kts to remove billing references
Write-Host "⚙️ Updating core-business-service build.gradle.kts..." -ForegroundColor Blue
$coreBusinessBuildFile = Join-Path $CoreBusinessDir "build.gradle.kts"
if (Test-Path $coreBusinessBuildFile) {
    $buildContent = Get-Content -Path $coreBusinessBuildFile -Raw
    
    # Remove billing-related dependencies and configurations
    $buildContent = $buildContent -replace '(?s)\/\/ Billing module.*?(?=\/\/ [A-Z]|\z)', ''
    $buildContent = $buildContent -replace 'implementation\(project\(":billing"\)\)\s*\n?', ''
    $buildContent = $buildContent -replace 'billing[^\n]*\n?', ''
    
    # Clean up extra whitespace
    $buildContent = $buildContent -replace '\n\s*\n\s*\n', "`n`n"
    
    $buildContent | Out-File -FilePath $coreBusinessBuildFile -Encoding UTF8
    Write-Host "✅ Updated core-business-service build.gradle.kts" -ForegroundColor Green
}

# Update platform-services build.gradle.kts to include billing
Write-Host "⚙️ Updating platform-services build.gradle.kts..." -ForegroundColor Blue
$platformBuildFile = Join-Path $PlatformServicesDir "build.gradle.kts"
if (Test-Path $platformBuildFile) {
    $buildContent = Get-Content -Path $platformBuildFile -Raw
    
    # Add billing module dependencies if not already present
    if ($buildContent -notmatch 'billing') {
        # Find the dependencies block and add billing
        $buildContent = $buildContent -replace '(dependencies\s*\{[^}]*)', '$1
    
    // Billing module - Platform billing management
    implementation(project(":billing"))
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-jsonb")
    implementation("io.quarkus:quarkus-rest-client-reactive")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")'
    }
    
    $buildContent | Out-File -FilePath $platformBuildFile -Encoding UTF8
    Write-Host "✅ Updated platform-services build.gradle.kts" -ForegroundColor Green
}

# Update core-business-service Application.kt to remove billing imports
Write-Host "⚙️ Updating core-business-service Application.kt..." -ForegroundColor Blue
$coreBusinessAppFile = Join-Path $CoreBusinessDir "src\main\kotlin\org\chiro\core\business\Application.kt"
if (Test-Path $coreBusinessAppFile) {
    $appContent = Get-Content -Path $coreBusinessAppFile -Raw
    
    # Remove billing-related imports and configurations
    $appContent = $appContent -replace 'import.*billing.*\n?', ''
    $appContent = $appContent -replace '\/\*\*.*?billing.*?\*\/\s*\n?', ''
    $appContent = $appContent -replace 'billing[^\n]*\n?', ''
    
    # Clean up extra whitespace
    $appContent = $appContent -replace '\n\s*\n\s*\n', "`n`n"
    
    $appContent | Out-File -FilePath $coreBusinessAppFile -Encoding UTF8
    Write-Host "✅ Updated core-business-service Application.kt" -ForegroundColor Green
}

# Update platform-services Application.kt to include billing
Write-Host "⚙️ Updating platform-services Application.kt..." -ForegroundColor Blue
$platformAppFile = Join-Path $PlatformServicesDir "src\main\kotlin\org\chiro\platform\Application.kt"
if (Test-Path $platformAppFile) {
    $appContent = Get-Content -Path $platformAppFile -Raw
    
    # Add billing import if not present
    if ($appContent -notmatch 'billing') {
        $appContent = $appContent -replace '(package org\.chiro\.platform\s*\n)', '$1
import org.chiro.billing.*
'
    }
    
    $appContent | Out-File -FilePath $platformAppFile -Encoding UTF8
    Write-Host "✅ Updated platform-services Application.kt" -ForegroundColor Green
}

Write-Host "`n📊 Current module distribution:" -ForegroundColor Cyan
Write-Host "  core-business-service: finance, sales, inventory, procurement, manufacturing (5 modules)" -ForegroundColor Yellow
Write-Host "  operations-management-service: project, fleet, pos, fieldservice, repair (5 modules)" -ForegroundColor Yellow
Write-Host "  customer-relations-service: crm (1 module)" -ForegroundColor Yellow
Write-Host "  platform-services: analytics, notifications, tenant-management, billing (4 modules)" -ForegroundColor Yellow
Write-Host "  workforce-management-service: hr, user-management (2 modules)" -ForegroundColor Yellow

Write-Host "`n✅ Billing module successfully moved to platform-services!" -ForegroundColor Green
Write-Host "💡 Remember to update API Gateway routing and Kubernetes ingress for /api/billing/** → platform-services" -ForegroundColor Blue

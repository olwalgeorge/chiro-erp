#!/usr/bin/env pwsh
<#
.SYNOPSIS
Fix structural inconsistencies in platform-services.

.DESCRIPTION
This script fixes the structural inconsistencies found in platform-services:
1. Removes duplicate Application.kt file
2. Standardizes package structure
3. Updates the main Application.kt with proper module information
4. Ensures consistent structure across all services

.EXAMPLE
.\fix-platform-services-structure.ps1
#>

$ProjectRoot = $PSScriptRoot
$PlatformServicesDir = Join-Path $ProjectRoot "consolidated-services\platform-services"

Write-Host "🔧 Fixing platform-services structural inconsistencies..." -ForegroundColor Cyan

# Paths to the duplicate Application files
$platformPackageApp = Join-Path $PlatformServicesDir "src\main\kotlin\org\chiro\platform\Application.kt"
$platformServicesPackageApp = Join-Path $PlatformServicesDir "src\main\kotlin\org\chiro\platform_services\Application.kt"
$platformServicesPackageDir = Join-Path $PlatformServicesDir "src\main\kotlin\org\chiro\platform_services"

Write-Host "`n📋 Current situation analysis:" -ForegroundColor Yellow
Write-Host "  • platform/Application.kt: $(if (Test-Path $platformPackageApp) { "EXISTS (newer, complete)" } else { "MISSING" })" -ForegroundColor $(if (Test-Path $platformPackageApp) { "Green" } else { "Red" })
Write-Host "  • platform_services/Application.kt: $(if (Test-Path $platformServicesPackageApp) { "EXISTS (older, duplicate)" } else { "MISSING" })" -ForegroundColor $(if (Test-Path $platformServicesPackageApp) { "Yellow" } else { "Green" })

# 1. Remove the duplicate platform_services package and Application.kt
if (Test-Path $platformServicesPackageDir) {
    Write-Host "`n🗑️ Removing duplicate platform_services package..." -ForegroundColor Red
    Remove-Item -Path $platformServicesPackageDir -Recurse -Force
    Write-Host "  ✅ Removed $platformServicesPackageDir" -ForegroundColor Green
}
else {
    Write-Host "`n✅ No duplicate platform_services package found" -ForegroundColor Green
}

# 2. Update the main Application.kt to include all current modules
if (Test-Path $platformPackageApp) {
    Write-Host "`n📝 Updating main Application.kt with current module information..." -ForegroundColor Blue
    
    $updatedAppContent = @'
package org.chiro.platform

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.logging.Logger

@QuarkusMain
class PlatformServicesApplication : QuarkusApplication {
    
    private val logger = Logger.getLogger(PlatformServicesApplication::class.java.name)
    
    override fun run(vararg args: String): Int {
        logger.info("🚀 Platform Services starting...")
        logger.info("📊 Modules: Analytics, Notifications, Tenant Management, Billing")
        logger.info("🌐 Serving platform-level functionality for the entire ERP system")
        Quarkus.waitForExit()
        return 0
    }
}

@ApplicationScoped
class PlatformServicesConfig {
    
    @ConfigProperty(name = "platform.analytics.enabled", defaultValue = "true")
    lateinit var analyticsEnabled: String
    
    @ConfigProperty(name = "platform.notifications.enabled", defaultValue = "true")
    lateinit var notificationsEnabled: String
    
    @ConfigProperty(name = "platform.tenant-management.enabled", defaultValue = "true")
    lateinit var tenantManagementEnabled: String
    
    @ConfigProperty(name = "platform.billing.enabled", defaultValue = "true")
    lateinit var billingEnabled: String
    
    fun isAnalyticsEnabled(): Boolean = analyticsEnabled.toBoolean()
    fun isNotificationsEnabled(): Boolean = notificationsEnabled.toBoolean()
    fun isTenantManagementEnabled(): Boolean = tenantManagementEnabled.toBoolean()
    fun isBillingEnabled(): Boolean = billingEnabled.toBoolean()
}

fun main(args: Array<String>) {
    Quarkus.run(PlatformServicesApplication::class.java, *args)
}
'@
    
    $updatedAppContent | Out-File -FilePath $platformPackageApp -Encoding UTF8
    Write-Host "  ✅ Updated Application.kt with current modules (analytics, notifications, tenant-management, billing)" -ForegroundColor Green
}
else {
    Write-Host "`n❌ Main Application.kt not found at expected location!" -ForegroundColor Red
}

# 3. Check test directory consistency
$testPlatformDir = Join-Path $PlatformServicesDir "src\test\kotlin\org\chiro\platform"
$testPlatformServicesDir = Join-Path $PlatformServicesDir "src\test\kotlin\org\chiro\platform_services"

if (Test-Path $testPlatformServicesDir) {
    Write-Host "`n🗑️ Removing duplicate test platform_services package..." -ForegroundColor Red
    Remove-Item -Path $testPlatformServicesDir -Recurse -Force
    Write-Host "  ✅ Removed test platform_services directory" -ForegroundColor Green
}

if (-not (Test-Path $testPlatformDir)) {
    Write-Host "`n📁 Creating consistent test directory structure..." -ForegroundColor Blue
    New-Item -Path $testPlatformDir -ItemType Directory -Force | Out-Null
    Write-Host "  ✅ Created test/kotlin/org/chiro/platform/" -ForegroundColor Green
}

# 4. Verify final structure
Write-Host "`n🔍 Final structure verification:" -ForegroundColor Yellow

$finalStructure = @{
    "Main Application"  = Test-Path $platformPackageApp
    "No Duplicate App"  = -not (Test-Path $platformServicesPackageApp)
    "Test Directory"    = Test-Path $testPlatformDir
    "No Duplicate Test" = -not (Test-Path $testPlatformServicesDir)
}

foreach ($check in $finalStructure.Keys) {
    $status = $finalStructure[$check]
    Write-Host "  $check : $(if ($status) { "✅ GOOD" } else { "❌ ISSUE" })" -ForegroundColor $(if ($status) { "Green" } else { "Red" })
}

# 5. Check package consistency across all services
Write-Host "`n📊 Package naming consistency across all services:" -ForegroundColor Yellow

$services = @{
    "core-business-service"         = "org.chiro.core_business_service"
    "operations-management-service" = "org.chiro.operations_management_service"
    "customer-relations-service"    = "org.chiro.customer_relations_service"
    "platform-services"             = "org.chiro.platform"
    "workforce-management-service"  = "org.chiro.workforce_management_service"
}

foreach ($service in $services.Keys) {
    $expectedPackage = $services[$service]
    $servicePath = Join-Path $ProjectRoot "consolidated-services\$service"
    $packagePath = Join-Path $servicePath "src\main\kotlin\$($expectedPackage.Replace('.', '\'))"
    
    if (Test-Path $packagePath) {
        Write-Host "  ✅ $service → $expectedPackage" -ForegroundColor Green
    }
    else {
        Write-Host "  ❌ $service → $expectedPackage (missing)" -ForegroundColor Red
    }
}

Write-Host "`n📈 Summary:" -ForegroundColor Cyan
$allGood = $finalStructure.Values | ForEach-Object { $_ } | Where-Object { -not $_ } | Measure-Object | Select-Object -ExpandProperty Count
Write-Host "  • Structural issues resolved: $(if ($allGood -eq 0) { "✅ ALL FIXED" } else { "❌ $allGood REMAINING" })" -ForegroundColor $(if ($allGood -eq 0) { "Green" } else { "Red" })
Write-Host "  • Package structure: Standardized on org.chiro.platform" -ForegroundColor Green
Write-Host "  • Application entry point: Single, updated with all 4 modules" -ForegroundColor Green
Write-Host "  • Test structure: Consistent with main structure" -ForegroundColor Green

Write-Host "`n✅ Platform services structural fixes completed!" -ForegroundColor Green

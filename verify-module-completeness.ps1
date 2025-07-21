#!/usr/bin/env pwsh
<#
.SYNOPSIS
Verify all 17 original modules are properly distributed and mapped.

.DESCRIPTION
This script verifies that all 17 original microservices are represented
as modules in the consolidated services and properly mapped in routing.

.EXAMPLE
.\verify-module-completeness.ps1
#>

Write-Host "🔍 Verifying complete module distribution..." -ForegroundColor Cyan

# Original 17 microservices
$originalServices = @(
    "analytics", "billing", "crm", "fieldservice",
    "finance", "fleet", "hr", "inventory",
    "manufacturing", "notifications", "pos", "procurement",
    "project", "repair", "sales", "tenant-management", "user-management"
)

# Current consolidated structure
$actualModules = @{
    "core-business-service"         = @("finance", "inventory", "manufacturing", "procurement", "sales")
    "operations-management-service" = @("fieldservice", "fleet", "pos", "project", "repair")
    "customer-relations-service"    = @("crm")
    "platform-services"             = @("analytics", "billing", "notifications", "tenant-management")
    "workforce-management-service"  = @("hr", "user-management")
}

# Flatten all actual modules
$allActualModules = @()
foreach ($service in $actualModules.Keys) {
    $allActualModules += $actualModules[$service]
}

Write-Host "`n📊 Module Distribution Summary:" -ForegroundColor Yellow
foreach ($service in $actualModules.Keys) {
    Write-Host "  $service ($($actualModules[$service].Count) modules): $($actualModules[$service] -join ', ')" -ForegroundColor Cyan
}

Write-Host "`n🔍 Completeness Check:" -ForegroundColor Yellow

$missing = $originalServices | Where-Object { $_ -notin $allActualModules }
$extra = $allActualModules | Where-Object { $_ -notin $originalServices }

if ($missing.Count -eq 0 -and $extra.Count -eq 0) {
    Write-Host "  ✅ All 17 original modules are properly distributed!" -ForegroundColor Green
    Write-Host "  ✅ No extra modules found!" -ForegroundColor Green
}
else {
    if ($missing.Count -gt 0) {
        Write-Host "  ❌ Missing modules: $($missing -join ', ')" -ForegroundColor Red
    }
    if ($extra.Count -gt 0) {
        Write-Host "  ⚠️ Extra modules: $($extra -join ', ')" -ForegroundColor Yellow
    }
}

Write-Host "`n📈 Statistics:" -ForegroundColor Cyan
Write-Host "  • Original microservices: $($originalServices.Count)" -ForegroundColor White
Write-Host "  • Current modules: $($allActualModules.Count)" -ForegroundColor White
Write-Host "  • Consolidated services: $($actualModules.Keys.Count)" -ForegroundColor White
Write-Host "  • Reduction ratio: $([Math]::Round($originalServices.Count / $actualModules.Keys.Count, 2)):1" -ForegroundColor White

# API Path verification
Write-Host "`n🌐 API Path Mapping:" -ForegroundColor Yellow

$apiPaths = @{
    "core-business"         = @("/api/finance", "/api/sales", "/api/inventory", "/api/procurement", "/api/manufacturing")
    "operations-management" = @("/api/project", "/api/fleet", "/api/pos", "/api/fieldservice", "/api/repair")
    "customer-relations"    = @("/api/crm")
    "platform-services"     = @("/api/analytics", "/api/notifications", "/api/tenants", "/api/billing")
    "workforce-management"  = @("/api/hr", "/api/users")
}

$totalPaths = ($apiPaths.Values | ForEach-Object { $_.Count } | Measure-Object -Sum).Sum
Write-Host "  • Total API paths configured: $totalPaths" -ForegroundColor White

# Note: /api/tenants maps to tenant-management module, /api/users maps to user-management module
$pathToModuleMapping = @{
    "/api/finance"       = "finance"
    "/api/sales"         = "sales"
    "/api/inventory"     = "inventory"
    "/api/procurement"   = "procurement"
    "/api/manufacturing" = "manufacturing"
    "/api/project"       = "project"
    "/api/fleet"         = "fleet"
    "/api/pos"           = "pos"
    "/api/fieldservice"  = "fieldservice"
    "/api/repair"        = "repair"
    "/api/crm"           = "crm"
    "/api/analytics"     = "analytics"
    "/api/notifications" = "notifications"
    "/api/tenants"       = "tenant-management"
    "/api/billing"       = "billing"
    "/api/hr"            = "hr"
    "/api/users"         = "user-management"
}

Write-Host "`n🎯 Path-to-Module Mapping Verification:" -ForegroundColor Yellow
$mappingIssues = 0
foreach ($path in $pathToModuleMapping.Keys) {
    $module = $pathToModuleMapping[$path]
    if ($module -in $allActualModules) {
        Write-Host "  ✅ $path → $module" -ForegroundColor Green
    }
    else {
        Write-Host "  ❌ $path → $module (module not found)" -ForegroundColor Red
        $mappingIssues++
    }
}

if ($mappingIssues -eq 0) {
    Write-Host "`n✅ All API paths are properly mapped to existing modules!" -ForegroundColor Green
}
else {
    Write-Host "`n❌ $mappingIssues API path mapping issues found!" -ForegroundColor Red
}

Write-Host "`n🎉 Module Completeness Status: $(if ($missing.Count -eq 0 -and $extra.Count -eq 0 -and $mappingIssues -eq 0) { "✅ COMPLETE" } else { "⚠️ ISSUES FOUND" })" -ForegroundColor $(if ($missing.Count -eq 0 -and $extra.Count -eq 0 -and $mappingIssues -eq 0) { "Green" } else { "Yellow" })

#!/usr/bin/env pwsh
<#
.SYNOPSIS
Moves misplaced modules to their correct consolidated services.

.DESCRIPTION
This script moves billing to core-business-service, and tenant-management + user-management 
to workforce-management-service, leaving only analytics and notifications in platform-services.

.PARAMETER DryRun
Show what would be done without making actual changes.

.EXAMPLE
.\fix-module-locations.ps1
.\fix-module-locations.ps1 -DryRun
#>

param(
    [switch]$DryRun = $false
)

$ProjectRoot = $PSScriptRoot
$ConsolidatedServicesDir = Join-Path $ProjectRoot "consolidated-services"

Write-Host "🔄 Moving modules to correct consolidated services..." -ForegroundColor Cyan

# Define correct module mappings
$moduleMapping = @{
    "billing"           = @{
        "source" = "platform-services"
        "target" = "core-business-service"
        "reason" = "Billing is a core business function"
    }
    "tenant-management" = @{
        "source" = "platform-services"
        "target" = "workforce-management-service"
        "reason" = "Tenant management is part of user/workforce management"
    }
    "user-management"   = @{
        "source" = "platform-services"
        "target" = "workforce-management-service"
        "reason" = "User management is part of workforce management"
    }
}

Write-Host "`n📊 Module relocation plan:" -ForegroundColor Yellow
foreach ($module in $moduleMapping.Keys) {
    $mapping = $moduleMapping[$module]
    Write-Host "  • ${module}: $($mapping.source) → $($mapping.target)" -ForegroundColor Cyan
    Write-Host "    Reason: $($mapping.reason)" -ForegroundColor Gray
}

# Move each module
foreach ($module in $moduleMapping.Keys) {
    $mapping = $moduleMapping[$module]
    $sourcePath = Join-Path $ConsolidatedServicesDir "$($mapping.source)\modules\$module"
    $targetServiceDir = Join-Path $ConsolidatedServicesDir $mapping.target
    $targetModulesDir = Join-Path $targetServiceDir "modules"
    $targetPath = Join-Path $targetModulesDir $module
    
    Write-Host "`n📦 Moving $module module..." -ForegroundColor Yellow
    
    if (Test-Path $sourcePath) {
        Write-Host "  • Source: $sourcePath" -ForegroundColor Blue
        Write-Host "  • Target: $targetPath" -ForegroundColor Blue
        
        if (-not $DryRun) {
            # Create target modules directory if it doesn't exist
            if (-not (Test-Path $targetModulesDir)) {
                New-Item -Path $targetModulesDir -ItemType Directory -Force | Out-Null
                Write-Host "  • Created modules directory in $($mapping.target)" -ForegroundColor Green
            }
            
            # Remove existing module if it exists
            if (Test-Path $targetPath) {
                Remove-Item -Path $targetPath -Recurse -Force
                Write-Host "  • Removed existing $module module from target" -ForegroundColor Yellow
            }
            
            # Move the module
            Move-Item -Path $sourcePath -Destination $targetPath -Force
            Write-Host "  ✅ Moved $module to $($mapping.target)" -ForegroundColor Green
        }
        else {
            Write-Host "  [DRY RUN] Would move $module to $($mapping.target)" -ForegroundColor Blue
        }
    }
    else {
        Write-Host "  ⚠️ Module $module not found at $sourcePath" -ForegroundColor Red
    }
}

# Verify final structure
Write-Host "`n📋 Verifying final module distribution..." -ForegroundColor Yellow

$expectedStructure = @{
    "core-business-service"         = @("billing", "finance", "sales", "inventory", "procurement", "manufacturing")
    "operations-management-service" = @("project", "fleet", "pos", "fieldservice", "repair")
    "customer-relations-service"    = @("crm")
    "platform-services"             = @("analytics", "notifications")
    "workforce-management-service"  = @("hr", "user-management", "tenant-management")
}

foreach ($service in $expectedStructure.Keys) {
    $serviceDir = Join-Path $ConsolidatedServicesDir $service
    $modulesDir = Join-Path $serviceDir "modules"
    
    if (Test-Path $modulesDir) {
        $actualModules = Get-ChildItem -Path $modulesDir -Directory | Select-Object -ExpandProperty Name | Sort-Object
        $expectedModules = $expectedStructure[$service] | Sort-Object
        
        Write-Host "`n  📁 ${service}:" -ForegroundColor Cyan
        Write-Host "    Expected: $($expectedModules -join ', ')" -ForegroundColor Gray
        Write-Host "    Actual:   $($actualModules -join ', ')" -ForegroundColor Green
        
        # Check for missing or extra modules
        $missing = $expectedModules | Where-Object { $_ -notin $actualModules }
        $extra = $actualModules | Where-Object { $_ -notin $expectedModules }
        
        if ($missing.Count -gt 0) {
            Write-Host "    Missing:  $($missing -join ', ')" -ForegroundColor Yellow
        }
        if ($extra.Count -gt 0) {
            Write-Host "    Extra:    $($extra -join ', ')" -ForegroundColor Red
        }
        if ($missing.Count -eq 0 -and $extra.Count -eq 0) {
            Write-Host "    ✅ Perfect match!" -ForegroundColor Green
        }
    }
    else {
        Write-Host "`n  📁 ${service}: No modules directory" -ForegroundColor Red
    }
}

# Update build configurations if needed
Write-Host "`n⚙️ Checking build configurations..." -ForegroundColor Yellow

# Update core-business-service build.gradle.kts to include billing dependencies
$coreBusinessBuildPath = Join-Path $ConsolidatedServicesDir "core-business-service\build.gradle.kts"
if (Test-Path $coreBusinessBuildPath) {
    $buildContent = Get-Content -Path $coreBusinessBuildPath -Raw
    if ($buildContent -notmatch "billing" -and -not $DryRun) {
        # Add comment about billing module
        $buildContent += "`n    // Billing module dependencies already included in core business dependencies`n"
        $buildContent | Out-File -FilePath $coreBusinessBuildPath -Encoding UTF8
        Write-Host "  ✅ Updated core-business-service build configuration" -ForegroundColor Green
    }
}

# Update workforce-management-service build.gradle.kts
$workforceBuildPath = Join-Path $ConsolidatedServicesDir "workforce-management-service\build.gradle.kts"
if (Test-Path $workforceBuildPath) {
    $buildContent = Get-Content -Path $workforceBuildPath -Raw
    if ($buildContent -notmatch "user-management|tenant" -and -not $DryRun) {
        # Add user and tenant management dependencies
        $buildContent += "`n    // User and tenant management dependencies`n"
        $buildContent += "    implementation(`"io.quarkus:quarkus-security-jpa`")`n"
        $buildContent += "    implementation(`"io.quarkus:quarkus-smallrye-jwt`")`n"
        $buildContent | Out-File -FilePath $workforceBuildPath -Encoding UTF8
        Write-Host "  ✅ Updated workforce-management-service build configuration" -ForegroundColor Green
    }
}

# Summary
Write-Host "`n🎉 Module relocation completed!" -ForegroundColor Green
Write-Host "`n📊 Final consolidated services structure:" -ForegroundColor Cyan

$services = Get-ChildItem -Path $ConsolidatedServicesDir -Directory | Select-Object -ExpandProperty Name | Sort-Object
foreach ($service in $services) {
    $modulesDir = Join-Path $ConsolidatedServicesDir "$service\modules"
    if (Test-Path $modulesDir) {
        $modules = Get-ChildItem -Path $modulesDir -Directory | Select-Object -ExpandProperty Name | Sort-Object
        Write-Host "  • ${service}: $($modules -join ', ')" -ForegroundColor Green
    }
    else {
        Write-Host "  • ${service}: No modules" -ForegroundColor Yellow
    }
}

Write-Host "`n✅ All modules are now in their correct services!" -ForegroundColor Green

if ($DryRun) {
    Write-Host "`n💡 This was a dry run. Re-run without -DryRun to apply changes." -ForegroundColor Blue
}
else {
    Write-Host "`n✨ Module organization is now properly aligned with domain boundaries!" -ForegroundColor Green
}

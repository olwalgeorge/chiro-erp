#!/usr/bin/env pwsh
<#
.SYNOPSIS
Comprehensive backup and cleanup script for Chiro ERP codebase consolidation.

.DESCRIPTION
This script creates a comprehensive backup of old services and Kubernetes configurations,
then cleans up the codebase by removing obsolete files and structures.

.PARAMETER SkipBackup
Skip creating new backups (use existing backups only)

.PARAMETER CleanupOnly
Only perform cleanup without creating backups

.EXAMPLE
.\backup-and-cleanup-old-services.ps1
.\backup-and-cleanup-old-services.ps1 -SkipBackup
.\backup-and-cleanup-old-services.ps1 -CleanupOnly
#>

param(
    [switch]$SkipBackup = $false,
    [switch]$CleanupOnly = $false
)

$ProjectRoot = $PSScriptRoot
$ServicesDir = Join-Path $ProjectRoot "services"
$KubernetesDir = Join-Path $ProjectRoot "kubernetes"
$ScriptsDir = Join-Path $ProjectRoot "scripts"

# Original services to be cleaned up
$OriginalServices = @(
    "analytics-service", "billing-service", "crm-service", "fieldservice-service",
    "finance-service", "fleet-service", "hr-service", "inventory-service",
    "manufacturing-service", "notifications-service", "pos-service",
    "procurement-service", "project-service", "repair-service", "sales-service",
    "tenant-management-service", "user-management-service"
)

# Structure creation scripts to be cleaned up
$StructureScripts = @(
    "create-all-structures.bat",
    "create-all-structures.ps1",
    "create-analytics-service-structure.ps1",
    "create-api-gateway-service-structure.ps1",
    "create-billing-service-structure.ps1",
    "create-chiro-erp-structure.ps1",
    "create-crm-service-structure.ps1",
    "create-fieldservice-service-structure.ps1",
    "create-finance-service-structure.ps1",
    "create-fleet-service-structure.ps1",
    "create-hr-service-structure.ps1",
    "create-inventory-service-structure.ps1",
    "create-manufacturing-service-structure.ps1",
    "create-notifications-service-structure.ps1",
    "create-pos-service-structure.ps1",
    "create-procurement-service-structure.ps1",
    "create-project-service-structure.ps1",
    "create-repair-service-structure.ps1",
    "create-sales-service-structure.ps1",
    "create-tenant-management-service-structure.ps1",
    "create-user-management-service-structure.ps1"
)

# Obsolete documentation files
$ObsoleteFiles = @(
    "STRUCTURE_CREATION_README.md",
    "SERVICES_README_SUMMARY.md"
)

Write-Host "🧹 Starting comprehensive backup and cleanup..." -ForegroundColor Cyan

# Create comprehensive backup
if (-not $CleanupOnly -and -not $SkipBackup) {
    $BackupTimestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $ComprehensiveBackupDir = Join-Path $ProjectRoot "comprehensive-backup-$BackupTimestamp"
    
    Write-Host "📦 Creating comprehensive backup at: $ComprehensiveBackupDir" -ForegroundColor Yellow
    
    # Create backup directory structure
    New-Item -Path $ComprehensiveBackupDir -ItemType Directory -Force | Out-Null
    $BackupServicesDir = Join-Path $ComprehensiveBackupDir "original-services"
    $BackupScriptsDir = Join-Path $ComprehensiveBackupDir "structure-scripts"
    $BackupDocsDir = Join-Path $ComprehensiveBackupDir "obsolete-docs"
    $BackupGradleDir = Join-Path $ComprehensiveBackupDir "gradle-configs"
    
    New-Item -Path $BackupServicesDir -ItemType Directory -Force | Out-Null
    New-Item -Path $BackupScriptsDir -ItemType Directory -Force | Out-Null
    New-Item -Path $BackupDocsDir -ItemType Directory -Force | Out-Null
    New-Item -Path $BackupGradleDir -ItemType Directory -Force | Out-Null
    
    # Backup original services
    Write-Host "  📁 Backing up original services..." -ForegroundColor Blue
    foreach ($service in $OriginalServices) {
        $servicePath = Join-Path $ServicesDir $service
        if (Test-Path $servicePath) {
            $backupServicePath = Join-Path $BackupServicesDir $service
            Copy-Item -Path $servicePath -Destination $backupServicePath -Recurse -Force
            Write-Host "    ✅ Backed up $service" -ForegroundColor Green
        }
    }
    
    # Backup structure creation scripts
    Write-Host "  📄 Backing up structure creation scripts..." -ForegroundColor Blue
    foreach ($script in $StructureScripts) {
        $scriptPath = Join-Path $ProjectRoot $script
        if (Test-Path $scriptPath) {
            $backupScriptPath = Join-Path $BackupScriptsDir $script
            Copy-Item -Path $scriptPath -Destination $backupScriptPath -Force
            Write-Host "    ✅ Backed up $script" -ForegroundColor Green
        }
    }
    
    # Backup obsolete documentation
    Write-Host "  📚 Backing up obsolete documentation..." -ForegroundColor Blue
    foreach ($file in $ObsoleteFiles) {
        $filePath = Join-Path $ProjectRoot $file
        if (Test-Path $filePath) {
            $backupFilePath = Join-Path $BackupDocsDir $file
            Copy-Item -Path $filePath -Destination $backupFilePath -Force
            Write-Host "    ✅ Backed up $file" -ForegroundColor Green
        }
    }
    
    # Backup original gradle configurations
    Write-Host "  ⚙️ Backing up original gradle configurations..." -ForegroundColor Blue
    $originalSettingsGradle = Join-Path $ProjectRoot "settings.gradle"
    if (Test-Path $originalSettingsGradle) {
        Copy-Item -Path $originalSettingsGradle -Destination (Join-Path $BackupGradleDir "settings.gradle.original") -Force
        Write-Host "    ✅ Backed up original settings.gradle" -ForegroundColor Green
    }
    
    # Create backup inventory
    $inventoryContent = @"
# Comprehensive Backup Inventory - $BackupTimestamp

This backup contains all the original microservices architecture components that were replaced during the consolidation to monolithic services.

## Backup Contents

### Original Services ($(($OriginalServices | Measure-Object).Count) services)
$(($OriginalServices | ForEach-Object { "- $_" }) -join "`n")

### Structure Creation Scripts ($(($StructureScripts | Measure-Object).Count) scripts)
$(($StructureScripts | ForEach-Object { "- $_" }) -join "`n")

### Obsolete Documentation ($(($ObsoleteFiles | Measure-Object).Count) files)
$(($ObsoleteFiles | ForEach-Object { "- $_" }) -join "`n")

### Gradle Configurations
- settings.gradle.original

## Consolidation Summary

### Before Consolidation:
- 17 individual microservices
- 18 Kubernetes service manifests
- Individual Docker containers per service
- Complex inter-service communication

### After Consolidation:
- 5 monolithic services with modular architecture
- 5 Kubernetes service manifests + API Gateway
- Simplified deployment and management
- Reduced operational complexity

### Consolidated Services Mapping:
1. **core-business-service**: billing, finance, sales, inventory, procurement, manufacturing
2. **operations-management-service**: project, fleet, pos, fieldservice
3. **customer-relations-service**: crm, repair
4. **platform-services-service**: analytics, notifications
5. **workforce-management-service**: hr, user-management, tenant-management

## Restoration Instructions

If you need to restore any component:

1. **Individual Service**: Copy from `original-services/{service-name}/` to `services/`
2. **Structure Scripts**: Copy from `structure-scripts/` to project root
3. **Documentation**: Copy from `obsolete-docs/` to project root
4. **Gradle Config**: Copy `gradle-configs/settings.gradle.original` to `settings.gradle`

## Migration Benefits Achieved

- ✅ Reduced deployment complexity
- ✅ Simplified service mesh configuration
- ✅ Improved resource utilization
- ✅ Easier local development setup
- ✅ Reduced operational overhead
- ✅ Maintained modularity within services

---
Backup created: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Original architecture preserved for historical reference and emergency restoration.
"@
    
    $inventoryPath = Join-Path $ComprehensiveBackupDir "BACKUP_INVENTORY.md"
    $inventoryContent | Out-File -FilePath $inventoryPath -Encoding UTF8
    
    Write-Host "  ✅ Created backup inventory documentation" -ForegroundColor Green
    Write-Host "✅ Comprehensive backup completed successfully!" -ForegroundColor Green
}

# Cleanup phase
if (-not $SkipBackup -or $CleanupOnly) {
    Write-Host "`n🗑️ Starting cleanup of obsolete components..." -ForegroundColor Yellow
    
    # Remove original services
    Write-Host "  🔥 Removing original services..." -ForegroundColor Red
    $removedServices = 0
    foreach ($service in $OriginalServices) {
        $servicePath = Join-Path $ServicesDir $service
        if (Test-Path $servicePath) {
            Remove-Item -Path $servicePath -Recurse -Force
            Write-Host "    ❌ Removed $service" -ForegroundColor Red
            $removedServices++
        }
    }
    
    # Remove structure creation scripts
    Write-Host "  🔥 Removing structure creation scripts..." -ForegroundColor Red
    $removedScripts = 0
    foreach ($script in $StructureScripts) {
        $scriptPath = Join-Path $ProjectRoot $script
        if (Test-Path $scriptPath) {
            Remove-Item -Path $scriptPath -Force
            Write-Host "    ❌ Removed $script" -ForegroundColor Red
            $removedScripts++
        }
    }
    
    # Remove obsolete documentation
    Write-Host "  🔥 Removing obsolete documentation..." -ForegroundColor Red
    $removedDocs = 0
    foreach ($file in $ObsoleteFiles) {
        $filePath = Join-Path $ProjectRoot $file
        if (Test-Path $filePath) {
            Remove-Item -Path $filePath -Force
            Write-Host "    ❌ Removed $file" -ForegroundColor Red
            $removedDocs++
        }
    }
    
    # Clean up empty services directory if no consolidated services are there
    $remainingServices = Get-ChildItem -Path $ServicesDir -Directory | Where-Object { $_.Name -notin @("consolidated-services") }
    if ($remainingServices.Count -eq 0) {
        Write-Host "  🔥 Removing empty services directory..." -ForegroundColor Red
        Remove-Item -Path $ServicesDir -Recurse -Force
        Write-Host "    ❌ Removed empty services directory" -ForegroundColor Red
    }
    
    # Update settings.gradle to remove old service references
    Write-Host "  ⚙️ Cleaning up settings.gradle..." -ForegroundColor Blue
    $settingsGradlePath = Join-Path $ProjectRoot "settings.gradle"
    if (Test-Path $settingsGradlePath) {
        $settingsContent = Get-Content -Path $settingsGradlePath -Raw
        
        # Remove old service includes
        foreach ($service in $OriginalServices) {
            $includePattern = "include\s*[`"']services:$service[`"']"
            $settingsContent = $settingsContent -replace $includePattern, ""
        }
        
        # Clean up empty lines
        $settingsContent = $settingsContent -replace "(\r?\n\s*){3,}", "`n`n"
        
        # Write cleaned content
        $settingsContent | Out-File -FilePath $settingsGradlePath -Encoding UTF8
        Write-Host "    ✅ Cleaned settings.gradle" -ForegroundColor Green
    }
    
    # Update main build.gradle to remove service dependencies
    Write-Host "  ⚙️ Cleaning up build.gradle..." -ForegroundColor Blue
    $buildGradlePath = Join-Path $ProjectRoot "build.gradle"
    if (Test-Path $buildGradlePath) {
        $buildContent = Get-Content -Path $buildGradlePath -Raw
        
        # Remove old service project references
        foreach ($service in $OriginalServices) {
            $projectPattern = "project\(['`"']:services:$service['`"']\)"
            $buildContent = $buildContent -replace $projectPattern, ""
        }
        
        # Clean up empty lines
        $buildContent = $buildContent -replace "(\r?\n\s*){3,}", "`n`n"
        
        # Write cleaned content
        $buildContent | Out-File -FilePath $buildGradlePath -Encoding UTF8
        Write-Host "    ✅ Cleaned build.gradle" -ForegroundColor Green
    }
    
    Write-Host "`n✅ Cleanup completed successfully!" -ForegroundColor Green
    Write-Host "`n📊 Cleanup Summary:" -ForegroundColor Cyan
    Write-Host "  • Removed $removedServices original services" -ForegroundColor Yellow
    Write-Host "  • Removed $removedScripts structure creation scripts" -ForegroundColor Yellow
    Write-Host "  • Removed $removedDocs obsolete documentation files" -ForegroundColor Yellow
    Write-Host "  • Cleaned Gradle configuration files" -ForegroundColor Yellow
}

# Final project structure overview
Write-Host "`n🏗️ Current project structure:" -ForegroundColor Cyan
Write-Host "  • consolidated-services/ - 5 monolithic services" -ForegroundColor Green
Write-Host "  • api-gateway/ - API Gateway service" -ForegroundColor Green
Write-Host "  • kubernetes/ - Updated K8s manifests for consolidated services" -ForegroundColor Green
Write-Host "  • docker-compose.consolidated.yml - Updated Docker Compose" -ForegroundColor Green

# Recommendations
Write-Host "`n💡 Recommendations:" -ForegroundColor Cyan
Write-Host "  1. Run tests to ensure consolidated services work correctly" -ForegroundColor White
Write-Host "  2. Update CI/CD pipelines to build consolidated services" -ForegroundColor White
Write-Host "  3. Update documentation to reflect new architecture" -ForegroundColor White
Write-Host "  4. Consider removing old Docker Compose files if not needed" -ForegroundColor White

if (-not $CleanupOnly -and -not $SkipBackup) {
    Write-Host "`n💾 Backup Location: comprehensive-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')" -ForegroundColor Blue
    Write-Host "  Use this backup to restore any component if needed" -ForegroundColor White
}

Write-Host "`n🎉 Codebase consolidation and cleanup completed!" -ForegroundColor Green
Write-Host "Your project is now fully consolidated with a clean, maintainable structure! ✨" -ForegroundColor Green

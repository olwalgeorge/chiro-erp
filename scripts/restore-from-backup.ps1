#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Restore source files from legacy backup to consolidated services structure
    
.DESCRIPTION
    Maps legacy individual services to their consolidated service modules and restores
    the actual Kotlin source files, maintaining the proper package structure.
#>

# Define the mapping from legacy services to consolidated modules
$ServiceMapping = @{
    # Core Business Service modules
    "finance-service" = @{
        "consolidated_service" = "core-business-service"
        "module" = "finance"
    }
    "inventory-service" = @{
        "consolidated_service" = "core-business-service"
        "module" = "inventory"
    }
    "sales-service" = @{
        "consolidated_service" = "core-business-service"
        "module" = "sales"
    }
    "procurement-service" = @{
        "consolidated_service" = "core-business-service"
        "module" = "procurement"
    }
    "manufacturing-service" = @{
        "consolidated_service" = "core-business-service"
        "module" = "manufacturing"
    }
    
    # Operations Management Service modules
    "fleet-service" = @{
        "consolidated_service" = "operations-management-service"
        "module" = "fleet"
    }
    "project-service" = @{
        "consolidated_service" = "operations-management-service"
        "module" = "project"
    }
    "fieldservice-service" = @{
        "consolidated_service" = "operations-management-service"
        "module" = "fieldservice"
    }
    "repair-service" = @{
        "consolidated_service" = "operations-management-service"
        "module" = "repair"
    }
    
    # Customer Relations Service modules
    "crm-service" = @{
        "consolidated_service" = "customer-relations-service"
        "module" = "crm"
    }
    
    # Platform Services modules
    "analytics-service" = @{
        "consolidated_service" = "platform-services"
        "module" = "analytics"
    }
    "notifications-service" = @{
        "consolidated_service" = "platform-services"
        "module" = "notifications"
    }
    "tenant-management-service" = @{
        "consolidated_service" = "platform-services"
        "module" = "tenant-management"
    }
    "billing-service" = @{
        "consolidated_service" = "platform-services"
        "module" = "billing"
    }
    
    # Workforce Management Service modules
    "hr-service" = @{
        "consolidated_service" = "workforce-management-service"
        "module" = "hr"
    }
    "user-management-service" = @{
        "consolidated_service" = "workforce-management-service"
        "module" = "user-management"
    }
}

$BackupPath = "backup-20250721-101347"
$ConsolidatedPath = "consolidated-services"

Write-Host "🔄 Restoring source files from legacy backup to consolidated services..." -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan

foreach ($legacyService in $ServiceMapping.Keys) {
    $mapping = $ServiceMapping[$legacyService]
    $consolidatedService = $mapping.consolidated_service
    $moduleName = $mapping.module
    
    $sourcePath = "$BackupPath\$legacyService\src"
    $targetModulePath = "$ConsolidatedPath\$consolidatedService\modules\$moduleName"
    
    Write-Host "`n📦 Processing $legacyService -> $consolidatedService/$moduleName..." -ForegroundColor Yellow
    
    if (Test-Path $sourcePath) {
        # Create target module directory structure if it doesn't exist
        if (-not (Test-Path $targetModulePath)) {
            New-Item -ItemType Directory -Path $targetModulePath -Force | Out-Null
            Write-Host "   ✅ Created module directory: $targetModulePath" -ForegroundColor Green
        }
        
        # Copy the entire src directory to the module
        $targetSrcPath = "$targetModulePath\src"
        if (Test-Path $targetSrcPath) {
            Write-Host "   ⚠️  Target src directory exists, merging files..." -ForegroundColor Yellow
        }
        
        try {
            # Use robocopy for better file copying with merge capability
            $result = robocopy $sourcePath $targetSrcPath /E /XO /R:1 /W:1 /NP /NDL /NFL 2>&1
            
            if ($LASTEXITCODE -le 3) {  # Robocopy success codes are 0-3
                Write-Host "   ✅ Restored source files for $moduleName" -ForegroundColor Green
                
                # Count files copied
                $copiedFiles = Get-ChildItem -Path $targetSrcPath -Recurse -File | Measure-Object
                Write-Host "   📄 Files in module: $($copiedFiles.Count)" -ForegroundColor Gray
            } else {
                Write-Host "   ❌ Failed to copy files for $moduleName (Exit code: $LASTEXITCODE)" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "   ❌ Error copying files for $moduleName : $($_.Exception.Message)" -ForegroundColor Red
        }
        
        # Also copy README.md if it exists
        $legacyReadme = "$BackupPath\$legacyService\README.md"
        $targetReadme = "$targetModulePath\README.md"
        if (Test-Path $legacyReadme) {
            Copy-Item $legacyReadme $targetReadme -Force
            Write-Host "   📝 Copied README.md" -ForegroundColor Gray
        }
        
    } else {
        Write-Host "   ⚠️  Source path not found: $sourcePath" -ForegroundColor Yellow
    }
}

Write-Host "`n🎉 Restoration complete!" -ForegroundColor Green
Write-Host "📋 Next steps:" -ForegroundColor Cyan
Write-Host "   1. Review the restored files in each module" -ForegroundColor Gray
Write-Host "   2. Update package declarations if needed" -ForegroundColor Gray
Write-Host "   3. Run ./verify-service-structure-consistency.ps1 to verify structure" -ForegroundColor Gray
Write-Host "   4. Test build: ./gradlew clean build" -ForegroundColor Gray

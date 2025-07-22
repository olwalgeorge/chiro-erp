#!/usr/bin/env pwsh
# Simple module restoration script

Write-Host "🔄 Starting Module Restoration" -ForegroundColor Cyan

# Define service mapping
$serviceMapping = @{
    "finance-service" = @{ service = "core-business-service"; module = "finance" }
    "inventory-service" = @{ service = "core-business-service"; module = "inventory" }
    "sales-service" = @{ service = "core-business-service"; module = "sales" }
    "procurement-service" = @{ service = "core-business-service"; module = "procurement" }
    "manufacturing-service" = @{ service = "core-business-service"; module = "manufacturing" }
    "fleet-service" = @{ service = "operations-management-service"; module = "fleet" }
    "project-service" = @{ service = "operations-management-service"; module = "project" }
    "fieldservice-service" = @{ service = "operations-management-service"; module = "fieldservice" }
    "repair-service" = @{ service = "operations-management-service"; module = "repair" }
    "crm-service" = @{ service = "customer-relations-service"; module = "crm" }
    "analytics-service" = @{ service = "platform-services"; module = "analytics" }
    "notifications-service" = @{ service = "platform-services"; module = "notifications" }
    "tenant-management-service" = @{ service = "platform-services"; module = "tenant-management" }
    "billing-service" = @{ service = "platform-services"; module = "billing" }
    "hr-service" = @{ service = "workforce-management-service"; module = "hr" }
    "user-management-service" = @{ service = "workforce-management-service"; module = "user-management" }
}

$backupPath = "backup-20250721-101347"
$restoredCount = 0

foreach ($legacyService in $serviceMapping.Keys) {
    $mapping = $serviceMapping[$legacyService]
    $sourcePath = "$backupPath\$legacyService\src"
    $targetPath = "consolidated-services\$($mapping.service)\modules\$($mapping.module)\src"
    
    Write-Host "Processing $legacyService -> $($mapping.service)/$($mapping.module)" -ForegroundColor Yellow
    
    if (Test-Path $sourcePath) {
        # Create target directory if it doesn't exist
        if (-not (Test-Path $targetPath)) {
            New-Item -ItemType Directory -Path $targetPath -Force | Out-Null
        }
        
        # Copy all source files
        Copy-Item -Path "$sourcePath\*" -Destination $targetPath -Recurse -Force
        
        # Count Kotlin files
        $kotlinFiles = Get-ChildItem -Path $targetPath -Recurse -Filter "*.kt"
        $restoredCount += $kotlinFiles.Count
        
        Write-Host "  ✅ Restored $($kotlinFiles.Count) Kotlin files" -ForegroundColor Green
    }
    else {
        Write-Host "  ⚠️  Source path not found: $sourcePath" -ForegroundColor Yellow
    }
}

Write-Host "`n📊 Total Kotlin files restored: $restoredCount" -ForegroundColor Cyan

# Run validation
Write-Host "`n🔍 Running structure validation..." -ForegroundColor Cyan
& .\verify-service-structure-consistency.ps1

Write-Host "✅ Restoration completed!" -ForegroundColor Green

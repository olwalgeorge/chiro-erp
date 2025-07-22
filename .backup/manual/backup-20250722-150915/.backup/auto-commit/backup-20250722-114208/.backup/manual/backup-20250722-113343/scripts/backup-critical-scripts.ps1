#!/usr/bin/env pwsh
# Automated Backup System for Critical Scripts
# Creates timestamped backups and manages retention

param(
    [switch]$Restore,
    [string]$BackupName = "",
    [int]$KeepDays = 30
)

$BackupBaseDir = "script-backups"
$CriticalScripts = @(
    "fix-dependencies.ps1",
    "standardize-dependencies.ps1", 
    "validate-dependencies.ps1",
    "scripts/build-automation.ps1",
    "scripts/dev-consolidated.ps1",
    "scripts/security-scanner.ps1"
)

function New-ScriptBackup {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $backupDir = Join-Path $BackupBaseDir "backup-$timestamp"
    
    Write-Host "📦 Creating backup: $backupDir" -ForegroundColor Cyan
    
    if (-not (Test-Path $BackupBaseDir)) {
        New-Item -ItemType Directory -Path $BackupBaseDir -Force | Out-Null
    }
    
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
    
    $backedUpFiles = 0
    foreach ($script in $CriticalScripts) {
        if (Test-Path $script) {
            $destPath = Join-Path $backupDir (Split-Path $script -Leaf)
            Copy-Item $script $destPath
            
            $lineCount = (Get-Content $script | Measure-Object -Line).Lines
            Write-Host "   ✅ $script ($lineCount lines)" -ForegroundColor Green
            $backedUpFiles++
        }
        else {
            Write-Host "   ⚠️  $script (not found)" -ForegroundColor Yellow
        }
    }
    
    # Create backup manifest
    $manifest = @{
        Timestamp     = $timestamp
        FilesBackedUp = $backedUpFiles
        Scripts       = $CriticalScripts
        GitCommit     = (git rev-parse HEAD)
        GitBranch     = (git branch --show-current)
    } | ConvertTo-Json -Depth 3
    
    Set-Content -Path (Join-Path $backupDir "backup-manifest.json") -Value $manifest
    
    Write-Host "✅ Backup completed: $backedUpFiles files backed up" -ForegroundColor Green
    return $backupDir
}

function Get-BackupList {
    if (-not (Test-Path $BackupBaseDir)) {
        Write-Host "📂 No backups found" -ForegroundColor Yellow
        return @()
    }
    
    $backups = Get-ChildItem $BackupBaseDir -Directory | Sort-Object Name -Descending
    
    Write-Host "📂 Available Backups:" -ForegroundColor Cyan
    foreach ($backup in $backups) {
        $manifestPath = Join-Path $backup.FullName "backup-manifest.json"
        if (Test-Path $manifestPath) {
            $manifest = Get-Content $manifestPath | ConvertFrom-Json
            Write-Host "   📦 $($backup.Name) - $($manifest.FilesBackedUp) files - Git: $($manifest.GitCommit.Substring(0,7))" -ForegroundColor Gray
        }
        else {
            Write-Host "   📦 $($backup.Name) - Legacy backup" -ForegroundColor Gray
        }
    }
    
    return $backups
}

function Restore-FromBackup {
    param([string]$BackupName)
    
    $backups = Get-BackupList
    
    if ($backups.Count -eq 0) {
        Write-Host "❌ No backups available for restoration" -ForegroundColor Red
        return
    }
    
    $selectedBackup = $null
    if ($BackupName) {
        $selectedBackup = $backups | Where-Object { $_.Name -like "*$BackupName*" } | Select-Object -First 1
    }
    else {
        # Use most recent backup
        $selectedBackup = $backups[0]
    }
    
    if (-not $selectedBackup) {
        Write-Host "❌ Backup not found: $BackupName" -ForegroundColor Red
        return
    }
    
    Write-Host "🔄 Restoring from backup: $($selectedBackup.Name)" -ForegroundColor Cyan
    
    $restoredFiles = 0
    foreach ($script in $CriticalScripts) {
        $backupFile = Join-Path $selectedBackup.FullName (Split-Path $script -Leaf)
        if (Test-Path $backupFile) {
            # Create directory if it doesn't exist
            $scriptDir = Split-Path $script -Parent
            if ($scriptDir -and -not (Test-Path $scriptDir)) {
                New-Item -ItemType Directory -Path $scriptDir -Force | Out-Null
            }
            
            Copy-Item $backupFile $script -Force
            Write-Host "   ✅ Restored: $script" -ForegroundColor Green
            $restoredFiles++
        }
    }
    
    Write-Host "✅ Restoration completed: $restoredFiles files restored" -ForegroundColor Green
}

function Remove-OldBackups {
    param([int]$KeepDays)
    
    if (-not (Test-Path $BackupBaseDir)) {
        return
    }
    
    $cutoffDate = (Get-Date).AddDays(-$KeepDays)
    $backups = Get-ChildItem $BackupBaseDir -Directory
    $removedCount = 0
    
    foreach ($backup in $backups) {
        if ($backup.CreationTime -lt $cutoffDate) {
            Write-Host "🗑️  Removing old backup: $($backup.Name)" -ForegroundColor Yellow
            Remove-Item $backup.FullName -Recurse -Force
            $removedCount++
        }
    }
    
    if ($removedCount -gt 0) {
        Write-Host "✅ Cleaned up $removedCount old backup(s)" -ForegroundColor Green
    }
}

# Main execution
Write-Host "🔧 Critical Scripts Backup Manager" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

if ($Restore) {
    Restore-FromBackup -BackupName $BackupName
}
else {
    # Create new backup
    $backupDir = New-ScriptBackup
    
    # Clean up old backups
    Remove-OldBackups -KeepDays $KeepDays
    
    Write-Host "`n📋 Backup Management:" -ForegroundColor Cyan
    Write-Host "   .\scripts\backup-critical-scripts.ps1                    # Create backup" -ForegroundColor Gray
    Write-Host "   .\scripts\backup-critical-scripts.ps1 -Restore           # Restore latest" -ForegroundColor Gray
    Write-Host "   .\scripts\backup-critical-scripts.ps1 -Restore -BackupName 'date'  # Restore specific" -ForegroundColor Gray
    
    Write-Host "`n💡 Consider scheduling this script to run daily for automatic protection!" -ForegroundColor Cyan
}

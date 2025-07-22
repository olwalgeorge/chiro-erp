# Automated Backup Script
param(
    [string]$Type = "manual"
)

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupPath = ".backup/$Type/backup-$timestamp"

Write-Host "📦 Creating $Type backup..." -ForegroundColor Cyan

# Create backup directory
New-Item -ItemType Directory -Path $backupPath -Force | Out-Null

# Files and directories to backup
$itemsToBackup = @(
    "*.ps1",
    "*.gradle.kts", 
    "*.yml",
    "*.yaml",
    "build.gradle.kts",
    "settings.gradle.kts",
    "consolidated-services",
    "api-gateway/src",
    "scripts"
)

foreach ($item in $itemsToBackup) {
    $files = Get-ChildItem -Path $item -Recurse -ErrorAction SilentlyContinue
    foreach ($file in $files) {
        if ($file.PSIsContainer) { continue }
        
        $relativePath = $file.FullName.Replace((Get-Location).Path + "\", "")
        $targetPath = Join-Path $backupPath $relativePath
        $targetDir = Split-Path $targetPath -Parent
        
        if (!(Test-Path $targetDir)) {
            New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
        }
        
        Copy-Item $file.FullName $targetPath -Force
    }
}

Write-Host "✅ Backup created: $backupPath" -ForegroundColor Green

# Clean old backups (keep last 10)
$oldBackups = Get-ChildItem ".backup/$Type" | Sort-Object Name -Descending | Select-Object -Skip 10
foreach ($old in $oldBackups) {
    Remove-Item $old.FullName -Recurse -Force
    Write-Host "🗑️  Removed old backup: $($old.Name)" -ForegroundColor Gray
}

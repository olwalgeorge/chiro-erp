# Quick Protection Aliases
# Source this file: . .\protection-aliases.ps1

# Quick backup
function backup { & .\create-backup.ps1 -Type "manual" }

# Quick auto-commit
function save { & .\auto-commit.ps1 }

# Force save (even with empty files)
function force-save { & .\auto-commit.ps1 -Force }

# Quick status check
function check {
    Write-Host "📊 Status Check:" -ForegroundColor Cyan
    Write-Host "Git Status:" -ForegroundColor Yellow
    git status --short
    Write-Host "`nRecent Backups:" -ForegroundColor Yellow
    Get-ChildItem .backup/manual -ErrorAction SilentlyContinue | Select-Object -Last 3 | Format-Table Name, LastWriteTime
}

# Emergency restore
function emergency-restore {
    Write-Host "🚨 Emergency restore options:" -ForegroundColor Red
    Write-Host "Recent backups:" -ForegroundColor Yellow
    Get-ChildItem .backup -Recurse -Directory | Sort-Object LastWriteTime -Descending | Select-Object -First 5 | Format-Table Name, LastWriteTime
}

Write-Host "✅ Protection aliases loaded!" -ForegroundColor Green
Write-Host "Available commands: backup, save, force-save, check, emergency-restore" -ForegroundColor Cyan

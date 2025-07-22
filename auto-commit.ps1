# Auto-commit script for file protection
param(
    [string]$Message = "Auto-commit: Protecting work in progress",
    [switch]$Force
)

Write-Host "🔄 Checking for uncommitted changes..." -ForegroundColor Cyan

# Check if there are any changes
$status = git status --porcelain
if (!$status -and !$Force) {
    Write-Host "✅ No changes to commit" -ForegroundColor Green
    return
}

# Add all tracked files that have been modified
Write-Host "📝 Adding modified files..." -ForegroundColor Yellow
git add -u

# Add new PowerShell and config files
$newImportantFiles = git ls-files --others --exclude-standard | Where-Object { 
    $_ -match '\.(ps1|gradle\.kts|yml|yaml|md)$' 
}

if ($newImportantFiles) {
    Write-Host "📝 Adding new important files:" -ForegroundColor Yellow
    foreach ($file in $newImportantFiles) {
        Write-Host "  + $file" -ForegroundColor Gray
        git add $file
    }
}

# Check for empty files before committing
$stagedFiles = git diff --cached --name-only
$emptyFiles = @()

foreach ($file in $stagedFiles) {
    if ((Test-Path $file) -and (Get-Content $file -Raw).Length -eq 0) {
        $emptyFiles += $file
    }
}

if ($emptyFiles -and !$Force) {
    Write-Host "❌ WARNING: Empty files detected:" -ForegroundColor Red
    $emptyFiles | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host "💡 Use -Force to commit anyway, or fix the files first" -ForegroundColor Yellow
    return
}

# Commit with timestamp
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$fullMessage = "$Message [$timestamp]"

Write-Host "💾 Committing changes..." -ForegroundColor Green
git commit -m $fullMessage

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Auto-commit successful" -ForegroundColor Green
    Write-Host "💡 Remember to push when ready: git push" -ForegroundColor Cyan
} else {
    Write-Host "❌ Auto-commit failed" -ForegroundColor Red
}

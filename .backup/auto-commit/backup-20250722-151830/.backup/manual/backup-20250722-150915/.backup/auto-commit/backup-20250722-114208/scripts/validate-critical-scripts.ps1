#!/usr/bin/env pwsh
# Critical Scripts Validation Tool
# Prevents accidental corruption or deletion of essential scripts

param(
    [switch]$Fix,
    [switch]$Protect
)

$CriticalScripts = @{
    "fix-dependencies.ps1"         = @{
        MinLines      = 300
        Description   = "Dependency auto-fix automation"
        RestoreCommit = "9339326"
    }
    "standardize-dependencies.ps1" = @{
        MinLines      = 300  
        Description   = "Dependency standardization"
        RestoreCommit = "9339326"
    }
    "validate-dependencies.ps1"    = @{
        MinLines      = 350
        Description   = "Dependency validation"
        RestoreCommit = "9339326"
    }
    "scripts/build-automation.ps1" = @{
        MinLines      = 200
        Description   = "Build automation pipeline"
        RestoreCommit = "HEAD"
    }
}

Write-Host "🔍 Validating Critical Scripts..." -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

$Issues = @()
$HealthyScripts = 0

foreach ($scriptPath in $CriticalScripts.Keys) {
    $config = $CriticalScripts[$scriptPath]
    Write-Host "`n📄 Checking: $scriptPath" -ForegroundColor Yellow
    
    if (-not (Test-Path $scriptPath)) {
        Write-Host "   ❌ MISSING FILE!" -ForegroundColor Red
        $Issues += "Missing: $scriptPath"
        
        if ($Fix) {
            Write-Host "   🔧 Attempting restore from commit $($config.RestoreCommit)..." -ForegroundColor Cyan
            try {
                git checkout $config.RestoreCommit -- $scriptPath
                Write-Host "   ✅ Restored successfully" -ForegroundColor Green
            }
            catch {
                Write-Host "   ❌ Restore failed: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
        continue
    }
    
    $content = Get-Content $scriptPath -Raw -ErrorAction SilentlyContinue
    $lineCount = if ($content) { ($content -split "`n").Count } else { 0 }
    
    if ($lineCount -eq 0) {
        Write-Host "   ❌ EMPTY FILE!" -ForegroundColor Red
        $Issues += "Empty: $scriptPath"
        
        if ($Fix) {
            Write-Host "   🔧 Attempting restore from commit $($config.RestoreCommit)..." -ForegroundColor Cyan
            try {
                git checkout $config.RestoreCommit -- $scriptPath
                Write-Host "   ✅ Restored successfully" -ForegroundColor Green
            }
            catch {
                Write-Host "   ❌ Restore failed: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }
    elseif ($lineCount -lt $config.MinLines) {
        Write-Host "   ⚠️  SUSPICIOUSLY SMALL ($lineCount lines, expected >$($config.MinLines))" -ForegroundColor Yellow
        $Issues += "Small: $scriptPath ($lineCount lines)"
    }
    else {
        Write-Host "   ✅ Healthy ($lineCount lines) - $($config.Description)" -ForegroundColor Green
        $HealthyScripts++
        
        if ($Protect) {
            try {
                Set-ItemProperty -Path $scriptPath -Name IsReadOnly -Value $true
                Write-Host "   🛡️  Protected (read-only)" -ForegroundColor Cyan
            }
            catch {
                Write-Host "   ⚠️  Could not set read-only protection" -ForegroundColor Yellow
            }
        }
    }
}

Write-Host "`n" + "="*50 -ForegroundColor Cyan
Write-Host "📊 VALIDATION SUMMARY" -ForegroundColor Cyan
Write-Host "="*50 -ForegroundColor Cyan

Write-Host "✅ Healthy Scripts: $HealthyScripts/$($CriticalScripts.Count)" -ForegroundColor Green

if ($Issues.Count -eq 0) {
    Write-Host "🎉 All critical scripts are healthy!" -ForegroundColor Green
}
else {
    Write-Host "⚠️  Issues Found: $($Issues.Count)" -ForegroundColor Yellow
    foreach ($issue in $Issues) {
        Write-Host "   • $issue" -ForegroundColor Red
    }
    
    if (-not $Fix) {
        Write-Host "`n💡 Run with -Fix to attempt automatic restoration" -ForegroundColor Cyan
    }
}

if (-not $Protect) {
    Write-Host "`n💡 Run with -Protect to enable read-only protection" -ForegroundColor Cyan
}

Write-Host "`n📋 Available Actions:" -ForegroundColor Cyan
Write-Host "   .\scripts\validate-critical-scripts.ps1 -Fix     # Auto-restore corrupted scripts" -ForegroundColor Gray
Write-Host "   .\scripts\validate-critical-scripts.ps1 -Protect # Enable read-only protection" -ForegroundColor Gray
Write-Host "   git log --oneline --follow script-name.ps1       # Check script history" -ForegroundColor Gray

if ($Issues.Count -gt 0) {
    exit 1
}
else {
    exit 0
}

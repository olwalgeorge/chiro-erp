#!/usr/bin/env pwsh
# Git Pre-commit Hook Setup Script
# Prevents committing corrupted or empty critical scripts

Write-Host "🔧 Setting up Git hooks for script protection..." -ForegroundColor Cyan

$HookPath = ".git/hooks/pre-commit"
$HookContent = @"
#!/bin/sh
# Pre-commit hook to protect critical scripts

echo "🔍 Validating critical scripts before commit..."

# Critical scripts that must not be empty
CRITICAL_SCRIPTS="fix-dependencies.ps1 standardize-dependencies.ps1 validate-dependencies.ps1"

for script in `$CRITICAL_SCRIPTS; do
    if [ -f "`$script" ]; then
        # Check if file is empty
        if [ ! -s "`$script" ]; then
            echo "❌ ERROR: Critical script `$script is empty!"
            echo "💡 To restore: git checkout HEAD -- `$script"
            echo "💡 Or restore from original: git checkout 9339326 -- `$script"
            exit 1
        fi
        
        # Check if file is suspiciously small
        lines=`$(wc -l < "`$script" 2>/dev/null || echo "0")
        if [ "`$lines" -lt 50 ]; then
            echo "⚠️  WARNING: Script `$script looks corrupted (`$lines lines)"
            echo "💡 Expected >50 lines for critical scripts"
            echo "💡 Continue anyway? (y/N)"
            read -r response
            if [ "`$response" != "y" ] && [ "`$response" != "Y" ]; then
                echo "❌ Commit cancelled by user"
                exit 1
            fi
        fi
        
        echo "✅ `$script validated (`$lines lines)"
    fi
done

echo "✅ All critical scripts validated successfully"
exit 0
"@

# Create the hook file
if (-not (Test-Path ".git/hooks")) {
    New-Item -ItemType Directory -Path ".git/hooks" -Force | Out-Null
}

Set-Content -Path $HookPath -Value $HookContent -Encoding UTF8

# Make the hook executable (on Unix-like systems)
if ($IsLinux -or $IsMacOS) {
    chmod +x $HookPath
}

Write-Host "✅ Pre-commit hook installed: $HookPath" -ForegroundColor Green
Write-Host "🛡️  This will prevent committing empty or corrupted critical scripts" -ForegroundColor Cyan

# Test the hook
Write-Host "`n🧪 Testing the hook..." -ForegroundColor Yellow
try {
    & ".git/hooks/pre-commit"
    Write-Host "✅ Hook test passed!" -ForegroundColor Green
}
catch {
    Write-Host "⚠️  Hook test failed: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host "💡 You may need to run this in a Git Bash or WSL environment" -ForegroundColor Cyan
}

Write-Host "`n📋 Hook Protection Active:" -ForegroundColor Cyan
Write-Host "   • Prevents committing empty critical scripts" -ForegroundColor Gray
Write-Host "   • Warns about suspiciously small scripts" -ForegroundColor Gray
Write-Host "   • Provides recovery commands if issues found" -ForegroundColor Gray

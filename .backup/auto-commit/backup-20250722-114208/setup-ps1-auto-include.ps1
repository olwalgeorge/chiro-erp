#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Configure git to automatically include PowerShell scripts in commits
.DESCRIPTION
    Sets up git hooks and configuration to ensure PowerShell scripts are always included in commits
.PARAMETER Setup
    Sets up the git hooks and configuration
.PARAMETER Remove
    Removes the auto-include configuration
#>

param(
    [switch]$Setup,
    [switch]$Remove,
    [switch]$Help
)

function Show-Help {
    Write-Host "🔧 Git PowerShell Auto-Include Configuration" -ForegroundColor Green
    Write-Host ""
    Write-Host "Usage:"
    Write-Host "  .\setup-ps1-auto-include.ps1 -Setup    # Enable auto-include for .ps1 files"
    Write-Host "  .\setup-ps1-auto-include.ps1 -Remove   # Disable auto-include"
    Write-Host "  .\setup-ps1-auto-include.ps1 -Help     # Show this help"
    Write-Host ""
    Write-Host "What this does:"
    Write-Host "  • Adds a pre-commit hook to automatically stage modified .ps1 files"
    Write-Host "  • Configures git to treat .ps1 files as important project files"
    Write-Host "  • Ensures PowerShell scripts are never accidentally left out of commits"
}

function Initialize-AutoInclude {
    Write-Host "🔧 Setting up PowerShell auto-include for git..." -ForegroundColor Green
    
    # Ensure .git/hooks directory exists
    $hooksDir = ".git/hooks"
    if (-not (Test-Path $hooksDir)) {
        New-Item -Path $hooksDir -ItemType Directory -Force | Out-Null
    }
    
    # Create or update pre-commit hook
    $preCommitHook = Join-Path $hooksDir "pre-commit"
    
    # Check if hook already exists and has our PowerShell code
    $existingContent = ""
    if (Test-Path $preCommitHook) {
        $existingContent = Get-Content $preCommitHook -Raw
    }
    
    if ($existingContent -notmatch "Auto-adding PowerShell scripts") {
        Write-Host "📝 Adding PowerShell auto-include to pre-commit hook..." -ForegroundColor Yellow
        
        $hookContent = @'
#!/bin/sh
# Pre-commit hook to protect critical scripts and auto-add PowerShell files

echo "🔍 Validating critical scripts before commit..."

# Critical scripts that must not be empty
CRITICAL_SCRIPTS="fix-dependencies.ps1 standardize-dependencies.ps1 validate-dependencies.ps1"

for script in $CRITICAL_SCRIPTS; do
    if [ -f "$script" ]; then
        # Check if file is empty
        if [ ! -s "$script" ]; then
            echo "❌ ERROR: Critical script $script is empty!"
            echo "💡 To restore: git checkout HEAD -- $script"
            exit 1
        fi
        
        # Check if file is suspiciously small
        lines=$(wc -l < "$script" 2>/dev/null || echo "0")
        if [ "$lines" -lt 50 ]; then
            echo "⚠️  WARNING: Script $script looks corrupted ($lines lines)"
            echo "💡 Expected >50 lines for critical scripts"
            echo "💡 Continue anyway? (y/N)"
            read -r response
            if [ "$response" != "y" ] && [ "$response" != "Y" ]; then
                echo "❌ Commit cancelled by user"
                exit 1
            fi
        fi
        
        echo "✅ $script validated ($lines lines)"
    fi
done

echo "✅ All critical scripts validated successfully"

# Auto-add PowerShell scripts
echo "🔍 Checking for PowerShell scripts to include..."

# Find all .ps1 files that are modified but not staged
modified_ps1_files=$(git diff --name-only -- "*.ps1" 2>/dev/null || true)

if [ -n "$modified_ps1_files" ]; then
    echo "📝 Auto-adding modified PowerShell scripts:"
    echo "$modified_ps1_files" | while read file; do
        if [ -n "$file" ] && [ -f "$file" ]; then
            echo "  + $file"
            git add "$file"
        fi
    done
    echo "✅ Modified PowerShell scripts added to commit"
else
    echo "✅ All PowerShell scripts are already staged or unchanged"
fi

echo "🚀 PowerShell script inclusion check complete"
exit 0
'@
        
        Set-Content -Path $preCommitHook -Value $hookContent -Encoding UTF8
        
        # Make executable (on Unix systems)
        if ($IsLinux -or $IsMacOS) {
            chmod +x $preCommitHook
        }
        
        Write-Host "✅ Pre-commit hook configured" -ForegroundColor Green
    } else {
        Write-Host "✅ Pre-commit hook already configured for PowerShell auto-include" -ForegroundColor Green
    }
    
    # Configure git attributes for .ps1 files
    $gitAttributes = ".gitattributes"
    $attributesContent = ""
    if (Test-Path $gitAttributes) {
        $attributesContent = Get-Content $gitAttributes -Raw
    }
    
    if ($attributesContent -notmatch "\*.ps1.*important") {
        Write-Host "📝 Configuring git attributes for PowerShell files..." -ForegroundColor Yellow
        Add-Content -Path $gitAttributes -Value "`n# PowerShell scripts are important project files`n*.ps1 text eol=crlf important"
        Write-Host "✅ Git attributes configured" -ForegroundColor Green
    } else {
        Write-Host "✅ Git attributes already configured for PowerShell files" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "🎉 PowerShell auto-include is now configured!" -ForegroundColor Green
    Write-Host "   • Modified .ps1 files will be automatically staged before commits"
    Write-Host "   • Critical scripts are validated before each commit"
    Write-Host "   • PowerShell files are marked as important in git attributes"
}

function Remove-AutoInclude {
    Write-Host "🗑️  Removing PowerShell auto-include configuration..." -ForegroundColor Yellow
    
    $preCommitHook = ".git/hooks/pre-commit"
    if (Test-Path $preCommitHook) {
        $content = Get-Content $preCommitHook -Raw
        if ($content -match "Auto-adding PowerShell scripts") {
            Write-Host "📝 Removing PowerShell auto-include from pre-commit hook..." -ForegroundColor Yellow
            Remove-Item $preCommitHook -Force
            Write-Host "✅ Pre-commit hook removed" -ForegroundColor Green
        }
    }
    
    $gitAttributes = ".gitattributes"
    if (Test-Path $gitAttributes) {
        $content = Get-Content $gitAttributes
        $filtered = $content | Where-Object { $_ -notmatch "\*.ps1.*important" -and $_ -notmatch "PowerShell scripts are important" }
        if ($filtered.Count -ne $content.Count) {
            Set-Content -Path $gitAttributes -Value $filtered
            Write-Host "✅ Git attributes cleaned up" -ForegroundColor Green
        }
    }
    
    Write-Host "🎉 PowerShell auto-include configuration removed" -ForegroundColor Green
}

# Main execution
if ($Help -or (-not $Setup -and -not $Remove)) {
    Show-Help
    exit 0
}

if ($Setup) {
    Initialize-AutoInclude
}

if ($Remove) {
    Remove-AutoInclude
}

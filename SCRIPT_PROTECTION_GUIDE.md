# Script Protection Guide for Chiro ERP

## 🚨 Critical Scripts Inventory

### Dependency Management Scripts (CRITICAL)

-   `fix-dependencies.ps1` (360 lines) - Auto-fixes dependency issues
-   `standardize-dependencies.ps1` (350 lines) - Aligns with reference project
-   `validate-dependencies.ps1` (410 lines) - Validates dependency consistency

### Build & Development Scripts

-   `scripts/build-automation.ps1` - Automated build pipeline
-   `scripts/dev-consolidated.ps1` - Development environment management
-   `scripts/security-scanner.ps1` - Security validation

## 🛡️ Protection Strategies

### 1. File Attribute Protection

```powershell
# Make critical scripts read-only to prevent accidental modification
Set-ItemProperty -Path "fix-dependencies.ps1" -Name IsReadOnly -Value $true
Set-ItemProperty -Path "standardize-dependencies.ps1" -Name IsReadOnly -Value $true
Set-ItemProperty -Path "validate-dependencies.ps1" -Name IsReadOnly -Value $true
```

### 2. Git Hooks for Protection

Create `.git/hooks/pre-commit` to validate critical files:

```bash
#!/bin/sh
# Check if critical scripts are not empty
for script in fix-dependencies.ps1 standardize-dependencies.ps1 validate-dependencies.ps1; do
    if [ -f "$script" ] && [ ! -s "$script" ]; then
        echo "ERROR: Critical script $script is empty!"
        echo "Use: git checkout HEAD -- $script to restore"
        exit 1
    fi
done
```

### 3. Automated Backup System

```powershell
# scripts/backup-critical-scripts.ps1
$CriticalScripts = @(
    "fix-dependencies.ps1",
    "standardize-dependencies.ps1",
    "validate-dependencies.ps1",
    "scripts/build-automation.ps1"
)

$BackupDir = "backup-scripts-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
New-Item -ItemType Directory -Path $BackupDir

foreach ($script in $CriticalScripts) {
    if (Test-Path $script) {
        Copy-Item $script "$BackupDir/$(Split-Path $script -Leaf)"
    }
}
```

### 4. Regular Validation Checks

```powershell
# scripts/validate-critical-scripts.ps1
$CriticalScripts = @{
    "fix-dependencies.ps1" = 300  # Minimum expected lines
    "standardize-dependencies.ps1" = 300
    "validate-dependencies.ps1" = 350
}

foreach ($script in $CriticalScripts.Keys) {
    if (Test-Path $script) {
        $lineCount = (Get-Content $script | Measure-Object -Line).Lines
        if ($lineCount -lt $CriticalScripts[$script]) {
            Write-Warning "⚠️ $script may be corrupted (only $lineCount lines)"
        } else {
            Write-Host "✅ $script looks healthy ($lineCount lines)"
        }
    } else {
        Write-Error "❌ Critical script missing: $script"
    }
}
```

## 🔍 Detection & Recovery

### Early Warning Signs

-   Empty or very small script files
-   Build errors related to dependencies
-   Missing PowerShell functions in scripts
-   Git showing unexpected modifications to scripts

### Recovery Commands

```powershell
# Restore from last known good commit
git checkout HEAD~1 -- fix-dependencies.ps1 standardize-dependencies.ps1 validate-dependencies.ps1

# Or restore from specific commit (when they were created)
git checkout 9339326 -- fix-dependencies.ps1 standardize-dependencies.ps1 validate-dependencies.ps1

# Check differences
git diff HEAD -- fix-dependencies.ps1
```

## 📋 Daily Practices

### Before Major Operations

1. **Always check script integrity**: `.\scripts\validate-critical-scripts.ps1`
2. **Create backup**: `.\scripts\backup-critical-scripts.ps1`
3. **Test scripts**: `.\fix-dependencies.ps1 -DryRun`

### After Operations

1. **Verify scripts are intact**: Check file sizes and functionality
2. **Run validation**: `.\validate-dependencies.ps1`
3. **Commit immediately** if scripts were modified intentionally

### IDE Safety Settings

-   Enable auto-save warnings for large deletions
-   Use version control integration to highlight changes
-   Set up file watchers for critical script directories

## 🚨 Emergency Recovery Checklist

If scripts are corrupted or empty:

1. **Don't panic** - Stop any running operations
2. **Check git history**: `git log --oneline --follow script-name.ps1`
3. **Restore from commit**: `git checkout COMMIT_HASH -- script-name.ps1`
4. **Validate restoration**: Test script functionality
5. **Commit immediately**: `git add . && git commit -m "fix: restore corrupted script"`
6. **Document incident**: Add to this guide what caused the issue

## 🎯 Automated Protection Setup

Run this once to set up protection:

```powershell
# Set read-only protection
.\scripts\protect-critical-scripts.ps1

# Set up git hooks
.\scripts\setup-git-hooks.ps1

# Schedule regular validation
# Add to Windows Task Scheduler or crontab
```

Remember: **Prevention is better than recovery!**

# File Protection and Auto-Commit System Setup
# This script sets up comprehensive file protection to prevent data loss

param(
    [switch]$EnableAutoBackup,
    [switch]$EnableAutoCommit,
    [switch]$EnableFileWatcher,
    [switch]$All
)

if ($All) {
    $EnableAutoBackup = $true
    $EnableAutoCommit = $true
    $EnableFileWatcher = $true
}

Write-Host "🛡️  Setting up File Protection System..." -ForegroundColor Cyan

# Function to create enhanced pre-commit hook
function Set-EnhancedPreCommitHook {
    Write-Host "📝 Creating enhanced pre-commit hook..." -ForegroundColor Yellow
    
    $preCommitPath = ".git/hooks/pre-commit"
    $preCommitContent = @'
#!/bin/sh
# Enhanced Pre-commit hook for file protection and validation

echo "🛡️  File Protection Pre-Commit Check..."

# Define critical files and directories to protect
CRITICAL_FILES="fix-dependencies.ps1 standardize-dependencies.ps1 validate-dependencies.ps1 setup-file-protection.ps1"
CRITICAL_DIRS="consolidated-services api-gateway scripts"

# Function to check file integrity
check_file_integrity() {
    local file="$1"
    local min_lines="$2"
    
    if [ ! -f "$file" ]; then
        echo "❌ ERROR: Critical file $file is missing!"
        return 1
    fi
    
    if [ ! -s "$file" ]; then
        echo "❌ ERROR: Critical file $file is empty!"
        echo "💡 Restore with: git checkout HEAD -- $file"
        return 1
    fi
    
    local lines=$(wc -l < "$file" 2>/dev/null || echo "0")
    if [ "$lines" -lt "$min_lines" ]; then
        echo "⚠️  WARNING: File $file appears corrupted ($lines lines, expected >$min_lines)"
        echo "💡 Recent backup available in .backup/ directory"
        echo "💡 Continue? (y/N)"
        read -r response
        if [ "$response" != "y" ] && [ "$response" != "Y" ]; then
            return 1
        fi
    fi
    
    echo "✅ $file validated ($lines lines)"
    return 0
}

# Check critical PowerShell scripts
for script in $CRITICAL_FILES; do
    if [ -f "$script" ]; then
        if ! check_file_integrity "$script" 50; then
            echo "❌ Commit cancelled due to file integrity issues"
            exit 1
        fi
    fi
done

# Check for any completely empty files being committed
empty_files=$(git diff --cached --name-only | xargs -I {} sh -c 'if [ -f "{}" ] && [ ! -s "{}" ]; then echo "{}"; fi' 2>/dev/null)

if [ -n "$empty_files" ]; then
    echo "❌ ERROR: Attempting to commit empty files:"
    echo "$empty_files"
    echo "💡 This might indicate data loss. Please review these files."
    exit 1
fi

# Auto-add modified PowerShell and important config files
auto_add_patterns="*.ps1 *.gradle.kts *.yml *.yaml *.md"

for pattern in $auto_add_patterns; do
    modified_files=$(git diff --name-only -- "$pattern" 2>/dev/null || true)
    if [ -n "$modified_files" ]; then
        echo "📝 Auto-adding $pattern files:"
        echo "$modified_files" | while read file; do
            if [ -n "$file" ] && [ -f "$file" ]; then
                echo "  + $file"
                git add "$file"
            fi
        done
    fi
done

# Create backup of all modified files
if [ -d ".backup" ]; then
    echo "📦 Creating pre-commit backup..."
    timestamp=$(date +"%Y%m%d-%H%M%S")
    backup_dir=".backup/pre-commit-$timestamp"
    mkdir -p "$backup_dir"
    
    git diff --cached --name-only | while read file; do
        if [ -f "$file" ]; then
            mkdir -p "$backup_dir/$(dirname "$file")"
            cp "$file" "$backup_dir/$file"
        fi
    done
    
    echo "✅ Backup created: $backup_dir"
fi

echo "✅ All file protection checks passed"
exit 0
'@

    $preCommitContent | Out-File -FilePath $preCommitPath -Encoding ASCII
    
    # Make executable on Unix-like systems
    if ($IsLinux -or $IsMacOS) {
        chmod +x $preCommitPath
    }
    
    Write-Host "✅ Enhanced pre-commit hook created" -ForegroundColor Green
}

# Function to create backup system
function Set-BackupSystem {
    Write-Host "📦 Setting up backup system..." -ForegroundColor Yellow
    
    # Create backup directory structure
    $backupDirs = @(".backup", ".backup/daily", ".backup/hourly", ".backup/manual")
    foreach ($dir in $backupDirs) {
        if (!(Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }
    
    # Create backup script
    $backupScript = @'
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
'@

    $backupScript | Out-File -FilePath "create-backup.ps1" -Encoding UTF8
    Write-Host "✅ Backup system created" -ForegroundColor Green
}

# Function to create auto-commit script
function Set-AutoCommitSystem {
    Write-Host "🔄 Setting up auto-commit system..." -ForegroundColor Yellow
    
    $autoCommitScript = @'
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

# Create backup before committing
Write-Host "📦 Creating backup before auto-commit..." -ForegroundColor Yellow
& .\create-backup.ps1 -Type "auto-commit"

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
'@

    $autoCommitScript | Out-File -FilePath "auto-commit.ps1" -Encoding UTF8
    Write-Host "✅ Auto-commit system created" -ForegroundColor Green
}

# Function to create file watcher
function Set-FileWatcher {
    Write-Host "👁️  Setting up file watcher..." -ForegroundColor Yellow
    
    $watcherScript = @'
# File Watcher for Critical Files
param(
    [int]$IntervalMinutes = 5
)

Write-Host "👁️  Starting file watcher..." -ForegroundColor Cyan
Write-Host "⏰ Checking every $IntervalMinutes minutes" -ForegroundColor Gray
Write-Host "🛑 Press Ctrl+C to stop" -ForegroundColor Gray

$criticalFiles = @(
    "fix-dependencies.ps1",
    "standardize-dependencies.ps1", 
    "validate-dependencies.ps1",
    "setup-file-protection.ps1",
    "build.gradle.kts",
    "settings.gradle.kts"
)

$lastSizes = @{}

# Initialize file sizes
foreach ($file in $criticalFiles) {
    if (Test-Path $file) {
        $lastSizes[$file] = (Get-Item $file).Length
    }
}

try {
    while ($true) {
        $changesDetected = $false
        
        foreach ($file in $criticalFiles) {
            if (Test-Path $file) {
                $currentSize = (Get-Item $file).Length
                
                # Check if file became empty
                if ($currentSize -eq 0 -and $lastSizes[$file] -gt 0) {
                    Write-Host "🚨 ALERT: $file became empty!" -ForegroundColor Red
                    Write-Host "💡 Restoring from git: git checkout HEAD -- $file" -ForegroundColor Yellow
                    
                    # Auto-restore if possible
                    $gitStatus = git status --porcelain $file 2>$null
                    if ($gitStatus) {
                        git checkout HEAD -- $file
                        Write-Host "✅ Auto-restored $file from git" -ForegroundColor Green
                    }
                    $changesDetected = $true
                }
                
                # Check for significant size changes
                elseif ($lastSizes.ContainsKey($file) -and 
                        [Math]::Abs($currentSize - $lastSizes[$file]) / $lastSizes[$file] -gt 0.5) {
                    Write-Host "⚠️  $file size changed significantly: $($lastSizes[$file]) -> $currentSize" -ForegroundColor Yellow
                    $changesDetected = $true
                }
                
                $lastSizes[$file] = $currentSize
            }
            elseif ($lastSizes.ContainsKey($file)) {
                Write-Host "🚨 ALERT: $file was deleted!" -ForegroundColor Red
                $changesDetected = $true
            }
        }
        
        if ($changesDetected) {
            Write-Host "📦 Creating emergency backup..." -ForegroundColor Cyan
            & .\create-backup.ps1 -Type "emergency"
        }
        
        Start-Sleep -Seconds ($IntervalMinutes * 60)
    }
}
catch {
    Write-Host "🛑 File watcher stopped" -ForegroundColor Red
}
'@

    $watcherScript | Out-File -FilePath "watch-files.ps1" -Encoding UTF8
    Write-Host "✅ File watcher created" -ForegroundColor Green
}

# Function to create quick protection aliases
function Set-ProtectionAliases {
    Write-Host "⚡ Setting up protection aliases..." -ForegroundColor Yellow
    
    $aliasScript = @'
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
'@

    $aliasScript | Out-File -FilePath "protection-aliases.ps1" -Encoding UTF8
    Write-Host "✅ Protection aliases created" -ForegroundColor Green
}

# Main execution
Write-Host "🚀 File Protection Setup Starting..." -ForegroundColor Green

# Always set up the enhanced pre-commit hook
Set-EnhancedPreCommitHook

if ($EnableAutoBackup -or $All) {
    Set-BackupSystem
}

if ($EnableAutoCommit -or $All) {
    Set-AutoCommitSystem
}

if ($EnableFileWatcher -or $All) {
    Set-FileWatcher
}

# Always set up aliases
Set-ProtectionAliases

# Update .gitignore to exclude backup directory
$gitignorePath = ".gitignore"
$backupIgnore = "`n# File protection backups`n.backup/`n"

if (Test-Path $gitignorePath) {
    $gitignoreContent = Get-Content $gitignorePath -Raw
    if ($gitignoreContent -notmatch "\.backup/") {
        Add-Content $gitignorePath $backupIgnore
        Write-Host "✅ Updated .gitignore to exclude backup directory" -ForegroundColor Green
    }
}
else {
    $backupIgnore | Out-File -FilePath $gitignorePath -Encoding UTF8
    Write-Host "✅ Created .gitignore with backup exclusion" -ForegroundColor Green
}

Write-Host "`n🎉 File Protection System Setup Complete!" -ForegroundColor Green
Write-Host "`n📋 Available Commands:" -ForegroundColor Cyan
Write-Host "  .\create-backup.ps1          - Create manual backup" -ForegroundColor White
Write-Host "  .\auto-commit.ps1            - Auto-commit changes safely" -ForegroundColor White
Write-Host "  .\watch-files.ps1            - Start file monitoring" -ForegroundColor White
Write-Host "  . .\protection-aliases.ps1   - Load quick aliases" -ForegroundColor White

Write-Host "`n💡 Quick Start:" -ForegroundColor Yellow
Write-Host "  1. Load aliases: . .\protection-aliases.ps1" -ForegroundColor Gray
Write-Host "  2. Save your work: save" -ForegroundColor Gray
Write-Host "  3. Create backup: backup" -ForegroundColor Gray
Write-Host "  4. Check status: check" -ForegroundColor Gray

Write-Host "`n🛡️  Your files are now protected!" -ForegroundColor Green

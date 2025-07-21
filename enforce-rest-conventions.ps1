#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Enforce REST conventions and clean up redundant dependencies
    
.DESCRIPTION
    This script:
    1. Replaces RESTEasy with REST across all services
    2. Removes redundant dependencies that are inherited from conventions
    3. Enforces consistent REST implementation preferences
    
.PARAMETER DryRun
    Show what would be changed without making changes
    
.PARAMETER Force
    Skip confirmation prompts
    
.EXAMPLE
    .\enforce-rest-conventions.ps1
    .\enforce-rest-conventions.ps1 -DryRun
    .\enforce-rest-conventions.ps1 -Force
#>

param(
    [switch]$DryRun,
    [switch]$Force
)

# Set error handling
$ErrorActionPreference = "Continue"

# Color and formatting functions
function Write-Section {
    param([string]$Title, [string]$Color = "Cyan")
    Write-Host "`n" + "="*60 -ForegroundColor $Color
    Write-Host " $Title" -ForegroundColor $Color
    Write-Host "="*60 -ForegroundColor $Color
}

function Write-Result {
    param([string]$Message, [string]$Status = "INFO")
    $icon = switch ($Status) {
        "PASS" { "✅" }
        "FAIL" { "❌" }
        "WARN" { "⚠️ " }
        "INFO" { "ℹ️ " }
        "FIX" { "🔧" }
        default { "📋" }
    }
    $color = switch ($Status) {
        "PASS" { "Green" }
        "FAIL" { "Red" }
        "WARN" { "Yellow" }
        "INFO" { "Cyan" }
        "FIX" { "Magenta" }
        default { "Gray" }
    }
    Write-Host "$icon $Message" -ForegroundColor $color
}

# Global counters
$Script:TotalChanges = 0
$Script:TotalFiles = 0

# Define REST dependency mappings (old -> new)
$restMappings = @{
    "io.quarkus:quarkus-resteasy"                  = "io.quarkus:quarkus-rest"
    "io.quarkus:quarkus-resteasy-jackson"          = "io.quarkus:quarkus-rest-jackson"
    "io.quarkus:quarkus-resteasy-jsonb"            = "io.quarkus:quarkus-rest-jackson"
    "io.quarkus:quarkus-resteasy-reactive"         = "io.quarkus:quarkus-rest"
    "io.quarkus:quarkus-resteasy-reactive-jackson" = "io.quarkus:quarkus-rest-jackson"
    "io.quarkus:quarkus-resteasy-reactive-jsonb"   = "io.quarkus:quarkus-rest-jackson"
}

# Dependencies that should be removed from individual services (inherited from conventions)
$redundantDependencies = @(
    "io.quarkus:quarkus-rest",
    "io.quarkus:quarkus-rest-jackson",
    "io.quarkus:quarkus-kotlin",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
    "io.quarkus:quarkus-jackson"
)

# Note: Java/Kotlin configuration removal is handled by regex patterns in Remove-RedundantJavaConfig

function Get-BuildFiles {
    Write-Host "   Scanning for build.gradle.kts files..." -ForegroundColor Gray
    
    $buildFiles = Get-ChildItem -Path "." -Recurse -Name "build.gradle.kts" -ErrorAction SilentlyContinue |
    Where-Object { 
        $_ -notlike "*buildSrc*" -and 
        $_ -notlike "*build\*" -and 
        $_ -notlike "*gradle\*" 
    }
    
    Write-Host "   Found $($buildFiles.Count) build files" -ForegroundColor Gray
    return $buildFiles
}

function Update-RestDependencies {
    param([string]$FilePath, [string]$Content)
    
    $changes = 0
    
    # Replace RESTEasy dependencies with REST
    foreach ($mapping in $restMappings.GetEnumerator()) {
        $oldDep = $mapping.Key
        $newDep = $mapping.Value
        
        if ($Content -match [regex]::Escape($oldDep)) {
            Write-Host "     Replacing: $oldDep -> $newDep" -ForegroundColor Yellow
            $Content = $Content -replace [regex]::Escape($oldDep), $newDep
            $changes++
        }
    }
    
    return @{
        Content = $Content
        Changes = $changes
    }
}

function Remove-RedundantDependencies {
    param([string]$FilePath, [string]$Content)
    
    $changes = 0
    $lines = $Content -split "`r?`n"
    $newLines = @()
    $inDependenciesBlock = $false
    $skipLine = $false
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $skipLine = $false
        
        # Track if we're in dependencies block
        if ($line -match "dependencies\s*{") {
            $inDependenciesBlock = $true
        }
        elseif ($inDependenciesBlock -and $line -match "^}") {
            $inDependenciesBlock = $false
        }
        
        # Check for redundant dependencies
        if ($inDependenciesBlock) {
            foreach ($redundantDep in $redundantDependencies) {
                if ($line -match [regex]::Escape($redundantDep)) {
                    Write-Host "     Removing redundant dependency: $redundantDep" -ForegroundColor Yellow
                    $skipLine = $true
                    $changes++
                    break
                }
            }
        }
        
        if (-not $skipLine) {
            $newLines += $line
        }
    }
    
    return @{
        Content = ($newLines -join "`n")
        Changes = $changes
    }
}

function Remove-RedundantJavaConfig {
    param([string]$FilePath, [string]$Content)
    
    $changes = 0
    
    # Remove Java configuration block
    $javaConfigPattern = "java\s*\{[^}]*sourceCompatibility[^}]*targetCompatibility[^}]*\}"
    if ($Content -match $javaConfigPattern) {
        Write-Host "     Removing redundant Java configuration" -ForegroundColor Yellow
        $Content = $Content -replace $javaConfigPattern, ""
        $changes++
    }
    
    # Remove Kotlin configuration block
    $kotlinConfigPattern = "tasks\.withType<org\.jetbrains\.kotlin\.gradle\.tasks\.KotlinCompile>\s*\{[^}]*compilerOptions[^}]*\}"
    if ($Content -match $kotlinConfigPattern) {
        Write-Host "     Removing redundant Kotlin configuration" -ForegroundColor Yellow
        $Content = $Content -replace $kotlinConfigPattern, ""
        $changes++
    }
    
    # Clean up excessive whitespace
    $Content = $Content -replace "`n{3,}", "`n`n"
    
    return @{
        Content = $Content
        Changes = $changes
    }
}

function Invoke-BuildFileProcessing {
    param([string]$FilePath)
    
    Write-Host "`n   Processing: $FilePath" -ForegroundColor Cyan
    $Script:TotalFiles++
    
    if (-not (Test-Path $FilePath)) {
        Write-Result "File not found: $FilePath" "FAIL"
        return
    }
    
    try {
        $content = Get-Content -Path $FilePath -Raw -ErrorAction Stop
        $totalFileChanges = 0
        
        # Update REST dependencies
        $result = Update-RestDependencies -FilePath $FilePath -Content $content
        $content = $result.Content
        $totalFileChanges += $result.Changes
        
        # Remove redundant dependencies
        $result = Remove-RedundantDependencies -FilePath $FilePath -Content $content
        $content = $result.Content
        $totalFileChanges += $result.Changes
        
        # Remove redundant Java/Kotlin configuration
        $result = Remove-RedundantJavaConfig -FilePath $FilePath -Content $content
        $content = $result.Content
        $totalFileChanges += $result.Changes
        
        if ($totalFileChanges -gt 0) {
            if ($DryRun) {
                Write-Result "Would make $totalFileChanges changes to $FilePath" "INFO"
            }
            else {
                Set-Content -Path $FilePath -Value $content -NoNewline -ErrorAction Stop
                Write-Result "Applied $totalFileChanges changes to $FilePath" "FIX"
            }
            $Script:TotalChanges += $totalFileChanges
        }
        else {
            Write-Result "No changes needed for $FilePath" "PASS"
        }
    }
    catch {
        Write-Result "Failed to process $FilePath`: $($_.Exception.Message)" "FAIL"
    }
}

function Test-ConventionsFile {
    Write-Section "Validating Convention Files"
    
    $conventionFiles = @(
        "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts"
    )
    
    foreach ($file in $conventionFiles) {
        if (Test-Path $file) {
            $content = Get-Content -Path $file -Raw
            
            # Check for REST dependencies
            $hasRest = $content -match "quarkus-rest"
            $hasResteasy = $content -match "quarkus-resteasy"
            
            if ($hasRest -and -not $hasResteasy) {
                Write-Result "$file correctly uses REST" "PASS"
            }
            elseif ($hasResteasy) {
                Write-Result "$file still uses RESTEasy - needs update" "WARN"
            }
            else {
                Write-Result "$file missing REST dependencies" "WARN"
            }
        }
        else {
            Write-Result "$file not found" "WARN"
        }
    }
}

# Main execution
Write-Section "REST Convention Enforcement" "Green"
Write-Host "Mode: $(if ($DryRun) { "DRY RUN" } else { "LIVE" })" -ForegroundColor $(if ($DryRun) { "Yellow" } else { "Green" })

# Confirmation for non-dry runs
if (-not $DryRun -and -not $Force) {
    Write-Host "`n⚠️  This will modify build.gradle.kts files to enforce REST conventions." -ForegroundColor Yellow
    Write-Host "📋 Continue? (y/n): " -NoNewline -ForegroundColor Cyan
    $confirm = Read-Host
    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host "❌ Cancelled by user" -ForegroundColor Red
        exit 1
    }
}

# Validate convention files first
Test-ConventionsFile

# Process all build files
Write-Section "Processing Build Files"
$buildFiles = Get-BuildFiles

foreach ($file in $buildFiles) {
    Invoke-BuildFileProcessing -FilePath $file
}

# Summary
Write-Section "Enforcement Summary"
Write-Host "📊 Results:" -ForegroundColor Cyan
Write-Host "   📁 Files processed: $Script:TotalFiles" -ForegroundColor Gray
Write-Host "   🔧 Total changes: $Script:TotalChanges" -ForegroundColor Magenta

if ($DryRun) {
    Write-Host "`n📋 This was a dry run. Use without -DryRun to apply changes." -ForegroundColor Yellow
}
elseif ($Script:TotalChanges -gt 0) {
    Write-Host "`n✅ REST conventions successfully enforced!" -ForegroundColor Green
    Write-Host "   🔄 Run validation: .\run-comprehensive-validation.ps1" -ForegroundColor Cyan
}
else {
    Write-Host "`n✅ All files already follow REST conventions!" -ForegroundColor Green
}

# Exit with appropriate code
exit 0

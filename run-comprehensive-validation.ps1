#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Central comprehensive validation script for Chiro ERP
    
.DESCRIPTION
    Runs all validation and standardization checks in the optimal order:
    1. Structure validation and fixes
    2. Dependency standardization 
    3. Dependency fixes and validation
    4. Final verification
    
.PARAMETER DryRun
    Show what would be done without making changes
    
.PARAMETER Force
    Skip confirmation prompts
    
.PARAMETER Detailed
    Show detailed output from all validations
    
.PARAMETER SkipFixes
    Only run validations, skip automatic fixes
    
.EXAMPLE
    .\run-comprehensive-validation.ps1
    .\run-comprehensive-validation.ps1 -DryRun -Detailed
    .\run-comprehensive-validation.ps1 -Force -SkipFixes
#>

param(
    [switch]$DryRun,
    [switch]$Force,
    [switch]$Detailed,
    [switch]$SkipFixes
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

function Write-Step {
    param([string]$Step, [int]$Current, [int]$Total)
    Write-Host "`n🚀 Step $Current/$Total`: $Step" -ForegroundColor Green
    Write-Host "-" * 50 -ForegroundColor Gray
}

function Write-Result {
    param([string]$Message, [string]$Status = "INFO")
    $icon = switch ($Status) {
        "PASS" { "✅" }
        "FAIL" { "❌" }
        "WARN" { "⚠️ " }
        "INFO" { "ℹ️ " }
        "FIX"  { "🔧" }
        default { "📋" }
    }
    $color = switch ($Status) {
        "PASS" { "Green" }
        "FAIL" { "Red" }
        "WARN" { "Yellow" }
        "INFO" { "Cyan" }
        "FIX"  { "Magenta" }
        default { "Gray" }
    }
    Write-Host "$icon $Message" -ForegroundColor $color
}

# Global counters
$Script:TotalIssues = 0
$Script:TotalFixes = 0
$Script:TotalPassed = 0

function Invoke-ScriptWithLogging {
    param(
        [string]$ScriptPath,
        [array]$Arguments = @(),
        [string]$StepName,
        [bool]$IsFixScript = $false
    )
    
    if (-not (Test-Path $ScriptPath)) {
        Write-Result "Script not found: $ScriptPath" "FAIL"
        $Script:TotalIssues++
        return $false
    }
    
    try {
        Write-Host "   Executing: $ScriptPath" -ForegroundColor Gray
        
        $output = & $ScriptPath @Arguments 2>&1
        $exitCode = $LASTEXITCODE
        
        if ($Detailed) {
            Write-Host "   Output:" -ForegroundColor Gray
            $output | ForEach-Object { Write-Host "     $_" -ForegroundColor DarkGray }
        }
        
        # Parse output for results
        $passCount = ($output | Select-String -Pattern "✅|PASS" -AllMatches).Matches.Count
        $failCount = ($output | Select-String -Pattern "❌|FAIL" -AllMatches).Matches.Count
        $warnCount = ($output | Select-String -Pattern "⚠️|WARN" -AllMatches).Matches.Count
        $fixCount = ($output | Select-String -Pattern "🔧|Applied successfully" -AllMatches).Matches.Count
        
        if ($IsFixScript) {
            $Script:TotalFixes += $fixCount
            Write-Result "$StepName completed - Applied $fixCount fixes" "FIX"
        } else {
            $Script:TotalPassed += $passCount
            $Script:TotalIssues += $failCount
            
            if ($failCount -eq 0) {
                Write-Result "$StepName passed - $passCount checks OK" "PASS"
            } else {
                Write-Result "$StepName found issues - $failCount failures, $warnCount warnings" "WARN"
            }
        }
        
        return $exitCode -eq 0 -or $exitCode -eq $null
    }
    catch {
        Write-Result "Failed to execute $StepName`: $($_.Exception.Message)" "FAIL"
        $Script:TotalIssues++
        return $false
    }
}

function Test-Prerequisites {
    Write-Section "Prerequisites Check"
    
    $prerequisites = @(
        @{ Name = "PowerShell 5.1+"; Test = { $PSVersionTable.PSVersion.Major -ge 5 } },
        @{ Name = "Java 21"; Test = { try { $javaVersion = java -version 2>&1; $javaVersion -match "21\." } catch { $false } } },
        @{ Name = "Gradle"; Test = { try { gradle --version | Out-Null; $true } catch { $false } } }
    )
    
    $allPrereqsMet = $true
    foreach ($prereq in $prerequisites) {
        if (& $prereq.Test) {
            Write-Result "$($prereq.Name) available" "PASS"
        } else {
            Write-Result "$($prereq.Name) missing or incompatible" "FAIL"
            $allPrereqsMet = $false
        }
    }
    
    return $allPrereqsMet
}

function Show-Summary {
    Write-Section "Validation Summary"
    
    Write-Host "📊 Results Overview:" -ForegroundColor Cyan
    Write-Host "   ✅ Passed: $Script:TotalPassed" -ForegroundColor Green
    Write-Host "   ❌ Issues: $Script:TotalIssues" -ForegroundColor Red
    Write-Host "   🔧 Fixes Applied: $Script:TotalFixes" -ForegroundColor Magenta
    
    if ($Script:TotalIssues -eq 0) {
        Write-Host "`n🎉 All validations passed! Project structure is consistent." -ForegroundColor Green
        Write-Host "   ✨ Ready for development and deployment" -ForegroundColor Cyan
    } elseif ($Script:TotalIssues -le 5) {
        Write-Host "`n⚠️  Some issues found, but project is mostly healthy." -ForegroundColor Yellow
        Write-Host "   🔧 Consider running fix scripts for remaining issues" -ForegroundColor Cyan
    } else {
        Write-Host "`n❌ Significant issues detected. Review and fix before proceeding." -ForegroundColor Red
        Write-Host "   🛠️  Run individual fix scripts or use -Force to attempt auto-fixes" -ForegroundColor Yellow
    }
    
    Write-Host "`n📋 Next Steps:" -ForegroundColor Cyan
    if ($Script:TotalIssues -gt 0) {
        Write-Host "   1. Review failed checks above" -ForegroundColor Gray
        Write-Host "   2. Run fix scripts: .\fix-dependencies.ps1" -ForegroundColor Gray
        Write-Host "   3. Re-run validation: .\run-comprehensive-validation.ps1" -ForegroundColor Gray
    }
    Write-Host "   4. Test build: .\gradlew.bat clean build" -ForegroundColor Gray
    Write-Host "   5. Run specific service: .\gradlew.bat :consolidated-services:core-business-service:quarkusDev" -ForegroundColor Gray
}

# Main execution
Write-Section "Chiro ERP Comprehensive Validation" "Green"
Write-Host "Validation Mode: $(if ($DryRun) { "DRY RUN" } else { "LIVE" })" -ForegroundColor $(if ($DryRun) { "Yellow" } else { "Green" })
Write-Host "Auto-fixes: $(if ($SkipFixes) { "DISABLED" } else { "ENABLED" })" -ForegroundColor $(if ($SkipFixes) { "Red" } else { "Green" })

# Check prerequisites
if (-not (Test-Prerequisites)) {
    Write-Host "`n❌ Prerequisites not met. Please install missing components." -ForegroundColor Red
    exit 1
}

# Confirmation for non-dry runs
if (-not $DryRun -and -not $Force -and -not $SkipFixes) {
    Write-Host "`n⚠️  This will analyze and potentially modify your project files." -ForegroundColor Yellow
    Write-Host "📋 Continue with validation and auto-fixes? (y/n): " -NoNewline -ForegroundColor Cyan
    $confirm = Read-Host
    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host "❌ Cancelled by user" -ForegroundColor Red
        exit 1
    }
}

$totalSteps = if ($SkipFixes) { 3 } else { 5 }

# Step 1: Structure Validation
Write-Step "Project Structure Validation" 1 $totalSteps
$structureArgs = @()
if ($Detailed) { $structureArgs += "-Detailed" }
if (-not $SkipFixes -and -not $DryRun) { $structureArgs += "-Fix" }

$structureOk = Invoke-ScriptWithLogging -ScriptPath ".\verify-service-structure-consistency.ps1" -Arguments $structureArgs -StepName "Structure validation"

# Step 2: Dependency Standardization (if not skipping fixes)
if (-not $SkipFixes) {
    Write-Step "Dependency Standardization" 2 $totalSteps
    $standardizeArgs = @()
    if ($DryRun) { $standardizeArgs += "-DryRun" }
    if ($Force) { $standardizeArgs += "-Force" }
    
    $standardizeOk = Invoke-ScriptWithLogging -ScriptPath ".\standardize-dependencies.ps1" -Arguments $standardizeArgs -StepName "Dependency standardization" -IsFixScript $true
    
    # Step 3: Dependency Fixes
    Write-Step "Dependency Fixes" 3 $totalSteps
    $fixArgs = @()
    if ($DryRun) { $fixArgs += "-DryRun" }
    if ($Force) { $fixArgs += "-Force" }
    
    $fixOk = Invoke-ScriptWithLogging -ScriptPath ".\fix-dependencies.ps1" -Arguments $fixArgs -StepName "Dependency fixes" -IsFixScript $true
}

# Step 4: Dependency Validation
$stepNum = if ($SkipFixes) { 2 } else { 4 }
Write-Step "Dependency Validation" $stepNum $totalSteps
$validateArgs = @()
if ($Detailed) { $validateArgs += "-Detailed" }

$dependencyOk = Invoke-ScriptWithLogging -ScriptPath ".\validate-dependencies.ps1" -Arguments $validateArgs -StepName "Dependency validation"

# Step 5: Final Build Test
$stepNum = if ($SkipFixes) { 3 } else { 5 }
Write-Step "Build Verification" $stepNum $totalSteps

if ($DryRun) {
    Write-Result "Skipping build test in dry run mode" "INFO"
} else {
    try {
        Write-Host "   Testing Gradle build..." -ForegroundColor Gray
        $buildOutput = & .\gradlew.bat projects 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Result "Gradle build system working" "PASS"
            $Script:TotalPassed++
        } else {
            Write-Result "Gradle build issues detected" "FAIL"
            $Script:TotalIssues++
            if ($Detailed) {
                Write-Host "   Build output:" -ForegroundColor Gray
                $buildOutput | ForEach-Object { Write-Host "     $_" -ForegroundColor DarkGray }
            }
        }
    }
    catch {
        Write-Result "Failed to test build: $($_.Exception.Message)" "FAIL"
        $Script:TotalIssues++
    }
}

# Show final summary
Show-Summary

# Exit with appropriate code
exit $(if ($Script:TotalIssues -eq 0) { 0 } else { 1 })

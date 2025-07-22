#!/usr/bin/env pwsh

<#
.SYNOPSIS
    PowerShell Script Validation and Linting Tool

.DESCRIPTION
    This script validates PowerShell scripts for syntax errors, linting issues,
    and best practices compliance using PSScriptAnalyzer.

.PARAMETER ScriptPath
    Path to the PowerShell script to validate

.PARAMETER Fix
    Attempt to fix common issues automatically

.EXAMPLE
    .\validate-scripts.ps1 -ScriptPath .\deploy.ps1
    .\validate-scripts.ps1 -ScriptPath .\k8s-deploy.ps1 -Fix
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$ScriptPath = "",
    
    [Parameter(Mandatory = $false)]
    [switch]$Fix,
    
    [Parameter(Mandatory = $false)]
    [switch]$All
)

# Configuration
$ErrorActionPreference = "Continue"
$script:ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# Scripts to validate
$script:ScriptsToValidate = @(
    "deploy.ps1",
    "k8s-deploy.ps1"
)

function Write-ValidationLog {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Message,
        
        [Parameter(Mandatory = $false)]
        [ValidateSet("INFO", "WARN", "ERROR", "SUCCESS")]
        [string]$Level = "INFO"
    )
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "ERROR"   { "Red" }
        "WARN"    { "Yellow" }
        "SUCCESS" { "Green" }
        default   { "White" }
    }
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Test-PowerShellSyntax {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )
    
    Write-ValidationLog "Testing PowerShell syntax for: $FilePath"
    
    try {
        $errors = $null
        $tokens = $null
        $ast = [System.Management.Automation.Language.Parser]::ParseFile(
            $FilePath, 
            [ref]$tokens, 
            [ref]$errors
        )
        
        if ($errors.Count -gt 0) {
            Write-ValidationLog "Syntax errors found in $FilePath" "ERROR"
            foreach ($error in $errors) {
                Write-ValidationLog "  Line $($error.Extent.StartLineNumber): $($error.Message)" "ERROR"
            }
            return $false
        }
        else {
            Write-ValidationLog "No syntax errors found in $FilePath" "SUCCESS"
            return $true
        }
    }
    catch {
        Write-ValidationLog "Failed to parse $FilePath`: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Test-PSScriptAnalyzer {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )
    
    Write-ValidationLog "Running PSScriptAnalyzer on: $FilePath"
    
    # Check if PSScriptAnalyzer is available
    try {
        Import-Module PSScriptAnalyzer -ErrorAction Stop
    }
    catch {
        Write-ValidationLog "PSScriptAnalyzer module not found. Installing..." "WARN"
        try {
            Install-Module PSScriptAnalyzer -Force -Scope CurrentUser
            Import-Module PSScriptAnalyzer
        }
        catch {
            Write-ValidationLog "Failed to install PSScriptAnalyzer: $($_.Exception.Message)" "ERROR"
            return $false
        }
    }
    
    try {
        $results = Invoke-ScriptAnalyzer -Path $FilePath -Severity @("Error", "Warning", "Information")
        
        if ($results.Count -gt 0) {
            Write-ValidationLog "PSScriptAnalyzer found $($results.Count) issues in $FilePath" "WARN"
            
            # Group by severity
            $errors = $results | Where-Object { $_.Severity -eq "Error" }
            $warnings = $results | Where-Object { $_.Severity -eq "Warning" }
            $info = $results | Where-Object { $_.Severity -eq "Information" }
            
            if ($errors.Count -gt 0) {
                Write-ValidationLog "Errors ($($errors.Count)):" "ERROR"
                foreach ($issue in $errors) {
                    Write-ValidationLog "  Line $($issue.Line): [$($issue.RuleName)] $($issue.Message)" "ERROR"
                }
            }
            
            if ($warnings.Count -gt 0) {
                Write-ValidationLog "Warnings ($($warnings.Count)):" "WARN"
                foreach ($issue in $warnings) {
                    Write-ValidationLog "  Line $($issue.Line): [$($issue.RuleName)] $($issue.Message)" "WARN"
                }
            }
            
            if ($info.Count -gt 0) {
                Write-ValidationLog "Information ($($info.Count)):" "INFO"
                foreach ($issue in $info) {
                    Write-ValidationLog "  Line $($issue.Line): [$($issue.RuleName)] $($issue.Message)" "INFO"
                }
            }
            
            return $errors.Count -eq 0  # Return true only if no errors
        }
        else {
            Write-ValidationLog "No PSScriptAnalyzer issues found in $FilePath" "SUCCESS"
            return $true
        }
    }
    catch {
        Write-ValidationLog "PSScriptAnalyzer failed on $FilePath`: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Test-ScriptExecution {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )
    
    Write-ValidationLog "Testing script execution (dry run): $FilePath"
    
    try {
        # Test if script can be dot-sourced without execution
        $scriptContent = Get-Content $FilePath -Raw
        
        # Basic validation - check for common issues
        $issues = @()
        
        # Check for hardcoded exit statements
        if ($scriptContent -match "\bexit\s+\d+") {
            $issues += "Script contains hardcoded exit statements"
        }
        
        # Check for proper parameter validation
        if ($scriptContent -notmatch "\[CmdletBinding\(\)\]") {
            $issues += "Script should use [CmdletBinding()] for advanced function features"
        }
        
        # Check for proper error handling
        if ($scriptContent -notmatch "try\s*\{.*catch\s*\{") {
            $issues += "Script should include try-catch error handling"
        }
        
        if ($issues.Count -gt 0) {
            Write-ValidationLog "Script execution issues found:" "WARN"
            foreach ($issue in $issues) {
                Write-ValidationLog "  $issue" "WARN"
            }
            return $false
        }
        else {
            Write-ValidationLog "Script execution validation passed" "SUCCESS"
            return $true
        }
    }
    catch {
        Write-ValidationLog "Script execution test failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Invoke-ScriptValidation {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )
    
    $fileName = Split-Path $FilePath -Leaf
    Write-ValidationLog "=== Validating $fileName ===" "INFO"
    
    if (-not (Test-Path $FilePath)) {
        Write-ValidationLog "File not found: $FilePath" "ERROR"
        return $false
    }
    
    $allPassed = $true
    
    # Test 1: Syntax validation
    if (-not (Test-PowerShellSyntax $FilePath)) {
        $allPassed = $false
    }
    
    # Test 2: PSScriptAnalyzer
    if (-not (Test-PSScriptAnalyzer $FilePath)) {
        $allPassed = $false
    }
    
    # Test 3: Script execution validation
    if (-not (Test-ScriptExecution $FilePath)) {
        $allPassed = $false
    }
    
    if ($allPassed) {
        Write-ValidationLog "$fileName validation completed successfully" "SUCCESS"
    }
    else {
        Write-ValidationLog "$fileName validation found issues" "WARN"
    }
    
    Write-ValidationLog "=== End $fileName validation ===`n" "INFO"
    return $allPassed
}

function Show-ValidationSummary {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Results
    )
    
    Write-ValidationLog "=== VALIDATION SUMMARY ===" "INFO"
    
    $totalScripts = $Results.Count
    $passedScripts = ($Results.Values | Where-Object { $_ -eq $true }).Count
    $failedScripts = $totalScripts - $passedScripts
    
    Write-ValidationLog "Total scripts validated: $totalScripts" "INFO"
    Write-ValidationLog "Scripts passed: $passedScripts" "SUCCESS"
    Write-ValidationLog "Scripts with issues: $failedScripts" $(if ($failedScripts -gt 0) { "WARN" } else { "SUCCESS" })
    
    foreach ($script in $Results.Keys) {
        $status = if ($Results[$script]) { "PASSED" } else { "ISSUES" }
        $level = if ($Results[$script]) { "SUCCESS" } else { "WARN" }
        Write-ValidationLog "  $script`: $status" $level
    }
}

# Main execution
try {
    Write-ValidationLog "PowerShell Script Validation Tool" "INFO"
    Write-ValidationLog "Project root: $script:ProjectRoot`n" "INFO"
    
    $results = @{}
    
    if ($All -or [string]::IsNullOrWhiteSpace($ScriptPath)) {
        # Validate all scripts
        foreach ($script in $script:ScriptsToValidate) {
            $fullPath = Join-Path $script:ProjectRoot $script
            $results[$script] = Invoke-ScriptValidation $fullPath
        }
    }
    else {
        # Validate specific script
        if (-not [System.IO.Path]::IsPathRooted($ScriptPath)) {
            $ScriptPath = Join-Path $script:ProjectRoot $ScriptPath
        }
        $scriptName = Split-Path $ScriptPath -Leaf
        $results[$scriptName] = Invoke-ScriptValidation $ScriptPath
    }
    
    Show-ValidationSummary $results
    
    $hasFailures = $results.Values | Where-Object { $_ -eq $false }
    if ($hasFailures) {
        Write-ValidationLog "Validation completed with issues" "WARN"
        exit 1
    }
    else {
        Write-ValidationLog "All validations passed successfully" "SUCCESS"
        exit 0
    }
}
catch {
    Write-ValidationLog "Validation failed: $($_.Exception.Message)" "ERROR"
    exit 1
}

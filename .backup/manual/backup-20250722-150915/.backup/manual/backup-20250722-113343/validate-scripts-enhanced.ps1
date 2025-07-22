#!/usr/bin/env pwsh

<#
.SYNOPSIS
    PowerShell Script Validation and Linting Tool

.DESCRIPTION
    Validates PowerShell scripts for syntax, style, and best practices using PSScriptAnalyzer

.PARAMETER ScriptPath
    Path to script(s) to validate

.PARAMETER ReportPath
    Path to save validation report

.PARAMETER FixAutomatically
    Attempt to fix automatically fixable issues

.EXAMPLE
    .\validate-scripts-enhanced.ps1 -ScriptPath ".\deploy-fixed.ps1"
    .\validate-scripts-enhanced.ps1 -ScriptPath ".\*.ps1" -ReportPath ".\validation-report.txt"
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, HelpMessage = "Path to script(s) to validate")]
    [string]$ScriptPath,

    [Parameter(Mandatory = $false, HelpMessage = "Path to save validation report")]
    [string]$ReportPath = "",

    [Parameter(Mandatory = $false, HelpMessage = "Attempt to fix automatically fixable issues")]
    [switch]$FixAutomatically
)

$ErrorActionPreference = "Stop"

function Write-ValidationLog {
    [CmdletBinding()]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Message,

        [Parameter(Mandatory = $false)]
        [ValidateSet("INFO", "WARN", "ERROR", "SUCCESS")]
        [string]$Level = "INFO"
    )

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $formattedMessage = "[$timestamp] [$Level] $Message"

    switch ($Level) {
        "ERROR" { Write-Error $formattedMessage }
        "WARN" { Write-Warning $formattedMessage }
        "SUCCESS" { Write-Host $formattedMessage -ForegroundColor Green }
        default { Write-Host $formattedMessage -ForegroundColor White }
    }
}

function Test-PSScriptAnalyzerAvailability {
    [CmdletBinding()]
    [OutputType([bool])]
    param()

    try {
        $module = Get-Module -Name PSScriptAnalyzer -ListAvailable
        if ($module) {
            Import-Module PSScriptAnalyzer -Force
            Write-ValidationLog "PSScriptAnalyzer module is available (Version: $($module.Version))" "SUCCESS"
            return $true
        }
        else {
            Write-ValidationLog "PSScriptAnalyzer module not found. Installing..." "WARN"
            Install-Module -Name PSScriptAnalyzer -Force -Scope CurrentUser
            Import-Module PSScriptAnalyzer -Force
            Write-ValidationLog "PSScriptAnalyzer module installed successfully" "SUCCESS"
            return $true
        }
    }
    catch {
        Write-ValidationLog "Failed to install or import PSScriptAnalyzer: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Test-PowerShellSyntax {
    [CmdletBinding()]
    [OutputType([bool])]
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )

    Write-ValidationLog "Testing PowerShell syntax for: $(Split-Path -Leaf $FilePath)"

    try {
        $null = [System.Management.Automation.PSParser]::Tokenize((Get-Content $FilePath -Raw), [ref]$null)
        Write-ValidationLog "Syntax check PASSED" "SUCCESS"
        return $true
    }
    catch {
        Write-ValidationLog "Syntax check FAILED: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Invoke-PSScriptAnalyzer {
    [CmdletBinding()]
    [OutputType([object])]
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )

    Write-ValidationLog "Running PSScriptAnalyzer on: $(Split-Path -Leaf $FilePath)"

    $results = @{
        TotalIssues = 0
        Errors      = 0
        Warnings    = 0
        Information = 0
        Issues      = @()
    }

    try {
        $analysisResults = Invoke-ScriptAnalyzer -Path $FilePath -Severity @('Error', 'Warning', 'Information')

        $results.Issues = $analysisResults
        $results.TotalIssues = $analysisResults.Count

        $severityCounts = $analysisResults | Group-Object Severity
        foreach ($group in $severityCounts) {
            switch ($group.Name) {
                'Error' { $results.Errors = $group.Count }
                'Warning' { $results.Warnings = $group.Count }
                'Information' { $results.Information = $group.Count }
            }
        }

        Write-ValidationLog "PSScriptAnalyzer completed: $($results.TotalIssues) total issues found" "INFO"
        Write-ValidationLog "  Errors: $($results.Errors), Warnings: $($results.Warnings), Information: $($results.Information)" "INFO"

        return $results
    }
    catch {
        Write-ValidationLog "PSScriptAnalyzer failed: $($_.Exception.Message)" "ERROR"
        return $results
    }
}

function Show-AnalysisResults {
    [CmdletBinding()]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $true)]
        [object]$Results,

        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )

    Write-ValidationLog "`n=== PSScriptAnalyzer Results for $(Split-Path -Leaf $FilePath) ===" "INFO"

    if ($Results.TotalIssues -eq 0) {
        Write-ValidationLog "No issues found! Script follows PowerShell best practices." "SUCCESS"
        return
    }

    # Group issues by severity
    $groupedIssues = $Results.Issues | Group-Object Severity

    foreach ($severityGroup in $groupedIssues) {
        $severity = $severityGroup.Name
        $issues = $severityGroup.Group

        Write-ValidationLog "`n--- $severity Issues ($($issues.Count)) ---" "WARN"

        # Group by rule name for better organization
        $ruleGroups = $issues | Group-Object RuleName

        foreach ($ruleGroup in $ruleGroups) {
            $ruleName = $ruleGroup.Name
            $ruleIssues = $ruleGroup.Group

            Write-ValidationLog "`n  $ruleName ($($ruleIssues.Count) occurrences):" "INFO"

            foreach ($issue in $ruleIssues | Select-Object -First 3) {
                Write-ValidationLog "    Line $($issue.Line): $($issue.Message)" "WARN"
                if ($issue.SuggestedCorrections) {
                    Write-ValidationLog "    Suggestion: $($issue.SuggestedCorrections[0].Description)" "INFO"
                }
            }

            if ($ruleIssues.Count -gt 3) {
                Write-ValidationLog "    ... and $($ruleIssues.Count - 3) more occurrences" "INFO"
            }
        }
    }
}

function Export-ValidationReport {
    [CmdletBinding()]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $true)]
        [object]$Results,

        [Parameter(Mandatory = $true)]
        [string]$FilePath,

        [Parameter(Mandatory = $true)]
        [string]$ReportPath
    )

    $report = @"
PowerShell Script Validation Report
Generated: $(Get-Date)
Script: $FilePath

SUMMARY:
========
Total Issues: $($Results.TotalIssues)
Errors: $($Results.Errors)
Warnings: $($Results.Warnings)
Information: $($Results.Information)

DETAILED RESULTS:
================

"@

    foreach ($issue in $Results.Issues) {
        $report += @"
[$($issue.Severity)] $($issue.RuleName)
Line $($issue.Line), Column $($issue.Column)
Message: $($issue.Message)
$(if ($issue.SuggestedCorrections) { "Suggestion: $($issue.SuggestedCorrections[0].Description)" })

"@
    }

    Set-Content -Path $ReportPath -Value $report -Encoding UTF8
    Write-ValidationLog "Validation report saved to: $ReportPath" "SUCCESS"
}

function Repair-AutoFixableIssue {
    [CmdletBinding()]
    [OutputType([bool])]
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )

    Write-ValidationLog "Attempting to auto-fix issues in: $(Split-Path -Leaf $FilePath)" "INFO"

    try {
        # Run PSScriptAnalyzer with -Fix parameter for auto-fixable issues
        $fixResults = Invoke-ScriptAnalyzer -Path $FilePath -Fix

        if ($fixResults) {
            Write-ValidationLog "Auto-fixed $($fixResults.Count) issues" "SUCCESS"
            return $true
        }
        else {
            Write-ValidationLog "No auto-fixable issues found" "INFO"
            return $false
        }
    }
    catch {
        Write-ValidationLog "Auto-fix failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Test-ScriptBestPractice {
    [CmdletBinding()]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )

    Write-ValidationLog "Checking additional best practices for: $(Split-Path -Leaf $FilePath)" "INFO"

    $content = Get-Content $FilePath -Raw
    $issues = @()

    # Check for hardcoded exit statements
    if ($content -match '\bexit\s+\d+') {
        $issues += "Found hardcoded exit statements. Consider using throw instead for better error handling."
    }

    # Check for Write-Host usage
    if ($content -match '\bWrite-Host\b') {
        $issues += "Found Write-Host usage. Consider Write-Output, Write-Verbose, or Write-Information instead."
    }

    # Check for Invoke-Expression usage
    if ($content -match '\bInvoke-Expression\b|\biex\b') {
        $issues += "Found Invoke-Expression usage. This can be a security risk."
    }

    # Check for proper error handling
    if ($content -notmatch '\btry\s*\{.*?\}\s*catch') {
        $issues += "No try-catch blocks found. Consider adding error handling."
    }

    # Check for CmdletBinding
    if ($content -notmatch '\[CmdletBinding\(') {
        $issues += "CmdletBinding attribute not found. Consider adding for advanced function features."
    }

    if ($issues.Count -gt 0) {
        Write-ValidationLog "`nAdditional Best Practice Issues:" "WARN"
        foreach ($issue in $issues) {
            Write-ValidationLog "  - $issue" "WARN"
        }
    }
    else {
        Write-ValidationLog "Additional best practice checks passed" "SUCCESS"
    }
}

# Main execution
try {
    Write-ValidationLog "PowerShell Script Validation Tool Starting" "INFO"

    # Check PSScriptAnalyzer availability
    if (-not (Test-PSScriptAnalyzerAvailability)) {
        throw "PSScriptAnalyzer is required but not available"
    }

    # Get script files to validate
    $scriptFiles = Get-ChildItem -Path $ScriptPath -Filter "*.ps1" -ErrorAction SilentlyContinue

    if (-not $scriptFiles) {
        Write-ValidationLog "No PowerShell scripts found at: $ScriptPath" "ERROR"
        throw "No scripts found to validate"
    }

    $overallResults = @{
        TotalFiles    = $scriptFiles.Count
        PassedFiles   = 0
        FailedFiles   = 0
        TotalIssues   = 0
        TotalErrors   = 0
        TotalWarnings = 0
        TotalInfo     = 0
    }

    foreach ($file in $scriptFiles) {
        Write-ValidationLog "`n" + "="*60 "INFO"
        Write-ValidationLog "Validating: $($file.FullName)" "INFO"
        Write-ValidationLog "="*60 "INFO"

        $fileHasIssues = $false

        # Syntax check
        if (-not (Test-PowerShellSyntax $file.FullName)) {
            $fileHasIssues = $true
        }

        # Auto-fix if requested
        if ($FixAutomatically) {
            Repair-AutoFixableIssue $file.FullName
        }

        # PSScriptAnalyzer check
        $results = Invoke-PSScriptAnalyzer $file.FullName

        if ($results.TotalIssues -gt 0) {
            $fileHasIssues = $true
            $overallResults.TotalIssues += $results.TotalIssues
            $overallResults.TotalErrors += $results.Errors
            $overallResults.TotalWarnings += $results.Warnings
            $overallResults.TotalInfo += $results.Information
        }

        # Show results
        Show-AnalysisResults $results $file.FullName

        # Additional best practice checks
        Test-ScriptBestPractice $file.FullName

        # Export individual report if requested
        if ($ReportPath) {
            $individualReportPath = $ReportPath -replace '\.txt$', "-$($file.BaseName).txt"
            Export-ValidationReport $results $file.FullName $individualReportPath
        }

        # Update overall stats
        if ($fileHasIssues) {
            $overallResults.FailedFiles++
        }
        else {
            $overallResults.PassedFiles++
        }
    }

    # Overall summary
    Write-ValidationLog "`n" + "="*60 "INFO"
    Write-ValidationLog "OVERALL VALIDATION SUMMARY" "INFO"
    Write-ValidationLog "="*60 "INFO"
    Write-ValidationLog "Total Files: $($overallResults.TotalFiles)" "INFO"
    Write-ValidationLog "Passed: $($overallResults.PassedFiles)" "SUCCESS"
    
    if ($overallResults.FailedFiles -gt 0) {
        Write-ValidationLog "Failed: $($overallResults.FailedFiles)" "ERROR"
    }
    else {
        Write-ValidationLog "Failed: $($overallResults.FailedFiles)" "SUCCESS"
    }
    
    if ($overallResults.TotalIssues -gt 0) {
        Write-ValidationLog "Total Issues: $($overallResults.TotalIssues)" "WARN"
    }
    else {
        Write-ValidationLog "Total Issues: $($overallResults.TotalIssues)" "SUCCESS"
    }
    
    if ($overallResults.TotalErrors -gt 0) {
        Write-ValidationLog "  Errors: $($overallResults.TotalErrors)" "ERROR"
    }
    else {
        Write-ValidationLog "  Errors: $($overallResults.TotalErrors)" "INFO"
    }
    
    if ($overallResults.TotalWarnings -gt 0) {
        Write-ValidationLog "  Warnings: $($overallResults.TotalWarnings)" "WARN"
    }
    else {
        Write-ValidationLog "  Warnings: $($overallResults.TotalWarnings)" "INFO"
    }
    
    Write-ValidationLog "  Information: $($overallResults.TotalInfo)" "INFO"

    if ($overallResults.TotalIssues -eq 0) {
        Write-ValidationLog "`nAll scripts passed validation! Excellent PowerShell coding practices." "SUCCESS"
    }
    else {
        Write-ValidationLog "`nValidation completed with issues. Review the results above." "WARN"
    }

}
catch {
    Write-ValidationLog "Validation failed: $($_.Exception.Message)" "ERROR"
    exit 1
}

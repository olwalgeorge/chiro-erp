#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Standardize and Verify Kubernetes Manifests

.DESCRIPTION
    This script standardizes the naming of Kubernetes manifests and verifies
    they are consistent with the consolidated services structure, including
    correct ports, service names, and configurations.
    
.PARAMETER Verify
    Only verify manifests without making changes
    
.PARAMETER Fix
    Automatically fix inconsistencies found during verification
    
.EXAMPLE
    .\standardize-k8s-manifests.ps1
    .\standardize-k8s-manifests.ps1 -Verify
    .\standardize-k8s-manifests.ps1 -Fix
#>

[CmdletBinding()]
param(
    [switch]$Verify,
    [switch]$Fix
)

$ErrorActionPreference = "Stop"

# Define consolidated services configuration
$ConsolidatedServices = @{
    "core-business-service" = @{
        "port" = 8080
        "modules" = @("finance", "inventory", "sales", "procurement", "manufacturing")
        "database" = "chiro_core_business"
    }
    "operations-management-service" = @{
        "port" = 8081
        "modules" = @("fleet", "project", "fieldservice", "repair")
        "database" = "chiro_operations"
    }
    "customer-relations-service" = @{
        "port" = 8082
        "modules" = @("crm")
        "database" = "chiro_customer_relations"
    }
    "platform-services" = @{
        "port" = 8083
        "modules" = @("analytics", "notifications", "tenant-management", "billing")
        "database" = "chiro_platform"
    }
    "workforce-management-service" = @{
        "port" = 8084
        "modules" = @("hr", "user-management")
        "database" = "chiro_workforce"
    }
}

# Initialize counters
$Script:TotalChecks = 0
$Script:PassedChecks = 0
$Script:FailedChecks = 0
$Script:FixedIssues = 0
$Script:Issues = @()

function Write-StatusMessage {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "ERROR" { "Red" }
        "WARN" { "Yellow" }
        "SUCCESS" { "Green" }
        default { "White" }
    }
    Write-Information "[$timestamp] [$Level] $Message" -InformationAction Continue
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Write-CheckResult {
    param(
        [string]$Check,
        [string]$Status,
        [string]$Message = "",
        [string]$Details = ""
    )
    
    $Script:TotalChecks++
    
    $icon = switch ($Status) {
        "PASS" { "✅"; $Script:PassedChecks++ }
        "FAIL" { "❌"; $Script:FailedChecks++ }
        "WARN" { "⚠️ " }
        "INFO" { "ℹ️ " }
        "FIXED" { "🔧"; $Script:FixedIssues++ }
    }
    
    $color = switch ($Status) {
        "PASS" { "Green" }
        "FAIL" { "Red" }
        "WARN" { "Yellow" }
        "INFO" { "Cyan" }
        "FIXED" { "Magenta" }
    }
    
    Write-Information "$icon $Check" -InformationAction Continue
    Write-Host "$icon $Check" -ForegroundColor $color
    if ($Message) {
        Write-Information "   $Message" -InformationAction Continue
        Write-Host "   $Message" -ForegroundColor Gray
    }
    if ($Details) {
        Write-Information "   Details: $Details" -InformationAction Continue
        Write-Host "   Details: $Details" -ForegroundColor DarkGray
    }
    
    if ($Status -eq "FAIL") {
        $Script:Issues += @{
            Check   = $Check
            Message = $Message
            Details = $Details
        }
    }
}

$ServicesPath = ".\kubernetes\services"
$Services = @(
    "api-gateway",
    "core-business-service", 
    "customer-relations-service",
    "operations-management-service",
    "platform-services",
    "workforce-management-service"
)

function Set-ManifestNaming {
    [CmdletBinding(SupportsShouldProcess)]
    param()
    
    Write-StatusMessage "Standardizing Kubernetes manifest naming..."

    foreach ($service in $Services) {
        $servicePath = Join-Path $ServicesPath $service
        
        if (Test-Path $servicePath) {
            Write-StatusMessage "Processing service: $service"
            
            # Expected files
            $expectedFiles = @{
                "$service-deployment.yaml" = @("deployment.yml", "deployment.yaml", "$service.yml")
                "$service-service.yaml"    = @("service.yml", "service.yaml")
                "$service-configmap.yaml"  = @("configmap.yml", "configmap.yaml", "config.yml")
                "$service-ingress.yaml"    = @("ingress.yml", "ingress.yaml")
            }
            
            foreach ($expectedFile in $expectedFiles.Keys) {
                $expectedPath = Join-Path $servicePath $expectedFile
                $found = $false
                
                foreach ($possibleName in $expectedFiles[$expectedFile]) {
                    $possiblePath = Join-Path $servicePath $possibleName
                    if (Test-Path $possiblePath) {
                        if ($possiblePath -ne $expectedPath) {
                            Write-StatusMessage "Renaming $possibleName to $expectedFile" "SUCCESS"
                            if (-not $Verify -and $PSCmdlet.ShouldProcess($possiblePath, "Rename to $expectedFile")) {
                                Move-Item $possiblePath $expectedPath -Force
                            }
                        }
                        $found = $true
                        break
                    }
                }
                
                if (-not $found -and $expectedFile -like "*deployment*") {
                    Write-StatusMessage "Missing deployment manifest for $service" "WARN"
                }
                elseif (-not $found -and $expectedFile -like "*service*") {
                    Write-StatusMessage "Missing service manifest for $service" "WARN"
                }
            }
        }
        else {
            Write-StatusMessage "Service directory not found: $service" "ERROR"
        }
    }
}

function Test-ManifestConsistency {
    Write-Information "`n🔍 Verifying Kubernetes Manifest Consistency..." -InformationAction Continue
    Write-Host "`n🔍 Verifying Kubernetes Manifest Consistency..." -ForegroundColor Cyan
    
    foreach ($service in $Services) {
        if ($service -eq "api-gateway") {
            continue # Skip API gateway as it's not a consolidated service
        }
        
        $servicePath = Join-Path $ServicesPath $service
        
        if (-not (Test-Path $servicePath)) {
            Write-CheckResult "Service directory exists: $service" "FAIL" "Directory not found at $servicePath"
            continue
        }
        
        Write-CheckResult "Service directory exists: $service" "PASS"
        
        # Check deployment manifest
        $deploymentFile = Join-Path $servicePath "$service-deployment.yaml"
        if (Test-Path $deploymentFile) {
            Write-CheckResult "Deployment manifest exists: $service" "PASS"
            Test-DeploymentManifest -ServiceName $service -FilePath $deploymentFile
        }
        else {
            Write-CheckResult "Deployment manifest exists: $service" "FAIL" "Missing deployment manifest"
        }
        
        # Check service manifest
        $serviceFile = Join-Path $servicePath "$service-service.yaml"
        if (Test-Path $serviceFile) {
            Write-CheckResult "Service manifest exists: $service" "PASS"
            Test-ServiceManifest -ServiceName $service -FilePath $serviceFile
        }
        else {
            Write-CheckResult "Service manifest exists: $service" "FAIL" "Missing service manifest"
        }
        
        # Check configmap if it exists
        $configMapFile = Join-Path $servicePath "$service-configmap.yaml"
        if (Test-Path $configMapFile) {
            Write-CheckResult "ConfigMap manifest exists: $service" "PASS"
            Test-ConfigMapManifest -ServiceName $service -FilePath $configMapFile
        }
    }
}

function Test-DeploymentManifest {
    param(
        [string]$ServiceName,
        [string]$FilePath
    )
    
    if (-not $ConsolidatedServices.ContainsKey($ServiceName)) {
        return
    }
    
    $serviceConfig = $ConsolidatedServices[$ServiceName]
    $content = Get-Content $FilePath -Raw
    
    # Check container port
    $expectedPort = $serviceConfig.port
    if ($content -match "containerPort:\s*(\d+)") {
        $actualPort = [int]$matches[1]
        if ($actualPort -eq $expectedPort) {
            Write-CheckResult "$ServiceName deployment port" "PASS" "Port $expectedPort correctly configured"
        }
        else {
            Write-CheckResult "$ServiceName deployment port" "FAIL" "Expected port $expectedPort but found $actualPort"
            if ($Fix) {
                $newContent = $content -replace "containerPort:\s*\d+", "containerPort: $expectedPort"
                Set-Content -Path $FilePath -Value $newContent -Encoding UTF8
                Write-CheckResult "$ServiceName deployment port fixed" "FIXED" "Updated port to $expectedPort"
            }
        }
    }
    else {
        Write-CheckResult "$ServiceName deployment port" "FAIL" "No containerPort found in deployment"
    }
    
    # Check image name consistency
    if ($content -match "image:\s*([^\s]+)") {
        $imageName = $matches[1]
        if ($imageName -like "*$ServiceName*" -or $imageName -like "*chiro*") {
            Write-CheckResult "$ServiceName deployment image" "PASS" "Image name appears consistent"
        }
        else {
            Write-CheckResult "$ServiceName deployment image" "WARN" "Image name may not be consistent: $imageName"
        }
    }
}

function Test-ServiceManifest {
    param(
        [string]$ServiceName,
        [string]$FilePath
    )
    
    if (-not $ConsolidatedServices.ContainsKey($ServiceName)) {
        return
    }
    
    $serviceConfig = $ConsolidatedServices[$ServiceName]
    $content = Get-Content $FilePath -Raw
    
    # Check service port
    $expectedPort = $serviceConfig.port
    if ($content -match "port:\s*(\d+)") {
        $actualPort = [int]$matches[1]
        if ($actualPort -eq $expectedPort) {
            Write-CheckResult "$ServiceName service port" "PASS" "Service port $expectedPort correctly configured"
        }
        else {
            Write-CheckResult "$ServiceName service port" "FAIL" "Expected service port $expectedPort but found $actualPort"
            if ($Fix) {
                $newContent = $content -replace "port:\s*\d+", "port: $expectedPort"
                Set-Content -Path $FilePath -Value $newContent -Encoding UTF8
                Write-CheckResult "$ServiceName service port fixed" "FIXED" "Updated service port to $expectedPort"
            }
        }
    }
    else {
        Write-CheckResult "$ServiceName service port" "FAIL" "No port found in service"
    }
    
    # Check target port
    if ($content -match "targetPort:\s*(\d+)") {
        $actualTargetPort = [int]$matches[1]
        if ($actualTargetPort -eq $expectedPort) {
            Write-CheckResult "$ServiceName service targetPort" "PASS" "Target port $expectedPort correctly configured"
        }
        else {
            Write-CheckResult "$ServiceName service targetPort" "FAIL" "Expected target port $expectedPort but found $actualTargetPort"
            if ($Fix) {
                $newContent = $content -replace "targetPort:\s*\d+", "targetPort: $expectedPort"
                Set-Content -Path $FilePath -Value $newContent -Encoding UTF8
                Write-CheckResult "$ServiceName service targetPort fixed" "FIXED" "Updated target port to $expectedPort"
            }
        }
    }
}

function Test-ConfigMapManifest {
    param(
        [string]$ServiceName,
        [string]$FilePath
    )
    
    if (-not $ConsolidatedServices.ContainsKey($ServiceName)) {
        return
    }
    
    $serviceConfig = $ConsolidatedServices[$ServiceName]
    $content = Get-Content $FilePath -Raw
    
    # Check database configuration
    $expectedDatabase = $serviceConfig.database
    if ($content -match $expectedDatabase) {
        Write-CheckResult "$ServiceName configmap database" "PASS" "Database $expectedDatabase correctly configured"
    }
    else {
        Write-CheckResult "$ServiceName configmap database" "WARN" "Database configuration may need review"
    }
    
    # Check port configuration in configmap
    $expectedPort = $serviceConfig.port
    if ($content -match "quarkus\.http\.port.*$expectedPort") {
        Write-CheckResult "$ServiceName configmap port" "PASS" "Port $expectedPort correctly configured in configmap"
    }
    else {
        Write-CheckResult "$ServiceName configmap port" "WARN" "Port configuration in configmap may need review"
    }
}

function Show-Summary {
    $summaryMessage = "`n" + "="*70
    Write-Information $summaryMessage -InformationAction Continue
    Write-Host $summaryMessage -ForegroundColor Cyan
    
    $titleMessage = "📊 KUBERNETES MANIFEST VERIFICATION SUMMARY"
    Write-Information $titleMessage -InformationAction Continue
    Write-Host $titleMessage -ForegroundColor Cyan
    
    $separatorMessage = "="*70
    Write-Information $separatorMessage -InformationAction Continue
    Write-Host $separatorMessage -ForegroundColor Cyan
    
    Write-Host "📈 Results:" -ForegroundColor White
    Write-Host "   ✅ Passed: $Script:PassedChecks" -ForegroundColor Green
    Write-Host "   ❌ Failed: $Script:FailedChecks" -ForegroundColor Red
    Write-Host "   🔧 Fixed: $Script:FixedIssues" -ForegroundColor Magenta
    Write-Host "   📊 Total: $Script:TotalChecks" -ForegroundColor Gray
    
    if ($Script:TotalChecks -gt 0) {
        $successRate = [math]::Round(($Script:PassedChecks / $Script:TotalChecks) * 100, 1)
        Write-Host "   🎯 Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })
    }
    
    Write-Host "`n🏗️  Consolidated Services Configuration:" -ForegroundColor White
    foreach ($serviceName in $ConsolidatedServices.Keys) {
        $serviceConfig = $ConsolidatedServices[$serviceName]
        $modulesStr = $serviceConfig.modules -join ", "
        Write-Host "   📦 $serviceName" -ForegroundColor Cyan
        Write-Host "      Port: $($serviceConfig.port) | Database: $($serviceConfig.database)" -ForegroundColor Gray
        Write-Host "      Modules: $modulesStr" -ForegroundColor Gray
    }
    
    if ($Script:Issues.Count -gt 0) {
        Write-Host "`n⚠️  Issues Found:" -ForegroundColor Yellow
        foreach ($issue in $Script:Issues) {
            Write-Host "   • $($issue.Check): $($issue.Message)" -ForegroundColor Red
        }
        
        if (-not $Fix) {
            Write-Host "`n💡 Run with -Fix to automatically resolve fixable issues" -ForegroundColor Cyan
        }
    }
    
    if ($Script:FailedChecks -eq 0 -and $Script:Issues.Count -eq 0) {
        Write-Host "`n🎉 All Kubernetes manifest checks passed!" -ForegroundColor Green
        Write-Host "🚀 Your manifests are consistent with the consolidated services!" -ForegroundColor Green
    }
}

# Main execution
$titleMessage = "🔍 Kubernetes Manifest Standardization and Verification"
Write-Information $titleMessage -InformationAction Continue
Write-Host $titleMessage -ForegroundColor Cyan

$separatorMessage = "======================================================="
Write-Information $separatorMessage -InformationAction Continue
Write-Host $separatorMessage -ForegroundColor Cyan

if ($Verify) {
    $verifyMessage = "🔍 Verification-only mode enabled"
    Write-Information $verifyMessage -InformationAction Continue
    Write-Host $verifyMessage -ForegroundColor Yellow
}
elseif ($Fix) {
    $fixMessage = "🔧 Auto-fix mode enabled"
    Write-Information $fixMessage -InformationAction Continue
    Write-Host $fixMessage -ForegroundColor Yellow
}

if (-not $Verify) {
    Set-ManifestNaming
    Write-StatusMessage "Manifest naming standardization completed!" "SUCCESS"
}

Test-ManifestConsistency
Show-Summary

# Exit with appropriate code
if ($Script:FailedChecks -gt 0) {
    exit 1
}
else {
    exit 0
}

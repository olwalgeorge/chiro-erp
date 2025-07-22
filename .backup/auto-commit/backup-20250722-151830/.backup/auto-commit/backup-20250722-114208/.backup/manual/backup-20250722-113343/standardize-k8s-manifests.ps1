#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Standardize Kubernetes Manifest Naming

.DESCRIPTION
    This script standardizes the naming of Kubernetes manifests to match
    the expected naming convention used by the deployment scripts.
#>

[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $(if ($Level -eq "ERROR") { "Red" } elseif ($Level -eq "WARN") { "Yellow" } elseif ($Level -eq "SUCCESS") { "Green" } else { "White" })
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

Write-Log "Standardizing Kubernetes manifest naming..."

foreach ($service in $Services) {
    $servicePath = Join-Path $ServicesPath $service
    
    if (Test-Path $servicePath) {
        Write-Log "Processing service: $service"
        
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
                        Write-Log "Renaming $possibleName to $expectedFile" "SUCCESS"
                        Move-Item $possiblePath $expectedPath -Force
                    }
                    $found = $true
                    break
                }
            }
            
            if (-not $found -and $expectedFile -like "*deployment*") {
                Write-Log "Missing deployment manifest for $service" "WARN"
            }
            elseif (-not $found -and $expectedFile -like "*service*") {
                Write-Log "Missing service manifest for $service" "WARN"
            }
        }
    }
    else {
        Write-Log "Service directory not found: $service" "ERROR"
    }
}

Write-Log "Manifest naming standardization completed!" "SUCCESS"

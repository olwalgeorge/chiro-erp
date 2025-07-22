#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Chiro ERP Kubernetes Deployment Manager

.DESCRIPTION
    This script provides comprehensive Kubernetes deployment management for the Chiro ERP system,
    including namespace management, service deployment, scaling, and monitoring.

.PARAMETER Command
    The Kubernetes deployment command to execute

.PARAMETER Environment
    Target environment (dev, staging, prod)

.PARAMETER ServiceName
    Specific services to deploy (comma-separated)

.PARAMETER Namespace
    Kubernetes namespace to deploy to

.PARAMETER Force
    Force operations (skip confirmation prompts)

.PARAMETER Verbose
    Enable verbose output

.EXAMPLE
    .\k8s-deploy.ps1 -Command deploy -Environment dev
    .\k8s-deploy.ps1 -Command scale -Environment prod -ServiceName "core-business-service" -Replicas 3

.NOTES
    Requires kubectl to be installed and configured with cluster access
#>

[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory = $true, HelpMessage = "The Kubernetes deployment command to execute")]
    [ValidateSet("deploy", "delete", "scale", "status", "logs", "port-forward", "rollback", "clean")]
    [string]$Command,

    [Parameter(Mandatory = $false, HelpMessage = "Target environment")]
    [ValidateSet("dev", "staging", "prod")]
    [string]$Environment = "dev",

    [Parameter(Mandatory = $false, HelpMessage = "Specific services to deploy (comma-separated)")]
    [ValidateNotNull()]
    [string]$ServiceName = "",

    [Parameter(Mandatory = $false, HelpMessage = "Kubernetes namespace")]
    [ValidatePattern('^[a-z0-9]([-a-z0-9]*[a-z0-9])?$')]
    [string]$Namespace = "",

    [Parameter(Mandatory = $false, HelpMessage = "Number of replicas for scaling")]
    [ValidateRange(0, 20)]
    [int]$Replicas = 1,

    [Parameter(Mandatory = $false, HelpMessage = "Force operations")]
    [switch]$Force,

    [Parameter(Mandatory = $false, HelpMessage = "Enable verbose output")]
    [switch]$VerboseOutput
)

# Configuration
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$InformationPreference = "Continue"

# Get script directory
$script:ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$script:KubernetesPath = Join-Path $script:ProjectRoot "kubernetes"

# Service definitions
$script:AllServiceCollection = @(
    "core-business-service",
    "operations-management-service",
    "customer-relations-service",
    "platform-services",
    "workforce-management-service",
    "api-gateway"
)

$script:InfrastructureServiceCollection = @(
    "postgres",
    "redis",
    "kafka",
    "zookeeper"
)

# Environment configurations
$script:EnvironmentConfiguration = @{
    "dev"     = @{
        Namespace       = "chiro-dev"
        Replicas        = 1
        ResourceProfile = "minimal"
        IngressDomain   = "dev.chiro.local"
        ImageTag        = "dev"
        PullPolicy      = "Always"
    }
    "staging" = @{
        Namespace       = "chiro-staging"
        Replicas        = 2
        ResourceProfile = "standard"
        IngressDomain   = "staging.chiro.com"
        ImageTag        = "staging"
        PullPolicy      = "Always"
    }
    "prod"    = @{
        Namespace       = "chiro-prod"
        Replicas        = 3
        ResourceProfile = "production"
        IngressDomain   = "chiro.com"
        ImageTag        = "latest"
        PullPolicy      = "IfNotPresent"
    }
}

# Automatic namespace setting
if ([string]::IsNullOrWhiteSpace($Namespace)) {
    $Namespace = $script:EnvironmentConfiguration[$Environment].Namespace
}

function Write-DeploymentLog {
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
        "SUCCESS" { Write-Information $formattedMessage -InformationAction Continue }
        default { Write-Information $formattedMessage -InformationAction Continue }
    }
}

function Test-KubernetesConnection {
    [CmdletBinding()]
    [OutputType([void])]
    param()

    Write-DeploymentLog "Testing Kubernetes connection..."

    try {
        # Test kubectl availability
        $null = kubectl version --client=true 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "kubectl command failed"
        }

        $clientVersion = kubectl version --client=true --output=json | ConvertFrom-Json
        Write-DeploymentLog "kubectl client version: $($clientVersion.clientVersion.gitVersion)" "SUCCESS"

        # Test cluster connectivity
        $null = kubectl cluster-info --request-timeout=10s 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Cluster connection failed"
        }

        $clusterInfo = kubectl cluster-info
        Write-DeploymentLog "Cluster connection successful" "SUCCESS"

        # Test namespace access
        $null = kubectl get namespace $Namespace 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-DeploymentLog "Namespace '$Namespace' does not exist. It will be created." "WARN"
        }
        else {
            Write-DeploymentLog "Namespace '$Namespace' is accessible" "SUCCESS"
        }
    }
    catch {
        Write-DeploymentLog "Kubernetes connectivity test failed: $($_.Exception.Message)" "ERROR"
        throw "Kubernetes connectivity test failed"
    }
}

function New-K8sNamespace {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param()

    if ($PSCmdlet.ShouldProcess($Namespace, "Create namespace")) {
        Write-DeploymentLog "Creating namespace: $Namespace"

        $namespaceYaml = @"
apiVersion: v1
kind: Namespace
metadata:
  name: $Namespace
  labels:
    name: $Namespace
    environment: $Environment
    managed-by: chiro-deployment-script
"@

        $tempFile = [System.IO.Path]::GetTempFileName()
        try {
            Set-Content -Path $tempFile -Value $namespaceYaml -Encoding UTF8
            kubectl apply -f $tempFile

            if ($LASTEXITCODE -eq 0) {
                Write-DeploymentLog "Namespace '$Namespace' created successfully" "SUCCESS"
            }
            else {
                throw "Failed to create namespace"
            }
        }
        finally {
            if (Test-Path $tempFile) {
                Remove-Item $tempFile -Force
            }
        }
    }
}

function Get-K8sManifest {
    [CmdletBinding()]
    [OutputType([string[]])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    $manifestList = @()
    $config = $script:EnvironmentConfiguration[$Environment]

    # Infrastructure manifests
    $infraManifests = @(
        "postgres-deployment.yaml",
        "postgres-service.yaml",
        "redis-deployment.yaml",
        "redis-service.yaml",
        "kafka-deployment.yaml",
        "kafka-service.yaml"
    )

    foreach ($manifest in $infraManifests) {
        $manifestPath = Join-Path $script:KubernetesPath "infrastructure" $manifest
        if (Test-Path $manifestPath) {
            $manifestList += $manifestPath
        }
    }

    # Service manifests
    $targetServiceList = if ($ServiceList.Count -gt 0) { $ServiceList } else { $script:AllServiceCollection }

    foreach ($service in $targetServiceList) {
        $serviceManifests = @(
            "$service-configmap.yaml",
            "$service-deployment.yaml",
            "$service-service.yaml",
            "$service-ingress.yaml"
        )

        foreach ($manifest in $serviceManifests) {
            $manifestPath = Join-Path $script:KubernetesPath "services" $manifest
            if (Test-Path $manifestPath) {
                $manifestList += $manifestPath
            }
        }
    }

    return $manifestList
}

function Invoke-K8sDeployment {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    if ($PSCmdlet.ShouldProcess("Kubernetes services", "Deploy")) {
        Write-DeploymentLog "Deploying to Kubernetes - Environment: $Environment, Namespace: $Namespace"

        # Ensure namespace exists
        $null = kubectl get namespace $Namespace 2>$null
        if ($LASTEXITCODE -ne 0) {
            New-K8sNamespace
        }

        # Get manifest files
        $manifestList = Get-K8sManifest $ServiceList

        if ($manifestList.Count -eq 0) {
            Write-DeploymentLog "No manifest files found for deployment" "ERROR"
            throw "No manifest files found"
        }

        Write-DeploymentLog "Found $($manifestList.Count) manifest files to deploy"

        # Apply manifests
        foreach ($manifest in $manifestList) {
            Write-DeploymentLog "Applying manifest: $(Split-Path -Leaf $manifest)"

            try {
                kubectl apply -f $manifest -n $Namespace

                if ($LASTEXITCODE -ne 0) {
                    throw "Failed to apply manifest: $manifest"
                }
            }
            catch {
                Write-DeploymentLog "Failed to apply manifest $manifest`: $($_.Exception.Message)" "ERROR"
                throw
            }
        }

        Write-DeploymentLog "Deployment completed successfully" "SUCCESS"

        # Wait for rollout
        Write-DeploymentLog "Waiting for deployments to be ready..."
        Start-Sleep 10
        Show-K8sStatus
    }
}

function Remove-K8sDeployment {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    if ($PSCmdlet.ShouldProcess("Kubernetes services", "Delete")) {
        Write-DeploymentLog "Deleting Kubernetes resources - Environment: $Environment, Namespace: $Namespace"

        if ($Force -or $PSCmdlet.ShouldContinue("This will delete all resources in namespace '$Namespace'", "Confirm Deletion")) {
            if ($ServiceList.Count -gt 0) {
                # Delete specific services
                foreach ($service in $ServiceList) {
                    Write-DeploymentLog "Deleting service: $service"
                    kubectl delete deployment, service, ingress, configmap -l "app=$service" -n $Namespace --ignore-not-found=true
                }
            }
            else {
                # Delete entire namespace
                Write-DeploymentLog "Deleting namespace: $Namespace"
                kubectl delete namespace $Namespace --ignore-not-found=true
            }

            Write-DeploymentLog "Deletion completed successfully" "SUCCESS"
        }
        else {
            Write-DeploymentLog "Deletion cancelled by user" "INFO"
        }
    }
}

function Set-K8sServiceScale {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$ServiceList,

        [Parameter(Mandatory = $true)]
        [int]$ReplicaCount
    )

    if ($PSCmdlet.ShouldProcess("Services", "Scale to $ReplicaCount replicas")) {
        Write-DeploymentLog "Scaling services to $ReplicaCount replicas"

        foreach ($service in $ServiceList) {
            Write-DeploymentLog "Scaling $service to $ReplicaCount replicas"

            kubectl scale deployment $service --replicas=$ReplicaCount -n $Namespace

            if ($LASTEXITCODE -eq 0) {
                Write-DeploymentLog "$service scaled successfully" "SUCCESS"
            }
            else {
                Write-DeploymentLog "Failed to scale $service" "ERROR"
                throw "Failed to scale $service"
            }
        }

        Write-DeploymentLog "Scaling completed successfully" "SUCCESS"
    }
}

function Show-K8sStatus {
    [CmdletBinding()]
    [OutputType([void])]
    param()

    Write-DeploymentLog "Checking Kubernetes status for namespace: $Namespace"

    Write-DeploymentLog "`n=== Deployments ==="
    kubectl get deployments -n $Namespace -o wide

    Write-DeploymentLog "`n=== Services ==="
    kubectl get services -n $Namespace -o wide

    Write-DeploymentLog "`n=== Pods ==="
    kubectl get pods -n $Namespace -o wide

    Write-DeploymentLog "`n=== Ingress ==="
    kubectl get ingress -n $Namespace -o wide

    # Check pod health
    Write-DeploymentLog "`n=== Pod Health Check ==="
    $unhealthyPods = kubectl get pods -n $Namespace --field-selector=status.phase!=Running -o name 2>$null
    if ($unhealthyPods) {
        Write-DeploymentLog "Unhealthy pods found:" "WARN"
        $unhealthyPods | ForEach-Object { Write-DeploymentLog "  $_" "WARN" }
    }
    else {
        Write-DeploymentLog "All pods are running" "SUCCESS"
    }
}

function Show-K8sLog {
    [CmdletBinding()]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    if ($ServiceList.Count -eq 0) {
        Write-DeploymentLog "Showing logs for all pods in namespace: $Namespace"
        kubectl logs --all-containers=true --prefix=true -f -n $Namespace
    }
    else {
        foreach ($service in $ServiceList) {
            Write-DeploymentLog "Showing logs for service: $service"
            kubectl logs -f -l "app=$service" -n $Namespace --all-containers=true --prefix=true
        }
    }
}

function Start-K8sPortForward {
    [CmdletBinding()]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $true)]
        [string]$ServiceTarget,

        [Parameter(Mandatory = $true)]
        [string]$PortMapping
    )

    Write-DeploymentLog "Starting port forwarding for $ServiceTarget - $PortMapping"
    kubectl port-forward service/$ServiceTarget $PortMapping -n $Namespace
}

function Invoke-K8sRollback {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$ServiceList
    )

    if ($PSCmdlet.ShouldProcess("Services", "Rollback")) {
        foreach ($service in $ServiceList) {
            Write-DeploymentLog "Rolling back deployment: $service"

            kubectl rollout undo deployment/$service -n $Namespace

            if ($LASTEXITCODE -eq 0) {
                Write-DeploymentLog "$service rollback initiated" "SUCCESS"
            }
            else {
                Write-DeploymentLog "Failed to rollback $service" "ERROR"
                throw "Failed to rollback $service"
            }
        }

        Write-DeploymentLog "Rollback completed successfully" "SUCCESS"
    }
}

function Clear-K8sEnvironment {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param()

    if ($PSCmdlet.ShouldProcess("Kubernetes environment", "Clean")) {
        Write-DeploymentLog "Cleaning Kubernetes environment: $Environment"

        if ($Force -or $PSCmdlet.ShouldContinue("This will delete completed pods and failed deployments", "Confirm Cleanup")) {
            # Clean completed pods
            Write-DeploymentLog "Removing completed pods..."
            kubectl delete pods --field-selector=status.phase=Succeeded -n $Namespace --ignore-not-found=true

            # Clean failed pods
            Write-DeploymentLog "Removing failed pods..."
            kubectl delete pods --field-selector=status.phase=Failed -n $Namespace --ignore-not-found=true

            Write-DeploymentLog "Environment cleanup completed" "SUCCESS"
        }
        else {
            Write-DeploymentLog "Cleanup cancelled by user" "INFO"
        }
    }
}

function ConvertFrom-ServiceString {
    [CmdletBinding()]
    [OutputType([string[]])]
    param(
        [Parameter(Mandatory = $false)]
        [AllowEmptyString()]
        [string]$ServiceString
    )

    if ([string]::IsNullOrWhiteSpace($ServiceString)) {
        return @()
    }

    return $ServiceString.Split(',') | ForEach-Object { $_.Trim() }
}

# Main execution
try {
    Write-DeploymentLog "Chiro ERP Kubernetes Deployment Manager - Environment: $Environment, Command: $Command"

    Test-KubernetesConnection

    $serviceList = ConvertFrom-ServiceString $ServiceName

    switch ($Command) {
        "deploy" {
            Invoke-K8sDeployment $serviceList
        }
        "delete" {
            Remove-K8sDeployment $serviceList
        }
        "scale" {
            if ($serviceList.Count -eq 0) {
                Write-DeploymentLog "Service name is required for scaling operation" "ERROR"
                throw "Service name is required for scaling"
            }
            Set-K8sServiceScale $serviceList $Replicas
        }
        "status" {
            Show-K8sStatus
        }
        "logs" {
            Show-K8sLog $serviceList
        }
        "port-forward" {
            if ($serviceList.Count -ne 1) {
                Write-DeploymentLog "Exactly one service name is required for port forwarding" "ERROR"
                throw "Single service required for port forwarding"
            }
            Start-K8sPortForward $serviceList[0] "8080:8080"
        }
        "rollback" {
            if ($serviceList.Count -eq 0) {
                Write-DeploymentLog "Service name is required for rollback operation" "ERROR"
                throw "Service name is required for rollback"
            }
            Invoke-K8sRollback $serviceList
        }
        "clean" {
            Clear-K8sEnvironment
        }
    }

    Write-DeploymentLog "Kubernetes command completed successfully" "SUCCESS"
}
catch {
    Write-DeploymentLog "Kubernetes deployment failed: $($_.Exception.Message)" "ERROR"
    throw
}

#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Chiro ERP Deployment Manager - Build and Deploy Consolidated Services

.DESCRIPTION
    This script provides comprehensive deployment management for the Chiro ERP system,
    including building Docker images, running services, and managing environments.

.PARAMETER Command
    The deployment command to execute

.PARAMETER Environment
    Target environment (dev, staging, prod)

.PARAMETER ServiceName
    Specific services to deploy (comma-separated)

.PARAMETER Force
    Force operations (no-cache builds, remove volumes)

.PARAMETER Verbose
    Enable verbose output

.EXAMPLE
    .\deploy.ps1 -Command build -Environment dev
    .\deploy.ps1 -Command up -Environment prod -ServiceName "core-business-service,api-gateway"

.NOTES
    Requires Docker and Docker Compose to be installed and accessible
#>

[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory = $true, HelpMessage = "The deployment command to execute")]
    [ValidateSet("build", "up", "down", "restart", "logs", "status", "clean")]
    [string]$Command,

    [Parameter(Mandatory = $false, HelpMessage = "Target environment")]
    [ValidateSet("dev", "staging", "prod")]
    [string]$Environment = "dev",

    [Parameter(Mandatory = $false, HelpMessage = "Specific services to deploy (comma-separated)")]
    [ValidateNotNull()]
    [string]$ServiceName = "",

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
        ComposeFile = "docker-compose.enhanced-dev.yml"
        EnvFile     = ".env.dev"
        BuildTarget = "runtime"
        LogLevel    = "INFO"
    }
    "staging" = @{
        ComposeFile = "docker-compose.staging.yml"
        EnvFile     = ".env.staging"
        BuildTarget = "runtime"
        LogLevel    = "WARN"
    }
    "prod"    = @{
        ComposeFile = "docker-compose.production.yml"
        EnvFile     = ".env.prod"
        BuildTarget = "runtime"
        LogLevel    = "ERROR"
    }
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

function Test-Prerequisite {
    [CmdletBinding()]
    [OutputType([void])]
    param()

    Write-DeploymentLog "Checking prerequisites..."

    # Check Docker
    try {
        $null = docker --version 2>$null
        if ($LASTEXITCODE -eq 0) {
            $dockerVersion = docker --version
            Write-DeploymentLog "Docker found: $dockerVersion" "SUCCESS"
        }
        else {
            throw "Docker command failed"
        }
    }
    catch {
        Write-DeploymentLog "Docker is not installed or not accessible" "ERROR"
        throw "Docker prerequisite check failed"
    }

    # Check Docker Compose
    try {
        $null = docker compose version 2>$null
        if ($LASTEXITCODE -eq 0) {
            $composeVersion = docker compose version
            Write-DeploymentLog "Docker Compose found: $composeVersion" "SUCCESS"
        }
        else {
            throw "Docker Compose command failed"
        }
    }
    catch {
        Write-DeploymentLog "Docker Compose is not installed or not accessible" "ERROR"
        throw "Docker Compose prerequisite check failed"
    }

    # Check if running on Windows with WSL2
    if ($IsWindows) {
        try {
            $null = wsl --version 2>$null
            if ($LASTEXITCODE -eq 0) {
                $wslVersion = wsl --version
                Write-DeploymentLog "WSL2 detected: $wslVersion" "SUCCESS"
            }
            else {
                Write-DeploymentLog "WSL2 not detected. Docker Desktop should be configured for Windows containers" "WARN"
            }
        }
        catch {
            Write-DeploymentLog "WSL2 not detected. Docker Desktop should be configured for Windows containers" "WARN"
        }
    }
}

function Get-ComposeCommand {
    [CmdletBinding()]
    [OutputType([string[]])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    $config = $script:EnvironmentConfiguration[$Environment]
    $composeFile = Join-Path $script:ProjectRoot $config.ComposeFile
    $envFile = Join-Path $script:ProjectRoot $config.EnvFile

    $cmd = @("docker", "compose")

    if (Test-Path $composeFile) {
        $cmd += @("-f", $composeFile)
    }
    else {
        Write-DeploymentLog "Compose file not found: $composeFile" "ERROR"
        throw "Compose file not found: $composeFile"
    }

    if (Test-Path $envFile) {
        $cmd += @("--env-file", $envFile)
    }
    else {
        Write-DeploymentLog "Environment file not found: $envFile. Using defaults." "WARN"
    }

    return $cmd
}

function Invoke-ServiceBuild {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    if ($PSCmdlet.ShouldProcess("Services", "Build")) {
        Write-DeploymentLog "Building services for environment: $Environment"

        $buildCmd = Get-ComposeCommand
        $buildCmd += @("build")

        if ($Force) {
            $buildCmd += "--no-cache"
        }

        if ($ServiceList.Count -gt 0) {
            $buildCmd += $ServiceList
            Write-DeploymentLog "Building specific services: $($ServiceList -join ', ')"
        }
        else {
            Write-DeploymentLog "Building all services"
        }

        if ($VerboseOutput) {
            $buildCmd += "--progress=plain"
        }

        Write-DeploymentLog "Executing: $($buildCmd -join ' ')"
        & $buildCmd[0] $buildCmd[1..($buildCmd.Length - 1)]

        if ($LASTEXITCODE -ne 0) {
            Write-DeploymentLog "Build failed with exit code: $LASTEXITCODE" "ERROR"
            throw "Build failed with exit code: $LASTEXITCODE"
        }

        Write-DeploymentLog "Build completed successfully" "SUCCESS"
    }
}

function Start-ServiceCollection {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    if ($PSCmdlet.ShouldProcess("Services", "Start")) {
        Write-DeploymentLog "Starting services for environment: $Environment"

        # Start infrastructure services first
        Write-DeploymentLog "Starting infrastructure services..."
        $infraCmd = Get-ComposeCommand
        $infraCmd += @("up", "-d")
        $infraCmd += $script:InfrastructureServiceCollection

        & $infraCmd[0] $infraCmd[1..($infraCmd.Length - 1)]

        if ($LASTEXITCODE -ne 0) {
            Write-DeploymentLog "Failed to start infrastructure services" "ERROR"
            throw "Failed to start infrastructure services"
        }

        # Wait for infrastructure to be ready
        Write-DeploymentLog "Waiting for infrastructure services to be ready..."
        Start-Sleep 30

        # Start application services
        $appCmd = Get-ComposeCommand
        $appCmd += @("up", "-d")

        if ($ServiceList.Count -gt 0) {
            $appCmd += $ServiceList
            Write-DeploymentLog "Starting specific services: $($ServiceList -join ', ')"
        }
        else {
            Write-DeploymentLog "Starting all application services"
        }

        & $appCmd[0] $appCmd[1..($appCmd.Length - 1)]

        if ($LASTEXITCODE -ne 0) {
            Write-DeploymentLog "Failed to start application services" "ERROR"
            throw "Failed to start application services"
        }

        Write-DeploymentLog "Services started successfully" "SUCCESS"
        Show-ServiceStatus
    }
}

function Stop-ServiceCollection {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    if ($PSCmdlet.ShouldProcess("Services", "Stop")) {
        Write-DeploymentLog "Stopping services for environment: $Environment"

        $stopCmd = Get-ComposeCommand
        $stopCmd += "down"

        if ($Force) {
            $stopCmd += @("--volumes", "--remove-orphans")
        }

        & $stopCmd[0] $stopCmd[1..($stopCmd.Length - 1)]

        if ($LASTEXITCODE -ne 0) {
            Write-DeploymentLog "Failed to stop services" "ERROR"
            throw "Failed to stop services"
        }

        Write-DeploymentLog "Services stopped successfully" "SUCCESS"
    }
}

function Restart-ServiceCollection {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    if ($PSCmdlet.ShouldProcess("Services", "Restart")) {
        Write-DeploymentLog "Restarting services for environment: $Environment"

        $restartCmd = Get-ComposeCommand
        $restartCmd += "restart"

        if ($ServiceList.Count -gt 0) {
            $restartCmd += $ServiceList
        }

        & $restartCmd[0] $restartCmd[1..($restartCmd.Length - 1)]

        if ($LASTEXITCODE -ne 0) {
            Write-DeploymentLog "Failed to restart services" "ERROR"
            throw "Failed to restart services"
        }

        Write-DeploymentLog "Services restarted successfully" "SUCCESS"
    }
}

function Show-ServiceLog {
    [CmdletBinding()]
    [OutputType([void])]
    param(
        [Parameter(Mandatory = $false)]
        [string[]]$ServiceList = @()
    )

    $logsCmd = Get-ComposeCommand
    $logsCmd += @("logs", "-f", "--tail=100")

    if ($ServiceList.Count -gt 0) {
        $logsCmd += $ServiceList
    }

    & $logsCmd[0] $logsCmd[1..($logsCmd.Length - 1)]
}

function Show-ServiceStatus {
    [CmdletBinding()]
    [OutputType([void])]
    param()

    Write-DeploymentLog "Checking service status..."

    $statusCmd = Get-ComposeCommand
    $statusCmd += "ps"

    & $statusCmd[0] $statusCmd[1..($statusCmd.Length - 1)]

    # Check health of key services
    Write-DeploymentLog "`nHealth check results:"

    $healthChecks = @{
        "postgres"    = @("docker", "exec", "chiro-postgres-$Environment", "pg_isready", "-U", "chiro")
        "redis"       = @("docker", "exec", "chiro-redis-$Environment", "redis-cli", "ping")
        "api-gateway" = @("curl", "-f", "http://localhost:8090/q/health/ready")
    }

    foreach ($service in $healthChecks.Keys) {
        try {
            $healthCommand = $healthChecks[$service]
            $null = & $healthCommand[0] $healthCommand[1..($healthCommand.Length - 1)] 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-DeploymentLog "${service}: HEALTHY" "SUCCESS"
            }
            else {
                Write-DeploymentLog "${service}: UNHEALTHY" "ERROR"
            }
        }
        catch {
            Write-DeploymentLog "${service}: UNKNOWN" "WARN"
        }
    }
}

function Clear-Environment {
    [CmdletBinding(SupportsShouldProcess)]
    [OutputType([void])]
    param()

    if ($PSCmdlet.ShouldProcess("Environment", "Clean")) {
        Write-DeploymentLog "Cleaning environment: $Environment"

        # Stop all services
        Stop-ServiceCollection -Force

        # Remove unused images
        Write-DeploymentLog "Removing unused Docker images..."
        docker image prune -f

        # Remove unused volumes (only if forced)
        if ($Force) {
            Write-DeploymentLog "Removing unused Docker volumes..."
            docker volume prune -f
        }

        Write-DeploymentLog "Environment cleaned successfully" "SUCCESS"
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
    Write-DeploymentLog "Chiro ERP Deployment Manager - Environment: $Environment, Command: $Command"

    Test-Prerequisite

    $serviceList = ConvertFrom-ServiceString $ServiceName

    switch ($Command) {
        "build" {
            Invoke-ServiceBuild $serviceList
        }
        "up" {
            Invoke-ServiceBuild $serviceList
            Start-ServiceCollection $serviceList
        }
        "down" {
            Stop-ServiceCollection $serviceList
        }
        "restart" {
            Restart-ServiceCollection $serviceList
        }
        "logs" {
            Show-ServiceLog $serviceList
        }
        "status" {
            Show-ServiceStatus
        }
        "clean" {
            Clear-Environment
        }
    }

    Write-DeploymentLog "Deployment command completed successfully" "SUCCESS"
}
catch {
    Write-DeploymentLog "Deployment failed: $($_.Exception.Message)" "ERROR"
    throw
}

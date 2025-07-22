#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Development workflow automation for Chiro ERP
.DESCRIPTION
    This script provides common development workflows
#>

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("start", "stop", "restart", "status", "logs", "build", "test")]
    [string]$Action,
    
    [Parameter(Mandatory = $false)]
    [string]$Service = "",
    
    [Parameter(Mandatory = $false)]
    [switch]$Follow
)

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "=" * 60 -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan  
    Write-Host "=" * 60 -ForegroundColor Cyan
    Write-Host ""
}

function Start-DevEnvironment {
    Write-Header "Starting Chiro ERP Development Environment"
    
    # Copy environment template if .env doesn't exist
    if (!(Test-Path ".env")) {
        if (Test-Path ".env.template") {
            Copy-Item ".env.template" ".env"
            Write-Host "✓ Created .env file from template" -ForegroundColor Green
            Write-Host "⚠️  Please review and update .env with your settings" -ForegroundColor Yellow
        }
    }
    
    # Start infrastructure first
    Write-Host "Starting infrastructure services..." -ForegroundColor Yellow
    docker compose -f docker-compose.dev.yml up -d postgres redis zookeeper kafka
    
    # Wait for infrastructure
    Write-Host "Waiting for infrastructure to be ready..." -ForegroundColor Yellow
    Start-Sleep 20
    
    # Start observability
    Write-Host "Starting observability stack..." -ForegroundColor Yellow
    docker compose -f docker-compose.dev.yml up -d kafka-ui jaeger prometheus grafana
    
    # Start API Gateway
    Write-Host "Starting API Gateway..." -ForegroundColor Yellow
    docker compose -f docker-compose.dev.yml up -d api-gateway
    
    Write-Host "✓ Development environment started successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Available services:" -ForegroundColor Cyan
    Write-Host "- API Gateway: http://localhost:8080" -ForegroundColor White
    Write-Host "- Kafka UI: http://localhost:8090" -ForegroundColor White  
    Write-Host "- Grafana: http://localhost:3000 (admin/admin)" -ForegroundColor White
    Write-Host "- Prometheus: http://localhost:9090" -ForegroundColor White
    Write-Host "- Jaeger: http://localhost:16686" -ForegroundColor White
}

function Stop-DevEnvironment {
    Write-Header "Stopping Chiro ERP Development Environment"
    docker compose -f docker-compose.dev.yml down
    Write-Host "✓ Development environment stopped" -ForegroundColor Green
}

function Restart-DevEnvironment {
    Write-Header "Restarting Chiro ERP Development Environment"
    Stop-DevEnvironment
    Start-Sleep 5
    Start-DevEnvironment
}

function Get-ServiceStatus {
    Write-Header "Chiro ERP Service Status"
    docker compose -f docker-compose.dev.yml ps
}

function Show-ServiceLogs {
    param([string]$ServiceName)
    
    if ($ServiceName) {
        Write-Header "Logs for $ServiceName"
        if ($Follow) {
            docker compose -f docker-compose.dev.yml logs -f $ServiceName
        }
        else {
            docker compose -f docker-compose.dev.yml logs --tail=100 $ServiceName
        }
    }
    else {
        Write-Header "All Service Logs"
        if ($Follow) {
            docker compose -f docker-compose.dev.yml logs -f
        }
        else {
            docker compose -f docker-compose.dev.yml logs --tail=50
        }
    }
}

function Build-Services {
    Write-Header "Building Services"
    & ".\scripts\build-automation.ps1" -Action build -Service $Service
}

function Test-Services {
    Write-Header "Testing Services"
    & ".\scripts\build-automation.ps1" -Action test -Service $Service
}

# Main execution
switch ($Action) {
    "start" { Start-DevEnvironment }
    "stop" { Stop-DevEnvironment }
    "restart" { Restart-DevEnvironment }
    "status" { Get-ServiceStatus }
    "logs" { Show-ServiceLogs $Service }
    "build" { Build-Services }
    "test" { Test-Services }
}

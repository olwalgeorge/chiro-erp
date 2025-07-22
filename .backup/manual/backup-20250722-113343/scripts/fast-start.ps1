#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Fast startup script for Chiro ERP development environment
.DESCRIPTION
    Optimized startup sequence to minimize wait times and avoid common issues
#>

param(
    [Parameter(Mandatory = $false)]
    [switch]$SkipBuild,
    
    [Parameter(Mandatory = $false)]
    [switch]$Minimal
)

$ErrorActionPreference = "Stop"

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "=" * 60 -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan  
    Write-Host "=" * 60 -ForegroundColor Cyan
    Write-Host ""
}

function Write-Status {
    param([string]$Message, [string]$Status = "INFO")
    $color = switch ($Status) {
        "SUCCESS" { "Green" }
        "WARNING" { "Yellow" }
        "ERROR" { "Red" }
        default { "White" }
    }
    Write-Host "[$Status] $Message" -ForegroundColor $color
}

function Test-ContainerHealth {
    param([string]$ContainerName, [int]$MaxAttempts = 30)
    
    Write-Status "Waiting for $ContainerName to be healthy..." "INFO"
    
    for ($i = 1; $i -le $MaxAttempts; $i++) {
        $health = docker inspect --format='{{.State.Health.Status}}' $ContainerName 2>$null
        
        if ($health -eq "healthy") {
            Write-Status "$ContainerName is healthy" "SUCCESS"
            return $true
        }
        
        if ($i % 5 -eq 0) {
            Write-Status "$ContainerName health check $i/$MaxAttempts..." "INFO"
        }
        
        Start-Sleep 2
    }
    
    Write-Status "$ContainerName failed to become healthy" "ERROR"
    return $false
}

function Start-CoreInfrastructure {
    Write-Header "Phase 1: Starting Core Infrastructure"
    
    # Start database first
    Write-Status "Starting PostgreSQL..." "INFO"
    docker compose -f docker-compose.dev.yml up -d postgres
    
    if (!(Test-ContainerHealth "chiro-postgres")) {
        throw "PostgreSQL failed to start"
    }
    
    # Start Redis (fast)
    Write-Status "Starting Redis..." "INFO"
    docker compose -f docker-compose.dev.yml up -d redis
    
    if (!(Test-ContainerHealth "chiro-redis")) {
        throw "Redis failed to start"
    }
    
    Write-Status "Core infrastructure ready" "SUCCESS"
}

function Start-MessageBroker {
    Write-Header "Phase 2: Starting Message Broker"
    
    # Start Zookeeper first
    Write-Status "Starting Zookeeper..." "INFO"
    docker compose -f docker-compose.dev.yml up -d zookeeper
    
    if (!(Test-ContainerHealth "chiro-zookeeper")) {
        throw "Zookeeper failed to start"
    }
    
    # Start Kafka
    Write-Status "Starting Kafka..." "INFO"
    docker compose -f docker-compose.dev.yml up -d kafka
    
    if (!(Test-ContainerHealth "chiro-kafka")) {
        Write-Status "Checking Kafka logs..." "WARNING"
        docker compose -f docker-compose.dev.yml logs kafka --tail 20
        throw "Kafka failed to start"
    }
    
    Write-Status "Message broker ready" "SUCCESS"
}

function Start-Observability {
    Write-Header "Phase 3: Starting Observability Stack"
    
    if ($Minimal) {
        Write-Status "Skipping observability stack (minimal mode)" "INFO"
        return
    }
    
    # Start observability services (they don't have health checks dependencies)
    Write-Status "Starting monitoring services..." "INFO"
    docker compose -f docker-compose.dev.yml up -d jaeger prometheus grafana kafka-ui
    
    # Give them time to start
    Start-Sleep 10
    
    Write-Status "Observability stack started" "SUCCESS"
}

function Build-Services {
    if ($SkipBuild) {
        Write-Status "Skipping build (--SkipBuild specified)" "WARNING"
        return
    }
    
    Write-Header "Phase 4: Building Services"
    
    # Check if API Gateway needs building
    if (!(Test-Path "build/quarkus-app")) {
        Write-Status "Building API Gateway..." "INFO"
        ./gradlew build -x test --no-daemon --quiet
        
        if ($LASTEXITCODE -ne 0) {
            Write-Status "Build failed. Trying with verbose output..." "WARNING"
            ./gradlew build -x test --no-daemon
            
            if ($LASTEXITCODE -ne 0) {
                throw "Service build failed"
            }
        }
        
        Write-Status "Build completed successfully" "SUCCESS"
    }
    else {
        Write-Status "Build artifacts found, skipping build" "INFO"
    }
}

function Start-ApplicationServices {
    Write-Header "Phase 5: Starting Application Services"
    
    # Start API Gateway
    Write-Status "Starting API Gateway..." "INFO"
    docker compose -f docker-compose.dev.yml up -d api-gateway
    
    # Wait a bit for startup
    Start-Sleep 15
    
    # Check if it's accessible
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/health" -TimeoutSec 10
        Write-Status "API Gateway is responding" "SUCCESS"
    }
    catch {
        Write-Status "API Gateway may still be starting up..." "WARNING"
    }
}

function Show-ServiceStatus {
    Write-Header "Development Environment Status"
    
    docker compose -f docker-compose.dev.yml ps
    
    Write-Host ""
    Write-Status "Available services:" "SUCCESS"
    Write-Host "- API Gateway: http://localhost:8080" -ForegroundColor White
    if (!$Minimal) {
        Write-Host "- Kafka UI: http://localhost:8090" -ForegroundColor White  
        Write-Host "- Grafana: http://localhost:3000 (admin/admin)" -ForegroundColor White
        Write-Host "- Prometheus: http://localhost:9090" -ForegroundColor White
        Write-Host "- Jaeger: http://localhost:16686" -ForegroundColor White
    }
    Write-Host "- PostgreSQL: localhost:5432" -ForegroundColor White
    Write-Host "- Redis: localhost:6379" -ForegroundColor White
}

# Main execution
try {
    Write-Header "Chiro ERP Fast Startup"
    $startTime = Get-Date
    
    Start-CoreInfrastructure
    Start-MessageBroker
    Start-Observability
    Build-Services
    Start-ApplicationServices
    
    $duration = (Get-Date) - $startTime
    Write-Status "Startup completed in $($duration.TotalSeconds.ToString('F1')) seconds" "SUCCESS"
    
    Show-ServiceStatus
    
}
catch {
    Write-Status "Startup failed: $_" "ERROR"
    Write-Status "Showing container status..." "INFO"
    docker compose -f docker-compose.dev.yml ps
    exit 1
}

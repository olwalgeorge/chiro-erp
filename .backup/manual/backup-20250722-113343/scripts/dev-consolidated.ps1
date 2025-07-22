#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Development utilities for consolidated Chiro ERP services
#>

param(
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "restart", "logs", "build", "test", "clean")]
    [string]$Action = "start",
    
    [Parameter(Position=1)]
    [ValidateSet("all", "workforce-management-service", "customer-relations-service", "operations-management-service", "core-business-service", "platform-services")]
    [string]$Service = "all"
)

function Start-Services {
    param([string]$ServiceName)
    
    if ($ServiceName -eq "all") {
        Write-Host "Starting all consolidated services..." -ForegroundColor Green
        docker-compose -f docker-compose.consolidated.yml up -d
    } else {
        Write-Host "Starting $ServiceName..." -ForegroundColor Green
        docker-compose -f docker-compose.consolidated.yml up -d $ServiceName postgres kafka zookeeper
    }
}

function Stop-Services {
    param([string]$ServiceName)
    
    if ($ServiceName -eq "all") {
        Write-Host "Stopping all services..." -ForegroundColor Yellow
        docker-compose -f docker-compose.consolidated.yml down
    } else {
        Write-Host "Stopping $ServiceName..." -ForegroundColor Yellow
        docker-compose -f docker-compose.consolidated.yml stop $ServiceName
    }
}

function Show-Logs {
    param([string]$ServiceName)
    
    if ($ServiceName -eq "all") {
        docker-compose -f docker-compose.consolidated.yml logs -f
    } else {
        docker-compose -f docker-compose.consolidated.yml logs -f $ServiceName
    }
}

function Build-Services {
    param([string]$ServiceName)
    
    if ($ServiceName -eq "all") {
        Write-Host "Building all services..." -ForegroundColor Blue
        ./gradlew build
    } else {
        Write-Host "Building $ServiceName..." -ForegroundColor Blue
        ./gradlew ":consolidated-services:$ServiceName:build"
    }
}

function Test-Services {
    param([string]$ServiceName)
    
    if ($ServiceName -eq "all") {
        Write-Host "Testing all services..." -ForegroundColor Blue
        ./gradlew test
    } else {
        Write-Host "Testing $ServiceName..." -ForegroundColor Blue
        ./gradlew ":consolidated-services:$ServiceName:test"
    }
}

switch ($Action) {
    "start" { Start-Services $Service }
    "stop" { Stop-Services $Service }
    "restart" { 
        Stop-Services $Service
        Start-Sleep -Seconds 2
        Start-Services $Service
    }
    "logs" { Show-Logs $Service }
    "build" { Build-Services $Service }
    "test" { Test-Services $Service }
    "clean" {
        Write-Host "Cleaning build artifacts..." -ForegroundColor Yellow
        ./gradlew clean
        docker-compose -f docker-compose.consolidated.yml down --volumes
    }
}

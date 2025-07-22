# =============================================================================
# CHIRO ERP - COMPREHENSIVE DEPLOYMENT        } catch {
Write-Error "${service} build failed with exception: $($_.Exception.Message)"
$failedBuilds += $service
}UTION
# ==============            } catch {
Write-Error "Failed to deploy ${service}: $($_.Exception.Message)"
}============================================================
# This is the SINGLE, DEFINITIVE deployment script for the entire ERP system
# Combines infrastructure + application services with proper error handling

param(
    [Parameter(Mandatory = $false)]
    [ValidateSet("infrastructure", "applications", "full", "status", "cleanup")]
    [string]$Action = "full",
    
    [Parameter(Mandatory = $false)]
    [ValidateSet("dev", "prod")]
    [string]$Environment = "dev",
    
    [Parameter(Mandatory = $false)]
    [switch]$Force
)

# Color functions for better output
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Info { param($Message) Write-Host "ℹ️  $Message" -ForegroundColor Cyan }
function Write-Warning { param($Message) Write-Host "⚠️  $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }
function Write-Header { param($Message) Write-Host "`n🚀 $Message" -ForegroundColor Magenta -BackgroundColor Black }

Write-Header "CHIRO ERP COMPREHENSIVE DEPLOYMENT"
Write-Info "Action: $Action | Environment: $Environment"

# =============================================================================
# INFRASTRUCTURE DEPLOYMENT
# =============================================================================
function Deploy-Infrastructure {
    Write-Header "DEPLOYING INFRASTRUCTURE SERVICES"
    
    try {
        # Check if already running
        $runningServices = docker ps --format "table {{.Names}}" | Select-String "chiro-erp"
        if ($runningServices -and !$Force) {
            Write-Warning "Infrastructure services already running. Use -Force to restart."
            return $true
        }
        
        # Deploy infrastructure
        Write-Info "Starting PostgreSQL, Kafka, and Zookeeper..."
        docker-compose -f docker-compose.consolidated.yml up -d postgres kafka zookeeper
        
        # Wait for services to be healthy
        Write-Info "Waiting for services to become healthy..."
        $maxWait = 60
        $waited = 0
        
        do {
            Start-Sleep 5
            $waited += 5
            $postgresHealth = docker exec chiro-erp-postgres-1 pg_isready -h localhost 2>$null
            $kafkaHealth = docker exec chiro-erp-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list 2>$null
            
            if ($postgresHealth -and $kafkaHealth) {
                Write-Success "All infrastructure services are healthy!"
                return $true
            }
            
            Write-Info "Waiting for services... ($waited/$maxWait seconds)"
        } while ($waited -lt $maxWait)
        
        Write-Error "Infrastructure services failed to become healthy within $maxWait seconds"
        return $false
        
    }
    catch {
        Write-Error "Infrastructure deployment failed: $($_.Exception.Message)"
        return $false
    }
}

# =============================================================================
# APPLICATION DEPLOYMENT
# =============================================================================
function Deploy-Applications {
    Write-Header "DEPLOYING APPLICATION SERVICES"
    
    $services = @(
        "core-business-service",
        "operations-management-service", 
        "customer-relations-service",
        "platform-services",
        "workforce-management-service"
    )
    
    $successfulBuilds = @()
    $failedBuilds = @()
    
    foreach ($service in $services) {
        Write-Info "Building $service..."
        
        try {
            # Use optimized Docker build with resource constraints
            $buildResult = docker build `
                --progress=plain `
                --memory=3g `
                --build-arg SERVICE_NAME=$service `
                -f Dockerfile.consolidated `
                -t "chiro-erp/$service" `
                . 2>&1
            
            if ($LASTEXITCODE -eq 0) {
                Write-Success "$service built successfully"
                $successfulBuilds += $service
            }
            else {
                Write-Error "$service build failed"
                $failedBuilds += $service
                Write-Info "Build output: $($buildResult | Select-Object -Last 10)"
            }
            
        }
        catch {
            Write-Error "$service build failed with exception: $($_.Exception.Message)"
            $failedBuilds += $service
        }
    }
    
    # Deploy successful builds
    if ($successfulBuilds.Count -gt 0) {
        Write-Info "Deploying successfully built services..."
        
        foreach ($service in $successfulBuilds) {
            try {
                $port = 8080 + ($successfulBuilds.IndexOf($service))
                Write-Info "Starting $service on port $port..."
                
                docker run -d `
                    --name "chiro-erp-$service" `
                    --network chiro-erp_default `
                    -p "${port}:8080" `
                    -e "QUARKUS_HTTP_PORT=8080" `
                    -e "QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/chiro_erp" `
                    -e "QUARKUS_DATASOURCE_USERNAME=chiro_user" `
                    -e "QUARKUS_DATASOURCE_PASSWORD=chiro_password" `
                    -e "KAFKA_BOOTSTRAP_SERVERS=kafka:9092" `
                    "chiro-erp/$service"
                    
                Write-Success "$service deployed on port $port"
                
            }
            catch {
                Write-Error "Failed to deploy $service: $($_.Exception.Message)"
            }
        }
    }
    
    # Report results
    Write-Header "APPLICATION DEPLOYMENT SUMMARY"
    Write-Success "Successfully built: $($successfulBuilds -join ', ')"
    if ($failedBuilds.Count -gt 0) {
        Write-Warning "Failed builds: $($failedBuilds -join ', ')"
    }
    
    return @{
        Success = $successfulBuilds
        Failed  = $failedBuilds
    }
}

# =============================================================================
# STATUS CHECK
# =============================================================================
function Show-Status {
    Write-Header "CHIRO ERP SYSTEM STATUS"
    
    # Infrastructure status
    Write-Info "`nInfrastructure Services:"
    docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}" | Select-String "chiro-erp-(postgres|kafka|zookeeper)"
    
    # Application status
    Write-Info "`nApplication Services:"
    docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}" | Select-String "chiro-erp-.*-service"
    
    # Available images
    Write-Info "`nBuilt Images:"
    docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | Select-String "chiro-erp"
    
    # Health check URLs
    Write-Info "`nHealth Check URLs:"
    $runningApps = docker ps --filter "name=chiro-erp-*-service" --format "{{.Names}}\t{{.Ports}}"
    if ($runningApps) {
        $runningApps | ForEach-Object {
            $parts = $_ -split '\t'
            $name = $parts[0]
            $ports = $parts[1]
            if ($ports -match '(\d+)->8080') {
                $port = $matches[1]
                Write-Info "  http://localhost:$port/q/health"
            }
        }
    }
    else {
        Write-Warning "No application services currently running"
    }
}

# =============================================================================
# CLEANUP
# =============================================================================
function Cleanup-Deployment {
    Write-Header "CLEANING UP DEPLOYMENT"
    
    if ($Force -or (Read-Host "Are you sure you want to cleanup all services? (y/N)") -eq 'y') {
        Write-Info "Stopping and removing all containers..."
        docker-compose -f docker-compose.consolidated.yml down -v
        
        Write-Info "Removing application containers..."
        docker ps -a --filter "name=chiro-erp-*-service" --format "{{.Names}}" | ForEach-Object {
            docker rm -f $_ 2>$null
        }
        
        Write-Info "Removing built images..."
        docker images --filter "reference=chiro-erp/*" --format "{{.Repository}}:{{.Tag}}" | ForEach-Object {
            docker rmi $_ 2>$null
        }
        
        Write-Success "Cleanup completed"
    }
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================
try {
    switch ($Action) {
        "infrastructure" {
            $result = Deploy-Infrastructure
            if ($result) { Show-Status }
        }
        
        "applications" {
            $result = Deploy-Applications
            Show-Status
        }
        
        "full" {
            $infraResult = Deploy-Infrastructure
            if ($infraResult) {
                Start-Sleep 10  # Give infrastructure time to stabilize
                $appResult = Deploy-Applications
                Show-Status
                
                Write-Header "DEPLOYMENT COMPLETE"
                Write-Success "Infrastructure: Ready"
                Write-Success "Applications: $($appResult.Success.Count) deployed, $($appResult.Failed.Count) failed"
                
                if ($appResult.Success.Count -gt 0) {
                    Write-Info "`nAccess your ERP system:"
                    Write-Info "- Database: localhost:5432 (chiro_erp/chiro_user)"
                    Write-Info "- Kafka: localhost:9092"
                    Write-Info "- Applications: Check health endpoints above"
                }
            }
        }
        
        "status" {
            Show-Status
        }
        
        "cleanup" {
            Cleanup-Deployment
        }
    }
    
}
catch {
    Write-Error "Deployment failed: $($_.Exception.Message)"
    exit 1
}

Write-Header "CHIRO ERP DEPLOYMENT SCRIPT COMPLETED"

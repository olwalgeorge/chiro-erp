#!/usr/bin/env pwsh
<#
.SYNOPSIS
Refactors Kubernetes manifests to align with consolidated monolithic services architecture.

.DESCRIPTION
This script updates the Kubernetes configuration from 18 individual microservices
to 5 consolidated monolithic services, updating deployments, services, and ingress.

.PARAMETER BackupExisting
Create backup of existing Kubernetes configuration before refactoring.

.EXAMPLE
.\refactor-kubernetes-for-consolidated-services.ps1 -BackupExisting
#>

param(
    [switch]$BackupExisting = $true
)

# Consolidated services mapping
$ConsolidatedServices = @{
    "core-business-service"         = @{
        "port"    = 8081
        "modules" = @("billing", "finance", "sales", "inventory", "procurement", "manufacturing")
        "paths"   = @("/api/billing", "/api/finance", "/api/sales", "/api/inventory", "/api/procurement", "/api/manufacturing")
    }
    "operations-management-service" = @{
        "port"    = 8082
        "modules" = @("project", "fleet", "pos", "fieldservice")
        "paths"   = @("/api/project", "/api/fleet", "/api/pos", "/api/fieldservice")
    }
    "customer-relations-service"    = @{
        "port"    = 8083
        "modules" = @("crm", "repair")
        "paths"   = @("/api/crm", "/api/repair")
    }
    "platform-services-service"     = @{
        "port"    = 8084
        "modules" = @("analytics", "notifications")
        "paths"   = @("/api/analytics", "/api/notifications")
    }
    "workforce-management-service"  = @{
        "port"    = 8085
        "modules" = @("hr", "user-management", "tenant-management")
        "paths"   = @("/api/hr", "/api/users", "/api/tenants")
    }
}

# Original services to be replaced
$OriginalServices = @(
    "analytics-service", "billing-service", "crm-service", "fieldservice-service",
    "finance-service", "fleet-service", "hr-service", "inventory-service",
    "manufacturing-service", "notifications-service", "pos-service",
    "procurement-service", "project-service", "repair-service", "sales-service",
    "tenant-management-service", "user-management-service"
)

$ProjectRoot = $PSScriptRoot
$KubernetesDir = Join-Path $ProjectRoot "kubernetes"
$ServicesDir = Join-Path $KubernetesDir "services"
$IngressDir = Join-Path $KubernetesDir "ingress"

Write-Host "🔄 Starting Kubernetes refactoring for consolidated services..." -ForegroundColor Cyan

# Create backup if requested
if ($BackupExisting) {
    $BackupDir = Join-Path $ProjectRoot "kubernetes-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Write-Host "📦 Creating backup at: $BackupDir" -ForegroundColor Yellow
    Copy-Item -Path $KubernetesDir -Destination $BackupDir -Recurse -Force
    Write-Host "✅ Backup created successfully" -ForegroundColor Green
}

# Function to create deployment manifest
function New-DeploymentManifest {
    param(
        [string]$ServiceName,
        [int]$Port,
        [string[]]$Modules
    )
    
    $modulesStr = ($Modules -join ", ")
    
    return @"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: $ServiceName
  namespace: chiro-erp
  labels:
    app: $ServiceName
    version: v1
    tier: backend
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: $ServiceName
  template:
    metadata:
      labels:
        app: $ServiceName
        version: v1
    spec:
      containers:
      - name: $ServiceName
        image: chiro-erp/${ServiceName}:latest
        ports:
        - containerPort: $Port
          name: http
        env:
        - name: QUARKUS_HTTP_PORT
          value: "$Port"
        - name: QUARKUS_PROFILE
          value: "prod"
        - name: MODULES
          value: "$modulesStr"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /q/health/live
            port: $Port
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /q/health/ready
            port: $Port
          initialDelaySeconds: 5
          periodSeconds: 5
        startupProbe:
          httpGet:
            path: /q/health/started
            port: $Port
          initialDelaySeconds: 10
          periodSeconds: 5
          failureThreshold: 30
---
"@
}

# Function to create service manifest
function New-ServiceManifest {
    param(
        [string]$ServiceName,
        [int]$Port
    )
    
    return @"
apiVersion: v1
kind: Service
metadata:
  name: $ServiceName
  namespace: chiro-erp
  labels:
    app: $ServiceName
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: $Port
    protocol: TCP
    name: http
  selector:
    app: $ServiceName
---
"@
}

# Remove old service directories
Write-Host "🗑️ Removing old service manifests..." -ForegroundColor Yellow
foreach ($service in $OriginalServices) {
    $serviceDir = Join-Path $ServicesDir $service
    if (Test-Path $serviceDir) {
        Remove-Item -Path $serviceDir -Recurse -Force
        Write-Host "  ❌ Removed $service" -ForegroundColor Red
    }
}

# Create new consolidated service manifests
Write-Host "📁 Creating consolidated service manifests..." -ForegroundColor Cyan
foreach ($service in $ConsolidatedServices.Keys) {
    $serviceConfig = $ConsolidatedServices[$service]
    $serviceDir = Join-Path $ServicesDir $service
    
    # Create service directory
    New-Item -Path $serviceDir -ItemType Directory -Force | Out-Null
    
    # Create deployment manifest
    $deploymentContent = New-DeploymentManifest -ServiceName $service -Port $serviceConfig.port -Modules $serviceConfig.modules
    $deploymentPath = Join-Path $serviceDir "deployment.yml"
    $deploymentContent | Out-File -FilePath $deploymentPath -Encoding UTF8
    
    # Create service manifest
    $serviceContent = New-ServiceManifest -ServiceName $service -Port $serviceConfig.port
    $servicePath = Join-Path $serviceDir "service.yml"
    $serviceContent | Out-File -FilePath $servicePath -Encoding UTF8
    
    Write-Host "  ✅ Created manifests for $service (port: $($serviceConfig.port))" -ForegroundColor Green
}

# Create new ingress configuration
Write-Host "🌐 Creating consolidated ingress configuration..." -ForegroundColor Cyan
$ingressContent = @"
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: chiro-erp-ingress
  namespace: chiro-erp
  labels:
    app: chiro-erp
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
    nginx.ingress.kubernetes.io/use-regex: "true"
    nginx.ingress.kubernetes.io/cors-allow-origin: "*"
    nginx.ingress.kubernetes.io/cors-allow-methods: "GET, POST, PUT, DELETE, OPTIONS"
    nginx.ingress.kubernetes.io/cors-allow-headers: "Content-Type, Authorization"
spec:
  rules:
  - host: chiro-erp.local
    http:
      paths:
      # API Gateway (remains separate)
      - path: /api/gateway
        pathType: Prefix
        backend:
          service:
            name: api-gateway
            port:
              number: 8080
"@

# Add paths for each consolidated service
foreach ($service in $ConsolidatedServices.Keys) {
    $serviceConfig = $ConsolidatedServices[$service]
    
    foreach ($path in $serviceConfig.paths) {
        $ingressContent += @"

      # $service - $path
      - path: $path
        pathType: Prefix
        backend:
          service:
            name: $service
            port:
              number: 80
"@
    }
}

$ingressContent += @"

  # TLS configuration (optional)
  # tls:
  # - hosts:
  #   - chiro-erp.local
  #   secretName: chiro-erp-tls
"@

$ingressPath = Join-Path $IngressDir "ingress.yml"
$ingressContent | Out-File -FilePath $ingressPath -Encoding UTF8
Write-Host "  ✅ Updated ingress configuration" -ForegroundColor Green

# Create namespace manifest if it doesn't exist
$namespaceDir = Join-Path $KubernetesDir "base"
if (-not (Test-Path $namespaceDir)) {
    New-Item -Path $namespaceDir -ItemType Directory -Force | Out-Null
}

$namespaceContent = @"
apiVersion: v1
kind: Namespace
metadata:
  name: chiro-erp
  labels:
    name: chiro-erp
    app: chiro-erp
---
"@

$namespacePath = Join-Path $namespaceDir "namespace.yml"
$namespaceContent | Out-File -FilePath $namespacePath -Encoding UTF8
Write-Host "  ✅ Created namespace manifest" -ForegroundColor Green

# Create kustomization.yml for easier deployment
$kustomizationContent = @"
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

# Namespace for all resources
namespace: chiro-erp

# Base resources
resources:
  - base/namespace.yml
  - services/core-business-service/deployment.yml
  - services/core-business-service/service.yml
  - services/operations-management-service/deployment.yml
  - services/operations-management-service/service.yml
  - services/customer-relations-service/deployment.yml
  - services/customer-relations-service/service.yml
  - services/platform-services-service/deployment.yml
  - services/platform-services-service/service.yml
  - services/workforce-management-service/deployment.yml
  - services/workforce-management-service/service.yml
  - api-gateway/deployment.yml
  - api-gateway/service.yml
  - ingress/ingress.yml

# Common labels
commonLabels:
  app.kubernetes.io/name: chiro-erp
  app.kubernetes.io/version: v1.0.0

# Images (update these with your registry)
images:
  - name: chiro-erp/core-business-service
    newTag: latest
  - name: chiro-erp/operations-management-service
    newTag: latest
  - name: chiro-erp/customer-relations-service
    newTag: latest
  - name: chiro-erp/platform-services-service
    newTag: latest
  - name: chiro-erp/workforce-management-service
    newTag: latest
"@

$kustomizationPath = Join-Path $KubernetesDir "kustomization.yml"
$kustomizationContent | Out-File -FilePath $kustomizationPath -Encoding UTF8
Write-Host "  ✅ Created kustomization.yml" -ForegroundColor Green

# Create deployment script
$deployScriptContent = @"
#!/usr/bin/env pwsh
<#
.SYNOPSIS
Deploy consolidated Chiro ERP services to Kubernetes

.DESCRIPTION
Deploys the consolidated monolithic services to a Kubernetes cluster.

.PARAMETER Namespace
Kubernetes namespace to deploy to (default: chiro-erp)

.PARAMETER DryRun
Perform a dry run without actually applying changes

.EXAMPLE
.\deploy-consolidated-services.ps1
.\deploy-consolidated-services.ps1 -DryRun
#>

param(
    [string]`$Namespace = "chiro-erp",
    [switch]`$DryRun
)

Write-Host "🚀 Deploying Chiro ERP Consolidated Services..." -ForegroundColor Cyan

if (`$DryRun) {
    Write-Host "🔍 Performing dry run..." -ForegroundColor Yellow
    kubectl apply --dry-run=client -k . --namespace=`$Namespace
} else {
    Write-Host "📦 Applying manifests..." -ForegroundColor Green
    kubectl apply -k . --namespace=`$Namespace
    
    Write-Host "⏳ Waiting for deployments to be ready..." -ForegroundColor Yellow
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=chiro-erp --timeout=300s --namespace=`$Namespace
    
    Write-Host "📊 Checking deployment status..." -ForegroundColor Cyan
    kubectl get pods,svc,ingress --namespace=`$Namespace
}

Write-Host "✅ Deployment complete!" -ForegroundColor Green
"@

$deployScriptPath = Join-Path $KubernetesDir "deploy-consolidated-services.ps1"
$deployScriptContent | Out-File -FilePath $deployScriptPath -Encoding UTF8
Write-Host "  ✅ Created deployment script" -ForegroundColor Green

# Create README for Kubernetes
$kubernetesReadmeContent = @"
# Kubernetes Configuration for Consolidated Chiro ERP

This directory contains Kubernetes manifests for the consolidated Chiro ERP microservices architecture.

## Architecture Overview

The original 18 microservices have been consolidated into 5 monolithic services:

### Consolidated Services

1. **core-business-service** (Port 8081)
   - Modules: billing, finance, sales, inventory, procurement, manufacturing
   - Endpoints: `/api/billing`, `/api/finance`, `/api/sales`, `/api/inventory`, `/api/procurement`, `/api/manufacturing`

2. **operations-management-service** (Port 8082)
   - Modules: project, fleet, pos, fieldservice
   - Endpoints: `/api/project`, `/api/fleet`, `/api/pos`, `/api/fieldservice`

3. **customer-relations-service** (Port 8083)
   - Modules: crm, repair
   - Endpoints: `/api/crm`, `/api/repair`

4. **platform-services-service** (Port 8084)
   - Modules: analytics, notifications
   - Endpoints: `/api/analytics`, `/api/notifications`

5. **workforce-management-service** (Port 8085)
   - Modules: hr, user-management, tenant-management
   - Endpoints: `/api/hr`, `/api/users`, `/api/tenants`

## Directory Structure

```
kubernetes/
├── base/
│   └── namespace.yml              # Namespace definition
├── services/
│   ├── core-business-service/     # Core business service manifests
│   ├── operations-management-service/
│   ├── customer-relations-service/
│   ├── platform-services-service/
│   └── workforce-management-service/
├── ingress/
│   └── ingress.yml               # Consolidated ingress configuration
├── api-gateway/                  # API Gateway (unchanged)
├── kustomization.yml            # Kustomize configuration
└── deploy-consolidated-services.ps1  # Deployment script
```

## Deployment

### Prerequisites

- kubectl configured with access to your Kubernetes cluster
- Kubernetes cluster with NGINX Ingress Controller

### Quick Deployment

```powershell
# Deploy all services
./deploy-consolidated-services.ps1

# Dry run to preview changes
./deploy-consolidated-services.ps1 -DryRun
```

### Manual Deployment

```bash
# Apply all manifests
kubectl apply -k .

# Check deployment status
kubectl get pods,svc,ingress -n chiro-erp

# Wait for pods to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=chiro-erp --timeout=300s -n chiro-erp
```

### Individual Service Deployment

```bash
# Deploy specific service
kubectl apply -f services/core-business-service/

# Check specific service
kubectl get pods -l app=core-business-service -n chiro-erp
```

## Service Configuration

Each consolidated service includes:

- **Deployment**: Defines the pod template, replicas, and container configuration
- **Service**: Exposes the deployment within the cluster
- **Health Checks**: Liveness, readiness, and startup probes
- **Resource Limits**: Memory and CPU constraints
- **Environment Variables**: Service-specific configuration

## Ingress Configuration

The ingress configuration routes traffic based on path prefixes:

- `/api/gateway` → api-gateway
- `/api/billing` → core-business-service
- `/api/finance` → core-business-service
- `/api/project` → operations-management-service
- `/api/crm` → customer-relations-service
- And so on...

## Monitoring and Health Checks

All services expose Quarkus health endpoints:

- `/q/health/live` - Liveness probe
- `/q/health/ready` - Readiness probe  
- `/q/health/started` - Startup probe

## Scaling

To scale a specific service:

```bash
kubectl scale deployment core-business-service --replicas=3 -n chiro-erp
```

## Troubleshooting

### Check pod logs
```bash
kubectl logs -l app=core-business-service -n chiro-erp
```

### Check service endpoints
```bash
kubectl get endpoints -n chiro-erp
```

### Check ingress
```bash
kubectl describe ingress chiro-erp-ingress -n chiro-erp
```

## Migration Notes

This configuration replaces the previous 18-service architecture. The old service manifests have been consolidated into these 5 services for better resource utilization and simplified operations.
"@

$kubernetesReadmePath = Join-Path $KubernetesDir "README.md"
$kubernetesReadmeContent | Out-File -FilePath $kubernetesReadmePath -Encoding UTF8
Write-Host "  ✅ Created Kubernetes README.md" -ForegroundColor Green

# Summary
Write-Host "`n🎉 Kubernetes refactoring completed successfully!" -ForegroundColor Green
Write-Host "`n📊 Summary:" -ForegroundColor Cyan
Write-Host "  • Removed $($OriginalServices.Count) original service manifests" -ForegroundColor Yellow
Write-Host "  • Created $($ConsolidatedServices.Count) consolidated service manifests" -ForegroundColor Green
Write-Host "  • Updated ingress configuration for new routing" -ForegroundColor Green
Write-Host "  • Created kustomization.yml for easy deployment" -ForegroundColor Green
Write-Host "  • Created deployment script and documentation" -ForegroundColor Green

if ($BackupExisting) {
    Write-Host "`n💾 Original configuration backed up to: kubernetes-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')" -ForegroundColor Blue
}

Write-Host "`n🚀 Next steps:" -ForegroundColor Cyan
Write-Host "  1. Review the generated manifests in kubernetes/services/" -ForegroundColor White
Write-Host "  2. Update Docker images if needed" -ForegroundColor White
Write-Host "  3. Run: ./kubernetes/deploy-consolidated-services.ps1 -DryRun" -ForegroundColor White
Write-Host "  4. Deploy: ./kubernetes/deploy-consolidated-services.ps1" -ForegroundColor White

Write-Host "`n✨ Kubernetes is now aligned with your consolidated services architecture!" -ForegroundColor Green

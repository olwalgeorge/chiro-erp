# Kubernetes Configuration for Consolidated Chiro ERP

This directory contains Kubernetes manifests for the consolidated Chiro ERP microservices architecture.

## Architecture Overview

The original 18 microservices have been consolidated into 5 monolithic services:

### Consolidated Services

1. **core-business-service** (Port 8081)
   - Modules: billing, finance, sales, inventory, procurement, manufacturing
   - Endpoints: /api/billing, /api/finance, /api/sales, /api/inventory, /api/procurement, /api/manufacturing

2. **operations-management-service** (Port 8082)
   - Modules: project, fleet, pos, fieldservice
   - Endpoints: /api/project, /api/fleet, /api/pos, /api/fieldservice

3. **customer-relations-service** (Port 8083)
   - Modules: crm, repair
   - Endpoints: /api/crm, /api/repair

4. **platform-services-service** (Port 8084)
   - Modules: analytics, notifications
   - Endpoints: /api/analytics, /api/notifications

5. **workforce-management-service** (Port 8085)
   - Modules: hr, user-management, tenant-management
   - Endpoints: /api/hr, /api/users, /api/tenants

## Directory Structure

`
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
`

## Deployment

### Prerequisites

- kubectl configured with access to your Kubernetes cluster
- Kubernetes cluster with NGINX Ingress Controller

### Quick Deployment

`powershell
# Deploy all services
./deploy-consolidated-services.ps1

# Dry run to preview changes
./deploy-consolidated-services.ps1 -DryRun
`

### Manual Deployment

`ash
# Apply all manifests
kubectl apply -k .

# Check deployment status
kubectl get pods,svc,ingress -n chiro-erp

# Wait for pods to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=chiro-erp --timeout=300s -n chiro-erp
`

### Individual Service Deployment

`ash
# Deploy specific service
kubectl apply -f services/core-business-service/

# Check specific service
kubectl get pods -l app=core-business-service -n chiro-erp
`

## Service Configuration

Each consolidated service includes:

- **Deployment**: Defines the pod template, replicas, and container configuration
- **Service**: Exposes the deployment within the cluster
- **Health Checks**: Liveness, readiness, and startup probes
- **Resource Limits**: Memory and CPU constraints
- **Environment Variables**: Service-specific configuration

## Ingress Configuration

The ingress configuration routes traffic based on path prefixes:

- /api/gateway → api-gateway
- /api/billing → core-business-service
- /api/finance → core-business-service
- /api/project → operations-management-service
- /api/crm → customer-relations-service
- And so on...

## Monitoring and Health Checks

All services expose Quarkus health endpoints:

- /q/health/live - Liveness probe
- /q/health/ready - Readiness probe  
- /q/health/started - Startup probe

## Scaling

To scale a specific service:

`ash
kubectl scale deployment core-business-service --replicas=3 -n chiro-erp
`

## Troubleshooting

### Check pod logs
`ash
kubectl logs -l app=core-business-service -n chiro-erp
`

### Check service endpoints
`ash
kubectl get endpoints -n chiro-erp
`

### Check ingress
`ash
kubectl describe ingress chiro-erp-ingress -n chiro-erp
`

## Migration Notes

This configuration replaces the previous 18-service architecture. The old service manifests have been consolidated into these 5 services for better resource utilization and simplified operations.

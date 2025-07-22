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
    [string]$Namespace = "chiro-erp",
    [switch]$DryRun
)

Write-Host "🚀 Deploying Chiro ERP Consolidated Services..." -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "🔍 Performing dry run..." -ForegroundColor Yellow
    kubectl apply --dry-run=client -k . --namespace=$Namespace
} else {
    Write-Host "📦 Applying manifests..." -ForegroundColor Green
    kubectl apply -k . --namespace=$Namespace
    
    Write-Host "⏳ Waiting for deployments to be ready..." -ForegroundColor Yellow
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=chiro-erp --timeout=300s --namespace=$Namespace
    
    Write-Host "📊 Checking deployment status..." -ForegroundColor Cyan
    kubectl get pods,svc,ingress --namespace=$Namespace
}

Write-Host "✅ Deployment complete!" -ForegroundColor Green

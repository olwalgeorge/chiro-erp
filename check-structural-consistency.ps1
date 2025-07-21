#!/usr/bin/env pwsh
<#
.SYNOPSIS
Structural consistency check for Chiro ERP consolidated services architecture.

.DESCRIPTION
This script performs a comprehensive structural consistency check across:
- Module distribution in consolidated services
- API Gateway routing configuration
- Kubernetes ingress routing
- Kubernetes service definitions
- Build configurations

.EXAMPLE
.\check-structural-consistency.ps1
#>

$ProjectRoot = $PSScriptRoot
$ConsolidatedServicesDir = Join-Path $ProjectRoot "consolidated-services"
$ApiGatewayDir = Join-Path $ProjectRoot "api-gateway"
$KubernetesDir = Join-Path $ProjectRoot "kubernetes"

Write-Host "🔍 Checking structural consistency across Chiro ERP architecture..." -ForegroundColor Cyan

# 1. Check module distribution
Write-Host "`n📁 Module Distribution Analysis:" -ForegroundColor Yellow

$serviceModules = @{}
$consolidatedServices = Get-ChildItem -Path $ConsolidatedServicesDir -Directory

foreach ($service in $consolidatedServices) {
    $modulesPath = Join-Path $service.FullName "modules"
    if (Test-Path $modulesPath) {
        $modules = Get-ChildItem -Path $modulesPath -Directory | Select-Object -ExpandProperty Name
        $serviceModules[$service.Name] = $modules
        Write-Host "  ✅ $($service.Name): $($modules.Count) modules - $($modules -join ', ')" -ForegroundColor Green
    }
    else {
        Write-Host "  ❌ $($service.Name): No modules directory found!" -ForegroundColor Red
    }
}

# 2. Check API Gateway configuration
Write-Host "`n🌐 API Gateway Configuration Analysis:" -ForegroundColor Yellow

$apiGatewayConfig = Join-Path $ApiGatewayDir "src\main\resources\application.yml"
$routingService = Join-Path $ApiGatewayDir "src\main\kotlin\org\chiro\gateway\application\service\RequestRoutingService.kt"

if (Test-Path $apiGatewayConfig) {
    $configContent = Get-Content -Path $apiGatewayConfig -Raw
    Write-Host "  ✅ API Gateway application.yml found" -ForegroundColor Green
    
    # Extract service paths from config
    $configPaths = @{}
    if ($configContent -match '(?s)gateway:\s*services:(.*?)(?=\n\s*#|\n[a-z]|\z)') {
        $servicesBlock = $Matches[1]
        
        # Parse each service and its paths
        $serviceBlocks = $servicesBlock -split '(?=\s{4}[a-z-]+:)'
        foreach ($block in $serviceBlocks) {
            if ($block -match '\s{4}([a-z-]+):\s*\n.*?paths:\s*\n((?:\s*-\s*"[^"]+"\s*\n?)*)') {
                $serviceName = $Matches[1]
                $pathsText = $Matches[2]
                $paths = @()
                if ($pathsText -match '\s*-\s*"([^"]+)"') {
                    $paths = ([regex]::Matches($pathsText, '\s*-\s*"([^"]+)"') | ForEach-Object { $_.Groups[1].Value })
                }
                $configPaths[$serviceName] = $paths
                Write-Host "    📍 $serviceName paths: $($paths -join ', ')" -ForegroundColor Cyan
            }
        }
    }
}
else {
    Write-Host "  ❌ API Gateway application.yml not found!" -ForegroundColor Red
}

if (Test-Path $routingService) {
    Write-Host "  ✅ RequestRoutingService.kt found" -ForegroundColor Green
    
    $routingContent = Get-Content -Path $routingService -Raw
    
    # Extract service mapping
    if ($routingContent -match '(?s)serviceMapping\s*=\s*mapOf\((.*?)\)') {
        $mappingContent = $Matches[1]
        $routingPaths = @{}
        
        $mappings = [regex]::Matches($mappingContent, '"(/api/[^"]+)"\s*to\s*"([^"]+)"')
        foreach ($mapping in $mappings) {
            $path = $mapping.Groups[1].Value
            $service = $mapping.Groups[2].Value
            if (-not $routingPaths.ContainsKey($service)) {
                $routingPaths[$service] = @()
            }
            $routingPaths[$service] += $path
        }
        
        Write-Host "    🎯 Routing Service Mappings:" -ForegroundColor Cyan
        foreach ($service in $routingPaths.Keys | Sort-Object) {
            Write-Host "      $service : $($routingPaths[$service] -join ', ')" -ForegroundColor White
        }
    }
}
else {
    Write-Host "  ❌ RequestRoutingService.kt not found!" -ForegroundColor Red
}

# 3. Check Kubernetes service definitions
Write-Host "`n☸️ Kubernetes Services Analysis:" -ForegroundColor Yellow

$kubernetesServicesDir = Join-Path $KubernetesDir "services"
if (Test-Path $kubernetesServicesDir) {
    $k8sServices = Get-ChildItem -Path $kubernetesServicesDir -Directory
    Write-Host "  ✅ Kubernetes services directory found" -ForegroundColor Green
    
    $definedServices = @()
    foreach ($service in $k8sServices) {
        $definedServices += $service.Name
        Write-Host "    📦 $($service.Name)" -ForegroundColor Cyan
    }
    
    # Check for missing platform-services
    $expectedServices = @("core-business-service", "operations-management-service", "customer-relations-service", "platform-services", "workforce-management-service", "api-gateway")
    $missingServices = $expectedServices | Where-Object { $_ -notin $definedServices }
    
    if ($missingServices.Count -gt 0) {
        Write-Host "  ❌ Missing Kubernetes service definitions:" -ForegroundColor Red
        foreach ($missing in $missingServices) {
            Write-Host "    - $missing" -ForegroundColor Red
        }
    }
}
else {
    Write-Host "  ❌ Kubernetes services directory not found!" -ForegroundColor Red
}

# 4. Check Kubernetes ingress
Write-Host "`n🚦 Kubernetes Ingress Analysis:" -ForegroundColor Yellow

$ingressFile = Join-Path $KubernetesDir "ingress\ingress.yml"
if (Test-Path $ingressFile) {
    Write-Host "  ✅ Kubernetes ingress.yml found" -ForegroundColor Green
    
    $ingressContent = Get-Content -Path $ingressFile -Raw
    $ingressPaths = @{}
    
    # Extract ingress paths and their target services
    $pathBlocks = [regex]::Matches($ingressContent, '(?s)# ([^-]+) - (/api/[^\s]+)\s*\n\s*-\s*path:\s*(/api/[^\s]+)\s*\n.*?name:\s*([^\s]+)')
    foreach ($block in $pathBlocks) {
        $comment = $block.Groups[1].Value.Trim()
        $pathInComment = $block.Groups[2].Value
        $actualPath = $block.Groups[3].Value
        $serviceName = $block.Groups[4].Value
        
        Write-Host "    🎯 $actualPath → $serviceName ($comment)" -ForegroundColor Cyan
        
        if (-not $ingressPaths.ContainsKey($serviceName)) {
            $ingressPaths[$serviceName] = @()
        }
        $ingressPaths[$serviceName] += $actualPath
    }
}
else {
    Write-Host "  ❌ Kubernetes ingress.yml not found!" -ForegroundColor Red
}

# 5. Consistency Analysis
Write-Host "`n📊 Consistency Analysis:" -ForegroundColor Yellow

# Expected module-to-service mapping based on current structure
$expectedMapping = @{
    "core-business-service"         = @("finance", "sales", "inventory", "procurement", "manufacturing")
    "operations-management-service" = @("project", "fleet", "pos", "fieldservice", "repair")
    "customer-relations-service"    = @("crm")
    "platform-services"             = @("analytics", "notifications", "tenant-management", "billing")
    "workforce-management-service"  = @("hr", "user-management")
}

# Expected API paths for each service
$expectedPaths = @{
    "core-business"         = @("/api/finance", "/api/sales", "/api/inventory", "/api/procurement", "/api/manufacturing")
    "operations-management" = @("/api/project", "/api/fleet", "/api/pos", "/api/fieldservice", "/api/repair")
    "customer-relations"    = @("/api/crm")
    "platform-services"     = @("/api/analytics", "/api/notifications", "/api/tenants", "/api/billing")
    "workforce-management"  = @("/api/hr", "/api/users")
}

$issues = @()

# Check module distribution consistency
foreach ($service in $expectedMapping.Keys) {
    if ($serviceModules.ContainsKey($service)) {
        $actualModules = $serviceModules[$service] | Sort-Object
        $expectedModules = $expectedMapping[$service] | Sort-Object
        
        $missing = $expectedModules | Where-Object { $_ -notin $actualModules }
        $extra = $actualModules | Where-Object { $_ -notin $expectedModules }
        
        if ($missing.Count -gt 0) {
            $issues += "❌ $service missing modules: $($missing -join ', ')"
        }
        if ($extra.Count -gt 0) {
            $issues += "⚠️ $service has extra modules: $($extra -join ', ')"
        }
        
        if ($missing.Count -eq 0 -and $extra.Count -eq 0) {
            Write-Host "  ✅ $service modules are consistent" -ForegroundColor Green
        }
    }
    else {
        $issues += "❌ $service directory not found"
    }
}

# Check API Gateway routing consistency
$gatewayServiceNames = @("core-business", "operations-management", "customer-relations", "platform-services", "workforce-management")
foreach ($service in $gatewayServiceNames) {
    if ($routingPaths.ContainsKey($service) -and $expectedPaths.ContainsKey($service)) {
        $actualPaths = $routingPaths[$service] | Sort-Object
        $expectedServicePaths = $expectedPaths[$service] | Sort-Object
        
        $missing = $expectedServicePaths | Where-Object { $_ -notin $actualPaths }
        $extra = $actualPaths | Where-Object { $_ -notin $expectedServicePaths }
        
        if ($missing.Count -gt 0) {
            $issues += "❌ API Gateway $service missing paths: $($missing -join ', ')"
        }
        if ($extra.Count -gt 0) {
            $issues += "⚠️ API Gateway $service has extra paths: $($extra -join ', ')"
        }
        
        if ($missing.Count -eq 0 -and $extra.Count -eq 0) {
            Write-Host "  ✅ API Gateway $service paths are consistent" -ForegroundColor Green
        }
    }
}

# Check Kubernetes service naming consistency
$k8sServiceNames = @("core-business-service", "operations-management-service", "customer-relations-service", "platform-services", "workforce-management-service")
foreach ($service in $k8sServiceNames) {
    if ($service -notin $definedServices) {
        $issues += "❌ Kubernetes service definition missing: $service"
    }
}

# Display all issues
if ($issues.Count -gt 0) {
    Write-Host "`n🚨 Issues Found:" -ForegroundColor Red
    foreach ($issue in $issues) {
        Write-Host "  $issue" -ForegroundColor Red
    }
    
    Write-Host "`n💡 Recommendations:" -ForegroundColor Blue
    Write-Host "  1. Create missing Kubernetes service definition for platform-services" -ForegroundColor White
    Write-Host "  2. Fix any inconsistent service naming in ingress.yml" -ForegroundColor White
    Write-Host "  3. Ensure all API paths are properly mapped across all configurations" -ForegroundColor White
}
else {
    Write-Host "`n✅ All structural components are consistent!" -ForegroundColor Green
}

Write-Host "`n📈 Summary:" -ForegroundColor Cyan
Write-Host "  • Total Services: $($consolidatedServices.Count)" -ForegroundColor Yellow
Write-Host "  • Total Modules: $($serviceModules.Values | ForEach-Object { $_.Count } | Measure-Object -Sum | Select-Object -ExpandProperty Sum)" -ForegroundColor Yellow
Write-Host "  • Issues Found: $($issues.Count)" -ForegroundColor $(if ($issues.Count -eq 0) { "Green" } else { "Red" })

Write-Host "`n🎯 Architecture Status: $(if ($issues.Count -eq 0) { "✅ CONSISTENT" } else { "⚠️ NEEDS ATTENTION" })" -ForegroundColor $(if ($issues.Count -eq 0) { "Green" } else { "Yellow" })

# PowerShell script to create chiro-erp project structure
# This script creates all folders and files for the chiro-erp project without any code content
# It skips existing files and folders

Write-Host "Creating chiro-erp project structure..." -ForegroundColor Green

# Define base directories
$baseDir = "."

# Create all main directories
$directories = @(
    # Build source
    "buildSrc/src/main/kotlin/org/chiro",
    
    # Gradle wrapper
    "gradle/wrapper",
    
    # Kubernetes base
    "kubernetes/base",
    
    # Kubernetes services
    "kubernetes/services/finance-service",
    "kubernetes/services/inventory-service",
    "kubernetes/services/sales-service",
    "kubernetes/services/procurement-service",
    "kubernetes/services/fleet-service",
    "kubernetes/services/project-service",
    "kubernetes/services/manufacturing-service",
    "kubernetes/services/fieldservice-service",
    "kubernetes/services/repair-service",
    "kubernetes/services/hr-service",
    "kubernetes/services/tenant-management-service",
    "kubernetes/services/user-management-service",
    "kubernetes/services/billing-service",
    "kubernetes/services/pos-service",
    "kubernetes/services/crm-service",
    "kubernetes/services/api-gateway",
    "kubernetes/services/analytics-service",
    "kubernetes/services/notifications-service",
    
    # Kubernetes infrastructure
    "kubernetes/kafka",
    "kubernetes/postgresql",
    "kubernetes/ingress",
    
    # Services directory (will be created if it doesn't exist)
    "services"
)

Write-Host "Creating directories..." -ForegroundColor Yellow

# Create all directories
foreach ($dir in $directories) {
    $fullPath = Join-Path $baseDir $dir
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
        Write-Host "  Created: $dir" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $dir (already exists)" -ForegroundColor DarkGray
    }
}

Write-Host "Creating root configuration files..." -ForegroundColor Yellow

# Create root configuration files
$rootFiles = @(
    @{Path = ".editorconfig"; Description = "Editor configuration" },
    @{Path = ".gitignore"; Description = "Git ignore file" },
    @{Path = "build.gradle.kts"; Description = "Main build script" },
    @{Path = "settings.gradle.kts"; Description = "Gradle settings" },
    @{Path = "docker-compose.yml"; Description = "Docker Compose configuration" },
    @{Path = "gradlew"; Description = "Gradle wrapper script (Unix)" },
    @{Path = "gradlew.bat"; Description = "Gradle wrapper script (Windows)" },
    @{Path = "Dockerfile"; Description = "Multi-stage Docker build file" },
    @{Path = ".dockerignore"; Description = "Docker ignore file" },
    @{Path = "README.md"; Description = "Project documentation" }
)

foreach ($file in $rootFiles) {
    $fullPath = Join-Path $baseDir $file.Path
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType File -Path $fullPath -Force | Out-Null
        Write-Host "  Created: $($file.Path) - $($file.Description)" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $($file.Path) (already exists)" -ForegroundColor DarkGray
    }
}

Write-Host "Creating buildSrc files..." -ForegroundColor Yellow

# Create buildSrc files
$buildSrcFiles = @(
    "buildSrc/src/main/kotlin/org/chiro/common-conventions.gradle.kts"
)

foreach ($file in $buildSrcFiles) {
    $fullPath = Join-Path $baseDir $file
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType File -Path $fullPath -Force | Out-Null
        Write-Host "  Created: $file" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $file (already exists)" -ForegroundColor DarkGray
    }
}

Write-Host "Creating Gradle wrapper files..." -ForegroundColor Yellow

# Create Gradle wrapper files
$gradleFiles = @(
    "gradle/wrapper/gradle-wrapper.jar",
    "gradle/wrapper/gradle-wrapper.properties"
)

foreach ($file in $gradleFiles) {
    $fullPath = Join-Path $baseDir $file
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType File -Path $fullPath -Force | Out-Null
        Write-Host "  Created: $file" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $file (already exists)" -ForegroundColor DarkGray
    }
}

Write-Host "Creating Kubernetes base files..." -ForegroundColor Yellow

# Create Kubernetes base files
$k8sBaseFiles = @(
    "kubernetes/base/kustomization.yml",
    "kubernetes/base/namespace.yml"
)

foreach ($file in $k8sBaseFiles) {
    $fullPath = Join-Path $baseDir $file
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType File -Path $fullPath -Force | Out-Null
        Write-Host "  Created: $file" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $file (already exists)" -ForegroundColor DarkGray
    }
}

Write-Host "Creating Kubernetes service deployment files..." -ForegroundColor Yellow

# Create Kubernetes service files
$services = @(
    "finance-service",
    "inventory-service",
    "sales-service",
    "procurement-service",
    "fleet-service",
    "project-service",
    "manufacturing-service",
    "fieldservice-service",
    "repair-service",
    "hr-service",
    "tenant-management-service",
    "user-management-service",
    "billing-service",
    "pos-service",
    "crm-service",
    "api-gateway",
    "analytics-service",
    "notifications-service"
)

foreach ($service in $services) {
    $deploymentFile = "kubernetes/services/$service/deployment.yml"
    $serviceFile = "kubernetes/services/$service/service.yml"
    
    $deploymentPath = Join-Path $baseDir $deploymentFile
    $servicePath = Join-Path $baseDir $serviceFile
    
    if (-not (Test-Path $deploymentPath)) {
        New-Item -ItemType File -Path $deploymentPath -Force | Out-Null
        Write-Host "  Created: $deploymentFile" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $deploymentFile (already exists)" -ForegroundColor DarkGray
    }
    
    if (-not (Test-Path $servicePath)) {
        New-Item -ItemType File -Path $servicePath -Force | Out-Null
        Write-Host "  Created: $serviceFile" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $serviceFile (already exists)" -ForegroundColor DarkGray
    }
}

Write-Host "Creating Kafka topic files..." -ForegroundColor Yellow

# Create Kafka configuration files
$kafkaFiles = @(
    "kubernetes/kafka/kafka-cluster.yml",
    "kubernetes/kafka/topic-finance-events.yml",
    "kubernetes/kafka/topic-inventory-events.yml",
    "kubernetes/kafka/topic-sales-events.yml",
    "kubernetes/kafka/topic-procurement-events.yml",
    "kubernetes/kafka/topic-fleet-events.yml",
    "kubernetes/kafka/topic-project-events.yml",
    "kubernetes/kafka/topic-manufacturing-events.yml",
    "kubernetes/kafka/topic-fieldservice-events.yml",
    "kubernetes/kafka/topic-repair-events.yml",
    "kubernetes/kafka/topic-hr-events.yml",
    "kubernetes/kafka/topic-tenant-events.yml",
    "kubernetes/kafka/topic-user-events.yml",
    "kubernetes/kafka/topic-billing-events.yml",
    "kubernetes/kafka/topic-pos-events.yml",
    "kubernetes/kafka/topic-crm-events.yml",
    "kubernetes/kafka/topic-analytics-events.yml",
    "kubernetes/kafka/topic-notifications-events.yml",
    "kubernetes/kafka/topic-product-events.yml"
)

foreach ($file in $kafkaFiles) {
    $fullPath = Join-Path $baseDir $file
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType File -Path $fullPath -Force | Out-Null
        Write-Host "  Created: $file" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $file (already exists)" -ForegroundColor DarkGray
    }
}

Write-Host "Creating PostgreSQL and Ingress files..." -ForegroundColor Yellow

# Create PostgreSQL and Ingress files
$infraFiles = @(
    "kubernetes/postgresql/postgres-db.yml",
    "kubernetes/ingress/ingress.yml"
)

foreach ($file in $infraFiles) {
    $fullPath = Join-Path $baseDir $file
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType File -Path $fullPath -Force | Out-Null
        Write-Host "  Created: $file" -ForegroundColor Gray
    }
    else {
        Write-Host "  Skipped: $file (already exists)" -ForegroundColor DarkGray
    }
}

Write-Host ""
Write-Host "✅ Successfully created chiro-erp project structure!" -ForegroundColor Green
Write-Host ""

# Count created and existing files
$allPaths = @()
$allPaths += $rootFiles | ForEach-Object { $_.Path }
$allPaths += $buildSrcFiles
$allPaths += $gradleFiles
$allPaths += $k8sBaseFiles
$allPaths += $kafkaFiles
$allPaths += $infraFiles

# Add service files
foreach ($service in $services) {
    $allPaths += "kubernetes/services/$service/deployment.yml"
    $allPaths += "kubernetes/services/$service/service.yml"
}

$existingCount = 0
$createdCount = 0

foreach ($path in $allPaths) {
    if (Test-Path (Join-Path $baseDir $path)) {
        if ((Get-Item (Join-Path $baseDir $path)).CreationTime -gt (Get-Date).AddMinutes(-1)) {
            $createdCount++
        }
        else {
            $existingCount++
        }
    }
}

Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Total directories: $(($directories | Where-Object { Test-Path (Join-Path $baseDir $_) }).Count) directories"
Write-Host "   • Files created: $createdCount files"
Write-Host "   • Files skipped (already existed): $existingCount files"
Write-Host "   • Total files: $($allPaths.Count) files"
Write-Host ""
Write-Host "🎯 Next steps:"
Write-Host "   1. Configure build.gradle.kts with multi-project setup and common dependencies"
Write-Host "   2. Set up settings.gradle.kts to include all microservices"
Write-Host "   3. Configure common-conventions.gradle.kts with shared build logic"
Write-Host "   4. Set up Docker Compose with all services, Kafka, and PostgreSQL"
Write-Host "   5. Configure Kubernetes deployments for each service"
Write-Host "   6. Set up Kafka topics for inter-service communication"
Write-Host "   7. Configure ingress for API routing"
Write-Host "   8. Update .gitignore with appropriate exclusions"
Write-Host "   9. Create comprehensive README.md with project documentation"
Write-Host ""
Write-Host "🚀 Ready to start implementing individual services!"

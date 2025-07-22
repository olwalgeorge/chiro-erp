#!/usr/bin/env pwsh

param(
    [switch]$DryRun,
    [switch]$Verbose
)

Write-Host "🚀 COMPREHENSIVE DEPENDENCY & CONFIGURATION FIX" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

# Configuration
$QuarkusVersion = "3.24.4"
$KotlinVersion = "2.1.21"
$JavaVersion = "21"

# Auto-include configured for PowerShell scripts

# Define dependency sets based on actual needs
$DependencySets = @{
    "Gateway"  = @(
        'implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))',
        '',
        '// Core Quarkus dependencies',
        'implementation("io.quarkus:quarkus-kotlin")',
        'implementation("io.quarkus:quarkus-arc")',
        'implementation("io.quarkus:quarkus-rest")',
        'implementation("io.quarkus:quarkus-rest-kotlin-serialization")',
        '',
        '// Configuration and observability',
        'implementation("io.quarkus:quarkus-config-yaml")',
        'implementation("io.quarkus:quarkus-micrometer")',
        'implementation("io.quarkus:quarkus-smallrye-health")',
        'implementation("io.quarkus:quarkus-smallrye-openapi")',
        'implementation("io.quarkus:quarkus-smallrye-fault-tolerance")',
        '',
        '// Security',
        'implementation("io.quarkus:quarkus-security")',
        'implementation("io.quarkus:quarkus-oidc")',
        '',
        '// Caching',
        'implementation("io.quarkus:quarkus-cache")',
        '',
        '// Kotlin stdlib',
        'implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")',
        '',
        '// Test dependencies',
        'testImplementation("io.quarkus:quarkus-junit5")',
        'testImplementation("io.rest-assured:rest-assured")',
        'testImplementation("io.quarkus:quarkus-test-security")'
    )
    
    "Business" = @(
        'implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))',
        '',
        '// Core Quarkus dependencies',
        'implementation("io.quarkus:quarkus-kotlin")',
        'implementation("io.quarkus:quarkus-arc")',
        'implementation("io.quarkus:quarkus-rest")',
        'implementation("io.quarkus:quarkus-rest-kotlin-serialization")',
        '',
        '// Database dependencies (simplified for H2)',
        'implementation("io.quarkus:quarkus-hibernate-orm")',
        'implementation("io.quarkus:quarkus-jdbc-h2")',
        '',
        '// Configuration and observability',
        'implementation("io.quarkus:quarkus-config-yaml")',
        'implementation("io.quarkus:quarkus-smallrye-health")',
        '',
        '// Kotlin stdlib',
        'implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")',
        '',
        '// Test dependencies',
        'testImplementation("io.quarkus:quarkus-junit5")',
        'testImplementation("io.rest-assured:rest-assured")'
    )
    
    "Simple"   = @(
        'implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))',
        '',
        '// Core Quarkus dependencies',
        'implementation("io.quarkus:quarkus-kotlin")',
        'implementation("io.quarkus:quarkus-arc")',
        'implementation("io.quarkus:quarkus-rest")',
        'implementation("io.quarkus:quarkus-rest-kotlin-serialization")',
        '',
        '// Configuration and observability',
        'implementation("io.quarkus:quarkus-config-yaml")',
        'implementation("io.quarkus:quarkus-smallrye-health")',
        '',
        '// Kotlin stdlib',
        'implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")',
        '',
        '// Test dependencies',
        'testImplementation("io.quarkus:quarkus-junit5")',
        'testImplementation("io.rest-assured:rest-assured")'
    )
}

# Service configurations
$ServiceConfigs = @{
    "api-gateway"                   = @{
        "type"           = "Gateway"
        "needsDatabase"  = $false
        "needsMessaging" = $false
        "port"           = "8080"
    }
    "core-business-service"         = @{
        "type"           = "Business"
        "needsDatabase"  = $true
        "needsMessaging" = $false
        "port"           = "8081"
    }
    "customer-relations-service"    = @{
        "type"           = "Simple"
        "needsDatabase"  = $false
        "needsMessaging" = $false
        "port"           = "8083"
    }
    "operations-management-service" = @{
        "type"           = "Simple"
        "needsDatabase"  = $false
        "needsMessaging" = $false
        "port"           = "8082"
    }
    "platform-services"             = @{
        "type"           = "Business"
        "needsDatabase"  = $true
        "needsMessaging" = $false
        "port"           = "8084"
    }
    "workforce-management-service"  = @{
        "type"           = "Business"
        "needsDatabase"  = $true
        "needsMessaging" = $false
        "port"           = "8085"
    }
}

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "ERROR" { "Red" }
        "WARN" { "Yellow" }
        "SUCCESS" { "Green" }
        default { "White" }
    }
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Update-BuildFile {
    param(
        [string]$FilePath,
        [string]$ServiceName
    )
    
    Write-Log "Processing $ServiceName build file: $FilePath"
    
    if (-not (Test-Path $FilePath)) {
        Write-Log "File not found: $FilePath" "ERROR"
        return $false
    }
    
    try {
        $config = $ServiceConfigs[$ServiceName]
        if (-not $config) {
            Write-Log "No configuration found for service: $ServiceName" "WARN"
            return $false
        }
        
        $dependencyType = $config.type
        $dependencies = $DependencySets[$dependencyType]
        
        # Generate complete build file
        $buildContent = @"
plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    kotlin("plugin.serialization")
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
$($dependencies -join "`n    ")
}

group = "org.chiro"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        javaParameters.set(true)
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
"@
        
        if (-not $DryRun) {
            # Create backup
            $backupPath = "$FilePath.backup.$(Get-Date -Format 'yyyyMMdd-HHmmss')"
            Copy-Item $FilePath $backupPath -Force
            
            Set-Content -Path $FilePath -Value $buildContent -Encoding UTF8
            Write-Log "✅ Updated $ServiceName (backup: $backupPath)" "SUCCESS"
        }
        else {
            Write-Log "🔍 [DRY RUN] Would update $ServiceName" "INFO"
        }
        
        return $true
    }
    catch {
        Write-Log "Error processing $FilePath`: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Update-ApplicationProperties {
    param(
        [string]$FilePath,
        [string]$ServiceName
    )
    
    Write-Log "Processing $ServiceName application.properties: $FilePath"
    
    if (-not (Test-Path $FilePath)) {
        Write-Log "Application properties not found: $FilePath" "WARN"
        return $true # Not an error, just skip
    }
    
    try {
        $config = $ServiceConfigs[$ServiceName]
        $port = $config.port
        
        # Create basic configuration based on service type
        if ($config.type -eq "Gateway") {
            $newContent = @"
# Application Configuration for $ServiceName
quarkus.application.name=$ServiceName
quarkus.http.port=$port

# CORS Configuration
quarkus.http.cors=true
quarkus.http.cors.origins=*
quarkus.http.cors.methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
quarkus.http.cors.headers=Content-Type,Authorization,X-Requested-With

# Logging
quarkus.log.level=INFO
quarkus.log.category."org.chiro".level=DEBUG

# Health checks
quarkus.smallrye-health.enabled=true
"@
        }
        elseif ($config.needsDatabase) {
            $newContent = @"
# Application Configuration for $ServiceName
quarkus.application.name=$ServiceName
quarkus.http.port=$port

# Database Configuration (using H2 for development)
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.sql-load-script=import.sql

# Logging
quarkus.log.level=INFO
quarkus.log.category."org.chiro".level=DEBUG

# Health checks
quarkus.smallrye-health.enabled=true
"@
        }
        else {
            $newContent = @"
# Application Configuration for $ServiceName
quarkus.application.name=$ServiceName
quarkus.http.port=$port

# Logging
quarkus.log.level=INFO
quarkus.log.category."org.chiro".level=DEBUG

# Health checks
quarkus.smallrye-health.enabled=true
"@
        }
        
        if (-not $DryRun) {
            # Create backup
            $backupPath = "$FilePath.backup.$(Get-Date -Format 'yyyyMMdd-HHmmss')"
            Copy-Item $FilePath $backupPath -Force
            
            Set-Content -Path $FilePath -Value $newContent -Encoding UTF8
            Write-Log "✅ Updated application.properties for $ServiceName" "SUCCESS"
        }
        else {
            Write-Log "🔍 [DRY RUN] Would update application.properties for $ServiceName" "INFO"
        }
        
        return $true
    }
    catch {
        Write-Log "Error updating application.properties for $ServiceName`: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

function Create-ImportSql {
    param(
        [string]$ResourcesPath
    )
    
    $importSqlPath = Join-Path $ResourcesPath "import.sql"
    
    if (-not (Test-Path $importSqlPath) -and -not $DryRun) {
        $importContent = @"
-- Sample data for development
-- This file is automatically loaded by Hibernate when using database.generation=drop-and-create
"@
        Set-Content -Path $importSqlPath -Value $importContent -Encoding UTF8
        Write-Log "✅ Created import.sql at $importSqlPath" "SUCCESS"
    }
}

# Main execution
Write-Log "Starting comprehensive dependency and configuration fix..."

if ($DryRun) {
    Write-Log "🔍 DRY RUN MODE - No files will be modified" "WARN"
}

$services = @(
    @{ Name = "api-gateway"; Path = "api-gateway/build.gradle.kts"; PropsPath = "api-gateway/src/main/resources/application.properties" },
    @{ Name = "core-business-service"; Path = "consolidated-services/core-business-service/build.gradle.kts"; PropsPath = "consolidated-services/core-business-service/src/main/resources/application.properties" },
    @{ Name = "customer-relations-service"; Path = "consolidated-services/customer-relations-service/build.gradle.kts"; PropsPath = "consolidated-services/customer-relations-service/src/main/resources/application.properties" },
    @{ Name = "operations-management-service"; Path = "consolidated-services/operations-management-service/build.gradle.kts"; PropsPath = "consolidated-services/operations-management-service/src/main/resources/application.properties" },
    @{ Name = "platform-services"; Path = "consolidated-services/platform-services/build.gradle.kts"; PropsPath = "consolidated-services/platform-services/src/main/resources/application.properties" },
    @{ Name = "workforce-management-service"; Path = "consolidated-services/workforce-management-service/build.gradle.kts"; PropsPath = "consolidated-services/workforce-management-service/src/main/resources/application.properties" }
)

$successCount = 0
$totalCount = $services.Count

foreach ($service in $services) {
    Write-Log "Processing service: $($service.Name)" "INFO"
    
    $buildSuccess = Update-BuildFile -FilePath $service.Path -ServiceName $service.Name
    $propsSuccess = Update-ApplicationProperties -FilePath $service.PropsPath -ServiceName $service.Name
    
    # Create import.sql for database services
    $config = $ServiceConfigs[$service.Name]
    if ($config.needsDatabase -and -not $DryRun) {
        $resourcesDir = Split-Path $service.PropsPath -Parent
        if (Test-Path $resourcesDir) {
            Create-ImportSql -ResourcesPath $resourcesDir
        }
    }
    
    if ($buildSuccess -and $propsSuccess) {
        $successCount++
    }
    
    Write-Log "Completed processing: $($service.Name)" "INFO"
    Write-Host ""
}

# Summary
Write-Host "================================================================" -ForegroundColor Green
Write-Log "✅ Comprehensive fix completed!" "SUCCESS"
Write-Log "📊 Successfully processed: $successCount/$totalCount services" "SUCCESS"

if ($successCount -eq $totalCount) {
    Write-Log "🎉 All services updated successfully!" "SUCCESS"
    Write-Host ""
    Write-Log "📋 Applied Changes:" "INFO"
    Write-Log "  • Standardized dependencies based on service type" "INFO"
    Write-Log "  • Fixed configuration files to match available dependencies" "INFO"
    Write-Log "  • Removed problematic Flyway and Kafka configurations" "INFO"
    Write-Log "  • Set up proper database configurations for development" "INFO"
    Write-Log "  • Added basic import.sql files for database services" "INFO"
    Write-Log "  • Standardized ports: Gateway(8080), Core(8081), Ops(8082), CRM(8083), Platform(8084), Workforce(8085)" "INFO"
}
else {
    Write-Log "⚠️  Some services may need manual review" "WARN"
}

if (-not $DryRun) {
    Write-Log "💡 Next steps:" "INFO"
    Write-Log "  1. Run './gradlew clean build' to verify all fixes work" "INFO"
    Write-Log "  2. Check individual service builds if needed" "INFO"
    Write-Log "  3. Update database configurations for production later" "INFO"
}
else {
    Write-Log "💡 Run this script without -DryRun to apply changes" "INFO"
}

Write-Host "================================================================" -ForegroundColor Green

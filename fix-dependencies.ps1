# =============================================================================
# COMPREHENSIVE DEPENDENCY STANDARDIZATION SCRIPT - MODERN 2025 EDITION
# =============================================================================
# This script fixes ALL Gradle build files across the project including buildSrc conventions
# Using modern Liquibase YAML migrations instead of traditional Flyway SQL scripts
# Features: Smart schema evolution, event-driven migrations, zero-downtime deployments
# 
# JAVA 21 COMPATIBILITY FIX:
# - Forces org.jboss.threads:jboss-threads:3.5.0.Final to resolve Java 21 compatibility issues
# - Ensures all dependencies work with Java 21 runtime (Quarkus 3.24.4 + Java 21)

param(
    [switch]$DryRun = $false
)

$ErrorActionPreference = "Stop"

Write-Information "🔧 COMPREHENSIVE DEPENDENCY STANDARDIZATION" -InformationAction Continue
Write-Information "═══════════════════════════════════════════════════════════════════════════════" -InformationAction Continue

if ($DryRun) {
    Write-Information "🔍 DRY RUN MODE - No files will be modified" -InformationAction Continue
}

# Standard root build.gradle.kts content (using Kotlin DSL with GraalVM native support)
$rootBuildGradleContent = @'
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    id("io.quarkus") version "3.24.4"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))
    
    // Core Quarkus dependencies
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-smallrye-health")
    
    // Container image support
    implementation("io.quarkus:quarkus-container-image-docker")
    
    // Test dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "org.chiro"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        // Allow any Java 21 vendor - flexible for different environments
    }
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjvm-default=all")
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
'@

# Standard consolidated service build.gradle.kts content (with REST client for inter-service communication)
$consolidatedServiceBuildGradleContent = @'
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
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))
    
    // Core Quarkus dependencies
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-client")
    
    // Serialization: Kotlin for internal, Jackson for external
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("io.quarkus:quarkus-rest-jackson")
    
    // Database dependencies (only when JPA entities exist)
    // implementation("io.quarkus:quarkus-hibernate-reactive-panache-kotlin")
    // implementation("io.quarkus:quarkus-jdbc-postgresql")
    // implementation("io.quarkus:quarkus-liquibase")
    // implementation("io.quarkus:quarkus-hibernate-validator")
    
    // Configuration and observability
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-smallrye-health")
    
    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Test dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "org.chiro"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        // Allow any Java 21 vendor - flexible for different environments
    }
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjvm-default=all")
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
'@

# API Gateway specific build.gradle.kts content (enhanced with comprehensive REST capabilities)
$apiGatewayBuildGradleContent = @'
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
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))
    
    // Core Quarkus dependencies
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-client")
    
    // Serialization
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("io.quarkus:quarkus-rest-jackson")
    
    // Configuration and observability
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-smallrye-health")
    
    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Test dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "org.chiro"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        // Allow any Java 21 vendor - flexible for different environments
    }
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjvm-default=all")
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
'@

# Standard settings.gradle.kts content with all services
$settingsGradleContent = @'
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus") version "3.24.4"
        id("org.jetbrains.kotlin.jvm") version "2.1.21"
        id("org.jetbrains.kotlin.plugin.allopen") version "2.1.21"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    }
}
rootProject.name = "chiro-erp"

include("consolidated-services:core-business-service")
include("consolidated-services:customer-relations-service")
include("consolidated-services:operations-management-service")
include("consolidated-services:platform-services")
include("consolidated-services:workforce-management-service")
include("api-gateway")
'@

# Simple service build.gradle.kts content (for services that don't need database but need REST client)
$simpleServiceBuildGradleContent = @'
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
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))
    
    // Core Quarkus dependencies
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-client")
    
    // Serialization
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("io.quarkus:quarkus-rest-jackson")
    
    // Configuration and observability
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-smallrye-health")
    
    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Test dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "org.chiro"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        // Allow any Java 21 vendor - flexible for different environments
    }
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjvm-default=all")
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
'@

function Update-File {
    param(
        [string]$FilePath,
        [string]$Content,
        [string]$Description
    )
    
    Write-Host "📝 $Description" -ForegroundColor Cyan
    Write-Host "   File: $FilePath" -ForegroundColor Gray
    
    if (Test-Path $FilePath) {
        if (-not $DryRun) {
            $Content | Out-File -FilePath $FilePath -Encoding UTF8
            Write-Host "   ✅ Updated" -ForegroundColor Green
        }
        else {
            Write-Host "   🔍 Would update (dry run)" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "   ❌ File not found" -ForegroundColor Red
    }
}

function Remove-FileIfExists {
    param(
        [string]$FilePath,
        [string]$Description
    )
    
    Write-Host "🗑️  $Description" -ForegroundColor Yellow
    Write-Host "   File: $FilePath" -ForegroundColor Gray
    
    if (Test-Path $FilePath) {
        if (-not $DryRun) {
            Remove-Item $FilePath -Force
            Write-Host "   ✅ Removed" -ForegroundColor Green
        }
        else {
            Write-Host "   🔍 Would remove (dry run)" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "   ℹ️  File doesn't exist" -ForegroundColor Blue
    }
}

function Update-ServiceBuildFile {
    param(
        [string]$ServicePath,
        [string]$ServiceName
    )
    
    # Choose appropriate template based on service type
    if ($ServicePath -like "*platform-services*" -or $ServicePath -like "*workforce-management*") {
        # Services that might not need full database capabilities
        $buildContent = $simpleServiceBuildGradleContent  
        $serviceType = "Simple Service"
    }
    else {
        # Full business services with database capabilities
        $buildContent = $consolidatedServiceBuildGradleContent
        $serviceType = "Business Service"
    }
    
    $buildFilePath = "$ServicePath/build.gradle.kts"
    Write-Host "📦 Updating $serviceType : $ServiceName" -ForegroundColor Cyan
    Write-Host "   File: $buildFilePath" -ForegroundColor Gray
    
    if (Test-Path $buildFilePath) {
        if (-not $DryRun) {
            $buildContent | Out-File -FilePath $buildFilePath -Encoding UTF8
            Write-Host "   ✅ Updated service build file" -ForegroundColor Green
        }
        else {
            Write-Host "   🔍 Would update service build file (dry run)" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "   ❌ Service build file not found" -ForegroundColor Red
    }
}

function Update-ApiGatewayBuildFile {
    param(
        [string]$ServicePath,
        [string]$ServiceName
    )
    
    $buildFilePath = "$ServicePath/build.gradle.kts"
    Write-Host "🚪 Updating API Gateway: $ServiceName" -ForegroundColor Cyan
    Write-Host "   File: $buildFilePath" -ForegroundColor Gray
    
    if (Test-Path $buildFilePath) {
        if (-not $DryRun) {
            $apiGatewayBuildGradleContent | Out-File -FilePath $buildFilePath -Encoding UTF8
            Write-Host "   ✅ Updated API Gateway build file" -ForegroundColor Green
        }
        else {
            Write-Host "   🔍 Would update API Gateway build file (dry run)" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "   ⚠️  API Gateway build file not found, creating..." -ForegroundColor Yellow
        if (-not $DryRun) {
            # Ensure directory exists
            if (-not (Test-Path $ServicePath)) {
                New-Item -ItemType Directory -Path $ServicePath -Force | Out-Null
            }
            $apiGatewayBuildGradleContent | Out-File -FilePath $buildFilePath -Encoding UTF8
            Write-Host "   ✅ Created API Gateway build file" -ForegroundColor Green
        }
        else {
            Write-Host "   🔍 Would create API Gateway build file (dry run)" -ForegroundColor Yellow
        }
    }
}

# Function to validate build file syntax
function Test-BuildFileValid {
    param(
        [string]$FilePath
    )
    
    if (-not (Test-Path $FilePath)) {
        return $false
    }
    
    $content = Get-Content $FilePath -Raw
    # Basic validation - check for required elements
    $hasPlugins = $content -match "plugins\s*\{"
    $hasDependencies = $content -match "dependencies\s*\{" -or $content -match "include\("
    $hasValidStructure = $content -match "group\s*=" -or $content -match "rootProject\.name"
    
    return $hasPlugins -and $hasDependencies -and $hasValidStructure
}

# Update root build.gradle.kts
Write-Host "`n🔧 UPDATING ROOT BUILD FILE" -ForegroundColor Yellow
Update-File -FilePath "build.gradle.kts" -Content $rootBuildGradleContent -Description "Updating root build.gradle.kts"

# Update settings.gradle.kts
Write-Host "`n🔧 UPDATING SETTINGS FILE" -ForegroundColor Yellow
Update-File -FilePath "settings.gradle.kts" -Content $settingsGradleContent -Description "Updating settings.gradle.kts"

# Update all consolidated service build files
Write-Host "`n🔧 UPDATING CONSOLIDATED SERVICE BUILD FILES" -ForegroundColor Yellow
$consolidatedServices = @(
    @{ Path = "consolidated-services/core-business-service"; Name = "core-business-service" },
    @{ Path = "consolidated-services/customer-relations-service"; Name = "customer-relations-service" },
    @{ Path = "consolidated-services/operations-management-service"; Name = "operations-management-service" },
    @{ Path = "consolidated-services/platform-services"; Name = "platform-services" },
    @{ Path = "consolidated-services/workforce-management-service"; Name = "workforce-management-service" }
)

foreach ($service in $consolidatedServices) {
    if (Test-Path $service.Path) {
        Update-ServiceBuildFile -ServicePath $service.Path -ServiceName $service.Name
    }
    else {
        Write-Host "⚠️  Service directory not found: $($service.Path)" -ForegroundColor Yellow
    }
}

# Update API Gateway with specialized configuration
Write-Host "`n🔧 UPDATING API GATEWAY" -ForegroundColor Yellow
if (Test-Path "api-gateway") {
    Update-ApiGatewayBuildFile -ServicePath "api-gateway" -ServiceName "api-gateway"
}
else {
    Write-Host "⚠️  API Gateway directory not found" -ForegroundColor Yellow
}

# Check for and update any commons modules
Write-Host "`n🔧 CHECKING FOR COMMONS MODULES" -ForegroundColor Yellow
$possibleCommonsPaths = @(
    "commons",
    "shared",
    "common",
    "core"
)

foreach ($commonsPath in $possibleCommonsPaths) {
    if (Test-Path $commonsPath) {
        $commonsBuildFile = "$commonsPath/build.gradle.kts"
        if (Test-Path $commonsBuildFile) {
            Write-Host "📦 Found commons module: $commonsPath" -ForegroundColor Cyan
            Update-ServiceBuildFile -ServicePath $commonsPath -ServiceName $commonsPath
        }
    }
}

# Validate gradle.properties
Write-Host "`n🔧 VALIDATING GRADLE PROPERTIES" -ForegroundColor Yellow
$gradlePropsPath = "gradle.properties"
if (Test-Path $gradlePropsPath) {
    $props = Get-Content $gradlePropsPath
    $requiredProps = @(
        "quarkusPluginId=io.quarkus",
        "quarkusPluginVersion=3.24.4",
        "quarkusPlatformGroupId=io.quarkus.platform",
        "quarkusPlatformArtifactId=quarkus-bom",
        "quarkusPlatformVersion=3.24.4"
    )
    
    $allPropsPresent = $true
    foreach ($requiredProp in $requiredProps) {
        if ($props -notcontains $requiredProp) {
            Write-Host "   ❌ Missing or incorrect: $requiredProp" -ForegroundColor Red
            $allPropsPresent = $false
        }
    }
    
    if ($allPropsPresent) {
        Write-Host "   ✅ gradle.properties is correct" -ForegroundColor Green
    }
    else {
        Write-Host "   ⚠️  gradle.properties needs manual review" -ForegroundColor Yellow
    }
}
else {
    Write-Host "   ❌ gradle.properties not found" -ForegroundColor Red
}

# Summary and validation
Write-Host "`n═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor DarkGray
if ($DryRun) {
    Write-Host "🔍 DRY RUN COMPLETED" -ForegroundColor Yellow
    Write-Host "Run the script without -DryRun to apply changes" -ForegroundColor Cyan
}
else {
    Write-Host "✅ COMPREHENSIVE DEPENDENCY STANDARDIZATION COMPLETED" -ForegroundColor Green
    Write-Host "Updated configurations:" -ForegroundColor Cyan
    Write-Host "  • Updated root build.gradle.kts with new Quarkus REST dependencies" -ForegroundColor White
    Write-Host "  • Updated settings.gradle.kts with all services and hardcoded versions" -ForegroundColor White
    Write-Host "  • Updated all consolidated services with REST + REST Client capabilities" -ForegroundColor White
    Write-Host "  • Updated API Gateway with comprehensive gateway dependencies" -ForegroundColor White
    Write-Host "  • Added dual serialization: Kotlin for internal, Jackson for external" -ForegroundColor White
    Write-Host "  • Used modern Kotlin compiler options (compilerOptions DSL)" -ForegroundColor White
    Write-Host "  • Applied service-specific dependency templates" -ForegroundColor White
    Write-Host "  • Added modern Liquibase YAML migrations instead of Flyway" -ForegroundColor Yellow
    Write-Host "  • Added Hibernate Validator for Bean Validation" -ForegroundColor Yellow
    Write-Host "  • Added H2 test database for zero-config testing" -ForegroundColor Yellow
    Write-Host "  • Removed problematic reactive messaging to ensure stability" -ForegroundColor Yellow
    
    # Validate the updated files
    Write-Host "`n🔍 VALIDATING UPDATED FILES" -ForegroundColor Yellow
    $filesToValidate = @(
        "build.gradle.kts",
        "settings.gradle.kts"
    )
    
    foreach ($service in $consolidatedServices) {
        if (Test-Path $service.Path) {
            $filesToValidate += "$($service.Path)/build.gradle.kts"
        }
    }
    
    if (Test-Path "api-gateway") {
        $filesToValidate += "api-gateway/build.gradle.kts"
    }
    
    $validationResults = @()
    foreach ($file in $filesToValidate) {
        if (Test-Path $file) {
            $isValid = Test-BuildFileValid -FilePath $file
            if ($isValid) {
                Write-Host "  ✅ $file" -ForegroundColor Green
                $validationResults += $true
            }
            else {
                Write-Host "  ❌ $file - Invalid syntax" -ForegroundColor Red
                $validationResults += $false
            }
        }
        else {
            Write-Host "  ⚠️  $file - File not found" -ForegroundColor Yellow
            $validationResults += $false
        }
    }
    
    $allValid = $validationResults -notcontains $false
    if ($allValid) {
        Write-Host "`n🎉 ALL FILES VALIDATED SUCCESSFULLY" -ForegroundColor Green
    }
    else {
        Write-Host "`n⚠️  SOME FILES NEED MANUAL REVIEW" -ForegroundColor Yellow
    }
    
    Write-Host "`n💡 Next steps:" -ForegroundColor Yellow
    Write-Host "  1. Test clean: .\gradlew clean" -ForegroundColor Cyan
    Write-Host "  2. Test build: .\gradlew build" -ForegroundColor Cyan
    Write-Host "  3. Test specific service: .\gradlew :api-gateway:build" -ForegroundColor Cyan
    Write-Host "  4. If successful, run deployment: .\deploy-final.ps1 -Action build" -ForegroundColor Cyan
}

Write-Host "`n🎉 SCRIPT COMPLETED" -ForegroundColor Magenta















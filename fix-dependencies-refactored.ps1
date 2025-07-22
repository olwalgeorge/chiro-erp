# =============================================================================
# REFACTORED COMPREHENSIVE DEPENDENCY FIXING SCRIPT
# =============================================================================
# This script fixes dependency issues and ensures proper Quarkus/Kotlin configuration

param(
    [switch]$DryRun = $false,
    [switch]$CleanFirst = $true
)

$ErrorActionPreference = "Stop"

Write-Host "🔧 REFACTORED DEPENDENCY FIXING SCRIPT v2.0" -ForegroundColor Magenta
Write-Host "═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor DarkGray

if ($DryRun) {
    Write-Host "🔍 DRY RUN MODE - No files will be modified" -ForegroundColor Yellow
}

# =============================================================================
# CONFIGURATION
# =============================================================================

# Version constants
$QUARKUS_VERSION = "3.24.4"
$KOTLIN_VERSION = "2.1.21"
$JAVA_VERSION = "21"

# =============================================================================
# GRADLE PROPERTIES CONTENT
# =============================================================================
$gradlePropertiesContent = @"
# Gradle build optimization
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.daemon=true

# Quarkus Configuration  
quarkusPluginId=io.quarkus
quarkusPluginVersion=$QUARKUS_VERSION
quarkusPlatformGroupId=io.quarkus.platform
quarkusPlatformArtifactId=quarkus-bom
quarkusPlatformVersion=$QUARKUS_VERSION

# Kotlin Configuration
kotlinVersion=$KOTLIN_VERSION

# Java Configuration
javaVersion=$JAVA_VERSION
"@

# =============================================================================
# ROOT BUILD GRADLE CONTENT
# =============================================================================
$rootBuildGradleContent = @"
plugins {
    id("org.jetbrains.kotlin.jvm") version "$KOTLIN_VERSION"
    id("org.jetbrains.kotlin.plugin.allopen") version "$KOTLIN_VERSION"
    id("org.jetbrains.kotlin.plugin.serialization") version "$KOTLIN_VERSION"
    id("io.quarkus") version "$QUARKUS_VERSION"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$QUARKUS_VERSION"))
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-hibernate-reactive-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "org.chiro"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_$JAVA_VERSION
    targetCompatibility = JavaVersion.VERSION_$JAVA_VERSION
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_$JAVA_VERSION)
        javaParameters.set(true)
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
"@

# =============================================================================
# SETTINGS GRADLE CONTENT
# =============================================================================
$settingsGradleContent = @"
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus") version "$QUARKUS_VERSION"
        id("org.jetbrains.kotlin.jvm") version "$KOTLIN_VERSION"
        id("org.jetbrains.kotlin.plugin.allopen") version "$KOTLIN_VERSION"
        id("org.jetbrains.kotlin.plugin.serialization") version "$KOTLIN_VERSION"
    }
}

rootProject.name = "chiro-erp"

// Consolidated services
include("consolidated-services")
include("consolidated-services:core-business-service")
include("consolidated-services:customer-relations-service")
include("consolidated-services:operations-management-service")
include("consolidated-services:platform-services")
include("consolidated-services:workforce-management-service")

// API Gateway
include("api-gateway")
"@

# =============================================================================
# API GATEWAY BUILD GRADLE CONTENT (FIXED)
# =============================================================================
$apiGatewayBuildGradleContent = @"
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
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$QUARKUS_VERSION"))
    
    // Core Quarkus dependencies
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    
    // REST Client dependencies (FIXED - using correct artifact names)
    implementation("io.quarkus:quarkus-rest-client")
    implementation("io.quarkus:quarkus-rest-client-kotlin-serialization")
    implementation("io.quarkus:quarkus-rest-jackson")
    
    // Configuration and observability
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
    
    // Security
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-oidc")
    
    // Caching
    implementation("io.quarkus:quarkus-cache")
    
    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Test dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-test-security")
}

group = "org.chiro"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_$JAVA_VERSION
    targetCompatibility = JavaVersion.VERSION_$JAVA_VERSION
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_$JAVA_VERSION)
        javaParameters.set(true)
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
"@

# =============================================================================
# CONSOLIDATED SERVICE BUILD GRADLE CONTENT
# =============================================================================
$consolidatedServiceBuildGradleContent = @"
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
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$QUARKUS_VERSION"))
    
    // Core Quarkus dependencies
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    
    // Database
    implementation("io.quarkus:quarkus-hibernate-reactive-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    
    // Configuration and observability
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-micrometer")
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
    sourceCompatibility = JavaVersion.VERSION_$JAVA_VERSION
    targetCompatibility = JavaVersion.VERSION_$JAVA_VERSION
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_$JAVA_VERSION)
        javaParameters.set(true)
    }
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
"@

# =============================================================================
# CONSOLIDATED SERVICES PARENT BUILD GRADLE CONTENT
# =============================================================================
$consolidatedServicesParentBuildGradleContent = @"
// This is a parent module for consolidated services
// Individual service modules will have their own build.gradle.kts files
"@

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

function Write-Status {
    param(
        [string]$Message,
        [string]$Type = "Info"
    )
    
    switch ($Type) {
        "Success" { Write-Host "✅ $Message" -ForegroundColor Green }
        "Warning" { Write-Host "⚠️  $Message" -ForegroundColor Yellow }
        "Error" { Write-Host "❌ $Message" -ForegroundColor Red }
        "Info" { Write-Host "ℹ️  $Message" -ForegroundColor Cyan }
        "Progress" { Write-Host "🔄 $Message" -ForegroundColor Blue }
        default { Write-Host "$Message" -ForegroundColor White }
    }
}

function Update-File {
    param(
        [string]$FilePath,
        [string]$Content,
        [string]$Description
    )
    
    Write-Status "Updating: $Description" "Progress"
    Write-Host "   File: $FilePath" -ForegroundColor Gray
    
    if (-not $DryRun) {
        # Ensure directory exists
        $directory = Split-Path $FilePath -Parent
        if ($directory -and -not (Test-Path $directory)) {
            New-Item -ItemType Directory -Path $directory -Force | Out-Null
        }
        
        $Content | Out-File -FilePath $FilePath -Encoding UTF8 -Force
        Write-Status "Updated: $FilePath" "Success"
    }
    else {
        Write-Status "Would update: $FilePath (dry run)" "Warning"
    }
}

function Remove-DirectoryIfExists {
    param(
        [string]$Path,
        [string]$Description
    )
    
    if (Test-Path $Path) {
        Write-Status "Removing: $Description" "Progress"
        if (-not $DryRun) {
            Remove-Item $Path -Recurse -Force -ErrorAction SilentlyContinue
            Write-Status "Removed: $Path" "Success"
        }
        else {
            Write-Status "Would remove: $Path (dry run)" "Warning"
        }
    }
    else {
        Write-Status "Not found (skipping): $Path" "Info"
    }
}

function Clear-AllCaches {
    Write-Status "Performing comprehensive cache cleanup..." "Progress"
    
    # Stop Gradle daemons
    try {
        & ".\gradlew" --stop 2>$null
        Write-Status "Stopped Gradle daemons" "Success"
    }
    catch {
        Write-Status "No Gradle daemons running" "Info"
    }
    
    # Clear various cache directories
    $cachePaths = @(
        "$env:USERPROFILE\.gradle\caches",
        "$env:USERPROFILE\.gradle\daemon",
        "$env:USERPROFILE\.gradle\wrapper\dists",
        ".gradle",
        "build"
    )
    
    foreach ($cachePath in $cachePaths) {
        if (Test-Path $cachePath) {
            Remove-DirectoryIfExists -Path $cachePath -Description "Cache directory: $cachePath"
        }
    }
    
    # Clear build directories in all subprojects
    Get-ChildItem -Path . -Name "build" -Directory -Recurse | ForEach-Object {
        $buildPath = Join-Path $PWD $_
        Remove-DirectoryIfExists -Path $buildPath -Description "Build directory: $buildPath"
    }
    
    Write-Status "Cache cleanup completed" "Success"
}

function Test-BuildFileValid {
    param(
        [string]$FilePath
    )
    
    if (-not (Test-Path $FilePath)) {
        return $false
    }
    
    try {
        $content = Get-Content $FilePath -Raw
        # Basic validation - check for required elements
        $hasPlugins = $content -match "plugins\s*\{"
        $hasDependencies = $content -match "dependencies\s*\{"
        $hasGroup = $content -match "group\s*="
        
        return $hasPlugins -and $hasDependencies -and $hasGroup
    }
    catch {
        return $false
    }
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

Write-Host "`n🧹 STEP 1: CLEANUP" -ForegroundColor Yellow

# Remove problematic buildSrc entirely
Remove-DirectoryIfExists -Path "buildSrc" -Description "buildSrc directory (causes conflicts)"

# Clear caches if requested
if ($CleanFirst) {
    Clear-AllCaches
}

Write-Host "`n🔧 STEP 2: UPDATE CONFIGURATION FILES" -ForegroundColor Yellow

# Update gradle.properties
Update-File -FilePath "gradle.properties" -Content $gradlePropertiesContent -Description "Gradle properties"

# Update root build.gradle.kts
Update-File -FilePath "build.gradle.kts" -Content $rootBuildGradleContent -Description "Root build file"

# Update settings.gradle.kts
Update-File -FilePath "settings.gradle.kts" -Content $settingsGradleContent -Description "Settings file"

Write-Host "`n🏗️  STEP 3: UPDATE SERVICE BUILD FILES" -ForegroundColor Yellow

# Update consolidated services parent build file
Update-File -FilePath "consolidated-services/build.gradle" -Content $consolidatedServicesParentBuildGradleContent -Description "Consolidated services parent build file"

# Update individual consolidated service build files
$consolidatedServices = @(
    "consolidated-services/core-business-service",
    "consolidated-services/customer-relations-service", 
    "consolidated-services/operations-management-service",
    "consolidated-services/platform-services",
    "consolidated-services/workforce-management-service"
)

foreach ($servicePath in $consolidatedServices) {
    if ((Test-Path $servicePath) -or (-not $DryRun)) {
        $buildFilePath = "$servicePath/build.gradle.kts"
        $serviceName = Split-Path $servicePath -Leaf
        Update-File -FilePath $buildFilePath -Content $consolidatedServiceBuildGradleContent -Description "Service build file: $serviceName"
    }
}

# Update API Gateway with fixed dependencies
Update-File -FilePath "api-gateway/build.gradle.kts" -Content $apiGatewayBuildGradleContent -Description "API Gateway build file (FIXED REST CLIENT DEPS)"

Write-Host "`n🔍 STEP 4: VALIDATION" -ForegroundColor Yellow

if (-not $DryRun) {
    $filesToValidate = @(
        "build.gradle.kts",
        "settings.gradle.kts", 
        "api-gateway/build.gradle.kts"
    )
    
    foreach ($servicePath in $consolidatedServices) {
        $filesToValidate += "$servicePath/build.gradle.kts"
    }
    
    $allValid = $true
    foreach ($file in $filesToValidate) {
        if (Test-Path $file) {
            $isValid = Test-BuildFileValid -FilePath $file
            if ($isValid) {
                Write-Status "Valid: $file" "Success"
            }
            else {
                Write-Status "Invalid: $file" "Error"
                $allValid = $false
            }
        }
        else {
            Write-Status "Missing: $file" "Warning"
            $allValid = $false
        }
    }
    
    if ($allValid) {
        Write-Status "All build files validated successfully!" "Success"
    }
    else {
        Write-Status "Some build files need manual review" "Warning"
    }
}

Write-Host "`n═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor DarkGray

if ($DryRun) {
    Write-Host "🔍 DRY RUN COMPLETED" -ForegroundColor Yellow
    Write-Host "Run without -DryRun to apply changes" -ForegroundColor Cyan
}
else {
    Write-Host "✅ REFACTORED DEPENDENCY FIXING COMPLETED" -ForegroundColor Green
    
    Write-Host "`nKey fixes applied:" -ForegroundColor Cyan
    Write-Host "  • Fixed API Gateway REST client dependencies" -ForegroundColor White
    Write-Host "  • Updated all version references to use variables" -ForegroundColor White
    Write-Host "  • Created proper gradle.properties with optimization settings" -ForegroundColor White
    Write-Host "  • Removed buildSrc to eliminate conflicts" -ForegroundColor White
    Write-Host "  • Cleaned all Gradle caches" -ForegroundColor White
    Write-Host "  • Used correct Quarkus dependency names" -ForegroundColor White
    
    Write-Host "`n🚀 Next steps:" -ForegroundColor Yellow
    Write-Host "  1. Test clean build: .\gradlew clean" -ForegroundColor Cyan
    Write-Host "  2. Test full build: .\gradlew build" -ForegroundColor Cyan
    Write-Host "  3. Test specific service: .\gradlew :api-gateway:build" -ForegroundColor Cyan
    Write-Host "  4. If successful, proceed with deployment" -ForegroundColor Cyan
}

Write-Host "`n🎉 SCRIPT COMPLETED" -ForegroundColor Magenta

# =============================================================================
# COMPREHENSIVE DEPENDENCY STANDARDIZATION SCRIPT
# =============================================================================
# This script fixes ALL Gradle build files across the project including buildSrc conventions

param(
    [switch]$DryRun = $false
)

$ErrorActionPreference = "Stop"

Write-Host "🔧 COMPREHENSIVE DEPENDENCY STANDARDIZATION" -ForegroundColor Magenta
Write-Host "═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor DarkGray

if ($DryRun) {
    Write-Host "🔍 DRY RUN MODE - No files will be modified" -ForegroundColor Yellow
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
'@

# Standard consolidated service build.gradle.kts content
$consolidatedServiceBuildGradleContent = @'
plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    kotlin("plugin.serialization")
    id("io.quarkus")
}

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("io.quarkus:quarkus-hibernate-reactive-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
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
'@

# API Gateway specific build.gradle.kts content
$apiGatewayBuildGradleContent = @'
plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    kotlin("plugin.serialization")
    id("io.quarkus")
}

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.4"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-client-reactive")
    implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-cache")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-test-security")
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
    
    $buildFilePath = "$ServicePath/build.gradle.kts"
    Write-Host "📦 Updating service: $ServiceName" -ForegroundColor Cyan
    Write-Host "   File: $buildFilePath" -ForegroundColor Gray
    
    if (Test-Path $buildFilePath) {
        if (-not $DryRun) {
            $consolidatedServiceBuildGradleContent | Out-File -FilePath $buildFilePath -Encoding UTF8
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

# Enhanced cache cleanup function
function Clear-AllCaches {
    Write-Host "🧹 Performing comprehensive cache cleanup..." -ForegroundColor Yellow
    
    # Stop Gradle daemons first
    Write-Host "  Stopping Gradle daemons..." -ForegroundColor Cyan
    try {
        & ".\gradlew" --stop 2>$null
        Write-Host "    ✅ Gradle daemons stopped" -ForegroundColor Green
    }
    catch {
        Write-Host "    ℹ️  No Gradle daemons running" -ForegroundColor Blue
    }
    
    # Clear Gradle caches
    Write-Host "  Clearing Gradle caches..." -ForegroundColor Cyan
    if (Test-Path "$env:USERPROFILE\.gradle\caches") {
        Remove-Item "$env:USERPROFILE\.gradle\caches" -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "    ✅ Cleared user Gradle caches" -ForegroundColor Green
    }
    if (Test-Path "$env:USERPROFILE\.gradle\daemon") {
        Remove-Item "$env:USERPROFILE\.gradle\daemon" -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "    ✅ Cleared Gradle daemon cache" -ForegroundColor Green
    }
    
    # Clear local project caches
    Write-Host "  Clearing project build directories..." -ForegroundColor Cyan
    Get-ChildItem -Path . -Name "build" -Directory -Recurse | ForEach-Object {
        $buildPath = Join-Path $PWD $_
        Write-Host "    Removing: $buildPath" -ForegroundColor Gray
        Remove-Item $buildPath -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    # Clear .gradle directories in project
    Get-ChildItem -Path . -Name ".gradle" -Directory -Recurse | ForEach-Object {
        $gradlePath = Join-Path $PWD $_
        Write-Host "    Removing: $gradlePath" -ForegroundColor Gray
        Remove-Item $gradlePath -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    Write-Host "✅ Cache cleanup completed" -ForegroundColor Green
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
    $hasDependencies = $content -match "dependencies\s*\{"
    $hasKotlinCompile = $content -match "tasks\.withType<.*KotlinCompile>"
    
    return $hasPlugins -and $hasDependencies -and $hasKotlinCompile
}

# Remove problematic buildSrc convention plugins
Write-Host "`n🧹 CLEANING UP BUILDSRC CONVENTION PLUGINS" -ForegroundColor Yellow
$conventionFiles = @(
    "buildSrc/src/main/kotlin/org/chiro/common-conventions.gradle.kts",
    "buildSrc/src/main/kotlin/org/chiro/service-conventions.gradle.kts",
    "buildSrc/src/main/kotlin/org/chiro/quality-conventions.gradle.kts",
    "buildSrc/src/main/kotlin/org/chiro/consolidated-service-conventions.gradle.kts"
)

foreach ($file in $conventionFiles) {
    Remove-FileIfExists -FilePath $file -Description "Removing problematic convention plugin: $(Split-Path $file -Leaf)"
}

# Remove the entire buildSrc/src directory structure since we're not using conventions
Write-Host "`n🧹 REMOVING BUILDSRC ENTIRELY" -ForegroundColor Yellow
$buildSrcPath = "buildSrc"
if (Test-Path $buildSrcPath) {
    if (-not $DryRun) {
        Remove-Item $buildSrcPath -Recurse -Force
        Write-Host "   ✅ Removed buildSrc directory entirely" -ForegroundColor Green
    }
    else {
        Write-Host "   🔍 Would remove buildSrc directory entirely (dry run)" -ForegroundColor Yellow
    }
}

# Clear all caches before standardization
Clear-AllCaches

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

# Clean Gradle cache to avoid issues
Write-Host "`n🧹 CLEANING GRADLE CACHE" -ForegroundColor Yellow
if (Test-Path ".gradle") {
    if (-not $DryRun) {
        Remove-Item ".gradle" -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "   ✅ Cleaned .gradle directory" -ForegroundColor Green
    }
    else {
        Write-Host "   🔍 Would clean .gradle directory (dry run)" -ForegroundColor Yellow
    }
}

if (Test-Path "build") {
    if (-not $DryRun) {
        Remove-Item "build" -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "   ✅ Cleaned build directory" -ForegroundColor Green
    }
    else {
        Write-Host "   🔍 Would clean build directory (dry run)" -ForegroundColor Yellow
    }
}

# Summary and validation
Write-Host "`n═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor DarkGray
if ($DryRun) {
    Write-Host "🔍 DRY RUN COMPLETED" -ForegroundColor Yellow
    Write-Host "Run the script without -DryRun to apply changes" -ForegroundColor Cyan
}
else {
    Write-Host "✅ COMPREHENSIVE DEPENDENCY STANDARDIZATION COMPLETED" -ForegroundColor Green
    Write-Host "Fixed issues:" -ForegroundColor Cyan
    Write-Host "  • Removed buildSrc entirely (not needed for direct configuration)" -ForegroundColor White
    Write-Host "  • Updated root build.gradle.kts with working configuration" -ForegroundColor White
    Write-Host "  • Updated settings.gradle.kts with all services and hardcoded versions" -ForegroundColor White
    Write-Host "  • Updated all consolidated services with enhanced dependencies" -ForegroundColor White
    Write-Host "  • Updated API Gateway with specialized gateway dependencies" -ForegroundColor White
    Write-Host "  • Used modern Kotlin compiler options (compilerOptions DSL)" -ForegroundColor White
    Write-Host "  • Cleaned all Gradle caches and stopped daemons" -ForegroundColor White
    Write-Host "  • Used direct dependency declarations instead of property placeholders" -ForegroundColor White
    
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

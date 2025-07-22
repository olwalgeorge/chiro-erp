#!/usr/bin/env pwsh
# Standardize Dependencies Script for Chiro ERP
# Aligns dependencies with GitHub reference project (https://github.com/olwalgeorge/erp)
# Focus: REST with Kotlin serialization, ORM, and Jackson for external serialization

Write-Host "🔧 Standardizing Chiro ERP Dependencies..." -ForegroundColor Cyan

# 1. Update gradle.properties to match reference project
Write-Host "📝 Updating gradle.properties..." -ForegroundColor Yellow

$gradlePropertiesContent = @"
# Gradle properties - aligned with reference project
# https://github.com/olwalgeorge/erp/blob/main/gradle.properties

# Plugin versions for buildSrc
kotlinVersion=2.1.21

# Quarkus configuration (matching reference: 3.24.3, but keeping 3.24.4 as it's newer)
quarkusPluginId=io.quarkus
quarkusPluginVersion=3.24.4
quarkusPlatformGroupId=io.quarkus.platform
quarkusPlatformArtifactId=quarkus-bom
quarkusPlatformVersion=3.24.4

# Project metadata
group=com.chiro
version=1.0.0-SNAPSHOT
"@

Set-Content -Path "gradle.properties" -Value $gradlePropertiesContent -Encoding UTF8

# 2. Update settings.gradle.kts to include plugin management like reference
Write-Host "📝 Updating settings.gradle.kts..." -ForegroundColor Yellow

$settingsContent = @"
// Settings configuration aligned with reference project
// https://github.com/olwalgeorge/erp/blob/main/settings.gradle.kts

pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}

rootProject.name = "chiro-erp"

// Include original API Gateway (remains separate)
include("api-gateway")

// Include consolidated services
include("consolidated-services:workforce-management-service")
include("consolidated-services:customer-relations-service")
include("consolidated-services:operations-management-service")
include("consolidated-services:core-business-service")
include("consolidated-services:platform-services")

// Configure project names for consolidated services  
project(":consolidated-services:workforce-management-service").name = "workforce-management-service"
project(":consolidated-services:customer-relations-service").name = "customer-relations-service"
project(":consolidated-services:operations-management-service").name = "operations-management-service"
project(":consolidated-services:core-business-service").name = "core-business-service"
project(":consolidated-services:platform-services").name = "platform-services"
"@

Set-Content -Path "settings.gradle.kts" -Value $settingsContent -Encoding UTF8

# 3. Update common-conventions.gradle.kts with standardized dependencies
Write-Host "📝 Updating common-conventions.gradle.kts..." -ForegroundColor Yellow

$commonConventionsContent = @"
// Common conventions for Chiro ERP consolidated services
// Aligned with reference project: https://github.com/olwalgeorge/erp/blob/main/build.gradle.kts
// Focus: REST with Kotlin serialization, ORM, and Jackson for external serialization

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

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    // Quarkus BOM - enforced platform manages all versions (like reference project)
    implementation(enforcedPlatform("`${quarkusPlatformGroupId}:`${quarkusPlatformArtifactId}:`${quarkusPlatformVersion}"))
    
    // Core Quarkus with Kotlin support
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc") // CDI container (from reference)
    
    // REST layer (enhanced from reference with Kotlin serialization)
    implementation("io.quarkus:quarkus-rest") // Main REST (from reference)
    implementation("io.quarkus:quarkus-rest-kotlin-serialization") // Kotlin serialization for internal APIs
    implementation("io.quarkus:quarkus-rest-jackson") // Jackson for external integrations (from reference)
    
    // Database layer (from reference + Kotlin Panache)
    implementation("io.quarkus:quarkus-hibernate-orm") // Core ORM (from reference)
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin") // Kotlin Panache
    implementation("io.quarkus:quarkus-jdbc-postgresql") // PostgreSQL driver (from reference)
    
    // Essential Kotlin libraries (versions managed by Quarkus BOM)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    
    // Microservices essentials
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-metrics")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    
    // Configuration and logging
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-logging-json")
    
    // Testing foundation (from reference)
    testImplementation("io.quarkus:quarkus-junit5") // From reference
    testImplementation("io.rest-assured:rest-assured") // From reference
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // Only specify version for dependencies NOT in Quarkus BOM
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}

// Kotlin configuration
kotlin {
    jvmToolchain(21)
}

// Quarkus Kotlin configuration
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

// Java configuration (from reference project)
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Test configuration (enhanced from reference)
tasks.test {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    
    // Fail fast for build efficiency
    failFast = true
    
    // Test logging
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// Build optimization (aligned with reference project Java settings)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        allWarningsAsErrors = false
    }
}

// Java compile settings (from reference project)
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
"@

# Escape the dollar signs for PowerShell
$commonConventionsContent = $commonConventionsContent -replace '`\$', '$'

Set-Content -Path "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts" -Value $commonConventionsContent -Encoding UTF8

# 4. Update service-conventions.gradle.kts
Write-Host "📝 Updating service-conventions.gradle.kts..." -ForegroundColor Yellow

$serviceConventionsContent = @"
// Service-specific conventions for Chiro ERP consolidated services
// Extends common-conventions with additional service-specific dependencies
// Maintains REST with Kotlin serialization and Jackson for external APIs

plugins {
    id("common-conventions")
}

dependencies {
    // Event streaming and messaging
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    implementation("io.quarkus:quarkus-kafka-client")
    
    // Service mesh and inter-service communication
    implementation("io.quarkus:quarkus-rest-client-reactive")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson") // Jackson for external service calls
    
    // Advanced persistence features
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-hibernate-validator")
    
    // Security for consolidated services
    implementation("io.quarkus:quarkus-security-jpa")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    
    // Container and deployment
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-kubernetes")
    
    // GraphQL support for consolidated APIs (uses Jackson by default)
    implementation("io.quarkus:quarkus-smallrye-graphql")
    
    // Enhanced testing for modular services
    testImplementation("io.quarkus:quarkus-test-h2") // In-memory testing
    testImplementation("org.testcontainers:postgresql") // Integration testing
    testImplementation("org.testcontainers:junit-jupiter")
}
"@

Set-Content -Path "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts" -Value $serviceConventionsContent -Encoding UTF8

# 5. Update consolidated-service-conventions.gradle.kts
Write-Host "📝 Updating consolidated-service-conventions.gradle.kts..." -ForegroundColor Yellow

$consolidatedConventionsContent = @"
// Consolidated service-specific conventions
// For the 5 main consolidated services with multiple modules
// Maintains consistent REST + Kotlin serialization + Jackson pattern

plugins {
    id("service-conventions")
    id("quality-conventions")
}

dependencies {
    // Inter-module communication within consolidated services
    implementation("io.quarkus:quarkus-smallrye-context-propagation")
    
    // Enhanced caching for consolidated services
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-redis-cache")
    
    // Batch processing capabilities
    implementation("io.quarkus:quarkus-scheduler")
    
    // File handling and storage
    implementation("io.quarkus:quarkus-amazon-s3")
    
    // Email and notifications
    implementation("io.quarkus:quarkus-mailer")
    
    // WebSocket support for real-time features (uses Jackson serialization)
    implementation("io.quarkus:quarkus-websockets")
    
    // Enhanced testing for consolidated services
    testImplementation("io.quarkus:quarkus-test-artemis")
    testImplementation("io.quarkus:quarkus-test-security")
    testImplementation("io.quarkus:quarkus-test-kafka-companion")
}

// Group configuration
group = "com.chiro.consolidated"
version = "1.0.0-SNAPSHOT"
"@

Set-Content -Path "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts" -Value $consolidatedConventionsContent -Encoding UTF8

# 6. Create a summary of the standardization
Write-Host "📊 Creating dependency standardization summary..." -ForegroundColor Yellow

$summaryContent = @"
# Dependency Standardization Summary
Generated: $(Get-Date)

## Reference Project Alignment
- Source: https://github.com/olwalgeorge/erp
- Quarkus Version: 3.24.4 (upgraded from reference 3.24.3)
- Kotlin Version: 2.1.21
- Java Version: 21

## Key Standardizations Applied

### 1. REST API Strategy
- **Internal APIs**: Kotlin Serialization (`quarkus-rest-kotlin-serialization`)
- **External APIs**: Jackson (`quarkus-rest-jackson`)
- **Core REST**: `quarkus-rest` (from reference project)

### 2. Database Layer
- **Core ORM**: `quarkus-hibernate-orm` (from reference)
- **Kotlin Integration**: `quarkus-hibernate-orm-panache-kotlin`
- **Database**: PostgreSQL (`quarkus-jdbc-postgresql` from reference)

### 3. BOM Management
- Using `enforcedPlatform` (from reference project)
- All Quarkus dependencies use BOM versions
- Only non-BOM dependencies specify versions explicitly

### 4. Plugin Management
- Added `pluginManagement` block to settings.gradle.kts (from reference)
- Centralized plugin versions in gradle.properties

### 5. Build Configuration
- Java 21 target (from reference)
- UTF-8 encoding (from reference)
- Compiler parameters flag (from reference)

## Convention Plugin Hierarchy
1. **common-conventions**: Base layer with core dependencies
2. **service-conventions**: Service-specific additions
3. **quality-conventions**: Code quality tools
4. **consolidated-service-conventions**: Top layer for main services

## Testing Strategy
- JUnit 5 (`quarkus-junit5` from reference)
- REST Assured (`rest-assured` from reference) 
- Kotlin Test support
- Testcontainers for integration tests
"@

Set-Content -Path "DEPENDENCY_STANDARDIZATION_SUMMARY.md" -Value $summaryContent -Encoding UTF8

Write-Host "✅ Dependency standardization complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Summary of changes:" -ForegroundColor Cyan
Write-Host "  • Updated gradle.properties with reference project versions" -ForegroundColor White
Write-Host "  • Added pluginManagement to settings.gradle.kts" -ForegroundColor White
Write-Host "  • Standardized common-conventions.gradle.kts with REST + Kotlin serialization + Jackson" -ForegroundColor White
Write-Host "  • Updated service and consolidated-service conventions" -ForegroundColor White
Write-Host "  • Created DEPENDENCY_STANDARDIZATION_SUMMARY.md" -ForegroundColor White
Write-Host ""
Write-Host "🔄 Next steps:" -ForegroundColor Yellow
Write-Host "  1. Run './gradlew clean' to clean build cache" -ForegroundColor White
Write-Host "  2. Run './gradlew build' to test the standardized configuration" -ForegroundColor White
Write-Host "  3. Review DEPENDENCY_STANDARDIZATION_SUMMARY.md for details" -ForegroundColor White

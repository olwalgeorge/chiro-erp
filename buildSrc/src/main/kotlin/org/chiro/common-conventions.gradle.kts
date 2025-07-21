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
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    
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

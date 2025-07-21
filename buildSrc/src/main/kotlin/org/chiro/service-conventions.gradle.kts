// Service-specific conventions for Chiro ERP consolidated services
// Optimized for monolithic services with modular architecture

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    kotlin("plugin.serialization")
    id("io.quarkus")
    id("common-conventions")
}

dependencies {
    // Additional service-specific dependencies
    
    // Event streaming and messaging
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    implementation("io.quarkus:quarkus-kafka-client")
    
    // Service mesh and inter-service communication
    implementation("io.quarkus:quarkus-rest-client-reactive")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson")
    
    // Advanced persistence features
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-hibernate-validator")
    
    // Security for consolidated services
    implementation("io.quarkus:quarkus-security-jpa")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    
    // Container and deployment
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-kubernetes")
    
    // GraphQL support for consolidated APIs
    implementation("io.quarkus:quarkus-smallrye-graphql")
    
    // Enhanced testing for modular services
    testImplementation("io.quarkus:quarkus-test-h2")
    testImplementation("io.quarkus:quarkus-test-security")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
}

// Quarkus configuration for consolidated services
quarkus {
    extension {
        // Enable native compilation optimization
        buildNative {
            additionalBuildArgs = [
                "--initialize-at-build-time=org.slf4j.LoggerFactory",
                "--initialize-at-build-time=org.slf4j.impl.StaticLoggerBinder"
            ]
        }
    }
}

// Enhanced JVM configuration for consolidated services
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-Xcontext-receivers"
        )
    }
}

// Test configuration optimized for modular testing
tasks.test {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    
    // Increased timeouts for consolidated service testing
    systemProperty("quarkus.test.timeout", "300")
    
    // Memory configuration for testing multiple modules
    minHeapSize = "512m"
    maxHeapSize = "2g"
    
    // Parallel execution for module tests
    maxParallelForks = Runtime.runtime.availableProcessors().div(2).takeIf { it > 0 } ?: 1
}

// Docker configuration for consolidated services
tasks.named("buildDockerImage") {
    dependsOn("build")
}

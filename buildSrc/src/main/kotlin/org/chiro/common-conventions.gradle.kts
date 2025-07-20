// Common conventions for Chiro ERP microservices within monolithic structure
// Provides shared dependencies and configurations

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
    // Quarkus BOM - manages all versions (using gradle.properties version)
    implementation(platform("io.quarkus.platform:quarkus-bom:${property("quarkusVersion")}"))
    
    // Core Quarkus with Kotlin support
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc") // CDI container
    
    // REST with Kotlin Serialization (consistent across all services)
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    
    // Database layer - Hibernate ORM with Kotlin Panache
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    
    // Essential Kotlin libraries
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    
    // Microservices essentials
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-metrics")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    
    // Testing foundation
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

// Kotlin configuration
kotlin {
    jvmToolchain(21)
    
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        javaParameters.set(true)
        freeCompilerArgs.set(listOf("-Xjsr305=strict"))
    }
}

// Quarkus AllOpen configuration for Kotlin
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

// Testing configuration
tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

// Ensure consistent Kotlin serialization, exclude Jackson completely
configurations.all {
    exclude(group = "com.fasterxml.jackson.core")
    exclude(group = "com.fasterxml.jackson.annotation") 
    exclude(group = "com.fasterxml.jackson.databind")
    exclude(group = "io.quarkus", module = "quarkus-resteasy-reactive-jackson")
    exclude(group = "io.quarkus", module = "quarkus-resteasy-jackson")
}

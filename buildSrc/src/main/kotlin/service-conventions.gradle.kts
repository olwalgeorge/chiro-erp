// Service-specific configuration for Quarkus microservices
plugins {
    id("common-conventions")
    kotlin("plugin.allopen")
    kotlin("plugin.serialization")
    id("io.quarkus")
}

// Open Quarkus classes for testing
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

// Quarkus BOM will manage all Quarkus dependency versions
dependencies {
    // Use BOM for all Quarkus dependencies - no versions needed
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:3.24.4"))
    
    // Essential Core Quarkus dependencies that every service needs
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    
    // Standardized REST API with Kotlin Serialization
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    
    // Standardized Database Access with Hibernate ORM and Panache Kotlin
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-hibernate-validator")
    
    // Database Migration
    implementation("io.quarkus:quarkus-flyway")
    
    // Security
    implementation("io.quarkus:quarkus-security-jpa")
    
    // Testing dependencies that every service needs
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

quarkus {
    buildForkOptions {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    }
}

tasks.named("quarkusBuild") {
    doFirst {
        logger.info("Building Quarkus service: ${project.name}")
    }
}

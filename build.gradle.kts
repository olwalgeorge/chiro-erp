plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    
    // Core Quarkus with Kotlin
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    
    // REST with Kotlin Serialization (NOT Jackson)
    implementation("io.quarkus:quarkus-rest-kotlin-serialization") 
    
    // Database - Hibernate ORM Panache with Kotlin
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    
    // Container image
    implementation("io.quarkus:quarkus-container-image-docker")
    
    // Kotlin Standard Libraries
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Explicitly exclude Jackson to avoid conflicts
    configurations.all {
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.annotation") 
        exclude(group = "com.fasterxml.jackson.databind")
        exclude(group = "io.quarkus", module = "quarkus-resteasy-reactive-jackson")
        exclude(group = "io.quarkus", module = "quarkus-resteasy-jackson")
    }
    
    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "org.chiro"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        javaParameters = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

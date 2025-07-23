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

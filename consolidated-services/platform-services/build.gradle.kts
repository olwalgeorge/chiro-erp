plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
    id("org.jetbrains.kotlin.plugin.jpa")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId
    
    // Billing module - Platform billing management
    implementation(project(":billing"))
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-jsonb")
    implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    
    // Quarkus Core
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-jackson")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    
    // Database
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")
    
    // Messaging
    implementation("io.quarkus:quarkus-kafka-client")
    implementation("io.quarkus:quarkus-kafka-streams")
    
    // Monitoring & Health
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-health")
    implementation("io.quarkus:quarkus-info")
    
    // Security
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-security-jpa")
    
    // Validation
    
    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-test-h2")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        javaParameters = true
    }
}




// GraalVM Native Configuration
quarkus {
    buildForkOptions {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
        systemProperty("maven.home", System.getenv("M2_HOME"))
    }
}

tasks {
    register("buildNative") {
        group = "build"
        description = "Build native executable"
        
        doLast {
            exec {
                commandLine("./gradlew", "build", "-Dquarkus.package.type=native")
            }
        }
    }
    
    register("dockerBuildNative") {
        dependsOn("buildNative")
        group = "docker"
        description = "Build native Docker image"
        
        doLast {
            exec {
                commandLine("docker", "build", "-f", "docker/Dockerfile.native", "-t", "-native", ".")
            }
        }
    }
}

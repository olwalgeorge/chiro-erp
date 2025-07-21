plugins {
    id("consolidated-service-conventions")
}

dependencies {
    // Operations Management Service specific dependencies
    
    // Field service and project management
    implementation("io.quarkus:quarkus-scheduler")
    
    // Fleet and asset tracking
    implementation("io.quarkus:quarkus-cache")
    
    // Additional operations dependencies will be inherited from conventions
    
    // Database
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
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
    implementation("io.quarkus:quarkus-hibernate-validator")
    
    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-test-h2")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
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

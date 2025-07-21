plugins {
    id("service-conventions")
}

// API Gateway specific dependencies
dependencies {
    // API Gateway & Routing
    implementation("io.quarkus:quarkus-vertx-http")
    implementation("io.quarkus:quarkus-reactive-routes")
    
    // Service Discovery & Load Balancing
    
    // Security & Authentication
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    
    // Circuit Breaker & Fault Tolerance
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
    
    // Rate Limiting & Throttling  
    implementation("io.quarkus:quarkus-rate-limiter")
    
    // API Documentation aggregation
    implementation("io.quarkus:quarkus-smallrye-openapi")
    
    // Logging & Tracing for Gateway
    implementation("io.quarkus:quarkus-logging-json")
    implementation("io.quarkus:quarkus-opentelemetry")
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
                commandLine("docker", "build", "-f", "docker/Dockerfile.native", "-t", "api-gateway:latest-native", ".")
            }
        }
    }
}

plugins {
    id("service-conventions")
}

// API Gateway specific dependencies
dependencies {
    // API Gateway & Routing
    implementation("io.quarkus:quarkus-vertx-http")
    implementation("io.quarkus:quarkus-reactive-routes")
    
    // Service Discovery & Load Balancing
    implementation("io.quarkus:quarkus-rest-client-reactive")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson") // For external API calls
    
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

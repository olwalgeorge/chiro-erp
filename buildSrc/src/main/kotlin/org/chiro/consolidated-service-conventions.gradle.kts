// Consolidated service-specific conventions
// For the 5 main consolidated services with multiple modules
// HYBRID SERIALIZATION: Kotlin + Jackson for maximum compatibility and performance

plugins {
    id("service-conventions")
    id("quality-conventions")
}

dependencies {
    // Inter-module communication within consolidated services
    implementation("io.quarkus:quarkus-smallrye-context-propagation")
    
    // Enhanced caching for consolidated services
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-redis-cache")
    
    // Batch processing capabilities
    implementation("io.quarkus:quarkus-scheduler")
    
    // File handling and storage
    implementation("io.quarkus:quarkus-amazon-s3")
    
    // Email and notifications
    implementation("io.quarkus:quarkus-mailer")
    
    // WebSocket support for real-time features (supports both serializers)
    implementation("io.quarkus:quarkus-websockets")
    
    // External integrations (Jackson for compatibility)
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson") // External APIs
    
    // Enhanced testing for consolidated services
    testImplementation("io.quarkus:quarkus-test-h2") // In-memory testing
    testImplementation("org.testcontainers:postgresql") // Integration testing
    testImplementation("org.testcontainers:junit-jupiter")
}

// Group configuration
group = "com.chiro.consolidated"
version = "1.0.0-SNAPSHOT"

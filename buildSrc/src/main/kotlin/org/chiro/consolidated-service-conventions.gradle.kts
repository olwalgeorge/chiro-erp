// Consolidated service-specific conventions
// For the 5 main consolidated services with multiple modules
// Maintains consistent REST + Kotlin serialization + Jackson pattern

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
    
    // WebSocket support for real-time features (uses Jackson serialization)
    implementation("io.quarkus:quarkus-websockets")
    
    // Enhanced testing for consolidated services
    testImplementation("io.quarkus:quarkus-test-artemis")
    testImplementation("io.quarkus:quarkus-test-security")
    testImplementation("io.quarkus:quarkus-test-kafka-companion")
}

// Group configuration
group = "com.chiro.consolidated"
version = "1.0.0-SNAPSHOT"

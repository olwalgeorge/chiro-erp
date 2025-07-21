// Service-specific conventions for Chiro ERP consolidated services
// Extends common-conventions with additional service-specific dependencies
// Maintains REST with Kotlin serialization and Jackson for external APIs

plugins {
    id("common-conventions")
}

dependencies {
    // Event streaming and messaging
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    implementation("io.quarkus:quarkus-kafka-client")
    
    // Service mesh and inter-service communication
    implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization") // Kotlin serialization for internal APIs
    
    // Advanced persistence features
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-hibernate-validator")
    
    // Security for consolidated services
    implementation("io.quarkus:quarkus-security-jpa")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    
    // Container and deployment
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-kubernetes")
    
    // GraphQL support for consolidated APIs (uses Jackson by default)
    implementation("io.quarkus:quarkus-smallrye-graphql")
    
    // Enhanced testing for modular services
    testImplementation("io.quarkus:quarkus-test-h2") // In-memory testing
    testImplementation("org.testcontainers:postgresql") // Integration testing
    testImplementation("org.testcontainers:junit-jupiter")
}


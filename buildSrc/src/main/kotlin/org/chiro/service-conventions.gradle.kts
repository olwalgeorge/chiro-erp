// Service-specific conventions for Chiro ERP consolidated services
// Extends common-conventions with additional service-specific dependencies
// HYBRID SERIALIZATION: Kotlin for internal APIs, Jackson for external integrations

plugins {
    id("common-conventions")
}

dependencies {
    // Event streaming and messaging
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    implementation("io.quarkus:quarkus-kafka-client")
    
    // Service mesh and inter-service communication (HYBRID APPROACH)
    implementation("io.quarkus:quarkus-rest-client-reactive")
    implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization") // Internal service calls
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson") // External service integrations
    
    // Advanced persistence features
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-hibernate-validator")
    
    // Security for consolidated services
    implementation("io.quarkus:quarkus-security-jpa")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    
    // Container and deployment
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-kubernetes")
    
    // GraphQL support for consolidated APIs (HYBRID: uses both serializers)
    implementation("io.quarkus:quarkus-smallrye-graphql")
    implementation("io.quarkus:quarkus-smallrye-graphql-client") // For external GraphQL APIs
    
    // Enhanced testing for modular services
    testImplementation("io.quarkus:quarkus-test-h2") // In-memory testing
    testImplementation("org.testcontainers:postgresql") // Integration testing
    testImplementation("org.testcontainers:junit-jupiter")
}

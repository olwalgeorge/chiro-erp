plugins {
    id("service-conventions")
    id("quality-conventions")
    id("testing-conventions")
}

description = "API Gateway - Central API gateway and routing"

dependencies {
    // API Gateway specific dependencies
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-opentelemetry")
    
    // Standard dependencies (REST Kotlin Serialization, Hibernate ORM Panache Kotlin, etc.)
    // are inherited from service-conventions plugin
}

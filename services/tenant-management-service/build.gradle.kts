plugins {
    id("service-conventions")
    id("quality-conventions")
    id("testing-conventions")
}

description = "Tenant Management Service - Multi-tenancy foundation"

dependencies {
    // Multi-tenancy specific dependencies
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-scheduler")
    
    // Testing specific to this service
    testImplementation("io.quarkus:quarkus-test-h2")
    
    // Standard dependencies (REST Kotlin Serialization, Hibernate ORM Panache Kotlin, etc.)
    // are inherited from service-conventions plugin
}
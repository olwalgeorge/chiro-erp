plugins {
    id("service-conventions")
    id("quality-conventions")
    id("testing-conventions")
}

description = "Repair Service - Equipment and maintenance tracking"

dependencies {
    // Repair-specific additional dependencies
    implementation("io.quarkus:quarkus-messaging-kafka")
    implementation("io.quarkus:quarkus-smallrye-graphql")
    
    // Standard dependencies (REST Kotlin Serialization, Hibernate ORM Panache Kotlin, etc.)
    // are inherited from service-conventions plugin
}

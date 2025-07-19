plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    implementation("io.quarkus:quarkus-smallrye-graphql")
    implementation("io.quarkus:quarkus-security-jpa")
    implementation("io.quarkus:quarkus-flyway")
    
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

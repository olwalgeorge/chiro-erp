// Consolidated service-specific conventions
// For the 5 main consolidated services with multiple modules

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
    
    // WebSocket support for real-time features
    implementation("io.quarkus:quarkus-websockets")
    
    // Enhanced testing for consolidated services
    testImplementation("io.quarkus:quarkus-test-artemis")
    testImplementation("org.testcontainers:junit-jupiter")
}

// Module structure validation
tasks.register("validateModuleStructure") {
    doLast {
        val modulesDir = project.file("modules")
        if (!modulesDir.exists()) {
            throw GradleException("Consolidated service must have a 'modules' directory")
        }
        
        val modules = modulesDir.listFiles()?.filter { it.isDirectory }
        if (modules.isNullOrEmpty()) {
            throw GradleException("Consolidated service must have at least one module")
        }
        
        println("✅ Validated modules in ${project.name}")
    }
}

// Ensure module validation runs before build
tasks.build {
    dependsOn("validateModuleStructure")
}

// Integration test configuration for consolidated services
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests for consolidated service modules"
    group = "verification"
    
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    
    useJUnitPlatform {
        includeTags("integration")
    }
    
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    
    // Longer timeout for integration tests
    systemProperty("quarkus.test.timeout", "600")
    
    // Test containers configuration
    systemProperty("testcontainers.reuse.enable", "true")
    
    shouldRunAfter("test")
}

// Module test task
tasks.register<Test>("moduleTest") {
    description = "Runs module-specific tests"
    group = "verification"
    
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    
    useJUnitPlatform {
        includeTags("module")
    }
    
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    shouldRunAfter("test")
}

// Add integration tests to check task
tasks.check {
    dependsOn("integrationTest", "moduleTest")
}

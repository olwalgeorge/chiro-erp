// Enhanced testing configuration
plugins {
    id("common-conventions")
    id("org.gradle.test-retry")
}

dependencies {
    // Testing framework dependencies with explicit versions
    // (not managed by Quarkus BOM)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.assertj:assertj-core:3.25.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks.withType<Test> {
    retry {
        maxRetries.set(2)
        maxFailures.set(5)
        failOnPassedAfterRetry.set(false)
    }
    
    // Parallel test execution
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    
    // Memory settings for tests
    minHeapSize = "512m"
    maxHeapSize = "2g"
    
    // Test filtering
    filter {
        includeTestsMatching("*Test")
        includeTestsMatching("*Tests")
    }
}

// Only create integration test source set if it doesn't already exist
if (!sourceSets.names.contains("integrationTest")) {
    sourceSets {
        create("integrationTest") {
            kotlin.srcDir("src/integrationTest/kotlin")
            resources.srcDir("src/integrationTest/resources")
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }
    
    val integrationTestImplementation: Configuration by configurations.getting {
        extendsFrom(configurations.testImplementation.get())
    }
    
    val integrationTest by tasks.registering(Test::class) {
        description = "Runs integration tests."
        group = "verification"
        
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        shouldRunAfter("test")
        
        useJUnitPlatform()
    }
}

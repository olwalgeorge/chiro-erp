plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Quarkus plugin for access to BOM and version management
    implementation("io.quarkus:gradle-application-plugin:3.24.4")
    
    // Kotlin plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.1.21")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.1.21")
    
    // Code Quality plugins
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.4")
    
    // Build & Testing plugins
    implementation("org.gradle.test-retry:org.gradle.test-retry.gradle.plugin:1.5.8")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.50.0")
    
    // Container & Deployment
    implementation("com.github.node-gradle:gradle-node-plugin:7.0.1")
}

// Kotlin configuration for buildSrc
kotlin {
    jvmToolchain(21)
}

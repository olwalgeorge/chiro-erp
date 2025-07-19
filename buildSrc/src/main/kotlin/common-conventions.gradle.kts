// Common configuration applied to all modules
plugins {
    kotlin("jvm")
    id("jacoco")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

configurations.all {
    resolutionStrategy {
        // Align all Kotlin stdlib versions
        force("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.21")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}

tasks.withType<JacocoReport> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Ensure jacocoTestReport task is created after test task
tasks.named<Test>("test") {
    finalizedBy(tasks.named("jacocoTestReport"))
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus") version "3.24.4"
        id("org.jetbrains.kotlin.jvm") version "2.1.21"
        id("org.jetbrains.kotlin.plugin.allopen") version "2.1.21"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    }
}

rootProject.name = "chiro-erp"

// Consolidated services
include("consolidated-services")
include("consolidated-services:core-business-service")
include("consolidated-services:customer-relations-service")
include("consolidated-services:operations-management-service")
include("consolidated-services:platform-services")
include("consolidated-services:workforce-management-service")

// API Gateway
include("api-gateway")

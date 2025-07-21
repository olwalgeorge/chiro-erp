// Settings configuration aligned with reference project
// https://github.com/olwalgeorge/erp/blob/main/settings.gradle.kts

pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}

rootProject.name = "chiro-erp"

// Include original API Gateway (remains separate)
include("api-gateway")

// Include consolidated services
include("consolidated-services:workforce-management-service")
include("consolidated-services:customer-relations-service")
include("consolidated-services:operations-management-service")
include("consolidated-services:core-business-service")
include("consolidated-services:platform-services")

// Configure project names for consolidated services  
project(":consolidated-services:workforce-management-service").name = "workforce-management-service"
project(":consolidated-services:customer-relations-service").name = "customer-relations-service"
project(":consolidated-services:operations-management-service").name = "operations-management-service"
project(":consolidated-services:core-business-service").name = "core-business-service"
project(":consolidated-services:platform-services").name = "platform-services"

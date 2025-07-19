pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus") version "3.24.4"
    }
}

rootProject.name = "chiro-erp"

// Include all services
include(":api-gateway")
include(":services:tenant-management-service")
include(":services:user-management-service")
include(":services:crm-service")
include(":services:sales-service")
include(":services:inventory-service")
include(":services:procurement-service")
include(":services:manufacturing-service")
include(":services:project-service")
include(":services:hr-service")
include(":services:finance-service")
include(":services:billing-service")
include(":services:pos-service")
include(":services:fleet-service")
include(":services:fieldservice-service")
include(":services:repair-service")
include(":services:analytics-service")
include(":services:notifications-service")

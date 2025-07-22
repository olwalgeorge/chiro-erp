#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Comprehensive verification script for Chiro ERP consolidated services structure
    
.DESCRIPTION
    Verifies that the consolidated services structure follows consistent patterns,
    validates paths, checks module organization, and ensures all dependencies
    and configurations are properly set up.
    
.PARAMETER Fix
    Automatically fix issues where possible
    
.PARAMETER Detailed
    Show detailed information about each check
    
.EXAMPLE
    .\verify-service-structure-consistency.ps1
    .\verify-service-structure-consistency.ps1 -Fix -Detailed
#>

param(
    [switch]$Fix,
    [switch]$Detailed
)

# Set error handling
$ErrorActionPreference = "Stop"

# Define consolidated services structure
$ConsolidatedServices = @{
    "core-business-service"         = @{
        "description" = "Core ERP business operations"
        "modules"     = @("finance", "inventory", "sales", "procurement", "manufacturing")
        "port"        = 8080
        "database"    = "chiro_core_business"
    }
    "operations-management-service" = @{
        "description" = "Operational services"
        "modules"     = @("fleet", "project", "fieldservice", "repair")
        "port"        = 8081
        "database"    = "chiro_operations"
    }
    "customer-relations-service"    = @{
        "description" = "Customer relationship management"
        "modules"     = @("crm")
        "port"        = 8082
        "database"    = "chiro_customer_relations"
    }
    "platform-services"             = @{
        "description" = "Platform infrastructure"
        "modules"     = @("analytics", "notifications", "tenant-management", "billing")
        "port"        = 8083
        "database"    = "chiro_platform"
    }
    "workforce-management-service"  = @{
        "description" = "Human resources and workforce"
        "modules"     = @("hr", "user-management")
        "port"        = 8084
        "database"    = "chiro_workforce"
    }
}

# Initialize counters
$Script:TotalChecks = 0
$Script:PassedChecks = 0
$Script:FailedChecks = 0
$Script:FixedIssues = 0
$Script:Issues = @()

function Write-CheckResult {
    param(
        [string]$Check,
        [string]$Status,
        [string]$Message = "",
        [string]$Details = ""
    )
    
    $Script:TotalChecks++
    
    $icon = switch ($Status) {
        "PASS" { "✅"; $Script:PassedChecks++ }
        "FAIL" { "❌"; $Script:FailedChecks++ }
        "WARN" { "⚠️ " }
        "INFO" { "ℹ️ " }
        "FIXED" { "🔧"; $Script:FixedIssues++ }
    }
    
    $color = switch ($Status) {
        "PASS" { "Green" }
        "FAIL" { "Red" }
        "WARN" { "Yellow" }
        "INFO" { "Cyan" }
        "FIXED" { "Magenta" }
    }
    
    Write-Host "$icon $Check" -ForegroundColor $color
    if ($Message) {
        Write-Host "   $Message" -ForegroundColor Gray
    }
    if ($Detailed -and $Details) {
        Write-Host "   Details: $Details" -ForegroundColor DarkGray
    }
    
    if ($Status -eq "FAIL") {
        $Script:Issues += @{
            Check   = $Check
            Message = $Message
            Details = $Details
        }
    }
}

function Test-DirectoryStructure {
    Write-Host "`n🏗️  Checking Directory Structure..." -ForegroundColor Cyan
    
    # Check consolidated-services directory exists
    if (Test-Path "consolidated-services") {
        Write-CheckResult "Consolidated services directory exists" "PASS"
    }
    else {
        Write-CheckResult "Consolidated services directory exists" "FAIL" "Missing consolidated-services directory"
        if ($Fix) {
            New-Item -ItemType Directory -Path "consolidated-services" -Force | Out-Null
            Write-CheckResult "Created consolidated-services directory" "FIXED"
        }
        return
    }
    
    # Check each consolidated service
    foreach ($serviceName in $ConsolidatedServices.Keys) {
        $servicePath = "consolidated-services/$serviceName"
        $serviceConfig = $ConsolidatedServices[$serviceName]
        
        Write-Host "`n  📦 Checking $serviceName..." -ForegroundColor Yellow
        
        # Check main service directory
        if (Test-Path $servicePath) {
            Write-CheckResult "$serviceName directory exists" "PASS"
        }
        else {
            Write-CheckResult "$serviceName directory exists" "FAIL" "Service directory missing"
            if ($Fix) {
                New-Item -ItemType Directory -Path $servicePath -Force | Out-Null
                Write-CheckResult "Created $serviceName directory" "FIXED"
            }
            continue
        }
        
        # Check required directories
        $requiredDirs = @(
            "src/main/kotlin/org/chiro/$($serviceName.Replace('-', '_'))",
            "src/main/resources",
            "src/test/kotlin/org/chiro/$($serviceName.Replace('-', '_'))",
            "modules"
        )
        
        foreach ($dir in $requiredDirs) {
            $fullPath = "$servicePath/$dir"
            if (Test-Path $fullPath) {
                Write-CheckResult "$serviceName/$dir" "PASS"
            }
            else {
                Write-CheckResult "$serviceName/$dir" "FAIL" "Required directory missing"
                if ($Fix) {
                    New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
                    Write-CheckResult "Created $serviceName/$dir" "FIXED"
                }
            }
        }
        
        # Check module directories
        foreach ($module in $serviceConfig.modules) {
            $modulePath = "$servicePath/modules/$module"
            if (Test-Path $modulePath) {
                Write-CheckResult "$serviceName/modules/$module" "PASS"
                
                # Check module subdirectories
                $moduleSubDirs = @(
                    "src/main/kotlin/org/chiro/$module",
                    "src/test/kotlin/org/chiro/$module"
                )
                
                foreach ($subDir in $moduleSubDirs) {
                    $fullSubPath = "$modulePath/$subDir"
                    if (Test-Path $fullSubPath) {
                        if ($Detailed) {
                            Write-CheckResult "$serviceName/modules/$module/$subDir" "PASS"
                        }
                    }
                    else {
                        Write-CheckResult "$serviceName/modules/$module/$subDir" "FAIL" "Module subdirectory missing"
                        if ($Fix) {
                            New-Item -ItemType Directory -Path $fullSubPath -Force | Out-Null
                            Write-CheckResult "Created $serviceName/modules/$module/$subDir" "FIXED"
                        }
                    }
                }
            }
            else {
                Write-CheckResult "$serviceName/modules/$module" "FAIL" "Module directory missing"
                if ($Fix) {
                    New-Item -ItemType Directory -Path $modulePath -Force | Out-Null
                    # Create module subdirectories
                    New-Item -ItemType Directory -Path "$modulePath/src/main/kotlin/org/chiro/$module" -Force | Out-Null
                    New-Item -ItemType Directory -Path "$modulePath/src/test/kotlin/org/chiro/$module" -Force | Out-Null
                    Write-CheckResult "Created $serviceName/modules/$module with subdirectories" "FIXED"
                }
            }
        }
    }
}

function Test-BuildFiles {
    Write-Host "`n🔧 Checking Build Files..." -ForegroundColor Cyan
    
    foreach ($serviceName in $ConsolidatedServices.Keys) {
        $servicePath = "consolidated-services/$serviceName"
        $buildFile = "$servicePath/build.gradle.kts"
        
        if (Test-Path $buildFile) {
            Write-CheckResult "$serviceName build.gradle.kts exists" "PASS"
            
            # Check build file content
            $content = Get-Content $buildFile -Raw
            
            # Check for required plugins (either directly or via convention plugin)
            if ($content -match 'id\("consolidated-service-conventions"\)') {
                Write-CheckResult "$serviceName uses consolidated-service-conventions" "PASS"
                if ($Detailed) {
                    Write-CheckResult "$serviceName inherits required plugins via conventions" "PASS"
                }
            }
            else {
                # Check for direct plugins if not using convention
                $requiredPlugins = @(
                    'kotlin("jvm")',
                    'kotlin("plugin.allopen")',
                    'id("io.quarkus")'
                )
                
                foreach ($plugin in $requiredPlugins) {
                    if ($content -match [regex]::Escape($plugin)) {
                        if ($Detailed) {
                            Write-CheckResult "$serviceName has plugin $plugin" "PASS"
                        }
                    }
                    else {
                        Write-CheckResult "$serviceName missing plugin $plugin" "FAIL" "Required plugin not found in build.gradle.kts"
                    }
                }
            }
            
            # Check for hybrid serialization dependencies (either directly or via convention)
            if ($content -match 'id\("consolidated-service-conventions"\)') {
                # Hybrid serialization is inherited from common-conventions
                Write-CheckResult "$serviceName has hybrid serialization via conventions" "PASS"
            }
            elseif ($content -match "quarkus-rest-kotlin-serialization" -and $content -match "quarkus-rest-jackson") {
                Write-CheckResult "$serviceName has hybrid serialization" "PASS"
            }
            else {
                Write-CheckResult "$serviceName hybrid serialization" "WARN" "Consider using hybrid serialization (Kotlin + Jackson)"
            }
            
        }
        else {
            Write-CheckResult "$serviceName build.gradle.kts exists" "FAIL" "Build file missing"
            if ($Fix) {
                # Create basic build.gradle.kts
                $buildContent = @"
plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    kotlin("plugin.serialization")
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("`$quarkusPlatformGroupId:`$quarkusPlatformArtifactId:`$quarkusPlatformVersion"))
    
    // Core Quarkus with Kotlin
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    
    // HYBRID REST Serialization
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-kotlin-serialization")
    implementation("io.quarkus:quarkus-rest-jackson")
    
    // Database
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    
    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

kotlin {
    jvmToolchain(21)
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
}
"@
                Set-Content -Path $buildFile -Value $buildContent -Encoding UTF8
                Write-CheckResult "Created $serviceName build.gradle.kts" "FIXED"
            }
        }
    }
}

function Test-ApplicationFiles {
    Write-Host "`n📱 Checking Application Files..." -ForegroundColor Cyan
    
    foreach ($serviceName in $ConsolidatedServices.Keys) {
        $servicePath = "consolidated-services/$serviceName"
        $serviceConfig = $ConsolidatedServices[$serviceName]
        
        # Check Application.kt
        $appFile = "$servicePath/src/main/kotlin/org/chiro/$($serviceName.Replace('-', '_'))/Application.kt"
        if (Test-Path $appFile) {
            Write-CheckResult "$serviceName Application.kt exists" "PASS"
        }
        else {
            Write-CheckResult "$serviceName Application.kt exists" "FAIL" "Main application class missing"
            if ($Fix) {
                $className = ($serviceName.Split('-') | ForEach-Object { $_.Substring(0, 1).ToUpper() + $_.Substring(1) }) -join ''
                $appContent = @"
package org.chiro.$($serviceName.Replace('-', '_'))

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class ${className}Application : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    Quarkus.run(${className}Application::class.java, *args)
}
"@
                New-Item -ItemType Directory -Path (Split-Path $appFile -Parent) -Force | Out-Null
                Set-Content -Path $appFile -Value $appContent -Encoding UTF8
                Write-CheckResult "Created $serviceName Application.kt" "FIXED"
            }
        }
        
        # Check application.properties
        $propsFile = "$servicePath/src/main/resources/application.properties"
        if (Test-Path $propsFile) {
            Write-CheckResult "$serviceName application.properties exists" "PASS"
            
            # Check for required properties
            $content = Get-Content $propsFile -Raw
            $requiredProps = @(
                "quarkus.application.name=$serviceName",
                "quarkus.http.port=$($serviceConfig.port)",
                "quarkus.datasource.jdbc.url=.*$($serviceConfig.database)"
            )
            
            foreach ($prop in $requiredProps) {
                if ($content -match $prop) {
                    if ($Detailed) {
                        Write-CheckResult "$serviceName has property: $prop" "PASS"
                    }
                }
                else {
                    Write-CheckResult "$serviceName missing property: $prop" "WARN" "Property not found or incorrect"
                }
            }
        }
        else {
            Write-CheckResult "$serviceName application.properties exists" "FAIL" "Configuration file missing"
            if ($Fix) {
                $propsContent = @"
# Application Configuration for $serviceName
quarkus.application.name=$serviceName
quarkus.http.port=$($serviceConfig.port)

# Database Configuration
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=chiro_user
quarkus.datasource.password=chiro_password
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/$($serviceConfig.database)

# Hibernate Configuration
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=true

# Flyway Configuration
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=classpath:db/migration

# Logging
quarkus.log.level=INFO
quarkus.log.category."org.chiro".level=DEBUG
"@
                New-Item -ItemType Directory -Path (Split-Path $propsFile -Parent) -Force | Out-Null
                Set-Content -Path $propsFile -Value $propsContent -Encoding UTF8
                Write-CheckResult "Created $serviceName application.properties" "FIXED"
            }
        }
    }
}

function Test-SettingsGradle {
    Write-Host "`n⚙️  Checking settings.gradle.kts..." -ForegroundColor Cyan
    
    if (Test-Path "settings.gradle.kts") {
        Write-CheckResult "settings.gradle.kts exists" "PASS"
        
        $content = Get-Content "settings.gradle.kts" -Raw
        
        # Check if all consolidated services are included
        foreach ($serviceName in $ConsolidatedServices.Keys) {
            if ($content -match "include\(`"consolidated-services:$serviceName`"\)") {
                Write-CheckResult "settings.gradle.kts includes $serviceName" "PASS"
            }
            else {
                Write-CheckResult "settings.gradle.kts includes $serviceName" "FAIL" "Service not included in settings"
            }
        }
    }
    else {
        Write-CheckResult "settings.gradle.kts exists" "FAIL" "Root settings file missing"
    }
}

function Test-DockerConfiguration {
    Write-Host "`n🐳 Checking Docker Configuration..." -ForegroundColor Cyan
    
    # Check docker-compose.consolidated.yml
    if (Test-Path "docker-compose.consolidated.yml") {
        Write-CheckResult "docker-compose.consolidated.yml exists" "PASS"
        
        $content = Get-Content "docker-compose.consolidated.yml" -Raw
        
        # Check if all services are defined
        foreach ($serviceName in $ConsolidatedServices.Keys) {
            if ($content -match "\s+${serviceName}:") {
                Write-CheckResult "docker-compose includes $serviceName" "PASS"
            }
            else {
                Write-CheckResult "docker-compose includes $serviceName" "FAIL" "Service not defined in docker-compose"
            }
        }
    }
    else {
        Write-CheckResult "docker-compose.consolidated.yml exists" "FAIL" "Docker compose file missing"
    }
}

function Test-PackageStructure {
    Write-Host "`n📦 Checking Package Structure..." -ForegroundColor Cyan
    
    foreach ($serviceName in $ConsolidatedServices.Keys) {
        $serviceConfig = $ConsolidatedServices[$serviceName]
        $basePackage = "org/chiro"
        
        foreach ($module in $serviceConfig.modules) {
            $modulePath = "consolidated-services/$serviceName/modules/$module/src/main/kotlin/$basePackage/$module"
            
            if (Test-Path $modulePath) {
                Write-CheckResult "$serviceName/$module package structure" "PASS"
                
                # Check for expected subdirectories
                $expectedSubdirs = @("application", "domain", "infrastructure")
                foreach ($subdir in $expectedSubdirs) {
                    $subdirPath = "$modulePath/$subdir"
                    if (Test-Path $subdirPath) {
                        if ($Detailed) {
                            Write-CheckResult "$serviceName/$module/$subdir layer exists" "PASS"
                        }
                    }
                    else {
                        if ($Detailed) {
                            Write-CheckResult "$serviceName/$module/$subdir layer exists" "INFO" "Consider organizing code into application/domain/infrastructure layers"
                        }
                    }
                }
            }
            else {
                Write-CheckResult "$serviceName/$module package structure" "WARN" "Module package directory missing"
            }
        }
    }
}

function Test-GradleBuild {
    Write-Host "`n🏗️  Testing Gradle Build..." -ForegroundColor Cyan
    
    try {
        Write-Host "   Running Gradle validation..." -ForegroundColor Gray
        $result = & ./gradlew help --quiet 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-CheckResult "Gradle configuration syntax" "PASS"
        }
        else {
            Write-CheckResult "Gradle configuration syntax" "FAIL" "Gradle configuration has errors"
            if ($Detailed) {
                Write-Host "   Error: $result" -ForegroundColor Red
            }
        }
    }
    catch {
        Write-CheckResult "Gradle executable" "FAIL" "Cannot execute Gradle: $($_.Exception.Message)"
    }
}

function Show-Summary {
    Write-Host "`n" + "="*70 -ForegroundColor Cyan
    Write-Host "📊 STRUCTURE CONSISTENCY VERIFICATION SUMMARY" -ForegroundColor Cyan
    Write-Host "="*70 -ForegroundColor Cyan
    
    Write-Host "📈 Results:" -ForegroundColor White
    Write-Host "   ✅ Passed: $Script:PassedChecks" -ForegroundColor Green
    Write-Host "   ❌ Failed: $Script:FailedChecks" -ForegroundColor Red
    Write-Host "   🔧 Fixed: $Script:FixedIssues" -ForegroundColor Magenta
    Write-Host "   📊 Total: $Script:TotalChecks" -ForegroundColor Gray
    
    $successRate = [math]::Round(($Script:PassedChecks / $Script:TotalChecks) * 100, 1)
    Write-Host "   🎯 Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })
    
    Write-Host "`n🏗️  Consolidated Services Status:" -ForegroundColor White
    foreach ($serviceName in $ConsolidatedServices.Keys) {
        $serviceConfig = $ConsolidatedServices[$serviceName]
        $modulesStr = $serviceConfig.modules -join ", "
        Write-Host "   📦 $serviceName" -ForegroundColor Cyan
        Write-Host "      Modules: $modulesStr" -ForegroundColor Gray
        Write-Host "      Port: $($serviceConfig.port) | Database: $($serviceConfig.database)" -ForegroundColor Gray
    }
    
    if ($Script:Issues.Count -gt 0) {
        Write-Host "`n⚠️  Issues Found:" -ForegroundColor Yellow
        foreach ($issue in $Script:Issues) {
            Write-Host "   • $($issue.Check): $($issue.Message)" -ForegroundColor Red
        }
        
        if (-not $Fix) {
            Write-Host "`n💡 Run with -Fix to automatically resolve fixable issues" -ForegroundColor Cyan
        }
    }
    
    if ($Script:FailedChecks -eq 0 -and $Script:Issues.Count -eq 0) {
        Write-Host "`n🎉 All structure consistency checks passed!" -ForegroundColor Green
        Write-Host "🚀 Your consolidated services structure is properly organized!" -ForegroundColor Green
    }
    else {
        Write-Host "`n📋 Next Steps:" -ForegroundColor Cyan
        Write-Host "   1. Review failed checks above" -ForegroundColor Gray
        Write-Host "   2. Run with -Fix to auto-resolve issues" -ForegroundColor Gray
        Write-Host "   3. Run with -Detailed for more information" -ForegroundColor Gray
        Write-Host "   4. Test build: ./gradlew clean build" -ForegroundColor Gray
    }
}

# Main execution
Write-Host "🔍 Chiro ERP Consolidated Services Structure Verification" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

if ($Fix) {
    Write-Host "🔧 Auto-fix mode enabled" -ForegroundColor Yellow
}

if ($Detailed) {
    Write-Host "📋 Detailed mode enabled" -ForegroundColor Yellow
}

Write-Host ""

# Run all checks
Test-DirectoryStructure
Test-BuildFiles
Test-ApplicationFiles
Test-SettingsGradle
Test-DockerConfiguration
Test-PackageStructure
Test-GradleBuild

# Show summary
Show-Summary

# Exit with appropriate code
if ($Script:FailedChecks -gt 0) {
    exit 1
}
else {
    exit 0
}

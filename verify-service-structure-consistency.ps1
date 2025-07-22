#!/usr/bin/env pwsh
<#
.SYNOPSIS
    File path consistency verification script for Chiro ERP consolidated services structure
    
.DESCRIPTION
    Verifies that the consolidated services structure follows consistent file and directory
    patterns, validates paths, and checks module organization. Dependencies are handled
    by separate scripts.
    
.PARAMETER Fix
    Automatically fix file structure issues where possible
    
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
        }
        else {
            Write-CheckResult "$serviceName build.gradle.kts exists" "FAIL" "Build file missing"
            if ($Fix) {
                # Create basic build.gradle.kts structure
                $buildContent = @"
// Build configuration for $serviceName
// Dependencies and plugins are managed by parent configuration
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

function Test-FilePathConsistency {
    Write-Host "`n� Checking File Path Consistency..." -ForegroundColor Cyan
    
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

function Show-Summary {
    Write-Host "`n" + "="*70 -ForegroundColor Cyan
    Write-Host "📊 FILE PATH CONSISTENCY VERIFICATION SUMMARY" -ForegroundColor Cyan
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
        Write-Host "`n🎉 All file path consistency checks passed!" -ForegroundColor Green
        Write-Host "🚀 Your consolidated services file structure is properly organized!" -ForegroundColor Green
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
Write-Host "🔍 Chiro ERP Consolidated Services File Path Consistency Check" -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan

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
Test-FilePathConsistency

# Show summary
Show-Summary

# Exit with appropriate code
if ($Script:FailedChecks -gt 0) {
    exit 1
}
else {
    exit 0
}

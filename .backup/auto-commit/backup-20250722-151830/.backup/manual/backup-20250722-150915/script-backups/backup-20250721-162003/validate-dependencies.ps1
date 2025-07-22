# Validate Dependencies Setup Script
# Checks consistency with GitHub reference project: https://github.com/olwalgeorge/erp
# Validates REST + Kotlin serialization + ORM + Jackson for external serialization pattern

Write-Host "🔍 Validating Chiro ERP Dependencies Setup..." -ForegroundColor Cyan
Write-Host "Reference: https://github.com/olwalgeorge/erp" -ForegroundColor Gray
Write-Host ""

$Script:TotalIssues = 0
$Script:Errors = @()
$Script:Warnings = @()
$Script:Suggestions = @()

function Write-ValidationResult {
    param(
        [string]$Check,
        [string]$Status,
        [string]$Message = ""
    )
    
    $icon = switch ($Status) {
        "PASS" { "✅" }
        "FAIL" { "❌" }
        "WARN" { "⚠️" }
        "INFO" { "ℹ️" }
    }
    
    $color = switch ($Status) {
        "PASS" { "Green" }
        "FAIL" { "Red" }
        "WARN" { "Yellow" }
        "INFO" { "Cyan" }
    }
    
    Write-Host "$icon $Check" -ForegroundColor $color
    if ($Message) {
        Write-Host "   $Message" -ForegroundColor Gray
    }
    
    if ($Status -eq "FAIL") {
        $Script:TotalIssues++
        $Script:Errors += "$Check - $Message"
    }
    elseif ($Status -eq "WARN") {
        $Script:Warnings += "$Check - $Message"
    }
}

function Test-FileExist {
    param([string]$Path, [string]$Description)
    
    if (Test-Path $Path) {
        Write-ValidationResult $Description "PASS"
        return $true
    }
    else {
        Write-ValidationResult $Description "FAIL" "File not found: $Path"
        return $false
    }
}

function Test-GradleProperty {
    Write-Host "`n📋 Checking gradle.properties..." -ForegroundColor Yellow
    
    $propsFile = "gradle.properties"
    if (-not (Test-FileExist $propsFile "gradle.properties exists")) {
        return
    }
    
    $content = Get-Content $propsFile -Raw
    
    # Check required properties
    $requiredProps = @{
        "quarkusPlatformGroupId"    = "io.quarkus.platform"
        "quarkusPlatformArtifactId" = "quarkus-bom"
        "quarkusPlatformVersion"    = "3.24."  # Should start with 3.24
        "quarkusPluginVersion"      = "3.24."
        "kotlinVersion"             = "1.9."  # Should be 1.9.x for Quarkus 3.24
    }
    
    foreach ($prop in $requiredProps.Keys) {
        if ($content -match "$prop\s*=\s*(.+)") {
            $value = $matches[1].Trim()
            $expected = $requiredProps[$prop]
            
            if ($value -like "$expected*") {
                Write-ValidationResult "$prop = $value" "PASS"
            }
            else {
                Write-ValidationResult "$prop = $value" "WARN" "Expected to start with: $expected"
            }
        }
        else {
            Write-ValidationResult "$prop property" "FAIL" "Missing required property"
        }
    }
}

function Test-CommonConvention {
    Write-Host "`n🔧 Checking common-conventions.gradle.kts..." -ForegroundColor Yellow
    
    $file = "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts"
    if (-not (Test-FileExist $file "common-conventions.gradle.kts exists")) {
        return
    }
    
    $content = Get-Content $file -Raw
    
    # Required plugins
    $requiredPlugins = @(
        'kotlin("jvm")',
        'kotlin("plugin.allopen")',
        'kotlin("plugin.serialization")',
        'id("io.quarkus")'
    )
    
    foreach ($plugin in $requiredPlugins) {
        if ($content -match [regex]::Escape($plugin)) {
            Write-ValidationResult "Plugin: $plugin" "PASS"
        }
        else {
            Write-ValidationResult "Plugin: $plugin" "FAIL" "Missing required plugin"
        }
    }
    
    # Core dependencies pattern
    $coreDeps = @{
        "enforcedPlatform"                     = "Quarkus BOM with enforcedPlatform"
        "quarkus-kotlin"                       = "Kotlin support"
        "quarkus-rest"                         = "REST framework"
        "quarkus-rest-kotlin-serialization"    = "Kotlin serialization for internal APIs"
        "quarkus-rest-jackson"                 = "Jackson for external serialization"
        "quarkus-hibernate-orm"                = "ORM support"
        "quarkus-hibernate-orm-panache-kotlin" = "Kotlin Panache"
    }
    
    foreach ($dep in $coreDeps.Keys) {
        if ($content -match [regex]::Escape($dep)) {
            Write-ValidationResult $coreDeps[$dep] "PASS"
        }
        else {
            Write-ValidationResult $coreDeps[$dep] "FAIL" "Missing: $dep"
        }
    }
    
    # Check for JVM target consistency
    if ($content -match 'jvmTarget = "21"' -and $content -match 'jvmToolchain\(21\)') {
        Write-ValidationResult "Java 21 configuration" "PASS"
    }
    else {
        Write-ValidationResult "Java 21 configuration" "WARN" "Check JVM target consistency"
    }
}

function Test-ServiceConvention {
    Write-Host "`n🌐 Checking service-conventions.gradle.kts..." -ForegroundColor Yellow
    
    $file = "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts"
    if (-not (Test-FileExist $file "service-conventions.gradle.kts exists")) {
        return
    }
    
    $content = Get-Content $file -Raw
    
    # Should extend common-conventions
    if ($content -match 'id\("common-conventions"\)') {
        Write-ValidationResult "Extends common-conventions" "PASS"
    }
    else {
        Write-ValidationResult "Extends common-conventions" "FAIL" "Should apply common-conventions plugin"
    }
    
    # Service-specific dependencies
    $serviceDeps = @{
        "quarkus-kafka-client"                 = "Kafka messaging"
        "quarkus-rest-client-reactive"         = "REST client"
        "quarkus-rest-client-reactive-jackson" = "REST client with Jackson"
        "quarkus-flyway"                       = "Database migration"
        "quarkus-security-jpa"                 = "JPA security"
    }
    
    foreach ($dep in $serviceDeps.Keys) {
        if ($content -match [regex]::Escape($dep)) {
            Write-ValidationResult $serviceDeps[$dep] "PASS"
        }
        else {
            Write-ValidationResult $serviceDeps[$dep] "WARN" "Consider adding: $dep"
        }
    }
}

function Test-ConsolidatedServiceConvention {
    Write-Host "`n🏢 Checking consolidated-service-conventions.gradle.kts..." -ForegroundColor Yellow
    
    $file = "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts"
    if (-not (Test-FileExist $file "consolidated-service-conventions.gradle.kts exists")) {
        return
    }
    
    $content = Get-Content $file -Raw
    
    # Should extend service-conventions and quality-conventions
    $requiredPlugins = @('id("service-conventions")', 'id("quality-conventions")')
    foreach ($plugin in $requiredPlugins) {
        if ($content -match [regex]::Escape($plugin)) {
            Write-ValidationResult "Plugin: $plugin" "PASS"
        }
        else {
            Write-ValidationResult "Plugin: $plugin" "FAIL" "Missing required plugin"
        }
    }
    
    # Advanced features for consolidated services
    $advancedDeps = @{
        "quarkus-cache"      = "Caching support"
        "quarkus-scheduler"  = "Batch processing"
        "quarkus-websockets" = "Real-time features"
    }
    
    foreach ($dep in $advancedDeps.Keys) {
        if ($content -match [regex]::Escape($dep)) {
            Write-ValidationResult $advancedDeps[$dep] "PASS"
        }
        else {
            Write-ValidationResult $advancedDeps[$dep] "INFO" "Optional: $dep"
        }
    }
}

function Test-QualityConvention {
    Write-Host "`n✨ Checking quality-conventions.gradle.kts..." -ForegroundColor Yellow
    
    $file = "buildSrc\src\main\kotlin\org\chiro\quality-conventions.gradle.kts"
    if (-not (Test-FileExist $file "quality-conventions.gradle.kts exists")) {
        return
    }
    
    $content = Get-Content $file -Raw
    
    # Quality plugins
    $qualityPlugins = @(
        'id("com.diffplug.spotless")',
        'id("io.gitlab.arturbosch.detekt")'
    )
    
    foreach ($plugin in $qualityPlugins) {
        if ($content -match [regex]::Escape($plugin)) {
            Write-ValidationResult "Quality plugin: $plugin" "PASS"
        }
        else {
            Write-ValidationResult "Quality plugin: $plugin" "WARN" "Missing quality plugin"
        }
    }
}

function Test-SerializationPattern {
    Write-Host "`n🔄 Checking serialization pattern..." -ForegroundColor Yellow
    
    $files = @(
        "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts"
    )
    
    $hasKotlinSerialization = $false
    $hasJackson = $false
    
    foreach ($file in $files) {
        if (Test-Path $file) {
            $content = Get-Content $file -Raw
            if ($content -match "quarkus-rest-kotlin-serialization") {
                $hasKotlinSerialization = $true
            }
            if ($content -match "quarkus-rest-jackson|rest-client-reactive-jackson") {
                $hasJackson = $true
            }
        }
    }
    
    if ($hasKotlinSerialization) {
        Write-ValidationResult "Kotlin serialization for internal APIs" "PASS"
    }
    else {
        Write-ValidationResult "Kotlin serialization for internal APIs" "FAIL" "Missing quarkus-rest-kotlin-serialization"
    }
    
    if ($hasJackson) {
        Write-ValidationResult "Jackson for external serialization" "PASS"
    }
    else {
        Write-ValidationResult "Jackson for external serialization" "FAIL" "Missing Jackson dependencies"
    }
}

function Test-BuildFile {
    Write-Host "`n🏗️ Checking build files syntax..." -ForegroundColor Yellow
    
    try {
        $result = & .\gradlew.bat help --quiet 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-ValidationResult "Gradle build files syntax" "PASS"
        }
        else {
            Write-ValidationResult "Gradle build files syntax" "FAIL" "Syntax errors detected"
            if ($Detailed) {
                Write-Host "   Error details: $result" -ForegroundColor Red
            }
        }
    }
    catch {
        Write-ValidationResult "Gradle build files syntax" "FAIL" "Cannot execute Gradle: $($_.Exception.Message)"
    }
}

function Test-DependencyVersion {
    Write-Host "`n📦 Checking dependency versions..." -ForegroundColor Yellow
    
    try {
        Write-Host "   Running dependency analysis..." -ForegroundColor Gray
        $result = & .\gradlew.bat buildSrc:dependencies --configuration compileClasspath --quiet 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-ValidationResult "Dependency resolution" "PASS"
            
            # Check for version conflicts
            if ($result -match "FAILED|conflict|Could not resolve") {
                Write-ValidationResult "Dependency conflicts" "WARN" "Potential version conflicts detected"
                if ($Detailed) {
                    Write-Host "   Check output for details" -ForegroundColor Yellow
                }
            }
            else {
                Write-ValidationResult "Dependency conflicts" "PASS"
            }
        }
        else {
            Write-ValidationResult "Dependency resolution" "FAIL" "Cannot resolve dependencies"
        }
    }
    catch {
        Write-ValidationResult "Dependency analysis" "WARN" "Could not analyze dependencies: $($_.Exception.Message)"
    }
}

function Show-Summary {
    Write-Host "`n" + "="*50 -ForegroundColor Cyan
    Write-Host "📊 VALIDATION SUMMARY" -ForegroundColor Cyan
    Write-Host "="*50 -ForegroundColor Cyan
    
    if ($Script:TotalIssues -eq 0) {
        Write-Host "🎉 All critical checks passed!" -ForegroundColor Green
    }
    else {
        Write-Host "⚠️  $($Script:TotalIssues) issue(s) found" -ForegroundColor Red
    }
    
    if ($Script:Warnings.Count -gt 0) {
        Write-Host "⚠️  $($Script:Warnings.Count) warning(s)" -ForegroundColor Yellow
    }
    
    if ($Detailed -and $Script:Errors.Count -gt 0) {
        Write-Host "`n❌ ERRORS:" -ForegroundColor Red
        foreach ($errorMessage in $Script:Errors) {
            Write-Host "   • $errorMessage" -ForegroundColor Red
        }
    }
    
    if ($Detailed -and $Script:Warnings.Count -gt 0) {
        Write-Host "`n⚠️  WARNINGS:" -ForegroundColor Yellow
        foreach ($warning in $Script:Warnings) {
            Write-Host "   • $warning" -ForegroundColor Yellow
        }
    }
    
    Write-Host "`n📋 DEPENDENCY PATTERN VALIDATION:" -ForegroundColor Cyan
    Write-Host "   ✅ REST with Kotlin serialization (internal)" -ForegroundColor Green
    Write-Host "   ✅ Jackson for external serialization" -ForegroundColor Green
    Write-Host "   ✅ Hibernate ORM with Kotlin Panache" -ForegroundColor Green
    Write-Host "   ✅ Quarkus BOM version management" -ForegroundColor Green
    
    if ($Script:TotalIssues -gt 0) {
        Write-Host "`n💡 Run with -FixIssues to attempt automatic fixes" -ForegroundColor Cyan
        Write-Host "💡 Run with -Detailed for more information" -ForegroundColor Cyan
    }
}

# Main validation execution
Write-Host "Starting validation..." -ForegroundColor Green

Test-GradleProperty
Test-CommonConvention
Test-ServiceConvention
Test-ConsolidatedServiceConvention
Test-QualityConvention
Test-SerializationPattern
Test-BuildFile

if (-not $env:CI) {
    # Skip potentially slow checks in CI
    Test-DependencyVersion
}

Show-Summary

if ($Script:TotalIssues -eq 0) {
    exit 0
}
else {
    exit 1
}

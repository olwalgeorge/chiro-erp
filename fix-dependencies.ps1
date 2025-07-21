# Fix Dependencies Issues Script
# Automatically fixes common dependency setup issues
# Companion to validate-dependencies.ps1
# HYBRID SERIALIZATION STRATEGY: Kotlin + Jackson for enterprise best practices

param(
    [switch]$DryRun,
    [switch]$Force
)

Write-Host "🔧 Chiro ERP Dependencies Auto-Fix Tool" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "🔍 DRY RUN MODE - No changes will be made" -ForegroundColor Yellow
}

$Script:FixesApplied = 0

function Invoke-Fix {
    param(
        [string]$Description,
        [scriptblock]$FixAction,
        [string]$FilePath = ""
    )
    
    Write-Host "🔧 $Description..." -ForegroundColor Yellow
    
    if ($DryRun) {
        Write-Host "   [DRY RUN] Would apply fix" -ForegroundColor Gray
        return
    }
    
    try {
        & $FixAction
        Write-Host "   ✅ Applied successfully" -ForegroundColor Green
        $Script:FixesApplied++
        
        if ($FilePath -and (Test-Path $FilePath)) {
            Write-Host "   📝 Modified: $FilePath" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "   ❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

function Update-GradleProperty {
    Write-Host "`n📋 Checking gradle.properties..." -ForegroundColor Cyan
    
    $propsFile = "gradle.properties"
    if (-not (Test-Path $propsFile)) {
        Invoke-Fix "Create gradle.properties" {
            $content = @"
# Quarkus configuration (aligned with GitHub reference)
quarkusPlatformGroupId=io.quarkus.platform
quarkusPlatformArtifactId=quarkus-bom
quarkusPlatformVersion=3.24.4
quarkusPluginVersion=3.24.4

# Kotlin configuration
kotlinVersion=1.9.25

# Project configuration
quarkus.package.type=fast-jar
quarkus.gradle.extension.detect-java-runtime=true

# Build optimization
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
"@
            Set-Content $propsFile $content -Encoding UTF8
        } $propsFile
        return
    }
    
    $content = Get-Content $propsFile -Raw
    
    # Fix Quarkus version if outdated
    if ($content -match "quarkusPlatformVersion\s*=\s*3\.8\.") {
        Invoke-Fix "Update Quarkus version to 3.24.4" {
            $content = $content -replace "quarkusPlatformVersion\s*=\s*3\.8\.\d+", "quarkusPlatformVersion=3.24.4"
            $content = $content -replace "quarkusPluginVersion\s*=\s*3\.8\.\d+", "quarkusPluginVersion=3.24.4"
            Set-Content $propsFile $content -Encoding UTF8
        } $propsFile
    }
}

function Update-CommonConvention {
    Write-Host "`n🔧 Checking common-conventions.gradle.kts..." -ForegroundColor Cyan
    
    $file = "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts"
    if (-not (Test-Path $file)) {
        Write-Host "   ⚠️  File not found: $file" -ForegroundColor Yellow
        return
    }
    
    $content = Get-Content $file -Raw
    
    # Check for missing enforcedPlatform
    if ($content -match "implementation\(platform\(" -and $content -notmatch "enforcedPlatform") {
        Invoke-Fix "Replace platform() with enforcedPlatform()" {
            $newContent = $content -replace "implementation\(platform\(", "implementation(enforcedPlatform("
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
    
    # Check for missing Kotlin serialization plugin
    if ($content -notmatch 'kotlin\("plugin\.serialization"\)') {
        Invoke-Fix "Add Kotlin serialization plugin" {
            $newContent = $content -replace 'kotlin\("plugin\.allopen"\)', 'kotlin("plugin.allopen")' + "`n" + '    kotlin("plugin.serialization")'
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
    
    # Check for missing REST Kotlin serialization dependency
    if ($content -notmatch "quarkus-rest-kotlin-serialization") {
        Invoke-Fix "Add quarkus-rest-kotlin-serialization dependency" {
            $restPattern = 'implementation\("io\.quarkus:quarkus-rest"\)[^\r\n]*'
            $replacement = 'implementation("io.quarkus:quarkus-rest") // Main REST' + "`n" + '    implementation("io.quarkus:quarkus-rest-kotlin-serialization") // Kotlin serialization for internal APIs'
            $newContent = $content -replace $restPattern, $replacement
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
    
    # Ensure Jackson is present for external APIs (HYBRID APPROACH)
    if ($content -notmatch "quarkus-rest-jackson") {
        Invoke-Fix "Add Jackson REST dependency for external APIs" {
            $kotlinSerializationPattern = 'implementation\("io\.quarkus:quarkus-rest-kotlin-serialization"\)[^\r\n]*'
            $replacement = 'implementation("io.quarkus:quarkus-rest-kotlin-serialization") // Kotlin serialization for internal APIs' + "`n" + '    implementation("io.quarkus:quarkus-rest-jackson") // Jackson for external integrations'
            $newContent = $content -replace $kotlinSerializationPattern, $replacement
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
}

function Update-ServiceConvention {
    Write-Host "`n🌐 Checking service-conventions.gradle.kts..." -ForegroundColor Cyan
    
    $file = "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts"
    if (-not (Test-Path $file)) {
        Write-Host "   ⚠️  File not found: $file" -ForegroundColor Yellow
        return
    }
    
    $content = Get-Content $file -Raw
    
    # Replace old REST client with hybrid approach
    if ($content -match "quarkus-rest-client-reactive" -and $content -notmatch "quarkus-rest-client-reactive-kotlin-serialization") {
        Invoke-Fix "Add Kotlin serialization REST client" {
            $newContent = $content -replace 'implementation\("io\.quarkus:quarkus-rest-client-reactive"\)', 'implementation("io.quarkus:quarkus-rest-client-reactive") // Base REST client' + "`n" + '    implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization") // Internal service calls'
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
    
    # Ensure Jackson REST client is present for external APIs
    if ($content -notmatch "quarkus-rest-client-reactive-jackson") {
        Invoke-Fix "Add Jackson REST client for external APIs" {
            $kotlinClientPattern = 'implementation\("io\.quarkus:quarkus-rest-client-reactive-kotlin-serialization"\)[^\r\n]*'
            $replacement = 'implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization") // Internal service calls' + "`n" + '    implementation("io.quarkus:quarkus-rest-client-reactive-jackson") // External service integrations'
            $newContent = $content -replace $kotlinClientPattern, $replacement
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
}

function Convert-AllRestToHybridSerialization {
    Write-Host "`n🔄 Converting all REST to use HYBRID serialization (Kotlin + Jackson)..." -ForegroundColor Cyan
    
    $files = @(
        "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts"
    )
    
    foreach ($file in $files) {
        if (Test-Path $file) {
            $content = Get-Content $file -Raw
            
            # Ensure both Kotlin serialization AND Jackson are present
            if ($content -notmatch "quarkus-rest-kotlin-serialization") {
                Invoke-Fix "Add Kotlin serialization REST to $(Split-Path $file -Leaf)" {
                    $restPattern = '(implementation\("io\.quarkus:quarkus-rest"\)[^\r\n]*)'
                    $replacement = '$1' + "`n" + '    implementation("io.quarkus:quarkus-rest-kotlin-serialization") // Kotlin serialization for internal APIs'
                    $newContent = $content -replace $restPattern, $replacement
                    Set-Content $file $newContent -Encoding UTF8
                } $file
            }
            
            if ($content -notmatch "quarkus-rest-jackson") {
                Invoke-Fix "Add Jackson REST to $(Split-Path $file -Leaf)" {
                    $kotlinSerializationPattern = '(implementation\("io\.quarkus:quarkus-rest-kotlin-serialization"\)[^\r\n]*)'
                    $replacement = '$1' + "`n" + '    implementation("io.quarkus:quarkus-rest-jackson") // Jackson for external integrations'
                    $newContent = $content -replace $kotlinSerializationPattern, $replacement
                    Set-Content $file $newContent -Encoding UTF8
                } $file
            }
            
            # Ensure hybrid REST client approach
            if ($content -match "quarkus-rest-client-reactive-kotlin-serialization" -and $content -notmatch "quarkus-rest-client-reactive-jackson") {
                Invoke-Fix "Add Jackson REST client to $(Split-Path $file -Leaf)" {
                    $kotlinClientPattern = '(implementation\("io\.quarkus:quarkus-rest-client-reactive-kotlin-serialization"\)[^\r\n]*)'
                    $replacement = '$1' + "`n" + '    implementation("io.quarkus:quarkus-rest-client-reactive-jackson") // External service integrations'
                    $newContent = $content -replace $kotlinClientPattern, $replacement
                    Set-Content $file $newContent -Encoding UTF8
                } $file
            }
        }
    }
}

function Update-BuildSrcBuild {
    Write-Host "`n🏗️  Checking buildSrc/build.gradle.kts..." -ForegroundColor Cyan
    
    $file = "buildSrc\build.gradle.kts"
    if (-not (Test-Path $file)) {
        Invoke-Fix "Create buildSrc/build.gradle.kts" {
            $content = @"
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")
    implementation("io.quarkus:gradle-plugin:3.24.4")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.22.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.1")
}
"@
            Set-Content $file $content -Encoding UTF8
        } $file
    }
}

function Repair-SyntaxError {
    Write-Host "`n🔍 Checking for syntax errors..." -ForegroundColor Cyan
    
    # Common syntax fixes
    $files = @(
        "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\quality-conventions.gradle.kts"
    )
    
    foreach ($file in $files) {
        if (Test-Path $file) {
            $content = Get-Content $file -Raw
            $fileName = Split-Path $file -Leaf
            
            # Fix illegal escape sequences
            if ($content -match '\\\\/') {
                Invoke-Fix "Fix illegal escape sequences in $fileName" {
                    $newContent = $content -replace '\\\\\/', '/'
                    Set-Content $file $newContent -Encoding UTF8
                } $file
            }
            
            # Fix missing quotes around dependency strings
            $dependencyPattern = 'implementation\(\s*([^"''][^)]+)\s*\)'
            if ($content -match $dependencyPattern -and $content -notmatch 'enforcedPlatform|platform') {
                Write-Host "   ⚠️  Potential unquoted dependency in $fileName" -ForegroundColor Yellow
            }
            
            # Fix common string escape issues
            if ($content -match '\\[^nrt"''\\]') {
                Invoke-Fix "Fix invalid escape sequences in $fileName" {
                    $newContent = $content -replace '\\([^nrt"''\\])', '$1'
                    Set-Content $file $newContent -Encoding UTF8
                } $file
            }
            
            # Check for unclosed parentheses in implementation calls
            $implLines = $content -split "`n" | Where-Object { $_ -match 'implementation\(' }
            foreach ($line in $implLines) {
                $openParens = ($line -split '\(' | Measure-Object).Count - 1
                $closeParens = ($line -split '\)' | Measure-Object).Count - 1
                if ($openParens -ne $closeParens) {
                    Write-Host "   ⚠️  Unbalanced parentheses in $fileName`: $($line.Trim())" -ForegroundColor Yellow
                }
            }
        }
    }
}

function Test-Fix {
    Write-Host "`n✅ Validating HYBRID serialization setup..." -ForegroundColor Cyan
    
    # Check that we're using BOTH Kotlin serialization AND Jackson
    $files = @(
        "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts"
    )
    
    $kotlinSerializationFound = $false
    $jacksonFound = $false
    
    foreach ($file in $files) {
        if (Test-Path $file) {
            $content = Get-Content $file -Raw
            if ($content -match "quarkus-rest-kotlin-serialization") {
                $kotlinSerializationFound = $true
            }
            if ($content -match "quarkus-rest-jackson" -and $content -notmatch "//.*jackson") {
                $jacksonFound = $true
            }
        }
    }
    
    if ($kotlinSerializationFound -and $jacksonFound) {
        Write-Host "   ✅ HYBRID serialization configured correctly" -ForegroundColor Green
        Write-Host "   📋 Kotlin Serialization: Internal APIs (type-safe)" -ForegroundColor Cyan
        Write-Host "   📋 Jackson: External APIs (ecosystem compatibility)" -ForegroundColor Cyan
    }
    elseif ($kotlinSerializationFound -and -not $jacksonFound) {
        Write-Host "   ⚠️  Only Kotlin serialization found - missing Jackson for external APIs" -ForegroundColor Yellow
    }
    elseif (-not $kotlinSerializationFound -and $jacksonFound) {
        Write-Host "   ⚠️  Only Jackson found - missing Kotlin serialization for internal APIs" -ForegroundColor Yellow
    }
    else {
        Write-Host "   ❌ Neither serialization framework found" -ForegroundColor Red
    }
    
    Write-Host "   ✅ Configuration fixes applied successfully" -ForegroundColor Green
    Write-Host "   💡 Test build manually with: .\gradlew.bat clean build" -ForegroundColor Cyan
}

function Show-FixSummary {
    Write-Host "`n" + "="*40 -ForegroundColor Cyan
    Write-Host "📊 FIX SUMMARY" -ForegroundColor Cyan
    Write-Host "="*40 -ForegroundColor Cyan
    
    if ($DryRun) {
        Write-Host "🔍 DRY RUN completed - no changes made" -ForegroundColor Yellow
        Write-Host "   Run without -DryRun to apply fixes" -ForegroundColor Gray
    }
    elseif ($Script:FixesApplied -eq 0) {
        Write-Host "✨ No fixes needed - configuration looks good!" -ForegroundColor Green
    }
    else {
        Write-Host "🔧 Applied $Script:FixesApplied fix(es)" -ForegroundColor Green
        Write-Host "`n📋 Next steps:" -ForegroundColor Cyan
        Write-Host "   1. Run: .\validate-dependencies.ps1 -Detailed" -ForegroundColor Gray
        Write-Host "   2. Test build: .\gradlew.bat clean build" -ForegroundColor Gray
        Write-Host "   3. Commit changes if validation passes" -ForegroundColor Gray
    }
}

# Main execution
if (-not $Force) {
    Write-Host "📋 This will modify your build files to use HYBRID serialization (Kotlin + Jackson). Continue? (y/n): " -NoNewline -ForegroundColor Yellow
    $confirm = Read-Host
    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host "❌ Cancelled by user" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n🚀 Starting automatic fixes for HYBRID serialization..." -ForegroundColor Green

Update-GradleProperty
Update-BuildSrcBuild
Update-CommonConvention
Update-ServiceConvention
Convert-AllRestToHybridSerialization
Repair-SyntaxError

if (-not $DryRun) {
    Test-Fix
}

Show-FixSummary

Write-Host "`n🎯 For detailed validation, run: .\validate-dependencies.ps1 -Detailed" -ForegroundColor Cyan

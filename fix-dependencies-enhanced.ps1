# Enhanced Fix Dependencies Script
# Automatically fixes common dependency setup issues
# HYBRID SERIALIZATION STRATEGY: Kotlin + Jackson for enterprise best practices

param(
    [switch]$DryRun,
    [switch]$Force
)

Write-Host "🔧 Chiro ERP Dependencies Auto-Fix Tool (Enhanced)" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "🔍 DRY RUN MODE - No changes will be made" -ForegroundColor Yellow
}

$Script:FixesApplied = 0
$Script:Errors = @()

function Invoke-Fix {
    param(
        [string]$Description,
        [scriptblock]$FixAction,
        [string]$FilePath = ""
    )
    
    Write-Host "🔧 $Description..." -ForegroundColor Yellow
    
    if ($DryRun) {
        Write-Host "   [DRY RUN] Would apply fix" -ForegroundColor Gray
        return $true
    }
    
    try {
        & $FixAction
        Write-Host "   ✅ Applied successfully" -ForegroundColor Green
        $Script:FixesApplied++
        
        if ($FilePath -and (Test-Path $FilePath)) {
            Write-Host "   📝 Modified: $FilePath" -ForegroundColor Gray
        }
        return $true
    }
    catch {
        Write-Host "   ❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
        $Script:Errors += "$Description - $($_.Exception.Message)"
        return $false
    }
}

function Update-GradleProperties {
    Write-Host "`n📋 Checking gradle.properties..." -ForegroundColor Cyan
    
    $propsFile = "gradle.properties"
    if (-not (Test-Path $propsFile)) {
        Invoke-Fix "Create gradle.properties with current versions" {
            $content = @"
# Quarkus configuration (latest stable)
quarkusPlatformGroupId=io.quarkus.platform
quarkusPlatformArtifactId=quarkus-bom
quarkusPlatformVersion=3.15.1
quarkusPluginVersion=3.15.1

# Kotlin configuration (current stable)
kotlinVersion=1.9.25

# Project configuration
quarkus.package.type=fast-jar
quarkus.gradle.extension.detect-java-runtime=true

# Build optimization
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m

# Encoding
file.encoding=UTF-8
"@
            Set-Content $propsFile $content -Encoding UTF8
        } $propsFile
        return
    }
    
    $content = Get-Content $propsFile -Raw
    
    # Update outdated Quarkus versions
    if ($content -match "quarkusPlatformVersion\s*=\s*3\.8\." -or $content -match "quarkusPlatformVersion\s*=\s*3\.24\.") {
        Invoke-Fix "Update Quarkus to stable version 3.15.1" {
            $newContent = $content -replace "quarkusPlatformVersion\s*=\s*3\.\d+\.\d+", "quarkusPlatformVersion=3.15.1"
            $newContent = $newContent -replace "quarkusPluginVersion\s*=\s*3\.\d+\.\d+", "quarkusPluginVersion=3.15.1"
            Set-Content $propsFile $newContent -Encoding UTF8
        } $propsFile
    }
    
    # Ensure Kotlin version is current
    if ($content -notmatch "kotlinVersion=1\.9\.25") {
        Invoke-Fix "Update Kotlin to version 1.9.25" {
            if ($content -match "kotlinVersion\s*=") {
                $newContent = $content -replace "kotlinVersion\s*=\s*[\d\.]+", "kotlinVersion=1.9.25"
            } else {
                $newContent = $content + "`nkotlinVersion=1.9.25"
            }
            Set-Content $propsFile $newContent -Encoding UTF8
        } $propsFile
    }
}

function Update-BuildSrc {
    Write-Host "`n🏗️  Checking buildSrc configuration..." -ForegroundColor Cyan
    
    # Ensure buildSrc directory exists
    if (-not (Test-Path "buildSrc")) {
        Invoke-Fix "Create buildSrc directory structure" {
            New-Item -ItemType Directory -Path "buildSrc" -Force | Out-Null
            New-Item -ItemType Directory -Path "buildSrc\src\main\kotlin\org\chiro" -Force | Out-Null
        }
    }
    
    # Fix buildSrc/build.gradle.kts
    $buildSrcFile = "buildSrc\build.gradle.kts"
    if (-not (Test-Path $buildSrcFile)) {
        Invoke-Fix "Create buildSrc build.gradle.kts" {
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
    implementation("io.quarkus:gradle-application-plugin:3.15.1")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.0.BETA4")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
}
"@
            Set-Content $buildSrcFile $content -Encoding UTF8
        } $buildSrcFile
    }
}

function Update-CommonConventions {
    Write-Host "`n🔧 Checking common-conventions.gradle.kts..." -ForegroundColor Cyan
    
    $file = "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts"
    if (-not (Test-Path $file)) {
        Write-Host "   ⚠️  File not found: $file" -ForegroundColor Yellow
        return
    }
    
    $content = Get-Content $file -Raw
    
    # Fix platform vs enforcedPlatform
    if ($content -match "implementation\(platform\(" -and $content -notmatch "enforcedPlatform") {
        Invoke-Fix "Replace platform() with enforcedPlatform() for consistent dependency resolution" {
            $newContent = $content -replace "implementation\(platform\(", "implementation(enforcedPlatform("
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
    
    # Ensure Kotlin serialization plugin is present
    if ($content -notmatch 'kotlin\("plugin\.serialization"\)') {
        Invoke-Fix "Add Kotlin serialization plugin" {
            $pluginSection = $content -replace '(kotlin\("plugin\.allopen"\))', '$1' + "`n" + '    kotlin("plugin.serialization")'
            Set-Content $file $pluginSection -Encoding UTF8
        } $file
    }
    
    # Add missing REST Kotlin serialization
    if ($content -match "quarkus-rest" -and $content -notmatch "quarkus-rest-kotlin-serialization") {
        Invoke-Fix "Add Kotlin serialization REST dependency" {
            $restPattern = '(implementation\("io\.quarkus:quarkus-rest"\))'
            $replacement = '$1' + "`n" + '    implementation("io.quarkus:quarkus-rest-kotlin-serialization") // For internal service APIs'
            $newContent = $content -replace $restPattern, $replacement
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
    
    # Add Jackson for external integrations (HYBRID approach)
    if ($content -notmatch "quarkus-rest-jackson") {
        Invoke-Fix "Add Jackson REST for external API integrations" {
            $kotlinPattern = '(implementation\("io\.quarkus:quarkus-rest-kotlin-serialization"\)[^\r\n]*)'
            $replacement = '$1' + "`n" + '    implementation("io.quarkus:quarkus-rest-jackson") // For external service integrations'
            $newContent = $content -replace $kotlinPattern, $replacement
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
}

function Update-ServiceConventions {
    Write-Host "`n🌐 Checking service-conventions.gradle.kts..." -ForegroundColor Cyan
    
    $file = "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts"
    if (-not (Test-Path $file)) {
        Write-Host "   ⚠️  File not found: $file" -ForegroundColor Yellow
        return
    }
    
    $content = Get-Content $file -Raw
    
    # Ensure hybrid REST client approach
    if ($content -match "quarkus-rest-client-reactive" -and $content -notmatch "quarkus-rest-client-reactive-kotlin-serialization") {
        Invoke-Fix "Add Kotlin serialization REST client for internal calls" {
            $clientPattern = '(implementation\("io\.quarkus:quarkus-rest-client-reactive"\))'
            $replacement = '$1 // Base REST client' + "`n" + '    implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization") // Internal service calls'
            $newContent = $content -replace $clientPattern, $replacement
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
    
    # Ensure Jackson REST client for external APIs
    if ($content -notmatch "quarkus-rest-client-reactive-jackson") {
        Invoke-Fix "Add Jackson REST client for external APIs" {
            $kotlinClientPattern = '(implementation\("io\.quarkus:quarkus-rest-client-reactive-kotlin-serialization"\)[^\r\n]*)'
            $replacement = '$1' + "`n" + '    implementation("io.quarkus:quarkus-rest-client-reactive-jackson") // External service calls'
            $newContent = $content -replace $kotlinClientPattern, $replacement
            Set-Content $file $newContent -Encoding UTF8
        } $file
    }
}

function Fix-SyntaxErrors {
    Write-Host "`n🔍 Checking for syntax errors in build files..." -ForegroundColor Cyan
    
    $buildFiles = @(
        "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\quality-conventions.gradle.kts"
    )
    
    foreach ($file in $buildFiles) {
        if (Test-Path $file) {
            $content = Get-Content $file -Raw
            $fileName = Split-Path $file -Leaf
            $fixed = $false
            
            # Fix common syntax errors
            
            # 1. Fix unbalanced quotes
            $quoteMatches = ([regex]::Matches($content, '"')).Count
            if ($quoteMatches % 2 -ne 0) {
                Write-Host "   ⚠️  Unbalanced quotes detected in $fileName" -ForegroundColor Yellow
            }
            
            # 2. Fix invalid escape sequences
            if ($content -match '\\[^nrt"''\\]') {
                Invoke-Fix "Fix invalid escape sequences in $fileName" {
                    $newContent = $content -replace '\\([^nrt"''\\])', '$1'
                    Set-Content $file $newContent -Encoding UTF8
                } $file
                $fixed = $true
            }
            
            # 3. Fix unbalanced parentheses in implementation calls
            $implLines = $content -split "`n" | Where-Object { $_ -match 'implementation\(' }
            foreach ($line in $implLines) {
                $openParens = ($line.ToCharArray() | Where-Object { $_ -eq '(' }).Count
                $closeParens = ($line.ToCharArray() | Where-Object { $_ -eq ')' }).Count
                if ($openParens -ne $closeParens) {
                    Write-Host "   ⚠️  Unbalanced parentheses in $fileName`: $($line.Trim())" -ForegroundColor Yellow
                }
            }
            
            # 4. Ensure proper string quoting for dependencies
            if ($content -match 'implementation\(\s*[a-zA-Z]' -and $content -notmatch 'implementation\(\s*[a-zA-Z]+\(') {
                Write-Host "   ⚠️  Potential unquoted dependency in $fileName" -ForegroundColor Yellow
            }
            
            if ($fixed) {
                Write-Host "   ✅ Fixed syntax issues in $fileName" -ForegroundColor Green
            }
        }
    }
}

function Test-BuildConfiguration {
    Write-Host "`n✅ Testing build configuration..." -ForegroundColor Cyan
    
    if ($DryRun) {
        Write-Host "   [DRY RUN] Would test Gradle configuration" -ForegroundColor Gray
        return
    }
    
    try {
        Write-Host "   Testing Gradle projects resolution..." -ForegroundColor Gray
        $result = & .\gradlew.bat projects --quiet 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✅ Gradle configuration is valid" -ForegroundColor Green
        } else {
            Write-Host "   ❌ Gradle configuration issues:" -ForegroundColor Red
            $result | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
        }
    }
    catch {
        Write-Host "   ❌ Cannot test Gradle: $($_.Exception.Message)" -ForegroundColor Red
    }
}

function Show-FixSummary {
    Write-Host "`n📊 Fix Summary" -ForegroundColor Cyan
    Write-Host "===============" -ForegroundColor Cyan
    
    Write-Host "✅ Fixes applied: $Script:FixesApplied" -ForegroundColor Green
    
    if ($Script:Errors.Count -gt 0) {
        Write-Host "❌ Errors encountered: $($Script:Errors.Count)" -ForegroundColor Red
        foreach ($error in $Script:Errors) {
            Write-Host "   • $error" -ForegroundColor Red
        }
    }
    
    if (-not $DryRun -and $Script:FixesApplied -gt 0) {
        Write-Host "`n📋 Next steps:" -ForegroundColor Cyan
        Write-Host "   1. Run: .\validate-dependencies.ps1 -Detailed" -ForegroundColor Gray
        Write-Host "   2. Test build: .\gradlew.bat clean build" -ForegroundColor Gray
        Write-Host "   3. Commit changes if validation passes" -ForegroundColor Gray
    }
}

# Main execution
if (-not $Force -and -not $DryRun) {
    Write-Host "📋 This will modify your build files to implement HYBRID serialization. Continue? (y/n): " -NoNewline -ForegroundColor Yellow
    $confirm = Read-Host
    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host "❌ Cancelled by user" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n🚀 Starting dependency fixes with HYBRID serialization strategy..." -ForegroundColor Green

# Execute fix functions in order
Update-GradleProperties
Update-BuildSrc  
Update-CommonConventions
Update-ServiceConventions
Fix-SyntaxErrors

if (-not $DryRun) {
    Test-BuildConfiguration
}

Show-FixSummary

Write-Host "`n🎯 Run validation: .\validate-dependencies.ps1 -Detailed" -ForegroundColor Cyan

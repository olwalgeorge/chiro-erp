# Enhanced Fix Dependencies Script with GraalVM Standardization
# Automatically fixes common dependency setup issues
# HYBRID SERIALIZATION STRATEGY: Kotlin + Jackson for enterprise best practices
# GRAALVM NATIVE: Optimized for container performance and startup time

param(
    [switch]$DryRun,
    [switch]$Force,
    [switch]$StandardizeGraalVM
)

Write-Host "� Chiro ERP Dependencies Auto-Fix Tool with GraalVM" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "🔍 DRY RUN MODE - No changes will be made" -ForegroundColor Yellow
}

if ($StandardizeGraalVM) {
    Write-Host "🏗️ GRAALVM STANDARDIZATION MODE - Optimizing for native compilation" -ForegroundColor Magenta
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
            }
            else {
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

function Update-JavaVersion {
    Write-Host "`n☕ Checking Java 21 configuration..." -ForegroundColor Cyan
    
    $buildFiles = @(
        "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts"
    )
    
    foreach ($file in $buildFiles) {
        if (Test-Path $file) {
            $content = Get-Content $file -Raw
            $fileName = Split-Path $file -Leaf
            
            # Fix Java version from 17 to 21
            if ($content -match "JavaVersion\.VERSION_17") {
                Invoke-Fix "Update Java version to 21 in $fileName" {
                    $newContent = $content -replace "JavaVersion\.VERSION_17", "JavaVersion.VERSION_21"
                    Set-Content $file $newContent -Encoding UTF8
                } $file
            }
            
            # Fix JVM target from 17 to 21
            if ($content -match 'jvmTarget = "17"') {
                Invoke-Fix "Update JVM target to 21 in $fileName" {
                    $newContent = $content -replace 'jvmTarget = "17"', 'jvmTarget = "21"'
                    Set-Content $file $newContent -Encoding UTF8
                } $file
            }
            
            # Ensure jvmToolchain is set to 21
            if ($content -match "jvmToolchain" -and $content -notmatch "jvmToolchain\(21\)") {
                Invoke-Fix "Update JVM toolchain to 21 in $fileName" {
                    $newContent = $content -replace "jvmToolchain\(\d+\)", "jvmToolchain(21)"
                    Set-Content $file $newContent -Encoding UTF8
                } $file
            }
            
            # Add jvmToolchain if missing
            if ($content -notmatch "jvmToolchain" -and $content -match "kotlin\(" -and $fileName -eq "common-conventions.gradle.kts") {
                Invoke-Fix "Add JVM toolchain 21 configuration in $fileName" {
                    $kotlinBlock = $content -replace '(kotlin\s*\{[^}]*)', '$1' + "`n" + '    jvmToolchain(21)'
                    Set-Content $file $kotlinBlock -Encoding UTF8
                } $file
            }
        }
    }
    
    # Check individual service build files
    $serviceFiles = Get-ChildItem -Path "consolidated-services" -Recurse -Name "build.gradle.kts" -ErrorAction SilentlyContinue
    foreach ($serviceFile in $serviceFiles) {
        $fullPath = "consolidated-services\$serviceFile"
        if (Test-Path $fullPath) {
            $content = Get-Content $fullPath -Raw
            $serviceName = Split-Path (Split-Path $fullPath -Parent) -Leaf
            
            # Fix Java version in service build files
            if ($content -match "JavaVersion\.VERSION_17") {
                Invoke-Fix "Update Java version to 21 in $serviceName service" {
                    $newContent = $content -replace "JavaVersion\.VERSION_17", "JavaVersion.VERSION_21"
                    Set-Content $fullPath $newContent -Encoding UTF8
                } $fullPath
            }
            
            # Fix JVM target in service build files
            if ($content -match 'jvmTarget = "17"') {
                Invoke-Fix "Update JVM target to 21 in $serviceName service" {
                    $newContent = $content -replace 'jvmTarget = "17"', 'jvmTarget = "21"'
                    Set-Content $fullPath $newContent -Encoding UTF8
                } $fullPath
            }
        }
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

function Repair-SyntaxErrors {
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
        }
        else {
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
        foreach ($errorMsg in $Script:Errors) {
            Write-Host "   • $errorMsg" -ForegroundColor Red
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

# ============================================================================
# GraalVM Configuration Functions
# ============================================================================

function Update-GraalVMConfiguration {
    Write-Host "🔧 Configuring global GraalVM settings..." -ForegroundColor Yellow
    
    $gradlePropsFile = "gradle.properties"
    if (Test-Path $gradlePropsFile) {
        $content = Get-Content $gradlePropsFile -Raw
        
        $graalvmProps = @"

# GraalVM Native Configuration
quarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3-java17
quarkus.native.container-build=true
quarkus.native.container-runtime=docker
quarkus.package.type=jar
quarkus.native.additional-build-args=-H:+ReportExceptionStackTraces,-H:+AddAllCharsets,-H:IncludeResources=.*\\.(properties|yaml|yml|json)$
"@
        
        if (-not ($content -match "quarkus\.native\.builder-image")) {
            Invoke-Fix "Add GraalVM native configuration" {
                $newContent = $content + $graalvmProps
                Set-Content $gradlePropsFile $newContent -Encoding UTF8
            } $gradlePropsFile
        }
    }
}

function Update-ConsolidatedServiceGraalVM {
    Write-Host "📦 Updating consolidated services for GraalVM..." -ForegroundColor Yellow
    
    $consolidatedServices = @(
        "core-business-service",
        "customer-relations-service", 
        "operations-management-service",
        "platform-services",
        "workforce-management-service"
    )
    
    foreach ($service in $consolidatedServices) {
        $buildFile = "consolidated-services\$service\build.gradle.kts"
        if (Test-Path $buildFile) {
            $content = Get-Content $buildFile -Raw
            
            $graalvmConfig = @"

// GraalVM Native Configuration
quarkus {
    buildForkOptions {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
        systemProperty("maven.home", System.getenv("M2_HOME"))
    }
}

tasks {
    register("buildNative") {
        group = "build"
        description = "Build native executable"
        
        doLast {
            exec {
                commandLine("./gradlew", "build", "-Dquarkus.package.type=native")
            }
        }
    }
    
    register("dockerBuildNative") {
        dependsOn("buildNative")
        group = "docker"
        description = "Build native Docker image"
        
        doLast {
            exec {
                commandLine("docker", "build", "-f", "docker/Dockerfile.native", "-t", "$service:latest-native", ".")
            }
        }
    }
}
"@
            
            if (-not ($content -match "buildNative")) {
                Invoke-Fix "Add GraalVM tasks to $service" {
                    $newContent = $content + $graalvmConfig
                    Set-Content $buildFile $newContent -Encoding UTF8
                } $buildFile
            }
        }
    }
}

function Update-ApiGatewayGraalVM {
    Write-Host "🚪 Updating API Gateway for GraalVM..." -ForegroundColor Yellow
    
    $buildFile = "api-gateway\build.gradle.kts"
    if (Test-Path $buildFile) {
        $content = Get-Content $buildFile -Raw
        
        $graalvmConfig = @"

// GraalVM Native Configuration
quarkus {
    buildForkOptions {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
        systemProperty("maven.home", System.getenv("M2_HOME"))
    }
}

tasks {
    register("buildNative") {
        group = "build"
        description = "Build native executable"
        
        doLast {
            exec {
                commandLine("./gradlew", "build", "-Dquarkus.package.type=native")
            }
        }
    }
    
    register("dockerBuildNative") {
        dependsOn("buildNative")  
        group = "docker"
        description = "Build native Docker image"
        
        doLast {
            exec {
                commandLine("docker", "build", "-f", "docker/Dockerfile.native", "-t", "api-gateway:latest-native", ".")
            }
        }
    }
}
"@
        
        if (-not ($content -match "buildNative")) {
            Invoke-Fix "Add GraalVM tasks to API Gateway" {
                $newContent = $content + $graalvmConfig
                Set-Content $buildFile $newContent -Encoding UTF8
            } $buildFile
        }
    }
}

function Create-NativeDockerfiles {
    Write-Host "🐳 Creating native Dockerfiles..." -ForegroundColor Yellow
    
    $services = @(
        @{name = "core-business-service"; path = "consolidated-services\core-business-service" },
        @{name = "customer-relations-service"; path = "consolidated-services\customer-relations-service" },
        @{name = "operations-management-service"; path = "consolidated-services\operations-management-service" },
        @{name = "platform-services"; path = "consolidated-services\platform-services" },
        @{name = "workforce-management-service"; path = "consolidated-services\workforce-management-service" },
        @{name = "api-gateway"; path = "api-gateway" }
    )
    
    foreach ($service in $services) {
        $dockerDir = "$($service.path)\docker"
        $dockerFile = "$dockerDir\Dockerfile.native"
        
        New-Item -ItemType Directory -Path $dockerDir -Force | Out-Null
        
        $nativeDockerContent = @"
# Dockerfile.native - GraalVM Native Build
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3-java17 AS build

# Set working directory
WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle.properties ./

# Copy buildSrc if exists
COPY buildSrc buildSrc

# Copy source code
COPY src src

# Build native executable
RUN ./gradlew build -Dquarkus.package.type=native

# Runtime stage - Minimal distroless image
FROM gcr.io/distroless/cc-debian11:nonroot

# Copy native executable
COPY --from=build /app/build/*-runner /application

# Expose port
EXPOSE 8080

# Run native executable
ENTRYPOINT ["/application"]
"@
        
        if (-not (Test-Path $dockerFile)) {
            Invoke-Fix "Create native Dockerfile for $($service.name)" {
                Set-Content $dockerFile $nativeDockerContent -Encoding UTF8
            } $dockerFile
        }
    }
}

function Update-BuildConventionsGraalVM {
    Write-Host "⚙️ Updating build conventions for GraalVM..." -ForegroundColor Yellow
    
    $conventionsFile = "buildSrc\src\main\kotlin\consolidated-service-conventions.gradle.kts"
    if (Test-Path $conventionsFile) {
        $content = Get-Content $conventionsFile -Raw
        
        $nativeConfig = @"

// GraalVM Native Support
tasks {
    register("buildNative") {
        group = "build"
        description = "Build native executable"
        
        doLast {
            exec {
                commandLine("./gradlew", "build", "-Dquarkus.package.type=native")
            }
        }
    }
    
    register("dockerBuildNative") {
        dependsOn("buildNative")
        group = "docker"
        description = "Build native Docker image"
        
        doLast {
            exec {
                commandLine("docker", "build", "-f", "docker/Dockerfile.native", "-t", "`${project.name}:latest-native", ".")
            }
        }
    }
}
"@
        
        if (-not ($content -match "buildNative")) {
            Invoke-Fix "Add GraalVM configuration to consolidated-service-conventions" {
                $newContent = $content + $nativeConfig
                Set-Content $conventionsFile $newContent -Encoding UTF8
            } $conventionsFile
        }
    }
}

# Execute fix functions in order
Update-GradleProperties
Update-BuildSrc  
Update-CommonConventions
Update-ServiceConventions
Update-JavaVersion
Repair-SyntaxErrors

# GraalVM standardization
if ($StandardizeGraalVM) {
    Write-Host "`n🚀 Executing GraalVM Standardization..." -ForegroundColor Magenta
    Update-GraalVMConfiguration
    Update-ConsolidatedServiceGraalVM
    Update-ApiGatewayGraalVM
    Create-NativeDockerfiles
    Update-BuildConventionsGraalVM
    
    Write-Host "`n✅ GraalVM standardization complete!" -ForegroundColor Green
    Write-Host "📋 Native build commands:" -ForegroundColor Cyan
    Write-Host "   • Build native: .\gradlew.bat build -Dquarkus.package.type=native" -ForegroundColor Gray
    Write-Host "   • Docker native: .\gradlew.bat dockerBuildNative" -ForegroundColor Gray
}

if (-not $DryRun) {
    Test-BuildConfiguration
}

Show-FixSummary

Write-Host "`n🎯 Run validation: .\validate-dependencies.ps1 -Detailed" -ForegroundColor Cyan

function Update-GraalVMConfiguration {
    Write-Host "`n🚀 Standardizing GraalVM Native Configuration..." -ForegroundColor Magenta
    
    # Update gradle.properties with GraalVM settings
    $propsFile = "gradle.properties"
    if (Test-Path $propsFile) {
        $content = Get-Content $propsFile -Raw
        
        # Add GraalVM properties if missing
        $graalvmProps = @"

# GraalVM Native Configuration
quarkus.native.container-build=true
quarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21
quarkus.container-image.build=true
quarkus.container-image.push=false
quarkus.container-image.group=chiro-erp
quarkus.native.additional-build-args=-H:+ReportExceptionStackTraces,-H:+PrintClassInitialization,--enable-url-protocols=https

# Native optimization
quarkus.native.resources.includes=META-INF/services/**,application.yml,application.properties
quarkus.native.enable-jni=true
quarkus.native.enable-all-security-services=true
quarkus.native.enable-https-url-handler=true

# Container optimization
quarkus.container-image.registry=docker.io
quarkus.container-image.tag=latest-native
"@
        
        if (-not ($content -match "quarkus\.native\.container-build")) {
            Invoke-Fix "Add GraalVM native configuration to gradle.properties" {
                $newContent = $content + $graalvmProps
                Set-Content $propsFile $newContent -Encoding UTF8
            } $propsFile
        }
    }
}

function Update-ConsolidatedServiceGraalVM {
    Write-Host "`n🏗️ Updating Consolidated Services for GraalVM..." -ForegroundColor Magenta
    
    $consolidatedServices = @(
        "consolidated-services\core-business-service",
        "consolidated-services\customer-relations-service", 
        "consolidated-services\operations-management-service",
        "consolidated-services\platform-services",
        "consolidated-services\workforce-management-service"
    )
    
    foreach ($servicePath in $consolidatedServices) {
        $buildFile = Join-Path $servicePath "build.gradle.kts"
        
        if (Test-Path $buildFile) {
            $content = Get-Content $buildFile -Raw
            
            # Add GraalVM plugin if missing
            if (-not ($content -match "io\.quarkus\.gradle\.plugin")) {
                Invoke-Fix "Add Quarkus GraalVM plugin to $servicePath" {
                    $newContent = $content -replace "(plugins \{)", "`$1`n    id(`"io.quarkus.gradle.plugin`") version `"`${quarkusPluginVersion}`""
                    Set-Content $buildFile $newContent -Encoding UTF8
                } $buildFile
            }
            
            # Add native-specific dependencies
            $nativeDeps = @"

    // GraalVM Native optimizations
    implementation("io.quarkus:quarkus-container-image-jib")
    implementation("io.quarkus:quarkus-kubernetes")
    
    // Native-friendly JSON processing
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    
    // Native reflection configuration
    implementation("io.quarkus:quarkus-reflection-config")
"@
            
            if (-not ($content -match "quarkus-container-image-jib")) {
                Invoke-Fix "Add GraalVM native dependencies to $servicePath" {
                    $newContent = $content -replace "(dependencies \{[^}]*)", "`$1$nativeDeps"
                    Set-Content $buildFile $newContent -Encoding UTF8
                } $buildFile
            }
        }
    }
}

function Update-ApiGatewayGraalVM {
    Write-Host "`n🌐 Updating API Gateway for GraalVM..." -ForegroundColor Magenta
    
    $apiGatewayPath = "api-gateway"
    $buildFile = Join-Path $apiGatewayPath "build.gradle.kts"
    
    if (-not (Test-Path $buildFile)) {
        Invoke-Fix "Create API Gateway build.gradle.kts with GraalVM support" {
            $content = @"
plugins {
    id("service-conventions")
    id("quality-conventions")
    id("io.quarkus.gradle.plugin") version "`${quarkusPluginVersion}"
}

dependencies {
    // API Gateway core
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    
    // Gateway routing and security
    implementation("io.quarkus:quarkus-vertx-http")
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-security-jpa")
    implementation("io.quarkus:quarkus-oidc")
    
    // Service discovery and load balancing
    implementation("io.quarkus:quarkus-kubernetes-service-binding")
    implementation("io.quarkus:quarkus-load-balancer")
    
    // Monitoring and observability
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-health")
    implementation("io.quarkus:quarkus-info")
    
    // GraalVM Native support
    implementation("io.quarkus:quarkus-container-image-jib")
    implementation("io.quarkus:quarkus-kubernetes")
    implementation("io.quarkus:quarkus-reflection-config")
    
    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "com.chiro.gateway"
version = "1.0.0-SNAPSHOT"

// GraalVM Native configuration
quarkus {
    buildForkOptions {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    }
}
"@
            Set-Content $buildFile $content -Encoding UTF8
        } $buildFile
    }
}

function Create-NativeDockerfiles {
    Write-Host "`n🐳 Creating GraalVM Native Dockerfiles..." -ForegroundColor Magenta
    
    $services = @(
        "consolidated-services\core-business-service",
        "consolidated-services\customer-relations-service",
        "consolidated-services\operations-management-service", 
        "consolidated-services\platform-services",
        "consolidated-services\workforce-management-service",
        "api-gateway"
    )
    
    foreach ($servicePath in $services) {
        $dockerDir = Join-Path $servicePath "docker"
        $dockerFile = Join-Path $dockerDir "Dockerfile.native"
        
        if (-not (Test-Path $dockerDir)) {
            New-Item -ItemType Directory -Path $dockerDir -Force | Out-Null
        }
        
        if (-not (Test-Path $dockerFile)) {
            $serviceName = Split-Path $servicePath -Leaf
            
            Invoke-Fix "Create native Dockerfile for $serviceName" {
                $content = @"
####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode.
# It uses a micro base image, minimizing the final image size.
####

FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/

# Copy the native executable into the container
COPY --chown=1001 build/native-image/$serviceName-*-runner /work/application

# Set permissions for the application
RUN chmod 775 /work/application

# Use the run-user to run the application  
USER 1001

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/q/health/live || exit 1

# Labels for better container management
LABEL maintainer="Chiro ERP Team" \
      description="$serviceName - GraalVM Native" \
      version="1.0.0" \
      architecture="native"
"@
                Set-Content $dockerFile $content -Encoding UTF8
            } $dockerFile
        }
    }
}

function Update-BuildConventionsGraalVM {
    Write-Host "`n⚙️ Updating build conventions for GraalVM..." -ForegroundColor Magenta
    
    $conventionsFile = "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts"
    
    if (Test-Path $conventionsFile) {
        $content = Get-Content $conventionsFile -Raw
        
        # Add native configuration if missing
        $nativeConfig = @"

// GraalVM Native Image configuration
quarkus {
    buildForkOptions {
        systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
        systemProperty("maven.home", System.getenv("MAVEN_HOME"))
    }
    
    // Native build configuration
    nativeConfig {
        containerBuild.set(true)
        builderImage.set("quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21")
        additionalBuildArgs.addAll(listOf(
            "-H:+ReportExceptionStackTraces",
            "-H:+PrintClassInitialization",
            "--enable-url-protocols=https",
            "--initialize-at-build-time=org.slf4j.LoggerFactory,org.slf4j.impl.StaticLoggerBinder"
        ))
    }
}

// Native-specific tasks
tasks {
    register("buildNative") {
        dependsOn("build")
        group = "build"
        description = "Build native executable"
        
        doLast {
            exec {
                commandLine("./gradlew", "build", "-Dquarkus.package.type=native")
            }
        }
    }
    
    register("dockerBuildNative") {
        dependsOn("buildNative")
        group = "docker"
        description = "Build native Docker image"
        
        doLast {
            exec {
                commandLine("docker", "build", "-f", "docker/Dockerfile.native", "-t", "`${project.name}:latest-native", ".")
            }
        }
    }
}
"@
        
        if (-not ($content -match "quarkus \{")) {
            Invoke-Fix "Add GraalVM configuration to consolidated-service-conventions" {
                $newContent = $content + $nativeConfig
                Set-Content $conventionsFile $newContent -Encoding UTF8
            } $conventionsFile
        }
    }
}

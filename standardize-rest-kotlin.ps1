# Standardize REST Dependencies to Kotlin Serialization Only
# This script ensures all REST dependencies use Kotlin serialization consistently

param(
    [switch]$DryRun
)

Write-Host "🔧 Standardizing REST Dependencies to Kotlin Serialization" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "🔍 DRY RUN MODE - No changes will be made" -ForegroundColor Yellow
}

$Script:FilesModified = 0

function Update-BuildFile {
    param(
        [string]$FilePath,
        [string]$Description
    )
    
    if (-not (Test-Path $FilePath)) {
        Write-Host "   ⚠️  File not found: $FilePath" -ForegroundColor Yellow
        return
    }
    
    $content = Get-Content $FilePath -Raw
    $originalContent = $content
    
    Write-Host "🔄 Processing: $Description" -ForegroundColor Yellow
    
    # 1. Replace RESTEasy with REST + Kotlin serialization
    $content = $content -replace 'implementation\("io\.quarkus:quarkus-resteasy-reactive"\)', 'implementation("io.quarkus:quarkus-rest")'
    $content = $content -replace 'implementation\("io\.quarkus:quarkus-resteasy-reactive-jackson"\)', 'implementation("io.quarkus:quarkus-rest-kotlin-serialization")'
    $content = $content -replace 'implementation\("io\.quarkus:quarkus-resteasy-reactive-kotlin-serialization"\)', 'implementation("io.quarkus:quarkus-rest-kotlin-serialization")'
    
    # 2. Replace old REST client with Kotlin serialization version (unified approach)
    $content = $content -replace 'implementation\("io\.quarkus:quarkus-rest-client-reactive"\)', 'implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization")'
    $content = $content -replace 'implementation\("io\.quarkus:quarkus-rest-client-reactive-jackson"\)', 'implementation("io.quarkus:quarkus-rest-client-reactive-kotlin-serialization")'
    
    # 3. Remove standalone Jackson REST dependencies (we only use Kotlin serialization)
    $content = $content -replace '\s*implementation\("io\.quarkus:quarkus-rest-jackson"\)[^\r\n]*[\r\n]*', ''
    
    # 4. Remove RESTEasy references in comments
    $content = $content -replace '// RESTEasy', '// REST'
    $content = $content -replace 'RESTEasy', 'REST'
    
    # 5. Add Kotlin serialization if REST is present but Kotlin serialization is missing
    if ($content -match 'implementation\("io\.quarkus:quarkus-rest"\)' -and $content -notmatch 'quarkus-rest-kotlin-serialization') {
        $content = $content -replace '(implementation\("io\.quarkus:quarkus-rest"\))', '$1' + "`n" + '    implementation("io.quarkus:quarkus-rest-kotlin-serialization") // Kotlin serialization for REST'
    }
    
    # 6. Remove duplicate dependencies
    $lines = $content -split "`n"
    $seenDependencies = @{}
    $filteredLines = @()
    
    foreach ($line in $lines) {
        if ($line -match 'implementation\("io\.quarkus:([^"]+)"\)') {
            $dependency = $Matches[1]
            if (-not $seenDependencies.ContainsKey($dependency)) {
                $seenDependencies[$dependency] = $true
                $filteredLines += $line
            }
            else {
                Write-Host "   🔄 Removed duplicate: $dependency" -ForegroundColor Yellow
            }
        }
        else {
            $filteredLines += $line
        }
    }
    
    $content = $filteredLines -join "`n"
    
    # 7. Clean up extra blank lines
    $content = $content -replace '(\r?\n){3,}', "`n`n"
    
    if ($content -ne $originalContent) {
        if (!$DryRun) {
            Set-Content -Path $FilePath -Value $content -NoNewline -Encoding UTF8
        }
        Write-Host "   ✅ Updated: $FilePath" -ForegroundColor Green
        $Script:FilesModified++
        
        # Show what was changed
        if ($originalContent -match 'resteasy' -or $originalContent -match 'rest-jackson') {
            Write-Host "   📝 Converted RESTEasy/Jackson to REST + Kotlin serialization" -ForegroundColor Cyan
        }
    }
    else {
        Write-Host "   ✅ Already standardized: $FilePath" -ForegroundColor Gray
    }
}

function Update-AllBuildFile {
    Write-Host "`n🔍 Finding all build.gradle.kts files..." -ForegroundColor Cyan
    
    # Get all build.gradle.kts files in the project
    $buildFiles = Get-ChildItem -Path "." -Filter "build.gradle.kts" -Recurse | 
    Where-Object { $_.FullName -notmatch "\\build\\" -and $_.FullName -notmatch "\\.gradle\\" }
    
    Write-Host "   Found $($buildFiles.Count) build files to process" -ForegroundColor Cyan
    
    # Process buildSrc convention files first
    $conventionFiles = @(
        "buildSrc\src\main\kotlin\org\chiro\common-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\service-conventions.gradle.kts", 
        "buildSrc\src\main\kotlin\org\chiro\consolidated-service-conventions.gradle.kts",
        "buildSrc\src\main\kotlin\org\chiro\quality-conventions.gradle.kts"
    )
    
    Write-Host "`n📦 Processing convention files..." -ForegroundColor Cyan
    foreach ($file in $conventionFiles) {
        $fileName = Split-Path $file -Leaf
        Update-BuildFile $file "Convention: $fileName"
    }
    
    # Process all other build files
    Write-Host "`n🏗️  Processing service build files..." -ForegroundColor Cyan
    foreach ($file in $buildFiles) {
        $relativePath = $file.FullName.Replace((Get-Location).Path + "\", "")
        if ($relativePath -notmatch "buildSrc") {
            Update-BuildFile $file.FullName "Service: $relativePath"
        }
    }
}

function Test-Standardization {
    Write-Host "`n✅ Validating standardization..." -ForegroundColor Cyan
    
    $buildFiles = Get-ChildItem -Path "." -Filter "build.gradle.kts" -Recurse | 
    Where-Object { $_.FullName -notmatch "\\build\\" }
    
    $issuesFound = 0
    
    foreach ($file in $buildFiles) {
        $content = Get-Content $file.FullName -Raw
        $relativePath = $file.FullName.Replace((Get-Location).Path + "\", "")
        
        # Check for old REST dependencies (but ignore excludes)
        if ($content -match 'quarkus-resteasy' -and $content -notmatch 'exclude.*quarkus-resteasy') {
            Write-Host "   ❌ Found RESTEasy dependency in: $relativePath" -ForegroundColor Red
            $issuesFound++
        }
        
        if ($content -match 'quarkus-rest-jackson' -and $content -notmatch 'exclude.*quarkus-rest-jackson' -and $content -notmatch 'rest-client.*jackson') {
            Write-Host "   ❌ Found Jackson REST dependency in: $relativePath" -ForegroundColor Red
            $issuesFound++
        }
        
        # Check for missing Kotlin serialization when REST is present
        if ($content -match 'quarkus-rest"' -and $content -notmatch 'quarkus-rest-kotlin-serialization') {
            Write-Host "   ⚠️  REST found without Kotlin serialization in: $relativePath" -ForegroundColor Yellow
            $issuesFound++
        }
    }
    
    if ($issuesFound -eq 0) {
        Write-Host "   ✅ All files follow REST + Kotlin serialization standard!" -ForegroundColor Green
    }
    else {
        Write-Host "   ❌ Found $issuesFound issues that need attention" -ForegroundColor Red
    }
    
    return $issuesFound
}

function Show-Summary {
    Write-Host "`n📊 Summary" -ForegroundColor Cyan
    Write-Host "==========" -ForegroundColor Cyan
    Write-Host "Files modified: $Script:FilesModified" -ForegroundColor Green
    
    if ($DryRun) {
        Write-Host "✨ Run without -DryRun to apply changes" -ForegroundColor Yellow
    }
    else {
        Write-Host "✨ Standardization complete!" -ForegroundColor Green
    }
}

# Main execution
try {
    Update-AllBuildFile
    Test-Standardization
    Show-Summary
    
    if (!$DryRun -and $Script:FilesModified -gt 0) {
        Write-Host "`n✅ Standardization completed successfully!" -ForegroundColor Green
        Write-Host "   💡 Test build manually with: .\gradlew.bat clean build" -ForegroundColor Cyan
        Write-Host "   💡 Or run quick test with: .\gradlew.bat help" -ForegroundColor Cyan
    }
    
}
catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

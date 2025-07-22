#!/usr/bin/env pwsh
# Fix Repositories Script
# Adds missing repositories block to all Gradle build files

Write-Host "🔧 FIXING REPOSITORIES ACROSS ALL MODULES" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor Gray

$repositoriesBlock = @"
repositories {
    mavenCentral()
    mavenLocal()
}

"@

# Function to add repositories if missing
function Add-RepositoriesIfMissing {
    param(
        [string]$FilePath
    )
    
    if (-not (Test-Path $FilePath)) {
        Write-Host "   ❌ File not found: $FilePath" -ForegroundColor Red
        return
    }
    
    $content = Get-Content $FilePath -Raw
    
    # Check if repositories block already exists
    if ($content -match "repositories\s*\{") {
        Write-Host "   ✅ Repositories already exist: $FilePath" -ForegroundColor Green
        return
    }
    
    # Find the plugins block and add repositories after it
    if ($content -match "(plugins\s*\{[^}]*\})\s*") {
        $pluginsBlock = $matches[1]
        $newContent = $content -replace "(plugins\s*\{[^}]*\})\s*", "`$1`n`n$repositoriesBlock"
        
        Set-Content -Path $FilePath -Value $newContent -Encoding UTF8
        Write-Host "   ✅ Added repositories: $FilePath" -ForegroundColor Green
    }
    else {
        Write-Host "   ⚠️  Could not find plugins block in: $FilePath" -ForegroundColor Yellow
    }
}

# List of all build files to fix
$buildFiles = @(
    "api-gateway/build.gradle.kts",
    "consolidated-services/core-business-service/build.gradle.kts",
    "consolidated-services/customer-relations-service/build.gradle.kts", 
    "consolidated-services/operations-management-service/build.gradle.kts",
    "consolidated-services/platform-services/build.gradle.kts",
    "consolidated-services/workforce-management-service/build.gradle.kts"
)

Write-Host "`n🔧 PROCESSING BUILD FILES" -ForegroundColor Cyan

foreach ($file in $buildFiles) {
    Write-Host "📝 Processing: $file" -ForegroundColor Yellow
    Add-RepositoriesIfMissing -FilePath $file
}

Write-Host "`n🔍 VALIDATION" -ForegroundColor Cyan
Write-Host "Running quick validation..." -ForegroundColor Yellow

# Validate that all files now have repositories
$allGood = $true
foreach ($file in $buildFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        if ($content -match "repositories\s*\{") {
            Write-Host "   ✅ $file" -ForegroundColor Green
        }
        else {
            Write-Host "   ❌ $file - Missing repositories" -ForegroundColor Red
            $allGood = $false
        }
    }
    else {
        Write-Host "   ❌ $file - File not found" -ForegroundColor Red
        $allGood = $false
    }
}

Write-Host "`n═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor Gray
if ($allGood) {
    Write-Host "✅ ALL REPOSITORIES FIXED SUCCESSFULLY!" -ForegroundColor Green
    Write-Host "`n💡 Next step: Run .\gradlew build" -ForegroundColor Cyan
}
else {
    Write-Host "⚠️  SOME ISSUES REMAIN - CHECK OUTPUT ABOVE" -ForegroundColor Yellow
}
Write-Host "🎉 SCRIPT COMPLETED" -ForegroundColor Cyan

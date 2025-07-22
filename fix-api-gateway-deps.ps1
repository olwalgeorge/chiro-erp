#!/usr/bin/env pwsh
# Fix API Gateway Dependencies Script
# Removes problematic dependencies and replaces with working alternatives

Write-Host "🔧 FIXING API GATEWAY DEPENDENCIES" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor Gray

$apiGatewayFile = "api-gateway/build.gradle.kts"

if (-not (Test-Path $apiGatewayFile)) {
    Write-Host "❌ API Gateway build file not found: $apiGatewayFile" -ForegroundColor Red
    exit 1
}

Write-Host "📝 Reading current API Gateway build file..." -ForegroundColor Yellow
$content = Get-Content $apiGatewayFile -Raw

Write-Host "🔧 Removing problematic dependencies..." -ForegroundColor Yellow

# Remove the problematic dependencies
$content = $content -replace 'implementation\("io\.quarkus:quarkus-rest-client-reactive"\)', '// Removed problematic dependency'
$content = $content -replace 'implementation\("io\.quarkus:quarkus-rest-client-reactive-kotlin-serialization"\)', '// Removed problematic dependency'

# Add working REST client alternatives
$content = $content -replace '(implementation\("io\.quarkus:quarkus-rest"\))', "`$1`n    implementation(`"io.quarkus:quarkus-rest-client`")"

Write-Host "💾 Writing updated API Gateway build file..." -ForegroundColor Yellow
Set-Content -Path $apiGatewayFile -Value $content -Encoding UTF8

Write-Host "🔍 VALIDATION" -ForegroundColor Cyan
Write-Host "Checking for problematic dependencies..." -ForegroundColor Yellow

$updatedContent = Get-Content $apiGatewayFile -Raw

if ($updatedContent -match "quarkus-rest-client-reactive[^-]") {
    Write-Host "   ⚠️  Still contains quarkus-rest-client-reactive" -ForegroundColor Yellow
}
else {
    Write-Host "   ✅ Removed quarkus-rest-client-reactive" -ForegroundColor Green
}

if ($updatedContent -match "quarkus-rest-client-reactive-kotlin-serialization") {
    Write-Host "   ⚠️  Still contains quarkus-rest-client-reactive-kotlin-serialization" -ForegroundColor Yellow
}
else {
    Write-Host "   ✅ Removed quarkus-rest-client-reactive-kotlin-serialization" -ForegroundColor Green
}

if ($updatedContent -match "quarkus-rest-client") {
    Write-Host "   ✅ Added quarkus-rest-client as replacement" -ForegroundColor Green
}
else {
    Write-Host "   ⚠️  Missing quarkus-rest-client replacement" -ForegroundColor Yellow
}

Write-Host "`n═══════════════════════════════════════════════════════════════════════════════" -ForegroundColor Gray
Write-Host "✅ API GATEWAY DEPENDENCIES FIXED!" -ForegroundColor Green
Write-Host "`n💡 Next step: Run .\gradlew build" -ForegroundColor Cyan
Write-Host "🎉 SCRIPT COMPLETED" -ForegroundColor Cyan

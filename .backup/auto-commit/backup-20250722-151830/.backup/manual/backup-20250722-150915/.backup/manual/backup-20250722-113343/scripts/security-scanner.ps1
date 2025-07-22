#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Docker security scanner and vulnerability checker for Chiro ERP
.DESCRIPTION
    This script scans Docker images for security vulnerabilities using multiple tools
.PARAMETER ImageName
    The Docker image name to scan
.PARAMETER ScanType
    Type of scan: quick, full, critical-only
.PARAMETER OutputFormat
    Output format: json, table, sarif
#>

param(
    [Parameter(Mandatory = $false)]
    [string]$ImageName = "",
    
    [Parameter(Mandatory = $false)]
    [ValidateSet("quick", "full", "critical-only")]
    [string]$ScanType = "quick",
    
    [Parameter(Mandatory = $false)]
    [ValidateSet("json", "table", "sarif")]
    [string]$OutputFormat = "table",
    
    [Parameter(Mandatory = $false)]
    [switch]$FixVulnerabilities,
    
    [Parameter(Mandatory = $false)]
    [switch]$BuildSecure
)

$ErrorActionPreference = "Stop"

# Colors for output
$Colors = @{
    Critical = "Red"
    High     = "DarkRed"
    Medium   = "Yellow"
    Low      = "DarkYellow"
    Success  = "Green"
    Info     = "Cyan"
    Warning  = "Magenta"
}

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Colors[$Color]
}

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-ColorOutput "=" * 80 "Info"
    Write-ColorOutput $Message "Info"
    Write-ColorOutput "=" * 80 "Info"
}

function Test-Prerequisites {
    Write-ColorOutput "Checking prerequisites..." "Info"
    
    # Check Docker
    try {
        docker --version | Out-Null
        Write-ColorOutput "✓ Docker is available" "Success"
    }
    catch {
        Write-ColorOutput "✗ Docker is not available" "Critical"
        exit 1
    }
    
    # Check Trivy
    try {
        trivy --version | Out-Null
        Write-ColorOutput "✓ Trivy scanner is available" "Success"
    }
    catch {
        Write-ColorOutput "⚠ Trivy not found. Installing..." "Warning"
        Install-Trivy
    }
}

function Install-Trivy {
    Write-ColorOutput "Installing Trivy security scanner..." "Info"
    
    if ($IsWindows -or $env:OS -eq "Windows_NT") {
        # Windows installation via Chocolatey or direct download
        if (Get-Command choco -ErrorAction SilentlyContinue) {
            choco install trivy -y
        }
        else {
            Write-ColorOutput "Please install Trivy manually from: https://github.com/aquasecurity/trivy/releases" "Warning"
            exit 1
        }
    }
    else {
        # Linux/Mac installation
        curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
    }
}

function Get-ImageVulnerabilities {
    param([string]$Image)
    
    Write-Header "Scanning $Image for vulnerabilities"
    
    $trivyArgs = @(
        "image",
        "--format", $OutputFormat,
        "--severity", "UNKNOWN,LOW,MEDIUM,HIGH,CRITICAL"
    )
    
    if ($ScanType -eq "critical-only") {
        $trivyArgs[3] = "CRITICAL"
    }
    elseif ($ScanType -eq "quick") {
        $trivyArgs += @("--skip-files", "/usr/share/doc/**,/usr/share/man/**")
    }
    
    $trivyArgs += $Image
    
    try {
        $result = & trivy @trivyArgs
        
        # Parse results for summary
        if ($OutputFormat -eq "json") {
            $jsonResult = $result | ConvertFrom-Json
            Show-VulnerabilitySummary $jsonResult
        }
        else {
            Write-Host $result
        }
        
        return $result
    }
    catch {
        Write-ColorOutput "✗ Failed to scan image: $_" "Critical"
        return $null
    }
}

function Show-VulnerabilitySummary {
    param($ScanResults)
    
    $summary = @{
        CRITICAL = 0
        HIGH     = 0
        MEDIUM   = 0
        LOW      = 0
        UNKNOWN  = 0
    }
    
    foreach ($result in $ScanResults.Results) {
        if ($result.Vulnerabilities) {
            foreach ($vuln in $result.Vulnerabilities) {
                $summary[$vuln.Severity]++
            }
        }
    }
    
    Write-Header "Vulnerability Summary"
    Write-ColorOutput "Critical: $($summary.CRITICAL)" "Critical"
    Write-ColorOutput "High:     $($summary.HIGH)" "High"
    Write-ColorOutput "Medium:   $($summary.MEDIUM)" "Medium"
    Write-ColorOutput "Low:      $($summary.LOW)" "Low"
    Write-ColorOutput "Unknown:  $($summary.UNKNOWN)" "Info"
    
    $total = $summary.CRITICAL + $summary.HIGH + $summary.MEDIUM + $summary.LOW + $summary.UNKNOWN
    Write-ColorOutput "Total:    $total" "Info"
    
    # Security gate
    if ($summary.CRITICAL -gt 0) {
        Write-ColorOutput "❌ SECURITY GATE FAILED: Critical vulnerabilities found!" "Critical"
        return $false
    }
    elseif ($summary.HIGH -gt 10) {
        Write-ColorOutput "⚠️  WARNING: High number of high-severity vulnerabilities!" "Warning"
        return $false
    }
    else {
        Write-ColorOutput "✅ Security scan passed" "Success"
        return $true
    }
}

function Build-SecureImage {
    param([string]$Service)
    
    Write-Header "Building secure image for $Service"
    
    $dockerfileOptions = @(
        @{ Name = "distroless"; File = "Dockerfile.multi"; Target = "runtime-distroless"; Description = "Most secure - Distroless base" },
        @{ Name = "alpine"; File = "Dockerfile.multi"; Target = "runtime-alpine"; Description = "Secure with health checks" },
        @{ Name = "native"; File = "Dockerfile.multi"; Target = "runtime-native"; Description = "Native GraalVM - Smallest attack surface" }
    )
    
    Write-ColorOutput "Available secure build options:" "Info"
    for ($i = 0; $i -lt $dockerfileOptions.Length; $i++) {
        $option = $dockerfileOptions[$i]
        Write-ColorOutput "$($i + 1). $($option.Name) - $($option.Description)" "Info"
    }
    
    $choice = Read-Host "Select option (1-3) or press Enter for distroless"
    if ([string]::IsNullOrEmpty($choice)) { $choice = "1" }
    
    $selectedOption = $dockerfileOptions[[int]$choice - 1]
    $imageName = "chiro-erp/$Service`:secure-$($selectedOption.Name)"
    
    Write-ColorOutput "Building $imageName..." "Info"
    
    $buildArgs = @(
        "build",
        "--target", $selectedOption.Target,
        "--file", $selectedOption.File,
        "--tag", $imageName,
        "."
    )
    
    if ($Service -ne "") {
        $buildArgs += @("--build-arg", "SERVICE_NAME=$Service")
    }
    
    try {
        & docker @buildArgs
        Write-ColorOutput "✓ Successfully built secure image: $imageName" "Success"
        
        # Immediately scan the new image
        Get-ImageVulnerabilities $imageName
        
        return $imageName
    }
    catch {
        Write-ColorOutput "✗ Failed to build secure image: $_" "Critical"
        return $null
    }
}

function Get-RecommendedFixes {
    Write-Header "Security Recommendations"
    
    $recommendations = @(
        "Use distroless or minimal base images",
        "Pin specific versions of dependencies",
        "Use multi-stage builds to reduce attack surface",
        "Run containers as non-root users",
        "Remove package managers in production images",
        "Keep base images updated",
        "Use security scanners in CI/CD pipeline",
        "Implement proper secrets management",
        "Enable read-only root filesystem when possible",
        "Use specific image tags, not 'latest'"
    )
    
    foreach ($rec in $recommendations) {
        Write-ColorOutput "• $rec" "Info"
    }
}

function Scan-AllImages {
    Write-Header "Scanning All Chiro ERP Images"
    
    # Get all chiro-erp images
    $images = docker images --format "{{.Repository}}:{{.Tag}}" | Where-Object { $_ -like "chiro-erp/*" }
    
    if ($images.Count -eq 0) {
        Write-ColorOutput "No Chiro ERP images found. Build some images first." "Warning"
        return
    }
    
    $results = @()
    foreach ($image in $images) {
        Write-ColorOutput "Scanning $image..." "Info"
        $scanResult = Get-ImageVulnerabilities $image
        $results += @{ Image = $image; Result = $scanResult }
    }
    
    # Summary report
    Write-Header "Scan Summary Report"
    foreach ($result in $results) {
        Write-ColorOutput "Image: $($result.Image)" "Info"
        # Process scan result here
    }
}

# Main execution
function Main {
    Write-Header "Chiro ERP Docker Security Scanner"
    
    Test-Prerequisites
    
    if ($BuildSecure) {
        $imageName = Build-SecureImage $ImageName
        if ($imageName) {
            Get-ImageVulnerabilities $imageName
        }
    }
    elseif ($ImageName) {
        Get-ImageVulnerabilities $ImageName
    }
    else {
        Scan-AllImages
    }
    
    if ($FixVulnerabilities) {
        Get-RecommendedFixes
    }
    
    Write-Header "Security Scan Complete"
}

# Run main function
Main

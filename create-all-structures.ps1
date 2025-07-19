# Master PowerShell script to create complete chiro-erp project structure
# This script runs the main structure creation first, then creates all individual services
# Run this script from the root of the chiro-erp project directory

param(
    [switch]$Force,
    [switch]$Parallel,
    [switch]$Verbose
)

Write-Host "🚀 Starting chiro-erp complete project structure creation..." -ForegroundColor Green
Write-Host ""

$totalStartTime = Get-Date

# Function to execute script and measure time
function Invoke-ScriptWithTiming {
    param(
        [string]$ScriptPath,
        [string]$Description
    )
    
    if (-not (Test-Path $ScriptPath)) {
        Write-Host "❌ Script not found: $ScriptPath" -ForegroundColor Red
        return $false
    }
    
    $startTime = Get-Date
    Write-Host "⏳ Running: $Description..." -ForegroundColor Yellow
    
    try {
        if ($Verbose) {
            & $ScriptPath
        }
        else {
            & $ScriptPath | Out-Null
        }
        
        $duration = (Get-Date) - $startTime
        Write-Host "✅ Completed: $Description ($([math]::Round($duration.TotalSeconds, 1))s)" -ForegroundColor Green
        return $true
    }
    catch {
        $duration = (Get-Date) - $startTime
        Write-Host "❌ Failed: $Description ($([math]::Round($duration.TotalSeconds, 1))s)" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Step 1: Create main project structure
Write-Host "📁 Step 1: Creating main chiro-erp project structure" -ForegroundColor Cyan
Write-Host "=" * 60

$success = Invoke-ScriptWithTiming -ScriptPath ".\create-chiro-erp-structure.ps1" -Description "Main project structure"

if (-not $success -and -not $Force) {
    Write-Host ""
    Write-Host "❌ Failed to create main structure. Use -Force to continue anyway." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🏗️ Step 2: Creating individual service structures" -ForegroundColor Cyan
Write-Host "=" * 60

# Define all service creation scripts in logical order
$serviceScripts = @(
    @{Path = ".\create-api-gateway-service-structure.ps1"; Name = "API Gateway Service" },
    @{Path = ".\create-user-management-service-structure.ps1"; Name = "User Management Service" },
    @{Path = ".\create-tenant-management-service-structure.ps1"; Name = "Tenant Management Service" },
    @{Path = ".\create-finance-service-structure.ps1"; Name = "Finance Service" },
    @{Path = ".\create-inventory-service-structure.ps1"; Name = "Inventory Service" },
    @{Path = ".\create-sales-service-structure.ps1"; Name = "Sales Service" },
    @{Path = ".\create-procurement-service-structure.ps1"; Name = "Procurement Service" },
    @{Path = ".\create-fleet-service-structure.ps1"; Name = "Fleet Service" },
    @{Path = ".\create-project-service-structure.ps1"; Name = "Project Service" },
    @{Path = ".\create-manufacturing-service-structure.ps1"; Name = "Manufacturing Service" },
    @{Path = ".\create-fieldservice-service-structure.ps1"; Name = "Field Service" },
    @{Path = ".\create-repair-service-structure.ps1"; Name = "Repair Service" },
    @{Path = ".\create-hr-service-structure.ps1"; Name = "HR Service" },
    @{Path = ".\create-billing-service-structure.ps1"; Name = "Billing Service" },
    @{Path = ".\create-pos-service-structure.ps1"; Name = "POS Service" },
    @{Path = ".\create-crm-service-structure.ps1"; Name = "CRM Service" },
    @{Path = ".\create-analytics-service-structure.ps1"; Name = "Analytics Service" },
    @{Path = ".\create-notifications-service-structure.ps1"; Name = "Notifications Service" }
)

$successCount = 0
$failureCount = 0
$skippedCount = 0

if ($Parallel) {
    Write-Host "🔄 Running service creation scripts in parallel..." -ForegroundColor Yellow
    
    # Run scripts in parallel using background jobs
    $jobs = @()
    foreach ($script in $serviceScripts) {
        if (Test-Path $script.Path) {
            $job = Start-Job -ScriptBlock {
                param($scriptPath, $verbose)
                $ErrorActionPreference = "Stop"
                try {
                    if ($verbose) {
                        & $scriptPath
                    }
                    else {
                        & $scriptPath | Out-Null
                    }
                    return @{Success = $true; Error = $null }
                }
                catch {
                    return @{Success = $false; Error = $_.Exception.Message }
                }
            } -ArgumentList $script.Path, $Verbose
            
            $jobs += @{Job = $job; Script = $script }
        }
        else {
            Write-Host "⚠️ Skipped: $($script.Name) (script not found)" -ForegroundColor Yellow
            $skippedCount++
        }
    }
    
    # Wait for all jobs to complete and collect results
    foreach ($jobInfo in $jobs) {
        $result = Receive-Job -Job $jobInfo.Job -Wait
        Remove-Job -Job $jobInfo.Job
        
        if ($result.Success) {
            Write-Host "✅ Completed: $($jobInfo.Script.Name)" -ForegroundColor Green
            $successCount++
        }
        else {
            Write-Host "❌ Failed: $($jobInfo.Script.Name)" -ForegroundColor Red
            if ($result.Error) {
                Write-Host "   Error: $($result.Error)" -ForegroundColor Red
            }
            $failureCount++
        }
    }
}
else {
    # Run scripts sequentially
    foreach ($script in $serviceScripts) {
        if (Test-Path $script.Path) {
            $success = Invoke-ScriptWithTiming -ScriptPath $script.Path -Description $script.Name
            if ($success) {
                $successCount++
            }
            else {
                $failureCount++
                
                if (-not $Force) {
                    Write-Host ""
                    Write-Host "❌ Failed to create $($script.Name). Use -Force to continue with remaining services." -ForegroundColor Red
                    break
                }
            }
        }
        else {
            Write-Host "⚠️ Skipped: $($script.Name) (script not found)" -ForegroundColor Yellow
            $skippedCount++
        }
    }
}

$totalDuration = (Get-Date) - $totalStartTime

Write-Host ""
Write-Host "🎉 Project structure creation completed!" -ForegroundColor Green
Write-Host "=" * 60

Write-Host ""
Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "   • Total execution time: $([math]::Round($totalDuration.TotalMinutes, 1)) minutes"
Write-Host "   • Services created successfully: $successCount"
Write-Host "   • Services failed: $failureCount"
Write-Host "   • Services skipped: $skippedCount"
Write-Host "   • Total services: $($serviceScripts.Count)"

if ($failureCount -eq 0) {
    Write-Host ""
    Write-Host "✨ All structures created successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🎯 Next steps:" -ForegroundColor Cyan
    Write-Host "   1. Review and configure build.gradle.kts files"
    Write-Host "   2. Set up Docker Compose configuration"
    Write-Host "   3. Configure Kubernetes deployments"
    Write-Host "   4. Set up CI/CD pipelines"
    Write-Host "   5. Initialize Git repository"
    Write-Host "   6. Start implementing service logic"
}
elseif ($failureCount -gt 0) {
    Write-Host ""
    Write-Host "⚠️ Some services failed to create. Check the errors above." -ForegroundColor Yellow
    Write-Host "   You can re-run this script with -Force to skip failures and continue." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🚀 Ready to start development!" -ForegroundColor Green

# Optional: Show directory structure summary
if ($Verbose) {
    Write-Host ""
    Write-Host "📁 Project structure overview:" -ForegroundColor Cyan
    Write-Host "   └── chiro-erp/"
    Write-Host "       ├── services/ ($(if (Test-Path 'services') { (Get-ChildItem 'services' -Directory).Count } else { 0 }) services)"
    Write-Host "       ├── kubernetes/"
    Write-Host "       ├── gradle/"
    Write-Host "       ├── buildSrc/"
    Write-Host "       └── docs/"
}

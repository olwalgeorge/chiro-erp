# File Watcher for Critical Files
param(
    [int]$IntervalMinutes = 5
)

Write-Host "👁️  Starting file watcher..." -ForegroundColor Cyan
Write-Host "⏰ Checking every $IntervalMinutes minutes" -ForegroundColor Gray
Write-Host "🛑 Press Ctrl+C to stop" -ForegroundColor Gray

$criticalFiles = @(
    "fix-dependencies.ps1",
    "standardize-dependencies.ps1", 
    "validate-dependencies.ps1",
    "setup-file-protection.ps1",
    "build.gradle.kts",
    "settings.gradle.kts"
)

$lastSizes = @{}

# Initialize file sizes
foreach ($file in $criticalFiles) {
    if (Test-Path $file) {
        $lastSizes[$file] = (Get-Item $file).Length
    }
}

try {
    while ($true) {
        $changesDetected = $false
        
        foreach ($file in $criticalFiles) {
            if (Test-Path $file) {
                $currentSize = (Get-Item $file).Length
                
                # Check if file became empty
                if ($currentSize -eq 0 -and $lastSizes[$file] -gt 0) {
                    Write-Host "🚨 ALERT: $file became empty!" -ForegroundColor Red
                    Write-Host "💡 Restoring from git: git checkout HEAD -- $file" -ForegroundColor Yellow
                    
                    # Auto-restore if possible
                    $gitStatus = git status --porcelain $file 2>$null
                    if ($gitStatus) {
                        git checkout HEAD -- $file
                        Write-Host "✅ Auto-restored $file from git" -ForegroundColor Green
                    }
                    $changesDetected = $true
                }
                
                # Check for significant size changes
                elseif ($lastSizes.ContainsKey($file) -and 
                        [Math]::Abs($currentSize - $lastSizes[$file]) / $lastSizes[$file] -gt 0.5) {
                    Write-Host "⚠️  $file size changed significantly: $($lastSizes[$file]) -> $currentSize" -ForegroundColor Yellow
                    $changesDetected = $true
                }
                
                $lastSizes[$file] = $currentSize
            }
            elseif ($lastSizes.ContainsKey($file)) {
                Write-Host "🚨 ALERT: $file was deleted!" -ForegroundColor Red
                $changesDetected = $true
            }
        }
        
        if ($changesDetected) {
            Write-Host "📦 Creating emergency backup..." -ForegroundColor Cyan
            & .\create-backup.ps1 -Type "emergency"
        }
        
        Start-Sleep -Seconds ($IntervalMinutes * 60)
    }
}
catch {
    Write-Host "🛑 File watcher stopped" -ForegroundColor Red
}

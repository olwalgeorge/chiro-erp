# Setup Java Environment for Liberica JDK 24 with GraalVM
# Run this script as Administrator to set system-wide environment variables

Write-Host "Setting up Java Environment Variables..." -ForegroundColor Green

$JAVA_HOME = "C:\Program Files\BellSoft\LibericaNIK-Full-24-OpenJDK-24"

# Set JAVA_HOME system environment variable
[Environment]::SetEnvironmentVariable("JAVA_HOME", $JAVA_HOME, "Machine")
Write-Host "Set JAVA_HOME to: $JAVA_HOME" -ForegroundColor Yellow

# Get current system PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")

# Add Java bin to PATH if not already present
$javaBinPath = "$JAVA_HOME\bin"
if ($currentPath -notlike "*$javaBinPath*") {
    $newPath = "$javaBinPath;$currentPath"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
    Write-Host "Added Java bin to system PATH: $javaBinPath" -ForegroundColor Yellow
} else {
    Write-Host "Java bin already in PATH" -ForegroundColor Blue
}

# Set for current session as well
$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$javaBinPath;$env:PATH"

Write-Host "Environment setup complete!" -ForegroundColor Green
Write-Host "You may need to restart VS Code or open a new terminal for changes to take effect." -ForegroundColor Cyan

# Verify setup
Write-Host "`nVerifying Java installation:" -ForegroundColor Green
& "$JAVA_HOME\bin\java.exe" -version
Write-Host "`nVerifying GraalVM native-image:" -ForegroundColor Green
& "$JAVA_HOME\bin\native-image.exe" --version

@echo off
REM Batch wrapper for the master structure creation script
REM This allows easy execution from Command Prompt or double-clicking

echo Starting chiro-erp structure creation...
echo.

REM Check if PowerShell is available
where powershell >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: PowerShell is not available on this system.
    echo Please install PowerShell or run the .ps1 script directly.
    pause
    exit /b 1
)

REM Run the PowerShell script
powershell -ExecutionPolicy Bypass -File "create-all-structures.ps1" %*

REM Keep window open if run from double-click
if "%1"=="" pause

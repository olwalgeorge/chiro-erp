# 🔧 Chiro ERP Automation Scripts

This directory contains all automation scripts for the Chiro ERP platform. All scripts are written in PowerShell for Windows environments.

## 📋 Script Categories

### 🚀 Deployment Scripts
- **`deploy-final.ps1`** - Main deployment script with multiple actions (status, full, infrastructure, applications, cleanup, predeploy)
- **`k8s-deploy.ps1`** - Kubernetes-specific deployment script
- **`fast-start.ps1`** - Quick development environment startup

### 🔧 Maintenance Scripts
- **`fix-dependencies.ps1`** - Standardize Gradle dependencies across all services
- **`verify-service-structure-consistency.ps1`** - Validate service structure consistency
- **`standardize-k8s-manifests.ps1`** - Ensure Kubernetes manifest consistency
- **`check-structural-consistency.ps1`** - Validate overall project structure

### 💾 Backup & Recovery Scripts
- **`create-backup.ps1`** - Create comprehensive system backups
- **`restore-from-backup.ps1`** - Restore from backup files
- **`simple-restore.ps1`** - Simple backup restoration
- **`restore-and-validate-modules.ps1`** - Restore and validate service modules
- **`backup-and-cleanup-old-services.ps1`** - Clean up old service versions
- **`backup-critical-scripts.ps1`** - Backup critical scripts only

### 👨‍💻 Development Scripts
- **`dev-productivity.ps1`** - Development environment setup and productivity tools
- **`dev-consolidated.ps1`** - Consolidated development environment management
- **`dev.ps1`** - Basic development utilities
- **`auto-commit.ps1`** - Automated commit processes
- **`build-automation.ps1`** - Build automation utilities

### 🔒 Security & Validation Scripts
- **`security-scanner.ps1`** - Security scanning and validation
- **`validate-critical-scripts.ps1`** - Validate critical script integrity
- **`setup-git-hooks.ps1`** - Set up Git hooks for quality control
- **`test-pre-commit-hook.sh`** - Test pre-commit hook functionality

### 📊 SQL Scripts
- **`sql/`** - Directory containing SQL scripts for database management

## 🚀 Quick Usage Examples

### Deployment
```powershell
# Check current status
.\scripts\deploy-final.ps1 -Action status

# Full deployment (recommended)
.\scripts\deploy-final.ps1 -Action full

# Pre-deployment checks only
.\scripts\deploy-final.ps1 -Action predeploy
```

### Development Setup
```powershell
# Fix all dependencies
.\scripts\fix-dependencies.ps1

# Validate service structure
.\scripts\verify-service-structure-consistency.ps1 -Fix

# Start development environment
.\scripts\fast-start.ps1
```

### Maintenance
```powershell
# Create backup before major changes
.\scripts\create-backup.ps1

# Validate project structure
.\scripts\check-structural-consistency.ps1

# Run security scan
.\scripts\security-scanner.ps1
```

## 🎯 Script Execution Tips

### Prerequisites
- **PowerShell 5.1+** or **PowerShell Core 7+**
- **Execution Policy**: Set to allow script execution
  ```powershell
  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
  ```

### Running Scripts
Always run scripts from the project root directory:
```powershell
# From project root
.\scripts\script-name.ps1

# Not from scripts directory
cd scripts
.\script-name.ps1  # ❌ May cause path issues
```

### Common Parameters
Many scripts support common parameters:
- **`-Fix`** - Automatically fix issues when possible
- **`-Verbose`** - Show detailed output
- **`-WhatIf`** - Show what would be done without executing
- **`-Force`** - Force execution without prompts

## 🔍 Troubleshooting

### Script Won't Run
1. Check execution policy: `Get-ExecutionPolicy`
2. Ensure running from project root
3. Check script permissions
4. Verify PowerShell version: `$PSVersionTable`

### Path Issues
- Always use absolute paths in scripts
- Use `$PSScriptRoot` for script-relative paths
- Test paths before operations

### Docker Issues
- Ensure Docker Desktop is running
- Check Docker daemon status
- Verify Docker Compose is available

## 📚 Related Documentation

- **[../docs/DEPLOYMENT_GUIDE.md](../docs/DEPLOYMENT_GUIDE.md)** - Detailed deployment procedures
- **[../docs/DEPLOYMENT_IMPROVEMENTS.md](../docs/DEPLOYMENT_IMPROVEMENTS.md)** - Recent script improvements
- **[../docs/README.md](../docs/README.md)** - Full documentation index

## 🔧 Script Development Guidelines

### Best Practices
1. **Error Handling**: Use try-catch blocks and proper error handling
2. **Parameter Validation**: Validate all input parameters
3. **Logging**: Provide clear, actionable output messages
4. **Idempotency**: Scripts should be safe to run multiple times
5. **Documentation**: Include help documentation in scripts

### Testing
- Test scripts in isolated environments first
- Use `-WhatIf` parameters when available
- Validate with different PowerShell versions
- Test error conditions and edge cases

---

**All scripts are maintained and tested with the Chiro ERP platform. For issues or suggestions, refer to the main documentation.**

# Comprehensive Validation & Fix Scripts

This document describes the central validation and fix scripts created for the Chiro ERP project.

## 🎯 Main Script: `run-comprehensive-validation.ps1`

**Purpose**: Central script that runs all validation and standardization checks in the optimal order.

### Features:

-   **Structure validation** - Checks project structure consistency
-   **Dependency standardization** - Ensures consistent dependency versions
-   **Dependency fixes** - Automatically fixes common issues
-   **Final validation** - Comprehensive dependency validation
-   **Build verification** - Tests Gradle configuration

### Usage:

```powershell
# Basic validation (with prompts)
.\run-comprehensive-validation.ps1

# Dry run to see what would be done
.\run-comprehensive-validation.ps1 -DryRun -Detailed

# Force run without prompts
.\run-comprehensive-validation.ps1 -Force

# Only run validations, skip fixes
.\run-comprehensive-validation.ps1 -SkipFixes
```

### Execution Order:

1. **Prerequisites Check** - Verifies PowerShell, Java 21, Gradle
2. **Structure Validation** - Runs `verify-service-structure-consistency.ps1`
3. **Dependency Standardization** - Runs `standardize-dependencies.ps1`
4. **Dependency Fixes** - Runs `fix-dependencies.ps1`
5. **Dependency Validation** - Runs `validate-dependencies.ps1`
6. **Build Verification** - Tests Gradle configuration

## 🔧 Enhanced Fix Script: `fix-dependencies.ps1`

**Purpose**: Automatically fixes common dependency setup issues with HYBRID serialization strategy.

### Fixes Applied:

-   ✅ Updates Quarkus to stable version (3.15.1)
-   ✅ Updates Kotlin to current version (1.9.25)
-   ✅ Creates/fixes `gradle.properties`
-   ✅ Ensures buildSrc configuration is correct
-   ✅ Implements HYBRID serialization (Kotlin + Jackson)
-   ✅ Fixes syntax errors in build files
-   ✅ Tests Gradle configuration

### HYBRID Serialization Strategy:

-   **Kotlin Serialization** for internal service-to-service communication (type-safe)
-   **Jackson** for external API integrations (ecosystem compatibility)
-   **Balanced approach** for enterprise requirements

### Usage:

```powershell
# Interactive mode
.\fix-dependencies.ps1

# Dry run to see changes
.\fix-dependencies.ps1 -DryRun

# Force run without prompts
.\fix-dependencies.ps1 -Force
```

## 📊 Output & Results

### Success Indicators:

-   ✅ Green checkmarks for passed validations
-   🔧 Blue for applied fixes
-   ⚠️ Yellow for warnings
-   ❌ Red for errors

### Summary Information:

-   **Passed checks count**
-   **Issues found count**
-   **Fixes applied count**
-   **Success rate percentage**

### Next Steps Guidance:

The scripts provide clear next steps:

1. Review failed checks
2. Run additional fixes if needed
3. Test with `.\gradlew.bat clean build`
4. Run specific services for testing

## 🚀 Quick Start

For new setups or when issues are detected:

```powershell
# Complete validation and fix cycle
.\run-comprehensive-validation.ps1 -Force

# If issues remain, run manual fix
.\fix-dependencies.ps1 -Force

# Final validation
.\validate-dependencies.ps1 -Detailed

# Test build
.\gradlew.bat clean build
```

## 🔍 Individual Scripts

The comprehensive script orchestrates these individual scripts:

-   `verify-service-structure-consistency.ps1` - Structure validation
-   `standardize-dependencies.ps1` - Dependency standardization
-   `fix-dependencies.ps1` - Automatic fixes
-   `validate-dependencies.ps1` - Comprehensive validation

Each can be run individually for targeted checks or fixes.

## 📋 Best Practices

1. **Always run dry run first** to see what changes will be made
2. **Use detailed mode** (`-Detailed`) for troubleshooting
3. **Commit changes** after successful validation
4. **Test builds** before deploying
5. **Run validation** after major dependency updates

## 🎯 Enterprise Benefits

-   **Consistent project structure** across all services
-   **Standardized dependency management** with version alignment
-   **Hybrid serialization strategy** for optimal performance
-   **Automated issue detection and resolution**
-   **Clear validation reporting** for team visibility

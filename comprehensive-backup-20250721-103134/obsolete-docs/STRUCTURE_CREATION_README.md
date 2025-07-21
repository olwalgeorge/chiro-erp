# Structure Creation Scripts

This directory contains scripts to automatically create the complete chiro-erp project structure.

## Master Script

### `create-all-structures.ps1` (PowerShell)

The main script that orchestrates the creation of the entire project structure. It runs in two phases:

1. **Phase 1**: Creates the main project structure using `create-chiro-erp-structure.ps1`
2. **Phase 2**: Creates all individual service structures

#### Usage

```powershell
# Basic usage - sequential execution with progress output
.\create-all-structures.ps1

# Force continue even if some scripts fail
.\create-all-structures.ps1 -Force

# Run service creation in parallel (faster but less readable output)
.\create-all-structures.ps1 -Parallel

# Verbose output (show all script output)
.\create-all-structures.ps1 -Verbose

# Combine options
.\create-all-structures.ps1 -Force -Parallel -Verbose
```

### `create-all-structures.bat` (Batch)

A simple batch wrapper for easier execution from Command Prompt or by double-clicking.

```cmd
# Run with default options
create-all-structures.bat

# Pass parameters to the PowerShell script
create-all-structures.bat -Force -Verbose
```

## Individual Scripts

### Main Structure

-   `create-chiro-erp-structure.ps1` - Creates the main project structure (directories, config files, Kubernetes setup)

### Service Structures

-   `create-api-gateway-service-structure.ps1` - API Gateway service
-   `create-user-management-service-structure.ps1` - User management service
-   `create-tenant-management-service-structure.ps1` - Multi-tenant management
-   `create-finance-service-structure.ps1` - Financial management
-   `create-inventory-service-structure.ps1` - Inventory management
-   `create-sales-service-structure.ps1` - Sales operations
-   `create-procurement-service-structure.ps1` - Procurement management
-   `create-fleet-service-structure.ps1` - Fleet management
-   `create-project-service-structure.ps1` - Project management
-   `create-manufacturing-service-structure.ps1` - Manufacturing operations
-   `create-fieldservice-service-structure.ps1` - Field service management
-   `create-repair-service-structure.ps1` - Repair service management
-   `create-hr-service-structure.ps1` - Human resources
-   `create-billing-service-structure.ps1` - Billing and invoicing
-   `create-pos-service-structure.ps1` - Point of sale
-   `create-crm-service-structure.ps1` - Customer relationship management
-   `create-analytics-service-structure.ps1` - Analytics and reporting
-   `create-notifications-service-structure.ps1` - Notification service

## Parameters

| Parameter   | Description                                                   |
| ----------- | ------------------------------------------------------------- |
| `-Force`    | Continue execution even if individual scripts fail            |
| `-Parallel` | Run service creation scripts in parallel for faster execution |
| `-Verbose`  | Show detailed output from all scripts                         |

## Execution Order

The master script executes in the following order:

1. **Main Structure** (always first)

    - Creates base directories
    - Sets up Gradle configuration
    - Creates Kubernetes infrastructure files
    - Sets up Kafka topics
    - Creates Docker configuration

2. **Services** (in logical dependency order)
    - API Gateway (entry point)
    - User & Tenant Management (core authentication)
    - Core business services (Finance, Inventory, Sales, etc.)
    - Supporting services (Analytics, Notifications)

## Error Handling

-   By default, the script stops on first failure
-   Use `-Force` to continue past failures
-   Failed services are reported in the final summary
-   Individual script errors are displayed with details

## Output

The master script provides:

-   Real-time progress indicators
-   Execution timing for each script
-   Final summary with success/failure counts
-   Next steps recommendations

## Example Output

```
🚀 Starting chiro-erp complete project structure creation...

📁 Step 1: Creating main chiro-erp project structure
============================================================
⏳ Running: Main project structure...
✅ Completed: Main project structure (2.3s)

🏗️ Step 2: Creating individual service structures
============================================================
⏳ Running: API Gateway Service...
✅ Completed: API Gateway Service (1.2s)
⏳ Running: User Management Service...
✅ Completed: User Management Service (1.1s)
...

🎉 Project structure creation completed!
============================================================

📊 Summary:
   • Total execution time: 3.2 minutes
   • Services created successfully: 18
   • Services failed: 0
   • Services skipped: 0
   • Total services: 18

✨ All structures created successfully!
```

## Prerequisites

-   PowerShell 5.0 or higher
-   Windows operating system (scripts are Windows-specific)
-   Write permissions in the target directory

## Troubleshooting

1. **Execution Policy Error**: Run `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`
2. **Permission Denied**: Ensure you have write access to the target directory
3. **Script Not Found**: Verify all individual creation scripts are present
4. **PowerShell Not Available**: Install PowerShell or use PowerShell Core

## Next Steps After Running

1. Review generated `build.gradle.kts` files
2. Configure `settings.gradle.kts` with all services
3. Set up Docker Compose configuration
4. Configure Kubernetes deployments
5. Initialize Git repository
6. Set up CI/CD pipelines
7. Start implementing service logic

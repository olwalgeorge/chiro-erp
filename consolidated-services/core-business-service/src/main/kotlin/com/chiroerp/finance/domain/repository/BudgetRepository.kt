package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.Budget
import com.chiroerp.finance.domain.valueobject.BudgetId
import com.chiroerp.finance.domain.valueobject.AccountId
import com.chiroerp.finance.domain.valueobject.TenantId
import com.chiroerp.finance.domain.valueobject.Currency
import com.chiroerp.finance.domain.valueobject.Money
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Repository interface for Budget entity data access operations.
 * 
 * Provides comprehensive budget persistence capabilities including:
 * - Budget planning and management with hierarchical structures
 * - Variance tracking and analysis for budget monitoring
 * - Complex querying by period, category, and performance criteria
 * - Budget allocation and approval workflow support
 * - Financial planning and forecasting data access
 * - High-performance operations for budget reporting and analytics
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure budget consistency and financial planning integrity.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface BudgetRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves a budget entity to the persistent store with planning workflow tracking.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation, budget consistency, and
     * maintains budget planning workflow state across approval processes.
     * 
     * @param budget The budget entity to save
     * @return The saved budget with updated metadata (version, timestamps, state)
     * @throws IllegalArgumentException if budget is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws ConcurrencyException if optimistic locking conflict occurs
     * @throws BudgetValidationException if budget validation fails
     */
    suspend fun save(budget: Budget): Budget
    
    /**
     * Finds a budget by its unique identifier within a tenant context.
     * 
     * @param budgetId The unique budget identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The budget if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(budgetId: BudgetId, tenantId: TenantId): Budget?
    
    /**
     * Finds a budget by its unique budget code within a tenant context.
     * 
     * @param budgetCode The unique budget code
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The budget if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByBudgetCode(
        budgetCode: String,
        tenantId: TenantId
    ): Budget?
    
    /**
     * Checks if a budget exists by its identifier within a tenant context.
     * 
     * @param budgetId The unique budget identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if budget exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(budgetId: BudgetId, tenantId: TenantId): Boolean
    
    /**
     * Checks if a budget exists by its code within a tenant context.
     * 
     * @param budgetCode The unique budget code
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if budget exists, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsByBudgetCode(
        budgetCode: String,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Deletes a budget by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and planning history.
     * May be restricted based on budget status and business rules.
     * 
     * @param budgetId The unique budget identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if budget cannot be deleted
     */
    suspend fun deleteById(budgetId: BudgetId, tenantId: TenantId)
    
    // =====================================
    // Account and Category Queries
    // =====================================
    
    /**
     * Finds all budgets for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets ordered by budget period descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets for multiple accounts within a tenant context.
     * 
     * @param accountIds The list of account identifiers to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets ordered by budget period descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccounts(
        accountIds: List<AccountId>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets by category within a tenant context.
     * 
     * @param category The budget category to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets with the specified category
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCategory(
        category: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets by department within a tenant context.
     * 
     * @param department The department identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets for the specified department
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByDepartment(
        department: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets by cost center within a tenant context.
     * 
     * @param costCenter The cost center identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets for the specified cost center
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCostCenter(
        costCenter: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    // =====================================
    // Period and Date-based Queries
    // =====================================
    
    /**
     * Finds budgets for a specific fiscal year within a tenant context.
     * 
     * @param fiscalYear The fiscal year to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets for the specified fiscal year
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByFiscalYear(
        fiscalYear: Int,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets within a specific date range for a tenant.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByPeriodRange(
        startDate: LocalDate,
        endDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds active budgets for a specific date within a tenant context.
     * 
     * @param effectiveDate The date to check budget effectiveness
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of active budgets on the specified date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findActiveBudgetsForDate(
        effectiveDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets by budget type and period within a tenant context.
     * 
     * @param budgetType The budget type to filter by (e.g., "OPERATIONAL", "CAPITAL")
     * @param fiscalYear The fiscal year to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets matching type and period criteria
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTypeAndPeriod(
        budgetType: String,
        fiscalYear: Int,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    // =====================================
    // Status and Approval Queries
    // =====================================
    
    /**
     * Finds budgets by status within a tenant context.
     * 
     * @param status The budget status to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets with the specified status
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByStatus(
        status: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds draft budgets within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of draft budgets ordered by creation date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findDraftBudgets(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets pending approval within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets pending approval ordered by submission date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findBudgetsPendingApproval(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds approved budgets within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of approved budgets ordered by approval date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findApprovedBudgets(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets by approver within a tenant context.
     * 
     * @param approverId The approver identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets approved by the specified approver
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByApprover(
        approverId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    // =====================================
    // Amount and Variance Queries
    // =====================================
    
    /**
     * Finds budgets with allocated amount greater than the specified threshold.
     * 
     * @param minimumAmount The minimum allocated amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets with allocated amount above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMinimumAllocatedAmount(
        minimumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets with allocated amount within the specified range.
     * 
     * @param minimumAmount The minimum allocated amount threshold (inclusive)
     * @param maximumAmount The maximum allocated amount threshold (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets with allocated amount within range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAllocatedAmountRange(
        minimumAmount: Money,
        maximumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets with significant variance from allocated amounts.
     * 
     * @param varianceThresholdPercent The variance threshold as a percentage
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets with variance above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByVarianceThreshold(
        varianceThresholdPercent: Double,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets exceeding their allocated amounts within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of over-budget budgets ordered by excess amount descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findOverBudgetBudgets(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    /**
     * Finds budgets under-utilizing their allocated amounts within a tenant context.
     * 
     * @param utilizationThresholdPercent The utilization threshold as a percentage
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of under-utilized budgets
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findUnderUtilizedBudgets(
        utilizationThresholdPercent: Double,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    // =====================================
    // Currency-based Queries
    // =====================================
    
    /**
     * Finds budgets by currency within a tenant context.
     * 
     * @param currency The currency to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of budgets with the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCurrency(
        currency: Currency,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Budget>
    
    // =====================================
    // Aggregation and Reporting Queries
    // =====================================
    
    /**
     * Counts the total number of budgets for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of budgets
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts budgets by status for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of status to budget count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByStatus(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts budgets by category for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of category to budget count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByCategory(tenantId: TenantId): Map<String, Long>
    
    /**
     * Calculates the total allocated budget amount for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The total allocated budget in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalAllocatedAmount(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the total spent amount across all budgets for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The total spent amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalSpentAmount(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the overall budget utilization rate for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The overall utilization rate as a percentage (0.0 to 1.0)
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateOverallUtilizationRate(tenantId: TenantId): Double
    
    /**
     * Calculates budget variance analysis for a tenant by category.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return Map of category to variance amount (positive = over-budget, negative = under-budget)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateVarianceAnalysis(
        tenantId: TenantId,
        currency: Currency? = null
    ): Map<String, Money>
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple budgets in a single batch operation with planning consistency.
     * 
     * Provides better performance for bulk operations while maintaining
     * budget consistency, planning workflow integrity, and approval processes.
     * 
     * @param budgets The list of budgets to save
     * @return The list of saved budgets with updated metadata
     * @throws IllegalArgumentException if budgets are invalid
     * @throws DataAccessException if batch operation fails
     * @throws ConcurrencyException if optimistic locking conflicts occur
     * @throws BudgetValidationException if budget validations fail
     */
    suspend fun saveAll(budgets: List<Budget>): List<Budget>
    
    /**
     * Finds multiple budgets by their identifiers within a tenant context.
     * 
     * @param budgetIds The list of budget identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found budgets (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        budgetIds: List<BudgetId>,
        tenantId: TenantId
    ): List<Budget>
    
    /**
     * Updates budget statuses in bulk for workflow processing.
     * 
     * @param budgetIds The list of budget identifiers to update
     * @param newStatus The new status to set
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if update operation fails
     * @throws BusinessRuleException if status transition is invalid
     */
    suspend fun updateStatusBulk(
        budgetIds: List<BudgetId>,
        newStatus: String,
        tenantId: TenantId
    )
    
    /**
     * Deletes multiple budgets by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and planning history.
     * May be restricted based on budget statuses and business rules.
     * 
     * @param budgetIds The list of budget identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any budget cannot be deleted
     */
    suspend fun deleteByIds(
        budgetIds: List<BudgetId>,
        tenantId: TenantId
    )
}

package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.Reconciliation
import com.chiroerp.finance.domain.valueobject.ReconciliationId
import com.chiroerp.finance.domain.valueobject.AccountId
import com.chiroerp.finance.domain.valueobject.TenantId
import com.chiroerp.finance.domain.valueobject.Currency
import com.chiroerp.finance.domain.valueobject.Money
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Repository interface for Reconciliation entity data access operations.
 * 
 * Provides comprehensive reconciliation persistence capabilities including:
 * - Financial reconciliation process management and automation
 * - Complex querying by account, period, and reconciliation criteria
 * - Discrepancy tracking and resolution workflow support
 * - Multi-source reconciliation and matching algorithms
 * - Audit trail and compliance data access for regulatory requirements
 * - High-performance operations for automated reconciliation processing
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure reconciliation integrity and financial accuracy.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface ReconciliationRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves a reconciliation entity to the persistent store with process tracking.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation, reconciliation consistency, and
     * maintains reconciliation process state across financial integrity workflows.
     * 
     * @param reconciliation The reconciliation entity to save
     * @return The saved reconciliation with updated metadata (version, timestamps, status)
     * @throws IllegalArgumentException if reconciliation is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws ConcurrencyException if optimistic locking conflict occurs
     * @throws ReconciliationValidationException if reconciliation validation fails
     */
    suspend fun save(reconciliation: Reconciliation): Reconciliation
    
    /**
     * Finds a reconciliation by its unique identifier within a tenant context.
     * 
     * @param reconciliationId The unique reconciliation identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The reconciliation if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(reconciliationId: ReconciliationId, tenantId: TenantId): Reconciliation?
    
    /**
     * Finds a reconciliation by its unique reference number within a tenant context.
     * 
     * @param reconciliationReference The unique reconciliation reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The reconciliation if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByReconciliationReference(
        reconciliationReference: String,
        tenantId: TenantId
    ): Reconciliation?
    
    /**
     * Checks if a reconciliation exists by its identifier within a tenant context.
     * 
     * @param reconciliationId The unique reconciliation identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if reconciliation exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(reconciliationId: ReconciliationId, tenantId: TenantId): Boolean
    
    /**
     * Checks if a reconciliation exists by its reference within a tenant context.
     * 
     * @param reconciliationReference The unique reconciliation reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if reconciliation exists, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsByReconciliationReference(
        reconciliationReference: String,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Deletes a reconciliation by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and reconciliation history.
     * May be restricted based on reconciliation status and business rules.
     * 
     * @param reconciliationId The unique reconciliation identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if reconciliation cannot be deleted
     */
    suspend fun deleteById(reconciliationId: ReconciliationId, tenantId: TenantId)
    
    // =====================================
    // Account-based Queries
    // =====================================
    
    /**
     * Finds all reconciliations for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations ordered by reconciliation date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds reconciliations for multiple accounts within a tenant context.
     * 
     * @param accountIds The list of account identifiers to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations ordered by reconciliation date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccounts(
        accountIds: List<AccountId>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds the latest reconciliation for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The latest reconciliation for the account, null if none found
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findLatestByAccount(
        accountId: AccountId,
        tenantId: TenantId
    ): Reconciliation?
    
    // =====================================
    // Period and Date-based Queries
    // =====================================
    
    /**
     * Finds reconciliations within a specific date range for a tenant.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByReconciliationDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds reconciliations by period identifier within a tenant context.
     * 
     * @param period The period identifier (e.g., "2025-01", "2025-Q1")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations for the specified period
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByPeriod(
        period: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds reconciliations processed within a specific date range.
     * 
     * @param startDate The start processing date (inclusive)
     * @param endDate The end processing date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations processed within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByProcessedDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds reconciliations due for processing by a specific date.
     * 
     * @param dueDate The due date to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations due for processing
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findReconciliationsDueBy(
        dueDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    // =====================================
    // Status and Type Queries
    // =====================================
    
    /**
     * Finds reconciliations by status within a tenant context.
     * 
     * @param status The reconciliation status to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations with the specified status
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByStatus(
        status: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds reconciliations by type within a tenant context.
     * 
     * @param reconciliationType The reconciliation type to filter by (e.g., "BANK", "CREDIT_CARD", "ACCOUNT")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations with the specified type
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByReconciliationType(
        reconciliationType: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds pending reconciliations within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of pending reconciliations ordered by due date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPendingReconciliations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds in-progress reconciliations within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of in-progress reconciliations ordered by start date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findInProgressReconciliations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds completed reconciliations within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of completed reconciliations ordered by completion date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findCompletedReconciliations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds failed reconciliations within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of failed reconciliations ordered by failure date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findFailedReconciliations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    // =====================================
    // Discrepancy and Exception Queries
    // =====================================
    
    /**
     * Finds reconciliations with discrepancies within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations with discrepancies ordered by discrepancy amount descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findReconciliationsWithDiscrepancies(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds reconciliations with discrepancies above a specified threshold.
     * 
     * @param discrepancyThreshold The discrepancy amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations with significant discrepancies
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findReconciliationsWithSignificantDiscrepancies(
        discrepancyThreshold: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds reconciliations requiring manual review within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations requiring manual review
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findRequiringManualReview(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds auto-reconciled transactions within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of auto-reconciled reconciliations ordered by completion date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findAutoReconciledReconciliations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    // =====================================
    // Source and Reference Queries
    // =====================================
    
    /**
     * Finds reconciliations by source system within a tenant context.
     * 
     * @param sourceSystem The source system identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations from the specified source system
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findBySourceSystem(
        sourceSystem: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    /**
     * Finds reconciliations by external reference within a tenant context.
     * 
     * @param externalReference The external reference identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations with the specified external reference
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByExternalReference(
        externalReference: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    // =====================================
    // Currency-based Queries
    // =====================================
    
    /**
     * Finds reconciliations by currency within a tenant context.
     * 
     * @param currency The currency to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reconciliations with the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCurrency(
        currency: Currency,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Reconciliation>
    
    // =====================================
    // Aggregation and Reporting Queries
    // =====================================
    
    /**
     * Counts the total number of reconciliations for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of reconciliations
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts reconciliations by status for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of status to reconciliation count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByStatus(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts reconciliations by type for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of reconciliation type to count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByReconciliationType(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts reconciliations by account for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of account ID to reconciliation count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByAccount(tenantId: TenantId): Map<AccountId, Long>
    
    /**
     * Calculates the total discrepancy amount for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The total discrepancy amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalDiscrepancyAmount(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the reconciliation success rate for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The success rate as a percentage (0.0 to 1.0)
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateSuccessRate(tenantId: TenantId): Double
    
    /**
     * Calculates the average reconciliation processing time for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The average processing time in minutes
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAverageProcessingTime(tenantId: TenantId): Double
    
    /**
     * Calculates auto-reconciliation rate for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The auto-reconciliation rate as a percentage (0.0 to 1.0)
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAutoReconciliationRate(tenantId: TenantId): Double
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple reconciliations in a single batch operation with process consistency.
     * 
     * Provides better performance for bulk operations while maintaining
     * reconciliation consistency, process workflow integrity, and financial accuracy.
     * 
     * @param reconciliations The list of reconciliations to save
     * @return The list of saved reconciliations with updated metadata
     * @throws IllegalArgumentException if reconciliations are invalid
     * @throws DataAccessException if batch operation fails
     * @throws ConcurrencyException if optimistic locking conflicts occur
     * @throws ReconciliationValidationException if reconciliation validations fail
     */
    suspend fun saveAll(reconciliations: List<Reconciliation>): List<Reconciliation>
    
    /**
     * Finds multiple reconciliations by their identifiers within a tenant context.
     * 
     * @param reconciliationIds The list of reconciliation identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found reconciliations (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        reconciliationIds: List<ReconciliationId>,
        tenantId: TenantId
    ): List<Reconciliation>
    
    /**
     * Updates reconciliation statuses in bulk for workflow processing.
     * 
     * @param reconciliationIds The list of reconciliation identifiers to update
     * @param newStatus The new status to set
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if update operation fails
     * @throws BusinessRuleException if status transition is invalid
     */
    suspend fun updateStatusBulk(
        reconciliationIds: List<ReconciliationId>,
        newStatus: String,
        tenantId: TenantId
    )
    
    /**
     * Deletes multiple reconciliations by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and reconciliation history.
     * May be restricted based on reconciliation statuses and business rules.
     * 
     * @param reconciliationIds The list of reconciliation identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any reconciliation cannot be deleted
     */
    suspend fun deleteByIds(
        reconciliationIds: List<ReconciliationId>,
        tenantId: TenantId
    )
}

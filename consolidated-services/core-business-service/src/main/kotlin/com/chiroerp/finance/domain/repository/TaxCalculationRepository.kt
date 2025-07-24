package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.TaxCalculation
import com.chiroerp.finance.domain.valueobject.TaxCalculationId
import com.chiroerp.finance.domain.valueobject.TenantId
import com.chiroerp.finance.domain.valueobject.Currency
import com.chiroerp.finance.domain.valueobject.Money
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Repository interface for TaxCalculation entity data access operations.
 * 
 * Provides comprehensive tax calculation persistence capabilities including:
 * - Multi-jurisdiction tax computation and compliance data storage
 * - Complex querying by tax type, jurisdiction, and calculation criteria
 * - Tax rate management and historical tracking support
 * - Regulatory compliance and audit trail data access
 * - Tax reporting and analytics for compliance requirements
 * - High-performance operations for real-time tax calculations
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure tax calculation accuracy and regulatory compliance.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface TaxCalculationRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves a tax calculation entity to the persistent store with compliance tracking.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation, tax calculation accuracy, and
     * maintains compliance audit trail across tax jurisdictions.
     * 
     * @param taxCalculation The tax calculation entity to save
     * @return The saved tax calculation with updated metadata (version, timestamps)
     * @throws IllegalArgumentException if tax calculation is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws ConcurrencyException if optimistic locking conflict occurs
     * @throws TaxValidationException if tax calculation validation fails
     */
    suspend fun save(taxCalculation: TaxCalculation): TaxCalculation
    
    /**
     * Finds a tax calculation by its unique identifier within a tenant context.
     * 
     * @param taxCalculationId The unique tax calculation identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The tax calculation if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(taxCalculationId: TaxCalculationId, tenantId: TenantId): TaxCalculation?
    
    /**
     * Finds a tax calculation by its unique calculation reference within a tenant context.
     * 
     * @param calculationReference The unique calculation reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The tax calculation if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCalculationReference(
        calculationReference: String,
        tenantId: TenantId
    ): TaxCalculation?
    
    /**
     * Checks if a tax calculation exists by its identifier within a tenant context.
     * 
     * @param taxCalculationId The unique tax calculation identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if tax calculation exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(taxCalculationId: TaxCalculationId, tenantId: TenantId): Boolean
    
    /**
     * Checks if a tax calculation exists by its reference within a tenant context.
     * 
     * @param calculationReference The unique calculation reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if tax calculation exists, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsByCalculationReference(
        calculationReference: String,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Deletes a tax calculation by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and regulatory compliance.
     * May be restricted based on calculation status and compliance requirements.
     * 
     * @param taxCalculationId The unique tax calculation identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if tax calculation cannot be deleted
     */
    suspend fun deleteById(taxCalculationId: TaxCalculationId, tenantId: TenantId)
    
    // =====================================
    // Jurisdiction and Tax Type Queries
    // =====================================
    
    /**
     * Finds tax calculations by jurisdiction within a tenant context.
     * 
     * @param jurisdiction The tax jurisdiction to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations for the specified jurisdiction
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByJurisdiction(
        jurisdiction: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations for multiple jurisdictions within a tenant context.
     * 
     * @param jurisdictions The list of tax jurisdictions to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations for the specified jurisdictions
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByJurisdictions(
        jurisdictions: List<String>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations by tax type within a tenant context.
     * 
     * @param taxType The tax type to filter by (e.g., "VAT", "SALES_TAX", "INCOME_TAX")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with the specified tax type
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTaxType(
        taxType: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations by tax code within a tenant context.
     * 
     * @param taxCode The tax code to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with the specified tax code
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTaxCode(
        taxCode: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations by entity type within a tenant context.
     * 
     * @param entityType The entity type to filter by (e.g., "INVOICE", "PAYMENT", "TRANSACTION")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations for the specified entity type
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByEntityType(
        entityType: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations by entity reference within a tenant context.
     * 
     * @param entityReference The entity reference to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations for the specified entity
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByEntityReference(
        entityReference: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    // =====================================
    // Date-based Queries
    // =====================================
    
    /**
     * Finds tax calculations within a specific date range for a tenant.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCalculationDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations by effective date range within a tenant context.
     * 
     * @param startDate The start effective date (inclusive)
     * @param endDate The end effective date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations effective within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByEffectiveDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations for a specific tax period within a tenant context.
     * 
     * @param taxPeriod The tax period identifier (e.g., "2025-Q1", "2025-01")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations for the specified tax period
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTaxPeriod(
        taxPeriod: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    // =====================================
    // Rate and Amount Queries
    // =====================================
    
    /**
     * Finds tax calculations by tax rate within a tenant context.
     * 
     * @param taxRate The tax rate to filter by (as percentage, e.g., 0.15 for 15%)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with the specified tax rate
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTaxRate(
        taxRate: Double,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations by tax rate range within a tenant context.
     * 
     * @param minimumRate The minimum tax rate (inclusive)
     * @param maximumRate The maximum tax rate (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with tax rate within range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTaxRateRange(
        minimumRate: Double,
        maximumRate: Double,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations with taxable amount greater than the specified threshold.
     * 
     * @param minimumAmount The minimum taxable amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with taxable amount above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMinimumTaxableAmount(
        minimumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations with tax amount greater than the specified threshold.
     * 
     * @param minimumTaxAmount The minimum tax amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with tax amount above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMinimumTaxAmount(
        minimumTaxAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations with taxable amount within the specified range.
     * 
     * @param minimumAmount The minimum taxable amount threshold (inclusive)
     * @param maximumAmount The maximum taxable amount threshold (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with taxable amount within range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTaxableAmountRange(
        minimumAmount: Money,
        maximumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    // =====================================
    // Status and Compliance Queries
    // =====================================
    
    /**
     * Finds tax calculations by status within a tenant context.
     * 
     * @param status The calculation status to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with the specified status
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByStatus(
        status: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds validated tax calculations within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of validated tax calculations ordered by validation date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findValidatedCalculations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds pending tax calculations within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of pending tax calculations ordered by creation date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPendingCalculations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds failed tax calculations within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of failed tax calculations ordered by failure date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findFailedCalculations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    /**
     * Finds tax calculations requiring compliance review within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations requiring compliance review
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findRequiringComplianceReview(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    // =====================================
    // Currency-based Queries
    // =====================================
    
    /**
     * Finds tax calculations by currency within a tenant context.
     * 
     * @param currency The currency to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of tax calculations with the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCurrency(
        currency: Currency,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<TaxCalculation>
    
    // =====================================
    // Aggregation and Reporting Queries
    // =====================================
    
    /**
     * Counts the total number of tax calculations for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of tax calculations
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts tax calculations by jurisdiction for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of jurisdiction to calculation count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByJurisdiction(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts tax calculations by tax type for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of tax type to calculation count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTaxType(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts tax calculations by status for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of status to calculation count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByStatus(tenantId: TenantId): Map<String, Long>
    
    /**
     * Calculates the total taxable amount for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The total taxable amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalTaxableAmount(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the total tax amount for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The total tax amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalTaxAmount(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the effective tax rate for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The effective tax rate as a percentage (0.0 to 1.0)
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateEffectiveTaxRate(tenantId: TenantId): Double
    
    /**
     * Calculates tax amounts by jurisdiction for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return Map of jurisdiction to total tax amount
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTaxByJurisdiction(
        tenantId: TenantId,
        currency: Currency? = null
    ): Map<String, Money>
    
    /**
     * Calculates tax amounts by tax type for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return Map of tax type to total tax amount
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTaxByType(
        tenantId: TenantId,
        currency: Currency? = null
    ): Map<String, Money>
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple tax calculations in a single batch operation with compliance integrity.
     * 
     * Provides better performance for bulk operations while maintaining
     * tax calculation accuracy, compliance requirements, and audit trail integrity.
     * 
     * @param taxCalculations The list of tax calculations to save
     * @return The list of saved tax calculations with updated metadata
     * @throws IllegalArgumentException if tax calculations are invalid
     * @throws DataAccessException if batch operation fails
     * @throws ConcurrencyException if optimistic locking conflicts occur
     * @throws TaxValidationException if tax calculation validations fail
     */
    suspend fun saveAll(taxCalculations: List<TaxCalculation>): List<TaxCalculation>
    
    /**
     * Finds multiple tax calculations by their identifiers within a tenant context.
     * 
     * @param taxCalculationIds The list of tax calculation identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found tax calculations (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        taxCalculationIds: List<TaxCalculationId>,
        tenantId: TenantId
    ): List<TaxCalculation>
    
    /**
     * Updates tax calculation statuses in bulk for compliance processing.
     * 
     * @param taxCalculationIds The list of tax calculation identifiers to update
     * @param newStatus The new status to set
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if update operation fails
     * @throws BusinessRuleException if status transition is invalid
     */
    suspend fun updateStatusBulk(
        taxCalculationIds: List<TaxCalculationId>,
        newStatus: String,
        tenantId: TenantId
    )
    
    /**
     * Deletes multiple tax calculations by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and regulatory compliance.
     * May be restricted based on calculation statuses and compliance requirements.
     * 
     * @param taxCalculationIds The list of tax calculation identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any tax calculation cannot be deleted
     */
    suspend fun deleteByIds(
        taxCalculationIds: List<TaxCalculationId>,
        tenantId: TenantId
    )
}

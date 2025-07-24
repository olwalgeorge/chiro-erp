package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.Invoice
import com.chiroerp.finance.domain.valueobject.InvoiceId
import com.chiroerp.finance.domain.valueobject.AccountId
import com.chiroerp.finance.domain.valueobject.TenantId
import com.chiroerp.finance.domain.valueobject.Currency
import com.chiroerp.finance.domain.valueobject.Money
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Repository interface for Invoice entity data access operations.
 * 
 * Provides comprehensive invoice persistence capabilities including:
 * - Invoice lifecycle management from creation to payment
 * - Customer billing and payment tracking integration
 * - Complex querying by customer, status, amount, and date criteria
 * - Aging analysis and collections management support
 * - Tax calculation and regulatory compliance data access
 * - High-performance operations for billing automation
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure invoice consistency and regulatory compliance.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface InvoiceRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves an invoice entity to the persistent store with lifecycle tracking.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation, invoice consistency, and
     * maintains invoice lifecycle state across billing processes.
     * 
     * @param invoice The invoice entity to save
     * @return The saved invoice with updated metadata (version, timestamps, state)
     * @throws IllegalArgumentException if invoice is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws ConcurrencyException if optimistic locking conflict occurs
     * @throws InvoiceValidationException if invoice validation fails
     */
    suspend fun save(invoice: Invoice): Invoice
    
    /**
     * Finds an invoice by its unique identifier within a tenant context.
     * 
     * @param invoiceId The unique invoice identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The invoice if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(invoiceId: InvoiceId, tenantId: TenantId): Invoice?
    
    /**
     * Finds an invoice by its unique invoice number within a tenant context.
     * 
     * @param invoiceNumber The unique invoice number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The invoice if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByInvoiceNumber(
        invoiceNumber: String,
        tenantId: TenantId
    ): Invoice?
    
    /**
     * Finds an invoice by purchase order number within a tenant context.
     * 
     * @param purchaseOrderNumber The purchase order number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The invoice if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByPurchaseOrderNumber(
        purchaseOrderNumber: String,
        tenantId: TenantId
    ): Invoice?
    
    /**
     * Checks if an invoice exists by its identifier within a tenant context.
     * 
     * @param invoiceId The unique invoice identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if invoice exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(invoiceId: InvoiceId, tenantId: TenantId): Boolean
    
    /**
     * Checks if an invoice exists by its number within a tenant context.
     * 
     * @param invoiceNumber The unique invoice number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if invoice exists, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsByInvoiceNumber(
        invoiceNumber: String,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Deletes an invoice by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and regulatory compliance.
     * May be restricted based on invoice status and business rules.
     * 
     * @param invoiceId The unique invoice identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if invoice cannot be deleted
     */
    suspend fun deleteById(invoiceId: InvoiceId, tenantId: TenantId)
    
    // =====================================
    // Customer and Account Queries
    // =====================================
    
    /**
     * Finds all invoices for a specific customer account within a tenant context.
     * 
     * @param customerId The customer identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices ordered by invoice date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCustomer(
        customerId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices for multiple customers within a tenant context.
     * 
     * @param customerIds The list of customer identifiers to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices ordered by invoice date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCustomers(
        customerIds: List<String>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices by billing account within a tenant context.
     * 
     * @param accountId The billing account identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices for the billing account
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByBillingAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    // =====================================
    // Status and Lifecycle Queries
    // =====================================
    
    /**
     * Finds invoices by status within a tenant context.
     * 
     * @param status The invoice status to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices with the specified status
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByStatus(
        status: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds draft invoices within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of draft invoices ordered by creation date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findDraftInvoices(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds sent invoices within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of sent invoices ordered by send date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findSentInvoices(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds outstanding (unpaid) invoices within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of outstanding invoices ordered by due date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findOutstandingInvoices(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds paid invoices within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of paid invoices ordered by payment date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPaidInvoices(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds overdue invoices within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param asOfDate The date to determine overdue status (default: current date)
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of overdue invoices ordered by days overdue descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findOverdueInvoices(
        tenantId: TenantId,
        asOfDate: LocalDate = LocalDate.now(),
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds partially paid invoices within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of partially paid invoices ordered by last payment date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPartiallyPaidInvoices(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    // =====================================
    // Date-based Queries
    // =====================================
    
    /**
     * Finds invoices within a specific date range for a tenant.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByInvoiceDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices due within a specific date range.
     * 
     * @param startDate The start due date (inclusive)
     * @param endDate The end due date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices due within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByDueDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices created within a specific date range.
     * 
     * @param startDate The start creation date (inclusive)
     * @param endDate The end creation date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices created within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCreatedDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices due by a specific date.
     * 
     * @param dueDate The due date to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices due by the specified date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findInvoicesDueBy(
        dueDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    // =====================================
    // Amount-based Queries
    // =====================================
    
    /**
     * Finds invoices with total amount greater than the specified threshold.
     * 
     * @param minimumAmount The minimum amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices with amount above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMinimumAmount(
        minimumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices with total amount within the specified range.
     * 
     * @param minimumAmount The minimum amount threshold (inclusive)
     * @param maximumAmount The maximum amount threshold (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices with amount within range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAmountRange(
        minimumAmount: Money,
        maximumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices with outstanding balance greater than the specified threshold.
     * 
     * @param minimumBalance The minimum outstanding balance threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices with outstanding balance above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMinimumOutstandingBalance(
        minimumBalance: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    // =====================================
    // Currency and Tax Queries
    // =====================================
    
    /**
     * Finds invoices by currency within a tenant context.
     * 
     * @param currency The currency to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices with the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCurrency(
        currency: Currency,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices by tax jurisdiction within a tenant context.
     * 
     * @param taxJurisdiction The tax jurisdiction to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices with the specified tax jurisdiction
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTaxJurisdiction(
        taxJurisdiction: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    // =====================================
    // Aging and Collections Queries
    // =====================================
    
    /**
     * Finds invoices by aging bucket for collections management.
     * 
     * @param agingDays The number of days overdue to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices in the specified aging bucket
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAgingBucket(
        agingDays: Int,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    /**
     * Finds invoices for collections follow-up based on customer and aging.
     * 
     * @param customerId The customer identifier to filter by
     * @param minimumAgingDays The minimum aging days for collections
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of invoices requiring collections follow-up
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findForCollectionsFollowUp(
        customerId: String,
        minimumAgingDays: Int = 30,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Invoice>
    
    // =====================================
    // Aggregation and Reporting Queries
    // =====================================
    
    /**
     * Counts the total number of invoices for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of invoices
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts invoices by status for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of status to invoice count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByStatus(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts invoices by customer for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of customer ID to invoice count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByCustomer(tenantId: TenantId): Map<String, Long>
    
    /**
     * Calculates the total invoice value for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The total invoice value in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalInvoiceValue(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the total outstanding balance for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The total outstanding balance in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalOutstandingBalance(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the average invoice amount for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The average invoice amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAverageInvoiceAmount(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates aging analysis for outstanding invoices.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return Map of aging bucket (days) to outstanding amount
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAgingAnalysis(
        tenantId: TenantId,
        currency: Currency? = null
    ): Map<Int, Money>
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple invoices in a single batch operation with lifecycle consistency.
     * 
     * Provides better performance for bulk operations while maintaining
     * invoice consistency, lifecycle state integrity, and regulatory compliance.
     * 
     * @param invoices The list of invoices to save
     * @return The list of saved invoices with updated metadata
     * @throws IllegalArgumentException if invoices are invalid
     * @throws DataAccessException if batch operation fails
     * @throws ConcurrencyException if optimistic locking conflicts occur
     * @throws InvoiceValidationException if invoice validations fail
     */
    suspend fun saveAll(invoices: List<Invoice>): List<Invoice>
    
    /**
     * Finds multiple invoices by their identifiers within a tenant context.
     * 
     * @param invoiceIds The list of invoice identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found invoices (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        invoiceIds: List<InvoiceId>,
        tenantId: TenantId
    ): List<Invoice>
    
    /**
     * Updates invoice statuses in bulk for workflow processing.
     * 
     * @param invoiceIds The list of invoice identifiers to update
     * @param newStatus The new status to set
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if update operation fails
     * @throws BusinessRuleException if status transition is invalid
     */
    suspend fun updateStatusBulk(
        invoiceIds: List<InvoiceId>,
        newStatus: String,
        tenantId: TenantId
    )
    
    /**
     * Deletes multiple invoices by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and regulatory compliance.
     * May be restricted based on invoice statuses and business rules.
     * 
     * @param invoiceIds The list of invoice identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any invoice cannot be deleted
     */
    suspend fun deleteByIds(
        invoiceIds: List<InvoiceId>,
        tenantId: TenantId
    )
}

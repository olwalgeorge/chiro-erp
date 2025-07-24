package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.JournalEntry
import com.chiroerp.finance.domain.valueobject.JournalEntryId
import com.chiroerp.finance.domain.valueobject.AccountId
import com.chiroerp.finance.domain.valueobject.TenantId
import com.chiroerp.finance.domain.valueobject.TransactionId
import com.chiroerp.finance.domain.valueobject.Currency
import com.chiroerp.finance.domain.valueobject.Money
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Repository interface for JournalEntry entity data access operations.
 * 
 * Provides comprehensive journal entry persistence capabilities including:
 * - Double-entry bookkeeping system management and validation
 * - Complex querying by account, date, and financial criteria
 * - Transaction linkage and journal entry relationship tracking
 * - Multi-currency support with foreign exchange handling
 * - Audit trail and compliance data access for regulatory requirements
 * - High-performance operations for financial reporting and analysis
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure journal integrity and double-entry consistency.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface JournalEntryRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves a journal entry entity to the persistent store with double-entry validation.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation, double-entry consistency, and
     * maintains journal integrity across all financial accounting workflows.
     * 
     * @param journalEntry The journal entry entity to save
     * @return The saved journal entry with updated metadata (version, timestamps, status)
     * @throws IllegalArgumentException if journal entry is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws ConcurrencyException if optimistic locking conflict occurs
     * @throws JournalValidationException if double-entry validation fails
     */
    suspend fun save(journalEntry: JournalEntry): JournalEntry
    
    /**
     * Finds a journal entry by its unique identifier within a tenant context.
     * 
     * @param journalEntryId The unique journal entry identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The journal entry if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(journalEntryId: JournalEntryId, tenantId: TenantId): JournalEntry?
    
    /**
     * Finds a journal entry by its unique reference number within a tenant context.
     * 
     * @param journalReference The unique journal reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The journal entry if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByJournalReference(
        journalReference: String,
        tenantId: TenantId
    ): JournalEntry?
    
    /**
     * Checks if a journal entry exists by its identifier within a tenant context.
     * 
     * @param journalEntryId The unique journal entry identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if journal entry exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(journalEntryId: JournalEntryId, tenantId: TenantId): Boolean
    
    /**
     * Checks if a journal entry exists by its reference within a tenant context.
     * 
     * @param journalReference The unique journal reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if journal entry exists, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsByJournalReference(
        journalReference: String,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Deletes a journal entry by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and accounting history.
     * May be restricted based on posting status and business rules.
     * 
     * @param journalEntryId The unique journal entry identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if journal entry cannot be deleted
     */
    suspend fun deleteById(journalEntryId: JournalEntryId, tenantId: TenantId)
    
    // =====================================
    // Account-based Queries
    // =====================================
    
    /**
     * Finds all journal entries for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries ordered by posting date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries for multiple accounts within a tenant context.
     * 
     * @param accountIds The list of account identifiers to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries ordered by posting date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccounts(
        accountIds: List<AccountId>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds debit journal entries for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of debit journal entries ordered by posting date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findDebitEntriesByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds credit journal entries for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of credit journal entries ordered by posting date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findCreditEntriesByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    // =====================================
    // Transaction-based Queries
    // =====================================
    
    /**
     * Finds journal entries linked to a specific transaction within a tenant context.
     * 
     * @param transactionId The transaction identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries for the transaction
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTransaction(
        transactionId: TransactionId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries linked to multiple transactions within a tenant context.
     * 
     * @param transactionIds The list of transaction identifiers to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries for the transactions
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTransactions(
        transactionIds: List<TransactionId>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    // =====================================
    // Date and Period-based Queries
    // =====================================
    
    /**
     * Finds journal entries within a specific posting date range for a tenant.
     * 
     * @param startDate The start posting date (inclusive)
     * @param endDate The end posting date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByPostingDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries created within a specific date range.
     * 
     * @param startDate The start creation date (inclusive)
     * @param endDate The end creation date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries created within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCreatedDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries by period identifier within a tenant context.
     * 
     * @param period The accounting period identifier (e.g., "2025-01", "2025-Q1")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries for the specified accounting period
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccountingPeriod(
        period: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    // =====================================
    // Amount-based Queries
    // =====================================
    
    /**
     * Finds journal entries within a specific amount range for a tenant.
     * 
     * @param minAmount The minimum amount (inclusive)
     * @param maxAmount The maximum amount (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries within the amount range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAmountRange(
        minAmount: Money,
        maxAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries above a specific amount threshold.
     * 
     * @param amountThreshold The amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries above the threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findAboveAmount(
        amountThreshold: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    // =====================================
    // Status and Type Queries
    // =====================================
    
    /**
     * Finds journal entries by posting status within a tenant context.
     * 
     * @param postingStatus The posting status to filter by (e.g., "DRAFT", "POSTED", "REVERSED")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries with the specified posting status
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByPostingStatus(
        postingStatus: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries by entry type within a tenant context.
     * 
     * @param entryType The journal entry type to filter by (e.g., "MANUAL", "AUTOMATIC", "ADJUSTMENT")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries with the specified entry type
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByEntryType(
        entryType: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds draft (unposted) journal entries within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of draft journal entries ordered by creation date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findDraftEntries(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds posted journal entries within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of posted journal entries ordered by posting date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPostedEntries(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds reversed journal entries within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reversed journal entries ordered by reversal date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findReversedEntries(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries requiring approval within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries awaiting approval
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findRequiringApproval(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    // =====================================
    // Currency-based Queries
    // =====================================
    
    /**
     * Finds journal entries by currency within a tenant context.
     * 
     * @param currency The currency to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries with the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCurrency(
        currency: Currency,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds multi-currency journal entries within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries involving foreign exchange
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findMultiCurrencyEntries(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    // =====================================
    // Source and Reference Queries
    // =====================================
    
    /**
     * Finds journal entries by source system within a tenant context.
     * 
     * @param sourceSystem The source system identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries from the specified source system
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findBySourceSystem(
        sourceSystem: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries by external reference within a tenant context.
     * 
     * @param externalReference The external reference identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries with the specified external reference
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByExternalReference(
        externalReference: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    /**
     * Finds journal entries by description pattern within a tenant context.
     * 
     * @param descriptionPattern The description pattern to search for
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of journal entries matching the description pattern
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByDescriptionPattern(
        descriptionPattern: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    // =====================================
    // Aggregation and Reporting Queries
    // =====================================
    
    /**
     * Counts the total number of journal entries for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of journal entries
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts journal entries by posting status for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of posting status to journal entry count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByPostingStatus(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts journal entries by entry type for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of entry type to count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByEntryType(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts journal entries by account for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of account ID to journal entry count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByAccount(tenantId: TenantId): Map<AccountId, Long>
    
    /**
     * Calculates the total debits for a tenant in a specific period.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param currency The currency for calculation (optional)
     * @return The total debit amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalDebits(
        tenantId: TenantId,
        startDate: LocalDate,
        endDate: LocalDate,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the total credits for a tenant in a specific period.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param currency The currency for calculation (optional)
     * @return The total credit amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalCredits(
        tenantId: TenantId,
        startDate: LocalDate,
        endDate: LocalDate,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the trial balance for a tenant at a specific date.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param asOfDate The date for trial balance calculation
     * @param currency The currency for calculation (optional)
     * @return Map of account ID to balance amount
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTrialBalance(
        tenantId: TenantId,
        asOfDate: LocalDate,
        currency: Currency? = null
    ): Map<AccountId, Money>
    
    /**
     * Validates double-entry balance for a specific transaction.
     * 
     * @param transactionId The transaction identifier to validate
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if debits equal credits, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun validateDoubleEntryBalance(
        transactionId: TransactionId,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Finds unbalanced journal entries (where debits != credits) for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of unbalanced journal entries
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findUnbalancedEntries(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<JournalEntry>
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple journal entries in a single batch operation with double-entry validation.
     * 
     * Provides better performance for bulk operations while maintaining
     * double-entry consistency, journal integrity, and financial accuracy.
     * 
     * @param journalEntries The list of journal entries to save
     * @return The list of saved journal entries with updated metadata
     * @throws IllegalArgumentException if journal entries are invalid
     * @throws DataAccessException if batch operation fails
     * @throws ConcurrencyException if optimistic locking conflicts occur
     * @throws JournalValidationException if double-entry validations fail
     */
    suspend fun saveAll(journalEntries: List<JournalEntry>): List<JournalEntry>
    
    /**
     * Finds multiple journal entries by their identifiers within a tenant context.
     * 
     * @param journalEntryIds The list of journal entry identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found journal entries (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        journalEntryIds: List<JournalEntryId>,
        tenantId: TenantId
    ): List<JournalEntry>
    
    /**
     * Updates journal entry posting statuses in bulk for workflow processing.
     * 
     * @param journalEntryIds The list of journal entry identifiers to update
     * @param newPostingStatus The new posting status to set
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if update operation fails
     * @throws BusinessRuleException if status transition is invalid
     */
    suspend fun updatePostingStatusBulk(
        journalEntryIds: List<JournalEntryId>,
        newPostingStatus: String,
        tenantId: TenantId
    )
    
    /**
     * Posts multiple journal entries in bulk with double-entry validation.
     * 
     * @param journalEntryIds The list of journal entry identifiers to post
     * @param postingDate The posting date for the entries
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if posting operation fails
     * @throws JournalValidationException if double-entry validation fails
     * @throws BusinessRuleException if any entry cannot be posted
     */
    suspend fun postEntriesBulk(
        journalEntryIds: List<JournalEntryId>,
        postingDate: LocalDate,
        tenantId: TenantId
    )
    
    /**
     * Reverses multiple journal entries in bulk with audit trail.
     * 
     * @param journalEntryIds The list of journal entry identifiers to reverse
     * @param reversalDate The reversal date for the entries
     * @param reversalReason The reason for reversal
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if reversal operation fails
     * @throws BusinessRuleException if any entry cannot be reversed
     */
    suspend fun reverseEntriesBulk(
        journalEntryIds: List<JournalEntryId>,
        reversalDate: LocalDate,
        reversalReason: String,
        tenantId: TenantId
    )
    
    /**
     * Deletes multiple journal entries by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and accounting history.
     * May be restricted based on posting statuses and business rules.
     * 
     * @param journalEntryIds The list of journal entry identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any journal entry cannot be deleted
     */
    suspend fun deleteByIds(
        journalEntryIds: List<JournalEntryId>,
        tenantId: TenantId
    )
}

package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.Transaction
import com.chiroerp.finance.domain.valueobject.TransactionId
import com.chiroerp.finance.domain.valueobject.AccountId
import com.chiroerp.finance.domain.valueobject.TenantId
import com.chiroerp.finance.domain.valueobject.Currency
import com.chiroerp.finance.domain.valueobject.Money
import java.time.LocalDateTime

/**
 * Repository interface for Transaction entity data access operations.
 * 
 * Provides comprehensive transaction persistence capabilities including:
 * - ACID-compliant transaction operations with multi-tenant support
 * - Complex querying by account, date, amount, and business criteria
 * - Financial reporting and audit trail data access
 * - High-performance bulk operations for batch processing
 * - Transaction integrity and consistency enforcement
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure ACID compliance and maintain data consistency.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface TransactionRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves a transaction entity to the persistent store with ACID compliance.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation, data consistency, and
     * maintains transaction integrity across related entities.
     * 
     * @param transaction The transaction entity to save
     * @return The saved transaction with updated metadata (version, timestamps)
     * @throws IllegalArgumentException if transaction is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws ConcurrencyException if optimistic locking conflict occurs
     */
    suspend fun save(transaction: Transaction): Transaction
    
    /**
     * Finds a transaction by its unique identifier within a tenant context.
     * 
     * @param transactionId The unique transaction identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The transaction if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(transactionId: TransactionId, tenantId: TenantId): Transaction?
    
    /**
     * Finds a transaction by its unique transaction reference within a tenant context.
     * 
     * @param transactionReference The unique transaction reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The transaction if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTransactionReference(
        transactionReference: String,
        tenantId: TenantId
    ): Transaction?
    
    /**
     * Checks if a transaction exists by its identifier within a tenant context.
     * 
     * @param transactionId The unique transaction identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if transaction exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(transactionId: TransactionId, tenantId: TenantId): Boolean
    
    /**
     * Checks if a transaction exists by its reference within a tenant context.
     * 
     * @param transactionReference The unique transaction reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if transaction exists, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsByTransactionReference(
        transactionReference: String,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Deletes a transaction by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and referential integrity.
     * May be restricted based on business rules and transaction status.
     * 
     * @param transactionId The unique transaction identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if transaction cannot be deleted
     */
    suspend fun deleteById(transactionId: TransactionId, tenantId: TenantId)
    
    // =====================================
    // Account-based Queries
    // =====================================
    
    /**
     * Finds all transactions for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions ordered by transaction date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds transactions for multiple accounts within a tenant context.
     * 
     * @param accountIds The list of account identifiers to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions ordered by transaction date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccounts(
        accountIds: List<AccountId>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds debit transactions for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of debit transactions ordered by transaction date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findDebitTransactionsByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds credit transactions for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of credit transactions ordered by transaction date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findCreditTransactionsByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    // =====================================
    // Date-based Queries
    // =====================================
    
    /**
     * Finds transactions within a specific date range for a tenant.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds transactions within a specific date range for an account.
     * 
     * @param accountId The account identifier to filter by
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions within the date range for the account
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccountAndDateRange(
        accountId: AccountId,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds transactions processed within a specific date range.
     * 
     * @param startDate The start processing date (inclusive)
     * @param endDate The end processing date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions processed within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByProcessedDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    // =====================================
    // Amount-based Queries
    // =====================================
    
    /**
     * Finds transactions with amount greater than the specified threshold.
     * 
     * @param minimumAmount The minimum amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions with amount above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMinimumAmount(
        minimumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds transactions with amount less than the specified threshold.
     * 
     * @param maximumAmount The maximum amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions with amount below threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMaximumAmount(
        maximumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds transactions with amount within the specified range.
     * 
     * @param minimumAmount The minimum amount threshold (inclusive)
     * @param maximumAmount The maximum amount threshold (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions with amount within range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAmountRange(
        minimumAmount: Money,
        maximumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    // =====================================
    // Status and Type Queries
    // =====================================
    
    /**
     * Finds transactions by status within a tenant context.
     * 
     * @param status The transaction status to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions with the specified status
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByStatus(
        status: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds transactions by type within a tenant context.
     * 
     * @param transactionType The transaction type to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions with the specified type
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTransactionType(
        transactionType: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds pending transactions within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of pending transactions
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPendingTransactions(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    /**
     * Finds failed transactions within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of failed transactions
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findFailedTransactions(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    // =====================================
    // Currency-based Queries
    // =====================================
    
    /**
     * Finds transactions by currency within a tenant context.
     * 
     * @param currency The currency to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of transactions with the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCurrency(
        currency: Currency,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Transaction>
    
    // =====================================
    // Aggregation and Reporting Queries
    // =====================================
    
    /**
     * Counts the total number of transactions for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of transactions
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts transactions by account for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of account ID to transaction count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByAccount(tenantId: TenantId): Map<AccountId, Long>
    
    /**
     * Counts transactions by status for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of status to transaction count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByStatus(tenantId: TenantId): Map<String, Long>
    
    /**
     * Calculates the total transaction volume for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for volume calculation (optional)
     * @return The total transaction volume in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalVolume(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the total transaction volume for an account.
     * 
     * @param accountId The account identifier to calculate for
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for volume calculation (optional)
     * @return The total transaction volume for the account
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAccountVolume(
        accountId: AccountId,
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the average transaction amount for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The average transaction amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAverageAmount(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple transactions in a single batch operation with ACID compliance.
     * 
     * Provides better performance for bulk operations while maintaining
     * data consistency, transaction integrity, and ACID properties.
     * 
     * @param transactions The list of transactions to save
     * @return The list of saved transactions with updated metadata
     * @throws IllegalArgumentException if transactions are invalid
     * @throws DataAccessException if batch operation fails
     * @throws ConcurrencyException if optimistic locking conflicts occur
     */
    suspend fun saveAll(transactions: List<Transaction>): List<Transaction>
    
    /**
     * Finds multiple transactions by their identifiers within a tenant context.
     * 
     * @param transactionIds The list of transaction identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found transactions (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        transactionIds: List<TransactionId>,
        tenantId: TenantId
    ): List<Transaction>
    
    /**
     * Deletes multiple transactions by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and referential integrity.
     * May be restricted based on business rules and transaction statuses.
     * 
     * @param transactionIds The list of transaction identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any transaction cannot be deleted
     */
    suspend fun deleteByIds(
        transactionIds: List<TransactionId>,
        tenantId: TenantId
    )
}

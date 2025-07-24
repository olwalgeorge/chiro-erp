package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.Account
import com.chiroerp.finance.domain.valueobject.AccountId
import com.chiroerp.finance.domain.valueobject.TenantId
import com.chiroerp.finance.domain.valueobject.Currency
import com.chiroerp.finance.domain.valueobject.Money
import java.time.LocalDateTime

/**
 * Repository interface for Account entity data access operations.
 * 
 * Provides comprehensive account persistence capabilities including:
 * - Basic CRUD operations with multi-tenant support
 * - Complex querying by various business criteria
 * - Balance tracking and financial reporting queries
 * - Audit trail and compliance data access
 * - Performance-optimized bulk operations
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface AccountRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves an account entity to the persistent store.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation and data consistency.
     * 
     * @param account The account entity to save
     * @return The saved account with updated metadata (version, timestamps)
     * @throws IllegalArgumentException if account is invalid
     * @throws DataAccessException if persistence operation fails
     */
    suspend fun save(account: Account): Account
    
    /**
     * Finds an account by its unique identifier within a tenant context.
     * 
     * @param accountId The unique account identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The account if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(accountId: AccountId, tenantId: TenantId): Account?
    
    /**
     * Finds an account by its unique account number within a tenant context.
     * 
     * @param accountNumber The unique account number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The account if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccountNumber(accountNumber: String, tenantId: TenantId): Account?
    
    /**
     * Checks if an account exists by its identifier within a tenant context.
     * 
     * @param accountId The unique account identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if account exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(accountId: AccountId, tenantId: TenantId): Boolean
    
    /**
     * Deletes an account by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and referential integrity.
     * 
     * @param accountId The unique account identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if account has dependent transactions
     */
    suspend fun deleteById(accountId: AccountId, tenantId: TenantId)
    
    // =====================================
    // Business Query Operations
    // =====================================
    
    /**
     * Finds all accounts for a specific tenant with optional pagination.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of accounts ordered by account number
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTenant(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    /**
     * Finds accounts by account type within a tenant context.
     * 
     * @param accountType The account type to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of accounts matching the type criteria
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccountType(
        accountType: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    /**
     * Finds active accounts within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of active accounts
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findActiveAccounts(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    /**
     * Finds accounts by currency within a tenant context.
     * 
     * @param currency The currency to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of accounts with the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCurrency(
        currency: Currency,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    // =====================================
    // Balance and Financial Queries
    // =====================================
    
    /**
     * Finds accounts with balance greater than the specified amount.
     * 
     * @param minimumBalance The minimum balance threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of accounts with balance above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMinimumBalance(
        minimumBalance: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    /**
     * Finds accounts with balance less than the specified amount.
     * 
     * @param maximumBalance The maximum balance threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of accounts with balance below threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMaximumBalance(
        maximumBalance: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    /**
     * Finds accounts with balance within the specified range.
     * 
     * @param minimumBalance The minimum balance threshold (inclusive)
     * @param maximumBalance The maximum balance threshold (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of accounts with balance within range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByBalanceRange(
        minimumBalance: Money,
        maximumBalance: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    // =====================================
    // Date-based Queries
    // =====================================
    
    /**
     * Finds accounts created within a specific date range.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of accounts created within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCreatedDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    /**
     * Finds accounts modified within a specific date range.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of accounts modified within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByModifiedDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Account>
    
    // =====================================
    // Aggregation and Reporting Queries
    // =====================================
    
    /**
     * Counts the total number of accounts for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of accounts
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts accounts by account type for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of account type to count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByAccountType(tenantId: TenantId): Map<String, Long>
    
    /**
     * Calculates the total balance across all accounts for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for balance calculation (optional)
     * @return The total balance in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalBalance(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the average balance across all accounts for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for balance calculation (optional)
     * @return The average balance in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAverageBalance(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple accounts in a single batch operation.
     * 
     * Provides better performance for bulk operations while maintaining
     * data consistency and transaction integrity.
     * 
     * @param accounts The list of accounts to save
     * @return The list of saved accounts with updated metadata
     * @throws IllegalArgumentException if accounts are invalid
     * @throws DataAccessException if batch operation fails
     */
    suspend fun saveAll(accounts: List<Account>): List<Account>
    
    /**
     * Finds multiple accounts by their identifiers within a tenant context.
     * 
     * @param accountIds The list of account identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found accounts (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        accountIds: List<AccountId>,
        tenantId: TenantId
    ): List<Account>
    
    /**
     * Deletes multiple accounts by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and referential integrity.
     * 
     * @param accountIds The list of account identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any account has dependent transactions
     */
    suspend fun deleteByIds(
        accountIds: List<AccountId>,
        tenantId: TenantId
    )
}

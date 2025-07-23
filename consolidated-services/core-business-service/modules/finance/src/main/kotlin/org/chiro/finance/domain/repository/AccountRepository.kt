package org.chiro.finance.domain.repository

import org.chiro.finance.domain.entity.Account
import org.chiro.finance.domain.valueobject.AccountId
import org.chiro.finance.domain.valueobject.AccountType
import org.chiro.finance.domain.valueobject.AccountCategory
import org.chiro.finance.domain.valueobject.Currency
import java.util.*

/**
 * Account Repository Interface - Domain Layer Contract
 * 
 * Defines the persistence contract for Account aggregate following
 * Repository pattern and Domain-Driven Design principles.
 * 
 * Features:
 * - Strongly typed queries using domain value objects
 * - Chart of accounts specific operations
 * - Hierarchical account queries
 * - Performance-optimized bulk operations
 * - Multi-currency support
 * - Advanced filtering and search capabilities
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
interface AccountRepository {
    
    // ===================== BASIC CRUD OPERATIONS =====================
    
    /**
     * Saves or updates an account
     */
    suspend fun save(account: Account): Account
    
    /**
     * Saves multiple accounts in a single transaction
     */
    suspend fun saveAll(accounts: List<Account>): List<Account>
    
    /**
     * Finds account by ID
     */
    suspend fun findById(id: AccountId): Account?
    
    /**
     * Finds account by unique account code
     */
    suspend fun findByAccountCode(accountCode: String): Account?
    
    /**
     * Checks if account exists by ID
     */
    suspend fun existsById(id: AccountId): Boolean
    
    /**
     * Checks if account code is already in use
     */
    suspend fun existsByAccountCode(accountCode: String): Boolean
    
    /**
     * Deletes an account (soft delete - marks as inactive)
     */
    suspend fun delete(account: Account)
    
    /**
     * Hard delete (use with extreme caution)
     */
    suspend fun hardDelete(id: AccountId)
    
    // ===================== CHART OF ACCOUNTS QUERIES =====================
    
    /**
     * Gets all accounts ordered by account code
     */
    suspend fun findAllOrderByAccountCode(): List<Account>
    
    /**
     * Gets all active accounts
     */
    suspend fun findAllActive(): List<Account>
    
    /**
     * Gets accounts by category
     */
    suspend fun findByAccountCategory(category: AccountCategory): List<Account>
    
    /**
     * Gets accounts by type
     */
    suspend fun findByAccountType(accountType: AccountType): List<Account>
    
    /**
     * Gets accounts by multiple types
     */
    suspend fun findByAccountTypeIn(accountTypes: Set<AccountType>): List<Account>
    
    /**
     * Gets accounts by currency
     */
    suspend fun findByCurrency(currency: Currency): List<Account>
    
    /**
     * Gets all control accounts
     */
    suspend fun findAllControlAccounts(): List<Account>
    
    /**
     * Gets all system accounts
     */
    suspend fun findAllSystemAccounts(): List<Account>
    
    // ===================== HIERARCHICAL QUERIES =====================
    
    /**
     * Gets all root accounts (no parent)
     */
    suspend fun findAllRootAccounts(): List<Account>
    
    /**
     * Gets all child accounts of a parent
     */
    suspend fun findByParentAccount(parentId: AccountId): List<Account>
    
    /**
     * Gets all descendants of an account (children, grandchildren, etc.)
     */
    suspend fun findAllDescendants(parentId: AccountId): List<Account>
    
    /**
     * Gets all leaf accounts (no children)
     */
    suspend fun findAllLeafAccounts(): List<Account>
    
    /**
     * Gets account hierarchy as tree structure
     */
    suspend fun findAccountHierarchy(): List<Account>
    
    /**
     * Gets account hierarchy for specific category
     */
    suspend fun findAccountHierarchyByCategory(category: AccountCategory): List<Account>
    
    // ===================== SEARCH AND FILTERING =====================
    
    /**
     * Searches accounts by name (case-insensitive, partial match)
     */
    suspend fun searchByAccountName(namePattern: String): List<Account>
    
    /**
     * Searches accounts by code pattern
     */
    suspend fun searchByAccountCodePattern(codePattern: String): List<Account>
    
    /**
     * Advanced search with multiple criteria
     */
    suspend fun searchAccounts(criteria: AccountSearchCriteria): List<Account>
    
    /**
     * Gets accounts with non-zero balances
     */
    suspend fun findAccountsWithNonZeroBalance(): List<Account>
    
    /**
     * Gets accounts requiring reconciliation
     */
    suspend fun findAccountsRequiringReconciliation(): List<Account>
    
    // ===================== REPORTING QUERIES =====================
    
    /**
     * Gets trial balance data (all accounts with balances)
     */
    suspend fun getTrialBalanceData(): List<TrialBalanceItem>
    
    /**
     * Gets balance sheet accounts
     */
    suspend fun getBalanceSheetAccounts(): List<Account>
    
    /**
     * Gets income statement accounts
     */
    suspend fun getIncomeStatementAccounts(): List<Account>
    
    /**
     * Gets accounts summary by category
     */
    suspend fun getAccountSummaryByCategory(): List<AccountCategorySummary>
    
    // ===================== VALIDATION QUERIES =====================
    
    /**
     * Validates account code uniqueness
     */
    suspend fun isAccountCodeUnique(accountCode: String, excludeId: AccountId? = null): Boolean
    
    /**
     * Gets next available account code for a given type
     */
    suspend fun getNextAccountCode(accountType: AccountType): String
    
    /**
     * Validates account hierarchy consistency
     */
    suspend fun validateAccountHierarchy(): List<String>
    
    // ===================== PERFORMANCE QUERIES =====================
    
    /**
     * Gets account count by category
     */
    suspend fun countByCategory(category: AccountCategory): Long
    
    /**
     * Gets total count of active accounts
     */
    suspend fun countActiveAccounts(): Long
    
    /**
     * Checks if any accounts exist for given criteria
     */
    suspend fun existsByAccountType(accountType: AccountType): Boolean
}

/**
 * Account Search Criteria for advanced queries
 */
data class AccountSearchCriteria(
    val accountCode: String? = null,
    val accountName: String? = null,
    val accountTypes: Set<AccountType>? = null,
    val accountCategories: Set<AccountCategory>? = null,
    val currencies: Set<Currency>? = null,
    val isActive: Boolean? = null,
    val isControlAccount: Boolean? = null,
    val isSystemAccount: Boolean? = null,
    val hasParent: Boolean? = null,
    val hasChildren: Boolean? = null,
    val hasNonZeroBalance: Boolean? = null,
    val requiresReconciliation: Boolean? = null,
    val createdBy: String? = null,
    val parentAccountId: AccountId? = null,
    val excludeAccountIds: Set<AccountId>? = null
)

/**
 * Trial Balance Item for reporting
 */
data class TrialBalanceItem(
    val accountId: AccountId,
    val accountCode: String,
    val accountName: String,
    val accountType: AccountType,
    val accountCategory: AccountCategory,
    val debitBalance: org.chiro.finance.domain.valueobject.Money,
    val creditBalance: org.chiro.finance.domain.valueobject.Money,
    val netBalance: org.chiro.finance.domain.valueobject.Money
)

/**
 * Account Category Summary for reporting
 */
data class AccountCategorySummary(
    val category: AccountCategory,
    val totalAccounts: Long,
    val activeAccounts: Long,
    val totalBalance: org.chiro.finance.domain.valueobject.Money,
    val accountCount: Map<AccountType, Long>
)

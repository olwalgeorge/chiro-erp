package org.chiro.finance.domain.repository

import org.chiro.finance.domain.entity.Transaction
import org.chiro.finance.domain.entity.TransactionLine
import org.chiro.finance.domain.valueobject.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Journal Entry Repository Interface
 * 
 * Defines the contract for persisting and retrieving journal entries (transactions)
 * in the double-entry bookkeeping system. This repository handles complex queries
 * for financial reporting, audit trails, and transaction analysis.
 * 
 * Domain Repository Pattern: Encapsulates the logic needed to access data sources.
 * The repository centralizes common data access functionality.
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
interface JournalEntryRepository {
    
    // ==================== BASIC CRUD OPERATIONS ====================
    
    /**
     * Saves a journal entry (transaction) to the repository
     */
    suspend fun save(transaction: Transaction): Transaction
    
    /**
     * Saves multiple journal entries in a batch operation
     */
    suspend fun saveAll(transactions: List<Transaction>): List<Transaction>
    
    /**
     * Finds a journal entry by its unique identifier
     */
    suspend fun findById(id: JournalEntryId): Transaction?
    
    /**
     * Finds journal entries by their IDs in batch
     */
    suspend fun findByIds(ids: List<JournalEntryId>): List<Transaction>
    
    /**
     * Finds a journal entry by reference number
     */
    suspend fun findByReferenceNumber(referenceNumber: String): Transaction?
    
    /**
     * Deletes a journal entry (soft delete for audit purposes)
     */
    suspend fun delete(id: JournalEntryId): Boolean
    
    /**
     * Checks if a journal entry exists
     */
    suspend fun exists(id: JournalEntryId): Boolean
    
    /**
     * Gets the total count of journal entries
     */
    suspend fun count(): Long
    
    // ==================== QUERY OPERATIONS ====================
    
    /**
     * Finds all journal entries with pagination
     */
    suspend fun findAll(
        page: Int = 0,
        size: Int = 50,
        sortBy: String = "transactionDate",
        sortDirection: SortDirection = SortDirection.DESC
    ): Page<Transaction>
    
    /**
     * Finds journal entries by date range
     */
    suspend fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        includeReversed: Boolean = true,
        page: Int = 0,
        size: Int = 100
    ): Page<Transaction>
    
    /**
     * Finds journal entries by transaction type
     */
    suspend fun findByTransactionType(
        transactionType: TransactionType,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries by status
     */
    suspend fun findByStatus(
        status: TransactionStatus,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries affecting a specific account
     */
    suspend fun findByAccount(
        accountId: AccountId,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 100
    ): Page<Transaction>
    
    /**
     * Finds journal entries by accounting period
     */
    suspend fun findByAccountingPeriod(
        period: AccountingPeriod,
        includeReversed: Boolean = true,
        page: Int = 0,
        size: Int = 100
    ): Page<Transaction>
    
    /**
     * Finds journal entries by fiscal period status
     */
    suspend fun findByFiscalPeriodStatus(
        status: FiscalPeriodStatus,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries by amount range
     */
    suspend fun findByAmountRange(
        minAmount: FinancialAmount,
        maxAmount: FinancialAmount,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries by multiple accounts
     */
    suspend fun findByAccounts(
        accountIds: List<AccountId>,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 100
    ): Page<Transaction>
    
    /**
     * Finds journal entries by reference pattern (wildcard search)
     */
    suspend fun findByReferencePattern(
        pattern: String,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries by description pattern
     */
    suspend fun findByDescriptionPattern(
        pattern: String,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries created by specific user
     */
    suspend fun findByCreatedBy(
        createdBy: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries that are reversals
     */
    suspend fun findReversals(
        originalTransactionId: JournalEntryId? = null,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries that need approval
     */
    suspend fun findPendingApproval(
        approverRole: String? = null,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries by source system
     */
    suspend fun findBySourceSystem(
        sourceSystem: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    // ==================== ADVANCED SEARCH OPERATIONS ====================
    
    /**
     * Advanced search with multiple criteria
     */
    suspend fun findByCriteria(
        criteria: JournalEntrySearchCriteria,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Full-text search across transaction descriptions and references
     */
    suspend fun searchByText(
        searchText: String,
        searchFields: List<String> = listOf("description", "referenceNumber"),
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds related journal entries (same batch, same source document)
     */
    suspend fun findRelated(
        transactionId: JournalEntryId,
        relationType: RelationType = RelationType.ALL
    ): List<Transaction>
    
    /**
     * Finds journal entries with specific tags
     */
    suspend fun findByTags(
        tags: List<String>,
        matchAll: Boolean = false,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    // ==================== BALANCE AND REPORTING OPERATIONS ====================
    
    /**
     * Calculates account balance for a specific date
     */
    suspend fun calculateAccountBalance(
        accountId: AccountId,
        asOfDate: LocalDate = LocalDate.now(),
        includeUnposted: Boolean = false
    ): FinancialAmount
    
    /**
     * Calculates account balances for multiple accounts
     */
    suspend fun calculateAccountBalances(
        accountIds: List<AccountId>,
        asOfDate: LocalDate = LocalDate.now(),
        includeUnposted: Boolean = false
    ): Map<AccountId, FinancialAmount>
    
    /**
     * Gets account activity for a date range
     */
    suspend fun getAccountActivity(
        accountId: AccountId,
        startDate: LocalDate,
        endDate: LocalDate,
        includeUnposted: Boolean = false
    ): List<AccountActivity>
    
    /**
     * Gets trial balance data for all accounts
     */
    suspend fun getTrialBalanceData(
        asOfDate: LocalDate = LocalDate.now(),
        includeZeroBalances: Boolean = false,
        currency: Currency? = null
    ): List<TrialBalanceEntry>
    
    /**
     * Gets general ledger entries for an account
     */
    suspend fun getGeneralLedgerEntries(
        accountId: AccountId,
        startDate: LocalDate,
        endDate: LocalDate,
        includeUnposted: Boolean = false,
        page: Int = 0,
        size: Int = 100
    ): Page<GeneralLedgerEntry>
    
    /**
     * Gets account summary with beginning balance, activity, and ending balance
     */
    suspend fun getAccountSummary(
        accountId: AccountId,
        startDate: LocalDate,
        endDate: LocalDate
    ): AccountSummary
    
    /**
     * Gets monthly account balances for a year
     */
    suspend fun getMonthlyBalances(
        accountId: AccountId,
        year: Int,
        currency: Currency? = null
    ): List<MonthlyBalance>
    
    /**
     * Gets daily balances for an account over a date range
     */
    suspend fun getDailyBalances(
        accountId: AccountId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyBalance>
    
    // ==================== TRANSACTION LINE OPERATIONS ====================
    
    /**
     * Finds transaction lines by account
     */
    suspend fun findTransactionLinesByAccount(
        accountId: AccountId,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 100
    ): Page<TransactionLine>
    
    /**
     * Finds transaction lines by amount range
     */
    suspend fun findTransactionLinesByAmountRange(
        minAmount: FinancialAmount,
        maxAmount: FinancialAmount,
        page: Int = 0,
        size: Int = 50
    ): Page<TransactionLine>
    
    /**
     * Gets the largest transaction lines for an account
     */
    suspend fun getLargestTransactionLines(
        accountId: AccountId,
        limit: Int = 10,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): List<TransactionLine>
    
    /**
     * Gets transaction line details for a specific transaction
     */
    suspend fun getTransactionLines(transactionId: JournalEntryId): List<TransactionLine>
    
    // ==================== AUDIT AND COMPLIANCE OPERATIONS ====================
    
    /**
     * Finds journal entries modified after a specific date
     */
    suspend fun findModifiedAfter(
        modifiedAfter: LocalDateTime,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Gets audit trail for a specific journal entry
     */
    suspend fun getAuditTrail(transactionId: JournalEntryId): List<AuditLogEntry>
    
    /**
     * Finds journal entries that have been reversed
     */
    suspend fun findReversedTransactions(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Finds journal entries with approval workflow history
     */
    suspend fun findWithApprovalHistory(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Gets compliance report data
     */
    suspend fun getComplianceReportData(
        startDate: LocalDate,
        endDate: LocalDate,
        reportType: ComplianceReportType
    ): List<ComplianceReportEntry>
    
    /**
     * Finds journal entries that may have compliance issues
     */
    suspend fun findPotentialComplianceIssues(
        checkTypes: List<ComplianceCheckType> = ComplianceCheckType.values().toList(),
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    // ==================== BATCH OPERATIONS ====================
    
    /**
     * Posts multiple journal entries in a batch
     */
    suspend fun batchPost(
        transactionIds: List<JournalEntryId>,
        postedBy: String,
        postingDate: LocalDate = LocalDate.now()
    ): BatchOperationResult
    
    /**
     * Reverses multiple journal entries in a batch
     */
    suspend fun batchReverse(
        transactionIds: List<JournalEntryId>,
        reversalReason: String,
        reversedBy: String,
        reversalDate: LocalDate = LocalDate.now()
    ): BatchOperationResult
    
    /**
     * Updates multiple journal entries in a batch
     */
    suspend fun batchUpdate(
        updates: List<JournalEntryUpdate>,
        updatedBy: String
    ): BatchOperationResult
    
    /**
     * Deletes multiple journal entries in a batch (soft delete)
     */
    suspend fun batchDelete(
        transactionIds: List<JournalEntryId>,
        deletedBy: String,
        deletionReason: String
    ): BatchOperationResult
    
    // ==================== STATISTICS AND ANALYTICS ====================
    
    /**
     * Gets transaction volume statistics
     */
    suspend fun getTransactionVolumeStats(
        startDate: LocalDate,
        endDate: LocalDate,
        groupBy: StatisticsGroupBy = StatisticsGroupBy.DAY
    ): List<TransactionVolumeStats>
    
    /**
     * Gets transaction amount statistics
     */
    suspend fun getTransactionAmountStats(
        startDate: LocalDate,
        endDate: LocalDate,
        currency: Currency? = null
    ): TransactionAmountStats
    
    /**
     * Gets account activity statistics
     */
    suspend fun getAccountActivityStats(
        accountIds: List<AccountId>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AccountActivityStats>
    
    /**
     * Gets user activity statistics
     */
    suspend fun getUserActivityStats(
        startDate: LocalDate,
        endDate: LocalDate,
        userRole: String? = null
    ): List<UserActivityStats>
    
    /**
     * Gets transaction type distribution
     */
    suspend fun getTransactionTypeDistribution(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TransactionTypeStats>
    
    /**
     * Gets monthly transaction trends
     */
    suspend fun getMonthlyTrends(
        year: Int,
        transactionTypes: List<TransactionType>? = null
    ): List<MonthlyTrendStats>
    
    // ==================== PERFORMANCE OPERATIONS ====================
    
    /**
     * Optimizes database indexes for better query performance
     */
    suspend fun optimizeIndexes(): IndexOptimizationResult
    
    /**
     * Archives old journal entries to improve performance
     */
    suspend fun archiveOldEntries(
        archiveBefore: LocalDate,
        dryRun: Boolean = true
    ): ArchiveOperationResult
    
    /**
     * Rebuilds account balances from transaction history
     */
    suspend fun rebuildAccountBalances(
        accountIds: List<AccountId>? = null,
        fromDate: LocalDate? = null
    ): RebuildOperationResult
    
    /**
     * Validates data integrity across all journal entries
     */
    suspend fun validateDataIntegrity(): DataIntegrityReport
    
    // ==================== CURRENCY OPERATIONS ====================
    
    /**
     * Finds journal entries by currency
     */
    suspend fun findByCurrency(
        currency: Currency,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        page: Int = 0,
        size: Int = 50
    ): Page<Transaction>
    
    /**
     * Gets currency exposure report
     */
    suspend fun getCurrencyExposure(
        asOfDate: LocalDate = LocalDate.now(),
        baseCurrency: Currency = Currency.USD
    ): List<CurrencyExposureEntry>
    
    /**
     * Converts transaction amounts to a target currency
     */
    suspend fun convertToTargetCurrency(
        transactionIds: List<JournalEntryId>,
        targetCurrency: Currency,
        exchangeRateDate: LocalDate = LocalDate.now()
    ): List<CurrencyConversionResult>
}

// ==================== SUPPORTING DATA CLASSES ====================

/**
 * Page wrapper for paginated results
 */
data class Page<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

/**
 * Sort direction enum
 */
enum class SortDirection {
    ASC, DESC
}

/**
 * Transaction status enum
 */
enum class TransactionStatus {
    DRAFT, PENDING_APPROVAL, APPROVED, POSTED, REVERSED, CANCELLED
}

/**
 * Journal entry search criteria
 */
data class JournalEntrySearchCriteria(
    val referenceNumber: String? = null,
    val description: String? = null,
    val transactionType: TransactionType? = null,
    val status: TransactionStatus? = null,
    val accountIds: List<AccountId>? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minAmount: FinancialAmount? = null,
    val maxAmount: FinancialAmount? = null,
    val createdBy: String? = null,
    val currency: Currency? = null,
    val tags: List<String>? = null,
    val sourceSystem: String? = null,
    val hasApprovalWorkflow: Boolean? = null,
    val isReversed: Boolean? = null,
    val customFields: Map<String, Any>? = null
)

/**
 * Relation type for finding related transactions
 */
enum class RelationType {
    SAME_BATCH, SAME_SOURCE_DOCUMENT, REVERSALS, ALL
}

/**
 * Account activity entry
 */
data class AccountActivity(
    val transactionId: JournalEntryId,
    val transactionDate: LocalDate,
    val referenceNumber: String,
    val description: String,
    val debitAmount: FinancialAmount?,
    val creditAmount: FinancialAmount?,
    val runningBalance: FinancialAmount,
    val transactionType: TransactionType
)

/**
 * Trial balance entry
 */
data class TrialBalanceEntry(
    val accountId: AccountId,
    val accountCode: String,
    val accountName: String,
    val accountType: AccountType,
    val debitAmount: FinancialAmount,
    val creditAmount: FinancialAmount,
    val balance: FinancialAmount
)

/**
 * General ledger entry
 */
data class GeneralLedgerEntry(
    val transactionId: JournalEntryId,
    val transactionDate: LocalDate,
    val accountId: AccountId,
    val accountCode: String,
    val accountName: String,
    val referenceNumber: String,
    val description: String,
    val debitAmount: FinancialAmount?,
    val creditAmount: FinancialAmount?,
    val runningBalance: FinancialAmount
)

/**
 * Account summary
 */
data class AccountSummary(
    val accountId: AccountId,
    val accountCode: String,
    val accountName: String,
    val beginningBalance: FinancialAmount,
    val totalDebits: FinancialAmount,
    val totalCredits: FinancialAmount,
    val endingBalance: FinancialAmount,
    val transactionCount: Int,
    val lastTransactionDate: LocalDate?
)

/**
 * Monthly balance
 */
data class MonthlyBalance(
    val accountId: AccountId,
    val year: Int,
    val month: Int,
    val endingBalance: FinancialAmount,
    val currency: Currency
)

/**
 * Daily balance
 */
data class DailyBalance(
    val accountId: AccountId,
    val date: LocalDate,
    val endingBalance: FinancialAmount,
    val currency: Currency
)

/**
 * Audit log entry
 */
data class AuditLogEntry(
    val id: UUID,
    val transactionId: JournalEntryId,
    val action: String,
    val oldValue: String?,
    val newValue: String?,
    val changedBy: String,
    val changedAt: LocalDateTime,
    val reason: String?
)

/**
 * Compliance report type
 */
enum class ComplianceReportType {
    SOX_404, AUDIT_TRAIL, SEGREGATION_OF_DUTIES, AUTHORIZATION_LIMITS
}

/**
 * Compliance check type
 */
enum class ComplianceCheckType {
    MISSING_APPROVAL, UNAUTHORIZED_CHANGES, DUPLICATE_REFERENCE, 
    UNUSUAL_AMOUNTS, BACKDATED_ENTRIES, WEEKEND_ENTRIES
}

/**
 * Compliance report entry
 */
data class ComplianceReportEntry(
    val transactionId: JournalEntryId,
    val issueType: ComplianceCheckType,
    val description: String,
    val severity: ComplianceSeverity,
    val recommendedAction: String
)

/**
 * Compliance severity
 */
enum class ComplianceSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Journal entry update
 */
data class JournalEntryUpdate(
    val transactionId: JournalEntryId,
    val updates: Map<String, Any>,
    val updateReason: String
)

/**
 * Batch operation result
 */
data class BatchOperationResult(
    val success: Boolean,
    val processedCount: Int,
    val failedCount: Int,
    val errors: List<String>,
    val warnings: List<String>,
    val processedIds: List<JournalEntryId>
)

/**
 * Statistics group by
 */
enum class StatisticsGroupBy {
    DAY, WEEK, MONTH, QUARTER, YEAR
}

/**
 * Transaction volume stats
 */
data class TransactionVolumeStats(
    val period: String,
    val transactionCount: Int,
    val totalAmount: FinancialAmount,
    val averageAmount: FinancialAmount
)

/**
 * Transaction amount stats
 */
data class TransactionAmountStats(
    val totalAmount: FinancialAmount,
    val averageAmount: FinancialAmount,
    val minimumAmount: FinancialAmount,
    val maximumAmount: FinancialAmount,
    val transactionCount: Int,
    val currency: Currency
)

/**
 * Account activity stats
 */
data class AccountActivityStats(
    val accountId: AccountId,
    val accountCode: String,
    val transactionCount: Int,
    val totalDebits: FinancialAmount,
    val totalCredits: FinancialAmount,
    val averageTransactionAmount: FinancialAmount
)

/**
 * User activity stats
 */
data class UserActivityStats(
    val username: String,
    val transactionCount: Int,
    val totalAmount: FinancialAmount,
    val lastActivity: LocalDateTime
)

/**
 * Transaction type stats
 */
data class TransactionTypeStats(
    val transactionType: TransactionType,
    val count: Int,
    val percentage: Double,
    val totalAmount: FinancialAmount
)

/**
 * Monthly trend stats
 */
data class MonthlyTrendStats(
    val year: Int,
    val month: Int,
    val transactionCount: Int,
    val totalAmount: FinancialAmount,
    val averageAmount: FinancialAmount,
    val growthRate: Double
)

/**
 * Index optimization result
 */
data class IndexOptimizationResult(
    val success: Boolean,
    val optimizedIndexes: List<String>,
    val improvementEstimate: String,
    val errors: List<String>
)

/**
 * Archive operation result
 */
data class ArchiveOperationResult(
    val success: Boolean,
    val archivedCount: Int,
    val archiveLocation: String,
    val spaceSaved: String,
    val errors: List<String>
)

/**
 * Rebuild operation result
 */
data class RebuildOperationResult(
    val success: Boolean,
    val accountsProcessed: Int,
    val balancesRebuilt: Int,
    val discrepanciesFound: Int,
    val errors: List<String>
)

/**
 * Data integrity report
 */
data class DataIntegrityReport(
    val totalTransactions: Long,
    val validTransactions: Long,
    val invalidTransactions: Long,
    val issues: List<DataIntegrityIssue>,
    val recommendedActions: List<String>
)

/**
 * Data integrity issue
 */
data class DataIntegrityIssue(
    val transactionId: JournalEntryId,
    val issueType: String,
    val description: String,
    val severity: ComplianceSeverity
)

/**
 * Currency exposure entry
 */
data class CurrencyExposureEntry(
    val currency: Currency,
    val totalAmount: FinancialAmount,
    val accountCount: Int,
    val percentageOfTotal: Double
)

/**
 * Currency conversion result
 */
data class CurrencyConversionResult(
    val transactionId: JournalEntryId,
    val originalAmount: FinancialAmount,
    val convertedAmount: FinancialAmount,
    val exchangeRate: FinancialAmount,
    val conversionDate: LocalDate
)

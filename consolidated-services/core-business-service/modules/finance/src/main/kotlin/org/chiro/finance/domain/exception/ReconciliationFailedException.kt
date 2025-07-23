package org.chiro.finance.domain.exception

import java.math.BigDecimal
import java.util.*

/**
 * ReconciliationFailedException
 * 
 * Domain exception thrown when financial reconciliation processes fail
 * to match expected balances, transactions, or accounting records within
 * the finance domain.
 * 
 * This exception is thrown when:
 * - Account balances don't match expected reconciled amounts
 * - Transaction records are missing or duplicated during reconciliation
 * - Bank statement reconciliation shows unexplained differences
 * - Inter-company reconciliation reveals discrepancies
 * - Period-end reconciliation fails validation checks
 * - Automated reconciliation processes encounter data inconsistencies
 * - Manual reconciliation adjustments exceed tolerance limits
 */
class ReconciliationFailedException : FinanceDomainException {
    
    val reconciliationId: UUID
    val reconciliationType: ReconciliationType
    val accountId: UUID?
    val expectedAmount: BigDecimal
    val actualAmount: BigDecimal
    val discrepancyAmount: BigDecimal
    val reconciliationPeriod: String
    val failureReason: ReconciliationFailureReason
    val transactionCount: Int?
    val expectedTransactionCount: Int?
    val missingTransactions: List<UUID>?
    val duplicateTransactions: List<UUID>?
    val toleranceThreshold: BigDecimal?
    val entityId: UUID?
    val departmentId: UUID?
    val reconciliationDate: Date
    val lastSuccessfulReconciliation: Date?
    val autoReconciliationAttempts: Int
    val manualInterventionRequired: Boolean
    val reconciliationRules: String?
    val dataSourceDiscrepancy: String?
    
    constructor(
        reconciliationId: UUID,
        reconciliationType: ReconciliationType,
        expectedAmount: BigDecimal,
        actualAmount: BigDecimal,
        failureReason: ReconciliationFailureReason,
        message: String = "Reconciliation failed for ${reconciliationType.name}"
    ) : super(message) {
        this.reconciliationId = reconciliationId
        this.reconciliationType = reconciliationType
        this.accountId = null
        this.expectedAmount = expectedAmount
        this.actualAmount = actualAmount
        this.discrepancyAmount = actualAmount - expectedAmount
        this.reconciliationPeriod = "CURRENT"
        this.failureReason = failureReason
        this.transactionCount = null
        this.expectedTransactionCount = null
        this.missingTransactions = null
        this.duplicateTransactions = null
        this.toleranceThreshold = null
        this.entityId = null
        this.departmentId = null
        this.reconciliationDate = Date()
        this.lastSuccessfulReconciliation = null
        this.autoReconciliationAttempts = 1
        this.manualInterventionRequired = false
        this.reconciliationRules = null
        this.dataSourceDiscrepancy = null
    }
    
    constructor(
        reconciliationId: UUID,
        reconciliationType: ReconciliationType,
        accountId: UUID,
        expectedAmount: BigDecimal,
        actualAmount: BigDecimal,
        reconciliationPeriod: String,
        failureReason: ReconciliationFailureReason,
        transactionCount: Int,
        expectedTransactionCount: Int,
        missingTransactions: List<UUID>,
        duplicateTransactions: List<UUID>,
        toleranceThreshold: BigDecimal,
        entityId: UUID,
        departmentId: UUID?,
        reconciliationDate: Date,
        lastSuccessfulReconciliation: Date?,
        autoReconciliationAttempts: Int,
        manualInterventionRequired: Boolean,
        reconciliationRules: String,
        dataSourceDiscrepancy: String,
        message: String
    ) : super(message) {
        this.reconciliationId = reconciliationId
        this.reconciliationType = reconciliationType
        this.accountId = accountId
        this.expectedAmount = expectedAmount
        this.actualAmount = actualAmount
        this.discrepancyAmount = actualAmount - expectedAmount
        this.reconciliationPeriod = reconciliationPeriod
        this.failureReason = failureReason
        this.transactionCount = transactionCount
        this.expectedTransactionCount = expectedTransactionCount
        this.missingTransactions = missingTransactions
        this.duplicateTransactions = duplicateTransactions
        this.toleranceThreshold = toleranceThreshold
        this.entityId = entityId
        this.departmentId = departmentId
        this.reconciliationDate = reconciliationDate
        this.lastSuccessfulReconciliation = lastSuccessfulReconciliation
        this.autoReconciliationAttempts = autoReconciliationAttempts
        this.manualInterventionRequired = manualInterventionRequired
        this.reconciliationRules = reconciliationRules
        this.dataSourceDiscrepancy = dataSourceDiscrepancy
    }
    
    /**
     * Calculate the absolute discrepancy amount
     */
    fun getAbsoluteDiscrepancy(): BigDecimal {
        return discrepancyAmount.abs()
    }
    
    /**
     * Calculate the discrepancy percentage
     */
    fun getDiscrepancyPercentage(): BigDecimal {
        return if (expectedAmount.abs() > BigDecimal.ZERO) {
            getAbsoluteDiscrepancy()
                .divide(expectedAmount.abs(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal("100.00"))
        } else {
            BigDecimal.ZERO
        }
    }
    
    /**
     * Check if discrepancy is within tolerance threshold
     */
    fun isWithinTolerance(): Boolean {
        return toleranceThreshold?.let { threshold ->
            getAbsoluteDiscrepancy() <= threshold
        } ?: false
    }
    
    /**
     * Check if this is a balance discrepancy
     */
    fun isBalanceDiscrepancy(): Boolean {
        return failureReason in setOf(
            ReconciliationFailureReason.BALANCE_MISMATCH,
            ReconciliationFailureReason.OPENING_BALANCE_ERROR,
            ReconciliationFailureReason.CLOSING_BALANCE_ERROR
        )
    }
    
    /**
     * Check if this is a transaction-related issue
     */
    fun isTransactionIssue(): Boolean {
        return failureReason in setOf(
            ReconciliationFailureReason.MISSING_TRANSACTIONS,
            ReconciliationFailureReason.DUPLICATE_TRANSACTIONS,
            ReconciliationFailureReason.TRANSACTION_COUNT_MISMATCH,
            ReconciliationFailureReason.TIMING_DIFFERENCES
        )
    }
    
    /**
     * Check if this is a data quality issue
     */
    fun isDataQualityIssue(): Boolean {
        return failureReason in setOf(
            ReconciliationFailureReason.DATA_CORRUPTION,
            ReconciliationFailureReason.INCOMPLETE_DATA,
            ReconciliationFailureReason.DATA_SOURCE_UNAVAILABLE,
            ReconciliationFailureReason.SYSTEM_INTEGRATION_ERROR
        )
    }
    
    /**
     * Check if this requires manual intervention
     */
    fun requiresManualIntervention(): Boolean {
        return manualInterventionRequired || 
               autoReconciliationAttempts >= 3 ||
               failureReason in setOf(
                   ReconciliationFailureReason.COMPLEX_ADJUSTMENTS_NEEDED,
                   ReconciliationFailureReason.MANUAL_ENTRIES_REQUIRED,
                   ReconciliationFailureReason.APPROVAL_REQUIRED
               )
    }
    
    /**
     * Get the severity level of this reconciliation failure
     */
    fun getReconciliationSeverity(): ReconciliationSeverity {
        return when {
            failureReason == ReconciliationFailureReason.DATA_CORRUPTION -> ReconciliationSeverity.CRITICAL
            failureReason == ReconciliationFailureReason.SYSTEM_INTEGRATION_ERROR -> ReconciliationSeverity.CRITICAL
            getDiscrepancyPercentage() > BigDecimal("10.00") -> ReconciliationSeverity.HIGH
            !isWithinTolerance() && isBalanceDiscrepancy() -> ReconciliationSeverity.HIGH
            autoReconciliationAttempts >= 3 -> ReconciliationSeverity.MEDIUM
            isTransactionIssue() -> ReconciliationSeverity.MEDIUM
            isWithinTolerance() -> ReconciliationSeverity.LOW
            else -> ReconciliationSeverity.MEDIUM
        }
    }
    
    /**
     * Get suggested resolution actions
     */
    fun getSuggestedResolutions(): List<String> {
        return when (failureReason) {
            ReconciliationFailureReason.BALANCE_MISMATCH -> listOf(
                "Review transactions for period: $reconciliationPeriod",
                "Check for unrecorded transactions or adjustments",
                "Verify opening balance from previous reconciliation",
                "Investigate discrepancy of ${getAbsoluteDiscrepancy()}",
                "Compare with bank statements or external records",
                "Consider timing differences for pending transactions"
            )
            ReconciliationFailureReason.MISSING_TRANSACTIONS -> listOf(
                "Identify and record missing transactions: ${missingTransactions?.size ?: 0} transactions",
                "Review transaction import processes for completeness",
                "Check for failed transaction uploads or processing",
                "Verify transaction cut-off dates for reconciliation period",
                "Cross-reference with source documents or bank statements",
                "Update transaction records with missing entries"
            )
            ReconciliationFailureReason.DUPLICATE_TRANSACTIONS -> listOf(
                "Remove duplicate transaction entries: ${duplicateTransactions?.size ?: 0} duplicates",
                "Review transaction import controls to prevent duplicates",
                "Implement unique transaction ID validation",
                "Check for multiple data source imports causing duplicates",
                "Establish transaction deduplication procedures",
                "Update reconciliation rules to detect duplicates"
            )
            ReconciliationFailureReason.TIMING_DIFFERENCES -> listOf(
                "Identify and document timing differences",
                "Create reconciling items for outstanding transactions",
                "Adjust cut-off procedures for future reconciliations",
                "Review transaction processing delays and causes",
                "Implement more frequent reconciliation cycles",
                "Set up automated alerts for significant timing differences"
            )
            ReconciliationFailureReason.DATA_CORRUPTION -> listOf(
                "URGENT: Restore data from most recent clean backup",
                "Investigate root cause of data corruption",
                "Run data integrity checks across all affected systems",
                "Contact IT support for system recovery procedures",
                "Document corruption incident for audit trail",
                "Implement additional data validation controls"
            )
            ReconciliationFailureReason.SYSTEM_INTEGRATION_ERROR -> listOf(
                "Check integration connectivity between systems",
                "Verify data mapping and transformation rules",
                "Review integration logs for error messages",
                "Test system interfaces with sample transactions",
                "Contact system administrators for integration support",
                "Implement manual reconciliation as temporary workaround"
            )
            ReconciliationFailureReason.TOLERANCE_EXCEEDED -> listOf(
                "Review tolerance threshold: currently ${toleranceThreshold ?: "not set"}",
                "Investigate reasons for discrepancy exceeding tolerance",
                "Consider adjusting tolerance limits if operationally justified",
                "Implement additional controls to reduce discrepancies",
                "Seek management approval for tolerance adjustment",
                "Document business case for tolerance threshold changes"
            )
            ReconciliationFailureReason.INCOMPLETE_DATA -> listOf(
                "Identify missing data elements required for reconciliation",
                "Review data extraction and loading processes",
                "Check for system downtime during data collection period",
                "Verify data source availability and accessibility",
                "Implement data completeness validation checks",
                "Create procedures for handling incomplete data scenarios"
            )
            ReconciliationFailureReason.MANUAL_ENTRIES_REQUIRED -> listOf(
                "Review and approve required manual journal entries",
                "Document business justification for manual adjustments",
                "Implement approval workflow for manual reconciliation entries",
                "Verify manual entries comply with accounting policies",
                "Create audit trail for all manual reconciliation adjustments",
                "Train staff on proper manual reconciliation procedures"
            )
            ReconciliationFailureReason.OPENING_BALANCE_ERROR -> listOf(
                "Verify opening balance from previous period reconciliation",
                "Check for unrecorded adjustments from prior period",
                "Review prior period closing procedures and timing",
                "Investigate any manual adjustments made to opening balance",
                "Reconcile opening balance to general ledger",
                "Document resolution of opening balance discrepancy"
            )
            ReconciliationFailureReason.CLOSING_BALANCE_ERROR -> listOf(
                "Verify all transactions included in closing balance calculation",
                "Check for pending transactions not yet processed",
                "Review month-end cut-off procedures and timing",
                "Investigate any adjusting entries affecting closing balance",
                "Compare closing balance to bank statement or external records",
                "Document closing balance reconciliation and adjustments"
            )
            ReconciliationFailureReason.TRANSACTION_COUNT_MISMATCH -> listOf(
                "Count: Expected ${expectedTransactionCount ?: "unknown"}, Actual ${transactionCount ?: "unknown"}",
                "Review transaction extraction criteria and filters",
                "Check for transactions excluded due to status or classification",
                "Verify transaction counting logic and rules",
                "Investigate duplicate or missing transaction records",
                "Align transaction counting methodology with reconciliation requirements"
            )
            ReconciliationFailureReason.COMPLEX_ADJUSTMENTS_NEEDED -> listOf(
                "Engage accounting specialist for complex reconciliation analysis",
                "Document all complex adjustments with detailed explanations",
                "Seek management review and approval for significant adjustments",
                "Consider impact on financial reporting and audit requirements",
                "Implement controls to prevent similar complex reconciliation issues",
                "Create detailed procedures for handling complex reconciliation scenarios"
            )
            ReconciliationFailureReason.DATA_SOURCE_UNAVAILABLE -> listOf(
                dataSourceDiscrepancy ?: "Identify unavailable data source",
                "Check system connectivity and access permissions",
                "Verify data source system status and availability",
                "Implement alternative data collection methods if needed",
                "Set up monitoring and alerts for data source availability",
                "Create contingency procedures for data source outages"
            )
            ReconciliationFailureReason.APPROVAL_REQUIRED -> listOf(
                "Submit reconciliation results for management approval",
                "Document business impact of reconciliation discrepancies",
                "Provide detailed analysis supporting reconciliation adjustments",
                "Ensure compliance with approval authority matrix",
                "Set up approval workflow for reconciliation exceptions",
                "Track approval status and follow up on pending approvals"
            )
        }
    }
    
    /**
     * Get the days since last successful reconciliation
     */
    fun getDaysSinceLastSuccessfulReconciliation(): Long {
        return lastSuccessfulReconciliation?.let { lastSuccess ->
            (reconciliationDate.time - lastSuccess.time) / (1000 * 60 * 60 * 24)
        } ?: -1
    }
    
    override fun getErrorCode(): String = "RECONCILIATION_FAILED"
    
    override fun getErrorCategory(): String = "RECONCILIATION_ERROR"
    
    override fun getBusinessImpact(): String = when (getReconciliationSeverity()) {
        ReconciliationSeverity.CRITICAL -> "CRITICAL - System integrity compromised, financial reporting at risk"
        ReconciliationSeverity.HIGH -> "HIGH - Significant discrepancy affecting accuracy of financial records"
        ReconciliationSeverity.MEDIUM -> "MEDIUM - Reconciliation discrepancy requiring investigation and resolution"
        ReconciliationSeverity.LOW -> "LOW - Minor reconciliation difference within operational parameters"
    }
    
    override fun getRecommendedAction(): String = when {
        getReconciliationSeverity() == ReconciliationSeverity.CRITICAL -> "URGENT: ${getSuggestedResolutions().first()}"
        requiresManualIntervention() -> "MANUAL INTERVENTION REQUIRED: ${getSuggestedResolutions().first()}"
        else -> getSuggestedResolutions().first()
    }
    
    override fun isRetryable(): Boolean = when (failureReason) {
        ReconciliationFailureReason.DATA_CORRUPTION -> false
        ReconciliationFailureReason.COMPLEX_ADJUSTMENTS_NEEDED -> false
        ReconciliationFailureReason.MANUAL_ENTRIES_REQUIRED -> false
        ReconciliationFailureReason.APPROVAL_REQUIRED -> false
        else -> autoReconciliationAttempts < 3 && !requiresManualIntervention()
    }
    
    override fun getRetryDelay(): Long = when {
        failureReason == ReconciliationFailureReason.DATA_SOURCE_UNAVAILABLE -> 1800000L // 30 minutes
        failureReason == ReconciliationFailureReason.SYSTEM_INTEGRATION_ERROR -> 900000L // 15 minutes
        isDataQualityIssue() -> 3600000L // 1 hour
        autoReconciliationAttempts >= 2 -> 7200000L // 2 hours
        else -> 300000L // 5 minutes
    }
    
    override fun requiresEscalation(): Boolean = getReconciliationSeverity() in setOf(
        ReconciliationSeverity.CRITICAL,
        ReconciliationSeverity.HIGH
    ) || requiresManualIntervention() || getDaysSinceLastSuccessfulReconciliation() > 7
    
    override fun getContextInformation(): Map<String, Any> {
        return super.getContextInformation() + mapOf(
            "reconciliationId" to reconciliationId.toString(),
            "reconciliationType" to reconciliationType.name,
            "accountId" to (accountId?.toString() ?: ""),
            "expectedAmount" to expectedAmount.toString(),
            "actualAmount" to actualAmount.toString(),
            "discrepancyAmount" to discrepancyAmount.toString(),
            "absoluteDiscrepancy" to getAbsoluteDiscrepancy().toString(),
            "discrepancyPercentage" to getDiscrepancyPercentage().toString(),
            "reconciliationPeriod" to reconciliationPeriod,
            "failureReason" to failureReason.name,
            "reconciliationSeverity" to getReconciliationSeverity().name,
            "isWithinTolerance" to isWithinTolerance(),
            "isBalanceDiscrepancy" to isBalanceDiscrepancy(),
            "isTransactionIssue" to isTransactionIssue(),
            "isDataQualityIssue" to isDataQualityIssue(),
            "requiresManualIntervention" to requiresManualIntervention(),
            "transactionCount" to (transactionCount ?: 0),
            "expectedTransactionCount" to (expectedTransactionCount ?: 0),
            "missingTransactionsCount" to (missingTransactions?.size ?: 0),
            "duplicateTransactionsCount" to (duplicateTransactions?.size ?: 0),
            "toleranceThreshold" to (toleranceThreshold?.toString() ?: ""),
            "reconciliationDate" to reconciliationDate.toString(),
            "lastSuccessfulReconciliation" to (lastSuccessfulReconciliation?.toString() ?: ""),
            "daysSinceLastSuccess" to getDaysSinceLastSuccessfulReconciliation(),
            "autoReconciliationAttempts" to autoReconciliationAttempts,
            "manualInterventionRequired" to manualInterventionRequired,
            "reconciliationRules" to (reconciliationRules ?: ""),
            "dataSourceDiscrepancy" to (dataSourceDiscrepancy ?: ""),
            "entityId" to (entityId?.toString() ?: ""),
            "departmentId" to (departmentId?.toString() ?: "")
        )
    }
    
    companion object {
        /**
         * Create exception for balance mismatch
         */
        fun balanceMismatch(
            reconciliationId: UUID,
            accountId: UUID,
            expectedAmount: BigDecimal,
            actualAmount: BigDecimal,
            reconciliationType: ReconciliationType = ReconciliationType.ACCOUNT_RECONCILIATION,
            toleranceThreshold: BigDecimal = BigDecimal("0.01")
        ): ReconciliationFailedException {
            val discrepancy = actualAmount - expectedAmount
            return ReconciliationFailedException(
                reconciliationId = reconciliationId,
                reconciliationType = reconciliationType,
                accountId = accountId,
                expectedAmount = expectedAmount,
                actualAmount = actualAmount,
                reconciliationPeriod = "CURRENT_PERIOD",
                failureReason = ReconciliationFailureReason.BALANCE_MISMATCH,
                transactionCount = 0,
                expectedTransactionCount = 0,
                missingTransactions = emptyList(),
                duplicateTransactions = emptyList(),
                toleranceThreshold = toleranceThreshold,
                entityId = UUID.randomUUID(),
                departmentId = null,
                reconciliationDate = Date(),
                lastSuccessfulReconciliation = null,
                autoReconciliationAttempts = 1,
                manualInterventionRequired = discrepancy.abs() > toleranceThreshold,
                reconciliationRules = "Standard balance reconciliation",
                dataSourceDiscrepancy = "",
                message = "Balance mismatch: expected $expectedAmount, actual $actualAmount, discrepancy $discrepancy"
            )
        }
        
        /**
         * Create exception for missing transactions
         */
        fun missingTransactions(
            reconciliationId: UUID,
            reconciliationType: ReconciliationType,
            accountId: UUID,
            missingTransactionIds: List<UUID>,
            expectedCount: Int,
            actualCount: Int
        ): ReconciliationFailedException {
            return ReconciliationFailedException(
                reconciliationId = reconciliationId,
                reconciliationType = reconciliationType,
                accountId = accountId,
                expectedAmount = BigDecimal.ZERO,
                actualAmount = BigDecimal.ZERO,
                reconciliationPeriod = "CURRENT_PERIOD",
                failureReason = ReconciliationFailureReason.MISSING_TRANSACTIONS,
                transactionCount = actualCount,
                expectedTransactionCount = expectedCount,
                missingTransactions = missingTransactionIds,
                duplicateTransactions = emptyList(),
                toleranceThreshold = BigDecimal.ZERO,
                entityId = UUID.randomUUID(),
                departmentId = null,
                reconciliationDate = Date(),
                lastSuccessfulReconciliation = null,
                autoReconciliationAttempts = 1,
                manualInterventionRequired = missingTransactionIds.size > 5,
                reconciliationRules = "Transaction count validation",
                dataSourceDiscrepancy = "Missing ${missingTransactionIds.size} transactions",
                message = "Missing transactions in reconciliation: expected $expectedCount, found $actualCount, missing ${missingTransactionIds.size}"
            )
        }
        
        /**
         * Create exception for data corruption
         */
        fun dataCorruption(
            reconciliationId: UUID,
            reconciliationType: ReconciliationType,
            corruptionDescription: String,
            affectedAccounts: List<UUID>
        ): ReconciliationFailedException {
            return ReconciliationFailedException(
                reconciliationId = reconciliationId,
                reconciliationType = reconciliationType,
                accountId = affectedAccounts.firstOrNull(),
                expectedAmount = BigDecimal.ZERO,
                actualAmount = BigDecimal.ZERO,
                reconciliationPeriod = "AFFECTED_PERIOD",
                failureReason = ReconciliationFailureReason.DATA_CORRUPTION,
                transactionCount = 0,
                expectedTransactionCount = 0,
                missingTransactions = emptyList(),
                duplicateTransactions = emptyList(),
                toleranceThreshold = BigDecimal.ZERO,
                entityId = UUID.randomUUID(),
                departmentId = null,
                reconciliationDate = Date(),
                lastSuccessfulReconciliation = null,
                autoReconciliationAttempts = 0,
                manualInterventionRequired = true,
                reconciliationRules = "Data integrity validation",
                dataSourceDiscrepancy = corruptionDescription,
                message = "Data corruption detected during reconciliation: $corruptionDescription affecting ${affectedAccounts.size} accounts"
            )
        }
        
        /**
         * Create exception for system integration error
         */
        fun systemIntegrationError(
            reconciliationId: UUID,
            reconciliationType: ReconciliationType,
            systemError: String,
            attemptCount: Int
        ): ReconciliationFailedException {
            return ReconciliationFailedException(
                reconciliationId = reconciliationId,
                reconciliationType = reconciliationType,
                expectedAmount = BigDecimal.ZERO,
                actualAmount = BigDecimal.ZERO,
                failureReason = ReconciliationFailureReason.SYSTEM_INTEGRATION_ERROR,
                message = "System integration error during reconciliation after $attemptCount attempts: $systemError"
            )
        }
        
        /**
         * Create exception for tolerance exceeded
         */
        fun toleranceExceeded(
            reconciliationId: UUID,
            reconciliationType: ReconciliationType,
            accountId: UUID,
            discrepancyAmount: BigDecimal,
            toleranceThreshold: BigDecimal,
            period: String
        ): ReconciliationFailedException {
            return ReconciliationFailedException(
                reconciliationId = reconciliationId,
                reconciliationType = reconciliationType,
                accountId = accountId,
                expectedAmount = BigDecimal.ZERO,
                actualAmount = discrepancyAmount,
                reconciliationPeriod = period,
                failureReason = ReconciliationFailureReason.TOLERANCE_EXCEEDED,
                transactionCount = 0,
                expectedTransactionCount = 0,
                missingTransactions = emptyList(),
                duplicateTransactions = emptyList(),
                toleranceThreshold = toleranceThreshold,
                entityId = UUID.randomUUID(),
                departmentId = null,
                reconciliationDate = Date(),
                lastSuccessfulReconciliation = null,
                autoReconciliationAttempts = 1,
                manualInterventionRequired = true,
                reconciliationRules = "Tolerance threshold validation",
                dataSourceDiscrepancy = "Discrepancy ${discrepancyAmount.abs()} exceeds tolerance $toleranceThreshold",
                message = "Reconciliation discrepancy of ${discrepancyAmount.abs()} exceeds tolerance threshold of $toleranceThreshold for period $period"
            )
        }
    }
}

/**
 * Reconciliation Types
 */
enum class ReconciliationType {
    ACCOUNT_RECONCILIATION,
    BANK_RECONCILIATION,
    INTER_COMPANY_RECONCILIATION,
    BALANCE_SHEET_RECONCILIATION,
    CASH_RECONCILIATION,
    INVESTMENT_RECONCILIATION,
    PAYROLL_RECONCILIATION,
    TAX_RECONCILIATION,
    PERIOD_END_RECONCILIATION,
    INVENTORY_RECONCILIATION
}

/**
 * Reconciliation Failure Reasons
 */
enum class ReconciliationFailureReason {
    BALANCE_MISMATCH,
    MISSING_TRANSACTIONS,
    DUPLICATE_TRANSACTIONS,
    TIMING_DIFFERENCES,
    DATA_CORRUPTION,
    SYSTEM_INTEGRATION_ERROR,
    TOLERANCE_EXCEEDED,
    INCOMPLETE_DATA,
    MANUAL_ENTRIES_REQUIRED,
    OPENING_BALANCE_ERROR,
    CLOSING_BALANCE_ERROR,
    TRANSACTION_COUNT_MISMATCH,
    COMPLEX_ADJUSTMENTS_NEEDED,
    DATA_SOURCE_UNAVAILABLE,
    APPROVAL_REQUIRED
}

/**
 * Reconciliation Severity Levels
 */
enum class ReconciliationSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

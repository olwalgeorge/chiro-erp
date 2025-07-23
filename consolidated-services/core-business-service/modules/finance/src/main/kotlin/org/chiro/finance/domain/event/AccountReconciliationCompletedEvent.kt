package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * AccountReconciliationCompletedEvent
 * 
 * Domain event published when a bank account reconciliation process is completed.
 * This event triggers post-reconciliation workflows and integrates with various
 * financial reporting and cash management systems.
 * 
 * This event enables:
 * - Cash position reporting automation
 * - Bank relationship management
 * - Exception handling workflows
 * - Audit trail completion
 * - Treasury management updates
 * - Financial reporting accuracy
 */
data class AccountReconciliationCompletedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Reconciliation ID
    val reconciliationId: UUID,
    val reconciliationNumber: String,
    val accountId: UUID,
    val accountNumber: String,
    val accountName: String,
    val accountType: AccountType,
    val bankId: UUID,
    val bankName: String,
    val branchCode: String?,
    val reconciliationPeriod: ReconciliationPeriod,
    val reconciliationDate: LocalDate,
    val statementDate: LocalDate,
    val statementPeriodStart: LocalDate,
    val statementPeriodEnd: LocalDate,
    val reconciliationType: ReconciliationType,
    val reconciliationMethod: ReconciliationMethod,
    val reconciliationStatus: ReconciliationCompletionStatus,
    val currency: Currency,
    val openingBookBalance: FinancialAmount,
    val closingBookBalance: FinancialAmount,
    val openingBankBalance: FinancialAmount,
    val closingBankBalance: FinancialAmount,
    val reconciledBalance: FinancialAmount,
    val totalReconciliationAdjustments: FinancialAmount,
    val outstandingDepositsAmount: FinancialAmount,
    val outstandingChecksAmount: FinancialAmount,
    val bankChargesAmount: FinancialAmount,
    val interestEarnedAmount: FinancialAmount,
    val otherAdjustmentsAmount: FinancialAmount,
    val totalTransactionsReconciled: Int,
    val autoMatchedTransactions: Int,
    val manuallyMatchedTransactions: Int,
    val unmatchedBookTransactions: Int,
    val unmatchedBankTransactions: Int,
    val exceptionCount: Int,
    val discrepancyCount: Int,
    val totalDiscrepancyAmount: FinancialAmount,
    val largestDiscrepancyAmount: FinancialAmount,
    val exceptions: List<ReconciliationException>,
    val adjustmentEntries: List<AdjustmentEntry>,
    val automatedMatchingAccuracy: BigDecimal, // Percentage
    val reconciliationEfficiencyScore: Int, // 0-100
    val timeToComplete: Long, // Minutes
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime,
    val reconciledBy: UUID,
    val reconciledByName: String,
    val reviewedBy: UUID?,
    val reviewedByName: String?,
    val approvedBy: UUID?,
    val approvedByName: String?,
    val reviewDate: LocalDateTime?,
    val approvalDate: LocalDateTime?,
    val requiresManagerReview: Boolean,
    val requiresAuditReview: Boolean,
    val nextReconciliationDue: LocalDate,
    val reconciliationFrequency: ReconciliationFrequency,
    val qualityScore: Int, // 0-100
    val riskLevel: RiskLevel,
    val complianceStatus: ComplianceStatus,
    val auditTrailId: UUID,
    val workflowInstanceId: UUID?,
    val notificationsSent: Set<NotificationType>,
    val tags: Set<String> = emptySet(),
    val notes: String?,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Check if reconciliation is fully balanced (no discrepancies)
     */
    fun isFullyBalanced(): Boolean {
        return totalDiscrepancyAmount.isZero() && exceptionCount == 0
    }
    
    /**
     * Check if reconciliation has material discrepancies
     */
    fun hasMaterialDiscrepancies(materialityThreshold: FinancialAmount): Boolean {
        return totalDiscrepancyAmount.abs().isGreaterThan(materialityThreshold)
    }
    
    /**
     * Check if reconciliation was completed efficiently
     */
    fun isEfficientReconciliation(): Boolean {
        return reconciliationEfficiencyScore >= 80 && 
               automatedMatchingAccuracy >= BigDecimal("90") &&
               timeToComplete <= 120 // 2 hours
    }
    
    /**
     * Get outstanding items ratio (items not reconciled)
     */
    fun getOutstandingItemsRatio(): BigDecimal {
        val totalItems = totalTransactionsReconciled + unmatchedBookTransactions + unmatchedBankTransactions
        val outstandingItems = unmatchedBookTransactions + unmatchedBankTransactions
        
        return if (totalItems > 0) {
            BigDecimal(outstandingItems).divide(BigDecimal(totalItems), 4, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }
    
    /**
     * Get reconciliation variance percentage
     */
    fun getVariancePercentage(): BigDecimal {
        return if (closingBankBalance.isPositive()) {
            totalDiscrepancyAmount.amount.abs()
                .divide(closingBankBalance.amount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else {
            BigDecimal.ZERO
        }
    }
    
    /**
     * Check if reconciliation requires immediate attention
     */
    fun requiresImmediateAttention(): Boolean {
        return !isFullyBalanced() && 
               (hasMaterialDiscrepancies(FinancialAmount(BigDecimal("1000"), currency)) ||
                exceptionCount >= 5 ||
                riskLevel == RiskLevel.VERY_HIGH)
    }
    
    /**
     * Get cash availability assessment
     */
    fun getCashAvailability(): CashAvailability {
        val netPosition = reconciledBalance
            .subtract(outstandingChecksAmount)
            .add(outstandingDepositsAmount)
        
        return when {
            netPosition.amount >= closingBookBalance.amount.multiply(BigDecimal("1.1")) -> CashAvailability.EXCELLENT
            netPosition.amount >= closingBookBalance.amount -> CashAvailability.GOOD
            netPosition.amount >= closingBookBalance.amount.multiply(BigDecimal("0.9")) -> CashAvailability.ADEQUATE
            netPosition.amount >= closingBookBalance.amount.multiply(BigDecimal("0.8")) -> CashAvailability.TIGHT
            else -> CashAvailability.CRITICAL
        }
    }
    
    /**
     * Check if reconciliation indicates potential fraud risk
     */
    fun indicatesFraudRisk(): Boolean {
        return exceptions.any { it.type == ExceptionType.UNAUTHORIZED_TRANSACTION } ||
               largestDiscrepancyAmount.amount >= BigDecimal("10000") ||
               (exceptionCount >= 3 && totalDiscrepancyAmount.amount >= BigDecimal("5000"))
    }
    
    /**
     * Get reconciliation trend indicator
     */
    fun getReconciliationTrend(): ReconciliationTrend {
        // This would typically compare with previous periods
        return when {
            reconciliationEfficiencyScore >= 90 && exceptionCount <= 1 -> ReconciliationTrend.IMPROVING
            reconciliationEfficiencyScore >= 75 && exceptionCount <= 3 -> ReconciliationTrend.STABLE
            reconciliationEfficiencyScore < 60 || exceptionCount >= 5 -> ReconciliationTrend.DETERIORATING
            else -> ReconciliationTrend.STABLE
        }
    }
    
    /**
     * Get control effectiveness rating
     */
    fun getControlEffectiveness(): ControlEffectiveness {
        return when {
            isFullyBalanced() && reconciliationEfficiencyScore >= 95 -> ControlEffectiveness.HIGHLY_EFFECTIVE
            exceptionCount <= 2 && reconciliationEfficiencyScore >= 85 -> ControlEffectiveness.EFFECTIVE
            exceptionCount <= 5 && reconciliationEfficiencyScore >= 70 -> ControlEffectiveness.MODERATELY_EFFECTIVE
            else -> ControlEffectiveness.NEEDS_IMPROVEMENT
        }
    }
    
    /**
     * Check if reconciliation affects cash flow forecasting
     */
    fun affectsCashFlowForecasting(): Boolean {
        return outstandingDepositsAmount.amount >= BigDecimal("10000") ||
               outstandingChecksAmount.amount >= BigDecimal("10000") ||
               hasMaterialDiscrepancies(FinancialAmount(BigDecimal("5000"), currency))
    }
    
    companion object {
        /**
         * Create event for successful reconciliation
         */
        fun forSuccessfulReconciliation(
            reconciliationId: UUID,
            reconciliationNumber: String,
            accountId: UUID,
            accountNumber: String,
            accountName: String,
            bankName: String,
            reconciliationDate: LocalDate,
            statementDate: LocalDate,
            openingBalance: FinancialAmount,
            closingBalance: FinancialAmount,
            bankBalance: FinancialAmount,
            transactionsReconciled: Int,
            autoMatched: Int,
            reconciledBy: UUID,
            reconciledByName: String,
            completionTime: Long,
            auditTrailId: UUID
        ): AccountReconciliationCompletedEvent {
            
            val efficiency = calculateEfficiencyScore(autoMatched, transactionsReconciled, completionTime)
            val accuracy = if (transactionsReconciled > 0) {
                BigDecimal(autoMatched).divide(BigDecimal(transactionsReconciled), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
            } else {
                BigDecimal.ZERO
            }
            
            return AccountReconciliationCompletedEvent(
                aggregateId = reconciliationId,
                reconciliationId = reconciliationId,
                reconciliationNumber = reconciliationNumber,
                accountId = accountId,
                accountNumber = accountNumber,
                accountName = accountName,
                accountType = AccountType.ASSET, // Bank account
                bankId = UUID.randomUUID(),
                bankName = bankName,
                reconciliationPeriod = ReconciliationPeriod.MONTHLY,
                reconciliationDate = reconciliationDate,
                statementDate = statementDate,
                statementPeriodStart = statementDate.withDayOfMonth(1),
                statementPeriodEnd = statementDate,
                reconciliationType = ReconciliationType.BANK_RECONCILIATION,
                reconciliationMethod = ReconciliationMethod.AUTOMATED_WITH_MANUAL_REVIEW,
                reconciliationStatus = ReconciliationCompletionStatus.COMPLETED,
                currency = openingBalance.currency,
                openingBookBalance = openingBalance,
                closingBookBalance = closingBalance,
                openingBankBalance = openingBalance,
                closingBankBalance = bankBalance,
                reconciledBalance = bankBalance,
                totalReconciliationAdjustments = FinancialAmount.ZERO,
                outstandingDepositsAmount = FinancialAmount.ZERO,
                outstandingChecksAmount = FinancialAmount.ZERO,
                bankChargesAmount = FinancialAmount.ZERO,
                interestEarnedAmount = FinancialAmount.ZERO,
                otherAdjustmentsAmount = FinancialAmount.ZERO,
                totalTransactionsReconciled = transactionsReconciled,
                autoMatchedTransactions = autoMatched,
                manuallyMatchedTransactions = transactionsReconciled - autoMatched,
                unmatchedBookTransactions = 0,
                unmatchedBankTransactions = 0,
                exceptionCount = 0,
                discrepancyCount = 0,
                totalDiscrepancyAmount = FinancialAmount.ZERO,
                largestDiscrepancyAmount = FinancialAmount.ZERO,
                exceptions = emptyList(),
                adjustmentEntries = emptyList(),
                automatedMatchingAccuracy = accuracy,
                reconciliationEfficiencyScore = efficiency,
                timeToComplete = completionTime,
                startedAt = LocalDateTime.now().minusMinutes(completionTime),
                completedAt = LocalDateTime.now(),
                reconciledBy = reconciledBy,
                reconciledByName = reconciledByName,
                requiresManagerReview = false,
                requiresAuditReview = false,
                nextReconciliationDue = reconciliationDate.plusMonths(1),
                reconciliationFrequency = ReconciliationFrequency.MONTHLY,
                qualityScore = efficiency,
                riskLevel = RiskLevel.LOW,
                complianceStatus = ComplianceStatus.COMPLIANT,
                auditTrailId = auditTrailId,
                notificationsSent = emptySet()
            )
        }
        
        private fun calculateEfficiencyScore(autoMatched: Int, total: Int, timeMinutes: Long): Int {
            var score = 100
            
            // Deduct for low automation rate
            val automationRate = if (total > 0) (autoMatched * 100) / total else 100
            if (automationRate < 80) score -= (80 - automationRate) / 2
            
            // Deduct for excessive time
            when {
                timeMinutes > 240 -> score -= 30 // > 4 hours
                timeMinutes > 120 -> score -= 20 // > 2 hours
                timeMinutes > 60 -> score -= 10  // > 1 hour
            }
            
            return maxOf(0, score)
        }
    }
}

/**
 * Reconciliation Period Types
 */
enum class ReconciliationPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    ANNUALLY,
    AD_HOC
}

/**
 * Reconciliation Type Classifications
 */
enum class ReconciliationType {
    BANK_RECONCILIATION,
    CREDIT_CARD_RECONCILIATION,
    INTERCOMPANY_RECONCILIATION,
    SUBSIDIARY_RECONCILIATION,
    GENERAL_LEDGER_RECONCILIATION,
    BALANCE_SHEET_RECONCILIATION
}

/**
 * Reconciliation Method Types
 */
enum class ReconciliationMethod {
    FULLY_AUTOMATED,
    AUTOMATED_WITH_MANUAL_REVIEW,
    SEMI_AUTOMATED,
    MANUAL,
    SYSTEM_ASSISTED
}

/**
 * Reconciliation Completion Status
 */
enum class ReconciliationCompletionStatus {
    COMPLETED,
    COMPLETED_WITH_EXCEPTIONS,
    PARTIALLY_COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Reconciliation Frequency Options
 */
enum class ReconciliationFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    ANNUALLY
}

/**
 * Compliance Status Values
 */
enum class ComplianceStatus {
    COMPLIANT,
    NON_COMPLIANT,
    PENDING_REVIEW,
    REQUIRES_ATTENTION
}

/**
 * Cash Availability Levels
 */
enum class CashAvailability {
    EXCELLENT,
    GOOD,
    ADEQUATE,
    TIGHT,
    CRITICAL
}

/**
 * Reconciliation Trend Indicators
 */
enum class ReconciliationTrend {
    IMPROVING,
    STABLE,
    DETERIORATING,
    VOLATILE
}

/**
 * Control Effectiveness Ratings
 */
enum class ControlEffectiveness {
    HIGHLY_EFFECTIVE,
    EFFECTIVE,
    MODERATELY_EFFECTIVE,
    NEEDS_IMPROVEMENT,
    INEFFECTIVE
}

/**
 * Exception Type Classifications
 */
enum class ExceptionType {
    UNMATCHED_TRANSACTION,
    AMOUNT_DISCREPANCY,
    DATE_MISMATCH,
    DUPLICATE_TRANSACTION,
    MISSING_TRANSACTION,
    UNAUTHORIZED_TRANSACTION,
    BANK_ERROR,
    TIMING_DIFFERENCE,
    OTHER
}

/**
 * Reconciliation Exception Details
 */
data class ReconciliationException(
    val exceptionId: UUID,
    val type: ExceptionType,
    val description: String,
    val amount: FinancialAmount,
    val transactionId: UUID?,
    val resolution: String?,
    val resolvedBy: UUID?,
    val resolvedAt: LocalDateTime?
)

/**
 * Adjustment Entry Details
 */
data class AdjustmentEntry(
    val adjustmentId: UUID,
    val journalEntryId: UUID,
    val description: String,
    val amount: FinancialAmount,
    val accountCode: AccountCode,
    val adjustmentType: AdjustmentType
)

/**
 * Adjustment Type Classifications
 */
enum class AdjustmentType {
    BANK_CHARGES,
    INTEREST_EARNED,
    NSF_CHECK,
    BANK_ERROR_CORRECTION,
    TIMING_ADJUSTMENT,
    OTHER
}

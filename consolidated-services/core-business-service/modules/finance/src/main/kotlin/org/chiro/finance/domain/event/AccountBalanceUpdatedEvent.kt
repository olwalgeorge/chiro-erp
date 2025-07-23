package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDateTime
import java.util.*

/**
 * AccountBalanceUpdatedEvent
 * 
 * Domain event published when an account balance is updated through posting
 * of journal entries, adjustments, or reconciliation activities.
 * 
 * This event enables:
 * - Real-time balance monitoring and alerts
 * - Integration with external reporting systems
 * - Audit trail maintenance
 * - Cache invalidation for balance queries
 * - Downstream system notifications
 */
data class AccountBalanceUpdatedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Account ID
    val accountCode: AccountCode,
    val accountName: String,
    val accountType: AccountType,
    val previousBalance: FinancialAmount,
    val newBalance: FinancialAmount,
    val balanceChange: FinancialAmount,
    val transactionId: UUID?, // Journal entry that caused the change
    val transactionType: TransactionType?,
    val fiscalPeriodId: UUID,
    val fiscalYear: Int,
    val fiscalPeriod: Int,
    val changeReason: BalanceChangeReason,
    val changeDescription: String,
    val postedBy: UUID,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Check if this is a significant balance change requiring attention
     */
    fun isSignificantChange(thresholdPercentage: Double = 10.0): Boolean {
        if (previousBalance.isZero()) return !newBalance.isZero()
        
        val changePercentage = balanceChange.abs().divideBy(previousBalance.abs()) * 100.0
        return changePercentage >= thresholdPercentage
    }
    
    /**
     * Check if balance increased
     */
    fun isIncrease(): Boolean = balanceChange.isPositive()
    
    /**
     * Check if balance decreased
     */
    fun isDecrease(): Boolean = balanceChange.isNegative()
    
    /**
     * Get the percentage change
     */
    fun getChangePercentage(): Double {
        return if (previousBalance.isZero()) {
            if (newBalance.isZero()) 0.0 else 100.0
        } else {
            balanceChange.divideBy(previousBalance.abs()) * 100.0
        }
    }
    
    /**
     * Check if this is a reversal operation
     */
    fun isReversal(): Boolean = changeReason == BalanceChangeReason.REVERSAL
    
    /**
     * Check if this is an adjustment
     */
    fun isAdjustment(): Boolean = changeReason == BalanceChangeReason.ADJUSTMENT
    
    /**
     * Check if this is from normal transaction posting
     */
    fun isNormalPosting(): Boolean = changeReason == BalanceChangeReason.JOURNAL_ENTRY_POSTING
    
    companion object {
        /**
         * Create event for journal entry posting
         */
        fun forJournalEntryPosting(
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            accountType: AccountType,
            previousBalance: FinancialAmount,
            newBalance: FinancialAmount,
            journalEntryId: UUID,
            transactionType: TransactionType,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int,
            postedBy: UUID,
            description: String = "Journal entry posting"
        ): AccountBalanceUpdatedEvent {
            
            return AccountBalanceUpdatedEvent(
                aggregateId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                accountType = accountType,
                previousBalance = previousBalance,
                newBalance = newBalance,
                balanceChange = newBalance.subtract(previousBalance),
                transactionId = journalEntryId,
                transactionType = transactionType,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                changeReason = BalanceChangeReason.JOURNAL_ENTRY_POSTING,
                changeDescription = description,
                postedBy = postedBy
            )
        }
        
        /**
         * Create event for balance adjustment
         */
        fun forBalanceAdjustment(
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            accountType: AccountType,
            previousBalance: FinancialAmount,
            newBalance: FinancialAmount,
            adjustmentReason: String,
            adjustedBy: UUID,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int
        ): AccountBalanceUpdatedEvent {
            
            return AccountBalanceUpdatedEvent(
                aggregateId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                accountType = accountType,
                previousBalance = previousBalance,
                newBalance = newBalance,
                balanceChange = newBalance.subtract(previousBalance),
                transactionId = null,
                transactionType = null,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                changeReason = BalanceChangeReason.ADJUSTMENT,
                changeDescription = "Balance adjustment: $adjustmentReason",
                postedBy = adjustedBy
            )
        }
        
        /**
         * Create event for reconciliation
         */
        fun forReconciliation(
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            accountType: AccountType,
            previousBalance: FinancialAmount,
            reconciledBalance: FinancialAmount,
            reconciliationId: UUID,
            reconciledBy: UUID,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int
        ): AccountBalanceUpdatedEvent {
            
            return AccountBalanceUpdatedEvent(
                aggregateId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                accountType = accountType,
                previousBalance = previousBalance,
                newBalance = reconciledBalance,
                balanceChange = reconciledBalance.subtract(previousBalance),
                transactionId = reconciliationId,
                transactionType = TransactionType.BANK_RECONCILIATION,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                changeReason = BalanceChangeReason.RECONCILIATION,
                changeDescription = "Bank reconciliation adjustment",
                postedBy = reconciledBy
            )
        }
    }
}

/**
 * Reasons for balance changes
 */
enum class BalanceChangeReason {
    JOURNAL_ENTRY_POSTING,
    ADJUSTMENT,
    RECONCILIATION,
    REVERSAL,
    PERIOD_CLOSING,
    OPENING_BALANCE,
    CURRENCY_REVALUATION
}

/**
 * Base interface for all domain events
 */
interface DomainEvent {
    fun getAggregateId(): UUID
    fun getEventId(): UUID
    fun getOccurredAt(): LocalDateTime
    fun getVersion(): Long
}

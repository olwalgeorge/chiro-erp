package org.chiro.finance.domain.aggregate

import org.chiro.finance.domain.valueobject.*
import org.chiro.finance.domain.entity.*
import org.chiro.finance.domain.event.*
import org.chiro.finance.domain.exception.*
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * FiscalPeriod Aggregate Root
 * 
 * Core fiscal period aggregate that manages accounting periods, financial reporting cycles,
 * and period-specific business rules within the financial domain.
 * 
 * Business Rules:
 * - Fiscal periods cannot overlap within the same fiscal year
 * - Period must be opened before transactions can be posted
 * - Closed periods cannot be reopened without proper authorization
 * - Period end dates must be in chronological order
 * - All journal entries must belong to an open fiscal period
 */
@Entity
@Table(name = "fiscal_periods")
data class FiscalPeriod(
    @Id
    val periodId: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val fiscalYear: Int,
    
    @Column(nullable = false)
    val periodNumber: Int,
    
    @Column(nullable = false, length = 100)
    val periodName: String, // e.g., "January 2024", "Q1 2024"
    
    @Column(nullable = false)
    val startDate: LocalDate,
    
    @Column(nullable = false)
    val endDate: LocalDate,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: FiscalPeriodStatus = FiscalPeriodStatus.FUTURE,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val periodType: FiscalPeriodType,
    
    @Column(nullable = false)
    val isAdjustmentPeriod: Boolean = false,
    
    @Column(nullable = false)
    val isClosingPeriod: Boolean = false,
    
    @ElementCollection
    @CollectionTable(name = "fiscal_period_balances")
    val accountBalances: Map<AccountCode, FinancialAmount> = emptyMap(),
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_debits")),
        AttributeOverride(name = "currency", column = Column(name = "base_currency"))
    )
    val totalDebits: FinancialAmount = FinancialAmount.ZERO,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_credits")),
        AttributeOverride(name = "currency", column = Column(name = "total_credits_currency"))
    )
    val totalCredits: FinancialAmount = FinancialAmount.ZERO,
    
    @Column(nullable = false)
    val transactionCount: Long = 0L,
    
    @Column(nullable = false)
    val journalEntryCount: Long = 0L,
    
    @Column
    val openedAt: LocalDateTime? = null,
    
    @Column
    val closedAt: LocalDateTime? = null,
    
    @Column
    val openedBy: UUID? = null,
    
    @Column
    val closedBy: UUID? = null,
    
    @Column(length = 1000)
    val closingNotes: String? = null,
    
    @Column(nullable = false)
    val allowsPosting: Boolean = false,
    
    @Column(nullable = false)
    val requiresApproval: Boolean = true,
    
    @ElementCollection
    @CollectionTable(name = "fiscal_period_restrictions")
    val postingRestrictions: Set<PostingRestriction> = emptySet(),
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    /**
     * Open the fiscal period for posting
     */
    fun open(openedBy: UUID): FiscalPeriod {
        when (status) {
            FiscalPeriodStatus.OPEN -> throw IllegalPeriodOperationException("Period is already open")
            FiscalPeriodStatus.CLOSED -> throw IllegalPeriodOperationException("Cannot reopen closed period without proper authorization")
            FiscalPeriodStatus.FUTURE -> {
                // Can open future periods if start date has arrived
                if (LocalDate.now().isBefore(startDate)) {
                    throw IllegalPeriodOperationException("Cannot open period before start date: $startDate")
                }
            }
        }
        
        return copy(
            status = FiscalPeriodStatus.OPEN,
            allowsPosting = true,
            openedAt = LocalDateTime.now(),
            openedBy = openedBy,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Close the fiscal period
     */
    fun close(closedBy: UUID, closingNotes: String? = null): FiscalPeriod {
        if (status != FiscalPeriodStatus.OPEN) {
            throw IllegalPeriodOperationException("Can only close open periods")
        }
        
        // Validate that all journal entries are balanced
        if (!isBalanced()) {
            throw UnbalancedPeriodException("Cannot close period with unbalanced accounts")
        }
        
        return copy(
            status = FiscalPeriodStatus.CLOSED,
            allowsPosting = false,
            closedAt = LocalDateTime.now(),
            closedBy = closedBy,
            closingNotes = closingNotes,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Soft close period (prevent new postings but allow adjustments)
     */
    fun softClose(closedBy: UUID, notes: String? = null): FiscalPeriod {
        if (status != FiscalPeriodStatus.OPEN) {
            throw IllegalPeriodOperationException("Can only soft close open periods")
        }
        
        return copy(
            status = FiscalPeriodStatus.SOFT_CLOSED,
            allowsPosting = false,
            postingRestrictions = postingRestrictions + PostingRestriction.ADJUSTMENT_ONLY,
            closingNotes = notes,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Reopen a closed period (requires special authorization)
     */
    fun reopen(reopenedBy: UUID, reason: String): FiscalPeriod {
        if (status != FiscalPeriodStatus.CLOSED && status != FiscalPeriodStatus.SOFT_CLOSED) {
            throw IllegalPeriodOperationException("Can only reopen closed periods")
        }
        
        return copy(
            status = FiscalPeriodStatus.OPEN,
            allowsPosting = true,
            openedAt = LocalDateTime.now(),
            openedBy = reopenedBy,
            closingNotes = (closingNotes ?: "") + "\nReopened: $reason",
            postingRestrictions = emptySet(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Add posting restriction
     */
    fun addPostingRestriction(restriction: PostingRestriction): FiscalPeriod {
        return copy(
            postingRestrictions = postingRestrictions + restriction,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Remove posting restriction
     */
    fun removePostingRestriction(restriction: PostingRestriction): FiscalPeriod {
        return copy(
            postingRestrictions = postingRestrictions - restriction,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Update account balance
     */
    fun updateAccountBalance(accountCode: AccountCode, newBalance: FinancialAmount): FiscalPeriod {
        val updatedBalances = accountBalances.toMutableMap()
        updatedBalances[accountCode] = newBalance
        
        return copy(
            accountBalances = updatedBalances,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Record journal entry posting
     */
    fun recordJournalEntryPosting(
        debitAmount: FinancialAmount,
        creditAmount: FinancialAmount,
        transactionCount: Int = 1
    ): FiscalPeriod {
        if (!allowsPosting) {
            throw IllegalPeriodOperationException("Period does not allow posting")
        }
        
        return copy(
            totalDebits = totalDebits.add(debitAmount),
            totalCredits = totalCredits.add(creditAmount),
            transactionCount = this.transactionCount + transactionCount,
            journalEntryCount = this.journalEntryCount + 1,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Check if period allows posting for specific entry type
     */
    fun allowsPosting(entryType: JournalEntryType): Boolean {
        if (!allowsPosting) return false
        
        return when {
            postingRestrictions.contains(PostingRestriction.ADJUSTMENT_ONLY) -> 
                entryType in listOf(JournalEntryType.ADJUSTMENT, JournalEntryType.CLOSING, JournalEntryType.REVERSAL)
            postingRestrictions.contains(PostingRestriction.CLOSING_ONLY) -> 
                entryType == JournalEntryType.CLOSING
            postingRestrictions.contains(PostingRestriction.NO_POSTING) -> false
            else -> true
        }
    }
    
    /**
     * Check if period is balanced (total debits = total credits)
     */
    fun isBalanced(): Boolean {
        return totalDebits.isEqualTo(totalCredits)
    }
    
    /**
     * Check if period is current (today falls within period dates)
     */
    fun isCurrent(): Boolean {
        val today = LocalDate.now()
        return !today.isBefore(startDate) && !today.isAfter(endDate)
    }
    
    /**
     * Check if period is in the future
     */
    fun isFuture(): Boolean {
        return LocalDate.now().isBefore(startDate)
    }
    
    /**
     * Check if period is in the past
     */
    fun isPast(): Boolean {
        return LocalDate.now().isAfter(endDate)
    }
    
    /**
     * Get number of days in period
     */
    fun getDaysInPeriod(): Long {
        return endDate.toEpochDay() - startDate.toEpochDay() + 1
    }
    
    /**
     * Get number of days elapsed in period
     */
    fun getDaysElapsed(): Long {
        val today = LocalDate.now()
        return when {
            today.isBefore(startDate) -> 0
            today.isAfter(endDate) -> getDaysInPeriod()
            else -> today.toEpochDay() - startDate.toEpochDay() + 1
        }
    }
    
    /**
     * Get account balance for specific account
     */
    fun getAccountBalance(accountCode: AccountCode): FinancialAmount {
        return accountBalances[accountCode] ?: FinancialAmount.ZERO
    }
    
    /**
     * Get variance between debits and credits
     */
    fun getVariance(): FinancialAmount {
        return totalDebits.subtract(totalCredits)
    }
    
    /**
     * Check if date falls within this period
     */
    fun containsDate(date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }
    
    companion object {
        /**
         * Create a new fiscal period
         */
        fun create(
            fiscalYear: Int,
            periodNumber: Int,
            periodName: String,
            startDate: LocalDate,
            endDate: LocalDate,
            periodType: FiscalPeriodType,
            baseCurrency: Currency,
            isAdjustmentPeriod: Boolean = false,
            isClosingPeriod: Boolean = false
        ): FiscalPeriod {
            
            if (startDate.isAfter(endDate)) {
                throw IllegalArgumentException("Start date cannot be after end date")
            }
            
            if (periodNumber < 1) {
                throw IllegalArgumentException("Period number must be positive")
            }
            
            return FiscalPeriod(
                fiscalYear = fiscalYear,
                periodNumber = periodNumber,
                periodName = periodName,
                startDate = startDate,
                endDate = endDate,
                periodType = periodType,
                isAdjustmentPeriod = isAdjustmentPeriod,
                isClosingPeriod = isClosingPeriod,
                totalDebits = FinancialAmount(BigDecimal.ZERO, baseCurrency),
                totalCredits = FinancialAmount(BigDecimal.ZERO, baseCurrency),
                status = if (LocalDate.now().isBefore(startDate)) {
                    FiscalPeriodStatus.FUTURE
                } else {
                    FiscalPeriodStatus.OPEN
                }
            )
        }
        
        /**
         * Create monthly periods for a fiscal year
         */
        fun createMonthlyPeriods(
            fiscalYear: Int,
            fiscalYearStartDate: LocalDate,
            baseCurrency: Currency
        ): List<FiscalPeriod> {
            val periods = mutableListOf<FiscalPeriod>()
            var currentDate = fiscalYearStartDate
            
            for (month in 1..12) {
                val monthStart = currentDate
                val monthEnd = currentDate.withDayOfMonth(currentDate.lengthOfMonth())
                
                periods.add(
                    create(
                        fiscalYear = fiscalYear,
                        periodNumber = month,
                        periodName = "${monthStart.month.name} $fiscalYear",
                        startDate = monthStart,
                        endDate = monthEnd,
                        periodType = FiscalPeriodType.MONTHLY,
                        baseCurrency = baseCurrency
                    )
                )
                
                currentDate = monthEnd.plusDays(1)
            }
            
            return periods
        }
    }
}

/**
 * Fiscal Period Status
 */
enum class FiscalPeriodStatus {
    FUTURE,
    OPEN,
    SOFT_CLOSED,
    CLOSED
}

/**
 * Fiscal Period Types
 */
enum class FiscalPeriodType {
    MONTHLY,
    QUARTERLY,
    SEMI_ANNUAL,
    ANNUAL,
    ADJUSTMENT,
    CLOSING
}

/**
 * Posting Restrictions
 */
enum class PostingRestriction {
    NO_POSTING,
    ADJUSTMENT_ONLY,
    CLOSING_ONLY,
    SUPERVISOR_APPROVAL_REQUIRED,
    EXTERNAL_AUDIT_RESTRICTION
}

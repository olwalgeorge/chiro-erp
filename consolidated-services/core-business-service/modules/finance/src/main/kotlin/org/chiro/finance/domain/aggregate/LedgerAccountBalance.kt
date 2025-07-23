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
 * LedgerAccountBalance Aggregate Root
 * 
 * Core general ledger balance aggregate that maintains running account balances,
 * period-to-date totals, and balance history within the financial domain.
 * 
 * Business Rules:
 * - Balance calculations must follow account type normal balance rules
 * - Period balances must reconcile with journal entry postings
 * - Balance changes must be recorded with proper audit trail
 * - Year-end closing balances become opening balances for next period
 * - Trial balance must always be in balance (total debits = total credits)
 */
@Entity
@Table(name = "ledger_account_balances")
data class LedgerAccountBalance(
    @Id
    val balanceId: UUID = UUID.randomUUID(),
    
    @Embedded
    val accountCode: AccountCode,
    
    @Column(nullable = false)
    val accountName: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val accountType: AccountType,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val normalBalance: AccountSide, // DEBIT or CREDIT
    
    @Column(nullable = false)
    val fiscalPeriodId: UUID,
    
    @Column(nullable = false)
    val fiscalYear: Int,
    
    @Column(nullable = false)
    val fiscalPeriod: Int,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "opening_balance_amount")),
        AttributeOverride(name = "currency", column = Column(name = "base_currency"))
    )
    val openingBalance: FinancialAmount,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "closing_balance_amount")),
        AttributeOverride(name = "currency", column = Column(name = "closing_balance_currency"))
    )
    val closingBalance: FinancialAmount,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "period_debits_amount")),
        AttributeOverride(name = "currency", column = Column(name = "period_debits_currency"))
    )
    val periodDebits: FinancialAmount = FinancialAmount.ZERO,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "period_credits_amount")),
        AttributeOverride(name = "currency", column = Column(name = "period_credits_currency"))
    )
    val periodCredits: FinancialAmount = FinancialAmount.ZERO,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "ytd_debits_amount")),
        AttributeOverride(name = "currency", column = Column(name = "ytd_debits_currency"))
    )
    val yearToDateDebits: FinancialAmount = FinancialAmount.ZERO,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "ytd_credits_amount")),
        AttributeOverride(name = "currency", column = Column(name = "ytd_credits_currency"))
    )
    val yearToDateCredits: FinancialAmount = FinancialAmount.ZERO,
    
    @Column(nullable = false)
    val transactionCount: Long = 0L,
    
    @Column(nullable = false)
    val lastTransactionDate: LocalDateTime? = null,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val requiresReconciliation: Boolean = false,
    
    @Column
    val lastReconciledDate: LocalDateTime? = null,
    
    @Column
    val reconciledBalance: FinancialAmount? = null,
    
    @ElementCollection
    @CollectionTable(name = "ledger_balance_adjustments")
    val balanceAdjustments: List<BalanceAdjustment> = emptyList(),
    
    @ElementCollection
    @CollectionTable(name = "ledger_daily_balances")
    val dailyBalances: Map<LocalDate, FinancialAmount> = emptyMap(),
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    /**
     * Post debit transaction to account
     */
    fun postDebit(
        amount: FinancialAmount,
        transactionDate: LocalDateTime,
        journalEntryId: UUID,
        description: String
    ): LedgerAccountBalance {
        validateCanPost()
        validateCurrencyMatch(amount)
        
        val newPeriodDebits = periodDebits.add(amount)
        val newYtdDebits = yearToDateDebits.add(amount)
        val newClosingBalance = calculateNewBalance(amount, isDebit = true)
        
        return copy(
            periodDebits = newPeriodDebits,
            yearToDateDebits = newYtdDebits,
            closingBalance = newClosingBalance,
            transactionCount = transactionCount + 1,
            lastTransactionDate = transactionDate,
            updatedAt = LocalDateTime.now()
        ).updateDailyBalance(transactionDate.toLocalDate(), newClosingBalance)
    }
    
    /**
     * Post credit transaction to account
     */
    fun postCredit(
        amount: FinancialAmount,
        transactionDate: LocalDateTime,
        journalEntryId: UUID,
        description: String
    ): LedgerAccountBalance {
        validateCanPost()
        validateCurrencyMatch(amount)
        
        val newPeriodCredits = periodCredits.add(amount)
        val newYtdCredits = yearToDateCredits.add(amount)
        val newClosingBalance = calculateNewBalance(amount, isDebit = false)
        
        return copy(
            periodCredits = newPeriodCredits,
            yearToDateCredits = newYtdCredits,
            closingBalance = newClosingBalance,
            transactionCount = transactionCount + 1,
            lastTransactionDate = transactionDate,
            updatedAt = LocalDateTime.now()
        ).updateDailyBalance(transactionDate.toLocalDate(), newClosingBalance)
    }
    
    /**
     * Apply balance adjustment
     */
    fun applyAdjustment(
        adjustmentAmount: FinancialAmount,
        reason: String,
        approvedBy: UUID,
        adjustmentDate: LocalDateTime = LocalDateTime.now()
    ): LedgerAccountBalance {
        validateCanPost()
        validateCurrencyMatch(adjustmentAmount)
        
        val adjustment = BalanceAdjustment(
            amount = adjustmentAmount,
            reason = reason,
            approvedBy = approvedBy,
            adjustmentDate = adjustmentDate
        )
        
        val newClosingBalance = closingBalance.add(adjustmentAmount)
        
        return copy(
            closingBalance = newClosingBalance,
            balanceAdjustments = balanceAdjustments + adjustment,
            updatedAt = LocalDateTime.now()
        ).updateDailyBalance(adjustmentDate.toLocalDate(), newClosingBalance)
    }
    
    /**
     * Reconcile account balance
     */
    fun reconcileBalance(
        reconciledAmount: FinancialAmount,
        reconciliationDate: LocalDateTime = LocalDateTime.now()
    ): LedgerAccountBalance {
        validateCurrencyMatch(reconciledAmount)
        
        val variance = closingBalance.subtract(reconciledAmount)
        
        return copy(
            reconciledBalance = reconciledAmount,
            lastReconciledDate = reconciliationDate,
            updatedAt = LocalDateTime.now()
        ).also {
            if (!variance.isZero()) {
                // Significant variance detected - might need investigation
                // ReconciliationVarianceEvent could be published here
            }
        }
    }
    
    /**
     * Close period (transfer closing balance to opening balance of next period)
     */
    fun closePeriod(): LedgerAccountBalance {
        // For balance sheet accounts, closing balance becomes opening balance
        // For income statement accounts, closing balance is reset to zero
        val newOpeningBalance = when (accountType) {
            AccountType.ASSET, AccountType.LIABILITY, AccountType.EQUITY -> closingBalance
            AccountType.REVENUE, AccountType.EXPENSE -> FinancialAmount.ZERO
        }
        
        return copy(
            openingBalance = newOpeningBalance,
            closingBalance = newOpeningBalance,
            periodDebits = FinancialAmount.ZERO,
            periodCredits = FinancialAmount.ZERO,
            // YTD amounts are maintained across periods within the same fiscal year
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Start new fiscal year
     */
    fun startNewFiscalYear(newFiscalYear: Int): LedgerAccountBalance {
        // For balance sheet accounts, closing balance becomes opening balance
        // Reset YTD amounts for new fiscal year
        val newOpeningBalance = when (accountType) {
            AccountType.ASSET, AccountType.LIABILITY, AccountType.EQUITY -> closingBalance
            AccountType.REVENUE, AccountType.EXPENSE -> FinancialAmount.ZERO
        }
        
        return copy(
            fiscalYear = newFiscalYear,
            openingBalance = newOpeningBalance,
            closingBalance = newOpeningBalance,
            periodDebits = FinancialAmount.ZERO,
            periodCredits = FinancialAmount.ZERO,
            yearToDateDebits = FinancialAmount.ZERO,
            yearToDateCredits = FinancialAmount.ZERO,
            transactionCount = 0L,
            lastTransactionDate = null,
            dailyBalances = emptyMap(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Deactivate account
     */
    fun deactivate(): LedgerAccountBalance {
        if (!closingBalance.isZero()) {
            throw IllegalAccountOperationException("Cannot deactivate account with non-zero balance: ${closingBalance.amount}")
        }
        
        return copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Reactivate account
     */
    fun reactivate(): LedgerAccountBalance {
        return copy(
            isActive = true,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Get net change for period
     */
    fun getPeriodNetChange(): FinancialAmount {
        return when (normalBalance) {
            AccountSide.DEBIT -> periodDebits.subtract(periodCredits)
            AccountSide.CREDIT -> periodCredits.subtract(periodDebits)
        }
    }
    
    /**
     * Get year-to-date net change
     */
    fun getYearToDateNetChange(): FinancialAmount {
        return when (normalBalance) {
            AccountSide.DEBIT -> yearToDateDebits.subtract(yearToDateCredits)
            AccountSide.CREDIT -> yearToDateCredits.subtract(yearToDateDebits)
        }
    }
    
    /**
     * Get balance on specific date
     */
    fun getBalanceOnDate(date: LocalDate): FinancialAmount {
        return dailyBalances[date] ?: openingBalance
    }
    
    /**
     * Check if account needs reconciliation
     */
    fun needsReconciliation(): Boolean {
        if (!requiresReconciliation) return false
        
        val daysSinceLastReconciliation = lastReconciledDate?.let {
            LocalDateTime.now().toLocalDate().toEpochDay() - it.toLocalDate().toEpochDay()
        } ?: Long.MAX_VALUE
        
        return daysSinceLastReconciliation > 30 // Monthly reconciliation
    }
    
    /**
     * Get reconciliation variance
     */
    fun getReconciliationVariance(): FinancialAmount? {
        return reconciledBalance?.let { closingBalance.subtract(it) }
    }
    
    /**
     * Check if balance is normal (positive for normal balance side)
     */
    fun hasNormalBalance(): Boolean {
        return when (normalBalance) {
            AccountSide.DEBIT -> !closingBalance.isNegative()
            AccountSide.CREDIT -> closingBalance.isNegative() || closingBalance.isZero()
        }
    }
    
    // Private helper methods
    private fun calculateNewBalance(amount: FinancialAmount, isDebit: Boolean): FinancialAmount {
        return when (normalBalance) {
            AccountSide.DEBIT -> {
                if (isDebit) closingBalance.add(amount) else closingBalance.subtract(amount)
            }
            AccountSide.CREDIT -> {
                if (isDebit) closingBalance.subtract(amount) else closingBalance.add(amount)
            }
        }
    }
    
    private fun updateDailyBalance(date: LocalDate, balance: FinancialAmount): LedgerAccountBalance {
        val updatedDailyBalances = dailyBalances.toMutableMap()
        updatedDailyBalances[date] = balance
        
        return copy(dailyBalances = updatedDailyBalances)
    }
    
    private fun validateCanPost() {
        if (!isActive) {
            throw IllegalAccountOperationException("Cannot post to inactive account: ${accountCode.value}")
        }
    }
    
    private fun validateCurrencyMatch(amount: FinancialAmount) {
        if (amount.currency != openingBalance.currency) {
            throw CurrencyMismatchException("Amount currency ${amount.currency} does not match account currency ${openingBalance.currency}")
        }
    }
    
    companion object {
        /**
         * Create a new ledger account balance
         */
        fun create(
            accountCode: AccountCode,
            accountName: String,
            accountType: AccountType,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int,
            openingBalance: FinancialAmount,
            requiresReconciliation: Boolean = false
        ): LedgerAccountBalance {
            
            val normalBalance = when (accountType) {
                AccountType.ASSET, AccountType.EXPENSE -> AccountSide.DEBIT
                AccountType.LIABILITY, AccountType.EQUITY, AccountType.REVENUE -> AccountSide.CREDIT
            }
            
            return LedgerAccountBalance(
                accountCode = accountCode,
                accountName = accountName,
                accountType = accountType,
                normalBalance = normalBalance,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                openingBalance = openingBalance,
                closingBalance = openingBalance,
                requiresReconciliation = requiresReconciliation
            )
        }
        
        /**
         * Create trial balance report data
         */
        fun createTrialBalance(accountBalances: List<LedgerAccountBalance>): TrialBalanceData {
            val totalDebits = accountBalances
                .filter { it.normalBalance == AccountSide.DEBIT && !it.closingBalance.isNegative() }
                .fold(FinancialAmount.ZERO) { acc, balance -> acc.add(balance.closingBalance) }
                
            val totalCredits = accountBalances
                .filter { it.normalBalance == AccountSide.CREDIT && it.closingBalance.isNegative() }
                .fold(FinancialAmount.ZERO) { acc, balance -> acc.add(balance.closingBalance.abs()) }
            
            return TrialBalanceData(
                accountBalances = accountBalances,
                totalDebits = totalDebits,
                totalCredits = totalCredits,
                isBalanced = totalDebits.isEqualTo(totalCredits)
            )
        }
    }
}

/**
 * Account Types
 */
enum class AccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE
}

/**
 * Account Balance Side
 */
enum class AccountSide {
    DEBIT,
    CREDIT
}

/**
 * Balance Adjustment Record
 */
@Embeddable
data class BalanceAdjustment(
    val amount: FinancialAmount,
    val reason: String,
    val approvedBy: UUID,
    val adjustmentDate: LocalDateTime
)

/**
 * Trial Balance Data Structure
 */
data class TrialBalanceData(
    val accountBalances: List<LedgerAccountBalance>,
    val totalDebits: FinancialAmount,
    val totalCredits: FinancialAmount,
    val isBalanced: Boolean
)

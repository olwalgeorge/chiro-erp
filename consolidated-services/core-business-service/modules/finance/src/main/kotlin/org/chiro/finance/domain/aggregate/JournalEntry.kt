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
 * JournalEntry Aggregate Root
 * 
 * Core accounting journal entry that maintains the fundamental accounting equation
 * and ensures double-entry bookkeeping integrity within the financial domain.
 * 
 * Business Rules:
 * - Total debits must equal total credits (balanced entry)
 * - Journal entry must have at least two account lines
 * - Entry cannot be modified after posting
 * - All account lines must use same base currency
 * - Account codes must be valid and active
 */
@Entity
@Table(name = "journal_entries")
data class JournalEntry(
    @Id
    val entryId: UUID = UUID.randomUUID(),
    
    @Column(nullable = false, unique = true)
    val entryNumber: String,
    
    @Column(nullable = false)
    val entryDate: LocalDate,
    
    @Column(nullable = false)
    val postingDate: LocalDate,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val entryType: JournalEntryType,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: JournalEntryStatus = JournalEntryStatus.DRAFT,
    
    @Column(nullable = false, length = 500)
    val description: String,
    
    @Column(length = 1000)
    val reference: String? = null,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_debit_amount")),
        AttributeOverride(name = "currency", column = Column(name = "base_currency"))
    )
    val totalDebitAmount: FinancialAmount,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_credit_amount")),
        AttributeOverride(name = "currency", column = Column(name = "total_credit_currency"))
    )
    val totalCreditAmount: FinancialAmount,
    
    @OneToMany(mappedBy = "journalEntry", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val accountLines: List<JournalEntryLine> = emptyList(),
    
    @Column(nullable = false)
    val sourceName: String, // e.g., "INVOICE", "PAYMENT", "MANUAL", "PAYROLL"
    
    @Column
    val sourceId: UUID? = null, // Reference to source document
    
    @Column(nullable = false)
    val fiscalPeriodId: UUID,
    
    @Column(nullable = false)
    val fiscalYear: Int,
    
    @Column(nullable = false)
    val fiscalPeriod: Int,
    
    @Column(nullable = false)
    val reversalOfEntryId: UUID? = null,
    
    @Column(nullable = false)
    val isReversalEntry: Boolean = false,
    
    @Column(nullable = false)
    val createdBy: UUID,
    
    @Column(nullable = false)
    val approvedBy: UUID? = null,
    
    @Column(nullable = false)
    val postedBy: UUID? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val approvedAt: LocalDateTime? = null,
    
    @Column
    val postedAt: LocalDateTime? = null
) {

    /**
     * Add debit line to journal entry
     */
    fun addDebitLine(
        accountCode: AccountCode,
        amount: FinancialAmount,
        description: String,
        reference: String? = null
    ): JournalEntry {
        validateCanModify()
        validateCurrencyMatch(amount)
        
        val debitLine = JournalEntryLine.createDebit(
            journalEntry = this,
            accountCode = accountCode,
            amount = amount,
            description = description,
            reference = reference
        )
        
        val updatedLines = accountLines + debitLine
        return recalculateTotals(updatedLines)
    }
    
    /**
     * Add credit line to journal entry
     */
    fun addCreditLine(
        accountCode: AccountCode,
        amount: FinancialAmount,
        description: String,
        reference: String? = null
    ): JournalEntry {
        validateCanModify()
        validateCurrencyMatch(amount)
        
        val creditLine = JournalEntryLine.createCredit(
            journalEntry = this,
            accountCode = accountCode,
            amount = amount,
            description = description,
            reference = reference
        )
        
        val updatedLines = accountLines + creditLine
        return recalculateTotals(updatedLines)
    }
    
    /**
     * Remove account line from journal entry
     */
    fun removeAccountLine(lineId: UUID): JournalEntry {
        validateCanModify()
        
        val updatedLines = accountLines.filter { it.lineId != lineId }
        return recalculateTotals(updatedLines)
    }
    
    /**
     * Update account line amount
     */
    fun updateLineAmount(lineId: UUID, newAmount: FinancialAmount): JournalEntry {
        validateCanModify()
        validateCurrencyMatch(newAmount)
        
        val updatedLines = accountLines.map { line ->
            if (line.lineId == lineId) {
                line.updateAmount(newAmount)
            } else {
                line
            }
        }
        
        return recalculateTotals(updatedLines)
    }
    
    /**
     * Validate that entry is balanced (debits = credits)
     */
    fun validateIsBalanced(): Boolean {
        return totalDebitAmount.isEqualTo(totalCreditAmount) && 
               accountLines.size >= 2
    }
    
    /**
     * Submit entry for approval
     */
    fun submitForApproval(): JournalEntry {
        if (status != JournalEntryStatus.DRAFT) {
            throw IllegalJournalEntryOperationException("Can only submit draft entries for approval")
        }
        
        if (!validateIsBalanced()) {
            throw UnbalancedJournalEntryException("Journal entry is not balanced. Debits: ${totalDebitAmount.amount}, Credits: ${totalCreditAmount.amount}")
        }
        
        if (accountLines.isEmpty()) {
            throw IllegalJournalEntryOperationException("Journal entry must have at least one account line")
        }
        
        return copy(
            status = JournalEntryStatus.PENDING_APPROVAL,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Approve entry
     */
    fun approve(approvedBy: UUID): JournalEntry {
        if (status != JournalEntryStatus.PENDING_APPROVAL) {
            throw IllegalJournalEntryOperationException("Can only approve entries pending approval")
        }
        
        return copy(
            status = JournalEntryStatus.APPROVED,
            approvedBy = approvedBy,
            approvedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Reject entry (send back to draft)
     */
    fun reject(reason: String): JournalEntry {
        if (status != JournalEntryStatus.PENDING_APPROVAL) {
            throw IllegalJournalEntryOperationException("Can only reject entries pending approval")
        }
        
        return copy(
            status = JournalEntryStatus.DRAFT,
            reference = (reference ?: "") + "\nRejection reason: $reason",
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Post entry to general ledger
     */
    fun post(postedBy: UUID, postingDate: LocalDate = LocalDate.now()): JournalEntry {
        if (status != JournalEntryStatus.APPROVED) {
            throw IllegalJournalEntryOperationException("Can only post approved entries")
        }
        
        if (!validateIsBalanced()) {
            throw UnbalancedJournalEntryException("Cannot post unbalanced journal entry")
        }
        
        return copy(
            status = JournalEntryStatus.POSTED,
            postingDate = postingDate,
            postedBy = postedBy,
            postedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ).also {
            // Publish domain events for each account line
            accountLines.forEach { line ->
                // AccountBalanceUpdatedEvent would be published here
            }
        }
    }
    
    /**
     * Create reversal entry
     */
    fun createReversalEntry(
        reversalDate: LocalDate,
        reversalReason: String,
        createdBy: UUID
    ): JournalEntry {
        if (status != JournalEntryStatus.POSTED) {
            throw IllegalJournalEntryOperationException("Can only reverse posted entries")
        }
        
        // Create opposite entries (debits become credits, credits become debits)
        val reversalLines = accountLines.map { line ->
            JournalEntryLine(
                accountCode = line.accountCode,
                debitAmount = line.creditAmount, // Swap debit/credit
                creditAmount = line.debitAmount,
                description = "Reversal: ${line.description}",
                reference = line.reference
            )
        }
        
        return JournalEntry(
            entryNumber = generateReversalEntryNumber(),
            entryDate = reversalDate,
            postingDate = reversalDate,
            entryType = JournalEntryType.REVERSAL,
            description = "Reversal of ${this.entryNumber}: $reversalReason",
            reference = "Reversal of entry ${this.entryNumber}",
            totalDebitAmount = this.totalCreditAmount, // Swap totals
            totalCreditAmount = this.totalDebitAmount,
            accountLines = reversalLines,
            sourceName = "REVERSAL",
            sourceId = this.entryId,
            fiscalPeriodId = this.fiscalPeriodId,
            fiscalYear = this.fiscalYear,
            fiscalPeriod = this.fiscalPeriod,
            reversalOfEntryId = this.entryId,
            isReversalEntry = true,
            createdBy = createdBy
        )
    }
    
    /**
     * Check if entry can be modified
     */
    fun canBeModified(): Boolean {
        return status == JournalEntryStatus.DRAFT
    }
    
    /**
     * Check if entry is balanced
     */
    fun isBalanced(): Boolean {
        return validateIsBalanced()
    }
    
    /**
     * Get variance between debits and credits
     */
    fun getVariance(): FinancialAmount {
        return totalDebitAmount.subtract(totalCreditAmount)
    }
    
    /**
     * Get all debit lines
     */
    fun getDebitLines(): List<JournalEntryLine> {
        return accountLines.filter { it.isDebit() }
    }
    
    /**
     * Get all credit lines
     */
    fun getCreditLines(): List<JournalEntryLine> {
        return accountLines.filter { it.isCredit() }
    }
    
    // Private helper methods
    private fun validateCanModify() {
        if (!canBeModified()) {
            throw IllegalJournalEntryOperationException("Cannot modify journal entry in status: $status")
        }
    }
    
    private fun validateCurrencyMatch(amount: FinancialAmount) {
        if (amount.currency != totalDebitAmount.currency) {
            throw CurrencyMismatchException("Amount currency ${amount.currency} does not match entry currency ${totalDebitAmount.currency}")
        }
    }
    
    private fun recalculateTotals(updatedLines: List<JournalEntryLine>): JournalEntry {
        val newTotalDebits = updatedLines
            .filter { it.isDebit() }
            .fold(FinancialAmount.ZERO) { acc, line -> acc.add(line.debitAmount) }
            
        val newTotalCredits = updatedLines
            .filter { it.isCredit() }
            .fold(FinancialAmount.ZERO) { acc, line -> acc.add(line.creditAmount) }
        
        return copy(
            accountLines = updatedLines,
            totalDebitAmount = newTotalDebits,
            totalCreditAmount = newTotalCredits,
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun generateReversalEntryNumber(): String {
        return "REV-${entryNumber}-${LocalDate.now().toString().replace("-", "")}"
    }
    
    companion object {
        /**
         * Create a new journal entry
         */
        fun create(
            entryNumber: String,
            entryDate: LocalDate,
            entryType: JournalEntryType,
            description: String,
            currency: Currency,
            sourceName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int,
            createdBy: UUID,
            reference: String? = null,
            sourceId: UUID? = null
        ): JournalEntry {
            
            return JournalEntry(
                entryNumber = entryNumber,
                entryDate = entryDate,
                postingDate = entryDate,
                entryType = entryType,
                description = description,
                reference = reference,
                totalDebitAmount = FinancialAmount(BigDecimal.ZERO, currency),
                totalCreditAmount = FinancialAmount(BigDecimal.ZERO, currency),
                sourceName = sourceName,
                sourceId = sourceId,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                createdBy = createdBy
            )
        }
    }
}

/**
 * Journal Entry Types
 */
enum class JournalEntryType {
    MANUAL,
    AUTOMATIC,
    INVOICE,
    PAYMENT,
    PAYROLL,
    DEPRECIATION,
    ADJUSTMENT,
    CLOSING,
    REVERSAL,
    ACCRUAL,
    PREPAYMENT
}

/**
 * Journal Entry Status
 */
enum class JournalEntryStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    POSTED,
    REJECTED,
    REVERSED
}

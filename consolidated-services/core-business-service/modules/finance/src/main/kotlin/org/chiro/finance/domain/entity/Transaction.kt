package org.chiro.finance.domain.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.chiro.finance.domain.valueobject.Money
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Transaction Entity - Journal Entry Aggregate Root
 * 
 * Represents a financial transaction following double-entry bookkeeping principles:
 * - Every transaction must have at least two transaction lines
 * - Total debits must equal total credits
 * - Transactions are immutable once posted
 * - Supports multi-currency transactions with proper conversion
 * 
 * Features:
 * - Automatic balance validation (debits = credits)
 * - Transaction status workflow (DRAFT → POSTED → REVERSED)
 * - Audit trail with user tracking
 * - Multi-currency support with conversion rates
 * - Reference document linking
 * - Automatic sequential numbering
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Entity
@Table(
    name = "transactions", 
    schema = "finance",
    indexes = [
        Index(name = "idx_transaction_number", columnList = "transaction_number", unique = true),
        Index(name = "idx_transaction_date", columnList = "transaction_date"),
        Index(name = "idx_transaction_status", columnList = "transaction_status"),
        Index(name = "idx_transaction_type", columnList = "transaction_type"),
        Index(name = "idx_reference_number", columnList = "reference_number"),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
class Transaction : PanacheEntityBase {
    
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID()
    
    @field:NotBlank(message = "Transaction number is required")
    @field:Size(max = 50, message = "Transaction number cannot exceed 50 characters")
    @Column(name = "transaction_number", unique = true, nullable = false, length = 50)
    lateinit var transactionNumber: String
    
    @field:NotNull(message = "Transaction date is required")
    @Column(name = "transaction_date", nullable = false)
    lateinit var transactionDate: LocalDate
    
    @field:NotBlank(message = "Description is required")
    @field:Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    @Column(name = "description", nullable = false, length = 500)
    lateinit var description: String
    
    @field:Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    var notes: String? = null
    
    @field:NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    lateinit var transactionType: TransactionType
    
    @field:NotNull(message = "Transaction status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false, length = 20)
    var transactionStatus: TransactionStatus = TransactionStatus.DRAFT
    
    // Reference to source document
    @field:Size(max = 100, message = "Reference number cannot exceed 100 characters")
    @Column(name = "reference_number", length = 100)
    var referenceNumber: String? = null
    
    @field:Size(max = 50, message = "Reference type cannot exceed 50 characters")
    @Column(name = "reference_type", length = 50)
    var referenceType: String? = null
    
    // Transaction Lines (Double-entry bookkeeping)
    @OneToMany(mappedBy = "transaction", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var transactionLines: MutableSet<TransactionLine> = mutableSetOf()
    
    // Base Currency for the transaction
    @field:NotBlank(message = "Base currency is required")
    @field:Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Column(name = "base_currency", nullable = false, length = 3)
    var baseCurrency: String = Money.DEFAULT_CURRENCY
    
    // Totals in base currency
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_debit_amount", precision = 19, scale = 4)),
        AttributeOverride(name = "currencyCode", column = Column(name = "total_debit_currency", length = 3))
    )
    var totalDebit: Money = Money.zero()
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_credit_amount", precision = 19, scale = 4)),
        AttributeOverride(name = "currencyCode", column = Column(name = "total_credit_currency", length = 3))
    )
    var totalCredit: Money = Money.zero()
    
    // Audit Fields
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
    
    @Column(name = "posted_at")
    var postedAt: LocalDateTime? = null
    
    @field:Size(max = 100, message = "Created by cannot exceed 100 characters")
    @Column(name = "created_by", length = 100)
    var createdBy: String? = null
    
    @field:Size(max = 100, message = "Updated by cannot exceed 100 characters")
    @Column(name = "updated_by", length = 100)
    var updatedBy: String? = null
    
    @field:Size(max = 100, message = "Posted by cannot exceed 100 characters")
    @Column(name = "posted_by", length = 100)
    var postedBy: String? = null
    
    // Reversal Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversed_transaction_id")
    var reversedTransaction: Transaction? = null
    
    @OneToOne(mappedBy = "reversedTransaction", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var reversalTransaction: Transaction? = null
    
    @Column(name = "reversal_reason", length = 500)
    var reversalReason: String? = null
    
    // ===================== BUSINESS LOGIC =====================
    
    /**
     * Adds a transaction line to this transaction
     */
    fun addTransactionLine(transactionLine: TransactionLine) {
        require(transactionStatus == TransactionStatus.DRAFT) {
            "Cannot modify posted or reversed transactions"
        }
        require(transactionLine.amount.currencyCode == baseCurrency || transactionLine.exchangeRate != null) {
            "Transaction line currency must match base currency or have exchange rate specified"
        }
        
        transactionLine.transaction = this
        transactionLines.add(transactionLine)
        recalculateTotals()
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Removes a transaction line from this transaction
     */
    fun removeTransactionLine(transactionLine: TransactionLine) {
        require(transactionStatus == TransactionStatus.DRAFT) {
            "Cannot modify posted or reversed transactions"
        }
        
        transactionLines.remove(transactionLine)
        recalculateTotals()
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Recalculates total debits and credits
     */
    private fun recalculateTotals() {
        var debitTotal = Money.zero(baseCurrency)
        var creditTotal = Money.zero(baseCurrency)
        
        for (line in transactionLines) {
            val amountInBaseCurrency = if (line.amount.currencyCode == baseCurrency) {
                line.amount
            } else {
                line.amount.convertTo(baseCurrency, line.exchangeRate!!)
            }
            
            if (line.debitAmount != null) {
                debitTotal += amountInBaseCurrency
            } else {
                creditTotal += amountInBaseCurrency
            }
        }
        
        totalDebit = debitTotal
        totalCredit = creditTotal
    }
    
    /**
     * Validates that the transaction is balanced (debits = credits)
     */
    fun isBalanced(): Boolean = totalDebit == totalCredit
    
    /**
     * Validates that the transaction has minimum required lines
     */
    fun hasMinimumLines(): Boolean = transactionLines.size >= 2
    
    /**
     * Validates the entire transaction
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (!hasMinimumLines()) {
            errors.add("Transaction must have at least 2 lines")
        }
        
        if (!isBalanced()) {
            errors.add("Transaction is not balanced: Debits ($totalDebit) ≠ Credits ($totalCredit)")
        }
        
        // Validate each transaction line
        for ((index, line) in transactionLines.withIndex()) {
            if (line.account.accountStatus != AccountStatus.ACTIVE) {
                errors.add("Line ${index + 1}: Account ${line.account.accountCode} is not active")
            }
            
            if (!line.account.allowManualEntries && transactionType == TransactionType.MANUAL) {
                errors.add("Line ${index + 1}: Account ${line.account.accountCode} does not allow manual entries")
            }
        }
        
        return errors
    }
    
    /**
     * Posts the transaction (makes it immutable)
     */
    fun post(postedBy: String) {
        require(transactionStatus == TransactionStatus.DRAFT) {
            "Only draft transactions can be posted"
        }
        
        val validationErrors = validate()
        require(validationErrors.isEmpty()) {
            "Cannot post invalid transaction: ${validationErrors.joinToString(", ")}"
        }
        
        transactionStatus = TransactionStatus.POSTED
        postedAt = LocalDateTime.now()
        this.postedBy = postedBy
        updatedAt = LocalDateTime.now()
        
        // Update account balances
        updateAccountBalances()
    }
    
    /**
     * Updates account balances when transaction is posted
     */
    private fun updateAccountBalances() {
        for (line in transactionLines) {
            val amountInAccountCurrency = if (line.amount.currencyCode == line.account.balance.currencyCode) {
                line.amount
            } else {
                // Convert to account's currency if different
                line.amount.convertTo(line.account.balance.currencyCode, line.exchangeRate!!)
            }
            
            if (line.debitAmount != null) {
                line.account.addToBalance(amountInAccountCurrency)
            } else {
                line.account.subtractFromBalance(amountInAccountCurrency)
            }
        }
    }
    
    /**
     * Reverses the transaction
     */
    fun reverse(reason: String, reversedBy: String): Transaction {
        require(transactionStatus == TransactionStatus.POSTED) {
            "Only posted transactions can be reversed"
        }
        require(reversalTransaction == null) {
            "Transaction is already reversed"
        }
        
        val reversalTxn = Transaction().apply {
            transactionNumber = generateReversalNumber()
            transactionDate = LocalDate.now()
            description = "REVERSAL: $description"
            notes = "Reversal of transaction $transactionNumber. Reason: $reason"
            transactionType = TransactionType.REVERSAL
            baseCurrency = this@Transaction.baseCurrency
            reversedTransaction = this@Transaction
            createdBy = reversedBy
        }
        
        // Create reversal lines (opposite debits/credits)
        for (originalLine in transactionLines) {
            val reversalLine = TransactionLine().apply {
                account = originalLine.account
                amount = originalLine.amount
                exchangeRate = originalLine.exchangeRate
                description = "REVERSAL: ${originalLine.description}"
                
                // Flip debit/credit
                if (originalLine.debitAmount != null) {
                    creditAmount = originalLine.debitAmount
                } else {
                    debitAmount = originalLine.creditAmount
                }
            }
            reversalTxn.addTransactionLine(reversalLine)
        }
        
        reversalTxn.post(reversedBy)
        
        // Update this transaction
        reversalTransaction = reversalTxn
        reversalReason = reason
        transactionStatus = TransactionStatus.REVERSED
        updatedAt = LocalDateTime.now()
        
        return reversalTxn
    }
    
    /**
     * Generates a reversal transaction number
     */
    private fun generateReversalNumber(): String = "${transactionNumber}-REV"
    
    /**
     * Creates a copy of this transaction for editing
     */
    fun createCopy(newTransactionNumber: String): Transaction {
        val copy = Transaction().apply {
            transactionNumber = newTransactionNumber
            transactionDate = this@Transaction.transactionDate
            description = "COPY: ${this@Transaction.description}"
            notes = this@Transaction.notes
            transactionType = this@Transaction.transactionType
            baseCurrency = this@Transaction.baseCurrency
            referenceNumber = this@Transaction.referenceNumber
            referenceType = this@Transaction.referenceType
        }
        
        // Copy transaction lines
        for (originalLine in transactionLines) {
            val copiedLine = TransactionLine().apply {
                account = originalLine.account
                amount = originalLine.amount
                exchangeRate = originalLine.exchangeRate
                description = originalLine.description
                debitAmount = originalLine.debitAmount
                creditAmount = originalLine.creditAmount
            }
            copy.addTransactionLine(copiedLine)
        }
        
        return copy
    }
    
    val isReversed: Boolean get() = transactionStatus == TransactionStatus.REVERSED
    val isPosted: Boolean get() = transactionStatus == TransactionStatus.POSTED
    val isDraft: Boolean get() = transactionStatus == TransactionStatus.DRAFT
    
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
    
    override fun toString(): String = "Transaction(number='$transactionNumber', date=$transactionDate, status=$transactionStatus, balance=${totalDebit})"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transaction) return false
        return id == other.id
    }
    
    override fun hashCode(): Int = id.hashCode()
}

/**
 * Transaction Type Enumeration
 */
enum class TransactionType(val description: String) {
    MANUAL("Manual Journal Entry"),
    SALES("Sales Transaction"),
    PURCHASE("Purchase Transaction"),
    PAYMENT("Payment Transaction"),
    RECEIPT("Receipt Transaction"),
    PAYROLL("Payroll Transaction"),
    ADJUSTMENT("Adjustment Entry"),
    ACCRUAL("Accrual Entry"),
    REVERSAL("Reversal Entry"),
    OPENING("Opening Balance"),
    CLOSING("Closing Entry"),
    DEPRECIATION("Depreciation Entry"),
    REVALUATION("Currency Revaluation"),
    TRANSFER("Transfer Entry"),
    SYSTEM("System Generated")
}

/**
 * Transaction Status Enumeration
 */
enum class TransactionStatus {
    DRAFT,      // Transaction is being prepared
    POSTED,     // Transaction is posted and affects balances
    REVERSED    // Transaction has been reversed
}

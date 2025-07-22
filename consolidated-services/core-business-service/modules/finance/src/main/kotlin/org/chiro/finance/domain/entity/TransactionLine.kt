package org.chiro.finance.domain.entity

import org.chiro.finance.domain.valueobject.Money
import java.util.UUID
import java.time.Instant
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase

/**
 * Transaction Line Entity - Individual debit/credit entries in a journal entry
 * Part of the Transaction aggregate following double-entry bookkeeping principles
 */
@Entity
@Table(
    name = "transaction_lines", 
    schema = "finance",
    indexes = [
        Index(name = "idx_transaction_lines_transaction", columnList = "transaction_id"),
        Index(name = "idx_transaction_lines_account", columnList = "account_id")
    ]
)
class TransactionLine : PanacheEntityBase {
    
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()
    
    @field:NotNull
    @Column(name = "transaction_id", nullable = false)
    lateinit var transactionId: UUID
    
    @field:NotNull  
    @Column(name = "account_id", nullable = false)
    lateinit var accountId: UUID
    
    @field:NotNull
    @field:PositiveOrZero
    @Column(name = "line_number", nullable = false)
    var lineNumber: Int = 0
    
    @Column(name = "description", length = 255)
    var description: String? = null
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "debit_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "currency_code"))
    )
    var debitAmount: Money = Money.zero()
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "credit_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "currency_code", insertable = false, updatable = false))
    )
    var creditAmount: Money = Money.zero()
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
    
    @Version
    @Column(name = "version")
    var version: Long = 0
    
    // Domain logic
    fun isDebit(): Boolean = debitAmount.isPositive()
    fun isCre(): Boolean = creditAmount.isPositive()
    fun isBalanced(): Boolean = debitAmount.isZero() != creditAmount.isZero() // One must be zero, one must be non-zero
    
    fun getAmount(): Money = if (isDebit()) debitAmount else creditAmount
    
    companion object {
        fun createDebit(
            transactionId: UUID,
            accountId: UUID, 
            lineNumber: Int,
            amount: Money,
            description: String? = null
        ): TransactionLine {
            return TransactionLine().apply {
                this.transactionId = transactionId
                this.accountId = accountId
                this.lineNumber = lineNumber
                this.debitAmount = amount
                this.creditAmount = Money.zero(amount.currencyCode)
                this.description = description
                this.createdAt = Instant.now()
            }
        }
        
        fun createCredit(
            transactionId: UUID,
            accountId: UUID,
            lineNumber: Int, 
            amount: Money,
            description: String? = null
        ): TransactionLine {
            return TransactionLine().apply {
                this.transactionId = transactionId
                this.accountId = accountId
                this.lineNumber = lineNumber
                this.debitAmount = Money.zero(amount.currencyCode)
                this.creditAmount = amount
                this.description = description
                this.createdAt = Instant.now()
            }
        }
    }
}
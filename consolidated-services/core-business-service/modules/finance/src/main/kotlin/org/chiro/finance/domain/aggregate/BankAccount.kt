package org.chiro.finance.domain.aggregate

import org.chiro.finance.domain.valueobject.*
import org.chiro.finance.domain.entity.*
import org.chiro.finance.domain.event.*
import org.chiro.finance.domain.exception.*
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * BankAccount Aggregate Root
 * 
 * Core banking account aggregate that manages account balances, transactions,
 * and banking relationships within the financial domain.
 * 
 * Business Rules:
 * - Account balance cannot go below overdraft limit
 * - Account must have valid routing and account numbers
 * - Account status controls transaction capabilities
 * - Account currency determines transaction processing rules
 */
@Entity
@Table(name = "bank_accounts")
data class BankAccount(
    @Id
    val accountId: UUID = UUID.randomUUID(),
    
    @Embedded
    val accountNumber: AccountNumber,
    
    @Embedded
    val routingNumber: RoutingNumber,
    
    @Column(nullable = false)
    val accountName: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val accountType: BankAccountType,
    
    @Embedded
    val balance: FinancialAmount,
    
    @Embedded
    val overdraftLimit: FinancialAmount = FinancialAmount.ZERO,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AccountStatus = AccountStatus.ACTIVE,
    
    @Column(nullable = false)
    val bankName: String,
    
    @Column(nullable = false)
    val branchCode: String,
    
    @Column(nullable = false)
    val openedDate: LocalDateTime,
    
    @Column
    val closedDate: LocalDateTime? = null,
    
    @Column(nullable = false)
    val lastTransactionDate: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val isReconciledToday: Boolean = false,
    
    @ElementCollection
    @CollectionTable(name = "bank_account_signatories")
    val authorizedSignatories: Set<String> = emptySet(),
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    /**
     * Debit the account with the specified amount
     * Business rule: Cannot exceed available balance + overdraft limit
     */
    fun debit(amount: FinancialAmount, description: String, transactionDate: LocalDateTime = LocalDateTime.now()): BankAccount {
        validateAccountIsActive()
        validateCurrencyMatch(amount)
        
        val availableBalance = balance.add(overdraftLimit)
        if (amount.isGreaterThan(availableBalance)) {
            throw InsufficientFundsException("Insufficient funds. Available: ${availableBalance.amount}, Requested: ${amount.amount}")
        }
        
        val newBalance = balance.subtract(amount)
        
        return copy(
            balance = newBalance,
            lastTransactionDate = transactionDate,
            updatedAt = LocalDateTime.now()
        ).also {
            // Publish domain event
            // AccountBalanceUpdatedEvent would be published here
        }
    }
    
    /**
     * Credit the account with the specified amount
     */
    fun credit(amount: FinancialAmount, description: String, transactionDate: LocalDateTime = LocalDateTime.now()): BankAccount {
        validateAccountIsActive()
        validateCurrencyMatch(amount)
        
        val newBalance = balance.add(amount)
        
        return copy(
            balance = newBalance,
            lastTransactionDate = transactionDate,
            updatedAt = LocalDateTime.now()
        ).also {
            // Publish domain event
            // AccountBalanceUpdatedEvent would be published here
        }
    }
    
    /**
     * Transfer funds to another bank account
     */
    fun transferTo(targetAccount: BankAccount, amount: FinancialAmount, description: String): Pair<BankAccount, BankAccount> {
        val debitedAccount = this.debit(amount, "Transfer to ${targetAccount.accountNumber.value}: $description")
        val creditedAccount = targetAccount.credit(amount, "Transfer from ${this.accountNumber.value}: $description")
        
        return Pair(debitedAccount, creditedAccount)
    }
    
    /**
     * Close the account
     * Business rule: Account balance must be zero to close
     */
    fun close(closureDate: LocalDateTime = LocalDateTime.now()): BankAccount {
        validateAccountIsActive()
        
        if (!balance.isZero()) {
            throw AccountClosureException("Cannot close account with non-zero balance: ${balance.amount}")
        }
        
        return copy(
            status = AccountStatus.CLOSED,
            closedDate = closureDate,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Freeze the account (prevent transactions)
     */
    fun freeze(): BankAccount {
        validateAccountIsActive()
        
        return copy(
            status = AccountStatus.FROZEN,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Unfreeze the account
     */
    fun unfreeze(): BankAccount {
        if (status != AccountStatus.FROZEN) {
            throw IllegalAccountOperationException("Cannot unfreeze account that is not frozen")
        }
        
        return copy(
            status = AccountStatus.ACTIVE,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Add authorized signatory
     */
    fun addSignatory(signatoryId: String): BankAccount {
        validateAccountIsActive()
        
        return copy(
            authorizedSignatories = authorizedSignatories + signatoryId,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Remove authorized signatory
     * Business rule: Must maintain at least one signatory
     */
    fun removeSignatory(signatoryId: String): BankAccount {
        validateAccountIsActive()
        
        if (authorizedSignatories.size <= 1) {
            throw IllegalAccountOperationException("Cannot remove last signatory from account")
        }
        
        return copy(
            authorizedSignatories = authorizedSignatories - signatoryId,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Update overdraft limit
     */
    fun updateOverdraftLimit(newLimit: FinancialAmount): BankAccount {
        validateAccountIsActive()
        validateCurrencyMatch(newLimit)
        
        return copy(
            overdraftLimit = newLimit,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Mark account as reconciled for today
     */
    fun markAsReconciled(): BankAccount {
        return copy(
            isReconciledToday = true,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Get available balance (current balance + overdraft limit)
     */
    fun getAvailableBalance(): FinancialAmount {
        return balance.add(overdraftLimit)
    }
    
    /**
     * Check if account can process transaction amount
     */
    fun canProcessTransaction(amount: FinancialAmount): Boolean {
        return status == AccountStatus.ACTIVE && 
               amount.currency == balance.currency &&
               amount.isLessThanOrEqualTo(getAvailableBalance())
    }
    
    /**
     * Check if signatory is authorized
     */
    fun isAuthorizedSignatory(signatoryId: String): Boolean {
        return authorizedSignatories.contains(signatoryId)
    }
    
    // Private validation methods
    private fun validateAccountIsActive() {
        if (status != AccountStatus.ACTIVE) {
            throw IllegalAccountOperationException("Account is not active. Current status: $status")
        }
    }
    
    private fun validateCurrencyMatch(amount: FinancialAmount) {
        if (amount.currency != balance.currency) {
            throw CurrencyMismatchException("Transaction currency ${amount.currency} does not match account currency ${balance.currency}")
        }
    }
    
    companion object {
        /**
         * Create a new bank account
         */
        fun create(
            accountNumber: AccountNumber,
            routingNumber: RoutingNumber,
            accountName: String,
            accountType: BankAccountType,
            initialBalance: FinancialAmount,
            bankName: String,
            branchCode: String,
            overdraftLimit: FinancialAmount = FinancialAmount.ZERO
        ): BankAccount {
            
            return BankAccount(
                accountNumber = accountNumber,
                routingNumber = routingNumber,
                accountName = accountName,
                accountType = accountType,
                balance = initialBalance,
                overdraftLimit = overdraftLimit,
                bankName = bankName,
                branchCode = branchCode,
                openedDate = LocalDateTime.now()
            )
        }
    }
}

/**
 * Bank Account Types
 */
enum class BankAccountType {
    CHECKING,
    SAVINGS,
    MONEY_MARKET,
    CERTIFICATE_OF_DEPOSIT,
    BUSINESS_CHECKING,
    BUSINESS_SAVINGS,
    ESCROW,
    TRUST
}

/**
 * Account Status
 */
enum class AccountStatus {
    ACTIVE,
    INACTIVE,
    FROZEN,
    CLOSED,
    PENDING_CLOSURE
}

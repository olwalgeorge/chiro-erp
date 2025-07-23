package org.chiro.finance.domain.exception

import org.chiro.finance.domain.valueobject.FinancialAmount
import org.chiro.finance.domain.valueobject.AccountCode
import java.util.*

/**
 * InsufficientFundsException
 * 
 * Domain exception thrown when an operation cannot be completed due to
 * insufficient funds in an account. This exception is part of the finance
 * domain's business rule enforcement.
 * 
 * This exception is thrown when:
 * - A payment cannot be processed due to insufficient account balance
 * - A withdrawal exceeds the available account balance
 * - A transfer cannot be completed due to insufficient source funds
 * - Credit limit validation fails for customer accounts
 * - Budget allocation exceeds available budget funds
 */
class InsufficientFundsException : FinanceDomainException {
    
    val accountId: UUID
    val accountCode: AccountCode?
    val accountName: String?
    val requestedAmount: FinancialAmount
    val availableAmount: FinancialAmount
    val shortfallAmount: FinancialAmount
    val operationType: String
    val transactionId: UUID?
    val customerId: UUID?
    val customerName: String?
    
    constructor(
        accountId: UUID,
        requestedAmount: FinancialAmount,
        availableAmount: FinancialAmount,
        operationType: String,
        message: String = "Insufficient funds for requested operation"
    ) : super(message) {
        this.accountId = accountId
        this.accountCode = null
        this.accountName = null
        this.requestedAmount = requestedAmount
        this.availableAmount = availableAmount
        this.shortfallAmount = requestedAmount.subtract(availableAmount)
        this.operationType = operationType
        this.transactionId = null
        this.customerId = null
        this.customerName = null
    }
    
    constructor(
        accountId: UUID,
        accountCode: AccountCode,
        accountName: String,
        requestedAmount: FinancialAmount,
        availableAmount: FinancialAmount,
        operationType: String,
        transactionId: UUID? = null,
        message: String = "Insufficient funds in account ${accountCode.code} ($accountName)"
    ) : super(message) {
        this.accountId = accountId
        this.accountCode = accountCode
        this.accountName = accountName
        this.requestedAmount = requestedAmount
        this.availableAmount = availableAmount
        this.shortfallAmount = requestedAmount.subtract(availableAmount)
        this.operationType = operationType
        this.transactionId = transactionId
        this.customerId = null
        this.customerName = null
    }
    
    constructor(
        accountId: UUID,
        accountCode: AccountCode,
        accountName: String,
        customerId: UUID,
        customerName: String,
        requestedAmount: FinancialAmount,
        availableAmount: FinancialAmount,
        operationType: String,
        transactionId: UUID? = null,
        message: String = "Insufficient funds for customer $customerName (Account: ${accountCode.code})"
    ) : super(message) {
        this.accountId = accountId
        this.accountCode = accountCode
        this.accountName = accountName
        this.requestedAmount = requestedAmount
        this.availableAmount = availableAmount
        this.shortfallAmount = requestedAmount.subtract(availableAmount)
        this.operationType = operationType
        this.transactionId = transactionId
        this.customerId = customerId
        this.customerName = customerName
    }
    
    /**
     * Get the percentage shortfall of the requested amount
     */
    fun getShortfallPercentage(): Double {
        return if (requestedAmount.isPositive()) {
            (shortfallAmount.amount.toDouble() / requestedAmount.amount.toDouble()) * 100
        } else {
            0.0
        }
    }
    
    /**
     * Check if this is a minor shortfall (less than 5%)
     */
    fun isMinorShortfall(): Boolean {
        return getShortfallPercentage() < 5.0
    }
    
    /**
     * Check if this is a significant shortfall (more than 50%)
     */
    fun isSignificantShortfall(): Boolean {
        return getShortfallPercentage() > 50.0
    }
    
    /**
     * Get suggested resolution actions
     */
    fun getSuggestedResolutions(): List<String> {
        val suggestions = mutableListOf<String>()
        
        when {
            isMinorShortfall() -> {
                suggestions.add("Consider partial payment processing")
                suggestions.add("Check for pending deposits or credits")
                suggestions.add("Review account for rounding differences")
            }
            isSignificantShortfall() -> {
                suggestions.add("Obtain additional funding authorization")
                suggestions.add("Consider credit line extension")
                suggestions.add("Split transaction into multiple payments")
                suggestions.add("Review customer payment terms")
            }
            else -> {
                suggestions.add("Transfer funds from another account")
                suggestions.add("Wait for pending deposits to clear")
                suggestions.add("Request customer payment")
            }
        }
        
        if (operationType.contains("PAYMENT", ignoreCase = true)) {
            suggestions.add("Consider payment plan arrangement")
            suggestions.add("Review payment due dates")
        }
        
        if (customerId != null) {
            suggestions.add("Contact customer for payment")
            suggestions.add("Review customer credit terms")
        }
        
        return suggestions
    }
    
    override fun getErrorCode(): String = "INSUFFICIENT_FUNDS"
    
    override fun getErrorCategory(): String = "BUSINESS_RULE_VIOLATION"
    
    override fun getBusinessImpact(): String = when {
        isSignificantShortfall() -> "HIGH - Transaction cannot proceed, customer service impact"
        isMinorShortfall() -> "LOW - Minor shortfall, may be resolvable"
        else -> "MEDIUM - Requires attention and resolution"
    }
    
    override fun getRecommendedAction(): String = getSuggestedResolutions().firstOrNull() 
        ?: "Review account balance and funding sources"
    
    override fun isRetryable(): Boolean = true
    
    override fun getRetryDelay(): Long = if (isMinorShortfall()) 30000 else 300000 // 30 seconds or 5 minutes
    
    override fun requiresEscalation(): Boolean = isSignificantShortfall()
    
    companion object {
        /**
         * Create exception for payment processing failure
         */
        fun forPaymentProcessing(
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            paymentAmount: FinancialAmount,
            availableBalance: FinancialAmount,
            transactionId: UUID
        ): InsufficientFundsException {
            return InsufficientFundsException(
                accountId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                requestedAmount = paymentAmount,
                availableAmount = availableBalance,
                operationType = "PAYMENT_PROCESSING",
                transactionId = transactionId,
                message = "Payment of ${paymentAmount.amount} ${paymentAmount.currency} cannot be processed - insufficient funds in account ${accountCode.code}. Available: ${availableBalance.amount} ${availableBalance.currency}"
            )
        }
        
        /**
         * Create exception for customer credit limit exceeded
         */
        fun forCreditLimitExceeded(
            customerId: UUID,
            customerName: String,
            requestedCredit: FinancialAmount,
            availableCredit: FinancialAmount,
            transactionId: UUID
        ): InsufficientFundsException {
            return InsufficientFundsException(
                accountId = customerId, // Using customer ID as account ID for credit scenarios
                accountCode = null,
                accountName = "Customer Credit Account",
                customerId = customerId,
                customerName = customerName,
                requestedAmount = requestedCredit,
                availableAmount = availableCredit,
                operationType = "CREDIT_AUTHORIZATION",
                transactionId = transactionId,
                message = "Credit limit exceeded for customer $customerName. Requested: ${requestedCredit.amount} ${requestedCredit.currency}, Available: ${availableCredit.amount} ${availableCredit.currency}"
            )
        }
        
        /**
         * Create exception for budget allocation failure
         */
        fun forBudgetAllocation(
            budgetAccountId: UUID,
            budgetAccountCode: AccountCode,
            budgetAccountName: String,
            requestedAllocation: FinancialAmount,
            availableBudget: FinancialAmount
        ): InsufficientFundsException {
            return InsufficientFundsException(
                accountId = budgetAccountId,
                accountCode = budgetAccountCode,
                accountName = budgetAccountName,
                requestedAmount = requestedAllocation,
                availableAmount = availableBudget,
                operationType = "BUDGET_ALLOCATION",
                message = "Budget allocation of ${requestedAllocation.amount} ${requestedAllocation.currency} exceeds available budget in ${budgetAccountCode.code} ($budgetAccountName). Available: ${availableBudget.amount} ${availableBudget.currency}"
            )
        }
        
        /**
         * Create exception for fund transfer failure
         */
        fun forFundTransfer(
            sourceAccountId: UUID,
            sourceAccountCode: AccountCode,
            sourceAccountName: String,
            transferAmount: FinancialAmount,
            availableBalance: FinancialAmount,
            transactionId: UUID
        ): InsufficientFundsException {
            return InsufficientFundsException(
                accountId = sourceAccountId,
                accountCode = sourceAccountCode,
                accountName = sourceAccountName,
                requestedAmount = transferAmount,
                availableAmount = availableBalance,
                operationType = "FUND_TRANSFER",
                transactionId = transactionId,
                message = "Fund transfer of ${transferAmount.amount} ${transferAmount.currency} cannot be completed - insufficient balance in source account ${sourceAccountCode.code} ($sourceAccountName). Available: ${availableBalance.amount} ${availableBalance.currency}"
            )
        }
    }
}

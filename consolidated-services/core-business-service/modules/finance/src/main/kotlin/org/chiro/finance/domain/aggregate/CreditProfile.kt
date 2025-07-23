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
 * CreditProfile Aggregate Root
 * 
 * Core credit management aggregate that tracks customer creditworthiness,
 * credit limits, payment history, and risk assessment within the financial domain.
 * 
 * Business Rules:
 * - Credit limit cannot be negative
 * - Credit utilization ratio affects credit status
 * - Payment history influences credit score calculations
 * - Credit holds prevent new credit extensions
 * - Risk assessment must be updated periodically
 */
@Entity
@Table(name = "credit_profiles")
data class CreditProfile(
    @Id
    val profileId: UUID = UUID.randomUUID(),
    
    @Column(nullable = false, unique = true)
    val customerId: UUID,
    
    @Column(nullable = false)
    val customerName: String,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "credit_limit_amount")),
        AttributeOverride(name = "currency", column = Column(name = "credit_limit_currency"))
    )
    val creditLimit: FinancialAmount,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "available_credit_amount")),
        AttributeOverride(name = "currency", column = Column(name = "available_credit_currency"))
    )
    val availableCredit: FinancialAmount,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "outstanding_balance_amount")),
        AttributeOverride(name = "currency", column = Column(name = "outstanding_balance_currency"))
    )
    val outstandingBalance: FinancialAmount = FinancialAmount.ZERO,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "overdue_balance_amount")),
        AttributeOverride(name = "currency", column = Column(name = "overdue_balance_currency"))
    )
    val overdueBalance: FinancialAmount = FinancialAmount.ZERO,
    
    @Column(nullable = false)
    val creditScore: Int = 0,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val creditRating: CreditRating = CreditRating.UNRATED,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val creditStatus: CreditStatus = CreditStatus.ACTIVE,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val riskLevel: RiskLevel = RiskLevel.MEDIUM,
    
    @Embedded
    val paymentTerms: PaymentTerms,
    
    @Column(nullable = false)
    val paymentHistoryScore: Double = 0.0, // 0.0 to 100.0
    
    @Column(nullable = false)
    val creditUtilizationRatio: Double = 0.0, // 0.0 to 1.0
    
    @Column(nullable = false)
    val daysSalesOutstanding: Int = 0,
    
    @Column(nullable = false)
    val totalInvoicesCount: Long = 0L,
    
    @Column(nullable = false)
    val paidOnTimeCount: Long = 0L,
    
    @Column(nullable = false)
    val latePaymentCount: Long = 0L,
    
    @Column(nullable = false)
    val overdueInvoicesCount: Long = 0L,
    
    @Column
    val lastPaymentDate: LocalDate? = null,
    
    @Column
    val lastCreditReviewDate: LocalDate? = null,
    
    @Column
    val nextCreditReviewDate: LocalDate? = null,
    
    @Column(nullable = false)
    val isOnCreditHold: Boolean = false,
    
    @Column
    val creditHoldReason: String? = null,
    
    @Column
    val creditHoldDate: LocalDateTime? = null,
    
    @ElementCollection
    @CollectionTable(name = "credit_profile_alerts")
    val creditAlerts: Set<CreditAlert> = emptySet(),
    
    @ElementCollection
    @CollectionTable(name = "credit_profile_history")
    val creditLimitHistory: List<CreditLimitChange> = emptyList(),
    
    @Column(nullable = false)
    val accountOpenDate: LocalDate,
    
    @Column
    val accountCloseDate: LocalDate? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val lastCreditScoreUpdateAt: LocalDateTime? = null
) {

    /**
     * Update credit limit
     */
    fun updateCreditLimit(
        newLimit: FinancialAmount,
        reason: String,
        approvedBy: UUID
    ): CreditProfile {
        validateCreditLimitChange(newLimit)
        
        val limitChange = CreditLimitChange(
            oldLimit = creditLimit,
            newLimit = newLimit,
            reason = reason,
            approvedBy = approvedBy,
            changeDate = LocalDateTime.now()
        )
        
        val newAvailableCredit = newLimit.subtract(outstandingBalance)
        
        return copy(
            creditLimit = newLimit,
            availableCredit = newAvailableCredit,
            creditLimitHistory = creditLimitHistory + limitChange,
            creditUtilizationRatio = calculateUtilizationRatio(outstandingBalance, newLimit),
            updatedAt = LocalDateTime.now()
        ).recalculateRiskLevel()
    }
    
    /**
     * Apply charge to outstanding balance
     */
    fun applyCharge(chargeAmount: FinancialAmount, invoiceNumber: String): CreditProfile {
        if (isOnCreditHold) {
            throw CreditHoldException("Cannot apply charges while customer is on credit hold")
        }
        
        validateCurrencyMatch(chargeAmount)
        
        val newOutstandingBalance = outstandingBalance.add(chargeAmount)
        val newAvailableCredit = creditLimit.subtract(newOutstandingBalance)
        
        if (newAvailableCredit.isNegative()) {
            throw CreditLimitExceededException("Charge would exceed credit limit. Available: ${availableCredit.amount}, Requested: ${chargeAmount.amount}")
        }
        
        return copy(
            outstandingBalance = newOutstandingBalance,
            availableCredit = newAvailableCredit,
            totalInvoicesCount = totalInvoicesCount + 1,
            creditUtilizationRatio = calculateUtilizationRatio(newOutstandingBalance, creditLimit),
            updatedAt = LocalDateTime.now()
        ).recalculateRiskLevel()
    }
    
    /**
     * Apply payment to outstanding balance
     */
    fun applyPayment(
        paymentAmount: FinancialAmount,
        paymentDate: LocalDate,
        isOnTime: Boolean
    ): CreditProfile {
        validateCurrencyMatch(paymentAmount)
        
        val newOutstandingBalance = outstandingBalance.subtract(paymentAmount)
        val newAvailableCredit = creditLimit.subtract(newOutstandingBalance)
        
        val updatedProfile = copy(
            outstandingBalance = if (newOutstandingBalance.isNegative()) FinancialAmount.ZERO else newOutstandingBalance,
            availableCredit = newAvailableCredit,
            lastPaymentDate = paymentDate,
            paidOnTimeCount = if (isOnTime) paidOnTimeCount + 1 else paidOnTimeCount,
            latePaymentCount = if (!isOnTime) latePaymentCount + 1 else latePaymentCount,
            creditUtilizationRatio = calculateUtilizationRatio(newOutstandingBalance, creditLimit),
            updatedAt = LocalDateTime.now()
        )
        
        return updatedProfile
            .recalculatePaymentHistoryScore()
            .recalculateRiskLevel()
            .updateCreditStatus()
    }
    
    /**
     * Mark invoice as overdue
     */
    fun markInvoiceOverdue(overdueAmount: FinancialAmount): CreditProfile {
        validateCurrencyMatch(overdueAmount)
        
        return copy(
            overdueBalance = overdueBalance.add(overdueAmount),
            overdueInvoicesCount = overdueInvoicesCount + 1,
            updatedAt = LocalDateTime.now()
        ).recalculateRiskLevel()
    }
    
    /**
     * Clear overdue amount when paid
     */
    fun clearOverdueAmount(clearedAmount: FinancialAmount): CreditProfile {
        validateCurrencyMatch(clearedAmount)
        
        val newOverdueBalance = overdueBalance.subtract(clearedAmount)
        val newOverdueCount = if (newOverdueBalance.isZero()) overdueInvoicesCount - 1 else overdueInvoicesCount
        
        return copy(
            overdueBalance = if (newOverdueBalance.isNegative()) FinancialAmount.ZERO else newOverdueBalance,
            overdueInvoicesCount = maxOf(0, newOverdueCount),
            updatedAt = LocalDateTime.now()
        ).recalculateRiskLevel()
    }
    
    /**
     * Place customer on credit hold
     */
    fun placeCreditHold(reason: String): CreditProfile {
        if (isOnCreditHold) {
            throw IllegalCreditOperationException("Customer is already on credit hold")
        }
        
        return copy(
            isOnCreditHold = true,
            creditHoldReason = reason,
            creditHoldDate = LocalDateTime.now(),
            creditStatus = CreditStatus.ON_HOLD,
            creditAlerts = creditAlerts + CreditAlert.CREDIT_HOLD_PLACED,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Release from credit hold
     */
    fun releaseCreditHold(releaseReason: String): CreditProfile {
        if (!isOnCreditHold) {
            throw IllegalCreditOperationException("Customer is not on credit hold")
        }
        
        return copy(
            isOnCreditHold = false,
            creditHoldReason = null,
            creditHoldDate = null,
            creditStatus = CreditStatus.ACTIVE,
            creditAlerts = creditAlerts - CreditAlert.CREDIT_HOLD_PLACED,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Add credit alert
     */
    fun addCreditAlert(alert: CreditAlert): CreditProfile {
        return copy(
            creditAlerts = creditAlerts + alert,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Remove credit alert
     */
    fun removeCreditAlert(alert: CreditAlert): CreditProfile {
        return copy(
            creditAlerts = creditAlerts - alert,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Perform credit review
     */
    fun performCreditReview(
        newCreditScore: Int,
        newCreditRating: CreditRating,
        reviewDate: LocalDate
    ): CreditProfile {
        val nextReviewDate = when (newCreditRating) {
            CreditRating.EXCELLENT -> reviewDate.plusYears(1)
            CreditRating.GOOD -> reviewDate.plusMonths(6)
            CreditRating.FAIR -> reviewDate.plusMonths(3)
            CreditRating.POOR -> reviewDate.plusMonths(1)
            CreditRating.UNRATED -> reviewDate.plusMonths(6)
        }
        
        return copy(
            creditScore = newCreditScore,
            creditRating = newCreditRating,
            lastCreditReviewDate = reviewDate,
            nextCreditReviewDate = nextReviewDate,
            lastCreditScoreUpdateAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ).recalculateRiskLevel()
    }
    
    /**
     * Check if customer can place order for given amount
     */
    fun canPlaceOrder(orderAmount: FinancialAmount): Boolean {
        if (isOnCreditHold) return false
        if (creditStatus != CreditStatus.ACTIVE) return false
        
        return orderAmount.isLessThanOrEqualTo(availableCredit)
    }
    
    /**
     * Get credit utilization percentage
     */
    fun getCreditUtilizationPercentage(): Double {
        return creditUtilizationRatio * 100.0
    }
    
    /**
     * Check if credit review is due
     */
    fun isCreditReviewDue(): Boolean {
        return nextCreditReviewDate?.let { LocalDate.now().isAfter(it) } ?: true
    }
    
    /**
     * Get payment performance percentage
     */
    fun getPaymentPerformancePercentage(): Double {
        return if (totalInvoicesCount > 0) {
            (paidOnTimeCount.toDouble() / totalInvoicesCount.toDouble()) * 100.0
        } else {
            0.0
        }
    }
    
    // Private helper methods
    private fun calculateUtilizationRatio(outstanding: FinancialAmount, limit: FinancialAmount): Double {
        return if (limit.isZero()) 0.0 else outstanding.divideBy(limit)
    }
    
    private fun recalculatePaymentHistoryScore(): CreditProfile {
        val newScore = when {
            totalInvoicesCount == 0L -> 0.0
            getPaymentPerformancePercentage() >= 95.0 -> 100.0
            getPaymentPerformancePercentage() >= 90.0 -> 85.0
            getPaymentPerformancePercentage() >= 80.0 -> 70.0
            getPaymentPerformancePercentage() >= 70.0 -> 55.0
            else -> 30.0
        }
        
        return copy(paymentHistoryScore = newScore)
    }
    
    private fun recalculateRiskLevel(): CreditProfile {
        val newRiskLevel = when {
            isOnCreditHold -> RiskLevel.HIGH
            overdueBalance.isGreaterThan(FinancialAmount.ZERO) -> RiskLevel.HIGH
            creditUtilizationRatio > 0.8 -> RiskLevel.HIGH
            creditUtilizationRatio > 0.5 -> RiskLevel.MEDIUM
            paymentHistoryScore < 70.0 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        
        return copy(riskLevel = newRiskLevel)
    }
    
    private fun updateCreditStatus(): CreditProfile {
        val newStatus = when {
            isOnCreditHold -> CreditStatus.ON_HOLD
            overdueBalance.isGreaterThan(FinancialAmount.ZERO) -> CreditStatus.PAST_DUE
            else -> CreditStatus.ACTIVE
        }
        
        return copy(creditStatus = newStatus)
    }
    
    private fun validateCreditLimitChange(newLimit: FinancialAmount) {
        if (newLimit.isNegative()) {
            throw IllegalCreditOperationException("Credit limit cannot be negative")
        }
        
        if (newLimit.currency != creditLimit.currency) {
            throw CurrencyMismatchException("New limit currency must match existing currency")
        }
    }
    
    private fun validateCurrencyMatch(amount: FinancialAmount) {
        if (amount.currency != creditLimit.currency) {
            throw CurrencyMismatchException("Amount currency ${amount.currency} does not match profile currency ${creditLimit.currency}")
        }
    }
    
    companion object {
        /**
         * Create a new credit profile
         */
        fun create(
            customerId: UUID,
            customerName: String,
            initialCreditLimit: FinancialAmount,
            paymentTerms: PaymentTerms
        ): CreditProfile {
            
            return CreditProfile(
                customerId = customerId,
                customerName = customerName,
                creditLimit = initialCreditLimit,
                availableCredit = initialCreditLimit,
                paymentTerms = paymentTerms,
                accountOpenDate = LocalDate.now()
            )
        }
    }
}

/**
 * Credit Rating Scale
 */
enum class CreditRating {
    EXCELLENT,  // 750+
    GOOD,       // 700-749
    FAIR,       // 650-699
    POOR,       // <650
    UNRATED
}

/**
 * Credit Status
 */
enum class CreditStatus {
    ACTIVE,
    INACTIVE,
    ON_HOLD,
    PAST_DUE,
    CLOSED
}

/**
 * Risk Level Assessment
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Credit Alerts
 */
enum class CreditAlert {
    CREDIT_LIMIT_EXCEEDED,
    HIGH_UTILIZATION,
    PAYMENT_OVERDUE,
    MULTIPLE_LATE_PAYMENTS,
    CREDIT_REVIEW_DUE,
    CREDIT_HOLD_PLACED,
    SUSPICIOUS_ACTIVITY
}

/**
 * Credit Limit Change History
 */
@Embeddable
data class CreditLimitChange(
    val oldLimit: FinancialAmount,
    val newLimit: FinancialAmount,
    val reason: String,
    val approvedBy: UUID,
    val changeDate: LocalDateTime
)

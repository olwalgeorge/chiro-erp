package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDateTime
import java.util.*

/**
 * CreditLimitExceededEvent
 * 
 * Domain event published when a customer's credit limit is exceeded.
 * This event triggers risk management workflows and customer credit
 * monitoring processes.
 * 
 * This event enables:
 * - Automated credit risk assessment
 * - Customer notification workflows
 * - Sales order blocking/approval processes
 * - Credit manager escalation procedures
 * - Financial risk reporting
 * - Credit policy enforcement
 */
data class CreditLimitExceededEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Customer ID
    val customerId: UUID,
    val customerName: String,
    val customerType: CustomerType,
    val customerClassification: CustomerClassification,
    val currentCreditLimit: FinancialAmount,
    val currentOutstandingBalance: FinancialAmount,
    val newTransactionAmount: FinancialAmount,
    val projectedBalance: FinancialAmount,
    val exceedanceAmount: FinancialAmount,
    val exceedancePercentage: BigDecimal,
    val currency: Currency,
    val triggeringTransactionId: UUID?,
    val triggeringTransactionType: TransactionType,
    val triggeringDocumentNumber: String?,
    val creditTerms: PaymentTerms,
    val lastPaymentDate: LocalDateTime?,
    val lastPaymentAmount: FinancialAmount?,
    val overdueAmount: FinancialAmount,
    val overdueInvoiceCount: Int,
    val daysPastDue: Int,
    val creditRating: CreditRating,
    val previousCreditRating: CreditRating?,
    val riskScore: Int, // 0-100
    val previousRiskScore: Int?,
    val accountManagerId: UUID?,
    val accountManagerName: String?,
    val salesRepId: UUID?,
    val salesRepName: String?,
    val creditManagerId: UUID?,
    val creditManagerName: String?,
    val industryCode: String?,
    val industryRiskLevel: RiskLevel,
    val geographicRegion: String?,
    val isFirstTimeExceedance: Boolean,
    val exceedanceHistory: ExceedanceFrequency,
    val recommendedAction: CreditAction,
    val urgencyLevel: UrgencyLevel,
    val approvalRequired: Boolean,
    val approvalThreshold: FinancialAmount?,
    val autoHoldOrders: Boolean,
    val notificationsSent: Set<NotificationType>,
    val escalationLevel: EscalationLevel,
    val reviewDate: LocalDateTime?,
    val notes: String?,
    val detectedBy: UUID,
    val detectedByName: String,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Check if this is a critical credit risk situation
     */
    fun isCriticalRisk(): Boolean {
        return exceedancePercentage >= BigDecimal("50") || 
               exceedanceAmount.amount >= currentCreditLimit.amount.multiply(BigDecimal("0.5")) ||
               riskScore >= 80
    }
    
    /**
     * Check if customer has significant overdue amounts
     */
    fun hasSignificantOverdueAmounts(): Boolean {
        return overdueAmount.amount >= currentCreditLimit.amount.multiply(BigDecimal("0.25")) &&
               daysPastDue >= 30
    }
    
    /**
     * Check if credit rating has been downgraded
     */
    fun hasCreditRatingDowngrade(): Boolean {
        return previousCreditRating != null && 
               creditRating.ordinal > previousCreditRating.ordinal
    }
    
    /**
     * Check if risk score has increased significantly
     */
    fun hasSignificantRiskIncrease(): Boolean {
        return previousRiskScore != null && 
               (riskScore - previousRiskScore) >= 20
    }
    
    /**
     * Check if immediate intervention is required
     */
    fun requiresImmediateIntervention(): Boolean {
        return isCriticalRisk() || 
               hasSignificantOverdueAmounts() ||
               urgencyLevel == UrgencyLevel.CRITICAL ||
               escalationLevel == EscalationLevel.EXECUTIVE
    }
    
    /**
     * Get credit exposure ratio
     */
    fun getCreditExposureRatio(): BigDecimal {
        return if (currentCreditLimit.isPositive()) {
            projectedBalance.amount.divide(currentCreditLimit.amount, 4, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }
    
    /**
     * Get utilization before new transaction
     */
    fun getCurrentUtilizationRatio(): BigDecimal {
        return if (currentCreditLimit.isPositive()) {
            currentOutstandingBalance.amount.divide(currentCreditLimit.amount, 4, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }
    
    /**
     * Get days of credit exposure
     */
    fun getDaysOfCreditExposure(): Int {
        return when (creditTerms) {
            PaymentTerms.NET_15 -> 15
            PaymentTerms.NET_30 -> 30
            PaymentTerms.NET_45 -> 45
            PaymentTerms.NET_60 -> 60
            else -> 30
        }
    }
    
    /**
     * Check if customer is in default risk category
     */
    fun isDefaultRisk(): Boolean {
        return daysPastDue >= 90 || 
               creditRating == CreditRating.POOR ||
               riskScore >= 90
    }
    
    /**
     * Get recommended credit limit adjustment
     */
    fun getRecommendedCreditLimitAdjustment(): FinancialAmount? {
        return when (recommendedAction) {
            CreditAction.REDUCE_LIMIT -> {
                val reduction = currentCreditLimit.amount.multiply(BigDecimal("0.25"))
                FinancialAmount(reduction.negate(), currency)
            }
            CreditAction.SUSPEND_CREDIT -> {
                FinancialAmount(currentCreditLimit.amount.negate(), currency)
            }
            else -> null
        }
    }
    
    /**
     * Check if insurance claim should be considered
     */
    fun shouldConsiderInsuranceClaim(): Boolean {
        return isDefaultRisk() && 
               overdueAmount.amount >= FinancialAmount(BigDecimal("5000"), currency).amount &&
               daysPastDue >= 120
    }
    
    companion object {
        /**
         * Create event for new transaction credit limit breach
         */
        fun forNewTransaction(
            customerId: UUID,
            customerName: String,
            customerType: CustomerType,
            currentCreditLimit: FinancialAmount,
            currentBalance: FinancialAmount,
            newTransactionAmount: FinancialAmount,
            transactionId: UUID,
            transactionType: TransactionType,
            documentNumber: String,
            creditTerms: PaymentTerms,
            creditRating: CreditRating,
            riskScore: Int,
            overdueAmount: FinancialAmount,
            overdueCount: Int,
            daysPastDue: Int,
            detectedBy: UUID,
            detectedByName: String
        ): CreditLimitExceededEvent {
            
            val projectedBalance = currentBalance.add(newTransactionAmount)
            val exceedance = projectedBalance.subtract(currentCreditLimit)
            val exceedancePercent = if (currentCreditLimit.isPositive()) {
                exceedance.amount.divide(currentCreditLimit.amount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
            } else {
                BigDecimal("100")
            }
            
            val recommendedAction = determineRecommendedAction(exceedancePercent, riskScore, daysPastDue)
            val urgency = determineUrgencyLevel(exceedancePercent, riskScore, daysPastDue)
            val escalation = determineEscalationLevel(exceedancePercent, riskScore)
            
            return CreditLimitExceededEvent(
                aggregateId = customerId,
                customerId = customerId,
                customerName = customerName,
                customerType = customerType,
                customerClassification = CustomerClassification.STANDARD,
                currentCreditLimit = currentCreditLimit,
                currentOutstandingBalance = currentBalance,
                newTransactionAmount = newTransactionAmount,
                projectedBalance = projectedBalance,
                exceedanceAmount = exceedance,
                exceedancePercentage = exceedancePercent,
                currency = currentCreditLimit.currency,
                triggeringTransactionId = transactionId,
                triggeringTransactionType = transactionType,
                triggeringDocumentNumber = documentNumber,
                creditTerms = creditTerms,
                overdueAmount = overdueAmount,
                overdueInvoiceCount = overdueCount,
                daysPastDue = daysPastDue,
                creditRating = creditRating,
                riskScore = riskScore,
                industryRiskLevel = RiskLevel.MEDIUM,
                isFirstTimeExceedance = true, // Would be determined from history
                exceedanceHistory = ExceedanceFrequency.RARE,
                recommendedAction = recommendedAction,
                urgencyLevel = urgency,
                approvalRequired = urgency != UrgencyLevel.LOW,
                autoHoldOrders = urgency == UrgencyLevel.CRITICAL,
                notificationsSent = emptySet(),
                escalationLevel = escalation,
                detectedBy = detectedBy,
                detectedByName = detectedByName
            )
        }
        
        private fun determineRecommendedAction(
            exceedancePercent: BigDecimal,
            riskScore: Int,
            daysPastDue: Int
        ): CreditAction {
            return when {
                daysPastDue >= 90 || riskScore >= 90 -> CreditAction.SUSPEND_CREDIT
                exceedancePercent >= BigDecimal("50") || riskScore >= 80 -> CreditAction.REDUCE_LIMIT
                exceedancePercent >= BigDecimal("25") || riskScore >= 70 -> CreditAction.REQUIRE_APPROVAL
                exceedancePercent >= BigDecimal("10") -> CreditAction.MONITOR_CLOSELY
                else -> CreditAction.NOTIFY_ONLY
            }
        }
        
        private fun determineUrgencyLevel(
            exceedancePercent: BigDecimal,
            riskScore: Int,
            daysPastDue: Int
        ): UrgencyLevel {
            return when {
                daysPastDue >= 90 || riskScore >= 90 || exceedancePercent >= BigDecimal("100") -> UrgencyLevel.CRITICAL
                riskScore >= 80 || exceedancePercent >= BigDecimal("50") || daysPastDue >= 60 -> UrgencyLevel.HIGH
                riskScore >= 70 || exceedancePercent >= BigDecimal("25") || daysPastDue >= 30 -> UrgencyLevel.MEDIUM
                else -> UrgencyLevel.LOW
            }
        }
        
        private fun determineEscalationLevel(
            exceedancePercent: BigDecimal,
            riskScore: Int
        ): EscalationLevel {
            return when {
                riskScore >= 90 || exceedancePercent >= BigDecimal("100") -> EscalationLevel.EXECUTIVE
                riskScore >= 80 || exceedancePercent >= BigDecimal("50") -> EscalationLevel.MANAGER
                riskScore >= 70 || exceedancePercent >= BigDecimal("25") -> EscalationLevel.SUPERVISOR
                else -> EscalationLevel.NONE
            }
        }
    }
}

/**
 * Customer Type Classifications
 */
enum class CustomerType {
    INDIVIDUAL,
    SMALL_BUSINESS,
    MEDIUM_BUSINESS,
    LARGE_ENTERPRISE,
    GOVERNMENT,
    NON_PROFIT
}

/**
 * Customer Credit Classifications
 */
enum class CustomerClassification {
    PREMIUM,
    STANDARD,
    BASIC,
    RESTRICTED,
    SUSPENDED
}

/**
 * Credit Rating Scale
 */
enum class CreditRating {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    DEFAULT
}

/**
 * Risk Level Classifications
 */
enum class RiskLevel {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
}

/**
 * Exceedance Frequency Patterns
 */
enum class ExceedanceFrequency {
    FIRST_TIME,
    RARE,
    OCCASIONAL,
    FREQUENT,
    CHRONIC
}

/**
 * Recommended Credit Actions
 */
enum class CreditAction {
    NOTIFY_ONLY,
    MONITOR_CLOSELY,
    REQUIRE_APPROVAL,
    REDUCE_LIMIT,
    SUSPEND_CREDIT,
    LEGAL_ACTION
}

/**
 * Urgency Level Classifications
 */
enum class UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Notification Types
 */
enum class NotificationType {
    EMAIL_CUSTOMER,
    EMAIL_SALES_REP,
    EMAIL_ACCOUNT_MANAGER,
    EMAIL_CREDIT_MANAGER,
    SMS_CUSTOMER,
    SYSTEM_ALERT,
    DASHBOARD_NOTIFICATION
}

/**
 * Escalation Levels
 */
enum class EscalationLevel {
    NONE,
    SUPERVISOR,
    MANAGER,
    DIRECTOR,
    EXECUTIVE
}

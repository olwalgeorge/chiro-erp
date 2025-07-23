package org.chiro.finance.domain.valueobject

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.DecimalMin
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Credit Limit Value Object
 * 
 * Represents credit limits and credit management for customers and vendors.
 * This value object encapsulates credit policies, limits, and utilization tracking.
 * 
 * Design Pattern: Value Object (immutable, equality based on value)
 * Domain: Finance
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
enum class CreditLimitType {
    // ==================== CUSTOMER CREDIT LIMITS ====================
    CUSTOMER_TRADE_CREDIT,      // Standard trade credit for customers
    CUSTOMER_CASH_CREDIT,       // Cash advance credit for customers
    CUSTOMER_REVOLVING_CREDIT,  // Revolving credit line for customers
    CUSTOMER_INSTALLMENT_CREDIT, // Installment credit for customers
    
    // ==================== VENDOR CREDIT LIMITS ====================
    VENDOR_TRADE_CREDIT,        // Trade credit from vendors
    VENDOR_PURCHASE_CREDIT,     // Purchase credit from vendors
    VENDOR_PAYMENT_TERMS,       // Extended payment terms credit
    
    // ==================== INTERNAL CREDIT LIMITS ====================
    DEPARTMENT_BUDGET_LIMIT,    // Department spending limits
    PROJECT_BUDGET_LIMIT,       // Project-specific credit limits
    EMPLOYEE_EXPENSE_LIMIT,     // Employee expense limits
    PETTY_CASH_LIMIT,          // Petty cash handling limits
    
    // ==================== FACILITY LIMITS ====================
    BANK_CREDIT_LINE,          // Bank credit line facility
    OVERDRAFT_FACILITY,        // Bank overdraft facility
    LETTER_OF_CREDIT_FACILITY, // LC facility limits
    GUARANTEE_FACILITY,        // Bank guarantee facility
    
    // ==================== SPECIAL LIMITS ====================
    SEASONAL_CREDIT_LIMIT,     // Seasonal credit adjustments
    PROMOTIONAL_CREDIT_LIMIT,  // Promotional credit increases
    EMERGENCY_CREDIT_LIMIT,    // Emergency credit authorization
    TEMPORARY_CREDIT_LIMIT,    // Temporary credit adjustment
    
    // ==================== RISK-BASED LIMITS ====================
    HIGH_RISK_CUSTOMER_LIMIT,  // High-risk customer limits
    LOW_RISK_CUSTOMER_LIMIT,   // Low-risk customer limits
    SECURED_CREDIT_LIMIT,      // Secured/collateralized credit
    UNSECURED_CREDIT_LIMIT,    // Unsecured credit limits
    
    // ==================== REGULATORY LIMITS ====================
    REGULATORY_CAPITAL_LIMIT,  // Regulatory capital limits
    CONCENTRATION_LIMIT,       // Concentration risk limits
    EXPOSURE_LIMIT,           // Total exposure limits
    
    // ==================== OTHER ====================
    CUSTOM_CREDIT_LIMIT       // Custom defined credit limits
}

/**
 * Credit Limit Status Enum
 */
enum class CreditLimitStatus {
    ACTIVE,                   // Credit limit is active and available
    SUSPENDED,               // Credit limit is temporarily suspended
    EXPIRED,                 // Credit limit has expired
    CANCELLED,               // Credit limit has been cancelled
    UNDER_REVIEW,           // Credit limit is under review
    PENDING_APPROVAL,       // Credit limit pending approval
    EXCEEDED,               // Credit limit has been exceeded
    FROZEN                  // Credit limit is frozen
}

/**
 * Credit Limit Value Object
 * 
 * Encapsulates credit limit information with usage tracking and validation.
 * This is an immutable value object that represents a specific credit limit.
 */
data class CreditLimit(
    @field:NotNull(message = "Credit limit type cannot be null")
    val type: CreditLimitType,
    
    @field:NotNull(message = "Credit limit amount cannot be null")
    @field:DecimalMin(value = "0.0", message = "Credit limit amount must be non-negative")
    val limitAmount: FinancialAmount,
    
    @field:NotNull(message = "Used amount cannot be null")
    @field:DecimalMin(value = "0.0", message = "Used amount must be non-negative")
    val usedAmount: FinancialAmount = FinancialAmount.zero(limitAmount.currency),
    
    val name: String = type.getDisplayName(),
    val description: String? = null,
    
    // ==================== VALIDITY PERIOD ====================
    val effectiveDate: LocalDate = LocalDate.now(),
    val expirationDate: LocalDate? = null,
    
    // ==================== STATUS AND FLAGS ====================
    val status: CreditLimitStatus = CreditLimitStatus.ACTIVE,
    val isAutoRenewable: Boolean = false,
    val isRevocable: Boolean = true,
    val requiresCollateral: Boolean = false,
    val allowsOverdraft: Boolean = false,
    
    // ==================== RISK PARAMETERS ====================
    val creditScore: Int? = null,
    val riskRating: String? = null,
    val interestRate: BigDecimal? = null,
    val overdraftFee: FinancialAmount? = null,
    val penaltyRate: BigDecimal? = null,
    
    // ==================== UTILIZATION THRESHOLDS ====================
    val warningThresholdPercentage: BigDecimal = BigDecimal("80.0"),
    val criticalThresholdPercentage: BigDecimal = BigDecimal("95.0"),
    val overdraftLimitPercentage: BigDecimal? = null,
    
    // ==================== REVIEW AND APPROVAL ====================
    val lastReviewDate: LocalDate? = null,
    val nextReviewDate: LocalDate? = null,
    val approvedBy: String? = null,
    val approvalDate: LocalDate? = null,
    
    // ==================== AUDIT FIELDS ====================
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastModifiedAt: LocalDateTime = LocalDateTime.now(),
    val lastUsedAt: LocalDateTime? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
        
        // ==================== FACTORY METHODS ====================
        
        /**
         * Creates a customer trade credit limit
         */
        fun customerTradeCredit(
            limitAmount: FinancialAmount,
            creditScore: Int? = null
        ): CreditLimit = CreditLimit(
            type = CreditLimitType.CUSTOMER_TRADE_CREDIT,
            limitAmount = limitAmount,
            creditScore = creditScore,
            nextReviewDate = LocalDate.now().plusYears(1)
        )
        
        /**
         * Creates a bank credit line facility
         */
        fun bankCreditLine(
            limitAmount: FinancialAmount,
            interestRate: BigDecimal,
            expirationDate: LocalDate
        ): CreditLimit = CreditLimit(
            type = CreditLimitType.BANK_CREDIT_LINE,
            limitAmount = limitAmount,
            interestRate = interestRate,
            expirationDate = expirationDate,
            requiresCollateral = true,
            allowsOverdraft = true
        )
        
        /**
         * Creates an employee expense limit
         */
        fun employeeExpenseLimit(
            limitAmount: FinancialAmount,
            employeeName: String
        ): CreditLimit = CreditLimit(
            type = CreditLimitType.EMPLOYEE_EXPENSE_LIMIT,
            limitAmount = limitAmount,
            name = "$employeeName Expense Limit",
            isAutoRenewable = true,
            nextReviewDate = LocalDate.now().plusMonths(6)
        )
        
        /**
         * Creates a department budget limit
         */
        fun departmentBudgetLimit(
            limitAmount: FinancialAmount,
            departmentName: String,
            fiscalYearEnd: LocalDate
        ): CreditLimit = CreditLimit(
            type = CreditLimitType.DEPARTMENT_BUDGET_LIMIT,
            limitAmount = limitAmount,
            name = "$departmentName Budget Limit",
            expirationDate = fiscalYearEnd,
            isAutoRenewable = true
        )
        
        /**
         * Creates a temporary credit limit
         */
        fun temporaryLimit(
            limitAmount: FinancialAmount,
            expirationDate: LocalDate,
            reason: String
        ): CreditLimit = CreditLimit(
            type = CreditLimitType.TEMPORARY_CREDIT_LIMIT,
            limitAmount = limitAmount,
            expirationDate = expirationDate,
            description = "Temporary limit: $reason",
            isRevocable = true,
            nextReviewDate = expirationDate
        )
        
        /**
         * Creates a secured credit limit
         */
        fun securedCredit(
            limitAmount: FinancialAmount,
            collateralValue: FinancialAmount
        ): CreditLimit {
            require(collateralValue.isGreaterThanOrEqualTo(limitAmount)) {
                "Collateral value must be at least equal to credit limit"
            }
            
            return CreditLimit(
                type = CreditLimitType.SECURED_CREDIT_LIMIT,
                limitAmount = limitAmount,
                requiresCollateral = true,
                description = "Secured by collateral worth ${collateralValue.format()}"
            )
        }
    }
    
    // ==================== COMPUTED PROPERTIES ====================
    
    /**
     * Available credit amount
     */
    val availableAmount: FinancialAmount
        get() = limitAmount.subtract(usedAmount)
    
    /**
     * Credit utilization percentage
     */
    val utilizationPercentage: BigDecimal
        get() = if (limitAmount.isZero) {
            BigDecimal.ZERO
        } else {
            usedAmount.divideBy(limitAmount).multiply(BigDecimal("100"))
        }
    
    /**
     * Check if credit limit is available for use
     */
    val isAvailable: Boolean
        get() = status == CreditLimitStatus.ACTIVE && 
                !isExpired && 
                availableAmount.isPositive
    
    /**
     * Check if credit limit has expired
     */
    val isExpired: Boolean
        get() = expirationDate?.isBefore(LocalDate.now()) ?: false
    
    /**
     * Check if credit limit is fully utilized
     */
    val isFullyUtilized: Boolean
        get() = usedAmount.isGreaterThanOrEqualTo(limitAmount)
    
    /**
     * Check if credit limit is over the warning threshold
     */
    val isOverWarningThreshold: Boolean
        get() = utilizationPercentage >= warningThresholdPercentage
    
    /**
     * Check if credit limit is over the critical threshold
     */
    val isOverCriticalThreshold: Boolean
        get() = utilizationPercentage >= criticalThresholdPercentage
    
    /**
     * Check if credit limit has been exceeded
     */
    val isExceeded: Boolean
        get() = usedAmount.isGreaterThan(limitAmount)
    
    /**
     * Check if overdraft is allowed and within limits
     */
    val isOverdraftAllowed: Boolean
        get() = allowsOverdraft && overdraftLimitPercentage != null
    
    /**
     * Maximum overdraft amount allowed
     */
    val maxOverdraftAmount: FinancialAmount?
        get() = overdraftLimitPercentage?.let { percentage ->
            limitAmount.multiply(percentage.divide(BigDecimal("100")))
        }
    
    /**
     * Check if current usage is within overdraft limits
     */
    val isWithinOverdraftLimits: Boolean
        get() = if (isOverdraftAllowed && maxOverdraftAmount != null) {
            usedAmount.isLessThanOrEqualTo(limitAmount.add(maxOverdraftAmount!!))
        } else {
            usedAmount.isLessThanOrEqualTo(limitAmount)
        }
    
    /**
     * Days until expiration
     */
    val daysUntilExpiration: Long?
        get() = expirationDate?.let { expDate ->
            java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expDate)
        }
    
    /**
     * Check if credit limit needs review
     */
    val needsReview: Boolean
        get() = nextReviewDate?.let { reviewDate ->
            !reviewDate.isAfter(LocalDate.now())
        } ?: false
    
    // ==================== BUSINESS OPERATIONS ====================
    
    /**
     * Checks if a specific amount can be utilized
     */
    fun canUtilize(amount: FinancialAmount): Boolean {
        requireSameCurrency(amount)
        
        if (!isAvailable) return false
        
        val newUsedAmount = usedAmount.add(amount)
        
        return if (isOverdraftAllowed && maxOverdraftAmount != null) {
            newUsedAmount.isLessThanOrEqualTo(limitAmount.add(maxOverdraftAmount!!))
        } else {
            newUsedAmount.isLessThanOrEqualTo(limitAmount)
        }
    }
    
    /**
     * Utilizes a specific amount from the credit limit
     */
    fun utilize(amount: FinancialAmount, description: String? = null): CreditLimit {
        requireSameCurrency(amount)
        require(canUtilize(amount)) { 
            "Cannot utilize ${amount.format()}. Available: ${availableAmount.format()}" 
        }
        
        val newUsedAmount = usedAmount.add(amount)
        val newStatus = if (newUsedAmount.isGreaterThan(limitAmount)) {
            CreditLimitStatus.EXCEEDED
        } else {
            status
        }
        
        return copy(
            usedAmount = newUsedAmount,
            status = newStatus,
            lastUsedAt = LocalDateTime.now(),
            lastModifiedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Releases (reduces) utilized amount
     */
    fun release(amount: FinancialAmount, description: String? = null): CreditLimit {
        requireSameCurrency(amount)
        require(amount.isLessThanOrEqualTo(usedAmount)) { 
            "Cannot release ${amount.format()}. Used amount: ${usedAmount.format()}" 
        }
        
        val newUsedAmount = usedAmount.subtract(amount)
        val newStatus = if (status == CreditLimitStatus.EXCEEDED && 
                           newUsedAmount.isLessThanOrEqualTo(limitAmount)) {
            CreditLimitStatus.ACTIVE
        } else {
            status
        }
        
        return copy(
            usedAmount = newUsedAmount,
            status = newStatus,
            lastModifiedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Adjusts the credit limit amount
     */
    fun adjustLimit(
        newLimitAmount: FinancialAmount,
        reason: String,
        approvedBy: String
    ): CreditLimit {
        requireSameCurrency(newLimitAmount)
        require(newLimitAmount.isPositive) { "New credit limit must be positive" }
        
        val newStatus = if (usedAmount.isGreaterThan(newLimitAmount)) {
            CreditLimitStatus.EXCEEDED
        } else if (status == CreditLimitStatus.EXCEEDED && 
                   usedAmount.isLessThanOrEqualTo(newLimitAmount)) {
            CreditLimitStatus.ACTIVE
        } else {
            status
        }
        
        return copy(
            limitAmount = newLimitAmount,
            status = newStatus,
            approvedBy = approvedBy,
            approvalDate = LocalDate.now(),
            description = reason,
            lastModifiedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Suspends the credit limit
     */
    fun suspend(reason: String): CreditLimit {
        return copy(
            status = CreditLimitStatus.SUSPENDED,
            description = "Suspended: $reason",
            lastModifiedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Reactivates the credit limit
     */
    fun reactivate(reason: String): CreditLimit {
        require(status in setOf(CreditLimitStatus.SUSPENDED, CreditLimitStatus.FROZEN)) {
            "Can only reactivate suspended or frozen credit limits"
        }
        
        return copy(
            status = CreditLimitStatus.ACTIVE,
            description = "Reactivated: $reason",
            lastModifiedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Extends the expiration date
     */
    fun extend(newExpirationDate: LocalDate, reason: String): CreditLimit {
        require(newExpirationDate.isAfter(LocalDate.now())) {
            "New expiration date must be in the future"
        }
        
        return copy(
            expirationDate = newExpirationDate,
            description = "Extended: $reason",
            lastModifiedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Marks credit limit for review
     */
    fun scheduleReview(reviewDate: LocalDate): CreditLimit {
        return copy(
            nextReviewDate = reviewDate,
            status = if (status == CreditLimitStatus.ACTIVE) CreditLimitStatus.UNDER_REVIEW else status,
            lastModifiedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Completes review process
     */
    fun completeReview(
        newNextReviewDate: LocalDate,
        reviewNotes: String,
        reviewedBy: String
    ): CreditLimit {
        return copy(
            lastReviewDate = LocalDate.now(),
            nextReviewDate = newNextReviewDate,
            status = if (status == CreditLimitStatus.UNDER_REVIEW) CreditLimitStatus.ACTIVE else status,
            description = "Review completed by $reviewedBy: $reviewNotes",
            lastModifiedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Calculates interest charges on used amount
     */
    fun calculateInterestCharges(days: Int): FinancialAmount? {
        return interestRate?.let { rate ->
            val dailyRate = rate.divide(BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP)
            val interestFactor = dailyRate.multiply(BigDecimal(days)).divide(BigDecimal("100"))
            usedAmount.multiply(interestFactor)
        }
    }
    
    /**
     * Calculates overdraft fees
     */
    fun calculateOverdraftFees(): FinancialAmount? {
        return if (isExceeded && overdraftFee != null) {
            overdraftFee
        } else {
            null
        }
    }
    
    /**
     * Gets utilization status description
     */
    fun getUtilizationStatus(): String {
        return when {
            isExceeded -> "Exceeded (${utilizationPercentage.setScale(1, BigDecimal.ROUND_HALF_UP)}%)"
            isOverCriticalThreshold -> "Critical (${utilizationPercentage.setScale(1, BigDecimal.ROUND_HALF_UP)}%)"
            isOverWarningThreshold -> "Warning (${utilizationPercentage.setScale(1, BigDecimal.ROUND_HALF_UP)}%)"
            isFullyUtilized -> "Fully Utilized"
            else -> "Normal (${utilizationPercentage.setScale(1, BigDecimal.ROUND_HALF_UP)}%)"
        }
    }
    
    /**
     * Gets credit limit summary
     */
    fun getSummary(): String {
        return buildString {
            append("$name: ")
            append("${usedAmount.format()} / ${limitAmount.format()}")
            append(" (${utilizationPercentage.setScale(1, BigDecimal.ROUND_HALF_UP)}%)")
            
            if (isExpired) {
                append(" [EXPIRED]")
            } else if (status != CreditLimitStatus.ACTIVE) {
                append(" [${status.name}]")
            }
            
            if (needsReview) {
                append(" [NEEDS REVIEW]")
            }
        }
    }
    
    // ==================== VALIDATION ====================
    
    private fun requireSameCurrency(amount: FinancialAmount) {
        require(limitAmount.currency == amount.currency) {
            "Currency mismatch: ${limitAmount.currency.code} vs ${amount.currency.code}"
        }
    }
    
    // ==================== VALIDATION ====================
    
    init {
        require(name.isNotBlank()) { "Credit limit name cannot be blank" }
        require(limitAmount.isPositive) { "Credit limit amount must be positive" }
        require(usedAmount >= FinancialAmount.zero(limitAmount.currency)) { 
            "Used amount must be non-negative" 
        }
        require(limitAmount.currency == usedAmount.currency) { 
            "Limit amount and used amount must have the same currency" 
        }
        
        expirationDate?.let { expDate ->
            require(!expDate.isBefore(effectiveDate)) {
                "Expiration date cannot be before effective date"
            }
        }
        
        require(warningThresholdPercentage >= BigDecimal.ZERO && 
                warningThresholdPercentage <= BigDecimal("100")) {
            "Warning threshold must be between 0 and 100 percent"
        }
        
        require(criticalThresholdPercentage >= BigDecimal.ZERO && 
                criticalThresholdPercentage <= BigDecimal("100")) {
            "Critical threshold must be between 0 and 100 percent"
        }
        
        require(criticalThresholdPercentage >= warningThresholdPercentage) {
            "Critical threshold must be greater than or equal to warning threshold"
        }
        
        overdraftLimitPercentage?.let { percentage ->
            require(percentage >= BigDecimal.ZERO && percentage <= BigDecimal("100")) {
                "Overdraft limit percentage must be between 0 and 100 percent"
            }
        }
        
        creditScore?.let { score ->
            require(score in 300..850) { "Credit score must be between 300 and 850" }
        }
        
        interestRate?.let { rate ->
            require(rate >= BigDecimal.ZERO) { "Interest rate must be non-negative" }
        }
        
        penaltyRate?.let { rate ->
            require(rate >= BigDecimal.ZERO) { "Penalty rate must be non-negative" }
        }
    }
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension functions for CreditLimitType enum
 */
fun CreditLimitType.getDisplayName(): String = when (this) {
    CreditLimitType.CUSTOMER_TRADE_CREDIT -> "Customer Trade Credit"
    CreditLimitType.CUSTOMER_CASH_CREDIT -> "Customer Cash Credit"
    CreditLimitType.CUSTOMER_REVOLVING_CREDIT -> "Customer Revolving Credit"
    CreditLimitType.CUSTOMER_INSTALLMENT_CREDIT -> "Customer Installment Credit"
    CreditLimitType.VENDOR_TRADE_CREDIT -> "Vendor Trade Credit"
    CreditLimitType.VENDOR_PURCHASE_CREDIT -> "Vendor Purchase Credit"
    CreditLimitType.VENDOR_PAYMENT_TERMS -> "Vendor Payment Terms"
    CreditLimitType.DEPARTMENT_BUDGET_LIMIT -> "Department Budget Limit"
    CreditLimitType.PROJECT_BUDGET_LIMIT -> "Project Budget Limit"
    CreditLimitType.EMPLOYEE_EXPENSE_LIMIT -> "Employee Expense Limit"
    CreditLimitType.PETTY_CASH_LIMIT -> "Petty Cash Limit"
    CreditLimitType.BANK_CREDIT_LINE -> "Bank Credit Line"
    CreditLimitType.OVERDRAFT_FACILITY -> "Overdraft Facility"
    CreditLimitType.LETTER_OF_CREDIT_FACILITY -> "Letter of Credit Facility"
    CreditLimitType.GUARANTEE_FACILITY -> "Bank Guarantee Facility"
    CreditLimitType.SEASONAL_CREDIT_LIMIT -> "Seasonal Credit Limit"
    CreditLimitType.PROMOTIONAL_CREDIT_LIMIT -> "Promotional Credit Limit"
    CreditLimitType.EMERGENCY_CREDIT_LIMIT -> "Emergency Credit Limit"
    CreditLimitType.TEMPORARY_CREDIT_LIMIT -> "Temporary Credit Limit"
    CreditLimitType.HIGH_RISK_CUSTOMER_LIMIT -> "High Risk Customer Limit"
    CreditLimitType.LOW_RISK_CUSTOMER_LIMIT -> "Low Risk Customer Limit"
    CreditLimitType.SECURED_CREDIT_LIMIT -> "Secured Credit Limit"
    CreditLimitType.UNSECURED_CREDIT_LIMIT -> "Unsecured Credit Limit"
    CreditLimitType.REGULATORY_CAPITAL_LIMIT -> "Regulatory Capital Limit"
    CreditLimitType.CONCENTRATION_LIMIT -> "Concentration Limit"
    CreditLimitType.EXPOSURE_LIMIT -> "Exposure Limit"
    CreditLimitType.CUSTOM_CREDIT_LIMIT -> "Custom Credit Limit"
}

fun CreditLimitType.getCategory(): String = when (this) {
    CreditLimitType.CUSTOMER_TRADE_CREDIT,
    CreditLimitType.CUSTOMER_CASH_CREDIT,
    CreditLimitType.CUSTOMER_REVOLVING_CREDIT,
    CreditLimitType.CUSTOMER_INSTALLMENT_CREDIT -> "Customer Credit"
    
    CreditLimitType.VENDOR_TRADE_CREDIT,
    CreditLimitType.VENDOR_PURCHASE_CREDIT,
    CreditLimitType.VENDOR_PAYMENT_TERMS -> "Vendor Credit"
    
    CreditLimitType.DEPARTMENT_BUDGET_LIMIT,
    CreditLimitType.PROJECT_BUDGET_LIMIT,
    CreditLimitType.EMPLOYEE_EXPENSE_LIMIT,
    CreditLimitType.PETTY_CASH_LIMIT -> "Internal Limits"
    
    CreditLimitType.BANK_CREDIT_LINE,
    CreditLimitType.OVERDRAFT_FACILITY,
    CreditLimitType.LETTER_OF_CREDIT_FACILITY,
    CreditLimitType.GUARANTEE_FACILITY -> "Bank Facilities"
    
    CreditLimitType.SEASONAL_CREDIT_LIMIT,
    CreditLimitType.PROMOTIONAL_CREDIT_LIMIT,
    CreditLimitType.EMERGENCY_CREDIT_LIMIT,
    CreditLimitType.TEMPORARY_CREDIT_LIMIT -> "Special Limits"
    
    CreditLimitType.HIGH_RISK_CUSTOMER_LIMIT,
    CreditLimitType.LOW_RISK_CUSTOMER_LIMIT,
    CreditLimitType.SECURED_CREDIT_LIMIT,
    CreditLimitType.UNSECURED_CREDIT_LIMIT -> "Risk-Based Limits"
    
    CreditLimitType.REGULATORY_CAPITAL_LIMIT,
    CreditLimitType.CONCENTRATION_LIMIT,
    CreditLimitType.EXPOSURE_LIMIT -> "Regulatory Limits"
    
    CreditLimitType.CUSTOM_CREDIT_LIMIT -> "Custom"
}

fun CreditLimitType.requiresCollateral(): Boolean = when (this) {
    CreditLimitType.SECURED_CREDIT_LIMIT,
    CreditLimitType.BANK_CREDIT_LINE,
    CreditLimitType.LETTER_OF_CREDIT_FACILITY,
    CreditLimitType.GUARANTEE_FACILITY -> true
    else -> false
}

fun CreditLimitType.isCustomerRelated(): Boolean = when (this) {
    CreditLimitType.CUSTOMER_TRADE_CREDIT,
    CreditLimitType.CUSTOMER_CASH_CREDIT,
    CreditLimitType.CUSTOMER_REVOLVING_CREDIT,
    CreditLimitType.CUSTOMER_INSTALLMENT_CREDIT,
    CreditLimitType.HIGH_RISK_CUSTOMER_LIMIT,
    CreditLimitType.LOW_RISK_CUSTOMER_LIMIT -> true
    else -> false
}

fun CreditLimitType.isVendorRelated(): Boolean = when (this) {
    CreditLimitType.VENDOR_TRADE_CREDIT,
    CreditLimitType.VENDOR_PURCHASE_CREDIT,
    CreditLimitType.VENDOR_PAYMENT_TERMS -> true
    else -> false
}

package org.chiro.finance.domain.valueobject

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Max
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Payment Term Value Object
 * 
 * Represents payment terms and conditions for invoices and transactions.
 * This value object encapsulates payment timing, discounts, and penalties.
 * 
 * Design Pattern: Value Object (immutable, equality based on value)
 * Domain: Finance
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
enum class PaymentTermType {
    // ==================== IMMEDIATE PAYMENTS ====================
    CASH_ON_DELIVERY,           // COD - Payment due on delivery
    CASH_IN_ADVANCE,            // CIA - Payment before shipment
    PAYMENT_DUE_UPON_RECEIPT,   // Due immediately when invoice received
    
    // ==================== NET TERMS ====================
    NET_10,                     // Payment due in 10 days
    NET_15,                     // Payment due in 15 days
    NET_30,                     // Payment due in 30 days (most common)
    NET_45,                     // Payment due in 45 days
    NET_60,                     // Payment due in 60 days
    NET_90,                     // Payment due in 90 days
    NET_120,                    // Payment due in 120 days
    
    // ==================== DISCOUNT TERMS ====================
    DISCOUNT_2_10_NET_30,       // 2% discount if paid within 10 days, otherwise net 30
    DISCOUNT_1_10_NET_30,       // 1% discount if paid within 10 days, otherwise net 30
    DISCOUNT_3_15_NET_45,       // 3% discount if paid within 15 days, otherwise net 45
    DISCOUNT_2_15_NET_30,       // 2% discount if paid within 15 days, otherwise net 30
    DISCOUNT_1_15_NET_30,       // 1% discount if paid within 15 days, otherwise net 30
    
    // ==================== END OF MONTH TERMS ====================
    EOM,                        // End of Month - due at month end
    EOM_PLUS_10,               // Due 10 days after end of month
    EOM_PLUS_15,               // Due 15 days after end of month
    EOM_PLUS_30,               // Due 30 days after end of month
    
    // ==================== INSTALLMENT TERMS ====================
    INSTALLMENT_MONTHLY,        // Monthly installments
    INSTALLMENT_QUARTERLY,      // Quarterly installments
    INSTALLMENT_SEMI_ANNUAL,    // Semi-annual installments
    INSTALLMENT_ANNUAL,         // Annual installments
    
    // ==================== SPECIAL TERMS ====================
    CONTRA,                     // Offset against other accounts
    CONSIGNMENT,                // Payment when goods are sold
    LETTER_OF_CREDIT,          // LC terms
    OPEN_ACCOUNT,              // Ongoing credit arrangement
    CASH_BEFORE_DELIVERY,      // CBD - Payment before delivery
    
    // ==================== CUSTOM/OTHER ====================
    CUSTOM_TERMS,              // Custom negotiated terms
    PREPAID,                   // Prepayment required
    SEASONAL_TERMS,            // Seasonal payment schedule
    OTHER
}

/**
 * Payment Term Value Object
 * 
 * Encapsulates payment terms including due dates, discounts, and penalties.
 * This is an immutable value object that defines when and how payments are due.
 */
data class PaymentTerm(
    @field:NotBlank(message = "Payment term type cannot be blank")
    val type: PaymentTermType,
    
    @field:Size(max = 100, message = "Payment term name cannot exceed 100 characters")
    val name: String = type.getDisplayName(),
    
    @field:Size(max = 500, message = "Payment term description cannot exceed 500 characters")
    val description: String? = null,
    
    // ==================== PAYMENT TIMING ====================
    @field:Min(value = 0, message = "Net days must be non-negative")
    @field:Max(value = 365, message = "Net days cannot exceed 365")
    val netDays: Int = 30,
    
    // ==================== DISCOUNT TERMS ====================
    @field:Min(value = 0, message = "Discount days must be non-negative")
    @field:Max(value = 365, message = "Discount days cannot exceed 365")
    val discountDays: Int? = null,
    
    val discountPercentage: BigDecimal? = null,
    
    // ==================== PENALTY TERMS ====================
    @field:Min(value = 0, message = "Grace period days must be non-negative")
    val gracePeriodDays: Int = 0,
    
    val lateFeePercentage: BigDecimal? = null,
    val lateFeeFixed: BigDecimal? = null,
    val interestRatePerMonth: BigDecimal? = null,
    
    // ==================== INSTALLMENT TERMS ====================
    @field:Min(value = 1, message = "Number of installments must be at least 1")
    val numberOfInstallments: Int? = null,
    val installmentFrequencyDays: Int? = null,
    
    // ==================== SPECIAL CONDITIONS ====================
    val isActive: Boolean = true,
    val requiresApproval: Boolean = false,
    val allowsPartialPayments: Boolean = true,
    val requiresSecurityDeposit: Boolean = false,
    val securityDepositPercentage: BigDecimal? = null,
    
    // ==================== END OF MONTH HANDLING ====================
    val isEndOfMonth: Boolean = false,
    val eomAdditionalDays: Int = 0,
    
    // ==================== AUDIT FIELDS ====================
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val lastModifiedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
        
        // ==================== COMMON PAYMENT TERMS ====================
        
        /**
         * Cash on Delivery - Payment due upon delivery
         */
        fun cashOnDelivery(): PaymentTerm = PaymentTerm(
            type = PaymentTermType.CASH_ON_DELIVERY,
            netDays = 0,
            allowsPartialPayments = false
        )
        
        /**
         * Cash in Advance - Payment required before shipment
         */
        fun cashInAdvance(): PaymentTerm = PaymentTerm(
            type = PaymentTermType.CASH_IN_ADVANCE,
            netDays = 0,
            allowsPartialPayments = false,
            requiresApproval = true
        )
        
        /**
         * Net 30 - Payment due in 30 days (most common business term)
         */
        fun net30(): PaymentTerm = PaymentTerm(
            type = PaymentTermType.NET_30,
            netDays = 30
        )
        
        /**
         * Net 15 - Payment due in 15 days
         */
        fun net15(): PaymentTerm = PaymentTerm(
            type = PaymentTermType.NET_15,
            netDays = 15
        )
        
        /**
         * Net 60 - Payment due in 60 days
         */
        fun net60(): PaymentTerm = PaymentTerm(
            type = PaymentTermType.NET_60,
            netDays = 60
        )
        
        /**
         * 2/10 Net 30 - 2% discount if paid within 10 days, otherwise net 30
         */
        fun discount2_10Net30(): PaymentTerm = PaymentTerm(
            type = PaymentTermType.DISCOUNT_2_10_NET_30,
            netDays = 30,
            discountDays = 10,
            discountPercentage = BigDecimal("2.0")
        )
        
        /**
         * 1/10 Net 30 - 1% discount if paid within 10 days, otherwise net 30
         */
        fun discount1_10Net30(): PaymentTerm = PaymentTerm(
            type = PaymentTermType.DISCOUNT_1_10_NET_30,
            netDays = 30,
            discountDays = 10,
            discountPercentage = BigDecimal("1.0")
        )
        
        /**
         * End of Month - Payment due at the end of the month
         */
        fun endOfMonth(): PaymentTerm = PaymentTerm(
            type = PaymentTermType.EOM,
            netDays = 30,
            isEndOfMonth = true
        )
        
        /**
         * Monthly Installments
         */
        fun monthlyInstallments(numberOfInstallments: Int): PaymentTerm = PaymentTerm(
            type = PaymentTermType.INSTALLMENT_MONTHLY,
            netDays = 30,
            numberOfInstallments = numberOfInstallments,
            installmentFrequencyDays = 30,
            allowsPartialPayments = false
        )
        
        /**
         * Custom payment terms
         */
        fun custom(
            name: String,
            netDays: Int,
            discountDays: Int? = null,
            discountPercentage: BigDecimal? = null
        ): PaymentTerm = PaymentTerm(
            type = PaymentTermType.CUSTOM_TERMS,
            name = name,
            netDays = netDays,
            discountDays = discountDays,
            discountPercentage = discountPercentage
        )
    }
    
    // ==================== BUSINESS LOGIC ====================
    
    /**
     * Calculates the due date for an invoice based on the invoice date
     */
    fun calculateDueDate(invoiceDate: LocalDate): LocalDate {
        return when {
            isEndOfMonth -> {
                val endOfMonth = invoiceDate.withDayOfMonth(invoiceDate.lengthOfMonth())
                endOfMonth.plusDays(eomAdditionalDays.toLong())
            }
            else -> invoiceDate.plusDays(netDays.toLong())
        }
    }
    
    /**
     * Calculates the early payment discount due date
     */
    fun calculateDiscountDueDate(invoiceDate: LocalDate): LocalDate? {
        return discountDays?.let { days ->
            invoiceDate.plusDays(days.toLong())
        }
    }
    
    /**
     * Calculates the discount amount for early payment
     */
    fun calculateDiscountAmount(invoiceAmount: BigDecimal): BigDecimal {
        return discountPercentage?.let { percentage ->
            invoiceAmount.multiply(percentage.divide(BigDecimal("100")))
        } ?: BigDecimal.ZERO
    }
    
    /**
     * Checks if early payment discount is available for the given payment date
     */
    fun isDiscountAvailable(invoiceDate: LocalDate, paymentDate: LocalDate): Boolean {
        val discountDueDate = calculateDiscountDueDate(invoiceDate)
        return discountDueDate?.let { dueDate ->
            !paymentDate.isAfter(dueDate)
        } ?: false
    }
    
    /**
     * Calculates late fees for overdue payments
     */
    fun calculateLateFees(invoiceAmount: BigDecimal, daysOverdue: Int): BigDecimal {
        if (daysOverdue <= gracePeriodDays) return BigDecimal.ZERO
        
        var lateFees = BigDecimal.ZERO
        
        // Fixed late fee
        lateFeeFixed?.let { fixed ->
            lateFees = lateFees.add(fixed)
        }
        
        // Percentage late fee
        lateFeePercentage?.let { percentage ->
            lateFees = lateFees.add(
                invoiceAmount.multiply(percentage.divide(BigDecimal("100")))
            )
        }
        
        // Monthly interest
        interestRatePerMonth?.let { monthlyRate ->
            val monthsOverdue = BigDecimal(daysOverdue).divide(BigDecimal("30"), 2, BigDecimal.ROUND_UP)
            val interestFee = invoiceAmount.multiply(monthlyRate.divide(BigDecimal("100"))).multiply(monthsOverdue)
            lateFees = lateFees.add(interestFee)
        }
        
        return lateFees
    }
    
    /**
     * Calculates installment amount for installment payment terms
     */
    fun calculateInstallmentAmount(totalAmount: BigDecimal): BigDecimal? {
        return numberOfInstallments?.let { installments ->
            totalAmount.divide(BigDecimal(installments), 2, BigDecimal.ROUND_HALF_UP)
        }
    }
    
    /**
     * Generates installment schedule for the given invoice
     */
    fun generateInstallmentSchedule(
        invoiceDate: LocalDate,
        totalAmount: BigDecimal
    ): List<InstallmentPayment> {
        val installments = numberOfInstallments ?: return emptyList()
        val frequencyDays = installmentFrequencyDays ?: 30
        val installmentAmount = calculateInstallmentAmount(totalAmount) ?: return emptyList()
        
        return (1..installments).map { installmentNumber ->
            val dueDate = invoiceDate.plusDays((frequencyDays * installmentNumber).toLong())
            val amount = if (installmentNumber == installments) {
                // Last installment gets any rounding difference
                totalAmount.subtract(installmentAmount.multiply(BigDecimal(installments - 1)))
            } else {
                installmentAmount
            }
            
            InstallmentPayment(
                installmentNumber = installmentNumber,
                dueDate = dueDate,
                amount = amount
            )
        }
    }
    
    /**
     * Checks if payment is overdue
     */
    fun isOverdue(invoiceDate: LocalDate, currentDate: LocalDate = LocalDate.now()): Boolean {
        val dueDate = calculateDueDate(invoiceDate)
        return currentDate.isAfter(dueDate)
    }
    
    /**
     * Calculates days overdue
     */
    fun getDaysOverdue(invoiceDate: LocalDate, currentDate: LocalDate = LocalDate.now()): Int {
        val dueDate = calculateDueDate(invoiceDate)
        return if (currentDate.isAfter(dueDate)) {
            currentDate.toEpochDay().minus(dueDate.toEpochDay()).toInt()
        } else {
            0
        }
    }
    
    /**
     * Gets payment term summary
     */
    fun getSummary(): String {
        val summary = StringBuilder(name)
        
        if (discountDays != null && discountPercentage != null) {
            summary.append(" (${discountPercentage}% discount if paid within $discountDays days)")
        }
        
        if (lateFeePercentage != null || lateFeeFixed != null) {
            summary.append(" - Late fees apply")
        }
        
        if (numberOfInstallments != null) {
            summary.append(" - $numberOfInstallments installments")
        }
        
        return summary.toString()
    }
    
    // ==================== VALIDATION ====================
    
    init {
        require(name.isNotBlank()) { "Payment term name cannot be blank" }
        require(name.length <= 100) { "Payment term name cannot exceed 100 characters" }
        require(netDays >= 0) { "Net days must be non-negative" }
        require(netDays <= 365) { "Net days cannot exceed 365" }
        
        description?.let { desc ->
            require(desc.length <= 500) { "Payment term description cannot exceed 500 characters" }
        }
        
        discountDays?.let { days ->
            require(days >= 0) { "Discount days must be non-negative" }
            require(days <= netDays) { "Discount days cannot exceed net days" }
        }
        
        discountPercentage?.let { percentage ->
            require(percentage >= BigDecimal.ZERO) { "Discount percentage must be non-negative" }
            require(percentage <= BigDecimal("100")) { "Discount percentage cannot exceed 100%" }
        }
        
        lateFeePercentage?.let { percentage ->
            require(percentage >= BigDecimal.ZERO) { "Late fee percentage must be non-negative" }
        }
        
        lateFeeFixed?.let { fixed ->
            require(fixed >= BigDecimal.ZERO) { "Late fee fixed amount must be non-negative" }
        }
        
        interestRatePerMonth?.let { rate ->
            require(rate >= BigDecimal.ZERO) { "Interest rate must be non-negative" }
            require(rate <= BigDecimal("50")) { "Interest rate per month seems unreasonably high" }
        }
        
        numberOfInstallments?.let { installments ->
            require(installments >= 1) { "Number of installments must be at least 1" }
            require(installments <= 360) { "Number of installments cannot exceed 360" }
        }
        
        installmentFrequencyDays?.let { frequency ->
            require(frequency >= 1) { "Installment frequency must be at least 1 day" }
        }
        
        securityDepositPercentage?.let { deposit ->
            require(deposit >= BigDecimal.ZERO) { "Security deposit percentage must be non-negative" }
            require(deposit <= BigDecimal("100")) { "Security deposit percentage cannot exceed 100%" }
        }
        
        require(gracePeriodDays >= 0) { "Grace period days must be non-negative" }
        require(eomAdditionalDays >= 0) { "EOM additional days must be non-negative" }
    }
}

/**
 * Represents a single installment payment in an installment schedule
 */
data class InstallmentPayment(
    val installmentNumber: Int,
    val dueDate: LocalDate,
    val amount: BigDecimal,
    val isPaid: Boolean = false,
    val paidDate: LocalDate? = null,
    val paidAmount: BigDecimal? = null
)

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension functions for PaymentTermType enum
 */
fun PaymentTermType.getDisplayName(): String = when (this) {
    PaymentTermType.CASH_ON_DELIVERY -> "Cash on Delivery (COD)"
    PaymentTermType.CASH_IN_ADVANCE -> "Cash in Advance (CIA)"
    PaymentTermType.PAYMENT_DUE_UPON_RECEIPT -> "Due Upon Receipt"
    PaymentTermType.NET_10 -> "Net 10 Days"
    PaymentTermType.NET_15 -> "Net 15 Days"
    PaymentTermType.NET_30 -> "Net 30 Days"
    PaymentTermType.NET_45 -> "Net 45 Days"
    PaymentTermType.NET_60 -> "Net 60 Days"
    PaymentTermType.NET_90 -> "Net 90 Days"
    PaymentTermType.NET_120 -> "Net 120 Days"
    PaymentTermType.DISCOUNT_2_10_NET_30 -> "2/10 Net 30"
    PaymentTermType.DISCOUNT_1_10_NET_30 -> "1/10 Net 30"
    PaymentTermType.DISCOUNT_3_15_NET_45 -> "3/15 Net 45"
    PaymentTermType.DISCOUNT_2_15_NET_30 -> "2/15 Net 30"
    PaymentTermType.DISCOUNT_1_15_NET_30 -> "1/15 Net 30"
    PaymentTermType.EOM -> "End of Month (EOM)"
    PaymentTermType.EOM_PLUS_10 -> "End of Month + 10 Days"
    PaymentTermType.EOM_PLUS_15 -> "End of Month + 15 Days"
    PaymentTermType.EOM_PLUS_30 -> "End of Month + 30 Days"
    PaymentTermType.INSTALLMENT_MONTHLY -> "Monthly Installments"
    PaymentTermType.INSTALLMENT_QUARTERLY -> "Quarterly Installments"
    PaymentTermType.INSTALLMENT_SEMI_ANNUAL -> "Semi-Annual Installments"
    PaymentTermType.INSTALLMENT_ANNUAL -> "Annual Installments"
    PaymentTermType.CONTRA -> "Contra Account"
    PaymentTermType.CONSIGNMENT -> "Consignment Terms"
    PaymentTermType.LETTER_OF_CREDIT -> "Letter of Credit"
    PaymentTermType.OPEN_ACCOUNT -> "Open Account"
    PaymentTermType.CASH_BEFORE_DELIVERY -> "Cash Before Delivery (CBD)"
    PaymentTermType.CUSTOM_TERMS -> "Custom Terms"
    PaymentTermType.PREPAID -> "Prepaid"
    PaymentTermType.SEASONAL_TERMS -> "Seasonal Terms"
    PaymentTermType.OTHER -> "Other"
}

fun PaymentTermType.getNetDays(): Int = when (this) {
    PaymentTermType.CASH_ON_DELIVERY,
    PaymentTermType.CASH_IN_ADVANCE,
    PaymentTermType.PAYMENT_DUE_UPON_RECEIPT,
    PaymentTermType.CASH_BEFORE_DELIVERY -> 0
    PaymentTermType.NET_10 -> 10
    PaymentTermType.NET_15 -> 15
    PaymentTermType.NET_30,
    PaymentTermType.DISCOUNT_2_10_NET_30,
    PaymentTermType.DISCOUNT_1_10_NET_30,
    PaymentTermType.DISCOUNT_2_15_NET_30,
    PaymentTermType.DISCOUNT_1_15_NET_30,
    PaymentTermType.EOM,
    PaymentTermType.EOM_PLUS_10,
    PaymentTermType.EOM_PLUS_15,
    PaymentTermType.EOM_PLUS_30 -> 30
    PaymentTermType.NET_45,
    PaymentTermType.DISCOUNT_3_15_NET_45 -> 45
    PaymentTermType.NET_60 -> 60
    PaymentTermType.NET_90 -> 90
    PaymentTermType.NET_120 -> 120
    PaymentTermType.INSTALLMENT_MONTHLY -> 30
    PaymentTermType.INSTALLMENT_QUARTERLY -> 90
    PaymentTermType.INSTALLMENT_SEMI_ANNUAL -> 180
    PaymentTermType.INSTALLMENT_ANNUAL -> 365
    else -> 30 // Default to 30 days
}

fun PaymentTermType.isInstallmentTerm(): Boolean = when (this) {
    PaymentTermType.INSTALLMENT_MONTHLY,
    PaymentTermType.INSTALLMENT_QUARTERLY,
    PaymentTermType.INSTALLMENT_SEMI_ANNUAL,
    PaymentTermType.INSTALLMENT_ANNUAL -> true
    else -> false
}

fun PaymentTermType.hasDiscount(): Boolean = when (this) {
    PaymentTermType.DISCOUNT_2_10_NET_30,
    PaymentTermType.DISCOUNT_1_10_NET_30,
    PaymentTermType.DISCOUNT_3_15_NET_45,
    PaymentTermType.DISCOUNT_2_15_NET_30,
    PaymentTermType.DISCOUNT_1_15_NET_30 -> true
    else -> false
}

fun PaymentTermType.isImmediatePayment(): Boolean = when (this) {
    PaymentTermType.CASH_ON_DELIVERY,
    PaymentTermType.CASH_IN_ADVANCE,
    PaymentTermType.PAYMENT_DUE_UPON_RECEIPT,
    PaymentTermType.CASH_BEFORE_DELIVERY -> true
    else -> false
}

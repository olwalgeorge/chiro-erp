package org.chiro.finance.domain.entity

import org.chiro.finance.domain.valueobject.*
import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 * Invoice Payment Application - Payment allocation to invoices
 * 
 * Tracks the application of customer payments to outstanding invoices with:
 * - Precise payment allocation tracking
 * - Partial and full payment support
 * - Cash discount and early payment handling
 * - Payment reversal and adjustment capabilities
 * - Comprehensive audit trail
 * 
 * Key Features:
 * - Multi-invoice payment allocation
 * - Cash discount calculation
 * - Over/under payment handling
 * - Currency conversion support
 * - Payment aging analysis
 * 
 * Business Rules:
 * - Payment applications cannot exceed invoice balance
 * - Cash discounts must be within terms
 * - Reversed applications require proper authorization
 * - Payment allocation must be fully documented
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Serializable
data class InvoicePaymentApplication(
    val id: UUID = UUID.randomUUID(),
    
    @field:NotNull(message = "Payment receipt ID is required")
    val paymentReceiptId: UUID,
    
    @field:NotNull(message = "Invoice ID is required")
    val invoiceId: UUID,
    
    @field:NotBlank(message = "Invoice number cannot be blank")
    @field:Size(min = 1, max = 50, message = "Invoice number must be between 1 and 50 characters")
    val invoiceNumber: String,
    
    @field:NotNull(message = "Customer ID is required")
    val customerId: UUID,
    
    @field:NotNull(message = "Application amount is required")
    @field:Valid
    val applicationAmount: FinancialAmount,
    
    @field:NotNull(message = "Application date is required")
    val applicationDate: LocalDateTime,
    
    @field:NotNull(message = "Status is required")
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    
    val cashDiscountAmount: FinancialAmount? = null,
    val cashDiscountPercent: Double? = null,
    val cashDiscountDate: LocalDateTime? = null,
    
    val writeOffAmount: FinancialAmount? = null,
    val writeOffReason: String? = null,
    
    val originalInvoiceAmount: FinancialAmount? = null,
    val remainingBalance: FinancialAmount? = null,
    val previousBalance: FinancialAmount? = null,
    
    @field:Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    val notes: String? = null,
    
    @field:NotNull(message = "Applied by is required")
    val appliedBy: UUID,
    
    @field:NotNull(message = "Applied date is required")
    val appliedDate: LocalDateTime = LocalDateTime.now(),
    
    val modifiedBy: UUID? = null,
    val modifiedDate: LocalDateTime? = null,
    
    val reversedBy: UUID? = null,
    val reversedDate: LocalDateTime? = null,
    val reversalReason: String? = null,
    
    val approvedBy: UUID? = null,
    val approvedDate: LocalDateTime? = null,
    
    val glAccountId: AccountId? = null,
    val journalEntryId: UUID? = null,
    
    val exchangeRate: Double? = null,
    val originalCurrency: Currency? = null,
    
    val paymentTerms: PaymentTerm? = null,
    val discountEligible: Boolean = false,
    val isOverPayment: Boolean = false,
    val isUnderPayment: Boolean = false,
    
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(invoiceNumber.isNotBlank()) { "Invoice number cannot be blank" }
        require(applicationAmount.amount > 0.0) { "Application amount must be positive" }
        
        // Validate cash discount
        if (cashDiscountAmount != null) {
            require(cashDiscountAmount.amount >= 0.0) { 
                "Cash discount amount cannot be negative" 
            }
            require(applicationAmount.currency == cashDiscountAmount.currency) {
                "Application and discount amounts must have same currency"
            }
        }
        
        // Validate cash discount percentage
        if (cashDiscountPercent != null) {
            require(cashDiscountPercent in 0.0..100.0) {
                "Cash discount percentage must be between 0 and 100"
            }
        }
        
        // Validate write-off
        if (writeOffAmount != null) {
            require(writeOffAmount.amount >= 0.0) { 
                "Write-off amount cannot be negative" 
            }
            require(!writeOffReason.isNullOrBlank()) {
                "Write-off reason is required when write-off amount is specified"
            }
        }
        
        // Validate status-specific requirements
        if (status == ApplicationStatus.REVERSED) {
            require(!reversalReason.isNullOrBlank()) { 
                "Reversal reason is required for reversed applications" 
            }
            require(reversedBy != null) { 
                "Reversed by user is required for reversed applications" 
            }
        }
        
        // Validate currency consistency
        if (originalCurrency != null) {
            require(exchangeRate != null && exchangeRate > 0) {
                "Exchange rate is required for foreign currency applications"
            }
        }
        
        // Validate balance calculations
        if (previousBalance != null && remainingBalance != null) {
            val expectedRemaining = previousBalance.amount - getTotalApplicationAmount()
            val tolerance = 0.01 // Allow for rounding differences
            require(kotlin.math.abs(expectedRemaining - remainingBalance.amount) <= tolerance) {
                "Remaining balance calculation is incorrect"
            }
        }
    }
    
    /**
     * Gets total amount applied including discounts and write-offs
     */
    fun getTotalApplicationAmount(): Double {
        var total = applicationAmount.amount
        
        if (cashDiscountAmount != null) {
            total += cashDiscountAmount.amount
        }
        
        if (writeOffAmount != null) {
            total += writeOffAmount.amount
        }
        
        return total
    }
    
    /**
     * Gets effective payment amount (application + discount)
     */
    fun getEffectivePaymentAmount(): FinancialAmount {
        val effectiveAmount = applicationAmount.amount + (cashDiscountAmount?.amount ?: 0.0)
        return FinancialAmount(
            amount = effectiveAmount,
            currency = applicationAmount.currency
        )
    }
    
    /**
     * Calculates cash discount if eligible
     */
    fun calculateCashDiscount(invoiceDate: LocalDateTime, paymentTerms: PaymentTerm): FinancialAmount? {
        return if (discountEligible && this.paymentTerms != null && originalInvoiceAmount != null) {
            val discountDeadline = invoiceDate.plusDays(paymentTerms.discountDays.toLong())
            if (applicationDate.isBefore(discountDeadline) || applicationDate.isEqual(discountDeadline)) {
                val discountAmount = originalInvoiceAmount.amount * (paymentTerms.discountPercentage / 100.0)
                FinancialAmount(
                    amount = discountAmount,
                    currency = originalInvoiceAmount.currency
                )
            } else null
        } else null
    }
    
    /**
     * Applies cash discount to the application
     */
    fun applyCashDiscount(
        discountAmount: FinancialAmount,
        discountPercent: Double,
        appliedBy: UUID
    ): InvoicePaymentApplication {
        require(discountEligible) { "Invoice is not eligible for cash discount" }
        require(discountAmount.amount >= 0) { "Discount amount cannot be negative" }
        require(discountPercent in 0.0..100.0) { "Discount percentage must be between 0 and 100" }
        
        return copy(
            cashDiscountAmount = discountAmount,
            cashDiscountPercent = discountPercent,
            cashDiscountDate = LocalDateTime.now(),
            modifiedBy = appliedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Applies write-off for uncollectible amount
     */
    fun applyWriteOff(
        writeOffAmount: FinancialAmount,
        reason: String,
        appliedBy: UUID
    ): InvoicePaymentApplication {
        require(writeOffAmount.amount >= 0) { "Write-off amount cannot be negative" }
        require(reason.isNotBlank()) { "Write-off reason is required" }
        
        return copy(
            writeOffAmount = writeOffAmount,
            writeOffReason = reason,
            modifiedBy = appliedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Reverses the payment application
     */
    fun reverse(reversedBy: UUID, reason: String): InvoicePaymentApplication {
        require(status == ApplicationStatus.APPLIED) {
            "Only applied payments can be reversed"
        }
        require(reason.isNotBlank()) { "Reversal reason is required" }
        
        return copy(
            status = ApplicationStatus.REVERSED,
            reversedBy = reversedBy,
            reversedDate = LocalDateTime.now(),
            reversalReason = reason,
            modifiedBy = reversedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Processes application for GL posting
     */
    fun process(
        processedBy: UUID,
        glAccountId: AccountId,
        journalEntryId: UUID
    ): InvoicePaymentApplication {
        require(status == ApplicationStatus.APPLIED) {
            "Application must be applied to process"
        }
        
        return copy(
            status = ApplicationStatus.PROCESSED,
            glAccountId = glAccountId,
            journalEntryId = journalEntryId,
            approvedBy = processedBy,
            approvedDate = LocalDateTime.now(),
            modifiedBy = processedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Updates remaining balance after application
     */
    fun updateBalance(
        newRemainingBalance: FinancialAmount,
        updatedBy: UUID
    ): InvoicePaymentApplication {
        return copy(
            previousBalance = remainingBalance,
            remainingBalance = newRemainingBalance,
            modifiedBy = updatedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Checks if invoice is fully paid after this application
     */
    fun isInvoiceFullyPaid(): Boolean {
        return remainingBalance?.amount == 0.0
    }
    
    /**
     * Checks if application resulted in overpayment
     */
    fun isOverpayment(): Boolean {
        return remainingBalance?.amount?.let { it < 0.0 } ?: false
    }
    
    /**
     * Gets payment aging in days from invoice date
     */
    fun getPaymentAging(invoiceDate: LocalDateTime): Long {
        return java.time.Duration.between(invoiceDate, applicationDate).toDays()
    }
    
    /**
     * Gets application amount in base currency
     */
    fun getApplicationAmountInBaseCurrency(): FinancialAmount {
        return if (originalCurrency != null && exchangeRate != null) {
            FinancialAmount(
                amount = applicationAmount.amount * exchangeRate,
                currency = Currency.USD // Assuming USD as base currency
            )
        } else {
            applicationAmount
        }
    }
    
    /**
     * Validates payment application for business rules
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (applicationAmount.amount <= 0) {
            errors.add("Application amount must be positive")
        }
        
        if (cashDiscountAmount != null && cashDiscountAmount.amount < 0) {
            errors.add("Cash discount amount cannot be negative")
        }
        
        if (cashDiscountPercent != null && cashDiscountPercent !in 0.0..100.0) {
            errors.add("Cash discount percentage must be between 0 and 100")
        }
        
        if (writeOffAmount != null) {
            if (writeOffAmount.amount < 0) {
                errors.add("Write-off amount cannot be negative")
            }
            if (writeOffReason.isNullOrBlank()) {
                errors.add("Write-off reason is required when write-off amount is specified")
            }
        }
        
        if (status == ApplicationStatus.REVERSED && reversalReason.isNullOrBlank()) {
            errors.add("Reversal reason is required for reversed applications")
        }
        
        if (originalCurrency != null && (exchangeRate == null || exchangeRate <= 0)) {
            errors.add("Valid exchange rate is required for foreign currency applications")
        }
        
        if (previousBalance != null && remainingBalance != null) {
            val expectedRemaining = previousBalance.amount - getTotalApplicationAmount()
            val tolerance = 0.01
            if (kotlin.math.abs(expectedRemaining - remainingBalance.amount) > tolerance) {
                errors.add("Remaining balance calculation is incorrect")
            }
        }
        
        return errors
    }
    
    companion object {
        /**
         * Creates a new payment application
         */
        fun create(
            paymentReceiptId: UUID,
            invoiceId: UUID,
            invoiceNumber: String,
            customerId: UUID,
            applicationAmount: FinancialAmount,
            appliedBy: UUID,
            originalInvoiceAmount: FinancialAmount? = null,
            paymentTerms: PaymentTerm? = null,
            notes: String? = null
        ): InvoicePaymentApplication {
            return InvoicePaymentApplication(
                paymentReceiptId = paymentReceiptId,
                invoiceId = invoiceId,
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                applicationAmount = applicationAmount,
                appliedBy = appliedBy,
                applicationDate = LocalDateTime.now(),
                originalInvoiceAmount = originalInvoiceAmount,
                paymentTerms = paymentTerms,
                notes = notes,
                discountEligible = paymentTerms?.discountPercentage?.let { it > 0 } ?: false
            )
        }
        
        /**
         * Creates application with cash discount
         */
        fun createWithDiscount(
            paymentReceiptId: UUID,
            invoiceId: UUID,
            invoiceNumber: String,
            customerId: UUID,
            applicationAmount: FinancialAmount,
            cashDiscountAmount: FinancialAmount,
            discountPercent: Double,
            appliedBy: UUID,
            originalInvoiceAmount: FinancialAmount,
            paymentTerms: PaymentTerm
        ): InvoicePaymentApplication {
            return InvoicePaymentApplication(
                paymentReceiptId = paymentReceiptId,
                invoiceId = invoiceId,
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                applicationAmount = applicationAmount,
                appliedBy = appliedBy,
                applicationDate = LocalDateTime.now(),
                cashDiscountAmount = cashDiscountAmount,
                cashDiscountPercent = discountPercent,
                cashDiscountDate = LocalDateTime.now(),
                originalInvoiceAmount = originalInvoiceAmount,
                paymentTerms = paymentTerms,
                discountEligible = true
            )
        }
        
        /**
         * Creates foreign currency application
         */
        fun createForeignCurrency(
            paymentReceiptId: UUID,
            invoiceId: UUID,
            invoiceNumber: String,
            customerId: UUID,
            applicationAmount: FinancialAmount,
            originalCurrency: Currency,
            exchangeRate: Double,
            appliedBy: UUID
        ): InvoicePaymentApplication {
            return InvoicePaymentApplication(
                paymentReceiptId = paymentReceiptId,
                invoiceId = invoiceId,
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                applicationAmount = applicationAmount,
                appliedBy = appliedBy,
                applicationDate = LocalDateTime.now(),
                originalCurrency = originalCurrency,
                exchangeRate = exchangeRate
            )
        }
    }
}

/**
 * Payment application status
 */
@Serializable
enum class ApplicationStatus(
    val description: String,
    val isFinal: Boolean = false
) {
    APPLIED("Applied"),
    PROCESSED("Processed", isFinal = true),
    REVERSED("Reversed", isFinal = true);
    
    val canModify: Boolean
        get() = !isFinal
    
    val isActive: Boolean
        get() = this != REVERSED
}

package org.chiro.finance.domain.entity

import org.chiro.finance.domain.valueobject.*
import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 * Payment Receipt - Payment processing and receipt management
 * 
 * Represents received payments from customers with comprehensive tracking:
 * - Customer payment processing and validation
 * - Multiple payment method support
 * - Automated GL integration and posting
 * - Bank reconciliation and matching
 * - Outstanding payment tracking
 * 
 * Key Features:
 * - Multi-currency payment support
 * - Partial and full payment allocation
 * - Automated accounting integration
 * - Bank reconciliation capabilities
 * - Payment reversal and adjustments
 * 
 * Business Rules:
 * - Payments must have valid customer and amount
 * - Payment allocation cannot exceed receipt amount
 * - Reversed payments require proper authorization
 * - Bank deposits must match receipt totals
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Serializable
data class PaymentReceipt(
    val id: UUID = UUID.randomUUID(),
    
    @field:NotNull(message = "Receipt number is required")
    @field:Size(min = 1, max = 50, message = "Receipt number must be between 1 and 50 characters")
    val receiptNumber: String,
    
    @field:NotNull(message = "Customer ID is required")
    val customerId: UUID,
    
    @field:NotBlank(message = "Customer name cannot be blank")
    @field:Size(min = 1, max = 200, message = "Customer name must be between 1 and 200 characters")
    val customerName: String,
    
    @field:NotNull(message = "Payment amount is required")
    @field:Valid
    val paymentAmount: FinancialAmount,
    
    @field:NotNull(message = "Payment method is required")
    @field:Valid
    val paymentMethod: PaymentMethod,
    
    @field:NotNull(message = "Receipt date is required")
    val receiptDate: LocalDateTime,
    
    val paymentDate: LocalDateTime? = null,
    
    @field:NotNull(message = "Status is required")
    val status: PaymentReceiptStatus = PaymentReceiptStatus.PENDING,
    
    @field:Size(max = 500, message = "Reference number cannot exceed 500 characters")
    val referenceNumber: String? = null,
    
    @field:Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    val notes: String? = null,
    
    val bankDepositId: UUID? = null,
    val bankReconciliationId: UUID? = null,
    val glAccountId: AccountId? = null,
    val journalEntryId: UUID? = null,
    
    @field:NotNull(message = "Created by is required")
    val createdBy: UUID,
    
    @field:NotNull(message = "Created date is required")
    val createdDate: LocalDateTime = LocalDateTime.now(),
    
    val modifiedBy: UUID? = null,
    val modifiedDate: LocalDateTime? = null,
    
    val approvedBy: UUID? = null,
    val approvedDate: LocalDateTime? = null,
    
    val reversedBy: UUID? = null,
    val reversedDate: LocalDateTime? = null,
    val reversalReason: String? = null,
    
    @field:Valid
    val allocations: List<PaymentAllocation> = emptyList(),
    
    val exchangeRate: Double? = null,
    val originalCurrency: Currency? = null,
    val bankCharges: FinancialAmount? = null,
    
    val isReconciled: Boolean = false,
    val reconciledDate: LocalDateTime? = null,
    
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(receiptNumber.isNotBlank()) { "Receipt number cannot be blank" }
        require(customerName.isNotBlank()) { "Customer name cannot be blank" }
        require(paymentAmount.amount > 0.0) { "Payment amount must be positive" }
        
        // Validate allocation totals don't exceed payment amount
        val totalAllocated = allocations.sumOf { it.allocatedAmount.amount }
        require(totalAllocated <= paymentAmount.amount) {
            "Total allocated amount ($totalAllocated) cannot exceed payment amount (${paymentAmount.amount})"
        }
        
        // Validate status transitions
        if (status == PaymentReceiptStatus.REVERSED) {
            require(!reversalReason.isNullOrBlank()) { 
                "Reversal reason is required for reversed payments" 
            }
            require(reversedBy != null) { 
                "Reversed by user is required for reversed payments" 
            }
        }
        
        // Validate currency consistency
        if (originalCurrency != null) {
            require(exchangeRate != null && exchangeRate > 0) {
                "Exchange rate is required for foreign currency payments"
            }
        }
    }
    
    /**
     * Gets the unallocated amount available for application to invoices
     */
    fun getUnallocatedAmount(): FinancialAmount {
        val totalAllocated = allocations.sumOf { it.allocatedAmount.amount }
        val unallocated = paymentAmount.amount - totalAllocated
        return FinancialAmount(
            amount = unallocated,
            currency = paymentAmount.currency
        )
    }
    
    /**
     * Checks if the payment is fully allocated to invoices
     */
    fun isFullyAllocated(): Boolean {
        return getUnallocatedAmount().amount <= 0.0
    }
    
    /**
     * Applies payment to an invoice with specified amount
     */
    fun allocateToInvoice(
        invoiceId: UUID,
        invoiceNumber: String,
        allocatedAmount: FinancialAmount,
        appliedBy: UUID
    ): PaymentReceipt {
        require(status == PaymentReceiptStatus.PENDING) {
            "Cannot allocate reversed or processed payments"
        }
        
        val availableAmount = getUnallocatedAmount()
        require(allocatedAmount.amount <= availableAmount.amount) {
            "Allocation amount (${allocatedAmount.amount}) exceeds available amount (${availableAmount.amount})"
        }
        
        val newAllocation = PaymentAllocation(
            invoiceId = invoiceId,
            invoiceNumber = invoiceNumber,
            allocatedAmount = allocatedAmount,
            allocationDate = LocalDateTime.now(),
            allocatedBy = appliedBy
        )
        
        return copy(
            allocations = allocations + newAllocation,
            modifiedBy = appliedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Removes allocation from specific invoice
     */
    fun removeAllocation(invoiceId: UUID, removedBy: UUID): PaymentReceipt {
        require(status == PaymentReceiptStatus.PENDING) {
            "Cannot modify allocations for processed payments"
        }
        
        return copy(
            allocations = allocations.filterNot { it.invoiceId == invoiceId },
            modifiedBy = removedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Processes the payment receipt for posting to GL
     */
    fun process(
        processedBy: UUID,
        glAccountId: AccountId,
        journalEntryId: UUID
    ): PaymentReceipt {
        require(status == PaymentReceiptStatus.PENDING) {
            "Payment must be pending to process"
        }
        require(isFullyAllocated()) {
            "Payment must be fully allocated before processing"
        }
        
        return copy(
            status = PaymentReceiptStatus.PROCESSED,
            glAccountId = glAccountId,
            journalEntryId = journalEntryId,
            approvedBy = processedBy,
            approvedDate = LocalDateTime.now(),
            modifiedBy = processedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Reverses the payment receipt with reason
     */
    fun reverse(reversedBy: UUID, reason: String): PaymentReceipt {
        require(status in listOf(PaymentReceiptStatus.PENDING, PaymentReceiptStatus.PROCESSED)) {
            "Only pending or processed payments can be reversed"
        }
        require(reason.isNotBlank()) { "Reversal reason is required" }
        
        return copy(
            status = PaymentReceiptStatus.REVERSED,
            reversedBy = reversedBy,
            reversedDate = LocalDateTime.now(),
            reversalReason = reason,
            modifiedBy = reversedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Marks payment as reconciled with bank statement
     */
    fun reconcile(
        bankReconciliationId: UUID,
        reconciledBy: UUID,
        bankDepositId: UUID? = null
    ): PaymentReceipt {
        require(status == PaymentReceiptStatus.PROCESSED) {
            "Only processed payments can be reconciled"
        }
        
        return copy(
            bankReconciliationId = bankReconciliationId,
            bankDepositId = bankDepositId,
            isReconciled = true,
            reconciledDate = LocalDateTime.now(),
            modifiedBy = reconciledBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Gets effective payment amount in base currency
     */
    fun getEffectiveAmount(): FinancialAmount {
        return if (originalCurrency != null && exchangeRate != null) {
            FinancialAmount(
                amount = paymentAmount.amount * exchangeRate,
                currency = Currency.USD // Assuming USD as base currency
            )
        } else {
            paymentAmount
        }
    }
    
    /**
     * Gets net payment amount after bank charges
     */
    fun getNetAmount(): FinancialAmount {
        val netAmount = if (bankCharges != null) {
            paymentAmount.amount - bankCharges.amount
        } else {
            paymentAmount.amount
        }
        
        return FinancialAmount(
            amount = netAmount,
            currency = paymentAmount.currency
        )
    }
    
    /**
     * Validates payment receipt for business rules
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (paymentAmount.amount <= 0) {
            errors.add("Payment amount must be positive")
        }
        
        if (allocations.isNotEmpty()) {
            val totalAllocated = allocations.sumOf { it.allocatedAmount.amount }
            if (totalAllocated > paymentAmount.amount) {
                errors.add("Total allocated amount exceeds payment amount")
            }
        }
        
        if (status == PaymentReceiptStatus.REVERSED && reversalReason.isNullOrBlank()) {
            errors.add("Reversal reason is required for reversed payments")
        }
        
        if (originalCurrency != null && (exchangeRate == null || exchangeRate <= 0)) {
            errors.add("Valid exchange rate is required for foreign currency payments")
        }
        
        return errors
    }
    
    companion object {
        /**
         * Creates a new payment receipt
         */
        fun create(
            receiptNumber: String,
            customerId: UUID,
            customerName: String,
            paymentAmount: FinancialAmount,
            paymentMethod: PaymentMethod,
            createdBy: UUID,
            receiptDate: LocalDateTime = LocalDateTime.now(),
            referenceNumber: String? = null,
            notes: String? = null
        ): PaymentReceipt {
            return PaymentReceipt(
                receiptNumber = receiptNumber,
                customerId = customerId,
                customerName = customerName,
                paymentAmount = paymentAmount,
                paymentMethod = paymentMethod,
                receiptDate = receiptDate,
                createdBy = createdBy,
                referenceNumber = referenceNumber,
                notes = notes
            )
        }
        
        /**
         * Creates foreign currency payment receipt
         */
        fun createForeignCurrency(
            receiptNumber: String,
            customerId: UUID,
            customerName: String,
            paymentAmount: FinancialAmount,
            originalCurrency: Currency,
            exchangeRate: Double,
            paymentMethod: PaymentMethod,
            createdBy: UUID,
            receiptDate: LocalDateTime = LocalDateTime.now()
        ): PaymentReceipt {
            return PaymentReceipt(
                receiptNumber = receiptNumber,
                customerId = customerId,
                customerName = customerName,
                paymentAmount = paymentAmount,
                paymentMethod = paymentMethod,
                receiptDate = receiptDate,
                createdBy = createdBy,
                originalCurrency = originalCurrency,
                exchangeRate = exchangeRate
            )
        }
    }
}

/**
 * Payment allocation to specific invoices
 */
@Serializable
data class PaymentAllocation(
    val invoiceId: UUID,
    
    @field:NotBlank(message = "Invoice number cannot be blank")
    val invoiceNumber: String,
    
    @field:NotNull(message = "Allocated amount is required")
    @field:Valid
    val allocatedAmount: FinancialAmount,
    
    @field:NotNull(message = "Allocation date is required")
    val allocationDate: LocalDateTime,
    
    @field:NotNull(message = "Allocated by user is required")
    val allocatedBy: UUID,
    
    @field:Size(max = 500, message = "Notes cannot exceed 500 characters")
    val notes: String? = null
) {
    
    init {
        require(invoiceNumber.isNotBlank()) { "Invoice number cannot be blank" }
        require(allocatedAmount.amount > 0) { "Allocated amount must be positive" }
    }
}

/**
 * Payment receipt status enumeration
 */
@Serializable
enum class PaymentReceiptStatus(
    val description: String,
    val isFinal: Boolean = false
) {
    PENDING("Pending Allocation"),
    PROCESSED("Processed and Posted", isFinal = true),
    REVERSED("Reversed", isFinal = true);
    
    val canModify: Boolean
        get() = !isFinal
    
    val isActive: Boolean
        get() = this != REVERSED
}

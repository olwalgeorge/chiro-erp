package org.chiro.finance.domain.entity

import org.chiro.finance.domain.valueobject.*
import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 * Payment Disbursement - Outgoing payment processing and management
 * 
 * Represents payments made to vendors, suppliers, and other payees with:
 * - Vendor payment processing and tracking
 * - Multiple disbursement methods support
 * - Automated GL integration and posting
 * - Approval workflow and authorization
 * - Check printing and electronic transfers
 * 
 * Key Features:
 * - Multi-currency disbursement support
 * - Invoice payment allocation tracking
 * - Automated accounting integration
 * - Approval workflow management
 * - Payment reversal and void capabilities
 * 
 * Business Rules:
 * - Disbursements require proper authorization
 * - Payment allocation must match invoice amounts
 * - Voided payments require management approval
 * - Check numbers must be unique and sequential
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Serializable
data class PaymentDisbursement(
    val id: UUID = UUID.randomUUID(),
    
    @field:NotNull(message = "Payment number is required")
    @field:Size(min = 1, max = 50, message = "Payment number must be between 1 and 50 characters")
    val paymentNumber: String,
    
    @field:NotNull(message = "Vendor ID is required")
    val vendorId: UUID,
    
    @field:NotBlank(message = "Vendor name cannot be blank")
    @field:Size(min = 1, max = 200, message = "Vendor name must be between 1 and 200 characters")
    val vendorName: String,
    
    @field:NotNull(message = "Payment amount is required")
    @field:Valid
    val paymentAmount: FinancialAmount,
    
    @field:NotNull(message = "Payment method is required")
    @field:Valid
    val paymentMethod: PaymentMethod,
    
    @field:NotNull(message = "Payment date is required")
    val paymentDate: LocalDateTime,
    
    @field:NotNull(message = "Status is required")
    val status: DisbursementStatus = DisbursementStatus.DRAFT,
    
    @field:Size(max = 500, message = "Reference number cannot exceed 500 characters")
    val referenceNumber: String? = null,
    
    @field:Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    val notes: String? = null,
    
    val checkNumber: String? = null,
    val bankAccountId: AccountId? = null,
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
    
    val issuedBy: UUID? = null,
    val issuedDate: LocalDateTime? = null,
    
    val voidedBy: UUID? = null,
    val voidedDate: LocalDateTime? = null,
    val voidReason: String? = null,
    
    @field:Valid
    val invoiceAllocations: List<InvoiceAllocation> = emptyList(),
    
    val exchangeRate: Double? = null,
    val originalCurrency: Currency? = null,
    val bankCharges: FinancialAmount? = null,
    
    val requiresApproval: Boolean = true,
    val approvalLimit: FinancialAmount? = null,
    
    val isReconciled: Boolean = false,
    val reconciledDate: LocalDateTime? = null,
    val bankReconciliationId: UUID? = null,
    
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(paymentNumber.isNotBlank()) { "Payment number cannot be blank" }
        require(vendorName.isNotBlank()) { "Vendor name cannot be blank" }
        require(paymentAmount.amount > 0.0) { "Payment amount must be positive" }
        
        // Validate allocation totals match payment amount
        if (invoiceAllocations.isNotEmpty()) {
            val totalAllocated = invoiceAllocations.sumOf { it.allocatedAmount.amount }
            require(totalAllocated == paymentAmount.amount) {
                "Total allocated amount ($totalAllocated) must equal payment amount (${paymentAmount.amount})"
            }
        }
        
        // Validate status-specific requirements
        when (status) {
            DisbursementStatus.VOIDED -> {
                require(!voidReason.isNullOrBlank()) { 
                    "Void reason is required for voided payments" 
                }
                require(voidedBy != null) { 
                    "Voided by user is required for voided payments" 
                }
            }
            DisbursementStatus.ISSUED -> {
                require(issuedBy != null) { 
                    "Issued by user is required for issued payments" 
                }
                if (paymentMethod == PaymentMethod.CHECK) {
                    require(!checkNumber.isNullOrBlank()) { 
                        "Check number is required for check payments" 
                    }
                }
            }
            else -> { /* No additional validation */ }
        }
        
        // Validate currency consistency
        if (originalCurrency != null) {
            require(exchangeRate != null && exchangeRate > 0) {
                "Exchange rate is required for foreign currency payments"
            }
        }
        
        // Validate check payment specifics
        if (paymentMethod == PaymentMethod.CHECK && status == DisbursementStatus.ISSUED) {
            require(!checkNumber.isNullOrBlank()) {
                "Check number is required for issued check payments"
            }
        }
    }
    
    /**
     * Submits disbursement for approval
     */
    fun submitForApproval(submittedBy: UUID): PaymentDisbursement {
        require(status == DisbursementStatus.DRAFT) {
            "Only draft payments can be submitted for approval"
        }
        require(invoiceAllocations.isNotEmpty()) {
            "Payment must have invoice allocations before approval"
        }
        
        return copy(
            status = DisbursementStatus.PENDING_APPROVAL,
            modifiedBy = submittedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Approves the disbursement for payment
     */
    fun approve(
        approvedBy: UUID,
        bankAccountId: AccountId,
        glAccountId: AccountId
    ): PaymentDisbursement {
        require(status == DisbursementStatus.PENDING_APPROVAL) {
            "Only pending approval payments can be approved"
        }
        
        // Check approval authority if limit is set
        if (approvalLimit != null) {
            require(paymentAmount.amount <= approvalLimit.amount) {
                "Payment amount exceeds approval limit"
            }
        }
        
        return copy(
            status = DisbursementStatus.APPROVED,
            approvedBy = approvedBy,
            approvedDate = LocalDateTime.now(),
            bankAccountId = bankAccountId,
            glAccountId = glAccountId,
            modifiedBy = approvedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Issues the payment (prints check or processes transfer)
     */
    fun issue(
        issuedBy: UUID,
        checkNumber: String? = null,
        journalEntryId: UUID
    ): PaymentDisbursement {
        require(status == DisbursementStatus.APPROVED) {
            "Only approved payments can be issued"
        }
        
        // Validate check number for check payments
        if (paymentMethod == PaymentMethod.CHECK) {
            require(!checkNumber.isNullOrBlank()) {
                "Check number is required for check payments"
            }
        }
        
        return copy(
            status = DisbursementStatus.ISSUED,
            issuedBy = issuedBy,
            issuedDate = LocalDateTime.now(),
            checkNumber = checkNumber,
            journalEntryId = journalEntryId,
            modifiedBy = issuedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Voids the payment with reason
     */
    fun void(voidedBy: UUID, reason: String): PaymentDisbursement {
        require(status in listOf(DisbursementStatus.APPROVED, DisbursementStatus.ISSUED)) {
            "Only approved or issued payments can be voided"
        }
        require(reason.isNotBlank()) { "Void reason is required" }
        
        return copy(
            status = DisbursementStatus.VOIDED,
            voidedBy = voidedBy,
            voidedDate = LocalDateTime.now(),
            voidReason = reason,
            modifiedBy = voidedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Reconciles payment with bank statement
     */
    fun reconcile(
        bankReconciliationId: UUID,
        reconciledBy: UUID
    ): PaymentDisbursement {
        require(status == DisbursementStatus.ISSUED) {
            "Only issued payments can be reconciled"
        }
        
        return copy(
            bankReconciliationId = bankReconciliationId,
            isReconciled = true,
            reconciledDate = LocalDateTime.now(),
            modifiedBy = reconciledBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Allocates payment to specific invoices
     */
    fun allocateToInvoices(
        allocations: List<InvoiceAllocation>,
        allocatedBy: UUID
    ): PaymentDisbursement {
        require(status == DisbursementStatus.DRAFT) {
            "Can only allocate invoices for draft payments"
        }
        
        val totalAllocated = allocations.sumOf { it.allocatedAmount.amount }
        require(totalAllocated == paymentAmount.amount) {
            "Total allocated amount ($totalAllocated) must equal payment amount (${paymentAmount.amount})"
        }
        
        return copy(
            invoiceAllocations = allocations,
            modifiedBy = allocatedBy,
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
            paymentAmount.amount + bankCharges.amount
        } else {
            paymentAmount.amount
        }
        
        return FinancialAmount(
            amount = netAmount,
            currency = paymentAmount.currency
        )
    }
    
    /**
     * Checks if payment can be modified
     */
    fun canModify(): Boolean {
        return status in listOf(DisbursementStatus.DRAFT, DisbursementStatus.PENDING_APPROVAL)
    }
    
    /**
     * Checks if payment can be voided
     */
    fun canVoid(): Boolean {
        return status in listOf(DisbursementStatus.APPROVED, DisbursementStatus.ISSUED) && !isReconciled
    }
    
    /**
     * Validates payment disbursement for business rules
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (paymentAmount.amount <= 0) {
            errors.add("Payment amount must be positive")
        }
        
        if (invoiceAllocations.isNotEmpty()) {
            val totalAllocated = invoiceAllocations.sumOf { it.allocatedAmount.amount }
            if (totalAllocated != paymentAmount.amount) {
                errors.add("Total allocated amount must equal payment amount")
            }
        }
        
        if (status == DisbursementStatus.VOIDED && voidReason.isNullOrBlank()) {
            errors.add("Void reason is required for voided payments")
        }
        
        if (originalCurrency != null && (exchangeRate == null || exchangeRate <= 0)) {
            errors.add("Valid exchange rate is required for foreign currency payments")
        }
        
        if (paymentMethod == PaymentMethod.CHECK && status == DisbursementStatus.ISSUED && checkNumber.isNullOrBlank()) {
            errors.add("Check number is required for issued check payments")
        }
        
        return errors
    }
    
    companion object {
        /**
         * Creates a new payment disbursement
         */
        fun create(
            paymentNumber: String,
            vendorId: UUID,
            vendorName: String,
            paymentAmount: FinancialAmount,
            paymentMethod: PaymentMethod,
            createdBy: UUID,
            paymentDate: LocalDateTime = LocalDateTime.now(),
            referenceNumber: String? = null,
            notes: String? = null,
            requiresApproval: Boolean = true
        ): PaymentDisbursement {
            return PaymentDisbursement(
                paymentNumber = paymentNumber,
                vendorId = vendorId,
                vendorName = vendorName,
                paymentAmount = paymentAmount,
                paymentMethod = paymentMethod,
                paymentDate = paymentDate,
                createdBy = createdBy,
                referenceNumber = referenceNumber,
                notes = notes,
                requiresApproval = requiresApproval
            )
        }
        
        /**
         * Creates foreign currency payment disbursement
         */
        fun createForeignCurrency(
            paymentNumber: String,
            vendorId: UUID,
            vendorName: String,
            paymentAmount: FinancialAmount,
            originalCurrency: Currency,
            exchangeRate: Double,
            paymentMethod: PaymentMethod,
            createdBy: UUID,
            paymentDate: LocalDateTime = LocalDateTime.now()
        ): PaymentDisbursement {
            return PaymentDisbursement(
                paymentNumber = paymentNumber,
                vendorId = vendorId,
                vendorName = vendorName,
                paymentAmount = paymentAmount,
                paymentMethod = paymentMethod,
                paymentDate = paymentDate,
                createdBy = createdBy,
                originalCurrency = originalCurrency,
                exchangeRate = exchangeRate
            )
        }
    }
}

/**
 * Invoice allocation for disbursement payments
 */
@Serializable
data class InvoiceAllocation(
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
    val notes: String? = null,
    
    val discountTaken: FinancialAmount? = null,
    val originalInvoiceAmount: FinancialAmount? = null
) {
    
    init {
        require(invoiceNumber.isNotBlank()) { "Invoice number cannot be blank" }
        require(allocatedAmount.amount > 0) { "Allocated amount must be positive" }
        
        if (discountTaken != null && originalInvoiceAmount != null) {
            val maxAllowedPayment = originalInvoiceAmount.amount - discountTaken.amount
            require(allocatedAmount.amount <= maxAllowedPayment) {
                "Allocated amount cannot exceed invoice amount minus discount"
            }
        }
    }
    
    /**
     * Gets the effective payment amount including discount
     */
    fun getEffectivePayment(): FinancialAmount {
        val effectiveAmount = if (discountTaken != null) {
            allocatedAmount.amount + discountTaken.amount
        } else {
            allocatedAmount.amount
        }
        
        return FinancialAmount(
            amount = effectiveAmount,
            currency = allocatedAmount.currency
        )
    }
}

/**
 * Disbursement status enumeration
 */
@Serializable
enum class DisbursementStatus(
    val description: String,
    val isEditable: Boolean = false,
    val requiresApproval: Boolean = false
) {
    DRAFT("Draft", isEditable = true),
    PENDING_APPROVAL("Pending Approval", requiresApproval = true),
    APPROVED("Approved"),
    ISSUED("Issued"),
    VOIDED("Voided");
    
    val isFinal: Boolean
        get() = this in listOf(ISSUED, VOIDED)
    
    val canProcess: Boolean
        get() = this == APPROVED
}

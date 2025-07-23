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
 * VendorBill Aggregate Root
 * 
 * Core vendor billing aggregate that manages accounts payable, vendor invoices,
 * payment processing, and purchase-to-pay workflow within the financial domain.
 * 
 * Business Rules:
 * - Bill number must be unique per vendor
 * - Bill cannot be paid more than the total amount
 * - Bills must be approved before payment
 * - Payment terms determine due dates and discount calculations
 * - Tax calculations must be accurate based on jurisdiction
 */
@Entity
@Table(name = "vendor_bills")
data class VendorBill(
    @Id
    val billId: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val billNumber: String,
    
    @Column(nullable = false)
    val vendorBillNumber: String, // Vendor's own invoice number
    
    @Column(nullable = false)
    val vendorId: UUID,
    
    @Column(nullable = false)
    val vendorName: String,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "line1", column = Column(name = "vendor_address_line1")),
        AttributeOverride(name = "line2", column = Column(name = "vendor_address_line2")),
        AttributeOverride(name = "city", column = Column(name = "vendor_city")),
        AttributeOverride(name = "state", column = Column(name = "vendor_state")),
        AttributeOverride(name = "postalCode", column = Column(name = "vendor_postal_code")),
        AttributeOverride(name = "country", column = Column(name = "vendor_country"))
    )
    val vendorAddress: Address,
    
    @Column(nullable = false)
    val billDate: LocalDate,
    
    @Column(nullable = false)
    val dueDate: LocalDate,
    
    @Column
    val earlyPaymentDiscountDate: LocalDate? = null,
    
    @Embedded
    val earlyPaymentDiscountRate: TaxRate? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: VendorBillStatus = VendorBillStatus.DRAFT,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "subtotal_amount")),
        AttributeOverride(name = "currency", column = Column(name = "subtotal_currency"))
    )
    val subtotal: FinancialAmount,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "tax_amount")),
        AttributeOverride(name = "currency", column = Column(name = "tax_currency"))
    )
    val taxAmount: FinancialAmount,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "discount_amount")),
        AttributeOverride(name = "currency", column = Column(name = "discount_currency"))
    )
    val discountAmount: FinancialAmount = FinancialAmount.ZERO,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_amount")),
        AttributeOverride(name = "currency", column = Column(name = "total_currency"))
    )
    val totalAmount: FinancialAmount,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "paid_amount")),
        AttributeOverride(name = "currency", column = Column(name = "paid_currency"))
    )
    val paidAmount: FinancialAmount = FinancialAmount.ZERO,
    
    @OneToMany(mappedBy = "vendorBill", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val lineItems: List<VendorBillLineItem> = emptyList(),
    
    @ElementCollection
    @CollectionTable(name = "vendor_bill_payments")
    val paymentRecords: List<BillPaymentRecord> = emptyList(),
    
    @Embedded
    val paymentTerms: PaymentTerms,
    
    @Column
    val purchaseOrderNumber: String? = null,
    
    @Column
    val receivingDocumentNumber: String? = null,
    
    @Column(length = 1000)
    val description: String? = null,
    
    @Column(length = 1000)
    val notes: String? = null,
    
    @Column(nullable = false)
    val departmentId: UUID? = null,
    
    @Column(nullable = false)
    val projectId: UUID? = null,
    
    @Column(nullable = false)
    val createdBy: UUID,
    
    @Column
    val approvedBy: UUID? = null,
    
    @Column
    val rejectedBy: UUID? = null,
    
    @Column(length = 500)
    val rejectionReason: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val approvedAt: LocalDateTime? = null,
    
    @Column
    val rejectedAt: LocalDateTime? = null,
    
    @Column
    val lastPaymentDate: LocalDateTime? = null
) {

    /**
     * Add line item to vendor bill
     */
    fun addLineItem(
        description: String,
        quantity: Quantity,
        unitCost: FinancialAmount,
        accountCode: AccountCode,
        taxRate: TaxRate = TaxRate.ZERO
    ): VendorBill {
        validateCanModify()
        
        val lineItem = VendorBillLineItem.create(
            vendorBill = this,
            description = description,
            quantity = quantity,
            unitCost = unitCost,
            accountCode = accountCode,
            taxRate = taxRate
        )
        
        val updatedLineItems = lineItems + lineItem
        return recalculateAmounts(updatedLineItems)
    }
    
    /**
     * Remove line item from vendor bill
     */
    fun removeLineItem(lineItemId: UUID): VendorBill {
        validateCanModify()
        
        val updatedLineItems = lineItems.filter { it.lineItemId != lineItemId }
        return recalculateAmounts(updatedLineItems)
    }
    
    /**
     * Update line item quantity
     */
    fun updateLineItemQuantity(lineItemId: UUID, newQuantity: Quantity): VendorBill {
        validateCanModify()
        
        val updatedLineItems = lineItems.map { lineItem ->
            if (lineItem.lineItemId == lineItemId) {
                lineItem.updateQuantity(newQuantity)
            } else {
                lineItem
            }
        }
        
        return recalculateAmounts(updatedLineItems)
    }
    
    /**
     * Apply discount to bill
     */
    fun applyDiscount(discountAmount: FinancialAmount, reason: String): VendorBill {
        validateCanModify()
        validateCurrencyMatch(discountAmount)
        
        if (discountAmount.isGreaterThan(subtotal)) {
            throw InvalidDiscountException("Discount amount ${discountAmount.amount} cannot exceed subtotal ${subtotal.amount}")
        }
        
        val newTotalAmount = subtotal.add(taxAmount).subtract(discountAmount)
        
        return copy(
            discountAmount = discountAmount,
            totalAmount = newTotalAmount,
            notes = (notes ?: "") + "\nDiscount applied: $reason",
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Submit bill for approval
     */
    fun submitForApproval(): VendorBill {
        if (status != VendorBillStatus.DRAFT) {
            throw IllegalBillOperationException("Can only submit draft bills for approval")
        }
        
        if (lineItems.isEmpty()) {
            throw IllegalBillOperationException("Cannot submit bill with no line items")
        }
        
        if (totalAmount.isZero()) {
            throw IllegalBillOperationException("Cannot submit bill with zero amount")
        }
        
        return copy(
            status = VendorBillStatus.PENDING_APPROVAL,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Approve bill for payment
     */
    fun approve(approvedBy: UUID): VendorBill {
        if (status != VendorBillStatus.PENDING_APPROVAL) {
            throw IllegalBillOperationException("Can only approve bills pending approval")
        }
        
        return copy(
            status = VendorBillStatus.APPROVED,
            approvedBy = approvedBy,
            approvedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Reject bill (send back to draft)
     */
    fun reject(rejectedBy: UUID, reason: String): VendorBill {
        if (status != VendorBillStatus.PENDING_APPROVAL) {
            throw IllegalBillOperationException("Can only reject bills pending approval")
        }
        
        return copy(
            status = VendorBillStatus.REJECTED,
            rejectedBy = rejectedBy,
            rejectionReason = reason,
            rejectedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Process payment against bill
     */
    fun processPayment(
        paymentAmount: FinancialAmount,
        paymentDate: LocalDateTime,
        paymentMethod: PaymentMethod,
        paymentReference: String,
        bankAccountId: UUID? = null
    ): VendorBill {
        if (status != VendorBillStatus.APPROVED) {
            throw IllegalBillOperationException("Can only pay approved bills")
        }
        
        validateCurrencyMatch(paymentAmount)
        
        val outstandingBalance = getOutstandingBalance()
        if (paymentAmount.isGreaterThan(outstandingBalance)) {
            throw ExcessivePaymentException("Payment amount ${paymentAmount.amount} exceeds outstanding balance ${outstandingBalance.amount}")
        }
        
        // Apply early payment discount if applicable
        val effectivePaymentAmount = if (isEligibleForEarlyPaymentDiscount(paymentDate)) {
            calculateEarlyPaymentAmount(paymentAmount)
        } else {
            paymentAmount
        }
        
        val newPaidAmount = paidAmount.add(effectivePaymentAmount)
        val paymentRecord = BillPaymentRecord(
            paymentAmount = effectivePaymentAmount,
            originalPaymentAmount = paymentAmount,
            paymentDate = paymentDate,
            paymentMethod = paymentMethod,
            paymentReference = paymentReference,
            bankAccountId = bankAccountId
        )
        
        val newStatus = if (newPaidAmount.isEqualTo(totalAmount)) {
            VendorBillStatus.PAID
        } else {
            VendorBillStatus.PARTIALLY_PAID
        }
        
        return copy(
            paidAmount = newPaidAmount,
            paymentRecords = paymentRecords + paymentRecord,
            status = newStatus,
            lastPaymentDate = paymentDate,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Void the bill
     */
    fun void(reason: String): VendorBill {
        if (status == VendorBillStatus.PAID || status == VendorBillStatus.PARTIALLY_PAID) {
            throw IllegalBillOperationException("Cannot void paid or partially paid bill")
        }
        
        return copy(
            status = VendorBillStatus.VOIDED,
            notes = (notes ?: "") + "\nVoided: $reason",
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Mark bill as overdue
     */
    fun markOverdue(): VendorBill {
        if (status in listOf(VendorBillStatus.PAID, VendorBillStatus.VOIDED, VendorBillStatus.DRAFT)) {
            throw IllegalBillOperationException("Cannot mark bill as overdue in current status: $status")
        }
        
        return copy(
            status = VendorBillStatus.OVERDUE,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Get outstanding balance
     */
    fun getOutstandingBalance(): FinancialAmount {
        return totalAmount.subtract(paidAmount)
    }
    
    /**
     * Check if bill is overdue
     */
    fun isOverdue(): Boolean {
        return LocalDate.now().isAfter(dueDate) && 
               status in listOf(VendorBillStatus.APPROVED, VendorBillStatus.PARTIALLY_PAID)
    }
    
    /**
     * Check if bill is fully paid
     */
    fun isFullyPaid(): Boolean {
        return status == VendorBillStatus.PAID
    }
    
    /**
     * Check if bill can be modified
     */
    fun canBeModified(): Boolean {
        return status in listOf(VendorBillStatus.DRAFT, VendorBillStatus.REJECTED)
    }
    
    /**
     * Check if eligible for early payment discount
     */
    fun isEligibleForEarlyPaymentDiscount(paymentDate: LocalDateTime): Boolean {
        return earlyPaymentDiscountDate?.let { discountDate ->
            paymentDate.toLocalDate().isBefore(discountDate) || paymentDate.toLocalDate().isEqual(discountDate)
        } ?: false
    }
    
    /**
     * Calculate early payment amount with discount
     */
    fun calculateEarlyPaymentAmount(paymentAmount: FinancialAmount): FinancialAmount {
        return earlyPaymentDiscountRate?.let { discountRate ->
            val discountAmount = paymentAmount.multiply(discountRate.rate)
            paymentAmount.subtract(discountAmount)
        } ?: paymentAmount
    }
    
    /**
     * Get days past due
     */
    fun getDaysPastDue(): Int {
        return if (isOverdue()) {
            LocalDate.now().toEpochDay().minus(dueDate.toEpochDay()).toInt()
        } else {
            0
        }
    }
    
    /**
     * Get days until due
     */
    fun getDaysUntilDue(): Int {
        return dueDate.toEpochDay().minus(LocalDate.now().toEpochDay()).toInt()
    }
    
    // Private helper methods
    private fun validateCanModify() {
        if (!canBeModified()) {
            throw IllegalBillOperationException("Cannot modify bill in status: $status")
        }
    }
    
    private fun validateCurrencyMatch(amount: FinancialAmount) {
        if (amount.currency != totalAmount.currency) {
            throw CurrencyMismatchException("Amount currency ${amount.currency} does not match bill currency ${totalAmount.currency}")
        }
    }
    
    private fun recalculateAmounts(updatedLineItems: List<VendorBillLineItem>): VendorBill {
        val newSubtotal = updatedLineItems.fold(FinancialAmount.ZERO) { acc, item ->
            acc.add(item.getLineTotal())
        }
        
        val newTaxAmount = updatedLineItems.fold(FinancialAmount.ZERO) { acc, item ->
            acc.add(item.getTaxAmount())
        }
        
        val newTotalAmount = newSubtotal.add(newTaxAmount).subtract(discountAmount)
        
        return copy(
            lineItems = updatedLineItems,
            subtotal = newSubtotal,
            taxAmount = newTaxAmount,
            totalAmount = newTotalAmount,
            updatedAt = LocalDateTime.now()
        )
    }
    
    companion object {
        /**
         * Create a new vendor bill
         */
        fun create(
            billNumber: String,
            vendorBillNumber: String,
            vendorId: UUID,
            vendorName: String,
            vendorAddress: Address,
            billDate: LocalDate,
            paymentTerms: PaymentTerms,
            currency: Currency,
            createdBy: UUID,
            purchaseOrderNumber: String? = null,
            description: String? = null
        ): VendorBill {
            
            val dueDate = paymentTerms.calculateDueDate(billDate)
            val earlyDiscountDate = paymentTerms.calculateEarlyPaymentDiscountDate(billDate)
            
            return VendorBill(
                billNumber = billNumber,
                vendorBillNumber = vendorBillNumber,
                vendorId = vendorId,
                vendorName = vendorName,
                vendorAddress = vendorAddress,
                billDate = billDate,
                dueDate = dueDate,
                earlyPaymentDiscountDate = earlyDiscountDate,
                earlyPaymentDiscountRate = paymentTerms.earlyPaymentDiscountRate,
                subtotal = FinancialAmount(BigDecimal.ZERO, currency),
                taxAmount = FinancialAmount(BigDecimal.ZERO, currency),
                totalAmount = FinancialAmount(BigDecimal.ZERO, currency),
                paymentTerms = paymentTerms,
                purchaseOrderNumber = purchaseOrderNumber,
                description = description,
                createdBy = createdBy
            )
        }
    }
}

/**
 * Vendor Bill Status
 */
enum class VendorBillStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    PARTIALLY_PAID,
    PAID,
    OVERDUE,
    VOIDED
}

/**
 * Bill Payment Record
 */
@Embeddable
data class BillPaymentRecord(
    val paymentAmount: FinancialAmount,
    val originalPaymentAmount: FinancialAmount,
    val paymentDate: LocalDateTime,
    val paymentMethod: PaymentMethod,
    val paymentReference: String,
    val bankAccountId: UUID? = null,
    val appliedAt: LocalDateTime = LocalDateTime.now()
)

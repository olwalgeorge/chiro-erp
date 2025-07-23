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
 * CustomerInvoice Aggregate Root
 * 
 * Core invoice aggregate that manages invoice lifecycle, line items,
 * payments, and customer billing within the financial domain.
 * 
 * Business Rules:
 * - Invoice number must be unique within fiscal period
 * - Invoice cannot be modified after being finalized
 * - Payment amount cannot exceed outstanding balance
 * - Tax calculations must be accurate based on jurisdiction
 * - Discounts cannot exceed line item totals
 */
@Entity
@Table(name = "customer_invoices")
data class CustomerInvoice(
    @Id
    val invoiceId: UUID = UUID.randomUUID(),
    
    @Column(nullable = false, unique = true)
    val invoiceNumber: String,
    
    @Column(nullable = false)
    val customerId: UUID,
    
    @Column(nullable = false)
    val customerName: String,
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "line1", column = Column(name = "billing_address_line1")),
        AttributeOverride(name = "line2", column = Column(name = "billing_address_line2")),
        AttributeOverride(name = "city", column = Column(name = "billing_city")),
        AttributeOverride(name = "state", column = Column(name = "billing_state")),
        AttributeOverride(name = "postalCode", column = Column(name = "billing_postal_code")),
        AttributeOverride(name = "country", column = Column(name = "billing_country"))
    )
    val billingAddress: Address,
    
    @Column(nullable = false)
    val invoiceDate: LocalDate,
    
    @Column(nullable = false)
    val dueDate: LocalDate,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    
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
    
    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val lineItems: List<InvoiceLineItem> = emptyList(),
    
    @ElementCollection
    @CollectionTable(name = "invoice_payment_applications")
    val paymentApplications: List<PaymentApplication> = emptyList(),
    
    @Embedded
    val paymentTerms: PaymentTerms,
    
    @Column
    val purchaseOrderNumber: String? = null,
    
    @Column(length = 1000)
    val notes: String? = null,
    
    @Column(nullable = false)
    val salesRepId: UUID? = null,
    
    @Column(nullable = false)
    val createdBy: UUID,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val finalizedAt: LocalDateTime? = null,
    
    @Column
    val sentAt: LocalDateTime? = null,
    
    @Column
    val lastPaymentDate: LocalDateTime? = null
) {

    /**
     * Add line item to invoice
     * Business rule: Cannot modify finalized invoices
     */
    fun addLineItem(
        productId: UUID,
        description: String,
        quantity: Quantity,
        unitPrice: FinancialAmount,
        taxRate: TaxRate = TaxRate.ZERO
    ): CustomerInvoice {
        validateCanModify()
        
        val lineItem = InvoiceLineItem.create(
            invoice = this,
            productId = productId,
            description = description,
            quantity = quantity,
            unitPrice = unitPrice,
            taxRate = taxRate
        )
        
        val updatedLineItems = lineItems + lineItem
        return recalculateAmounts(updatedLineItems)
    }
    
    /**
     * Remove line item from invoice
     */
    fun removeLineItem(lineItemId: UUID): CustomerInvoice {
        validateCanModify()
        
        val updatedLineItems = lineItems.filter { it.lineItemId != lineItemId }
        return recalculateAmounts(updatedLineItems)
    }
    
    /**
     * Update line item quantity
     */
    fun updateLineItemQuantity(lineItemId: UUID, newQuantity: Quantity): CustomerInvoice {
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
     * Apply discount to invoice
     */
    fun applyDiscount(discountAmount: FinancialAmount, reason: String): CustomerInvoice {
        validateCanModify()
        validateCurrencyMatch(discountAmount)
        
        if (discountAmount.isGreaterThan(subtotal)) {
            throw InvalidDiscountException("Discount amount ${discountAmount.amount} cannot exceed subtotal ${subtotal.amount}")
        }
        
        val newTotalAmount = subtotal.add(taxAmount).subtract(discountAmount)
        
        return copy(
            discountAmount = discountAmount,
            totalAmount = newTotalAmount,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Finalize invoice (make it immutable and ready for sending)
     */
    fun finalize(): CustomerInvoice {
        if (status != InvoiceStatus.DRAFT) {
            throw IllegalInvoiceOperationException("Can only finalize draft invoices")
        }
        
        if (lineItems.isEmpty()) {
            throw IllegalInvoiceOperationException("Cannot finalize invoice with no line items")
        }
        
        return copy(
            status = InvoiceStatus.FINALIZED,
            finalizedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Send invoice to customer
     */
    fun send(): CustomerInvoice {
        if (status != InvoiceStatus.FINALIZED) {
            throw IllegalInvoiceOperationException("Can only send finalized invoices")
        }
        
        return copy(
            status = InvoiceStatus.SENT,
            sentAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Apply payment to invoice
     */
    fun applyPayment(
        paymentAmount: FinancialAmount,
        paymentDate: LocalDateTime,
        paymentReference: String
    ): CustomerInvoice {
        if (status == InvoiceStatus.DRAFT) {
            throw IllegalInvoiceOperationException("Cannot apply payment to draft invoice")
        }
        
        validateCurrencyMatch(paymentAmount)
        
        val outstandingBalance = getOutstandingBalance()
        if (paymentAmount.isGreaterThan(outstandingBalance)) {
            throw ExcessivePaymentException("Payment amount ${paymentAmount.amount} exceeds outstanding balance ${outstandingBalance.amount}")
        }
        
        val newPaidAmount = paidAmount.add(paymentAmount)
        val paymentApplication = PaymentApplication(
            paymentAmount = paymentAmount,
            paymentDate = paymentDate,
            paymentReference = paymentReference
        )
        
        val newStatus = if (newPaidAmount.isEqualTo(totalAmount)) {
            InvoiceStatus.PAID
        } else {
            InvoiceStatus.PARTIALLY_PAID
        }
        
        return copy(
            paidAmount = newPaidAmount,
            paymentApplications = paymentApplications + paymentApplication,
            status = newStatus,
            lastPaymentDate = paymentDate,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Mark invoice as overdue
     */
    fun markOverdue(): CustomerInvoice {
        if (status in listOf(InvoiceStatus.PAID, InvoiceStatus.CANCELLED, InvoiceStatus.DRAFT)) {
            throw IllegalInvoiceOperationException("Cannot mark invoice as overdue in current status: $status")
        }
        
        return copy(
            status = InvoiceStatus.OVERDUE,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Cancel invoice
     */
    fun cancel(reason: String): CustomerInvoice {
        if (status == InvoiceStatus.PAID) {
            throw IllegalInvoiceOperationException("Cannot cancel paid invoice")
        }
        
        return copy(
            status = InvoiceStatus.CANCELLED,
            notes = (notes ?: "") + "\nCancellation reason: $reason",
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
     * Check if invoice is overdue
     */
    fun isOverdue(): Boolean {
        return LocalDate.now().isAfter(dueDate) && 
               status in listOf(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID)
    }
    
    /**
     * Check if invoice is fully paid
     */
    fun isFullyPaid(): Boolean {
        return status == InvoiceStatus.PAID
    }
    
    /**
     * Check if invoice can be modified
     */
    fun canBeModified(): Boolean {
        return status == InvoiceStatus.DRAFT
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
    
    // Private helper methods
    private fun validateCanModify() {
        if (!canBeModified()) {
            throw IllegalInvoiceOperationException("Cannot modify invoice in status: $status")
        }
    }
    
    private fun validateCurrencyMatch(amount: FinancialAmount) {
        if (amount.currency != totalAmount.currency) {
            throw CurrencyMismatchException("Amount currency ${amount.currency} does not match invoice currency ${totalAmount.currency}")
        }
    }
    
    private fun recalculateAmounts(updatedLineItems: List<InvoiceLineItem>): CustomerInvoice {
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
         * Create a new customer invoice
         */
        fun create(
            invoiceNumber: String,
            customerId: UUID,
            customerName: String,
            billingAddress: Address,
            dueDate: LocalDate,
            paymentTerms: PaymentTerms,
            currency: Currency,
            createdBy: UUID,
            salesRepId: UUID? = null,
            purchaseOrderNumber: String? = null
        ): CustomerInvoice {
            
            return CustomerInvoice(
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                customerName = customerName,
                billingAddress = billingAddress,
                invoiceDate = LocalDate.now(),
                dueDate = dueDate,
                subtotal = FinancialAmount(BigDecimal.ZERO, currency),
                taxAmount = FinancialAmount(BigDecimal.ZERO, currency),
                totalAmount = FinancialAmount(BigDecimal.ZERO, currency),
                paymentTerms = paymentTerms,
                createdBy = createdBy,
                salesRepId = salesRepId,
                purchaseOrderNumber = purchaseOrderNumber
            )
        }
    }
}

/**
 * Invoice Status
 */
enum class InvoiceStatus {
    DRAFT,
    FINALIZED,
    SENT,
    PARTIALLY_PAID,
    PAID,
    OVERDUE,
    CANCELLED
}

/**
 * Payment Application for tracking payments applied to invoice
 */
@Embeddable
data class PaymentApplication(
    val paymentAmount: FinancialAmount,
    val paymentDate: LocalDateTime,
    val paymentReference: String,
    val appliedAt: LocalDateTime = LocalDateTime.now()
)

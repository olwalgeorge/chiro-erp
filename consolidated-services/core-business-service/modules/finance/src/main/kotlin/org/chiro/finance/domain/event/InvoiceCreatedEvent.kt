package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * InvoiceCreatedEvent
 * 
 * Domain event published when a customer invoice is created in the system.
 * This event triggers the accounts receivable workflow and integrates with
 * various downstream systems.
 * 
 * This event enables:
 * - AR workflow automation
 * - Customer notification systems
 * - Credit limit monitoring
 * - Revenue recognition processes
 * - Business intelligence and reporting
 */
data class InvoiceCreatedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Invoice ID
    val invoiceNumber: String,
    val customerId: UUID,
    val customerName: String,
    val billingAddress: Address,
    val invoiceDate: LocalDate,
    val dueDate: LocalDate,
    val paymentTerms: PaymentTerms,
    val invoiceStatus: InvoiceStatus,
    val subtotalAmount: FinancialAmount,
    val taxAmount: FinancialAmount,
    val discountAmount: FinancialAmount,
    val totalAmount: FinancialAmount,
    val currency: Currency,
    val lineItemCount: Int,
    val productCategories: Set<String>,
    val salesRepId: UUID?,
    val salesRepName: String?,
    val departmentId: UUID?,
    val projectId: UUID?,
    val purchaseOrderNumber: String?,
    val createdBy: UUID,
    val createdByName: String,
    val fiscalPeriodId: UUID,
    val fiscalYear: Int,
    val fiscalPeriod: Int,
    val isRecurring: Boolean = false,
    val parentInvoiceId: UUID? = null,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Check if invoice is high value (requires special attention)
     */
    fun isHighValue(threshold: FinancialAmount): Boolean {
        return totalAmount.isGreaterThan(threshold)
    }
    
    /**
     * Check if invoice is due soon (within specified days)
     */
    fun isDueSoon(daysThreshold: Int = 7): Boolean {
        val today = LocalDate.now()
        val daysToDue = dueDate.toEpochDay() - today.toEpochDay()
        return daysToDue <= daysThreshold && daysToDue >= 0
    }
    
    /**
     * Check if invoice has early payment discount opportunity
     */
    fun hasEarlyPaymentDiscount(): Boolean {
        return paymentTerms.earlyPaymentDiscountRate != null && 
               paymentTerms.earlyPaymentDiscountDays != null
    }
    
    /**
     * Get early payment discount deadline
     */
    fun getEarlyPaymentDiscountDeadline(): LocalDate? {
        return paymentTerms.earlyPaymentDiscountDays?.let { days ->
            invoiceDate.plusDays(days.toLong())
        }
    }
    
    /**
     * Check if this is a large customer order
     */
    fun isLargeCustomerOrder(customerOrderThreshold: FinancialAmount): Boolean {
        return totalAmount.isGreaterThan(customerOrderThreshold)
    }
    
    /**
     * Get days until due
     */
    fun getDaysUntilDue(): Long {
        return dueDate.toEpochDay() - LocalDate.now().toEpochDay()
    }
    
    /**
     * Check if invoice has project billing
     */
    fun isProjectBilling(): Boolean = projectId != null
    
    /**
     * Check if invoice has purchase order reference
     */
    fun hasPurchaseOrderReference(): Boolean = !purchaseOrderNumber.isNullOrBlank()
    
    /**
     * Check if this is a repeat customer (based on parent invoice)
     */
    fun isRepeatCustomer(): Boolean = parentInvoiceId != null
    
    /**
     * Get invoice priority based on amount and customer
     */
    fun getInvoicePriority(): InvoicePriority {
        return when {
            totalAmount.amount >= BigDecimal("10000") -> InvoicePriority.HIGH
            totalAmount.amount >= BigDecimal("5000") -> InvoicePriority.MEDIUM
            totalAmount.amount >= BigDecimal("1000") -> InvoicePriority.NORMAL
            else -> InvoicePriority.LOW
        }
    }
    
    /**
     * Check if invoice requires approval (based on amount threshold)
     */
    fun requiresApproval(approvalThreshold: FinancialAmount): Boolean {
        return totalAmount.isGreaterThan(approvalThreshold)
    }
    
    companion object {
        /**
         * Create event from invoice aggregate
         */
        fun fromInvoice(
            invoice: Any, // CustomerInvoice aggregate
            createdByName: String,
            salesRepName: String?,
            productCategories: Set<String>,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int
        ): InvoiceCreatedEvent {
            // This would be called from the CustomerInvoice aggregate
            // Implementation would extract values from the aggregate
            TODO("Implementation depends on CustomerInvoice aggregate structure")
        }
        
        /**
         * Create event for recurring invoice
         */
        fun forRecurringInvoice(
            invoiceId: UUID,
            invoiceNumber: String,
            customerId: UUID,
            customerName: String,
            billingAddress: Address,
            invoiceDate: LocalDate,
            dueDate: LocalDate,
            paymentTerms: PaymentTerms,
            totalAmount: FinancialAmount,
            currency: Currency,
            parentInvoiceId: UUID,
            createdBy: UUID,
            createdByName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int
        ): InvoiceCreatedEvent {
            
            return InvoiceCreatedEvent(
                aggregateId = invoiceId,
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                customerName = customerName,
                billingAddress = billingAddress,
                invoiceDate = invoiceDate,
                dueDate = dueDate,
                paymentTerms = paymentTerms,
                invoiceStatus = InvoiceStatus.DRAFT,
                subtotalAmount = totalAmount,
                taxAmount = FinancialAmount.ZERO,
                discountAmount = FinancialAmount.ZERO,
                totalAmount = totalAmount,
                currency = currency,
                lineItemCount = 0,
                productCategories = emptySet(),
                salesRepId = null,
                salesRepName = null,
                departmentId = null,
                projectId = null,
                purchaseOrderNumber = null,
                createdBy = createdBy,
                createdByName = createdByName,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                isRecurring = true,
                parentInvoiceId = parentInvoiceId
            )
        }
        
        /**
         * Create event for project invoice
         */
        fun forProjectInvoice(
            invoiceId: UUID,
            invoiceNumber: String,
            customerId: UUID,
            customerName: String,
            projectId: UUID,
            totalAmount: FinancialAmount,
            currency: Currency,
            createdBy: UUID,
            createdByName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int
        ): InvoiceCreatedEvent {
            
            return InvoiceCreatedEvent(
                aggregateId = invoiceId,
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                customerName = customerName,
                billingAddress = Address.empty(), // Would be loaded from customer
                invoiceDate = LocalDate.now(),
                dueDate = LocalDate.now().plusDays(30),
                paymentTerms = PaymentTerms.NET_30,
                invoiceStatus = InvoiceStatus.DRAFT,
                subtotalAmount = totalAmount,
                taxAmount = FinancialAmount.ZERO,
                discountAmount = FinancialAmount.ZERO,
                totalAmount = totalAmount,
                currency = currency,
                lineItemCount = 0,
                productCategories = emptySet(),
                salesRepId = null,
                salesRepName = null,
                departmentId = null,
                projectId = projectId,
                purchaseOrderNumber = null,
                createdBy = createdBy,
                createdByName = createdByName,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod
            )
        }
    }
}

/**
 * Invoice Priority Levels
 */
enum class InvoicePriority {
    LOW,
    NORMAL, 
    MEDIUM,
    HIGH,
    CRITICAL
}

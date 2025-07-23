package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDateTime
import java.util.*

/**
 * PaymentProcessedEvent
 * 
 * Domain event published when a payment is successfully processed in the system.
 * This event triggers the payment pipeline workflows and integrates with
 * various financial and accounting systems.
 * 
 * This event enables:
 * - Cash flow tracking and forecasting
 * - Invoice payment matching
 * - Bank reconciliation processes
 * - Customer credit management
 * - Revenue recognition updates
 * - Financial reporting automation
 */
data class PaymentProcessedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Payment ID
    val paymentReferenceNumber: String,
    val paymentType: PaymentType,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val payerId: UUID,
    val payerName: String,
    val payerType: PayerType,
    val payeeId: UUID,
    val payeeName: String,
    val amount: FinancialAmount,
    val currency: Currency,
    val paidDate: LocalDateTime,
    val processedDate: LocalDateTime,
    val sourceAccountId: UUID?,
    val sourceAccountNumber: String?,
    val destinationAccountId: UUID,
    val destinationAccountNumber: String,
    val invoiceId: UUID?,
    val invoiceNumber: String?,
    val originalInvoiceAmount: FinancialAmount?,
    val remainingInvoiceBalance: FinancialAmount?,
    val isPartialPayment: Boolean = false,
    val isOverpayment: Boolean = false,
    val overpaymentAmount: FinancialAmount?,
    val exchangeRate: BigDecimal?,
    val baseCurrencyAmount: FinancialAmount?,
    val transactionFee: FinancialAmount?,
    val feesPaidBy: FeePayerType?,
    val bankTransactionId: String?,
    val checkNumber: String?,
    val cardLast4Digits: String?,
    val processingInstitution: String?,
    val authorizationCode: String?,
    val reconciliationStatus: ReconciliationStatus = ReconciliationStatus.PENDING,
    val processedBy: UUID,
    val processedByName: String,
    val fiscalPeriodId: UUID,
    val fiscalYear: Int,
    val fiscalPeriod: Int,
    val notes: String?,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Check if payment is high value (requires special attention)
     */
    fun isHighValue(threshold: FinancialAmount): Boolean {
        return amount.isGreaterThan(threshold)
    }
    
    /**
     * Check if payment involves foreign currency conversion
     */
    fun requiresCurrencyConversion(): Boolean {
        return exchangeRate != null && baseCurrencyAmount != null
    }
    
    /**
     * Get effective amount in base currency
     */
    fun getBaseCurrencyAmount(): FinancialAmount {
        return baseCurrencyAmount ?: amount
    }
    
    /**
     * Check if payment has transaction fees
     */
    fun hasTransactionFees(): Boolean {
        return transactionFee?.isPositive() == true
    }
    
    /**
     * Get net amount received (after fees)
     */
    fun getNetAmountReceived(): FinancialAmount {
        return if (hasTransactionFees() && feesPaidBy == FeePayerType.PAYEE) {
            amount.subtract(transactionFee!!)
        } else {
            amount
        }
    }
    
    /**
     * Check if payment is electronic
     */
    fun isElectronicPayment(): Boolean {
        return when (paymentMethod) {
            PaymentMethod.ACH,
            PaymentMethod.WIRE_TRANSFER,
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.DEBIT_CARD,
            PaymentMethod.ONLINE_PAYMENT -> true
            else -> false
        }
    }
    
    /**
     * Check if payment requires manual verification
     */
    fun requiresManualVerification(): Boolean {
        return when {
            isHighValue(FinancialAmount(BigDecimal("10000"), currency)) -> true
            isOverpayment -> true
            requiresCurrencyConversion() -> true
            paymentMethod == PaymentMethod.CASH && amount.amount >= BigDecimal("1000") -> true
            else -> false
        }
    }
    
    /**
     * Get payment processing priority
     */
    fun getProcessingPriority(): PaymentPriority {
        return when {
            isHighValue(FinancialAmount(BigDecimal("50000"), currency)) -> PaymentPriority.CRITICAL
            isHighValue(FinancialAmount(BigDecimal("10000"), currency)) -> PaymentPriority.HIGH
            isOverpayment || requiresCurrencyConversion() -> PaymentPriority.MEDIUM
            else -> PaymentPriority.NORMAL
        }
    }
    
    /**
     * Check if payment fully settles the invoice
     */
    fun fullySetsInvoice(): Boolean {
        return !isPartialPayment && remainingInvoiceBalance?.isZero() == true
    }
    
    /**
     * Get payment efficiency score (0-100)
     */
    fun getPaymentEfficiencyScore(): Int {
        var score = 100
        
        // Deduct for partial payments
        if (isPartialPayment) score -= 20
        
        // Deduct for overpayments  
        if (isOverpayment) score -= 15
        
        // Deduct for manual payment methods
        if (!isElectronicPayment()) score -= 10
        
        // Deduct for currency conversion complexity
        if (requiresCurrencyConversion()) score -= 10
        
        // Deduct for transaction fees
        if (hasTransactionFees()) score -= 5
        
        return maxOf(0, score)
    }
    
    /**
     * Check if payment affects customer credit standing
     */
    fun affectsCreditStanding(): Boolean {
        return invoiceId != null && (fullySetsInvoice() || isOverpayment)
    }
    
    companion object {
        /**
         * Create event for invoice payment
         */
        fun forInvoicePayment(
            paymentId: UUID,
            paymentReferenceNumber: String,
            paymentMethod: PaymentMethod,
            customerId: UUID,
            customerName: String,
            amount: FinancialAmount,
            currency: Currency,
            invoiceId: UUID,
            invoiceNumber: String,
            originalInvoiceAmount: FinancialAmount,
            remainingBalance: FinancialAmount,
            destinationAccountId: UUID,
            destinationAccountNumber: String,
            processedBy: UUID,
            processedByName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int
        ): PaymentProcessedEvent {
            
            val isPartial = remainingBalance.isPositive()
            val isOver = amount.isGreaterThan(originalInvoiceAmount)
            val overAmount = if (isOver) amount.subtract(originalInvoiceAmount) else null
            
            return PaymentProcessedEvent(
                aggregateId = paymentId,
                paymentReferenceNumber = paymentReferenceNumber,
                paymentType = PaymentType.CUSTOMER_PAYMENT,
                paymentMethod = paymentMethod,
                paymentStatus = PaymentStatus.PROCESSED,
                payerId = customerId,
                payerName = customerName,
                payerType = PayerType.CUSTOMER,
                payeeId = UUID.randomUUID(), // Company ID
                payeeName = "Company", // Would be from configuration
                amount = amount,
                currency = currency,
                paidDate = LocalDateTime.now(),
                processedDate = LocalDateTime.now(),
                sourceAccountId = null,
                sourceAccountNumber = null,
                destinationAccountId = destinationAccountId,
                destinationAccountNumber = destinationAccountNumber,
                invoiceId = invoiceId,
                invoiceNumber = invoiceNumber,
                originalInvoiceAmount = originalInvoiceAmount,
                remainingInvoiceBalance = remainingBalance,
                isPartialPayment = isPartial,
                isOverpayment = isOver,
                overpaymentAmount = overAmount,
                processedBy = processedBy,
                processedByName = processedByName,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                notes = if (isPartial) "Partial payment" else if (isOver) "Overpayment" else null
            )
        }
        
        /**
         * Create event for vendor payment
         */
        fun forVendorPayment(
            paymentId: UUID,
            paymentReferenceNumber: String,
            paymentMethod: PaymentMethod,
            vendorId: UUID,
            vendorName: String,
            amount: FinancialAmount,
            currency: Currency,
            sourceAccountId: UUID,
            sourceAccountNumber: String,
            processedBy: UUID,
            processedByName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int
        ): PaymentProcessedEvent {
            
            return PaymentProcessedEvent(
                aggregateId = paymentId,
                paymentReferenceNumber = paymentReferenceNumber,
                paymentType = PaymentType.VENDOR_PAYMENT,
                paymentMethod = paymentMethod,
                paymentStatus = PaymentStatus.PROCESSED,
                payerId = UUID.randomUUID(), // Company ID
                payerName = "Company",
                payerType = PayerType.COMPANY,
                payeeId = vendorId,
                payeeName = vendorName,
                amount = amount,
                currency = currency,
                paidDate = LocalDateTime.now(),
                processedDate = LocalDateTime.now(),
                sourceAccountId = sourceAccountId,
                sourceAccountNumber = sourceAccountNumber,
                destinationAccountId = UUID.randomUUID(), // Vendor account
                destinationAccountNumber = "VENDOR-ACCOUNT",
                processedBy = processedBy,
                processedByName = processedByName,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod
            )
        }
        
        /**
         * Create event for cash payment
         */
        fun forCashPayment(
            paymentId: UUID,
            paymentReferenceNumber: String,
            customerId: UUID,
            customerName: String,
            amount: FinancialAmount,
            currency: Currency,
            destinationAccountId: UUID,
            processedBy: UUID,
            processedByName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            fiscalPeriod: Int
        ): PaymentProcessedEvent {
            
            return PaymentProcessedEvent(
                aggregateId = paymentId,
                paymentReferenceNumber = paymentReferenceNumber,
                paymentType = PaymentType.CUSTOMER_PAYMENT,
                paymentMethod = PaymentMethod.CASH,
                paymentStatus = PaymentStatus.PROCESSED,
                payerId = customerId,
                payerName = customerName,
                payerType = PayerType.CUSTOMER,
                payeeId = UUID.randomUUID(), // Company ID
                payeeName = "Company",
                amount = amount,
                currency = currency,
                paidDate = LocalDateTime.now(),
                processedDate = LocalDateTime.now(),
                destinationAccountId = destinationAccountId,
                destinationAccountNumber = "CASH-ACCOUNT",
                reconciliationStatus = ReconciliationStatus.MANUAL_REQUIRED,
                processedBy = processedBy,
                processedByName = processedByName,
                fiscalPeriodId = fiscalPeriodId,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                notes = "Cash payment - manual reconciliation required"
            )
        }
    }
}

/**
 * Payment Type Classifications
 */
enum class PaymentType {
    CUSTOMER_PAYMENT,
    VENDOR_PAYMENT,
    EMPLOYEE_REIMBURSEMENT,
    TAX_PAYMENT,
    LOAN_PAYMENT,
    INTEREST_PAYMENT,
    DIVIDEND_PAYMENT,
    REFUND,
    TRANSFER,
    OTHER
}

/**
 * Payment Status Values
 */
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED,
    CANCELLED,
    REVERSED
}

/**
 * Payer Type Classifications
 */
enum class PayerType {
    CUSTOMER,
    VENDOR,
    EMPLOYEE,
    COMPANY,
    BANK,
    GOVERNMENT,
    OTHER
}

/**
 * Fee Payer Responsibility
 */
enum class FeePayerType {
    PAYER,
    PAYEE,
    SHARED
}

/**
 * Reconciliation Status
 */
enum class ReconciliationStatus {
    PENDING,
    MATCHED,
    UNMATCHED,
    MANUAL_REQUIRED,
    DISCREPANCY,
    COMPLETED
}

/**
 * Payment Processing Priority
 */
enum class PaymentPriority {
    NORMAL,
    MEDIUM,
    HIGH,
    CRITICAL
}

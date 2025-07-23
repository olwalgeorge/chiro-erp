package org.chiro.finance.domain.exception

import org.chiro.finance.domain.valueobject.FinancialAmount
import java.util.*

/**
 * DuplicateTransactionException
 * 
 * Domain exception thrown when a transaction that already exists is attempted
 * to be created again. This exception enforces transaction uniqueness and
 * prevents double-posting within the finance domain.
 * 
 * This exception is thrown when:
 * - A transaction with the same reference number already exists
 * - Duplicate payment processing is attempted
 * - Journal entry with same batch and sequence exists
 * - Invoice with same number already exists for customer
 * - Bank reconciliation entry is processed multiple times
 * - Identical financial transaction is detected within time window
 */
class DuplicateTransactionException : FinanceDomainException {
    
    val duplicateField: String
    val duplicateValue: String
    val existingTransactionId: UUID
    val existingTransactionType: String
    val existingTransactionDate: String?
    val attemptedTransactionType: String
    val attemptedAmount: FinancialAmount?
    val existingAmount: FinancialAmount?
    val customerId: UUID?
    val customerName: String?
    val vendorId: UUID?
    val vendorName: String?
    val accountId: UUID?
    val duplicateCheckMethod: DuplicateCheckMethod
    val timeWindowMinutes: Int?
    val similarityScore: Double?
    
    constructor(
        duplicateField: String,
        duplicateValue: String,
        existingTransactionId: UUID,
        existingTransactionType: String,
        attemptedTransactionType: String,
        message: String = "Duplicate transaction detected"
    ) : super(message) {
        this.duplicateField = duplicateField
        this.duplicateValue = duplicateValue
        this.existingTransactionId = existingTransactionId
        this.existingTransactionType = existingTransactionType
        this.existingTransactionDate = null
        this.attemptedTransactionType = attemptedTransactionType
        this.attemptedAmount = null
        this.existingAmount = null
        this.customerId = null
        this.customerName = null
        this.vendorId = null
        this.vendorName = null
        this.accountId = null
        this.duplicateCheckMethod = DuplicateCheckMethod.EXACT_MATCH
        this.timeWindowMinutes = null
        this.similarityScore = null
    }
    
    constructor(
        duplicateField: String,
        duplicateValue: String,
        existingTransactionId: UUID,
        existingTransactionType: String,
        existingTransactionDate: String,
        attemptedTransactionType: String,
        attemptedAmount: FinancialAmount,
        existingAmount: FinancialAmount,
        duplicateCheckMethod: DuplicateCheckMethod,
        message: String
    ) : super(message) {
        this.duplicateField = duplicateField
        this.duplicateValue = duplicateValue
        this.existingTransactionId = existingTransactionId
        this.existingTransactionType = existingTransactionType
        this.existingTransactionDate = existingTransactionDate
        this.attemptedTransactionType = attemptedTransactionType
        this.attemptedAmount = attemptedAmount
        this.existingAmount = existingAmount
        this.customerId = null
        this.customerName = null
        this.vendorId = null
        this.vendorName = null
        this.accountId = null
        this.duplicateCheckMethod = duplicateCheckMethod
        this.timeWindowMinutes = null
        this.similarityScore = null
    }
    
    constructor(
        duplicateField: String,
        duplicateValue: String,
        existingTransactionId: UUID,
        existingTransactionType: String,
        existingTransactionDate: String,
        attemptedTransactionType: String,
        attemptedAmount: FinancialAmount,
        existingAmount: FinancialAmount,
        customerId: UUID?,
        customerName: String?,
        duplicateCheckMethod: DuplicateCheckMethod,
        timeWindowMinutes: Int?,
        similarityScore: Double?,
        message: String
    ) : super(message) {
        this.duplicateField = duplicateField
        this.duplicateValue = duplicateValue
        this.existingTransactionId = existingTransactionId
        this.existingTransactionType = existingTransactionType
        this.existingTransactionDate = existingTransactionDate
        this.attemptedTransactionType = attemptedTransactionType
        this.attemptedAmount = attemptedAmount
        this.existingAmount = existingAmount
        this.customerId = customerId
        this.customerName = customerName
        this.vendorId = null
        this.vendorName = null
        this.accountId = null
        this.duplicateCheckMethod = duplicateCheckMethod
        this.timeWindowMinutes = timeWindowMinutes
        this.similarityScore = similarityScore
    }
    
    /**
     * Check if this is an exact duplicate (same reference, amount, etc.)
     */
    fun isExactDuplicate(): Boolean {
        return duplicateCheckMethod == DuplicateCheckMethod.EXACT_MATCH ||
               (attemptedAmount != null && existingAmount != null && 
                attemptedAmount.equals(existingAmount) && similarityScore == 100.0)
    }
    
    /**
     * Check if this is a potential duplicate (similar but not identical)
     */
    fun isPotentialDuplicate(): Boolean {
        return duplicateCheckMethod in setOf(
            DuplicateCheckMethod.FUZZY_MATCH,
            DuplicateCheckMethod.SIMILARITY_THRESHOLD,
            DuplicateCheckMethod.TIME_WINDOW_MATCH
        ) && !isExactDuplicate()
    }
    
    /**
     * Check if amounts match (for financial validation)
     */
    fun amountsMatch(): Boolean {
        return attemptedAmount != null && existingAmount != null && 
               attemptedAmount.equals(existingAmount)
    }
    
    /**
     * Get the confidence level of the duplicate detection
     */
    fun getConfidenceLevel(): DuplicateConfidence {
        return when {
            isExactDuplicate() -> DuplicateConfidence.CERTAIN
            similarityScore != null && similarityScore >= 95.0 -> DuplicateConfidence.VERY_HIGH
            similarityScore != null && similarityScore >= 85.0 -> DuplicateConfidence.HIGH
            similarityScore != null && similarityScore >= 70.0 -> DuplicateConfidence.MEDIUM
            isPotentialDuplicate() -> DuplicateConfidence.LOW
            else -> DuplicateConfidence.UNCERTAIN
        }
    }
    
    /**
     * Get suggested resolution actions
     */
    fun getSuggestedResolutions(): List<String> {
        val suggestions = mutableListOf<String>()
        
        when (getConfidenceLevel()) {
            DuplicateConfidence.CERTAIN -> {
                suggestions.add("Transaction already exists - do not process")
                suggestions.add("Review existing transaction: $existingTransactionId")
                suggestions.add("Check if reversal or correction is needed")
            }
            DuplicateConfidence.VERY_HIGH -> {
                suggestions.add("High probability of duplicate - verify before processing")
                suggestions.add("Compare transaction details manually")
                suggestions.add("Consider if this is a legitimate duplicate transaction")
            }
            DuplicateConfidence.HIGH -> {
                suggestions.add("Likely duplicate - requires manual review")
                suggestions.add("Verify transaction legitimacy")
                suggestions.add("Check for timing differences or corrections")
            }
            DuplicateConfidence.MEDIUM -> {
                suggestions.add("Possible duplicate - review recommended")
                suggestions.add("Compare key transaction attributes")
                suggestions.add("Proceed with caution if legitimate")
            }
            DuplicateConfidence.LOW -> {
                suggestions.add("Low probability duplicate - minor similarity detected")
                suggestions.add("Brief review recommended")
                suggestions.add("Likely safe to proceed")
            }
            DuplicateConfidence.UNCERTAIN -> {
                suggestions.add("Uncertain duplicate status - requires investigation")
                suggestions.add("Manual review of transaction details needed")
            }
        }
        
        if (duplicateField == "REFERENCE_NUMBER") {
            suggestions.add("Use a different reference number")
            suggestions.add("Add suffix or prefix to make reference unique")
        }
        
        if (timeWindowMinutes != null && timeWindowMinutes <= 60) {
            suggestions.add("This may be a legitimate retry - check processing status")
            suggestions.add("Verify if original transaction failed")
        }
        
        return suggestions
    }
    
    /**
     * Get risk level of processing this potential duplicate
     */
    fun getProcessingRisk(): ProcessingRisk {
        return when (getConfidenceLevel()) {
            DuplicateConfidence.CERTAIN -> ProcessingRisk.CRITICAL
            DuplicateConfidence.VERY_HIGH -> ProcessingRisk.HIGH
            DuplicateConfidence.HIGH -> ProcessingRisk.MEDIUM
            DuplicateConfidence.MEDIUM -> ProcessingRisk.LOW
            DuplicateConfidence.LOW -> ProcessingRisk.MINIMAL
            DuplicateConfidence.UNCERTAIN -> ProcessingRisk.MEDIUM
        }
    }
    
    override fun getErrorCode(): String = "DUPLICATE_TRANSACTION"
    
    override fun getErrorCategory(): String = "BUSINESS_RULE_VIOLATION"
    
    override fun getBusinessImpact(): String = when (getProcessingRisk()) {
        ProcessingRisk.CRITICAL -> "CRITICAL - Exact duplicate would cause financial discrepancy"
        ProcessingRisk.HIGH -> "HIGH - Likely duplicate, high risk of double-posting"
        ProcessingRisk.MEDIUM -> "MEDIUM - Potential duplicate, requires verification"
        ProcessingRisk.LOW -> "LOW - Minor duplicate indicators, low risk"
        ProcessingRisk.MINIMAL -> "MINIMAL - Slight similarity detected, minimal risk"
    }
    
    override fun getRecommendedAction(): String = getSuggestedResolutions().firstOrNull() 
        ?: "Review for potential duplicate transaction"
    
    override fun isRetryable(): Boolean = false // Duplicates should not be retried
    
    override fun getRetryDelay(): Long = -1 // Not retryable
    
    override fun requiresEscalation(): Boolean = getProcessingRisk() in setOf(
        ProcessingRisk.CRITICAL, 
        ProcessingRisk.HIGH
    )
    
    companion object {
        /**
         * Create exception for exact reference number duplicate
         */
        fun forReferenceNumber(
            referenceNumber: String,
            existingTransactionId: UUID,
            existingTransactionType: String,
            attemptedTransactionType: String
        ): DuplicateTransactionException {
            return DuplicateTransactionException(
                duplicateField = "REFERENCE_NUMBER",
                duplicateValue = referenceNumber,
                existingTransactionId = existingTransactionId,
                existingTransactionType = existingTransactionType,
                attemptedTransactionType = attemptedTransactionType,
                message = "Transaction with reference number '$referenceNumber' already exists (ID: $existingTransactionId, Type: $existingTransactionType)"
            )
        }
        
        /**
         * Create exception for payment duplicate
         */
        fun forPaymentDuplicate(
            paymentReference: String,
            existingPaymentId: UUID,
            existingPaymentDate: String,
            attemptedAmount: FinancialAmount,
            existingAmount: FinancialAmount,
            customerId: UUID,
            customerName: String
        ): DuplicateTransactionException {
            return DuplicateTransactionException(
                duplicateField = "PAYMENT_REFERENCE",
                duplicateValue = paymentReference,
                existingTransactionId = existingPaymentId,
                existingTransactionType = "CUSTOMER_PAYMENT",
                existingTransactionDate = existingPaymentDate,
                attemptedTransactionType = "CUSTOMER_PAYMENT",
                attemptedAmount = attemptedAmount,
                existingAmount = existingAmount,
                customerId = customerId,
                customerName = customerName,
                duplicateCheckMethod = DuplicateCheckMethod.EXACT_MATCH,
                timeWindowMinutes = null,
                similarityScore = 100.0,
                message = "Duplicate payment detected for customer $customerName. Payment reference '$paymentReference' already exists (ID: $existingPaymentId, Date: $existingPaymentDate, Amount: ${existingAmount.amount} ${existingAmount.currency})"
            )
        }
        
        /**
         * Create exception for invoice number duplicate
         */
        fun forInvoiceNumber(
            invoiceNumber: String,
            customerId: UUID,
            customerName: String,
            existingInvoiceId: UUID,
            existingInvoiceDate: String,
            attemptedAmount: FinancialAmount,
            existingAmount: FinancialAmount
        ): DuplicateTransactionException {
            return DuplicateTransactionException(
                duplicateField = "INVOICE_NUMBER",
                duplicateValue = invoiceNumber,
                existingTransactionId = existingInvoiceId,
                existingTransactionType = "CUSTOMER_INVOICE",
                existingTransactionDate = existingInvoiceDate,
                attemptedTransactionType = "CUSTOMER_INVOICE",
                attemptedAmount = attemptedAmount,
                existingAmount = existingAmount,
                customerId = customerId,
                customerName = customerName,
                duplicateCheckMethod = DuplicateCheckMethod.EXACT_MATCH,
                timeWindowMinutes = null,
                similarityScore = 100.0,
                message = "Duplicate invoice number '$invoiceNumber' for customer $customerName. Invoice already exists (ID: $existingInvoiceId, Date: $existingInvoiceDate, Amount: ${existingAmount.amount} ${existingAmount.currency})"
            )
        }
        
        /**
         * Create exception for journal entry duplicate
         */
        fun forJournalEntry(
            batchNumber: String,
            sequenceNumber: String,
            existingEntryId: UUID,
            existingEntryDate: String,
            attemptedAmount: FinancialAmount,
            existingAmount: FinancialAmount
        ): DuplicateTransactionException {
            return DuplicateTransactionException(
                duplicateField = "BATCH_SEQUENCE",
                duplicateValue = "$batchNumber-$sequenceNumber",
                existingTransactionId = existingEntryId,
                existingTransactionType = "JOURNAL_ENTRY",
                existingTransactionDate = existingEntryDate,
                attemptedTransactionType = "JOURNAL_ENTRY",
                attemptedAmount = attemptedAmount,
                existingAmount = existingAmount,
                duplicateCheckMethod = DuplicateCheckMethod.EXACT_MATCH,
                message = "Duplicate journal entry detected. Batch '$batchNumber' sequence '$sequenceNumber' already exists (ID: $existingEntryId, Date: $existingEntryDate, Amount: ${existingAmount.amount} ${existingAmount.currency})"
            )
        }
        
        /**
         * Create exception for potential time-window duplicate
         */
        fun forTimeWindowDuplicate(
            transactionType: String,
            existingTransactionId: UUID,
            existingTransactionDate: String,
            attemptedAmount: FinancialAmount,
            existingAmount: FinancialAmount,
            timeWindowMinutes: Int,
            similarityScore: Double,
            customerId: UUID?,
            customerName: String?
        ): DuplicateTransactionException {
            return DuplicateTransactionException(
                duplicateField = "TIME_AMOUNT_MATCH",
                duplicateValue = "${attemptedAmount.amount}_${attemptedAmount.currency}_${timeWindowMinutes}min",
                existingTransactionId = existingTransactionId,
                existingTransactionType = transactionType,
                existingTransactionDate = existingTransactionDate,
                attemptedTransactionType = transactionType,
                attemptedAmount = attemptedAmount,
                existingAmount = existingAmount,
                customerId = customerId,
                customerName = customerName,
                duplicateCheckMethod = DuplicateCheckMethod.TIME_WINDOW_MATCH,
                timeWindowMinutes = timeWindowMinutes,
                similarityScore = similarityScore,
                message = "Potential duplicate $transactionType detected within $timeWindowMinutes minutes. Similar transaction exists (ID: $existingTransactionId, Date: $existingTransactionDate, Amount: ${existingAmount.amount} ${existingAmount.currency}, Similarity: ${similarityScore}%)"
            )
        }
    }
}

/**
 * Duplicate Check Method Types
 */
enum class DuplicateCheckMethod {
    EXACT_MATCH,
    FUZZY_MATCH,
    SIMILARITY_THRESHOLD,
    TIME_WINDOW_MATCH,
    BUSINESS_RULE_MATCH
}

/**
 * Duplicate Detection Confidence Levels
 */
enum class DuplicateConfidence {
    CERTAIN,
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    UNCERTAIN
}

/**
 * Processing Risk Levels
 */
enum class ProcessingRisk {
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

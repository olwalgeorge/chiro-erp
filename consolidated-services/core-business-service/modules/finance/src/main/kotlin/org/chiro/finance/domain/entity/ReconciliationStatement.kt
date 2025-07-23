package org.chiro.finance.domain.entity

import org.chiro.finance.domain.valueobject.*
import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 * Reconciliation Statement - Bank and account reconciliation management
 * 
 * Manages the reconciliation process between bank statements and accounting records with:
 * - Automated matching of transactions
 * - Outstanding item tracking
 * - Variance identification and resolution
 * - Multi-currency reconciliation support
 * - Comprehensive audit trail
 * 
 * Key Features:
 * - Intelligent transaction matching
 * - Outstanding checks and deposits tracking
 * - Bank error identification
 * - Adjustment and correction handling
 * - Regulatory compliance reporting
 * 
 * Business Rules:
 * - Reconciliations must be performed monthly
 * - All variances must be explained
 * - Outstanding items require follow-up
 * - Adjustments require proper authorization
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Serializable
data class ReconciliationStatement(
    val id: UUID = UUID.randomUUID(),
    
    @field:NotNull(message = "Bank account ID is required")
    val bankAccountId: AccountId,
    
    @field:NotBlank(message = "Statement number cannot be blank")
    @field:Size(min = 1, max = 50, message = "Statement number must be between 1 and 50 characters")
    val statementNumber: String,
    
    @field:NotNull(message = "Statement date is required")
    val statementDate: LocalDateTime,
    
    @field:NotNull(message = "Statement period start is required")
    val periodStartDate: LocalDateTime,
    
    @field:NotNull(message = "Statement period end is required")
    val periodEndDate: LocalDateTime,
    
    @field:NotNull(message = "Bank balance is required")
    @field:Valid
    val bankBalance: FinancialAmount,
    
    @field:NotNull(message = "Book balance is required")
    @field:Valid
    val bookBalance: FinancialAmount,
    
    @field:NotNull(message = "Status is required")
    val status: ReconciliationStatus = ReconciliationStatus.IN_PROGRESS,
    
    @field:Valid
    val outstandingChecks: List<OutstandingItem> = emptyList(),
    
    @field:Valid
    val outstandingDeposits: List<OutstandingItem> = emptyList(),
    
    @field:Valid
    val bankAdjustments: List<ReconciliationAdjustment> = emptyList(),
    
    @field:Valid
    val bookAdjustments: List<ReconciliationAdjustment> = emptyList(),
    
    val reconciledBalance: FinancialAmount? = null,
    val variance: FinancialAmount? = null,
    val varianceExplanation: String? = null,
    
    @field:Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    val notes: String? = null,
    
    @field:NotNull(message = "Prepared by is required")
    val preparedBy: UUID,
    
    @field:NotNull(message = "Prepared date is required")
    val preparedDate: LocalDateTime = LocalDateTime.now(),
    
    val reviewedBy: UUID? = null,
    val reviewedDate: LocalDateTime? = null,
    
    val approvedBy: UUID? = null,
    val approvedDate: LocalDateTime? = null,
    
    val modifiedBy: UUID? = null,
    val modifiedDate: LocalDateTime? = null,
    
    val matchedTransactionCount: Int = 0,
    val unmatchedBankItems: Int = 0,
    val unmatchedBookItems: Int = 0,
    
    val previousReconciliationId: UUID? = null,
    val isAutomaticallyMatched: Boolean = false,
    val matchingConfidence: Double? = null,
    
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(statementNumber.isNotBlank()) { "Statement number cannot be blank" }
        require(periodEndDate.isAfter(periodStartDate)) {
            "Period end date must be after start date"
        }
        require(bankBalance.currency == bookBalance.currency) {
            "Bank and book balances must have the same currency"
        }
        
        // Validate outstanding items currency
        outstandingChecks.forEach { check ->
            require(check.amount.currency == bankBalance.currency) {
                "Outstanding check currency must match bank balance currency"
            }
        }
        outstandingDeposits.forEach { deposit ->
            require(deposit.amount.currency == bankBalance.currency) {
                "Outstanding deposit currency must match bank balance currency"
            }
        }
        
        // Validate adjustments currency
        bankAdjustments.forEach { adjustment ->
            require(adjustment.amount.currency == bankBalance.currency) {
                "Bank adjustment currency must match bank balance currency"
            }
        }
        bookAdjustments.forEach { adjustment ->
            require(adjustment.amount.currency == bookBalance.currency) {
                "Book adjustment currency must match book balance currency"
            }
        }
        
        // Validate status-specific requirements
        when (status) {
            ReconciliationStatus.COMPLETED -> {
                require(approvedBy != null) {
                    "Approved by is required for completed reconciliations"
                }
                require(variance == null || variance.amount == 0.0) {
                    "Completed reconciliations cannot have unresolved variances"
                }
            }
            ReconciliationStatus.VARIANCE_PENDING -> {
                require(variance != null && variance.amount != 0.0) {
                    "Variance amount is required for variance pending status"
                }
                require(!varianceExplanation.isNullOrBlank()) {
                    "Variance explanation is required for variance pending status"
                }
            }
            else -> { /* No additional validation */ }
        }
        
        // Validate matching confidence
        if (matchingConfidence != null) {
            require(matchingConfidence in 0.0..100.0) {
                "Matching confidence must be between 0 and 100"
            }
        }
    }
    
    /**
     * Calculates adjusted bank balance
     */
    fun getAdjustedBankBalance(): FinancialAmount {
        var adjustedBalance = bankBalance.amount
        
        // Subtract outstanding checks
        adjustedBalance -= outstandingChecks.sumOf { it.amount.amount }
        
        // Add outstanding deposits
        adjustedBalance += outstandingDeposits.sumOf { it.amount.amount }
        
        // Apply bank adjustments
        adjustedBalance += bankAdjustments.sumOf { 
            if (it.adjustmentType == AdjustmentType.INCREASE) it.amount.amount
            else -it.amount.amount
        }
        
        return FinancialAmount(
            amount = adjustedBalance,
            currency = bankBalance.currency
        )
    }
    
    /**
     * Calculates adjusted book balance
     */
    fun getAdjustedBookBalance(): FinancialAmount {
        var adjustedBalance = bookBalance.amount
        
        // Apply book adjustments
        adjustedBalance += bookAdjustments.sumOf { 
            if (it.adjustmentType == AdjustmentType.INCREASE) it.amount.amount
            else -it.amount.amount
        }
        
        return FinancialAmount(
            amount = adjustedBalance,
            currency = bookBalance.currency
        )
    }
    
    /**
     * Calculates reconciliation variance
     */
    fun calculateVariance(): FinancialAmount {
        val adjustedBankBalance = getAdjustedBankBalance()
        val adjustedBookBalance = getAdjustedBookBalance()
        
        val varianceAmount = adjustedBankBalance.amount - adjustedBookBalance.amount
        return FinancialAmount(
            amount = varianceAmount,
            currency = bankBalance.currency
        )
    }
    
    /**
     * Checks if reconciliation is balanced
     */
    fun isBalanced(): Boolean {
        val variance = calculateVariance()
        return kotlin.math.abs(variance.amount) < 0.01 // Allow for rounding differences
    }
    
    /**
     * Adds outstanding check
     */
    fun addOutstandingCheck(
        checkNumber: String,
        amount: FinancialAmount,
        date: LocalDateTime,
        payee: String,
        addedBy: UUID
    ): ReconciliationStatement {
        val outstandingCheck = OutstandingItem(
            itemType = OutstandingItemType.CHECK,
            referenceNumber = checkNumber,
            amount = amount,
            date = date,
            description = "Check to $payee",
            addedBy = addedBy
        )
        
        return copy(
            outstandingChecks = outstandingChecks + outstandingCheck,
            modifiedBy = addedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Adds outstanding deposit
     */
    fun addOutstandingDeposit(
        depositReference: String,
        amount: FinancialAmount,
        date: LocalDateTime,
        description: String,
        addedBy: UUID
    ): ReconciliationStatement {
        val outstandingDeposit = OutstandingItem(
            itemType = OutstandingItemType.DEPOSIT,
            referenceNumber = depositReference,
            amount = amount,
            date = date,
            description = description,
            addedBy = addedBy
        )
        
        return copy(
            outstandingDeposits = outstandingDeposits + outstandingDeposit,
            modifiedBy = addedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Adds bank adjustment
     */
    fun addBankAdjustment(
        amount: FinancialAmount,
        adjustmentType: AdjustmentType,
        reason: String,
        addedBy: UUID
    ): ReconciliationStatement {
        val adjustment = ReconciliationAdjustment(
            amount = amount,
            adjustmentType = adjustmentType,
            reason = reason,
            addedBy = addedBy
        )
        
        return copy(
            bankAdjustments = bankAdjustments + adjustment,
            modifiedBy = addedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Adds book adjustment
     */
    fun addBookAdjustment(
        amount: FinancialAmount,
        adjustmentType: AdjustmentType,
        reason: String,
        addedBy: UUID
    ): ReconciliationStatement {
        val adjustment = ReconciliationAdjustment(
            amount = amount,
            adjustmentType = adjustmentType,
            reason = reason,
            addedBy = addedBy
        )
        
        return copy(
            bookAdjustments = bookAdjustments + adjustment,
            modifiedBy = addedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Completes the reconciliation
     */
    fun complete(completedBy: UUID): ReconciliationStatement {
        require(isBalanced()) { "Reconciliation must be balanced before completion" }
        require(status == ReconciliationStatus.IN_PROGRESS) {
            "Only in-progress reconciliations can be completed"
        }
        
        return copy(
            status = ReconciliationStatus.COMPLETED,
            reconciledBalance = getAdjustedBankBalance(),
            variance = calculateVariance(),
            approvedBy = completedBy,
            approvedDate = LocalDateTime.now(),
            modifiedBy = completedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Marks reconciliation as having pending variance
     */
    fun markVariancePending(
        explanation: String,
        markedBy: UUID
    ): ReconciliationStatement {
        require(!isBalanced()) { "Balanced reconciliations cannot have pending variance" }
        require(explanation.isNotBlank()) { "Variance explanation is required" }
        
        return copy(
            status = ReconciliationStatus.VARIANCE_PENDING,
            variance = calculateVariance(),
            varianceExplanation = explanation,
            modifiedBy = markedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Reviews the reconciliation
     */
    fun review(reviewedBy: UUID, notes: String? = null): ReconciliationStatement {
        return copy(
            reviewedBy = reviewedBy,
            reviewedDate = LocalDateTime.now(),
            notes = notes?.let { existingNotes ->
                if (this.notes.isNullOrBlank()) it
                else "${this.notes}\n$it"
            } ?: this.notes,
            modifiedBy = reviewedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Gets reconciliation summary
     */
    fun getSummary(): ReconciliationSummary {
        return ReconciliationSummary(
            reconciliationId = id,
            bankBalance = bankBalance,
            bookBalance = bookBalance,
            adjustedBankBalance = getAdjustedBankBalance(),
            adjustedBookBalance = getAdjustedBookBalance(),
            variance = calculateVariance(),
            isBalanced = isBalanced(),
            outstandingCheckCount = outstandingChecks.size,
            outstandingDepositCount = outstandingDeposits.size,
            adjustmentCount = bankAdjustments.size + bookAdjustments.size,
            status = status
        )
    }
    
    companion object {
        /**
         * Creates a new reconciliation statement
         */
        fun create(
            bankAccountId: AccountId,
            statementNumber: String,
            statementDate: LocalDateTime,
            periodStartDate: LocalDateTime,
            periodEndDate: LocalDateTime,
            bankBalance: FinancialAmount,
            bookBalance: FinancialAmount,
            preparedBy: UUID
        ): ReconciliationStatement {
            return ReconciliationStatement(
                bankAccountId = bankAccountId,
                statementNumber = statementNumber,
                statementDate = statementDate,
                periodStartDate = periodStartDate,
                periodEndDate = periodEndDate,
                bankBalance = bankBalance,
                bookBalance = bookBalance,
                preparedBy = preparedBy
            )
        }
    }
}

/**
 * Outstanding items in reconciliation
 */
@Serializable
data class OutstandingItem(
    val itemType: OutstandingItemType,
    
    @field:NotBlank(message = "Reference number cannot be blank")
    val referenceNumber: String,
    
    @field:NotNull(message = "Amount is required")
    @field:Valid
    val amount: FinancialAmount,
    
    @field:NotNull(message = "Date is required")
    val date: LocalDateTime,
    
    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,
    
    @field:NotNull(message = "Added by is required")
    val addedBy: UUID,
    
    val addedDate: LocalDateTime = LocalDateTime.now()
)

/**
 * Reconciliation adjustments
 */
@Serializable
data class ReconciliationAdjustment(
    @field:NotNull(message = "Amount is required")
    @field:Valid
    val amount: FinancialAmount,
    
    @field:NotNull(message = "Adjustment type is required")
    val adjustmentType: AdjustmentType,
    
    @field:NotBlank(message = "Reason cannot be blank")
    @field:Size(min = 1, max = 500, message = "Reason must be between 1 and 500 characters")
    val reason: String,
    
    @field:NotNull(message = "Added by is required")
    val addedBy: UUID,
    
    val addedDate: LocalDateTime = LocalDateTime.now()
)

/**
 * Reconciliation summary
 */
@Serializable
data class ReconciliationSummary(
    val reconciliationId: UUID,
    val bankBalance: FinancialAmount,
    val bookBalance: FinancialAmount,
    val adjustedBankBalance: FinancialAmount,
    val adjustedBookBalance: FinancialAmount,
    val variance: FinancialAmount,
    val isBalanced: Boolean,
    val outstandingCheckCount: Int,
    val outstandingDepositCount: Int,
    val adjustmentCount: Int,
    val status: ReconciliationStatus
)

/**
 * Outstanding item types
 */
@Serializable
enum class OutstandingItemType(val description: String) {
    CHECK("Outstanding Check"),
    DEPOSIT("Outstanding Deposit"),
    TRANSFER("Outstanding Transfer"),
    FEE("Outstanding Fee"),
    OTHER("Other Outstanding Item");
}

/**
 * Adjustment types
 */
@Serializable
enum class AdjustmentType(val description: String) {
    INCREASE("Increase"),
    DECREASE("Decrease");
}

/**
 * Reconciliation status
 */
@Serializable
enum class ReconciliationStatus(val description: String) {
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    VARIANCE_PENDING("Variance Pending"),
    REJECTED("Rejected");
    
    val isFinal: Boolean
        get() = this in listOf(COMPLETED, REJECTED)
}

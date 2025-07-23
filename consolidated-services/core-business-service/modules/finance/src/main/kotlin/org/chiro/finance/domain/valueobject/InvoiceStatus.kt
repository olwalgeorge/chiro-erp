package org.chiro.finance.domain.valueobject

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.time.LocalDateTime

/**
 * Invoice Status Value Object
 * 
 * Represents the lifecycle status of invoices in the ERP system.
 * This value object encapsulates invoice state transitions and validation rules.
 * 
 * Design Pattern: Value Object (immutable, equality based on value)
 * Domain: Finance
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
enum class InvoiceStatusType {
    // ==================== DRAFT STATES ====================
    DRAFT,                      // Invoice is being created/edited
    PENDING_REVIEW,            // Awaiting review before sending
    PENDING_APPROVAL,          // Awaiting management approval
    
    // ==================== ACTIVE STATES ====================
    SENT,                      // Invoice has been sent to customer
    DELIVERED,                 // Invoice has been delivered/received
    ACKNOWLEDGED,              // Customer has acknowledged receipt
    
    // ==================== PAYMENT STATES ====================
    PAYMENT_PENDING,           // Awaiting payment
    PARTIALLY_PAID,           // Some payment received
    PAID,                     // Fully paid
    OVERPAID,                 // Payment exceeds invoice amount
    
    // ==================== OVERDUE STATES ====================
    OVERDUE,                  // Payment is overdue
    COLLECTION_NOTICE_SENT,   // Collection notice has been sent
    COLLECTION_IN_PROGRESS,   // Collection activities in progress
    
    // ==================== DISPUTE STATES ====================
    DISPUTED,                 // Customer has disputed the invoice
    DISPUTE_RESOLVED,         // Dispute has been resolved
    DISPUTE_ESCALATED,        // Dispute escalated to legal
    
    // ==================== ADJUSTMENT STATES ====================
    ADJUSTMENT_PENDING,       // Adjustment requested
    CREDIT_MEMO_ISSUED,      // Credit memo has been issued
    DEBIT_MEMO_ISSUED,       // Debit memo has been issued
    
    // ==================== CLOSED STATES ====================
    CANCELLED,                // Invoice has been cancelled
    VOIDED,                   // Invoice has been voided
    WRITTEN_OFF,              // Bad debt write-off
    REFUNDED,                 // Full refund issued
    
    // ==================== SPECIAL STATES ====================
    ON_HOLD,                  // Invoice processing on hold
    SUSPENDED,                // Account suspended
    ARCHIVED,                 // Archived for historical purposes
    
    // ==================== ERROR STATES ====================
    PROCESSING_ERROR,         // System processing error
    VALIDATION_ERROR,         // Data validation error
    INTEGRATION_ERROR         // External system integration error
}

/**
 * Invoice Status Value Object
 * 
 * Encapsulates the current status of an invoice with validation and business rules.
 * This is an immutable value object that represents the state of an invoice.
 */
data class InvoiceStatus(
    @field:NotBlank(message = "Invoice status type cannot be blank")
    val type: InvoiceStatusType,
    
    @field:Size(max = 100, message = "Status name cannot exceed 100 characters")
    val name: String = type.getDisplayName(),
    
    @field:Size(max = 500, message = "Status description cannot exceed 500 characters")
    val description: String? = null,
    
    @field:Size(max = 1000, message = "Status reason cannot exceed 1000 characters")
    val reason: String? = null,
    
    // ==================== STATUS METADATA ====================
    val isActive: Boolean = type.isActiveStatus(),
    val isFinal: Boolean = type.isFinalStatus(),
    val allowsPayment: Boolean = type.allowsPayment(),
    val allowsModification: Boolean = type.allowsModification(),
    val requiresAction: Boolean = type.requiresAction(),
    
    // ==================== AUDIT FIELDS ====================
    val statusChangedAt: LocalDateTime = LocalDateTime.now(),
    val statusChangedBy: String? = null,
    val previousStatus: InvoiceStatusType? = null,
    
    // ==================== WORKFLOW FIELDS ====================
    val nextPossibleStatuses: Set<InvoiceStatusType> = type.getNextPossibleStatuses(),
    val workflowStage: String = type.getWorkflowStage(),
    val priority: InvoiceStatusPriority = type.getDefaultPriority()
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
        
        // ==================== FACTORY METHODS ====================
        
        /**
         * Creates a new draft invoice status
         */
        fun draft(reason: String? = null): InvoiceStatus = InvoiceStatus(
            type = InvoiceStatusType.DRAFT,
            reason = reason
        )
        
        /**
         * Creates a sent invoice status
         */
        fun sent(sentBy: String? = null): InvoiceStatus = InvoiceStatus(
            type = InvoiceStatusType.SENT,
            reason = "Invoice sent to customer",
            statusChangedBy = sentBy
        )
        
        /**
         * Creates a paid invoice status
         */
        fun paid(paidBy: String? = null): InvoiceStatus = InvoiceStatus(
            type = InvoiceStatusType.PAID,
            reason = "Invoice fully paid",
            statusChangedBy = paidBy
        )
        
        /**
         * Creates a partially paid invoice status
         */
        fun partiallyPaid(amount: java.math.BigDecimal, paidBy: String? = null): InvoiceStatus = InvoiceStatus(
            type = InvoiceStatusType.PARTIALLY_PAID,
            reason = "Partial payment received: $amount",
            statusChangedBy = paidBy
        )
        
        /**
         * Creates an overdue invoice status
         */
        fun overdue(daysOverdue: Int): InvoiceStatus = InvoiceStatus(
            type = InvoiceStatusType.OVERDUE,
            reason = "Invoice is $daysOverdue days overdue",
            priority = InvoiceStatusPriority.HIGH
        )
        
        /**
         * Creates a cancelled invoice status
         */
        fun cancelled(reason: String, cancelledBy: String? = null): InvoiceStatus = InvoiceStatus(
            type = InvoiceStatusType.CANCELLED,
            reason = reason,
            statusChangedBy = cancelledBy
        )
        
        /**
         * Creates a disputed invoice status
         */
        fun disputed(disputeReason: String, disputedBy: String? = null): InvoiceStatus = InvoiceStatus(
            type = InvoiceStatusType.DISPUTED,
            reason = "Dispute: $disputeReason",
            statusChangedBy = disputedBy,
            priority = InvoiceStatusPriority.HIGH
        )
        
        /**
         * Creates a voided invoice status
         */
        fun voided(reason: String, voidedBy: String? = null): InvoiceStatus = InvoiceStatus(
            type = InvoiceStatusType.VOIDED,
            reason = reason,
            statusChangedBy = voidedBy
        )
    }
    
    // ==================== BUSINESS LOGIC ====================
    
    /**
     * Checks if the status can transition to the target status
     */
    fun canTransitionTo(targetStatus: InvoiceStatusType): Boolean {
        return nextPossibleStatuses.contains(targetStatus)
    }
    
    /**
     * Creates a new status with transition validation
     */
    fun transitionTo(
        targetStatus: InvoiceStatusType,
        reason: String? = null,
        changedBy: String? = null
    ): InvoiceStatus {
        require(canTransitionTo(targetStatus)) {
            "Invalid status transition from ${type.name} to ${targetStatus.name}"
        }
        
        return InvoiceStatus(
            type = targetStatus,
            reason = reason,
            statusChangedBy = changedBy,
            previousStatus = this.type
        )
    }
    
    /**
     * Checks if the invoice is in a payable state
     */
    fun isPayable(): Boolean {
        return allowsPayment && isActive && !isFinal
    }
    
    /**
     * Checks if the invoice can be modified
     */
    fun isModifiable(): Boolean {
        return allowsModification && !isFinal
    }
    
    /**
     * Checks if the invoice requires immediate attention
     */
    fun requiresImmediateAttention(): Boolean {
        return priority == InvoiceStatusPriority.CRITICAL || 
               type in setOf(
                   InvoiceStatusType.PROCESSING_ERROR,
                   InvoiceStatusType.VALIDATION_ERROR,
                   InvoiceStatusType.INTEGRATION_ERROR,
                   InvoiceStatusType.DISPUTED,
                   InvoiceStatusType.COLLECTION_IN_PROGRESS
               )
    }
    
    /**
     * Gets the status color for UI display
     */
    fun getStatusColor(): String = when (type) {
        InvoiceStatusType.DRAFT -> "#6B7280"                    // Gray
        InvoiceStatusType.PENDING_REVIEW -> "#F59E0B"           // Amber
        InvoiceStatusType.PENDING_APPROVAL -> "#F59E0B"         // Amber
        InvoiceStatusType.SENT -> "#3B82F6"                     // Blue
        InvoiceStatusType.DELIVERED -> "#06B6D4"                // Cyan
        InvoiceStatusType.ACKNOWLEDGED -> "#06B6D4"             // Cyan
        InvoiceStatusType.PAYMENT_PENDING -> "#8B5CF6"          // Purple
        InvoiceStatusType.PARTIALLY_PAID -> "#F97316"           // Orange
        InvoiceStatusType.PAID -> "#10B981"                     // Green
        InvoiceStatusType.OVERPAID -> "#059669"                 // Dark Green
        InvoiceStatusType.OVERDUE -> "#EF4444"                  // Red
        InvoiceStatusType.COLLECTION_NOTICE_SENT -> "#DC2626"   // Dark Red
        InvoiceStatusType.COLLECTION_IN_PROGRESS -> "#B91C1C"   // Darker Red
        InvoiceStatusType.DISPUTED -> "#7C2D12"                 // Brown
        InvoiceStatusType.DISPUTE_RESOLVED -> "#65A30D"         // Lime
        InvoiceStatusType.DISPUTE_ESCALATED -> "#991B1B"        // Very Dark Red
        InvoiceStatusType.ADJUSTMENT_PENDING -> "#D97706"       // Dark Amber
        InvoiceStatusType.CREDIT_MEMO_ISSUED -> "#0D9488"       // Teal
        InvoiceStatusType.DEBIT_MEMO_ISSUED -> "#0891B2"        // Dark Cyan
        InvoiceStatusType.CANCELLED -> "#6B7280"                // Gray
        InvoiceStatusType.VOIDED -> "#6B7280"                   // Gray
        InvoiceStatusType.WRITTEN_OFF -> "#374151"              // Dark Gray
        InvoiceStatusType.REFUNDED -> "#059669"                 // Dark Green
        InvoiceStatusType.ON_HOLD -> "#F59E0B"                  // Amber
        InvoiceStatusType.SUSPENDED -> "#EF4444"                // Red
        InvoiceStatusType.ARCHIVED -> "#6B7280"                 // Gray
        InvoiceStatusType.PROCESSING_ERROR -> "#DC2626"         // Dark Red
        InvoiceStatusType.VALIDATION_ERROR -> "#DC2626"         // Dark Red
        InvoiceStatusType.INTEGRATION_ERROR -> "#DC2626"        // Dark Red
    }
    
    /**
     * Gets the status icon for UI display
     */
    fun getStatusIcon(): String = when (type) {
        InvoiceStatusType.DRAFT -> "📝"
        InvoiceStatusType.PENDING_REVIEW -> "👀"
        InvoiceStatusType.PENDING_APPROVAL -> "✋"
        InvoiceStatusType.SENT -> "📤"
        InvoiceStatusType.DELIVERED -> "📬"
        InvoiceStatusType.ACKNOWLEDGED -> "✅"
        InvoiceStatusType.PAYMENT_PENDING -> "⏳"
        InvoiceStatusType.PARTIALLY_PAID -> "💰"
        InvoiceStatusType.PAID -> "✅"
        InvoiceStatusType.OVERPAID -> "💵"
        InvoiceStatusType.OVERDUE -> "⚠️"
        InvoiceStatusType.COLLECTION_NOTICE_SENT -> "📢"
        InvoiceStatusType.COLLECTION_IN_PROGRESS -> "🔍"
        InvoiceStatusType.DISPUTED -> "❗"
        InvoiceStatusType.DISPUTE_RESOLVED -> "🤝"
        InvoiceStatusType.DISPUTE_ESCALATED -> "⚖️"
        InvoiceStatusType.ADJUSTMENT_PENDING -> "🔄"
        InvoiceStatusType.CREDIT_MEMO_ISSUED -> "📝"
        InvoiceStatusType.DEBIT_MEMO_ISSUED -> "📝"
        InvoiceStatusType.CANCELLED -> "❌"
        InvoiceStatusType.VOIDED -> "🚫"
        InvoiceStatusType.WRITTEN_OFF -> "📋"
        InvoiceStatusType.REFUNDED -> "↩️"
        InvoiceStatusType.ON_HOLD -> "⏸️"
        InvoiceStatusType.SUSPENDED -> "🔒"
        InvoiceStatusType.ARCHIVED -> "📦"
        InvoiceStatusType.PROCESSING_ERROR -> "❌"
        InvoiceStatusType.VALIDATION_ERROR -> "❌"
        InvoiceStatusType.INTEGRATION_ERROR -> "❌"
    }
    
    /**
     * Gets a human-readable status summary
     */
    fun getSummary(): String {
        val summary = StringBuilder("${getStatusIcon()} $name")
        
        reason?.let { r ->
            summary.append(" - $r")
        }
        
        if (requiresAction) {
            summary.append(" (Action Required)")
        }
        
        return summary.toString()
    }
    
    // ==================== VALIDATION ====================
    
    init {
        require(name.isNotBlank()) { "Invoice status name cannot be blank" }
        require(name.length <= 100) { "Invoice status name cannot exceed 100 characters" }
        
        description?.let { desc ->
            require(desc.length <= 500) { "Invoice status description cannot exceed 500 characters" }
        }
        
        reason?.let { r ->
            require(r.length <= 1000) { "Invoice status reason cannot exceed 1000 characters" }
        }
        
        statusChangedBy?.let { changedBy ->
            require(changedBy.isNotBlank()) { "Status changed by cannot be blank if provided" }
        }
    }
}

/**
 * Invoice Status Priority Enum
 */
enum class InvoiceStatusPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension functions for InvoiceStatusType enum
 */
fun InvoiceStatusType.getDisplayName(): String = when (this) {
    InvoiceStatusType.DRAFT -> "Draft"
    InvoiceStatusType.PENDING_REVIEW -> "Pending Review"
    InvoiceStatusType.PENDING_APPROVAL -> "Pending Approval"
    InvoiceStatusType.SENT -> "Sent"
    InvoiceStatusType.DELIVERED -> "Delivered"
    InvoiceStatusType.ACKNOWLEDGED -> "Acknowledged"
    InvoiceStatusType.PAYMENT_PENDING -> "Payment Pending"
    InvoiceStatusType.PARTIALLY_PAID -> "Partially Paid"
    InvoiceStatusType.PAID -> "Paid"
    InvoiceStatusType.OVERPAID -> "Overpaid"
    InvoiceStatusType.OVERDUE -> "Overdue"
    InvoiceStatusType.COLLECTION_NOTICE_SENT -> "Collection Notice Sent"
    InvoiceStatusType.COLLECTION_IN_PROGRESS -> "Collection in Progress"
    InvoiceStatusType.DISPUTED -> "Disputed"
    InvoiceStatusType.DISPUTE_RESOLVED -> "Dispute Resolved"
    InvoiceStatusType.DISPUTE_ESCALATED -> "Dispute Escalated"
    InvoiceStatusType.ADJUSTMENT_PENDING -> "Adjustment Pending"
    InvoiceStatusType.CREDIT_MEMO_ISSUED -> "Credit Memo Issued"
    InvoiceStatusType.DEBIT_MEMO_ISSUED -> "Debit Memo Issued"
    InvoiceStatusType.CANCELLED -> "Cancelled"
    InvoiceStatusType.VOIDED -> "Voided"
    InvoiceStatusType.WRITTEN_OFF -> "Written Off"
    InvoiceStatusType.REFUNDED -> "Refunded"
    InvoiceStatusType.ON_HOLD -> "On Hold"
    InvoiceStatusType.SUSPENDED -> "Suspended"
    InvoiceStatusType.ARCHIVED -> "Archived"
    InvoiceStatusType.PROCESSING_ERROR -> "Processing Error"
    InvoiceStatusType.VALIDATION_ERROR -> "Validation Error"
    InvoiceStatusType.INTEGRATION_ERROR -> "Integration Error"
}

fun InvoiceStatusType.isActiveStatus(): Boolean = when (this) {
    InvoiceStatusType.SENT,
    InvoiceStatusType.DELIVERED,
    InvoiceStatusType.ACKNOWLEDGED,
    InvoiceStatusType.PAYMENT_PENDING,
    InvoiceStatusType.PARTIALLY_PAID,
    InvoiceStatusType.OVERDUE,
    InvoiceStatusType.COLLECTION_NOTICE_SENT,
    InvoiceStatusType.COLLECTION_IN_PROGRESS,
    InvoiceStatusType.DISPUTED,
    InvoiceStatusType.ADJUSTMENT_PENDING -> true
    else -> false
}

fun InvoiceStatusType.isFinalStatus(): Boolean = when (this) {
    InvoiceStatusType.PAID,
    InvoiceStatusType.CANCELLED,
    InvoiceStatusType.VOIDED,
    InvoiceStatusType.WRITTEN_OFF,
    InvoiceStatusType.REFUNDED,
    InvoiceStatusType.ARCHIVED,
    InvoiceStatusType.DISPUTE_RESOLVED -> true
    else -> false
}

fun InvoiceStatusType.allowsPayment(): Boolean = when (this) {
    InvoiceStatusType.SENT,
    InvoiceStatusType.DELIVERED,
    InvoiceStatusType.ACKNOWLEDGED,
    InvoiceStatusType.PAYMENT_PENDING,
    InvoiceStatusType.PARTIALLY_PAID,
    InvoiceStatusType.OVERDUE,
    InvoiceStatusType.COLLECTION_NOTICE_SENT,
    InvoiceStatusType.COLLECTION_IN_PROGRESS,
    InvoiceStatusType.DISPUTE_RESOLVED -> true
    else -> false
}

fun InvoiceStatusType.allowsModification(): Boolean = when (this) {
    InvoiceStatusType.DRAFT,
    InvoiceStatusType.PENDING_REVIEW,
    InvoiceStatusType.PENDING_APPROVAL -> true
    else -> false
}

fun InvoiceStatusType.requiresAction(): Boolean = when (this) {
    InvoiceStatusType.PENDING_REVIEW,
    InvoiceStatusType.PENDING_APPROVAL,
    InvoiceStatusType.OVERDUE,
    InvoiceStatusType.COLLECTION_IN_PROGRESS,
    InvoiceStatusType.DISPUTED,
    InvoiceStatusType.DISPUTE_ESCALATED,
    InvoiceStatusType.ADJUSTMENT_PENDING,
    InvoiceStatusType.ON_HOLD,
    InvoiceStatusType.PROCESSING_ERROR,
    InvoiceStatusType.VALIDATION_ERROR,
    InvoiceStatusType.INTEGRATION_ERROR -> true
    else -> false
}

fun InvoiceStatusType.getNextPossibleStatuses(): Set<InvoiceStatusType> = when (this) {
    InvoiceStatusType.DRAFT -> setOf(
        InvoiceStatusType.PENDING_REVIEW,
        InvoiceStatusType.SENT,
        InvoiceStatusType.CANCELLED,
        InvoiceStatusType.VOIDED
    )
    InvoiceStatusType.PENDING_REVIEW -> setOf(
        InvoiceStatusType.DRAFT,
        InvoiceStatusType.PENDING_APPROVAL,
        InvoiceStatusType.SENT,
        InvoiceStatusType.CANCELLED
    )
    InvoiceStatusType.PENDING_APPROVAL -> setOf(
        InvoiceStatusType.DRAFT,
        InvoiceStatusType.SENT,
        InvoiceStatusType.CANCELLED
    )
    InvoiceStatusType.SENT -> setOf(
        InvoiceStatusType.DELIVERED,
        InvoiceStatusType.PAYMENT_PENDING,
        InvoiceStatusType.DISPUTED,
        InvoiceStatusType.ON_HOLD,
        InvoiceStatusType.CANCELLED
    )
    InvoiceStatusType.DELIVERED -> setOf(
        InvoiceStatusType.ACKNOWLEDGED,
        InvoiceStatusType.PAYMENT_PENDING,
        InvoiceStatusType.DISPUTED,
        InvoiceStatusType.ON_HOLD
    )
    InvoiceStatusType.ACKNOWLEDGED -> setOf(
        InvoiceStatusType.PAYMENT_PENDING,
        InvoiceStatusType.PARTIALLY_PAID,
        InvoiceStatusType.PAID,
        InvoiceStatusType.DISPUTED
    )
    InvoiceStatusType.PAYMENT_PENDING -> setOf(
        InvoiceStatusType.PARTIALLY_PAID,
        InvoiceStatusType.PAID,
        InvoiceStatusType.OVERDUE,
        InvoiceStatusType.DISPUTED,
        InvoiceStatusType.ON_HOLD
    )
    InvoiceStatusType.PARTIALLY_PAID -> setOf(
        InvoiceStatusType.PAID,
        InvoiceStatusType.OVERDUE,
        InvoiceStatusType.DISPUTED,
        InvoiceStatusType.ADJUSTMENT_PENDING
    )
    InvoiceStatusType.PAID -> setOf(
        InvoiceStatusType.OVERPAID,
        InvoiceStatusType.REFUNDED,
        InvoiceStatusType.ARCHIVED
    )
    InvoiceStatusType.OVERDUE -> setOf(
        InvoiceStatusType.PAID,
        InvoiceStatusType.PARTIALLY_PAID,
        InvoiceStatusType.COLLECTION_NOTICE_SENT,
        InvoiceStatusType.DISPUTED,
        InvoiceStatusType.WRITTEN_OFF
    )
    InvoiceStatusType.COLLECTION_NOTICE_SENT -> setOf(
        InvoiceStatusType.PAID,
        InvoiceStatusType.PARTIALLY_PAID,
        InvoiceStatusType.COLLECTION_IN_PROGRESS,
        InvoiceStatusType.WRITTEN_OFF
    )
    InvoiceStatusType.COLLECTION_IN_PROGRESS -> setOf(
        InvoiceStatusType.PAID,
        InvoiceStatusType.PARTIALLY_PAID,
        InvoiceStatusType.DISPUTE_ESCALATED,
        InvoiceStatusType.WRITTEN_OFF
    )
    InvoiceStatusType.DISPUTED -> setOf(
        InvoiceStatusType.DISPUTE_RESOLVED,
        InvoiceStatusType.DISPUTE_ESCALATED,
        InvoiceStatusType.ADJUSTMENT_PENDING,
        InvoiceStatusType.CANCELLED
    )
    InvoiceStatusType.DISPUTE_RESOLVED -> setOf(
        InvoiceStatusType.PAYMENT_PENDING,
        InvoiceStatusType.PAID,
        InvoiceStatusType.ADJUSTMENT_PENDING,
        InvoiceStatusType.ARCHIVED
    )
    InvoiceStatusType.DISPUTE_ESCALATED -> setOf(
        InvoiceStatusType.DISPUTE_RESOLVED,
        InvoiceStatusType.WRITTEN_OFF,
        InvoiceStatusType.CANCELLED
    )
    InvoiceStatusType.ADJUSTMENT_PENDING -> setOf(
        InvoiceStatusType.CREDIT_MEMO_ISSUED,
        InvoiceStatusType.DEBIT_MEMO_ISSUED,
        InvoiceStatusType.PAYMENT_PENDING
    )
    InvoiceStatusType.CREDIT_MEMO_ISSUED -> setOf(
        InvoiceStatusType.PAID,
        InvoiceStatusType.ARCHIVED
    )
    InvoiceStatusType.DEBIT_MEMO_ISSUED -> setOf(
        InvoiceStatusType.PAYMENT_PENDING,
        InvoiceStatusType.PAID
    )
    InvoiceStatusType.ON_HOLD -> setOf(
        InvoiceStatusType.PAYMENT_PENDING,
        InvoiceStatusType.CANCELLED,
        InvoiceStatusType.SUSPENDED
    )
    InvoiceStatusType.SUSPENDED -> setOf(
        InvoiceStatusType.ON_HOLD,
        InvoiceStatusType.CANCELLED,
        InvoiceStatusType.WRITTEN_OFF
    )
    InvoiceStatusType.PROCESSING_ERROR -> setOf(
        InvoiceStatusType.DRAFT,
        InvoiceStatusType.VOIDED
    )
    InvoiceStatusType.VALIDATION_ERROR -> setOf(
        InvoiceStatusType.DRAFT,
        InvoiceStatusType.VOIDED
    )
    InvoiceStatusType.INTEGRATION_ERROR -> setOf(
        InvoiceStatusType.SENT,
        InvoiceStatusType.VOIDED
    )
    else -> emptySet() // Final statuses have no next statuses
}

fun InvoiceStatusType.getWorkflowStage(): String = when (this) {
    InvoiceStatusType.DRAFT,
    InvoiceStatusType.PENDING_REVIEW,
    InvoiceStatusType.PENDING_APPROVAL -> "Creation"
    InvoiceStatusType.SENT,
    InvoiceStatusType.DELIVERED,
    InvoiceStatusType.ACKNOWLEDGED -> "Distribution"
    InvoiceStatusType.PAYMENT_PENDING,
    InvoiceStatusType.PARTIALLY_PAID,
    InvoiceStatusType.PAID,
    InvoiceStatusType.OVERPAID -> "Payment"
    InvoiceStatusType.OVERDUE,
    InvoiceStatusType.COLLECTION_NOTICE_SENT,
    InvoiceStatusType.COLLECTION_IN_PROGRESS -> "Collection"
    InvoiceStatusType.DISPUTED,
    InvoiceStatusType.DISPUTE_RESOLVED,
    InvoiceStatusType.DISPUTE_ESCALATED -> "Dispute"
    InvoiceStatusType.ADJUSTMENT_PENDING,
    InvoiceStatusType.CREDIT_MEMO_ISSUED,
    InvoiceStatusType.DEBIT_MEMO_ISSUED -> "Adjustment"
    InvoiceStatusType.CANCELLED,
    InvoiceStatusType.VOIDED,
    InvoiceStatusType.WRITTEN_OFF,
    InvoiceStatusType.REFUNDED,
    InvoiceStatusType.ARCHIVED -> "Closed"
    InvoiceStatusType.ON_HOLD,
    InvoiceStatusType.SUSPENDED -> "Hold"
    InvoiceStatusType.PROCESSING_ERROR,
    InvoiceStatusType.VALIDATION_ERROR,
    InvoiceStatusType.INTEGRATION_ERROR -> "Error"
}

fun InvoiceStatusType.getDefaultPriority(): InvoiceStatusPriority = when (this) {
    InvoiceStatusType.PROCESSING_ERROR,
    InvoiceStatusType.VALIDATION_ERROR,
    InvoiceStatusType.INTEGRATION_ERROR,
    InvoiceStatusType.DISPUTE_ESCALATED -> InvoiceStatusPriority.CRITICAL
    InvoiceStatusType.OVERDUE,
    InvoiceStatusType.COLLECTION_IN_PROGRESS,
    InvoiceStatusType.DISPUTED,
    InvoiceStatusType.SUSPENDED -> InvoiceStatusPriority.HIGH
    InvoiceStatusType.PENDING_APPROVAL,
    InvoiceStatusType.COLLECTION_NOTICE_SENT,
    InvoiceStatusType.ADJUSTMENT_PENDING,
    InvoiceStatusType.ON_HOLD -> InvoiceStatusPriority.NORMAL
    else -> InvoiceStatusPriority.LOW
}

package org.chiro.finance.domain.valueobject

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Fiscal Period Status Value Object
 * 
 * Represents the status and lifecycle of fiscal periods in the ERP system.
 * This value object encapsulates fiscal period states, validation rules, and business logic.
 * 
 * Design Pattern: Value Object (immutable, equality based on value)
 * Domain: Finance
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
enum class FiscalPeriodStatusType {
    // ==================== PLANNING STATUSES ====================
    PLANNING,                   // Period is in planning phase
    DRAFT,                      // Period is drafted but not finalized
    PENDING_APPROVAL,          // Period setup pending approval
    APPROVED,                  // Period has been approved for use
    
    // ==================== ACTIVE STATUSES ====================
    OPEN,                      // Period is open for transactions
    CURRENT,                   // Currently active period
    ACTIVE,                    // Period is active but not current
    
    // ==================== CLOSING STATUSES ====================
    SOFT_CLOSE,                // Soft close - limited access
    HARD_CLOSE,                // Hard close - no new transactions
    CLOSING_IN_PROGRESS,       // Period closing procedures in progress
    PRELIMINARY_CLOSE,         // Preliminary period close
    
    // ==================== CLOSED STATUSES ====================
    CLOSED,                    // Period is closed
    PERMANENTLY_CLOSED,        // Period is permanently closed
    ARCHIVED,                  // Period is archived
    
    // ==================== ADJUSTMENT STATUSES ====================
    ADJUSTMENT_PERIOD,         // Period for adjustments only
    YEAR_END_ADJUSTMENTS,      // Year-end adjustment period
    AUDIT_ADJUSTMENTS,         // Audit adjustment period
    PRIOR_PERIOD_ADJUSTMENTS,  // Prior period adjustment entries
    
    // ==================== SPECIAL STATUSES ====================
    SUSPENDED,                 // Period operations suspended
    LOCKED,                    // Period is locked for transactions
    FROZEN,                    // Period is frozen temporarily
    UNDER_REVIEW,              // Period under financial review
    
    // ==================== ERROR STATUSES ====================
    ERROR_STATE,               // Period in error state
    RECONCILIATION_REQUIRED,   // Requires reconciliation
    VALIDATION_FAILED,         // Period validation failed
    DATA_INCONSISTENCY,        // Data inconsistency detected
    
    // ==================== MAINTENANCE STATUSES ====================
    MAINTENANCE_MODE,          // Period in maintenance mode
    ROLLBACK_IN_PROGRESS,      // Period rollback in progress
    BACKUP_RESTORE,            // Backup/restore operation
    
    // ==================== FUTURE STATUSES ====================
    FUTURE_PERIOD,             // Future period not yet active
    SCHEDULED_OPEN,            // Scheduled to open
    SCHEDULED_CLOSE            // Scheduled to close
}

/**
 * Fiscal Period Priority Enum
 */
enum class FiscalPeriodPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL,
    URGENT
}

/**
 * Fiscal Period Status Value Object
 * 
 * Encapsulates the status of a fiscal period with business rules and validation.
 * This is an immutable value object that represents the state of a fiscal period.
 */
data class FiscalPeriodStatus(
    @field:NotBlank(message = "Fiscal period status type cannot be blank")
    val type: FiscalPeriodStatusType,
    
    @field:Size(max = 100, message = "Status name cannot exceed 100 characters")
    val name: String = type.getDisplayName(),
    
    @field:Size(max = 500, message = "Status description cannot exceed 500 characters")
    val description: String? = null,
    
    @field:Size(max = 1000, message = "Status reason cannot exceed 1000 characters")
    val reason: String? = null,
    
    // ==================== STATUS METADATA ====================
    val isActive: Boolean = type.isActiveStatus(),
    val isEditable: Boolean = type.allowsEditing(),
    val allowsNewTransactions: Boolean = type.allowsNewTransactions(),
    val allowsAdjustments: Boolean = type.allowsAdjustments(),
    val requiresApproval: Boolean = type.requiresApproval(),
    val isFinal: Boolean = type.isFinalStatus(),
    
    // ==================== TRANSACTION CONTROLS ====================
    val allowsJournalEntries: Boolean = type.allowsJournalEntries(),
    val allowsReversals: Boolean = type.allowsReversals(),
    val allowsReclassifications: Boolean = type.allowsReclassifications(),
    val allowsAccruals: Boolean = type.allowsAccruals(),
    val restrictToAdjustmentsOnly: Boolean = type.isAdjustmentOnlyStatus(),
    
    // ==================== WORKFLOW CONTROLS ====================
    val priority: FiscalPeriodPriority = type.getDefaultPriority(),
    val escalationRequired: Boolean = false,
    val automatedProcessingEnabled: Boolean = type.supportsAutomatedProcessing(),
    val batchProcessingAllowed: Boolean = true,
    
    // ==================== NOTIFICATION SETTINGS ====================
    val notifyOnStatusChange: Boolean = true,
    val notifyStakeholders: Boolean = type.requiresStakeholderNotification(),
    val alertOnDeadlines: Boolean = true,
    val sendReminders: Boolean = false,
    
    // ==================== DEADLINE TRACKING ====================
    val hasDeadlines: Boolean = type.hasDeadlines(),
    val softCloseDeadline: LocalDate? = null,
    val hardCloseDeadline: LocalDate? = null,
    val finalReportingDeadline: LocalDate? = null,
    val complianceDeadline: LocalDate? = null,
    
    // ==================== AUDIT AND COMPLIANCE ====================
    val requiresAuditTrail: Boolean = true,
    val requiresApprovalSignature: Boolean = type.requiresApprovalSignature(),
    val requiresSecondaryApproval: Boolean = false,
    val complianceValidationRequired: Boolean = type.requiresComplianceValidation(),
    val regulatoryReportingRequired: Boolean = false,
    
    // ==================== STATUS HISTORY ====================
    val statusChangedAt: LocalDateTime = LocalDateTime.now(),
    val statusChangedBy: String? = null,
    val previousStatus: FiscalPeriodStatusType? = null,
    val statusChangeReason: String? = null,
    
    // ==================== NEXT POSSIBLE STATES ====================
    val nextPossibleStatuses: Set<FiscalPeriodStatusType> = type.getNextPossibleStatuses(),
    val autoTransitionEnabled: Boolean = false,
    val autoTransitionDate: LocalDate? = null,
    val autoTransitionConditions: String? = null
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
        
        // ==================== FACTORY METHODS ====================
        
        /**
         * Creates a new planning status
         */
        fun planning(reason: String? = null): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.PLANNING,
            reason = reason,
            description = "Period is in planning phase"
        )
        
        /**
         * Creates a draft status
         */
        fun draft(createdBy: String? = null): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.DRAFT,
            statusChangedBy = createdBy,
            description = "Period is drafted but not finalized"
        )
        
        /**
         * Creates an open status
         */
        fun open(openedBy: String? = null): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.OPEN,
            statusChangedBy = openedBy,
            reason = "Period opened for transactions"
        )
        
        /**
         * Creates a current period status
         */
        fun current(activatedBy: String? = null): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.CURRENT,
            statusChangedBy = activatedBy,
            reason = "Period is currently active",
            priority = FiscalPeriodPriority.HIGH
        )
        
        /**
         * Creates a soft close status
         */
        fun softClose(
            closedBy: String? = null,
            deadline: LocalDate? = null
        ): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.SOFT_CLOSE,
            statusChangedBy = closedBy,
            softCloseDeadline = deadline,
            reason = "Period in soft close - limited access"
        )
        
        /**
         * Creates a hard close status
         */
        fun hardClose(
            closedBy: String? = null,
            deadline: LocalDate? = null
        ): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.HARD_CLOSE,
            statusChangedBy = closedBy,
            hardCloseDeadline = deadline,
            reason = "Period in hard close - no new transactions"
        )
        
        /**
         * Creates a closed status
         */
        fun closed(
            closedBy: String,
            finalReportingDeadline: LocalDate? = null
        ): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.CLOSED,
            statusChangedBy = closedBy,
            finalReportingDeadline = finalReportingDeadline,
            reason = "Period has been closed"
        )
        
        /**
         * Creates an adjustment period status
         */
        fun adjustmentPeriod(
            reason: String,
            approvedBy: String? = null
        ): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.ADJUSTMENT_PERIOD,
            reason = reason,
            statusChangedBy = approvedBy,
            description = "Period for adjustments only"
        )
        
        /**
         * Creates a suspended status
         */
        fun suspended(
            reason: String,
            suspendedBy: String? = null
        ): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.SUSPENDED,
            reason = reason,
            statusChangedBy = suspendedBy,
            priority = FiscalPeriodPriority.HIGH
        )
        
        /**
         * Creates an error state status
         */
        fun errorState(
            errorDescription: String,
            detectedBy: String? = null
        ): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.ERROR_STATE,
            reason = errorDescription,
            statusChangedBy = detectedBy,
            priority = FiscalPeriodPriority.CRITICAL,
            escalationRequired = true
        )
        
        /**
         * Creates a future period status
         */
        fun futurePeriod(
            scheduledOpenDate: LocalDate? = null
        ): FiscalPeriodStatus = FiscalPeriodStatus(
            type = FiscalPeriodStatusType.FUTURE_PERIOD,
            autoTransitionDate = scheduledOpenDate,
            autoTransitionEnabled = scheduledOpenDate != null,
            description = "Future period not yet active"
        )
    }
    
    // ==================== COMPUTED PROPERTIES ====================
    
    /**
     * Check if the period is operationally active
     */
    val isOperationallyActive: Boolean
        get() = isActive && !isFinal && allowsNewTransactions
    
    /**
     * Check if the period requires immediate attention
     */
    val requiresImmediateAttention: Boolean
        get() = priority in setOf(FiscalPeriodPriority.CRITICAL, FiscalPeriodPriority.URGENT) ||
                escalationRequired ||
                type in setOf(
                    FiscalPeriodStatusType.ERROR_STATE,
                    FiscalPeriodStatusType.VALIDATION_FAILED,
                    FiscalPeriodStatusType.DATA_INCONSISTENCY,
                    FiscalPeriodStatusType.RECONCILIATION_REQUIRED
                )
    
    /**
     * Check if any deadlines are approaching
     */
    val hasApproachingDeadlines: Boolean
        get() {
            val today = LocalDate.now()
            val warningPeriod = 7L // 7 days warning
            
            return listOfNotNull(
                softCloseDeadline,
                hardCloseDeadline,
                finalReportingDeadline,
                complianceDeadline
            ).any { deadline ->
                deadline.minusDays(warningPeriod) <= today && deadline >= today
            }
        }
    
    /**
     * Check if any deadlines have been missed
     */
    val hasMissedDeadlines: Boolean
        get() {
            val today = LocalDate.now()
            return listOfNotNull(
                softCloseDeadline,
                hardCloseDeadline,
                finalReportingDeadline,
                complianceDeadline
            ).any { deadline ->
                deadline < today
            }
        }
    
    /**
     * Get the most urgent upcoming deadline
     */
    val nextDeadline: LocalDate?
        get() {
            val today = LocalDate.now()
            return listOfNotNull(
                softCloseDeadline,
                hardCloseDeadline,
                finalReportingDeadline,
                complianceDeadline
            ).filter { it >= today }
             .minOrNull()
        }
    
    /**
     * Get days until next deadline
     */
    val daysUntilNextDeadline: Long?
        get() = nextDeadline?.let { deadline ->
            java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline)
        }
    
    // ==================== BUSINESS LOGIC ====================
    
    /**
     * Checks if the status can transition to the target status
     */
    fun canTransitionTo(targetStatus: FiscalPeriodStatusType): Boolean {
        return nextPossibleStatuses.contains(targetStatus)
    }
    
    /**
     * Creates a new status with transition validation
     */
    fun transitionTo(
        targetStatus: FiscalPeriodStatusType,
        reason: String? = null,
        changedBy: String? = null,
        requiresApproval: Boolean = false
    ): FiscalPeriodStatus {
        require(canTransitionTo(targetStatus)) {
            "Invalid status transition from ${type.name} to ${targetStatus.name}"
        }
        
        return FiscalPeriodStatus(
            type = targetStatus,
            reason = reason,
            statusChangedBy = changedBy,
            previousStatus = this.type,
            statusChangeReason = reason,
            requiresApproval = requiresApproval
        )
    }
    
    /**
     * Validates if a transaction type is allowed in this status
     */
    fun isTransactionAllowed(transactionType: String): Boolean {
        return when {
            !allowsNewTransactions -> false
            restrictToAdjustmentsOnly -> transactionType.contains("adjustment", ignoreCase = true)
            type == FiscalPeriodStatusType.ADJUSTMENT_PERIOD -> 
                transactionType.contains("adjustment", ignoreCase = true)
            type == FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS -> 
                transactionType.contains("year-end", ignoreCase = true) ||
                transactionType.contains("adjustment", ignoreCase = true)
            type == FiscalPeriodStatusType.AUDIT_ADJUSTMENTS ->
                transactionType.contains("audit", ignoreCase = true) ||
                transactionType.contains("adjustment", ignoreCase = true)
            else -> true
        }
    }
    
    /**
     * Gets the validation rules for this status
     */
    fun getValidationRules(): List<String> = buildList {
        if (requiresApproval) {
            add("Requires approval before processing")
        }
        if (requiresApprovalSignature) {
            add("Requires digital signature for approval")
        }
        if (requiresSecondaryApproval) {
            add("Requires secondary approval")
        }
        if (complianceValidationRequired) {
            add("Must pass compliance validation")
        }
        if (regulatoryReportingRequired) {
            add("Regulatory reporting must be completed")
        }
        if (restrictToAdjustmentsOnly) {
            add("Only adjustment transactions allowed")
        }
        if (!allowsNewTransactions) {
            add("No new transactions allowed")
        }
        if (!allowsReversals) {
            add("Transaction reversals not allowed")
        }
        if (hasDeadlines) {
            add("Must meet specified deadlines")
        }
    }
    
    /**
     * Gets available actions for this status
     */
    fun getAvailableActions(): List<String> = buildList {
        if (isEditable) {
            add("Edit Period Settings")
        }
        if (allowsNewTransactions) {
            add("Create New Transactions")
        }
        if (allowsAdjustments) {
            add("Make Adjustments")
        }
        if (allowsReversals) {
            add("Reverse Transactions")
        }
        if (allowsReclassifications) {
            add("Reclassify Entries")
        }
        if (allowsAccruals) {
            add("Create Accruals")
        }
        
        nextPossibleStatuses.forEach { nextStatus ->
            add("Transition to ${nextStatus.getDisplayName()}")
        }
        
        if (type in setOf(
            FiscalPeriodStatusType.SUSPENDED,
            FiscalPeriodStatusType.LOCKED,
            FiscalPeriodStatusType.FROZEN
        )) {
            add("Reactivate Period")
        }
        
        if (type == FiscalPeriodStatusType.ERROR_STATE) {
            add("Diagnose Issues")
            add("Attempt Recovery")
        }
        
        if (type == FiscalPeriodStatusType.RECONCILIATION_REQUIRED) {
            add("Perform Reconciliation")
        }
        
        if (batchProcessingAllowed) {
            add("Run Batch Processing")
        }
    }
    
    /**
     * Checks if period can be reopened
     */
    fun canReopen(): Boolean {
        return type in setOf(
            FiscalPeriodStatusType.SOFT_CLOSE,
            FiscalPeriodStatusType.HARD_CLOSE,
            FiscalPeriodStatusType.CLOSED
        ) && !type.isFinalStatus()
    }
    
    /**
     * Sets deadlines for the period
     */
    fun withDeadlines(
        softClose: LocalDate? = null,
        hardClose: LocalDate? = null,
        finalReporting: LocalDate? = null,
        compliance: LocalDate? = null
    ): FiscalPeriodStatus {
        return copy(
            softCloseDeadline = softClose,
            hardCloseDeadline = hardClose,
            finalReportingDeadline = finalReporting,
            complianceDeadline = compliance,
            hasDeadlines = listOfNotNull(softClose, hardClose, finalReporting, compliance).isNotEmpty()
        )
    }
    
    /**
     * Enables auto-transition with conditions
     */
    fun withAutoTransition(
        transitionDate: LocalDate,
        conditions: String? = null
    ): FiscalPeriodStatus {
        return copy(
            autoTransitionEnabled = true,
            autoTransitionDate = transitionDate,
            autoTransitionConditions = conditions
        )
    }
    
    /**
     * Updates priority level
     */
    fun withPriority(
        newPriority: FiscalPeriodPriority,
        escalate: Boolean = false
    ): FiscalPeriodStatus {
        return copy(
            priority = newPriority,
            escalationRequired = escalate
        )
    }
    
    /**
     * Gets status color for UI display
     */
    fun getStatusColor(): String = when (type) {
        FiscalPeriodStatusType.PLANNING -> "#6B7280"              // Gray
        FiscalPeriodStatusType.DRAFT -> "#6B7280"                 // Gray
        FiscalPeriodStatusType.PENDING_APPROVAL -> "#F59E0B"      // Amber
        FiscalPeriodStatusType.APPROVED -> "#10B981"              // Green
        FiscalPeriodStatusType.OPEN -> "#3B82F6"                  // Blue
        FiscalPeriodStatusType.CURRENT -> "#059669"               // Dark Green
        FiscalPeriodStatusType.ACTIVE -> "#06B6D4"                // Cyan
        FiscalPeriodStatusType.SOFT_CLOSE -> "#F97316"            // Orange
        FiscalPeriodStatusType.HARD_CLOSE -> "#EF4444"            // Red
        FiscalPeriodStatusType.CLOSING_IN_PROGRESS -> "#D97706"   // Dark Amber
        FiscalPeriodStatusType.PRELIMINARY_CLOSE -> "#F59E0B"     // Amber
        FiscalPeriodStatusType.CLOSED -> "#6B7280"                // Gray
        FiscalPeriodStatusType.PERMANENTLY_CLOSED -> "#374151"    // Dark Gray
        FiscalPeriodStatusType.ARCHIVED -> "#6B7280"              // Gray
        FiscalPeriodStatusType.ADJUSTMENT_PERIOD -> "#8B5CF6"     // Purple
        FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS -> "#7C3AED"  // Dark Purple
        FiscalPeriodStatusType.AUDIT_ADJUSTMENTS -> "#5B21B6"     // Darker Purple
        FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS -> "#6D28D9" // Purple
        FiscalPeriodStatusType.SUSPENDED -> "#EF4444"             // Red
        FiscalPeriodStatusType.LOCKED -> "#DC2626"                // Dark Red
        FiscalPeriodStatusType.FROZEN -> "#1E40AF"                // Dark Blue
        FiscalPeriodStatusType.UNDER_REVIEW -> "#0891B2"          // Dark Cyan
        FiscalPeriodStatusType.ERROR_STATE -> "#B91C1C"           // Very Dark Red
        FiscalPeriodStatusType.RECONCILIATION_REQUIRED -> "#C2410C" // Dark Orange
        FiscalPeriodStatusType.VALIDATION_FAILED -> "#B91C1C"     // Very Dark Red
        FiscalPeriodStatusType.DATA_INCONSISTENCY -> "#991B1B"    // Darker Red
        FiscalPeriodStatusType.MAINTENANCE_MODE -> "#4B5563"      // Gray
        FiscalPeriodStatusType.ROLLBACK_IN_PROGRESS -> "#7C2D12"  // Brown
        FiscalPeriodStatusType.BACKUP_RESTORE -> "#92400E"        // Dark Amber
        FiscalPeriodStatusType.FUTURE_PERIOD -> "#A3A3A3"         // Light Gray
        FiscalPeriodStatusType.SCHEDULED_OPEN -> "#65A30D"        // Lime
        FiscalPeriodStatusType.SCHEDULED_CLOSE -> "#CA8A04"       // Yellow
    }
    
    /**
     * Gets status icon for UI display
     */
    fun getStatusIcon(): String = when (type) {
        FiscalPeriodStatusType.PLANNING -> "📋"
        FiscalPeriodStatusType.DRAFT -> "📝"
        FiscalPeriodStatusType.PENDING_APPROVAL -> "⏳"
        FiscalPeriodStatusType.APPROVED -> "✅"
        FiscalPeriodStatusType.OPEN -> "🔓"
        FiscalPeriodStatusType.CURRENT -> "🟢"
        FiscalPeriodStatusType.ACTIVE -> "🔵"
        FiscalPeriodStatusType.SOFT_CLOSE -> "🟡"
        FiscalPeriodStatusType.HARD_CLOSE -> "🔴"
        FiscalPeriodStatusType.CLOSING_IN_PROGRESS -> "⏳"
        FiscalPeriodStatusType.PRELIMINARY_CLOSE -> "⚠️"
        FiscalPeriodStatusType.CLOSED -> "🔒"
        FiscalPeriodStatusType.PERMANENTLY_CLOSED -> "🔐"
        FiscalPeriodStatusType.ARCHIVED -> "📦"
        FiscalPeriodStatusType.ADJUSTMENT_PERIOD -> "🔧"
        FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS -> "📊"
        FiscalPeriodStatusType.AUDIT_ADJUSTMENTS -> "🔍"
        FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS -> "⏪"
        FiscalPeriodStatusType.SUSPENDED -> "⏸️"
        FiscalPeriodStatusType.LOCKED -> "🔒"
        FiscalPeriodStatusType.FROZEN -> "❄️"
        FiscalPeriodStatusType.UNDER_REVIEW -> "👀"
        FiscalPeriodStatusType.ERROR_STATE -> "❌"
        FiscalPeriodStatusType.RECONCILIATION_REQUIRED -> "⚖️"
        FiscalPeriodStatusType.VALIDATION_FAILED -> "❌"
        FiscalPeriodStatusType.DATA_INCONSISTENCY -> "⚠️"
        FiscalPeriodStatusType.MAINTENANCE_MODE -> "🛠️"
        FiscalPeriodStatusType.ROLLBACK_IN_PROGRESS -> "↩️"
        FiscalPeriodStatusType.BACKUP_RESTORE -> "💾"
        FiscalPeriodStatusType.FUTURE_PERIOD -> "🔮"
        FiscalPeriodStatusType.SCHEDULED_OPEN -> "⏰"
        FiscalPeriodStatusType.SCHEDULED_CLOSE -> "⏲️"
    }
    
    /**
     * Gets a comprehensive status summary
     */
    fun getSummary(): String = buildString {
        append("${getStatusIcon()} $name")
        
        if (!isActive) append(" [INACTIVE]")
        if (isFinal) append(" [FINAL]")
        if (requiresImmediateAttention) append(" [URGENT]")
        
        reason?.let { r ->
            append(" - $r")
        }
        
        if (hasApproachingDeadlines) {
            daysUntilNextDeadline?.let { days ->
                append(" (Deadline in $days days)")
            }
        }
        
        if (hasMissedDeadlines) {
            append(" [DEADLINE MISSED]")
        }
        
        if (escalationRequired) {
            append(" [ESCALATION REQUIRED]")
        }
    }
    
    // ==================== VALIDATION ====================
    
    init {
        require(name.isNotBlank()) { "Fiscal period status name cannot be blank" }
        require(name.length <= 100) { "Status name cannot exceed 100 characters" }
        
        description?.let { desc ->
            require(desc.length <= 500) { "Status description cannot exceed 500 characters" }
        }
        
        reason?.let { r ->
            require(r.length <= 1000) { "Status reason cannot exceed 1000 characters" }
        }
        
        statusChangedBy?.let { changedBy ->
            require(changedBy.isNotBlank()) { "Status changed by cannot be blank if provided" }
        }
        
        // Validate deadline sequence
        if (softCloseDeadline != null && hardCloseDeadline != null) {
            require(!hardCloseDeadline.isBefore(softCloseDeadline)) {
                "Hard close deadline cannot be before soft close deadline"
            }
        }
        
        if (hardCloseDeadline != null && finalReportingDeadline != null) {
            require(!finalReportingDeadline.isBefore(hardCloseDeadline)) {
                "Final reporting deadline cannot be before hard close deadline"
            }
        }
        
        autoTransitionDate?.let { transitionDate ->
            require(autoTransitionEnabled) {
                "Auto transition must be enabled if transition date is provided"
            }
        }
    }
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension functions for FiscalPeriodStatusType enum
 */
fun FiscalPeriodStatusType.getDisplayName(): String = when (this) {
    FiscalPeriodStatusType.PLANNING -> "Planning"
    FiscalPeriodStatusType.DRAFT -> "Draft"
    FiscalPeriodStatusType.PENDING_APPROVAL -> "Pending Approval"
    FiscalPeriodStatusType.APPROVED -> "Approved"
    FiscalPeriodStatusType.OPEN -> "Open"
    FiscalPeriodStatusType.CURRENT -> "Current"
    FiscalPeriodStatusType.ACTIVE -> "Active"
    FiscalPeriodStatusType.SOFT_CLOSE -> "Soft Close"
    FiscalPeriodStatusType.HARD_CLOSE -> "Hard Close"
    FiscalPeriodStatusType.CLOSING_IN_PROGRESS -> "Closing in Progress"
    FiscalPeriodStatusType.PRELIMINARY_CLOSE -> "Preliminary Close"
    FiscalPeriodStatusType.CLOSED -> "Closed"
    FiscalPeriodStatusType.PERMANENTLY_CLOSED -> "Permanently Closed"
    FiscalPeriodStatusType.ARCHIVED -> "Archived"
    FiscalPeriodStatusType.ADJUSTMENT_PERIOD -> "Adjustment Period"
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS -> "Year-End Adjustments"
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS -> "Audit Adjustments"
    FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS -> "Prior Period Adjustments"
    FiscalPeriodStatusType.SUSPENDED -> "Suspended"
    FiscalPeriodStatusType.LOCKED -> "Locked"
    FiscalPeriodStatusType.FROZEN -> "Frozen"
    FiscalPeriodStatusType.UNDER_REVIEW -> "Under Review"
    FiscalPeriodStatusType.ERROR_STATE -> "Error State"
    FiscalPeriodStatusType.RECONCILIATION_REQUIRED -> "Reconciliation Required"
    FiscalPeriodStatusType.VALIDATION_FAILED -> "Validation Failed"
    FiscalPeriodStatusType.DATA_INCONSISTENCY -> "Data Inconsistency"
    FiscalPeriodStatusType.MAINTENANCE_MODE -> "Maintenance Mode"
    FiscalPeriodStatusType.ROLLBACK_IN_PROGRESS -> "Rollback in Progress"
    FiscalPeriodStatusType.BACKUP_RESTORE -> "Backup/Restore"
    FiscalPeriodStatusType.FUTURE_PERIOD -> "Future Period"
    FiscalPeriodStatusType.SCHEDULED_OPEN -> "Scheduled to Open"
    FiscalPeriodStatusType.SCHEDULED_CLOSE -> "Scheduled to Close"
}

fun FiscalPeriodStatusType.isActiveStatus(): Boolean = when (this) {
    FiscalPeriodStatusType.OPEN,
    FiscalPeriodStatusType.CURRENT,
    FiscalPeriodStatusType.ACTIVE,
    FiscalPeriodStatusType.ADJUSTMENT_PERIOD,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS,
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS,
    FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.allowsEditing(): Boolean = when (this) {
    FiscalPeriodStatusType.PLANNING,
    FiscalPeriodStatusType.DRAFT,
    FiscalPeriodStatusType.OPEN,
    FiscalPeriodStatusType.ACTIVE,
    FiscalPeriodStatusType.ADJUSTMENT_PERIOD,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS,
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS,
    FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.allowsNewTransactions(): Boolean = when (this) {
    FiscalPeriodStatusType.OPEN,
    FiscalPeriodStatusType.CURRENT,
    FiscalPeriodStatusType.ACTIVE,
    FiscalPeriodStatusType.ADJUSTMENT_PERIOD,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS,
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS,
    FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.allowsAdjustments(): Boolean = when (this) {
    FiscalPeriodStatusType.OPEN,
    FiscalPeriodStatusType.CURRENT,
    FiscalPeriodStatusType.ACTIVE,
    FiscalPeriodStatusType.SOFT_CLOSE,
    FiscalPeriodStatusType.ADJUSTMENT_PERIOD,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS,
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS,
    FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.allowsJournalEntries(): Boolean = allowsNewTransactions()

fun FiscalPeriodStatusType.allowsReversals(): Boolean = when (this) {
    FiscalPeriodStatusType.OPEN,
    FiscalPeriodStatusType.CURRENT,
    FiscalPeriodStatusType.ACTIVE,
    FiscalPeriodStatusType.SOFT_CLOSE,
    FiscalPeriodStatusType.ADJUSTMENT_PERIOD -> true
    else -> false
}

fun FiscalPeriodStatusType.allowsReclassifications(): Boolean = allowsAdjustments()

fun FiscalPeriodStatusType.allowsAccruals(): Boolean = allowsNewTransactions()

fun FiscalPeriodStatusType.isAdjustmentOnlyStatus(): Boolean = when (this) {
    FiscalPeriodStatusType.ADJUSTMENT_PERIOD,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS,
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS,
    FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.requiresApproval(): Boolean = when (this) {
    FiscalPeriodStatusType.PENDING_APPROVAL,
    FiscalPeriodStatusType.CLOSING_IN_PROGRESS,
    FiscalPeriodStatusType.PRELIMINARY_CLOSE,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS,
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.requiresApprovalSignature(): Boolean = when (this) {
    FiscalPeriodStatusType.CLOSED,
    FiscalPeriodStatusType.PERMANENTLY_CLOSED,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.requiresComplianceValidation(): Boolean = when (this) {
    FiscalPeriodStatusType.CLOSED,
    FiscalPeriodStatusType.PERMANENTLY_CLOSED,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS,
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.isFinalStatus(): Boolean = when (this) {
    FiscalPeriodStatusType.PERMANENTLY_CLOSED,
    FiscalPeriodStatusType.ARCHIVED -> true
    else -> false
}

fun FiscalPeriodStatusType.hasDeadlines(): Boolean = when (this) {
    FiscalPeriodStatusType.SOFT_CLOSE,
    FiscalPeriodStatusType.HARD_CLOSE,
    FiscalPeriodStatusType.CLOSING_IN_PROGRESS,
    FiscalPeriodStatusType.PRELIMINARY_CLOSE,
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS,
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS -> true
    else -> false
}

fun FiscalPeriodStatusType.requiresStakeholderNotification(): Boolean = when (this) {
    FiscalPeriodStatusType.OPEN,
    FiscalPeriodStatusType.CURRENT,
    FiscalPeriodStatusType.SOFT_CLOSE,
    FiscalPeriodStatusType.HARD_CLOSE,
    FiscalPeriodStatusType.CLOSED,
    FiscalPeriodStatusType.ERROR_STATE,
    FiscalPeriodStatusType.SUSPENDED -> true
    else -> false
}

fun FiscalPeriodStatusType.supportsAutomatedProcessing(): Boolean = when (this) {
    FiscalPeriodStatusType.OPEN,
    FiscalPeriodStatusType.CURRENT,
    FiscalPeriodStatusType.ACTIVE,
    FiscalPeriodStatusType.SCHEDULED_OPEN,
    FiscalPeriodStatusType.SCHEDULED_CLOSE -> true
    else -> false
}

fun FiscalPeriodStatusType.getDefaultPriority(): FiscalPeriodPriority = when (this) {
    FiscalPeriodStatusType.ERROR_STATE,
    FiscalPeriodStatusType.VALIDATION_FAILED,
    FiscalPeriodStatusType.DATA_INCONSISTENCY -> FiscalPeriodPriority.CRITICAL
    
    FiscalPeriodStatusType.CURRENT,
    FiscalPeriodStatusType.RECONCILIATION_REQUIRED,
    FiscalPeriodStatusType.SUSPENDED,
    FiscalPeriodStatusType.CLOSING_IN_PROGRESS -> FiscalPeriodPriority.HIGH
    
    FiscalPeriodStatusType.SOFT_CLOSE,
    FiscalPeriodStatusType.HARD_CLOSE,
    FiscalPeriodStatusType.UNDER_REVIEW,
    FiscalPeriodStatusType.PENDING_APPROVAL -> FiscalPeriodPriority.NORMAL
    
    else -> FiscalPeriodPriority.LOW
}

fun FiscalPeriodStatusType.getNextPossibleStatuses(): Set<FiscalPeriodStatusType> = when (this) {
    FiscalPeriodStatusType.PLANNING -> setOf(
        FiscalPeriodStatusType.DRAFT,
        FiscalPeriodStatusType.PENDING_APPROVAL
    )
    FiscalPeriodStatusType.DRAFT -> setOf(
        FiscalPeriodStatusType.PLANNING,
        FiscalPeriodStatusType.PENDING_APPROVAL,
        FiscalPeriodStatusType.OPEN
    )
    FiscalPeriodStatusType.PENDING_APPROVAL -> setOf(
        FiscalPeriodStatusType.APPROVED,
        FiscalPeriodStatusType.DRAFT
    )
    FiscalPeriodStatusType.APPROVED -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.SCHEDULED_OPEN
    )
    FiscalPeriodStatusType.OPEN -> setOf(
        FiscalPeriodStatusType.CURRENT,
        FiscalPeriodStatusType.ACTIVE,
        FiscalPeriodStatusType.SOFT_CLOSE,
        FiscalPeriodStatusType.SUSPENDED,
        FiscalPeriodStatusType.LOCKED
    )
    FiscalPeriodStatusType.CURRENT -> setOf(
        FiscalPeriodStatusType.ACTIVE,
        FiscalPeriodStatusType.SOFT_CLOSE,
        FiscalPeriodStatusType.ADJUSTMENT_PERIOD,
        FiscalPeriodStatusType.SUSPENDED
    )
    FiscalPeriodStatusType.ACTIVE -> setOf(
        FiscalPeriodStatusType.CURRENT,
        FiscalPeriodStatusType.SOFT_CLOSE,
        FiscalPeriodStatusType.ADJUSTMENT_PERIOD,
        FiscalPeriodStatusType.SUSPENDED
    )
    FiscalPeriodStatusType.SOFT_CLOSE -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.HARD_CLOSE,
        FiscalPeriodStatusType.CLOSING_IN_PROGRESS,
        FiscalPeriodStatusType.ADJUSTMENT_PERIOD
    )
    FiscalPeriodStatusType.HARD_CLOSE -> setOf(
        FiscalPeriodStatusType.SOFT_CLOSE,
        FiscalPeriodStatusType.CLOSED,
        FiscalPeriodStatusType.ADJUSTMENT_PERIOD
    )
    FiscalPeriodStatusType.CLOSING_IN_PROGRESS -> setOf(
        FiscalPeriodStatusType.SOFT_CLOSE,
        FiscalPeriodStatusType.PRELIMINARY_CLOSE,
        FiscalPeriodStatusType.CLOSED
    )
    FiscalPeriodStatusType.PRELIMINARY_CLOSE -> setOf(
        FiscalPeriodStatusType.SOFT_CLOSE,
        FiscalPeriodStatusType.CLOSED,
        FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS
    )
    FiscalPeriodStatusType.CLOSED -> setOf(
        FiscalPeriodStatusType.PERMANENTLY_CLOSED,
        FiscalPeriodStatusType.ARCHIVED,
        FiscalPeriodStatusType.AUDIT_ADJUSTMENTS,
        FiscalPeriodStatusType.PRIOR_PERIOD_ADJUSTMENTS
    )
    FiscalPeriodStatusType.ADJUSTMENT_PERIOD -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.SOFT_CLOSE,
        FiscalPeriodStatusType.CLOSED
    )
    FiscalPeriodStatusType.YEAR_END_ADJUSTMENTS -> setOf(
        FiscalPeriodStatusType.CLOSED,
        FiscalPeriodStatusType.PRELIMINARY_CLOSE
    )
    FiscalPeriodStatusType.AUDIT_ADJUSTMENTS -> setOf(
        FiscalPeriodStatusType.CLOSED,
        FiscalPeriodStatusType.UNDER_REVIEW
    )
    FiscalPeriodStatusType.SUSPENDED -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.ACTIVE,
        FiscalPeriodStatusType.LOCKED,
        FiscalPeriodStatusType.ERROR_STATE
    )
    FiscalPeriodStatusType.LOCKED -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.SUSPENDED,
        FiscalPeriodStatusType.FROZEN
    )
    FiscalPeriodStatusType.FROZEN -> setOf(
        FiscalPeriodStatusType.LOCKED,
        FiscalPeriodStatusType.MAINTENANCE_MODE
    )
    FiscalPeriodStatusType.UNDER_REVIEW -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.CLOSED,
        FiscalPeriodStatusType.ERROR_STATE
    )
    FiscalPeriodStatusType.ERROR_STATE -> setOf(
        FiscalPeriodStatusType.MAINTENANCE_MODE,
        FiscalPeriodStatusType.ROLLBACK_IN_PROGRESS,
        FiscalPeriodStatusType.SUSPENDED
    )
    FiscalPeriodStatusType.RECONCILIATION_REQUIRED -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.UNDER_REVIEW,
        FiscalPeriodStatusType.ERROR_STATE
    )
    FiscalPeriodStatusType.VALIDATION_FAILED -> setOf(
        FiscalPeriodStatusType.DRAFT,
        FiscalPeriodStatusType.ERROR_STATE,
        FiscalPeriodStatusType.MAINTENANCE_MODE
    )
    FiscalPeriodStatusType.DATA_INCONSISTENCY -> setOf(
        FiscalPeriodStatusType.RECONCILIATION_REQUIRED,
        FiscalPeriodStatusType.ERROR_STATE,
        FiscalPeriodStatusType.MAINTENANCE_MODE
    )
    FiscalPeriodStatusType.MAINTENANCE_MODE -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.SUSPENDED,
        FiscalPeriodStatusType.BACKUP_RESTORE
    )
    FiscalPeriodStatusType.ROLLBACK_IN_PROGRESS -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.ERROR_STATE,
        FiscalPeriodStatusType.MAINTENANCE_MODE
    )
    FiscalPeriodStatusType.BACKUP_RESTORE -> setOf(
        FiscalPeriodStatusType.MAINTENANCE_MODE,
        FiscalPeriodStatusType.OPEN
    )
    FiscalPeriodStatusType.FUTURE_PERIOD -> setOf(
        FiscalPeriodStatusType.SCHEDULED_OPEN,
        FiscalPeriodStatusType.PLANNING
    )
    FiscalPeriodStatusType.SCHEDULED_OPEN -> setOf(
        FiscalPeriodStatusType.OPEN,
        FiscalPeriodStatusType.FUTURE_PERIOD
    )
    FiscalPeriodStatusType.SCHEDULED_CLOSE -> setOf(
        FiscalPeriodStatusType.SOFT_CLOSE,
        FiscalPeriodStatusType.ACTIVE
    )
    else -> emptySet() // Final statuses have no transitions
}

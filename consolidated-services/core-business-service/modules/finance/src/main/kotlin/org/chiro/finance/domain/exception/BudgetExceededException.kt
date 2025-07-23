package org.chiro.finance.domain.exception

import java.math.BigDecimal
import java.util.*

/**
 * BudgetExceededException
 * 
 * Domain exception thrown when a financial transaction or allocation
 * would exceed established budget limits within the finance domain.
 * 
 * This exception is thrown when:
 * - Transaction amount exceeds available budget allocation
 * - Cumulative spending exceeds period budget limits
 * - Budget variance thresholds are breached
 * - Emergency spending requires approval but none granted
 * - Multi-dimensional budget limits are violated (by category, department, project)
 * - Budget revision is required but not authorized
 * - Forecast spending indicates future budget exhaustion
 */
class BudgetExceededException : FinanceDomainException {
    
    val budgetId: UUID
    val budgetCategory: String
    val budgetPeriod: String
    val requestedAmount: BigDecimal
    val availableAmount: BigDecimal
    val exceedingAmount: BigDecimal
    val totalBudgetLimit: BigDecimal
    val currentSpent: BigDecimal
    val budgetType: BudgetType
    val exceedanceType: BudgetExceedanceType
    val departmentId: UUID?
    val projectId: UUID?
    val transactionId: UUID?
    val entityId: UUID?
    val approvalRequired: Boolean
    val overrideAuthorization: String?
    val budgetOwner: String?
    val forecastImpact: BigDecimal?
    val warningThreshold: BigDecimal?
    val criticalThreshold: BigDecimal?
    
    constructor(
        budgetId: UUID,
        budgetCategory: String,
        requestedAmount: BigDecimal,
        availableAmount: BigDecimal,
        budgetType: BudgetType,
        exceedanceType: BudgetExceedanceType,
        message: String = "Budget exceeded for category '$budgetCategory'"
    ) : super(message) {
        this.budgetId = budgetId
        this.budgetCategory = budgetCategory
        this.budgetPeriod = "CURRENT"
        this.requestedAmount = requestedAmount
        this.availableAmount = availableAmount
        this.exceedingAmount = requestedAmount - availableAmount
        this.totalBudgetLimit = availableAmount
        this.currentSpent = BigDecimal.ZERO
        this.budgetType = budgetType
        this.exceedanceType = exceedanceType
        this.departmentId = null
        this.projectId = null
        this.transactionId = null
        this.entityId = null
        this.approvalRequired = false
        this.overrideAuthorization = null
        this.budgetOwner = null
        this.forecastImpact = null
        this.warningThreshold = null
        this.criticalThreshold = null
    }
    
    constructor(
        budgetId: UUID,
        budgetCategory: String,
        budgetPeriod: String,
        requestedAmount: BigDecimal,
        totalBudgetLimit: BigDecimal,
        currentSpent: BigDecimal,
        budgetType: BudgetType,
        exceedanceType: BudgetExceedanceType,
        departmentId: UUID?,
        projectId: UUID?,
        transactionId: UUID,
        entityId: UUID,
        approvalRequired: Boolean,
        budgetOwner: String,
        warningThreshold: BigDecimal?,
        criticalThreshold: BigDecimal?,
        message: String
    ) : super(message) {
        this.budgetId = budgetId
        this.budgetCategory = budgetCategory
        this.budgetPeriod = budgetPeriod
        this.requestedAmount = requestedAmount
        this.availableAmount = totalBudgetLimit - currentSpent
        this.exceedingAmount = (currentSpent + requestedAmount) - totalBudgetLimit
        this.totalBudgetLimit = totalBudgetLimit
        this.currentSpent = currentSpent
        this.budgetType = budgetType
        this.exceedanceType = exceedanceType
        this.departmentId = departmentId
        this.projectId = projectId
        this.transactionId = transactionId
        this.entityId = entityId
        this.approvalRequired = approvalRequired
        this.overrideAuthorization = null
        this.budgetOwner = budgetOwner
        this.forecastImpact = null
        this.warningThreshold = warningThreshold
        this.criticalThreshold = criticalThreshold
    }
    
    /**
     * Calculate the percentage of budget that would be exceeded
     */
    fun getExceedancePercentage(): BigDecimal {
        return if (totalBudgetLimit > BigDecimal.ZERO) {
            (exceedingAmount.divide(totalBudgetLimit, 4, BigDecimal.ROUND_HALF_UP))
                .multiply(BigDecimal("100.00"))
        } else {
            BigDecimal.ZERO
        }
    }
    
    /**
     * Calculate current budget utilization percentage
     */
    fun getBudgetUtilizationPercentage(): BigDecimal {
        return if (totalBudgetLimit > BigDecimal.ZERO) {
            (currentSpent.divide(totalBudgetLimit, 4, BigDecimal.ROUND_HALF_UP))
                .multiply(BigDecimal("100.00"))
        } else {
            BigDecimal.ZERO
        }
    }
    
    /**
     * Check if the exceedance is within warning threshold
     */
    fun isWithinWarningThreshold(): Boolean {
        return warningThreshold?.let { threshold ->
            getExceedancePercentage() <= threshold
        } ?: false
    }
    
    /**
     * Check if the exceedance is within critical threshold
     */
    fun isWithinCriticalThreshold(): Boolean {
        return criticalThreshold?.let { threshold ->
            getExceedancePercentage() <= threshold
        } ?: true
    }
    
    /**
     * Check if this is a soft budget limit (allows override)
     */
    fun isSoftBudgetLimit(): Boolean {
        return budgetType in setOf(
            BudgetType.FLEXIBLE,
            BudgetType.GUIDANCE,
            BudgetType.FORECAST
        )
    }
    
    /**
     * Check if this is a hard budget limit (strict enforcement)
     */
    fun isHardBudgetLimit(): Boolean {
        return budgetType in setOf(
            BudgetType.FIXED,
            BudgetType.REGULATORY,
            BudgetType.CONTRACTUAL
        )
    }
    
    /**
     * Check if the exceedance requires immediate escalation
     */
    fun requiresImmediateEscalation(): Boolean {
        return when {
            isHardBudgetLimit() && !isWithinWarningThreshold() -> true
            exceedanceType == BudgetExceedanceType.CRITICAL_OVERSPEND -> true
            exceedanceType == BudgetExceedanceType.REGULATORY_BREACH -> true
            !isWithinCriticalThreshold() -> true
            else -> false
        }
    }
    
    /**
     * Get the severity level of this budget exceedance
     */
    fun getBudgetExceedanceSeverity(): BudgetExceedanceSeverity {
        return when {
            exceedanceType == BudgetExceedanceType.REGULATORY_BREACH -> BudgetExceedanceSeverity.CRITICAL
            exceedanceType == BudgetExceedanceType.CRITICAL_OVERSPEND -> BudgetExceedanceSeverity.CRITICAL
            isHardBudgetLimit() && !isWithinCriticalThreshold() -> BudgetExceedanceSeverity.HIGH
            exceedanceType == BudgetExceedanceType.UNAUTHORIZED_SPEND -> BudgetExceedanceSeverity.HIGH
            !isWithinWarningThreshold() -> BudgetExceedanceSeverity.MEDIUM
            isSoftBudgetLimit() -> BudgetExceedanceSeverity.LOW
            else -> BudgetExceedanceSeverity.MEDIUM
        }
    }
    
    /**
     * Get suggested resolution actions
     */
    fun getSuggestedResolutions(): List<String> {
        return when (exceedanceType) {
            BudgetExceedanceType.INSUFFICIENT_ALLOCATION -> listOf(
                "Reduce transaction amount to $availableAmount (available budget)",
                "Request budget reallocation from other categories",
                "Defer transaction to next budget period",
                "Seek budget increase approval from ${budgetOwner ?: "budget owner"}",
                "Split transaction across multiple budget periods"
            )
            BudgetExceedanceType.PERIOD_LIMIT_EXCEEDED -> listOf(
                "Wait for next budget period: $budgetPeriod",
                "Request emergency budget extension",
                "Reallocate funds from underutilized budget categories",
                "Reduce transaction amount to fit within remaining budget: $availableAmount",
                "Seek budget owner approval for period override"
            )
            BudgetExceedanceType.VARIANCE_THRESHOLD_BREACH -> listOf(
                "Review budget variance analysis and justification",
                "Provide business case for budget increase",
                "Implement cost control measures to reduce spending",
                "Seek variance approval from ${budgetOwner ?: "budget committee"}",
                "Revise forecast and adjust future spending plans"
            )
            BudgetExceedanceType.UNAUTHORIZED_SPEND -> listOf(
                "Obtain proper authorization before proceeding",
                "Submit spend request to ${budgetOwner ?: "budget approver"}",
                "Review spending authority and approval matrix",
                "Document business justification for emergency spend",
                "Ensure compliance with spending policies"
            )
            BudgetExceedanceType.CATEGORY_OVERSPEND -> listOf(
                "Reallocate budget from other categories within same department",
                "Reduce spend in over-budget category: $budgetCategory",
                "Seek cross-category budget transfer approval",
                "Review category spending patterns and adjust controls",
                "Implement category-specific spending limits"
            )
            BudgetExceedanceType.DEPARTMENTAL_OVERSPEND -> listOf(
                departmentId?.let { "Review departmental spending controls for department $it" } ?: "Review departmental spending",
                "Seek inter-departmental budget transfer",
                "Implement tighter departmental spending approval process",
                "Escalate to department head for budget revision",
                "Defer non-critical departmental expenses"
            )
            BudgetExceedanceType.PROJECT_OVERSPEND -> listOf(
                projectId?.let { "Review project budget and scope for project $it" } ?: "Review project budget scope",
                "Request project budget increase from project sponsor",
                "Reduce project scope to fit within budget",
                "Seek additional project funding approval",
                "Consider project schedule adjustment to spread costs"
            )
            BudgetExceedanceType.FORECAST_EXHAUSTION -> listOf(
                "Revise spending forecast and adjust future allocations",
                "Implement spending controls to prevent forecast breach",
                "Seek proactive budget increase before exhaustion",
                "Plan budget reallocation from projected surpluses",
                "Review and adjust budget planning methodology"
            )
            BudgetExceedanceType.EMERGENCY_SPEND -> listOf(
                "Document emergency nature and business impact",
                "Seek emergency spend authorization from ${budgetOwner ?: "authorized approver"}",
                "Plan budget amendment to cover emergency expenditure",
                "Implement post-emergency budget review process",
                "Consider contingency budget for future emergencies"
            )
            BudgetExceedanceType.REGULATORY_BREACH -> listOf(
                "IMMEDIATE: Stop transaction to prevent regulatory violation",
                "Consult with compliance team before proceeding",
                "Review regulatory budget requirements and limits",
                "Seek regulatory guidance on budget compliance",
                "Document regulatory impact and mitigation measures"
            )
            BudgetExceedanceType.CRITICAL_OVERSPEND -> listOf(
                "URGENT: Escalate to senior management immediately",
                "Implement immediate spending freeze for category: $budgetCategory",
                "Conduct emergency budget review and revision",
                "Assess business continuity impact of spending restriction",
                "Prepare executive summary of budget crisis and recovery plan"
            )
        }
    }
    
    /**
     * Calculate recommended budget adjustment
     */
    fun getRecommendedBudgetAdjustment(): BigDecimal {
        return when (exceedanceType) {
            BudgetExceedanceType.FORECAST_EXHAUSTION -> exceedingAmount.multiply(BigDecimal("1.2")) // 20% buffer
            BudgetExceedanceType.VARIANCE_THRESHOLD_BREACH -> exceedingAmount.multiply(BigDecimal("1.1")) // 10% buffer
            else -> exceedingAmount
        }
    }
    
    override fun getErrorCode(): String = "BUDGET_EXCEEDED"
    
    override fun getErrorCategory(): String = "BUDGET_CONTROL_ERROR"
    
    override fun getBusinessImpact(): String = when (getBudgetExceedanceSeverity()) {
        BudgetExceedanceSeverity.CRITICAL -> "CRITICAL - Regulatory or business-critical budget breach, transaction blocked"
        BudgetExceedanceSeverity.HIGH -> "HIGH - Hard budget limit exceeded, authorization required before proceeding"
        BudgetExceedanceSeverity.MEDIUM -> "MEDIUM - Budget variance threshold breached, approval recommended"
        BudgetExceedanceSeverity.LOW -> "LOW - Soft budget limit exceeded, monitoring and reporting required"
    }
    
    override fun getRecommendedAction(): String = when {
        requiresImmediateEscalation() -> "IMMEDIATE ESCALATION: ${getSuggestedResolutions().first()}"
        approvalRequired -> "APPROVAL REQUIRED: ${getSuggestedResolutions().first()}"
        else -> getSuggestedResolutions().first()
    }
    
    override fun isRetryable(): Boolean = when (exceedanceType) {
        BudgetExceedanceType.REGULATORY_BREACH -> false
        BudgetExceedanceType.CRITICAL_OVERSPEND -> false
        else -> isSoftBudgetLimit() || approvalRequired
    }
    
    override fun getRetryDelay(): Long = when {
        exceedanceType == BudgetExceedanceType.PERIOD_LIMIT_EXCEEDED -> 86400000L // 1 day
        approvalRequired -> 3600000L // 1 hour (waiting for approval)
        isSoftBudgetLimit() -> 0L // Immediate retry with proper authorization
        else -> -1L // Not retryable
    }
    
    override fun requiresEscalation(): Boolean = getBudgetExceedanceSeverity() in setOf(
        BudgetExceedanceSeverity.CRITICAL,
        BudgetExceedanceSeverity.HIGH
    ) || requiresImmediateEscalation()
    
    override fun getContextInformation(): Map<String, Any> {
        return super.getContextInformation() + mapOf(
            "budgetId" to budgetId.toString(),
            "budgetCategory" to budgetCategory,
            "budgetPeriod" to budgetPeriod,
            "requestedAmount" to requestedAmount.toString(),
            "availableAmount" to availableAmount.toString(),
            "exceedingAmount" to exceedingAmount.toString(),
            "totalBudgetLimit" to totalBudgetLimit.toString(),
            "currentSpent" to currentSpent.toString(),
            "budgetType" to budgetType.name,
            "exceedanceType" to exceedanceType.name,
            "budgetExceedanceSeverity" to getBudgetExceedanceSeverity().name,
            "exceedancePercentage" to getExceedancePercentage().toString(),
            "budgetUtilizationPercentage" to getBudgetUtilizationPercentage().toString(),
            "isWithinWarningThreshold" to isWithinWarningThreshold(),
            "isWithinCriticalThreshold" to isWithinCriticalThreshold(),
            "isSoftBudgetLimit" to isSoftBudgetLimit(),
            "isHardBudgetLimit" to isHardBudgetLimit(),
            "requiresImmediateEscalation" to requiresImmediateEscalation(),
            "approvalRequired" to approvalRequired,
            "budgetOwner" to (budgetOwner ?: ""),
            "recommendedAdjustment" to getRecommendedBudgetAdjustment().toString(),
            "departmentId" to (departmentId?.toString() ?: ""),
            "projectId" to (projectId?.toString() ?: ""),
            "transactionId" to (transactionId?.toString() ?: ""),
            "entityId" to (entityId?.toString() ?: ""),
            "warningThreshold" to (warningThreshold?.toString() ?: ""),
            "criticalThreshold" to (criticalThreshold?.toString() ?: "")
        )
    }
    
    companion object {
        /**
         * Create exception for insufficient budget allocation
         */
        fun insufficientAllocation(
            budgetId: UUID,
            budgetCategory: String,
            requestedAmount: BigDecimal,
            availableAmount: BigDecimal,
            budgetType: BudgetType = BudgetType.FIXED
        ): BudgetExceededException {
            return BudgetExceededException(
                budgetId = budgetId,
                budgetCategory = budgetCategory,
                requestedAmount = requestedAmount,
                availableAmount = availableAmount,
                budgetType = budgetType,
                exceedanceType = BudgetExceedanceType.INSUFFICIENT_ALLOCATION,
                message = "Insufficient budget allocation for '$budgetCategory': requested $requestedAmount, available $availableAmount"
            )
        }
        
        /**
         * Create exception for period limit exceeded
         */
        fun periodLimitExceeded(
            budgetId: UUID,
            budgetCategory: String,
            budgetPeriod: String,
            requestedAmount: BigDecimal,
            totalBudgetLimit: BigDecimal,
            currentSpent: BigDecimal,
            budgetOwner: String
        ): BudgetExceededException {
            return BudgetExceededException(
                budgetId = budgetId,
                budgetCategory = budgetCategory,
                budgetPeriod = budgetPeriod,
                requestedAmount = requestedAmount,
                totalBudgetLimit = totalBudgetLimit,
                currentSpent = currentSpent,
                budgetType = BudgetType.FIXED,
                exceedanceType = BudgetExceedanceType.PERIOD_LIMIT_EXCEEDED,
                departmentId = null,
                projectId = null,
                transactionId = UUID.randomUUID(), // Would be actual transaction ID
                entityId = UUID.randomUUID(), // Would be actual entity ID
                approvalRequired = true,
                budgetOwner = budgetOwner,
                warningThreshold = BigDecimal("5.00"), // 5%
                criticalThreshold = BigDecimal("10.00"), // 10%
                message = "Budget period limit exceeded for '$budgetCategory' in period '$budgetPeriod': requested $requestedAmount would exceed limit by ${(currentSpent + requestedAmount) - totalBudgetLimit}"
            )
        }
        
        /**
         * Create exception for regulatory budget breach
         */
        fun regulatoryBreach(
            budgetId: UUID,
            budgetCategory: String,
            requestedAmount: BigDecimal,
            regulatoryLimit: BigDecimal,
            currentSpent: BigDecimal,
            transactionId: UUID
        ): BudgetExceededException {
            return BudgetExceededException(
                budgetId = budgetId,
                budgetCategory = budgetCategory,
                budgetPeriod = "REGULATORY_PERIOD",
                requestedAmount = requestedAmount,
                totalBudgetLimit = regulatoryLimit,
                currentSpent = currentSpent,
                budgetType = BudgetType.REGULATORY,
                exceedanceType = BudgetExceedanceType.REGULATORY_BREACH,
                departmentId = null,
                projectId = null,
                transactionId = transactionId,
                entityId = UUID.randomUUID(),
                approvalRequired = false, // Cannot be approved - regulatory limit
                budgetOwner = "REGULATORY_AUTHORITY",
                warningThreshold = BigDecimal.ZERO,
                criticalThreshold = BigDecimal.ZERO,
                message = "REGULATORY BREACH: Transaction would exceed regulatory budget limit of $regulatoryLimit for category '$budgetCategory'"
            )
        }
        
        /**
         * Create exception for project budget overspend
         */
        fun projectOverspend(
            budgetId: UUID,
            budgetCategory: String,
            projectId: UUID,
            requestedAmount: BigDecimal,
            projectBudgetLimit: BigDecimal,
            currentProjectSpent: BigDecimal,
            projectManager: String
        ): BudgetExceededException {
            return BudgetExceededException(
                budgetId = budgetId,
                budgetCategory = budgetCategory,
                budgetPeriod = "PROJECT_LIFECYCLE",
                requestedAmount = requestedAmount,
                totalBudgetLimit = projectBudgetLimit,
                currentSpent = currentProjectSpent,
                budgetType = BudgetType.FLEXIBLE,
                exceedanceType = BudgetExceedanceType.PROJECT_OVERSPEND,
                departmentId = null,
                projectId = projectId,
                transactionId = UUID.randomUUID(),
                entityId = UUID.randomUUID(),
                approvalRequired = true,
                budgetOwner = projectManager,
                warningThreshold = BigDecimal("10.00"), // 10%
                criticalThreshold = BigDecimal("20.00"), // 20%
                message = "Project budget overspend for project $projectId in category '$budgetCategory': requested $requestedAmount would exceed project budget limit of $projectBudgetLimit"
            )
        }
        
        /**
         * Create exception for critical overspend requiring immediate escalation
         */
        fun criticalOverspend(
            budgetId: UUID,
            budgetCategory: String,
            requestedAmount: BigDecimal,
            totalBudgetLimit: BigDecimal,
            currentSpent: BigDecimal,
            transactionId: UUID,
            entityId: UUID
        ): BudgetExceededException {
            return BudgetExceededException(
                budgetId = budgetId,
                budgetCategory = budgetCategory,
                budgetPeriod = "CURRENT_PERIOD",
                requestedAmount = requestedAmount,
                totalBudgetLimit = totalBudgetLimit,
                currentSpent = currentSpent,
                budgetType = BudgetType.FIXED,
                exceedanceType = BudgetExceedanceType.CRITICAL_OVERSPEND,
                departmentId = null,
                projectId = null,
                transactionId = transactionId,
                entityId = entityId,
                approvalRequired = false, // Requires escalation, not just approval
                budgetOwner = "SENIOR_MANAGEMENT",
                warningThreshold = BigDecimal("50.00"), // 50%
                criticalThreshold = BigDecimal("100.00"), // 100%
                message = "CRITICAL BUDGET OVERSPEND: Transaction of $requestedAmount would critically exceed budget limit for '$budgetCategory' - immediate escalation required"
            )
        }
    }
}

/**
 * Budget Types
 */
enum class BudgetType {
    FIXED,
    FLEXIBLE,
    GUIDANCE,
    FORECAST,
    REGULATORY,
    CONTRACTUAL
}

/**
 * Budget Exceedance Types
 */
enum class BudgetExceedanceType {
    INSUFFICIENT_ALLOCATION,
    PERIOD_LIMIT_EXCEEDED,
    VARIANCE_THRESHOLD_BREACH,
    UNAUTHORIZED_SPEND,
    CATEGORY_OVERSPEND,
    DEPARTMENTAL_OVERSPEND,
    PROJECT_OVERSPEND,
    FORECAST_EXHAUSTION,
    EMERGENCY_SPEND,
    REGULATORY_BREACH,
    CRITICAL_OVERSPEND
}

/**
 * Budget Exceedance Severity Levels
 */
enum class BudgetExceedanceSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

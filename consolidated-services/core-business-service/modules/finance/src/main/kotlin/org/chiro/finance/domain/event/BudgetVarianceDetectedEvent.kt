package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * BudgetVarianceDetectedEvent
 * 
 * Domain event published when a significant budget variance is detected
 * during financial monitoring and analysis processes.
 * 
 * This event enables:
 * - Proactive budget management
 * - Financial performance monitoring
 * - Management reporting and alerts
 * - Budget adjustment workflows
 * - Forecasting accuracy improvements
 * - Cost control interventions
 */
data class BudgetVarianceDetectedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Budget ID or Budget Line Item ID
    val budgetId: UUID,
    val budgetName: String,
    val budgetType: BudgetType,
    val budgetCategory: BudgetCategory,
    val budgetPeriodId: UUID,
    val fiscalYear: Int,
    val fiscalPeriod: Int,
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate,
    val accountId: UUID,
    val accountCode: AccountCode,
    val accountName: String,
    val accountType: AccountType,
    val departmentId: UUID?,
    val departmentName: String?,
    val costCenterId: UUID?,
    val costCenterName: String?,
    val projectId: UUID?,
    val projectName: String?,
    val budgetedAmount: FinancialAmount,
    val actualAmount: FinancialAmount,
    val varianceAmount: FinancialAmount,
    val variancePercentage: BigDecimal,
    val varianceType: VarianceType,
    val varianceSeverity: VarianceSeverity,
    val cumulativeBudgetedAmount: FinancialAmount,
    val cumulativeActualAmount: FinancialAmount,
    val cumulativeVarianceAmount: FinancialAmount,
    val cumulativeVariancePercentage: BigDecimal,
    val previousPeriodVariance: FinancialAmount?,
    val trendDirection: TrendDirection,
    val currency: Currency,
    val isRecurringVariance: Boolean,
    val varianceFrequency: VarianceFrequency,
    val rootCauseCategory: RootCauseCategory?,
    val detectionThresholdAmount: FinancialAmount,
    val detectionThresholdPercentage: BigDecimal,
    val detectionRule: String,
    val impactLevel: ImpactLevel,
    val urgencyLevel: UrgencyLevel,
    val recommendedActions: Set<RecommendedAction>,
    val requiresApproval: Boolean,
    val approvalThreshold: FinancialAmount?,
    val budgetManagerId: UUID?,
    val budgetManagerName: String?,
    val departmentManagerId: UUID?,
    val departmentManagerName: String?,
    val escalationRequired: Boolean,
    val escalationLevel: EscalationLevel,
    val notificationsSent: Set<NotificationType>,
    val analysisCompleted: Boolean = false,
    val correctionPlanRequired: Boolean = false,
    val budgetRevisionRequired: Boolean = false,
    val forecastAdjustmentRequired: Boolean = false,
    val detectedBy: String, // System or User
    val detectionSource: DetectionSource,
    val analysisNotes: String?,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Check if variance is materially significant
     */
    fun isMaterialVariance(materialityThreshold: FinancialAmount): Boolean {
        return varianceAmount.abs().isGreaterThan(materialityThreshold)
    }
    
    /**
     * Check if variance is accelerating (getting worse)
     */
    fun isAcceleratingVariance(): Boolean {
        return trendDirection == TrendDirection.WORSENING && isRecurringVariance
    }
    
    /**
     * Check if variance indicates budget planning issues
     */
    fun indicatesBudgetPlanningIssues(): Boolean {
        return variancePercentage.abs() >= BigDecimal("50") && 
               varianceFrequency != VarianceFrequency.RARE
    }
    
    /**
     * Check if variance is consistently unfavorable
     */
    fun isConsistentlyUnfavorable(): Boolean {
        return isRecurringVariance && 
               varianceType == VarianceType.UNFAVORABLE &&
               varianceFrequency == VarianceFrequency.FREQUENT
    }
    
    /**
     * Get variance impact on overall budget
     */
    fun getBudgetImpactScore(): Int {
        var score = 0
        
        // Base score from variance percentage
        score += when {
            variancePercentage.abs() >= BigDecimal("100") -> 50
            variancePercentage.abs() >= BigDecimal("50") -> 40
            variancePercentage.abs() >= BigDecimal("25") -> 30
            variancePercentage.abs() >= BigDecimal("10") -> 20
            else -> 10
        }
        
        // Add impact from severity
        score += when (varianceSeverity) {
            VarianceSeverity.CRITICAL -> 30
            VarianceSeverity.HIGH -> 20
            VarianceSeverity.MEDIUM -> 10
            VarianceSeverity.LOW -> 5
        }
        
        // Add impact from frequency
        if (isRecurringVariance) score += 10
        if (varianceFrequency == VarianceFrequency.FREQUENT) score += 10
        
        // Add impact from trend
        if (trendDirection == TrendDirection.WORSENING) score += 15
        
        return minOf(100, score)
    }
    
    /**
     * Check if immediate corrective action is required
     */
    fun requiresImmediateAction(): Boolean {
        return varianceSeverity == VarianceSeverity.CRITICAL ||
               urgencyLevel == UrgencyLevel.CRITICAL ||
               escalationRequired
    }
    
    /**
     * Get projected end-of-period variance
     */
    fun getProjectedEndOfPeriodVariance(): FinancialAmount {
        // Simple projection based on current trend
        val remainingDays = periodEndDate.toEpochDay() - LocalDate.now().toEpochDay()
        val totalDays = periodEndDate.toEpochDay() - periodStartDate.toEpochDay()
        
        if (remainingDays <= 0 || totalDays <= 0) {
            return varianceAmount
        }
        
        val progressRatio = BigDecimal.valueOf(totalDays - remainingDays).divide(BigDecimal.valueOf(totalDays), 4, java.math.RoundingMode.HALF_UP)
        val projectedTotalVariance = varianceAmount.amount.divide(progressRatio, 2, java.math.RoundingMode.HALF_UP)
        
        return FinancialAmount(projectedTotalVariance, currency)
    }
    
    /**
     * Check if variance affects key performance indicators
     */
    fun affectsKPIs(): Boolean {
        return budgetCategory in setOf(
            BudgetCategory.REVENUE,
            BudgetCategory.GROSS_PROFIT,
            BudgetCategory.OPERATING_EXPENSES,
            BudgetCategory.NET_INCOME
        ) && varianceSeverity != VarianceSeverity.LOW
    }
    
    /**
     * Get variance efficiency ratio (actual vs budgeted efficiency)
     */
    fun getVarianceEfficiencyRatio(): BigDecimal {
        return if (budgetedAmount.isPositive()) {
            actualAmount.amount.divide(budgetedAmount.amount, 4, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ONE
        }
    }
    
    companion object {
        /**
         * Create event for expense variance
         */
        fun forExpenseVariance(
            budgetId: UUID,
            budgetName: String,
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            budgetedAmount: FinancialAmount,
            actualAmount: FinancialAmount,
            fiscalYear: Int,
            fiscalPeriod: Int,
            departmentId: UUID?,
            departmentName: String?,
            detectionThresholdPercentage: BigDecimal
        ): BudgetVarianceDetectedEvent {
            
            val variance = actualAmount.subtract(budgetedAmount)
            val variancePercent = if (budgetedAmount.isPositive()) {
                variance.amount.divide(budgetedAmount.amount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
            } else {
                BigDecimal.ZERO
            }
            
            val varianceType = if (variance.isPositive()) VarianceType.UNFAVORABLE else VarianceType.FAVORABLE
            val severity = determineSeverity(variancePercent.abs())
            val impact = determineImpact(variance.abs(), variancePercent.abs())
            val urgency = determineUrgency(severity, varianceType)
            
            return BudgetVarianceDetectedEvent(
                aggregateId = budgetId,
                budgetId = budgetId,
                budgetName = budgetName,
                budgetType = BudgetType.OPERATING,
                budgetCategory = BudgetCategory.OPERATING_EXPENSES,
                budgetPeriodId = UUID.randomUUID(),
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                periodStartDate = LocalDate.now().withDayOfMonth(1),
                periodEndDate = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1),
                accountId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                accountType = AccountType.EXPENSE,
                departmentId = departmentId,
                departmentName = departmentName,
                budgetedAmount = budgetedAmount,
                actualAmount = actualAmount,
                varianceAmount = variance,
                variancePercentage = variancePercent,
                varianceType = varianceType,
                varianceSeverity = severity,
                cumulativeBudgetedAmount = budgetedAmount,
                cumulativeActualAmount = actualAmount,
                cumulativeVarianceAmount = variance,
                cumulativeVariancePercentage = variancePercent,
                trendDirection = TrendDirection.STABLE,
                currency = budgetedAmount.currency,
                isRecurringVariance = false,
                varianceFrequency = VarianceFrequency.RARE,
                detectionThresholdAmount = budgetedAmount.multiply(detectionThresholdPercentage.divide(BigDecimal("100"))),
                detectionThresholdPercentage = detectionThresholdPercentage,
                detectionRule = "Expense variance > ${detectionThresholdPercentage}%",
                impactLevel = impact,
                urgencyLevel = urgency,
                recommendedActions = determineRecommendedActions(severity, varianceType),
                requiresApproval = severity != VarianceSeverity.LOW,
                escalationRequired = severity == VarianceSeverity.CRITICAL,
                escalationLevel = if (severity == VarianceSeverity.CRITICAL) EscalationLevel.MANAGER else EscalationLevel.NONE,
                notificationsSent = emptySet(),
                detectedBy = "SYSTEM",
                detectionSource = DetectionSource.AUTOMATED_MONITORING
            )
        }
        
        private fun determineSeverity(variancePercentage: BigDecimal): VarianceSeverity {
            return when {
                variancePercentage >= BigDecimal("50") -> VarianceSeverity.CRITICAL
                variancePercentage >= BigDecimal("25") -> VarianceSeverity.HIGH
                variancePercentage >= BigDecimal("10") -> VarianceSeverity.MEDIUM
                else -> VarianceSeverity.LOW
            }
        }
        
        private fun determineImpact(varianceAmount: FinancialAmount, variancePercentage: BigDecimal): ImpactLevel {
            return when {
                varianceAmount.amount >= BigDecimal("100000") || variancePercentage >= BigDecimal("50") -> ImpactLevel.HIGH
                varianceAmount.amount >= BigDecimal("50000") || variancePercentage >= BigDecimal("25") -> ImpactLevel.MEDIUM
                else -> ImpactLevel.LOW
            }
        }
        
        private fun determineUrgency(severity: VarianceSeverity, varianceType: VarianceType): UrgencyLevel {
            return when {
                severity == VarianceSeverity.CRITICAL -> UrgencyLevel.CRITICAL
                severity == VarianceSeverity.HIGH && varianceType == VarianceType.UNFAVORABLE -> UrgencyLevel.HIGH
                severity == VarianceSeverity.MEDIUM -> UrgencyLevel.MEDIUM
                else -> UrgencyLevel.LOW
            }
        }
        
        private fun determineRecommendedActions(severity: VarianceSeverity, varianceType: VarianceType): Set<RecommendedAction> {
            return when (severity) {
                VarianceSeverity.CRITICAL -> setOf(
                    RecommendedAction.IMMEDIATE_INVESTIGATION,
                    RecommendedAction.BUDGET_REVISION,
                    RecommendedAction.MANAGEMENT_REVIEW,
                    RecommendedAction.CORRECTIVE_ACTION_PLAN
                )
                VarianceSeverity.HIGH -> setOf(
                    RecommendedAction.INVESTIGATE_VARIANCE,
                    RecommendedAction.REVIEW_FORECAST,
                    RecommendedAction.MANAGEMENT_NOTIFICATION
                )
                VarianceSeverity.MEDIUM -> setOf(
                    RecommendedAction.MONITOR_TREND,
                    RecommendedAction.REVIEW_FORECAST
                )
                VarianceSeverity.LOW -> setOf(
                    RecommendedAction.CONTINUE_MONITORING
                )
            }
        }
    }
}

/**
 * Budget Type Classifications
 */
enum class BudgetType {
    OPERATING,
    CAPITAL,
    CASH_FLOW,
    PROJECT,
    DEPARTMENT,
    MASTER
}

/**
 * Budget Category Classifications
 */
enum class BudgetCategory {
    REVENUE,
    COST_OF_GOODS_SOLD,
    GROSS_PROFIT,
    OPERATING_EXPENSES,
    EBITDA,
    DEPRECIATION,
    INTEREST,
    TAXES,
    NET_INCOME,
    CAPITAL_EXPENDITURES,
    WORKING_CAPITAL
}

/**
 * Variance Type Classifications
 */
enum class VarianceType {
    FAVORABLE,
    UNFAVORABLE,
    NEUTRAL
}

/**
 * Variance Severity Levels
 */
enum class VarianceSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Trend Direction Indicators
 */
enum class TrendDirection {
    IMPROVING,
    STABLE,
    WORSENING,
    VOLATILE
}

/**
 * Variance Frequency Patterns
 */
enum class VarianceFrequency {
    RARE,
    OCCASIONAL,
    FREQUENT,
    CHRONIC
}

/**
 * Root Cause Categories
 */
enum class RootCauseCategory {
    POOR_PLANNING,
    MARKET_CONDITIONS,
    OPERATIONAL_INEFFICIENCY,
    COST_OVERRUNS,
    REVENUE_SHORTFALL,
    TIMING_DIFFERENCES,
    ACCOUNTING_ADJUSTMENTS,
    EXTERNAL_FACTORS,
    MANAGEMENT_DECISIONS
}

/**
 * Impact Level Classifications
 */
enum class ImpactLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Recommended Actions
 */
enum class RecommendedAction {
    CONTINUE_MONITORING,
    MONITOR_TREND,
    INVESTIGATE_VARIANCE,
    IMMEDIATE_INVESTIGATION,
    REVIEW_FORECAST,
    BUDGET_REVISION,
    CORRECTIVE_ACTION_PLAN,
    MANAGEMENT_NOTIFICATION,
    MANAGEMENT_REVIEW,
    COST_REDUCTION_INITIATIVE,
    REVENUE_ENHANCEMENT,
    PROCESS_IMPROVEMENT
}

/**
 * Detection Source Types
 */
enum class DetectionSource {
    AUTOMATED_MONITORING,
    MANUAL_REVIEW,
    SCHEDULED_ANALYSIS,
    REAL_TIME_ALERT,
    PERIOD_END_REVIEW
}

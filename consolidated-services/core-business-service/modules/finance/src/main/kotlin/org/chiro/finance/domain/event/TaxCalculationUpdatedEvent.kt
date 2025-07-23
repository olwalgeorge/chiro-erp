package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * TaxCalculationUpdatedEvent
 * 
 * Domain event published when tax calculations are updated or recalculated
 * for transactions, invoices, or tax periods. This event triggers tax
 * compliance workflows and integrates with tax reporting systems.
 * 
 * This event enables:
 * - Real-time tax compliance monitoring
 * - Tax liability tracking and reporting
 * - Audit trail for tax calculations
 * - Multi-jurisdiction tax management
 * - Tax authority reporting automation
 * - Variance analysis for tax planning
 */
data class TaxCalculationUpdatedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Tax Calculation ID
    val taxCalculationId: UUID,
    val calculationNumber: String,
    val calculationType: TaxCalculationType,
    val calculationTrigger: TaxCalculationTrigger,
    val calculationStatus: TaxCalculationStatus,
    val entityId: UUID, // Company/Entity being taxed
    val entityName: String,
    val entityTaxId: String,
    val transactionId: UUID?,
    val transactionType: TransactionType?,
    val invoiceId: UUID?,
    val invoiceNumber: String?,
    val customerId: UUID?,
    val customerName: String?,
    val vendorId: UUID?,
    val vendorName: String?,
    val taxPeriodId: UUID,
    val taxPeriod: TaxPeriod,
    val taxYear: Int,
    val calculationDate: LocalDate,
    val effectiveDate: LocalDate,
    val transactionDate: LocalDate?,
    val currency: Currency,
    val baseCurrency: Currency,
    val exchangeRate: BigDecimal?,
    val grossAmount: FinancialAmount,
    val taxableAmount: FinancialAmount,
    val exemptAmount: FinancialAmount,
    val totalTaxAmount: FinancialAmount,
    val previousTaxAmount: FinancialAmount?,
    val taxAdjustmentAmount: FinancialAmount,
    val netAmount: FinancialAmount,
    val taxJurisdictions: Set<TaxJurisdiction>,
    val taxRates: Map<TaxType, BigDecimal>,
    val taxAmountsByType: Map<TaxType, FinancialAmount>,
    val taxAmountsByJurisdiction: Map<TaxJurisdiction, FinancialAmount>,
    val exemptionReasons: Map<TaxType, ExemptionReason>,
    val taxCertificates: Set<TaxCertificateReference>,
    val isReverseTax: Boolean = false,
    val isWithholdingTax: Boolean = false,
    val withholdingRate: BigDecimal?,
    val withholdingAmount: FinancialAmount?,
    val taxPointDate: LocalDate?, // Date when tax becomes due
    val dueDates: Map<TaxType, LocalDate>,
    val complianceRequirements: Set<ComplianceRequirement>,
    val riskFactors: Set<TaxRiskFactor>,
    val auditFlags: Set<TaxAuditFlag>,
    val calculationMethod: TaxCalculationMethod,
    val calculationEngine: String, // Tax engine used
    val engineVersion: String,
    val calculationRules: Set<String>,
    val overrideApplied: Boolean = false,
    val overrideReason: String?,
    val overrideApprovedBy: UUID?,
    val manualAdjustments: List<TaxAdjustment>,
    val confidenceLevel: TaxConfidenceLevel,
    val uncertaintyAmount: FinancialAmount?,
    val reserveAmount: FinancialAmount?,
    val calculatedBy: UUID?,
    val calculatedByName: String?,
    val reviewedBy: UUID?,
    val reviewedByName: String?,
    val approvedBy: UUID?,
    val approvedByName: String?,
    val reviewDate: LocalDateTime?,
    val approvalDate: LocalDateTime?,
    val requiresReview: Boolean,
    val requiresApproval: Boolean,
    val nextReviewDate: LocalDate?,
    val reportingRequirements: Set<ReportingRequirement>,
    val submissionDeadlines: Map<TaxAuthority, LocalDate>,
    val paymentDeadlines: Map<TaxType, LocalDate>,
    val auditTrailId: UUID,
    val workflowInstanceId: UUID?,
    val integrationSource: String?,
    val sourceSystemId: String?,
    val validationErrors: List<ValidationError>,
    val warnings: List<TaxWarning>,
    val notes: String?,
    val tags: Set<String> = emptySet(),
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Check if tax calculation has significant impact
     */
    fun hasSignificantImpact(threshold: FinancialAmount): Boolean {
        return totalTaxAmount.abs().isGreaterThan(threshold) ||
               taxAdjustmentAmount.abs().isGreaterThan(threshold)
    }
    
    /**
     * Check if calculation involves multiple jurisdictions
     */
    fun isMultiJurisdictional(): Boolean {
        return taxJurisdictions.size > 1
    }
    
    /**
     * Check if calculation has high risk indicators
     */
    fun hasHighRiskIndicators(): Boolean {
        return riskFactors.any { it in setOf(
            TaxRiskFactor.HIGH_VALUE_TRANSACTION,
            TaxRiskFactor.CROSS_BORDER,
            TaxRiskFactor.COMPLEX_STRUCTURE,
            TaxRiskFactor.FREQUENT_ADJUSTMENTS
        ) }
    }
    
    /**
     * Get effective tax rate percentage
     */
    fun getEffectiveTaxRate(): BigDecimal {
        return if (taxableAmount.isPositive()) {
            totalTaxAmount.amount.divide(taxableAmount.amount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else {
            BigDecimal.ZERO
        }
    }
    
    /**
     * Check if calculation requires immediate attention
     */
    fun requiresImmediateAttention(): Boolean {
        return validationErrors.isNotEmpty() ||
               auditFlags.isNotEmpty() ||
               hasHighRiskIndicators() ||
               confidenceLevel == TaxConfidenceLevel.LOW
    }
    
    /**
     * Get tax burden by type
     */
    fun getTaxBurdenByType(): Map<TaxType, BigDecimal> {
        return taxAmountsByType.mapValues { (_, amount) ->
            if (grossAmount.isPositive()) {
                amount.amount.divide(grossAmount.amount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
            } else {
                BigDecimal.ZERO
            }
        }
    }
    
    /**
     * Check if calculation affects cash flow
     */
    fun affectsCashFlow(): Boolean {
        return paymentDeadlines.values.any { deadline ->
            deadline.isBefore(LocalDate.now().plusDays(30))
        } && totalTaxAmount.isPositive()
    }
    
    /**
     * Get days until earliest payment deadline
     */
    fun getDaysUntilEarliestPayment(): Long? {
        val earliestDeadline = paymentDeadlines.values.minOrNull()
        return earliestDeadline?.let { 
            it.toEpochDay() - LocalDate.now().toEpochDay()
        }
    }
    
    /**
     * Check if calculation has compliance gaps
     */
    fun hasComplianceGaps(): Boolean {
        return complianceRequirements.any { requirement ->
            when (requirement) {
                ComplianceRequirement.CERTIFICATE_REQUIRED -> taxCertificates.isEmpty()
                ComplianceRequirement.REVIEW_REQUIRED -> !requiresReview
                ComplianceRequirement.APPROVAL_REQUIRED -> !requiresApproval
                ComplianceRequirement.DOCUMENTATION_REQUIRED -> notes.isNullOrBlank()
                else -> false
            }
        }
    }
    
    /**
     * Get tax optimization opportunity score
     */
    fun getTaxOptimizationScore(): Int {
        var score = 0
        
        // Check for exempt amounts that could be optimized
        if (exemptAmount.isPositive()) score += 20
        
        // Check for multiple jurisdictions (planning opportunities)
        if (isMultiJurisdictional()) score += 15
        
        // Check for high effective tax rate (optimization potential)
        if (getEffectiveTaxRate() >= BigDecimal("25")) score += 25
        
        // Check for reverse tax opportunities
        if (isReverseTax) score += 10
        
        // Check for withholding tax optimization
        if (isWithholdingTax && withholdingRate != null && withholdingRate >= BigDecimal("10")) score += 15
        
        // Check for uncertainty reserves (potential refund opportunities)
        if (reserveAmount?.isPositive() == true) score += 15
        
        return minOf(100, score)
    }
    
    /**
     * Check if calculation indicates tax planning opportunity
     */
    fun indicatesTaxPlanningOpportunity(): Boolean {
        return getTaxOptimizationScore() >= 60 ||
               uncertaintyAmount?.amount?.let { it >= totalTaxAmount.amount.multiply(BigDecimal("0.1")) } == true
    }
    
    companion object {
        /**
         * Create event for invoice tax calculation
         */
        fun forInvoiceTaxCalculation(
            calculationId: UUID,
            calculationNumber: String,
            entityId: UUID,
            entityName: String,
            invoiceId: UUID,
            invoiceNumber: String,
            customerId: UUID,
            customerName: String,
            grossAmount: FinancialAmount,
            taxableAmount: FinancialAmount,
            totalTaxAmount: FinancialAmount,
            taxRates: Map<TaxType, BigDecimal>,
            taxJurisdictions: Set<TaxJurisdiction>,
            calculationDate: LocalDate,
            taxPeriodId: UUID,
            taxYear: Int,
            auditTrailId: UUID
        ): TaxCalculationUpdatedEvent {
            
            val taxAmountsByType = taxRates.mapValues { (taxType, rate) ->
                FinancialAmount(
                    taxableAmount.amount.multiply(rate).divide(BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP),
                    grossAmount.currency
                )
            }
            
            return TaxCalculationUpdatedEvent(
                aggregateId = calculationId,
                taxCalculationId = calculationId,
                calculationNumber = calculationNumber,
                calculationType = TaxCalculationType.TRANSACTION_TAX,
                calculationTrigger = TaxCalculationTrigger.INVOICE_CREATION,
                calculationStatus = TaxCalculationStatus.CALCULATED,
                entityId = entityId,
                entityName = entityName,
                entityTaxId = "TAX-${entityName.take(10)}-${Random().nextInt(1000)}",
                invoiceId = invoiceId,
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                customerName = customerName,
                taxPeriodId = taxPeriodId,
                taxPeriod = TaxPeriod.MONTHLY,
                taxYear = taxYear,
                calculationDate = calculationDate,
                effectiveDate = calculationDate,
                transactionDate = calculationDate,
                currency = grossAmount.currency,
                baseCurrency = grossAmount.currency,
                grossAmount = grossAmount,
                taxableAmount = taxableAmount,
                exemptAmount = grossAmount.subtract(taxableAmount),
                totalTaxAmount = totalTaxAmount,
                taxAdjustmentAmount = FinancialAmount.ZERO,
                netAmount = grossAmount.add(totalTaxAmount),
                taxJurisdictions = taxJurisdictions,
                taxRates = taxRates,
                taxAmountsByType = taxAmountsByType,
                taxAmountsByJurisdiction = mapOf(taxJurisdictions.first() to totalTaxAmount),
                exemptionReasons = emptyMap(),
                taxCertificates = emptySet(),
                dueDates = mapOf(TaxType.SALES_TAX to calculationDate.plusDays(30)),
                complianceRequirements = emptySet(),
                riskFactors = emptySet(),
                auditFlags = emptySet(),
                calculationMethod = TaxCalculationMethod.STANDARD_RATES,
                calculationEngine = "INTERNAL_ENGINE",
                engineVersion = "1.0.0",
                calculationRules = setOf("STANDARD_SALES_TAX"),
                confidenceLevel = TaxConfidenceLevel.HIGH,
                requiresReview = false,
                requiresApproval = false,
                reportingRequirements = setOf(ReportingRequirement.MONTHLY_RETURN),
                submissionDeadlines = mapOf(TaxAuthority.STATE to calculationDate.plusDays(20)),
                paymentDeadlines = mapOf(TaxType.SALES_TAX to calculationDate.plusDays(30)),
                auditTrailId = auditTrailId,
                validationErrors = emptyList(),
                warnings = emptyList()
            )
        }
        
        /**
         * Create event for tax adjustment
         */
        fun forTaxAdjustment(
            calculationId: UUID,
            originalCalculationId: UUID,
            adjustmentAmount: FinancialAmount,
            adjustmentReason: String,
            approvedBy: UUID,
            approvedByName: String,
            auditTrailId: UUID
        ): TaxCalculationUpdatedEvent {
            
            return TaxCalculationUpdatedEvent(
                aggregateId = calculationId,
                taxCalculationId = calculationId,
                calculationNumber = "ADJ-${originalCalculationId.toString().take(8)}",
                calculationType = TaxCalculationType.ADJUSTMENT,
                calculationTrigger = TaxCalculationTrigger.MANUAL_ADJUSTMENT,
                calculationStatus = TaxCalculationStatus.APPROVED,
                entityId = UUID.randomUUID(),
                entityName = "Default Entity",
                entityTaxId = "DEFAULT",
                taxPeriodId = UUID.randomUUID(),
                taxPeriod = TaxPeriod.MONTHLY,
                taxYear = LocalDate.now().year,
                calculationDate = LocalDate.now(),
                effectiveDate = LocalDate.now(),
                currency = adjustmentAmount.currency,
                baseCurrency = adjustmentAmount.currency,
                grossAmount = FinancialAmount.ZERO,
                taxableAmount = FinancialAmount.ZERO,
                exemptAmount = FinancialAmount.ZERO,
                totalTaxAmount = adjustmentAmount,
                taxAdjustmentAmount = adjustmentAmount,
                netAmount = adjustmentAmount,
                taxJurisdictions = emptySet(),
                taxRates = emptyMap(),
                taxAmountsByType = emptyMap(),
                taxAmountsByJurisdiction = emptyMap(),
                exemptionReasons = emptyMap(),
                taxCertificates = emptySet(),
                dueDates = emptyMap(),
                complianceRequirements = emptySet(),
                riskFactors = setOf(TaxRiskFactor.MANUAL_ADJUSTMENT),
                auditFlags = setOf(TaxAuditFlag.ADJUSTMENT_MADE),
                calculationMethod = TaxCalculationMethod.MANUAL_OVERRIDE,
                calculationEngine = "MANUAL",
                engineVersion = "1.0.0",
                calculationRules = emptySet(),
                overrideApplied = true,
                overrideReason = adjustmentReason,
                overrideApprovedBy = approvedBy,
                manualAdjustments = listOf(
                    TaxAdjustment(
                        adjustmentId = UUID.randomUUID(),
                        adjustmentType = TaxAdjustmentType.CORRECTION,
                        amount = adjustmentAmount,
                        reason = adjustmentReason,
                        approvedBy = approvedBy
                    )
                ),
                confidenceLevel = TaxConfidenceLevel.HIGH,
                approvedBy = approvedBy,
                approvedByName = approvedByName,
                approvalDate = LocalDateTime.now(),
                requiresReview = false,
                requiresApproval = false,
                reportingRequirements = emptySet(),
                submissionDeadlines = emptyMap(),
                paymentDeadlines = emptyMap(),
                auditTrailId = auditTrailId,
                validationErrors = emptyList(),
                warnings = emptyList(),
                notes = "Tax adjustment: $adjustmentReason"
            )
        }
    }
}

/**
 * Tax Calculation Type Classifications
 */
enum class TaxCalculationType {
    TRANSACTION_TAX,
    PERIOD_TAX,
    WITHHOLDING_TAX,
    ADJUSTMENT,
    REFUND,
    ESTIMATED_TAX,
    FINAL_RETURN
}

/**
 * Tax Calculation Trigger Events
 */
enum class TaxCalculationTrigger {
    INVOICE_CREATION,
    PAYMENT_PROCESSING,
    PERIOD_END,
    MANUAL_CALCULATION,
    MANUAL_ADJUSTMENT,
    SYSTEM_RECALCULATION,
    COMPLIANCE_UPDATE,
    RATE_CHANGE
}

/**
 * Tax Calculation Status Values
 */
enum class TaxCalculationStatus {
    DRAFT,
    CALCULATED,
    REVIEWED,
    APPROVED,
    SUBMITTED,
    PAID,
    CANCELLED,
    ERROR
}

/**
 * Tax Period Classifications
 */
enum class TaxPeriod {
    MONTHLY,
    QUARTERLY,
    ANNUALLY,
    DAILY,
    WEEKLY
}

/**
 * Tax Jurisdiction Classifications
 */
enum class TaxJurisdiction {
    FEDERAL,
    STATE,
    COUNTY,
    CITY,
    SPECIAL_DISTRICT,
    FOREIGN_COUNTRY
}

/**
 * Tax Type Classifications
 */
enum class TaxType {
    SALES_TAX,
    USE_TAX,
    VAT,
    GST,
    INCOME_TAX,
    WITHHOLDING_TAX,
    EXCISE_TAX,
    PROPERTY_TAX,
    PAYROLL_TAX,
    CUSTOMS_DUTY
}

/**
 * Exemption Reason Types
 */
enum class ExemptionReason {
    TAX_EXEMPT_ORGANIZATION,
    RESALE_CERTIFICATE,
    GOVERNMENT_ENTITY,
    EXPORT_SALE,
    MEDICAL_EXEMPTION,
    AGRICULTURAL_EXEMPTION,
    MANUFACTURING_EXEMPTION,
    OTHER
}

/**
 * Tax Risk Factor Classifications
 */
enum class TaxRiskFactor {
    HIGH_VALUE_TRANSACTION,
    CROSS_BORDER,
    COMPLEX_STRUCTURE,
    FREQUENT_ADJUSTMENTS,
    MANUAL_OVERRIDE,
    MISSING_DOCUMENTATION,
    RATE_UNCERTAINTY,
    JURISDICTION_COMPLEXITY
}

/**
 * Tax Audit Flag Types
 */
enum class TaxAuditFlag {
    ADJUSTMENT_MADE,
    HIGH_RISK_TRANSACTION,
    DOCUMENTATION_MISSING,
    CALCULATION_OVERRIDE,
    RATE_DEVIATION,
    JURISDICTION_MISMATCH
}

/**
 * Tax Calculation Method Types
 */
enum class TaxCalculationMethod {
    STANDARD_RATES,
    MATRIX_LOOKUP,
    API_INTEGRATION,
    MANUAL_OVERRIDE,
    ESTIMATED_CALCULATION,
    REVERSE_CALCULATION
}

/**
 * Tax Confidence Level Classifications
 */
enum class TaxConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW,
    UNCERTAIN
}

/**
 * Compliance Requirement Types
 */
enum class ComplianceRequirement {
    CERTIFICATE_REQUIRED,
    REVIEW_REQUIRED,
    APPROVAL_REQUIRED,
    DOCUMENTATION_REQUIRED,
    FILING_REQUIRED,
    PAYMENT_REQUIRED
}

/**
 * Reporting Requirement Types
 */
enum class ReportingRequirement {
    MONTHLY_RETURN,
    QUARTERLY_RETURN,
    ANNUAL_RETURN,
    TRANSACTION_REPORTING,
    WITHHOLDING_REPORTING,
    INFORMATION_RETURN
}

/**
 * Tax Authority Classifications
 */
enum class TaxAuthority {
    FEDERAL,
    STATE,
    LOCAL,
    FOREIGN
}

/**
 * Tax Certificate Reference
 */
data class TaxCertificateReference(
    val certificateId: UUID,
    val certificateNumber: String,
    val certificateType: String,
    val issuedBy: String,
    val validFrom: LocalDate,
    val validTo: LocalDate?
)

/**
 * Tax Adjustment Details
 */
data class TaxAdjustment(
    val adjustmentId: UUID,
    val adjustmentType: TaxAdjustmentType,
    val amount: FinancialAmount,
    val reason: String,
    val approvedBy: UUID
)

/**
 * Tax Adjustment Type Classifications
 */
enum class TaxAdjustmentType {
    CORRECTION,
    PENALTY,
    INTEREST,
    REFUND,
    CREDIT,
    WRITE_OFF
}

/**
 * Validation Error Details
 */
data class ValidationError(
    val errorCode: String,
    val errorMessage: String,
    val field: String?,
    val severity: ErrorSeverity
)

/**
 * Tax Warning Details
 */
data class TaxWarning(
    val warningCode: String,
    val warningMessage: String,
    val recommendation: String?
)

/**
 * Error Severity Levels
 */
enum class ErrorSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

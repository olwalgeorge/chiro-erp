package org.chiro.finance.domain.exception

import java.math.BigDecimal
import java.util.*

/**
 * InvalidTaxRateException
 * 
 * Domain exception thrown when a tax rate validation fails or when tax
 * calculations encounter invalid tax rate configurations within the
 * finance domain.
 * 
 * This exception is thrown when:
 * - Tax rate is outside valid percentage bounds (0-100%)
 * - Tax rate precision exceeds system limits
 * - Tax jurisdiction is invalid or not configured
 * - Tax rate is expired or not yet effective
 * - Multiple conflicting tax rates found for context
 * - Tax rate configuration is incomplete or corrupted
 * - Tax calculation results in invalid amounts
 */
class InvalidTaxRateException : FinanceDomainException {
    
    val taxRate: BigDecimal?
    val taxType: String
    val jurisdiction: String?
    val effectiveDate: Date?
    val expirationDate: Date?
    val minimumRate: BigDecimal?
    val maximumRate: BigDecimal?
    val validationFailure: TaxRateValidationFailure
    val taxableAmount: BigDecimal?
    val calculatedTax: BigDecimal?
    val transactionId: UUID?
    val entityId: UUID?
    val configurationIssue: String?
    val suggestedRate: BigDecimal?
    
    constructor(
        taxRate: BigDecimal?,
        taxType: String,
        validationFailure: TaxRateValidationFailure,
        message: String = "Invalid tax rate configuration for tax type '$taxType'"
    ) : super(message) {
        this.taxRate = taxRate
        this.taxType = taxType
        this.jurisdiction = null
        this.effectiveDate = null
        this.expirationDate = null
        this.minimumRate = null
        this.maximumRate = null
        this.validationFailure = validationFailure
        this.taxableAmount = null
        this.calculatedTax = null
        this.transactionId = null
        this.entityId = null
        this.configurationIssue = null
        this.suggestedRate = null
    }
    
    constructor(
        taxRate: BigDecimal,
        taxType: String,
        jurisdiction: String,
        minimumRate: BigDecimal,
        maximumRate: BigDecimal,
        validationFailure: TaxRateValidationFailure,
        message: String
    ) : super(message) {
        this.taxRate = taxRate
        this.taxType = taxType
        this.jurisdiction = jurisdiction
        this.effectiveDate = null
        this.expirationDate = null
        this.minimumRate = minimumRate
        this.maximumRate = maximumRate
        this.validationFailure = validationFailure
        this.taxableAmount = null
        this.calculatedTax = null
        this.transactionId = null
        this.entityId = null
        this.configurationIssue = null
        this.suggestedRate = null
    }
    
    constructor(
        taxRate: BigDecimal?,
        taxType: String,
        jurisdiction: String,
        effectiveDate: Date,
        expirationDate: Date?,
        taxableAmount: BigDecimal,
        calculatedTax: BigDecimal?,
        transactionId: UUID,
        entityId: UUID,
        validationFailure: TaxRateValidationFailure,
        configurationIssue: String,
        suggestedRate: BigDecimal?,
        message: String
    ) : super(message) {
        this.taxRate = taxRate
        this.taxType = taxType
        this.jurisdiction = jurisdiction
        this.effectiveDate = effectiveDate
        this.expirationDate = expirationDate
        this.minimumRate = null
        this.maximumRate = null
        this.validationFailure = validationFailure
        this.taxableAmount = taxableAmount
        this.calculatedTax = calculatedTax
        this.transactionId = transactionId
        this.entityId = entityId
        this.configurationIssue = configurationIssue
        this.suggestedRate = suggestedRate
    }
    
    /**
     * Check if the tax rate is outside valid bounds
     */
    fun isOutOfBounds(): Boolean {
        return taxRate?.let { rate ->
            when {
                rate < BigDecimal.ZERO -> true
                rate > BigDecimal("100.00") -> true
                minimumRate != null && rate < minimumRate -> true
                maximumRate != null && rate > maximumRate -> true
                else -> false
            }
        } ?: false
    }
    
    /**
     * Check if the tax rate issue is date-related
     */
    fun isDateRelated(): Boolean {
        return validationFailure in setOf(
            TaxRateValidationFailure.NOT_YET_EFFECTIVE,
            TaxRateValidationFailure.EXPIRED,
            TaxRateValidationFailure.CONFLICTING_EFFECTIVE_DATES
        )
    }
    
    /**
     * Check if the issue is configuration-related
     */
    fun isConfigurationIssue(): Boolean {
        return validationFailure in setOf(
            TaxRateValidationFailure.MISSING_CONFIGURATION,
            TaxRateValidationFailure.INCOMPLETE_SETUP,
            TaxRateValidationFailure.CORRUPTED_DATA,
            TaxRateValidationFailure.JURISDICTION_NOT_CONFIGURED
        )
    }
    
    /**
     * Check if the issue can be resolved with calculation adjustment
     */
    fun isCalculationAdjustable(): Boolean {
        return validationFailure in setOf(
            TaxRateValidationFailure.PRECISION_EXCEEDED,
            TaxRateValidationFailure.ROUNDING_ERROR,
            TaxRateValidationFailure.CALCULATION_OVERFLOW
        )
    }
    
    /**
     * Get the severity level of this tax rate issue
     */
    fun getTaxRateSeverity(): TaxRateSeverity {
        return when (validationFailure) {
            TaxRateValidationFailure.CORRUPTED_DATA -> TaxRateSeverity.CRITICAL
            TaxRateValidationFailure.CALCULATION_OVERFLOW -> TaxRateSeverity.CRITICAL
            TaxRateValidationFailure.OUT_OF_BOUNDS -> TaxRateSeverity.HIGH
            TaxRateValidationFailure.MULTIPLE_CONFLICTING_RATES -> TaxRateSeverity.HIGH
            TaxRateValidationFailure.MISSING_CONFIGURATION -> TaxRateSeverity.HIGH
            TaxRateValidationFailure.JURISDICTION_NOT_CONFIGURED -> TaxRateSeverity.MEDIUM
            TaxRateValidationFailure.EXPIRED -> TaxRateSeverity.MEDIUM
            TaxRateValidationFailure.NOT_YET_EFFECTIVE -> TaxRateSeverity.MEDIUM
            TaxRateValidationFailure.INCOMPLETE_SETUP -> TaxRateSeverity.MEDIUM
            TaxRateValidationFailure.PRECISION_EXCEEDED -> TaxRateSeverity.LOW
            TaxRateValidationFailure.ROUNDING_ERROR -> TaxRateSeverity.LOW
            TaxRateValidationFailure.CONFLICTING_EFFECTIVE_DATES -> TaxRateSeverity.LOW
        }
    }
    
    /**
     * Get suggested resolution actions
     */
    fun getSuggestedResolutions(): List<String> {
        return when (validationFailure) {
            TaxRateValidationFailure.OUT_OF_BOUNDS -> {
                val bounds = when {
                    minimumRate != null && maximumRate != null -> 
                        "between ${minimumRate}% and ${maximumRate}%"
                    minimumRate != null -> "at least ${minimumRate}%"
                    maximumRate != null -> "at most ${maximumRate}%"
                    else -> "between 0% and 100%"
                }
                listOf(
                    "Adjust tax rate to be $bounds",
                    suggestedRate?.let { "Use suggested rate: ${it}%" } ?: "Review tax rate configuration",
                    "Verify tax rate with jurisdiction requirements",
                    "Check if rate should be expressed as decimal vs percentage"
                )
            }
            TaxRateValidationFailure.EXPIRED -> listOf(
                "Update tax rate with current effective rates",
                "Check for new tax rate schedules",
                suggestedRate?.let { "Use current rate: ${it}%" } ?: "Contact tax authority for current rates",
                "Review tax rate renewal process"
            )
            TaxRateValidationFailure.NOT_YET_EFFECTIVE -> listOf(
                "Wait until effective date: ${effectiveDate?.toString() ?: "unknown"}",
                "Use currently effective tax rate",
                suggestedRate?.let { "Use current effective rate: ${it}%" } ?: "Check current tax schedule",
                "Verify transaction date and tax applicability"
            )
            TaxRateValidationFailure.MISSING_CONFIGURATION -> listOf(
                "Configure tax rate for type '$taxType'",
                jurisdiction?.let { "Set up tax rates for jurisdiction '$it'" } ?: "Configure jurisdiction tax rates",
                "Import tax rate schedules from tax authority",
                "Set up default tax rate as fallback"
            )
            TaxRateValidationFailure.JURISDICTION_NOT_CONFIGURED -> listOf(
                jurisdiction?.let { "Configure tax jurisdiction '$it'" } ?: "Set up tax jurisdiction configuration",
                "Add jurisdiction to tax rate master data",
                "Map entity location to tax jurisdiction",
                "Set up jurisdiction-specific tax rules"
            )
            TaxRateValidationFailure.MULTIPLE_CONFLICTING_RATES -> listOf(
                "Resolve conflicting tax rate definitions",
                "Prioritize tax rates by effective date and precedence",
                "Remove duplicate or outdated tax rate entries",
                "Establish tax rate hierarchy and conflict resolution rules"
            )
            TaxRateValidationFailure.PRECISION_EXCEEDED -> listOf(
                "Reduce tax rate precision to system limits",
                "Use rounding rules for tax rate precision",
                suggestedRate?.let { "Use rounded rate: ${it}%" } ?: "Apply standard rounding",
                "Configure system to handle higher precision rates"
            )
            TaxRateValidationFailure.ROUNDING_ERROR -> listOf(
                "Apply consistent rounding rules for tax calculations",
                "Recalculate tax amount with proper rounding",
                calculatedTax?.let { "Review calculated amount: $it" } ?: "Verify calculation method",
                "Use banker's rounding or jurisdiction-specific rounding"
            )
            TaxRateValidationFailure.CALCULATION_OVERFLOW -> listOf(
                "Tax calculation resulted in overflow",
                "Reduce taxable amount or adjust calculation method",
                "Use high-precision arithmetic for large amounts",
                "Split calculation into smaller components"
            )
            TaxRateValidationFailure.INCOMPLETE_SETUP -> listOf(
                configurationIssue ?: "Complete tax rate configuration",
                "Set up all required tax rate parameters",
                "Configure effective dates and jurisdictions",
                "Test tax rate calculation with sample data"
            )
            TaxRateValidationFailure.CORRUPTED_DATA -> listOf(
                "Restore tax rate data from backup",
                "Reimport tax rates from authoritative source",
                "Verify data integrity and repair corruption",
                "Contact system administrator for data recovery"
            )
            TaxRateValidationFailure.CONFLICTING_EFFECTIVE_DATES -> listOf(
                "Resolve overlapping effective date ranges",
                "Set clear start and end dates for tax rates",
                "Establish precedence rules for date conflicts",
                "Review tax rate change management process"
            )
        }.filterNotNull()
    }
    
    /**
     * Get the impact assessment for this tax rate issue
     */
    fun getImpactAssessment(): String {
        return when (getTaxRateSeverity()) {
            TaxRateSeverity.CRITICAL -> "CRITICAL - Tax calculation failure, transaction cannot proceed safely"
            TaxRateSeverity.HIGH -> "HIGH - Incorrect tax rate, may result in compliance issues or financial loss"
            TaxRateSeverity.MEDIUM -> "MEDIUM - Tax rate unavailable or expired, requires configuration update"
            TaxRateSeverity.LOW -> "LOW - Minor precision or rounding issue, calculation can be adjusted"
        }
    }
    
    override fun getErrorCode(): String = "INVALID_TAX_RATE"
    
    override fun getErrorCategory(): String = "TAX_CALCULATION_ERROR"
    
    override fun getBusinessImpact(): String = getImpactAssessment()
    
    override fun getRecommendedAction(): String = getSuggestedResolutions().firstOrNull() 
        ?: "Review and correct tax rate configuration"
    
    override fun isRetryable(): Boolean = when (validationFailure) {
        TaxRateValidationFailure.CORRUPTED_DATA -> false
        TaxRateValidationFailure.CALCULATION_OVERFLOW -> false
        TaxRateValidationFailure.OUT_OF_BOUNDS -> false
        TaxRateValidationFailure.MULTIPLE_CONFLICTING_RATES -> false
        else -> true // Configuration and date issues can be retried after correction
    }
    
    override fun getRetryDelay(): Long = when {
        validationFailure == TaxRateValidationFailure.NOT_YET_EFFECTIVE -> {
            effectiveDate?.let { 
                maxOf(it.time - System.currentTimeMillis(), 0L) 
            } ?: 3600000L // 1 hour if no effective date known
        }
        isConfigurationIssue() -> 0L // Immediate retry after configuration
        isCalculationAdjustable() -> 0L // Immediate retry with adjustment
        else -> -1L // Not retryable
    }
    
    override fun requiresEscalation(): Boolean = getTaxRateSeverity() in setOf(
        TaxRateSeverity.CRITICAL,
        TaxRateSeverity.HIGH
    )
    
    override fun getContextInformation(): Map<String, Any> {
        return super.getContextInformation() + mapOf(
            "taxRate" to (taxRate?.toString() ?: "null"),
            "taxType" to taxType,
            "jurisdiction" to (jurisdiction ?: ""),
            "effectiveDate" to (effectiveDate?.toString() ?: ""),
            "expirationDate" to (expirationDate?.toString() ?: ""),
            "minimumRate" to (minimumRate?.toString() ?: ""),
            "maximumRate" to (maximumRate?.toString() ?: ""),
            "validationFailure" to validationFailure.name,
            "taxRateSeverity" to getTaxRateSeverity().name,
            "taxableAmount" to (taxableAmount?.toString() ?: ""),
            "calculatedTax" to (calculatedTax?.toString() ?: ""),
            "isOutOfBounds" to isOutOfBounds(),
            "isDateRelated" to isDateRelated(),
            "isConfigurationIssue" to isConfigurationIssue(),
            "isCalculationAdjustable" to isCalculationAdjustable(),
            "configurationIssue" to (configurationIssue ?: ""),
            "suggestedRate" to (suggestedRate?.toString() ?: ""),
            "transactionId" to (transactionId?.toString() ?: ""),
            "entityId" to (entityId?.toString() ?: "")
        )
    }
    
    companion object {
        /**
         * Create exception for tax rate out of valid bounds
         */
        fun outOfBounds(
            taxRate: BigDecimal,
            taxType: String,
            jurisdiction: String,
            minimumRate: BigDecimal = BigDecimal.ZERO,
            maximumRate: BigDecimal = BigDecimal("100.00")
        ): InvalidTaxRateException {
            return InvalidTaxRateException(
                taxRate = taxRate,
                taxType = taxType,
                jurisdiction = jurisdiction,
                minimumRate = minimumRate,
                maximumRate = maximumRate,
                validationFailure = TaxRateValidationFailure.OUT_OF_BOUNDS,
                message = "Tax rate ${taxRate}% for '$taxType' in jurisdiction '$jurisdiction' is outside valid bounds (${minimumRate}% - ${maximumRate}%)"
            )
        }
        
        /**
         * Create exception for expired tax rate
         */
        fun expired(
            taxRate: BigDecimal,
            taxType: String,
            jurisdiction: String,
            expirationDate: Date,
            currentRate: BigDecimal?
        ): InvalidTaxRateException {
            return InvalidTaxRateException(
                taxRate = taxRate,
                taxType = taxType,
                jurisdiction = jurisdiction,
                effectiveDate = null,
                expirationDate = expirationDate,
                taxableAmount = null,
                calculatedTax = null,
                transactionId = null,
                entityId = null,
                validationFailure = TaxRateValidationFailure.EXPIRED,
                configurationIssue = "Tax rate expired on ${expirationDate}",
                suggestedRate = currentRate,
                message = "Tax rate ${taxRate}% for '$taxType' in jurisdiction '$jurisdiction' expired on $expirationDate"
            )
        }
        
        /**
         * Create exception for missing tax rate configuration
         */
        fun missingConfiguration(
            taxType: String,
            jurisdiction: String,
            entityId: UUID
        ): InvalidTaxRateException {
            return InvalidTaxRateException(
                taxRate = null,
                taxType = taxType,
                jurisdiction = jurisdiction,
                effectiveDate = null,
                expirationDate = null,
                taxableAmount = null,
                calculatedTax = null,
                transactionId = null,
                entityId = entityId,
                validationFailure = TaxRateValidationFailure.MISSING_CONFIGURATION,
                configurationIssue = "No tax rate configured for type '$taxType' in jurisdiction '$jurisdiction'",
                suggestedRate = null,
                message = "Tax rate configuration missing for '$taxType' in jurisdiction '$jurisdiction'"
            )
        }
        
        /**
         * Create exception for calculation overflow
         */
        fun calculationOverflow(
            taxRate: BigDecimal,
            taxType: String,
            taxableAmount: BigDecimal,
            transactionId: UUID
        ): InvalidTaxRateException {
            return InvalidTaxRateException(
                taxRate = taxRate,
                taxType = taxType,
                jurisdiction = null,
                effectiveDate = null,
                expirationDate = null,
                taxableAmount = taxableAmount,
                calculatedTax = null,
                transactionId = transactionId,
                entityId = null,
                validationFailure = TaxRateValidationFailure.CALCULATION_OVERFLOW,
                configurationIssue = "Tax calculation overflow with rate ${taxRate}% on amount $taxableAmount",
                suggestedRate = null,
                message = "Tax calculation overflow: rate ${taxRate}% applied to amount $taxableAmount exceeds system limits"
            )
        }
        
        /**
         * Create exception for multiple conflicting tax rates
         */
        fun conflictingRates(
            taxType: String,
            jurisdiction: String,
            conflictingRates: List<BigDecimal>,
            transactionId: UUID
        ): InvalidTaxRateException {
            return InvalidTaxRateException(
                taxRate = null,
                taxType = taxType,
                jurisdiction = jurisdiction,
                effectiveDate = null,
                expirationDate = null,
                taxableAmount = null,
                calculatedTax = null,
                transactionId = transactionId,
                entityId = null,
                validationFailure = TaxRateValidationFailure.MULTIPLE_CONFLICTING_RATES,
                configurationIssue = "Multiple conflicting rates found: ${conflictingRates.joinToString(", ")}%",
                suggestedRate = conflictingRates.maxOrNull(),
                message = "Multiple conflicting tax rates found for '$taxType' in jurisdiction '$jurisdiction': ${conflictingRates.joinToString(", ")}%"
            )
        }
    }
}

/**
 * Tax Rate Validation Failure Types
 */
enum class TaxRateValidationFailure {
    OUT_OF_BOUNDS,
    EXPIRED,
    NOT_YET_EFFECTIVE,
    MISSING_CONFIGURATION,
    JURISDICTION_NOT_CONFIGURED,
    MULTIPLE_CONFLICTING_RATES,
    PRECISION_EXCEEDED,
    ROUNDING_ERROR,
    CALCULATION_OVERFLOW,
    INCOMPLETE_SETUP,
    CORRUPTED_DATA,
    CONFLICTING_EFFECTIVE_DATES
}

/**
 * Tax Rate Issue Severity Levels
 */
enum class TaxRateSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

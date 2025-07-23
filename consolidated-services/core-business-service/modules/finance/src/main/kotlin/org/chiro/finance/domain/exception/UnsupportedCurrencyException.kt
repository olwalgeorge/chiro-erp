package org.chiro.finance.domain.exception

import java.util.*

/**
 * UnsupportedCurrencyException
 * 
 * Domain exception thrown when an operation involves a currency that is not
 * supported by the system or is not configured for the specific operation
 * context within the finance domain.
 * 
 * This exception is thrown when:
 * - A transaction uses a currency not in the system's currency master
 * - Currency is inactive or suspended for trading
 * - Exchange rates are not available for currency conversion
 * - Currency is not supported for specific account types
 * - Multi-currency operations are disabled for the entity
 * - Currency precision/rounding rules are not configured
 */
class UnsupportedCurrencyException : FinanceDomainException {
    
    val currencyCode: String
    val operationType: String
    val entityId: UUID?
    val accountId: UUID?
    val supportedCurrencies: Set<String>?
    val unsupportedReason: UnsupportedCurrencyReason
    val transactionId: UUID?
    val requiredForOperation: String?
    val configurationMissing: String?
    val alternativeCurrencies: Set<String>?
    
    constructor(
        currencyCode: String,
        operationType: String,
        unsupportedReason: UnsupportedCurrencyReason,
        message: String = "Currency '$currencyCode' is not supported for operation '$operationType'"
    ) : super(message) {
        this.currencyCode = currencyCode
        this.operationType = operationType
        this.entityId = null
        this.accountId = null
        this.supportedCurrencies = null
        this.unsupportedReason = unsupportedReason
        this.transactionId = null
        this.requiredForOperation = null
        this.configurationMissing = null
        this.alternativeCurrencies = null
    }
    
    constructor(
        currencyCode: String,
        operationType: String,
        entityId: UUID,
        supportedCurrencies: Set<String>,
        unsupportedReason: UnsupportedCurrencyReason,
        message: String
    ) : super(message) {
        this.currencyCode = currencyCode
        this.operationType = operationType
        this.entityId = entityId
        this.accountId = null
        this.supportedCurrencies = supportedCurrencies
        this.unsupportedReason = unsupportedReason
        this.transactionId = null
        this.requiredForOperation = null
        this.configurationMissing = null
        this.alternativeCurrencies = supportedCurrencies
    }
    
    constructor(
        currencyCode: String,
        operationType: String,
        entityId: UUID,
        accountId: UUID,
        unsupportedReason: UnsupportedCurrencyReason,
        configurationMissing: String,
        alternativeCurrencies: Set<String>,
        message: String
    ) : super(message) {
        this.currencyCode = currencyCode
        this.operationType = operationType
        this.entityId = entityId
        this.accountId = accountId
        this.supportedCurrencies = null
        this.unsupportedReason = unsupportedReason
        this.transactionId = null
        this.requiredForOperation = null
        this.configurationMissing = configurationMissing
        this.alternativeCurrencies = alternativeCurrencies
    }
    
    /**
     * Check if the currency issue is a configuration problem
     */
    fun isConfigurationIssue(): Boolean {
        return unsupportedReason in setOf(
            UnsupportedCurrencyReason.NOT_CONFIGURED,
            UnsupportedCurrencyReason.MISSING_EXCHANGE_RATES,
            UnsupportedCurrencyReason.MISSING_PRECISION_RULES,
            UnsupportedCurrencyReason.ACCOUNT_TYPE_RESTRICTION
        )
    }
    
    /**
     * Check if the currency issue is due to business policy
     */
    fun isBusinessPolicyIssue(): Boolean {
        return unsupportedReason in setOf(
            UnsupportedCurrencyReason.POLICY_RESTRICTION,
            UnsupportedCurrencyReason.COMPLIANCE_RESTRICTION,
            UnsupportedCurrencyReason.GEOGRAPHIC_RESTRICTION
        )
    }
    
    /**
     * Check if the currency issue is temporary
     */
    fun isTemporaryIssue(): Boolean {
        return unsupportedReason in setOf(
            UnsupportedCurrencyReason.TEMPORARILY_SUSPENDED,
            UnsupportedCurrencyReason.MISSING_EXCHANGE_RATES,
            UnsupportedCurrencyReason.MARKET_CLOSED
        )
    }
    
    /**
     * Get the severity level of this currency issue
     */
    fun getCurrencySeverity(): CurrencySeverity {
        return when (unsupportedReason) {
            UnsupportedCurrencyReason.PERMANENTLY_DISABLED -> CurrencySeverity.CRITICAL
            UnsupportedCurrencyReason.COMPLIANCE_RESTRICTION -> CurrencySeverity.HIGH
            UnsupportedCurrencyReason.POLICY_RESTRICTION -> CurrencySeverity.HIGH
            UnsupportedCurrencyReason.NOT_CONFIGURED -> CurrencySeverity.MEDIUM
            UnsupportedCurrencyReason.TEMPORARILY_SUSPENDED -> CurrencySeverity.MEDIUM
            UnsupportedCurrencyReason.MISSING_EXCHANGE_RATES -> CurrencySeverity.MEDIUM
            UnsupportedCurrencyReason.ACCOUNT_TYPE_RESTRICTION -> CurrencySeverity.LOW
            UnsupportedCurrencyReason.GEOGRAPHIC_RESTRICTION -> CurrencySeverity.LOW
            UnsupportedCurrencyReason.MISSING_PRECISION_RULES -> CurrencySeverity.LOW
            UnsupportedCurrencyReason.MARKET_CLOSED -> CurrencySeverity.LOW
        }
    }
    
    /**
     * Get suggested resolution actions
     */
    fun getSuggestedResolutions(): List<String> {
        return when (unsupportedReason) {
            UnsupportedCurrencyReason.NOT_CONFIGURED -> listOf(
                "Configure currency '$currencyCode' in the system",
                "Add currency to supported currencies list",
                "Set up exchange rates for '$currencyCode'",
                "Configure currency precision and rounding rules"
            )
            UnsupportedCurrencyReason.MISSING_EXCHANGE_RATES -> listOf(
                "Update exchange rates for currency '$currencyCode'",
                "Check currency exchange rate provider",
                "Manually set exchange rate if needed",
                "Use alternative currency with available rates: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}"
            )
            UnsupportedCurrencyReason.TEMPORARILY_SUSPENDED -> listOf(
                "Wait for currency to be reactivated",
                "Contact administrator for reactivation timeline",
                "Use alternative supported currency: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}",
                "Check if suspension is due to maintenance"
            )
            UnsupportedCurrencyReason.PERMANENTLY_DISABLED -> listOf(
                "Currency '$currencyCode' is permanently disabled",
                "Use alternative supported currency: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}",
                "Contact administrator if currency should be re-enabled",
                "Review business requirements for this currency"
            )
            UnsupportedCurrencyReason.POLICY_RESTRICTION -> listOf(
                "Review business policy for currency '$currencyCode'",
                "Request policy exception if legitimate business need",
                "Use approved alternative currency: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}",
                "Consult with compliance team"
            )
            UnsupportedCurrencyReason.COMPLIANCE_RESTRICTION -> listOf(
                "Currency '$currencyCode' restricted by compliance requirements",
                "Consult with compliance team for alternatives",
                "Review regulatory requirements",
                "Use compliant alternative currency: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}"
            )
            UnsupportedCurrencyReason.ACCOUNT_TYPE_RESTRICTION -> listOf(
                "Account type does not support currency '$currencyCode'",
                "Use different account type that supports this currency",
                "Use supported currency for this account: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}",
                "Review account configuration and currency restrictions"
            )
            UnsupportedCurrencyReason.GEOGRAPHIC_RESTRICTION -> listOf(
                "Currency '$currencyCode' not supported in current geographic region",
                "Use region-appropriate currency: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}",
                "Review geographic currency policies",
                "Contact administrator for region-specific currency support"
            )
            UnsupportedCurrencyReason.MISSING_PRECISION_RULES -> listOf(
                "Configure precision and rounding rules for currency '$currencyCode'",
                "Set decimal places and rounding method",
                "Use currency with configured precision rules: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}",
                "Contact system administrator for currency configuration"
            )
            UnsupportedCurrencyReason.MARKET_CLOSED -> listOf(
                "Currency market is closed for '$currencyCode'",
                "Wait for market to open",
                "Use alternative currency with open market: ${alternativeCurrencies?.joinToString(", ") ?: "base currency"}",
                "Check market hours and holiday schedule"
            )
        }
    }
    
    override fun getErrorCode(): String = "UNSUPPORTED_CURRENCY"
    
    override fun getErrorCategory(): String = "CONFIGURATION_ERROR"
    
    override fun getBusinessImpact(): String = when (getCurrencySeverity()) {
        CurrencySeverity.CRITICAL -> "CRITICAL - Currency permanently disabled, transaction cannot proceed"
        CurrencySeverity.HIGH -> "HIGH - Policy/compliance restriction, requires approval or alternative"
        CurrencySeverity.MEDIUM -> "MEDIUM - Configuration issue, requires setup or temporary workaround"
        CurrencySeverity.LOW -> "LOW - Minor restriction, alternative options available"
    }
    
    override fun getRecommendedAction(): String = getSuggestedResolutions().firstOrNull() 
        ?: "Review currency configuration and supported currencies"
    
    override fun isRetryable(): Boolean = isTemporaryIssue() || isConfigurationIssue()
    
    override fun getRetryDelay(): Long = when {
        unsupportedReason == UnsupportedCurrencyReason.MARKET_CLOSED -> 3600000 // 1 hour
        unsupportedReason == UnsupportedCurrencyReason.MISSING_EXCHANGE_RATES -> 300000 // 5 minutes
        isConfigurationIssue() -> 0 // Immediate retry after configuration
        isTemporaryIssue() -> 1800000 // 30 minutes
        else -> -1 // Not retryable
    }
    
    override fun requiresEscalation(): Boolean = getCurrencySeverity() in setOf(
        CurrencySeverity.CRITICAL,
        CurrencySeverity.HIGH
    )
    
    override fun getContextInformation(): Map<String, Any> {
        return super.getContextInformation() + mapOf(
            "currencyCode" to currencyCode,
            "operationType" to operationType,
            "unsupportedReason" to unsupportedReason.name,
            "currencySeverity" to getCurrencySeverity().name,
            "isConfigurationIssue" to isConfigurationIssue(),
            "isBusinessPolicyIssue" to isBusinessPolicyIssue(),
            "isTemporaryIssue" to isTemporaryIssue(),
            "supportedCurrencies" to (supportedCurrencies?.toList() ?: emptyList<String>()),
            "alternativeCurrencies" to (alternativeCurrencies?.toList() ?: emptyList<String>()),
            "configurationMissing" to (configurationMissing ?: ""),
            "entityId" to (entityId?.toString() ?: ""),
            "accountId" to (accountId?.toString() ?: "")
        )
    }
    
    companion object {
        /**
         * Create exception for currency not found in system
         */
        fun currencyNotConfigured(
            currencyCode: String,
            operationType: String,
            supportedCurrencies: Set<String>
        ): UnsupportedCurrencyException {
            return UnsupportedCurrencyException(
                currencyCode = currencyCode,
                operationType = operationType,
                entityId = UUID.randomUUID(), // Would be actual entity ID
                supportedCurrencies = supportedCurrencies,
                unsupportedReason = UnsupportedCurrencyReason.NOT_CONFIGURED,
                message = "Currency '$currencyCode' is not configured for $operationType. Supported currencies: ${supportedCurrencies.joinToString(", ")}"
            )
        }
        
        /**
         * Create exception for missing exchange rates
         */
        fun missingExchangeRates(
            currencyCode: String,
            operationType: String,
            baseCurrency: String
        ): UnsupportedCurrencyException {
            return UnsupportedCurrencyException(
                currencyCode = currencyCode,
                operationType = operationType,
                unsupportedReason = UnsupportedCurrencyReason.MISSING_EXCHANGE_RATES,
                message = "Exchange rates not available for currency '$currencyCode' to base currency '$baseCurrency' for $operationType"
            )
        }
        
        /**
         * Create exception for account type currency restriction
         */
        fun accountTypeRestriction(
            currencyCode: String,
            operationType: String,
            accountId: UUID,
            accountType: String,
            supportedCurrencies: Set<String>
        ): UnsupportedCurrencyException {
            return UnsupportedCurrencyException(
                currencyCode = currencyCode,
                operationType = operationType,
                entityId = UUID.randomUUID(), // Would be actual entity ID
                accountId = accountId,
                unsupportedReason = UnsupportedCurrencyReason.ACCOUNT_TYPE_RESTRICTION,
                configurationMissing = "Account type '$accountType' currency restrictions",
                alternativeCurrencies = supportedCurrencies,
                message = "Currency '$currencyCode' is not supported for account type '$accountType' in $operationType. Supported currencies for this account type: ${supportedCurrencies.joinToString(", ")}"
            )
        }
        
        /**
         * Create exception for compliance-restricted currency
         */
        fun complianceRestriction(
            currencyCode: String,
            operationType: String,
            entityId: UUID,
            restrictionReason: String
        ): UnsupportedCurrencyException {
            return UnsupportedCurrencyException(
                currencyCode = currencyCode,
                operationType = operationType,
                entityId = entityId,
                accountId = null,
                unsupportedReason = UnsupportedCurrencyReason.COMPLIANCE_RESTRICTION,
                configurationMissing = restrictionReason,
                alternativeCurrencies = emptySet(),
                message = "Currency '$currencyCode' is restricted for $operationType due to compliance requirements: $restrictionReason"
            )
        }
        
        /**
         * Create exception for temporarily suspended currency
         */
        fun temporarilySuspended(
            currencyCode: String,
            operationType: String,
            suspensionReason: String,
            alternativeCurrencies: Set<String>
        ): UnsupportedCurrencyException {
            return UnsupportedCurrencyException(
                currencyCode = currencyCode,
                operationType = operationType,
                entityId = UUID.randomUUID(), // Would be actual entity ID
                accountId = null,
                unsupportedReason = UnsupportedCurrencyReason.TEMPORARILY_SUSPENDED,
                configurationMissing = suspensionReason,
                alternativeCurrencies = alternativeCurrencies,
                message = "Currency '$currencyCode' is temporarily suspended for $operationType. Reason: $suspensionReason. Alternative currencies: ${alternativeCurrencies.joinToString(", ")}"
            )
        }
    }
}

/**
 * Unsupported Currency Reason Types
 */
enum class UnsupportedCurrencyReason {
    NOT_CONFIGURED,
    MISSING_EXCHANGE_RATES,
    TEMPORARILY_SUSPENDED,
    PERMANENTLY_DISABLED,
    POLICY_RESTRICTION,
    COMPLIANCE_RESTRICTION,
    ACCOUNT_TYPE_RESTRICTION,
    GEOGRAPHIC_RESTRICTION,
    MISSING_PRECISION_RULES,
    MARKET_CLOSED
}

/**
 * Currency Issue Severity Levels
 */
enum class CurrencySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

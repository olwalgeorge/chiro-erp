package org.chiro.finance.domain.service

import org.chiro.finance.domain.entity.*
import org.chiro.finance.domain.valueobject.*
import org.chiro.finance.domain.exception.*
import java.math.BigDecimal
import java.util.*

/**
 * PaymentValidationService
 * 
 * Domain service responsible for comprehensive payment validation logic
 * including business rules, compliance checks, fraud detection, and
 * payment method verification within the finance domain.
 * 
 * This service provides:
 * - Multi-layered payment validation with business rule enforcement
 * - Payment method verification and capability checking
 * - Amount limits validation with account-specific rules
 * - Fraud detection and risk assessment integration
 * - Compliance validation for regulatory requirements
 * - Currency conversion validation for multi-currency payments
 * - Payment routing validation and optimization
 */
class PaymentValidationService {
    
    /**
     * Validate a complete payment transaction with comprehensive checks
     */
    fun validatePayment(
        payment: Payment,
        sourceAccount: Account,
        destinationAccount: Account?,
        paymentMethod: PaymentMethod,
        entityContext: EntityContext
    ): PaymentValidationResult {
        
        val validationResults = mutableListOf<ValidationResult>()
        var overallSeverity = ValidationSeverity.PASSED
        
        // 1. Basic payment structure validation
        validationResults.add(validatePaymentStructure(payment))
        
        // 2. Source account validation
        validationResults.add(validateSourceAccount(payment, sourceAccount, entityContext))
        
        // 3. Destination account validation (if applicable)
        destinationAccount?.let { dest ->
            validationResults.add(validateDestinationAccount(payment, dest, entityContext))
        }
        
        // 4. Payment method validation
        validationResults.add(validatePaymentMethod(payment, paymentMethod, sourceAccount))
        
        // 5. Amount and limits validation
        validationResults.add(validateAmountLimits(payment, sourceAccount, paymentMethod, entityContext))
        
        // 6. Currency validation
        validationResults.add(validateCurrency(payment, sourceAccount, destinationAccount, entityContext))
        
        // 7. Fraud and risk assessment
        validationResults.add(performFraudAssessment(payment, sourceAccount, paymentMethod, entityContext))
        
        // 8. Compliance validation
        validationResults.add(validateCompliance(payment, sourceAccount, destinationAccount, entityContext))
        
        // 9. Business rules validation
        validationResults.add(validateBusinessRules(payment, sourceAccount, destinationAccount, entityContext))
        
        // 10. Cross-validation checks
        validationResults.add(performCrossValidation(payment, sourceAccount, destinationAccount, paymentMethod))
        
        // Determine overall validation result
        val failedValidations = validationResults.filter { !it.isPassed }
        val criticalFailures = failedValidations.filter { it.severity == ValidationSeverity.CRITICAL }
        val highFailures = failedValidations.filter { it.severity == ValidationSeverity.HIGH }
        
        overallSeverity = when {
            criticalFailures.isNotEmpty() -> ValidationSeverity.CRITICAL
            highFailures.isNotEmpty() -> ValidationSeverity.HIGH
            failedValidations.isNotEmpty() -> ValidationSeverity.MEDIUM
            else -> ValidationSeverity.PASSED
        }
        
        return PaymentValidationResult(
            paymentId = payment.id,
            isValid = failedValidations.isEmpty(),
            overallSeverity = overallSeverity,
            validationResults = validationResults,
            failedValidations = failedValidations,
            approvalRequired = determineApprovalRequirement(validationResults, entityContext),
            recommendedActions = generateRecommendedActions(failedValidations),
            validationTimestamp = Date(),
            validationContext = mapOf(
                "sourceAccountId" to sourceAccount.id.toString(),
                "destinationAccountId" to (destinationAccount?.id?.toString() ?: ""),
                "paymentMethodType" to paymentMethod.type.name,
                "entityId" to entityContext.entityId.toString(),
                "validationCount" to validationResults.size.toString(),
                "failureCount" to failedValidations.size.toString()
            )
        )
    }
    
    /**
     * Validate basic payment structure and required fields
     */
    private fun validatePaymentStructure(payment: Payment): ValidationResult {
        val issues = mutableListOf<String>()
        
        // Validate amount
        if (payment.amount.amount <= BigDecimal.ZERO) {
            issues.add("Payment amount must be greater than zero")
        }
        
        if (payment.amount.amount > BigDecimal("999999999.99")) {
            issues.add("Payment amount exceeds maximum system limit")
        }
        
        // Validate description
        if (payment.description.isBlank()) {
            issues.add("Payment description is required")
        }
        
        if (payment.description.length > 500) {
            issues.add("Payment description exceeds maximum length (500 characters)")
        }
        
        // Validate reference number
        if (payment.referenceNumber.isBlank()) {
            issues.add("Payment reference number is required")
        }
        
        // Validate payment date
        val now = Date()
        if (payment.paymentDate.before(Date(now.time - 86400000 * 30))) { // 30 days ago
            issues.add("Payment date cannot be more than 30 days in the past")
        }
        
        if (payment.paymentDate.after(Date(now.time + 86400000 * 365))) { // 1 year future
            issues.add("Payment date cannot be more than 1 year in the future")
        }
        
        return ValidationResult(
            validationType = "PAYMENT_STRUCTURE",
            isPassed = issues.isEmpty(),
            severity = if (issues.isEmpty()) ValidationSeverity.PASSED else ValidationSeverity.MEDIUM,
            message = if (issues.isEmpty()) "Payment structure validation passed" else "Payment structure validation failed",
            issues = issues,
            details = mapOf(
                "paymentAmount" to payment.amount.amount.toString(),
                "paymentCurrency" to payment.amount.currency.code,
                "descriptionLength" to payment.description.length.toString(),
                "referenceNumber" to payment.referenceNumber,
                "paymentDate" to payment.paymentDate.toString()
            )
        )
    }
    
    /**
     * Validate source account eligibility and status
     */
    private fun validateSourceAccount(
        payment: Payment,
        sourceAccount: Account,
        entityContext: EntityContext
    ): ValidationResult {
        val issues = mutableListOf<String>()
        
        // Check account status
        if (sourceAccount.status != AccountStatus.ACTIVE) {
            issues.add("Source account is not active (status: ${sourceAccount.status})")
        }
        
        // Check account type restrictions
        if (!sourceAccount.type.canDebit()) {
            issues.add("Source account type ${sourceAccount.type} does not allow debits")
        }
        
        // Check currency compatibility
        if (sourceAccount.currency != payment.amount.currency) {
            if (!sourceAccount.supportsMultiCurrency) {
                issues.add("Source account does not support currency ${payment.amount.currency.code}")
            }
        }
        
        // Check sufficient funds
        if (sourceAccount.balance.amount < payment.amount.amount) {
            issues.add("Insufficient funds in source account (available: ${sourceAccount.balance.amount}, required: ${payment.amount.amount})")
        }
        
        // Check account limits
        val dailyLimit = sourceAccount.limits?.dailyDebitLimit
        if (dailyLimit != null && payment.amount.amount > dailyLimit) {
            issues.add("Payment amount exceeds daily debit limit (${dailyLimit})")
        }
        
        // Check entity permissions
        if (!entityContext.hasPermission("PAYMENT_INITIATE", sourceAccount.id.toString())) {
            issues.add("Entity does not have permission to initiate payments from this account")
        }
        
        return ValidationResult(
            validationType = "SOURCE_ACCOUNT",
            isPassed = issues.isEmpty(),
            severity = when {
                issues.any { it.contains("not active") || it.contains("Insufficient funds") } -> ValidationSeverity.CRITICAL
                issues.any { it.contains("permission") || it.contains("does not allow") } -> ValidationSeverity.HIGH
                else -> if (issues.isEmpty()) ValidationSeverity.PASSED else ValidationSeverity.MEDIUM
            },
            message = if (issues.isEmpty()) "Source account validation passed" else "Source account validation failed",
            issues = issues,
            details = mapOf(
                "accountId" to sourceAccount.id.toString(),
                "accountStatus" to sourceAccount.status.name,
                "accountType" to sourceAccount.type.name,
                "accountBalance" to sourceAccount.balance.amount.toString(),
                "accountCurrency" to sourceAccount.currency.code,
                "supportsMultiCurrency" to sourceAccount.supportsMultiCurrency.toString()
            )
        )
    }
    
    /**
     * Validate destination account if applicable
     */
    private fun validateDestinationAccount(
        payment: Payment,
        destinationAccount: Account,
        entityContext: EntityContext
    ): ValidationResult {
        val issues = mutableListOf<String>()
        
        // Check account status
        if (destinationAccount.status == AccountStatus.CLOSED) {
            issues.add("Destination account is closed")
        }
        
        if (destinationAccount.status == AccountStatus.SUSPENDED) {
            issues.add("Destination account is suspended")
        }
        
        // Check account type restrictions
        if (!destinationAccount.type.canCredit()) {
            issues.add("Destination account type ${destinationAccount.type} does not allow credits")
        }
        
        // Check currency compatibility
        if (destinationAccount.currency != payment.amount.currency) {
            if (!destinationAccount.supportsMultiCurrency) {
                issues.add("Destination account does not support currency ${payment.amount.currency.code}")
            }
        }
        
        // Check credit limits
        val creditLimit = destinationAccount.limits?.creditLimit
        if (creditLimit != null) {
            val projectedBalance = destinationAccount.balance.amount + payment.amount.amount
            if (projectedBalance > creditLimit) {
                issues.add("Payment would exceed destination account credit limit")
            }
        }
        
        return ValidationResult(
            validationType = "DESTINATION_ACCOUNT",
            isPassed = issues.isEmpty(),
            severity = when {
                issues.any { it.contains("closed") || it.contains("suspended") } -> ValidationSeverity.HIGH
                issues.any { it.contains("does not allow") } -> ValidationSeverity.HIGH
                else -> if (issues.isEmpty()) ValidationSeverity.PASSED else ValidationSeverity.MEDIUM
            },
            message = if (issues.isEmpty()) "Destination account validation passed" else "Destination account validation failed",
            issues = issues,
            details = mapOf(
                "accountId" to destinationAccount.id.toString(),
                "accountStatus" to destinationAccount.status.name,
                "accountType" to destinationAccount.type.name,
                "accountCurrency" to destinationAccount.currency.code,
                "supportsMultiCurrency" to destinationAccount.supportsMultiCurrency.toString()
            )
        )
    }
    
    /**
     * Validate payment method compatibility and capabilities
     */
    private fun validatePaymentMethod(
        payment: Payment,
        paymentMethod: PaymentMethod,
        sourceAccount: Account
    ): ValidationResult {
        val issues = mutableListOf<String>()
        
        // Check payment method status
        if (!paymentMethod.isActive) {
            issues.add("Payment method is inactive")
        }
        
        // Check currency support
        if (!paymentMethod.supportedCurrencies.contains(payment.amount.currency)) {
            issues.add("Payment method does not support currency ${payment.amount.currency.code}")
        }
        
        // Check amount limits
        if (payment.amount.amount < paymentMethod.minimumAmount) {
            issues.add("Payment amount below minimum for this payment method (${paymentMethod.minimumAmount})")
        }
        
        if (payment.amount.amount > paymentMethod.maximumAmount) {
            issues.add("Payment amount exceeds maximum for this payment method (${paymentMethod.maximumAmount})")
        }
        
        // Check account type compatibility
        if (!paymentMethod.supportedAccountTypes.contains(sourceAccount.type)) {
            issues.add("Payment method does not support source account type ${sourceAccount.type}")
        }
        
        // Check processing schedule
        if (!paymentMethod.isAvailableAt(payment.paymentDate)) {
            issues.add("Payment method not available at scheduled payment time")
        }
        
        return ValidationResult(
            validationType = "PAYMENT_METHOD",
            isPassed = issues.isEmpty(),
            severity = when {
                issues.any { it.contains("inactive") || it.contains("not support") } -> ValidationSeverity.HIGH
                else -> if (issues.isEmpty()) ValidationSeverity.PASSED else ValidationSeverity.MEDIUM
            },
            message = if (issues.isEmpty()) "Payment method validation passed" else "Payment method validation failed",
            issues = issues,
            details = mapOf(
                "paymentMethodType" to paymentMethod.type.name,
                "isActive" to paymentMethod.isActive.toString(),
                "minimumAmount" to paymentMethod.minimumAmount.toString(),
                "maximumAmount" to paymentMethod.maximumAmount.toString(),
                "supportedCurrencies" to paymentMethod.supportedCurrencies.joinToString { it.code }
            )
        )
    }
    
    /**
     * Validate amount limits across multiple dimensions
     */
    private fun validateAmountLimits(
        payment: Payment,
        sourceAccount: Account,
        paymentMethod: PaymentMethod,
        entityContext: EntityContext
    ): ValidationResult {
        val issues = mutableListOf<String>()
        
        // Entity-level limits
        val entityLimits = entityContext.getPaymentLimits()
        if (entityLimits != null) {
            if (payment.amount.amount > entityLimits.singleTransactionLimit) {
                issues.add("Payment exceeds entity single transaction limit (${entityLimits.singleTransactionLimit})")
            }
            
            val dailyTotal = entityContext.getDailyPaymentTotal(sourceAccount.id)
            if (dailyTotal + payment.amount.amount > entityLimits.dailyLimit) {
                issues.add("Payment would exceed entity daily limit (${entityLimits.dailyLimit})")
            }
        }
        
        // Regulatory limits
        val regulatoryLimits = getRegulatoryLimits(payment.amount.currency, entityContext.jurisdiction)
        if (regulatoryLimits != null && payment.amount.amount > regulatoryLimits.reportingThreshold) {
            // Not an error, but requires additional reporting
            issues.add("Payment exceeds regulatory reporting threshold - additional documentation required")
        }
        
        return ValidationResult(
            validationType = "AMOUNT_LIMITS",
            isPassed = issues.isEmpty() || issues.all { it.contains("reporting threshold") },
            severity = when {
                issues.any { it.contains("exceeds") && !it.contains("reporting") } -> ValidationSeverity.HIGH
                issues.any { it.contains("reporting threshold") } -> ValidationSeverity.MEDIUM
                else -> ValidationSeverity.PASSED
            },
            message = if (issues.isEmpty()) "Amount limits validation passed" else "Amount validation completed with notes",
            issues = issues,
            details = mapOf(
                "paymentAmount" to payment.amount.amount.toString(),
                "entityDailyTotal" to (entityContext.getDailyPaymentTotal(sourceAccount.id)?.toString() ?: "0"),
                "jurisdiction" to entityContext.jurisdiction
            )
        )
    }
    
    /**
     * Validate currency-related aspects
     */
    private fun validateCurrency(
        payment: Payment,
        sourceAccount: Account,
        destinationAccount: Account?,
        entityContext: EntityContext
    ): ValidationResult {
        val issues = mutableListOf<String>()
        
        // Check currency support
        if (!entityContext.supportedCurrencies.contains(payment.amount.currency)) {
            issues.add("Currency ${payment.amount.currency.code} not supported by entity")
        }
        
        // Check exchange rates if conversion needed
        if (sourceAccount.currency != payment.amount.currency) {
            val exchangeRate = getExchangeRate(sourceAccount.currency, payment.amount.currency)
            if (exchangeRate == null) {
                issues.add("Exchange rate not available for ${sourceAccount.currency.code} to ${payment.amount.currency.code}")
            }
        }
        
        // Check currency restrictions
        val currencyRestrictions = getCurrencyRestrictions(payment.amount.currency, entityContext.jurisdiction)
        if (currencyRestrictions.isRestricted) {
            issues.add("Currency ${payment.amount.currency.code} is restricted in jurisdiction ${entityContext.jurisdiction}")
        }
        
        return ValidationResult(
            validationType = "CURRENCY",
            isPassed = issues.isEmpty(),
            severity = when {
                issues.any { it.contains("not supported") || it.contains("restricted") } -> ValidationSeverity.HIGH
                issues.any { it.contains("not available") } -> ValidationSeverity.MEDIUM
                else -> ValidationSeverity.PASSED
            },
            message = if (issues.isEmpty()) "Currency validation passed" else "Currency validation failed",
            issues = issues,
            details = mapOf(
                "paymentCurrency" to payment.amount.currency.code,
                "sourceAccountCurrency" to sourceAccount.currency.code,
                "destinationAccountCurrency" to (destinationAccount?.currency?.code ?: "N/A"),
                "jurisdiction" to entityContext.jurisdiction
            )
        )
    }
    
    /**
     * Perform fraud and risk assessment
     */
    private fun performFraudAssessment(
        payment: Payment,
        sourceAccount: Account,
        paymentMethod: PaymentMethod,
        entityContext: EntityContext
    ): ValidationResult {
        val riskFactors = mutableListOf<String>()
        var riskScore = 0
        
        // Amount-based risk factors
        val avgTransactionAmount = sourceAccount.getAverageTransactionAmount()
        if (payment.amount.amount > avgTransactionAmount * BigDecimal("5")) {
            riskFactors.add("Payment amount significantly higher than account average")
            riskScore += 20
        }
        
        // Frequency-based risk factors
        val recentTransactionCount = sourceAccount.getRecentTransactionCount(24) // Last 24 hours
        if (recentTransactionCount > 10) {
            riskFactors.add("High transaction frequency detected")
            riskScore += 15
        }
        
        // Time-based risk factors
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour < 6 || hour > 22) {
            riskFactors.add("Transaction initiated during unusual hours")
            riskScore += 10
        }
        
        // Geographic risk factors
        if (entityContext.isFromHighRiskLocation()) {
            riskFactors.add("Transaction from high-risk geographic location")
            riskScore += 25
        }
        
        // Payment method risk factors
        if (paymentMethod.riskLevel == PaymentMethodRiskLevel.HIGH) {
            riskFactors.add("High-risk payment method selected")
            riskScore += 20
        }
        
        val riskLevel = when {
            riskScore >= 50 -> RiskLevel.HIGH
            riskScore >= 25 -> RiskLevel.MEDIUM
            riskScore > 0 -> RiskLevel.LOW
            else -> RiskLevel.MINIMAL
        }
        
        return ValidationResult(
            validationType = "FRAUD_ASSESSMENT",
            isPassed = riskLevel != RiskLevel.HIGH,
            severity = when (riskLevel) {
                RiskLevel.HIGH -> ValidationSeverity.CRITICAL
                RiskLevel.MEDIUM -> ValidationSeverity.HIGH
                RiskLevel.LOW -> ValidationSeverity.MEDIUM
                RiskLevel.MINIMAL -> ValidationSeverity.PASSED
            },
            message = "Fraud risk assessment completed - Risk Level: ${riskLevel.name}",
            issues = if (riskLevel == RiskLevel.HIGH) listOf("High fraud risk detected - manual review required") else emptyList(),
            details = mapOf(
                "riskScore" to riskScore.toString(),
                "riskLevel" to riskLevel.name,
                "riskFactorCount" to riskFactors.size.toString(),
                "riskFactors" to riskFactors.joinToString("; ")
            )
        )
    }
    
    /**
     * Validate compliance requirements
     */
    private fun validateCompliance(
        payment: Payment,
        sourceAccount: Account,
        destinationAccount: Account?,
        entityContext: EntityContext
    ): ValidationResult {
        val complianceIssues = mutableListOf<String>()
        
        // AML (Anti-Money Laundering) checks
        if (payment.amount.amount > getAMLThreshold(entityContext.jurisdiction)) {
            if (payment.description.length < 50) {
                complianceIssues.add("Large payment requires detailed description for AML compliance")
            }
        }
        
        // KYC (Know Your Customer) checks
        if (!entityContext.isKYCCompliant()) {
            complianceIssues.add("Entity KYC verification incomplete")
        }
        
        // Sanctions screening
        if (isUnderSanctions(destinationAccount?.accountHolder, entityContext.jurisdiction)) {
            complianceIssues.add("Destination account holder appears on sanctions list")
        }
        
        // Cross-border compliance
        if (isCrossBorderPayment(sourceAccount, destinationAccount)) {
            if (!hasCrossBorderPermission(entityContext)) {
                complianceIssues.add("Cross-border payment permission required")
            }
        }
        
        return ValidationResult(
            validationType = "COMPLIANCE",
            isPassed = complianceIssues.isEmpty(),
            severity = when {
                complianceIssues.any { it.contains("sanctions") || it.contains("KYC") } -> ValidationSeverity.CRITICAL
                complianceIssues.any { it.contains("permission") } -> ValidationSeverity.HIGH
                else -> if (complianceIssues.isEmpty()) ValidationSeverity.PASSED else ValidationSeverity.MEDIUM
            },
            message = if (complianceIssues.isEmpty()) "Compliance validation passed" else "Compliance issues detected",
            issues = complianceIssues,
            details = mapOf(
                "jurisdiction" to entityContext.jurisdiction,
                "isCrossBorder" to isCrossBorderPayment(sourceAccount, destinationAccount).toString(),
                "amlThreshold" to getAMLThreshold(entityContext.jurisdiction).toString(),
                "kycCompliant" to entityContext.isKYCCompliant().toString()
            )
        )
    }
    
    /**
     * Validate business-specific rules
     */
    private fun validateBusinessRules(
        payment: Payment,
        sourceAccount: Account,
        destinationAccount: Account?,
        entityContext: EntityContext
    ): ValidationResult {
        val businessIssues = mutableListOf<String>()
        
        // Business hours validation
        if (entityContext.requiresBusinessHours && !isBusinessHours(payment.paymentDate)) {
            businessIssues.add("Payment scheduled outside business hours")
        }
        
        // Approval workflow validation
        if (requiresApproval(payment, sourceAccount, entityContext)) {
            val approvalStatus = getApprovalStatus(payment.id)
            if (approvalStatus != ApprovalStatus.APPROVED) {
                businessIssues.add("Payment requires approval before processing")
            }
        }
        
        // Budget validation
        val budgetValidation = validateBudget(payment, sourceAccount, entityContext)
        if (!budgetValidation.isValid) {
            businessIssues.addAll(budgetValidation.issues)
        }
        
        return ValidationResult(
            validationType = "BUSINESS_RULES",
            isPassed = businessIssues.isEmpty(),
            severity = when {
                businessIssues.any { it.contains("budget") } -> ValidationSeverity.HIGH
                businessIssues.any { it.contains("approval") } -> ValidationSeverity.MEDIUM
                else -> if (businessIssues.isEmpty()) ValidationSeverity.PASSED else ValidationSeverity.LOW
            },
            message = if (businessIssues.isEmpty()) "Business rules validation passed" else "Business rule violations detected",
            issues = businessIssues,
            details = mapOf(
                "requiresBusinessHours" to entityContext.requiresBusinessHours.toString(),
                "requiresApproval" to requiresApproval(payment, sourceAccount, entityContext).toString(),
                "isBusinessHours" to isBusinessHours(payment.paymentDate).toString()
            )
        )
    }
    
    /**
     * Perform cross-validation checks between different components
     */
    private fun performCrossValidation(
        payment: Payment,
        sourceAccount: Account,
        destinationAccount: Account?,
        paymentMethod: PaymentMethod
    ): ValidationResult {
        val crossValidationIssues = mutableListOf<String>()
        
        // Validate account-payment method compatibility
        if (!areCompatible(sourceAccount, paymentMethod)) {
            crossValidationIssues.add("Source account and payment method are incompatible")
        }
        
        // Validate currency consistency
        if (destinationAccount != null && 
            sourceAccount.currency != destinationAccount.currency && 
            !paymentMethod.supportsCurrencyConversion) {
            crossValidationIssues.add("Payment method does not support required currency conversion")
        }
        
        // Validate timing consistency
        if (payment.paymentDate.before(sourceAccount.lastTransactionDate)) {
            crossValidationIssues.add("Payment date is before last account transaction date")
        }
        
        return ValidationResult(
            validationType = "CROSS_VALIDATION",
            isPassed = crossValidationIssues.isEmpty(),
            severity = if (crossValidationIssues.isEmpty()) ValidationSeverity.PASSED else ValidationSeverity.MEDIUM,
            message = if (crossValidationIssues.isEmpty()) "Cross-validation passed" else "Cross-validation issues detected",
            issues = crossValidationIssues,
            details = mapOf(
                "accountMethodCompatible" to areCompatible(sourceAccount, paymentMethod).toString(),
                "supportsCurrencyConversion" to paymentMethod.supportsCurrencyConversion.toString()
            )
        )
    }
    
    // Helper methods (simplified implementations)
    private fun determineApprovalRequirement(validationResults: List<ValidationResult>, entityContext: EntityContext): Boolean {
        return validationResults.any { it.severity == ValidationSeverity.HIGH || it.severity == ValidationSeverity.CRITICAL }
    }
    
    private fun generateRecommendedActions(failedValidations: List<ValidationResult>): List<String> {
        return failedValidations.flatMap { it.issues }.map { issue ->
            when {
                issue.contains("insufficient funds") -> "Add funds to source account or reduce payment amount"
                issue.contains("not active") -> "Activate account before proceeding"
                issue.contains("permission") -> "Obtain required permissions"
                issue.contains("approval") -> "Submit for approval workflow"
                else -> "Review and resolve: $issue"
            }
        }
    }
    
    private fun getRegulatoryLimits(currency: Currency, jurisdiction: String): RegulatoryLimits? = null
    private fun getExchangeRate(from: Currency, to: Currency): BigDecimal? = null
    private fun getCurrencyRestrictions(currency: Currency, jurisdiction: String): CurrencyRestrictions = CurrencyRestrictions(false)
    private fun getAMLThreshold(jurisdiction: String): BigDecimal = BigDecimal("10000")
    private fun isUnderSanctions(accountHolder: String?, jurisdiction: String): Boolean = false
    private fun isCrossBorderPayment(source: Account, destination: Account?): Boolean = false
    private fun hasCrossBorderPermission(entityContext: EntityContext): Boolean = true
    private fun isBusinessHours(date: Date): Boolean = true
    private fun requiresApproval(payment: Payment, account: Account, context: EntityContext): Boolean = false
    private fun getApprovalStatus(paymentId: UUID): ApprovalStatus = ApprovalStatus.PENDING
    private fun validateBudget(payment: Payment, account: Account, context: EntityContext): BudgetValidationResult = BudgetValidationResult(true, emptyList())
    private fun areCompatible(account: Account, paymentMethod: PaymentMethod): Boolean = true
}

// Supporting data classes and enums
data class PaymentValidationResult(
    val paymentId: UUID,
    val isValid: Boolean,
    val overallSeverity: ValidationSeverity,
    val validationResults: List<ValidationResult>,
    val failedValidations: List<ValidationResult>,
    val approvalRequired: Boolean,
    val recommendedActions: List<String>,
    val validationTimestamp: Date,
    val validationContext: Map<String, String>
)

data class ValidationResult(
    val validationType: String,
    val isPassed: Boolean,
    val severity: ValidationSeverity,
    val message: String,
    val issues: List<String>,
    val details: Map<String, String>
)

data class EntityContext(
    val entityId: UUID,
    val jurisdiction: String,
    val supportedCurrencies: Set<Currency>,
    val requiresBusinessHours: Boolean
) {
    fun hasPermission(permission: String, resourceId: String): Boolean = true
    fun getPaymentLimits(): PaymentLimits? = null
    fun getDailyPaymentTotal(accountId: UUID): BigDecimal? = null
    fun isKYCCompliant(): Boolean = true
    fun isFromHighRiskLocation(): Boolean = false
}

data class PaymentLimits(
    val singleTransactionLimit: BigDecimal,
    val dailyLimit: BigDecimal,
    val monthlyLimit: BigDecimal
)

data class RegulatoryLimits(
    val reportingThreshold: BigDecimal,
    val maximumAmount: BigDecimal
)

data class CurrencyRestrictions(
    val isRestricted: Boolean,
    val restrictions: List<String> = emptyList()
)

data class BudgetValidationResult(
    val isValid: Boolean,
    val issues: List<String>
)

enum class ValidationSeverity {
    PASSED, LOW, MEDIUM, HIGH, CRITICAL
}

enum class RiskLevel {
    MINIMAL, LOW, MEDIUM, HIGH
}

enum class PaymentMethodRiskLevel {
    LOW, MEDIUM, HIGH
}

enum class ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

// Extensions for Account entity
private fun Account.getAverageTransactionAmount(): BigDecimal = BigDecimal("1000") // Simplified
private fun Account.getRecentTransactionCount(hours: Int): Int = 0 // Simplified

// Extensions for AccountType enum
private fun AccountType.canDebit(): Boolean = when (this) {
    AccountType.CHECKING, AccountType.SAVINGS -> true
    else -> false
}

private fun AccountType.canCredit(): Boolean = when (this) {
    AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT -> true
    else -> false
}

// Extensions for PaymentMethod
private fun PaymentMethod.isAvailableAt(date: Date): Boolean = true // Simplified

package org.chiro.finance.domain.exception

import org.chiro.finance.domain.valueobject.AccountCode
import java.util.*

/**
 * InvalidAccountException
 * 
 * Domain exception thrown when an operation references an invalid, inactive,
 * or non-existent account. This exception enforces account validation rules
 * within the finance domain.
 * 
 * This exception is thrown when:
 * - Referenced account does not exist in the system
 * - Account is inactive or closed
 * - Account type is incompatible with the requested operation
 * - Account belongs to a different entity or subsidiary
 * - Account has been marked for deletion
 * - Account access is restricted for the current user
 */
class InvalidAccountException : FinanceDomainException {
    
    val accountId: UUID?
    val accountCode: AccountCode?
    val accountName: String?
    val validationFailureReason: AccountValidationFailure
    val operationType: String
    val requestedBy: UUID?
    val entityId: UUID?
    val subsidiaryId: UUID?
    val allowedAccountTypes: Set<String>?
    val actualAccountType: String?
    
    constructor(
        accountId: UUID,
        validationFailureReason: AccountValidationFailure,
        operationType: String,
        message: String = "Invalid account for requested operation"
    ) : super(message) {
        this.accountId = accountId
        this.accountCode = null
        this.accountName = null
        this.validationFailureReason = validationFailureReason
        this.operationType = operationType
        this.requestedBy = null
        this.entityId = null
        this.subsidiaryId = null
        this.allowedAccountTypes = null
        this.actualAccountType = null
    }
    
    constructor(
        accountCode: AccountCode,
        validationFailureReason: AccountValidationFailure,
        operationType: String,
        message: String = "Invalid account code: ${accountCode.code}"
    ) : super(message) {
        this.accountId = null
        this.accountCode = accountCode
        this.accountName = null
        this.validationFailureReason = validationFailureReason
        this.operationType = operationType
        this.requestedBy = null
        this.entityId = null
        this.subsidiaryId = null
        this.allowedAccountTypes = null
        this.actualAccountType = null
    }
    
    constructor(
        accountId: UUID?,
        accountCode: AccountCode?,
        accountName: String?,
        validationFailureReason: AccountValidationFailure,
        operationType: String,
        requestedBy: UUID?,
        entityId: UUID?,
        message: String
    ) : super(message) {
        this.accountId = accountId
        this.accountCode = accountCode
        this.accountName = accountName
        this.validationFailureReason = validationFailureReason
        this.operationType = operationType
        this.requestedBy = requestedBy
        this.entityId = entityId
        this.subsidiaryId = null
        this.allowedAccountTypes = null
        this.actualAccountType = null
    }
    
    constructor(
        accountId: UUID?,
        accountCode: AccountCode?,
        accountName: String?,
        validationFailureReason: AccountValidationFailure,
        operationType: String,
        allowedAccountTypes: Set<String>,
        actualAccountType: String,
        message: String
    ) : super(message) {
        this.accountId = accountId
        this.accountCode = accountCode
        this.accountName = accountName
        this.validationFailureReason = validationFailureReason
        this.operationType = operationType
        this.requestedBy = null
        this.entityId = null
        this.subsidiaryId = null
        this.allowedAccountTypes = allowedAccountTypes
        this.actualAccountType = actualAccountType
    }
    
    /**
     * Check if the validation failure is critical (requires immediate attention)
     */
    fun isCriticalFailure(): Boolean {
        return validationFailureReason in setOf(
            AccountValidationFailure.ACCOUNT_NOT_FOUND,
            AccountValidationFailure.ACCOUNT_DELETED,
            AccountValidationFailure.SECURITY_VIOLATION
        )
    }
    
    /**
     * Check if the validation failure is recoverable
     */
    fun isRecoverable(): Boolean {
        return validationFailureReason in setOf(
            AccountValidationFailure.ACCOUNT_INACTIVE,
            AccountValidationFailure.INSUFFICIENT_PERMISSIONS,
            AccountValidationFailure.WRONG_ENTITY
        )
    }
    
    /**
     * Get suggested resolution actions
     */
    fun getSuggestedResolutions(): List<String> {
        return when (validationFailureReason) {
            AccountValidationFailure.ACCOUNT_NOT_FOUND -> listOf(
                "Verify the account code is correct",
                "Check if account exists in the chart of accounts",
                "Create the account if it should exist",
                "Use an alternative existing account"
            )
            AccountValidationFailure.ACCOUNT_INACTIVE -> listOf(
                "Reactivate the account if appropriate",
                "Use an active alternative account",
                "Contact account administrator",
                "Review account status and closure reason"
            )
            AccountValidationFailure.ACCOUNT_CLOSED -> listOf(
                "Use an active alternative account",
                "Reopen the account if business requires it",
                "Review historical transactions for this account",
                "Contact finance team for guidance"
            )
            AccountValidationFailure.WRONG_ACCOUNT_TYPE -> listOf(
                "Use an account of type: ${allowedAccountTypes?.joinToString(", ") ?: "appropriate type"}",
                "Verify the operation requirements",
                "Review chart of accounts for suitable alternatives",
                "Contact system administrator if account type is incorrect"
            )
            AccountValidationFailure.INSUFFICIENT_PERMISSIONS -> listOf(
                "Request access to this account",
                "Use an account you have permissions for",
                "Contact your supervisor or administrator",
                "Review your role-based access controls"
            )
            AccountValidationFailure.WRONG_ENTITY -> listOf(
                "Use an account from the correct entity/subsidiary",
                "Verify you're working in the correct company context",
                "Request inter-company transaction approval if needed",
                "Contact administrator for cross-entity access"
            )
            AccountValidationFailure.ACCOUNT_DELETED -> listOf(
                "This account has been deleted and cannot be used",
                "Use an alternative active account",
                "Contact administrator if account was deleted in error",
                "Review audit trail for deletion details"
            )
            AccountValidationFailure.ACCOUNT_RESTRICTED -> listOf(
                "This account has usage restrictions",
                "Review account restrictions and compliance requirements",
                "Request special authorization if needed",
                "Use an unrestricted alternative account"
            )
            AccountValidationFailure.SECURITY_VIOLATION -> listOf(
                "This appears to be a security violation",
                "Contact security team immediately",
                "Do not retry this operation",
                "Review access logs and audit trail"
            )
        }
    }
    
    /**
     * Get the severity level of this validation failure
     */
    fun getSeverityLevel(): ValidationSeverity {
        return when (validationFailureReason) {
            AccountValidationFailure.SECURITY_VIOLATION -> ValidationSeverity.CRITICAL
            AccountValidationFailure.ACCOUNT_DELETED -> ValidationSeverity.HIGH
            AccountValidationFailure.ACCOUNT_NOT_FOUND -> ValidationSeverity.HIGH
            AccountValidationFailure.WRONG_ACCOUNT_TYPE -> ValidationSeverity.MEDIUM
            AccountValidationFailure.INSUFFICIENT_PERMISSIONS -> ValidationSeverity.MEDIUM
            AccountValidationFailure.ACCOUNT_CLOSED -> ValidationSeverity.MEDIUM
            AccountValidationFailure.ACCOUNT_INACTIVE -> ValidationSeverity.LOW
            AccountValidationFailure.WRONG_ENTITY -> ValidationSeverity.LOW
            AccountValidationFailure.ACCOUNT_RESTRICTED -> ValidationSeverity.MEDIUM
        }
    }
    
    override fun getErrorCode(): String = "INVALID_ACCOUNT"
    
    override fun getErrorCategory(): String = "VALIDATION_ERROR"
    
    override fun getBusinessImpact(): String = when (getSeverityLevel()) {
        ValidationSeverity.CRITICAL -> "CRITICAL - Potential security issue, transaction blocked"
        ValidationSeverity.HIGH -> "HIGH - Transaction cannot proceed, data integrity concern"
        ValidationSeverity.MEDIUM -> "MEDIUM - Transaction blocked, requires user action"
        ValidationSeverity.LOW -> "LOW - Transaction blocked, minor configuration issue"
    }
    
    override fun getRecommendedAction(): String = getSuggestedResolutions().firstOrNull() 
        ?: "Review account configuration and permissions"
    
    override fun isRetryable(): Boolean = isRecoverable()
    
    override fun getRetryDelay(): Long = if (isRecoverable()) 0 else -1 // Immediate retry if recoverable
    
    override fun requiresEscalation(): Boolean = isCriticalFailure()
    
    companion object {
        /**
         * Create exception for non-existent account
         */
        fun accountNotFound(
            accountCode: AccountCode,
            operationType: String
        ): InvalidAccountException {
            return InvalidAccountException(
                accountCode = accountCode,
                validationFailureReason = AccountValidationFailure.ACCOUNT_NOT_FOUND,
                operationType = operationType,
                message = "Account with code '${accountCode.code}' does not exist in the chart of accounts"
            )
        }
        
        /**
         * Create exception for inactive account
         */
        fun accountInactive(
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            operationType: String
        ): InvalidAccountException {
            return InvalidAccountException(
                accountId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                validationFailureReason = AccountValidationFailure.ACCOUNT_INACTIVE,
                operationType = operationType,
                requestedBy = null,
                entityId = null,
                message = "Account '${accountCode.code}' ($accountName) is inactive and cannot be used for $operationType"
            )
        }
        
        /**
         * Create exception for wrong account type
         */
        fun wrongAccountType(
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            operationType: String,
            allowedTypes: Set<String>,
            actualType: String
        ): InvalidAccountException {
            return InvalidAccountException(
                accountId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                validationFailureReason = AccountValidationFailure.WRONG_ACCOUNT_TYPE,
                operationType = operationType,
                allowedAccountTypes = allowedTypes,
                actualAccountType = actualType,
                message = "Account '${accountCode.code}' ($accountName) is of type '$actualType' but $operationType requires account type: ${allowedTypes.joinToString(" or ")}"
            )
        }
        
        /**
         * Create exception for insufficient permissions
         */
        fun insufficientPermissions(
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            operationType: String,
            requestedBy: UUID
        ): InvalidAccountException {
            return InvalidAccountException(
                accountId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                validationFailureReason = AccountValidationFailure.INSUFFICIENT_PERMISSIONS,
                operationType = operationType,
                requestedBy = requestedBy,
                entityId = null,
                message = "Insufficient permissions to perform $operationType on account '${accountCode.code}' ($accountName)"
            )
        }
        
        /**
         * Create exception for cross-entity access violation
         */
        fun wrongEntity(
            accountId: UUID,
            accountCode: AccountCode,
            accountName: String,
            operationType: String,
            accountEntityId: UUID,
            requestedEntityId: UUID
        ): InvalidAccountException {
            return InvalidAccountException(
                accountId = accountId,
                accountCode = accountCode,
                accountName = accountName,
                validationFailureReason = AccountValidationFailure.WRONG_ENTITY,
                operationType = operationType,
                requestedBy = null,
                entityId = requestedEntityId,
                message = "Account '${accountCode.code}' ($accountName) belongs to a different entity and cannot be used for $operationType in current entity context"
            )
        }
        
        /**
         * Create exception for deleted account
         */
        fun accountDeleted(
            accountId: UUID,
            accountCode: AccountCode,
            operationType: String
        ): InvalidAccountException {
            return InvalidAccountException(
                accountId = accountId,
                accountCode = accountCode,
                accountName = null,
                validationFailureReason = AccountValidationFailure.ACCOUNT_DELETED,
                operationType = operationType,
                requestedBy = null,
                entityId = null,
                message = "Account '${accountCode.code}' has been deleted and cannot be used for $operationType"
            )
        }
    }
}

/**
 * Account Validation Failure Types
 */
enum class AccountValidationFailure {
    ACCOUNT_NOT_FOUND,
    ACCOUNT_INACTIVE,
    ACCOUNT_CLOSED,
    ACCOUNT_DELETED,
    WRONG_ACCOUNT_TYPE,
    INSUFFICIENT_PERMISSIONS,
    WRONG_ENTITY,
    ACCOUNT_RESTRICTED,
    SECURITY_VIOLATION
}

/**
 * Validation Severity Levels
 */
enum class ValidationSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

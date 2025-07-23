package org.chiro.finance.domain.exception

import java.util.*

/**
 * FinanceDomainException
 * 
 * Base abstract class for all domain exceptions in the finance module.
 * This class provides common functionality and contracts for finance-specific
 * business rule violations and domain constraint failures.
 * 
 * All finance domain exceptions should extend this class to ensure consistent
 * error handling, logging, and recovery mechanisms across the domain.
 */
abstract class FinanceDomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    val exceptionId: UUID = UUID.randomUUID()
    val timestamp: Long = System.currentTimeMillis()
    val domainContext: String = "FINANCE"
    
    /**
     * Get the specific error code for this exception
     */
    abstract fun getErrorCode(): String
    
    /**
     * Get the category of this error (e.g., VALIDATION_ERROR, BUSINESS_RULE_VIOLATION)
     */
    abstract fun getErrorCategory(): String
    
    /**
     * Get the business impact description of this exception
     */
    abstract fun getBusinessImpact(): String
    
    /**
     * Get the recommended action to resolve this exception
     */
    abstract fun getRecommendedAction(): String
    
    /**
     * Determine if this exception represents a retryable operation
     */
    abstract fun isRetryable(): Boolean
    
    /**
     * Get the recommended delay before retry (in milliseconds)
     * Returns -1 if not retryable
     */
    abstract fun getRetryDelay(): Long
    
    /**
     * Determine if this exception requires escalation to management/support
     */
    abstract fun requiresEscalation(): Boolean
    
    /**
     * Get additional context information for logging and debugging
     */
    open fun getContextInformation(): Map<String, Any> {
        return mapOf(
            "exceptionId" to exceptionId,
            "timestamp" to timestamp,
            "domainContext" to domainContext,
            "errorCode" to getErrorCode(),
            "errorCategory" to getErrorCategory(),
            "businessImpact" to getBusinessImpact(),
            "recommendedAction" to getRecommendedAction(),
            "isRetryable" to isRetryable(),
            "retryDelay" to getRetryDelay(),
            "requiresEscalation" to requiresEscalation()
        )
    }
    
    /**
     * Get severity level of this exception
     */
    open fun getSeverityLevel(): ExceptionSeverity {
        return when {
            requiresEscalation() -> ExceptionSeverity.CRITICAL
            !isRetryable() -> ExceptionSeverity.HIGH
            getBusinessImpact().startsWith("HIGH") -> ExceptionSeverity.HIGH
            getBusinessImpact().startsWith("MEDIUM") -> ExceptionSeverity.MEDIUM
            else -> ExceptionSeverity.LOW
        }
    }
    
    /**
     * Check if this exception affects financial data integrity
     */
    open fun affectsDataIntegrity(): Boolean {
        return getErrorCategory() in setOf(
            "BUSINESS_RULE_VIOLATION",
            "DATA_CONSISTENCY_ERROR",
            "VALIDATION_ERROR"
        )
    }
    
    /**
     * Check if this exception affects compliance or audit requirements
     */
    open fun affectsCompliance(): Boolean {
        return getSeverityLevel() in setOf(
            ExceptionSeverity.HIGH,
            ExceptionSeverity.CRITICAL
        ) || affectsDataIntegrity()
    }
    
    /**
     * Get the exception as a structured error response
     */
    fun toErrorResponse(): FinanceErrorResponse {
        return FinanceErrorResponse(
            errorId = exceptionId.toString(),
            errorCode = getErrorCode(),
            errorCategory = getErrorCategory(),
            message = message ?: "Unknown finance domain error",
            businessImpact = getBusinessImpact(),
            recommendedAction = getRecommendedAction(),
            severity = getSeverityLevel().name,
            timestamp = timestamp,
            isRetryable = isRetryable(),
            retryDelayMs = if (isRetryable()) getRetryDelay() else null,
            requiresEscalation = requiresEscalation(),
            affectsDataIntegrity = affectsDataIntegrity(),
            affectsCompliance = affectsCompliance(),
            contextInformation = getContextInformation()
        )
    }
    
    /**
     * Create audit log entry for this exception
     */
    fun createAuditLogEntry(
        userId: UUID?,
        operationType: String?,
        entityId: UUID?,
        entityType: String?
    ): ExceptionAuditEntry {
        return ExceptionAuditEntry(
            auditId = UUID.randomUUID(),
            exceptionId = exceptionId,
            exceptionType = this::class.simpleName ?: "Unknown",
            errorCode = getErrorCode(),
            errorCategory = getErrorCategory(),
            message = message ?: "",
            severity = getSeverityLevel(),
            timestamp = timestamp,
            userId = userId,
            operationType = operationType,
            entityId = entityId,
            entityType = entityType,
            businessImpact = getBusinessImpact(),
            affectsDataIntegrity = affectsDataIntegrity(),
            affectsCompliance = affectsCompliance(),
            requiresEscalation = requiresEscalation(),
            contextInformation = getContextInformation()
        )
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(id=$exceptionId, code=${getErrorCode()}, message='$message')"
    }
}

/**
 * Exception Severity Levels
 */
enum class ExceptionSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Structured Error Response for Finance Domain
 */
data class FinanceErrorResponse(
    val errorId: String,
    val errorCode: String,
    val errorCategory: String,
    val message: String,
    val businessImpact: String,
    val recommendedAction: String,
    val severity: String,
    val timestamp: Long,
    val isRetryable: Boolean,
    val retryDelayMs: Long?,
    val requiresEscalation: Boolean,
    val affectsDataIntegrity: Boolean,
    val affectsCompliance: Boolean,
    val contextInformation: Map<String, Any>
)

/**
 * Audit Log Entry for Exception Tracking
 */
data class ExceptionAuditEntry(
    val auditId: UUID,
    val exceptionId: UUID,
    val exceptionType: String,
    val errorCode: String,
    val errorCategory: String,
    val message: String,
    val severity: ExceptionSeverity,
    val timestamp: Long,
    val userId: UUID?,
    val operationType: String?,
    val entityId: UUID?,
    val entityType: String?,
    val businessImpact: String,
    val affectsDataIntegrity: Boolean,
    val affectsCompliance: Boolean,
    val requiresEscalation: Boolean,
    val contextInformation: Map<String, Any>
) {
    /**
     * Check if this audit entry requires immediate attention
     */
    fun requiresImmediateAttention(): Boolean {
        return severity == ExceptionSeverity.CRITICAL || 
               requiresEscalation || 
               affectsCompliance
    }
    
    /**
     * Get priority level for processing this audit entry
     */
    fun getPriorityLevel(): AuditPriority {
        return when {
            requiresImmediateAttention() -> AuditPriority.URGENT
            severity == ExceptionSeverity.HIGH -> AuditPriority.HIGH
            affectsDataIntegrity -> AuditPriority.MEDIUM
            else -> AuditPriority.NORMAL
        }
    }
}

/**
 * Audit Priority Levels
 */
enum class AuditPriority {
    NORMAL,
    MEDIUM,
    HIGH,
    URGENT
}

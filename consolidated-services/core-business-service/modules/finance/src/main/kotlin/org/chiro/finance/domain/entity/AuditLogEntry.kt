package org.chiro.finance.domain.entity

import org.chiro.finance.domain.valueobject.AccountId
import org.chiro.finance.domain.valueobject.Currency
import org.chiro.finance.domain.valueobject.FinancialAmount
import org.chiro.finance.domain.valueobject.TransactionType
import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 * Audit Log Entry - Financial transaction audit trail
 * 
 * Provides comprehensive audit capabilities for financial operations including:
 * - Complete audit trail for all financial transactions
 * - User action tracking and accountability
 * - Change history with before/after values
 * - Compliance and regulatory reporting
 * - Security and fraud detection capabilities
 * 
 * Key Features:
 * - Immutable audit records for data integrity
 * - Comprehensive change tracking
 * - User and system action logging
 * - Regulatory compliance support
 * - Performance optimized queries
 * 
 * Business Rules:
 * - Audit entries are immutable once created
 * - All financial operations must be audited
 * - Audit data retention follows regulatory requirements
 * - Critical operations require enhanced audit details
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Serializable
data class AuditLogEntry(
    val id: UUID = UUID.randomUUID(),
    
    @field:NotNull(message = "Entity type is required")
    @field:Size(min = 1, max = 100, message = "Entity type must be between 1 and 100 characters")
    val entityType: String,
    
    @field:NotNull(message = "Entity ID is required")
    val entityId: UUID,
    
    @field:NotNull(message = "Action type is required")
    val actionType: AuditActionType,
    
    @field:NotNull(message = "User ID is required")
    val userId: UUID,
    
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
    val username: String,
    
    @field:NotNull(message = "Timestamp is required")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    val oldValues: Map<String, Any?>? = null,
    val newValues: Map<String, Any?>? = null,
    
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,
    
    @field:Size(max = 45, message = "IP address cannot exceed 45 characters")
    val ipAddress: String? = null,
    
    @field:Size(max = 500, message = "User agent cannot exceed 500 characters")
    val userAgent: String? = null,
    
    val sessionId: String? = null,
    val transactionId: UUID? = null,
    
    @field:NotNull(message = "Severity level is required")
    val severityLevel: AuditSeverity = AuditSeverity.INFO,
    
    val metadata: Map<String, Any> = emptyMap(),
    
    val isSystemGenerated: Boolean = false,
    val complianceFlags: Set<ComplianceFlag> = emptySet()
) {
    
    init {
        require(entityType.isNotBlank()) { "Entity type cannot be blank" }
        require(username.isNotBlank()) { "Username cannot be blank" }
        
        // Validate change tracking for update operations
        if (actionType == AuditActionType.UPDATE) {
            require(oldValues != null || newValues != null) { 
                "Update operations must include old or new values" 
            }
        }
        
        // Validate high-severity entries have sufficient detail
        if (severityLevel in listOf(AuditSeverity.HIGH, AuditSeverity.CRITICAL)) {
            require(!description.isNullOrBlank()) { 
                "High severity audit entries must include description" 
            }
        }
    }
    
    /**
     * Creates detailed change summary for audit reporting
     */
    fun getChangesSummary(): String {
        return when (actionType) {
            AuditActionType.CREATE -> "Created $entityType"
            AuditActionType.UPDATE -> buildString {
                append("Updated $entityType")
                if (oldValues != null && newValues != null) {
                    val changes = getFieldChanges()
                    if (changes.isNotEmpty()) {
                        append(": ${changes.joinToString(", ")}")
                    }
                }
            }
            AuditActionType.DELETE -> "Deleted $entityType"
            AuditActionType.READ -> "Accessed $entityType"
            AuditActionType.EXPORT -> "Exported $entityType data"
            AuditActionType.IMPORT -> "Imported $entityType data"
            AuditActionType.LOGIN -> "User login"
            AuditActionType.LOGOUT -> "User logout"
            AuditActionType.PERMISSION_CHANGE -> "Permission modified for $entityType"
            AuditActionType.SECURITY_EVENT -> "Security event: ${description ?: "Unknown"}"
        }
    }
    
    /**
     * Gets detailed field-level changes between old and new values
     */
    fun getFieldChanges(): List<String> {
        if (oldValues == null || newValues == null) return emptyList()
        
        val changes = mutableListOf<String>()
        val allKeys = (oldValues.keys + newValues.keys).toSet()
        
        for (key in allKeys) {
            val oldValue = oldValues[key]
            val newValue = newValues[key]
            
            when {
                oldValue == null && newValue != null -> 
                    changes.add("$key: added '$newValue'")
                oldValue != null && newValue == null -> 
                    changes.add("$key: removed '$oldValue'")
                oldValue != newValue -> 
                    changes.add("$key: '$oldValue' → '$newValue'")
            }
        }
        
        return changes
    }
    
    /**
     * Checks if this audit entry indicates a suspicious activity
     */
    fun isSuspicious(): Boolean {
        return severityLevel == AuditSeverity.CRITICAL ||
               actionType == AuditActionType.SECURITY_EVENT ||
               complianceFlags.any { it.isSecurity }
    }
    
    /**
     * Gets compliance status for regulatory reporting
     */
    fun getComplianceStatus(): ComplianceStatus {
        return when {
            complianceFlags.any { it == ComplianceFlag.SOX_VIOLATION } -> 
                ComplianceStatus.SOX_VIOLATION
            complianceFlags.any { it == ComplianceFlag.GDPR_CONCERN } -> 
                ComplianceStatus.GDPR_CONCERN
            complianceFlags.any { it.isCritical } -> 
                ComplianceStatus.COMPLIANCE_VIOLATION
            complianceFlags.isNotEmpty() -> 
                ComplianceStatus.REQUIRES_REVIEW
            else -> 
                ComplianceStatus.COMPLIANT
        }
    }
    
    /**
     * Creates risk assessment based on audit data
     */
    fun getRiskAssessment(): RiskLevel {
        return when {
            severityLevel == AuditSeverity.CRITICAL -> RiskLevel.CRITICAL
            isSuspicious() -> RiskLevel.HIGH
            severityLevel == AuditSeverity.HIGH -> RiskLevel.MEDIUM
            complianceFlags.isNotEmpty() -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
    
    companion object {
        /**
         * Creates audit entry for entity creation
         */
        fun forCreate(
            entityType: String,
            entityId: UUID,
            userId: UUID,
            username: String,
            newValues: Map<String, Any?>,
            description: String? = null
        ): AuditLogEntry {
            return AuditLogEntry(
                entityType = entityType,
                entityId = entityId,
                actionType = AuditActionType.CREATE,
                userId = userId,
                username = username,
                newValues = newValues,
                description = description
            )
        }
        
        /**
         * Creates audit entry for entity updates
         */
        fun forUpdate(
            entityType: String,
            entityId: UUID,
            userId: UUID,
            username: String,
            oldValues: Map<String, Any?>,
            newValues: Map<String, Any?>,
            description: String? = null
        ): AuditLogEntry {
            return AuditLogEntry(
                entityType = entityType,
                entityId = entityId,
                actionType = AuditActionType.UPDATE,
                userId = userId,
                username = username,
                oldValues = oldValues,
                newValues = newValues,
                description = description
            )
        }
        
        /**
         * Creates audit entry for entity deletion
         */
        fun forDelete(
            entityType: String,
            entityId: UUID,
            userId: UUID,
            username: String,
            oldValues: Map<String, Any?>,
            description: String? = null
        ): AuditLogEntry {
            return AuditLogEntry(
                entityType = entityType,
                entityId = entityId,
                actionType = AuditActionType.DELETE,
                userId = userId,
                username = username,
                oldValues = oldValues,
                description = description,
                severityLevel = AuditSeverity.HIGH
            )
        }
        
        /**
         * Creates security-related audit entry
         */
        fun forSecurityEvent(
            entityType: String,
            entityId: UUID,
            userId: UUID,
            username: String,
            description: String,
            ipAddress: String? = null,
            complianceFlags: Set<ComplianceFlag> = emptySet()
        ): AuditLogEntry {
            return AuditLogEntry(
                entityType = entityType,
                entityId = entityId,
                actionType = AuditActionType.SECURITY_EVENT,
                userId = userId,
                username = username,
                description = description,
                ipAddress = ipAddress,
                severityLevel = AuditSeverity.CRITICAL,
                complianceFlags = complianceFlags
            )
        }
    }
}

/**
 * Audit action types for different operations
 */
@Serializable
enum class AuditActionType(
    val description: String,
    val requiresApproval: Boolean = false
) {
    CREATE("Entity Created"),
    UPDATE("Entity Updated"),
    DELETE("Entity Deleted", requiresApproval = true),
    READ("Entity Accessed"),
    EXPORT("Data Exported", requiresApproval = true),
    IMPORT("Data Imported", requiresApproval = true),
    LOGIN("User Login"),
    LOGOUT("User Logout"),
    PERMISSION_CHANGE("Permission Changed", requiresApproval = true),
    SECURITY_EVENT("Security Event", requiresApproval = true);
    
    val isHighRisk: Boolean
        get() = requiresApproval || this in listOf(SECURITY_EVENT, DELETE, EXPORT)
}

/**
 * Audit severity levels for risk assessment
 */
@Serializable
enum class AuditSeverity(
    val level: Int,
    val description: String
) {
    LOW(1, "Low Priority"),
    INFO(2, "Informational"),
    MEDIUM(3, "Medium Priority"),
    HIGH(4, "High Priority"),
    CRITICAL(5, "Critical Priority");
    
    val isElevated: Boolean
        get() = level >= HIGH.level
}

/**
 * Compliance flags for regulatory requirements
 */
@Serializable
enum class ComplianceFlag(
    val description: String,
    val isSecurity: Boolean = false,
    val isCritical: Boolean = false
) {
    SOX_COMPLIANCE("Sarbanes-Oxley Compliance", isCritical = true),
    SOX_VIOLATION("SOX Compliance Violation", isSecurity = true, isCritical = true),
    GDPR_COMPLIANCE("GDPR Compliance"),
    GDPR_CONCERN("GDPR Privacy Concern", isSecurity = true),
    PCI_COMPLIANCE("PCI DSS Compliance", isSecurity = true),
    HIPAA_COMPLIANCE("HIPAA Compliance", isSecurity = true),
    FINANCIAL_REPORTING("Financial Reporting Requirement"),
    TAX_COMPLIANCE("Tax Compliance Requirement"),
    INTERNAL_CONTROL("Internal Control Requirement"),
    SEGREGATION_DUTY("Segregation of Duties", isCritical = true),
    AUTHORIZATION_BREACH("Authorization Breach", isSecurity = true, isCritical = true),
    DATA_BREACH("Data Breach", isSecurity = true, isCritical = true);
    
    val requiresReview: Boolean
        get() = isSecurity || isCritical
}

/**
 * Overall compliance status assessment
 */
@Serializable
enum class ComplianceStatus(
    val description: String,
    val isViolation: Boolean = false
) {
    COMPLIANT("Fully Compliant"),
    REQUIRES_REVIEW("Requires Compliance Review"),
    GDPR_CONCERN("GDPR Privacy Concern", isViolation = true),
    SOX_VIOLATION("SOX Compliance Violation", isViolation = true),
    COMPLIANCE_VIOLATION("Compliance Violation", isViolation = true);
    
    val needsImmedateAction: Boolean
        get() = isViolation
}

/**
 * Risk level assessment for audit entries
 */
@Serializable
enum class RiskLevel(
    val score: Int,
    val description: String,
    val colorCode: String
) {
    LOW(1, "Low Risk", "#28a745"),
    MEDIUM(2, "Medium Risk", "#ffc107"),
    HIGH(3, "High Risk", "#fd7e14"),
    CRITICAL(4, "Critical Risk", "#dc3545");
    
    val requiresEscalation: Boolean
        get() = score >= HIGH.score
}

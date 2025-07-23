package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDateTime
import java.util.*

/**
 * AuditTrailEvent
 * 
 * Domain event published for all significant financial operations to maintain
 * a comprehensive audit trail. This event ensures compliance with auditing
 * standards and provides detailed tracking of all financial activities.
 * 
 * This event enables:
 * - Comprehensive audit trail maintenance
 * - Compliance monitoring and reporting
 * - Security incident detection
 * - Forensic analysis capabilities
 * - Regulatory compliance validation
 * - Change tracking and accountability
 */
data class AuditTrailEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Audit Trail Entry ID
    val auditTrailId: UUID,
    val sequenceNumber: Long,
    val auditEventType: AuditEventType,
    val auditCategory: AuditCategory,
    val auditSeverity: AuditSeverity,
    val businessProcess: BusinessProcess,
    val subProcess: String?,
    val operationType: OperationType,
    val operationResult: OperationResult,
    val entityType: String, // Type of entity being audited
    val entityId: UUID, // ID of the entity being audited
    val entityName: String?,
    val parentEntityType: String?,
    val parentEntityId: UUID?,
    val userId: UUID,
    val userName: String,
    val userRole: String,
    val userDepartment: String?,
    val sessionId: UUID?,
    val ipAddress: String?,
    val userAgent: String?,
    val clientApplication: String?,
    val operationDescription: String,
    val operationDetails: Map<String, String>,
    val beforeValues: Map<String, String>?,
    val afterValues: Map<String, String>?,
    val changedFields: Set<String>,
    val reasonCode: String?,
    val reasonDescription: String?,
    val approvalRequired: Boolean,
    val approverUserId: UUID?,
    val approverUserName: String?,
    val approvalStatus: ApprovalStatus?,
    val approvalTimestamp: LocalDateTime?,
    val workflowInstanceId: UUID?,
    val workflowStepId: UUID?,
    val transactionId: UUID?,
    val batchId: UUID?,
    val correlationId: UUID?,
    val sourceSystem: String,
    val sourceSystemVersion: String?,
    val integrationRef: String?,
    val dataClassification: DataClassification,
    val sensitivityLevel: SensitivityLevel,
    val complianceFlags: Set<ComplianceFlag>,
    val regulatoryRequirements: Set<RegulatoryRequirement>,
    val retentionPeriod: Int, // Years
    val archiveDate: LocalDateTime?,
    val encryptionApplied: Boolean,
    val digitalSignature: String?,
    val checksumValue: String?,
    val riskLevel: RiskLevel,
    val riskFactors: Set<RiskFactor>,
    val alertTriggers: Set<AlertTrigger>,
    val notificationsSent: Set<NotificationType>,
    val geoLocation: String?,
    val timezone: String,
    val businessContext: String?,
    val errorCode: String?,
    val errorMessage: String?,
    val exceptionDetails: String?,
    val performanceMetrics: PerformanceMetrics?,
    val resourcesAccessed: Set<ResourceAccess>,
    val dataVolume: DataVolume?,
    val qualityIndicators: QualityIndicators?,
    val linkedEvents: Set<UUID>,
    val tags: Set<String> = emptySet(),
    val customAttributes: Map<String, String> = emptyMap(),
    val auditedAt: LocalDateTime = LocalDateTime.now(),
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = auditedAt
    override fun getVersion(): Long = version
    
    /**
     * Check if audit event indicates suspicious activity
     */
    fun indicatesSuspiciousActivity(): Boolean {
        return riskLevel == RiskLevel.VERY_HIGH ||
               riskFactors.any { it in setOf(
                   RiskFactor.UNUSUAL_PATTERN,
                   RiskFactor.UNAUTHORIZED_ACCESS,
                   RiskFactor.PRIVILEGE_ESCALATION,
                   RiskFactor.DATA_EXFILTRATION
               ) } ||
               alertTriggers.isNotEmpty()
    }
    
    /**
     * Check if audit event requires immediate investigation
     */
    fun requiresImmediateInvestigation(): Boolean {
        return auditSeverity == AuditSeverity.CRITICAL ||
               operationResult == OperationResult.SECURITY_VIOLATION ||
               indicatesSuspiciousActivity()
    }
    
    /**
     * Check if audit event affects compliance obligations
     */
    fun affectsComplianceObligations(): Boolean {
        return complianceFlags.isNotEmpty() ||
               regulatoryRequirements.isNotEmpty() ||
               dataClassification in setOf(
                   DataClassification.REGULATED,
                   DataClassification.FINANCIAL,
                   DataClassification.PERSONAL
               )
    }
    
    /**
     * Get audit completeness score (0-100)
     */
    fun getCompletenessScore(): Int {
        var score = 100
        
        // Deduct for missing critical information
        if (beforeValues == null && operationType in setOf(OperationType.UPDATE, OperationType.DELETE)) score -= 20
        if (afterValues == null && operationType in setOf(OperationType.CREATE, OperationType.UPDATE)) score -= 20
        if (reasonDescription.isNullOrBlank() && auditSeverity != AuditSeverity.LOW) score -= 15
        if (approvalRequired && (approverUserId == null || approvalStatus == null)) score -= 25
        if (userDepartment.isNullOrBlank()) score -= 5
        if (businessContext.isNullOrBlank()) score -= 5
        if (ipAddress.isNullOrBlank()) score -= 5
        if (sessionId == null) score -= 5
        
        return maxOf(0, score)
    }
    
    /**
     * Check if audit event has data integrity issues
     */
    fun hasDataIntegrityIssues(): Boolean {
        return checksumValue.isNullOrBlank() ||
               (qualityIndicators?.completeness ?: 100) < 90 ||
               (qualityIndicators?.accuracy ?: 100) < 95
    }
    
    /**
     * Get audit trail strength rating
     */
    fun getAuditTrailStrength(): AuditStrength {
        val completeness = getCompletenessScore()
        val hasIntegrity = !hasDataIntegrityIssues()
        val hasEncryption = encryptionApplied
        val hasSignature = !digitalSignature.isNullOrBlank()
        
        return when {
            completeness >= 95 && hasIntegrity && hasEncryption && hasSignature -> AuditStrength.STRONG
            completeness >= 85 && hasIntegrity && (hasEncryption || hasSignature) -> AuditStrength.ADEQUATE
            completeness >= 75 && hasIntegrity -> AuditStrength.MODERATE
            completeness >= 60 -> AuditStrength.WEAK
            else -> AuditStrength.INSUFFICIENT
        }
    }
    
    /**
     * Check if audit event indicates policy violation
     */
    fun indicatesPolicyViolation(): Boolean {
        return operationResult in setOf(
            OperationResult.POLICY_VIOLATION,
            OperationResult.SECURITY_VIOLATION,
            OperationResult.COMPLIANCE_VIOLATION
        ) || riskFactors.contains(RiskFactor.POLICY_VIOLATION)
    }
    
    /**
     * Get audit event priority for investigation
     */
    fun getInvestigationPriority(): InvestigationPriority {
        return when {
            requiresImmediateInvestigation() -> InvestigationPriority.CRITICAL
            indicatesSuspiciousActivity() || indicatesPolicyViolation() -> InvestigationPriority.HIGH
            auditSeverity == AuditSeverity.HIGH || riskLevel == RiskLevel.HIGH -> InvestigationPriority.MEDIUM
            auditSeverity == AuditSeverity.MEDIUM || riskLevel == RiskLevel.MEDIUM -> InvestigationPriority.LOW
            else -> InvestigationPriority.ROUTINE
        }
    }
    
    /**
     * Check if audit event requires long-term retention
     */
    fun requiresLongTermRetention(): Boolean {
        return affectsComplianceObligations() ||
               dataClassification in setOf(DataClassification.REGULATED, DataClassification.FINANCIAL) ||
               auditSeverity in setOf(AuditSeverity.CRITICAL, AuditSeverity.HIGH) ||
               retentionPeriod >= 7
    }
    
    companion object {
        /**
         * Create audit event for financial transaction
         */
        fun forFinancialTransaction(
            auditTrailId: UUID,
            sequenceNumber: Long,
            operationType: OperationType,
            entityType: String,
            entityId: UUID,
            userId: UUID,
            userName: String,
            userRole: String,
            operationDescription: String,
            beforeValues: Map<String, String>?,
            afterValues: Map<String, String>?,
            sourceSystem: String,
            transactionId: UUID
        ): AuditTrailEvent {
            
            val severity = determineSeverity(operationType, entityType)
            val riskLevel = determineRiskLevel(operationType, entityType, beforeValues, afterValues)
            
            return AuditTrailEvent(
                aggregateId = UUID.randomUUID(),
                auditTrailId = auditTrailId,
                sequenceNumber = sequenceNumber,
                auditEventType = AuditEventType.FINANCIAL_TRANSACTION,
                auditCategory = AuditCategory.FINANCIAL,
                auditSeverity = severity,
                businessProcess = BusinessProcess.FINANCIAL_MANAGEMENT,
                operationType = operationType,
                operationResult = OperationResult.SUCCESS,
                entityType = entityType,
                entityId = entityId,
                userId = userId,
                userName = userName,
                userRole = userRole,
                sessionId = UUID.randomUUID(),
                clientApplication = "CHIRO-ERP",
                operationDescription = operationDescription,
                operationDetails = emptyMap(),
                beforeValues = beforeValues,
                afterValues = afterValues,
                changedFields = determineChangedFields(beforeValues, afterValues),
                approvalRequired = severity >= AuditSeverity.HIGH,
                transactionId = transactionId,
                sourceSystem = sourceSystem,
                sourceSystemVersion = "1.0.0",
                dataClassification = DataClassification.FINANCIAL,
                sensitivityLevel = SensitivityLevel.HIGH,
                complianceFlags = setOf(ComplianceFlag.SOX_CONTROL),
                regulatoryRequirements = emptySet(),
                retentionPeriod = 7, // 7 years for financial records
                encryptionApplied = true,
                checksumValue = generateChecksum(operationDescription, beforeValues, afterValues),
                riskLevel = riskLevel,
                riskFactors = determineRiskFactors(operationType, beforeValues, afterValues),
                alertTriggers = emptySet(),
                notificationsSent = emptySet(),
                timezone = "UTC",
                resourcesAccessed = setOf(
                    ResourceAccess(
                        resourceType = entityType,
                        resourceId = entityId.toString(),
                        accessType = operationType.name,
                        accessTime = LocalDateTime.now()
                    )
                ),
                qualityIndicators = QualityIndicators(
                    completeness = if (beforeValues != null || afterValues != null) 95 else 80,
                    accuracy = 100,
                    consistency = 100,
                    timeliness = 100
                ),
                linkedEvents = emptySet()
            )
        }
        
        /**
         * Create audit event for security violation
         */
        fun forSecurityViolation(
            auditTrailId: UUID,
            sequenceNumber: Long,
            violationType: String,
            userId: UUID,
            userName: String,
            ipAddress: String,
            description: String,
            sourceSystem: String
        ): AuditTrailEvent {
            
            return AuditTrailEvent(
                aggregateId = UUID.randomUUID(),
                auditTrailId = auditTrailId,
                sequenceNumber = sequenceNumber,
                auditEventType = AuditEventType.SECURITY_EVENT,
                auditCategory = AuditCategory.SECURITY,
                auditSeverity = AuditSeverity.CRITICAL,
                businessProcess = BusinessProcess.SECURITY_MANAGEMENT,
                operationType = OperationType.SECURITY_VIOLATION,
                operationResult = OperationResult.SECURITY_VIOLATION,
                entityType = "SECURITY_EVENT",
                entityId = UUID.randomUUID(),
                userId = userId,
                userName = userName,
                userRole = "UNKNOWN",
                sessionId = UUID.randomUUID(),
                ipAddress = ipAddress,
                clientApplication = "SECURITY_MONITOR",
                operationDescription = description,
                operationDetails = mapOf("violationType" to violationType),
                beforeValues = null,
                afterValues = null,
                changedFields = emptySet(),
                reasonDescription = "Security violation detected",
                approvalRequired = false,
                sourceSystem = sourceSystem,
                sourceSystemVersion = "1.0.0",
                dataClassification = DataClassification.SECURITY,
                sensitivityLevel = SensitivityLevel.VERY_HIGH,
                complianceFlags = setOf(ComplianceFlag.SECURITY_INCIDENT),
                regulatoryRequirements = emptySet(),
                retentionPeriod = 10, // 10 years for security incidents
                encryptionApplied = true,
                checksumValue = generateChecksum(description, null, null),
                riskLevel = RiskLevel.VERY_HIGH,
                riskFactors = setOf(RiskFactor.SECURITY_THREAT, RiskFactor.UNAUTHORIZED_ACCESS),
                alertTriggers = setOf(AlertTrigger.SECURITY_INCIDENT),
                notificationsSent = setOf(NotificationType.SECURITY_ALERT),
                timezone = "UTC",
                resourcesAccessed = emptySet(),
                qualityIndicators = QualityIndicators(
                    completeness = 90,
                    accuracy = 100,
                    consistency = 100,
                    timeliness = 100
                ),
                linkedEvents = emptySet()
            )
        }
        
        private fun determineSeverity(operationType: OperationType, entityType: String): AuditSeverity {
            return when {
                operationType == OperationType.DELETE -> AuditSeverity.HIGH
                operationType == OperationType.SECURITY_VIOLATION -> AuditSeverity.CRITICAL
                entityType.contains("PAYMENT") || entityType.contains("INVOICE") -> AuditSeverity.MEDIUM
                else -> AuditSeverity.LOW
            }
        }
        
        private fun determineRiskLevel(
            operationType: OperationType,
            entityType: String,
            beforeValues: Map<String, String>?,
            afterValues: Map<String, String>?
        ): RiskLevel {
            return when {
                operationType == OperationType.DELETE -> RiskLevel.HIGH
                operationType == OperationType.SECURITY_VIOLATION -> RiskLevel.VERY_HIGH
                entityType.contains("FINANCIAL") -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }
        }
        
        private fun determineChangedFields(
            beforeValues: Map<String, String>?,
            afterValues: Map<String, String>?
        ): Set<String> {
            if (beforeValues == null || afterValues == null) return emptySet()
            
            return beforeValues.keys.union(afterValues.keys).filter { key ->
                beforeValues[key] != afterValues[key]
            }.toSet()
        }
        
        private fun determineRiskFactors(
            operationType: OperationType,
            beforeValues: Map<String, String>?,
            afterValues: Map<String, String>?
        ): Set<RiskFactor> {
            val factors = mutableSetOf<RiskFactor>()
            
            if (operationType == OperationType.DELETE) {
                factors.add(RiskFactor.DATA_DELETION)
            }
            
            if (beforeValues != null && afterValues != null) {
                val changedFields = determineChangedFields(beforeValues, afterValues)
                if (changedFields.size > 5) {
                    factors.add(RiskFactor.BULK_CHANGES)
                }
            }
            
            return factors
        }
        
        private fun generateChecksum(
            description: String,
            beforeValues: Map<String, String>?,
            afterValues: Map<String, String>?
        ): String {
            val content = "$description|${beforeValues?.hashCode()}|${afterValues?.hashCode()}"
            return content.hashCode().toString()
        }
    }
}

/**
 * Audit Event Type Classifications
 */
enum class AuditEventType {
    FINANCIAL_TRANSACTION,
    USER_ACTION,
    SYSTEM_EVENT,
    SECURITY_EVENT,
    COMPLIANCE_EVENT,
    DATA_ACCESS,
    CONFIGURATION_CHANGE,
    REPORT_GENERATION,
    APPROVAL_WORKFLOW,
    INTEGRATION_EVENT
}

/**
 * Audit Category Classifications
 */
enum class AuditCategory {
    FINANCIAL,
    SECURITY,
    COMPLIANCE,
    OPERATIONAL,
    ADMINISTRATIVE,
    TECHNICAL,
    LEGAL
}

/**
 * Audit Severity Levels
 */
enum class AuditSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Business Process Classifications
 */
enum class BusinessProcess {
    FINANCIAL_MANAGEMENT,
    ACCOUNTS_RECEIVABLE,
    ACCOUNTS_PAYABLE,
    GENERAL_LEDGER,
    BUDGETING,
    REPORTING,
    USER_MANAGEMENT,
    SECURITY_MANAGEMENT,
    COMPLIANCE_MANAGEMENT,
    SYSTEM_ADMINISTRATION
}

/**
 * Operation Type Classifications
 */
enum class OperationType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    APPROVE,
    REJECT,
    SUBMIT,
    CANCEL,
    EXPORT,
    IMPORT,
    LOGIN,
    LOGOUT,
    SECURITY_VIOLATION,
    CONFIGURATION_CHANGE
}

/**
 * Operation Result Classifications
 */
enum class OperationResult {
    SUCCESS,
    FAILURE,
    PARTIAL_SUCCESS,
    CANCELLED,
    TIMEOUT,
    SECURITY_VIOLATION,
    POLICY_VIOLATION,
    COMPLIANCE_VIOLATION,
    SYSTEM_ERROR
}

/**
 * Approval Status Values
 */
enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN,
    EXPIRED
}

/**
 * Data Classification Types
 */
enum class DataClassification {
    PUBLIC,
    INTERNAL,
    CONFIDENTIAL,
    RESTRICTED,
    REGULATED,
    FINANCIAL,
    PERSONAL,
    SECURITY
}

/**
 * Sensitivity Level Classifications
 */
enum class SensitivityLevel {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH,
    EXTREMELY_HIGH
}

/**
 * Risk Factor Types
 */
enum class RiskFactor {
    UNUSUAL_PATTERN,
    UNAUTHORIZED_ACCESS,
    PRIVILEGE_ESCALATION,
    DATA_EXFILTRATION,
    SECURITY_THREAT,
    POLICY_VIOLATION,
    DATA_DELETION,
    BULK_CHANGES,
    HIGH_VALUE_TRANSACTION,
    AFTER_HOURS_ACCESS
}

/**
 * Alert Trigger Types
 */
enum class AlertTrigger {
    SECURITY_INCIDENT,
    COMPLIANCE_VIOLATION,
    FRAUD_DETECTION,
    UNUSUAL_ACTIVITY,
    THRESHOLD_EXCEEDED,
    POLICY_BREACH
}

/**
 * Audit Strength Classifications
 */
enum class AuditStrength {
    INSUFFICIENT,
    WEAK,
    MODERATE,
    ADEQUATE,
    STRONG
}

/**
 * Investigation Priority Levels
 */
enum class InvestigationPriority {
    ROUTINE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Resource Access Details
 */
data class ResourceAccess(
    val resourceType: String,
    val resourceId: String,
    val accessType: String,
    val accessTime: LocalDateTime
)

/**
 * Data Volume Metrics
 */
data class DataVolume(
    val recordsProcessed: Long,
    val bytesProcessed: Long,
    val fieldsModified: Int
)

/**
 * Performance Metrics
 */
data class PerformanceMetrics(
    val executionTime: Long, // Milliseconds
    val memoryUsage: Long,   // Bytes
    val cpuUsage: Double,    // Percentage
    val networkBytes: Long
)

/**
 * Quality Indicators
 */
data class QualityIndicators(
    val completeness: Int,   // Percentage
    val accuracy: Int,       // Percentage
    val consistency: Int,    // Percentage
    val timeliness: Int      // Percentage
)

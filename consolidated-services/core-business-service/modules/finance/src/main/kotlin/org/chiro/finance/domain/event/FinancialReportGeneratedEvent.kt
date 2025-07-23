package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * FinancialReportGeneratedEvent
 * 
 * Domain event published when a financial report is generated in the system.
 * This event triggers post-report workflows and integrates with business
 * intelligence, compliance, and management reporting systems.
 * 
 * This event enables:
 * - Automated report distribution
 * - Compliance reporting workflows
 * - Business intelligence updates
 * - Management dashboard refreshes
 * - Audit trail for report generation
 * - Performance monitoring and analytics
 */
data class FinancialReportGeneratedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Report Generation ID
    val reportId: UUID,
    val reportName: String,
    val reportType: FinancialReportType,
    val reportCategory: ReportCategory,
    val reportFormat: ReportFormat,
    val reportPurpose: ReportPurpose,
    val reportScope: ReportScope,
    val reportStatus: ReportStatus,
    val reportVersion: String,
    val entityId: UUID,
    val entityName: String,
    val reportingPeriod: ReportingPeriod,
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate,
    val asOfDate: LocalDate,
    val fiscalYear: Int,
    val fiscalPeriod: Int,
    val fiscalQuarter: Int?,
    val currency: Currency,
    val baseCurrency: Currency,
    val exchangeRates: Map<Currency, BigDecimal>,
    val consolidationLevel: ConsolidationLevel,
    val subsidiariesIncluded: Set<UUID>,
    val departmentsIncluded: Set<UUID>,
    val accountsIncluded: Set<AccountCode>,
    val adjustmentsIncluded: Boolean,
    val eliminationsApplied: Boolean,
    val dataSource: DataSource,
    val dataAsOfTimestamp: LocalDateTime,
    val generationStartTime: LocalDateTime,
    val generationEndTime: LocalDateTime,
    val generationDuration: Long, // Milliseconds
    val reportSizeBytes: Long,
    val pageCount: Int?,
    val recordCount: Int,
    val totalValue: FinancialAmount?, // Key financial metric
    val keyMetrics: Map<String, FinancialAmount>,
    val comparativePeriod: ReportingPeriod?,
    val varianceAnalysisIncluded: Boolean,
    val benchmarkingIncluded: Boolean,
    val notesIncluded: Boolean,
    val attachmentsIncluded: Boolean,
    val confidentialityLevel: ConfidentialityLevel,
    val accessRestrictions: Set<AccessRestriction>,
    val distributionList: Set<ReportRecipient>,
    val approvalRequired: Boolean,
    val approvedBy: UUID?,
    val approvedByName: String?,
    val approvalDate: LocalDateTime?,
    val reviewedBy: UUID?,
    val reviewedByName: String?,
    val reviewDate: LocalDateTime?,
    val preparedBy: UUID,
    val preparedByName: String,
    val generatedBy: UUID,
    val generatedByName: String,
    val templateId: UUID?,
    val templateName: String?,
    val customizations: Map<String, String>,
    val parameters: Map<String, String>,
    val filters: Map<String, String>,
    val sortOrder: String?,
    val groupingCriteria: Set<String>,
    val calculationMethods: Map<String, String>,
    val assumptions: List<String>,
    val limitations: List<String>,
    val dataQualityScore: Int, // 0-100
    val completenessScore: Int, // 0-100
    val accuracyScore: Int, // 0-100
    val reliabilityScore: Int, // 0-100
    val validationErrors: List<ValidationError>,
    val warnings: List<ReportWarning>,
    val qualityChecks: List<QualityCheckResult>,
    val complianceStandards: Set<ComplianceStandard>,
    val regulatoryRequirements: Set<RegulatoryRequirement>,
    val auditTrail: Set<AuditEvent>,
    val retentionPeriod: Int, // Days
    val archiveDate: LocalDate?,
    val deliveryMethods: Set<DeliveryMethod>,
    val deliveryStatus: Map<DeliveryMethod, DeliveryStatus>,
    val scheduledDelivery: Boolean,
    val nextScheduledRun: LocalDateTime?,
    val automaticGeneration: Boolean,
    val generationTrigger: GenerationTrigger,
    val workflowInstanceId: UUID?,
    val jobId: UUID?,
    val integrationTargets: Set<IntegrationTarget>,
    val exportFormats: Set<ExportFormat>,
    val fileLocations: Map<ExportFormat, String>,
    val encryptionApplied: Boolean,
    val digitalSignature: String?,
    val checksumValues: Map<String, String>,
    val businessContext: String?,
    val executiveSummary: String?,
    val keyInsights: List<String>,
    val recommendations: List<String>,
    val riskFactors: Set<ReportRiskFactor>,
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap(),
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Check if report generation was successful and timely
     */
    fun wasGeneratedSuccessfully(): Boolean {
        return reportStatus == ReportStatus.COMPLETED &&
               validationErrors.isEmpty() &&
               generationDuration <= 300000 // 5 minutes
    }
    
    /**
     * Check if report has high data quality
     */
    fun hasHighDataQuality(): Boolean {
        return dataQualityScore >= 95 &&
               completenessScore >= 95 &&
               accuracyScore >= 95 &&
               reliabilityScore >= 95
    }
    
    /**
     * Check if report requires management attention
     */
    fun requiresManagementAttention(): Boolean {
        return reportType in setOf(
            FinancialReportType.INCOME_STATEMENT,
            FinancialReportType.BALANCE_SHEET,
            FinancialReportType.CASH_FLOW_STATEMENT
        ) && (
            warnings.any { it.severity == WarningSeverity.HIGH } ||
            dataQualityScore < 90 ||
            riskFactors.any { it in setOf(
                ReportRiskFactor.DATA_ANOMALY,
                ReportRiskFactor.SIGNIFICANT_VARIANCE,
                ReportRiskFactor.MISSING_DATA
            )}
        )
    }
    
    /**
     * Get report generation efficiency score
     */
    fun getGenerationEfficiencyScore(): Int {
        var score = 100
        
        // Deduct for slow generation
        val minutesTaken = generationDuration / 60000
        when {
            minutesTaken > 10 -> score -= 30
            minutesTaken > 5 -> score -= 20
            minutesTaken > 2 -> score -= 10
        }
        
        // Deduct for validation errors
        score -= validationErrors.size * 10
        
        // Deduct for warnings
        score -= warnings.size * 5
        
        // Deduct for poor data quality
        if (dataQualityScore < 90) score -= (90 - dataQualityScore)
        
        return maxOf(0, score)
    }
    
    /**
     * Check if report contains sensitive information
     */
    fun containsSensitiveInformation(): Boolean {
        return confidentialityLevel in setOf(
            ConfidentialityLevel.CONFIDENTIAL,
            ConfidentialityLevel.HIGHLY_CONFIDENTIAL,
            ConfidentialityLevel.TOP_SECRET
        ) || accessRestrictions.isNotEmpty()
    }
    
    /**
     * Check if report is ready for distribution
     */
    fun isReadyForDistribution(): Boolean {
        return reportStatus == ReportStatus.COMPLETED &&
               (!approvalRequired || approvalDate != null) &&
               validationErrors.isEmpty() &&
               hasHighDataQuality()
    }
    
    /**
     * Get distribution readiness score
     */
    fun getDistributionReadinessScore(): Int {
        var score = 0
        
        // Check completion status
        if (reportStatus == ReportStatus.COMPLETED) score += 30
        
        // Check approval status
        if (!approvalRequired || approvalDate != null) score += 20
        
        // Check data quality
        score += (dataQualityScore * 0.3).toInt()
        
        // Check validation status
        if (validationErrors.isEmpty()) score += 15
        
        // Check format readiness
        if (fileLocations.isNotEmpty()) score += 5
        
        return minOf(100, score)
    }
    
    /**
     * Check if report affects compliance obligations
     */
    fun affectsComplianceObligations(): Boolean {
        return regulatoryRequirements.isNotEmpty() ||
               complianceStandards.isNotEmpty() ||
               reportPurpose in setOf(
                   ReportPurpose.REGULATORY_FILING,
                   ReportPurpose.AUDIT_SUPPORT,
                   ReportPurpose.COMPLIANCE_MONITORING
               )
    }
    
    /**
     * Get business impact level
     */
    fun getBusinessImpactLevel(): BusinessImpactLevel {
        return when {
            reportType in setOf(
                FinancialReportType.INCOME_STATEMENT,
                FinancialReportType.BALANCE_SHEET,
                FinancialReportType.CASH_FLOW_STATEMENT
            ) && reportScope == ReportScope.CONSOLIDATED -> BusinessImpactLevel.CRITICAL
            
            reportPurpose in setOf(
                ReportPurpose.BOARD_REPORTING,
                ReportPurpose.REGULATORY_FILING,
                ReportPurpose.INVESTOR_REPORTING
            ) -> BusinessImpactLevel.HIGH
            
            reportType in setOf(
                FinancialReportType.BUDGET_VARIANCE,
                FinancialReportType.MANAGEMENT_REPORT,
                FinancialReportType.DEPARTMENTAL_REPORT
            ) -> BusinessImpactLevel.MEDIUM
            
            else -> BusinessImpactLevel.LOW
        }
    }
    
    /**
     * Check if report indicates financial anomalies
     */
    fun indicatesFinancialAnomalies(): Boolean {
        return riskFactors.any { it in setOf(
            ReportRiskFactor.DATA_ANOMALY,
            ReportRiskFactor.SIGNIFICANT_VARIANCE,
            ReportRiskFactor.UNUSUAL_PATTERN
        ) } || warnings.any { it.category == WarningCategory.FINANCIAL_ANOMALY }
    }
    
    companion object {
        /**
         * Create event for standard financial statement
         */
        fun forFinancialStatement(
            reportId: UUID,
            reportName: String,
            reportType: FinancialReportType,
            entityId: UUID,
            entityName: String,
            periodStart: LocalDate,
            periodEnd: LocalDate,
            fiscalYear: Int,
            fiscalPeriod: Int,
            currency: Currency,
            totalValue: FinancialAmount,
            keyMetrics: Map<String, FinancialAmount>,
            recordCount: Int,
            generationDuration: Long,
            preparedBy: UUID,
            preparedByName: String,
            generatedBy: UUID,
            generatedByName: String
        ): FinancialReportGeneratedEvent {
            
            val dataQuality = calculateDataQualityScore(recordCount, keyMetrics.size)
            
            return FinancialReportGeneratedEvent(
                aggregateId = reportId,
                reportId = reportId,
                reportName = reportName,
                reportType = reportType,
                reportCategory = ReportCategory.FINANCIAL_STATEMENT,
                reportFormat = ReportFormat.PDF,
                reportPurpose = ReportPurpose.MANAGEMENT_REPORTING,
                reportScope = ReportScope.ENTITY,
                reportStatus = ReportStatus.COMPLETED,
                reportVersion = "1.0",
                entityId = entityId,
                entityName = entityName,
                reportingPeriod = ReportingPeriod.MONTHLY,
                periodStartDate = periodStart,
                periodEndDate = periodEnd,
                asOfDate = periodEnd,
                fiscalYear = fiscalYear,
                fiscalPeriod = fiscalPeriod,
                fiscalQuarter = (fiscalPeriod - 1) / 3 + 1,
                currency = currency,
                baseCurrency = currency,
                exchangeRates = emptyMap(),
                consolidationLevel = ConsolidationLevel.ENTITY,
                subsidiariesIncluded = emptySet(),
                departmentsIncluded = emptySet(),
                accountsIncluded = emptySet(),
                adjustmentsIncluded = true,
                eliminationsApplied = false,
                dataSource = DataSource.GENERAL_LEDGER,
                dataAsOfTimestamp = LocalDateTime.now(),
                generationStartTime = LocalDateTime.now().minusNanos(generationDuration * 1_000_000),
                generationEndTime = LocalDateTime.now(),
                generationDuration = generationDuration,
                reportSizeBytes = 1024L * 1024L, // 1MB estimate
                recordCount = recordCount,
                totalValue = totalValue,
                keyMetrics = keyMetrics,
                varianceAnalysisIncluded = false,
                benchmarkingIncluded = false,
                notesIncluded = true,
                attachmentsIncluded = false,
                confidentialityLevel = ConfidentialityLevel.INTERNAL,
                accessRestrictions = emptySet(),
                distributionList = emptySet(),
                approvalRequired = reportType in setOf(
                    FinancialReportType.INCOME_STATEMENT,
                    FinancialReportType.BALANCE_SHEET
                ),
                preparedBy = preparedBy,
                preparedByName = preparedByName,
                generatedBy = generatedBy,
                generatedByName = generatedByName,
                customizations = emptyMap(),
                parameters = emptyMap(),
                filters = emptyMap(),
                groupingCriteria = emptySet(),
                calculationMethods = emptyMap(),
                assumptions = emptyList(),
                limitations = emptyList(),
                dataQualityScore = dataQuality,
                completenessScore = dataQuality,
                accuracyScore = dataQuality,
                reliabilityScore = dataQuality,
                validationErrors = emptyList(),
                warnings = emptyList(),
                qualityChecks = emptyList(),
                complianceStandards = setOf(ComplianceStandard.GAAP),
                regulatoryRequirements = emptySet(),
                auditTrail = emptySet(),
                retentionPeriod = 2555, // 7 years
                deliveryMethods = setOf(DeliveryMethod.SYSTEM_DOWNLOAD),
                deliveryStatus = mapOf(DeliveryMethod.SYSTEM_DOWNLOAD to DeliveryStatus.READY),
                scheduledDelivery = false,
                automaticGeneration = false,
                generationTrigger = GenerationTrigger.MANUAL_REQUEST,
                integrationTargets = emptySet(),
                exportFormats = setOf(ExportFormat.PDF),
                fileLocations = mapOf(ExportFormat.PDF to "/reports/${reportId}.pdf"),
                encryptionApplied = false,
                checksumValues = emptyMap(),
                riskFactors = emptySet(),
                keyInsights = emptyList(),
                recommendations = emptyList()
            )
        }
        
        private fun calculateDataQualityScore(recordCount: Int, metricsCount: Int): Int {
            var score = 100
            
            // Deduct for low record count
            if (recordCount < 100) score -= 10
            if (recordCount < 50) score -= 10
            
            // Deduct for few metrics
            if (metricsCount < 5) score -= 10
            if (metricsCount < 3) score -= 10
            
            return maxOf(70, score) // Minimum 70% quality
        }
    }
}

/**
 * Financial Report Type Classifications
 */
enum class FinancialReportType {
    INCOME_STATEMENT,
    BALANCE_SHEET,
    CASH_FLOW_STATEMENT,
    STATEMENT_OF_EQUITY,
    TRIAL_BALANCE,
    GENERAL_LEDGER,
    ACCOUNTS_RECEIVABLE_AGING,
    ACCOUNTS_PAYABLE_AGING,
    BUDGET_VARIANCE,
    MANAGEMENT_REPORT,
    DEPARTMENTAL_REPORT,
    PROJECT_REPORT,
    TAX_REPORT,
    AUDIT_REPORT,
    CUSTOM_REPORT
}

/**
 * Report Category Classifications
 */
enum class ReportCategory {
    FINANCIAL_STATEMENT,
    MANAGEMENT_REPORT,
    OPERATIONAL_REPORT,
    COMPLIANCE_REPORT,
    ANALYTICAL_REPORT,
    EXCEPTION_REPORT
}

/**
 * Report Format Types
 */
enum class ReportFormat {
    PDF,
    EXCEL,
    CSV,
    HTML,
    XML,
    JSON,
    WORD,
    POWERPOINT
}

/**
 * Report Purpose Classifications
 */
enum class ReportPurpose {
    MANAGEMENT_REPORTING,
    BOARD_REPORTING,
    REGULATORY_FILING,
    AUDIT_SUPPORT,
    INVESTOR_REPORTING,
    COMPLIANCE_MONITORING,
    OPERATIONAL_ANALYSIS,
    DECISION_SUPPORT
}

/**
 * Report Scope Classifications
 */
enum class ReportScope {
    ENTITY,
    SUBSIDIARY,
    DEPARTMENT,
    PROJECT,
    COST_CENTER,
    CONSOLIDATED,
    SEGMENT
}

/**
 * Report Status Values
 */
enum class ReportStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED,
    APPROVED,
    DISTRIBUTED
}

/**
 * Reporting Period Types
 */
enum class ReportingPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    ANNUALLY,
    YEAR_TO_DATE,
    CUSTOM_PERIOD
}

/**
 * Consolidation Level Types
 */
enum class ConsolidationLevel {
    ENTITY,
    SUBSIDIARY,
    SEGMENT,
    CONSOLIDATED,
    ELIMINATED
}

/**
 * Data Source Classifications
 */
enum class DataSource {
    GENERAL_LEDGER,
    SUBSIDIARY_LEDGER,
    TRIAL_BALANCE,
    BUDGET_SYSTEM,
    EXTERNAL_SYSTEM,
    MANUAL_INPUT,
    CONSOLIDATED_DATA
}

/**
 * Confidentiality Level Classifications
 */
enum class ConfidentialityLevel {
    PUBLIC,
    INTERNAL,
    CONFIDENTIAL,
    HIGHLY_CONFIDENTIAL,
    TOP_SECRET
}

/**
 * Access Restriction Types
 */
enum class AccessRestriction {
    ROLE_BASED,
    DEPARTMENT_BASED,
    CLEARANCE_LEVEL,
    NEED_TO_KNOW,
    GEOGRAPHIC,
    TIME_LIMITED
}

/**
 * Report Recipient Details
 */
data class ReportRecipient(
    val recipientId: UUID,
    val recipientName: String,
    val recipientType: RecipientType,
    val deliveryMethod: DeliveryMethod,
    val emailAddress: String?,
    val accessLevel: AccessLevel
)

/**
 * Recipient Type Classifications
 */
enum class RecipientType {
    EMPLOYEE,
    MANAGER,
    EXECUTIVE,
    BOARD_MEMBER,
    AUDITOR,
    REGULATOR,
    INVESTOR,
    EXTERNAL_PARTY
}

/**
 * Access Level Classifications
 */
enum class AccessLevel {
    READ_ONLY,
    DOWNLOAD,
    PRINT,
    FULL_ACCESS
}

/**
 * Report Warning Details
 */
data class ReportWarning(
    val warningCode: String,
    val warningMessage: String,
    val severity: WarningSeverity,
    val category: WarningCategory,
    val recommendation: String?
)

/**
 * Warning Severity Levels
 */
enum class WarningSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Warning Category Types
 */
enum class WarningCategory {
    DATA_QUALITY,
    COMPLETENESS,
    ACCURACY,
    TIMELINESS,
    FINANCIAL_ANOMALY,
    COMPLIANCE_ISSUE
}

/**
 * Quality Check Result Details
 */
data class QualityCheckResult(
    val checkName: String,
    val checkType: QualityCheckType,
    val passed: Boolean,
    val score: Int,
    val details: String?
)

/**
 * Quality Check Type Classifications
 */
enum class QualityCheckType {
    COMPLETENESS_CHECK,
    ACCURACY_CHECK,
    CONSISTENCY_CHECK,
    VALIDITY_CHECK,
    RECONCILIATION_CHECK,
    BALANCE_CHECK
}

/**
 * Compliance Standard Types
 */
enum class ComplianceStandard {
    GAAP,
    IFRS,
    SOX,
    BASEL_III,
    GDPR,
    CUSTOM_STANDARD
}

/**
 * Regulatory Requirement Types
 */
enum class RegulatoryRequirement {
    SEC_FILING,
    TAX_REPORTING,
    BANKING_REGULATION,
    INSURANCE_REGULATION,
    INDUSTRY_SPECIFIC,
    LOCAL_REGULATION
}

/**
 * Audit Event Details
 */
data class AuditEvent(
    val eventId: UUID,
    val eventType: String,
    val eventTimestamp: LocalDateTime,
    val userId: UUID,
    val userName: String,
    val action: String,
    val details: String?
)

/**
 * Delivery Method Types
 */
enum class DeliveryMethod {
    EMAIL,
    SYSTEM_DOWNLOAD,
    FTP,
    API,
    SECURE_PORTAL,
    PHYSICAL_DELIVERY,
    AUTOMATED_DISTRIBUTION
}

/**
 * Delivery Status Values
 */
enum class DeliveryStatus {
    PENDING,
    READY,
    SENT,
    DELIVERED,
    FAILED,
    CANCELLED
}

/**
 * Generation Trigger Types
 */
enum class GenerationTrigger {
    MANUAL_REQUEST,
    SCHEDULED_RUN,
    DATA_UPDATE,
    PERIOD_END,
    COMPLIANCE_DEADLINE,
    BUSINESS_EVENT
}

/**
 * Integration Target Types
 */
enum class IntegrationTarget {
    BUSINESS_INTELLIGENCE,
    DATA_WAREHOUSE,
    EXTERNAL_SYSTEM,
    REGULATORY_SYSTEM,
    AUDIT_SYSTEM,
    COLLABORATION_PLATFORM
}

/**
 * Export Format Types
 */
enum class ExportFormat {
    PDF,
    EXCEL,
    CSV,
    XML,
    JSON,
    HTML,
    WORD
}

/**
 * Business Impact Level Classifications
 */
enum class BusinessImpactLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Report Risk Factor Types
 */
enum class ReportRiskFactor {
    DATA_ANOMALY,
    SIGNIFICANT_VARIANCE,
    MISSING_DATA,
    LATE_GENERATION,
    QUALITY_ISSUE,
    COMPLIANCE_RISK,
    UNUSUAL_PATTERN
}

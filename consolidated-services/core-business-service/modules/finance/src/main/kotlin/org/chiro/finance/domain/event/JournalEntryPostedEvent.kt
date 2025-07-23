package org.chiro.finance.domain.event

import org.chiro.finance.domain.valueobject.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * JournalEntryPostedEvent
 * 
 * Domain event published when a journal entry is posted to the general ledger.
 * This event triggers general ledger automation workflows and integrates with
 * various accounting and financial reporting systems.
 * 
 * This event enables:
 * - General ledger automation
 * - Financial statement preparation
 * - Audit trail maintenance
 * - Compliance reporting
 * - Real-time financial position updates
 * - Subsidiary ledger reconciliation
 */
data class JournalEntryPostedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val aggregateId: UUID, // Journal Entry ID
    val journalEntryNumber: String,
    val entryType: JournalEntryType,
    val entrySource: JournalEntrySource,
    val entryStatus: JournalEntryStatus,
    val postingDate: LocalDate,
    val effectiveDate: LocalDate,
    val period: FiscalPeriod,
    val fiscalYear: Int,
    val fiscalPeriodId: UUID,
    val description: String,
    val reference: String?,
    val totalDebitAmount: FinancialAmount,
    val totalCreditAmount: FinancialAmount,
    val currency: Currency,
    val exchangeRate: BigDecimal?,
    val baseCurrencyTotalDebit: FinancialAmount?,
    val baseCurrencyTotalCredit: FinancialAmount?,
    val lineItemCount: Int,
    val accountsAffected: Set<AccountCode>,
    val departmentsAffected: Set<UUID>,
    val costCentersAffected: Set<UUID>,
    val projectsAffected: Set<UUID>,
    val sourceDocumentId: UUID?,
    val sourceDocumentType: SourceDocumentType?,
    val sourceDocumentNumber: String?,
    val batchId: UUID?,
    val batchNumber: String?,
    val reversalEntryId: UUID?,
    val originalEntryId: UUID?, // For reversal entries
    val isReversalEntry: Boolean = false,
    val isAdjustingEntry: Boolean = false,
    val isClosingEntry: Boolean = false,
    val isRecurringEntry: Boolean = false,
    val recurringFrequency: RecurringFrequency?,
    val nextRecurringDate: LocalDate?,
    val approvalRequired: Boolean,
    val approvedBy: UUID?,
    val approvedByName: String?,
    val approvalDate: LocalDateTime?,
    val approvalWorkflowId: UUID?,
    val preparedBy: UUID,
    val preparedByName: String,
    val reviewedBy: UUID?,
    val reviewedByName: String?,
    val postedBy: UUID,
    val postedByName: String,
    val reviewNotes: String?,
    val reconciliationStatus: ReconciliationStatus = ReconciliationStatus.PENDING,
    val impactsFinancialStatements: Boolean = true,
    val statementImpacts: Set<FinancialStatementType>,
    val complianceFlags: Set<ComplianceFlag>,
    val auditTrailId: UUID,
    val tags: Set<String> = emptySet(),
    val attachments: List<AttachmentReference> = emptyList(),
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 1L
) : DomainEvent {
    
    override fun getAggregateId(): UUID = aggregateId
    override fun getEventId(): UUID = eventId
    override fun getOccurredAt(): LocalDateTime = occurredAt
    override fun getVersion(): Long = version
    
    /**
     * Verify that debits equal credits (fundamental accounting equation)
     */
    fun isBalanced(): Boolean {
        return totalDebitAmount.equals(totalCreditAmount)
    }
    
    /**
     * Check if entry is high value (requires special attention)
     */
    fun isHighValue(threshold: FinancialAmount): Boolean {
        return totalDebitAmount.isGreaterThan(threshold)
    }
    
    /**
     * Check if entry involves foreign currency
     */
    fun involvesForeignCurrency(): Boolean {
        return exchangeRate != null && baseCurrencyTotalDebit != null
    }
    
    /**
     * Check if entry affects multiple departments
     */
    fun affectsMultipleDepartments(): Boolean {
        return departmentsAffected.size > 1
    }
    
    /**
     * Check if entry is from automated source
     */
    fun isAutomatedEntry(): Boolean {
        return entrySource in setOf(
            JournalEntrySource.AUTOMATED_ACCRUAL,
            JournalEntrySource.SYSTEM_GENERATED,
            JournalEntrySource.DEPRECIATION_CALC,
            JournalEntrySource.RECURRING_PROCESS
        )
    }
    
    /**
     * Check if entry requires regulatory compliance review
     */
    fun requiresComplianceReview(): Boolean {
        return complianceFlags.isNotEmpty() || 
               entryType in setOf(JournalEntryType.COMPLIANCE, JournalEntryType.REGULATORY)
    }
    
    /**
     * Get entry complexity score (0-100)
     */
    fun getComplexityScore(): Int {
        var score = 0
        
        // Base complexity from line items
        score += minOf(30, lineItemCount * 2)
        
        // Add complexity for multiple entities
        score += departmentsAffected.size * 5
        score += costCentersAffected.size * 3
        score += projectsAffected.size * 3
        
        // Add complexity for special types
        if (isAdjustingEntry) score += 15
        if (isClosingEntry) score += 20
        if (isReversalEntry) score += 10
        if (involvesForeignCurrency()) score += 15
        
        // Add complexity for compliance requirements
        score += complianceFlags.size * 10
        
        return minOf(100, score)
    }
    
    /**
     * Check if entry is part of period-end closing process
     */
    fun isPeriodEndEntry(): Boolean {
        return isClosingEntry || isAdjustingEntry ||
               entryType in setOf(JournalEntryType.CLOSING, JournalEntryType.ADJUSTING)
    }
    
    /**
     * Get financial statement impact summary
     */
    fun getStatementImpactSummary(): String {
        return when {
            statementImpacts.contains(FinancialStatementType.INCOME_STATEMENT) &&
            statementImpacts.contains(FinancialStatementType.BALANCE_SHEET) -> "Income Statement & Balance Sheet"
            statementImpacts.contains(FinancialStatementType.INCOME_STATEMENT) -> "Income Statement"
            statementImpacts.contains(FinancialStatementType.BALANCE_SHEET) -> "Balance Sheet"
            statementImpacts.contains(FinancialStatementType.CASH_FLOW) -> "Cash Flow Statement"
            else -> "Other Financial Statements"
        }
    }
    
    /**
     * Check if entry requires management review
     */
    fun requiresManagementReview(): Boolean {
        return isHighValue(FinancialAmount(BigDecimal("50000"), currency)) ||
               requiresComplianceReview() ||
               isAdjustingEntry ||
               getComplexityScore() >= 80
    }
    
    /**
     * Get entry risk level
     */
    fun getRiskLevel(): RiskLevel {
        return when {
            complianceFlags.isNotEmpty() && isHighValue(FinancialAmount(BigDecimal("100000"), currency)) -> RiskLevel.VERY_HIGH
            requiresComplianceReview() || isHighValue(FinancialAmount(BigDecimal("50000"), currency)) -> RiskLevel.HIGH
            isAdjustingEntry || involvesForeignCurrency() -> RiskLevel.MEDIUM
            isAutomatedEntry() && totalDebitAmount.amount <= BigDecimal("10000") -> RiskLevel.LOW
            else -> RiskLevel.MEDIUM
        }
    }
    
    /**
     * Check if entry can be reversed
     */
    fun canBeReversed(): Boolean {
        return !isReversalEntry && 
               !isClosingEntry && 
               entryStatus == JournalEntryStatus.POSTED &&
               period.isCurrentOrFuture()
    }
    
    companion object {
        /**
         * Create event for standard journal entry
         */
        fun forStandardEntry(
            entryId: UUID,
            entryNumber: String,
            description: String,
            postingDate: LocalDate,
            totalAmount: FinancialAmount,
            lineItemCount: Int,
            accountsAffected: Set<AccountCode>,
            preparedBy: UUID,
            preparedByName: String,
            postedBy: UUID,
            postedByName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            auditTrailId: UUID
        ): JournalEntryPostedEvent {
            
            return JournalEntryPostedEvent(
                aggregateId = entryId,
                journalEntryNumber = entryNumber,
                entryType = JournalEntryType.STANDARD,
                entrySource = JournalEntrySource.MANUAL_ENTRY,
                entryStatus = JournalEntryStatus.POSTED,
                postingDate = postingDate,
                effectiveDate = postingDate,
                period = FiscalPeriod.fromDate(postingDate),
                fiscalYear = fiscalYear,
                fiscalPeriodId = fiscalPeriodId,
                description = description,
                totalDebitAmount = totalAmount,
                totalCreditAmount = totalAmount,
                currency = totalAmount.currency,
                lineItemCount = lineItemCount,
                accountsAffected = accountsAffected,
                departmentsAffected = emptySet(),
                costCentersAffected = emptySet(),
                projectsAffected = emptySet(),
                approvalRequired = false,
                preparedBy = preparedBy,
                preparedByName = preparedByName,
                postedBy = postedBy,
                postedByName = postedByName,
                statementImpacts = determineStatementImpacts(accountsAffected),
                complianceFlags = emptySet(),
                auditTrailId = auditTrailId
            )
        }
        
        /**
         * Create event for adjusting entry
         */
        fun forAdjustingEntry(
            entryId: UUID,
            entryNumber: String,
            description: String,
            postingDate: LocalDate,
            totalAmount: FinancialAmount,
            accountsAffected: Set<AccountCode>,
            preparedBy: UUID,
            preparedByName: String,
            reviewedBy: UUID,
            reviewedByName: String,
            postedBy: UUID,
            postedByName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            auditTrailId: UUID
        ): JournalEntryPostedEvent {
            
            return JournalEntryPostedEvent(
                aggregateId = entryId,
                journalEntryNumber = entryNumber,
                entryType = JournalEntryType.ADJUSTING,
                entrySource = JournalEntrySource.PERIOD_END,
                entryStatus = JournalEntryStatus.POSTED,
                postingDate = postingDate,
                effectiveDate = postingDate,
                period = FiscalPeriod.fromDate(postingDate),
                fiscalYear = fiscalYear,
                fiscalPeriodId = fiscalPeriodId,
                description = description,
                totalDebitAmount = totalAmount,
                totalCreditAmount = totalAmount,
                currency = totalAmount.currency,
                lineItemCount = 2, // Typical adjusting entry
                accountsAffected = accountsAffected,
                departmentsAffected = emptySet(),
                costCentersAffected = emptySet(),
                projectsAffected = emptySet(),
                isAdjustingEntry = true,
                approvalRequired = true,
                preparedBy = preparedBy,
                preparedByName = preparedByName,
                reviewedBy = reviewedBy,
                reviewedByName = reviewedByName,
                postedBy = postedBy,
                postedByName = postedByName,
                statementImpacts = determineStatementImpacts(accountsAffected),
                complianceFlags = setOf(ComplianceFlag.PERIOD_END_ADJUSTMENT),
                auditTrailId = auditTrailId
            )
        }
        
        /**
         * Create event for reversal entry
         */
        fun forReversalEntry(
            entryId: UUID,
            entryNumber: String,
            originalEntryId: UUID,
            originalDescription: String,
            postingDate: LocalDate,
            totalAmount: FinancialAmount,
            accountsAffected: Set<AccountCode>,
            postedBy: UUID,
            postedByName: String,
            fiscalPeriodId: UUID,
            fiscalYear: Int,
            auditTrailId: UUID
        ): JournalEntryPostedEvent {
            
            return JournalEntryPostedEvent(
                aggregateId = entryId,
                journalEntryNumber = entryNumber,
                entryType = JournalEntryType.REVERSAL,
                entrySource = JournalEntrySource.REVERSAL,
                entryStatus = JournalEntryStatus.POSTED,
                postingDate = postingDate,
                effectiveDate = postingDate,
                period = FiscalPeriod.fromDate(postingDate),
                fiscalYear = fiscalYear,
                fiscalPeriodId = fiscalPeriodId,
                description = "REVERSAL: $originalDescription",
                totalDebitAmount = totalAmount,
                totalCreditAmount = totalAmount,
                currency = totalAmount.currency,
                lineItemCount = 2,
                accountsAffected = accountsAffected,
                departmentsAffected = emptySet(),
                costCentersAffected = emptySet(),
                projectsAffected = emptySet(),
                originalEntryId = originalEntryId,
                isReversalEntry = true,
                approvalRequired = true,
                postedBy = postedBy,
                postedByName = postedByName,
                preparedBy = postedBy,
                preparedByName = postedByName,
                statementImpacts = determineStatementImpacts(accountsAffected),
                complianceFlags = setOf(ComplianceFlag.REVERSAL_ENTRY),
                auditTrailId = auditTrailId
            )
        }
        
        private fun determineStatementImpacts(accounts: Set<AccountCode>): Set<FinancialStatementType> {
            val impacts = mutableSetOf<FinancialStatementType>()
            
            accounts.forEach { account ->
                when (account.accountType) {
                    AccountType.ASSET, AccountType.LIABILITY, AccountType.EQUITY -> {
                        impacts.add(FinancialStatementType.BALANCE_SHEET)
                    }
                    AccountType.REVENUE, AccountType.EXPENSE -> {
                        impacts.add(FinancialStatementType.INCOME_STATEMENT)
                    }
                }
            }
            
            // Cash accounts always impact cash flow
            if (accounts.any { it.isCashAccount() }) {
                impacts.add(FinancialStatementType.CASH_FLOW)
            }
            
            return impacts
        }
    }
}

/**
 * Journal Entry Type Classifications
 */
enum class JournalEntryType {
    STANDARD,
    ADJUSTING,
    CLOSING,
    REVERSAL,
    ACCRUAL,
    PREPAYMENT,
    DEPRECIATION,
    REVALUATION,
    CONSOLIDATION,
    COMPLIANCE,
    REGULATORY,
    CORRECTION
}

/**
 * Journal Entry Source Classifications
 */
enum class JournalEntrySource {
    MANUAL_ENTRY,
    AUTOMATED_ACCRUAL,
    SYSTEM_GENERATED,
    PERIOD_END,
    DEPRECIATION_CALC,
    RECURRING_PROCESS,
    INTERFACE_IMPORT,
    REVERSAL,
    CORRECTION,
    CONSOLIDATION
}

/**
 * Journal Entry Status Values
 */
enum class JournalEntryStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    POSTED,
    REVERSED,
    CANCELLED
}

/**
 * Source Document Types
 */
enum class SourceDocumentType {
    INVOICE,
    RECEIPT,
    PURCHASE_ORDER,
    SALES_ORDER,
    BANK_STATEMENT,
    TIMESHEET,
    EXPENSE_REPORT,
    CONTRACT,
    ADJUSTMENT_MEMO,
    OTHER
}

/**
 * Recurring Frequency Options
 */
enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    ANNUALLY
}

/**
 * Financial Statement Types
 */
enum class FinancialStatementType {
    BALANCE_SHEET,
    INCOME_STATEMENT,
    CASH_FLOW,
    STATEMENT_OF_EQUITY,
    NOTES_TO_STATEMENTS
}

/**
 * Compliance Flag Types
 */
enum class ComplianceFlag {
    SOX_CONTROL,
    GAAP_REQUIREMENT,
    IFRS_REQUIREMENT,
    TAX_REGULATION,
    AUDIT_REQUIREMENT,
    PERIOD_END_ADJUSTMENT,
    REVERSAL_ENTRY,
    HIGH_VALUE_TRANSACTION,
    INTER_COMPANY,
    RELATED_PARTY
}

/**
 * Attachment Reference
 */
data class AttachmentReference(
    val attachmentId: UUID,
    val fileName: String,
    val fileType: String,
    val fileSize: Long,
    val description: String?
)

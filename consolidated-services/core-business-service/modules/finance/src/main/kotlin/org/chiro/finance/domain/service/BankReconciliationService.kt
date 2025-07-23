package org.chiro.finance.domain.service

import org.chiro.finance.domain.entity.Account
import org.chiro.finance.domain.entity.Transaction
import org.chiro.finance.domain.entity.TransactionLine
import org.chiro.finance.domain.repository.AccountRepository
import org.chiro.finance.domain.repository.JournalEntryRepository
import org.chiro.finance.domain.valueobject.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Bank Reconciliation Domain Service
 * 
 * Manages bank reconciliation processes including statement import,
 * transaction matching, reconciliation adjustments, and variance analysis.
 * 
 * This service orchestrates complex reconciliation operations that ensure
 * accuracy between bank statements and general ledger cash accounts.
 * 
 * Domain Service Pattern: Encapsulates reconciliation logic that spans
 * multiple entities and applies complex business rules for cash management.
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
@ApplicationScoped
class BankReconciliationService @Inject constructor(
    private val accountRepository: AccountRepository,
    private val journalEntryRepository: JournalEntryRepository,
    private val ledgerService: LedgerService
) {
    
    companion object {
        // Reconciliation configuration constants
        const val AUTO_MATCH_TOLERANCE = 0.01 // $0.01 tolerance for auto-matching
        const val BATCH_SIZE_LIMIT = 10000
        const val MAX_RECONCILIATION_PERIOD_DAYS = 90
        const val STALE_ITEM_THRESHOLD_DAYS = 30
        
        // Matching criteria weights
        const val AMOUNT_MATCH_WEIGHT = 0.4
        const val DATE_MATCH_WEIGHT = 0.3
        const val REFERENCE_MATCH_WEIGHT = 0.2
        const val DESCRIPTION_MATCH_WEIGHT = 0.1
        
        // Auto-matching thresholds
        const val HIGH_CONFIDENCE_THRESHOLD = 0.9
        const val MEDIUM_CONFIDENCE_THRESHOLD = 0.7
        const val LOW_CONFIDENCE_THRESHOLD = 0.5
        
        // Standard reconciliation item types
        val RECONCILING_ITEM_TYPES = setOf(
            ReconciliationItemType.OUTSTANDING_DEPOSIT,
            ReconciliationItemType.OUTSTANDING_CHECK,
            ReconciliationItemType.BANK_CHARGE,
            ReconciliationItemType.INTEREST_EARNED,
            ReconciliationItemType.NSF_CHECK,
            ReconciliationItemType.BANK_ERROR,
            ReconciliationItemType.BOOK_ERROR
        )
    }
    
    // ==================== RECONCILIATION PROCESS OPERATIONS ====================
    
    /**
     * Initiates a new bank reconciliation process
     */
    suspend fun initiateReconciliation(
        bankAccountId: AccountId,
        statementDate: LocalDate,
        statementEndingBalance: FinancialAmount,
        reconciliationPeriodStart: LocalDate,
        reconciledBy: String,
        statementId: String? = null
    ): ReconciliationInitiationResult {
        
        try {
            // Validate bank account
            val bankAccount = accountRepository.findById(bankAccountId)
                ?: return ReconciliationInitiationResult.failure(
                    errors = listOf("Bank account not found: $bankAccountId")
                )
            
            if (bankAccount.type != AccountType.CASH && bankAccount.type != AccountType.BANK) {
                return ReconciliationInitiationResult.failure(
                    errors = listOf("Account ${bankAccount.code} is not a cash/bank account")
                )
            }
            
            // Validate reconciliation period
            val periodLength = ChronoUnit.DAYS.between(reconciliationPeriodStart, statementDate)
            if (periodLength > MAX_RECONCILIATION_PERIOD_DAYS) {
                return ReconciliationInitiationResult.failure(
                    errors = listOf("Reconciliation period exceeds maximum allowed days ($MAX_RECONCILIATION_PERIOD_DAYS)")
                )
            }
            
            // Check for existing reconciliation in progress
            val existingReconciliation = findActiveReconciliation(bankAccountId, statementDate)
            if (existingReconciliation != null) {
                return ReconciliationInitiationResult.failure(
                    errors = listOf("Reconciliation already in progress for this account and period")
                )
            }
            
            // Get book balance as of statement date
            val bookBalanceResult = ledgerService.calculateAccountBalance(
                accountId = bankAccountId,
                asOfDate = statementDate,
                includeUnposted = false
            )
            
            if (!bookBalanceResult.success) {
                return ReconciliationInitiationResult.failure(
                    errors = listOf("Failed to calculate book balance: ${bookBalanceResult.errors.joinToString()}")
                )
            }
            
            val bookBalance = bookBalanceResult.balance
            
            // Get unreconciled transactions
            val unreconciledTransactions = getUnreconciledTransactions(
                bankAccountId = bankAccountId,
                startDate = reconciliationPeriodStart,
                endDate = statementDate
            )
            
            // Create reconciliation record
            val reconciliation = BankReconciliation.create(
                id = BankReconciliationId(UUID.randomUUID()),
                bankAccountId = bankAccountId,
                statementDate = statementDate,
                statementEndingBalance = statementEndingBalance,
                bookBalance = bookBalance,
                reconciliationPeriodStart = reconciliationPeriodStart,
                reconciledBy = reconciledBy,
                statementId = statementId,
                unreconciledTransactions = unreconciledTransactions
            )
            
            // Calculate initial variance
            val initialVariance = calculateInitialVariance(
                bookBalance = bookBalance,
                statementBalance = statementEndingBalance,
                unreconciledTransactions = unreconciledTransactions
            )
            
            return ReconciliationInitiationResult.success(
                reconciliation = reconciliation,
                initialVariance = initialVariance,
                unreconciledTransactionCount = unreconciledTransactions.size
            )
            
        } catch (e: Exception) {
            return ReconciliationInitiationResult.failure(
                errors = listOf("Reconciliation initiation failed: ${e.message}")
            )
        }
    }
    
    /**
     * Imports bank statement data for reconciliation
     */
    suspend fun importBankStatement(
        reconciliationId: BankReconciliationId,
        statementTransactions: List<BankStatementTransaction>,
        validateData: Boolean = true
    ): StatementImportResult {
        
        try {
            if (statementTransactions.size > BATCH_SIZE_LIMIT) {
                return StatementImportResult.failure(
                    errors = listOf("Statement transaction count exceeds batch limit ($BATCH_SIZE_LIMIT)")
                )
            }
            
            // Validate statement data if required
            if (validateData) {
                val validationResult = validateStatementTransactions(statementTransactions)
                if (!validationResult.isValid) {
                    return StatementImportResult.failure(
                        errors = validationResult.errors,
                        warnings = validationResult.warnings
                    )
                }
            }
            
            // Process and normalize statement transactions
            val processedTransactions = normalizeStatementTransactions(statementTransactions)
            
            // Detect duplicates
            val duplicates = detectDuplicateStatementTransactions(processedTransactions)
            if (duplicates.isNotEmpty()) {
                return StatementImportResult.failure(
                    errors = listOf("${duplicates.size} duplicate transactions detected"),
                    warnings = duplicates.map { "Duplicate: ${it.referenceNumber} - ${it.amount}" }
                )
            }
            
            // Categorize transactions
            val categorizedTransactions = categorizeStatementTransactions(processedTransactions)
            
            return StatementImportResult.success(
                reconciliationId = reconciliationId,
                importedTransactions = processedTransactions,
                categorizedTransactions = categorizedTransactions,
                importedAt = LocalDateTime.now()
            )
            
        } catch (e: Exception) {
            return StatementImportResult.failure(
                errors = listOf("Statement import failed: ${e.message}")
            )
        }
    }
    
    /**
     * Performs automatic matching of book and bank transactions
     */
    suspend fun performAutoMatching(
        reconciliationId: BankReconciliationId,
        confidenceThreshold: Double = MEDIUM_CONFIDENCE_THRESHOLD,
        enableMachineLearning: Boolean = false
    ): AutoMatchingResult {
        
        try {
            val reconciliation = getReconciliation(reconciliationId)
                ?: return AutoMatchingResult.failure(
                    errors = listOf("Reconciliation not found: $reconciliationId")
                )
            
            val statementTransactions = getStatementTransactions(reconciliationId)
            val bookTransactions = reconciliation.unreconciledTransactions
            
            val matches = mutableListOf<TransactionMatch>()
            val unmatchedStatementTransactions = mutableListOf<BankStatementTransaction>()
            val unmatchedBookTransactions = mutableListOf<UnreconciledTransaction>()
            
            // Create matching matrix
            val matchingResults = createMatchingMatrix(bookTransactions, statementTransactions)
            
            // Process matches by confidence level
            val highConfidenceMatches = matchingResults.filter { it.confidence >= HIGH_CONFIDENCE_THRESHOLD }
            val mediumConfidenceMatches = matchingResults.filter { 
                it.confidence >= MEDIUM_CONFIDENCE_THRESHOLD && it.confidence < HIGH_CONFIDENCE_THRESHOLD 
            }
            val lowConfidenceMatches = matchingResults.filter { 
                it.confidence >= LOW_CONFIDENCE_THRESHOLD && it.confidence < MEDIUM_CONFIDENCE_THRESHOLD 
            }
            
            // Auto-approve high confidence matches
            for (match in highConfidenceMatches) {
                if (match.confidence >= confidenceThreshold) {
                    matches.add(TransactionMatch(
                        bookTransaction = match.bookTransaction,
                        statementTransaction = match.statementTransaction,
                        matchType = MatchType.AUTOMATIC,
                        confidence = match.confidence,
                        matchCriteria = match.matchCriteria,
                        matchedAt = LocalDateTime.now(),
                        matchedBy = "AUTO_MATCHING_SYSTEM"
                    ))
                }
            }
            
            // Collect suggested matches for manual review
            val suggestedMatches = mediumConfidenceMatches + lowConfidenceMatches
            
            // Identify unmatched transactions
            val matchedBookTransactionIds = matches.map { it.bookTransaction.transactionId }.toSet()
            val matchedStatementTransactionIds = matches.map { it.statementTransaction.id }.toSet()
            
            unmatchedBookTransactions.addAll(
                bookTransactions.filter { it.transactionId !in matchedBookTransactionIds }
            )
            unmatchedStatementTransactions.addAll(
                statementTransactions.filter { it.id !in matchedStatementTransactionIds }
            )
            
            // Update reconciliation with matches
            updateReconciliationWithMatches(reconciliationId, matches)
            
            return AutoMatchingResult.success(
                reconciliationId = reconciliationId,
                automaticMatches = matches,
                suggestedMatches = suggestedMatches,
                unmatchedBookTransactions = unmatchedBookTransactions,
                unmatchedStatementTransactions = unmatchedStatementTransactions,
                matchingStatistics = MatchingStatistics(
                    totalBookTransactions = bookTransactions.size,
                    totalStatementTransactions = statementTransactions.size,
                    automaticMatches = matches.size,
                    suggestedMatches = suggestedMatches.size,
                    unmatchedBook = unmatchedBookTransactions.size,
                    unmatchedStatement = unmatchedStatementTransactions.size
                )
            )
            
        } catch (e: Exception) {
            return AutoMatchingResult.failure(
                errors = listOf("Auto-matching failed: ${e.message}")
            )
        }
    }
    
    /**
     * Manually matches a book transaction with a statement transaction
     */
    suspend fun createManualMatch(
        reconciliationId: BankReconciliationId,
        bookTransactionId: JournalEntryId,
        statementTransactionId: BankStatementTransactionId,
        matchReason: String,
        matchedBy: String
    ): ManualMatchResult {
        
        try {
            val reconciliation = getReconciliation(reconciliationId)
                ?: return ManualMatchResult.failure(
                    errors = listOf("Reconciliation not found: $reconciliationId")
                )
            
            // Validate transactions exist and are unmatched
            val bookTransaction = reconciliation.unreconciledTransactions
                .find { it.transactionId == bookTransactionId }
                ?: return ManualMatchResult.failure(
                    errors = listOf("Book transaction not found or already matched: $bookTransactionId")
                )
            
            val statementTransaction = getStatementTransaction(statementTransactionId)
                ?: return ManualMatchResult.failure(
                    errors = listOf("Statement transaction not found: $statementTransactionId")
                )
            
            // Check if either transaction is already matched
            val existingMatch = findExistingMatch(reconciliationId, bookTransactionId, statementTransactionId)
            if (existingMatch != null) {
                return ManualMatchResult.failure(
                    errors = listOf("One or both transactions are already matched")
                )
            }
            
            // Validate match reasonableness
            val matchValidation = validateManualMatch(bookTransaction, statementTransaction)
            if (!matchValidation.isValid) {
                return ManualMatchResult.failure(
                    errors = matchValidation.errors,
                    warnings = matchValidation.warnings
                )
            }
            
            // Create manual match
            val match = TransactionMatch(
                bookTransaction = bookTransaction,
                statementTransaction = statementTransaction,
                matchType = MatchType.MANUAL,
                confidence = 1.0, // Manual matches are 100% confidence
                matchCriteria = listOf("Manual match: $matchReason"),
                matchedAt = LocalDateTime.now(),
                matchedBy = matchedBy
            )
            
            // Update reconciliation
            addMatchToReconciliation(reconciliationId, match)
            
            // Create audit trail
            createMatchAuditEntry(reconciliationId, match, "MANUAL_MATCH_CREATED")
            
            return ManualMatchResult.success(
                match = match,
                reconciliationId = reconciliationId
            )
            
        } catch (e: Exception) {
            return ManualMatchResult.failure(
                errors = listOf("Manual match creation failed: ${e.message}")
            )
        }
    }
    
    /**
     * Breaks an existing match between transactions
     */
    suspend fun breakMatch(
        reconciliationId: BankReconciliationId,
        matchId: TransactionMatchId,
        breakReason: String,
        brokenBy: String
    ): BreakMatchResult {
        
        try {
            val reconciliation = getReconciliation(reconciliationId)
                ?: return BreakMatchResult.failure(
                    errors = listOf("Reconciliation not found: $reconciliationId")
                )
            
            val match = findMatchById(reconciliationId, matchId)
                ?: return BreakMatchResult.failure(
                    errors = listOf("Match not found: $matchId")
                )
            
            // Validate match can be broken
            val breakValidation = validateMatchBreak(reconciliation, match)
            if (!breakValidation.isValid) {
                return BreakMatchResult.failure(
                    errors = breakValidation.errors
                )
            }
            
            // Remove match from reconciliation
            removeMatchFromReconciliation(reconciliationId, matchId)
            
            // Add transactions back to unmatched lists
            addToUnmatchedTransactions(reconciliation, match)
            
            // Create audit trail
            createMatchAuditEntry(reconciliationId, match, "MATCH_BROKEN", breakReason)
            
            return BreakMatchResult.success(
                brokenMatch = match,
                reconciliationId = reconciliationId,
                brokenAt = LocalDateTime.now()
            )
            
        } catch (e: Exception) {
            return BreakMatchResult.failure(
                errors = listOf("Match break failed: ${e.message}")
            )
        }
    }
    
    // ==================== RECONCILING ITEMS OPERATIONS ====================
    
    /**
     * Adds a reconciling item to the reconciliation
     */
    suspend fun addReconcilingItem(
        reconciliationId: BankReconciliationId,
        itemType: ReconciliationItemType,
        amount: FinancialAmount,
        description: String,
        referenceNumber: String? = null,
        addedBy: String,
        createAdjustingEntry: Boolean = true
    ): ReconcilingItemResult {
        
        try {
            val reconciliation = getReconciliation(reconciliationId)
                ?: return ReconcilingItemResult.failure(
                    errors = listOf("Reconciliation not found: $reconciliationId")
                )
            
            // Validate reconciling item
            val validationResult = validateReconcilingItem(itemType, amount, description)
            if (!validationResult.isValid) {
                return ReconcilingItemResult.failure(
                    errors = validationResult.errors
                )
            }
            
            // Create reconciling item
            val reconcilingItem = ReconciliationItem.create(
                id = ReconciliationItemId(UUID.randomUUID()),
                reconciliationId = reconciliationId,
                itemType = itemType,
                amount = amount,
                description = description,
                referenceNumber = referenceNumber,
                addedBy = addedBy,
                addedAt = LocalDateTime.now()
            )
            
            // Add to reconciliation
            addItemToReconciliation(reconciliationId, reconcilingItem)
            
            var adjustingEntryId: JournalEntryId? = null
            
            // Create adjusting entry if requested
            if (createAdjustingEntry) {
                val adjustingEntry = createAdjustingEntry(reconciliation, reconcilingItem, addedBy)
                adjustingEntryId = adjustingEntry.id
                
                // Post the adjusting entry
                ledgerService.postJournalEntry(adjustingEntry, LocalDate.now(), addedBy, true)
            }
            
            // Update reconciliation variance
            updateReconciliationVariance(reconciliationId)
            
            return ReconcilingItemResult.success(
                reconcilingItem = reconcilingItem,
                adjustingEntryId = adjustingEntryId,
                reconciliationId = reconciliationId
            )
            
        } catch (e: Exception) {
            return ReconcilingItemResult.failure(
                errors = listOf("Reconciling item addition failed: ${e.message}")
            )
        }
    }
    
    /**
     * Removes a reconciling item from the reconciliation
     */
    suspend fun removeReconcilingItem(
        reconciliationId: BankReconciliationId,
        itemId: ReconciliationItemId,
        removeReason: String,
        removedBy: String,
        reverseAdjustingEntry: Boolean = true
    ): RemoveReconcilingItemResult {
        
        try {
            val reconciliation = getReconciliation(reconciliationId)
                ?: return RemoveReconcilingItemResult.failure(
                    errors = listOf("Reconciliation not found: $reconciliationId")
                )
            
            val reconcilingItem = findReconcilingItemById(reconciliationId, itemId)
                ?: return RemoveReconcilingItemResult.failure(
                    errors = listOf("Reconciling item not found: $itemId")
                )
            
            // Validate item can be removed
            val removeValidation = validateReconcilingItemRemoval(reconciliation, reconcilingItem)
            if (!removeValidation.isValid) {
                return RemoveReconcilingItemResult.failure(
                    errors = removeValidation.errors
                )
            }
            
            var reversalEntryId: JournalEntryId? = null
            
            // Reverse adjusting entry if it exists and reversal is requested
            if (reverseAdjustingEntry && reconcilingItem.adjustingEntryId != null) {
                val reversalResult = ledgerService.reverseJournalEntry(
                    originalTransactionId = reconcilingItem.adjustingEntryId!!,
                    reversalReason = "Reconciling item removed: $removeReason",
                    reversedBy = removedBy
                )
                
                if (!reversalResult.success) {
                    return RemoveReconcilingItemResult.failure(
                        errors = listOf("Failed to reverse adjusting entry: ${reversalResult.errors.joinToString()}")
                    )
                }
                
                reversalEntryId = reversalResult.reversalTransactionId
            }
            
            // Remove item from reconciliation
            removeItemFromReconciliation(reconciliationId, itemId)
            
            // Update reconciliation variance
            updateReconciliationVariance(reconciliationId)
            
            return RemoveReconcilingItemResult.success(
                removedItem = reconcilingItem,
                reversalEntryId = reversalEntryId,
                reconciliationId = reconciliationId,
                removedAt = LocalDateTime.now()
            )
            
        } catch (e: Exception) {
            return RemoveReconcilingItemResult.failure(
                errors = listOf("Reconciling item removal failed: ${e.message}")
            )
        }
    }
    
    // ==================== RECONCILIATION COMPLETION OPERATIONS ====================
    
    /**
     * Completes the bank reconciliation process
     */
    suspend fun completeReconciliation(
        reconciliationId: BankReconciliationId,
        completedBy: String,
        forceComplete: Boolean = false,
        allowVariance: Boolean = false,
        maxVarianceThreshold: FinancialAmount? = null
    ): ReconciliationCompletionResult {
        
        try {
            val reconciliation = getReconciliation(reconciliationId)
                ?: return ReconciliationCompletionResult.failure(
                    errors = listOf("Reconciliation not found: $reconciliationId")
                )
            
            // Validate reconciliation can be completed
            val completionValidation = validateReconciliationCompletion(
                reconciliation = reconciliation,
                allowVariance = allowVariance,
                maxVarianceThreshold = maxVarianceThreshold
            )
            
            if (!completionValidation.isValid && !forceComplete) {
                return ReconciliationCompletionResult.failure(
                    errors = completionValidation.errors,
                    warnings = completionValidation.warnings
                )
            }
            
            // Calculate final reconciliation variance
            val finalVariance = calculateFinalVariance(reconciliation)
            
            // Generate reconciliation summary
            val summary = generateReconciliationSummary(reconciliation, finalVariance)
            
            // Mark reconciliation as completed
            val completedReconciliation = reconciliation.complete(
                completedBy = completedBy,
                completedAt = LocalDateTime.now(),
                finalVariance = finalVariance,
                summary = summary
            )
            
            // Save completed reconciliation
            saveReconciliation(completedReconciliation)
            
            // Update account reconciliation status
            updateAccountReconciliationStatus(
                accountId = reconciliation.bankAccountId,
                lastReconciledDate = reconciliation.statementDate,
                lastReconciledBalance = reconciliation.statementEndingBalance
            )
            
            // Create completion audit entry
            createReconciliationAuditEntry(
                reconciliationId = reconciliationId,
                action = "RECONCILIATION_COMPLETED",
                performedBy = completedBy,
                details = "Reconciliation completed with final variance: $finalVariance"
            )
            
            return ReconciliationCompletionResult.success(
                completedReconciliation = completedReconciliation,
                finalVariance = finalVariance,
                summary = summary,
                completedAt = LocalDateTime.now()
            )
            
        } catch (e: Exception) {
            return ReconciliationCompletionResult.failure(
                errors = listOf("Reconciliation completion failed: ${e.message}")
            )
        }
    }
    
    /**
     * Generates reconciliation report
     */
    suspend fun generateReconciliationReport(
        reconciliationId: BankReconciliationId,
        includeTransactionDetails: Boolean = true,
        includeReconcilingItems: Boolean = true,
        reportFormat: ReconciliationReportFormat = ReconciliationReportFormat.DETAILED
    ): ReconciliationReportResult {
        
        try {
            val reconciliation = getReconciliation(reconciliationId)
                ?: return ReconciliationReportResult.failure(
                    errors = listOf("Reconciliation not found: $reconciliationId")
                )
            
            val matches = getReconciliationMatches(reconciliationId)
            val reconcilingItems = getReconcilingItems(reconciliationId)
            val unmatchedTransactions = getUnmatchedTransactions(reconciliationId)
            
            // Build report sections
            val reportSections = mutableListOf<ReconciliationReportSection>()
            
            // Summary section
            reportSections.add(buildSummarySection(reconciliation))
            
            // Matched transactions section
            if (includeTransactionDetails && matches.isNotEmpty()) {
                reportSections.add(buildMatchedTransactionsSection(matches))
            }
            
            // Reconciling items section
            if (includeReconcilingItems && reconcilingItems.isNotEmpty()) {
                reportSections.add(buildReconcilingItemsSection(reconcilingItems))
            }
            
            // Unmatched transactions section
            if (includeTransactionDetails && unmatchedTransactions.isNotEmpty()) {
                reportSections.add(buildUnmatchedTransactionsSection(unmatchedTransactions))
            }
            
            // Variance analysis section
            reportSections.add(buildVarianceAnalysisSection(reconciliation))
            
            val report = ReconciliationReport(
                reconciliationId = reconciliationId,
                bankAccountId = reconciliation.bankAccountId,
                statementDate = reconciliation.statementDate,
                reportFormat = reportFormat,
                sections = reportSections,
                generatedAt = LocalDateTime.now(),
                generatedBy = "BankReconciliationService"
            )
            
            return ReconciliationReportResult.success(report)
            
        } catch (e: Exception) {
            return ReconciliationReportResult.failure(
                errors = listOf("Reconciliation report generation failed: ${e.message}")
            )
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private suspend fun getUnreconciledTransactions(
        bankAccountId: AccountId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<UnreconciledTransaction> {
        
        val transactions = journalEntryRepository.findByAccount(
            accountId = bankAccountId,
            startDate = startDate,
            endDate = endDate
        )
        
        return transactions.content
            .filter { it.status == TransactionStatus.POSTED }
            .filter { !it.isReconciled }
            .flatMap { transaction ->
                transaction.lines
                    .filter { line -> line.accountId == bankAccountId }
                    .map { line ->
                        UnreconciledTransaction(
                            transactionId = transaction.id,
                            transactionDate = transaction.transactionDate,
                            amount = line.amount,
                            transactionType = line.type,
                            description = line.description ?: transaction.description,
                            referenceNumber = transaction.referenceNumber
                        )
                    }
            }
    }
    
    private fun calculateInitialVariance(
        bookBalance: FinancialAmount,
        statementBalance: FinancialAmount,
        unreconciledTransactions: List<UnreconciledTransaction>
    ): FinancialAmount {
        
        val outstandingDeposits = unreconciledTransactions
            .filter { it.transactionType == TransactionLineType.DEBIT }
            .map { it.amount }
            .fold(FinancialAmount.zero(bookBalance.currency)) { acc, amount -> acc.add(amount) }
        
        val outstandingChecks = unreconciledTransactions
            .filter { it.transactionType == TransactionLineType.CREDIT }
            .map { it.amount }
            .fold(FinancialAmount.zero(bookBalance.currency)) { acc, amount -> acc.add(amount) }
        
        val adjustedBookBalance = bookBalance.add(outstandingDeposits).subtract(outstandingChecks)
        return statementBalance.subtract(adjustedBookBalance)
    }
    
    private fun validateStatementTransactions(
        transactions: List<BankStatementTransaction>
    ): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check for required fields
        transactions.forEachIndexed { index, transaction ->
            if (transaction.amount.isZero) {
                warnings.add("Transaction $index has zero amount")
            }
            
            if (transaction.description.isBlank()) {
                warnings.add("Transaction $index has blank description")
            }
            
            if (transaction.transactionDate == null) {
                errors.add("Transaction $index missing transaction date")
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    private fun normalizeStatementTransactions(
        transactions: List<BankStatementTransaction>
    ): List<BankStatementTransaction> {
        
        return transactions.map { transaction ->
            transaction.copy(
                description = transaction.description.trim().uppercase(),
                referenceNumber = transaction.referenceNumber?.trim()?.uppercase()
            )
        }
    }
    
    private fun detectDuplicateStatementTransactions(
        transactions: List<BankStatementTransaction>
    ): List<BankStatementTransaction> {
        
        val seen = mutableSetOf<String>()
        val duplicates = mutableListOf<BankStatementTransaction>()
        
        for (transaction in transactions) {
            val key = "${transaction.transactionDate}_${transaction.amount}_${transaction.description}"
            if (!seen.add(key)) {
                duplicates.add(transaction)
            }
        }
        
        return duplicates
    }
    
    private fun categorizeStatementTransactions(
        transactions: List<BankStatementTransaction>
    ): Map<StatementTransactionCategory, List<BankStatementTransaction>> {
        
        val categorized = mutableMapOf<StatementTransactionCategory, MutableList<BankStatementTransaction>>()
        
        for (transaction in transactions) {
            val category = determineTransactionCategory(transaction)
            categorized.getOrPut(category) { mutableListOf() }.add(transaction)
        }
        
        return categorized.mapValues { it.value.toList() }
    }
    
    private fun determineTransactionCategory(transaction: BankStatementTransaction): StatementTransactionCategory {
        val description = transaction.description.lowercase()
        
        return when {
            description.contains("deposit") || transaction.amount.isPositive -> StatementTransactionCategory.DEPOSIT
            description.contains("check") || description.contains("chk") -> StatementTransactionCategory.CHECK
            description.contains("fee") || description.contains("charge") -> StatementTransactionCategory.BANK_FEE
            description.contains("interest") -> StatementTransactionCategory.INTEREST
            description.contains("transfer") -> StatementTransactionCategory.TRANSFER
            description.contains("withdrawal") || description.contains("wd") -> StatementTransactionCategory.WITHDRAWAL
            else -> StatementTransactionCategory.OTHER
        }
    }
    
    private fun createMatchingMatrix(
        bookTransactions: List<UnreconciledTransaction>,
        statementTransactions: List<BankStatementTransaction>
    ): List<PotentialMatch> {
        
        val potentialMatches = mutableListOf<PotentialMatch>()
        
        for (bookTransaction in bookTransactions) {
            for (statementTransaction in statementTransactions) {
                val confidence = calculateMatchConfidence(bookTransaction, statementTransaction)
                if (confidence >= LOW_CONFIDENCE_THRESHOLD) {
                    potentialMatches.add(PotentialMatch(
                        bookTransaction = bookTransaction,
                        statementTransaction = statementTransaction,
                        confidence = confidence,
                        matchCriteria = generateMatchCriteria(bookTransaction, statementTransaction)
                    ))
                }
            }
        }
        
        return potentialMatches.sortedByDescending { it.confidence }
    }
    
    private fun calculateMatchConfidence(
        bookTransaction: UnreconciledTransaction,
        statementTransaction: BankStatementTransaction
    ): Double {
        
        var confidence = 0.0
        
        // Amount matching
        val amountDifference = (bookTransaction.amount.amount - statementTransaction.amount.amount).abs()
        val amountScore = if (amountDifference <= BigDecimal(AUTO_MATCH_TOLERANCE)) 1.0
            else maxOf(0.0, 1.0 - (amountDifference.toDouble() / bookTransaction.amount.amount.toDouble()))
        confidence += amountScore * AMOUNT_MATCH_WEIGHT
        
        // Date matching
        val daysDifference = ChronoUnit.DAYS.between(bookTransaction.transactionDate, statementTransaction.transactionDate).abs()
        val dateScore = when {
            daysDifference == 0L -> 1.0
            daysDifference <= 1L -> 0.9
            daysDifference <= 3L -> 0.7
            daysDifference <= 7L -> 0.5
            else -> 0.0
        }
        confidence += dateScore * DATE_MATCH_WEIGHT
        
        // Reference number matching
        val referenceScore = if (bookTransaction.referenceNumber != null && 
                                statementTransaction.referenceNumber != null &&
                                bookTransaction.referenceNumber == statementTransaction.referenceNumber) 1.0
            else 0.0
        confidence += referenceScore * REFERENCE_MATCH_WEIGHT
        
        // Description similarity
        val descriptionScore = calculateDescriptionSimilarity(
            bookTransaction.description, 
            statementTransaction.description
        )
        confidence += descriptionScore * DESCRIPTION_MATCH_WEIGHT
        
        return confidence
    }
    
    private fun calculateDescriptionSimilarity(desc1: String, desc2: String): Double {
        // Simplified similarity calculation
        val words1 = desc1.lowercase().split(Regex("\\s+")).toSet()
        val words2 = desc2.lowercase().split(Regex("\\s+")).toSet()
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        return if (union > 0) intersection.toDouble() / union.toDouble() else 0.0
    }
    
    private fun generateMatchCriteria(
        bookTransaction: UnreconciledTransaction,
        statementTransaction: BankStatementTransaction
    ): List<String> {
        
        val criteria = mutableListOf<String>()
        
        val amountDifference = (bookTransaction.amount.amount - statementTransaction.amount.amount).abs()
        if (amountDifference <= BigDecimal(AUTO_MATCH_TOLERANCE)) {
            criteria.add("Exact amount match")
        } else {
            criteria.add("Amount variance: $amountDifference")
        }
        
        val daysDifference = ChronoUnit.DAYS.between(bookTransaction.transactionDate, statementTransaction.transactionDate).abs()
        when {
            daysDifference == 0L -> criteria.add("Same date")
            daysDifference <= 3L -> criteria.add("Date within $daysDifference days")
            else -> criteria.add("Date difference: $daysDifference days")
        }
        
        if (bookTransaction.referenceNumber == statementTransaction.referenceNumber) {
            criteria.add("Reference number match")
        }
        
        val descSimilarity = calculateDescriptionSimilarity(bookTransaction.description, statementTransaction.description)
        if (descSimilarity > 0.7) {
            criteria.add("High description similarity")
        } else if (descSimilarity > 0.3) {
            criteria.add("Moderate description similarity")
        }
        
        return criteria
    }
    
    private fun validateManualMatch(
        bookTransaction: UnreconciledTransaction,
        statementTransaction: BankStatementTransaction
    ): ValidationResult {
        val warnings = mutableListOf<String>()
        
        // Check for significant amount differences
        val amountDifference = (bookTransaction.amount.amount - statementTransaction.amount.amount).abs()
        if (amountDifference > BigDecimal("100.00")) {
            warnings.add("Large amount difference: $amountDifference")
        }
        
        // Check for significant date differences
        val daysDifference = ChronoUnit.DAYS.between(bookTransaction.transactionDate, statementTransaction.transactionDate).abs()
        if (daysDifference > 30) {
            warnings.add("Large date difference: $daysDifference days")
        }
        
        return ValidationResult(
            isValid = true, // Manual matches are allowed with warnings
            warnings = warnings
        )
    }
    
    private fun validateReconcilingItem(
        itemType: ReconciliationItemType,
        amount: FinancialAmount,
        description: String
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (amount.isZero) {
            errors.add("Reconciling item amount cannot be zero")
        }
        
        if (description.isBlank()) {
            errors.add("Reconciling item description is required")
        }
        
        if (itemType !in RECONCILING_ITEM_TYPES) {
            errors.add("Invalid reconciling item type: $itemType")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    private fun createAdjustingEntry(
        reconciliation: BankReconciliation,
        reconcilingItem: ReconciliationItem,
        createdBy: String
    ): Transaction {
        
        val lines = when (reconcilingItem.itemType) {
            ReconciliationItemType.BANK_CHARGE -> listOf(
                TransactionLine.create(
                    accountId = getExpenseAccountForBankCharge(),
                    type = TransactionLineType.DEBIT,
                    amount = reconcilingItem.amount,
                    description = reconcilingItem.description
                ),
                TransactionLine.create(
                    accountId = reconciliation.bankAccountId,
                    type = TransactionLineType.CREDIT,
                    amount = reconcilingItem.amount,
                    description = reconcilingItem.description
                )
            )
            
            ReconciliationItemType.INTEREST_EARNED -> listOf(
                TransactionLine.create(
                    accountId = reconciliation.bankAccountId,
                    type = TransactionLineType.DEBIT,
                    amount = reconcilingItem.amount,
                    description = reconcilingItem.description
                ),
                TransactionLine.create(
                    accountId = getIncomeAccountForInterest(),
                    type = TransactionLineType.CREDIT,
                    amount = reconcilingItem.amount,
                    description = reconcilingItem.description
                )
            )
            
            else -> throw IllegalArgumentException("Unsupported reconciling item type for adjusting entry: ${reconcilingItem.itemType}")
        }
        
        return Transaction.create(
            referenceNumber = "ADJ-${reconcilingItem.id}",
            description = "Bank reconciliation adjustment: ${reconcilingItem.description}",
            transactionDate = LocalDate.now(),
            transactionType = TransactionType.BANK_RECONCILIATION_ADJUSTMENT,
            currency = reconcilingItem.amount.currency,
            lines = lines,
            createdBy = createdBy
        )
    }
    
    // Placeholder methods - would be implemented based on specific chart of accounts
    private fun getExpenseAccountForBankCharge(): AccountId = AccountId(UUID.randomUUID())
    private fun getIncomeAccountForInterest(): AccountId = AccountId(UUID.randomUUID())
    
    // Additional placeholder methods for complex operations
    private suspend fun findActiveReconciliation(bankAccountId: AccountId, statementDate: LocalDate): BankReconciliation? = null
    private suspend fun getReconciliation(reconciliationId: BankReconciliationId): BankReconciliation? = null
    private suspend fun getStatementTransactions(reconciliationId: BankReconciliationId): List<BankStatementTransaction> = emptyList()
    private suspend fun getStatementTransaction(statementTransactionId: BankStatementTransactionId): BankStatementTransaction? = null
    private suspend fun updateReconciliationWithMatches(reconciliationId: BankReconciliationId, matches: List<TransactionMatch>) {}
    private suspend fun findExistingMatch(reconciliationId: BankReconciliationId, bookTransactionId: JournalEntryId, statementTransactionId: BankStatementTransactionId): TransactionMatch? = null
    private suspend fun addMatchToReconciliation(reconciliationId: BankReconciliationId, match: TransactionMatch) {}
    private suspend fun findMatchById(reconciliationId: BankReconciliationId, matchId: TransactionMatchId): TransactionMatch? = null
    private suspend fun removeMatchFromReconciliation(reconciliationId: BankReconciliationId, matchId: TransactionMatchId) {}
    private suspend fun addToUnmatchedTransactions(reconciliation: BankReconciliation, match: TransactionMatch) {}
    private suspend fun addItemToReconciliation(reconciliationId: BankReconciliationId, item: ReconciliationItem) {}
    private suspend fun removeItemFromReconciliation(reconciliationId: BankReconciliationId, itemId: ReconciliationItemId) {}
    private suspend fun findReconcilingItemById(reconciliationId: BankReconciliationId, itemId: ReconciliationItemId): ReconciliationItem? = null
    private suspend fun updateReconciliationVariance(reconciliationId: BankReconciliationId) {}
    private suspend fun saveReconciliation(reconciliation: BankReconciliation) {}
    private suspend fun updateAccountReconciliationStatus(accountId: AccountId, lastReconciledDate: LocalDate, lastReconciledBalance: FinancialAmount) {}
    private fun validateReconciliationCompletion(reconciliation: BankReconciliation, allowVariance: Boolean, maxVarianceThreshold: FinancialAmount?): ValidationResult = ValidationResult(true)
    private fun validateMatchBreak(reconciliation: BankReconciliation, match: TransactionMatch): ValidationResult = ValidationResult(true)
    private fun validateReconcilingItemRemoval(reconciliation: BankReconciliation, item: ReconciliationItem): ValidationResult = ValidationResult(true)
    private fun calculateFinalVariance(reconciliation: BankReconciliation): FinancialAmount = FinancialAmount.zero(Currency.USD)
    private fun generateReconciliationSummary(reconciliation: BankReconciliation, finalVariance: FinancialAmount): ReconciliationSummary = ReconciliationSummary.empty()
    private suspend fun getReconciliationMatches(reconciliationId: BankReconciliationId): List<TransactionMatch> = emptyList()
    private suspend fun getReconcilingItems(reconciliationId: BankReconciliationId): List<ReconciliationItem> = emptyList()
    private suspend fun getUnmatchedTransactions(reconciliationId: BankReconciliationId): UnmatchedTransactions = UnmatchedTransactions.empty()
    private fun buildSummarySection(reconciliation: BankReconciliation): ReconciliationReportSection = ReconciliationReportSection.empty()
    private fun buildMatchedTransactionsSection(matches: List<TransactionMatch>): ReconciliationReportSection = ReconciliationReportSection.empty()
    private fun buildReconcilingItemsSection(items: List<ReconciliationItem>): ReconciliationReportSection = ReconciliationReportSection.empty()
    private fun buildUnmatchedTransactionsSection(unmatched: UnmatchedTransactions): ReconciliationReportSection = ReconciliationReportSection.empty()
    private fun buildVarianceAnalysisSection(reconciliation: BankReconciliation): ReconciliationReportSection = ReconciliationReportSection.empty()
    
    private suspend fun createMatchAuditEntry(reconciliationId: BankReconciliationId, match: TransactionMatch, action: String, details: String? = null) {}
    private suspend fun createReconciliationAuditEntry(reconciliationId: BankReconciliationId, action: String, performedBy: String, details: String) {}
}

// ==================== ENUMS ====================

enum class ReconciliationItemType {
    OUTSTANDING_DEPOSIT,
    OUTSTANDING_CHECK,
    BANK_CHARGE,
    INTEREST_EARNED,
    NSF_CHECK,
    BANK_ERROR,
    BOOK_ERROR
}

enum class MatchType {
    AUTOMATIC, MANUAL, SUGGESTED
}

enum class StatementTransactionCategory {
    DEPOSIT, CHECK, BANK_FEE, INTEREST, TRANSFER, WITHDRAWAL, OTHER
}

enum class ReconciliationReportFormat {
    SUMMARY, DETAILED, COMPREHENSIVE
}

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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * General Ledger Domain Service
 * 
 * Manages the general ledger operations including posting transactions,
 * calculating balances, generating reports, and maintaining the integrity
 * of the double-entry bookkeeping system.
 * 
 * This service orchestrates complex ledger operations that span multiple
 * entities and enforces business rules for financial accounting.
 * 
 * Domain Service Pattern: Encapsulates domain logic that doesn't naturally
 * fit within a single entity or value object.
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
@ApplicationScoped
class LedgerService @Inject constructor(
    private val accountRepository: AccountRepository,
    private val journalEntryRepository: JournalEntryRepository
) {
    
    companion object {
        // Ledger configuration constants
        const val MAX_POSTING_RETRIES = 3
        const val BALANCE_TOLERANCE = 0.01 // Tolerance for balance discrepancies
        const val MAX_BATCH_SIZE = 1000
        const val AUTO_REVERSAL_CUTOFF_DAYS = 90
        
        // Standard posting rules
        val DEBIT_ACCOUNT_TYPES = setOf(
            AccountType.ASSETS, AccountType.EXPENSES, AccountType.DIVIDEND_DISTRIBUTIONS
        )
        val CREDIT_ACCOUNT_TYPES = setOf(
            AccountType.LIABILITIES, AccountType.EQUITY, AccountType.REVENUE
        )
    }
    
    // ==================== TRANSACTION POSTING OPERATIONS ====================
    
    /**
     * Posts a single journal entry to the general ledger
     */
    suspend fun postJournalEntry(
        transaction: Transaction,
        postingDate: LocalDate = LocalDate.now(),
        postedBy: String,
        validateBalances: Boolean = true
    ): PostingResult {
        
        try {
            // Pre-posting validation
            val validationResult = validateTransactionForPosting(transaction)
            if (!validationResult.isValid) {
                return PostingResult.failure(
                    transactionId = transaction.id,
                    errors = validationResult.errors,
                    warnings = validationResult.warnings
                )
            }
            
            // Check if already posted
            if (transaction.status == TransactionStatus.POSTED) {
                return PostingResult.warning(
                    transactionId = transaction.id,
                    warnings = listOf("Transaction is already posted")
                )
            }
            
            // Verify account balances if required
            if (validateBalances) {
                val balanceValidation = validateAccountBalances(transaction)
                if (!balanceValidation.isValid) {
                    return PostingResult.failure(
                        transactionId = transaction.id,
                        errors = balanceValidation.errors
                    )
                }
            }
            
            // Post the transaction
            val postedTransaction = transaction.post(postedBy, postingDate)
            val savedTransaction = journalEntryRepository.save(postedTransaction)
            
            // Update account balances
            updateAccountBalances(savedTransaction)
            
            // Create audit trail
            createAuditEntry(
                transactionId = savedTransaction.id,
                action = "POSTED",
                performedBy = postedBy,
                details = "Transaction posted to general ledger"
            )
            
            return PostingResult.success(
                transactionId = savedTransaction.id,
                postedAt = postingDate
            )
            
        } catch (e: Exception) {
            return PostingResult.failure(
                transactionId = transaction.id,
                errors = listOf("Posting failed: ${e.message}")
            )
        }
    }
    
    /**
     * Posts multiple journal entries in a batch
     */
    suspend fun batchPostJournalEntries(
        transactions: List<Transaction>,
        postingDate: LocalDate = LocalDate.now(),
        postedBy: String,
        continueOnError: Boolean = false
    ): BatchPostingResult {
        
        val results = mutableListOf<PostingResult>()
        val successfulPosts = mutableListOf<JournalEntryId>()
        val failedPosts = mutableListOf<JournalEntryId>()
        var totalErrors = 0
        
        // Validate batch size
        if (transactions.size > MAX_BATCH_SIZE) {
            return BatchPostingResult.failure(
                totalTransactions = transactions.size,
                errors = listOf("Batch size exceeds maximum allowed ($MAX_BATCH_SIZE)")
            )
        }
        
        // Pre-validate all transactions
        val preValidationErrors = mutableListOf<String>()
        for (transaction in transactions) {
            val validation = validateTransactionForPosting(transaction)
            if (!validation.isValid) {
                preValidationErrors.addAll(validation.errors.map { 
                    "Transaction ${transaction.referenceNumber}: $it" 
                })
            }
        }
        
        if (preValidationErrors.isNotEmpty() && !continueOnError) {
            return BatchPostingResult.failure(
                totalTransactions = transactions.size,
                errors = preValidationErrors
            )
        }
        
        // Process each transaction
        for (transaction in transactions) {
            try {
                val result = postJournalEntry(transaction, postingDate, postedBy, true)
                results.add(result)
                
                if (result.success) {
                    successfulPosts.add(transaction.id)
                } else {
                    failedPosts.add(transaction.id)
                    totalErrors += result.errors.size
                    
                    if (!continueOnError) {
                        break
                    }
                }
                
            } catch (e: Exception) {
                val errorResult = PostingResult.failure(
                    transactionId = transaction.id,
                    errors = listOf("Batch posting error: ${e.message}")
                )
                results.add(errorResult)
                failedPosts.add(transaction.id)
                totalErrors++
                
                if (!continueOnError) {
                    break
                }
            }
        }
        
        return BatchPostingResult(
            success = failedPosts.isEmpty(),
            totalTransactions = transactions.size,
            successfulPosts = successfulPosts.size,
            failedPosts = failedPosts.size,
            results = results,
            errors = if (totalErrors > 0) listOf("$totalErrors posting errors occurred") else emptyList()
        )
    }
    
    /**
     * Reverses a posted journal entry
     */
    suspend fun reverseJournalEntry(
        originalTransactionId: JournalEntryId,
        reversalReason: String,
        reversedBy: String,
        reversalDate: LocalDate = LocalDate.now(),
        createReversalEntry: Boolean = true
    ): ReversalResult {
        
        try {
            // Find the original transaction
            val originalTransaction = journalEntryRepository.findById(originalTransactionId)
                ?: return ReversalResult.failure(
                    originalTransactionId = originalTransactionId,
                    errors = listOf("Original transaction not found")
                )
            
            // Validate reversal eligibility
            val validationResult = validateTransactionForReversal(originalTransaction, reversalDate)
            if (!validationResult.isValid) {
                return ReversalResult.failure(
                    originalTransactionId = originalTransactionId,
                    errors = validationResult.errors
                )
            }
            
            // Mark original transaction as reversed
            val reversedTransaction = originalTransaction.reverse(reversedBy, reversalReason, reversalDate)
            journalEntryRepository.save(reversedTransaction)
            
            var reversalTransactionId: JournalEntryId? = null
            
            // Create reversal entry if requested
            if (createReversalEntry) {
                val reversalTransaction = createReversalTransaction(
                    originalTransaction = originalTransaction,
                    reversalReason = reversalReason,
                    reversedBy = reversedBy,
                    reversalDate = reversalDate
                )
                
                val savedReversalTransaction = journalEntryRepository.save(reversalTransaction)
                reversalTransactionId = savedReversalTransaction.id
                
                // Post the reversal transaction
                postJournalEntry(savedReversalTransaction, reversalDate, reversedBy, false)
            }
            
            // Update account balances
            updateAccountBalancesForReversal(originalTransaction, reversalDate)
            
            // Create audit trail
            createAuditEntry(
                transactionId = originalTransactionId,
                action = "REVERSED",
                performedBy = reversedBy,
                details = "Transaction reversed: $reversalReason"
            )
            
            return ReversalResult.success(
                originalTransactionId = originalTransactionId,
                reversalTransactionId = reversalTransactionId,
                reversalDate = reversalDate
            )
            
        } catch (e: Exception) {
            return ReversalResult.failure(
                originalTransactionId = originalTransactionId,
                errors = listOf("Reversal failed: ${e.message}")
            )
        }
    }
    
    // ==================== BALANCE CALCULATION OPERATIONS ====================
    
    /**
     * Calculates current balance for an account
     */
    suspend fun calculateAccountBalance(
        accountId: AccountId,
        asOfDate: LocalDate = LocalDate.now(),
        includeUnposted: Boolean = false
    ): AccountBalanceResult {
        
        try {
            val account = accountRepository.findById(accountId)
                ?: return AccountBalanceResult.failure(
                    accountId = accountId,
                    errors = listOf("Account not found")
                )
            
            // Get all transactions affecting this account
            val transactions = journalEntryRepository.findByAccount(
                accountId = accountId,
                endDate = asOfDate
            )
            
            var debitTotal = FinancialAmount.zero(account.currency)
            var creditTotal = FinancialAmount.zero(account.currency)
            var transactionCount = 0
            
            for (transaction in transactions.content) {
                if (!includeUnposted && transaction.status != TransactionStatus.POSTED) {
                    continue
                }
                
                for (line in transaction.lines) {
                    if (line.accountId == accountId) {
                        when (line.type) {
                            TransactionLineType.DEBIT -> debitTotal = debitTotal.add(line.amount)
                            TransactionLineType.CREDIT -> creditTotal = creditTotal.add(line.amount)
                        }
                        transactionCount++
                    }
                }
            }
            
            // Calculate net balance based on account type
            val balance = if (account.type in DEBIT_ACCOUNT_TYPES) {
                debitTotal.subtract(creditTotal)
            } else {
                creditTotal.subtract(debitTotal)
            }
            
            return AccountBalanceResult.success(
                accountId = accountId,
                balance = balance,
                debitTotal = debitTotal,
                creditTotal = creditTotal,
                transactionCount = transactionCount,
                asOfDate = asOfDate
            )
            
        } catch (e: Exception) {
            return AccountBalanceResult.failure(
                accountId = accountId,
                errors = listOf("Balance calculation failed: ${e.message}")
            )
        }
    }
    
    /**
     * Calculates balances for multiple accounts efficiently
     */
    suspend fun calculateAccountBalances(
        accountIds: List<AccountId>,
        asOfDate: LocalDate = LocalDate.now(),
        includeUnposted: Boolean = false
    ): Map<AccountId, AccountBalanceResult> {
        
        val results = mutableMapOf<AccountId, AccountBalanceResult>()
        
        // Get all accounts in batch
        val accounts = accountRepository.findByIds(accountIds).associateBy { it.id }
        
        // Get all transactions for these accounts
        val allTransactions = journalEntryRepository.findByAccounts(
            accountIds = accountIds,
            endDate = asOfDate
        )
        
        // Group transactions by account
        val transactionsByAccount = mutableMapOf<AccountId, MutableList<TransactionLine>>()
        
        for (transaction in allTransactions.content) {
            if (!includeUnposted && transaction.status != TransactionStatus.POSTED) {
                continue
            }
            
            for (line in transaction.lines) {
                if (line.accountId in accountIds) {
                    transactionsByAccount.getOrPut(line.accountId) { mutableListOf() }.add(line)
                }
            }
        }
        
        // Calculate balance for each account
        for (accountId in accountIds) {
            val account = accounts[accountId]
            if (account == null) {
                results[accountId] = AccountBalanceResult.failure(
                    accountId = accountId,
                    errors = listOf("Account not found")
                )
                continue
            }
            
            val lines = transactionsByAccount[accountId] ?: emptyList()
            var debitTotal = FinancialAmount.zero(account.currency)
            var creditTotal = FinancialAmount.zero(account.currency)
            
            for (line in lines) {
                when (line.type) {
                    TransactionLineType.DEBIT -> debitTotal = debitTotal.add(line.amount)
                    TransactionLineType.CREDIT -> creditTotal = creditTotal.add(line.amount)
                }
            }
            
            val balance = if (account.type in DEBIT_ACCOUNT_TYPES) {
                debitTotal.subtract(creditTotal)
            } else {
                creditTotal.subtract(debitTotal)
            }
            
            results[accountId] = AccountBalanceResult.success(
                accountId = accountId,
                balance = balance,
                debitTotal = debitTotal,
                creditTotal = creditTotal,
                transactionCount = lines.size,
                asOfDate = asOfDate
            )
        }
        
        return results
    }
    
    /**
     * Generates trial balance report
     */
    suspend fun generateTrialBalance(
        asOfDate: LocalDate = LocalDate.now(),
        includeZeroBalances: Boolean = false,
        currency: Currency? = null,
        accountTypes: List<AccountType>? = null
    ): TrialBalanceReport {
        
        try {
            // Get all accounts or filter by criteria
            val accounts = if (accountTypes != null) {
                accountRepository.findByTypes(accountTypes)
            } else if (currency != null) {
                accountRepository.findByCurrency(currency)
            } else {
                accountRepository.findAll()
            }
            
            val accountIds = accounts.map { it.id }
            val balanceResults = calculateAccountBalances(accountIds, asOfDate, false)
            
            val entries = mutableListOf<TrialBalanceEntry>()
            var totalDebits = FinancialAmount.zero(currency ?: Currency.USD)
            var totalCredits = FinancialAmount.zero(currency ?: Currency.USD)
            
            for (account in accounts) {
                val balanceResult = balanceResults[account.id]
                if (balanceResult?.success != true) continue
                
                val balance = balanceResult.balance
                if (!includeZeroBalances && balance.isZero) continue
                
                val debitAmount = if (account.type in DEBIT_ACCOUNT_TYPES && balance.isPositive) 
                    balance else FinancialAmount.zero(balance.currency)
                    
                val creditAmount = if (account.type in CREDIT_ACCOUNT_TYPES && balance.isPositive) 
                    balance else FinancialAmount.zero(balance.currency)
                
                entries.add(TrialBalanceEntry(
                    accountId = account.id,
                    accountCode = account.code,
                    accountName = account.name,
                    accountType = account.type,
                    debitAmount = debitAmount,
                    creditAmount = creditAmount,
                    balance = balance
                ))
                
                totalDebits = totalDebits.add(debitAmount)
                totalCredits = totalCredits.add(creditAmount)
            }
            
            val isBalanced = totalDebits == totalCredits
            
            return TrialBalanceReport(
                asOfDate = asOfDate,
                currency = currency,
                entries = entries.sortedBy { it.accountCode },
                totalDebits = totalDebits,
                totalCredits = totalCredits,
                isBalanced = isBalanced,
                balanceDiscrepancy = totalDebits.subtract(totalCredits),
                generatedAt = LocalDateTime.now(),
                generatedBy = "LedgerService"
            )
            
        } catch (e: Exception) {
            return TrialBalanceReport.failure("Trial balance generation failed: ${e.message}")
        }
    }
    
    /**
     * Generates general ledger report for an account
     */
    suspend fun generateGeneralLedgerReport(
        accountId: AccountId,
        startDate: LocalDate,
        endDate: LocalDate,
        includeUnposted: Boolean = false
    ): GeneralLedgerReport {
        
        try {
            val account = accountRepository.findById(accountId)
                ?: return GeneralLedgerReport.failure(
                    accountId = accountId,
                    error = "Account not found"
                )
            
            // Get beginning balance
            val beginningBalanceResult = calculateAccountBalance(
                accountId = accountId,
                asOfDate = startDate.minusDays(1),
                includeUnposted = includeUnposted
            )
            
            val beginningBalance = if (beginningBalanceResult.success) {
                beginningBalanceResult.balance
            } else {
                FinancialAmount.zero(account.currency)
            }
            
            // Get transactions for the period
            val transactions = journalEntryRepository.findByAccount(
                accountId = accountId,
                startDate = startDate,
                endDate = endDate
            )
            
            val entries = mutableListOf<GeneralLedgerEntry>()
            var runningBalance = beginningBalance
            
            for (transaction in transactions.content) {
                if (!includeUnposted && transaction.status != TransactionStatus.POSTED) {
                    continue
                }
                
                for (line in transaction.lines) {
                    if (line.accountId == accountId) {
                        val debitAmount = if (line.type == TransactionLineType.DEBIT) line.amount else null
                        val creditAmount = if (line.type == TransactionLineType.CREDIT) line.amount else null
                        
                        // Update running balance
                        runningBalance = if (account.type in DEBIT_ACCOUNT_TYPES) {
                            if (debitAmount != null) runningBalance.add(debitAmount)
                            else runningBalance.subtract(creditAmount!!)
                        } else {
                            if (creditAmount != null) runningBalance.add(creditAmount)
                            else runningBalance.subtract(debitAmount!!)
                        }
                        
                        entries.add(GeneralLedgerEntry(
                            transactionId = transaction.id,
                            transactionDate = transaction.transactionDate,
                            accountId = accountId,
                            accountCode = account.code,
                            accountName = account.name,
                            referenceNumber = transaction.referenceNumber,
                            description = line.description ?: transaction.description,
                            debitAmount = debitAmount,
                            creditAmount = creditAmount,
                            runningBalance = runningBalance
                        ))
                    }
                }
            }
            
            return GeneralLedgerReport.success(
                accountId = accountId,
                accountCode = account.code,
                accountName = account.name,
                startDate = startDate,
                endDate = endDate,
                beginningBalance = beginningBalance,
                endingBalance = runningBalance,
                entries = entries
            )
            
        } catch (e: Exception) {
            return GeneralLedgerReport.failure(
                accountId = accountId,
                error = "General ledger report generation failed: ${e.message}"
            )
        }
    }
    
    // ==================== PERIOD CLOSING OPERATIONS ====================
    
    /**
     * Closes an accounting period
     */
    suspend fun closeAccountingPeriod(
        period: AccountingPeriod,
        closedBy: String,
        performPreCloseValidation: Boolean = true
    ): PeriodClosingResult {
        
        try {
            if (performPreCloseValidation) {
                val validationResult = validatePeriodForClosing(period)
                if (!validationResult.isValid) {
                    return PeriodClosingResult.failure(
                        period = period,
                        errors = validationResult.errors
                    )
                }
            }
            
            // Generate trial balance for the period
            val trialBalance = generateTrialBalance(
                asOfDate = period.endDate,
                includeZeroBalances = false
            )
            
            if (!trialBalance.isBalanced) {
                return PeriodClosingResult.failure(
                    period = period,
                    errors = listOf("Trial balance is not balanced: discrepancy of ${trialBalance.balanceDiscrepancy}")
                )
            }
            
            // Create closing entries for income and expense accounts
            val closingEntries = generateClosingEntries(period, closedBy)
            
            // Post closing entries
            val postingResults = mutableListOf<PostingResult>()
            for (closingEntry in closingEntries) {
                val result = postJournalEntry(closingEntry, period.endDate, closedBy, true)
                postingResults.add(result)
                
                if (!result.success) {
                    return PeriodClosingResult.failure(
                        period = period,
                        errors = listOf("Failed to post closing entry: ${result.errors.joinToString()}")
                    )
                }
            }
            
            // Update period status
            val closedPeriod = period.close(closedBy)
            
            // Create audit trail
            createAuditEntry(
                transactionId = null,
                action = "PERIOD_CLOSED",
                performedBy = closedBy,
                details = "Accounting period ${period.name} closed"
            )
            
            return PeriodClosingResult.success(
                period = closedPeriod,
                trialBalance = trialBalance,
                closingEntries = closingEntries.map { it.id },
                closedAt = LocalDateTime.now()
            )
            
        } catch (e: Exception) {
            return PeriodClosingResult.failure(
                period = period,
                errors = listOf("Period closing failed: ${e.message}")
            )
        }
    }
    
    /**
     * Reopens a closed accounting period
     */
    suspend fun reopenAccountingPeriod(
        period: AccountingPeriod,
        reopenedBy: String,
        reopenReason: String
    ): PeriodReopenResult {
        
        try {
            // Validate period can be reopened
            val validationResult = validatePeriodForReopening(period)
            if (!validationResult.isValid) {
                return PeriodReopenResult.failure(
                    period = period,
                    errors = validationResult.errors
                )
            }
            
            // Reverse closing entries if they exist
            val closingEntries = findClosingEntries(period)
            val reversalResults = mutableListOf<ReversalResult>()
            
            for (closingEntry in closingEntries) {
                val result = reverseJournalEntry(
                    originalTransactionId = closingEntry.id,
                    reversalReason = "Period reopened: $reopenReason",
                    reversedBy = reopenedBy
                )
                reversalResults.add(result)
                
                if (!result.success) {
                    return PeriodReopenResult.failure(
                        period = period,
                        errors = listOf("Failed to reverse closing entry: ${result.errors.joinToString()}")
                    )
                }
            }
            
            // Update period status
            val reopenedPeriod = period.reopen(reopenedBy, reopenReason)
            
            // Create audit trail
            createAuditEntry(
                transactionId = null,
                action = "PERIOD_REOPENED",
                performedBy = reopenedBy,
                details = "Accounting period ${period.name} reopened: $reopenReason"
            )
            
            return PeriodReopenResult.success(
                period = reopenedPeriod,
                reversedClosingEntries = closingEntries.map { it.id },
                reopenedAt = LocalDateTime.now()
            )
            
        } catch (e: Exception) {
            return PeriodReopenResult.failure(
                period = period,
                errors = listOf("Period reopening failed: ${e.message}")
            )
        }
    }
    
    // ==================== VALIDATION OPERATIONS ====================
    
    /**
     * Validates a transaction for posting
     */
    private fun validateTransactionForPosting(transaction: Transaction): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check if transaction is balanced
        val debitTotal = transaction.lines
            .filter { it.type == TransactionLineType.DEBIT }
            .map { it.amount }
            .fold(FinancialAmount.zero(transaction.currency)) { acc, amount -> acc.add(amount) }
            
        val creditTotal = transaction.lines
            .filter { it.type == TransactionLineType.CREDIT }
            .map { it.amount }
            .fold(FinancialAmount.zero(transaction.currency)) { acc, amount -> acc.add(amount) }
        
        if (debitTotal != creditTotal) {
            errors.add("Transaction is not balanced: debits $debitTotal, credits $creditTotal")
        }
        
        // Check for empty lines
        if (transaction.lines.isEmpty()) {
            errors.add("Transaction has no lines")
        }
        
        // Validate each line
        for (line in transaction.lines) {
            if (line.amount.isZero) {
                warnings.add("Transaction line has zero amount")
            }
            
            if (line.amount.isNegative) {
                errors.add("Transaction line has negative amount")
            }
        }
        
        // Check reference number uniqueness
        // This would typically involve a repository call
        
        // Check transaction date
        val cutoffDate = LocalDate.now().minusDays(AUTO_REVERSAL_CUTOFF_DAYS)
        if (transaction.transactionDate.isBefore(cutoffDate)) {
            warnings.add("Transaction date is more than $AUTO_REVERSAL_CUTOFF_DAYS days in the past")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * Validates account balances before posting
     */
    private suspend fun validateAccountBalances(transaction: Transaction): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check if all accounts exist and are active
        for (line in transaction.lines) {
            val account = accountRepository.findById(line.accountId)
            if (account == null) {
                errors.add("Account ${line.accountId} not found")
                continue
            }
            
            if (!account.isActive) {
                errors.add("Account ${account.code} is not active")
            }
            
            // Check currency consistency
            if (account.currency != line.amount.currency) {
                errors.add("Currency mismatch: account ${account.code} uses ${account.currency}, line uses ${line.amount.currency}")
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * Validates a transaction for reversal
     */
    private fun validateTransactionForReversal(
        transaction: Transaction,
        reversalDate: LocalDate
    ): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check if already reversed
        if (transaction.isReversed) {
            errors.add("Transaction is already reversed")
        }
        
        // Check if posted
        if (transaction.status != TransactionStatus.POSTED) {
            errors.add("Only posted transactions can be reversed")
        }
        
        // Check reversal date
        if (reversalDate.isBefore(transaction.transactionDate)) {
            errors.add("Reversal date cannot be before original transaction date")
        }
        
        // Check if too old
        val daysSincePosting = ChronoUnit.DAYS.between(transaction.transactionDate, reversalDate)
        if (daysSincePosting > AUTO_REVERSAL_CUTOFF_DAYS) {
            warnings.add("Transaction is more than $AUTO_REVERSAL_CUTOFF_DAYS days old")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * Validates a period for closing
     */
    private suspend fun validatePeriodForClosing(period: AccountingPeriod): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check if period is already closed
        if (period.isClosed) {
            errors.add("Period is already closed")
        }
        
        // Check for unposted transactions
        val unpostedTransactions = journalEntryRepository.findByAccountingPeriod(period, false)
            .content.filter { it.status != TransactionStatus.POSTED }
        
        if (unpostedTransactions.isNotEmpty()) {
            warnings.add("${unpostedTransactions.size} unposted transactions found in period")
        }
        
        // Check trial balance
        val trialBalance = generateTrialBalance(period.endDate, false)
        if (!trialBalance.isBalanced) {
            errors.add("Trial balance is not balanced")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * Validates a period for reopening
     */
    private fun validatePeriodForReopening(period: AccountingPeriod): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check if period is closed
        if (!period.isClosed) {
            errors.add("Period is not closed")
        }
        
        // Check if subsequent periods are closed
        // This would typically involve checking other periods
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    // ==================== HELPER METHODS ====================
    
    private suspend fun updateAccountBalances(transaction: Transaction) {
        // Update cached account balances
        // This would typically involve updating a balance cache table
        for (line in transaction.lines) {
            // Implementation would update account balance cache
        }
    }
    
    private suspend fun updateAccountBalancesForReversal(
        transaction: Transaction,
        reversalDate: LocalDate
    ) {
        // Update cached account balances for reversal
        for (line in transaction.lines) {
            // Implementation would update account balance cache
        }
    }
    
    private fun createReversalTransaction(
        originalTransaction: Transaction,
        reversalReason: String,
        reversedBy: String,
        reversalDate: LocalDate
    ): Transaction {
        
        // Create reversed transaction lines
        val reversedLines = originalTransaction.lines.map { originalLine ->
            TransactionLine.create(
                accountId = originalLine.accountId,
                type = if (originalLine.type == TransactionLineType.DEBIT) 
                    TransactionLineType.CREDIT else TransactionLineType.DEBIT,
                amount = originalLine.amount,
                description = "Reversal: ${originalLine.description ?: ""}"
            )
        }
        
        return Transaction.create(
            referenceNumber = "REV-${originalTransaction.referenceNumber}",
            description = "Reversal of ${originalTransaction.referenceNumber}: $reversalReason",
            transactionDate = reversalDate,
            transactionType = TransactionType.REVERSAL,
            currency = originalTransaction.currency,
            lines = reversedLines,
            createdBy = reversedBy
        )
    }
    
    private suspend fun generateClosingEntries(
        period: AccountingPeriod,
        closedBy: String
    ): List<Transaction> {
        
        val closingEntries = mutableListOf<Transaction>()
        
        // Get all revenue and expense accounts with balances
        val revenueAccounts = accountRepository.findByType(AccountType.REVENUE)
        val expenseAccounts = accountRepository.findByType(AccountType.EXPENSES)
        
        val revenueBalances = calculateAccountBalances(
            revenueAccounts.map { it.id },
            period.endDate,
            false
        )
        
        val expenseBalances = calculateAccountBalances(
            expenseAccounts.map { it.id },
            period.endDate,
            false
        )
        
        // Create closing entries
        val closingLines = mutableListOf<TransactionLine>()
        
        // Close revenue accounts (debit revenue, credit income summary)
        for ((accountId, balanceResult) in revenueBalances) {
            if (balanceResult.success && !balanceResult.balance.isZero) {
                closingLines.add(TransactionLine.create(
                    accountId = accountId,
                    type = TransactionLineType.DEBIT,
                    amount = balanceResult.balance,
                    description = "Close revenue account"
                ))
            }
        }
        
        // Close expense accounts (credit expense, debit income summary)
        for ((accountId, balanceResult) in expenseBalances) {
            if (balanceResult.success && !balanceResult.balance.isZero) {
                closingLines.add(TransactionLine.create(
                    accountId = accountId,
                    type = TransactionLineType.CREDIT,
                    amount = balanceResult.balance,
                    description = "Close expense account"
                ))
            }
        }
        
        if (closingLines.isNotEmpty()) {
            // Find or create income summary account
            val incomeSummaryAccount = accountRepository.findByCode("3900") 
                ?: throw IllegalStateException("Income Summary account not found")
            
            // Calculate net income
            val totalRevenue = revenueBalances.values
                .filter { it.success }
                .map { it.balance }
                .fold(FinancialAmount.zero(period.currency)) { acc, balance -> acc.add(balance) }
            
            val totalExpenses = expenseBalances.values
                .filter { it.success }
                .map { it.balance }
                .fold(FinancialAmount.zero(period.currency)) { acc, balance -> acc.add(balance) }
            
            val netIncome = totalRevenue.subtract(totalExpenses)
            
            // Add income summary line to balance the entry
            closingLines.add(TransactionLine.create(
                accountId = incomeSummaryAccount.id,
                type = if (netIncome.isPositive) TransactionLineType.CREDIT else TransactionLineType.DEBIT,
                amount = netIncome.absolute,
                description = "Net income for period"
            ))
            
            val closingEntry = Transaction.create(
                referenceNumber = "CLOSE-${period.name}",
                description = "Closing entries for ${period.name}",
                transactionDate = period.endDate,
                transactionType = TransactionType.CLOSING_ENTRY,
                currency = period.currency,
                lines = closingLines,
                createdBy = closedBy
            )
            
            closingEntries.add(closingEntry)
        }
        
        return closingEntries
    }
    
    private suspend fun findClosingEntries(period: AccountingPeriod): List<Transaction> {
        return journalEntryRepository.findByTransactionType(TransactionType.CLOSING_ENTRY)
            .content.filter { 
                it.transactionDate == period.endDate && 
                it.description.contains(period.name)
            }
    }
    
    private suspend fun createAuditEntry(
        transactionId: JournalEntryId?,
        action: String,
        performedBy: String,
        details: String
    ) {
        // Create audit log entry
        // This would typically involve saving to an audit log repository
    }
}

// ==================== RESULT CLASSES ====================

/**
 * Posting result
 */
data class PostingResult(
    val success: Boolean,
    val transactionId: JournalEntryId,
    val postedAt: LocalDate? = null,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun success(
            transactionId: JournalEntryId,
            postedAt: LocalDate
        ) = PostingResult(
            success = true,
            transactionId = transactionId,
            postedAt = postedAt
        )
        
        fun failure(
            transactionId: JournalEntryId,
            errors: List<String>,
            warnings: List<String> = emptyList()
        ) = PostingResult(
            success = false,
            transactionId = transactionId,
            errors = errors,
            warnings = warnings
        )
        
        fun warning(
            transactionId: JournalEntryId,
            warnings: List<String>
        ) = PostingResult(
            success = true,
            transactionId = transactionId,
            warnings = warnings
        )
    }
}

/**
 * Batch posting result
 */
data class BatchPostingResult(
    val success: Boolean,
    val totalTransactions: Int,
    val successfulPosts: Int,
    val failedPosts: Int,
    val results: List<PostingResult>,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun failure(
            totalTransactions: Int,
            errors: List<String>
        ) = BatchPostingResult(
            success = false,
            totalTransactions = totalTransactions,
            successfulPosts = 0,
            failedPosts = totalTransactions,
            results = emptyList(),
            errors = errors
        )
    }
}

/**
 * Reversal result
 */
data class ReversalResult(
    val success: Boolean,
    val originalTransactionId: JournalEntryId,
    val reversalTransactionId: JournalEntryId? = null,
    val reversalDate: LocalDate? = null,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun success(
            originalTransactionId: JournalEntryId,
            reversalTransactionId: JournalEntryId?,
            reversalDate: LocalDate
        ) = ReversalResult(
            success = true,
            originalTransactionId = originalTransactionId,
            reversalTransactionId = reversalTransactionId,
            reversalDate = reversalDate
        )
        
        fun failure(
            originalTransactionId: JournalEntryId,
            errors: List<String>
        ) = ReversalResult(
            success = false,
            originalTransactionId = originalTransactionId,
            errors = errors
        )
    }
}

/**
 * Account balance result
 */
data class AccountBalanceResult(
    val success: Boolean,
    val accountId: AccountId,
    val balance: FinancialAmount,
    val debitTotal: FinancialAmount? = null,
    val creditTotal: FinancialAmount? = null,
    val transactionCount: Int = 0,
    val asOfDate: LocalDate? = null,
    val errors: List<String> = emptyList()
) {
    companion object {
        fun success(
            accountId: AccountId,
            balance: FinancialAmount,
            debitTotal: FinancialAmount,
            creditTotal: FinancialAmount,
            transactionCount: Int,
            asOfDate: LocalDate
        ) = AccountBalanceResult(
            success = true,
            accountId = accountId,
            balance = balance,
            debitTotal = debitTotal,
            creditTotal = creditTotal,
            transactionCount = transactionCount,
            asOfDate = asOfDate
        )
        
        fun failure(
            accountId: AccountId,
            errors: List<String>
        ) = AccountBalanceResult(
            success = false,
            accountId = accountId,
            balance = FinancialAmount.zero(Currency.USD),
            errors = errors
        )
    }
}

/**
 * Trial balance report
 */
data class TrialBalanceReport(
    val success: Boolean = true,
    val asOfDate: LocalDate,
    val currency: Currency?,
    val entries: List<TrialBalanceEntry>,
    val totalDebits: FinancialAmount,
    val totalCredits: FinancialAmount,
    val isBalanced: Boolean,
    val balanceDiscrepancy: FinancialAmount,
    val generatedAt: LocalDateTime,
    val generatedBy: String,
    val error: String? = null
) {
    companion object {
        fun failure(error: String) = TrialBalanceReport(
            success = false,
            asOfDate = LocalDate.now(),
            currency = null,
            entries = emptyList(),
            totalDebits = FinancialAmount.zero(Currency.USD),
            totalCredits = FinancialAmount.zero(Currency.USD),
            isBalanced = false,
            balanceDiscrepancy = FinancialAmount.zero(Currency.USD),
            generatedAt = LocalDateTime.now(),
            generatedBy = "System",
            error = error
        )
    }
}

/**
 * General ledger report
 */
data class GeneralLedgerReport(
    val success: Boolean,
    val accountId: AccountId,
    val accountCode: String? = null,
    val accountName: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val beginningBalance: FinancialAmount? = null,
    val endingBalance: FinancialAmount? = null,
    val entries: List<GeneralLedgerEntry> = emptyList(),
    val error: String? = null
) {
    companion object {
        fun success(
            accountId: AccountId,
            accountCode: String,
            accountName: String,
            startDate: LocalDate,
            endDate: LocalDate,
            beginningBalance: FinancialAmount,
            endingBalance: FinancialAmount,
            entries: List<GeneralLedgerEntry>
        ) = GeneralLedgerReport(
            success = true,
            accountId = accountId,
            accountCode = accountCode,
            accountName = accountName,
            startDate = startDate,
            endDate = endDate,
            beginningBalance = beginningBalance,
            endingBalance = endingBalance,
            entries = entries
        )
        
        fun failure(
            accountId: AccountId,
            error: String
        ) = GeneralLedgerReport(
            success = false,
            accountId = accountId,
            error = error
        )
    }
}

/**
 * Period closing result
 */
data class PeriodClosingResult(
    val success: Boolean,
    val period: AccountingPeriod,
    val trialBalance: TrialBalanceReport? = null,
    val closingEntries: List<JournalEntryId> = emptyList(),
    val closedAt: LocalDateTime? = null,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun success(
            period: AccountingPeriod,
            trialBalance: TrialBalanceReport,
            closingEntries: List<JournalEntryId>,
            closedAt: LocalDateTime
        ) = PeriodClosingResult(
            success = true,
            period = period,
            trialBalance = trialBalance,
            closingEntries = closingEntries,
            closedAt = closedAt
        )
        
        fun failure(
            period: AccountingPeriod,
            errors: List<String>
        ) = PeriodClosingResult(
            success = false,
            period = period,
            errors = errors
        )
    }
}

/**
 * Period reopen result
 */
data class PeriodReopenResult(
    val success: Boolean,
    val period: AccountingPeriod,
    val reversedClosingEntries: List<JournalEntryId> = emptyList(),
    val reopenedAt: LocalDateTime? = null,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        fun success(
            period: AccountingPeriod,
            reversedClosingEntries: List<JournalEntryId>,
            reopenedAt: LocalDateTime
        ) = PeriodReopenResult(
            success = true,
            period = period,
            reversedClosingEntries = reversedClosingEntries,
            reopenedAt = reopenedAt
        )
        
        fun failure(
            period: AccountingPeriod,
            errors: List<String>
        ) = PeriodReopenResult(
            success = false,
            period = period,
            errors = errors
        )
    }
}

/**
 * Validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Transaction line type enum
 */
enum class TransactionLineType {
    DEBIT, CREDIT
}

package org.chiro.finance.domain.service

import org.chiro.finance.domain.entity.Account
import org.chiro.finance.domain.repository.AccountRepository
import org.chiro.finance.domain.valueobject.AccountId
import org.chiro.finance.domain.valueobject.AccountType
import org.chiro.finance.domain.valueobject.FinancialAmount
import org.chiro.finance.domain.valueobject.Currency
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

/**
 * Chart of Accounts Domain Service
 * 
 * Manages the hierarchical structure and business logic for the chart of accounts.
 * This service encapsulates complex account hierarchy operations, validation rules,
 * and business processes that don't naturally belong to a single entity.
 * 
 * Domain Service Pattern: Encapsulates domain logic that doesn't naturally fit 
 * within a single entity or value object.
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
@ApplicationScoped
class ChartOfAccountsService @Inject constructor(
    private val accountRepository: AccountRepository
) {
    
    companion object {
        // Chart of Accounts configuration constants
        const val MAX_HIERARCHY_DEPTH = 10
        const val MAX_ACCOUNT_CODE_LENGTH = 20
        const val MIN_ACCOUNT_CODE_LENGTH = 3
        const val MAX_CHILDREN_PER_ACCOUNT = 1000
        
        // Standard account code patterns
        val ASSET_CODE_PATTERN = Regex("^[1][0-9]{2,4}$")
        val LIABILITY_CODE_PATTERN = Regex("^[2][0-9]{2,4}$")
        val EQUITY_CODE_PATTERN = Regex("^[3][0-9]{2,4}$")
        val REVENUE_CODE_PATTERN = Regex("^[4][0-9]{2,4}$")
        val EXPENSE_CODE_PATTERN = Regex("^[5-9][0-9]{2,4}$")
        
        // Reserved account code ranges
        val RESERVED_RANGES = mapOf(
            "1000-1099" to "Cash and Cash Equivalents",
            "1100-1199" to "Accounts Receivable",
            "1200-1299" to "Inventory",
            "1300-1399" to "Prepaid Expenses",
            "1400-1499" to "Fixed Assets",
            "2000-2099" to "Accounts Payable",
            "2100-2199" to "Accrued Liabilities",
            "2200-2299" to "Short-term Debt",
            "2300-2399" to "Long-term Debt",
            "3000-3099" to "Owner's Equity",
            "3100-3199" to "Retained Earnings",
            "4000-4099" to "Sales Revenue",
            "4100-4199" to "Service Revenue",
            "5000-5099" to "Cost of Goods Sold",
            "5100-5199" to "Operating Expenses"
        )
    }
    
    // ==================== CHART STRUCTURE OPERATIONS ====================
    
    /**
     * Creates a complete chart of accounts with standard structure
     */
    suspend fun createStandardChartOfAccounts(
        companyName: String,
        baseCurrency: Currency = Currency.USD,
        includeDetailedSubAccounts: Boolean = false
    ): ChartOfAccountsStructure {
        
        val rootAccounts = mutableListOf<Account>()
        
        // Create main account categories
        val assets = createMainAccount(
            code = "1000",
            name = "Assets",
            type = AccountType.ASSETS,
            currency = baseCurrency,
            description = "All company assets"
        )
        
        val liabilities = createMainAccount(
            code = "2000", 
            name = "Liabilities",
            type = AccountType.LIABILITIES,
            currency = baseCurrency,
            description = "All company liabilities"
        )
        
        val equity = createMainAccount(
            code = "3000",
            name = "Equity", 
            type = AccountType.EQUITY,
            currency = baseCurrency,
            description = "Owner's equity and retained earnings"
        )
        
        val revenue = createMainAccount(
            code = "4000",
            name = "Revenue",
            type = AccountType.REVENUE,
            currency = baseCurrency,
            description = "All revenue accounts"
        )
        
        val expenses = createMainAccount(
            code = "5000", 
            name = "Expenses",
            type = AccountType.EXPENSES,
            currency = baseCurrency,
            description = "All expense accounts"
        )
        
        rootAccounts.addAll(listOf(assets, liabilities, equity, revenue, expenses))
        
        // Create standard sub-accounts
        val subAccounts = mutableListOf<Account>()
        
        // Asset sub-accounts
        subAccounts.addAll(createAssetSubAccounts(assets, baseCurrency, includeDetailedSubAccounts))
        
        // Liability sub-accounts
        subAccounts.addAll(createLiabilitySubAccounts(liabilities, baseCurrency, includeDetailedSubAccounts))
        
        // Equity sub-accounts
        subAccounts.addAll(createEquitySubAccounts(equity, baseCurrency, includeDetailedSubAccounts))
        
        // Revenue sub-accounts
        subAccounts.addAll(createRevenueSubAccounts(revenue, baseCurrency, includeDetailedSubAccounts))
        
        // Expense sub-accounts
        subAccounts.addAll(createExpenseSubAccounts(expenses, baseCurrency, includeDetailedSubAccounts))
        
        // Save all accounts
        rootAccounts.forEach { account ->
            accountRepository.save(account)
        }
        
        subAccounts.forEach { account ->
            accountRepository.save(account)
        }
        
        return ChartOfAccountsStructure(
            companyName = companyName,
            baseCurrency = baseCurrency,
            rootAccounts = rootAccounts,
            totalAccounts = rootAccounts.size + subAccounts.size,
            createdDate = LocalDate.now(),
            isStandardStructure = true
        )
    }
    
    /**
     * Validates account hierarchy rules and constraints
     */
    fun validateAccountHierarchy(
        account: Account,
        parentAccount: Account? = null
    ): AccountHierarchyValidation {
        
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Validate account code format
        if (!isValidAccountCode(account.code, account.type)) {
            issues.add("Account code '${account.code}' doesn't match expected pattern for ${account.type}")
        }
        
        // Validate hierarchy depth
        if (parentAccount != null) {
            val depth = calculateAccountDepth(parentAccount) + 1
            if (depth > MAX_HIERARCHY_DEPTH) {
                issues.add("Account hierarchy depth ($depth) exceeds maximum allowed ($MAX_HIERARCHY_DEPTH)")
            }
        }
        
        // Validate parent-child relationship
        if (parentAccount != null) {
            if (!canBeChildOf(account.type, parentAccount.type)) {
                issues.add("Account type '${account.type}' cannot be a child of '${parentAccount.type}'")
            }
            
            if (account.currency != parentAccount.currency) {
                warnings.add("Child account currency (${account.currency}) differs from parent (${parentAccount.currency})")
            }
        }
        
        // Check for circular references
        if (parentAccount != null && wouldCreateCircularReference(account.id, parentAccount.id)) {
            issues.add("Creating this parent-child relationship would create a circular reference")
        }
        
        // Validate account code uniqueness
        if (isAccountCodeTaken(account.code, account.id)) {
            issues.add("Account code '${account.code}' is already in use")
        }
        
        return AccountHierarchyValidation(
            isValid = issues.isEmpty(),
            issues = issues,
            warnings = warnings,
            recommendedActions = generateRecommendations(account, parentAccount, issues, warnings)
        )
    }
    
    /**
     * Reorganizes chart of accounts structure
     */
    suspend fun reorganizeChartStructure(
        reorganizationPlan: ChartReorganizationPlan
    ): ChartReorganizationResult {
        
        val movedAccounts = mutableListOf<Account>()
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            // Validate the reorganization plan
            val validationResult = validateReorganizationPlan(reorganizationPlan)
            if (!validationResult.isValid) {
                return ChartReorganizationResult(
                    success = false,
                    movedAccounts = emptyList(),
                    errors = validationResult.issues,
                    warnings = validationResult.warnings
                )
            }
            
            // Execute moves in dependency order
            for (move in reorganizationPlan.moves.sortedBy { it.order }) {
                try {
                    val account = accountRepository.findById(move.accountId)
                        ?: throw IllegalArgumentException("Account not found: ${move.accountId}")
                    
                    val newParent = move.newParentId?.let { parentId ->
                        accountRepository.findById(parentId)
                            ?: throw IllegalArgumentException("Parent account not found: $parentId")
                    }
                    
                    // Update account hierarchy
                    val updatedAccount = account.updateParent(newParent?.id)
                    accountRepository.save(updatedAccount)
                    
                    movedAccounts.add(updatedAccount)
                    
                } catch (e: Exception) {
                    errors.add("Failed to move account ${move.accountId}: ${e.message}")
                }
            }
            
            // Update account codes if requested
            if (reorganizationPlan.updateAccountCodes) {
                renumberAccountCodes(reorganizationPlan.renumberingRules)
            }
            
            return ChartReorganizationResult(
                success = errors.isEmpty(),
                movedAccounts = movedAccounts,
                errors = errors,
                warnings = warnings
            )
            
        } catch (e: Exception) {
            return ChartReorganizationResult(
                success = false,
                movedAccounts = emptyList(),
                errors = listOf("Reorganization failed: ${e.message}"),
                warnings = warnings
            )
        }
    }
    
    /**
     * Calculates trial balance for all accounts
     */
    suspend fun calculateTrialBalance(
        asOfDate: LocalDate = LocalDate.now(),
        currency: Currency? = null,
        includeZeroBalances: Boolean = false
    ): TrialBalance {
        
        val accounts = if (currency != null) {
            accountRepository.findByCurrency(currency)
        } else {
            accountRepository.findAll()
        }
        
        val balanceEntries = mutableListOf<TrialBalanceEntry>()
        var totalDebits = FinancialAmount.zero(currency ?: Currency.USD)
        var totalCredits = FinancialAmount.zero(currency ?: Currency.USD)
        
        for (account in accounts) {
            val balance = accountRepository.calculateBalance(account.id, asOfDate)
            
            if (!includeZeroBalances && balance.isZero) {
                continue
            }
            
            val entry = TrialBalanceEntry(
                accountId = account.id,
                accountCode = account.code,
                accountName = account.name,
                accountType = account.type,
                debitAmount = if (account.type.normalBalanceType == BalanceType.DEBIT && balance.isPositive) 
                    balance else FinancialAmount.zero(balance.currency),
                creditAmount = if (account.type.normalBalanceType == BalanceType.CREDIT && balance.isPositive) 
                    balance else FinancialAmount.zero(balance.currency),
                balance = balance
            )
            
            balanceEntries.add(entry)
            totalDebits = totalDebits.add(entry.debitAmount)
            totalCredits = totalCredits.add(entry.creditAmount)
        }
        
        return TrialBalance(
            asOfDate = asOfDate,
            currency = currency,
            entries = balanceEntries.sortedBy { it.accountCode },
            totalDebits = totalDebits,
            totalCredits = totalCredits,
            isBalanced = totalDebits == totalCredits,
            generatedAt = LocalDate.now()
        )
    }
    
    // ==================== ACCOUNT MANAGEMENT OPERATIONS ====================
    
    /**
     * Creates a new account with full validation
     */
    suspend fun createAccount(
        code: String,
        name: String,
        type: AccountType,
        currency: Currency,
        parentAccountId: AccountId? = null,
        description: String? = null,
        isActive: Boolean = true
    ): Account {
        
        // Validate account creation
        val parentAccount = parentAccountId?.let { 
            accountRepository.findById(it) 
                ?: throw IllegalArgumentException("Parent account not found: $it")
        }
        
        val newAccount = Account.create(
            code = code,
            name = name,
            type = type,
            currency = currency,
            parentAccountId = parentAccountId,
            description = description,
            isActive = isActive
        )
        
        val validationResult = validateAccountHierarchy(newAccount, parentAccount)
        if (!validationResult.isValid) {
            throw IllegalArgumentException("Account validation failed: ${validationResult.issues.joinToString(", ")}")
        }
        
        return accountRepository.save(newAccount)
    }
    
    /**
     * Merges two accounts (combines balances and reassigns transactions)
     */
    suspend fun mergeAccounts(
        sourceAccountId: AccountId,
        targetAccountId: AccountId,
        effectiveDate: LocalDate = LocalDate.now(),
        mergeReason: String
    ): AccountMergeResult {
        
        val sourceAccount = accountRepository.findById(sourceAccountId)
            ?: throw IllegalArgumentException("Source account not found: $sourceAccountId")
        
        val targetAccount = accountRepository.findById(targetAccountId)
            ?: throw IllegalArgumentException("Target account not found: $targetAccountId")
        
        // Validate merge compatibility
        val validation = validateAccountMerge(sourceAccount, targetAccount)
        if (!validation.isValid) {
            return AccountMergeResult(
                success = false,
                errors = validation.issues,
                sourceAccountId = sourceAccountId,
                targetAccountId = targetAccountId
            )
        }
        
        try {
            // Calculate balances before merge
            val sourceBalance = accountRepository.calculateBalance(sourceAccountId)
            val targetBalanceBefore = accountRepository.calculateBalance(targetAccountId)
            
            // Reassign all transactions from source to target
            val reassignedTransactions = accountRepository.reassignTransactions(
                fromAccountId = sourceAccountId,
                toAccountId = targetAccountId,
                effectiveDate = effectiveDate
            )
            
            // Deactivate source account
            val deactivatedSource = sourceAccount.deactivate("Merged into account ${targetAccount.code}", "System")
            accountRepository.save(deactivatedSource)
            
            // Calculate final balance
            val targetBalanceAfter = accountRepository.calculateBalance(targetAccountId)
            
            return AccountMergeResult(
                success = true,
                sourceAccountId = sourceAccountId,
                targetAccountId = targetAccountId,
                sourceBalance = sourceBalance,
                targetBalanceBefore = targetBalanceBefore,
                targetBalanceAfter = targetBalanceAfter,
                reassignedTransactionCount = reassignedTransactions,
                mergeDate = effectiveDate,
                mergeReason = mergeReason
            )
            
        } catch (e: Exception) {
            return AccountMergeResult(
                success = false,
                errors = listOf("Merge failed: ${e.message}"),
                sourceAccountId = sourceAccountId,
                targetAccountId = targetAccountId
            )
        }
    }
    
    /**
     * Archives old unused accounts
     */
    suspend fun archiveUnusedAccounts(
        inactiveSinceDate: LocalDate,
        minimumInactiveDays: Int = 365,
        dryRun: Boolean = false
    ): AccountArchiveResult {
        
        val candidateAccounts = accountRepository.findInactiveAccountsSince(inactiveSinceDate)
            .filter { account ->
                val daysSinceLastActivity = accountRepository.getDaysSinceLastActivity(account.id)
                daysSinceLastActivity >= minimumInactiveDays
            }
            .filter { account ->
                val balance = accountRepository.calculateBalance(account.id)
                balance.isZero
            }
        
        if (dryRun) {
            return AccountArchiveResult(
                success = true,
                candidateAccounts = candidateAccounts.map { it.id },
                archivedAccounts = emptyList(),
                isDryRun = true
            )
        }
        
        val archivedAccounts = mutableListOf<AccountId>()
        val errors = mutableListOf<String>()
        
        for (account in candidateAccounts) {
            try {
                val archivedAccount = account.archive("Archived due to inactivity", "System")
                accountRepository.save(archivedAccount)
                archivedAccounts.add(account.id)
            } catch (e: Exception) {
                errors.add("Failed to archive account ${account.code}: ${e.message}")
            }
        }
        
        return AccountArchiveResult(
            success = errors.isEmpty(),
            candidateAccounts = candidateAccounts.map { it.id },
            archivedAccounts = archivedAccounts,
            errors = errors,
            isDryRun = false
        )
    }
    
    // ==================== HELPER METHODS ====================
    
    private suspend fun createMainAccount(
        code: String,
        name: String,
        type: AccountType,
        currency: Currency,
        description: String
    ): Account {
        return Account.create(
            code = code,
            name = name,
            type = type,
            currency = currency,
            description = description,
            isActive = true
        )
    }
    
    private suspend fun createAssetSubAccounts(
        parentAccount: Account,
        currency: Currency,
        includeDetailed: Boolean
    ): List<Account> {
        val accounts = mutableListOf<Account>()
        
        // Current Assets
        accounts.add(createSubAccount("1100", "Current Assets", AccountType.CURRENT_ASSETS, currency, parentAccount.id))
        accounts.add(createSubAccount("1110", "Cash and Cash Equivalents", AccountType.CASH, currency, parentAccount.id))
        accounts.add(createSubAccount("1120", "Accounts Receivable", AccountType.ACCOUNTS_RECEIVABLE, currency, parentAccount.id))
        accounts.add(createSubAccount("1130", "Inventory", AccountType.INVENTORY, currency, parentAccount.id))
        accounts.add(createSubAccount("1140", "Prepaid Expenses", AccountType.PREPAID_EXPENSES, currency, parentAccount.id))
        
        // Fixed Assets
        accounts.add(createSubAccount("1200", "Fixed Assets", AccountType.FIXED_ASSETS, currency, parentAccount.id))
        accounts.add(createSubAccount("1210", "Property, Plant & Equipment", AccountType.PROPERTY_PLANT_EQUIPMENT, currency, parentAccount.id))
        accounts.add(createSubAccount("1220", "Accumulated Depreciation", AccountType.ACCUMULATED_DEPRECIATION, currency, parentAccount.id))
        
        if (includeDetailed) {
            // Detailed cash accounts
            accounts.add(createSubAccount("1111", "Petty Cash", AccountType.CASH, currency, parentAccount.id))
            accounts.add(createSubAccount("1112", "Checking Account", AccountType.CASH, currency, parentAccount.id))
            accounts.add(createSubAccount("1113", "Savings Account", AccountType.CASH, currency, parentAccount.id))
            
            // Detailed receivables
            accounts.add(createSubAccount("1121", "Trade Receivables", AccountType.ACCOUNTS_RECEIVABLE, currency, parentAccount.id))
            accounts.add(createSubAccount("1122", "Other Receivables", AccountType.ACCOUNTS_RECEIVABLE, currency, parentAccount.id))
            accounts.add(createSubAccount("1123", "Allowance for Doubtful Accounts", AccountType.ALLOWANCE_DOUBTFUL_ACCOUNTS, currency, parentAccount.id))
        }
        
        return accounts
    }
    
    private suspend fun createLiabilitySubAccounts(
        parentAccount: Account,
        currency: Currency,
        includeDetailed: Boolean
    ): List<Account> {
        val accounts = mutableListOf<Account>()
        
        // Current Liabilities
        accounts.add(createSubAccount("2100", "Current Liabilities", AccountType.CURRENT_LIABILITIES, currency, parentAccount.id))
        accounts.add(createSubAccount("2110", "Accounts Payable", AccountType.ACCOUNTS_PAYABLE, currency, parentAccount.id))
        accounts.add(createSubAccount("2120", "Accrued Liabilities", AccountType.ACCRUED_LIABILITIES, currency, parentAccount.id))
        accounts.add(createSubAccount("2130", "Short-term Debt", AccountType.SHORT_TERM_DEBT, currency, parentAccount.id))
        
        // Long-term Liabilities
        accounts.add(createSubAccount("2200", "Long-term Liabilities", AccountType.LONG_TERM_LIABILITIES, currency, parentAccount.id))
        accounts.add(createSubAccount("2210", "Long-term Debt", AccountType.LONG_TERM_DEBT, currency, parentAccount.id))
        accounts.add(createSubAccount("2220", "Deferred Tax Liabilities", AccountType.DEFERRED_TAX_LIABILITY, currency, parentAccount.id))
        
        if (includeDetailed) {
            // Detailed payables
            accounts.add(createSubAccount("2111", "Trade Payables", AccountType.ACCOUNTS_PAYABLE, currency, parentAccount.id))
            accounts.add(createSubAccount("2112", "Other Payables", AccountType.ACCOUNTS_PAYABLE, currency, parentAccount.id))
            
            // Detailed accruals
            accounts.add(createSubAccount("2121", "Accrued Wages", AccountType.ACCRUED_LIABILITIES, currency, parentAccount.id))
            accounts.add(createSubAccount("2122", "Accrued Taxes", AccountType.ACCRUED_LIABILITIES, currency, parentAccount.id))
            accounts.add(createSubAccount("2123", "Accrued Interest", AccountType.ACCRUED_LIABILITIES, currency, parentAccount.id))
        }
        
        return accounts
    }
    
    private suspend fun createEquitySubAccounts(
        parentAccount: Account,
        currency: Currency,
        includeDetailed: Boolean
    ): List<Account> {
        val accounts = mutableListOf<Account>()
        
        accounts.add(createSubAccount("3100", "Owner's Equity", AccountType.OWNERS_EQUITY, currency, parentAccount.id))
        accounts.add(createSubAccount("3200", "Retained Earnings", AccountType.RETAINED_EARNINGS, currency, parentAccount.id))
        accounts.add(createSubAccount("3300", "Current Year Earnings", AccountType.CURRENT_YEAR_EARNINGS, currency, parentAccount.id))
        
        if (includeDetailed) {
            accounts.add(createSubAccount("3110", "Common Stock", AccountType.COMMON_STOCK, currency, parentAccount.id))
            accounts.add(createSubAccount("3120", "Preferred Stock", AccountType.PREFERRED_STOCK, currency, parentAccount.id))
            accounts.add(createSubAccount("3130", "Additional Paid-in Capital", AccountType.ADDITIONAL_PAID_IN_CAPITAL, currency, parentAccount.id))
            accounts.add(createSubAccount("3140", "Treasury Stock", AccountType.TREASURY_STOCK, currency, parentAccount.id))
        }
        
        return accounts
    }
    
    private suspend fun createRevenueSubAccounts(
        parentAccount: Account,
        currency: Currency,
        includeDetailed: Boolean
    ): List<Account> {
        val accounts = mutableListOf<Account>()
        
        accounts.add(createSubAccount("4100", "Sales Revenue", AccountType.SALES_REVENUE, currency, parentAccount.id))
        accounts.add(createSubAccount("4200", "Service Revenue", AccountType.SERVICE_REVENUE, currency, parentAccount.id))
        accounts.add(createSubAccount("4300", "Other Income", AccountType.OTHER_INCOME, currency, parentAccount.id))
        
        if (includeDetailed) {
            accounts.add(createSubAccount("4110", "Product Sales", AccountType.SALES_REVENUE, currency, parentAccount.id))
            accounts.add(createSubAccount("4120", "Sales Returns and Allowances", AccountType.SALES_RETURNS_ALLOWANCES, currency, parentAccount.id))
            accounts.add(createSubAccount("4130", "Sales Discounts", AccountType.SALES_DISCOUNTS, currency, parentAccount.id))
            
            accounts.add(createSubAccount("4210", "Consulting Revenue", AccountType.SERVICE_REVENUE, currency, parentAccount.id))
            accounts.add(createSubAccount("4220", "Maintenance Revenue", AccountType.SERVICE_REVENUE, currency, parentAccount.id))
            
            accounts.add(createSubAccount("4310", "Interest Income", AccountType.INTEREST_INCOME, currency, parentAccount.id))
            accounts.add(createSubAccount("4320", "Dividend Income", AccountType.DIVIDEND_INCOME, currency, parentAccount.id))
        }
        
        return accounts
    }
    
    private suspend fun createExpenseSubAccounts(
        parentAccount: Account,
        currency: Currency,
        includeDetailed: Boolean
    ): List<Account> {
        val accounts = mutableListOf<Account>()
        
        accounts.add(createSubAccount("5100", "Cost of Goods Sold", AccountType.COST_OF_GOODS_SOLD, currency, parentAccount.id))
        accounts.add(createSubAccount("5200", "Operating Expenses", AccountType.OPERATING_EXPENSES, currency, parentAccount.id))
        accounts.add(createSubAccount("5300", "Administrative Expenses", AccountType.ADMINISTRATIVE_EXPENSES, currency, parentAccount.id))
        accounts.add(createSubAccount("5400", "Selling Expenses", AccountType.SELLING_EXPENSES, currency, parentAccount.id))
        
        if (includeDetailed) {
            // COGS detail
            accounts.add(createSubAccount("5110", "Direct Materials", AccountType.COST_OF_GOODS_SOLD, currency, parentAccount.id))
            accounts.add(createSubAccount("5120", "Direct Labor", AccountType.COST_OF_GOODS_SOLD, currency, parentAccount.id))
            accounts.add(createSubAccount("5130", "Manufacturing Overhead", AccountType.COST_OF_GOODS_SOLD, currency, parentAccount.id))
            
            // Operating expenses detail
            accounts.add(createSubAccount("5210", "Salaries and Wages", AccountType.OPERATING_EXPENSES, currency, parentAccount.id))
            accounts.add(createSubAccount("5220", "Rent Expense", AccountType.OPERATING_EXPENSES, currency, parentAccount.id))
            accounts.add(createSubAccount("5230", "Utilities Expense", AccountType.OPERATING_EXPENSES, currency, parentAccount.id))
            accounts.add(createSubAccount("5240", "Insurance Expense", AccountType.OPERATING_EXPENSES, currency, parentAccount.id))
            accounts.add(createSubAccount("5250", "Depreciation Expense", AccountType.DEPRECIATION_EXPENSE, currency, parentAccount.id))
            
            // Administrative expenses detail
            accounts.add(createSubAccount("5310", "Office Supplies", AccountType.ADMINISTRATIVE_EXPENSES, currency, parentAccount.id))
            accounts.add(createSubAccount("5320", "Professional Fees", AccountType.ADMINISTRATIVE_EXPENSES, currency, parentAccount.id))
            accounts.add(createSubAccount("5330", "Travel Expenses", AccountType.ADMINISTRATIVE_EXPENSES, currency, parentAccount.id))
            
            // Selling expenses detail
            accounts.add(createSubAccount("5410", "Advertising Expense", AccountType.SELLING_EXPENSES, currency, parentAccount.id))
            accounts.add(createSubAccount("5420", "Sales Commissions", AccountType.SELLING_EXPENSES, currency, parentAccount.id))
            accounts.add(createSubAccount("5430", "Marketing Expenses", AccountType.SELLING_EXPENSES, currency, parentAccount.id))
        }
        
        return accounts
    }
    
    private suspend fun createSubAccount(
        code: String,
        name: String,
        type: AccountType,
        currency: Currency,
        parentId: AccountId
    ): Account {
        return Account.create(
            code = code,
            name = name,
            type = type,
            currency = currency,
            parentAccountId = parentId,
            isActive = true
        )
    }
    
    private fun isValidAccountCode(code: String, type: AccountType): Boolean {
        return when (type.category) {
            AccountCategory.ASSETS -> ASSET_CODE_PATTERN.matches(code)
            AccountCategory.LIABILITIES -> LIABILITY_CODE_PATTERN.matches(code)
            AccountCategory.EQUITY -> EQUITY_CODE_PATTERN.matches(code)
            AccountCategory.REVENUE -> REVENUE_CODE_PATTERN.matches(code)
            AccountCategory.EXPENSES -> EXPENSE_CODE_PATTERN.matches(code)
        }
    }
    
    private suspend fun calculateAccountDepth(account: Account): Int {
        var depth = 0
        var currentAccount = account
        
        while (currentAccount.parentAccountId != null) {
            depth++
            currentAccount = accountRepository.findById(currentAccount.parentAccountId!!)
                ?: break
            
            if (depth > MAX_HIERARCHY_DEPTH) {
                break // Prevent infinite loops
            }
        }
        
        return depth
    }
    
    private fun canBeChildOf(childType: AccountType, parentType: AccountType): Boolean {
        // Define valid parent-child relationships
        val validRelationships = mapOf(
            AccountType.CURRENT_ASSETS to listOf(AccountType.ASSETS),
            AccountType.FIXED_ASSETS to listOf(AccountType.ASSETS),
            AccountType.CASH to listOf(AccountType.CURRENT_ASSETS, AccountType.ASSETS),
            AccountType.ACCOUNTS_RECEIVABLE to listOf(AccountType.CURRENT_ASSETS, AccountType.ASSETS),
            AccountType.INVENTORY to listOf(AccountType.CURRENT_ASSETS, AccountType.ASSETS),
            AccountType.CURRENT_LIABILITIES to listOf(AccountType.LIABILITIES),
            AccountType.LONG_TERM_LIABILITIES to listOf(AccountType.LIABILITIES),
            AccountType.ACCOUNTS_PAYABLE to listOf(AccountType.CURRENT_LIABILITIES, AccountType.LIABILITIES),
            AccountType.SALES_REVENUE to listOf(AccountType.REVENUE),
            AccountType.SERVICE_REVENUE to listOf(AccountType.REVENUE),
            AccountType.OPERATING_EXPENSES to listOf(AccountType.EXPENSES),
            AccountType.COST_OF_GOODS_SOLD to listOf(AccountType.EXPENSES)
        )
        
        return validRelationships[childType]?.contains(parentType) ?: true
    }
    
    private suspend fun wouldCreateCircularReference(accountId: AccountId, proposedParentId: AccountId): Boolean {
        var currentParentId: AccountId? = proposedParentId
        val visitedIds = mutableSetOf<AccountId>()
        
        while (currentParentId != null) {
            if (currentParentId == accountId) {
                return true // Circular reference detected
            }
            
            if (visitedIds.contains(currentParentId)) {
                return true // Loop in existing hierarchy
            }
            
            visitedIds.add(currentParentId)
            
            val parentAccount = accountRepository.findById(currentParentId)
            currentParentId = parentAccount?.parentAccountId
        }
        
        return false
    }
    
    private suspend fun isAccountCodeTaken(code: String, excludeAccountId: AccountId?): Boolean {
        val existingAccount = accountRepository.findByCode(code)
        return existingAccount != null && existingAccount.id != excludeAccountId
    }
    
    private fun generateRecommendations(
        account: Account,
        parentAccount: Account?,
        issues: List<String>,
        warnings: List<String>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (issues.any { it.contains("code") && it.contains("pattern") }) {
            recommendations.add("Use account code pattern: ${getSuggestedCodePattern(account.type)}")
        }
        
        if (issues.any { it.contains("hierarchy depth") }) {
            recommendations.add("Consider restructuring account hierarchy to reduce nesting levels")
        }
        
        if (warnings.any { it.contains("currency") }) {
            recommendations.add("Consider using the same currency as parent account for consistency")
        }
        
        if (issues.any { it.contains("circular reference") }) {
            recommendations.add("Choose a different parent account to avoid circular references")
        }
        
        return recommendations
    }
    
    private fun getSuggestedCodePattern(type: AccountType): String {
        return when (type.category) {
            AccountCategory.ASSETS -> "1XXX (Assets: 1000-1999)"
            AccountCategory.LIABILITIES -> "2XXX (Liabilities: 2000-2999)"
            AccountCategory.EQUITY -> "3XXX (Equity: 3000-3999)"
            AccountCategory.REVENUE -> "4XXX (Revenue: 4000-4999)"
            AccountCategory.EXPENSES -> "5XXX-9XXX (Expenses: 5000-9999)"
        }
    }
    
    private fun validateReorganizationPlan(plan: ChartReorganizationPlan): AccountHierarchyValidation {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Validate moves don't create circular references
        for (move in plan.moves) {
            if (move.newParentId != null && wouldCreateCircularReferenceInPlan(move.accountId, move.newParentId, plan.moves)) {
                issues.add("Move ${move.accountId} would create circular reference")
            }
        }
        
        // Check for conflicting moves
        val accountsBeingMoved = plan.moves.map { it.accountId }.toSet()
        val duplicateAccountMoves = plan.moves.groupBy { it.accountId }.filter { it.value.size > 1 }
        if (duplicateAccountMoves.isNotEmpty()) {
            issues.add("Multiple moves specified for accounts: ${duplicateAccountMoves.keys}")
        }
        
        return AccountHierarchyValidation(
            isValid = issues.isEmpty(),
            issues = issues,
            warnings = warnings
        )
    }
    
    private fun wouldCreateCircularReferenceInPlan(
        accountId: AccountId,
        newParentId: AccountId,
        allMoves: List<AccountMove>
    ): Boolean {
        // Build a map of the new parent relationships
        val newParentMap = allMoves.associate { it.accountId to it.newParentId }
        
        var currentParentId: AccountId? = newParentId
        val visitedIds = mutableSetOf<AccountId>()
        
        while (currentParentId != null) {
            if (currentParentId == accountId) {
                return true // Circular reference detected
            }
            
            if (visitedIds.contains(currentParentId)) {
                return true // Loop detected
            }
            
            visitedIds.add(currentParentId)
            
            // Check if this account is being moved in the plan
            currentParentId = newParentMap[currentParentId] ?: run {
                // If not in the plan, check existing parent relationship
                // This would require a repository call in real implementation
                null
            }
        }
        
        return false
    }
    
    private suspend fun renumberAccountCodes(rules: AccountRenumberingRules) {
        // Implementation for renumbering account codes based on rules
        // This would involve updating account codes according to new numbering scheme
    }
    
    private fun validateAccountMerge(sourceAccount: Account, targetAccount: Account): AccountHierarchyValidation {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check account types compatibility
        if (sourceAccount.type != targetAccount.type) {
            issues.add("Cannot merge accounts of different types: ${sourceAccount.type} vs ${targetAccount.type}")
        }
        
        // Check currency compatibility
        if (sourceAccount.currency != targetAccount.currency) {
            issues.add("Cannot merge accounts with different currencies: ${sourceAccount.currency} vs ${targetAccount.currency}")
        }
        
        // Check if source account has child accounts
        // This would require a repository call to check for children
        
        // Check if accounts are active
        if (!sourceAccount.isActive) {
            warnings.add("Source account is not active")
        }
        
        if (!targetAccount.isActive) {
            warnings.add("Target account is not active")
        }
        
        return AccountHierarchyValidation(
            isValid = issues.isEmpty(),
            issues = issues,
            warnings = warnings
        )
    }
}

// ==================== DATA CLASSES ====================

/**
 * Chart of Accounts Structure Result
 */
data class ChartOfAccountsStructure(
    val companyName: String,
    val baseCurrency: Currency,
    val rootAccounts: List<Account>,
    val totalAccounts: Int,
    val createdDate: LocalDate,
    val isStandardStructure: Boolean
)

/**
 * Account Hierarchy Validation Result
 */
data class AccountHierarchyValidation(
    val isValid: Boolean,
    val issues: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val recommendedActions: List<String> = emptyList()
)

/**
 * Chart Reorganization Plan
 */
data class ChartReorganizationPlan(
    val moves: List<AccountMove>,
    val updateAccountCodes: Boolean = false,
    val renumberingRules: AccountRenumberingRules = AccountRenumberingRules()
)

/**
 * Account Move Definition
 */
data class AccountMove(
    val accountId: AccountId,
    val newParentId: AccountId?,
    val order: Int = 0
)

/**
 * Account Renumbering Rules
 */
data class AccountRenumberingRules(
    val preserveExistingCodes: Boolean = true,
    val startingNumbers: Map<AccountCategory, Int> = mapOf(
        AccountCategory.ASSETS to 1000,
        AccountCategory.LIABILITIES to 2000,
        AccountCategory.EQUITY to 3000,
        AccountCategory.REVENUE to 4000,
        AccountCategory.EXPENSES to 5000
    ),
    val increment: Int = 10
)

/**
 * Chart Reorganization Result
 */
data class ChartReorganizationResult(
    val success: Boolean,
    val movedAccounts: List<Account>,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Trial Balance Entry
 */
data class TrialBalanceEntry(
    val accountId: AccountId,
    val accountCode: String,
    val accountName: String,
    val accountType: AccountType,
    val debitAmount: FinancialAmount,
    val creditAmount: FinancialAmount,
    val balance: FinancialAmount
)

/**
 * Trial Balance Report
 */
data class TrialBalance(
    val asOfDate: LocalDate,
    val currency: Currency?,
    val entries: List<TrialBalanceEntry>,
    val totalDebits: FinancialAmount,
    val totalCredits: FinancialAmount,
    val isBalanced: Boolean,
    val generatedAt: LocalDate
)

/**
 * Account Merge Result
 */
data class AccountMergeResult(
    val success: Boolean,
    val sourceAccountId: AccountId,
    val targetAccountId: AccountId,
    val sourceBalance: FinancialAmount? = null,
    val targetBalanceBefore: FinancialAmount? = null,
    val targetBalanceAfter: FinancialAmount? = null,
    val reassignedTransactionCount: Int = 0,
    val mergeDate: LocalDate? = null,
    val mergeReason: String? = null,
    val errors: List<String> = emptyList()
)

/**
 * Account Archive Result
 */
data class AccountArchiveResult(
    val success: Boolean,
    val candidateAccounts: List<AccountId>,
    val archivedAccounts: List<AccountId>,
    val errors: List<String> = emptyList(),
    val isDryRun: Boolean = false
)

/**
 * Balance Type Enum
 */
enum class BalanceType {
    DEBIT, CREDIT
}

/**
 * Account Category Enum
 */
enum class AccountCategory {
    ASSETS, LIABILITIES, EQUITY, REVENUE, EXPENSES
}

/**
 * Extension for AccountType to get normal balance type
 */
val AccountType.normalBalanceType: BalanceType
    get() = when (this.category) {
        AccountCategory.ASSETS, AccountCategory.EXPENSES -> BalanceType.DEBIT
        AccountCategory.LIABILITIES, AccountCategory.EQUITY, AccountCategory.REVENUE -> BalanceType.CREDIT
    }

/**
 * Extension for AccountType to get category
 */
val AccountType.category: AccountCategory
    get() = when (this) {
        AccountType.ASSETS, AccountType.CURRENT_ASSETS, AccountType.FIXED_ASSETS, 
        AccountType.CASH, AccountType.ACCOUNTS_RECEIVABLE, AccountType.INVENTORY,
        AccountType.PREPAID_EXPENSES, AccountType.PROPERTY_PLANT_EQUIPMENT,
        AccountType.ACCUMULATED_DEPRECIATION, AccountType.ALLOWANCE_DOUBTFUL_ACCOUNTS -> AccountCategory.ASSETS
        
        AccountType.LIABILITIES, AccountType.CURRENT_LIABILITIES, AccountType.LONG_TERM_LIABILITIES,
        AccountType.ACCOUNTS_PAYABLE, AccountType.ACCRUED_LIABILITIES, AccountType.SHORT_TERM_DEBT,
        AccountType.LONG_TERM_DEBT, AccountType.DEFERRED_TAX_LIABILITY -> AccountCategory.LIABILITIES
        
        AccountType.EQUITY, AccountType.OWNERS_EQUITY, AccountType.RETAINED_EARNINGS,
        AccountType.CURRENT_YEAR_EARNINGS, AccountType.COMMON_STOCK, AccountType.PREFERRED_STOCK,
        AccountType.ADDITIONAL_PAID_IN_CAPITAL, AccountType.TREASURY_STOCK -> AccountCategory.EQUITY
        
        AccountType.REVENUE, AccountType.SALES_REVENUE, AccountType.SERVICE_REVENUE,
        AccountType.OTHER_INCOME, AccountType.INTEREST_INCOME, AccountType.DIVIDEND_INCOME,
        AccountType.SALES_RETURNS_ALLOWANCES, AccountType.SALES_DISCOUNTS -> AccountCategory.REVENUE
        
        AccountType.EXPENSES, AccountType.COST_OF_GOODS_SOLD, AccountType.OPERATING_EXPENSES,
        AccountType.ADMINISTRATIVE_EXPENSES, AccountType.SELLING_EXPENSES,
        AccountType.DEPRECIATION_EXPENSE -> AccountCategory.EXPENSES
    }

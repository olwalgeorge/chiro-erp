package org.chiro.finance.domain.valueobject

/**
 * Account Type enumeration for the Chart of Accounts
 * Following standard accounting principles
 */
enum class AccountType(
    val displayName: String,
    val normalBalance: BalanceType,
    val category: AccountCategory
) {
    // Assets
    CASH("Cash", BalanceType.DEBIT, AccountCategory.ASSET),
    ACCOUNTS_RECEIVABLE("Accounts Receivable", BalanceType.DEBIT, AccountCategory.ASSET),
    INVENTORY("Inventory", BalanceType.DEBIT, AccountCategory.ASSET),
    PREPAID_EXPENSES("Prepaid Expenses", BalanceType.DEBIT, AccountCategory.ASSET),
    FIXED_ASSETS("Fixed Assets", BalanceType.DEBIT, AccountCategory.ASSET),
    ACCUMULATED_DEPRECIATION("Accumulated Depreciation", BalanceType.CREDIT, AccountCategory.ASSET),
    
    // Liabilities
    ACCOUNTS_PAYABLE("Accounts Payable", BalanceType.CREDIT, AccountCategory.LIABILITY),
    ACCRUED_LIABILITIES("Accrued Liabilities", BalanceType.CREDIT, AccountCategory.LIABILITY),
    SHORT_TERM_DEBT("Short Term Debt", BalanceType.CREDIT, AccountCategory.LIABILITY),
    LONG_TERM_DEBT("Long Term Debt", BalanceType.CREDIT, AccountCategory.LIABILITY),
    
    // Equity
    OWNERS_EQUITY("Owner's Equity", BalanceType.CREDIT, AccountCategory.EQUITY),
    RETAINED_EARNINGS("Retained Earnings", BalanceType.CREDIT, AccountCategory.EQUITY),
    
    // Revenue
    SALES_REVENUE("Sales Revenue", BalanceType.CREDIT, AccountCategory.REVENUE),
    SERVICE_REVENUE("Service Revenue", BalanceType.CREDIT, AccountCategory.REVENUE),
    OTHER_REVENUE("Other Revenue", BalanceType.CREDIT, AccountCategory.REVENUE),
    
    // Expenses
    COST_OF_GOODS_SOLD("Cost of Goods Sold", BalanceType.DEBIT, AccountCategory.EXPENSE),
    SALARY_EXPENSE("Salary Expense", BalanceType.DEBIT, AccountCategory.EXPENSE),
    RENT_EXPENSE("Rent Expense", BalanceType.DEBIT, AccountCategory.EXPENSE),
    UTILITIES_EXPENSE("Utilities Expense", BalanceType.DEBIT, AccountCategory.EXPENSE),
    DEPRECIATION_EXPENSE("Depreciation Expense", BalanceType.DEBIT, AccountCategory.EXPENSE),
    OTHER_EXPENSE("Other Expense", BalanceType.DEBIT, AccountCategory.EXPENSE);
    
    fun isAsset() = category == AccountCategory.ASSET
    fun isLiability() = category == AccountCategory.LIABILITY
    fun isEquity() = category == AccountCategory.EQUITY
    fun isRevenue() = category == AccountCategory.REVENUE
    fun isExpense() = category == AccountCategory.EXPENSE
    
    fun isIncomeStatement() = category == AccountCategory.REVENUE || category == AccountCategory.EXPENSE
    fun isBalanceSheet() = category == AccountCategory.ASSET || category == AccountCategory.LIABILITY || category == AccountCategory.EQUITY
}

enum class BalanceType {
    DEBIT, CREDIT
}

enum class AccountCategory {
    ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
}

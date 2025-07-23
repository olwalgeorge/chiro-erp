package org.chiro.finance.domain.valueobject

/**
 * Account Type Classification for World-Class ERP Chart of Accounts
 * 
 * Provides comprehensive account classification following international
 * accounting standards (IFRS/GAAP) with ERP-specific extensions for
 * manufacturing, multi-currency, and regulatory compliance.
 * 
 * Features:
 * - Standard 5-account classification (Assets, Liabilities, Equity, Revenue, Expenses)
 * - Detailed sub-classifications for ERP operations
 * - Normal balance types for double-entry validation
 * - Financial statement categorization
 * - Industry-specific account types
 * - Multi-currency and international support
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
enum class AccountType(
    val displayName: String,
    val normalBalance: BalanceType,
    val category: AccountCategory,
    val subCategory: AccountSubCategory,
    val accountCodePrefix: String,
    val isControlAccount: Boolean = false,
    val allowsDirectPosting: Boolean = true,
    val requiresSubsidiary: Boolean = false
) {
    // ===================== ASSETS (1000-1999) =====================
    
    // Current Assets (1000-1199)
    CASH(
        "Cash and Cash Equivalents", BalanceType.DEBIT, AccountCategory.ASSET, 
        AccountSubCategory.CURRENT_ASSET, "1000", allowsDirectPosting = false
    ),
    PETTY_CASH(
        "Petty Cash", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1010"
    ),
    CHECKING_ACCOUNT(
        "Checking Account", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1020"
    ),
    SAVINGS_ACCOUNT(
        "Savings Account", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1030"
    ),
    MONEY_MARKET_ACCOUNT(
        "Money Market Account", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1040"
    ),
    
    ACCOUNTS_RECEIVABLE(
        "Accounts Receivable", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1100", isControlAccount = true, requiresSubsidiary = true
    ),
    ALLOWANCE_FOR_DOUBTFUL_ACCOUNTS(
        "Allowance for Doubtful Accounts", BalanceType.CREDIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1105"
    ),
    NOTES_RECEIVABLE(
        "Notes Receivable", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1110"
    ),
    EMPLOYEE_ADVANCES(
        "Employee Advances", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1120"
    ),
    
    // Inventory (1200-1299)
    RAW_MATERIALS(
        "Raw Materials Inventory", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INVENTORY, "1200"
    ),
    WORK_IN_PROGRESS(
        "Work in Progress", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INVENTORY, "1210"
    ),
    FINISHED_GOODS(
        "Finished Goods Inventory", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INVENTORY, "1220"
    ),
    INVENTORY_SUPPLIES(
        "Inventory Supplies", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INVENTORY, "1230"
    ),
    INVENTORY_IN_TRANSIT(
        "Inventory in Transit", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INVENTORY, "1240"
    ),
    
    // Prepaid and Other Current Assets (1300-1399)
    PREPAID_EXPENSES(
        "Prepaid Expenses", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1300"
    ),
    PREPAID_INSURANCE(
        "Prepaid Insurance", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1310"
    ),
    PREPAID_RENT(
        "Prepaid Rent", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1320"
    ),
    DEPOSITS(
        "Security Deposits", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.CURRENT_ASSET, "1330"
    ),
    
    // Fixed Assets (1400-1699)
    LAND(
        "Land", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1400"
    ),
    BUILDINGS(
        "Buildings", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1410"
    ),
    ACCUMULATED_DEPRECIATION_BUILDINGS(
        "Accumulated Depreciation - Buildings", BalanceType.CREDIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1415"
    ),
    MACHINERY_EQUIPMENT(
        "Machinery and Equipment", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1420"
    ),
    ACCUMULATED_DEPRECIATION_MACHINERY(
        "Accumulated Depreciation - Machinery", BalanceType.CREDIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1425"
    ),
    VEHICLES(
        "Vehicles", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1430"
    ),
    ACCUMULATED_DEPRECIATION_VEHICLES(
        "Accumulated Depreciation - Vehicles", BalanceType.CREDIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1435"
    ),
    FURNITURE_FIXTURES(
        "Furniture and Fixtures", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1440"
    ),
    ACCUMULATED_DEPRECIATION_FURNITURE(
        "Accumulated Depreciation - Furniture", BalanceType.CREDIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1445"
    ),
    COMPUTER_EQUIPMENT(
        "Computer Equipment", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1450"
    ),
    ACCUMULATED_DEPRECIATION_COMPUTER(
        "Accumulated Depreciation - Computer", BalanceType.CREDIT, AccountCategory.ASSET,
        AccountSubCategory.FIXED_ASSET, "1455"
    ),
    
    // Intangible Assets (1700-1799)
    GOODWILL(
        "Goodwill", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INTANGIBLE_ASSET, "1700"
    ),
    PATENTS(
        "Patents", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INTANGIBLE_ASSET, "1710"
    ),
    TRADEMARKS(
        "Trademarks", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INTANGIBLE_ASSET, "1720"
    ),
    SOFTWARE_LICENSES(
        "Software Licenses", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.INTANGIBLE_ASSET, "1730"
    ),
    
    // Long-term Assets (1800-1899)
    INVESTMENTS(
        "Long-term Investments", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.LONG_TERM_ASSET, "1800"
    ),
    LOANS_TO_EMPLOYEES(
        "Loans to Employees", BalanceType.DEBIT, AccountCategory.ASSET,
        AccountSubCategory.LONG_TERM_ASSET, "1810"
    ),
    
    // ===================== LIABILITIES (2000-2999) =====================
    
    // Current Liabilities (2000-2299)
    ACCOUNTS_PAYABLE(
        "Accounts Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2000", isControlAccount = true, requiresSubsidiary = true
    ),
    NOTES_PAYABLE(
        "Notes Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2010"
    ),
    ACCRUED_WAGES(
        "Accrued Wages Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2020"
    ),
    ACCRUED_TAXES(
        "Accrued Taxes Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2030"
    ),
    SALES_TAX_PAYABLE(
        "Sales Tax Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2040"
    ),
    PAYROLL_TAX_PAYABLE(
        "Payroll Tax Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2050"
    ),
    INCOME_TAX_PAYABLE(
        "Income Tax Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2060"
    ),
    UNEARNED_REVENUE(
        "Unearned Revenue", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2070"
    ),
    CUSTOMER_DEPOSITS(
        "Customer Deposits", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.CURRENT_LIABILITY, "2080"
    ),
    
    // Long-term Liabilities (2300-2999)
    LONG_TERM_DEBT(
        "Long-term Debt", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.LONG_TERM_LIABILITY, "2300"
    ),
    MORTGAGE_PAYABLE(
        "Mortgage Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.LONG_TERM_LIABILITY, "2310"
    ),
    BONDS_PAYABLE(
        "Bonds Payable", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.LONG_TERM_LIABILITY, "2320"
    ),
    DEFERRED_TAX_LIABILITY(
        "Deferred Tax Liability", BalanceType.CREDIT, AccountCategory.LIABILITY,
        AccountSubCategory.LONG_TERM_LIABILITY, "2330"
    ),
    
    // ===================== EQUITY (3000-3999) =====================
    COMMON_STOCK(
        "Common Stock", BalanceType.CREDIT, AccountCategory.EQUITY,
        AccountSubCategory.STOCKHOLDERS_EQUITY, "3000"
    ),
    PREFERRED_STOCK(
        "Preferred Stock", BalanceType.CREDIT, AccountCategory.EQUITY,
        AccountSubCategory.STOCKHOLDERS_EQUITY, "3010"
    ),
    ADDITIONAL_PAID_IN_CAPITAL(
        "Additional Paid-in Capital", BalanceType.CREDIT, AccountCategory.EQUITY,
        AccountSubCategory.STOCKHOLDERS_EQUITY, "3020"
    ),
    RETAINED_EARNINGS(
        "Retained Earnings", BalanceType.CREDIT, AccountCategory.EQUITY,
        AccountSubCategory.STOCKHOLDERS_EQUITY, "3100"
    ),
    TREASURY_STOCK(
        "Treasury Stock", BalanceType.DEBIT, AccountCategory.EQUITY,
        AccountSubCategory.STOCKHOLDERS_EQUITY, "3200"
    ),
    DIVIDENDS(
        "Dividends", BalanceType.DEBIT, AccountCategory.EQUITY,
        AccountSubCategory.STOCKHOLDERS_EQUITY, "3300"
    ),
    OWNERS_EQUITY(
        "Owner's Equity", BalanceType.CREDIT, AccountCategory.EQUITY,
        AccountSubCategory.OWNERS_EQUITY, "3400"
    ),
    OWNERS_DRAW(
        "Owner's Draw", BalanceType.DEBIT, AccountCategory.EQUITY,
        AccountSubCategory.OWNERS_EQUITY, "3410"
    ),
    
    // ===================== REVENUE (4000-4999) =====================
    SALES_REVENUE(
        "Sales Revenue", BalanceType.CREDIT, AccountCategory.REVENUE,
        AccountSubCategory.OPERATING_REVENUE, "4000"
    ),
    SERVICE_REVENUE(
        "Service Revenue", BalanceType.CREDIT, AccountCategory.REVENUE,
        AccountSubCategory.OPERATING_REVENUE, "4100"
    ),
    CONSULTING_REVENUE(
        "Consulting Revenue", BalanceType.CREDIT, AccountCategory.REVENUE,
        AccountSubCategory.OPERATING_REVENUE, "4200"
    ),
    RENTAL_INCOME(
        "Rental Income", BalanceType.CREDIT, AccountCategory.REVENUE,
        AccountSubCategory.NON_OPERATING_REVENUE, "4300"
    ),
    INTEREST_INCOME(
        "Interest Income", BalanceType.CREDIT, AccountCategory.REVENUE,
        AccountSubCategory.NON_OPERATING_REVENUE, "4400"
    ),
    DIVIDEND_INCOME(
        "Dividend Income", BalanceType.CREDIT, AccountCategory.REVENUE,
        AccountSubCategory.NON_OPERATING_REVENUE, "4410"
    ),
    GAIN_ON_SALE_OF_ASSETS(
        "Gain on Sale of Assets", BalanceType.CREDIT, AccountCategory.REVENUE,
        AccountSubCategory.NON_OPERATING_REVENUE, "4500"
    ),
    FOREIGN_EXCHANGE_GAIN(
        "Foreign Exchange Gain", BalanceType.CREDIT, AccountCategory.REVENUE,
        AccountSubCategory.NON_OPERATING_REVENUE, "4600"
    ),
    
    // ===================== EXPENSES (5000-9999) =====================
    
    // Cost of Goods Sold (5000-5999)
    COST_OF_GOODS_SOLD(
        "Cost of Goods Sold", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.COST_OF_SALES, "5000"
    ),
    PURCHASES(
        "Purchases", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.COST_OF_SALES, "5100"
    ),
    PURCHASE_RETURNS(
        "Purchase Returns", BalanceType.CREDIT, AccountCategory.EXPENSE,
        AccountSubCategory.COST_OF_SALES, "5110"
    ),
    FREIGHT_IN(
        "Freight In", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.COST_OF_SALES, "5200"
    ),
    DIRECT_LABOR(
        "Direct Labor", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.COST_OF_SALES, "5300"
    ),
    MANUFACTURING_OVERHEAD(
        "Manufacturing Overhead", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.COST_OF_SALES, "5400"
    ),
    
    // Operating Expenses (6000-7999)
    SALARIES_EXPENSE(
        "Salaries Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6000"
    ),
    WAGES_EXPENSE(
        "Wages Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6010"
    ),
    BENEFITS_EXPENSE(
        "Employee Benefits Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6020"
    ),
    PAYROLL_TAX_EXPENSE(
        "Payroll Tax Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6030"
    ),
    RENT_EXPENSE(
        "Rent Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6100"
    ),
    UTILITIES_EXPENSE(
        "Utilities Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6200"
    ),
    INSURANCE_EXPENSE(
        "Insurance Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6300"
    ),
    DEPRECIATION_EXPENSE(
        "Depreciation Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6400"
    ),
    AMORTIZATION_EXPENSE(
        "Amortization Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6410"
    ),
    REPAIRS_MAINTENANCE(
        "Repairs and Maintenance", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6500"
    ),
    OFFICE_SUPPLIES(
        "Office Supplies Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6600"
    ),
    ADVERTISING_EXPENSE(
        "Advertising Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6700"
    ),
    PROFESSIONAL_FEES(
        "Professional Fees", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6800"
    ),
    TRAVEL_EXPENSE(
        "Travel Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "6900"
    ),
    TRAINING_EXPENSE(
        "Training and Development", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "7000"
    ),
    BAD_DEBT_EXPENSE(
        "Bad Debt Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.OPERATING_EXPENSE, "7100"
    ),
    
    // Non-Operating Expenses (8000-8999)
    INTEREST_EXPENSE(
        "Interest Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.NON_OPERATING_EXPENSE, "8000"
    ),
    LOSS_ON_SALE_OF_ASSETS(
        "Loss on Sale of Assets", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.NON_OPERATING_EXPENSE, "8100"
    ),
    FOREIGN_EXCHANGE_LOSS(
        "Foreign Exchange Loss", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.NON_OPERATING_EXPENSE, "8200"
    ),
    
    // Tax Expenses (9000-9999)
    INCOME_TAX_EXPENSE(
        "Income Tax Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.TAX_EXPENSE, "9000"
    ),
    STATE_TAX_EXPENSE(
        "State Tax Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.TAX_EXPENSE, "9100"
    ),
    FRANCHISE_TAX_EXPENSE(
        "Franchise Tax Expense", BalanceType.DEBIT, AccountCategory.EXPENSE,
        AccountSubCategory.TAX_EXPENSE, "9200"
    );
    
    // ===================== COMPUTED PROPERTIES =====================
    
    fun isAsset() = category == AccountCategory.ASSET
    fun isLiability() = category == AccountCategory.LIABILITY
    fun isEquity() = category == AccountCategory.EQUITY
    fun isRevenue() = category == AccountCategory.REVENUE
    fun isExpense() = category == AccountCategory.EXPENSE
    
    fun isIncomeStatement() = category == AccountCategory.REVENUE || category == AccountCategory.EXPENSE
    fun isBalanceSheet() = category == AccountCategory.ASSET || category == AccountCategory.LIABILITY || category == AccountCategory.EQUITY
    
    fun isCurrentAsset() = subCategory == AccountSubCategory.CURRENT_ASSET
    fun isFixedAsset() = subCategory == AccountSubCategory.FIXED_ASSET
    fun isCurrentLiability() = subCategory == AccountSubCategory.CURRENT_LIABILITY
    fun isLongTermLiability() = subCategory == AccountSubCategory.LONG_TERM_LIABILITY
    
    fun isOperatingAccount() = subCategory in setOf(
        AccountSubCategory.OPERATING_REVENUE,
        AccountSubCategory.COST_OF_SALES,
        AccountSubCategory.OPERATING_EXPENSE
    )
    
    fun isNonOperatingAccount() = subCategory in setOf(
        AccountSubCategory.NON_OPERATING_REVENUE,
        AccountSubCategory.NON_OPERATING_EXPENSE
    )
    
    /**
     * Get the account code range for this account type
     */
    fun getAccountCodeRange(): IntRange {
        val prefix = accountCodePrefix.toInt()
        return when (category) {
            AccountCategory.ASSET -> prefix..(prefix + 99)
            AccountCategory.LIABILITY -> prefix..(prefix + 99)
            AccountCategory.EQUITY -> prefix..(prefix + 99)
            AccountCategory.REVENUE -> prefix..(prefix + 99)
            AccountCategory.EXPENSE -> prefix..(prefix + 99)
        }
    }
    
    /**
     * Validate if an account code is appropriate for this account type
     */
    fun isValidAccountCode(accountCode: String): Boolean {
        return try {
            val code = accountCode.toInt()
            code in getAccountCodeRange()
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Get recommended account codes for this type
     */
    fun getRecommendedAccountCodes(): List<String> {
        val range = getAccountCodeRange()
        return listOf(
            range.first.toString(),
            (range.first + 10).toString(),
            (range.first + 20).toString()
        ).filter { it.toInt() <= range.last }
    }
}

/**
 * Balance Type for Double-Entry Bookkeeping
 */
enum class BalanceType(val displayName: String) {
    DEBIT("Debit"),
    CREDIT("Credit");
    
    fun opposite(): BalanceType = when (this) {
        DEBIT -> CREDIT
        CREDIT -> DEBIT
    }
}

/**
 * Primary Account Categories (Financial Statement Classifications)
 */
enum class AccountCategory(val displayName: String, val sortOrder: Int) {
    ASSET("Assets", 1),
    LIABILITY("Liabilities", 2),
    EQUITY("Equity", 3),
    REVENUE("Revenue", 4),
    EXPENSE("Expenses", 5)
}

/**
 * Detailed Account Sub-Categories for Enhanced Reporting
 */
enum class AccountSubCategory(
    val displayName: String,
    val parentCategory: AccountCategory,
    val sortOrder: Int
) {
    // Asset Subcategories
    CURRENT_ASSET("Current Assets", AccountCategory.ASSET, 1),
    INVENTORY("Inventory", AccountCategory.ASSET, 2),
    FIXED_ASSET("Fixed Assets", AccountCategory.ASSET, 3),
    INTANGIBLE_ASSET("Intangible Assets", AccountCategory.ASSET, 4),
    LONG_TERM_ASSET("Long-term Assets", AccountCategory.ASSET, 5),
    
    // Liability Subcategories
    CURRENT_LIABILITY("Current Liabilities", AccountCategory.LIABILITY, 1),
    LONG_TERM_LIABILITY("Long-term Liabilities", AccountCategory.LIABILITY, 2),
    
    // Equity Subcategories
    STOCKHOLDERS_EQUITY("Stockholders' Equity", AccountCategory.EQUITY, 1),
    OWNERS_EQUITY("Owner's Equity", AccountCategory.EQUITY, 2),
    
    // Revenue Subcategories
    OPERATING_REVENUE("Operating Revenue", AccountCategory.REVENUE, 1),
    NON_OPERATING_REVENUE("Non-Operating Revenue", AccountCategory.REVENUE, 2),
    
    // Expense Subcategories
    COST_OF_SALES("Cost of Sales", AccountCategory.EXPENSE, 1),
    OPERATING_EXPENSE("Operating Expenses", AccountCategory.EXPENSE, 2),
    NON_OPERATING_EXPENSE("Non-Operating Expenses", AccountCategory.EXPENSE, 3),
    TAX_EXPENSE("Tax Expenses", AccountCategory.EXPENSE, 4)
}

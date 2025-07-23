package org.chiro.finance.domain.valueobject

/**
 * Transaction Type Classification for Journal Entries
 * 
 * Provides comprehensive transaction categorization for financial reporting,
 * audit trails, and business intelligence. Supports both standard accounting
 * transactions and ERP-specific operations.
 * 
 * Features:
 * - Standard accounting transaction types
 * - ERP module integration (Sales, Procurement, Manufacturing, etc.)
 * - Automated vs Manual transaction tracking
 * - Audit and compliance categorization
 * - Reversing transaction support
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
enum class TransactionType(
    val displayName: String,
    val description: String,
    val category: TransactionCategory,
    val isSystemGenerated: Boolean = false,
    val allowsReversal: Boolean = true,
    val requiresApproval: Boolean = false
) {
    // ===================== MANUAL ACCOUNTING ENTRIES =====================
    MANUAL_JOURNAL_ENTRY(
        "Manual Journal Entry", 
        "User-created general ledger entry",
        TransactionCategory.GENERAL_LEDGER,
        isSystemGenerated = false,
        requiresApproval = true
    ),
    
    ADJUSTING_ENTRY(
        "Adjusting Entry", 
        "Period-end adjustment entry",
        TransactionCategory.GENERAL_LEDGER,
        requiresApproval = true
    ),
    
    CLOSING_ENTRY(
        "Closing Entry", 
        "Period-end closing entry",
        TransactionCategory.GENERAL_LEDGER,
        isSystemGenerated = true,
        allowsReversal = false
    ),
    
    REVERSING_ENTRY(
        "Reversing Entry", 
        "Reversal of previous transaction",
        TransactionCategory.GENERAL_LEDGER,
        allowsReversal = false
    ),
    
    // ===================== ACCOUNTS RECEIVABLE =====================
    CUSTOMER_INVOICE(
        "Customer Invoice", 
        "Sales invoice to customer",
        TransactionCategory.ACCOUNTS_RECEIVABLE,
        isSystemGenerated = true
    ),
    
    CUSTOMER_PAYMENT(
        "Customer Payment", 
        "Payment received from customer",
        TransactionCategory.ACCOUNTS_RECEIVABLE,
        isSystemGenerated = true
    ),
    
    CUSTOMER_CREDIT_MEMO(
        "Customer Credit Memo", 
        "Credit memo issued to customer",
        TransactionCategory.ACCOUNTS_RECEIVABLE
    ),
    
    CUSTOMER_DEBIT_MEMO(
        "Customer Debit Memo", 
        "Debit memo issued to customer",
        TransactionCategory.ACCOUNTS_RECEIVABLE
    ),
    
    BAD_DEBT_WRITEOFF(
        "Bad Debt Write-off", 
        "Uncollectible receivable write-off",
        TransactionCategory.ACCOUNTS_RECEIVABLE,
        requiresApproval = true
    ),
    
    // ===================== ACCOUNTS PAYABLE =====================
    VENDOR_BILL(
        "Vendor Bill", 
        "Bill received from vendor",
        TransactionCategory.ACCOUNTS_PAYABLE,
        isSystemGenerated = true
    ),
    
    VENDOR_PAYMENT(
        "Vendor Payment", 
        "Payment made to vendor",
        TransactionCategory.ACCOUNTS_PAYABLE,
        isSystemGenerated = true
    ),
    
    VENDOR_CREDIT_MEMO(
        "Vendor Credit Memo", 
        "Credit memo received from vendor",
        TransactionCategory.ACCOUNTS_PAYABLE
    ),
    
    VENDOR_DEBIT_MEMO(
        "Vendor Debit Memo", 
        "Debit memo received from vendor",
        TransactionCategory.ACCOUNTS_PAYABLE
    ),
    
    // ===================== INVENTORY MANAGEMENT =====================
    INVENTORY_RECEIPT(
        "Inventory Receipt", 
        "Goods received into inventory",
        TransactionCategory.INVENTORY,
        isSystemGenerated = true
    ),
    
    INVENTORY_ISSUE(
        "Inventory Issue", 
        "Goods issued from inventory",
        TransactionCategory.INVENTORY,
        isSystemGenerated = true
    ),
    
    INVENTORY_ADJUSTMENT(
        "Inventory Adjustment", 
        "Physical inventory count adjustment",
        TransactionCategory.INVENTORY,
        requiresApproval = true
    ),
    
    INVENTORY_TRANSFER(
        "Inventory Transfer", 
        "Goods transferred between locations",
        TransactionCategory.INVENTORY,
        isSystemGenerated = true
    ),
    
    INVENTORY_VALUATION(
        "Inventory Valuation", 
        "Inventory revaluation entry",
        TransactionCategory.INVENTORY,
        isSystemGenerated = true,
        requiresApproval = true
    ),
    
    // ===================== SALES OPERATIONS =====================
    SALES_ORDER_DEPOSIT(
        "Sales Order Deposit", 
        "Customer deposit on sales order",
        TransactionCategory.SALES,
        isSystemGenerated = true
    ),
    
    SALES_RETURN(
        "Sales Return", 
        "Customer return processing",
        TransactionCategory.SALES,
        isSystemGenerated = true
    ),
    
    COST_OF_GOODS_SOLD(
        "Cost of Goods Sold", 
        "COGS recognition on sale",
        TransactionCategory.SALES,
        isSystemGenerated = true
    ),
    
    // ===================== PROCUREMENT =====================
    PURCHASE_ORDER_PREPAYMENT(
        "Purchase Order Prepayment", 
        "Prepayment to vendor on PO",
        TransactionCategory.PROCUREMENT,
        isSystemGenerated = true
    ),
    
    PURCHASE_RETURN(
        "Purchase Return", 
        "Return to vendor processing",
        TransactionCategory.PROCUREMENT,
        isSystemGenerated = true
    ),
    
    // ===================== MANUFACTURING =====================
    WORK_ORDER_COST(
        "Work Order Cost", 
        "Manufacturing cost allocation",
        TransactionCategory.MANUFACTURING,
        isSystemGenerated = true
    ),
    
    MATERIAL_CONSUMPTION(
        "Material Consumption", 
        "Raw material consumption in production",
        TransactionCategory.MANUFACTURING,
        isSystemGenerated = true
    ),
    
    LABOR_COST_ALLOCATION(
        "Labor Cost Allocation", 
        "Direct labor cost allocation",
        TransactionCategory.MANUFACTURING,
        isSystemGenerated = true
    ),
    
    OVERHEAD_ALLOCATION(
        "Overhead Allocation", 
        "Manufacturing overhead allocation",
        TransactionCategory.MANUFACTURING,
        isSystemGenerated = true
    ),
    
    // ===================== FIXED ASSETS =====================
    ASSET_ACQUISITION(
        "Asset Acquisition", 
        "Fixed asset purchase",
        TransactionCategory.FIXED_ASSETS,
        requiresApproval = true
    ),
    
    ASSET_DISPOSAL(
        "Asset Disposal", 
        "Fixed asset disposal",
        TransactionCategory.FIXED_ASSETS,
        requiresApproval = true
    ),
    
    DEPRECIATION(
        "Depreciation", 
        "Periodic depreciation expense",
        TransactionCategory.FIXED_ASSETS,
        isSystemGenerated = true
    ),
    
    ASSET_REVALUATION(
        "Asset Revaluation", 
        "Asset fair value adjustment",
        TransactionCategory.FIXED_ASSETS,
        requiresApproval = true
    ),
    
    // ===================== PAYROLL =====================
    PAYROLL_EXPENSE(
        "Payroll Expense", 
        "Employee salary and wage expense",
        TransactionCategory.PAYROLL,
        isSystemGenerated = true
    ),
    
    PAYROLL_TAX(
        "Payroll Tax", 
        "Employer payroll tax expense",
        TransactionCategory.PAYROLL,
        isSystemGenerated = true
    ),
    
    EMPLOYEE_ADVANCE(
        "Employee Advance", 
        "Advance payment to employee",
        TransactionCategory.PAYROLL
    ),
    
    // ===================== BANKING =====================
    BANK_DEPOSIT(
        "Bank Deposit", 
        "Cash deposit to bank account",
        TransactionCategory.BANKING,
        isSystemGenerated = true
    ),
    
    BANK_WITHDRAWAL(
        "Bank Withdrawal", 
        "Cash withdrawal from bank account",
        TransactionCategory.BANKING
    ),
    
    BANK_TRANSFER(
        "Bank Transfer", 
        "Transfer between bank accounts",
        TransactionCategory.BANKING
    ),
    
    BANK_FEE(
        "Bank Fee", 
        "Bank service charge",
        TransactionCategory.BANKING,
        isSystemGenerated = true
    ),
    
    BANK_INTEREST(
        "Bank Interest", 
        "Interest earned or paid",
        TransactionCategory.BANKING,
        isSystemGenerated = true
    ),
    
    // ===================== FOREIGN EXCHANGE =====================
    CURRENCY_REVALUATION(
        "Currency Revaluation", 
        "Foreign currency revaluation",
        TransactionCategory.FOREIGN_EXCHANGE,
        isSystemGenerated = true
    ),
    
    EXCHANGE_RATE_GAIN_LOSS(
        "Exchange Rate Gain/Loss", 
        "Realized exchange rate difference",
        TransactionCategory.FOREIGN_EXCHANGE,
        isSystemGenerated = true
    );
    
    /**
     * Check if transaction is automatically generated by system
     */
    val isAutomated: Boolean get() = isSystemGenerated
    
    /**
     * Check if transaction affects balance sheet accounts
     */
    val affectsBalanceSheet: Boolean 
        get() = category in setOf(
            TransactionCategory.ACCOUNTS_RECEIVABLE,
            TransactionCategory.ACCOUNTS_PAYABLE,
            TransactionCategory.INVENTORY,
            TransactionCategory.FIXED_ASSETS,
            TransactionCategory.BANKING
        )
    
    /**
     * Check if transaction affects income statement accounts
     */
    val affectsIncomeStatement: Boolean
        get() = category in setOf(
            TransactionCategory.SALES,
            TransactionCategory.PROCUREMENT,
            TransactionCategory.MANUFACTURING,
            TransactionCategory.PAYROLL
        )
    
    /**
     * Check if transaction requires multi-level approval
     */
    val requiresMultiLevelApproval: Boolean
        get() = this in setOf(
            MANUAL_JOURNAL_ENTRY,
            BAD_DEBT_WRITEOFF,
            INVENTORY_ADJUSTMENT,
            INVENTORY_VALUATION,
            ASSET_ACQUISITION,
            ASSET_DISPOSAL,
            ASSET_REVALUATION
        )
    
    /**
     * Get related account types that this transaction typically affects
     */
    fun getTypicalAccountTypes(): Set<AccountType> {
        return when (this) {
            CUSTOMER_INVOICE -> setOf(AccountType.ACCOUNTS_RECEIVABLE, AccountType.SALES_REVENUE)
            CUSTOMER_PAYMENT -> setOf(AccountType.CASH, AccountType.ACCOUNTS_RECEIVABLE)
            VENDOR_BILL -> setOf(AccountType.ACCOUNTS_PAYABLE, AccountType.COST_OF_GOODS_SOLD)
            VENDOR_PAYMENT -> setOf(AccountType.ACCOUNTS_PAYABLE, AccountType.CASH)
            INVENTORY_RECEIPT -> setOf(AccountType.INVENTORY, AccountType.ACCOUNTS_PAYABLE)
            INVENTORY_ISSUE -> setOf(AccountType.COST_OF_GOODS_SOLD, AccountType.INVENTORY)
            DEPRECIATION -> setOf(AccountType.DEPRECIATION_EXPENSE, AccountType.ACCUMULATED_DEPRECIATION)
            else -> emptySet()
        }
    }
}

/**
 * High-level transaction categorization for reporting and analysis
 */
enum class TransactionCategory(val displayName: String) {
    GENERAL_LEDGER("General Ledger"),
    ACCOUNTS_RECEIVABLE("Accounts Receivable"),
    ACCOUNTS_PAYABLE("Accounts Payable"),
    INVENTORY("Inventory Management"),
    SALES("Sales Operations"),
    PROCUREMENT("Procurement"),
    MANUFACTURING("Manufacturing"),
    FIXED_ASSETS("Fixed Assets"),
    PAYROLL("Payroll"),
    BANKING("Banking"),
    FOREIGN_EXCHANGE("Foreign Exchange"),
    TAX("Tax Management"),
    INTERCOMPANY("Intercompany"),
    REGULATORY("Regulatory Compliance")
}

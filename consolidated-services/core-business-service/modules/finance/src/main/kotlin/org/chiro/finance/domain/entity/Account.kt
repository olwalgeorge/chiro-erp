package org.chiro.finance.domain.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.chiro.finance.domain.valueobject.AccountType
import org.chiro.finance.domain.valueobject.Money
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import java.time.LocalDateTime
import java.util.*

/**
 * Account Entity - Chart of Accounts Aggregate Root
 * 
 * Represents a financial account in the chart of accounts following accounting principles:
 * - Assets (what the company owns)
 * - Liabilities (what the company owes)
 * - Equity (owner's interest)
 * - Revenue (income from operations)
 * - Expenses (costs of operations)
 * 
 * Features:
 * - Hierarchical account structure with parent-child relationships
 * - Real-time balance calculation with currency support
 * - Account status management (active/inactive/closed)
 * - Audit trail with creation and modification tracking
 * - Account code validation following accounting standards
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Entity
@Table(
    name = "accounts", 
    schema = "finance",
    indexes = [
        Index(name = "idx_account_code", columnList = "account_code", unique = true),
        Index(name = "idx_account_type", columnList = "account_type"),
        Index(name = "idx_account_parent", columnList = "parent_account_id"),
        Index(name = "idx_account_status", columnList = "account_status"),
        Index(name = "idx_account_name", columnList = "account_name")
    ]
)
class Account : PanacheEntityBase {
    
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID()
    
    @field:NotBlank(message = "Account code is required")
    @field:Size(min = 3, max = 20, message = "Account code must be between 3 and 20 characters")
    @Column(name = "account_code", unique = true, nullable = false, length = 20)
    lateinit var accountCode: String
    
    @field:NotBlank(message = "Account name is required")
    @field:Size(min = 2, max = 255, message = "Account name must be between 2 and 255 characters")
    @Column(name = "account_name", nullable = false)
    lateinit var accountName: String
    
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description", length = 1000)
    var description: String? = null
    
    @field:NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    lateinit var accountType: AccountType
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "balance_amount", precision = 19, scale = 4)),
        AttributeOverride(name = "currencyCode", column = Column(name = "balance_currency", length = 3))
    )
    var balance: Money = Money.zero()
    
    // Parent-Child Relationship for Account Hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    var parentAccount: Account? = null
    
    @OneToMany(mappedBy = "parentAccount", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var childAccounts: MutableSet<Account> = mutableSetOf()
    
    @field:NotNull(message = "Account status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    var accountStatus: AccountStatus = AccountStatus.ACTIVE
    
    // Audit Fields
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
    
    @field:Size(max = 100, message = "Created by cannot exceed 100 characters")
    @Column(name = "created_by", length = 100)
    var createdBy: String? = null
    
    @field:Size(max = 100, message = "Updated by cannot exceed 100 characters")
    @Column(name = "updated_by", length = 100)
    var updatedBy: String? = null
    
    // Account Configuration
    @Column(name = "is_system_account", nullable = false)
    var isSystemAccount: Boolean = false
    
    @Column(name = "allow_manual_entries", nullable = false)
    var allowManualEntries: Boolean = true
    
    @Column(name = "require_reconciliation", nullable = false)
    var requireReconciliation: Boolean = false
    
    // ===================== BUSINESS LOGIC =====================
    
    /**
     * Updates account balance with proper currency validation
     */
    fun updateBalance(newBalance: Money) {
        require(newBalance.currencyCode == balance.currencyCode) {
            "Currency mismatch: Account balance is in ${balance.currencyCode}, but trying to update with ${newBalance.currencyCode}"
        }
        balance = newBalance
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Adds amount to current balance
     */
    fun addToBalance(amount: Money) {
        balance += amount
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Subtracts amount from current balance
     */
    fun subtractFromBalance(amount: Money) {
        balance -= amount
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Checks if account allows debit entries based on account type
     */
    val allowsDebit: Boolean
        get() = when (accountType) {
            AccountType.ASSET, AccountType.EXPENSE -> true
            AccountType.LIABILITY, AccountType.EQUITY, AccountType.REVENUE -> false
        }
    
    /**
     * Checks if account allows credit entries based on account type
     */
    val allowsCredit: Boolean
        get() = when (accountType) {
            AccountType.LIABILITY, AccountType.EQUITY, AccountType.REVENUE -> true
            AccountType.ASSET, AccountType.EXPENSE -> false
        }
    
    /**
     * Checks if account is a root account (no parent)
     */
    val isRootAccount: Boolean get() = parentAccount == null
    
    /**
     * Checks if account is a leaf account (no children)
     */
    val isLeafAccount: Boolean get() = childAccounts.isEmpty()
    
    /**
     * Gets the full account hierarchy path
     */
    val hierarchyPath: String
        get() {
            val path = mutableListOf<String>()
            var currentAccount: Account? = this
            
            while (currentAccount != null) {
                path.add(0, currentAccount.accountCode)
                currentAccount = currentAccount.parentAccount
            }
            
            return path.joinToString(" > ")
        }
    
    /**
     * Gets the account level in the hierarchy (root = 0)
     */
    val hierarchyLevel: Int
        get() {
            var level = 0
            var currentAccount = parentAccount
            
            while (currentAccount != null) {
                level++
                currentAccount = currentAccount.parentAccount
            }
            
            return level
        }
    
    /**
     * Activates the account
     */
    fun activate() {
        require(accountStatus != AccountStatus.CLOSED) { "Cannot activate a closed account" }
        accountStatus = AccountStatus.ACTIVE
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Deactivates the account
     */
    fun deactivate() {
        require(accountStatus != AccountStatus.CLOSED) { "Cannot deactivate a closed account" }
        require(balance.isZero) { "Cannot deactivate account with non-zero balance: $balance" }
        accountStatus = AccountStatus.INACTIVE
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Closes the account permanently
     */
    fun close() {
        require(balance.isZero) { "Cannot close account with non-zero balance: $balance" }
        require(childAccounts.isEmpty()) { "Cannot close account with child accounts" }
        accountStatus = AccountStatus.CLOSED
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Adds a child account to this account
     */
    fun addChildAccount(childAccount: Account) {
        require(childAccount.accountType == this.accountType) {
            "Child account type (${childAccount.accountType}) must match parent account type ($accountType)"
        }
        require(childAccount.balance.currencyCode == this.balance.currencyCode) {
            "Child account currency (${childAccount.balance.currencyCode}) must match parent currency (${balance.currencyCode})"
        }
        
        childAccount.parentAccount = this
        childAccounts.add(childAccount)
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Validates account code format based on accounting standards
     */
    fun validateAccountCode(): Boolean {
        val codePattern = when (accountType) {
            AccountType.ASSET -> "^[1]\\d{2,19}$"           // 1000-1999
            AccountType.LIABILITY -> "^[2]\\d{2,19}$"       // 2000-2999
            AccountType.EQUITY -> "^[3]\\d{2,19}$"          // 3000-3999
            AccountType.REVENUE -> "^[4]\\d{2,19}$"         // 4000-4999
            AccountType.EXPENSE -> "^[5-9]\\d{2,19}$"       // 5000-9999
        }
        
        return accountCode.matches(Regex(codePattern))
    }
    
    /**
     * Calculates total balance including all child accounts
     */
    fun calculateTotalBalance(): Money {
        var total = balance
        
        for (child in childAccounts) {
            total += child.calculateTotalBalance()
        }
        
        return total
    }
    
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
    
    override fun toString(): String = "Account(code='$accountCode', name='$accountName', type=$accountType, balance=$balance)"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false
        return id == other.id
    }
    
    override fun hashCode(): Int = id.hashCode()
}

/**
 * Account Status Enumeration
 */
enum class AccountStatus {
    ACTIVE,     // Account is active and can be used
    INACTIVE,   // Account is temporarily inactive
    CLOSED      // Account is permanently closed
}

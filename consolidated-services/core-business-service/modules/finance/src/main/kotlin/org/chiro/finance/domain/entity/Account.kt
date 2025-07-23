package org.chiro.finance.domain.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.chiro.finance.domain.valueobject.*
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import java.time.LocalDateTime
import java.util.*

/**
 * Account Entity - Chart of Accounts Aggregate Root
 * 
 * World-class enterprise account implementation following Domain-Driven Design
 * and international accounting standards (IFRS/GAAP). Supports complex ERP
 * operations including multi-currency, hierarchical structures, and comprehensive
 * business rule enforcement.
 * 
 * Features:
 * - Strongly typed identifiers and value objects
 * - Hierarchical account structure with unlimited levels
 * - Multi-currency support with automatic validation
 * - Account status lifecycle management
 * - Comprehensive business rule validation
 * - Audit trail with complete history tracking
 * - Control account and subsidiary ledger support
 * - Account code validation following accounting standards
 * - Integration points for ERP modules
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
        Index(name = "idx_account_name", columnList = "account_name"),
        Index(name = "idx_account_currency", columnList = "currency_code"),
        Index(name = "idx_account_category", columnList = "account_category"),
        Index(name = "idx_account_control", columnList = "is_control_account"),
        Index(name = "idx_account_active", columnList = "is_active")
    ]
)
class Account : PanacheEntityBase {
    
    @Id
    @Column(name = "id")
    val id: AccountId = AccountId.generate()
    
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
    @Column(name = "account_type", nullable = false, length = 30)
    lateinit var accountType: AccountType
    
    @field:NotNull(message = "Account category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_category", nullable = false, length = 20)
    var accountCategory: AccountCategory = AccountCategory.ASSET
        private set
    
    @field:NotNull(message = "Account sub-category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_sub_category", nullable = false, length = 30)
    var accountSubCategory: AccountSubCategory = AccountSubCategory.CURRENT_ASSET
        private set
    
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "balance_amount", precision = 19, scale = 4)),
        AttributeOverride(name = "currencyCode", column = Column(name = "currency_code", length = 3))
    )
    var balance: Money = Money.zero()
        private set
    
    // Enhanced Currency Support
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "code", column = Column(name = "currency_code")),
        AttributeOverride(name = "name", column = Column(name = "currency_name")),
        AttributeOverride(name = "symbol", column = Column(name = "currency_symbol")),
        AttributeOverride(name = "decimalPlaces", column = Column(name = "currency_decimal_places"))
    )
    var currency: Currency = Currency.DEFAULT
        private set
    
    // Hierarchical Structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    var parentAccount: Account? = null
        private set
    
    @OneToMany(mappedBy = "parentAccount", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var childAccounts: MutableSet<Account> = mutableSetOf()
        private set
    
    // Account Configuration
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
        private set
    
    @Column(name = "is_control_account", nullable = false)
    var isControlAccount: Boolean = false
        private set
    
    @Column(name = "allows_direct_posting", nullable = false)
    var allowsDirectPosting: Boolean = true
        private set
    
    @Column(name = "requires_subsidiary", nullable = false)
    var requiresSubsidiary: Boolean = false
        private set
    
    @Column(name = "requires_reconciliation", nullable = false)
    var requiresReconciliation: Boolean = false
    
    @Column(name = "allow_manual_entries", nullable = false)
    var allowManualEntries: Boolean = true
    
    @Column(name = "is_system_account", nullable = false)
    var isSystemAccount: Boolean = false
    
    // Tax and Regulatory
    @Column(name = "tax_code", length = 10)
    var taxCode: String? = null
    
    @Column(name = "external_account_id", length = 50)
    var externalAccountId: String? = null
    
    // Audit Fields
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
        private set
    
    @field:Size(max = 100, message = "Created by cannot exceed 100 characters")
    @Column(name = "created_by", length = 100)
    var createdBy: String? = null
    
    @field:Size(max = 100, message = "Updated by cannot exceed 100 characters")
    @Column(name = "updated_by", length = 100)
    var updatedBy: String? = null
        private set
    
    // Version for optimistic locking
    @Version
    @Column(name = "version")
    var version: Long = 0
        private set
    
    
    // ===================== DOMAIN BUSINESS LOGIC =====================
    
    /**
     * Factory method to create a new account with validation
     */
    companion object {
        fun create(
            accountCode: String,
            accountName: String,
            accountType: AccountType,
            currency: Currency = Currency.DEFAULT,
            description: String? = null,
            parentAccount: Account? = null,
            createdBy: String? = null
        ): Account {
            val account = Account().apply {
                this.accountCode = accountCode
                this.accountName = accountName
                this.accountType = accountType
                this.accountCategory = accountType.category
                this.accountSubCategory = accountType.subCategory
                this.currency = currency
                this.balance = Money.zero(currency.code)
                this.description = description
                this.createdBy = createdBy
                this.isControlAccount = accountType.isControlAccount
                this.allowsDirectPosting = accountType.allowsDirectPosting
                this.requiresSubsidiary = accountType.requiresSubsidiary
            }
            
            // Set parent relationship if provided
            parentAccount?.let { account.setParentAccount(it) }
            
            // Validate the account
            account.validateAccount()
            
            return account
        }
    }
    
    /**
     * Comprehensive account validation
     */
    private fun validateAccount() {
        require(accountCode.isNotBlank()) { "Account code cannot be blank" }
        require(accountName.isNotBlank()) { "Account name cannot be blank" }
        require(accountType.isValidAccountCode(accountCode)) { 
            "Account code '$accountCode' is not valid for account type $accountType" 
        }
        require(balance.currencyCode == currency.code) {
            "Balance currency (${balance.currencyCode}) must match account currency (${currency.code})"
        }
        
        // Validate parent-child relationship
        parentAccount?.let { parent ->
            require(parent.accountCategory == this.accountCategory) {
                "Child account category (${this.accountCategory}) must match parent category (${parent.accountCategory})"
            }
            require(parent.currency.code == this.currency.code) {
                "Child account currency (${this.currency.code}) must match parent currency (${parent.currency.code})"
            }
            require(parent.id != this.id) { "Account cannot be its own parent" }
        }
    }
    
    /**
     * Updates account balance with comprehensive validation
     */
    fun updateBalance(newBalance: Money, updatedBy: String? = null) {
        require(newBalance.currencyCode == currency.code) {
            "Currency mismatch: Account uses ${currency.code}, but trying to update with ${newBalance.currencyCode}"
        }
        require(isActive) { "Cannot update balance on inactive account" }
        require(allowsDirectPosting) { "Direct posting not allowed on this account" }
        
        balance = newBalance
        this.updatedBy = updatedBy
        markAsUpdated()
    }
    
    /**
     * Adds amount to current balance with validation
     */
    fun addToBalance(amount: Money, updatedBy: String? = null) {
        require(amount.currencyCode == currency.code) {
            "Currency mismatch: Account uses ${currency.code}, but trying to add ${amount.currencyCode}"
        }
        require(isActive) { "Cannot modify balance on inactive account" }
        
        balance += amount
        this.updatedBy = updatedBy
        markAsUpdated()
    }
    
    /**
     * Subtracts amount from current balance with validation
     */
    fun subtractFromBalance(amount: Money, updatedBy: String? = null) {
        require(amount.currencyCode == currency.code) {
            "Currency mismatch: Account uses ${currency.code}, but trying to subtract ${amount.currencyCode}"
        }
        require(isActive) { "Cannot modify balance on inactive account" }
        
        balance -= amount
        this.updatedBy = updatedBy
        markAsUpdated()
    }
    
    /**
     * Checks if account allows debit entries based on account type
     */
    val allowsDebit: Boolean
        get() = accountType.normalBalance == BalanceType.DEBIT
    
    /**
     * Checks if account allows credit entries based on account type
     */
    val allowsCredit: Boolean
        get() = accountType.normalBalance == BalanceType.CREDIT
    
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
     * Sets parent account with validation
     */
    fun setParentAccount(parent: Account, updatedBy: String? = null) {
        require(parent.id != this.id) { "Account cannot be its own parent" }
        require(parent.accountCategory == this.accountCategory) {
            "Parent account category (${parent.accountCategory}) must match child category (${this.accountCategory})"
        }
        require(parent.currency.code == this.currency.code) {
            "Parent account currency (${parent.currency.code}) must match child currency (${this.currency.code})"
        }
        require(!wouldCreateCycle(parent)) { "Setting parent would create a circular hierarchy" }
        
        // Remove from old parent
        this.parentAccount?.childAccounts?.remove(this)
        
        // Set new parent
        this.parentAccount = parent
        parent.childAccounts.add(this)
        
        this.updatedBy = updatedBy
        markAsUpdated()
    }
    
    /**
     * Checks if setting a parent would create a circular reference
     */
    private fun wouldCreateCycle(potentialParent: Account): Boolean {
        var current: Account? = potentialParent
        while (current != null) {
            if (current.id == this.id) return true
            current = current.parentAccount
        }
        return false
    }
    
    /**
     * Removes parent account relationship
     */
    fun removeParentAccount(updatedBy: String? = null) {
        parentAccount?.childAccounts?.remove(this)
        parentAccount = null
        this.updatedBy = updatedBy
        markAsUpdated()
    }
    
    /**
     * Adds a child account with validation
     */
    fun addChildAccount(childAccount: Account, updatedBy: String? = null) {
        childAccount.setParentAccount(this, updatedBy)
    }
    
    /**
     * Removes a child account
     */
    fun removeChildAccount(childAccount: Account, updatedBy: String? = null) {
        require(childAccounts.contains(childAccount)) { "Child account not found" }
        childAccount.removeParentAccount(updatedBy)
    }
    
    /**
     * Activates the account
     */
    fun activate(activatedBy: String? = null) {
        require(!isSystemAccount || activatedBy != null) { "System accounts require user authorization" }
        
        isActive = true
        this.updatedBy = activatedBy
        markAsUpdated()
    }
    
    /**
     * Deactivates the account
     */
    fun deactivate(deactivatedBy: String? = null) {
        require(balance.isZero) { "Cannot deactivate account with non-zero balance: $balance" }
        require(childAccounts.all { !it.isActive }) { "Cannot deactivate account with active child accounts" }
        require(!isSystemAccount || deactivatedBy != null) { "System accounts require user authorization" }
        
        isActive = false
        this.updatedBy = deactivatedBy
        markAsUpdated()
    }
    
    /**
     * Marks account as control account (requires subsidiary ledger)
     */
    fun markAsControlAccount(markedBy: String? = null) {
        require(isLeafAccount) { "Only leaf accounts can be marked as control accounts" }
        
        isControlAccount = true
        allowsDirectPosting = false
        requiresSubsidiary = true
        this.updatedBy = markedBy
        markAsUpdated()
    }
    
    /**
     * Removes control account designation
     */
    fun removeControlAccountDesignation(removedBy: String? = null) {
        require(balance.isZero) { "Cannot remove control account designation with non-zero balance" }
        
        isControlAccount = false
        allowsDirectPosting = true
        requiresSubsidiary = false
        this.updatedBy = removedBy
        markAsUpdated()
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
    
    /**
     * Gets all descendant accounts (children, grandchildren, etc.)
     */
    fun getAllDescendants(): Set<Account> {
        val descendants = mutableSetOf<Account>()
        
        fun collectDescendants(account: Account) {
            for (child in account.childAccounts) {
                descendants.add(child)
                collectDescendants(child)
            }
        }
        
        collectDescendants(this)
        return descendants
    }
    
    /**
     * Gets the root account of the hierarchy
     */
    fun getRootAccount(): Account {
        var current = this
        while (current.parentAccount != null) {
            current = current.parentAccount!!
        }
        return current
    }
    
    /**
     * Checks if this account is an ancestor of the given account
     */
    fun isAncestorOf(account: Account): Boolean {
        var current: Account? = account.parentAccount
        while (current != null) {
            if (current.id == this.id) return true
            current = current.parentAccount
        }
        return false
    }
    
    /**
     * Checks if this account is a descendant of the given account
     */
    fun isDescendantOf(account: Account): Boolean = account.isAncestorOf(this)
    
    /**
     * Updates the currency of the account (requires zero balance)
     */
    fun updateCurrency(newCurrency: Currency, updatedBy: String? = null) {
        require(balance.isZero) { "Cannot change currency with non-zero balance" }
        require(childAccounts.all { it.balance.isZero }) { "Cannot change currency with child accounts having non-zero balances" }
        
        currency = newCurrency
        balance = Money.zero(newCurrency.code)
        this.updatedBy = updatedBy
        markAsUpdated()
        
        // Update all child accounts
        childAccounts.forEach { it.updateCurrency(newCurrency, updatedBy) }
    }
    
    /**
     * Validates account business rules
     */
    fun validateBusinessRules(): List<String> {
        val violations = mutableListOf<String>()
        
        if (isControlAccount && allowsDirectPosting) {
            violations.add("Control accounts should not allow direct posting")
        }
        
        if (requiresSubsidiary && !isControlAccount) {
            violations.add("Only control accounts should require subsidiary ledgers")
        }
        
        if (!isActive && !balance.isZero) {
            violations.add("Inactive accounts should have zero balance")
        }
        
        if (parentAccount != null && parentAccount!!.accountCategory != accountCategory) {
            violations.add("Parent and child accounts must have the same category")
        }
        
        if (parentAccount != null && parentAccount!!.currency.code != currency.code) {
            violations.add("Parent and child accounts must use the same currency")
        }
        
        return violations
    }
    
    /**
     * Helper method to mark the entity as updated
     */
    private fun markAsUpdated() {
        updatedAt = LocalDateTime.now()
    }
    
    @PreUpdate
    fun preUpdate() {
        markAsUpdated()
    }
    
    override fun toString(): String = 
        "Account(id=${id.value}, code='$accountCode', name='$accountName', type=$accountType, balance=$balance, currency=${currency.code})"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false
        return id == other.id
    }
    
    override fun hashCode(): Int = id.hashCode()
}

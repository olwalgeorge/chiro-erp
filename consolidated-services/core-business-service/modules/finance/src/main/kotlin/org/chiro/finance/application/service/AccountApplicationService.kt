package org.chiro.finance.application.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.chiro.finance.domain.entity.Account
import org.chiro.finance.domain.entity.AccountStatus
import org.chiro.finance.domain.valueobject.AccountType
import org.chiro.finance.domain.valueobject.Money
import org.chiro.finance.application.dto.AccountDto
import org.chiro.finance.application.command.CreateAccountCommand
import org.chiro.finance.application.command.UpdateAccountCommand
import java.time.LocalDateTime
import java.util.*

/**
 * Account Application Service - Orchestrates account operations
 * 
 * Provides high-level business operations for account management following
 * Clean Architecture and DDD principles:
 * - Commands and queries separation
 * - Transaction management
 * - Domain validation enforcement
 * - DTO mapping
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@ApplicationScoped
class AccountApplicationService {
    
    /**
     * Creates a new account with validation
     */
    @Transactional
    suspend fun createAccount(command: CreateAccountCommand): AccountDto {
        // Validate account code uniqueness
        val existingAccount = Account.find("accountCode", command.accountCode).firstResult<Account>()
        require(existingAccount == null) { "Account code '${command.accountCode}' already exists" }
        
        // Create new account
        val account = Account().apply {
            accountCode = command.accountCode
            accountName = command.accountName
            description = command.description
            accountType = command.accountType
            balance = Money.zero(command.currency)
            parentAccount = command.parentAccountId?.let { parentId ->
                Account.findById<Account>(parentId) ?: throw IllegalArgumentException("Parent account not found: $parentId")
            }
            isSystemAccount = command.isSystemAccount
            allowManualEntries = command.allowManualEntries
            requireReconciliation = command.requireReconciliation
            createdBy = command.createdBy
        }
        
        // Validate account code format
        require(account.validateAccountCode()) {
            "Invalid account code format for ${account.accountType}: ${account.accountCode}"
        }
        
        // Validate parent account compatibility
        account.parentAccount?.let { parent ->
            require(parent.accountType == account.accountType) {
                "Parent account type (${parent.accountType}) must match child account type (${account.accountType})"
            }
        }
        
        // Persist the account
        account.persist<Account>()
        
        return mapToDto(account)
    }
    
    /**
     * Updates an existing account
     */
    @Transactional
    suspend fun updateAccount(accountId: UUID, command: UpdateAccountCommand): AccountDto {
        val account = Account.findById<Account>(accountId) 
            ?: throw IllegalArgumentException("Account not found: $accountId")
        
        require(account.accountStatus != AccountStatus.CLOSED) {
            "Cannot update closed account"
        }
        
        // Update fields
        command.accountName?.let { account.accountName = it }
        command.description?.let { account.description = it }
        command.allowManualEntries?.let { account.allowManualEntries = it }
        command.requireReconciliation?.let { account.requireReconciliation = it }
        command.updatedBy?.let { account.updatedBy = it }
        
        account.updatedAt = LocalDateTime.now()
        
        return mapToDto(account)
    }
    
    /**
     * Activates an account
     */
    @Transactional
    suspend fun activateAccount(accountId: UUID, activatedBy: String): AccountDto {
        val account = Account.findById<Account>(accountId)
            ?: throw IllegalArgumentException("Account not found: $accountId")
        
        account.activate()
        account.updatedBy = activatedBy
        
        return mapToDto(account)
    }
    
    /**
     * Deactivates an account
     */
    @Transactional
    suspend fun deactivateAccount(accountId: UUID, deactivatedBy: String): AccountDto {
        val account = Account.findById<Account>(accountId)
            ?: throw IllegalArgumentException("Account not found: $accountId")
        
        account.deactivate()
        account.updatedBy = deactivatedBy
        
        return mapToDto(account)
    }
    
    /**
     * Closes an account permanently
     */
    @Transactional
    suspend fun closeAccount(accountId: UUID, closedBy: String): AccountDto {
        val account = Account.findById<Account>(accountId)
            ?: throw IllegalArgumentException("Account not found: $accountId")
        
        account.close()
        account.updatedBy = closedBy
        
        return mapToDto(account)
    }
    
    /**
     * Gets account by ID
     */
    suspend fun getAccount(accountId: UUID): AccountDto? {
        val account = Account.findById<Account>(accountId) ?: return null
        return mapToDto(account)
    }
    
    /**
     * Gets account by code
     */
    suspend fun getAccountByCode(accountCode: String): AccountDto? {
        val account = Account.find("accountCode", accountCode).firstResult<Account>() ?: return null
        return mapToDto(account)
    }
    
    /**
     * Lists all accounts with optional filtering
     */
    suspend fun listAccounts(
        accountType: AccountType? = null,
        status: AccountStatus? = null,
        parentAccountId: UUID? = null,
        includeInactive: Boolean = false
    ): List<AccountDto> {
        val query = buildString {
            append("1=1")
            
            if (accountType != null) {
                append(" AND accountType = :accountType")
            }
            
            if (status != null) {
                append(" AND accountStatus = :status")
            } else if (!includeInactive) {
                append(" AND accountStatus = :activeStatus")
            }
            
            if (parentAccountId != null) {
                append(" AND parentAccount.id = :parentAccountId")
            }
            
            append(" ORDER BY accountCode")
        }
        
        val panacheQuery = Account.find(query)
        
        accountType?.let { panacheQuery.withParameter("accountType", it) }
        status?.let { panacheQuery.withParameter("status", it) }
        if (status == null && !includeInactive) {
            panacheQuery.withParameter("activeStatus", AccountStatus.ACTIVE)
        }
        parentAccountId?.let { panacheQuery.withParameter("parentAccountId", it) }
        
        val accounts = panacheQuery.list<Account>()
        return accounts.map { mapToDto(it) }
    }
    
    /**
     * Gets the chart of accounts as a hierarchical structure
     */
    suspend fun getChartOfAccounts(): List<AccountDto> {
        val allAccounts = Account.find("ORDER BY accountCode").list<Account>()
        return allAccounts.map { mapToDto(it) }
    }
    
    /**
     * Gets account balance including child accounts
     */
    suspend fun getAccountTotalBalance(accountId: UUID): Money? {
        val account = Account.findById<Account>(accountId) ?: return null
        return account.calculateTotalBalance()
    }
    
    /**
     * Validates account code availability
     */
    suspend fun isAccountCodeAvailable(accountCode: String): Boolean {
        return Account.find("accountCode", accountCode).firstResult<Account>() == null
    }
    
    /**
     * Maps Account entity to DTO
     */
    private fun mapToDto(account: Account): AccountDto {
        return AccountDto(
            id = account.id,
            accountCode = account.accountCode,
            accountName = account.accountName,
            description = account.description,
            accountType = account.accountType,
            balance = account.balance,
            parentAccountId = account.parentAccount?.id,
            parentAccountCode = account.parentAccount?.accountCode,
            accountStatus = account.accountStatus,
            isSystemAccount = account.isSystemAccount,
            allowManualEntries = account.allowManualEntries,
            requireReconciliation = account.requireReconciliation,
            hierarchyPath = account.hierarchyPath,
            hierarchyLevel = account.hierarchyLevel,
            isRootAccount = account.isRootAccount,
            isLeafAccount = account.isLeafAccount,
            childAccountCount = account.childAccounts.size,
            totalBalance = account.calculateTotalBalance(),
            createdAt = account.createdAt,
            updatedAt = account.updatedAt,
            createdBy = account.createdBy,
            updatedBy = account.updatedBy
        )
    }
}

package org.chiro.finance.application.command

import org.chiro.finance.domain.valueobject.AccountType
import java.util.*

/**
 * Create Account Command
 * 
 * Command for creating a new account in the Chart of Accounts
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
data class CreateAccountCommand(
    val accountCode: String,
    val accountName: String,
    val description: String?,
    val accountType: AccountType,
    val currency: String,
    val parentAccountId: UUID?,
    val isSystemAccount: Boolean = false,
    val allowManualEntries: Boolean = true,
    val requireReconciliation: Boolean = false,
    val createdBy: String
)

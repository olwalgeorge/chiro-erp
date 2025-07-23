package org.chiro.finance.application.command

import java.util.*

/**
 * Update Account Command
 * 
 * Command for updating an existing account
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
data class UpdateAccountCommand(
    val accountName: String?,
    val description: String?,
    val allowManualEntries: Boolean?,
    val requireReconciliation: Boolean?,
    val updatedBy: String?
)

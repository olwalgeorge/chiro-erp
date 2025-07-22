package org.chiro.finance.application.command

import org.chiro.finance.domain.valueobject.AccountType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.*

/**
 * Create Account Command
 * 
 * Command object for creating new accounts with validation
 */
data class CreateAccountCommand(
    @field:NotBlank(message = "Account code is required")
    @field:Size(min = 3, max = 20, message = "Account code must be between 3 and 20 characters")
    val accountCode: String,
    
    @field:NotBlank(message = "Account name is required")
    @field:Size(min = 2, max = 255, message = "Account name must be between 2 and 255 characters")
    val accountName: String,
    
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,
    
    @field:NotNull(message = "Account type is required")
    val accountType: AccountType,
    
    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    val currency: String = "USD",
    
    val parentAccountId: UUID? = null,
    
    val isSystemAccount: Boolean = false,
    
    val allowManualEntries: Boolean = true,
    
    val requireReconciliation: Boolean = false,
    
    @field:Size(max = 100, message = "Created by cannot exceed 100 characters")
    val createdBy: String? = null
)

/**
 * Update Account Command
 * 
 * Command object for updating existing accounts
 */
data class UpdateAccountCommand(
    @field:Size(min = 2, max = 255, message = "Account name must be between 2 and 255 characters")
    val accountName: String? = null,
    
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,
    
    val allowManualEntries: Boolean? = null,
    
    val requireReconciliation: Boolean? = null,
    
    @field:Size(max = 100, message = "Updated by cannot exceed 100 characters")
    val updatedBy: String? = null
)

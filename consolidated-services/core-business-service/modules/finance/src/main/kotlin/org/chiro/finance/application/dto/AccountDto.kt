package org.chiro.finance.application.dto

import org.chiro.finance.domain.entity.AccountStatus
import org.chiro.finance.domain.valueobject.AccountType
import org.chiro.finance.domain.valueobject.Money
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.*

/**
 * Account Data Transfer Object
 * 
 * External representation of Account entity for API responses
 * with additional computed fields for UI convenience
 */
data class AccountDto(
    @JsonProperty("id")
    val id: UUID,
    
    @JsonProperty("accountCode")
    val accountCode: String,
    
    @JsonProperty("accountName")
    val accountName: String,
    
    @JsonProperty("description")
    val description: String?,
    
    @JsonProperty("accountType")
    val accountType: AccountType,
    
    @JsonProperty("balance")
    val balance: Money,
    
    @JsonProperty("parentAccountId")
    val parentAccountId: UUID?,
    
    @JsonProperty("parentAccountCode")
    val parentAccountCode: String?,
    
    @JsonProperty("accountStatus")
    val accountStatus: AccountStatus,
    
    @JsonProperty("isSystemAccount")
    val isSystemAccount: Boolean,
    
    @JsonProperty("allowManualEntries")
    val allowManualEntries: Boolean,
    
    @JsonProperty("requireReconciliation")
    val requireReconciliation: Boolean,
    
    @JsonProperty("hierarchyPath")
    val hierarchyPath: String,
    
    @JsonProperty("hierarchyLevel")
    val hierarchyLevel: Int,
    
    @JsonProperty("isRootAccount")
    val isRootAccount: Boolean,
    
    @JsonProperty("isLeafAccount")
    val isLeafAccount: Boolean,
    
    @JsonProperty("childAccountCount")
    val childAccountCount: Int,
    
    @JsonProperty("totalBalance")
    val totalBalance: Money,
    
    @JsonProperty("createdAt")
    val createdAt: LocalDateTime,
    
    @JsonProperty("updatedAt")
    val updatedAt: LocalDateTime?,
    
    @JsonProperty("createdBy")
    val createdBy: String?,
    
    @JsonProperty("updatedBy")
    val updatedBy: String?
)

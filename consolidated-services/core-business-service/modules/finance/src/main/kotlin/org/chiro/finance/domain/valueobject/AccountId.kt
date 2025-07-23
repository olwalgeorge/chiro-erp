package org.chiro.finance.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.validation.constraints.NotNull
import java.util.*

/**
 * Account Identifier - Strongly Typed UUID for Account references
 * 
 * Provides type safety and prevents accidental mixing of different entity IDs.
 * Follows DDD principles for value objects with immutability and validation.
 * 
 * Features:
 * - Type safety (cannot accidentally use CustomerId where AccountId is expected)
 * - Immutable design
 * - Efficient UUID generation and comparison
 * - JPA Embeddable for database mapping
 * - JSON serialization support
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Embeddable
@JvmInline
value class AccountId(
    @field:NotNull
    @Column(name = "account_id", columnDefinition = "UUID")
    val value: UUID
) {
    companion object {
        /**
         * Generates a new random AccountId
         */
        fun generate(): AccountId = AccountId(UUID.randomUUID())
        
        /**
         * Creates AccountId from string representation
         */
        fun fromString(value: String): AccountId = AccountId(UUID.fromString(value))
        
        /**
         * Creates AccountId from existing UUID
         */
        fun from(uuid: UUID): AccountId = AccountId(uuid)
    }
    
    init {
        require(value.toString().isNotBlank()) { "AccountId cannot be blank" }
    }
    
    /**
     * String representation for logging and debugging
     */
    override fun toString(): String = value.toString()
    
    /**
     * Returns the underlying UUID value
     */
    fun toUUID(): UUID = value
}

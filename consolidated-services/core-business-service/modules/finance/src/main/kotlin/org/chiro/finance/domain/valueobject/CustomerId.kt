package org.chiro.finance.domain.valueobject

import java.util.*

/**
 * Customer Identifier - Strongly Typed UUID for Customer references
 * 
 * Links finance module to customer management while maintaining loose coupling.
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@JvmInline
value class CustomerId(val value: UUID) {
    companion object {
        fun generate(): CustomerId = CustomerId(UUID.randomUUID())
        fun fromString(value: String): CustomerId = CustomerId(UUID.fromString(value))
        fun from(uuid: UUID): CustomerId = CustomerId(uuid)
    }
    
    init {
        require(value.toString().isNotBlank()) { "CustomerId cannot be blank" }
    }
    
    override fun toString(): String = value.toString()
    fun toUUID(): UUID = value
}

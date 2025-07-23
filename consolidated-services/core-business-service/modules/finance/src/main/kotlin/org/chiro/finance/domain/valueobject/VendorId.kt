package org.chiro.finance.domain.valueobject

import java.util.*

/**
 * Vendor Identifier - Strongly Typed UUID for Vendor/Supplier references
 * 
 * Links finance module to vendor management for accounts payable operations.
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@JvmInline
value class VendorId(val value: UUID) {
    companion object {
        fun generate(): VendorId = VendorId(UUID.randomUUID())
        fun fromString(value: String): VendorId = VendorId(UUID.fromString(value))
        fun from(uuid: UUID): VendorId = VendorId(uuid)
    }
    
    init {
        require(value.toString().isNotBlank()) { "VendorId cannot be blank" }
    }
    
    override fun toString(): String = value.toString()
    fun toUUID(): UUID = value
}

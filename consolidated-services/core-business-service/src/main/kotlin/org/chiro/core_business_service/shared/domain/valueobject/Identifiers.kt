package org.chiro.core_business_service.shared.domain.valueobject

import java.util.*

/**
 * Base interface for all aggregate identifiers
 */
interface AggregateId {
    val value: String
}

/**
 * Base interface for all entity identifiers
 */
interface EntityId {
    val value: String
}

/**
 * Base implementation for UUID-based aggregate identifiers
 */
abstract class UUIDBasedAggregateId(
    override val value: String = UUID.randomUUID().toString()
) : AggregateId {
    
    init {
        require(value.isNotBlank()) { "Aggregate ID cannot be blank" }
        require(isValidUUID(value)) { "Aggregate ID must be a valid UUID: $value" }
    }
    
    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as UUIDBasedAggregateId
        
        return value == other.value
    }
    
    override fun hashCode(): Int {
        return value.hashCode()
    }
    
    override fun toString(): String {
        return value
    }
}

/**
 * Base implementation for UUID-based entity identifiers
 */
abstract class UUIDBasedEntityId(
    override val value: String = UUID.randomUUID().toString()
) : EntityId {
    
    init {
        require(value.isNotBlank()) { "Entity ID cannot be blank" }
        require(isValidUUID(value)) { "Entity ID must be a valid UUID: $value" }
    }
    
    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as UUIDBasedEntityId
        
        return value == other.value
    }
    
    override fun hashCode(): Int {
        return value.hashCode()
    }
    
    override fun toString(): String {
        return value
    }
}

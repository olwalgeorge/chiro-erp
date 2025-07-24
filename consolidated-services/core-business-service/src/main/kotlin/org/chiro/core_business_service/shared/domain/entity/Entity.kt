package org.chiro.core_business_service.shared.domain.entity

import org.chiro.core_business_service.shared.domain.valueobject.EntityId
import java.time.LocalDateTime

/**
 * Base class for all domain entities
 * 
 * Entities have identity and lifecycle, but are not aggregate roots
 */
abstract class Entity<ID : EntityId>(
    val id: ID
) {
    
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set
        
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set
        
    /**
     * Update the entity timestamp
     */
    protected fun markAsUpdated() {
        this.updatedAt = LocalDateTime.now()
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as Entity<*>
        
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(id=$id)"
    }
}

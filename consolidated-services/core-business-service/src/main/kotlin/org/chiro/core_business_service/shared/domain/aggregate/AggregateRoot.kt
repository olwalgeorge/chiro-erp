package org.chiro.core_business_service.shared.domain.aggregate

import org.chiro.core_business_service.shared.domain.event.DomainEvent
import org.chiro.core_business_service.shared.domain.valueobject.AggregateId
import java.time.LocalDateTime
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Base class for all aggregate roots in the Core Business Service
 * 
 * Implements the core DDD patterns:
 * - Aggregate Root responsibility
 * - Domain Event management
 * - Optimistic concurrency control
 * - Audit trail support
 */
abstract class AggregateRoot<ID : AggregateId>(
    val id: ID,
    var version: Long = 0L
) {
    
    private val _domainEvents = CopyOnWriteArrayList<DomainEvent>()
    
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set
        
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set
        
    var createdBy: String? = null
        protected set
        
    var updatedBy: String? = null
        protected set
        
    /**
     * Domain events that have been raised but not yet published
     */
    val domainEvents: List<DomainEvent>
        get() = _domainEvents.toList()
    
    /**
     * Raise a domain event to be published later
     */
    protected fun raiseEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }
    
    /**
     * Clear all domain events (typically called after publishing)
     */
    fun clearEvents() {
        _domainEvents.clear()
    }
    
    /**
     * Update the audit trail
     */
    protected fun updateAuditTrail(userId: String) {
        this.updatedAt = LocalDateTime.now()
        this.updatedBy = userId
    }
    
    /**
     * Set creation audit information
     */
    protected fun setCreationAudit(userId: String) {
        val now = LocalDateTime.now()
        this.createdAt = now
        this.updatedAt = now
        this.createdBy = userId
        this.updatedBy = userId
    }
    
    /**
     * Increment version for optimistic locking
     */
    protected fun incrementVersion() {
        this.version++
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as AggregateRoot<*>
        
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(id=$id, version=$version)"
    }
}

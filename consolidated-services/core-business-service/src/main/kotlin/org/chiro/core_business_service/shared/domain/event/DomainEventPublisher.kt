package org.chiro.core_business_service.shared.domain.event

/**
 * Interface for publishing domain events
 */
interface DomainEventPublisher {
    
    /**
     * Publish a single domain event
     */
    suspend fun publish(event: DomainEvent)
    
    /**
     * Publish multiple domain events
     */
    suspend fun publishAll(events: List<DomainEvent>)
}

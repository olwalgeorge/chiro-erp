package org.chiro.core_business_service.shared.domain.event

import java.time.LocalDateTime
import java.util.*

/**
 * Base interface for all domain events
 */
interface DomainEvent {
    val eventId: String
    val aggregateId: String
    val occurredOn: LocalDateTime
    val eventType: String
    val version: Long
}

/**
 * Base implementation for domain events
 */
abstract class BaseDomainEvent(
    override val aggregateId: String,
    override val version: Long = 1L,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: LocalDateTime = LocalDateTime.now()
) : DomainEvent {
    
    override val eventType: String = this::class.simpleName ?: "UnknownEvent"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as BaseDomainEvent
        
        return eventId == other.eventId
    }
    
    override fun hashCode(): Int {
        return eventId.hashCode()
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(eventId=$eventId, aggregateId=$aggregateId, occurredOn=$occurredOn)"
    }
}

/**
 * Marker interface for integration events that cross bounded contexts
 */
interface IntegrationEvent : DomainEvent

/**
 * Base implementation for integration events
 */
abstract class BaseIntegrationEvent(
    aggregateId: String,
    version: Long = 1L,
    eventId: String = UUID.randomUUID().toString(),
    occurredOn: LocalDateTime = LocalDateTime.now()
) : BaseDomainEvent(aggregateId, version, eventId, occurredOn), IntegrationEvent

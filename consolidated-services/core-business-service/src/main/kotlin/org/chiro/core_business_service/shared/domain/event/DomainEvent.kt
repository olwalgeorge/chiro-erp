package org.chiro.core_business_service.shared.domain.event

import java.time.Instant
import java.util.*

/**
 * Base interface for all domain events
 */
interface DomainEvent {
    val eventId: String
    val aggregateId: String
    val aggregateType: String
    val aggregateVersion: Long
    val timestamp: Instant
    val eventType: String
}

/**
 * Base implementation for domain events
 */
abstract class BaseDomainEvent(
    override val aggregateId: String,
    override val aggregateType: String,
    override val aggregateVersion: Long = 1L,
    override val eventId: String = UUID.randomUUID().toString(),
    override val timestamp: Instant = Instant.now()
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
        return "${this::class.simpleName}(eventId=$eventId, aggregateId=$aggregateId, timestamp=$timestamp)"
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
    aggregateType: String,
    aggregateVersion: Long = 1L,
    eventId: String = UUID.randomUUID().toString(),
    timestamp: Instant = Instant.now()
) : BaseDomainEvent(aggregateId, aggregateType, aggregateVersion, eventId, timestamp), IntegrationEvent

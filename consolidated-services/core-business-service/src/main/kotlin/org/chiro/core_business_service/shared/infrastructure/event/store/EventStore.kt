package org.chiro.core_business_service.shared.infrastructure.event.store

import org.chiro.core_business_service.shared.infrastructure.event.EventStoreRecord
import java.time.Instant

/**
 * Event store interface for persisting domain events.
 * 
 * The event store provides a durable log of all domain events that have occurred
 * in the system, enabling event sourcing, audit trails, and replay capabilities.
 */
interface EventStore {

    /**
     * Save an event to the store.
     * 
     * @param event The event to save
     */
    suspend fun save(event: EventStoreRecord)

    /**
     * Retrieve events for a specific aggregate.
     * 
     * @param aggregateId The aggregate identifier
     * @param aggregateType The aggregate type
     * @param fromVersion The minimum version to retrieve (optional)
     * @return List of events for the aggregate
     */
    suspend fun getEventsForAggregate(
        aggregateId: String,
        aggregateType: String,
        fromVersion: Long = 0
    ): List<EventStoreRecord>

    /**
     * Retrieve events by type within a time range.
     * 
     * @param eventType The event type
     * @param fromTime The start time (optional)
     * @param toTime The end time (optional)
     * @return List of events matching the criteria
     */
    suspend fun getEventsByType(
        eventType: String,
        fromTime: Instant? = null,
        toTime: Instant? = null
    ): List<EventStoreRecord>

    /**
     * Retrieve all events within a time range.
     * 
     * @param fromTime The start time
     * @param toTime The end time
     * @return List of events in the time range
     */
    suspend fun getEventsByTimeRange(
        fromTime: Instant,
        toTime: Instant
    ): List<EventStoreRecord>

    /**
     * Get the latest version for an aggregate.
     * 
     * @param aggregateId The aggregate identifier
     * @param aggregateType The aggregate type
     * @return The latest version number
     */
    suspend fun getLatestVersion(aggregateId: String, aggregateType: String): Long
}

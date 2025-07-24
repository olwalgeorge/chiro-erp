package org.chiro.core_business_service.shared.infrastructure.event.store

import org.chiro.core_business_service.shared.infrastructure.event.EventStoreRecord
import jakarta.persistence.*
import java.time.Instant

/**
 * Simple JPA entity for event store persistence.
 * Uses standard JPA annotations without Panache complications.
 */
@Entity
@Table(name = "event_store")
class SimpleEventStoreEntity {

    @Id
    @Column(name = "event_id", nullable = false, unique = true)
    var eventId: String = ""

    @Column(name = "event_type", nullable = false)
    var eventType: String = ""

    @Column(name = "aggregate_id", nullable = false)
    var aggregateId: String = ""

    @Column(name = "aggregate_type", nullable = false)
    var aggregateType: String = ""

    @Column(name = "aggregate_version", nullable = false)
    var aggregateVersion: Long = 0

    @Column(name = "event_data", columnDefinition = "TEXT", nullable = false)
    var eventData: String = ""

    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String = "{}"

    @Column(name = "timestamp", nullable = false)
    var timestamp: Instant = Instant.now()

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    // No-argument constructor for JPA
    constructor()

    // Constructor for creating new events
    constructor(
        eventId: String,
        eventType: String,
        aggregateId: String,
        aggregateType: String,
        aggregateVersion: Long,
        eventData: String,
        metadata: Map<String, Any>,
        timestamp: Instant
    ) {
        this.eventId = eventId
        this.eventType = eventType
        this.aggregateId = aggregateId
        this.aggregateType = aggregateType
        this.aggregateVersion = aggregateVersion
        this.eventData = eventData
        this.metadata = metadata.toString() // Simple serialization for now
        this.timestamp = timestamp
        this.createdAt = Instant.now()
    }

    /**
     * Convert this entity to an EventStoreRecord.
     */
    fun toEventStoreRecord(): EventStoreRecord {
        return EventStoreRecord(
            eventId = this.eventId,
            eventType = this.eventType,
            aggregateId = this.aggregateId,
            aggregateType = this.aggregateType,
            aggregateVersion = this.aggregateVersion,
            eventData = this.eventData,
            metadata = emptyMap(), // Parse from string if needed
            timestamp = this.timestamp
        )
    }
}

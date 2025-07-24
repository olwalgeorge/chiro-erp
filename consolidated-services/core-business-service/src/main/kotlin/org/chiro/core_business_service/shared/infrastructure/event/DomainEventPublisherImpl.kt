package org.chiro.core_business_service.shared.infrastructure.event

import org.chiro.core_business_service.shared.domain.event.DomainEvent
import org.chiro.core_business_service.shared.domain.event.DomainEventPublisher
import org.chiro.core_business_service.shared.infrastructure.event.store.EventStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID

/**
 * Domain event publisher implementation providing event publishing infrastructure.
 * 
 * This service handles the complete event publishing lifecycle:
 * - Event persistence to event store
 * - Publishing to reactive messaging channels
 * - Event serialization and metadata management
 * - Error handling and retry logic
 * 
 * Events are published to both:
 * 1. Event Store - for event sourcing and audit trail
 * 2. Message Bus - for real-time integration and notifications
 */
@ApplicationScoped
class DomainEventPublisherImpl : DomainEventPublisher {

    private val logger: Logger = LoggerFactory.getLogger(DomainEventPublisherImpl::class.java)

    @Inject
    lateinit var eventStore: EventStore

    @Inject
    lateinit var objectMapper: ObjectMapper

    /**
     * Publish a domain event through the complete event pipeline.
     * 
     * This method:
     * 1. Validates the event
     * 2. Stores it in the event store
     * 3. Publishes to reactive messaging channels
     * 4. Logs the operation for monitoring
     * 
     * @param event The domain event to publish
     */
    override suspend fun publish(event: DomainEvent) {
        try {
            logger.debug("Publishing domain event: ${event.eventType} for aggregate: ${event.aggregateId}")

            // 1. Validate event
            validateEvent(event)

            // 2. Store in event store
            storeEvent(event)

            // 3. Publish to message channels
            publishToChannels(event)

            logger.info("Successfully published domain event: ${event.eventType} for aggregate: ${event.aggregateId}")

        } catch (e: Exception) {
            logger.error("Failed to publish domain event: ${event.eventType} for aggregate: ${event.aggregateId}", e)
            throw EventPublishingException("Failed to publish event: ${event.eventType}", e)
        }
    }

    /**
     * Publish multiple domain events in a batch operation.
     * 
     * @param events The list of domain events to publish
     */
    override suspend fun publishAll(events: List<DomainEvent>) {
        if (events.isEmpty()) {
            logger.debug("No events to publish")
            return
        }

        logger.debug("Publishing batch of ${events.size} domain events")

        try {
            // Process events in order
            events.forEach { event ->
                publish(event)
            }

            logger.info("Successfully published batch of ${events.size} domain events")

        } catch (e: Exception) {
            logger.error("Failed to publish batch of ${events.size} domain events", e)
            throw EventPublishingException("Failed to publish event batch", e)
        }
    }

    /**
     * Validate that the domain event is properly formed.
     * 
     * @param event The event to validate
     * @throws IllegalArgumentException if validation fails
     */
    private fun validateEvent(event: DomainEvent) {
        require(event.eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(event.eventType.isNotBlank()) { "Event type cannot be blank" }
        require(event.aggregateId.isNotBlank()) { "Aggregate ID cannot be blank" }
        require(event.aggregateType.isNotBlank()) { "Aggregate type cannot be blank" }
        
        logger.debug("Event validation passed for: ${event.eventType}")
    }

    /**
     * Store the event in the event store for persistence and audit trail.
     * 
     * @param event The event to store
     */
    private suspend fun storeEvent(event: DomainEvent) {
        try {
            val eventStoreRecord = EventStoreRecord(
                eventId = event.eventId,
                eventType = event.eventType,
                aggregateId = event.aggregateId,
                aggregateType = event.aggregateType,
                aggregateVersion = event.aggregateVersion,
                eventData = objectMapper.writeValueAsString(event),
                metadata = createEventMetadata(event),
                timestamp = event.timestamp
            )

            eventStore.save(eventStoreRecord)
            logger.debug("Event stored in event store: ${event.eventType}")

        } catch (e: Exception) {
            logger.error("Failed to store event in event store: ${event.eventType}", e)
            throw EventStorageException("Failed to store event: ${event.eventType}", e)
        }
    }

    /**
     * Publish the event to in-memory handlers (messaging channels disabled for now).
     * 
     * @param event The event to publish
     */
    private suspend fun publishToChannels(event: DomainEvent) {
        try {
            val eventMessage = EventMessage(
                eventId = event.eventId,
                eventType = event.eventType,
                aggregateId = event.aggregateId,
                aggregateType = event.aggregateType,
                aggregateVersion = event.aggregateVersion,
                payload = objectMapper.writeValueAsString(event),
                timestamp = event.timestamp,
                correlationId = UUID.randomUUID().toString()
            )

            // TODO: Re-enable messaging channels when Kafka/messaging dependency is added
            logger.debug("Event ready for messaging (channels disabled): ${event.eventType}")

        } catch (e: Exception) {
            logger.error("Failed to prepare event for messaging channels: ${event.eventType}", e)
            throw EventMessagingException("Failed to prepare event for channels: ${event.eventType}", e)
        }
    }

    /**
     * Create metadata for the event.
     * 
     * @param event The event to create metadata for
     * @return The metadata map
     */
    private fun createEventMetadata(event: DomainEvent): Map<String, Any> {
        return mapOf(
            "publishedAt" to Instant.now().toString(),
            "publisher" to "CoreBusinessService",
            "version" to "1.0",
            "source" to event.aggregateType,
            "correlationId" to UUID.randomUUID().toString()
        )
    }
}

/**
 * Event message wrapper for reactive messaging.
 */
data class EventMessage(
    val eventId: String,
    val eventType: String,
    val aggregateId: String,
    val aggregateType: String,
    val aggregateVersion: Long,
    val payload: String,
    val timestamp: Instant,
    val correlationId: String
)

/**
 * Event store record for persistence.
 */
data class EventStoreRecord(
    val eventId: String,
    val eventType: String,
    val aggregateId: String,
    val aggregateType: String,
    val aggregateVersion: Long,
    val eventData: String,
    val metadata: Map<String, Any>,
    val timestamp: Instant
)

/**
 * Exception thrown when event publishing fails.
 */
class EventPublishingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when event storage fails.
 */
class EventStorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when event messaging fails.
 */
class EventMessagingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

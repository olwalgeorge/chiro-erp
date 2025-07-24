package org.chiro.core_business_service.shared.domain.exception

/**
 * Base class for all domain exceptions
 */
open class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Thrown when a domain invariant is violated
 */
class DomainRuleViolationException(
    rule: String,
    details: String? = null
) : DomainException("Domain rule violation: $rule${details?.let { " - $it" } ?: ""}")

/**
 * Thrown when an aggregate is not found
 */
class AggregateNotFoundException(
    aggregateType: String,
    aggregateId: String
) : DomainException("$aggregateType with ID $aggregateId not found")

/**
 * Thrown when trying to create an aggregate that already exists
 */
class AggregateAlreadyExistsException(
    aggregateType: String,
    aggregateId: String
) : DomainException("$aggregateType with ID $aggregateId already exists")

/**
 * Thrown when there's an optimistic concurrency conflict
 */
class ConcurrencyException(
    aggregateType: String,
    aggregateId: String,
    expectedVersion: Long,
    actualVersion: Long
) : DomainException(
    "Concurrency conflict for $aggregateType $aggregateId: expected version $expectedVersion, actual version $actualVersion"
)

/**
 * Thrown when a business operation is invalid in the current state
 */
class InvalidOperationException(
    operation: String,
    currentState: String
) : DomainException("Operation '$operation' is not valid in current state: $currentState")

/**
 * Thrown when a value object is constructed with invalid data
 */
class ValueObjectValidationException(
    valueObjectType: String,
    validationError: String
) : DomainException("Invalid $valueObjectType: $validationError")

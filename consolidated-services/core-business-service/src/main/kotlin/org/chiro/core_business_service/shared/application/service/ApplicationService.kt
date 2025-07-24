package org.chiro.core_business_service.shared.application.service

import org.chiro.core_business_service.shared.domain.event.DomainEvent

/**
 * Application service interface for domain event publishing
 */
interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publish(events: List<DomainEvent>)
}

/**
 * Application service interface for transaction management
 */
interface TransactionManager {
    suspend fun <T> executeInTransaction(operation: suspend () -> T): T
}

/**
 * Base application service that provides common functionality
 */
abstract class ApplicationService(
    protected val eventPublisher: DomainEventPublisher,
    protected val transactionManager: TransactionManager
) {
    
    /**
     * Execute an operation within a transaction and publish domain events
     */
    protected suspend fun <T> executeWithEventPublishing(
        operation: suspend () -> Pair<T, List<DomainEvent>>
    ): T = transactionManager.executeInTransaction {
        val (result, events) = operation()
        if (events.isNotEmpty()) {
            eventPublisher.publish(events)
        }
        result
    }
}

/**
 * Validation service interface
 */
interface ValidationService {
    fun <T> validate(obj: T): ValidationResult
}

/**
 * Validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, List<String>> = emptyMap()
) {
    constructor(errors: Map<String, List<String>>) : this(errors.isEmpty(), errors)
    
    fun addError(field: String, message: String): ValidationResult {
        val currentErrors = errors[field] ?: emptyList()
        val updatedErrors = errors + (field to (currentErrors + message))
        return ValidationResult(false, updatedErrors)
    }
    
    fun combine(other: ValidationResult): ValidationResult {
        if (isValid && other.isValid) return this
        
        val combinedErrors = mutableMapOf<String, List<String>>()
        
        errors.forEach { (field, messages) ->
            combinedErrors[field] = messages
        }
        
        other.errors.forEach { (field, messages) ->
            val existing = combinedErrors[field] ?: emptyList()
            combinedErrors[field] = existing + messages
        }
        
        return ValidationResult(false, combinedErrors)
    }
}

/**
 * Extension function to create a valid result
 */
fun valid(): ValidationResult = ValidationResult(true)

/**
 * Extension function to create an invalid result
 */
fun invalid(errors: Map<String, List<String>>): ValidationResult = ValidationResult(false, errors)

/**
 * Extension function to create an invalid result with a single error
 */
fun invalid(field: String, message: String): ValidationResult = 
    ValidationResult(false, mapOf(field to listOf(message)))

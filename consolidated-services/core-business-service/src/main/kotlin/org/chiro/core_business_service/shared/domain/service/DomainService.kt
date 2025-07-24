package org.chiro.core_business_service.shared.domain.service

/**
 * Marker interface for all domain services.
 * 
 * Domain services encapsulate business logic that doesn't naturally fit within
 * a single aggregate or entity. They coordinate between aggregates and provide
 * operations that span multiple domain objects.
 * 
 * Key characteristics:
 * - Stateless operations
 * - Pure business logic
 * - No knowledge of infrastructure concerns
 * - Operates on domain objects
 */
interface DomainService

/**
 * Base abstract class for domain services providing common functionality.
 */
open class BaseDomainService : DomainService {
    
    /**
     * Template method for validation that subclasses can override.
     */
    protected open fun validateOperation() {
        // Default implementation - override in subclasses
    }
    
    /**
     * Template method for pre-operation hooks.
     */
    protected open fun beforeOperation() {
        // Default implementation - override in subclasses  
    }
    
    /**
     * Template method for post-operation hooks.
     */
    protected open fun afterOperation() {
        // Default implementation - override in subclasses
    }
}

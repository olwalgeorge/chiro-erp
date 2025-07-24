package org.chiro.core_business_service.shared.domain.repository

import org.chiro.core_business_service.shared.domain.aggregate.AggregateRoot
import org.chiro.core_business_service.shared.domain.valueobject.AggregateId

/**
 * Base repository interface for all aggregate roots.
 * 
 * This interface defines the core operations that all repositories must support,
 * ensuring consistency across all bounded contexts and modules.
 * 
 * Key design principles:
 * - Generic type safety for aggregate roots and their IDs
 * - Async operations using suspend functions
 * - Clear separation of concerns
 * - Consistent API across all modules
 * 
 * @param T The aggregate root type
 * @param ID The aggregate identifier type
 */
interface Repository<T, ID : AggregateId> where T : AggregateRoot<ID> {
    
    /**
     * Find an aggregate by its identifier.
     * 
     * @param id The aggregate identifier
     * @return The aggregate if found, null otherwise
     */
    suspend fun findById(id: ID): T?
    
    /**
     * Save an aggregate (insert or update).
     * 
     * This method should:
     * - Persist the aggregate
     * - Publish any domain events
     * - Handle optimistic locking
     * 
     * @param aggregate The aggregate to save
     * @return The saved aggregate
     */
    suspend fun save(aggregate: T): T
    
    /**
     * Delete an aggregate by its identifier.
     * 
     * @param id The identifier of the aggregate to delete
     * @return true if the aggregate was deleted, false if not found
     */
    suspend fun delete(id: ID): Boolean
    
    /**
     * Check if an aggregate exists.
     * 
     * @param id The aggregate identifier
     * @return true if the aggregate exists, false otherwise
     */
    suspend fun exists(id: ID): Boolean
    
    /**
     * Count the total number of aggregates.
     * 
     * @return The total count
     */
    suspend fun count(): Long
    
    /**
     * Find all aggregates (use with caution for large datasets).
     * 
     * @return List of all aggregates
     */
    suspend fun findAll(): List<T>
}

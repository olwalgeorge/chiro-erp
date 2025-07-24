package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.AuditTrail
import com.chiroerp.finance.domain.valueobject.AuditTrailId
import com.chiroerp.finance.domain.valueobject.TenantId
import java.time.LocalDateTime

/**
 * Repository interface for AuditTrail entity data access operations.
 * 
 * Provides comprehensive audit trail persistence capabilities including:
 * - Regulatory compliance and audit logging for all financial operations
 * - Complex querying by entity, user, action, and temporal criteria
 * - Data integrity and security audit trail management
 * - Compliance reporting and forensic analysis support
 * - Immutable audit records with tamper-proof integrity
 * - High-performance operations for real-time audit logging
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure audit integrity and regulatory compliance requirements.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface AuditTrailRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves an audit trail entry to the persistent store with immutable integrity.
     * 
     * Handles insert operations only as audit trails are immutable by design.
     * Ensures proper multi-tenant isolation, audit integrity, and
     * maintains tamper-proof audit records for regulatory compliance.
     * 
     * @param auditTrail The audit trail entity to save
     * @return The saved audit trail with updated metadata (timestamps, sequence)
     * @throws IllegalArgumentException if audit trail is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws AuditIntegrityException if audit integrity validation fails
     */
    suspend fun save(auditTrail: AuditTrail): AuditTrail
    
    /**
     * Finds an audit trail by its unique identifier within a tenant context.
     * 
     * @param auditTrailId The unique audit trail identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The audit trail if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(auditTrailId: AuditTrailId, tenantId: TenantId): AuditTrail?
    
    /**
     * Checks if an audit trail exists by its identifier within a tenant context.
     * 
     * @param auditTrailId The unique audit trail identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if audit trail exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(auditTrailId: AuditTrailId, tenantId: TenantId): Boolean
    
    // Note: Delete operations are intentionally NOT provided for audit trails
    // as they must remain immutable for regulatory compliance and data integrity
    
    // =====================================
    // Entity-based Queries
    // =====================================
    
    /**
     * Finds audit trails by entity type within a tenant context.
     * 
     * @param entityType The entity type to filter by (e.g., "Account", "Transaction", "Payment")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByEntityType(
        entityType: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails by entity identifier within a tenant context.
     * 
     * @param entityId The entity identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails for the specified entity ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByEntityId(
        entityId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails by entity type and identifier within a tenant context.
     * 
     * @param entityType The entity type to filter by
     * @param entityId The entity identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails for the specified entity ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByEntityTypeAndId(
        entityType: String,
        entityId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails for multiple entities within a tenant context.
     * 
     * @param entityType The entity type to filter by
     * @param entityIds The list of entity identifiers to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails for the specified entities ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByEntityTypeAndIds(
        entityType: String,
        entityIds: List<String>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    // =====================================
    // User and Action Queries
    // =====================================
    
    /**
     * Finds audit trails by user identifier within a tenant context.
     * 
     * @param userId The user identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails for the specified user ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByUserId(
        userId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails by action type within a tenant context.
     * 
     * @param action The action type to filter by (e.g., "CREATE", "UPDATE", "DELETE")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails with the specified action ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAction(
        action: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails by user and action within a tenant context.
     * 
     * @param userId The user identifier to filter by
     * @param action The action type to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails matching user and action criteria
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByUserIdAndAction(
        userId: String,
        action: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails by session identifier within a tenant context.
     * 
     * @param sessionId The session identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails for the specified session ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findBySessionId(
        sessionId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails by IP address within a tenant context.
     * 
     * @param ipAddress The IP address to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails from the specified IP address ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIpAddress(
        ipAddress: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    // =====================================
    // Date and Time-based Queries
    // =====================================
    
    /**
     * Finds audit trails within a specific timestamp range for a tenant.
     * 
     * @param startTimestamp The start timestamp (inclusive)
     * @param endTimestamp The end timestamp (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails within the timestamp range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTimestampRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails after a specific timestamp for a tenant.
     * 
     * @param timestamp The timestamp threshold (exclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails after the specified timestamp
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findAfterTimestamp(
        timestamp: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds audit trails before a specific timestamp for a tenant.
     * 
     * @param timestamp The timestamp threshold (exclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails before the specified timestamp
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findBeforeTimestamp(
        timestamp: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds recent audit trails within the last specified minutes for a tenant.
     * 
     * @param minutes The number of minutes to look back
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of recent audit trails ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findRecentAuditTrails(
        minutes: Int = 60,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    // =====================================
    // Security and Compliance Queries
    // =====================================
    
    /**
     * Finds audit trails by severity level within a tenant context.
     * 
     * @param severity The severity level to filter by (e.g., "LOW", "MEDIUM", "HIGH", "CRITICAL")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of audit trails with the specified severity level
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findBySeverity(
        severity: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds high-risk audit trails within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of high-risk audit trails ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findHighRiskAuditTrails(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds security-related audit trails within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of security-related audit trails ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findSecurityAuditTrails(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds failed operation audit trails within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of failed operation audit trails ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findFailedOperations(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    /**
     * Finds suspicious activity audit trails within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of suspicious activity audit trails ordered by timestamp descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findSuspiciousActivity(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<AuditTrail>
    
    // =====================================
    // Complex Compliance Queries
    // =====================================
    
    /**
     * Finds audit trails for regulatory compliance reporting within a date range.
     * 
     * @param startTimestamp The start timestamp for compliance period (inclusive)
     * @param endTimestamp The end timestamp for compliance period (inclusive)
     * @param regulatoryRequirement The regulatory requirement type (optional)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 1000)
     * @return List of audit trails for compliance reporting
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findForComplianceReporting(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
        regulatoryRequirement: String? = null,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 1000
    ): List<AuditTrail>
    
    /**
     * Finds audit trails for forensic analysis within a tenant context.
     * 
     * @param entityType The entity type to analyze (optional)
     * @param entityId The entity identifier to analyze (optional)
     * @param userId The user identifier to analyze (optional)
     * @param startTimestamp The start timestamp for analysis (optional)
     * @param endTimestamp The end timestamp for analysis (optional)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 1000)
     * @return List of audit trails for forensic analysis
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findForForensicAnalysis(
        entityType: String? = null,
        entityId: String? = null,
        userId: String? = null,
        startTimestamp: LocalDateTime? = null,
        endTimestamp: LocalDateTime? = null,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 1000
    ): List<AuditTrail>
    
    // =====================================
    // Aggregation and Statistical Queries
    // =====================================
    
    /**
     * Counts the total number of audit trails for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of audit trails
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts audit trails by entity type for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of entity type to audit trail count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByEntityType(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts audit trails by action type for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of action type to audit trail count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByAction(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts audit trails by user for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of user ID to audit trail count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByUser(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts audit trails by severity level for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of severity level to audit trail count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countBySeverity(tenantId: TenantId): Map<String, Long>
    
    /**
     * Calculates audit trail statistics for a specific time period.
     * 
     * @param startTimestamp The start timestamp for statistics calculation
     * @param endTimestamp The end timestamp for statistics calculation
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map containing various audit trail statistics (counts, rates, etc.)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAuditStatistics(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
        tenantId: TenantId
    ): Map<String, Any>
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple audit trails in a single batch operation with integrity preservation.
     * 
     * Provides better performance for bulk audit operations while maintaining
     * audit integrity, immutability, and regulatory compliance requirements.
     * 
     * @param auditTrails The list of audit trails to save
     * @return The list of saved audit trails with updated metadata
     * @throws IllegalArgumentException if audit trails are invalid
     * @throws DataAccessException if batch operation fails
     * @throws AuditIntegrityException if audit integrity validations fail
     */
    suspend fun saveAll(auditTrails: List<AuditTrail>): List<AuditTrail>
    
    /**
     * Finds multiple audit trails by their identifiers within a tenant context.
     * 
     * @param auditTrailIds The list of audit trail identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found audit trails (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        auditTrailIds: List<AuditTrailId>,
        tenantId: TenantId
    ): List<AuditTrail>
    
    // Note: Bulk delete operations are intentionally NOT provided for audit trails
    // as they must remain immutable for regulatory compliance and data integrity
}

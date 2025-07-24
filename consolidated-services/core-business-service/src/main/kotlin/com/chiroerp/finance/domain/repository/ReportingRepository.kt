package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.Report
import com.chiroerp.finance.domain.valueobject.ReportId
import com.chiroerp.finance.domain.valueobject.TenantId
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Repository interface for Report entity data access operations.
 * 
 * Provides comprehensive reporting persistence capabilities including:
 * - Business intelligence and analytics report generation and storage
 * - Complex querying by report type, category, and generation criteria
 * - Report template management and customization support
 * - Scheduled reporting and automated report generation
 * - Performance metrics and KPI reporting data access
 * - High-performance operations for real-time dashboard and analytics
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure report consistency and business intelligence integrity.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface ReportingRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves a report entity to the persistent store with generation tracking.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation, report consistency, and
     * maintains report generation metadata across business intelligence processes.
     * 
     * @param report The report entity to save
     * @return The saved report with updated metadata (version, timestamps, status)
     * @throws IllegalArgumentException if report is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws ConcurrencyException if optimistic locking conflict occurs
     * @throws ReportValidationException if report validation fails
     */
    suspend fun save(report: Report): Report
    
    /**
     * Finds a report by its unique identifier within a tenant context.
     * 
     * @param reportId The unique report identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The report if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(reportId: ReportId, tenantId: TenantId): Report?
    
    /**
     * Finds a report by its unique report name within a tenant context.
     * 
     * @param reportName The unique report name
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The report if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByReportName(
        reportName: String,
        tenantId: TenantId
    ): Report?
    
    /**
     * Finds a report by its unique report code within a tenant context.
     * 
     * @param reportCode The unique report code
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The report if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByReportCode(
        reportCode: String,
        tenantId: TenantId
    ): Report?
    
    /**
     * Checks if a report exists by its identifier within a tenant context.
     * 
     * @param reportId The unique report identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if report exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(reportId: ReportId, tenantId: TenantId): Boolean
    
    /**
     * Checks if a report exists by its name within a tenant context.
     * 
     * @param reportName The unique report name
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if report exists, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsByReportName(
        reportName: String,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Deletes a report by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and report history.
     * May be restricted based on report status and business rules.
     * 
     * @param reportId The unique report identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if report cannot be deleted
     */
    suspend fun deleteById(reportId: ReportId, tenantId: TenantId)
    
    // =====================================
    // Report Type and Category Queries
    // =====================================
    
    /**
     * Finds reports by report type within a tenant context.
     * 
     * @param reportType The report type to filter by (e.g., "FINANCIAL", "OPERATIONAL", "COMPLIANCE")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports with the specified type ordered by creation date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByReportType(
        reportType: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds reports by category within a tenant context.
     * 
     * @param category The report category to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports with the specified category
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCategory(
        category: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds reports by format within a tenant context.
     * 
     * @param format The report format to filter by (e.g., "PDF", "EXCEL", "CSV", "JSON")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports with the specified format
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByFormat(
        format: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds reports by template identifier within a tenant context.
     * 
     * @param templateId The template identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports using the specified template
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByTemplate(
        templateId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    // =====================================
    // Status and Generation Queries
    // =====================================
    
    /**
     * Finds reports by status within a tenant context.
     * 
     * @param status The report status to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports with the specified status
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByStatus(
        status: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds draft reports within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of draft reports ordered by creation date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findDraftReports(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds reports currently being generated within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports in generation process ordered by start time
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findGeneratingReports(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds completed reports within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of completed reports ordered by completion date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findCompletedReports(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds failed reports within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of failed reports ordered by failure date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findFailedReports(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds published reports within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of published reports ordered by publication date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPublishedReports(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    // =====================================
    // User and Access Queries
    // =====================================
    
    /**
     * Finds reports by creator within a tenant context.
     * 
     * @param creatorId The creator identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports created by the specified user
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCreator(
        creatorId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds reports accessible by a specific user within a tenant context.
     * 
     * @param userId The user identifier to check access for
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports accessible by the specified user
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findAccessibleByUser(
        userId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds public reports within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of public reports ordered by creation date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPublicReports(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    // =====================================
    // Date-based Queries
    // =====================================
    
    /**
     * Finds reports within a specific generation date range for a tenant.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports generated within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByGenerationDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds reports by data period within a tenant context.
     * 
     * @param periodStart The data period start date
     * @param periodEnd The data period end date
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports covering the specified data period
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByDataPeriod(
        periodStart: LocalDate,
        periodEnd: LocalDate,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds recently generated reports within a tenant context.
     * 
     * @param hours The number of hours to look back
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of recently generated reports ordered by generation date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findRecentReports(
        hours: Int = 24,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    // =====================================
    // Scheduled and Automated Queries
    // =====================================
    
    /**
     * Finds scheduled reports within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of scheduled reports ordered by next execution time
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findScheduledReports(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds reports due for generation within a tenant context.
     * 
     * @param dueTime The time threshold for due reports
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of reports due for generation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findReportsDueForGeneration(
        dueTime: LocalDateTime = LocalDateTime.now(),
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds recurring reports within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of recurring reports ordered by next execution time
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findRecurringReports(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    // =====================================
    // Performance and Size Queries
    // =====================================
    
    /**
     * Finds large reports exceeding specified size threshold within a tenant context.
     * 
     * @param sizeThresholdMB The size threshold in megabytes
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of large reports ordered by size descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findLargeReports(
        sizeThresholdMB: Int = 100,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    /**
     * Finds slow-generating reports exceeding specified duration threshold.
     * 
     * @param durationThresholdMinutes The duration threshold in minutes
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of slow-generating reports ordered by generation duration descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findSlowGeneratingReports(
        durationThresholdMinutes: Int = 30,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Report>
    
    // =====================================
    // Aggregation and Analytics Queries
    // =====================================
    
    /**
     * Counts the total number of reports for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of reports
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts reports by type for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of report type to count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByReportType(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts reports by status for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of status to report count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByStatus(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts reports by creator for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of creator ID to report count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByCreator(tenantId: TenantId): Map<String, Long>
    
    /**
     * Calculates report generation statistics for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map containing generation statistics (success rate, average duration, etc.)
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateGenerationStatistics(tenantId: TenantId): Map<String, Any>
    
    /**
     * Calculates the average report generation time for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The average generation time in minutes
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAverageGenerationTime(tenantId: TenantId): Double
    
    /**
     * Calculates the report success rate for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The success rate as a percentage (0.0 to 1.0)
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateSuccessRate(tenantId: TenantId): Double
    
    /**
     * Calculates total storage size used by reports for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total storage size in megabytes
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalStorageSize(tenantId: TenantId): Long
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple reports in a single batch operation with generation consistency.
     * 
     * Provides better performance for bulk operations while maintaining
     * report consistency, generation workflow integrity, and business intelligence requirements.
     * 
     * @param reports The list of reports to save
     * @return The list of saved reports with updated metadata
     * @throws IllegalArgumentException if reports are invalid
     * @throws DataAccessException if batch operation fails
     * @throws ConcurrencyException if optimistic locking conflicts occur
     * @throws ReportValidationException if report validations fail
     */
    suspend fun saveAll(reports: List<Report>): List<Report>
    
    /**
     * Finds multiple reports by their identifiers within a tenant context.
     * 
     * @param reportIds The list of report identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found reports (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        reportIds: List<ReportId>,
        tenantId: TenantId
    ): List<Report>
    
    /**
     * Updates report statuses in bulk for generation workflow processing.
     * 
     * @param reportIds The list of report identifiers to update
     * @param newStatus The new status to set
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if update operation fails
     * @throws BusinessRuleException if status transition is invalid
     */
    suspend fun updateStatusBulk(
        reportIds: List<ReportId>,
        newStatus: String,
        tenantId: TenantId
    )
    
    /**
     * Deletes multiple reports by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and report history.
     * May be restricted based on report statuses and business rules.
     * 
     * @param reportIds The list of report identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any report cannot be deleted
     */
    suspend fun deleteByIds(
        reportIds: List<ReportId>,
        tenantId: TenantId
    )
}

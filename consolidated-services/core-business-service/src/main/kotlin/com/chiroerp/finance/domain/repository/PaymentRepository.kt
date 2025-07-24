package com.chiroerp.finance.domain.repository

import com.chiroerp.finance.domain.entity.Payment
import com.chiroerp.finance.domain.valueobject.PaymentId
import com.chiroerp.finance.domain.valueobject.AccountId
import com.chiroerp.finance.domain.valueobject.TenantId
import com.chiroerp.finance.domain.valueobject.Currency
import com.chiroerp.finance.domain.valueobject.Money
import java.time.LocalDateTime

/**
 * Repository interface for Payment entity data access operations.
 * 
 * Provides comprehensive payment persistence capabilities including:
 * - Payment lifecycle management with workflow state tracking
 * - Multi-channel payment routing and processing support
 * - Complex querying by status, amount, date, and routing criteria
 * - Integration with payment gateways and processing networks
 * - Audit trail and compliance data access for regulatory requirements
 * - High-performance operations for real-time payment processing
 * 
 * This interface follows Domain-Driven Design principles and provides
 * a clean abstraction layer between the domain and infrastructure layers.
 * All operations ensure payment consistency and regulatory compliance.
 * 
 * @since 1.0.0
 * @author ChiroERP Finance Team
 */
interface PaymentRepository {
    
    // =====================================
    // Basic CRUD Operations
    // =====================================
    
    /**
     * Saves a payment entity to the persistent store with workflow state tracking.
     * 
     * Handles both insert and update operations based on entity state.
     * Ensures proper multi-tenant isolation, payment consistency, and
     * maintains payment workflow state across processing stages.
     * 
     * @param payment The payment entity to save
     * @return The saved payment with updated metadata (version, timestamps, state)
     * @throws IllegalArgumentException if payment is invalid
     * @throws DataAccessException if persistence operation fails
     * @throws ConcurrencyException if optimistic locking conflict occurs
     * @throws PaymentValidationException if payment validation fails
     */
    suspend fun save(payment: Payment): Payment
    
    /**
     * Finds a payment by its unique identifier within a tenant context.
     * 
     * @param paymentId The unique payment identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The payment if found, null otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findById(paymentId: PaymentId, tenantId: TenantId): Payment?
    
    /**
     * Finds a payment by its unique payment reference within a tenant context.
     * 
     * @param paymentReference The unique payment reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The payment if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByPaymentReference(
        paymentReference: String,
        tenantId: TenantId
    ): Payment?
    
    /**
     * Finds a payment by external transaction ID (gateway reference).
     * 
     * @param externalTransactionId The external payment gateway transaction ID
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The payment if found, null otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByExternalTransactionId(
        externalTransactionId: String,
        tenantId: TenantId
    ): Payment?
    
    /**
     * Checks if a payment exists by its identifier within a tenant context.
     * 
     * @param paymentId The unique payment identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if payment exists, false otherwise
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsById(paymentId: PaymentId, tenantId: TenantId): Boolean
    
    /**
     * Checks if a payment exists by its reference within a tenant context.
     * 
     * @param paymentReference The unique payment reference number
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return True if payment exists, false otherwise
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun existsByPaymentReference(
        paymentReference: String,
        tenantId: TenantId
    ): Boolean
    
    /**
     * Deletes a payment by its identifier within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and regulatory compliance.
     * May be restricted based on payment status and business rules.
     * 
     * @param paymentId The unique payment identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if identifiers are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if payment cannot be deleted
     */
    suspend fun deleteById(paymentId: PaymentId, tenantId: TenantId)
    
    // =====================================
    // Account and Routing Queries
    // =====================================
    
    /**
     * Finds all payments for a specific account within a tenant context.
     * 
     * @param accountId The account identifier to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments ordered by creation date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccount(
        accountId: AccountId,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds payments for multiple accounts within a tenant context.
     * 
     * @param accountIds The list of account identifiers to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments ordered by creation date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAccounts(
        accountIds: List<AccountId>,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds payments by payment method within a tenant context.
     * 
     * @param paymentMethod The payment method to filter by (e.g., "CREDIT_CARD", "BANK_TRANSFER")
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments with the specified payment method
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByPaymentMethod(
        paymentMethod: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds payments by payment gateway within a tenant context.
     * 
     * @param gatewayId The payment gateway identifier
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments processed through the specified gateway
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByPaymentGateway(
        gatewayId: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    // =====================================
    // Status and Workflow Queries
    // =====================================
    
    /**
     * Finds payments by status within a tenant context.
     * 
     * @param status The payment status to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments with the specified status
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByStatus(
        status: String,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds pending payments within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of pending payments ordered by creation date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPendingPayments(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds processing payments within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of processing payments ordered by processing date
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findProcessingPayments(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds completed payments within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of completed payments ordered by completion date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findCompletedPayments(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds failed payments within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of failed payments ordered by failure date descending
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findFailedPayments(
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds payments requiring retry within a tenant context.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param maxRetryCount The maximum retry count to consider
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments that can be retried
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPaymentsForRetry(
        tenantId: TenantId,
        maxRetryCount: Int = 3,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    // =====================================
    // Date-based Queries
    // =====================================
    
    /**
     * Finds payments within a specific date range for a tenant.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds payments processed within a specific date range.
     * 
     * @param startDate The start processing date (inclusive)
     * @param endDate The end processing date (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments processed within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByProcessedDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds payments due for processing by a specific date.
     * 
     * @param dueDate The due date to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments due for processing
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findPaymentsDueBy(
        dueDate: LocalDateTime,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    // =====================================
    // Amount-based Queries
    // =====================================
    
    /**
     * Finds payments with amount greater than the specified threshold.
     * 
     * @param minimumAmount The minimum amount threshold
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments with amount above threshold
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByMinimumAmount(
        minimumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    /**
     * Finds payments with amount within the specified range.
     * 
     * @param minimumAmount The minimum amount threshold (inclusive)
     * @param maximumAmount The maximum amount threshold (inclusive)
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments with amount within range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByAmountRange(
        minimumAmount: Money,
        maximumAmount: Money,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    // =====================================
    // Currency-based Queries
    // =====================================
    
    /**
     * Finds payments by currency within a tenant context.
     * 
     * @param currency The currency to filter by
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param offset The number of records to skip (default: 0)
     * @param limit The maximum number of records to return (default: 100)
     * @return List of payments with the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByCurrency(
        currency: Currency,
        tenantId: TenantId,
        offset: Int = 0,
        limit: Int = 100
    ): List<Payment>
    
    // =====================================
    // Aggregation and Reporting Queries
    // =====================================
    
    /**
     * Counts the total number of payments for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The total count of payments
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByTenant(tenantId: TenantId): Long
    
    /**
     * Counts payments by status for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of status to payment count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByStatus(tenantId: TenantId): Map<String, Long>
    
    /**
     * Counts payments by payment method for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return Map of payment method to count
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun countByPaymentMethod(tenantId: TenantId): Map<String, Long>
    
    /**
     * Calculates the total payment volume for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for volume calculation (optional)
     * @return The total payment volume in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateTotalVolume(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    /**
     * Calculates the success rate for payments within a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return The payment success rate as a percentage (0.0 to 1.0)
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateSuccessRate(tenantId: TenantId): Double
    
    /**
     * Calculates the average payment amount for a tenant.
     * 
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @param currency The currency for calculation (optional)
     * @return The average payment amount in the specified currency
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun calculateAverageAmount(
        tenantId: TenantId,
        currency: Currency? = null
    ): Money
    
    // =====================================
    // Bulk Operations
    // =====================================
    
    /**
     * Saves multiple payments in a single batch operation with workflow consistency.
     * 
     * Provides better performance for bulk operations while maintaining
     * payment consistency, workflow state integrity, and regulatory compliance.
     * 
     * @param payments The list of payments to save
     * @return The list of saved payments with updated metadata
     * @throws IllegalArgumentException if payments are invalid
     * @throws DataAccessException if batch operation fails
     * @throws ConcurrencyException if optimistic locking conflicts occur
     * @throws PaymentValidationException if payment validations fail
     */
    suspend fun saveAll(payments: List<Payment>): List<Payment>
    
    /**
     * Finds multiple payments by their identifiers within a tenant context.
     * 
     * @param paymentIds The list of payment identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @return List of found payments (may be fewer than requested)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if query operation fails
     */
    suspend fun findByIds(
        paymentIds: List<PaymentId>,
        tenantId: TenantId
    ): List<Payment>
    
    /**
     * Updates payment statuses in bulk for workflow processing.
     * 
     * @param paymentIds The list of payment identifiers to update
     * @param newStatus The new status to set
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if update operation fails
     * @throws BusinessRuleException if status transition is invalid
     */
    suspend fun updateStatusBulk(
        paymentIds: List<PaymentId>,
        newStatus: String,
        tenantId: TenantId
    )
    
    /**
     * Deletes multiple payments by their identifiers within a tenant context.
     * 
     * Performs soft delete to maintain audit trail and regulatory compliance.
     * May be restricted based on payment statuses and business rules.
     * 
     * @param paymentIds The list of payment identifiers
     * @param tenantId The tenant identifier for multi-tenant isolation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws DataAccessException if delete operation fails
     * @throws BusinessRuleException if any payment cannot be deleted
     */
    suspend fun deleteByIds(
        paymentIds: List<PaymentId>,
        tenantId: TenantId
    )
}

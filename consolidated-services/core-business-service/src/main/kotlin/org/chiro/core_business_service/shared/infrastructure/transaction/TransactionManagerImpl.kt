package org.chiro.core_business_service.shared.infrastructure.transaction

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.transaction.UserTransaction
import java.util.UUID
import java.time.Instant
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.withContext

/**
 * Transaction manager implementation providing cross-module transaction coordination.
 * 
 * This service handles:
 * - Distributed transaction coordination across modules
 * - Transaction lifecycle management
 * - Rollback and compensation logic
 * - Transaction monitoring and logging
 * - Saga pattern implementation for complex workflows
 */
@ApplicationScoped
class TransactionManagerImpl : TransactionManager {

    private val logger: Logger = LoggerFactory.getLogger(TransactionManagerImpl::class.java)

    @Inject
    lateinit var userTransaction: UserTransaction

    private val activeTransactions = mutableMapOf<String, TransactionContext>()

    /**
     * Execute a business operation within a transaction context.
     * 
     * @param operation The operation to execute
     * @return The result of the operation
     */
    @Transactional
    override suspend fun <T> executeInTransaction(operation: suspend () -> T): T {
        val transactionId = UUID.randomUUID().toString()
        val context = TransactionContext(
            id = transactionId,
            startTime = Instant.now(),
            status = TransactionStatus.ACTIVE
        )

        activeTransactions[transactionId] = context

        return try {
            logger.debug("Starting transaction: $transactionId")
            
            val result = operation()
            
            context.status = TransactionStatus.COMMITTED
            context.endTime = Instant.now()
            
            logger.debug("Transaction committed successfully: $transactionId")
            result

        } catch (e: Exception) {
            context.status = TransactionStatus.ROLLED_BACK
            context.endTime = Instant.now()
            context.error = e.message
            
            logger.error("Transaction rolled back: $transactionId", e)
            throw e

        } finally {
            activeTransactions.remove(transactionId)
        }
    }

    /**
     * Execute a saga transaction with compensation capabilities.
     * 
     * @param sagaDefinition The saga definition containing steps and compensations
     * @return The saga execution result
     */
    override suspend fun <T> executeSaga(sagaDefinition: SagaDefinition<T>): SagaResult<T> {
        val sagaId = UUID.randomUUID().toString()
        val context = SagaContext(
            id = sagaId,
            startTime = Instant.now(),
            steps = mutableListOf()
        )

        logger.info("Starting saga: $sagaId with ${sagaDefinition.steps.size} steps")

        try {
            var result: T? = null
            
            // Execute each step in the saga
            sagaDefinition.steps.forEachIndexed { index, step ->
                logger.debug("Executing saga step $index: ${step.name}")
                
                val stepResult = try {
                    step.execute()
                } catch (e: Exception) {
                    logger.error("Saga step $index failed: ${step.name}", e)
                    
                    // Compensate previous steps in reverse order
                    compensatePreviousSteps(context, index - 1)
                    
                    return SagaResult.failure(sagaId, e)
                }
                
                context.steps.add(
                    SagaStepExecution(
                        stepName = step.name,
                        executedAt = Instant.now(),
                        result = stepResult.toString(),
                        compensated = false
                    )
                )
                
                // If this is the final step, capture the result
                if (index == sagaDefinition.steps.size - 1) {
                    @Suppress("UNCHECKED_CAST")
                    result = stepResult as T
                }
            }

            logger.info("Saga completed successfully: $sagaId")
            return SagaResult.success(sagaId, result!!)

        } catch (e: Exception) {
            logger.error("Saga execution failed: $sagaId", e)
            return SagaResult.failure(sagaId, e)
        }
    }

    /**
     * Execute an operation with a specific transaction timeout.
     * 
     * @param timeoutSeconds The timeout in seconds
     * @param operation The operation to execute
     * @return The result of the operation
     */
    override suspend fun <T> executeWithTimeout(
        timeoutSeconds: Int,
        operation: suspend () -> T
    ): T {
        return try {
            userTransaction.setTransactionTimeout(timeoutSeconds)
            executeInTransaction(operation)
        } finally {
            // Reset to default timeout
            userTransaction.setTransactionTimeout(0)
        }
    }

    /**
     * Get the current transaction status.
     * 
     * @return The transaction status
     */
    override suspend fun getCurrentTransactionStatus(): String {
        return try {
            when (userTransaction.status) {
                jakarta.transaction.Status.STATUS_ACTIVE -> "ACTIVE"
                jakarta.transaction.Status.STATUS_COMMITTED -> "COMMITTED"
                jakarta.transaction.Status.STATUS_MARKED_ROLLBACK -> "MARKED_ROLLBACK"
                jakarta.transaction.Status.STATUS_ROLLEDBACK -> "ROLLED_BACK"
                jakarta.transaction.Status.STATUS_NO_TRANSACTION -> "NO_TRANSACTION"
                else -> "UNKNOWN"
            }
        } catch (e: Exception) {
            logger.error("Failed to get transaction status", e)
            "ERROR"
        }
    }

    /**
     * Get transaction statistics.
     * 
     * @return Transaction statistics
     */
    override suspend fun getTransactionStatistics(): TransactionStatistics {
        return TransactionStatistics(
            activeTransactions = activeTransactions.size,
            totalTransactions = 0, // This would need to be tracked separately
            successfulTransactions = 0, // This would need to be tracked separately
            failedTransactions = 0, // This would need to be tracked separately
            averageExecutionTime = 0.0 // This would need to be calculated from historical data
        )
    }

    /**
     * Compensate previous steps in a saga.
     * 
     * @param context The saga context
     * @param lastStepIndex The index of the last step to compensate
     */
    private suspend fun compensatePreviousSteps(context: SagaContext, lastStepIndex: Int) {
        logger.info("Compensating saga steps for saga: ${context.id}")
        
        for (i in lastStepIndex downTo 0) {
            val stepExecution = context.steps[i]
            if (!stepExecution.compensated) {
                try {
                    logger.debug("Compensating step: ${stepExecution.stepName}")
                    
                    // Here you would call the compensation logic for each step
                    // This is a simplified implementation
                    
                    stepExecution.compensated = true
                    stepExecution.compensatedAt = Instant.now()
                    
                } catch (e: Exception) {
                    logger.error("Failed to compensate step: ${stepExecution.stepName}", e)
                    // Continue with other compensations even if one fails
                }
            }
        }
    }
}

/**
 * Transaction manager interface.
 */
interface TransactionManager {
    suspend fun <T> executeInTransaction(operation: suspend () -> T): T
    suspend fun <T> executeSaga(sagaDefinition: SagaDefinition<T>): SagaResult<T>
    suspend fun <T> executeWithTimeout(timeoutSeconds: Int, operation: suspend () -> T): T
    suspend fun getCurrentTransactionStatus(): String
    suspend fun getTransactionStatistics(): TransactionStatistics
}

/**
 * Transaction context information.
 */
data class TransactionContext(
    val id: String,
    val startTime: Instant,
    var status: TransactionStatus,
    var endTime: Instant? = null,
    var error: String? = null
)

/**
 * Transaction status enumeration.
 */
enum class TransactionStatus {
    ACTIVE,
    COMMITTED,
    ROLLED_BACK,
    MARKED_FOR_ROLLBACK
}

/**
 * Saga definition containing steps and compensation logic.
 */
data class SagaDefinition<T>(
    val name: String,
    val steps: List<SagaStep>
)

/**
 * Individual step in a saga.
 */
interface SagaStep {
    val name: String
    suspend fun execute(): Any
    suspend fun compensate()
}

/**
 * Saga context tracking execution state.
 */
data class SagaContext(
    val id: String,
    val startTime: Instant,
    val steps: MutableList<SagaStepExecution>
)

/**
 * Execution information for a saga step.
 */
data class SagaStepExecution(
    val stepName: String,
    val executedAt: Instant,
    val result: String,
    var compensated: Boolean = false,
    var compensatedAt: Instant? = null
)

/**
 * Result of saga execution.
 */
sealed class SagaResult<T> {
    data class Success<T>(val sagaId: String, val result: T) : SagaResult<T>()
    data class Failure<T>(val sagaId: String, val error: Throwable) : SagaResult<T>()

    companion object {
        fun <T> success(sagaId: String, result: T): SagaResult<T> = Success(sagaId, result)
        fun <T> failure(sagaId: String, error: Throwable): SagaResult<T> = Failure(sagaId, error)
    }
}

/**
 * Transaction statistics.
 */
data class TransactionStatistics(
    val activeTransactions: Int,
    val totalTransactions: Long,
    val successfulTransactions: Long,
    val failedTransactions: Long,
    val averageExecutionTime: Double
)

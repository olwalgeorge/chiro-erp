package org.chiro.core_business_service.shared.domain.service

import org.chiro.core_business_service.shared.domain.valueobject.AggregateId
import org.chiro.core_business_service.shared.domain.exception.DomainException
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.time.LocalDateTime

/**
 * Sequence Generation Domain Service - provides unique sequence numbers across all modules.
 * 
 * This service generates sequential numbers for:
 * - Invoice numbers
 * - Order numbers  
 * - Transaction IDs
 * - Receipt numbers
 * - Work order numbers
 * - Batch numbers
 * 
 * Features:
 * - Thread-safe sequence generation
 * - Configurable prefixes and formats
 * - Year/month reset capabilities
 * - Gap-free sequences
 * - Multiple sequence types per module
 */
@ApplicationScoped
class SequenceGenerationService : BaseDomainService() {

    private val logger: Logger = LoggerFactory.getLogger(SequenceGenerationService::class.java)
    
    // Thread-safe storage for sequence counters
    private val sequences = ConcurrentHashMap<String, SequenceCounter>()
    
    /**
     * Get the next sequence number for a given sequence type
     */
    fun getNextSequence(sequenceType: String, prefix: String = "", resetPeriod: ResetPeriod = ResetPeriod.NEVER): String {
        validateOperation()
        
        val sequenceKey = buildSequenceKey(sequenceType, resetPeriod)
        val counter = sequences.computeIfAbsent(sequenceKey) { 
            SequenceCounter(sequenceType, resetPeriod, 0L, getCurrentPeriod(resetPeriod))
        }
        
        val nextNumber = counter.getNext()
        val formattedNumber = formatSequenceNumber(prefix, nextNumber, resetPeriod)
        
        logger.debug("Generated sequence number: $formattedNumber for type: $sequenceType")
        
        return formattedNumber
    }
    
    /**
     * Get the current sequence number without incrementing
     */
    fun getCurrentSequence(sequenceType: String, resetPeriod: ResetPeriod = ResetPeriod.NEVER): Long {
        val sequenceKey = buildSequenceKey(sequenceType, resetPeriod)
        return sequences[sequenceKey]?.current ?: 0L
    }
    
    /**
     * Reset a sequence to start from a specific number
     */
    fun resetSequence(sequenceType: String, startFrom: Long = 1L, resetPeriod: ResetPeriod = ResetPeriod.NEVER) {
        require(startFrom >= 0) { "Start value must be non-negative" }
        
        val sequenceKey = buildSequenceKey(sequenceType, resetPeriod)
        val counter = SequenceCounter(sequenceType, resetPeriod, startFrom - 1, getCurrentPeriod(resetPeriod))
        sequences[sequenceKey] = counter
        
        logger.info("Reset sequence $sequenceType to start from $startFrom")
    }
    
    /**
     * Get formatted invoice number
     */
    fun getNextInvoiceNumber(year: Int = LocalDateTime.now().year): String {
        return getNextSequence("INVOICE", "INV-$year-", ResetPeriod.YEARLY)
    }
    
    /**
     * Get formatted order number
     */
    fun getNextOrderNumber(): String {
        return getNextSequence("ORDER", "ORD-", ResetPeriod.NEVER)
    }
    
    /**
     * Get formatted receipt number
     */
    fun getNextReceiptNumber(): String {
        return getNextSequence("RECEIPT", "REC-", ResetPeriod.DAILY)
    }
    
    /**
     * Get formatted work order number
     */
    fun getNextWorkOrderNumber(): String {
        return getNextSequence("WORK_ORDER", "WO-", ResetPeriod.NEVER)
    }
    
    /**
     * Get formatted batch number
     */
    fun getNextBatchNumber(): String {
        val now = LocalDateTime.now()
        val yearMonth = "${now.year}${now.monthValue.toString().padStart(2, '0')}"
        return getNextSequence("BATCH", "BATCH-$yearMonth-", ResetPeriod.MONTHLY)
    }
    
    private fun buildSequenceKey(sequenceType: String, resetPeriod: ResetPeriod): String {
        return when (resetPeriod) {
            ResetPeriod.NEVER -> sequenceType
            ResetPeriod.DAILY -> "$sequenceType-${LocalDateTime.now().toLocalDate()}"
            ResetPeriod.MONTHLY -> {
                val now = LocalDateTime.now()
                "$sequenceType-${now.year}-${now.monthValue}"
            }
            ResetPeriod.YEARLY -> "$sequenceType-${LocalDateTime.now().year}"
        }
    }
    
    private fun getCurrentPeriod(resetPeriod: ResetPeriod): String {
        val now = LocalDateTime.now()
        return when (resetPeriod) {
            ResetPeriod.NEVER -> "NEVER"
            ResetPeriod.DAILY -> now.toLocalDate().toString()
            ResetPeriod.MONTHLY -> "${now.year}-${now.monthValue}"
            ResetPeriod.YEARLY -> now.year.toString()
        }
    }
    
    private fun formatSequenceNumber(prefix: String, number: Long, resetPeriod: ResetPeriod): String {
        val paddedNumber = when (resetPeriod) {
            ResetPeriod.NEVER -> number.toString().padStart(8, '0')
            ResetPeriod.YEARLY -> number.toString().padStart(6, '0')
            ResetPeriod.MONTHLY -> number.toString().padStart(4, '0')
            ResetPeriod.DAILY -> number.toString().padStart(3, '0')
        }
        
        return "$prefix$paddedNumber"
    }
    
    override fun validateOperation() {
        // Validate that the service is in a valid state
    }
}

/**
 * Thread-safe sequence counter
 */
private class SequenceCounter(
    val sequenceType: String,
    val resetPeriod: ResetPeriod,
    private var _current: Long,
    private var _period: String
) {
    
    @Synchronized
    fun getNext(): Long {
        val currentPeriod = getCurrentPeriod(resetPeriod)
        
        // Check if we need to reset due to period change
        if (resetPeriod != ResetPeriod.NEVER && currentPeriod != _period) {
            _current = 0L
            _period = currentPeriod
        }
        
        return ++_current
    }
    
    val current: Long
        @Synchronized get() = _current
    
    private fun getCurrentPeriod(resetPeriod: ResetPeriod): String {
        val now = LocalDateTime.now()
        return when (resetPeriod) {
            ResetPeriod.NEVER -> "NEVER"
            ResetPeriod.DAILY -> now.toLocalDate().toString()
            ResetPeriod.MONTHLY -> "${now.year}-${now.monthValue}"
            ResetPeriod.YEARLY -> now.year.toString()
        }
    }
}

/**
 * Defines when sequences should reset
 */
enum class ResetPeriod {
    NEVER,      // Sequence never resets
    DAILY,      // Reset every day
    MONTHLY,    // Reset every month
    YEARLY      // Reset every year
}

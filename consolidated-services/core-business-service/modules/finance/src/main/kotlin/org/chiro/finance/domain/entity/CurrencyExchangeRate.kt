package org.chiro.finance.domain.entity

import org.chiro.finance.domain.valueobject.*
import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 * Currency Exchange Rate - Multi-currency support and conversion
 * 
 * Manages currency exchange rates for international operations with:
 * - Real-time and historical exchange rate tracking
 * - Multiple rate types (spot, forward, average)
 * - Automatic rate updates and validation
 * - Currency conversion calculations
 * - Rate variance analysis and alerts
 * 
 * Key Features:
 * - Multi-source rate aggregation
 * - Historical rate preservation
 * - Automatic rate expiration
 * - Rate validation and alerts
 * - Conversion accuracy tracking
 * 
 * Business Rules:
 * - Exchange rates must be positive
 * - Base currency rates are always 1.0
 * - Historical rates are immutable
 * - Rate changes require audit trail
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Serializable
data class CurrencyExchangeRate(
    val id: UUID = UUID.randomUUID(),
    
    @field:NotNull(message = "From currency is required")
    val fromCurrency: Currency,
    
    @field:NotNull(message = "To currency is required")
    val toCurrency: Currency,
    
    @field:NotNull(message = "Exchange rate is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Exchange rate must be positive")
    val rate: Double,
    
    @field:NotNull(message = "Rate type is required")
    val rateType: ExchangeRateType = ExchangeRateType.SPOT,
    
    @field:NotNull(message = "Effective date is required")
    val effectiveDate: LocalDateTime,
    
    val expirationDate: LocalDateTime? = null,
    
    @field:NotNull(message = "Status is required")
    val status: ExchangeRateStatus = ExchangeRateStatus.ACTIVE,
    
    @field:Size(max = 100, message = "Source cannot exceed 100 characters")
    val source: String? = null, // Rate provider (e.g., "Central Bank", "Reuters", "Manual")
    
    val bidRate: Double? = null,
    val askRate: Double? = null,
    val midRate: Double? = null,
    
    val previousRate: Double? = null,
    val rateVariance: Double? = null,
    val variancePercentage: Double? = null,
    
    @field:Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    val notes: String? = null,
    
    @field:NotNull(message = "Created by is required")
    val createdBy: UUID,
    
    @field:NotNull(message = "Created date is required")
    val createdDate: LocalDateTime = LocalDateTime.now(),
    
    val modifiedBy: UUID? = null,
    val modifiedDate: LocalDateTime? = null,
    
    val approvedBy: UUID? = null,
    val approvedDate: LocalDateTime? = null,
    
    val isSystemGenerated: Boolean = false,
    val isManualOverride: Boolean = false,
    
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(rate > 0.0) { "Exchange rate must be positive" }
        require(fromCurrency != toCurrency) { "From and to currencies must be different" }
        
        // Validate bid/ask rates if provided
        if (bidRate != null && askRate != null) {
            require(bidRate <= askRate) { "Bid rate cannot exceed ask rate" }
            require(midRate == null || (midRate >= bidRate && midRate <= askRate)) {
                "Mid rate must be between bid and ask rates"
            }
        }
        
        // Validate date ranges
        if (expirationDate != null) {
            require(expirationDate.isAfter(effectiveDate)) {
                "Expiration date must be after effective date"
            }
        }
        
        // Calculate variance if previous rate is provided
        if (previousRate != null && previousRate > 0) {
            val calculatedVariance = rate - previousRate
            val calculatedVariancePercentage = (calculatedVariance / previousRate) * 100.0
            
            // Validate calculated values match provided values (with tolerance)
            if (rateVariance != null) {
                require(kotlin.math.abs(rateVariance - calculatedVariance) < 0.000001) {
                    "Rate variance calculation mismatch"
                }
            }
            if (variancePercentage != null) {
                require(kotlin.math.abs(variancePercentage - calculatedVariancePercentage) < 0.0001) {
                    "Variance percentage calculation mismatch"
                }
            }
        }
    }
    
    /**
     * Converts amount from source currency to target currency
     */
    fun convert(amount: FinancialAmount): FinancialAmount {
        require(amount.currency == fromCurrency) {
            "Amount currency (${amount.currency}) must match from currency ($fromCurrency)"
        }
        require(status == ExchangeRateStatus.ACTIVE) {
            "Cannot use inactive exchange rate for conversion"
        }
        
        val convertedAmount = amount.amount * rate
        return FinancialAmount(
            amount = convertedAmount,
            currency = toCurrency
        )
    }
    
    /**
     * Converts amount in reverse direction (to -> from)
     */
    fun convertReverse(amount: FinancialAmount): FinancialAmount {
        require(amount.currency == toCurrency) {
            "Amount currency (${amount.currency}) must match to currency ($toCurrency)"
        }
        require(status == ExchangeRateStatus.ACTIVE) {
            "Cannot use inactive exchange rate for conversion"
        }
        require(rate != 0.0) { "Cannot perform reverse conversion with zero rate" }
        
        val convertedAmount = amount.amount / rate
        return FinancialAmount(
            amount = convertedAmount,
            currency = fromCurrency
        )
    }
    
    /**
     * Gets the inverse exchange rate
     */
    fun getInverseRate(): CurrencyExchangeRate {
        return copy(
            id = UUID.randomUUID(),
            fromCurrency = toCurrency,
            toCurrency = fromCurrency,
            rate = 1.0 / rate,
            bidRate = askRate?.let { 1.0 / it },
            askRate = bidRate?.let { 1.0 / it },
            midRate = midRate?.let { 1.0 / it },
            previousRate = previousRate?.let { 1.0 / it },
            createdDate = LocalDateTime.now()
        )
    }
    
    /**
     * Updates exchange rate with variance tracking
     */
    fun updateRate(
        newRate: Double,
        updatedBy: UUID,
        source: String? = null,
        notes: String? = null
    ): CurrencyExchangeRate {
        require(newRate > 0.0) { "New exchange rate must be positive" }
        
        val variance = newRate - rate
        val variancePercentage = (variance / rate) * 100.0
        
        return copy(
            rate = newRate,
            previousRate = rate,
            rateVariance = variance,
            variancePercentage = variancePercentage,
            source = source ?: this.source,
            modifiedBy = updatedBy,
            modifiedDate = LocalDateTime.now(),
            notes = notes?.let { existingNotes ->
                if (this.notes.isNullOrBlank()) it
                else "${this.notes}\n$it"
            } ?: this.notes
        )
    }
    
    /**
     * Updates bid/ask spread
     */
    fun updateBidAskSpread(
        bidRate: Double,
        askRate: Double,
        updatedBy: UUID
    ): CurrencyExchangeRate {
        require(bidRate > 0.0) { "Bid rate must be positive" }
        require(askRate > 0.0) { "Ask rate must be positive" }
        require(bidRate <= askRate) { "Bid rate cannot exceed ask rate" }
        
        val midRate = (bidRate + askRate) / 2.0
        
        return copy(
            bidRate = bidRate,
            askRate = askRate,
            midRate = midRate,
            rate = midRate, // Update main rate to mid-point
            modifiedBy = updatedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Expires the exchange rate
     */
    fun expire(expiredBy: UUID, reason: String? = null): CurrencyExchangeRate {
        return copy(
            status = ExchangeRateStatus.EXPIRED,
            expirationDate = LocalDateTime.now(),
            modifiedBy = expiredBy,
            modifiedDate = LocalDateTime.now(),
            notes = reason?.let { existingNotes ->
                if (this.notes.isNullOrBlank()) "Expired: $it"
                else "${this.notes}\nExpired: $it"
            } ?: this.notes
        )
    }
    
    /**
     * Approves manual exchange rate
     */
    fun approve(approvedBy: UUID): CurrencyExchangeRate {
        require(isManualOverride) { "Only manual rates require approval" }
        require(status == ExchangeRateStatus.PENDING_APPROVAL) {
            "Rate must be pending approval"
        }
        
        return copy(
            status = ExchangeRateStatus.ACTIVE,
            approvedBy = approvedBy,
            approvedDate = LocalDateTime.now(),
            modifiedBy = approvedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Checks if rate is within acceptable variance threshold
     */
    fun isWithinVarianceThreshold(thresholdPercentage: Double): Boolean {
        return if (variancePercentage != null) {
            kotlin.math.abs(variancePercentage) <= thresholdPercentage
        } else true
    }
    
    /**
     * Gets the spread percentage (bid-ask spread)
     */
    fun getSpreadPercentage(): Double? {
        return if (bidRate != null && askRate != null && midRate != null && midRate > 0) {
            ((askRate - bidRate) / midRate) * 100.0
        } else null
    }
    
    /**
     * Validates exchange rate for business rules
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (rate <= 0) {
            errors.add("Exchange rate must be positive")
        }
        
        if (fromCurrency == toCurrency) {
            errors.add("From and to currencies must be different")
        }
        
        if (bidRate != null && askRate != null && bidRate > askRate) {
            errors.add("Bid rate cannot exceed ask rate")
        }
        
        if (midRate != null && bidRate != null && askRate != null) {
            if (midRate < bidRate || midRate > askRate) {
                errors.add("Mid rate must be between bid and ask rates")
            }
        }
        
        if (expirationDate != null && !expirationDate.isAfter(effectiveDate)) {
            errors.add("Expiration date must be after effective date")
        }
        
        return errors
    }
    
    companion object {
        /**
         * Creates a new exchange rate
         */
        fun create(
            fromCurrency: Currency,
            toCurrency: Currency,
            rate: Double,
            rateType: ExchangeRateType,
            createdBy: UUID,
            source: String? = null,
            effectiveDate: LocalDateTime = LocalDateTime.now()
        ): CurrencyExchangeRate {
            return CurrencyExchangeRate(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rate = rate,
                rateType = rateType,
                createdBy = createdBy,
                source = source,
                effectiveDate = effectiveDate
            )
        }
        
        /**
         * Creates manual exchange rate requiring approval
         */
        fun createManual(
            fromCurrency: Currency,
            toCurrency: Currency,
            rate: Double,
            createdBy: UUID,
            notes: String? = null
        ): CurrencyExchangeRate {
            return CurrencyExchangeRate(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rate = rate,
                rateType = ExchangeRateType.MANUAL,
                createdBy = createdBy,
                notes = notes,
                effectiveDate = LocalDateTime.now(),
                status = ExchangeRateStatus.PENDING_APPROVAL,
                isManualOverride = true
            )
        }
        
        /**
         * Creates system-generated rate from external source
         */
        fun createSystem(
            fromCurrency: Currency,
            toCurrency: Currency,
            rate: Double,
            source: String,
            bidRate: Double? = null,
            askRate: Double? = null
        ): CurrencyExchangeRate {
            val midRate = if (bidRate != null && askRate != null) {
                (bidRate + askRate) / 2.0
            } else rate
            
            return CurrencyExchangeRate(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rate = midRate,
                rateType = ExchangeRateType.SPOT,
                createdBy = UUID.fromString("00000000-0000-0000-0000-000000000000"), // System user
                source = source,
                effectiveDate = LocalDateTime.now(),
                bidRate = bidRate,
                askRate = askRate,
                midRate = midRate,
                isSystemGenerated = true
            )
        }
        
        /**
         * Creates base currency rate (always 1.0)
         */
        fun createBaseCurrencyRate(baseCurrency: Currency): CurrencyExchangeRate {
            return CurrencyExchangeRate(
                fromCurrency = baseCurrency,
                toCurrency = baseCurrency,
                rate = 1.0,
                rateType = ExchangeRateType.BASE,
                createdBy = UUID.fromString("00000000-0000-0000-0000-000000000000"), // System user
                source = "System",
                effectiveDate = LocalDateTime.now(),
                isSystemGenerated = true
            )
        }
    }
}

/**
 * Exchange rate types
 */
@Serializable
enum class ExchangeRateType(val description: String) {
    SPOT("Spot Rate"),
    FORWARD("Forward Rate"),
    AVERAGE("Average Rate"),
    HISTORICAL("Historical Rate"),
    BUDGET("Budget Rate"),
    MANUAL("Manual Override"),
    BASE("Base Currency Rate");
}

/**
 * Exchange rate status
 */
@Serializable
enum class ExchangeRateStatus(val description: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    EXPIRED("Expired"),
    PENDING_APPROVAL("Pending Approval"),
    REJECTED("Rejected");
    
    val isUsable: Boolean
        get() = this == ACTIVE
}

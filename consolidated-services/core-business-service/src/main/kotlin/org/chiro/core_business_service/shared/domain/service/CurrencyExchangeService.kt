package org.chiro.core_business_service.shared.domain.service

import org.chiro.core_business_service.shared.domain.valueobject.Money
import org.chiro.core_business_service.shared.domain.exception.DomainException
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

/**
 * Currency Exchange Domain Service - handles currency conversion across all modules.
 * 
 * This service provides:
 * - Real-time currency conversion
 * - Historical exchange rates
 * - Multi-currency calculations
 * - Currency validation
 * 
 * Used by all modules that deal with international transactions:
 * - Finance: Multi-currency accounting
 * - Sales: International orders
 * - Procurement: Foreign vendor payments
 * - HR: International payroll
 */
@ApplicationScoped
class CurrencyExchangeService : BaseDomainService() {

    private val logger: Logger = LoggerFactory.getLogger(CurrencyExchangeService::class.java)
    
    // In production, this would be injected from infrastructure
    private val exchangeRates = mutableMapOf<ExchangeRateKey, BigDecimal>()
    
    init {
        // Initialize with some common exchange rates for demo
        // In production, this would come from external service
        initializeDefaultRates()
    }
    
    /**
     * Convert money from one currency to another
     */
    fun convert(money: Money, targetCurrencyCode: String, exchangeDate: LocalDate = LocalDate.now()): Money {
        validateOperation()
        
        if (money.currencyCode == targetCurrencyCode) {
            return money
        }
        
        val exchangeRate = getExchangeRate(money.currencyCode, targetCurrencyCode, exchangeDate)
        val convertedAmount = money.amount.multiply(exchangeRate, Money.FINANCIAL_CONTEXT)
        
        logger.debug("Converted ${money.amount} ${money.currencyCode} to $convertedAmount $targetCurrencyCode at rate $exchangeRate")
        
        return Money(convertedAmount, targetCurrencyCode)
    }
    
    /**
     * Get exchange rate between two currencies
     */
    fun getExchangeRate(fromCurrency: String, toCurrency: String, exchangeDate: LocalDate = LocalDate.now()): BigDecimal {
        validateCurrency(fromCurrency)
        validateCurrency(toCurrency)
        
        if (fromCurrency == toCurrency) {
            return BigDecimal.ONE
        }
        
        val key = ExchangeRateKey(fromCurrency, toCurrency, exchangeDate)
        val rate = exchangeRates[key]
        
        if (rate != null) {
            return rate
        }
        
        // Try reverse rate (1 / reverse rate)
        val reverseKey = ExchangeRateKey(toCurrency, fromCurrency, exchangeDate)
        val reverseRate = exchangeRates[reverseKey]
        
        if (reverseRate != null) {
            return BigDecimal.ONE.divide(reverseRate, Money.FINANCIAL_CONTEXT)
        }
        
        // Try via USD as intermediate currency
        if (fromCurrency != "USD" && toCurrency != "USD") {
            val fromUsdRate = getExchangeRate(fromCurrency, "USD", exchangeDate)
            val toUsdRate = getExchangeRate("USD", toCurrency, exchangeDate)
            return fromUsdRate.multiply(toUsdRate, Money.FINANCIAL_CONTEXT)
        }
        
        throw ExchangeRateNotFoundException(fromCurrency, toCurrency, exchangeDate)
    }
    
    /**
     * Check if currency is supported
     */
    fun isCurrencySupported(currencyCode: String): Boolean {
        return try {
            Currency.getInstance(currencyCode)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    /**
     * Get all supported currencies
     */
    fun getSupportedCurrencies(): Set<String> {
        return Currency.getAvailableCurrencies().map { it.currencyCode }.toSet()
    }
    
    /**
     * Update exchange rate (typically called by infrastructure layer)
     */
    fun updateExchangeRate(fromCurrency: String, toCurrency: String, rate: BigDecimal, effectiveDate: LocalDate = LocalDate.now()) {
        validateCurrency(fromCurrency)
        validateCurrency(toCurrency)
        require(rate > BigDecimal.ZERO) { "Exchange rate must be positive" }
        
        val key = ExchangeRateKey(fromCurrency, toCurrency, effectiveDate)
        exchangeRates[key] = rate
        
        logger.info("Updated exchange rate: $fromCurrency -> $toCurrency = $rate (effective: $effectiveDate)")
    }
    
    /**
     * Calculate cross-rate between two currencies via USD
     */
    fun calculateCrossRate(fromCurrency: String, toCurrency: String, exchangeDate: LocalDate = LocalDate.now()): BigDecimal {
        if (fromCurrency == toCurrency) return BigDecimal.ONE
        
        val fromUsdRate = getExchangeRate(fromCurrency, "USD", exchangeDate)
        val usdToRate = getExchangeRate("USD", toCurrency, exchangeDate)
        
        return fromUsdRate.multiply(usdToRate, Money.FINANCIAL_CONTEXT)
    }
    
    override fun validateOperation() {
        // Add any global validation logic here
    }
    
    private fun validateCurrency(currencyCode: String) {
        require(currencyCode.isNotBlank()) { "Currency code cannot be blank" }
        require(currencyCode.length == 3) { "Currency code must be 3 characters: $currencyCode" }
        require(isCurrencySupported(currencyCode)) { "Unsupported currency: $currencyCode" }
    }
    
    private fun initializeDefaultRates() {
        val today = LocalDate.now()
        
        // USD as base currency
        updateExchangeRate("USD", "EUR", BigDecimal("0.85"), today)
        updateExchangeRate("USD", "GBP", BigDecimal("0.73"), today)
        updateExchangeRate("USD", "CAD", BigDecimal("1.25"), today)
        updateExchangeRate("USD", "AUD", BigDecimal("1.35"), today)
        updateExchangeRate("USD", "JPY", BigDecimal("110.00"), today)
        updateExchangeRate("USD", "CHF", BigDecimal("0.92"), today)
        updateExchangeRate("USD", "CNY", BigDecimal("6.45"), today)
        updateExchangeRate("USD", "INR", BigDecimal("74.50"), today)
        
        logger.info("Initialized default exchange rates for ${exchangeRates.size} currency pairs")
    }
}

/**
 * Key for exchange rate lookup
 */
private data class ExchangeRateKey(
    val fromCurrency: String,
    val toCurrency: String,
    val exchangeDate: LocalDate
)

/**
 * Exception thrown when exchange rate is not found
 */
class ExchangeRateNotFoundException(
    fromCurrency: String,
    toCurrency: String,
    exchangeDate: LocalDate
) : DomainException("Exchange rate not found: $fromCurrency -> $toCurrency for date $exchangeDate")

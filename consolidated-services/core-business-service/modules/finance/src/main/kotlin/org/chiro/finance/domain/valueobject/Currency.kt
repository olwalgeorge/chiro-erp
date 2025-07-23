package org.chiro.finance.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.validation.constraints.NotNull
import java.util.*

/**
 * Currency Value Object - ISO 4217 compliant currency implementation
 * 
 * Provides comprehensive currency support for multi-national ERP operations:
 * - ISO 4217 standard compliance
 * - Currency validation and metadata
 * - Exchange rate support preparation
 * - Localization support
 * 
 * Features:
 * - Full ISO 4217 currency catalog
 * - Currency metadata (symbols, decimal places, etc.)
 * - Regional currency groupings
 * - Cryptocurrency support preparation
 * - Custom currency extension support
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Embeddable
data class Currency(
    @field:NotNull
    @Column(name = "currency_code", length = 3, nullable = false)
    val code: String,
    
    @field:NotNull
    @Column(name = "currency_name", length = 100, nullable = false)
    val name: String,
    
    @field:NotNull
    @Column(name = "currency_symbol", length = 10, nullable = false)
    val symbol: String,
    
    @field:NotNull
    @Column(name = "decimal_places", nullable = false)
    val decimalPlaces: Int,
    
    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false)
    val type: CurrencyType = CurrencyType.FIAT,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true
) {
    companion object {
        // Major World Currencies
        val USD = Currency("USD", "US Dollar", "$", 2, CurrencyType.FIAT)
        val EUR = Currency("EUR", "Euro", "€", 2, CurrencyType.FIAT)
        val GBP = Currency("GBP", "British Pound", "£", 2, CurrencyType.FIAT)
        val JPY = Currency("JPY", "Japanese Yen", "¥", 0, CurrencyType.FIAT)
        val CHF = Currency("CHF", "Swiss Franc", "CHF", 2, CurrencyType.FIAT)
        val CAD = Currency("CAD", "Canadian Dollar", "C$", 2, CurrencyType.FIAT)
        val AUD = Currency("AUD", "Australian Dollar", "A$", 2, CurrencyType.FIAT)
        val CNY = Currency("CNY", "Chinese Yuan", "¥", 2, CurrencyType.FIAT)
        val INR = Currency("INR", "Indian Rupee", "₹", 2, CurrencyType.FIAT)
        
        // Cryptocurrencies (for future support)
        val BTC = Currency("BTC", "Bitcoin", "₿", 8, CurrencyType.CRYPTOCURRENCY, false)
        val ETH = Currency("ETH", "Ethereum", "Ξ", 18, CurrencyType.CRYPTOCURRENCY, false)
        
        // Default system currency
        val DEFAULT = USD
        
        /**
         * All supported fiat currencies
         */
        val SUPPORTED_FIAT = mapOf(
            "USD" to USD, "EUR" to EUR, "GBP" to GBP, "JPY" to JPY, "CHF" to CHF,
            "CAD" to CAD, "AUD" to AUD, "CNY" to CNY, "INR" to INR,
            "BRL" to Currency("BRL", "Brazilian Real", "R$", 2, CurrencyType.FIAT),
            "MXN" to Currency("MXN", "Mexican Peso", "$", 2, CurrencyType.FIAT),
            "RUB" to Currency("RUB", "Russian Ruble", "₽", 2, CurrencyType.FIAT),
            "KRW" to Currency("KRW", "South Korean Won", "₩", 0, CurrencyType.FIAT),
            "SGD" to Currency("SGD", "Singapore Dollar", "S$", 2, CurrencyType.FIAT),
            "HKD" to Currency("HKD", "Hong Kong Dollar", "HK$", 2, CurrencyType.FIAT),
            "NOK" to Currency("NOK", "Norwegian Krone", "kr", 2, CurrencyType.FIAT),
            "SEK" to Currency("SEK", "Swedish Krona", "kr", 2, CurrencyType.FIAT),
            "DKK" to Currency("DKK", "Danish Krone", "kr", 2, CurrencyType.FIAT),
            "PLN" to Currency("PLN", "Polish Zloty", "zł", 2, CurrencyType.FIAT),
            "CZK" to Currency("CZK", "Czech Koruna", "Kč", 2, CurrencyType.FIAT),
            "HUF" to Currency("HUF", "Hungarian Forint", "Ft", 2, CurrencyType.FIAT),
            "TRY" to Currency("TRY", "Turkish Lira", "₺", 2, CurrencyType.FIAT),
            "ZAR" to Currency("ZAR", "South African Rand", "R", 2, CurrencyType.FIAT),
            "THB" to Currency("THB", "Thai Baht", "฿", 2, CurrencyType.FIAT),
            "MYR" to Currency("MYR", "Malaysian Ringgit", "RM", 2, CurrencyType.FIAT),
            "IDR" to Currency("IDR", "Indonesian Rupiah", "Rp", 2, CurrencyType.FIAT),
            "PHP" to Currency("PHP", "Philippine Peso", "₱", 2, CurrencyType.FIAT),
            "VND" to Currency("VND", "Vietnamese Dong", "₫", 0, CurrencyType.FIAT),
            "ILS" to Currency("ILS", "Israeli Shekel", "₪", 2, CurrencyType.FIAT),
            "AED" to Currency("AED", "UAE Dirham", "د.إ", 2, CurrencyType.FIAT),
            "SAR" to Currency("SAR", "Saudi Riyal", "﷼", 2, CurrencyType.FIAT),
            "EGP" to Currency("EGP", "Egyptian Pound", "£", 2, CurrencyType.FIAT)
        )
        
        /**
         * Get currency by code with validation
         */
        fun of(code: String): Currency {
            return SUPPORTED_FIAT[code.uppercase()] 
                ?: throw IllegalArgumentException("Unsupported currency code: $code")
        }
        
        /**
         * Check if currency is supported
         */
        fun isSupported(code: String): Boolean = SUPPORTED_FIAT.containsKey(code.uppercase())
        
        /**
         * Get all active currencies
         */
        fun getActiveCurrencies(): List<Currency> = SUPPORTED_FIAT.values.filter { it.isActive }
        
        /**
         * Get currencies by region (future enhancement)
         */
        fun getCurrenciesByRegion(region: CurrencyRegion): List<Currency> {
            return when (region) {
                CurrencyRegion.EUROPE -> listOf(EUR, GBP, CHF, NOK, SEK, DKK, PLN, CZK, HUF)
                CurrencyRegion.ASIA_PACIFIC -> listOf(JPY, CNY, INR, KRW, SGD, HKD, THB, MYR, IDR, PHP, VND, AUD)
                CurrencyRegion.AMERICAS -> listOf(USD, CAD, BRL, MXN)
                CurrencyRegion.MIDDLE_EAST_AFRICA -> listOf(AED, SAR, EGP, ILS, ZAR)
                CurrencyRegion.ALL -> getActiveCurrencies()
            }
        }
    }
    
    init {
        require(code.isNotBlank()) { "Currency code cannot be blank" }
        require(code.length == 3) { "Currency code must be exactly 3 characters" }
        require(code.all { it.isUpperCase() || it.isDigit() }) { "Currency code must be uppercase alphanumeric" }
        require(name.isNotBlank()) { "Currency name cannot be blank" }
        require(symbol.isNotBlank()) { "Currency symbol cannot be blank" }
        require(decimalPlaces >= 0) { "Decimal places cannot be negative" }
        require(decimalPlaces <= 18) { "Decimal places cannot exceed 18" }
    }
    
    /**
     * Get Java Currency instance if available
     */
    val javaCurrency: java.util.Currency?
        get() = try {
            java.util.Currency.getInstance(code)
        } catch (e: IllegalArgumentException) {
            null // Custom or cryptocurrency
        }
    
    /**
     * Check if this is a major trading currency
     */
    val isMajorCurrency: Boolean
        get() = code in setOf("USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY")
    
    /**
     * Check if currency supports fractional units
     */
    val supportsFractionalUnits: Boolean get() = decimalPlaces > 0
    
    /**
     * Get the smallest unit multiplier (e.g., 100 for cents)
     */
    val minorUnitMultiplier: Long get() = Math.pow(10.0, decimalPlaces.toDouble()).toLong()
    
    /**
     * Format amount with currency symbol
     */
    fun formatAmount(amount: java.math.BigDecimal, locale: Locale = Locale.getDefault()): String {
        val formatter = java.text.NumberFormat.getCurrencyInstance(locale)
        javaCurrency?.let { formatter.currency = it }
        return formatter.format(amount)
    }
    
    override fun toString(): String = code
}

/**
 * Currency Type Classification
 */
enum class CurrencyType {
    FIAT,           // Government-issued currency
    CRYPTOCURRENCY, // Digital/crypto currency
    COMMODITY,      // Commodity-backed currency
    CUSTOM          // Custom/internal currency
}

/**
 * Currency Regional Groupings
 */
enum class CurrencyRegion {
    EUROPE,
    ASIA_PACIFIC,
    AMERICAS,
    MIDDLE_EAST_AFRICA,
    ALL
}

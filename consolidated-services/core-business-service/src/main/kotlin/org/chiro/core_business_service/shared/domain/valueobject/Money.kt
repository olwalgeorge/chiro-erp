package org.chiro.core_business_service.shared.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale
import java.text.NumberFormat

/**
 * Shared Money Value Object for cross-module monetary operations.
 * 
 * This is the **core shared money type** used across all ERP modules for:
 * - Integration events that contain monetary amounts
 * - Cross-module financial calculations
 * - Shared financial reporting and analytics
 * - Transaction coordination between modules
 * 
 * Design Pattern: Value Object (immutable, thread-safe)
 * Usage: Shared Kernel across all bounded contexts
 * 
 * Note: Individual modules may have their own specialized money types,
 * but this shared type ensures interoperability and consistency.
 */
@Embeddable
data class Money(
    @field:NotNull
    @field:DecimalMin(value = "-999999999999999.9999", message = "Amount exceeds minimum allowed value")
    @field:DecimalMax(value = "999999999999999.9999", message = "Amount exceeds maximum allowed value")
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    val amount: BigDecimal,
    
    @field:NotNull
    @Column(name = "currency_code", length = 3, nullable = false)
    val currencyCode: String = DEFAULT_CURRENCY
) : Comparable<Money> {
    
    companion object {
        /** Default system currency */
        const val DEFAULT_CURRENCY = "USD"
        
        /** Mathematical context for financial calculations */
        val FINANCIAL_CONTEXT = MathContext(19, RoundingMode.HALF_EVEN)
        
        /** Supported major currencies */
        private val SUPPORTED_CURRENCIES = setOf(
            "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "SEK", "NOK", "DKK",
            "PLN", "CZK", "HUF", "RUB", "CNY", "INR", "BRL", "MXN", "ZAR", "KRW", "SGD"
        )
        
        /**
         * Creates zero amount in specified currency
         */
        fun zero(currencyCode: String = DEFAULT_CURRENCY): Money =
            Money(BigDecimal.ZERO, currencyCode)
        
        /**
         * Creates Money from string amount
         */
        fun of(amount: String, currencyCode: String = DEFAULT_CURRENCY): Money =
            Money(BigDecimal(amount), currencyCode)
        
        /**
         * Creates Money from numeric amount
         */
        fun of(amount: Double, currencyCode: String = DEFAULT_CURRENCY): Money =
            Money(BigDecimal.valueOf(amount), currencyCode)
    }
    
    init {
        require(currencyCode.isNotBlank()) { "Currency code cannot be blank" }
        require(currencyCode.length == 3) { "Currency code must be exactly 3 characters" }
        require(currencyCode.all { it.isUpperCase() }) { "Currency code must be uppercase" }
        require(currencyCode in SUPPORTED_CURRENCIES) { "Unsupported currency: $currencyCode" }
        require(amount.scale() <= 4) { "Amount precision cannot exceed 4 decimal places" }
    }
    
    /** ISO Currency instance */
    val currency: Currency get() = Currency.getInstance(currencyCode)
    
    /** Amount in smallest currency unit (cents, pence, etc.) */
    val minorUnits: Long get() = amount.movePointRight(currency.defaultFractionDigits).longValueExact()
    
    // ===================== KOTLIN PROPERTIES =====================
    
    val isZero: Boolean get() = amount.compareTo(BigDecimal.ZERO) == 0
    val isPositive: Boolean get() = amount.compareTo(BigDecimal.ZERO) > 0
    val isNegative: Boolean get() = amount.compareTo(BigDecimal.ZERO) < 0
    
    // ===================== KOTLIN OPERATORS =====================
    
    operator fun plus(other: Money): Money = add(other)
    operator fun minus(other: Money): Money = subtract(other)
    operator fun times(factor: BigDecimal): Money = multiply(factor)
    operator fun times(factor: Double): Money = multiply(BigDecimal.valueOf(factor))
    operator fun unaryMinus(): Money = negate()
    
    // ===================== ARITHMETIC OPERATIONS =====================
    
    /**
     * Adds another Money amount (same currency required)
     */
    fun add(other: Money): Money {
        requireSameCurrency(other)
        return copy(amount = amount.add(other.amount, FINANCIAL_CONTEXT))
    }
    
    /**
     * Subtracts another Money amount (same currency required)
     */
    fun subtract(other: Money): Money {
        requireSameCurrency(other)
        return copy(amount = amount.subtract(other.amount, FINANCIAL_CONTEXT))
    }
    
    /**
     * Multiplies by a factor
     */
    fun multiply(factor: BigDecimal): Money =
        copy(amount = amount.multiply(factor, FINANCIAL_CONTEXT))
    
    /**
     * Divides by a factor
     */
    fun divide(divisor: BigDecimal): Money {
        require(divisor.compareTo(BigDecimal.ZERO) != 0) { "Cannot divide by zero" }
        return copy(amount = amount.divide(divisor, FINANCIAL_CONTEXT))
    }
    
    /**
     * Returns absolute value
     */
    fun abs(): Money = if (isNegative) negate() else this
    
    /**
     * Returns negated amount
     */
    fun negate(): Money = copy(amount = amount.negate())
    
    // ===================== COMPARISON OPERATIONS =====================
    
    fun isGreaterThan(other: Money): Boolean {
        requireSameCurrency(other)
        return amount > other.amount
    }
    
    fun isLessThan(other: Money): Boolean {
        requireSameCurrency(other)
        return amount < other.amount
    }
    
    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return amount.compareTo(other.amount)
    }
    
    // ===================== FORMATTING =====================
    
    /**
     * Formats amount using default locale
     */
    fun format(locale: Locale = Locale.getDefault()): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = currency
        return format.format(amount)
    }
    
    override fun toString(): String = "$amount $currencyCode"
    
    // ===================== UTILITY METHODS =====================
    
    /**
     * Validates currency compatibility
     */
    private fun requireSameCurrency(other: Money) {
        require(currencyCode == other.currencyCode) {
            "Currency mismatch: Cannot perform operation between $currencyCode and ${other.currencyCode}"
        }
    }
}

// ===================== KOTLIN DSL EXTENSIONS =====================

/** Extension property to create USD Money from String */
val String.USD: Money get() = Money.of(this, "USD")

/** Extension property to create EUR Money from String */
val String.EUR: Money get() = Money.of(this, "EUR")

/** Extension property to create GBP Money from String */
val String.GBP: Money get() = Money.of(this, "GBP")

/** Extension property to create USD Money from Double */
val Double.USD: Money get() = Money.of(this, "USD")

/** Extension property to create EUR Money from Double */
val Double.EUR: Money get() = Money.of(this, "EUR")

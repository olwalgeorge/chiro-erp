package org.chiro.finance.domain.valueobject

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.DecimalMin
import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

/**
 * Financial Amount Value Object
 * 
 * Represents monetary amounts with currency and precision handling for financial calculations.
 * This value object ensures proper money arithmetic and currency consistency.
 * 
 * Design Pattern: Value Object (immutable, equality based on value)
 * Domain: Finance
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
data class FinancialAmount(
    @field:NotNull(message = "Amount cannot be null")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    val amount: BigDecimal,
    
    @field:NotNull(message = "Currency cannot be null")
    val currency: Currency,
    
    val scale: Int = currency.getDefaultFractionDigits(),
    val roundingMode: RoundingMode = RoundingMode.HALF_UP
) : Serializable, Comparable<FinancialAmount> {
    
    companion object {
        private const val serialVersionUID = 1L
        
        // ==================== FACTORY METHODS ====================
        
        /**
         * Creates a financial amount from a double value
         */
        fun of(amount: Double, currency: Currency): FinancialAmount {
            return FinancialAmount(
                amount = BigDecimal.valueOf(amount),
                currency = currency
            )
        }
        
        /**
         * Creates a financial amount from a BigDecimal value
         */
        fun of(amount: BigDecimal, currency: Currency): FinancialAmount {
            return FinancialAmount(
                amount = amount,
                currency = currency
            )
        }
        
        /**
         * Creates a financial amount from a string value
         */
        fun of(amount: String, currency: Currency): FinancialAmount {
            return FinancialAmount(
                amount = BigDecimal(amount),
                currency = currency
            )
        }
        
        /**
         * Creates a financial amount from an integer value
         */
        fun of(amount: Int, currency: Currency): FinancialAmount {
            return FinancialAmount(
                amount = BigDecimal(amount),
                currency = currency
            )
        }
        
        /**
         * Creates a financial amount from a long value
         */
        fun of(amount: Long, currency: Currency): FinancialAmount {
            return FinancialAmount(
                amount = BigDecimal(amount),
                currency = currency
            )
        }
        
        /**
         * Creates a zero amount in the specified currency
         */
        fun zero(currency: Currency): FinancialAmount {
            return FinancialAmount(
                amount = BigDecimal.ZERO,
                currency = currency
            )
        }
        
        /**
         * Creates USD amount
         */
        fun usd(amount: BigDecimal): FinancialAmount {
            return FinancialAmount(
                amount = amount,
                currency = Currency.USD
            )
        }
        
        /**
         * Creates EUR amount
         */
        fun eur(amount: BigDecimal): FinancialAmount {
            return FinancialAmount(
                amount = amount,
                currency = Currency.EUR
            )
        }
        
        /**
         * Creates GBP amount
         */
        fun gbp(amount: BigDecimal): FinancialAmount {
            return FinancialAmount(
                amount = amount,
                currency = Currency.GBP
            )
        }
        
        /**
         * Parses a formatted string like "$123.45" or "€1,234.56"
         */
        fun parse(formattedAmount: String, locale: Locale = Locale.getDefault()): FinancialAmount {
            val format = NumberFormat.getCurrencyInstance(locale)
            val number = format.parse(formattedAmount)
            val currency = (format as java.text.DecimalFormat).currency?.let { javaCurrency ->
                Currency.fromJavaCurrency(javaCurrency)
            } ?: Currency.getDefault()
            
            return FinancialAmount(
                amount = BigDecimal(number.toString()),
                currency = currency
            )
        }
    }
    
    // ==================== COMPUTED PROPERTIES ====================
    
    /**
     * The scaled amount with proper decimal places
     */
    val scaledAmount: BigDecimal
        get() = amount.setScale(scale, roundingMode)
    
    /**
     * Check if this amount is zero
     */
    val isZero: Boolean
        get() = scaledAmount.compareTo(BigDecimal.ZERO) == 0
    
    /**
     * Check if this amount is positive
     */
    val isPositive: Boolean
        get() = scaledAmount.compareTo(BigDecimal.ZERO) > 0
    
    /**
     * Check if this amount is negative
     */
    val isNegative: Boolean
        get() = scaledAmount.compareTo(BigDecimal.ZERO) < 0
    
    /**
     * Absolute value of this amount
     */
    val absolute: FinancialAmount
        get() = if (isNegative) {
            FinancialAmount(scaledAmount.abs(), currency, scale, roundingMode)
        } else {
            this
        }
    
    /**
     * Negated value of this amount
     */
    val negated: FinancialAmount
        get() = FinancialAmount(scaledAmount.negate(), currency, scale, roundingMode)
    
    // ==================== ARITHMETIC OPERATIONS ====================
    
    /**
     * Adds another financial amount (must be same currency)
     */
    fun add(other: FinancialAmount): FinancialAmount {
        requireSameCurrency(other)
        return FinancialAmount(
            amount = scaledAmount.add(other.scaledAmount),
            currency = currency,
            scale = maxOf(scale, other.scale),
            roundingMode = roundingMode
        )
    }
    
    /**
     * Subtracts another financial amount (must be same currency)
     */
    fun subtract(other: FinancialAmount): FinancialAmount {
        requireSameCurrency(other)
        return FinancialAmount(
            amount = scaledAmount.subtract(other.scaledAmount),
            currency = currency,
            scale = maxOf(scale, other.scale),
            roundingMode = roundingMode
        )
    }
    
    /**
     * Multiplies by a factor
     */
    fun multiply(factor: BigDecimal): FinancialAmount {
        return FinancialAmount(
            amount = scaledAmount.multiply(factor),
            currency = currency,
            scale = scale,
            roundingMode = roundingMode
        )
    }
    
    /**
     * Multiplies by a factor (double)
     */
    fun multiply(factor: Double): FinancialAmount {
        return multiply(BigDecimal.valueOf(factor))
    }
    
    /**
     * Multiplies by a factor (int)
     */
    fun multiply(factor: Int): FinancialAmount {
        return multiply(BigDecimal(factor))
    }
    
    /**
     * Divides by a divisor
     */
    fun divide(divisor: BigDecimal): FinancialAmount {
        require(divisor.compareTo(BigDecimal.ZERO) != 0) { "Cannot divide by zero" }
        return FinancialAmount(
            amount = scaledAmount.divide(divisor, scale, roundingMode),
            currency = currency,
            scale = scale,
            roundingMode = roundingMode
        )
    }
    
    /**
     * Divides by a divisor (double)
     */
    fun divide(divisor: Double): FinancialAmount {
        return divide(BigDecimal.valueOf(divisor))
    }
    
    /**
     * Divides by a divisor (int)
     */
    fun divide(divisor: Int): FinancialAmount {
        return divide(BigDecimal(divisor))
    }
    
    /**
     * Divides by another financial amount to get a ratio
     */
    fun divideBy(other: FinancialAmount): BigDecimal {
        requireSameCurrency(other)
        require(!other.isZero) { "Cannot divide by zero amount" }
        return scaledAmount.divide(other.scaledAmount, 10, roundingMode)
    }
    
    /**
     * Calculates percentage of this amount
     */
    fun percentage(percent: BigDecimal): FinancialAmount {
        return multiply(percent.divide(BigDecimal("100")))
    }
    
    /**
     * Calculates percentage of this amount (double)
     */
    fun percentage(percent: Double): FinancialAmount {
        return percentage(BigDecimal.valueOf(percent))
    }
    
    /**
     * Allocates this amount into specified proportions
     */
    fun allocate(proportions: List<BigDecimal>): List<FinancialAmount> {
        require(proportions.isNotEmpty()) { "Proportions cannot be empty" }
        require(proportions.all { it >= BigDecimal.ZERO }) { "Proportions must be non-negative" }
        
        val total = proportions.reduce { acc, proportion -> acc.add(proportion) }
        require(total > BigDecimal.ZERO) { "Total proportion must be positive" }
        
        val allocations = mutableListOf<FinancialAmount>()
        var remainder = scaledAmount
        
        for (i in 0 until proportions.size - 1) {
            val proportion = proportions[i]
            val allocation = scaledAmount.multiply(proportion).divide(total, scale, roundingMode)
            allocations.add(FinancialAmount(allocation, currency, scale, roundingMode))
            remainder = remainder.subtract(allocation)
        }
        
        // Last allocation gets the remainder to ensure exact total
        allocations.add(FinancialAmount(remainder, currency, scale, roundingMode))
        
        return allocations
    }
    
    /**
     * Allocates this amount into equal parts
     */
    fun allocateEvenly(parts: Int): List<FinancialAmount> {
        require(parts > 0) { "Number of parts must be positive" }
        val proportions = List(parts) { BigDecimal.ONE }
        return allocate(proportions)
    }
    
    // ==================== COMPARISON OPERATIONS ====================
    
    /**
     * Compares this amount with another (must be same currency)
     */
    override fun compareTo(other: FinancialAmount): Int {
        requireSameCurrency(other)
        return scaledAmount.compareTo(other.scaledAmount)
    }
    
    /**
     * Checks if this amount is greater than another
     */
    fun isGreaterThan(other: FinancialAmount): Boolean {
        return compareTo(other) > 0
    }
    
    /**
     * Checks if this amount is greater than or equal to another
     */
    fun isGreaterThanOrEqualTo(other: FinancialAmount): Boolean {
        return compareTo(other) >= 0
    }
    
    /**
     * Checks if this amount is less than another
     */
    fun isLessThan(other: FinancialAmount): Boolean {
        return compareTo(other) < 0
    }
    
    /**
     * Checks if this amount is less than or equal to another
     */
    fun isLessThanOrEqualTo(other: FinancialAmount): Boolean {
        return compareTo(other) <= 0
    }
    
    /**
     * Returns the minimum of this and another amount
     */
    fun min(other: FinancialAmount): FinancialAmount {
        return if (isLessThanOrEqualTo(other)) this else other
    }
    
    /**
     * Returns the maximum of this and another amount
     */
    fun max(other: FinancialAmount): FinancialAmount {
        return if (isGreaterThanOrEqualTo(other)) this else other
    }
    
    // ==================== CURRENCY OPERATIONS ====================
    
    /**
     * Converts this amount to another currency using the provided exchange rate
     */
    fun convertTo(targetCurrency: Currency, exchangeRate: BigDecimal): FinancialAmount {
        require(exchangeRate > BigDecimal.ZERO) { "Exchange rate must be positive" }
        
        if (currency == targetCurrency) {
            return this
        }
        
        val convertedAmount = scaledAmount.multiply(exchangeRate)
        return FinancialAmount(
            amount = convertedAmount,
            currency = targetCurrency,
            scale = targetCurrency.getDefaultFractionDigits(),
            roundingMode = roundingMode
        )
    }
    
    /**
     * Changes the scale (decimal places) of this amount
     */
    fun withScale(newScale: Int, newRoundingMode: RoundingMode = roundingMode): FinancialAmount {
        return FinancialAmount(
            amount = amount,
            currency = currency,
            scale = newScale,
            roundingMode = newRoundingMode
        )
    }
    
    /**
     * Changes the rounding mode of this amount
     */
    fun withRoundingMode(newRoundingMode: RoundingMode): FinancialAmount {
        return FinancialAmount(
            amount = amount,
            currency = currency,
            scale = scale,
            roundingMode = newRoundingMode
        )
    }
    
    // ==================== FORMATTING OPERATIONS ====================
    
    /**
     * Formats this amount for display with currency symbol
     */
    fun format(locale: Locale = Locale.getDefault()): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = currency.toJavaCurrency()
        format.maximumFractionDigits = scale
        format.minimumFractionDigits = scale
        return format.format(scaledAmount)
    }
    
    /**
     * Formats this amount without currency symbol
     */
    fun formatWithoutSymbol(locale: Locale = Locale.getDefault()): String {
        val format = NumberFormat.getNumberInstance(locale)
        format.maximumFractionDigits = scale
        format.minimumFractionDigits = scale
        return format.format(scaledAmount)
    }
    
    /**
     * Formats this amount in accounting style (negative amounts in parentheses)
     */
    fun formatAccounting(locale: Locale = Locale.getDefault()): String {
        val formatted = format(locale)
        return if (isNegative) {
            "(${formatted.removePrefix("-")})"
        } else {
            formatted
        }
    }
    
    /**
     * Returns a plain string representation of the amount
     */
    fun toPlainString(): String {
        return scaledAmount.toPlainString()
    }
    
    // ==================== BUSINESS LOGIC ====================
    
    /**
     * Applies tax to this amount
     */
    fun applyTax(taxRate: BigDecimal): FinancialAmount {
        val taxAmount = multiply(taxRate.divide(BigDecimal("100")))
        return add(taxAmount)
    }
    
    /**
     * Calculates tax amount for this amount
     */
    fun calculateTax(taxRate: BigDecimal): FinancialAmount {
        return multiply(taxRate.divide(BigDecimal("100")))
    }
    
    /**
     * Applies discount to this amount
     */
    fun applyDiscount(discountRate: BigDecimal): FinancialAmount {
        val discountAmount = multiply(discountRate.divide(BigDecimal("100")))
        return subtract(discountAmount)
    }
    
    /**
     * Calculates discount amount for this amount
     */
    fun calculateDiscount(discountRate: BigDecimal): FinancialAmount {
        return multiply(discountRate.divide(BigDecimal("100")))
    }
    
    /**
     * Rounds to nearest currency unit (e.g., nearest cent)
     */
    fun roundToCurrencyUnit(): FinancialAmount {
        return FinancialAmount(
            amount = scaledAmount,
            currency = currency,
            scale = currency.getDefaultFractionDigits(),
            roundingMode = RoundingMode.HALF_UP
        )
    }
    
    // ==================== VALIDATION ====================
    
    private fun requireSameCurrency(other: FinancialAmount) {
        require(currency == other.currency) {
            "Currency mismatch: ${currency.code} vs ${other.currency.code}"
        }
    }
    
    // ==================== OVERRIDES ====================
    
    override fun toString(): String {
        return "${currency.symbol}${scaledAmount.toPlainString()}"
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FinancialAmount) return false
        
        return scaledAmount.compareTo(other.scaledAmount) == 0 && 
               currency == other.currency
    }
    
    override fun hashCode(): Int {
        return scaledAmount.hashCode() * 31 + currency.hashCode()
    }
    
    // ==================== VALIDATION ====================
    
    init {
        require(scale >= 0) { "Scale must be non-negative" }
        require(scale <= 10) { "Scale cannot exceed 10 decimal places" }
    }
}

// ==================== OPERATOR OVERLOADS ====================

/**
 * Addition operator
 */
operator fun FinancialAmount.plus(other: FinancialAmount): FinancialAmount = this.add(other)

/**
 * Subtraction operator
 */
operator fun FinancialAmount.minus(other: FinancialAmount): FinancialAmount = this.subtract(other)

/**
 * Multiplication operator
 */
operator fun FinancialAmount.times(factor: BigDecimal): FinancialAmount = this.multiply(factor)
operator fun FinancialAmount.times(factor: Double): FinancialAmount = this.multiply(factor)
operator fun FinancialAmount.times(factor: Int): FinancialAmount = this.multiply(factor)

/**
 * Division operator
 */
operator fun FinancialAmount.div(divisor: BigDecimal): FinancialAmount = this.divide(divisor)
operator fun FinancialAmount.div(divisor: Double): FinancialAmount = this.divide(divisor)
operator fun FinancialAmount.div(divisor: Int): FinancialAmount = this.divide(divisor)

/**
 * Unary minus operator
 */
operator fun FinancialAmount.unaryMinus(): FinancialAmount = this.negated

/**
 * Unary plus operator
 */
operator fun FinancialAmount.unaryPlus(): FinancialAmount = this

// ==================== COLLECTION UTILITIES ====================

/**
 * Sums a collection of financial amounts
 */
fun Collection<FinancialAmount>.sum(): FinancialAmount? {
    if (isEmpty()) return null
    
    val first = first()
    return drop(1).fold(first) { acc, amount -> acc.add(amount) }
}

/**
 * Sums a collection of financial amounts with a specific currency for empty collections
 */
fun Collection<FinancialAmount>.sum(defaultCurrency: Currency): FinancialAmount {
    return sum() ?: FinancialAmount.zero(defaultCurrency)
}

/**
 * Finds the minimum amount in a collection
 */
fun Collection<FinancialAmount>.min(): FinancialAmount? {
    return minOrNull()
}

/**
 * Finds the maximum amount in a collection
 */
fun Collection<FinancialAmount>.max(): FinancialAmount? {
    return maxOrNull()
}

/**
 * Calculates the average of financial amounts
 */
fun Collection<FinancialAmount>.average(): FinancialAmount? {
    if (isEmpty()) return null
    
    val total = sum() ?: return null
    return total.divide(size)
}

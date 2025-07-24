package org.chiro.core_business_service.shared.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Shared Quantity Value Object for cross-module quantity operations.
 * 
 * This value object represents a measured quantity with its unit of measure,
 * used across all ERP modules for:
 * - Inventory stock levels and movements
 * - Sales order line items
 * - Manufacturing bill of materials
 * - Procurement purchase quantities
 * - Production planning and scheduling
 * 
 * Design Pattern: Value Object (immutable, thread-safe)
 * Usage: Shared Kernel across all bounded contexts
 * 
 * Key Features:
 * - Immutable with comprehensive validation
 * - Unit conversion support
 * - Mathematical operations (add, subtract, multiply, divide)
 * - Comparison operations with unit compatibility checking
 * - Production-grade precision handling
 */
@Embeddable
data class Quantity(
    @field:NotNull
    @field:Positive(message = "Quantity value must be positive")
    @field:DecimalMin(value = "0.0001", message = "Quantity cannot be smaller than 0.0001")
    @field:DecimalMax(value = "999999999.9999", message = "Quantity exceeds maximum allowed value")
    @Column(name = "quantity_value", precision = 15, scale = 4, nullable = false)
    val value: BigDecimal,
    
    @field:NotNull
    @Column(name = "unit_of_measure", length = 10, nullable = false)
    val unitOfMeasure: UnitOfMeasure
) : Comparable<Quantity> {
    
    init {
        require(value.scale() <= 4) { "Quantity precision cannot exceed 4 decimal places" }
        require(value > BigDecimal.ZERO) { "Quantity value must be positive" }
    }
    
    companion object {
        /** Mathematical context for quantity calculations */
        val QUANTITY_CONTEXT = MathContext(15, RoundingMode.HALF_EVEN)
        
        /**
         * Creates zero quantity with specified unit
         */
        @JvmStatic
        fun zero(unit: UnitOfMeasure): Quantity = Quantity(BigDecimal("0.0001"), unit)
        
        /**
         * Creates quantity from string value
         */
        @JvmStatic
        fun of(value: String, unit: UnitOfMeasure): Quantity = 
            Quantity(BigDecimal(value), unit)
        
        /**
         * Creates quantity from double value
         */
        @JvmStatic
        fun of(value: Double, unit: UnitOfMeasure): Quantity = 
            Quantity(BigDecimal.valueOf(value), unit)
        
        /**
         * Creates quantity from integer value
         */
        @JvmStatic
        fun of(value: Int, unit: UnitOfMeasure): Quantity = 
            Quantity(BigDecimal.valueOf(value.toLong()), unit)
    }
    
    // ===================== KOTLIN OPERATORS =====================
    
    operator fun plus(other: Quantity): Quantity = add(other)
    operator fun minus(other: Quantity): Quantity = subtract(other)
    operator fun times(factor: BigDecimal): Quantity = multiply(factor)
    operator fun times(factor: Double): Quantity = multiply(BigDecimal.valueOf(factor))
    operator fun times(factor: Int): Quantity = multiply(BigDecimal.valueOf(factor.toLong()))
    operator fun div(divisor: BigDecimal): Quantity = divide(divisor)
    operator fun div(divisor: Double): Quantity = divide(BigDecimal.valueOf(divisor))
    operator fun div(divisor: Int): Quantity = divide(BigDecimal.valueOf(divisor.toLong()))
    
    // ===================== ARITHMETIC OPERATIONS =====================
    
    /**
     * Adds another quantity (compatible units required)
     */
    fun add(other: Quantity): Quantity {
        requireCompatibleUnits(other)
        val convertedOther = convertToUnit(other, this.unitOfMeasure)
        return copy(value = value.add(convertedOther.value, QUANTITY_CONTEXT))
    }
    
    /**
     * Subtracts another quantity (compatible units required)
     */
    fun subtract(other: Quantity): Quantity {
        requireCompatibleUnits(other)
        val convertedOther = convertToUnit(other, this.unitOfMeasure)
        val result = value.subtract(convertedOther.value, QUANTITY_CONTEXT)
        require(result >= BigDecimal.ZERO) { "Cannot have negative quantity" }
        return copy(value = result)
    }
    
    /**
     * Multiplies by a factor
     */
    fun multiply(factor: BigDecimal): Quantity {
        require(factor >= BigDecimal.ZERO) { "Factor must be non-negative" }
        return copy(value = value.multiply(factor, QUANTITY_CONTEXT))
    }
    
    /**
     * Divides by a factor
     */
    fun divide(divisor: BigDecimal): Quantity {
        require(divisor > BigDecimal.ZERO) { "Divisor must be positive" }
        return copy(value = value.divide(divisor, QUANTITY_CONTEXT))
    }
    
    /**
     * Multiplies by a price to get monetary amount
     */
    fun times(price: Money): Money {
        return price.multiply(value)
    }
    
    // ===================== COMPARISON OPERATIONS =====================
    
    fun isGreaterThan(other: Quantity): Boolean {
        requireCompatibleUnits(other)
        val convertedOther = convertToUnit(other, this.unitOfMeasure)
        return value > convertedOther.value
    }
    
    fun isLessThan(other: Quantity): Boolean {
        requireCompatibleUnits(other)
        val convertedOther = convertToUnit(other, this.unitOfMeasure)
        return value < convertedOther.value
    }
    
    fun isGreaterThanOrEqual(other: Quantity): Boolean {
        requireCompatibleUnits(other)
        val convertedOther = convertToUnit(other, this.unitOfMeasure)
        return value >= convertedOther.value
    }
    
    fun isLessThanOrEqual(other: Quantity): Boolean {
        requireCompatibleUnits(other)
        val convertedOther = convertToUnit(other, this.unitOfMeasure)
        return value <= convertedOther.value
    }
    
    override fun compareTo(other: Quantity): Int {
        requireCompatibleUnits(other)
        val convertedOther = convertToUnit(other, this.unitOfMeasure)
        return value.compareTo(convertedOther.value)
    }
    
    // ===================== UNIT CONVERSION =====================
    
    /**
     * Converts this quantity to a different compatible unit
     */
    fun convertTo(targetUnit: UnitOfMeasure): Quantity {
        if (unitOfMeasure == targetUnit) return this
        
        require(unitOfMeasure.isCompatibleWith(targetUnit)) {
            "Cannot convert from ${unitOfMeasure.symbol} to ${targetUnit.symbol} - incompatible unit types"
        }
        
        val conversionFactor = unitOfMeasure.getConversionFactor(targetUnit)
        val convertedValue = value.multiply(conversionFactor, QUANTITY_CONTEXT)
        
        return Quantity(convertedValue, targetUnit)
    }
    
    // ===================== UTILITY METHODS =====================
    
    /**
     * Checks if this quantity is zero (considering precision)
     */
    val isZero: Boolean get() = value.compareTo(BigDecimal("0.0001")) <= 0
    
    /**
     * Returns the absolute value
     */
    fun abs(): Quantity = copy(value = value.abs())
    
    /**
     * Rounds to specified decimal places
     */
    fun round(scale: Int = 4): Quantity {
        val rounded = value.setScale(scale, RoundingMode.HALF_EVEN)
        return copy(value = rounded)
    }
    
    /**
     * Formats quantity for display
     */
    fun format(): String = "${value.stripTrailingZeros()} ${unitOfMeasure.symbol}"
    
    override fun toString(): String = format()
    
    // ===================== PRIVATE HELPER METHODS =====================
    
    private fun requireCompatibleUnits(other: Quantity) {
        require(unitOfMeasure.isCompatibleWith(other.unitOfMeasure)) {
            "Incompatible units: ${unitOfMeasure.symbol} and ${other.unitOfMeasure.symbol}"
        }
    }
    
    private fun convertToUnit(quantity: Quantity, targetUnit: UnitOfMeasure): Quantity {
        return if (quantity.unitOfMeasure == targetUnit) {
            quantity
        } else {
            quantity.convertTo(targetUnit)
        }
    }
}

// ===================== KOTLIN DSL EXTENSIONS =====================

/** Extension property to create EACH quantity from Int */
val Int.EACH: Quantity get() = Quantity.of(this, UnitOfMeasure.EACH)

/** Extension property to create KG quantity from Double */
val Double.KG: Quantity get() = Quantity.of(this, UnitOfMeasure.KILOGRAM)

/** Extension property to create LITER quantity from Double */
val Double.LITER: Quantity get() = Quantity.of(this, UnitOfMeasure.LITER)

/** Extension property to create METER quantity from Double */
val Double.METER: Quantity get() = Quantity.of(this, UnitOfMeasure.METER)

/** Extension function to create quantity in any unit */
infix fun String.of(unit: UnitOfMeasure): Quantity = Quantity.of(this, unit)

/** Extension function to create quantity in any unit */
infix fun Double.of(unit: UnitOfMeasure): Quantity = Quantity.of(this, unit)

/** Extension function to create quantity in any unit */
infix fun Int.of(unit: UnitOfMeasure): Quantity = Quantity.of(this, unit)

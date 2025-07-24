package org.chiro.core_business_service.shared.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

/**
 * Unit of Measure Value Object for standardized measurement across all modules.
 * 
 * This value object represents measurement units used throughout the ERP system:
 * - Inventory: Product quantities, stock levels, reorder points
 * - Sales: Order quantities, pricing per unit
 * - Manufacturing: BOM quantities, production capacity
 * - Procurement: Purchase quantities, vendor pricing
 * - Finance: Cost per unit calculations
 * 
 * Design Pattern: Value Object (immutable, thread-safe)
 * Usage: Shared Kernel across all bounded contexts
 * 
 * Key Features:
 * - Comprehensive unit type classification
 * - Unit conversion support with precision
 * - International standard units (SI, Imperial, US Customary)
 * - Industry-specific units (textile, construction, etc.)
 * - Production-grade validation and compatibility checking
 */
@Embeddable
data class UnitOfMeasure(
    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false)
    val type: UnitType,
    
    @field:NotBlank(message = "Unit symbol cannot be blank")
    @Column(name = "unit_symbol", length = 10, nullable = false)
    val symbol: String,
    
    @field:NotBlank(message = "Unit name cannot be blank")
    @Column(name = "unit_name", length = 50, nullable = false)
    val name: String,
    
    @Column(name = "conversion_to_base", precision = 19, scale = 9)
    val conversionToBase: BigDecimal? = null
) {
    
    init {
        require(symbol.isNotBlank()) { "Unit symbol cannot be blank" }
        require(name.isNotBlank()) { "Unit name cannot be blank" }
        require(symbol.length <= 10) { "Unit symbol too long" }
        require(name.length <= 50) { "Unit name too long" }
    }
    
    companion object {
        // ===================== COUNT/DISCRETE UNITS =====================
        @JvmStatic
        val EACH = UnitOfMeasure(UnitType.COUNT, "EA", "Each", BigDecimal.ONE)
        
        @JvmStatic
        val PIECE = UnitOfMeasure(UnitType.COUNT, "PC", "Piece", BigDecimal.ONE)
        
        @JvmStatic
        val DOZEN = UnitOfMeasure(UnitType.COUNT, "DOZ", "Dozen", BigDecimal("12"))
        
        @JvmStatic
        val GROSS = UnitOfMeasure(UnitType.COUNT, "GR", "Gross", BigDecimal("144"))
        
        @JvmStatic
        val PAIR = UnitOfMeasure(UnitType.COUNT, "PR", "Pair", BigDecimal("2"))
        
        // ===================== WEIGHT/MASS UNITS =====================
        @JvmStatic
        val KILOGRAM = UnitOfMeasure(UnitType.WEIGHT, "kg", "Kilogram", BigDecimal.ONE)
        
        @JvmStatic
        val GRAM = UnitOfMeasure(UnitType.WEIGHT, "g", "Gram", BigDecimal("0.001"))
        
        @JvmStatic
        val POUND = UnitOfMeasure(UnitType.WEIGHT, "lb", "Pound", BigDecimal("0.453592"))
        
        @JvmStatic
        val OUNCE = UnitOfMeasure(UnitType.WEIGHT, "oz", "Ounce", BigDecimal("0.0283495"))
        
        @JvmStatic
        val TON_METRIC = UnitOfMeasure(UnitType.WEIGHT, "MT", "Metric Ton", BigDecimal("1000"))
        
        @JvmStatic
        val TON_US = UnitOfMeasure(UnitType.WEIGHT, "ST", "Short Ton", BigDecimal("907.185"))
        
        // ===================== LENGTH/DISTANCE UNITS =====================
        @JvmStatic
        val METER = UnitOfMeasure(UnitType.LENGTH, "m", "Meter", BigDecimal.ONE)
        
        @JvmStatic
        val CENTIMETER = UnitOfMeasure(UnitType.LENGTH, "cm", "Centimeter", BigDecimal("0.01"))
        
        @JvmStatic
        val MILLIMETER = UnitOfMeasure(UnitType.LENGTH, "mm", "Millimeter", BigDecimal("0.001"))
        
        @JvmStatic
        val INCH = UnitOfMeasure(UnitType.LENGTH, "in", "Inch", BigDecimal("0.0254"))
        
        @JvmStatic
        val FOOT = UnitOfMeasure(UnitType.LENGTH, "ft", "Foot", BigDecimal("0.3048"))
        
        @JvmStatic
        val YARD = UnitOfMeasure(UnitType.LENGTH, "yd", "Yard", BigDecimal("0.9144"))
        
        // ===================== AREA UNITS =====================
        @JvmStatic
        val SQUARE_METER = UnitOfMeasure(UnitType.AREA, "m²", "Square Meter", BigDecimal.ONE)
        
        @JvmStatic
        val SQUARE_FOOT = UnitOfMeasure(UnitType.AREA, "ft²", "Square Foot", BigDecimal("0.092903"))
        
        @JvmStatic
        val SQUARE_INCH = UnitOfMeasure(UnitType.AREA, "in²", "Square Inch", BigDecimal("0.00064516"))
        
        // ===================== VOLUME UNITS =====================
        @JvmStatic
        val LITER = UnitOfMeasure(UnitType.VOLUME, "L", "Liter", BigDecimal.ONE)
        
        @JvmStatic
        val MILLILITER = UnitOfMeasure(UnitType.VOLUME, "mL", "Milliliter", BigDecimal("0.001"))
        
        @JvmStatic
        val CUBIC_METER = UnitOfMeasure(UnitType.VOLUME, "m³", "Cubic Meter", BigDecimal("1000"))
        
        @JvmStatic
        val GALLON_US = UnitOfMeasure(UnitType.VOLUME, "gal", "US Gallon", BigDecimal("3.78541"))
        
        @JvmStatic
        val GALLON_IMPERIAL = UnitOfMeasure(UnitType.VOLUME, "gal(UK)", "Imperial Gallon", BigDecimal("4.54609"))
        
        @JvmStatic
        val FLUID_OUNCE_US = UnitOfMeasure(UnitType.VOLUME, "fl oz", "US Fluid Ounce", BigDecimal("0.0295735"))
        
        // ===================== TIME UNITS =====================
        @JvmStatic
        val HOUR = UnitOfMeasure(UnitType.TIME, "hr", "Hour", BigDecimal.ONE)
        
        @JvmStatic
        val MINUTE = UnitOfMeasure(UnitType.TIME, "min", "Minute", BigDecimal("0.0166667"))
        
        @JvmStatic
        val SECOND = UnitOfMeasure(UnitType.TIME, "sec", "Second", BigDecimal("0.000277778"))
        
        @JvmStatic
        val DAY = UnitOfMeasure(UnitType.TIME, "day", "Day", BigDecimal("24"))
        
        // ===================== ENERGY/POWER UNITS =====================
        @JvmStatic
        val KILOWATT_HOUR = UnitOfMeasure(UnitType.ENERGY, "kWh", "Kilowatt Hour", BigDecimal.ONE)
        
        @JvmStatic
        val WATT_HOUR = UnitOfMeasure(UnitType.ENERGY, "Wh", "Watt Hour", BigDecimal("0.001"))
        
        @JvmStatic
        val BTU = UnitOfMeasure(UnitType.ENERGY, "BTU", "British Thermal Unit", BigDecimal("0.000293071"))
        
        // ===================== TEMPERATURE UNITS =====================
        @JvmStatic
        val CELSIUS = UnitOfMeasure(UnitType.TEMPERATURE, "°C", "Degrees Celsius", null)
        
        @JvmStatic
        val FAHRENHEIT = UnitOfMeasure(UnitType.TEMPERATURE, "°F", "Degrees Fahrenheit", null)
        
        @JvmStatic
        val KELVIN = UnitOfMeasure(UnitType.TEMPERATURE, "K", "Kelvin", null)
        
        /**
         * Gets all supported units by type
         */
        @JvmStatic
        fun getUnitsByType(type: UnitType): List<UnitOfMeasure> = when (type) {
            UnitType.COUNT -> listOf(EACH, PIECE, DOZEN, GROSS, PAIR)
            UnitType.WEIGHT -> listOf(KILOGRAM, GRAM, POUND, OUNCE, TON_METRIC, TON_US)
            UnitType.LENGTH -> listOf(METER, CENTIMETER, MILLIMETER, INCH, FOOT, YARD)
            UnitType.AREA -> listOf(SQUARE_METER, SQUARE_FOOT, SQUARE_INCH)
            UnitType.VOLUME -> listOf(LITER, MILLILITER, CUBIC_METER, GALLON_US, GALLON_IMPERIAL, FLUID_OUNCE_US)
            UnitType.TIME -> listOf(HOUR, MINUTE, SECOND, DAY)
            UnitType.ENERGY -> listOf(KILOWATT_HOUR, WATT_HOUR, BTU)
            UnitType.TEMPERATURE -> listOf(CELSIUS, FAHRENHEIT, KELVIN)
        }
    }
    
    // ===================== UNIT COMPATIBILITY =====================
    
    /**
     * Checks if this unit is compatible with another unit (same type)
     */
    fun isCompatibleWith(other: UnitOfMeasure): Boolean = this.type == other.type
    
    /**
     * Gets conversion factor to convert from this unit to target unit
     */
    fun getConversionFactor(targetUnit: UnitOfMeasure): BigDecimal {
        require(isCompatibleWith(targetUnit)) {
            "Cannot convert between incompatible unit types: ${this.type} and ${targetUnit.type}"
        }
        
        // Special handling for temperature conversions
        if (type == UnitType.TEMPERATURE) {
            throw UnsupportedOperationException(
                "Temperature conversions require special formulas and context"
            )
        }
        
        // For units without base conversion (like temperature), throw error
        if (conversionToBase == null || targetUnit.conversionToBase == null) {
            throw UnsupportedOperationException(
                "Cannot convert units without base conversion factors"
            )
        }
        
        // Convert: this -> base -> target
        // Factor = (this_to_base) / (target_to_base)
        return conversionToBase.divide(targetUnit.conversionToBase, 9, java.math.RoundingMode.HALF_EVEN)
    }
    
    // ===================== UTILITY METHODS =====================
    
    /**
     * Checks if this is a base unit for its type
     */
    val isBaseUnit: Boolean get() = conversionToBase == BigDecimal.ONE
    
    /**
     * Gets the base unit for this unit's type
     */
    fun getBaseUnit(): UnitOfMeasure = when (type) {
        UnitType.COUNT -> EACH
        UnitType.WEIGHT -> KILOGRAM
        UnitType.LENGTH -> METER
        UnitType.AREA -> SQUARE_METER
        UnitType.VOLUME -> LITER
        UnitType.TIME -> HOUR
        UnitType.ENERGY -> KILOWATT_HOUR
        UnitType.TEMPERATURE -> CELSIUS
    }
    
    /**
     * Returns display name with symbol
     */
    fun getDisplayName(): String = "$name ($symbol)"
    
    override fun toString(): String = symbol
}

/**
 * Unit Type Classification Enum
 */
enum class UnitType {
    COUNT,       // Discrete/countable items (pieces, dozens, etc.)
    WEIGHT,      // Mass/weight measurements (kg, lb, etc.)
    LENGTH,      // Linear measurements (m, ft, etc.)
    AREA,        // Area measurements (m², ft², etc.)
    VOLUME,      // Volume/capacity measurements (L, gal, etc.)
    TIME,        // Time measurements (hr, min, etc.)
    ENERGY,      // Energy/power measurements (kWh, BTU, etc.)
    TEMPERATURE  // Temperature measurements (°C, °F, etc.)
}

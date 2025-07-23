package org.chiro.finance.domain.valueobject

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Cost Method Value Object
 * 
 * Represents different costing methods used for inventory valuation and cost accounting.
 * This value object encapsulates costing methodologies, calculation rules, and business logic.
 * 
 * Design Pattern: Value Object (immutable, equality based on value)
 * Domain: Finance
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
enum class CostMethodType {
    // ==================== INVENTORY COSTING METHODS ====================
    FIFO,                       // First In, First Out
    LIFO,                       // Last In, First Out
    WEIGHTED_AVERAGE,           // Weighted Average Cost
    MOVING_AVERAGE,             // Moving Average Cost
    SPECIFIC_IDENTIFICATION,    // Specific Item Identification
    STANDARD_COST,              // Standard Costing
    
    // ==================== ACTIVITY-BASED COSTING ====================
    ACTIVITY_BASED_COSTING,     // ABC - Activity Based Costing
    TIME_DRIVEN_ABC,            // Time-Driven Activity Based Costing
    RESOURCE_CONSUMPTION_ACCOUNTING, // RCA Method
    
    // ==================== PROCESS COSTING ====================
    PROCESS_COSTING,            // Process Cost Method
    JOB_ORDER_COSTING,          // Job Order Costing
    BATCH_COSTING,              // Batch Costing
    CONTRACT_COSTING,           // Contract Costing
    
    // ==================== MANUFACTURING COSTING ====================
    ABSORPTION_COSTING,         // Full Absorption Costing
    VARIABLE_COSTING,           // Variable/Direct Costing
    MARGINAL_COSTING,           // Marginal Costing
    DIRECT_COSTING,             // Direct Cost Method
    
    // ==================== SERVICE COSTING ====================
    SERVICE_COSTING,            // Service Industry Costing
    OPERATION_COSTING,          // Operation Costing
    MULTIPLE_COSTING,           // Multiple/Composite Costing
    
    // ==================== COST ALLOCATION METHODS ====================
    DIRECT_ALLOCATION,          // Direct Cost Allocation
    STEP_DOWN_ALLOCATION,       // Step-down Allocation
    RECIPROCAL_ALLOCATION,      // Reciprocal Services Allocation
    
    // ==================== OVERHEAD ALLOCATION ====================
    TRADITIONAL_OVERHEAD,       // Traditional Overhead Allocation
    MACHINE_HOUR_RATE,         // Machine Hour Rate Method
    LABOR_HOUR_RATE,           // Labor Hour Rate Method
    PRIME_COST_PERCENTAGE,     // Prime Cost Percentage Method
    
    // ==================== MODERN COSTING METHODS ====================
    TARGET_COSTING,            // Target Cost Method
    KAIZEN_COSTING,            // Kaizen/Continuous Improvement Costing
    LIFECYCLE_COSTING,         // Product Lifecycle Costing
    QUALITY_COSTING,           // Quality Cost Method
    
    // ==================== PROJECT COSTING ====================
    PROJECT_COSTING,           // Project-based Costing
    EARNED_VALUE_METHOD,       // Earned Value Management
    MILESTONE_COSTING,         // Milestone-based Costing
    
    // ==================== SPECIAL METHODS ====================
    REPLACEMENT_COST,          // Replacement Cost Method
    NET_REALIZABLE_VALUE,      // Net Realizable Value
    FAIR_VALUE_METHOD,         // Fair Value Accounting
    HISTORICAL_COST,           // Historical Cost Method
    
    // ==================== CUSTOM METHODS ====================
    CUSTOM_COST_METHOD,        // Custom defined method
    HYBRID_METHOD              // Hybrid/Combined methods
}

/**
 * Cost Allocation Basis Enum
 */
enum class CostAllocationBasis {
    // ==================== VOLUME-BASED ====================
    UNITS_PRODUCED,            // Number of units produced
    MACHINE_HOURS,             // Machine operating hours
    LABOR_HOURS,               // Direct labor hours
    LABOR_COST,                // Direct labor cost
    MATERIAL_COST,             // Direct material cost
    PRIME_COST,                // Prime cost (labor + material)
    
    // ==================== VALUE-BASED ====================
    SALES_VALUE,               // Sales/revenue value
    NET_REALIZABLE_VALUE,      // Net realizable value
    RELATIVE_SALES_VALUE,      // Relative sales value
    MARKET_VALUE,              // Market value
    
    // ==================== ACTIVITY-BASED ====================
    NUMBER_OF_SETUPS,          // Number of production setups
    NUMBER_OF_ORDERS,          // Number of orders processed
    NUMBER_OF_BATCHES,         // Number of batches
    INSPECTION_HOURS,          // Quality inspection hours
    MATERIAL_MOVEMENTS,        // Material handling movements
    
    // ==================== SPACE/FACILITY-BASED ====================
    FLOOR_SPACE,               // Floor space occupied
    STORAGE_SPACE,             // Storage space used
    FACILITY_UTILIZATION,      // Facility utilization rate
    
    // ==================== TIME-BASED ====================
    PROCESSING_TIME,           // Processing time
    CYCLE_TIME,                // Cycle time
    SETUP_TIME,                // Setup time
    WAIT_TIME,                 // Wait time
    
    // ==================== WEIGHT/QUANTITY-BASED ====================
    WEIGHT,                    // Physical weight
    VOLUME,                    // Physical volume
    QUANTITY,                  // Quantity processed
    
    // ==================== CUSTOM ====================
    CUSTOM_BASIS               // Custom allocation basis
}

/**
 * Cost Method Value Object
 * 
 * Encapsulates costing methodology with allocation rules and business logic.
 * This is an immutable value object that represents a specific cost calculation method.
 */
data class CostMethod(
    @field:NotBlank(message = "Cost method type cannot be blank")
    val type: CostMethodType,
    
    @field:Size(max = 100, message = "Cost method name cannot exceed 100 characters")
    val name: String = type.getDisplayName(),
    
    @field:Size(max = 500, message = "Cost method description cannot exceed 500 characters")
    val description: String? = null,
    
    // ==================== ALLOCATION SETTINGS ====================
    val allocationBasis: CostAllocationBasis = type.getDefaultAllocationBasis(),
    val allocationRate: BigDecimal? = null,
    val allocationPercentage: BigDecimal? = null,
    
    // ==================== METHOD CONFIGURATION ====================
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val requiresStandardCosts: Boolean = type.requiresStandardCosts(),
    val requiresActualCosts: Boolean = type.requiresActualCosts(),
    val supportsVarianceFalysis: Boolean = type.supportsVarianceAnalysis(),
    
    // ==================== INVENTORY SPECIFIC ====================
    val includesDirectMaterial: Boolean = true,
    val includesDirectLabor: Boolean = true,
    val includesManufacturingOverhead: Boolean = type.includesOverheadByDefault(),
    val includesSellingExpenses: Boolean = false,
    val includesAdministrativeExpenses: Boolean = false,
    
    // ==================== CALCULATION PARAMETERS ====================
    val roundingPrecision: Int = 4,
    val roundingMode: java.math.RoundingMode = java.math.RoundingMode.HALF_UP,
    val minimumCostThreshold: BigDecimal? = null,
    val maximumCostThreshold: BigDecimal? = null,
    
    // ==================== PERIOD SETTINGS ====================
    val effectiveDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val fiscalPeriodDependent: Boolean = false,
    val requiresPeriodClosing: Boolean = type.requiresPeriodClosing(),
    
    // ==================== VARIANCE ANALYSIS ====================
    val enablePriceVariance: Boolean = false,
    val enableQuantityVariance: Boolean = false,
    val enableEfficiencyVariance: Boolean = false,
    val enableVolumeVariance: Boolean = false,
    val varianceThresholdPercentage: BigDecimal? = null,
    
    // ==================== AUDIT AND TRACKING ====================
    val trackCostComponents: Boolean = true,
    val maintainCostHistory: Boolean = true,
    val enableCostTraceability: Boolean = type.enablesCostTraceability(),
    
    // ==================== INTEGRATION SETTINGS ====================
    val integrationWithInventory: Boolean = type.integratesWithInventory(),
    val integrationWithProduction: Boolean = type.integratesWithProduction(),
    val integrationWithSales: Boolean = type.integratesWithSales(),
    val integrationWithPurchasing: Boolean = type.integratesWithPurchasing(),
    
    // ==================== AUDIT FIELDS ====================
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val lastModifiedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
        
        // ==================== FACTORY METHODS ====================
        
        /**
         * Creates a FIFO cost method
         */
        fun fifo(): CostMethod = CostMethod(
            type = CostMethodType.FIFO,
            description = "First In, First Out inventory valuation method"
        )
        
        /**
         * Creates a LIFO cost method
         */
        fun lifo(): CostMethod = CostMethod(
            type = CostMethodType.LIFO,
            description = "Last In, First Out inventory valuation method"
        )
        
        /**
         * Creates a weighted average cost method
         */
        fun weightedAverage(): CostMethod = CostMethod(
            type = CostMethodType.WEIGHTED_AVERAGE,
            description = "Weighted average cost inventory valuation method"
        )
        
        /**
         * Creates a standard cost method
         */
        fun standardCost(
            standardRate: BigDecimal,
            varianceThreshold: BigDecimal = BigDecimal("5.0")
        ): CostMethod = CostMethod(
            type = CostMethodType.STANDARD_COST,
            allocationRate = standardRate,
            enablePriceVariance = true,
            enableQuantityVariance = true,
            enableEfficiencyVariance = true,
            varianceThresholdPercentage = varianceThreshold,
            description = "Standard cost method with variance analysis"
        )
        
        /**
         * Creates an activity-based costing method
         */
        fun activityBasedCosting(
            allocationBasis: CostAllocationBasis,
            activityRate: BigDecimal
        ): CostMethod = CostMethod(
            type = CostMethodType.ACTIVITY_BASED_COSTING,
            allocationBasis = allocationBasis,
            allocationRate = activityRate,
            trackCostComponents = true,
            enableCostTraceability = true,
            description = "Activity-based costing method"
        )
        
        /**
         * Creates an absorption costing method
         */
        fun absorptionCosting(): CostMethod = CostMethod(
            type = CostMethodType.ABSORPTION_COSTING,
            includesDirectMaterial = true,
            includesDirectLabor = true,
            includesManufacturingOverhead = true,
            description = "Full absorption costing including all manufacturing costs"
        )
        
        /**
         * Creates a variable costing method
         */
        fun variableCosting(): CostMethod = CostMethod(
            type = CostMethodType.VARIABLE_COSTING,
            includesDirectMaterial = true,
            includesDirectLabor = true,
            includesManufacturingOverhead = false,
            description = "Variable costing excluding fixed manufacturing overhead"
        )
        
        /**
         * Creates a job order costing method
         */
        fun jobOrderCosting(): CostMethod = CostMethod(
            type = CostMethodType.JOB_ORDER_COSTING,
            allocationBasis = CostAllocationBasis.LABOR_HOURS,
            trackCostComponents = true,
            enableCostTraceability = true,
            description = "Job order costing for custom production"
        )
        
        /**
         * Creates a process costing method
         */
        fun processCosting(): CostMethod = CostMethod(
            type = CostMethodType.PROCESS_COSTING,
            allocationBasis = CostAllocationBasis.UNITS_PRODUCED,
            fiscalPeriodDependent = true,
            requiresPeriodClosing = true,
            description = "Process costing for continuous production"
        )
        
        /**
         * Creates a custom cost method
         */
        fun custom(
            name: String,
            allocationBasis: CostAllocationBasis,
            description: String? = null
        ): CostMethod = CostMethod(
            type = CostMethodType.CUSTOM_COST_METHOD,
            name = name,
            allocationBasis = allocationBasis,
            description = description
        )
    }
    
    // ==================== COMPUTED PROPERTIES ====================
    
    /**
     * Check if this cost method is currently effective
     */
    val isEffective: Boolean
        get() {
            val today = LocalDate.now()
            return !effectiveDate.isAfter(today) && 
                   (endDate?.let { !it.isBefore(today) } ?: true)
        }
    
    /**
     * Check if this cost method has expired
     */
    val isExpired: Boolean
        get() = endDate?.isBefore(LocalDate.now()) ?: false
    
    /**
     * Check if this cost method supports real-time costing
     */
    val supportsRealTimeCosting: Boolean
        get() = type in setOf(
            CostMethodType.MOVING_AVERAGE,
            CostMethodType.STANDARD_COST,
            CostMethodType.SPECIFIC_IDENTIFICATION
        )
    
    /**
     * Check if this cost method requires period-end calculations
     */
    val requiresPeriodEndCalculation: Boolean
        get() = type in setOf(
            CostMethodType.WEIGHTED_AVERAGE,
            CostMethodType.PROCESS_COSTING,
            CostMethodType.ABSORPTION_COSTING
        )
    
    /**
     * Get the cost components included in this method
     */
    val includedCostComponents: Set<String>
        get() = buildSet {
            if (includesDirectMaterial) add("Direct Material")
            if (includesDirectLabor) add("Direct Labor")
            if (includesManufacturingOverhead) add("Manufacturing Overhead")
            if (includesSellingExpenses) add("Selling Expenses")
            if (includesAdministrativeExpenses) add("Administrative Expenses")
        }
    
    // ==================== BUSINESS LOGIC ====================
    
    /**
     * Calculates allocated cost based on the allocation basis and rate
     */
    fun calculateAllocatedCost(
        basisAmount: BigDecimal,
        totalCostPool: BigDecimal? = null
    ): BigDecimal {
        return when {
            allocationRate != null -> {
                basisAmount.multiply(allocationRate).setScale(roundingPrecision, roundingMode)
            }
            allocationPercentage != null && totalCostPool != null -> {
                val percentage = allocationPercentage.divide(BigDecimal("100"))
                totalCostPool.multiply(percentage).setScale(roundingPrecision, roundingMode)
            }
            else -> BigDecimal.ZERO
        }
    }
    
    /**
     * Validates if the calculated cost is within thresholds
     */
    fun validateCostThresholds(calculatedCost: BigDecimal): Boolean {
        minimumCostThreshold?.let { min ->
            if (calculatedCost < min) return false
        }
        
        maximumCostThreshold?.let { max ->
            if (calculatedCost > max) return false
        }
        
        return true
    }
    
    /**
     * Calculates variance percentage between actual and standard cost
     */
    fun calculateVariancePercentage(actualCost: BigDecimal, standardCost: BigDecimal): BigDecimal? {
        if (!supportsVarianceFalysis || standardCost == BigDecimal.ZERO) return null
        
        val variance = actualCost.subtract(standardCost)
        return variance.divide(standardCost, roundingPrecision, roundingMode)
                     .multiply(BigDecimal("100"))
    }
    
    /**
     * Checks if variance is significant based on threshold
     */
    fun isSignificantVariance(actualCost: BigDecimal, standardCost: BigDecimal): Boolean {
        val variancePercentage = calculateVariancePercentage(actualCost, standardCost)
        return variancePercentage?.let { variance ->
            varianceThresholdPercentage?.let { threshold ->
                variance.abs() >= threshold
            } ?: false
        } ?: false
    }
    
    /**
     * Determines the cost flow assumption for inventory
     */
    fun getCostFlowAssumption(): String = when (type) {
        CostMethodType.FIFO -> "First units purchased are first units sold"
        CostMethodType.LIFO -> "Last units purchased are first units sold"
        CostMethodType.WEIGHTED_AVERAGE -> "Average cost of all units available"
        CostMethodType.MOVING_AVERAGE -> "Continuously updated average cost"
        CostMethodType.SPECIFIC_IDENTIFICATION -> "Actual cost of specific units"
        else -> "Cost flow based on $name method"
    }
    
    /**
     * Gets the recommended inventory valuation approach
     */
    fun getInventoryValuationApproach(): String = when (type) {
        CostMethodType.FIFO -> "Lower of cost or market using FIFO"
        CostMethodType.LIFO -> "LIFO cost or replacement cost"
        CostMethodType.WEIGHTED_AVERAGE -> "Average cost or net realizable value"
        CostMethodType.STANDARD_COST -> "Standard cost with variance adjustments"
        CostMethodType.ABSORPTION_COSTING -> "Full absorption cost"
        CostMethodType.VARIABLE_COSTING -> "Variable cost plus allocated overhead"
        else -> "Method-specific valuation approach"
    }
    
    /**
     * Checks compatibility with another cost method for consolidated reporting
     */
    fun isCompatibleWith(otherMethod: CostMethod): Boolean {
        // Same type methods are always compatible
        if (type == otherMethod.type) return true
        
        // Standard and actual cost methods are compatible
        if (type == CostMethodType.STANDARD_COST && otherMethod.requiresActualCosts) return true
        if (requiresActualCosts && otherMethod.type == CostMethodType.STANDARD_COST) return true
        
        // FIFO and Weighted Average are generally compatible
        if ((type == CostMethodType.FIFO && otherMethod.type == CostMethodType.WEIGHTED_AVERAGE) ||
            (type == CostMethodType.WEIGHTED_AVERAGE && otherMethod.type == CostMethodType.FIFO)) {
            return true
        }
        
        // Activity-based methods are compatible with absorption costing
        if ((type == CostMethodType.ACTIVITY_BASED_COSTING && otherMethod.type == CostMethodType.ABSORPTION_COSTING) ||
            (type == CostMethodType.ABSORPTION_COSTING && otherMethod.type == CostMethodType.ACTIVITY_BASED_COSTING)) {
            return true
        }
        
        return false
    }
    
    /**
     * Gets the required data elements for this cost method
     */
    fun getRequiredDataElements(): Set<String> = buildSet {
        when (type) {
            CostMethodType.FIFO, CostMethodType.LIFO -> {
                add("Purchase Date")
                add("Purchase Cost")
                add("Quantity Purchased")
                add("Quantity Sold")
            }
            CostMethodType.WEIGHTED_AVERAGE, CostMethodType.MOVING_AVERAGE -> {
                add("Total Cost")
                add("Total Quantity")
                add("Purchase Transactions")
            }
            CostMethodType.STANDARD_COST -> {
                add("Standard Cost Rates")
                add("Actual Cost")
                add("Quantity Standards")
                add("Actual Quantities")
            }
            CostMethodType.ACTIVITY_BASED_COSTING -> {
                add("Activity Drivers")
                add("Activity Rates")
                add("Resource Consumption")
                add("Cost Pool Allocations")
            }
            CostMethodType.JOB_ORDER_COSTING -> {
                add("Job Numbers")
                add("Labor Hours")
                add("Material Costs")
                add("Overhead Rates")
            }
            CostMethodType.PROCESS_COSTING -> {
                add("Production Volumes")
                add("Work in Process")
                add("Equivalent Units")
                add("Process Stages")
            }
            else -> {
                add("Cost Data")
                add("Allocation Basis")
            }
        }
        
        if (trackCostComponents) {
            add("Cost Component Breakdown")
        }
        
        if (maintainCostHistory) {
            add("Historical Cost Data")
        }
    }
    
    /**
     * Extends the effective period of this cost method
     */
    fun extendPeriod(newEndDate: LocalDate): CostMethod {
        require(newEndDate.isAfter(effectiveDate)) {
            "New end date must be after effective date"
        }
        
        return copy(
            endDate = newEndDate,
            lastModifiedAt = java.time.LocalDateTime.now()
        )
    }
    
    /**
     * Updates allocation parameters
     */
    fun updateAllocation(
        newAllocationBasis: CostAllocationBasis? = null,
        newAllocationRate: BigDecimal? = null,
        newAllocationPercentage: BigDecimal? = null
    ): CostMethod {
        return copy(
            allocationBasis = newAllocationBasis ?: allocationBasis,
            allocationRate = newAllocationRate,
            allocationPercentage = newAllocationPercentage,
            lastModifiedAt = java.time.LocalDateTime.now()
        )
    }
    
    /**
     * Enables or disables variance analysis
     */
    fun configureVarianceAnalysis(
        enablePrice: Boolean = false,
        enableQuantity: Boolean = false,
        enableEfficiency: Boolean = false,
        enableVolume: Boolean = false,
        thresholdPercentage: BigDecimal? = null
    ): CostMethod {
        return copy(
            enablePriceVariance = enablePrice,
            enableQuantityVariance = enableQuantity,
            enableEfficiencyVariance = enableEfficiency,
            enableVolumeVariance = enableVolume,
            varianceThresholdPercentage = thresholdPercentage,
            lastModifiedAt = java.time.LocalDateTime.now()
        )
    }
    
    /**
     * Gets a summary of the cost method configuration
     */
    fun getSummary(): String = buildString {
        append("$name")
        
        if (!isActive) append(" [INACTIVE]")
        if (isDefault) append(" [DEFAULT]")
        if (isExpired) append(" [EXPIRED]")
        
        append(" - Basis: ${allocationBasis.getDisplayName()}")
        
        allocationRate?.let { rate ->
            append(", Rate: $rate")
        }
        
        allocationPercentage?.let { percentage ->
            append(", Percentage: $percentage%")
        }
        
        if (supportsVarianceFalysis) {
            append(" [Variance Analysis]")
        }
        
        if (trackCostComponents) {
            append(" [Component Tracking]")
        }
    }
    
    // ==================== VALIDATION ====================
    
    init {
        require(name.isNotBlank()) { "Cost method name cannot be blank" }
        require(name.length <= 100) { "Cost method name cannot exceed 100 characters" }
        
        description?.let { desc ->
            require(desc.length <= 500) { "Cost method description cannot exceed 500 characters" }
        }
        
        allocationRate?.let { rate ->
            require(rate >= BigDecimal.ZERO) { "Allocation rate must be non-negative" }
        }
        
        allocationPercentage?.let { percentage ->
            require(percentage >= BigDecimal.ZERO && percentage <= BigDecimal("100")) {
                "Allocation percentage must be between 0 and 100"
            }
        }
        
        require(roundingPrecision >= 0 && roundingPrecision <= 10) {
            "Rounding precision must be between 0 and 10"
        }
        
        minimumCostThreshold?.let { min ->
            require(min >= BigDecimal.ZERO) { "Minimum cost threshold must be non-negative" }
        }
        
        maximumCostThreshold?.let { max ->
            require(max >= BigDecimal.ZERO) { "Maximum cost threshold must be non-negative" }
            minimumCostThreshold?.let { min ->
                require(max >= min) { "Maximum cost threshold must be >= minimum cost threshold" }
            }
        }
        
        varianceThresholdPercentage?.let { threshold ->
            require(threshold >= BigDecimal.ZERO) { "Variance threshold percentage must be non-negative" }
        }
        
        endDate?.let { end ->
            require(!end.isBefore(effectiveDate)) {
                "End date cannot be before effective date"
            }
        }
    }
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension functions for CostMethodType enum
 */
fun CostMethodType.getDisplayName(): String = when (this) {
    CostMethodType.FIFO -> "First In, First Out (FIFO)"
    CostMethodType.LIFO -> "Last In, First Out (LIFO)"
    CostMethodType.WEIGHTED_AVERAGE -> "Weighted Average Cost"
    CostMethodType.MOVING_AVERAGE -> "Moving Average Cost"
    CostMethodType.SPECIFIC_IDENTIFICATION -> "Specific Identification"
    CostMethodType.STANDARD_COST -> "Standard Cost"
    CostMethodType.ACTIVITY_BASED_COSTING -> "Activity-Based Costing (ABC)"
    CostMethodType.TIME_DRIVEN_ABC -> "Time-Driven ABC"
    CostMethodType.RESOURCE_CONSUMPTION_ACCOUNTING -> "Resource Consumption Accounting"
    CostMethodType.PROCESS_COSTING -> "Process Costing"
    CostMethodType.JOB_ORDER_COSTING -> "Job Order Costing"
    CostMethodType.BATCH_COSTING -> "Batch Costing"
    CostMethodType.CONTRACT_COSTING -> "Contract Costing"
    CostMethodType.ABSORPTION_COSTING -> "Absorption Costing"
    CostMethodType.VARIABLE_COSTING -> "Variable Costing"
    CostMethodType.MARGINAL_COSTING -> "Marginal Costing"
    CostMethodType.DIRECT_COSTING -> "Direct Costing"
    CostMethodType.SERVICE_COSTING -> "Service Costing"
    CostMethodType.OPERATION_COSTING -> "Operation Costing"
    CostMethodType.MULTIPLE_COSTING -> "Multiple Costing"
    CostMethodType.DIRECT_ALLOCATION -> "Direct Allocation"
    CostMethodType.STEP_DOWN_ALLOCATION -> "Step-Down Allocation"
    CostMethodType.RECIPROCAL_ALLOCATION -> "Reciprocal Allocation"
    CostMethodType.TRADITIONAL_OVERHEAD -> "Traditional Overhead"
    CostMethodType.MACHINE_HOUR_RATE -> "Machine Hour Rate"
    CostMethodType.LABOR_HOUR_RATE -> "Labor Hour Rate"
    CostMethodType.PRIME_COST_PERCENTAGE -> "Prime Cost Percentage"
    CostMethodType.TARGET_COSTING -> "Target Costing"
    CostMethodType.KAIZEN_COSTING -> "Kaizen Costing"
    CostMethodType.LIFECYCLE_COSTING -> "Lifecycle Costing"
    CostMethodType.QUALITY_COSTING -> "Quality Costing"
    CostMethodType.PROJECT_COSTING -> "Project Costing"
    CostMethodType.EARNED_VALUE_METHOD -> "Earned Value Method"
    CostMethodType.MILESTONE_COSTING -> "Milestone Costing"
    CostMethodType.REPLACEMENT_COST -> "Replacement Cost"
    CostMethodType.NET_REALIZABLE_VALUE -> "Net Realizable Value"
    CostMethodType.FAIR_VALUE_METHOD -> "Fair Value Method"
    CostMethodType.HISTORICAL_COST -> "Historical Cost"
    CostMethodType.CUSTOM_COST_METHOD -> "Custom Cost Method"
    CostMethodType.HYBRID_METHOD -> "Hybrid Method"
}

fun CostMethodType.getDefaultAllocationBasis(): CostAllocationBasis = when (this) {
    CostMethodType.FIFO, CostMethodType.LIFO, CostMethodType.WEIGHTED_AVERAGE,
    CostMethodType.MOVING_AVERAGE, CostMethodType.SPECIFIC_IDENTIFICATION -> CostAllocationBasis.UNITS_PRODUCED
    CostMethodType.STANDARD_COST -> CostAllocationBasis.LABOR_HOURS
    CostMethodType.ACTIVITY_BASED_COSTING, CostMethodType.TIME_DRIVEN_ABC -> CostAllocationBasis.NUMBER_OF_SETUPS
    CostMethodType.JOB_ORDER_COSTING -> CostAllocationBasis.LABOR_HOURS
    CostMethodType.PROCESS_COSTING -> CostAllocationBasis.UNITS_PRODUCED
    CostMethodType.MACHINE_HOUR_RATE -> CostAllocationBasis.MACHINE_HOURS
    CostMethodType.LABOR_HOUR_RATE -> CostAllocationBasis.LABOR_HOURS
    CostMethodType.PRIME_COST_PERCENTAGE -> CostAllocationBasis.PRIME_COST
    else -> CostAllocationBasis.UNITS_PRODUCED
}

fun CostMethodType.requiresStandardCosts(): Boolean = when (this) {
    CostMethodType.STANDARD_COST,
    CostMethodType.TARGET_COSTING,
    CostMethodType.KAIZEN_COSTING -> true
    else -> false
}

fun CostMethodType.requiresActualCosts(): Boolean = when (this) {
    CostMethodType.FIFO,
    CostMethodType.LIFO,
    CostMethodType.WEIGHTED_AVERAGE,
    CostMethodType.MOVING_AVERAGE,
    CostMethodType.SPECIFIC_IDENTIFICATION,
    CostMethodType.ACTIVITY_BASED_COSTING,
    CostMethodType.JOB_ORDER_COSTING,
    CostMethodType.PROCESS_COSTING -> true
    else -> false
}

fun CostMethodType.supportsVarianceAnalysis(): Boolean = when (this) {
    CostMethodType.STANDARD_COST,
    CostMethodType.ACTIVITY_BASED_COSTING,
    CostMethodType.TARGET_COSTING,
    CostMethodType.KAIZEN_COSTING -> true
    else -> false
}

fun CostMethodType.includesOverheadByDefault(): Boolean = when (this) {
    CostMethodType.ABSORPTION_COSTING,
    CostMethodType.ACTIVITY_BASED_COSTING,
    CostMethodType.TIME_DRIVEN_ABC,
    CostMethodType.JOB_ORDER_COSTING,
    CostMethodType.PROCESS_COSTING -> true
    CostMethodType.VARIABLE_COSTING,
    CostMethodType.DIRECT_COSTING,
    CostMethodType.MARGINAL_COSTING -> false
    else -> true
}

fun CostMethodType.requiresPeriodClosing(): Boolean = when (this) {
    CostMethodType.WEIGHTED_AVERAGE,
    CostMethodType.PROCESS_COSTING,
    CostMethodType.ABSORPTION_COSTING,
    CostMethodType.STANDARD_COST -> true
    else -> false
}

fun CostMethodType.enablesCostTraceability(): Boolean = when (this) {
    CostMethodType.SPECIFIC_IDENTIFICATION,
    CostMethodType.ACTIVITY_BASED_COSTING,
    CostMethodType.TIME_DRIVEN_ABC,
    CostMethodType.JOB_ORDER_COSTING,
    CostMethodType.PROJECT_COSTING -> true
    else -> false
}

fun CostMethodType.integratesWithInventory(): Boolean = when (this) {
    CostMethodType.FIFO,
    CostMethodType.LIFO,
    CostMethodType.WEIGHTED_AVERAGE,
    CostMethodType.MOVING_AVERAGE,
    CostMethodType.SPECIFIC_IDENTIFICATION,
    CostMethodType.STANDARD_COST -> true
    else -> false
}

fun CostMethodType.integratesWithProduction(): Boolean = when (this) {
    CostMethodType.STANDARD_COST,
    CostMethodType.ACTIVITY_BASED_COSTING,
    CostMethodType.JOB_ORDER_COSTING,
    CostMethodType.PROCESS_COSTING,
    CostMethodType.ABSORPTION_COSTING -> true
    else -> false
}

fun CostMethodType.integratesWithSales(): Boolean = when (this) {
    CostMethodType.TARGET_COSTING,
    CostMethodType.LIFECYCLE_COSTING,
    CostMethodType.NET_REALIZABLE_VALUE -> true
    else -> false
}

fun CostMethodType.integratesWithPurchasing(): Boolean = when (this) {
    CostMethodType.FIFO,
    CostMethodType.LIFO,
    CostMethodType.WEIGHTED_AVERAGE,
    CostMethodType.STANDARD_COST,
    CostMethodType.REPLACEMENT_COST -> true
    else -> false
}

fun CostAllocationBasis.getDisplayName(): String = when (this) {
    CostAllocationBasis.UNITS_PRODUCED -> "Units Produced"
    CostAllocationBasis.MACHINE_HOURS -> "Machine Hours"
    CostAllocationBasis.LABOR_HOURS -> "Labor Hours"
    CostAllocationBasis.LABOR_COST -> "Labor Cost"
    CostAllocationBasis.MATERIAL_COST -> "Material Cost"
    CostAllocationBasis.PRIME_COST -> "Prime Cost"
    CostAllocationBasis.SALES_VALUE -> "Sales Value"
    CostAllocationBasis.NET_REALIZABLE_VALUE -> "Net Realizable Value"
    CostAllocationBasis.RELATIVE_SALES_VALUE -> "Relative Sales Value"
    CostAllocationBasis.MARKET_VALUE -> "Market Value"
    CostAllocationBasis.NUMBER_OF_SETUPS -> "Number of Setups"
    CostAllocationBasis.NUMBER_OF_ORDERS -> "Number of Orders"
    CostAllocationBasis.NUMBER_OF_BATCHES -> "Number of Batches"
    CostAllocationBasis.INSPECTION_HOURS -> "Inspection Hours"
    CostAllocationBasis.MATERIAL_MOVEMENTS -> "Material Movements"
    CostAllocationBasis.FLOOR_SPACE -> "Floor Space"
    CostAllocationBasis.STORAGE_SPACE -> "Storage Space"
    CostAllocationBasis.FACILITY_UTILIZATION -> "Facility Utilization"
    CostAllocationBasis.PROCESSING_TIME -> "Processing Time"
    CostAllocationBasis.CYCLE_TIME -> "Cycle Time"
    CostAllocationBasis.SETUP_TIME -> "Setup Time"
    CostAllocationBasis.WAIT_TIME -> "Wait Time"
    CostAllocationBasis.WEIGHT -> "Weight"
    CostAllocationBasis.VOLUME -> "Volume"
    CostAllocationBasis.QUANTITY -> "Quantity"
    CostAllocationBasis.CUSTOM_BASIS -> "Custom Basis"
}

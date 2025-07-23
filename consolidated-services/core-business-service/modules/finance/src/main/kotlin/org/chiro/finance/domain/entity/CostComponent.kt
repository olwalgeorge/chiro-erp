package org.chiro.finance.domain.entity

import org.chiro.finance.domain.valueobject.*
import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

/**
 * Cost Component - Product and service cost tracking
 * 
 * Represents individual cost elements for products, services, and operations with:
 * - Detailed cost breakdown and analysis
 * - Multiple cost method support (FIFO, LIFO, Average, Standard)
 * - Activity-based costing integration
 * - Cost center and department allocation
 * - Variance analysis and reporting
 * 
 * Key Features:
 * - Multi-dimensional cost tracking
 * - Real-time cost calculation
 * - Cost rollup and aggregation
 * - Variance analysis capabilities
 * - Activity-based costing support
 * 
 * Business Rules:
 * - Cost components must have valid cost object
 * - Standard costs require approval for changes
 * - Cost variances must be explained and analyzed
 * - Cost allocations must total 100% when applicable
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Serializable
data class CostComponent(
    val id: UUID = UUID.randomUUID(),
    
    @field:NotNull(message = "Cost object ID is required")
    val costObjectId: UUID,
    
    @field:NotNull(message = "Cost object type is required")
    val costObjectType: CostObjectType,
    
    @field:NotBlank(message = "Component name cannot be blank")
    @field:Size(min = 1, max = 200, message = "Component name must be between 1 and 200 characters")
    val componentName: String,
    
    @field:NotNull(message = "Cost type is required")
    val costType: CostType,
    
    @field:NotNull(message = "Cost category is required")
    val costCategory: CostCategory,
    
    @field:NotNull(message = "Cost amount is required")
    @field:Valid
    val costAmount: FinancialAmount,
    
    @field:NotNull(message = "Cost method is required")
    @field:Valid
    val costMethod: CostMethod,
    
    @field:NotNull(message = "Cost period is required")
    val costPeriod: UUID, // Reference to accounting period
    
    val costCenterId: UUID? = null,
    val departmentId: UUID? = null,
    val activityId: UUID? = null,
    
    @field:NotNull(message = "Effective date is required")
    val effectiveDate: LocalDateTime,
    
    val expirationDate: LocalDateTime? = null,
    
    @field:NotNull(message = "Status is required")
    val status: CostComponentStatus = CostComponentStatus.ACTIVE,
    
    val standardCost: FinancialAmount? = null,
    val actualCost: FinancialAmount? = null,
    val budgetedCost: FinancialAmount? = null,
    
    val quantity: Double = 1.0,
    val unitOfMeasure: String? = null,
    val unitCost: FinancialAmount? = null,
    
    val allocationPercentage: Double? = null,
    val allocationBasis: AllocationBasis? = null,
    
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,
    
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
    
    val metadata: Map<String, Any> = emptyMap(),
    
    val isRecurring: Boolean = false,
    val recurringFrequency: RecurringFrequency? = null
) {
    
    init {
        require(componentName.isNotBlank()) { "Component name cannot be blank" }
        require(costAmount.amount >= 0.0) { "Cost amount cannot be negative" }
        require(quantity > 0.0) { "Quantity must be positive" }
        
        // Validate allocation percentage
        if (allocationPercentage != null) {
            require(allocationPercentage in 0.0..100.0) {
                "Allocation percentage must be between 0 and 100"
            }
            require(allocationBasis != null) {
                "Allocation basis is required when allocation percentage is specified"
            }
        }
        
        // Validate unit cost consistency
        if (unitCost != null) {
            val calculatedTotal = unitCost.amount * quantity
            val tolerance = 0.01 // Allow for rounding differences
            require(kotlin.math.abs(calculatedTotal - costAmount.amount) <= tolerance) {
                "Unit cost × quantity must equal total cost amount"
            }
        }
        
        // Validate date ranges
        if (expirationDate != null) {
            require(expirationDate.isAfter(effectiveDate)) {
                "Expiration date must be after effective date"
            }
        }
        
        // Validate recurring cost settings
        if (isRecurring) {
            require(recurringFrequency != null) {
                "Recurring frequency is required for recurring costs"
            }
        }
    }
    
    /**
     * Calculates variance between actual and standard cost
     */
    fun calculateVariance(): CostVariance? {
        return if (actualCost != null && standardCost != null) {
            val varianceAmount = actualCost.amount - standardCost.amount
            val variancePercentage = if (standardCost.amount != 0.0) {
                (varianceAmount / standardCost.amount) * 100.0
            } else 0.0
            
            CostVariance(
                costComponentId = id,
                varianceAmount = FinancialAmount(varianceAmount, actualCost.currency),
                variancePercentage = variancePercentage,
                varianceType = when {
                    varianceAmount > 0 -> VarianceType.UNFAVORABLE
                    varianceAmount < 0 -> VarianceType.FAVORABLE
                    else -> VarianceType.NEUTRAL
                }
            )
        } else null
    }
    
    /**
     * Gets the effective cost amount based on cost method
     */
    fun getEffectiveCost(): FinancialAmount {
        return when (costMethod) {
            CostMethod.STANDARD -> standardCost ?: costAmount
            CostMethod.ACTUAL -> actualCost ?: costAmount
            else -> costAmount
        }
    }
    
    /**
     * Calculates unit cost if not explicitly set
     */
    fun getCalculatedUnitCost(): FinancialAmount {
        return unitCost ?: FinancialAmount(
            amount = costAmount.amount / quantity,
            currency = costAmount.currency
        )
    }
    
    /**
     * Updates actual cost with variance tracking
     */
    fun updateActualCost(
        newActualCost: FinancialAmount,
        updatedBy: UUID,
        notes: String? = null
    ): CostComponent {
        return copy(
            actualCost = newActualCost,
            modifiedBy = updatedBy,
            modifiedDate = LocalDateTime.now(),
            notes = notes?.let { existingNotes ->
                if (this.notes.isNullOrBlank()) existingNotes
                else "${this.notes}\n$existingNotes"
            } ?: this.notes
        )
    }
    
    /**
     * Updates standard cost with approval
     */
    fun updateStandardCost(
        newStandardCost: FinancialAmount,
        approvedBy: UUID,
        effectiveDate: LocalDateTime = LocalDateTime.now()
    ): CostComponent {
        return copy(
            standardCost = newStandardCost,
            effectiveDate = effectiveDate,
            approvedBy = approvedBy,
            approvedDate = LocalDateTime.now(),
            modifiedBy = approvedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Allocates cost to multiple cost centers
     */
    fun allocate(
        allocationPercentage: Double,
        allocationBasis: AllocationBasis,
        allocatedBy: UUID
    ): CostComponent {
        require(allocationPercentage in 0.0..100.0) {
            "Allocation percentage must be between 0 and 100"
        }
        
        return copy(
            allocationPercentage = allocationPercentage,
            allocationBasis = allocationBasis,
            modifiedBy = allocatedBy,
            modifiedDate = LocalDateTime.now()
        )
    }
    
    /**
     * Deactivates the cost component
     */
    fun deactivate(deactivatedBy: UUID, reason: String? = null): CostComponent {
        return copy(
            status = CostComponentStatus.INACTIVE,
            expirationDate = LocalDateTime.now(),
            modifiedBy = deactivatedBy,
            modifiedDate = LocalDateTime.now(),
            notes = reason?.let { existingNotes ->
                if (this.notes.isNullOrBlank()) "Deactivated: $it"
                else "${this.notes}\nDeactivated: $it"
            } ?: this.notes
        )
    }
    
    /**
     * Validates cost component for business rules
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (costAmount.amount < 0) {
            errors.add("Cost amount cannot be negative")
        }
        
        if (quantity <= 0) {
            errors.add("Quantity must be positive")
        }
        
        if (allocationPercentage != null && allocationPercentage !in 0.0..100.0) {
            errors.add("Allocation percentage must be between 0 and 100")
        }
        
        if (allocationPercentage != null && allocationBasis == null) {
            errors.add("Allocation basis is required when allocation percentage is specified")
        }
        
        if (unitCost != null) {
            val calculatedTotal = unitCost.amount * quantity
            val tolerance = 0.01
            if (kotlin.math.abs(calculatedTotal - costAmount.amount) > tolerance) {
                errors.add("Unit cost × quantity must equal total cost amount")
            }
        }
        
        if (expirationDate != null && !expirationDate.isAfter(effectiveDate)) {
            errors.add("Expiration date must be after effective date")
        }
        
        if (isRecurring && recurringFrequency == null) {
            errors.add("Recurring frequency is required for recurring costs")
        }
        
        return errors
    }
    
    companion object {
        /**
         * Creates a new cost component
         */
        fun create(
            costObjectId: UUID,
            costObjectType: CostObjectType,
            componentName: String,
            costType: CostType,
            costCategory: CostCategory,
            costAmount: FinancialAmount,
            costMethod: CostMethod,
            costPeriod: UUID,
            createdBy: UUID,
            quantity: Double = 1.0,
            description: String? = null
        ): CostComponent {
            return CostComponent(
                costObjectId = costObjectId,
                costObjectType = costObjectType,
                componentName = componentName,
                costType = costType,
                costCategory = costCategory,
                costAmount = costAmount,
                costMethod = costMethod,
                costPeriod = costPeriod,
                createdBy = createdBy,
                quantity = quantity,
                description = description,
                effectiveDate = LocalDateTime.now()
            )
        }
        
        /**
         * Creates standard cost component
         */
        fun createStandard(
            costObjectId: UUID,
            costObjectType: CostObjectType,
            componentName: String,
            standardCost: FinancialAmount,
            costPeriod: UUID,
            createdBy: UUID,
            quantity: Double = 1.0
        ): CostComponent {
            return CostComponent(
                costObjectId = costObjectId,
                costObjectType = costObjectType,
                componentName = componentName,
                costType = CostType.STANDARD,
                costCategory = CostCategory.MATERIAL, // Default
                costAmount = standardCost,
                costMethod = CostMethod.STANDARD,
                costPeriod = costPeriod,
                createdBy = createdBy,
                quantity = quantity,
                standardCost = standardCost,
                effectiveDate = LocalDateTime.now()
            )
        }
    }
}

/**
 * Cost variance tracking
 */
@Serializable
data class CostVariance(
    val costComponentId: UUID,
    val varianceAmount: FinancialAmount,
    val variancePercentage: Double,
    val varianceType: VarianceType
)

/**
 * Cost object types for classification
 */
@Serializable
enum class CostObjectType(val description: String) {
    PRODUCT("Product"),
    SERVICE("Service"),
    PROJECT("Project"),
    ACTIVITY("Activity"),
    COST_CENTER("Cost Center"),
    DEPARTMENT("Department"),
    JOB_ORDER("Job Order"),
    PROCESS("Process");
}

/**
 * Cost types for categorization
 */
@Serializable
enum class CostType(val description: String) {
    STANDARD("Standard Cost"),
    ACTUAL("Actual Cost"),
    BUDGETED("Budgeted Cost"),
    ESTIMATED("Estimated Cost"),
    TARGET("Target Cost");
}

/**
 * Cost categories for classification
 */
@Serializable
enum class CostCategory(val description: String) {
    MATERIAL("Material Cost"),
    LABOR("Labor Cost"),
    OVERHEAD("Overhead Cost"),
    DIRECT("Direct Cost"),
    INDIRECT("Indirect Cost"),
    FIXED("Fixed Cost"),
    VARIABLE("Variable Cost"),
    SEMI_VARIABLE("Semi-Variable Cost");
}

/**
 * Allocation basis for cost distribution
 */
@Serializable
enum class AllocationBasis(val description: String) {
    DIRECT_LABOR_HOURS("Direct Labor Hours"),
    MACHINE_HOURS("Machine Hours"),
    MATERIAL_COST("Material Cost"),
    LABOR_COST("Labor Cost"),
    UNITS_PRODUCED("Units Produced"),
    SQUARE_FOOTAGE("Square Footage"),
    EMPLOYEE_COUNT("Employee Count"),
    REVENUE("Revenue"),
    CUSTOM("Custom Basis");
}

/**
 * Cost component status
 */
@Serializable
enum class CostComponentStatus(val description: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    OBSOLETE("Obsolete");
}

/**
 * Recurring frequency for cost components
 */
@Serializable
enum class RecurringFrequency(val description: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    ANNUALLY("Annually");
}

/**
 * Variance types for analysis
 */
@Serializable
enum class VarianceType(val description: String) {
    FAVORABLE("Favorable"),
    UNFAVORABLE("Unfavorable"),
    NEUTRAL("Neutral");
}

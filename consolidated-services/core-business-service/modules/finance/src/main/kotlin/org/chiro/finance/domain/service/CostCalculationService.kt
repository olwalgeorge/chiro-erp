package org.chiro.finance.domain.service

import org.chiro.finance.domain.entity.*
import org.chiro.finance.domain.valueobject.*
import org.chiro.finance.domain.exception.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.LocalDateTime
import java.util.*
import kotlin.math.abs

/**
 * Cost Calculation Service - Advanced cost accounting and calculation engine
 * 
 * Provides comprehensive cost calculation capabilities including:
 * - Multi-method cost calculation (FIFO, LIFO, Weighted Average, Standard)
 * - Activity-based costing (ABC) calculations
 * - Cost allocation and absorption
 * - Variance analysis and reporting
 * - Real-time cost updates
 * 
 * Key Features:
 * - Support for all major costing methods
 * - Real-time cost calculation and updates
 * - Multi-dimensional cost tracking
 * - Variance analysis and alerts
 * - Cost rollup and aggregation
 * 
 * Business Rules:
 * - Cost calculations must be consistent with selected method
 * - Standard costs require approval for changes
 * - Variances above threshold require investigation
 * - Cost allocations must total 100%
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@ApplicationScoped
class CostCalculationService {
    
    @Inject
    lateinit var costComponentRepository: CostComponentRepository
    
    @Inject
    lateinit var fiscalPeriodRepository: FiscalPeriodRepository
    
    @Inject
    lateinit var auditLogRepository: AuditLogRepository
    
    /**
     * Calculates total cost for a cost object using specified method
     */
    suspend fun calculateTotalCost(
        costObjectId: UUID,
        costObjectType: CostObjectType,
        costMethod: CostMethod,
        asOfDate: LocalDateTime = LocalDateTime.now()
    ): FinancialAmount {
        val costComponents = costComponentRepository.findByCostObject(
            costObjectId = costObjectId,
            costObjectType = costObjectType,
            effectiveDate = asOfDate
        )
        
        return when (costMethod) {
            CostMethod.FIFO -> calculateFIFOCost(costComponents)
            CostMethod.LIFO -> calculateLIFOCost(costComponents)
            CostMethod.WEIGHTED_AVERAGE -> calculateWeightedAverageCost(costComponents)
            CostMethod.STANDARD -> calculateStandardCost(costComponents)
            CostMethod.ACTUAL -> calculateActualCost(costComponents)
        }
    }
    
    /**
     * Calculates unit cost for a specific quantity
     */
    suspend fun calculateUnitCost(
        costObjectId: UUID,
        costObjectType: CostObjectType,
        quantity: Double,
        costMethod: CostMethod,
        asOfDate: LocalDateTime = LocalDateTime.now()
    ): FinancialAmount {
        require(quantity > 0) { "Quantity must be positive" }
        
        val totalCost = calculateTotalCost(costObjectId, costObjectType, costMethod, asOfDate)
        
        return FinancialAmount(
            amount = totalCost.amount / quantity,
            currency = totalCost.currency
        )
    }
    
    /**
     * Performs activity-based costing calculation
     */
    suspend fun calculateActivityBasedCost(
        costObjectId: UUID,
        costObjectType: CostObjectType,
        activityDrivers: Map<UUID, Double>, // Activity ID to driver quantity
        costPools: Map<UUID, FinancialAmount>, // Cost pool ID to total cost
        asOfDate: LocalDateTime = LocalDateTime.now()
    ): ActivityBasedCostResult {
        val allocatedCosts = mutableMapOf<UUID, FinancialAmount>()
        var totalAllocatedCost = 0.0
        val baseCurrency = Currency.USD // Assuming USD as base currency
        
        for ((activityId, driverQuantity) in activityDrivers) {
            val costPool = costPools[activityId] 
                ?: throw CostCalculationException("Cost pool not found for activity: $activityId")
            
            // Calculate cost per driver unit
            val totalDriverQuantity = getTotalDriverQuantity(activityId, asOfDate)
            require(totalDriverQuantity > 0) { "Total driver quantity must be positive" }
            
            val costPerDriver = costPool.amount / totalDriverQuantity
            val allocatedAmount = costPerDriver * driverQuantity
            
            allocatedCosts[activityId] = FinancialAmount(allocatedAmount, baseCurrency)
            totalAllocatedCost += allocatedAmount
        }
        
        return ActivityBasedCostResult(
            costObjectId = costObjectId,
            costObjectType = costObjectType,
            allocatedCosts = allocatedCosts,
            totalCost = FinancialAmount(totalAllocatedCost, baseCurrency),
            calculationDate = asOfDate
        )
    }
    
    /**
     * Calculates cost variances between actual and standard
     */
    suspend fun calculateCostVariances(
        costObjectId: UUID,
        costObjectType: CostObjectType,
        periodId: UUID
    ): List<CostVarianceAnalysis> {
        val costComponents = costComponentRepository.findByCostObjectAndPeriod(
            costObjectId = costObjectId,
            costObjectType = costObjectType,
            periodId = periodId
        )
        
        return costComponents.mapNotNull { component ->
            if (component.actualCost != null && component.standardCost != null) {
                calculateVarianceAnalysis(component)
            } else null
        }
    }
    
    /**
     * Allocates overhead costs to cost objects
     */
    suspend fun allocateOverheadCosts(
        overheadCostPool: FinancialAmount,
        allocationBasis: AllocationBasis,
        costObjects: List<CostObjectAllocation>,
        periodId: UUID
    ): List<OverheadAllocationResult> {
        require(costObjects.isNotEmpty()) { "Cost objects list cannot be empty" }
        
        val totalAllocationBase = costObjects.sumOf { it.allocationBase }
        require(totalAllocationBase > 0) { "Total allocation base must be positive" }
        
        val allocationRate = overheadCostPool.amount / totalAllocationBase
        
        return costObjects.map { costObject ->
            val allocatedAmount = allocationRate * costObject.allocationBase
            
            OverheadAllocationResult(
                costObjectId = costObject.costObjectId,
                costObjectType = costObject.costObjectType,
                allocationBasis = allocationBasis,
                allocationBase = costObject.allocationBase,
                allocationRate = allocationRate,
                allocatedAmount = FinancialAmount(allocatedAmount, overheadCostPool.currency),
                periodId = periodId
            )
        }
    }
    
    /**
     * Updates standard costs with approval workflow
     */
    suspend fun updateStandardCosts(
        costObjectId: UUID,
        costObjectType: CostObjectType,
        newStandardCosts: Map<String, FinancialAmount>, // Component name to cost
        approvedBy: UUID,
        effectiveDate: LocalDateTime = LocalDateTime.now()
    ) {
        val existingComponents = costComponentRepository.findByCostObject(
            costObjectId = costObjectId,
            costObjectType = costObjectType
        )
        
        for ((componentName, newCost) in newStandardCosts) {
            val existingComponent = existingComponents.find { it.componentName == componentName }
            
            if (existingComponent != null) {
                // Update existing component
                val updatedComponent = existingComponent.updateStandardCost(
                    newStandardCost = newCost,
                    approvedBy = approvedBy,
                    effectiveDate = effectiveDate
                )
                costComponentRepository.save(updatedComponent)
                
                // Log the change
                auditLogRepository.save(
                    AuditLogEntry.forUpdate(
                        entityType = "CostComponent",
                        entityId = existingComponent.id,
                        userId = approvedBy,
                        username = "system", // Would be resolved from user service
                        oldValues = mapOf("standardCost" to existingComponent.standardCost),
                        newValues = mapOf("standardCost" to newCost),
                        description = "Standard cost updated for $componentName"
                    )
                )
            } else {
                // Create new component
                val newComponent = CostComponent.createStandard(
                    costObjectId = costObjectId,
                    costObjectType = costObjectType,
                    componentName = componentName,
                    standardCost = newCost,
                    costPeriod = getCurrentPeriodId(),
                    createdBy = approvedBy
                )
                costComponentRepository.save(newComponent)
                
                // Log the creation
                auditLogRepository.save(
                    AuditLogEntry.forCreate(
                        entityType = "CostComponent",
                        entityId = newComponent.id,
                        userId = approvedBy,
                        username = "system",
                        newValues = mapOf(
                            "componentName" to componentName,
                            "standardCost" to newCost
                        ),
                        description = "New standard cost component created"
                    )
                )
            }
        }
    }
    
    /**
     * Performs cost rollup for hierarchical cost objects
     */
    suspend fun performCostRollup(
        parentCostObjectId: UUID,
        costMethod: CostMethod,
        includeSubComponents: Boolean = true
    ): CostRollupResult {
        val directCosts = calculateTotalCost(
            costObjectId = parentCostObjectId,
            costObjectType = CostObjectType.PRODUCT, // Default, would be passed as parameter
            costMethod = costMethod
        )
        
        var totalRolledUpCost = directCosts.amount
        val childCosts = mutableMapOf<UUID, FinancialAmount>()
        
        if (includeSubComponents) {
            val childCostObjects = getChildCostObjects(parentCostObjectId)
            
            for (childId in childCostObjects) {
                val childCost = calculateTotalCost(
                    costObjectId = childId,
                    costObjectType = CostObjectType.PRODUCT,
                    costMethod = costMethod
                )
                childCosts[childId] = childCost
                totalRolledUpCost += childCost.amount
            }
        }
        
        return CostRollupResult(
            parentCostObjectId = parentCostObjectId,
            directCosts = directCosts,
            childCosts = childCosts,
            totalCost = FinancialAmount(totalRolledUpCost, directCosts.currency),
            costMethod = costMethod,
            calculationDate = LocalDateTime.now()
        )
    }
    
    /**
     * Calculates cost absorption rate for overhead allocation
     */
    suspend fun calculateAbsorptionRate(
        overheadCostPool: FinancialAmount,
        plannedActivity: Double,
        actualActivity: Double? = null
    ): AbsorptionRateResult {
        require(plannedActivity > 0) { "Planned activity must be positive" }
        
        val plannedRate = overheadCostPool.amount / plannedActivity
        
        val actualRate = if (actualActivity != null && actualActivity > 0) {
            overheadCostPool.amount / actualActivity
        } else null
        
        val underAbsorbed = if (actualActivity != null && actualActivity < plannedActivity) {
            (plannedActivity - actualActivity) * plannedRate
        } else 0.0
        
        val overAbsorbed = if (actualActivity != null && actualActivity > plannedActivity) {
            (actualActivity - plannedActivity) * plannedRate
        } else 0.0
        
        return AbsorptionRateResult(
            plannedRate = plannedRate,
            actualRate = actualRate,
            underAbsorbed = FinancialAmount(underAbsorbed, overheadCostPool.currency),
            overAbsorbed = FinancialAmount(overAbsorbed, overheadCostPool.currency),
            plannedActivity = plannedActivity,
            actualActivity = actualActivity
        )
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private fun calculateFIFOCost(components: List<CostComponent>): FinancialAmount {
        if (components.isEmpty()) return FinancialAmount(0.0, Currency.USD)
        
        val sortedComponents = components.sortedBy { it.effectiveDate }
        var totalCost = 0.0
        val baseCurrency = sortedComponents.first().costAmount.currency
        
        for (component in sortedComponents) {
            totalCost += component.getEffectiveCost().amount
        }
        
        return FinancialAmount(totalCost, baseCurrency)
    }
    
    private fun calculateLIFOCost(components: List<CostComponent>): FinancialAmount {
        if (components.isEmpty()) return FinancialAmount(0.0, Currency.USD)
        
        val sortedComponents = components.sortedByDescending { it.effectiveDate }
        var totalCost = 0.0
        val baseCurrency = sortedComponents.first().costAmount.currency
        
        for (component in sortedComponents) {
            totalCost += component.getEffectiveCost().amount
        }
        
        return FinancialAmount(totalCost, baseCurrency)
    }
    
    private fun calculateWeightedAverageCost(components: List<CostComponent>): FinancialAmount {
        if (components.isEmpty()) return FinancialAmount(0.0, Currency.USD)
        
        var totalCost = 0.0
        var totalQuantity = 0.0
        val baseCurrency = components.first().costAmount.currency
        
        for (component in components) {
            totalCost += component.getEffectiveCost().amount * component.quantity
            totalQuantity += component.quantity
        }
        
        val averageCost = if (totalQuantity > 0) totalCost / totalQuantity else 0.0
        return FinancialAmount(averageCost, baseCurrency)
    }
    
    private fun calculateStandardCost(components: List<CostComponent>): FinancialAmount {
        if (components.isEmpty()) return FinancialAmount(0.0, Currency.USD)
        
        var totalCost = 0.0
        val baseCurrency = components.first().costAmount.currency
        
        for (component in components) {
            totalCost += (component.standardCost ?: component.costAmount).amount
        }
        
        return FinancialAmount(totalCost, baseCurrency)
    }
    
    private fun calculateActualCost(components: List<CostComponent>): FinancialAmount {
        if (components.isEmpty()) return FinancialAmount(0.0, Currency.USD)
        
        var totalCost = 0.0
        val baseCurrency = components.first().costAmount.currency
        
        for (component in components) {
            totalCost += (component.actualCost ?: component.costAmount).amount
        }
        
        return FinancialAmount(totalCost, baseCurrency)
    }
    
    private fun calculateVarianceAnalysis(component: CostComponent): CostVarianceAnalysis {
        val actualCost = component.actualCost!!
        val standardCost = component.standardCost!!
        
        val varianceAmount = actualCost.amount - standardCost.amount
        val variancePercentage = if (standardCost.amount != 0.0) {
            (varianceAmount / standardCost.amount) * 100.0
        } else 0.0
        
        val varianceType = when {
            varianceAmount > 0 -> VarianceType.UNFAVORABLE
            varianceAmount < 0 -> VarianceType.FAVORABLE
            else -> VarianceType.NEUTRAL
        }
        
        val isSignificant = abs(variancePercentage) > 5.0 // 5% threshold
        
        return CostVarianceAnalysis(
            costComponentId = component.id,
            componentName = component.componentName,
            standardCost = standardCost,
            actualCost = actualCost,
            varianceAmount = FinancialAmount(varianceAmount, actualCost.currency),
            variancePercentage = variancePercentage,
            varianceType = varianceType,
            isSignificant = isSignificant,
            analysisDate = LocalDateTime.now()
        )
    }
    
    private suspend fun getTotalDriverQuantity(activityId: UUID, asOfDate: LocalDateTime): Double {
        // This would query activity driver data from repository
        // Placeholder implementation
        return 100.0
    }
    
    private suspend fun getCurrentPeriodId(): UUID {
        // This would get current fiscal period from repository
        // Placeholder implementation
        return UUID.randomUUID()
    }
    
    private suspend fun getChildCostObjects(parentId: UUID): List<UUID> {
        // This would query hierarchical cost object relationships
        // Placeholder implementation
        return emptyList()
    }
}

// ==================== RESULT CLASSES ====================

/**
 * Activity-based costing calculation result
 */
data class ActivityBasedCostResult(
    val costObjectId: UUID,
    val costObjectType: CostObjectType,
    val allocatedCosts: Map<UUID, FinancialAmount>,
    val totalCost: FinancialAmount,
    val calculationDate: LocalDateTime
)

/**
 * Cost variance analysis result
 */
data class CostVarianceAnalysis(
    val costComponentId: UUID,
    val componentName: String,
    val standardCost: FinancialAmount,
    val actualCost: FinancialAmount,
    val varianceAmount: FinancialAmount,
    val variancePercentage: Double,
    val varianceType: VarianceType,
    val isSignificant: Boolean,
    val analysisDate: LocalDateTime
)

/**
 * Cost object allocation information
 */
data class CostObjectAllocation(
    val costObjectId: UUID,
    val costObjectType: CostObjectType,
    val allocationBase: Double
)

/**
 * Overhead allocation result
 */
data class OverheadAllocationResult(
    val costObjectId: UUID,
    val costObjectType: CostObjectType,
    val allocationBasis: AllocationBasis,
    val allocationBase: Double,
    val allocationRate: Double,
    val allocatedAmount: FinancialAmount,
    val periodId: UUID
)

/**
 * Cost rollup calculation result
 */
data class CostRollupResult(
    val parentCostObjectId: UUID,
    val directCosts: FinancialAmount,
    val childCosts: Map<UUID, FinancialAmount>,
    val totalCost: FinancialAmount,
    val costMethod: CostMethod,
    val calculationDate: LocalDateTime
)

/**
 * Absorption rate calculation result
 */
data class AbsorptionRateResult(
    val plannedRate: Double,
    val actualRate: Double?,
    val underAbsorbed: FinancialAmount,
    val overAbsorbed: FinancialAmount,
    val plannedActivity: Double,
    val actualActivity: Double?
)

/**
 * Cost calculation exception
 */
class CostCalculationException(message: String, cause: Throwable? = null) : Exception(message, cause)

// Repository interfaces needed for cost calculation
interface CostComponentRepository {
    suspend fun findByCostObject(costObjectId: UUID, costObjectType: CostObjectType, effectiveDate: LocalDateTime? = null): List<CostComponent>
    suspend fun findByCostObjectAndPeriod(costObjectId: UUID, costObjectType: CostObjectType, periodId: UUID): List<CostComponent>
    suspend fun save(component: CostComponent): CostComponent
}

interface FiscalPeriodRepository {
    suspend fun getCurrentPeriod(): UUID?
}

interface AuditLogRepository {
    suspend fun save(auditLog: AuditLogEntry): AuditLogEntry
}

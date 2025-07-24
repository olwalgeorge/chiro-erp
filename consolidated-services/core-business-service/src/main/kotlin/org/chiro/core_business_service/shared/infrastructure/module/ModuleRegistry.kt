package org.chiro.core_business_service.shared.infrastructure.module

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Module registry for dynamic service discovery and health management.
 * 
 * This service provides a central registry for all ERP modules within the
 * Core Business Service, enabling:
 * - Dynamic module registration and discovery
 * - Health monitoring and status reporting
 * - Module metadata and capabilities tracking
 * - Lifecycle management hooks
 */
@ApplicationScoped
class ModuleRegistry {

    private val logger: Logger = LoggerFactory.getLogger(ModuleRegistry::class.java)
    
    private val modules = ConcurrentHashMap<String, ModuleInfo>()
    private val moduleHealthCache = ConcurrentHashMap<String, HealthStatus>()

    /**
     * Register a module with the registry.
     * 
     * @param name The unique module name
     * @param facade The module facade implementation
     * @param healthCheck The health check function
     * @param metadata Additional module metadata
     */
    fun registerModule(
        name: String,
        facade: Any,
        healthCheck: suspend () -> Boolean,
        metadata: ModuleMetadata = ModuleMetadata()
    ) {
        require(name.isNotBlank()) { "Module name cannot be blank" }
        require(!modules.containsKey(name)) { "Module '$name' is already registered" }

        val moduleInfo = ModuleInfo(
            name = name,
            facade = facade,
            healthCheck = healthCheck,
            metadata = metadata,
            registeredAt = Instant.now()
        )

        modules[name] = moduleInfo
        logger.info("📦 Module registered: $name with facade: ${facade::class.simpleName}")
        
        // Initialize health status asynchronously
        GlobalScope.launch {
            updateModuleHealth(name)
        }
    }

    /**
     * Unregister a module from the registry.
     * 
     * @param name The module name to unregister
     * @return true if the module was unregistered, false if not found
     */
    fun unregisterModule(name: String): Boolean {
        return if (modules.remove(name) != null) {
            moduleHealthCache.remove(name)
            logger.info("📦 Module unregistered: $name")
            true
        } else {
            logger.warn("Attempted to unregister unknown module: $name")
            false
        }
    }

    /**
     * Get a module facade by name.
     * 
     * @param name The module name
     * @return The module facade if found, null otherwise
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getModuleFacade(name: String): T? {
        return modules[name]?.facade as? T
    }

    /**
     * Get module information by name.
     * 
     * @param name The module name
     * @return The module information if found, null otherwise
     */
    fun getModuleInfo(name: String): ModuleInfo? {
        return modules[name]
    }

    /**
     * Get all registered module names.
     * 
     * @return Set of all registered module names
     */
    fun getRegisteredModules(): Set<String> {
        return modules.keys.toSet()
    }

    /**
     * Get the count of registered modules.
     * 
     * @return The number of registered modules
     */
    fun getModuleCount(): Int {
        return modules.size
    }

    /**
     * Check if a module is registered.
     * 
     * @param name The module name
     * @return true if the module is registered, false otherwise
     */
    fun isModuleRegistered(name: String): Boolean {
        return modules.containsKey(name)
    }

    /**
     * Get health status for all modules.
     * 
     * @return Map of module names to their health status
     */
    suspend fun getModuleHealth(): Map<String, String> {
        val healthMap = mutableMapOf<String, String>()

        modules.forEach { (name, moduleInfo) ->
            val status = checkModuleHealth(moduleInfo)
            healthMap[name] = status.status
            moduleHealthCache[name] = status
        }

        return healthMap
    }

    /**
     * Get detailed health information for all modules.
     * 
     * @return Map of module names to their detailed health status
     */
    suspend fun getDetailedModuleHealth(): Map<String, HealthStatus> {
        val healthMap = mutableMapOf<String, HealthStatus>()

        modules.forEach { (name, moduleInfo) ->
            val status = checkModuleHealth(moduleInfo)
            healthMap[name] = status
            moduleHealthCache[name] = status
        }

        return healthMap
    }

    /**
     * Get health status for a specific module.
     * 
     * @param name The module name
     * @return The health status if the module exists, null otherwise
     */
    suspend fun getModuleHealth(name: String): HealthStatus? {
        val moduleInfo = modules[name] ?: return null
        
        val status = checkModuleHealth(moduleInfo)
        moduleHealthCache[name] = status
        return status
    }

    /**
     * Update health status for a specific module.
     * 
     * @param name The module name
     */
    private suspend fun updateModuleHealth(name: String) {
        val moduleInfo = modules[name]
        if (moduleInfo != null) {
            val status = checkModuleHealth(moduleInfo)
            moduleHealthCache[name] = status
        }
    }

    /**
     * Check the health of a specific module.
     * 
     * @param moduleInfo The module information
     * @return The health status
     */
    private suspend fun checkModuleHealth(moduleInfo: ModuleInfo): HealthStatus {
        return try {
            val startTime = System.currentTimeMillis()
            val isHealthy = moduleInfo.healthCheck()
            val responseTime = System.currentTimeMillis() - startTime

            HealthStatus(
                status = if (isHealthy) "UP" else "DOWN",
                timestamp = Instant.now(),
                responseTimeMs = responseTime,
                message = if (isHealthy) "Module is healthy" else "Module health check failed",
                details = mapOf(
                    "module" to moduleInfo.name,
                    "facade" to (moduleInfo.facade::class.simpleName ?: "Unknown"),
                    "registeredAt" to moduleInfo.registeredAt.toString()
                )
            )

        } catch (e: Exception) {
            logger.error("Health check failed for module: ${moduleInfo.name}", e)
            
            HealthStatus(
                status = "DOWN",
                timestamp = Instant.now(),
                responseTimeMs = -1,
                message = "Health check threw exception: ${e.message}",
                details = mapOf(
                    "module" to moduleInfo.name,
                    "error" to (e::class.simpleName ?: "Unknown"),
                    "errorMessage" to (e.message ?: "Unknown error")
                )
            )
        }
    }

    /**
     * Get module statistics and summary information.
     * 
     * @return Module registry statistics
     */
    suspend fun getModuleStatistics(): ModuleStatistics {
        val health = getDetailedModuleHealth()
        val upCount = health.values.count { it.status == "UP" }
        val downCount = health.values.count { it.status == "DOWN" }

        return ModuleStatistics(
            totalModules = modules.size,
            healthyModules = upCount,
            unhealthyModules = downCount,
            averageResponseTime = health.values
                .filter { it.responseTimeMs > 0 }
                .map { it.responseTimeMs }
                .average()
                .takeIf { !it.isNaN() } ?: 0.0,
            moduleNames = modules.keys.toList(),
            lastHealthCheck = Instant.now()
        )
    }
}

/**
 * Information about a registered module.
 */
data class ModuleInfo(
    val name: String,
    val facade: Any,
    val healthCheck: suspend () -> Boolean,
    val metadata: ModuleMetadata,
    val registeredAt: Instant
)

/**
 * Metadata for a module.
 */
data class ModuleMetadata(
    val version: String = "1.0.0",
    val description: String = "",
    val capabilities: List<String> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val tags: Map<String, String> = emptyMap()
)

/**
 * Health status information for a module.
 */
data class HealthStatus(
    val status: String,
    val timestamp: Instant,
    val responseTimeMs: Long,
    val message: String,
    val details: Map<String, String> = emptyMap()
)

/**
 * Statistics about the module registry.
 */
data class ModuleStatistics(
    val totalModules: Int,
    val healthyModules: Int,
    val unhealthyModules: Int,
    val averageResponseTime: Double,
    val moduleNames: List<String>,
    val lastHealthCheck: Instant
)

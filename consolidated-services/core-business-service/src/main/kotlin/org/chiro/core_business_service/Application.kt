package org.chiro.core_business_service

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.chiro.core_business_service.shared.infrastructure.configuration.ApplicationConfiguration
import org.chiro.core_business_service.shared.infrastructure.configuration.ModuleConfiguration
import org.chiro.core_business_service.shared.infrastructure.module.ModuleRegistry
import org.chiro.core_business_service.shared.infrastructure.event.DomainEventPublisherImpl
import org.chiro.core_business_service.shared.infrastructure.transaction.TransactionManagerImpl
import org.chiro.core_business_service.shared.infrastructure.validation.ValidationServiceImpl
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

/**
 * Core Business Service Application - Main Entry Point
 * 
 * This is the main application class for the Core Business Service,
 * which consolidates Finance, Inventory, Sales, Manufacturing, and Procurement
 * modules into a single service following microservice consolidation patterns.
 * 
 * Architecture:
 * - Multi-module monolith with clear bounded contexts
 * - Domain-Driven Design (DDD) patterns
 * - Event-Driven Architecture (EDA) for inter-module communication
 * - CQRS for read/write separation where appropriate
 * - Production-ready observability and configuration
 */
@QuarkusMain
class CoreBusinessServiceApplication : QuarkusApplication {
    
    @Inject
    lateinit var applicationConfig: ApplicationConfiguration
    
    @Inject
    lateinit var moduleConfig: ModuleConfiguration
    
    @Inject
    lateinit var moduleRegistry: ModuleRegistry
    
    @Inject
    lateinit var eventPublisher: DomainEventPublisherImpl
    
    @Inject
    lateinit var transactionManager: TransactionManagerImpl
    
    @Inject
    lateinit var validationService: ValidationServiceImpl
    
    private val logger = Logger.getLogger(CoreBusinessServiceApplication::class.java)
    
    override fun run(vararg args: String?): Int {
        logger.info("🚀 Starting Core Business Service Application")
        logger.info("📋 Application: ${applicationConfig.applicationName} v${applicationConfig.applicationVersion}")
        
        try {
            // Log module configuration
            logModuleConfiguration()
            
            // Start the application
            Quarkus.waitForExit()
            
            logger.info("✅ Core Business Service Application stopped gracefully")
            return 0
            
        } catch (e: Exception) {
            logger.error("❌ Core Business Service Application failed to start", e)
            return 1
        }
    }
    
    /**
     * Startup event handler - called when the application starts
     */
    suspend fun onStart(@Observes event: StartupEvent) {
        logger.info("🎯 Core Business Service startup complete")
        logger.info("🔧 Configuration: Strict validation=${applicationConfig.strictValidationMode}, Event sourcing=${applicationConfig.eventSourcingEnabled}")
        
        // Validate core infrastructure
        validateCoreInfrastructure()
        
        // Log module registry status
        logModuleRegistryStatus()
    }
    
    private fun logModuleConfiguration() {
        logger.info("📦 Module Configuration:")
        logger.info("   💰 Finance: ${if (moduleConfig.financeEnabled) "ENABLED" else "DISABLED"} (${moduleConfig.financeBasePath})")
        logger.info("   📦 Inventory: ${if (moduleConfig.inventoryEnabled) "ENABLED" else "DISABLED"} (${moduleConfig.inventoryBasePath})")
        logger.info("   💼 Sales: ${if (moduleConfig.salesEnabled) "ENABLED" else "DISABLED"} (${moduleConfig.salesBasePath})")
        logger.info("   🏭 Manufacturing: ${if (moduleConfig.manufacturingEnabled) "ENABLED" else "DISABLED"} (${moduleConfig.manufacturingBasePath})")
        logger.info("   🛒 Procurement: ${if (moduleConfig.procurementEnabled) "ENABLED" else "DISABLED"} (${moduleConfig.procurementBasePath})")
    }
    
    /**
     * Validate that all core infrastructure components are properly initialized.
     */
    private suspend fun validateCoreInfrastructure() {
        logger.info("🔍 Validating core infrastructure...")
        
        try {
            // Test event publishing
            logger.debug("✓ Event publisher: ${eventPublisher::class.simpleName}")
            
            // Test transaction manager
            val txStatus = transactionManager.getCurrentTransactionStatus()
            logger.debug("✓ Transaction manager: $txStatus")
            
            // Test validation service
            logger.debug("✓ Validation service: ${validationService::class.simpleName}")
            
            logger.info("✅ Core infrastructure validation complete")
            
        } catch (e: Exception) {
            logger.error("❌ Core infrastructure validation failed", e)
            throw e
        }
    }
    
    /**
     * Log the status of the module registry.
     */
    private suspend fun logModuleRegistryStatus() {
        try {
            val moduleCount = moduleRegistry.getModuleCount()
            val registeredModules = moduleRegistry.getRegisteredModules()
            
            logger.info("🏛️ Module Registry Status:")
            logger.info("   📊 Total modules: $moduleCount")
            
            if (registeredModules.isNotEmpty()) {
                logger.info("   📋 Registered modules:")
                registeredModules.forEach { moduleName ->
                    val health = moduleRegistry.getModuleHealth(moduleName)
                    val status = health?.status ?: "UNKNOWN"
                    logger.info("     • $moduleName: $status")
                }
            } else {
                logger.info("   ⚠️ No modules registered yet")
            }
            
        } catch (e: Exception) {
            logger.error("Failed to get module registry status", e)
        }
    }
}

/**
 * Main function - Application entry point
 */
fun main(args: Array<String>) {
    System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    Quarkus.run(CoreBusinessServiceApplication::class.java, *args)
}

package org.chiro.core_business_service.shared.infrastructure.configuration

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

/**
 * Core application configuration
 */
@ApplicationScoped
class ApplicationConfiguration {
    
    @ConfigProperty(name = "quarkus.application.name", defaultValue = "Core Business Service")
    lateinit var applicationName: String
    
    @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
    lateinit var applicationVersion: String
    
    @ConfigProperty(name = "business-rules.validation.strict-mode", defaultValue = "true")
    var strictValidationMode: Boolean = true
    
    @ConfigProperty(name = "event-store.enabled", defaultValue = "true")
    var eventSourcingEnabled: Boolean = true
    
    @ConfigProperty(name = "event-store.retention-days", defaultValue = "2555")
    var eventRetentionDays: Int = 2555
    
    @ConfigProperty(name = "performance.async-processing", defaultValue = "true")
    var asyncProcessingEnabled: Boolean = true
    
    @ConfigProperty(name = "performance.batch-size", defaultValue = "1000")
    var batchSize: Int = 1000
    
    @ConfigProperty(name = "performance.cache-ttl", defaultValue = "3600")
    var cacheTtlSeconds: Int = 3600
}

/**
 * Module configuration
 */
@ApplicationScoped
class ModuleConfiguration {
    
    @ConfigProperty(name = "modules.finance.enabled", defaultValue = "true")
    var financeEnabled: Boolean = true
    
    @ConfigProperty(name = "modules.finance.base-path", defaultValue = "/finance")
    lateinit var financeBasePath: String
    
    @ConfigProperty(name = "modules.inventory.enabled", defaultValue = "true")
    var inventoryEnabled: Boolean = true
    
    @ConfigProperty(name = "modules.inventory.base-path", defaultValue = "/inventory")
    lateinit var inventoryBasePath: String
    
    @ConfigProperty(name = "modules.sales.enabled", defaultValue = "true")
    var salesEnabled: Boolean = true
    
    @ConfigProperty(name = "modules.sales.base-path", defaultValue = "/sales")
    lateinit var salesBasePath: String
    
    @ConfigProperty(name = "modules.manufacturing.enabled", defaultValue = "true")
    var manufacturingEnabled: Boolean = true
    
    @ConfigProperty(name = "modules.manufacturing.base-path", defaultValue = "/manufacturing")
    lateinit var manufacturingBasePath: String
    
    @ConfigProperty(name = "modules.procurement.enabled", defaultValue = "true")
    var procurementEnabled: Boolean = true
    
    @ConfigProperty(name = "modules.procurement.base-path", defaultValue = "/procurement")
    lateinit var procurementBasePath: String
}

/**
 * Finance module specific configuration
 */
@ApplicationScoped
class FinanceConfiguration {
    
    @ConfigProperty(name = "modules.finance.currency.default", defaultValue = "USD")
    lateinit var defaultCurrency: String
    
    @ConfigProperty(name = "modules.finance.currency.supported")
    var supportedCurrencies: Optional<List<String>> = Optional.empty()
    
    @ConfigProperty(name = "modules.finance.fiscal-year.start-month", defaultValue = "1")
    var fiscalYearStartMonth: Int = 1
    
    @ConfigProperty(name = "business-rules.finance.multi-currency-enabled", defaultValue = "true")
    var multiCurrencyEnabled: Boolean = true
    
    @ConfigProperty(name = "business-rules.finance.auto-reconciliation", defaultValue = "false")
    var autoReconciliationEnabled: Boolean = false
    
    @ConfigProperty(name = "business-rules.finance.tax-calculation", defaultValue = "automatic")
    lateinit var taxCalculationMethod: String
}

/**
 * Inventory module specific configuration
 */
@ApplicationScoped
class InventoryConfiguration {
    
    @ConfigProperty(name = "modules.inventory.stock-tracking.method", defaultValue = "FIFO")
    lateinit var stockTrackingMethod: String
    
    @ConfigProperty(name = "modules.inventory.low-stock-threshold", defaultValue = "10")
    var lowStockThreshold: Int = 10
    
    @ConfigProperty(name = "business-rules.inventory.negative-stock-allowed", defaultValue = "false")
    var negativeStockAllowed: Boolean = false
    
    @ConfigProperty(name = "business-rules.inventory.serial-tracking-required", defaultValue = "true")
    var serialTrackingRequired: Boolean = true
}

/**
 * Integration configuration
 */
@ApplicationScoped
class IntegrationConfiguration {
    
    @ConfigProperty(name = "integration.external-services.timeout", defaultValue = "30000")
    var externalServiceTimeout: Int = 30000
    
    @ConfigProperty(name = "integration.external-services.retry-attempts", defaultValue = "3")
    var retryAttempts: Int = 3
}

/**
 * Configuration producer methods
 */
@ApplicationScoped
class ConfigurationProducer {
    
    @Produces
    @ApplicationScoped
    fun applicationProperties(config: ApplicationConfiguration): Properties {
        return Properties().apply {
            setProperty("application.name", config.applicationName)
            setProperty("application.version", config.applicationVersion)
            setProperty("strict.validation", config.strictValidationMode.toString())
            setProperty("event.sourcing.enabled", config.eventSourcingEnabled.toString())
        }
    }
}

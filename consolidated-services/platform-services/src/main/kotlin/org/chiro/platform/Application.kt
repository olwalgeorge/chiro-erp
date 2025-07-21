package org.chiro.platform

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.logging.Logger

@QuarkusMain
class PlatformServicesApplication : QuarkusApplication {
    
    private val logger = Logger.getLogger(PlatformServicesApplication::class.java.name)
    
    override fun run(vararg args: String): Int {
        logger.info("🚀 Platform Services starting...")
        logger.info("📊 Modules: Analytics, Notifications, Tenant Management, Billing")
        logger.info("🌐 Serving platform-level functionality for the entire ERP system")
        Quarkus.waitForExit()
        return 0
    }
}

@ApplicationScoped
class PlatformServicesConfig {
    
    @ConfigProperty(name = "platform.analytics.enabled", defaultValue = "true")
    lateinit var analyticsEnabled: String
    
    @ConfigProperty(name = "platform.notifications.enabled", defaultValue = "true")
    lateinit var notificationsEnabled: String
    
    @ConfigProperty(name = "platform.tenant-management.enabled", defaultValue = "true")
    lateinit var tenantManagementEnabled: String
    
    @ConfigProperty(name = "platform.billing.enabled", defaultValue = "true")
    lateinit var billingEnabled: String
    
    fun isAnalyticsEnabled(): Boolean = analyticsEnabled.toBoolean()
    fun isNotificationsEnabled(): Boolean = notificationsEnabled.toBoolean()
    fun isTenantManagementEnabled(): Boolean = tenantManagementEnabled.toBoolean()
    fun isBillingEnabled(): Boolean = billingEnabled.toBoolean()
}

fun main(args: Array<String>) {
    Quarkus.run(PlatformServicesApplication::class.java, *args)
}

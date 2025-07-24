package org.chiro.core_business_service.shared.infrastructure.rest

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.chiro.core_business_service.shared.infrastructure.configuration.ApplicationConfiguration
import org.chiro.core_business_service.shared.infrastructure.configuration.ModuleConfiguration
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Liveness
import org.eclipse.microprofile.health.Readiness
import java.time.LocalDateTime

/**
 * Main health check and service information endpoint
 */
@Path("/api/v1/health")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class HealthController : BaseRestController() {
    
    @Inject
    lateinit var applicationConfig: ApplicationConfiguration
    
    @Inject
    lateinit var moduleConfig: ModuleConfiguration
    
    @GET
    @Path("/status")
    fun getServiceStatus(): Response {
        val status = HealthStatus(
            status = "UP",
            version = applicationConfig.applicationVersion,
            dependencies = mapOf(
                "finance" to DependencyStatus(if (moduleConfig.financeEnabled) "UP" else "DISABLED"),
                "inventory" to DependencyStatus(if (moduleConfig.inventoryEnabled) "UP" else "DISABLED"),
                "sales" to DependencyStatus(if (moduleConfig.salesEnabled) "UP" else "DISABLED"),
                "manufacturing" to DependencyStatus(if (moduleConfig.manufacturingEnabled) "UP" else "DISABLED"),
                "procurement" to DependencyStatus(if (moduleConfig.procurementEnabled) "UP" else "DISABLED")
            )
        )
        return ok(status)
    }
    
    @GET
    @Path("/info")
    fun getServiceInfo(): Response {
        val info = mapOf(
            "name" to applicationConfig.applicationName,
            "version" to applicationConfig.applicationVersion,
            "timestamp" to LocalDateTime.now(),
            "modules" to mapOf(
                "finance" to mapOf(
                    "enabled" to moduleConfig.financeEnabled,
                    "basePath" to moduleConfig.financeBasePath
                ),
                "inventory" to mapOf(
                    "enabled" to moduleConfig.inventoryEnabled,
                    "basePath" to moduleConfig.inventoryBasePath
                ),
                "sales" to mapOf(
                    "enabled" to moduleConfig.salesEnabled,
                    "basePath" to moduleConfig.salesBasePath
                ),
                "manufacturing" to mapOf(
                    "enabled" to moduleConfig.manufacturingEnabled,
                    "basePath" to moduleConfig.manufacturingBasePath
                ),
                "procurement" to mapOf(
                    "enabled" to moduleConfig.procurementEnabled,
                    "basePath" to moduleConfig.procurementBasePath
                )
            ),
            "features" to mapOf(
                "eventSourcing" to applicationConfig.eventSourcingEnabled,
                "strictValidation" to applicationConfig.strictValidationMode,
                "asyncProcessing" to applicationConfig.asyncProcessingEnabled
            )
        )
        return ok(info)
    }
}

/**
 * Liveness probe - indicates if the application is running
 */
@Liveness
@ApplicationScoped
class LivenessCheck : HealthCheck {
    
    override fun call(): HealthCheckResponse {
        return HealthCheckResponse.named("core-business-service-liveness")
            .up()
            .withData("timestamp", LocalDateTime.now().toString())
            .build()
    }
}

/**
 * Readiness probe - indicates if the application is ready to serve requests
 */
@Readiness
@ApplicationScoped
class ReadinessCheck : HealthCheck {
    
    @Inject
    lateinit var applicationConfig: ApplicationConfiguration
    
    override fun call(): HealthCheckResponse {
        // Add any readiness checks here (database connectivity, etc.)
        val isReady = true // Implement actual readiness logic
        
        return HealthCheckResponse.named("core-business-service-readiness")
            .status(isReady)
            .withData("timestamp", LocalDateTime.now().toString())
            .withData("version", applicationConfig.applicationVersion)
            .build()
    }
}

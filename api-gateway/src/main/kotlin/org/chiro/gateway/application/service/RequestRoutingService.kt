package org.chiro.gateway.application.service

import io.quarkus.vertx.web.Route
import io.quarkus.vertx.web.Router
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.logging.Logger
import java.util.concurrent.CompletableFuture

@ApplicationScoped
class ConsolidatedRequestRoutingService {
    
    private val logger = Logger.getLogger(ConsolidatedRequestRoutingService::class.java.name)
    
    @Inject
    lateinit var webClient: WebClient
    
    @ConfigProperty(name = "gateway.services.core-business.url")
    lateinit var coreBusinessUrl: String
    
    @ConfigProperty(name = "gateway.services.operations-management.url")
    lateinit var operationsManagementUrl: String
    
    @ConfigProperty(name = "gateway.services.customer-relations.url")
    lateinit var customerRelationsUrl: String
    
    @ConfigProperty(name = "gateway.services.platform-services.url")
    lateinit var platformServicesUrl: String
    
    @ConfigProperty(name = "gateway.services.workforce-management.url")
    lateinit var workforceManagementUrl: String
    
    private val serviceMapping = mapOf(
        "/api/billing" to "core-business",
        "/api/finance" to "core-business",
        "/api/sales" to "core-business", 
        "/api/inventory" to "core-business",
        "/api/procurement" to "core-business",
        "/api/manufacturing" to "core-business",
        "/api/project" to "operations-management",
        "/api/fleet" to "operations-management",
        "/api/pos" to "operations-management",
        "/api/fieldservice" to "operations-management",
        "/api/repair" to "operations-management",
        "/api/crm" to "customer-relations",
        "/api/analytics" to "platform-services",
        "/api/notifications" to "platform-services",
        "/api/hr" to "workforce-management",
        "/api/users" to "workforce-management",
        "/api/tenants" to "workforce-management"
    )
    
    @Route(path = "/api/*", methods = [HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH])
    fun routeToConsolidatedServices(context: RoutingContext) {
        val path = context.request().path()
        val targetService = determineTargetService(path)
        
        if (targetService == null) {
            context.response()
                .setStatusCode(404)
                .end("Service not found for path: $path")
            return
        }
        
        val targetUrl = getServiceUrl(targetService)
        logger.info("Routing $path to $targetService at $targetUrl")
        
        // Forward request to target service
        forwardRequest(context, targetUrl, path)
    }
    
    private fun determineTargetService(path: String): String? {
        return serviceMapping.entries
            .firstOrNull { (prefix, _) -> path.startsWith(prefix) }
            ?.value
    }
    
    private fun getServiceUrl(serviceName: String): String {
        return when (serviceName) {
            "core-business" -> coreBusinessUrl
            "operations-management" -> operationsManagementUrl
            "customer-relations" -> customerRelationsUrl
            "platform-services" -> platformServicesUrl
            "workforce-management" -> workforceManagementUrl
            else -> throw IllegalArgumentException("Unknown service: $serviceName")
        }
    }
    
    private fun forwardRequest(context: RoutingContext, targetUrl: String, path: String) {
        val request = context.request()
        val method = request.method()
        val fullTargetUrl = "$targetUrl$path"
        
        val webRequest = webClient
            .requestAbs(method, fullTargetUrl)
            .putHeaders(request.headers())
        
        if (request.body() != null) {
            webRequest.sendBuffer(request.body()) { result ->
                if (result.succeeded()) {
                    val response = result.result()
                    context.response()
                        .setStatusCode(response.statusCode())
                        .putHeaders(response.headers())
                        .end(response.body())
                } else {
                    logger.severe("Failed to forward request: ")
                    context.response()
                        .setStatusCode(502)
                        .end("Bad Gateway")
                }
            }
        } else {
            webRequest.send { result ->
                if (result.succeeded()) {
                    val response = result.result()
                    context.response()
                        .setStatusCode(response.statusCode())
                        .putHeaders(response.headers())
                        .end(response.body())
                } else {
                    logger.severe("Failed to forward request: ")
                    context.response()
                        .setStatusCode(502)
                        .end("Bad Gateway")
                }
            }
        }
    }
}

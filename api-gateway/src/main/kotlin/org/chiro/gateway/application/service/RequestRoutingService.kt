package org.chiro.gateway.application.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.core.HttpHeaders
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.logging.Logger

@ApplicationScoped
@Path("/api")
class ConsolidatedRequestRoutingService {
    
    private val logger = Logger.getLogger(ConsolidatedRequestRoutingService::class.java.name)
    
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

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    
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
    
    @GET
    @Path("/{path:.*}")
    fun routeGetRequest(
        @PathParam("path") path: String,
        @Context uriInfo: UriInfo,
        @Context headers: HttpHeaders
    ): Response {
        return routeRequest("GET", path, uriInfo, headers, null)
    }
    
    @POST
    @Path("/{path:.*}")
    @Consumes("*/*")
    fun routePostRequest(
        @PathParam("path") path: String,
        @Context uriInfo: UriInfo,
        @Context headers: HttpHeaders,
        body: String?
    ): Response {
        return routeRequest("POST", path, uriInfo, headers, body)
    }
    
    @PUT
    @Path("/{path:.*}")
    @Consumes("*/*")
    fun routePutRequest(
        @PathParam("path") path: String,
        @Context uriInfo: UriInfo,
        @Context headers: HttpHeaders,
        body: String?
    ): Response {
        return routeRequest("PUT", path, uriInfo, headers, body)
    }
    
    @DELETE
    @Path("/{path:.*}")
    fun routeDeleteRequest(
        @PathParam("path") path: String,
        @Context uriInfo: UriInfo,
        @Context headers: HttpHeaders
    ): Response {
        return routeRequest("DELETE", path, uriInfo, headers, null)
    }
    
    @PATCH
    @Path("/{path:.*}")
    @Consumes("*/*")
    fun routePatchRequest(
        @PathParam("path") path: String,
        @Context uriInfo: UriInfo,
        @Context headers: HttpHeaders,
        body: String?
    ): Response {
        return routeRequest("PATCH", path, uriInfo, headers, body)
    }
    
    private fun routeRequest(
        method: String,
        path: String,
        uriInfo: UriInfo,
        headers: HttpHeaders,
        body: String?
    ): Response {
        val fullPath = "/api/$path"
        val targetService = determineTargetService(fullPath)
        
        if (targetService == null) {
            logger.warning("No service found for path: $fullPath")
            return Response.status(404)
                .entity("Service not found for path: $fullPath")
                .build()
        }
        
        val targetUrl = getServiceUrl(targetService)
        logger.info("Routing $method $fullPath to $targetService at $targetUrl")
        
        return try {
            forwardRequest(method, targetUrl, path, uriInfo, headers, body)
        } catch (e: Exception) {
            logger.severe("Failed to forward request: ${e.message}")
            Response.status(502)
                .entity("Bad Gateway: ${e.message}")
                .build()
        }
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
    
    private fun forwardRequest(
        method: String,
        targetUrl: String,
        path: String,
        uriInfo: UriInfo,
        headers: HttpHeaders,
        body: String?
    ): Response {
        val fullTargetUrl = "$targetUrl/api/$path"
        val queryString = uriInfo.requestUri.query
        val finalUrl = if (queryString != null) "$fullTargetUrl?$queryString" else fullTargetUrl
        
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(finalUrl))
            .timeout(Duration.ofSeconds(30))
        
        // Add headers (excluding hop-by-hop headers)
        headers.requestHeaders.forEach { (name, values) ->
            if (!isHopByHopHeader(name)) {
                values.forEach { value ->
                    requestBuilder.header(name, value)
                }
            }
        }
        
        // Set method and body
        when (method.uppercase()) {
            "GET" -> requestBuilder.GET()
            "DELETE" -> requestBuilder.DELETE()
            "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body ?: ""))
            "PUT" -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body ?: ""))
            "PATCH" -> requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(body ?: ""))
        }
        
        val request = requestBuilder.build()
        val httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        // Build response
        val responseBuilder = Response.status(httpResponse.statusCode())
        
        // Add response headers (excluding hop-by-hop headers)
        httpResponse.headers().map().forEach { (name, values) ->
            if (!isHopByHopHeader(name)) {
                values.forEach { value ->
                    responseBuilder.header(name, value)
                }
            }
        }
        
        return responseBuilder.entity(httpResponse.body()).build()
    }
    
    private fun isHopByHopHeader(headerName: String): Boolean {
        val hopByHopHeaders = setOf(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "transfer-encoding", "upgrade"
        )
        return hopByHopHeaders.contains(headerName.lowercase())
    }
}

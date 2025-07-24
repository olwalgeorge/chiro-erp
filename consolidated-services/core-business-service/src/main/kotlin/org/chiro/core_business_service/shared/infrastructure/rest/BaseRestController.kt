package org.chiro.core_business_service.shared.infrastructure.rest

import jakarta.ws.rs.core.Response
import org.chiro.core_business_service.shared.application.command.CommandResult
import org.chiro.core_business_service.shared.application.query.QueryResult
import java.time.LocalDateTime

/**
 * Base REST controller providing common response handling
 */
abstract class BaseRestController {
    
    /**
     * Convert command result to HTTP response
     */
    protected fun <T> CommandResult<T>.toResponse(): Response = when (this) {
        is CommandResult.Success -> Response.ok(ApiResponse.success(data)).build()
        is CommandResult.Failure -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(ApiResponse.error(error, errorCode))
            .build()
        is CommandResult.ValidationFailure -> Response.status(Response.Status.BAD_REQUEST)
            .entity(ApiResponse.validationError(errors))
            .build()
    }
    
    /**
     * Convert query result to HTTP response
     */
    protected fun <T> QueryResult<T>.toResponse(): Response = when (this) {
        is QueryResult.Success -> Response.ok(ApiResponse.success(data)).build()
        is QueryResult.NotFound -> Response.status(Response.Status.NOT_FOUND)
            .entity(ApiResponse.error(message))
            .build()
        is QueryResult.Error -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(ApiResponse.error(message))
            .build()
    }
    
    /**
     * Create a successful response with data
     */
    protected fun <T> ok(data: T): Response = 
        Response.ok(ApiResponse.success(data)).build()
    
    /**
     * Create a created response for new resources
     */
    protected fun <T> created(data: T): Response = 
        Response.status(Response.Status.CREATED)
            .entity(ApiResponse.success(data))
            .build()
    
    /**
     * Create a no content response
     */
    protected fun noContent(): Response = 
        Response.noContent().build()
    
    /**
     * Create a bad request response
     */
    protected fun badRequest(message: String): Response = 
        Response.status(Response.Status.BAD_REQUEST)
            .entity(ApiResponse.error(message))
            .build()
    
    /**
     * Create a not found response
     */
    protected fun notFound(message: String): Response = 
        Response.status(Response.Status.NOT_FOUND)
            .entity(ApiResponse.error(message))
            .build()
}

/**
 * Standard API response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetails? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val path: String? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(
            success = true,
            data = data
        )
        
        fun error(
            message: String,
            code: String? = null,
            path: String? = null
        ): ApiResponse<Nothing> = ApiResponse(
            success = false,
            error = ErrorDetails(message, code),
            path = path
        )
        
        fun validationError(
            errors: Map<String, List<String>>,
            path: String? = null
        ): ApiResponse<Nothing> = ApiResponse(
            success = false,
            error = ErrorDetails(
                message = "Validation failed",
                code = "VALIDATION_ERROR",
                validationErrors = errors
            ),
            path = path
        )
    }
}

/**
 * Error details for API responses
 */
data class ErrorDetails(
    val message: String,
    val code: String? = null,
    val validationErrors: Map<String, List<String>>? = null
)

/**
 * Health check response
 */
data class HealthStatus(
    val status: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val version: String? = null,
    val dependencies: Map<String, DependencyStatus> = emptyMap()
)

/**
 * Dependency status for health checks
 */
data class DependencyStatus(
    val status: String,
    val responseTime: Long? = null,
    val details: String? = null
)

package org.chiro.finance.infrastructure.rest

import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.chiro.finance.application.service.AccountApplicationService
import org.chiro.finance.application.command.CreateAccountCommand
import org.chiro.finance.application.command.UpdateAccountCommand
import org.chiro.finance.application.dto.AccountDto
import org.chiro.finance.domain.entity.AccountStatus
import org.chiro.finance.domain.valueobject.AccountType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.util.*

/**
 * Finance REST Controller - Modern Quarkus REST endpoints
 * 
 * Provides RESTful APIs for finance operations following:
 * - OpenAPI 3.0 documentation
 * - HTTP status code best practices
 * - Input validation
 * - Error handling
 * - Async/await patterns
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Path("/api/finance")
@Tag(name = "Finance", description = "Financial management operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class FinanceController {
    
    @Inject
    lateinit var accountService: AccountApplicationService
    
    // ===================== ACCOUNT MANAGEMENT =====================
    
    @POST
    @Path("/accounts")
    @Operation(summary = "Create new account", description = "Creates a new account in the chart of accounts")
    @APIResponse(responseCode = "201", description = "Account created successfully")
    @APIResponse(responseCode = "400", description = "Invalid input data")
    @APIResponse(responseCode = "409", description = "Account code already exists")
    suspend fun createAccount(@Valid command: CreateAccountCommand): Response {
        return try {
            val account = accountService.createAccount(command)
            Response.status(Response.Status.CREATED).entity(account).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
    
    @GET
    @Path("/accounts/{accountId}")
    @Operation(summary = "Get account by ID", description = "Retrieves account details by ID")
    @APIResponse(responseCode = "200", description = "Account found")
    @APIResponse(responseCode = "404", description = "Account not found")
    suspend fun getAccount(
        @Parameter(description = "Account ID") 
        @PathParam("accountId") accountId: UUID
    ): Response {
        val account = accountService.getAccount(accountId)
        return if (account != null) {
            Response.ok(account).build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Account not found"))
                .build()
        }
    }
    
    @GET
    @Path("/accounts/by-code/{accountCode}")
    @Operation(summary = "Get account by code", description = "Retrieves account details by account code")
    @APIResponse(responseCode = "200", description = "Account found")
    @APIResponse(responseCode = "404", description = "Account not found")
    suspend fun getAccountByCode(
        @Parameter(description = "Account code") 
        @PathParam("accountCode") accountCode: String
    ): Response {
        val account = accountService.getAccountByCode(accountCode)
        return if (account != null) {
            Response.ok(account).build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Account not found"))
                .build()
        }
    }
    
    @GET
    @Path("/accounts")
    @Operation(summary = "List accounts", description = "Lists accounts with optional filtering")
    @APIResponse(responseCode = "200", description = "Accounts retrieved successfully")
    suspend fun listAccounts(
        @Parameter(description = "Filter by account type")
        @QueryParam("accountType") accountType: AccountType?,
        
        @Parameter(description = "Filter by account status")
        @QueryParam("status") status: AccountStatus?,
        
        @Parameter(description = "Filter by parent account ID")
        @QueryParam("parentAccountId") parentAccountId: UUID?,
        
        @Parameter(description = "Include inactive accounts")
        @QueryParam("includeInactive") @DefaultValue("false") includeInactive: Boolean
    ): Response {
        val accounts = accountService.listAccounts(
            accountType = accountType,
            status = status,
            parentAccountId = parentAccountId,
            includeInactive = includeInactive
        )
        return Response.ok(accounts).build()
    }
    
    @GET
    @Path("/accounts/chart")
    @Operation(summary = "Get chart of accounts", description = "Retrieves the complete chart of accounts")
    @APIResponse(responseCode = "200", description = "Chart of accounts retrieved successfully")
    suspend fun getChartOfAccounts(): Response {
        val chartOfAccounts = accountService.getChartOfAccounts()
        return Response.ok(chartOfAccounts).build()
    }
    
    @PUT
    @Path("/accounts/{accountId}")
    @Operation(summary = "Update account", description = "Updates an existing account")
    @APIResponse(responseCode = "200", description = "Account updated successfully")
    @APIResponse(responseCode = "404", description = "Account not found")
    @APIResponse(responseCode = "400", description = "Invalid input data")
    suspend fun updateAccount(
        @Parameter(description = "Account ID")
        @PathParam("accountId") accountId: UUID,
        @Valid command: UpdateAccountCommand
    ): Response {
        return try {
            val account = accountService.updateAccount(accountId, command)
            Response.ok(account).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
    
    @POST
    @Path("/accounts/{accountId}/activate")
    @Operation(summary = "Activate account", description = "Activates an inactive account")
    @APIResponse(responseCode = "200", description = "Account activated successfully")
    @APIResponse(responseCode = "404", description = "Account not found")
    @APIResponse(responseCode = "400", description = "Cannot activate account")
    suspend fun activateAccount(
        @Parameter(description = "Account ID")
        @PathParam("accountId") accountId: UUID,
        
        @Parameter(description = "User performing the action")
        @QueryParam("activatedBy") @DefaultValue("system") activatedBy: String
    ): Response {
        return try {
            val account = accountService.activateAccount(accountId, activatedBy)
            Response.ok(account).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
    
    @POST
    @Path("/accounts/{accountId}/deactivate")
    @Operation(summary = "Deactivate account", description = "Deactivates an active account")
    @APIResponse(responseCode = "200", description = "Account deactivated successfully")
    @APIResponse(responseCode = "404", description = "Account not found")
    @APIResponse(responseCode = "400", description = "Cannot deactivate account")
    suspend fun deactivateAccount(
        @Parameter(description = "Account ID")
        @PathParam("accountId") accountId: UUID,
        
        @Parameter(description = "User performing the action")
        @QueryParam("deactivatedBy") @DefaultValue("system") deactivatedBy: String
    ): Response {
        return try {
            val account = accountService.deactivateAccount(accountId, deactivatedBy)
            Response.ok(account).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
    
    @POST
    @Path("/accounts/{accountId}/close")
    @Operation(summary = "Close account", description = "Permanently closes an account")
    @APIResponse(responseCode = "200", description = "Account closed successfully")
    @APIResponse(responseCode = "404", description = "Account not found")
    @APIResponse(responseCode = "400", description = "Cannot close account")
    suspend fun closeAccount(
        @Parameter(description = "Account ID")
        @PathParam("accountId") accountId: UUID,
        
        @Parameter(description = "User performing the action")
        @QueryParam("closedBy") @DefaultValue("system") closedBy: String
    ): Response {
        return try {
            val account = accountService.closeAccount(accountId, closedBy)
            Response.ok(account).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
    
    @GET
    @Path("/accounts/{accountId}/balance")
    @Operation(summary = "Get account total balance", description = "Gets account balance including child accounts")
    @APIResponse(responseCode = "200", description = "Balance retrieved successfully")
    @APIResponse(responseCode = "404", description = "Account not found")
    suspend fun getAccountTotalBalance(
        @Parameter(description = "Account ID")
        @PathParam("accountId") accountId: UUID
    ): Response {
        val balance = accountService.getAccountTotalBalance(accountId)
        return if (balance != null) {
            Response.ok(mapOf("totalBalance" to balance)).build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Account not found"))
                .build()
        }
    }
    
    @GET
    @Path("/accounts/check-code/{accountCode}")
    @Operation(summary = "Check account code availability", description = "Checks if account code is available")
    @APIResponse(responseCode = "200", description = "Check completed")
    suspend fun checkAccountCodeAvailability(
        @Parameter(description = "Account code to check")
        @PathParam("accountCode") accountCode: String
    ): Response {
        val isAvailable = accountService.isAccountCodeAvailable(accountCode)
        return Response.ok(mapOf(
            "accountCode" to accountCode,
            "available" to isAvailable
        )).build()
    }
    
    // ===================== HEALTH CHECK =====================
    
    @GET
    @Path("/health")
    @Operation(summary = "Health check", description = "Simple health check endpoint")
    @APIResponse(responseCode = "200", description = "Service is healthy")
    fun healthCheck(): Response {
        return Response.ok(mapOf(
            "status" to "UP",
            "service" to "Finance Service",
            "timestamp" to System.currentTimeMillis()
        )).build()
    }
}

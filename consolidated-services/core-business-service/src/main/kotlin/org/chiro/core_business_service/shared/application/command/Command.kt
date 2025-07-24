package org.chiro.core_business_service.shared.application.command

/**
 * Marker interface for all commands
 * Commands represent intent to change the system state
 */
interface Command

/**
 * Marker interface for commands that can be executed asynchronously
 */
interface AsyncCommand : Command

/**
 * Base interface for command handlers
 */
interface CommandHandler<in T : Command, out R> {
    suspend fun handle(command: T): R
}

/**
 * Command execution result
 */
sealed class CommandResult<out T> {
    
    data class Success<T>(val data: T) : CommandResult<T>()
    
    data class Failure(
        val error: String,
        val cause: Throwable? = null,
        val errorCode: String? = null
    ) : CommandResult<Nothing>()
    
    data class ValidationFailure(
        val errors: Map<String, List<String>>
    ) : CommandResult<Nothing>()
    
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Failure) -> R,
        onValidationFailure: (ValidationFailure) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(this)
        is ValidationFailure -> onValidationFailure(this)
    }
    
    inline fun onSuccess(action: (T) -> Unit): CommandResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onFailure(action: (Failure) -> Unit): CommandResult<T> {
        if (this is Failure) action(this)
        return this
    }
    
    inline fun onValidationFailure(action: (ValidationFailure) -> Unit): CommandResult<T> {
        if (this is ValidationFailure) action(this)
        return this
    }
    
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
    val isValidationFailure: Boolean get() = this is ValidationFailure
}

/**
 * Extension function to create a success result
 */
fun <T> T.asSuccess(): CommandResult<T> = CommandResult.Success(this)

/**
 * Extension function to create a failure result
 */
fun String.asFailure(cause: Throwable? = null, errorCode: String? = null): CommandResult<Nothing> =
    CommandResult.Failure(this, cause, errorCode)

/**
 * Extension function to create a validation failure result
 */
fun Map<String, List<String>>.asValidationFailure(): CommandResult<Nothing> =
    CommandResult.ValidationFailure(this)

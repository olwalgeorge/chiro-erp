package org.chiro.core_business_service.shared.application.query

/**
 * Marker interface for all queries
 * Queries represent requests for information without side effects
 */
interface Query<out T>

/**
 * Base interface for query handlers
 */
interface QueryHandler<in Q : Query<T>, out T> {
    suspend fun handle(query: Q): T
}

/**
 * Query execution result
 */
sealed class QueryResult<out T> {
    
    data class Success<T>(val data: T) : QueryResult<T>()
    
    data class NotFound(val message: String) : QueryResult<Nothing>()
    
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : QueryResult<Nothing>()
    
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onNotFound: (NotFound) -> R,
        onError: (Error) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is NotFound -> onNotFound(this)
        is Error -> onError(this)
    }
    
    inline fun onSuccess(action: (T) -> Unit): QueryResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onNotFound(action: (NotFound) -> Unit): QueryResult<T> {
        if (this is NotFound) action(this)
        return this
    }
    
    inline fun onError(action: (Error) -> Unit): QueryResult<T> {
        if (this is Error) action(this)
        return this
    }
    
    val isSuccess: Boolean get() = this is Success
    val isNotFound: Boolean get() = this is NotFound
    val isError: Boolean get() = this is Error
}

/**
 * Page request for paginated queries
 */
data class PageRequest(
    val page: Int = 0,
    val size: Int = 20,
    val sort: List<SortOrder> = emptyList()
) {
    init {
        require(page >= 0) { "Page number cannot be negative" }
        require(size > 0) { "Page size must be positive" }
        require(size <= 1000) { "Page size cannot exceed 1000" }
    }
    
    val offset: Int get() = page * size
}

/**
 * Sort order for queries
 */
data class SortOrder(
    val property: String,
    val direction: Direction = Direction.ASC
) {
    enum class Direction { ASC, DESC }
}

/**
 * Page result for paginated queries
 */
data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    val hasNext: Boolean get() = page < totalPages - 1
    val hasPrevious: Boolean get() = page > 0
    val isFirst: Boolean get() = page == 0
    val isLast: Boolean get() = page == totalPages - 1
    val numberOfElements: Int get() = content.size
    val isEmpty: Boolean get() = content.isEmpty()
}

/**
 * Extension function to create a success result
 */
fun <T> T.asQuerySuccess(): QueryResult<T> = QueryResult.Success(this)

/**
 * Extension function to create a not found result
 */
fun String.asNotFound(): QueryResult<Nothing> = QueryResult.NotFound(this)

/**
 * Extension function to create an error result
 */
fun String.asQueryError(cause: Throwable? = null): QueryResult<Nothing> = QueryResult.Error(this, cause)

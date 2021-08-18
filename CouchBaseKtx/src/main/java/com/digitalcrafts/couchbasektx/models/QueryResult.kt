package com.digitalcrafts.couchbasektx.models

public sealed class QueryResult<T> {

    public data class Success<T> internal constructor(
        val data: T
    ) : QueryResult<T>()

    public data class Error<T> internal constructor(
        val throwable: Throwable,
        val errorMessage: String?,
    ) : QueryResult<T>()

    public fun getOrNull(): T? = when (this) {
        is Error -> null
        is Success -> data
    }

    public fun getOrThrow(): T = when (this) {
        is Error -> throw throwable
        is Success -> data
    }

    public fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> throwable
        is Success -> null
    }

    override fun toString(): String = when (this) {
        is Error -> "QueryResult.Error(errorMessage:${errorMessage}, throwable:${throwable})"
        is Success -> "QueryResult.Success(data:${data.toString()})"
    }

    @PublishedApi
    internal companion object {

        fun <T> success(data: T): Success<T> = Success(data = data)

        fun <T> error(
            throwable: Throwable,
            message: String? = null,
        ): Error<T> = Error(throwable = throwable, errorMessage = message)
    }
}
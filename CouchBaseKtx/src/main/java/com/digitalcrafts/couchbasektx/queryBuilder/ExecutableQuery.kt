package com.digitalcrafts.couchbasektx.queryBuilder

import com.couchbase.lite.Query
import com.couchbase.lite.ResultSet
import com.digitalcrafts.couchbasektx.cbHelper.CbHelper
import com.digitalcrafts.couchbasektx.models.CbModel
import com.digitalcrafts.couchbasektx.models.QueryResult
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flowOn

public sealed class ExecutableQuery protected constructor(
    @PublishedApi
    internal val cbHelper: CbHelper,
    @PublishedApi
    internal val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    @PublishedApi
    internal val bgScope: CoroutineScope by lazy { CoroutineScope(backgroundDispatcher) }

    @PublishedApi
    internal abstract fun getQuery(): Query

    // TODO : Could deliver explain query as query error

    public suspend inline fun <reified S : CbModel> executeQuery(): QueryResult<List<S>> {

        return withContext(backgroundDispatcher) {

            val queryResult = kotlin.runCatching {

                getQuery()
                    .execute()
                    .toModels<S>()
            }

            queryResult
                .exceptionOrNull()
                ?.let { ex -> QueryResult.error(throwable = ex) }
                ?: run { QueryResult.success(queryResult.getOrThrow()) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    public inline fun <reified S : CbModel> executeAsLiveQuery():
            Flow<QueryResult<List<S>>> = callbackFlow<QueryResult<List<S>>> {

        val query = getQuery()

        val token = query.addChangeListener { queryChange ->

            val results = queryChange.results ?: return@addChangeListener

            bgScope.launch {

                val queryResult: Result<List<S>> = kotlin.runCatching { results.toModels() }

                val result: QueryResult<List<S>> = queryResult
                    .exceptionOrNull()
                    ?.let { ex -> QueryResult.error(throwable = ex) }
                    ?: run { QueryResult.success(queryResult.getOrThrow()) }

                trySend(result)
            }
        }

        awaitClose {
            query.removeChangeListener(token)
            this.channel.close()
        }

    }.flowOn(backgroundDispatcher)
        .cancellable()

    @PublishedApi
    internal suspend inline fun <reified S : CbModel> ResultSet.toModels(): List<S> {

        val dbName = cbHelper.getDataBase().name
        val converter = cbHelper.getConverterFactory()

        return withContext(backgroundDispatcher) {

            allResults()
                .mapNotNull { result ->

                    (result?.getDictionary(dbName)?.toMap() ?: result?.toMap())
                        ?.filter { it.value !is com.couchbase.lite.Blob }
                        ?.toMutableMap()
                        ?.let { map ->
                            map["_id"] = result.getString("id")
                            map.remove("_attachments")
                            converter.toModel(cls = S::class.java, map = map)
                        }
                }
        }
    }
}
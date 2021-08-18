package com.digitalcrafts.couchbasektx.queryBuilder

import com.couchbase.lite.MutableDocument
import com.digitalcrafts.couchbasektx.cbHelper.CbHelper
import com.digitalcrafts.couchbasektx.exceptions.QueryError
import com.digitalcrafts.couchbasektx.models.CbModel
import com.digitalcrafts.couchbasektx.models.QueryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class DeModelQuery private constructor(
    @PublishedApi
    internal val cbHelper: CbHelper,
) {

    public suspend inline fun <reified T : CbModel> getDocument(documentId: String): QueryResult<T> {

        val converter = cbHelper.getConverterFactory()

        return withContext(Dispatchers.IO) {

            val queryResult = kotlin.runCatching {

                val document = cbHelper.getDataBase()
                    .getDocument(documentId)
                    ?: throw QueryError.ModelNotFoundError.forId(documentId)

                converter.toModel(T::class.java, document.toMap())
            }

            queryResult
                .exceptionOrNull()
                ?.let { ex -> QueryResult.error(throwable = ex) }
                ?: run { QueryResult.success(queryResult.getOrThrow()) }
        }
    }

    public suspend inline fun <reified T : CbModel> saveDocument(model: T): QueryResult<T> {

        val converter = cbHelper.getConverterFactory()
        val id = model._id

        return withContext(Dispatchers.IO) {

            val queryResult = kotlin.runCatching {

                val document = if (id.isNullOrEmpty()) MutableDocument(converter.toMap(model))
                else MutableDocument(id, converter.toMap(model))

                cbHelper.getDataBase()
                    .save(document)

                document.id
            }

            queryResult.getOrNull()
                ?.let { docId -> getDocument(docId) }
                ?: kotlin.run { QueryResult.error(queryResult.exceptionOrNull()!!) }
        }
    }

    public suspend inline fun <reified T : CbModel> updateDocument(model: T): QueryResult<T> =
        saveDocument(model)

    public suspend fun deleteDocument(documentId: String): QueryResult<Boolean> {

        return withContext(Dispatchers.IO) {

            val queryResult = kotlin.runCatching {

                val document = cbHelper.getDataBase()
                    .getDocument(documentId)
                    ?: throw QueryError.ModelNotFoundError.forId(documentId)

                cbHelper.getDataBase()
                    .delete(document)
            }

            queryResult.exceptionOrNull()
                ?.let { ex -> QueryResult.error(ex) }
                ?: kotlin.run { QueryResult.success(true) }
        }
    }

    public companion object {

        public fun from(cbHelper: CbHelper): DeModelQuery = DeModelQuery(cbHelper)
    }
}
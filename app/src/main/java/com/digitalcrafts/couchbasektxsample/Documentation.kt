package com.digitalcrafts.couchbasektxsample

import android.util.Log
import com.digitalcrafts.couchbasektx.cbHelper.CbHelper
import com.digitalcrafts.couchbasektx.models.CbBlob
import com.digitalcrafts.couchbasektx.models.QueryResult
import com.digitalcrafts.couchbasektx.queryBuilder.DeAttachmentQuery
import com.digitalcrafts.couchbasektx.queryBuilder.DeModelQuery
import com.digitalcrafts.couchbasektx.queryBuilder.DeQuery
import com.digitalcrafts.couchbasektxsample.models.User

@Suppress("unused", "UNUSED_VARIABLE")
class Documentation(private val cbHelper: CbHelper) {

    suspend fun test() {
        queryBuilderTypes()
        selectAndWhereClause()
        queryReturnType()
        executableQuery()
        queryOperatorsAndProperties()
    }

    private suspend fun queryBuilderTypes() {

        /* There is support for 3 types of queries, and 3 different classes to handle them :
        *       * DeQuery : All the typical query operations.
        *       * DeModelQuery : For a specific document's CRUD operations
        *       * DeAttachmentQuery : For all the attachments related queries.
        */

        val basicQuery: QueryResult<List<User>> =
            DeQuery.from(cbHelper)
                .selectAll()
                .where { "type" equalTo "_cb_user" }
                .executeQuery()

        val modelQuery: QueryResult<User> =
            DeModelQuery.from(cbHelper)
                .getDocument("_cb_user:Martin0211")

        val attachmentQuery: QueryResult<List<CbBlob>> =
            DeAttachmentQuery.from(cbHelper)
                .getAttachment(
                    documentId = "_cb_user:Martin0211",
                    attachmentName = "cover_photo_x1"
                )
    }

    private fun selectAndWhereClause() {

        /* Select clause :
        *   Mandatory clause : will not compile without it
        *   Single instance : will not compile if multiple select clauses are added
        *   Two options :  selectAll(), select(vararg keys: String)
        */
        DeQuery.from(cbHelper)
            .selectAll()

        DeQuery.from(cbHelper)
            .select("name", "interests")


        /* Where clause :
        *   Mandatory clause : will not compile without it
        *   Single instance : will not compile if multiple where clauses are added
        */
        DeQuery.from(cbHelper)
            .selectAll()
            .where { "type" equalTo "_cb_user" }
    }

    private suspend fun queryReturnType() {

        /* All the one shot queries return QueryResult<T> objects, with T being the appropriate type
        *  inferred or passed along as a type parameter.
        */
        val result = DeQuery.from(cbHelper)
            .selectAll()
            .where { "type" equalTo "_cb_user" }
            .executeQuery<User>()

        /* All the live queries return Flow<QueryResult<T>> objects, with T being the appropriate type
        *  inferred or passed along as a type parameter.
        */
        val liveQueryResult = DeQuery.from(cbHelper)
            .selectAll()
            .where { "type" equalTo "_cb_user" }
            .executeAsLiveQuery<User>()

        /*  QueryResult : QueryResult is a sealed data holding class with two possible states :
        *       Success : has the result object
        *       Error   : has the throwable exception and an optional message.
        */
        when (result) {
            is QueryResult.Error -> Log.d(TAG, "Failed to execute query : ${result.throwable}")
            is QueryResult.Success -> Log.d(TAG, "Query result : ${result.data}")
        }

        // Helper method to retrieve data if success otherwise null
        result.getOrNull()

        // Helper method to retrieve data if success otherwise throw the error causing throwable
        result.getOrThrow()

        // Helper method to retrieve the throwable if failed otherwise null
        result.exceptionOrNull()
    }

    private suspend fun executableQuery() {

        /* Executable Query :
        *   After adding the where clause the query becomes executable,
        *   You can still build the query with other operators.
        *   Terminal operators : executeQuery(), executeAsLiveQuery()
        */

        /* Terminal operator : executeQuery()
        *   This will try to de-serialize with the passed type parameter and return an object with
        *   type of QueryResult<List<Type>>, if type inference is successful the type parameter is not required.
        *   Suspends the current scope and executes the query on a background dispatcher
        */

        DeQuery.from(cbHelper)
            .selectAll()
            .where { "type" equalTo "_cb_user" }
            .executeQuery<User>()

        val result: QueryResult<List<User>> = DeQuery.from(cbHelper)
            .selectAll()
            .where { "type" equalTo "_cb_user" }
            .executeQuery() // type not passed, got inferred automatically

        /* Terminal operator : executeAsLiveQuery()
        *   This will try to de-serialize with the passed type parameter and return a flow with
        *   type of Flow<QueryResult<List<Type>>>, if type inference is successful the type parameter is not required.
        *   Starts a live query and immediately emits the current object.
        *   All the up-stream operations are performed on a background dispatcher
        */

        DeQuery.from(cbHelper)
            .selectAll()
            .where { "type" equalTo "_cb_user" }
            .executeAsLiveQuery<User>()
    }

    private suspend fun queryOperatorsAndProperties() {

        /* Query Operators :
        *    For 'where', 'and' and 'or' clauses :
        *      equalTo, notEqualTo, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo
        *      like, isIn, between, isNullOrMissing, isNotNullOrMissing
        *
        *   Compound or Nested Queries with 'and' and 'or' operators
        */

        /* In built query properties :
        *   For 'where', 'and' and 'or' : _id, type, createdAt, updatedAt
        *   For 'orderBy' : createdAt, updatedAt
        */

        DeQuery.from(cbHelper)
            .selectAll()
            .where { type equalTo User.TYPE }
            .and { "lastActive".isNotNullOrMissing() }
            .and { ("majorInterest" equalTo "SCIENCE") and ("background" equalTo "ENGINEERING") }
            .or { "majorInterest" isIn listOf("ART", "POLITICS") }
            .orderBy {
                createdAt.descending()
                updatedAt.descending()
                "lastName".descending()
            }
            .limit(2)
            .executeQuery<User>()
    }

    companion object {

        private const val TAG: String = "CbQueryTest"
    }
}
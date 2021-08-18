package com.digitalcrafts.couchbasektx.queryBuilder

import com.couchbase.lite.*
import com.digitalcrafts.couchbasektx.cbHelper.CbHelper
import com.digitalcrafts.couchbasektx.queryBuilder.DeQueryLimiter.Companion.LimitableQuery.LqOrderBy
import com.digitalcrafts.couchbasektx.queryBuilder.DeQueryLimiter.Companion.LimitableQuery.LqWhere

public class DeQueryLimiter private constructor(
    private val query: LimitableQuery,
    cbHelper: CbHelper,
) : ExecutableQuery(cbHelper) {

    private var limitedQuery: Query = query.getQuery()

    override fun getQuery(): Query = limitedQuery

    internal fun limit(limitCount: Int): DeQueryLimiter {
        limitedQuery = query.limit(limitCount)
        return this
    }

    internal fun limitAndOffset(limitCount: Int, offset: Int): DeQueryLimiter {
        limitedQuery = query.limit(limitCount, offset)
        return this
    }

    internal companion object {

        private sealed class LimitableQuery {

            abstract fun getQuery(): Query
            abstract fun limit(limitCount: Int): Limit
            abstract fun limit(limitCount: Int, offset: Int): Limit

            class LqWhere(val where: Where) : LimitableQuery() {
                override fun getQuery(): Query = where
                override fun limit(limitCount: Int): Limit =
                    where.limit(Expression.value(limitCount))

                override fun limit(limitCount: Int, offset: Int): Limit =
                    where.limit(Expression.value(limitCount), Expression.value(offset))
            }

            class LqOrderBy(val orderBy: OrderBy) : LimitableQuery() {
                override fun getQuery(): Query = orderBy
                override fun limit(limitCount: Int): Limit =
                    orderBy.limit(Expression.value(limitCount))

                override fun limit(limitCount: Int, offset: Int): Limit =
                    orderBy.limit(Expression.value(limitCount), Expression.value(offset))
            }
        }

        internal fun with(where: Where, cbHelper: CbHelper): DeQueryLimiter =
            DeQueryLimiter(query = LqWhere(where), cbHelper = cbHelper)

        internal fun with(where: OrderBy, cbHelper: CbHelper): DeQueryLimiter =
            DeQueryLimiter(query = LqOrderBy(where), cbHelper = cbHelper)
    }
}
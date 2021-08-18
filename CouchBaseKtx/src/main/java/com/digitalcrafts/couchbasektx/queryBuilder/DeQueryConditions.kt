package com.digitalcrafts.couchbasektx.queryBuilder

import com.couchbase.lite.Expression
import com.couchbase.lite.From
import com.couchbase.lite.Ordering
import com.couchbase.lite.Query
import com.digitalcrafts.couchbasektx.cbHelper.CbHelper

@Suppress("unused")
public class DeQueryConditions internal constructor(
    private val from: From,
    whereExpression: Expression,
    cbHelper: CbHelper,
) : ExecutableQuery(cbHelper) {

    private var finalExpression: Expression = whereExpression

    private val queryOperations: DeQueryConditionsOperations by lazy { DeQueryConditionsOperations() }

    override fun getQuery(): Query = from.where(finalExpression)

    public fun and(logic: DeQueryConditionsOperations.() -> Expression): DeQueryConditions {
        finalExpression = finalExpression.and(queryOperations.logic())
        return this
    }

    public fun or(logic: DeQueryConditionsOperations.() -> Expression): DeQueryConditions {
        finalExpression = finalExpression.or(queryOperations.logic())
        return this
    }

    public fun orderBy(order: DeQueryOrderingOperations.() -> Unit): DeQueryOrderingOperations =
        DeQueryOrderingOperations().apply { order() }

    public fun limit(limitCount: Int): DeQueryLimiter = DeQueryLimiter.with(
        where = from.where(finalExpression),
        cbHelper = cbHelper,
    ).apply { this@apply.limit(limitCount = limitCount) }

    public fun limitAndOffset(limitCount: Int, offset: Int): DeQueryLimiter = DeQueryLimiter.with(
        where = from.where(finalExpression),
        cbHelper = cbHelper,
    ).apply { this@apply.limitAndOffset(limitCount = limitCount, offset = offset) }

    public inner class DeQueryConditionsOperations internal constructor() :
        DeWhere.DeWhereOperations()

    public inner class DeQueryOrderingOperations internal constructor() :
        ExecutableQuery(cbHelper) {

        private val orderings: MutableList<Ordering> = mutableListOf()

        override fun getQuery(): Query = from.where(finalExpression).orderBy(*orderings())

        public val createdAt: String = "createdAt"
        public val updatedAt: String = "updatedAt"

        public fun String.ascending() {
            orderings += Ordering.property(this).ascending()
        }

        public fun String.descending() {
            orderings += Ordering.property(this).descending()
        }

        public fun Expression.ascending() {
            orderings += Ordering.expression(this).ascending()
        }

        public fun Expression.descending() {
            orderings += Ordering.expression(this).descending()
        }

        public fun limit(limitCount: Int): DeQueryLimiter = DeQueryLimiter.with(
            where = from.where(finalExpression).orderBy(*orderings()),
            cbHelper = cbHelper,
        ).apply { this@apply.limit(limitCount = limitCount) }

        public fun limitAndOffset(limitCount: Int, offset: Int): DeQueryLimiter =
            DeQueryLimiter.with(
                where = from.where(finalExpression).orderBy(*orderings()),
                cbHelper = cbHelper,
            ).apply { this@apply.limitAndOffset(limitCount = limitCount, offset = offset) }

        private fun orderings(): Array<Ordering> = orderings.toTypedArray()
    }
}
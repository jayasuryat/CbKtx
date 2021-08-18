package com.digitalcrafts.couchbasektx.queryBuilder

import com.couchbase.lite.Expression
import com.couchbase.lite.From
import com.digitalcrafts.couchbasektx.cbHelper.CbHelper

@Suppress("NOTHING_TO_INLINE", "unused")
public class DeWhere internal constructor(
    private val from: From,
    private val cbHelper: CbHelper,
) {

    public fun where(logic: DeWhereOperations.() -> Expression): DeQueryConditions =
        DeQueryConditions(
            from = from,
            whereExpression = DeWhereOperations().logic(),
            cbHelper = cbHelper,
        )

    public open class DeWhereOperations internal constructor() {

        @Suppress("PropertyName")
        public val _id: String = "_id"
        public val type: String = "type"
        public val createdAt: String = "createdAt"
        public val updatedAt: String = "updatedAt"

        private inline fun property(name: String): Expression = Expression.property(name)

        public infix fun String.equalTo(value: Any): Expression =
            property(this).equalTo(Expression.value(value))

        public infix fun String.notEqualTo(value: Any): Expression =
            property(this).notEqualTo(Expression.value(value))

        public infix fun String.lessThan(value: Any): Expression =
            property(this).lessThan(Expression.value(value))

        public infix fun String.lessThanOrEqualTo(value: Any): Expression =
            property(this).lessThanOrEqualTo(Expression.value(value))

        public infix fun String.greaterThan(value: Any): Expression =
            property(this).greaterThan(Expression.value(value))

        public infix fun String.greaterThanOrEqualTo(value: Any): Expression =
            property(this).greaterThanOrEqualTo(Expression.value(value))

        public infix fun String.like(value: Any): Expression =
            property(this).like(Expression.value(value))

        public infix fun String.isIn(value: Any): Expression =
            property(this).`in`(Expression.value(value))

        public fun String.between(from: Any, to: Any): Expression =
            property(this).between(Expression.value(from), Expression.value(to))

        public fun String.isNullOrMissing(): Expression = property(this).isNullOrMissing

        public fun String.isNotNullOrMissing(): Expression = property(this).notNullOrMissing()

        public infix fun Expression.and(other: Expression): Expression = and(other)
        public infix fun Expression.or(other: Expression): Expression = or(other)
    }
}
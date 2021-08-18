package com.digitalcrafts.couchbasektx.queryBuilder

import com.couchbase.lite.DataSource
import com.couchbase.lite.Meta
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.digitalcrafts.couchbasektx.cbHelper.CbHelper


public class DeQuery private constructor(
    private val cbHelper: CbHelper,
) {

    public fun select(vararg keys: String): DeWhere {

        val projections: MutableList<SelectResult> = mutableListOf()
        projections.addAll(keys.map(SelectResult::property))
        projections.add(SelectResult.expression(Meta.id))

        return DeWhere(
            cbHelper = cbHelper,
            from = QueryBuilder
                .select(*projections.toTypedArray())
                .from(DataSource.database(cbHelper.getDataBase()))
        )
    }

    public fun selectAll(): DeWhere {

        val projections: Array<SelectResult> = arrayOf(
            SelectResult.expression(Meta.id),
            SelectResult.all()
        )

        return DeWhere(
            cbHelper = cbHelper,
            from = QueryBuilder
                .select(*projections)
                .from(DataSource.database(cbHelper.getDataBase()))
        )
    }

    public companion object {

        public fun from(cbHelper: CbHelper): DeQuery = DeQuery(cbHelper)
    }
}
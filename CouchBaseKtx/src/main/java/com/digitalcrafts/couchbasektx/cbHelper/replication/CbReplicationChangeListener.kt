package com.digitalcrafts.couchbasektx.cbHelper.replication

public fun interface CbReplicationChangeListener {
    public fun onReplicationChange(status: DeReplicationChange)
}
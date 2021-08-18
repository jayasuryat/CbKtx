package com.digitalcrafts.couchbasektx.cbHelper.replication

import com.couchbase.lite.AbstractReplicator
import com.couchbase.lite.AbstractReplicatorConfiguration.ReplicatorType
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.ReplicatorChange

public data class DeReplicationChange(
    var status: AbstractReplicator.ActivityLevel,
    var progress: AbstractReplicator.Progress,
    var replicationType: ReplicatorType,
    var error: CouchbaseLiteException? = null,
) {

    internal companion object {

        internal fun from(replicatorChange: ReplicatorChange): DeReplicationChange =
            DeReplicationChange(
                status = replicatorChange.status.activityLevel,
                progress = replicatorChange.status.progress,
                replicationType = replicatorChange.replicator.config.replicatorType,
                error = replicatorChange.status.error,
            )
    }
}
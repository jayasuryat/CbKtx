package com.digitalcrafts.couchbasektx.cbHelper.replication

import com.couchbase.lite.AbstractReplicatorConfiguration.ReplicatorType
import com.couchbase.lite.ListenerToken
import com.couchbase.lite.Replicator
import java.util.*

public class CbReplicator private constructor(
    public val replicatorId: String,
    public val syncUrl: String,
    public val authenticator: CbAuthenticator,
    public val replicationType: ReplicatorType,
    public val channels: List<String>,
    public val isContinuous: Boolean,
    public val replicationChangeListener: CbReplicationChangeListener?,
) {

    internal var replicator: Replicator? = null
        private set

    internal var replicationChangeListenerToken: ListenerToken? = null
        private set

    internal var isStopping: Boolean = false

    internal fun setReplicator(replicator: Replicator, changeListenerToken: ListenerToken) {
        this.replicator = replicator
        this.replicationChangeListenerToken = changeListenerToken
    }

    internal fun clearReplicator() {
        replicator = null
        replicationChangeListenerToken = null
    }

    public class Builder(
        private val syncUrl: String,
        private val authenticator: CbAuthenticator
    ) {

        private var mReplicatorId: String = UUID.randomUUID().toString()
        private var isContinuous: Boolean = false
        private var replicationType: ReplicatorType = ReplicatorType.PUSH_AND_PULL
        private var channels: List<String> = emptyList()
        private var replicationChangeListener: CbReplicationChangeListener? = null

        public fun setReplicatorId(replicatorId: String): Builder {
            mReplicatorId = replicatorId
            return this
        }

        public fun setReplicationType(replicationType: ReplicatorType): Builder {
            this.replicationType = replicationType
            return this
        }

        public fun setIsContinuous(isContinuous: Boolean): Builder {
            this.isContinuous = isContinuous
            return this
        }

        public fun setChannels(vararg channels: String): Builder {
            this.channels = channels.toList()
            return this
        }

        public fun setReplicationChangeListener(syncChangeListener: CbReplicationChangeListener): Builder {
            replicationChangeListener = syncChangeListener
            return this
        }

        public fun build(): CbReplicator {
            return CbReplicator(
                replicatorId = mReplicatorId,
                syncUrl = syncUrl,
                authenticator = authenticator,
                replicationType = replicationType,
                channels = channels,
                isContinuous = isContinuous,
                replicationChangeListener = replicationChangeListener
            )
        }
    }
}
package com.digitalcrafts.couchbasektx.cbHelper

import com.couchbase.lite.AbstractReplicator
import com.couchbase.lite.ListenerToken
import com.couchbase.lite.ReplicatorChangeListener
import com.digitalcrafts.couchbasektx.cbHelper.replication.CbReplicator
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class ActiveReplicators {

    private val job: Job by lazy { Job() }
    private val ioScope: CoroutineScope by lazy { CoroutineScope(Dispatchers.IO + job) }

    private val lock: Mutex by lazy { Mutex() }
    private val listOfActiveReplicators: MutableList<CbReplicator> = mutableListOf()

    // region : Public API
    val activeReplicatorsCount: Int
        get() = listOfActiveReplicators.size

    fun addReplicator(replicator: CbReplicator) = addActiveReplicator(replicator)

    suspend fun stopReplicator(replicator: CbReplicator) = stopActiveReplicatorWithLock(replicator)

    suspend fun stopAllReplicators() = stopAllActiveReplicatorWithLock()
    // endregion

    // region : Backing methods
    private fun addActiveReplicator(replicator: CbReplicator) {

        ioScope.launch {

            lock.withLock {

                if (listOfActiveReplicators.find { it.replicatorId == replicator.replicatorId } == null)
                    listOfActiveReplicators.add(replicator)
            }
        }
    }

    private suspend fun stopActiveReplicatorWithLock(cbReplicator: CbReplicator) {
        lock.withLock { stopActiveReplicator(cbReplicator) }
    }

    private suspend fun stopAllActiveReplicatorWithLock() {

        lock.withLock {
            listOfActiveReplicators.forEach { replicator ->
                stopActiveReplicator(replicator)
            }
        }
    }
    // endregion

    // region : Helper methods
    private suspend fun stopActiveReplicator(cbReplicator: CbReplicator) {

        val replicator = cbReplicator.replicator ?: return
        val currentToken = cbReplicator.replicationChangeListenerToken

        val isCancellationOwner = !cbReplicator.isStopping

        if (isCancellationOwner) {
            currentToken?.let { replicator.removeChangeListener(currentToken) }
            cbReplicator.isStopping = true
        }

        return suspendCoroutine { continuation ->

            fun onReplicationStopped() {
                if (isCancellationOwner) {
                    cbReplicator.clearReplicator()
                    listOfActiveReplicators.remove(cbReplicator)
                }
                continuation.resume(Unit)
            }

            var stopToken: ListenerToken? = null

            val changeListener = ReplicatorChangeListener { replicationChange ->

                if (replicationChange.status.activityLevel == AbstractReplicator.ActivityLevel.STOPPED
                    && continuation.context.isActive
                ) {
                    replicator.removeChangeListener(stopToken!!)
                    onReplicationStopped()
                }
            }

            stopToken = replicator.addChangeListener(changeListener)
            replicator.stop()

            if (replicator.status.activityLevel == AbstractReplicator.ActivityLevel.STOPPED
                && continuation.context.isActive
            ) onReplicationStopped()
        }
    }
    // endregion
}
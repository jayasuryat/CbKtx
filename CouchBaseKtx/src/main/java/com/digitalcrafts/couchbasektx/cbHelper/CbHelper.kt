package com.digitalcrafts.couchbasektx.cbHelper

import android.content.Context
import com.couchbase.lite.*
import com.digitalcrafts.couchbasektx.cbHelper.replication.CbReplicator
import com.digitalcrafts.couchbasektx.cbHelper.replication.DeReplicationChange
import com.digitalcrafts.couchbasektx.serialzation.CbConverterFactory
import java.net.URI

public class CbHelper private constructor(
    context: Context,
    dataBaseName: String,
    private val converterFactory: CbConverterFactory,
) {

    private val dataBase: Database

    private val activeReplicators: ActiveReplicators by lazy { ActiveReplicators() }

    init {
        CouchbaseLite.init(context)
        dataBase = Database(dataBaseName, DatabaseConfiguration())
    }

    public fun getDataBase(): Database = dataBase

    public fun getConverterFactory(): CbConverterFactory = converterFactory

    public fun getActiveReplicatorsCount(): Int = activeReplicators.activeReplicatorsCount

    public fun startReplication(cbReplicator: CbReplicator) {

        val uri = URI(cbReplicator.syncUrl)

        val replicatorConfig = ReplicatorConfiguration(getDataBase(), URLEndpoint(uri)).apply {
            replicatorType = cbReplicator.replicationType
            if (cbReplicator.channels.isNotEmpty()) channels = cbReplicator.channels
            isContinuous = cbReplicator.isContinuous
            setAuthenticator(cbReplicator.authenticator.getAuthenticator())
        }

        val replicator = Replicator(replicatorConfig)

        val token = replicator.addChangeListener { replicationChange ->
            cbReplicator.replicationChangeListener
                ?.onReplicationChange(DeReplicationChange.from(replicationChange))
        }

        cbReplicator.setReplicator(
            replicator = replicator,
            changeListenerToken = token
        )

        replicator.start(false)

        activeReplicators.addReplicator(cbReplicator)
    }

    public suspend fun stopReplicator(replicator: CbReplicator): Unit =
        activeReplicators.stopReplicator(replicator)

    public suspend fun stopAllReplicators(): Unit =
        activeReplicators.stopAllReplicators()

    public class Builder(
        private val context: Context,
        private val dataBaseName: String,
        private val converterFactory: CbConverterFactory,
    ) {

        public fun build(): CbHelper = CbHelper(
            context = context,
            dataBaseName = dataBaseName,
            converterFactory = converterFactory
        )
    }
}
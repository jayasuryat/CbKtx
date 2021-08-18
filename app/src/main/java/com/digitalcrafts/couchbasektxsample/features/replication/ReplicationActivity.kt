package com.digitalcrafts.couchbasektxsample.features.replication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.couchbase.lite.AbstractReplicator
import com.couchbase.lite.AbstractReplicator.ActivityLevel.*
import com.couchbase.lite.AbstractReplicatorConfiguration.ReplicatorType.PULL
import com.digitalcrafts.couchbasektx.cbHelper.replication.*
import com.digitalcrafts.couchbasektxsample.R
import com.digitalcrafts.couchbasektxsample.helpers.Constants
import com.digitalcrafts.couchbasektxsample.helpers.DatabaseManager
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.*

class ReplicationActivity : AppCompatActivity(), CbReplicationChangeListener {

    private val job: Job by lazy { Job() }
    private val ioScope: CoroutineScope by lazy { CoroutineScope(job + Dispatchers.IO) }

    private val progressBar: LinearProgressIndicator by lazy { findViewById(R.id.pbReplication) }
    private val progressText: TextView by lazy { findViewById(R.id.tvProgress) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replication)
        ioScope.launch { startReplication() }
    }

    private fun startReplication() {

        val authenticator: CbAuthenticator = CbBasicAuthentication(
            username = Constants.USERNAME,
            password = Constants.PASSWORD,
        )

        val replicator = CbReplicator
            .Builder(
                syncUrl = Constants.SYNC_URL,
                authenticator = authenticator
            )
            .setReplicationType(PULL)
            .setIsContinuous(false)
            .setChannels("config")
            .setReplicationChangeListener(this@ReplicationActivity)
            .build()

        DatabaseManager.cbHelper
            .startReplication(replicator)
    }

    override fun onReplicationChange(status: DeReplicationChange) {

        Log.d(TAG, "status : $status")
        updateUiWith(status.progress)

        when (status.status) {
            OFFLINE,
            CONNECTING,
            IDLE,
            BUSY -> Unit
            STOPPED -> onStopped()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUiWith(progress: AbstractReplicator.Progress) {

        progressBar.apply {
            this.progress = progress.completed.toInt()
            max = progress.total.toInt()
        }

        progressText.text = "${progress.completed.toInt()} / ${progress.total.toInt()}"
    }

    private fun onStopped() {

        fun getReplicatorsCount(): Int = DatabaseManager.cbHelper.getActiveReplicatorsCount()

        ioScope.launch {

            Log.d(TAG, "Stopping all #${getReplicatorsCount()} replicator(s)")

            DatabaseManager.cbHelper
                .stopAllReplicators()

            Log.d(
                TAG, "All replicators have been stopped.\n" +
                        " Currently active replicator : ${getReplicatorsCount()}."
            )
        }
    }

    companion object {

        private const val TAG: String = "ReplicationActivity"
    }
}
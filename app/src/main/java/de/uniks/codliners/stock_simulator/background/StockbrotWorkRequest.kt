package de.uniks.codliners.stock_simulator.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import de.uniks.codliners.stock_simulator.background.workers.StockbrotWorker
import java.util.concurrent.TimeUnit

class StockbrotWorkRequest(context: Context) {

    private val workManager: WorkManager = WorkManager.getInstance(context)

    fun start() {
        println("start StockbrotWorkRequest")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
            .putDouble(THRESHOLD_BUY_KEY, thresholdBuy)
            .putDouble(THRESHOLD_SELL_KEY, thresholdSell)
            .build()

        val workRequest = PeriodicWorkRequest.Builder(StockbrotWorker::class.java, 30, TimeUnit.MINUTES)
            .addTag(WORKER_TAG)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueue(workRequest)
    }

    fun stop() {
        println("stop StockbrotWorkRequest")
        workManager.cancelAllWorkByTag(WORKER_TAG)
    }

    companion object {
        private const val WORKER_TAG = "STOCKBROT_WORKER"
    }
}
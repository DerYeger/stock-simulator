package de.uniks.codliners.stock_simulator.background

import android.content.Context
import androidx.work.*
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_DEFAULT_VALUE
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_DEFAULT_VALUE
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.WORKER_TAG
import de.uniks.codliners.stock_simulator.background.workers.StockbrotWorker
import java.util.concurrent.TimeUnit

class StockbrotWorkRequest(context: Context) {

    private val workManager: WorkManager = WorkManager.getInstance(context)
    var thresholdBuy: Double = THRESHOLD_BUY_DEFAULT_VALUE
    var thresholdSell: Double = THRESHOLD_SELL_DEFAULT_VALUE

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

}

package de.uniks.codliners.stock_simulator.background

import android.content.Context
import androidx.work.*
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_KEY
import de.uniks.codliners.stock_simulator.background.workers.StockbrotWorker
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import java.util.concurrent.TimeUnit

class StockbrotWorkRequest(context: Context) {

    private val workManager: WorkManager = WorkManager.getInstance(context)
    private val intervalMinutes: Long = 15

    fun addQuote(stockbrotQuote: StockbrotQuote) {
        println("start StockbrotWorkRequest")

        val thresholdBuy = stockbrotQuote.thresholdBuy
        val thresholdSell = stockbrotQuote.thresholdSell
        val symbol = stockbrotQuote.id

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
            .putDouble(THRESHOLD_BUY_KEY, thresholdBuy)
            .putDouble(THRESHOLD_SELL_KEY, thresholdSell)
            .build()

        val buildWorkerTag = buildWorkerTag(symbol)
        val workRequest = PeriodicWorkRequest.Builder(StockbrotWorker::class.java, intervalMinutes, TimeUnit.MINUTES)
            .addTag(buildWorkerTag)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueue(workRequest)
    }

    fun removeQuote(stockbrotQuote: StockbrotQuote) {
        println("stop StockbrotWorkRequest")
        val buildWorkerTag = buildWorkerTag(stockbrotQuote.id)
        workManager.cancelAllWorkByTag(buildWorkerTag)
    }

    private fun buildWorkerTag(symbol: String): String {
        return "WORKER_TAG_$symbol"
    }

}

package de.uniks.codliners.stock_simulator.background

import android.content.Context
import androidx.work.*
import de.uniks.codliners.stock_simulator.background.workers.StockbrotWorker
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import java.util.concurrent.TimeUnit

const val ID_KEY = "ID_KEY"

class StockbrotWorkRequest(context: Context) {

    private val workManager: WorkManager = WorkManager.getInstance(context)
    private val intervalMinutes: Long = 15

    fun addQuote(stockbrotQuote: StockbrotQuote) {
        val id = stockbrotQuote.id

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
            .putString(ID_KEY, id)
            .build()

        val buildWorkerTag = buildWorkerTag(id)
        val workRequest = PeriodicWorkRequest.Builder(StockbrotWorker::class.java, intervalMinutes, TimeUnit.MINUTES)
            .addTag(buildWorkerTag)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueue(workRequest)
    }

    fun removeQuote(stockbrotQuote: StockbrotQuote) {
        val buildWorkerTag = buildWorkerTag(stockbrotQuote.id)
        workManager.cancelAllWorkByTag(buildWorkerTag)
    }

    fun cancelAll() {
        workManager.cancelAllWork()
    }

    private fun buildWorkerTag(symbol: String): String {
        return "WORKER_TAG_$symbol"
    }

}

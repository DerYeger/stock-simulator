package de.uniks.codliners.stock_simulator.background

import android.content.Context
import androidx.work.*
import de.uniks.codliners.stock_simulator.background.workers.StockbrotWorker
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import java.util.concurrent.TimeUnit

/**
 * Key for the [StockbrotQuote] Id
 */
const val ID_KEY = "ID_KEY"

/**
 * Manages the [StockbrotWorker]
 *
 * @param context
 */
class StockbrotWorkRequest(context: Context) {

    private val workManager: WorkManager = WorkManager.getInstance(context)

    /**
     * Specifies the Worker interval
     */
    private val intervalMinutes: Long = 15

    /**
     * Adds a periodic [StockbrotWorker] to the WorkManager
     *
     * @param stockbrotQuote The [StockbrotQuote] to add
     */
    fun addQuote(stockbrotQuote: StockbrotQuote) {
        val id = stockbrotQuote.id

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
            .putString(ID_KEY, id)
            .build()

        val buildWorkerTag = buildWorkerTag(id)
        val workRequest = PeriodicWorkRequest.Builder(
            StockbrotWorker::class.java,
            intervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(buildWorkerTag)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager.enqueue(workRequest)
    }

    /**
     * Removes a [StockbrotWorker] from the WorkManager
     *
     * @param stockbrotQuote The [StockbrotQuote] to remove
     *
     * @author Lucas Held
     */
    fun removeQuote(stockbrotQuote: StockbrotQuote) {
        val buildWorkerTag = buildWorkerTag(stockbrotQuote.id)
        workManager.cancelAllWorkByTag(buildWorkerTag)
    }

    /**
     * Removes all [StockbrotWorker]s from the WorkManager
     *
     * @author Lucas Held
     */
    fun cancelAll() {
        workManager.cancelAllWork()
    }

    /**
     * Builds a unique tag for the Worker
     *
     * @param id The [StockbrotQuote] ID
     * @return unique tag [String]
     *
     * @author Lucas Held
     */
    private fun buildWorkerTag(id: String): String {
        return "WORKER_TAG_$id"
    }
}

package de.uniks.codliners.stock_simulator.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_DEFAULT_VALUE
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_DEFAULT_VALUE
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_KEY

class StockbrotWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext
        println("started StockbrotWorker")

        return try {
            val thresholdBuy = inputData.getDouble(THRESHOLD_BUY_KEY, THRESHOLD_BUY_DEFAULT_VALUE)
            val thresholdSell = inputData.getDouble(THRESHOLD_SELL_KEY, THRESHOLD_SELL_DEFAULT_VALUE)

            println("Bot running with thresholds\nbuy: $thresholdBuy\nsell: $thresholdSell")

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

}

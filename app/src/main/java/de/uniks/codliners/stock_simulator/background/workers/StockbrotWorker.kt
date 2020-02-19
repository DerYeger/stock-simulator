package de.uniks.codliners.stock_simulator.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_KEY

class StockbrotWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        println("started StockbrotWorker")

        val thresholdBuy = inputData.getDouble(THRESHOLD_BUY_KEY, -1.0)
        val thresholdSell = inputData.getDouble(THRESHOLD_SELL_KEY, -1.0)
        if (thresholdBuy == -1.0 || thresholdSell == -1.0 ) {
            println("Bot canceled because of illegal thresholds")
            return Result.failure()
        }

        return try {
            println("Bot running with thresholds\nbuy: $thresholdBuy\nsell: $thresholdSell")

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

}

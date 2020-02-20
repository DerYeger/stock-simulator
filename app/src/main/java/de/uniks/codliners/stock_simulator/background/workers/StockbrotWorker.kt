package de.uniks.codliners.stock_simulator.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.uniks.codliners.stock_simulator.background.Constants.Companion.BUY_AMOUNT_DEFAULT
import de.uniks.codliners.stock_simulator.background.Constants.Companion.BUY_AMOUNT_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.SYMBOL_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_DEFAULT
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_KEY
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StockbrotWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val quoteRepository = QuoteRepository(context)
    private val accountRepository = AccountRepository(context)
    var symbol: String = ""
    var buyAmount: Int = BUY_AMOUNT_DEFAULT
    var thresholdBuy: Double = THRESHOLD_DEFAULT
    var thresholdSell: Double = THRESHOLD_DEFAULT

    init {
        symbol = inputData.getString(SYMBOL_KEY) ?: ""
        buyAmount = inputData.getInt(BUY_AMOUNT_KEY, BUY_AMOUNT_DEFAULT)
        thresholdBuy = inputData.getDouble(THRESHOLD_BUY_KEY, THRESHOLD_DEFAULT)
        thresholdSell = inputData.getDouble(THRESHOLD_SELL_KEY, THRESHOLD_DEFAULT)
    }

    override fun doWork(): Result {
        println("started StockbrotWorker")

        if (
            (buyAmount == BUY_AMOUNT_DEFAULT && thresholdBuy != THRESHOLD_DEFAULT) ||
            (thresholdBuy == THRESHOLD_DEFAULT && thresholdSell == THRESHOLD_DEFAULT) ||
            symbol == ""
        ) {
            println("Bot canceled because of illegal thresholds")
            return Result.failure()
        }

        println("Bot is checking quotes for $symbol")

        CoroutineScope(Dispatchers.IO).launch {
            // fetch current quote infos
            quoteRepository.fetchQuoteWithSymbol(symbol)

            val quote = quoteRepository.quoteBySymbol(symbol)
            if (quote != null) {
                val latestPrice = quote.latestPrice
                val depotQuote = accountRepository.depotQuoteBySymbol(symbol)
                println("latest price: $latestPrice")

                if (thresholdBuy != THRESHOLD_DEFAULT) {
                    // buy is enabled
                    if (latestPrice >= thresholdBuy) {
                        println("Bot buys $buyAmount quotes with symbol $symbol now")
                        accountRepository.buy(quote, buyAmount)
                    }
                }
                if (thresholdSell != THRESHOLD_DEFAULT) {
                    // sell is enabled
                    if (latestPrice <= thresholdSell) {
                        if (depotQuote == null) {
                            println("quote is not available in our depot")
                        } else {
                            val amount = depotQuote.amount
                            println("Bot sells $amount quotes with symbol $symbol now")
                            accountRepository.sell(quote, amount.toInt())
                        }
                    }
                }
            }
        }
        return Result.success()
    }

}

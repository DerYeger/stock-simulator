package de.uniks.codliners.stock_simulator.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.uniks.codliners.stock_simulator.background.Constants.Companion.DOUBLE_DEFAULT
import de.uniks.codliners.stock_simulator.background.Constants.Companion.BUY_AMOUNT_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.SYMBOL_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.TYPE_DEFAULT
import de.uniks.codliners.stock_simulator.background.Constants.Companion.TYPE_KEY
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.toType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StockbrotWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val quoteRepository = QuoteRepository(context)
    private val accountRepository = AccountRepository(context)
    var symbol: String = ""
    var type: Symbol.Type = TYPE_DEFAULT
    var buyAmount: Double = DOUBLE_DEFAULT
    var thresholdBuy: Double = DOUBLE_DEFAULT
    var thresholdSell: Double = DOUBLE_DEFAULT

    init {
        symbol = inputData.getString(SYMBOL_KEY) ?: ""
        type = inputData.getString(TYPE_KEY)?.toType() ?: TYPE_DEFAULT
        buyAmount = inputData.getDouble(BUY_AMOUNT_KEY, DOUBLE_DEFAULT)
        thresholdBuy = inputData.getDouble(THRESHOLD_BUY_KEY, DOUBLE_DEFAULT)
        thresholdSell = inputData.getDouble(THRESHOLD_SELL_KEY, DOUBLE_DEFAULT)
    }

    override fun doWork(): Result {
        println("started StockbrotWorker")

        if (
            (buyAmount == DOUBLE_DEFAULT && thresholdBuy != DOUBLE_DEFAULT) ||
            (thresholdBuy == DOUBLE_DEFAULT && thresholdSell == DOUBLE_DEFAULT) ||
            symbol == ""
        ) {
            println("Bot canceled because of illegal thresholds")
            return Result.failure()
        }

        println("Bot is checking quotes for $symbol")

        CoroutineScope(Dispatchers.IO).launch {
            // fetch current quote infos
            quoteRepository.fetchQuoteWithSymbol(symbol, type)

            val quote = quoteRepository.quoteBySymbol(symbol)
            if (quote != null) {
                val latestPrice = quote.latestPrice
                val depotQuote = accountRepository.depotQuoteBySymbol(symbol)
                println("latest price: $latestPrice")

                if (thresholdBuy != DOUBLE_DEFAULT) {
                    // buy is enabled
                    if (latestPrice >= thresholdBuy) {
                        println("Bot buys $buyAmount quotes with symbol $symbol now")
                        accountRepository.buy(quote, buyAmount)
                    }
                }
                if (thresholdSell != DOUBLE_DEFAULT) {
                    // sell is enabled
                    if (latestPrice <= thresholdSell) {
                        if (depotQuote == null) {
                            println("quote is not available in our depot")
                        } else {
                            val amount = depotQuote.amount
                            println("Bot sells $amount quotes with symbol $symbol now")
                            accountRepository.sell(quote, amount)
                        }
                    }
                }
            }
        }
        return Result.success()
    }

}

package de.uniks.codliners.stock_simulator.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.background.ID_KEY
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class StockbrotWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val quoteRepository = QuoteRepository(context)
    private val accountRepository = AccountRepository(context)
    private val stockbrotRepository = StockbrotRepository(context)
    private val id = inputData.getString(ID_KEY) ?: ""

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    override fun doWork(): Result {
        scope.launch {
            // fetch current quote infos
            val stockbrotQuote = stockbrotRepository.stockbrotQuoteById(id) ?: return@launch

            when (stockbrotQuote.type) {
                Symbol.Type.SHARE -> quoteRepository.fetchIEXQuote(id)
                Symbol.Type.CRYPTO -> quoteRepository.fetchCoinGeckoQuote(id)
            }

            quoteRepository.quoteById(id)?.let {
                Timber.i("Bot is using quote $it")
                if (stockbrotQuote.thresholdBuy != 0.0) {
                    // buy is enabled
                    stockbrotQuote.executeBuyOrder(it)
                }
                if (stockbrotQuote.thresholdSell != 0.0) {
                    // sell is enabled
                    stockbrotQuote.executeSellOrder(it)
                }
            }
        }
        return Result.success()
    }

    private suspend fun StockbrotQuote.executeBuyOrder(quote: Quote) {
        if (quote.latestPrice <= thresholdBuy) {
            val balance = accountRepository.getLatestBalance()
            val amount = (balance.value - BuildConfig.TRANSACTION_COSTS) / quote.latestPrice
            val actualAmount = when (quote.type) {
                Symbol.Type.SHARE -> amount.toLong().toDouble()
                Symbol.Type.CRYPTO -> amount
            }.coerceAtMost(buyAmount)
            Timber.i("Bot is buying $actualAmount ($amount) for ${quote.latestPrice}")
            val newStockbrotQuote = this.copy(buyAmount = buyAmount - actualAmount)
            stockbrotRepository.addStockbrotQuote(newStockbrotQuote)
            accountRepository.buy(quote, actualAmount)
        }
    }

    private suspend fun StockbrotQuote.executeSellOrder(quote: Quote) {
        val depotQuote = accountRepository.depotQuoteBySymbol(id) ?: return
        if (quote.latestPrice >= thresholdSell) {
            val amount = depotQuote.amount
            Timber.i("Bot is selling $amount for ${quote.latestPrice}")
            accountRepository.sell(quote, amount)
        }
    }
}

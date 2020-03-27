package de.uniks.codliners.stock_simulator.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.background.ID_KEY
import de.uniks.codliners.stock_simulator.background.StockbrotWorkRequest
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

/**
 * This Worker is started by [StockbrotWorkRequest].
 * It's used to buy and sell quotes automatically in the background.
 *
 * @param context
 * @param params Params for the [StockbrotWorker]
 *
 * @author Lucas Held
 * @author Jan Müller
 * @author Juri Lozowoj
 */
class StockbrotWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val quoteRepository = QuoteRepository(context)
    private val accountRepository = AccountRepository(context)
    private val stockbrotRepository = StockbrotRepository(context)
    private val id = inputData.getString(ID_KEY) ?: ""

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private val stockbrotWorkRequest = StockbrotWorkRequest(context)

    /**
     * This function is called if the worker is started
     *
     * @return [Result] of the work process
     *
     * @author Lucas Held
     * @author Jan Müller
     * @author Juri Lozowoj
     */
    override fun doWork(): Result {
        scope.launch {
            // fetch current quote
            val stockbrotQuote = stockbrotRepository.stockbrotQuoteById(id) ?: return@launch

            val connectionSuccess =
                quoteRepository.fetchQuote(id = stockbrotQuote.id, type = stockbrotQuote.type)

            if (!connectionSuccess) return@launch

            quoteRepository.quoteById(id)?.let {
                Timber.i("Bot is using quote $it")
                if (stockbrotQuote.maximumBuyPrice != 0.0) {
                    // buy is enabled
                    stockbrotQuote.executeBuyOrder(it)
                }
                if (stockbrotQuote.minimumSellPrice != 0.0) {
                    // sell is enabled
                    stockbrotQuote.executeSellOrder(it)
                }
            }
        }
        return Result.success()
    }

    /**
     * Executes the buy order for the given quote
     *
     * @param quote
     *
     * @author Jan Müller
     * @author Lucas Held
     */
    private suspend fun StockbrotQuote.executeBuyOrder(quote: Quote) {
        if (quote.latestPrice > maximumBuyPrice) return
        val balance = accountRepository.getLatestBalance()
        val amount = (balance.value - BuildConfig.TRANSACTION_COSTS) / quote.latestPrice
        val typedAmount = when (quote.type) {
            Symbol.Type.SHARE -> amount.toLong().toDouble()
            Symbol.Type.CRYPTO -> amount
        }
        Timber.i("$limitedBuying with $buyLimit")
        val actualAmount = if (limitedBuying) typedAmount.coerceAtMost(buyLimit) else typedAmount
        if (actualAmount <= 0.0) return
        Timber.i("Bot is buying $actualAmount ($typedAmount / $amount) for ${quote.latestPrice}")
        val newStockbrotQuote = this.copy(buyLimit = (buyLimit - actualAmount).coerceAtLeast(0.0))
        if (newStockbrotQuote.limitedBuying && newStockbrotQuote.buyLimit == 0.0) {
            // Remove StockbrotQuote
            stockbrotRepository.removeStockbrotQuote(newStockbrotQuote)
            stockbrotWorkRequest.removeQuote(newStockbrotQuote)
        } else {
            // Update StockbrotQuote
            stockbrotRepository.addStockbrotQuote(newStockbrotQuote)
        }
        accountRepository.buy(quote, actualAmount)
    }

    /**
     * Executes the sell order for the given quote
     *
     * @param quote
     *
     * @author Jan Müller
     * @author Lucas Held
     */
    private suspend fun StockbrotQuote.executeSellOrder(quote: Quote) {
        val depotQuote = accountRepository.depotQuoteBySymbol(id) ?: return
        if (quote.latestPrice >= minimumSellPrice) {
            val amount = depotQuote.amount
            Timber.i("Bot is selling $amount for ${quote.latestPrice}")
            accountRepository.sell(quote, amount)
        }
    }
}

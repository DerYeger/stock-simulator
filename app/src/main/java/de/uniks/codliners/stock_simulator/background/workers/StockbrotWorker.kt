package de.uniks.codliners.stock_simulator.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.background.Constants.Companion.BUY_AMOUNT_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.DOUBLE_DEFAULT
import de.uniks.codliners.stock_simulator.background.Constants.Companion.ID_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_KEY
import de.uniks.codliners.stock_simulator.background.Constants.Companion.TYPE_DEFAULT
import de.uniks.codliners.stock_simulator.background.Constants.Companion.TYPE_KEY
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.toType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class StockbrotWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val quoteRepository = QuoteRepository(context)
    private val accountRepository = AccountRepository(context)
    private val id = inputData.getString(ID_KEY) ?: ""
    private val type: Symbol.Type = inputData.getString(TYPE_KEY)?.toType() ?: TYPE_DEFAULT
    private val buyAmount: Double = inputData.getDouble(BUY_AMOUNT_KEY, DOUBLE_DEFAULT)
    private val thresholdBuy: Double = inputData.getDouble(THRESHOLD_BUY_KEY, DOUBLE_DEFAULT)
    private val thresholdSell: Double = inputData.getDouble(THRESHOLD_SELL_KEY, DOUBLE_DEFAULT)

    override fun doWork(): Result {
        Timber.i("Started StockbrotWorker")

        if (
            (buyAmount == DOUBLE_DEFAULT && thresholdBuy != DOUBLE_DEFAULT) ||
            (thresholdBuy == DOUBLE_DEFAULT && thresholdSell == DOUBLE_DEFAULT) ||
            id.isBlank()
        ) {
            Timber.i("Bot canceled because of illegal thresholds")
            return Result.failure()
        }

        Timber.i("Bot is checking quotes for $id")

        CoroutineScope(Dispatchers.IO).launch {
            // fetch current quote infos
            when (type) {
                Symbol.Type.SHARE -> quoteRepository.fetchIEXQuote(id)
                Symbol.Type.CRYPTO -> quoteRepository.fetchCoinGeckoQuote(id)
            }

            quoteRepository.quoteById(id)?.let {
                Timber.i("Bot is using quote $it")
                if (thresholdBuy != DOUBLE_DEFAULT) {
                    // buy is enabled
                    executeBuyOrder(it)
                }
                if (thresholdSell != DOUBLE_DEFAULT) {
                    // sell is enabled
                    executeSellOrder(it)
                }
            }
        }
        return Result.success()
    }

    private suspend fun executeBuyOrder(quote: Quote) {
        if (quote.latestPrice <= thresholdBuy) {
            val balance = accountRepository.getLatestBalance()
            val amount = (balance.value - BuildConfig.TRANSACTION_COSTS) / quote.latestPrice
            val actualAmount = when (quote.type) {
                Symbol.Type.SHARE -> amount.toLong().toDouble()
                Symbol.Type.CRYPTO -> amount
            }.coerceAtMost(buyAmount)
            Timber.i("Bot is buying $actualAmount ($amount) for ${quote.latestPrice}")
            accountRepository.buy(quote, actualAmount)
        }
    }

    private suspend fun executeSellOrder(quote: Quote) {
        val depotQuote = accountRepository.depotQuoteBySymbol(id) ?: return
        if (quote.latestPrice >= thresholdSell) {
            val amount = depotQuote.amount
            Timber.i("Bot is selling $amount for ${quote.latestPrice}")
            accountRepository.sell(quote, amount)
        }
    }
}

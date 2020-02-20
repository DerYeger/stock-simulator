package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockbrotRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val enabledQuotes by lazy {
        database.stockbrotDao.getEnabledStockbrotQuotes()
    }

    suspend fun stockbrotQuoteWithSymbol(symbol: String): MutableLiveData<StockbrotQuote> {
        val stockbrotQuoteMutableLive = MutableLiveData<StockbrotQuote>()

        val stockbrotQuote = database.stockbrotDao.getStockbrotQuoteWithSymbol(symbol)
        if (stockbrotQuote.value != null) {
            stockbrotQuoteMutableLive.value = stockbrotQuote.value
        } else {
            val stockbrotQuoteNew = StockbrotQuote(symbol, 0.0, 0.0, false)
            stockbrotQuoteMutableLive.value = stockbrotQuoteNew
            withContext(Dispatchers.IO) {
                addStockbrotQuote(stockbrotQuoteNew)
            }
        }
        return stockbrotQuoteMutableLive
    }

    private suspend fun addStockbrotQuote(stockbrotQuote: StockbrotQuote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.insertStockbrotQuote(stockbrotQuote)
            database.stockbrotDao.getStockbrotQuoteWithSymbol(stockbrotQuote.symbol)
        }
    }

    suspend fun saveAddStockbrotControl(stockbrotQuote: StockbrotQuote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                insertStockbrotQuote(stockbrotQuote)
            }
        }
    }

    suspend fun saveRemoveStockbrotControl(stockbrotQuote: StockbrotQuote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                deleteStockbrotQuoteBySymbol(stockbrotQuote.symbol)
            }
        }
    }

    suspend fun resetStockbrot() {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                deleteStockbrote()
            }
        }
    }

}

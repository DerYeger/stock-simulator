package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.domain.Symbol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockbrotRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val quotes by lazy {
        database.stockbrotDao.getStockbrotQuotes()
    }

    fun stockbrotQuoteWithSymbol(symbol: String): LiveData<StockbrotQuote> =
        database.stockbrotDao.getStockbrotQuoteWithId(symbol)
    suspend fun stockbrotQuoteWithSymbol(
        symbol: String,
        type: Symbol.Type
    ): MutableLiveData<StockbrotQuote> {
        val stockbrotQuoteMutableLive = MutableLiveData<StockbrotQuote>()

        val stockbrotQuote = database.stockbrotDao.getStockbrotQuoteWithId(symbol)
        if (stockbrotQuote.value != null) {
            stockbrotQuoteMutableLive.value = stockbrotQuote.value
        } else {
            val stockbrotQuoteNew = StockbrotQuote(symbol, type, 0.0, 0.0)
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
            database.stockbrotDao.getStockbrotQuoteWithId(stockbrotQuote.id)
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
                deleteStockbrotQuoteById(stockbrotQuote.id)
            }
        }
    }

    suspend fun resetStockbrot() {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                deleteStockbrotQuotes()
            }
        }
    }

}

package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockbrotRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val quotes by lazy {
        database.stockbrotDao.getStockbrotQuotes()
    }

    fun stockbrotQuoteWithSymbol(symbol: String): LiveData<StockbrotQuote> =
        database.stockbrotDao.getStockbrotQuoteWithSymbol(symbol)

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

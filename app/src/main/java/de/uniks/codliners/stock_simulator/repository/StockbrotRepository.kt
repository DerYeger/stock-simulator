package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.NetworkUtils
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

    fun stockbrotQuoteWithId(id: String): LiveData<StockbrotQuote> =
        database.stockbrotDao.getStockbrotQuoteWithId(id)

    fun stockbrotQuoteById(id: String) = database.stockbrotDao.getStockbrotQuoteById(id)

    suspend fun addStockbrotQuote(stockbrotQuote: StockbrotQuote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                insertStockbrotQuote(stockbrotQuote)
            }
        }
    }

    suspend fun removeStockbrotQuote(stockbrotQuote: StockbrotQuote) {
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

package de.uniks.codliners.stock_simulator.repository

import android.content.Context
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

    suspend fun enableStockbrotControl(stockbrotQuote: StockbrotQuote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                insertStockbrotQuote(stockbrotQuote)
            }
        }
    }

    suspend fun disableStockbrotControl(stockbrotQuote: StockbrotQuote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                deleteStockbrotQuoteBySymbol(stockbrotQuote.symbol)
            }
        }
    }

}

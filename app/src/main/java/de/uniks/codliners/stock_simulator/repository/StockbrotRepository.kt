package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockbrotRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val quotes by lazy {
        database.stockbrotDao.getQuotes()
    }

    suspend fun enableStockbrotControl(quote: Quote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                insertQuote(quote)
            }
        }
    }

    suspend fun disableStockbrotControl(quote: Quote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                deleteQuoteBySymbol(quote.symbol)
            }
        }
    }

}

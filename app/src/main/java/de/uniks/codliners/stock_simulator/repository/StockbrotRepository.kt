package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for accessing, inserting and filtering [StockbrotQuote]s.
 *
 * @property database The database used by this repository.
 *
 * @author Lucas Held
 */
class StockbrotRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val quotes by lazy {
        database.stockbrotDao.getStockbrotQuotes()
    }

    fun stockbrotQuoteWithId(id: String): LiveData<StockbrotQuote> =
        database.stockbrotDao.getStockbrotQuoteWithId(id)

    fun stockbrotQuoteById(id: String) = database.stockbrotDao.getStockbrotQuoteById(id)

    /**
     * Adds a [StockbrotQuote] into the database.
     *
     * @param stockbrotQuote [StockbrotQuote] to be added into the database.
     *
     * @author Lucas Held
     */
    suspend fun addStockbrotQuote(stockbrotQuote: StockbrotQuote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                insertStockbrotQuote(stockbrotQuote)
            }
        }
    }

    /**
     * Removes a specific [StockbrotQuote] from the database.
     *
     * @param stockbrotQuote [StockbrotQuote] to be removed.
     *
     * @author Lucas Held
     */
    suspend fun removeStockbrotQuote(stockbrotQuote: StockbrotQuote) {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                deleteStockbrotQuoteById(stockbrotQuote.id)
            }
        }
    }

    /**
     * Removes all [StockbrotQuote]s from the database.
     *
     * @author Lucas Held
     */
    suspend fun resetStockbrot() {
        withContext(Dispatchers.IO) {
            database.stockbrotDao.apply {
                deleteStockbrotQuotes()
            }
        }
    }

}

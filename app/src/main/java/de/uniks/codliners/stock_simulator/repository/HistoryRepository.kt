package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val transactions: LiveData<List<Transaction>> = database.transactionDao.getTransactions()

    fun transactionsLimited(limit: Int): LiveData<List<Transaction>> = database.transactionDao.getTransactionsLimited(limit)

    fun transactionBySymbol(symbol: String): LiveData<List<Transaction>> = database.transactionDao.getTransactionsById(symbol)

    suspend fun resetHistory() {
        withContext(Dispatchers.IO) {
            database.transactionDao.apply {
                deleteTransactions()
            }
        }
    }
}
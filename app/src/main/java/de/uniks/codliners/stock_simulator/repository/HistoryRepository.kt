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

    suspend fun resetHistory() {
        withContext(Dispatchers.IO) {
            database.transactionDao.apply {
                deleteTransactions()
            }
        }
    }
}
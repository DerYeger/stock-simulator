package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.database.transactionsAsDomainModel
import de.uniks.codliners.stock_simulator.domain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val transactions: LiveData<List<Transaction>> =
        Transformations.map(database.transactionDao.getTransactions()) {
            it?.transactionsAsDomainModel()
        }

    fun transactionBySymbol(symbol: String): LiveData<List<Transaction>> = Transformations
        .map(database.transactionDao.getTransactionsByShareName(symbol)) {
            it?.transactionsAsDomainModel()
        }

    suspend fun resetHistory() {
        withContext(Dispatchers.IO) {
            database.transactionDao.apply {
                deleteTransactions()
            }
        }
    }
}
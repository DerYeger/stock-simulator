package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for accessing [Transaction]s.
 *
 * @property database The database used by this repository.
 * @property transactions [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing a [List] of all [Transaction]s from the [StockAppDatabase].
 *
 * @author Juri Lozowoj
 */
class HistoryRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val transactions: LiveData<List<Transaction>> = database.transactionDao.getTransactions()

    /**
     * Deletes all transactions from the database.
     *
     * @author Jonas Thelemann
     */
    suspend fun resetHistory() {
        withContext(Dispatchers.IO) {
            database.transactionDao.apply {
                deleteTransactions()
            }
        }
    }
}
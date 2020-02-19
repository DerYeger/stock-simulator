package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.database.transactionsAsDomainModel
import de.uniks.codliners.stock_simulator.domain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    sealed class State {
        object Loading : State()
        object Done : State()
        class Error(val message: String)
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    val transactions: LiveData<List<Transaction>> =
        Transformations.map(database.transactionDao.getTransactions()) {
            it?.transactionsAsDomainModel()
        }

    fun transactionByShareName(shareName: String): LiveData<List<Transaction>> = Transformations
        .map(database.transactionDao.getTransactionsByShareName(shareName)) {
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
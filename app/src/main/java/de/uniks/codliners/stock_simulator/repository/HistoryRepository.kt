package de.uniks.codliners.stock_simulator.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.database.transactionsAsDomainModel
import de.uniks.codliners.stock_simulator.domain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class HistoryRepository(private val stockAppDatabase: StockAppDatabase) {

    constructor(application: Application): this(getDatabase(application))

    sealed class State {
        object Loading: State()
        object Done: State()
        class Error(val message: String)
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    val transactions: LiveData<List<Transaction>> =
        Transformations.map(stockAppDatabase.transactionDao.getTransactions()) {
            it?.transactionsAsDomainModel()
        }

    fun transactionByShareName(shareName: String): LiveData<List<Transaction>> = Transformations
        .map(stockAppDatabase.transactionDao.getTransactionsByShareName(shareName)) {
            it?.transactionsAsDomainModel()
        }



}
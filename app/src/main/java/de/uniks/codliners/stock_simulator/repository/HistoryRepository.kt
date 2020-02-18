package de.uniks.codliners.stock_simulator.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.transactionsAsDomainModel
import de.uniks.codliners.stock_simulator.domain.Transaction

class HistoryRepository(private val stockAppDatabase: StockAppDatabase) {

    sealed class State {
        object Loading: State()
        object Done: State()
        class Error(val message: String)
    }

    val transactions: LiveData<List<Transaction>> =
        Transformations.map(stockAppDatabase.transactionDao.getTransactions()) {
            it?.transactionsAsDomainModel()
        }

    fun transactionByShareName(shareName: String): LiveData<List<Transaction>> = Transformations
        .map(stockAppDatabase.transactionDao.getTransactionsByShareName(shareName)) {
            it?.transactionsAsDomainModel()
        }



}
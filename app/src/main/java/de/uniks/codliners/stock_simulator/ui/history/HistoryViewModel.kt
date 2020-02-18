package de.uniks.codliners.stock_simulator.ui.history

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.domain.Share
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.domain.TransactionType
import de.uniks.codliners.stock_simulator.repository.HistoryRepository
import de.uniks.codliners.stock_simulator.ui.quote.QuoteViewModel
import kotlinx.coroutines.launch
import java.lang.Exception

class HistoryViewModel(application: Application) : ViewModel() {

    private val historyRepository = HistoryRepository(application)

    // private val state = historyRepository.state
    val transactions = historyRepository.transactions


    val shareOne = Share("AA", "american airline", 20.0, 0.2, 0.2, 0.1)
    val shareTwo = Share("SMA", "sma ", 19.0, 0.2, 0.2, 0.1)

    val transaction = Transaction(shareOne.name, 2, TransactionType.BUY, "20-11-2020")

    // val transactions = listOf(transaction)


    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

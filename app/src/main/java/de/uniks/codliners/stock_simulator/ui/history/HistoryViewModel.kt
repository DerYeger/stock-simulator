package de.uniks.codliners.stock_simulator.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.domain.Share
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.domain.TransactionType
import de.uniks.codliners.stock_simulator.repository.HistoryRepository
import kotlinx.coroutines.launch
import java.lang.Exception

class HistoryViewModel : ViewModel() {

//    private val historyRepository = HistoryRepository()

    val shareOne = Share("AA", "american airline", 20.0, 0.2, 0.2, 0.1)
    val shareTwo = Share("SMA", "sma ", 19.0, 0.2, 0.2, 0.1)

    val transaction = Transaction(shareOne.name, 2, TransactionType.BUY, 0L)

    val transactions = listOf(transaction)

    init {
        getHistory()
    }

    private fun getHistory() {
        viewModelScope.launch {
            try {

            } catch (exception: Exception) {


            }
        }
    }
}

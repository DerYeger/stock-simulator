package de.uniks.codliners.stock_simulator.ui.history

import androidx.lifecycle.ViewModel
import de.uniks.codliners.stock_simulator.domain.Share
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.domain.TransactionType

class HistoryViewModel : ViewModel() {

    val shareOne = Share("AA", "american airline", 20.0, 0.2, 0.2, 0.1)
    val shareTwo = Share("SMA", "sma ", 19.0, 0.2, 0.2, 0.1)

    val transaction = Transaction(shareOne, 2,  TransactionType.BUY, "20-11-2020")

    val transactions = listOf(transaction)
}

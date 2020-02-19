package de.uniks.codliners.stock_simulator.ui.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.uniks.codliners.stock_simulator.repository.HistoryRepository

class HistoryViewModel(application: Application) : ViewModel() {

    private val historyRepository = HistoryRepository(application)

    val transactions = historyRepository.transactions

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

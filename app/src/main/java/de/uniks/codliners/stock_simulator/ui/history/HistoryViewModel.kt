package de.uniks.codliners.stock_simulator.ui.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.uniks.codliners.stock_simulator.repository.HistoryRepository

class HistoryViewModel(application: Application) : ViewModel() {

    private val historyRepository = HistoryRepository(application)

    val transactions = historyRepository.transactions

    /**
     * Factory for the HistoryViewModel.
     *
     * @property application The context used for creating the repositories.
     */
    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        /**
         * The factory's construction method.
         *
         * @param T The class's type.
         * @param modelClass The class to create.
         *
         * @throws [IllegalArgumentException] if [HistoryViewModel] is not assignable to [modelClass].
         *
         * @return A [HistoryViewModel] instance.
         */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

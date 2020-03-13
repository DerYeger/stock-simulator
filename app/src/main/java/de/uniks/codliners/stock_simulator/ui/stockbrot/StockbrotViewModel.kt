package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository

/**
 * ViewModel for the stockbrot ui.
 *
 * @constructor
 * @property stockbrotQuotes List of all [StockbrotQuote]s from the repository.
 *
 * @param application The application to create a [StockbrotRepository] for.
 *
 * @author Lucas Held
 */
class StockbrotViewModel(application: Application) : ViewModel() {

    private val stockbrotRepository = StockbrotRepository(application)

    val stockbrotQuotes = stockbrotRepository.quotes

    /**
     * Factory for the StockbrotViewModel.
     *
     * @property application The context used for creating the [StockbrotRepository].
     */
    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        /**
         * Attempts to create a [StockbrotViewModel].
         *
         * @param T The requested type of [ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel).
         * @param modelClass The requested class. [StockbrotViewModel] must be assignable to it.
         *
         * @throws [IllegalArgumentException] if [StockbrotViewModel] is not assignable to [modelClass].
         *
         * @return The created [StockbrotViewModel].
         */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StockbrotViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StockbrotViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}

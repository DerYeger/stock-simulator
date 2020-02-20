package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository

class StockbrotViewModel(application: Application) : ViewModel() {

    private val stockbrotRepository = StockbrotRepository(application)

    val stockbrotQuotes = stockbrotRepository.enabledQuotes

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StockbrotViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StockbrotViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}

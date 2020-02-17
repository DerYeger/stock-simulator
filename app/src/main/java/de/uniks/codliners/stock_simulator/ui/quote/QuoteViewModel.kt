package de.uniks.codliners.stock_simulator.ui.quote

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import kotlinx.coroutines.launch

class QuoteViewModel(application: Application, private val symbol: String) : ViewModel() {

    private val quoteRepository = QuoteRepository(application)

    val quote = quoteRepository.quoteWithSymbol(symbol)
    val refreshing = quoteRepository.refreshing

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            quoteRepository.fetchQuoteWithSymbol(symbol)
        }
    }

    class Factory(
        private val application: Application,
        private val shareId: String
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuoteViewModel(application, shareId) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

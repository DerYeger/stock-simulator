package de.uniks.codliners.stock_simulator.ui.quote

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import kotlinx.coroutines.launch

class QuoteViewModel(application: Application, private val symbol: String) : ViewModel() {

    private val quoteRepository = QuoteRepository(application)
    private val accountRepository = AccountRepository(application)

    val quote = quoteRepository.quoteWithSymbol(symbol)
    private val state = quoteRepository.state
    val refreshing = state.map { it === QuoteRepository.State.Refreshing }

    private val _errorAction = MediatorLiveData<String>()
    val errorAction: LiveData<String> = _errorAction

    init {
        _errorAction.addSource(state) { state ->
            _errorAction.value = when (state) {
                is QuoteRepository.State.Error -> state.message
                else -> null
            }
        }

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            quoteRepository.fetchQuoteWithSymbol(symbol)
        }
    }

    fun onErrorActionCompleted() {
        viewModelScope.launch {
            _errorAction.value = null
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

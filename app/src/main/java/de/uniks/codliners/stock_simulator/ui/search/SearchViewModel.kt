package de.uniks.codliners.stock_simulator.ui.search

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class SearchViewModel(application: Application) : ViewModel() {

    private val symbolRepository = SymbolRepository(application)
    private val symbols = symbolRepository.symbols
    private val state = symbolRepository.state
    val refreshing = state.map { it === SymbolRepository.State.Refreshing }

    private val _searchResults = MediatorLiveData<List<Symbol>>()
    val searchResults: LiveData<List<Symbol>> = _searchResults

    val searchQuery = MutableLiveData<String>()

    init {
        _searchResults.apply {
            addSource(symbols) { symbols: List<Symbol>? ->
                val formattedQuery = searchQuery.value?.toUpperCase(Locale.getDefault())
                value = symbols?.filter { formattedQuery.isNullOrBlank() || it.symbol.startsWith(formattedQuery) }
            }

            addSource(searchQuery) { query ->
                val symbols = symbols.value
                val formattedQuery = query.toUpperCase(Locale.getDefault())
                value = symbols?.filter { formattedQuery.isNullOrBlank() || it.symbol.startsWith(formattedQuery) }
            }
        }

        refreshSymbols()
    }

    fun refreshSymbols() {
        viewModelScope.launch {
            symbolRepository.refreshSymbols()
        }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

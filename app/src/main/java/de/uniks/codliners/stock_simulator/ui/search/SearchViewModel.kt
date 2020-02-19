package de.uniks.codliners.stock_simulator.ui.search

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import kotlinx.coroutines.launch
import java.util.*

class SearchViewModel(application: Application) : ViewModel() {

    private val symbolRepository = SymbolRepository(application)
    private val symbols = symbolRepository.symbols
    private val state = symbolRepository.state
    val refreshing = state.map { it === SymbolRepository.State.Refreshing }

    private val _searchResults = MediatorLiveData<List<Symbol>>()
    val searchResults: LiveData<List<Symbol>> = _searchResults

    val searchQuery = MutableLiveData<String>()
    val typeFilter = MutableLiveData<String>()

    init {
        _searchResults.apply {
            addSource(symbols) { symbols: List<Symbol>? ->
                value = symbols?.filtered(
                    query = searchQuery.value,
                    typeFilter = typeFilter.value
                )
            }

            addSource(searchQuery) { query ->
                value = symbols.value?.filtered(
                    query = query,
                    typeFilter = typeFilter.value
                )
            }

            addSource(typeFilter) { typeFilter ->
                value = symbols.value?.filtered(
                    query = searchQuery.value,
                    typeFilter = typeFilter
                )
            }
        }

        refreshSymbols()
    }

    fun refreshSymbols() {
        viewModelScope.launch {
            symbolRepository.refreshSymbols()
        }
    }

    private fun List<Symbol>.filtered(query: String?, typeFilter: String?) = filter { symbol ->
        symbol.matchesQuery(query) && symbol.matchesTypeFilter(typeFilter)
    }

    private fun Symbol.matchesQuery(query: String?): Boolean {
        val formattedQuery = query?.toUpperCase(Locale.getDefault())
        return formattedQuery.isNullOrBlank() || symbol.toUpperCase(Locale.getDefault()).startsWith(formattedQuery)
    }

    private fun Symbol.matchesTypeFilter(typeFilter: String?): Boolean {
        // TODO unhack this
        return when (typeFilter) {
            "Shares" -> type !== "Crypto"
            "Crypto" -> type === "crypto"
            else -> true
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

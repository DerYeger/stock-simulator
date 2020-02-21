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

    private val _errorAction = MediatorLiveData<String>()
    val errorAction: LiveData<String> = _errorAction

    private val _searchResults = MediatorLiveData<List<Symbol>>()
    val searchResults: LiveData<List<Symbol>> = _searchResults

    val searchQuery = MutableLiveData<String>()
    val typeFilter = MutableLiveData<String>()

    init {
        _errorAction.apply {
            addSource(state) { state ->
                value = when (state) {
                    is SymbolRepository.State.Error -> state.message
                    else -> null
                }
            }
        }

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
    }

    fun refreshSymbols() {
        viewModelScope.launch {
            symbolRepository.refreshSymbols()
        }
    }

    fun onErrorActionCompleted() {
        viewModelScope.launch {
            _errorAction.value = null
        }
    }

    private fun List<Symbol>.filtered(query: String?, typeFilter: String?): List<Symbol> {
        val formattedQuery = query?.toUpperCase(Locale.getDefault())
        return if (typeFilter == "Any") {
            filter { symbol -> symbol.matchesQuery(formattedQuery) }
        } else {
            val type = typeFilter.asSymbolType()
            filter { symbol -> symbol.type === type && symbol.matchesQuery(formattedQuery) }
        }
    }

    private fun Symbol.matchesQuery(query: String?) =
        query === null || symbol.toUpperCase(Locale.getDefault()).startsWith(query)

    private fun String?.asSymbolType() = when (this) {
        "Crypto" -> Symbol.Type.CRYPTO
        else -> Symbol.Type.SHARE
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

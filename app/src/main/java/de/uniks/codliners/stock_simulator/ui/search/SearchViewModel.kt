package de.uniks.codliners.stock_simulator.ui.search

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.mediatedLiveData
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData

class SearchViewModel(application: Application) : ViewModel() {

    private val symbolRepository = SymbolRepository(application)
    private val symbols = symbolRepository.symbols

    val searchQuery = MutableLiveData<String>()
    val typeFilter = MutableLiveData<String>()

    val filteredSymbols: LiveData<List<Symbol>> =
        sourcedLiveData(symbols, searchQuery, typeFilter) {
            _isLoading.value = true
            Symbol.Filter(
                query = searchQuery.value ?: "",
                type = typeFilter.value.asSymbolType()
            )
        }.switchMap { symbolFilter ->
            symbolRepository.filteredSymbols(symbolFilter)
        }

    private val _isLoading: MutableLiveData<Boolean> = mediatedLiveData {
        addSource(symbols) { symbols: List<Symbol>? ->
            value = symbols?.size ?: 0 == 0 || value ?: false
        }
    }
    val isLoading: LiveData<Boolean> = _isLoading

    val hasResults: LiveData<Boolean> = sourcedLiveData(filteredSymbols) {
        filteredSymbols.value?.size ?: 0 > 0
    }

    val hasNoResults: LiveData<Boolean> = sourcedLiveData(filteredSymbols, isLoading) {
        filteredSymbols.value?.size == 0 && !(isLoading.value ?: false)
    }

    val onSymbolListSubmitted = Runnable { _isLoading.postValue(false) }

    private fun String?.asSymbolType() = when (this) {
        "Crypto" -> Symbol.Type.CRYPTO
        "Shares" -> Symbol.Type.SHARE
        else -> null
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

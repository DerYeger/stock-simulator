package de.uniks.codliners.stock_simulator.ui.search

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData
import timber.log.Timber
import java.util.*

class SearchViewModel(application: Application) : ViewModel() {

    private val symbolRepository = SymbolRepository(application)
    private val symbols = symbolRepository.symbols

    val searchQuery = MutableLiveData<String>()
    val typeFilter = MutableLiveData<String>()
    private val searching = MutableLiveData<Boolean>(false)

    val filteredSymbols = sourcedLiveData(symbols, searchQuery, typeFilter) {
        searching.value = true
        val formattedQuery = searchQuery.value ?: ""
        SymbolRepository.SymbolFilter(
            "$formattedQuery%",
            typeFilter.value.asSymbolType()
        ).also { Timber.i("Created filter $it") }
    }.switchMap { symbolFilter ->
        Timber.i("Filtering with $symbolFilter")
        symbolRepository.filteredSymbols(symbolFilter).also {
            Timber.i("Got the filtered symbols")
            searching.value = false
        }
    }

//    // The search implementation should be refactored, as it currently runs on the UI-Thread
//    val searchResults = sourcedLiveData(symbols, searchQuery, typeFilter) {
//        // Manually set isLoading to true, as the UI-Thread is blocked by the search. This should be refactored in the future
//        (isLoading as MutableLiveData<Boolean>).value = true
//
//        searching.value = true
//        symbols.value?.filtered(
//            query = searchQuery.value,
//            typeFilter = typeFilter.value
//        ).also {
//            searching.value = false
//
//            // Manually set isLoading to false, as the UI-Thread is blocked by the search. This should be refactored in the future
//            isLoading.value = false
//        }
//    }

    val isLoading = sourcedLiveData(symbols, searching) {
        symbols.value?.size ?: 0 == 0 || searching.value ?: false
    }

    val hasResults = sourcedLiveData(filteredSymbols, isLoading) {
        filteredSymbols.value?.size ?: 0 > 0 && !(isLoading.value ?: false)
    }

    val hasNoResults = sourcedLiveData(filteredSymbols, isLoading) {
        filteredSymbols.value?.size == 0 && !(isLoading.value ?: false)
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

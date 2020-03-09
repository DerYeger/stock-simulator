package de.uniks.codliners.stock_simulator.ui.search

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.mediatedLiveData
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData

/**
 * The [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) of [SearchFragment].
 *
 * @param application The context used for creating the [SymbolRepository].
 *
 * @property searchQuery The search query used for filtering symbols.
 * @property typeFilter The type filter used for filtering symbols.
 * @property filteredSymbols The locally stored symbols filtered by the symbol, name and type.
 * @property isBusy Contains true while the repository is filtering. Has to be manually reset by running [onFilteredSymbolsApplied], once the filtered [Symbol] [List] has been applied.
 * @property onFilteredSymbolsApplied Resets [isBusy] to false. Should be called once [filteredSymbols]-changes have been applied (submitted to the adapter).
 * @property hasResults Contains true if [filteredSymbols] contains at least one [Symbol] or false otherwise
 * @property hasNoResults Contains true if [filteredSymbols] contains no [Symbol]s and [isBusy] contains false or false otherwise
 *
 * @author Jan Müller
 */
class SearchViewModel(application: Application) : ViewModel() {

    private val symbolRepository = SymbolRepository(application)
    private val symbols = symbolRepository.symbols

    val searchQuery = MutableLiveData<String>()
    val typeFilter = MutableLiveData<String>()

    val filteredSymbols: LiveData<List<Symbol>> =
        sourcedLiveData(symbols, searchQuery, typeFilter) {
            Symbol.Filter(
                query = searchQuery.value ?: "",
                type = typeFilter.value.asSymbolType()
            )
        }.switchMap { symbolFilter ->
            _isBusy.value = true
            symbolRepository.filteredSymbols(symbolFilter)
        }

    private val _isBusy: MutableLiveData<Boolean> = mediatedLiveData {
        addSource(symbols) { symbols: List<Symbol>? ->
            value = symbols?.size ?: 0 == 0 || value ?: false
        }
    }
    val isBusy: LiveData<Boolean> = _isBusy

    val onFilteredSymbolsApplied = Runnable { _isBusy.postValue(false) }

    val hasResults: LiveData<Boolean> = sourcedLiveData(filteredSymbols) {
        filteredSymbols.value?.size ?: 0 > 0
    }

    val hasNoResults: LiveData<Boolean> = sourcedLiveData(filteredSymbols, isBusy) {
        filteredSymbols.value?.size == 0 && !(isBusy.value ?: false)
    }

    private fun String?.asSymbolType() = when (this) {
        "Crypto" -> Symbol.Type.CRYPTO
        "Shares" -> Symbol.Type.SHARE
        else -> null
    }

    /**
     * The factory for [SearchViewModel]s.
     *
     * @property application The context used for creating the [SymbolRepository].
     */
    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        /**
         * Attempts to create a [SearchViewModel].
         *
         * @param T The requested type of [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel).
         * @param modelClass The requested class. [SearchViewModel] must be assignable to it.
         *
         * @throws [IllegalArgumentException] if [SearchViewModel] is not assignable to [modelClass].
         *
         * @return The created [SearchViewModel].
         */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

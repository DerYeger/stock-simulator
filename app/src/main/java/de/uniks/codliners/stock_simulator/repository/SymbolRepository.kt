package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.network.NetworkService
import de.uniks.codliners.stock_simulator.network.asDomainSymbols
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for refreshing, accessing and filtering [Symbol]s.
 *
 * @property database The database used by this repository.
 * @property state The current [State] of the repository.
 * @property symbols Lazily initialized [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing an ordered [List] of all locally stored [Symbol]s.
 *
 * @author Jan Müller
 */
class SymbolRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    sealed class State {
        object Idle : State()
        object Refreshing : State()
        object Done : State()
        class Error(val message: String) : State()
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    val symbols by lazy {
        database.symbolDao.getAll()
    }

    /**
     * Returns a [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing [Symbol]s that match the specified [Symbol.Filter].
     *
     * @param filter The filter to be used.
     * @return A [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing a filtered [List] of [Symbol]s.
     */
    fun filteredSymbols(filter: Symbol.Filter) = when (filter.type) {
        null -> database.symbolDao.getAllFiltered(
            symbolQuery = filter.symbolQuery,
            nameQuery = filter.nameQuery
        )
        else -> database.symbolDao.getAllFiltered(
            symbolQuery = filter.symbolQuery,
            nameQuery = filter.nameQuery,
            type = filter.type
        )
    }

    /**
     * Fetches all available symbols from the IEX and CoinGecko APIs, then stores them in the [StockAppDatabase].
     */
    suspend fun refreshSymbols() {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Refreshing)
                val shareSymbols = NetworkService.IEX_API.symbols()
                val cryptoSymbols = NetworkService.COINGECKO_API.symbols()
                database.symbolDao.insertAll(
                    *shareSymbols.asDomainSymbols(),
                    *cryptoSymbols.asDomainSymbols()
                )
                _state.postValue(State.Done)
                _state.postValue(State.Idle)
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }
}

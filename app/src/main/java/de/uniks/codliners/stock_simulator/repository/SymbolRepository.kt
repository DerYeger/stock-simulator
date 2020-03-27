package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.network.NetworkService
import de.uniks.codliners.stock_simulator.network.asDomainSymbols
import de.uniks.codliners.stock_simulator.repository.SymbolRepository.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository for refreshing, accessing and filtering [Symbol]s.
 *
 * @property database The database used by this repository.
 * @property state The current [State] of the repository.
 * @property symbols Lazily initialized [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing an ordered [List] of all locally stored [Symbol]s.
 *
 * @author Jan Müller
 */
class SymbolRepository(private val database: StockAppDatabase) {

    /**
     * Constructor that allows repository creation from a [Context].
     */
    constructor(context: Context) : this(getDatabase(context))

    /**
     * The state of a [SymbolRepository].
     *
     * @author Jan Müller
     */
    sealed class State {

        /**
         * Indicates that a [SymbolRepository] is idle.
         */
        object Idle : State()

        /**
         * Indicates that a [SymbolRepository] is currently working.
         */
        object Working : State()

        /**
         * Indicates that a [SymbolRepository]'s previous task has been completed.
         */
        object Done : State()

        /**
         * Indicates that a [SymbolRepository] has encountered an exception.
         *
         * @property exception The exception that caused this [State].
         */
        class Error(val exception: Exception) : State()
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    /**
     * List of all [Symbol]s.
     */
    val symbols by lazy {
        database.symbolDao.getAll()
    }

    /**
     * Returns a [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing [Symbol]s that match the specified [Symbol.Filter].
     *
     * @param filter The filter to be used.
     * @return A [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing a filtered [List] of [Symbol]s.
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
            _state.postValue(State.Working)
            try {
                val shareSymbols = NetworkService.IEX_API.symbols().asDomainSymbols()
                database.symbolDao.insertAll(*shareSymbols)
                _state.postValue(State.Done)
            } catch (exception: Exception) {
                Timber.e(exception)
                _state.postValue(State.Error(exception))
            }
            try {
                val cryptoSymbols = NetworkService.COINGECKO_API.symbols().asDomainSymbols()
                database.symbolDao.insertAll(*cryptoSymbols)
            } catch (exception: Exception) {
                Timber.e(exception)
                _state.postValue(State.Error(exception))
            }
            _state.postValue(State.Done)
            _state.postValue(State.Idle)
        }
    }

    /**
     * Checks if there are any [Symbol]s in the [StockAppDatabase].
     *
     * @return true if there are any [Symbol]s and false otherwise.
     */
    suspend fun hasSymbols(): Boolean = withContext(Dispatchers.IO) {
        database.symbolDao.getSymbolCount() > 0
    }
}

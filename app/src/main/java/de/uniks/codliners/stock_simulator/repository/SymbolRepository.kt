package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.network.NetworkService
import de.uniks.codliners.stock_simulator.network.asDomainSymbols
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SymbolRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    sealed class State {
        object Refreshing : State()
        object Done : State()
        class Error(val message: String) : State()
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    val symbols = database.symbolDao.getAll()

    fun symbol(symbol: String) = database.symbolDao.get(symbol)

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
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }
}

package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Exception

class SymbolRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    sealed class State {
        object Refreshing : State()
        object Done : State()
        class Error(val message: String) : State()
    }

    private val _state = MutableLiveData<State>().apply {
        value = State.Done
    }
    val state: LiveData<State> = _state

    val symbols = database.symbolDao.getAll()

    suspend fun refreshSymbols() {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Refreshing)
                val response = NetworkService.IEX_API.symbols()
                database.symbolDao.insertAll(*response.toTypedArray())
                _state.postValue(State.Done)
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }
}
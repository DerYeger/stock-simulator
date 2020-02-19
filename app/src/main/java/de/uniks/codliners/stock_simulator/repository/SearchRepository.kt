package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class SearchRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    sealed class State {
        object Empty : State()
        object Searching : State()
        object Done : State()
        class Error(val message: String) : State()
    }

    private val _state = MutableLiveData<State>().apply {
        value = State.Empty
    }
    val state: LiveData<State> = _state

    private val symbols = database.symbolDao.getAll()

    private val _searchResults = MediatorLiveData<List<Symbol>>()
    val searchResults: LiveData<List<Symbol>> = _searchResults

    val searchQuery = MutableLiveData<String>()

    init {
        _searchResults.apply {
            addSource(symbols) { symbols: List<Symbol>? ->
                val query = searchQuery.value
                value = symbols?.filter { query.isNullOrBlank() || it.symbol.contains(searchQuery.value.toString()) }
            }

            addSource(searchQuery) { query ->
                val symbols = symbols.value
                value = symbols?.filter { query.isNullOrBlank() || it.symbol.contains(searchQuery.value.toString()) }
            }
        }
    }

    suspend fun refreshSymbols() {
        withContext(Dispatchers.IO) {
            val response = NetworkService.IEX_API.symbols()
            database.symbolDao.insertAll(*response.toTypedArray())
        }
    }
}

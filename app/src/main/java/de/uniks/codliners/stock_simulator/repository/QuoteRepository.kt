package de.uniks.codliners.stock_simulator.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(private val database: StockAppDatabase) {

    constructor(application: Application) : this(getDatabase(application))

    sealed class State {
        object Empty: State()
        object Refreshing: State()
        object Done: State()
        class Error(val message: String): State()
    }

    private val _state = MutableLiveData<State>().apply {
        value = State.Empty
    }
    val state: LiveData<State> = _state


    fun quoteWithSymbol(symbol: String): LiveData<Quote> = database.quoteDao.getQuoteWithSymbol(symbol)

    suspend fun fetchQuoteWithSymbol(symbol: String) {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Refreshing)
                val response = NetworkService.IEX_API.quote(symbol)
                database.quoteDao.insert(response)
                _state.postValue(State.Done)
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }
}

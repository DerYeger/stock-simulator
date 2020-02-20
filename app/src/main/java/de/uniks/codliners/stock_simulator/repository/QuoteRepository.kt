package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.HistoricalPrice
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.apiPricesAsPricesWithSymbol
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.network.NetworkService
import de.uniks.codliners.stock_simulator.network.asDomainQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    sealed class State {
        object Empty : State()
        object Refreshing : State()
        object Done : State()
        class Error(val message: String) : State()
    }

    private val _state = MutableLiveData<State>().apply {
        postValue(State.Empty)
    }
    val state: LiveData<State> = _state

    fun quoteWithSymbol(symbol: String): LiveData<Quote> =
        database.quoteDao.getQuoteWithSymbol(symbol)

    fun quoteBySymbol(symbol: String): Quote? =
        database.quoteDao.getQuoteValueBySymbol(symbol)

    fun historicalPrices(symbol: String): LiveData<List<HistoricalPrice>> =
        database.historicalDao.getHistoricalPricesBySymbol(symbol)

    suspend fun fetchQuoteWithSymbol(symbol: String, type: Symbol.Type) {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Refreshing)
                val quote = NetworkService.IEX_API.quote(symbol).asDomainQuote(type)
                database.quoteDao.insert(quote)
                val historicalPricesFromApi = NetworkService.IEX_API.historical(symbol = symbol, chartCloseOnly = true)
                val historicalPricesWithSymbol = historicalPricesFromApi.apiPricesAsPricesWithSymbol(symbol)
                database.historicalDao.deleteHistoricalPricesBySymbol(symbol)
                database.historicalDao.insertAll(*historicalPricesWithSymbol.toTypedArray())
                _state.postValue(State.Done)
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }

    suspend fun resetQuotes() {
        withContext(Dispatchers.IO) {
            database.quoteDao.apply {
                deleteQuotes()
            }
        }
    }
}

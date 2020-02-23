package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.HistoricalPrice
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.network.NetworkService
import de.uniks.codliners.stock_simulator.network.asDomainHistoricalPrices
import de.uniks.codliners.stock_simulator.network.asDomainQuote
import de.uniks.codliners.stock_simulator.network.asHistoricalPrices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val HISTORICAL_PRICE_LIMIT: Int = 50

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

    fun quoteWithId(symbol: String): LiveData<Quote> =
        database.quoteDao.getQuoteWithId(symbol)

    fun quoteById(symbol: String): Quote? =
        database.quoteDao.getQuoteValueById(symbol)

    fun historicalPrices(symbol: String): LiveData<List<HistoricalPrice>> =
        database.historicalDao.getHistoricalPricesById(symbol)

    fun historicalPricesLimited(symbol: String, limit: Int = HISTORICAL_PRICE_LIMIT): LiveData<List<HistoricalPrice>> =
        database.historicalDao.getHistoricalPricesByIdLimited(symbol, limit)

    suspend fun fetchIEXQuote(symbol: String) {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Refreshing)
                val quote = NetworkService.IEX_API.quote(symbol).asDomainQuote()
                database.quoteDao.insert(quote)
                val historicalPrices = NetworkService.IEX_API
                    .historicalPrices(symbol = symbol, chartCloseOnly = true)
                    .asDomainHistoricalPrices(symbol)
                // database.historicalDao.deleteHistoricalPricesById(symbol)
                database.historicalDao.insertAll(*historicalPrices.toTypedArray())
                _state.postValue(State.Done)
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }

    suspend fun fetchCoinGeckoQuote(id: String) {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Refreshing)
                val quote = NetworkService.COINGECKO_API.quote(id).asDomainQuote()
                database.quoteDao.insert(quote)
                val historicalPrices = NetworkService.COINGECKO_API
                    .historicalPrices(id = id)
                    .asHistoricalPrices(id)
                // database.historicalDao.deleteHistoricalPricesById(id)
                database.historicalDao.insertAll(*historicalPrices.toTypedArray())
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

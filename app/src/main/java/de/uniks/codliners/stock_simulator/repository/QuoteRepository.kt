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
import de.uniks.codliners.stock_simulator.repository.QuoteRepository.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for accessing and updating [Quote]s.
 *
 * @property database The database used by this repository.
 * @property state The current [State] of this repository.
 *
 * @author Jan Müller
 * @author Jonas Thelemann
 * @author Juri Lozowoj
 */
class QuoteRepository(private val database: StockAppDatabase) {

    /**
     * Constructor that allows repository creation from a [Context].
     */
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

    /**
     * Returns a [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing the [Quote] with the requested id or null if no such [Quote] exists.
     *
     * @param id The id of the requested [Quote].
     * @return [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing the [Quote] with the requested id or null if no such [Quote] exists.
     */
    fun quoteWithId(id: String): LiveData<Quote> =
        database.quoteDao.getQuoteWithId(id)

    /**
     * Returns the [Quote] with the requested id or null if no such [Quote] exists.
     *
     * @param id The id of the requested [Quote].
     * @return [Quote] with the requested id or null if no such [Quote] exists.
     */
    fun quoteById(id: String): Quote? =
        database.quoteDao.getQuoteValueById(id)


    /**
     * Returns a [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing a [List] of [HistoricalPrice]s for the requested id or null if no such [HistoricalPrice]s exists.
     *
     * @param id The id of the requested [HistoricalPrice]s.
     * @return [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing a [List] of [HistoricalPrice]s for the requested id or null if no such [HistoricalPrice]s exists.
     */
    fun historicalPrices(id: String): LiveData<List<HistoricalPrice>> =
        database.historicalDao.getHistoricalPricesById(id)

    /**
     * Fetches the [Quote] and [HistoricalPrice]s for the requested symbol from the IEX API, then stores them in the [StockAppDatabase].
     *
     * @param symbol The IEX symbol of the share.
     * @return true if the operation was successful and false otherwise.
     */
    suspend fun fetchIEXQuote(symbol: String): Boolean = withContext(Dispatchers.IO) {
        try {
            _state.postValue(State.Refreshing)
            val quote = NetworkService.IEX_API.quote(symbol).asDomainQuote()
            database.quoteDao.insert(quote)
            val historicalPrices = NetworkService.IEX_API
                .historicalPrices(symbol = symbol, chartCloseOnly = true)
                .asDomainHistoricalPrices(symbol)
            database.historicalDao.deleteHistoricalPricesById(symbol)
            database.historicalDao.insertAll(*historicalPrices.toTypedArray())
            _state.postValue(State.Done)
            true
        } catch (exception: Exception) {
            _state.postValue(State.Error(exception.message ?: "Oops!"))
            false
        }
    }

    /**
     * Fetches the [Quote] and [HistoricalPrice]s for the requested symbol from the CoinGecko API, then stores them in the [StockAppDatabase].
     *
     * @param id The CoinGecko id of the cryptocurrency.
     * @return true if the operation was successful and false otherwise.
     */
    suspend fun fetchCoinGeckoQuote(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            _state.postValue(State.Refreshing)
            val quote = NetworkService.COINGECKO_API.quote(id).asDomainQuote()
            database.quoteDao.insert(quote)
            val historicalPrices = NetworkService.COINGECKO_API
                .historicalPrices(id = id)
                .asHistoricalPrices(id)
            database.historicalDao.deleteHistoricalPricesById(id)
            database.historicalDao.insertAll(*historicalPrices.toTypedArray())
            _state.postValue(State.Done)
            true
        } catch (exception: Exception) {
            _state.postValue(State.Error(exception.message ?: "Oops!"))
            false
        }
    }

    /**
     * Deletes all quotes from the database.
     *
     * @author Jonas Thelemann
     */
    suspend fun resetQuotes() {
        withContext(Dispatchers.IO) {
            database.quoteDao.deleteQuotes()
        }
    }
}

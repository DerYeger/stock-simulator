package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.News
import de.uniks.codliners.stock_simulator.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interface for fetching and resetting [News].
 *
 * @property database The database to store news in.
 * @property state The current [State] of the repository.
 * @property news Lazily initialized [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData) containing an ordered [List] of all locally stored [News].
 *
 * @author Jonas Thelemann
 */
class NewsRepository(private val database: StockAppDatabase) {

    /**
     * Constructor that allows repository creation from a [Context].
     */
    constructor(context: Context) : this(getDatabase(context))

    /**
     * The different states of news fetching.
     */
    sealed class State {
        /**
         * Nothing fetched.
         */
        object Empty : State()

        /**
         * Fetching.
         */
        object Refreshing : State()

        /**
         * Fetched data.
         */
        object Done : State()

        /**
         * An error occurred.
         *
         * @property message The error message.
         */
        class Error(val message: String) : State()
    }

    private val _state = MutableLiveData<State>().apply {
        postValue(State.Empty)
    }
    val state: LiveData<State> = _state

    private val _news = MutableLiveData<List<News>>()
    val news: LiveData<List<News>> = _news

    /**
     * Fetches all available news from the IEX API, then stores them in the [StockAppDatabase].
     */
    suspend fun fetchNews(symbol: String) {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Refreshing)
                val news = NetworkService.IEX_API.news(symbol)
                database.newsDao.insertAll(*news.toTypedArray())
                _news.postValue(news)

                if (news.isNotEmpty()) {
                    _state.postValue(State.Done)
                } else {
                    _state.postValue(State.Empty)
                }
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }

    /**
     * Deletes all news from the database.
     */
    suspend fun resetNews() {
        withContext(Dispatchers.IO) {
            database.newsDao.apply {
                deleteNews()
            }
        }
    }
}
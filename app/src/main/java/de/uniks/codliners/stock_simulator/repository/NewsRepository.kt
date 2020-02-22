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

class NewsRepository(private val database: StockAppDatabase) {

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

    private val _news = MutableLiveData<List<News>>()
    val news: LiveData<List<News>> = _news

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

    suspend fun resetNews() {
        withContext(Dispatchers.IO) {
            database.newsDao.apply {
                deleteNews()
            }
        }
    }
}
package de.uniks.codliners.stock_simulator.ui.news

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.repository.NewsRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData
import kotlinx.coroutines.launch

/**
 * The [NewsFragment]'s viewmodel.
 *
 * @property symbol The symbol to display news for.
 * @param application The application to create a [NewsRepository] for.
 * @property news The news to show.
 * @property refreshing Indicates whether news are being fetched.
 *
 * @author Jonas Thelemann
 */
class NewsViewModel(application: Application, private val symbol: String) : ViewModel() {

    private val newsRepository = NewsRepository(application)
    val news = newsRepository.news

    private val state = newsRepository.state
    val refreshing = state.map { it === NewsRepository.State.Refreshing }

    /**
     * The [NewsViewModel]'s factory class.
     *
     * @property application The application to create a [NewsRepository] for.
     * @property symbol The symbol to display news for.
     */
    class Factory(
        private val application: Application,
        private val symbol: String
    ) : ViewModelProvider.Factory {

        /**
         * The factory's construction method.
         *
         * @param T The class's type.
         * @param modelClass The class to create.
         * @return A [NewsViewModel] instance.
         */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewsViewModel(application, symbol) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    /**
     * Fetch news.
     */
    fun refresh() {
        viewModelScope.launch {
            newsRepository.fetchNews(symbol)
        }
    }
}

package de.uniks.codliners.stock_simulator.ui.news

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.repository.NewsRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * The [NewsFragment]'s viewmodel.
 *
 * @property symbol The symbol to display news for.
 * @param application The application to create a [NewsRepository] for.
 * @property news The news to show.
 * @property refreshing Indicates whether news are being fetched.
 *
 * @author Jonas Thelemann
 * @author Jan Müller
 */
class NewsViewModel(application: Application, private val symbol: String) : ViewModel() {

    private val newsRepository = NewsRepository(application)
    val news = newsRepository.news

    private val state = newsRepository.state
    val refreshing = state.map { it === NewsRepository.State.Refreshing }

    private val _errorAction: MutableLiveData<Exception> = sourcedLiveData(state) {
        when (val newState = state.value) {
            is NewsRepository.State.Error -> newState.exception
            else -> null
        }
    } as MutableLiveData
    val errorAction: LiveData<Exception> = _errorAction

    /**
     * Fetch news.
     */
    fun refresh() {
        viewModelScope.launch {
            newsRepository.fetchNews(symbol)
        }
    }

    fun onErrorActionCompleted() {
        _errorAction.postValue(null)
    }

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
}

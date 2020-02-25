package de.uniks.codliners.stock_simulator.ui.news

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.repository.NewsRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData
import kotlinx.coroutines.launch


class NewsViewModel(application: Application, private val symbol: String) : ViewModel() {

    private val newsRepository = NewsRepository(application)
    val news = newsRepository.news

    private val state = newsRepository.state
    val refreshing = state.map { it === NewsRepository.State.Refreshing }

    val errorAction = sourcedLiveData(state) {
        when (val state = state.value) {
            is NewsRepository.State.Error -> state.message
            else -> null
        }
    }

    class Factory(
        private val application: Application,
        private val symbol: String
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewsViewModel(application, symbol) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    fun refresh() {
        viewModelScope.launch {
            newsRepository.fetchNews(symbol)
        }
    }
}

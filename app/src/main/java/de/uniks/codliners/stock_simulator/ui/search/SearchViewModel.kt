package de.uniks.codliners.stock_simulator.ui.search

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.repository.SearchRepository
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : ViewModel() {

    private val searchRepository = SearchRepository(application)
    val searchResults = searchRepository.searchResults
    val searchState = searchRepository.state

    val searchQuery = searchRepository.searchQuery

    init {
       viewModelScope.launch {
           searchRepository.refreshSymbols()
       }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

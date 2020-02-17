package de.uniks.codliners.stock_simulator.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.repository.SearchRepository
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val searchRepository = SearchRepository()
    val searchResults = searchRepository.searchResults
    val searchState = searchRepository.state

    val searchQuery = MutableLiveData<String>()

    fun search() {
        viewModelScope.launch {
            searchRepository.search(searchQuery.value!!)
        }
    }
}

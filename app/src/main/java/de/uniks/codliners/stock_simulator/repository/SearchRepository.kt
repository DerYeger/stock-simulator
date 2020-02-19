package de.uniks.codliners.stock_simulator.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.domain.SearchResult
import de.uniks.codliners.stock_simulator.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class SearchRepository {

    sealed class State {
        object Empty: State()
        object Searching: State()
        object Done: State()
        class Error(val message: String): State()
    }

    private val _state = MutableLiveData<State>().apply {
        value = State.Empty
    }
    val state: LiveData<State> = _state

    private val _searchResults = MutableLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults

    suspend fun search(fragment: String) {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Searching)
                val response = NetworkService.IEX_API.search(fragment)
                _searchResults.postValue(response)
                if (response.isNotEmpty()) {
                    _state.postValue(State.Done)
                } else {
                    _state.postValue(State.Empty)
                }
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }
}

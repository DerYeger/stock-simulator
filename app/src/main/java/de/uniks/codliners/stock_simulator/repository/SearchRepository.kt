package de.uniks.codliners.stock_simulator.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _searchResults = MutableLiveData<List<Symbol>>()
    val searchResults: LiveData<Symbol> = _searchResults

    suspend fun search(fragment: String) {
        withContext(Dispatchers.IO) {
            try {
                _state.postValue(State.Searching)
                // TODO search
                _state.postValue(State.Done)
            } catch (exception: Exception) {
                _state.postValue(State.Error(exception.message ?: "Oops!"))
            }
        }
    }
}

package de.uniks.codliners.stock_simulator.ui.settings

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import de.uniks.codliners.stock_simulator.sourcing
import kotlinx.coroutines.launch


class SettingsViewModel(application: Application) : ViewModel() {

    private val symbolRepository = SymbolRepository(application)

    // Button click indicator for reset button.
    private val _clickResetStatus = MutableLiveData<Boolean>()
    val clickResetStatus: LiveData<Boolean> = _clickResetStatus

    // Button click indicator for fingerprint button.
    private val _toggleFingerprintStatus = MutableLiveData<Boolean>()
    val toggleFingerprintStatus: LiveData<Boolean> = _toggleFingerprintStatus

    private val state = symbolRepository.state
    private val _symbolRefreshInitiated = MutableLiveData<Boolean>(false)
    private val _symbolRepositoryStateAction = MediatorLiveData<SymbolRepository.State>()
    val symbolRepositoryStateAction: LiveData<SymbolRepository.State> = _symbolRepositoryStateAction

    init {
        _symbolRepositoryStateAction.sourcing(_symbolRefreshInitiated, state) {
            value = if (_symbolRefreshInitiated.value!!) state.value else null
        }
    }

    fun resetGame() {
        _clickResetStatus.value = true
    }

    fun onGameReset() {
        _clickResetStatus.value = false
    }

    fun toggleFingerprint() {
        _toggleFingerprintStatus.value = true
    }

    fun onFingerprintToggled() {
        _toggleFingerprintStatus.value = false
    }

    fun refreshSymbols() {
        viewModelScope.launch {
            _symbolRefreshInitiated.value = true
            symbolRepository.refreshSymbols()
            _symbolRefreshInitiated.value = false
        }
    }

    fun onSymbolActionCompleted() {
        _symbolRepositoryStateAction.value = null
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

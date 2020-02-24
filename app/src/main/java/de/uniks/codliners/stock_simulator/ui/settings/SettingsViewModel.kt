package de.uniks.codliners.stock_simulator.ui.settings

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import kotlinx.coroutines.launch


class SettingsViewModel(application: Application) : ViewModel() {

    private val symbolRepository = SymbolRepository(application)

    private val _stateAction = MediatorLiveData<SymbolRepository.State>()

    // Button click indicator for reset button.
    private val _clickResetStatus = MutableLiveData<Boolean>()
    val clickResetStatus: LiveData<Boolean> = _clickResetStatus

    // Button click indicator for fingerprint button.
    private val _toggleFingerprintStatus = MutableLiveData<Boolean>()
    val toggleFingerprintStatus: LiveData<Boolean> = _toggleFingerprintStatus

    private val _symbolRefreshInitiated = MutableLiveData<Boolean>(false)

    init {
        _stateAction.apply {
            addSource(symbolRepository.state) {
                value = if (_symbolRefreshInitiated.value!!) it else null
            }

            addSource(_symbolRefreshInitiated) {
                value = if (it) symbolRepository.state.value else null
            }
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
            symbolRepository.refreshSymbols()
        }
    }

    fun onSymbolActionCompleted() {
        _stateAction.value = null
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

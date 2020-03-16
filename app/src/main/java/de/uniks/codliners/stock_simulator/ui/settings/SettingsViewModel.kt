package de.uniks.codliners.stock_simulator.ui.settings

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData
import kotlinx.coroutines.launch

/**
 * The [ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel) of [SettingsFragment].
 *
 * @param application The context used for creating the [SymbolRepository].
 * @property clickResetStatus Button click indicator for reset button.
 * @property toggleFingerprintStatus Button click indicator for fingerprint button.
 * @property symbolRepositoryStateAction Gets triggered if the symbol repository is refreshed.
 * @property refreshing Indicates that the symbol repository is currently being refreshed.
 *
 * @author TODO
 * @author Jan Müller
 * @author Jonas Thelemann
 */
class SettingsViewModel(application: Application) : ViewModel() {

    private val symbolRepository = SymbolRepository(application)

    private val _clickResetStatus = MutableLiveData<Boolean>()
    val clickResetStatus: LiveData<Boolean> = _clickResetStatus

    private val _toggleFingerprintStatus = MutableLiveData<Boolean>()
    val toggleFingerprintStatus: LiveData<Boolean> = _toggleFingerprintStatus

    private val state = symbolRepository.state
    private val symbolRefreshInitiated = MutableLiveData(false)
    val symbolRepositoryStateAction = sourcedLiveData(symbolRefreshInitiated, state) {
        when (val state = state.value) {
            SymbolRepository.State.Refreshing -> if (symbolRefreshInitiated.value == true) state else null
            else -> state
        }
    }

    val refreshing = state.map { it === SymbolRepository.State.Refreshing }

    /**
     * Set the reset game click indicator.
     *
     * @author Jonas Thelemann
     */
    fun resetGame() {
        _clickResetStatus.value = true
    }

    /**
     * Reset the reset game click indicator.
     *
     * @author Jonas Thelemann
     */
    fun onGameReset() {
        _clickResetStatus.value = false
    }

    /**
     * Set the fingerprint button click indicator.
     *
     * @author Jonas Thelemann
     */
    fun toggleFingerprint() {
        _toggleFingerprintStatus.value = true
    }

    /**
     * Reset the fingerprint button click indicator.
     *
     * @author Jonas Thelemann
     */
    fun onFingerprintToggled() {
        _toggleFingerprintStatus.value = false
    }

    /**
     * Set the refresh symbols button click indicator.
     *
     * @author Jan Müller
     */
    fun refreshSymbols() {
        viewModelScope.launch {
            symbolRefreshInitiated.value = true
            symbolRepository.refreshSymbols()
            symbolRefreshInitiated.value = false
        }
    }

    /**
     * Reset the refresh symbols button click indicator.
     *
     * @author Jan Müller
     */
    fun onSymbolActionCompleted() {
        (symbolRepositoryStateAction as MutableLiveData<SymbolRepository.State>).value = null
    }

    /**
     * Factory for the SettingsViewModel.
     *
     * @property application The context used for creating the repositories.
     */
    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        /**
         * The factory's construction method.
         *
         * @param T The class's type.
         * @param modelClass The class to create.
         *
         * @throws [IllegalArgumentException] if [SettingsViewModel] is not assignable to [modelClass].
         *
         * @return A [SettingsViewModel] instance.
         */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

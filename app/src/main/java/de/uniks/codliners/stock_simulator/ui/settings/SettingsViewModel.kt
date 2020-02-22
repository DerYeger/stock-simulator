package de.uniks.codliners.stock_simulator.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SettingsViewModel : ViewModel() {

    // Button click indicator for reset button.
    private val _clickResetStatus = MutableLiveData<Boolean>()
    val clickResetStatus: LiveData<Boolean> = _clickResetStatus

    // Button click indicator for fingerprint button.
    private val _toggleFingerprintStatus = MutableLiveData<Boolean>()
    val toggleFingerprintStatus: LiveData<Boolean> = _toggleFingerprintStatus

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
}

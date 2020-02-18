package de.uniks.codliners.stock_simulator.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SettingsViewModel : ViewModel() {

    // Button click indicator for fingerprint button.
    var toggleFingerprintStatus = MutableLiveData<Boolean?>()

    fun resetGame() {
        // TODO
    }

    fun toggleFingerprint() {
        toggleFingerprintStatus.value = true
    }
}

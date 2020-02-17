package de.uniks.codliners.stock_simulator.ui.stockbrot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StockbrotViewModel : ViewModel() {
    private var _enabled = MutableLiveData<Boolean>(false)
    var enabled: LiveData<Boolean> = _enabled

    fun toggleBot() {
        println("pressed button toggle bot")
        when(enabled.value) {
            true -> disableBot()
            false -> enableBot()
        }
    }

    private fun enableBot() {
        println("enable bot")
        _enabled.value = true
    }

    private fun disableBot() {
        println("disable bot")
        _enabled.value = false
    }
}

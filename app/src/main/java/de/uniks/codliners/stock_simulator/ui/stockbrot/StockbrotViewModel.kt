package de.uniks.codliners.stock_simulator.ui.stockbrot

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class StockbrotViewModel : ViewModel() {
    private val _enabled = MutableLiveData(false)
    val enabled: LiveData<Boolean> = _enabled

    // TODO: add values to database
    val thresholdBuy = MutableLiveData<String>("")
    val thresholdSell = MutableLiveData<String>("")

    private val _enabledAction = MediatorLiveData<Boolean>()
    val enabledAction: LiveData<Boolean> = _enabledAction

    init {
        _enabledAction.addSource(enabled) { enabled ->
            if (enabled) {
                _enabledAction.value = true
            }
        }
    }

    fun onEnabledActionCompleted() {
        viewModelScope.launch {
            _enabledAction.value = null
        }
    }

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

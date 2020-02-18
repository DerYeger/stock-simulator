package de.uniks.codliners.stock_simulator.ui.stockbrot

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_BUY_DEFAULT_VALUE
import de.uniks.codliners.stock_simulator.background.Constants.Companion.THRESHOLD_SELL_DEFAULT_VALUE
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository
import kotlinx.coroutines.launch

class StockbrotViewModel(application: Application) : ViewModel() {

    private val stockbrotRepository = StockbrotRepository(application)

    val stockbrotQuotes = stockbrotRepository.quotes

    private val _enabled = MutableLiveData(false)
    val enabled: LiveData<Boolean> = _enabled

    // TODO: add values to database
    val thresholdBuy = MutableLiveData<String>(THRESHOLD_BUY_DEFAULT_VALUE.toString())
    val thresholdSell = MutableLiveData<String>(THRESHOLD_SELL_DEFAULT_VALUE.toString())

    private val _enabledAction = MediatorLiveData<Boolean>()
    val enabledAction: LiveData<Boolean> = _enabledAction

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StockbrotViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StockbrotViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

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

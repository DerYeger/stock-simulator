package de.uniks.codliners.stock_simulator.ui.account

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : ViewModel() {

    private val accountRepository = AccountRepository(application)

    val balance = accountRepository.latestBalance
    val balancesLimited = accountRepository.balancesLimited
    val depotQuotes = accountRepository.depot
    val depotValue = accountRepository.currentDepotValue
    val depotValuesLimited = accountRepository.depotValuesLimited

    val performance = sourcedLiveData(balance, depotValue) {
        calculatePerformance(balance.value?.value, depotValue.value?.value)
    }

    init {
        (performance as MutableLiveData).value = 0.0
        viewModelScope.launch {
            accountRepository.fetchCurrentDepotValue()
        }
    }

    private fun calculatePerformance(balance: Double?, depotValue: Double?): Double? {
        if (balance == null || depotValue == null) return 0.0
        return (((balance + depotValue ) / BuildConfig.NEW_ACCOUNT_BALANCE) - 1) * 100
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AccountViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
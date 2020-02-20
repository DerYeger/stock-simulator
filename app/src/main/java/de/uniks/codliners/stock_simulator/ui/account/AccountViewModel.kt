package de.uniks.codliners.stock_simulator.ui.account

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : ViewModel() {

    private val accountRepository = AccountRepository(application)

    val balance = accountRepository.latestBalance
    val balancesLimited = accountRepository.balancesLimited
    val depotQuotes = accountRepository.depot
    val depotValue = accountRepository.currentDepotValue
    val depotValuesLimited = accountRepository.depotValuesLimited
    var performance =  MutableLiveData<Double>(0.0)

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

    init {
        viewModelScope.launch {
            accountRepository.fetchCurrentDepotValue()
            calculatePerformance()
        }

    }

    private fun calculatePerformance(){
        if (balance.value == null || depotValue.value == null) return
        performance.value = ((balance.value!!.value + depotValue.value!!.value) / BuildConfig.NEW_ACCOUNT_BALANCE) - 1
    }

}
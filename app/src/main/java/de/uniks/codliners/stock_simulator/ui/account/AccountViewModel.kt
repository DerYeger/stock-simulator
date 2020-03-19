package de.uniks.codliners.stock_simulator.ui.account

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
        return (((balance + depotValue) / BuildConfig.NEW_ACCOUNT_BALANCE) - 1) * 100
    }

    /**
     * Factory for the AccountViewModel.
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
         * @throws [IllegalArgumentException] if [AccountViewModel] is not assignable to [modelClass].
         *
         * @return A [AccountViewModel] instance.
         */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AccountViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
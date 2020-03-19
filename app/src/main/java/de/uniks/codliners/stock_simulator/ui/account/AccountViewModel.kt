package de.uniks.codliners.stock_simulator.ui.account

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.domain.Achievement
import de.uniks.codliners.stock_simulator.domain.DepotQuote
import de.uniks.codliners.stock_simulator.domain.DepotQuotePurchase
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData

/**
 * ViewModel for the account ui.
 *
 * @param application The application to create a [AccountRepository].
 *
 * @property
 * @property balance The latest account balance.
 * @property balancesLimited The last {BALANCE_LIMIT = 50} account balance values.
 * @property depotQuotes All [DepotQuotePurchase]s conflated to [DepotQuote]s.
 * @property depotValue The latest depot value.
 * @property depotValuesLimited The latest {BALANCE_LIMIT = 50} depot values.
 * @property performance The calculated performance of the asset portfolio in relation to the initial capital.
 *
 * @author Jan Müller
 * @author Juri Lozowoj
 */
class AccountViewModel(application: Application) : ViewModel() {

    private val accountRepository = AccountRepository(application)

    val balance = accountRepository.latestBalance
    val balancesLimited = accountRepository.balancesLimited
    val depotQuotes = accountRepository.depot
    val depotValue = accountRepository.currentDepotValue
    val depotValuesLimited = accountRepository.depotValuesLimited
    
    val performance = sourcedLiveData(balance, depotValue) {
        calculatePerformance(balance.value?.value, depotValue.value?.value)
    }.apply {
        (this as MutableLiveData).value = 0.0
    }

    /**
     * Returns the calculated performance [Double] of the asset portfolio in relation to the initial capital.
     *
     * @param balance The current account balance.
     * @param depotValue The current depot value.
     * @return The calculated performance of the asset portfolio as a [Double].
     */
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
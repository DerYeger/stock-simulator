package de.uniks.codliners.stock_simulator.ui.account

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.domain.Achievement
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : ViewModel() {

    private val accountRepository = AccountRepository(application)
    private val achievementsRepository = AchievementsRepository(application)

    val balance = accountRepository.latestBalance
    val balancesLimited = accountRepository.balancesLimited
    val depotQuotes = accountRepository.depot
    val depotValue = accountRepository.currentDepotValue
    val depotValuesLimited = accountRepository.depotValuesLimited

    private val _balanceChanged = MediatorLiveData<Boolean>()
    val balanceChanged: LiveData<Boolean> = _balanceChanged

    private fun insertAchievement(achievement: Achievement) {
        viewModelScope.launch {
            achievementsRepository.insertAchievement(achievement)
        }
    }

    init {
        viewModelScope.launch {
            accountRepository.fetchCurrentDepotValue()
        }

        _balanceChanged.apply {
            addSource(balance) { balance: Balance? ->
                if (balance === null) return@addSource

                viewModelScope.launch {
                    if (balance.value <= BuildConfig.NEW_ACCOUNT_BALANCE - 5) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_5dollarlost_name)
                        val newAchievement = achievement!!.copy(
                            reached = true,
                            timestamp = System.currentTimeMillis()
                        )
                        insertAchievement(newAchievement)
                    }
                    if (balance.value >= BuildConfig.NEW_ACCOUNT_BALANCE + 10) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_10dollarwon_name)
                        val newAchievement = achievement!!.copy(
                            reached = true,
                            timestamp = System.currentTimeMillis()
                        )
                        insertAchievement(newAchievement)
                    }
                    if (balance.value >= BuildConfig.NEW_ACCOUNT_BALANCE + 10000) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_10000dollarwon_name)
                        val newAchievement = achievement!!.copy(
                            reached = true,
                            timestamp = System.currentTimeMillis()
                        )
                        insertAchievement(newAchievement)
                    }
                }
            }
        }
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
package de.uniks.codliners.stock_simulator.ui.account

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.domain.Achievement
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository
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

    private fun getAchievementByName(name: Int): Achievement? {
        return achievementsRepository.getAchievementsByName(name)
    }

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
            addSource(balance) { balance ->
                value = true

                if (balance.value <= 99995) {
                    val achievement =
                        getAchievementByName(R.string.achievement_99995reached_name)
                    val newAchievement = achievement!!.copy(
                        reached = true,
                        timestamp = System.currentTimeMillis()
                    )
                    insertAchievement(newAchievement)
                }
                if (balance.value >= 10010) {
                    val achievement =
                        getAchievementByName(R.string.achievement_10010reached_name)
                    val newAchievement = achievement!!.copy(
                        reached = true,
                        timestamp = System.currentTimeMillis()
                    )
                    insertAchievement(newAchievement)
                }
                if (balance.value >= 20000) {
                    val achievement =
                        getAchievementByName(R.string.achievement_20000reached_name)
                    val newAchievement = achievement!!.copy(
                        reached = true,
                        timestamp = System.currentTimeMillis()
                    )
                    insertAchievement(newAchievement)
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
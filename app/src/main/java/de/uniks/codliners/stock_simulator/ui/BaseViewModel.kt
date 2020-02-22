package de.uniks.codliners.stock_simulator.ui

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.domain.Achievement
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository
import kotlinx.coroutines.launch

class BaseViewModel(application: Application): ViewModel() {

    private val achievementsRepository = AchievementsRepository(application)
    private val accountRepository = AccountRepository(application)

    val latestAchievement = achievementsRepository.latestAchievement
    val balance = accountRepository.latestBalance

    private val _balanceChanged = MediatorLiveData<Boolean>()
    val balanceChanged: LiveData<Boolean> = _balanceChanged

    init {
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

    private fun insertAchievement(achievement: Achievement) {
        viewModelScope.launch {
            achievementsRepository.insertAchievement(achievement)
        }
    }

    fun markAchievementAsDisplayed(achievement: Achievement) {
        viewModelScope.launch {
            val newAchievement = achievement.copy(displayed = true)
            achievementsRepository.insertAchievement(newAchievement)
        }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BaseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BaseViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
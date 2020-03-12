package de.uniks.codliners.stock_simulator.ui

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.domain.Achievement
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.domain.DepotQuote
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository
import kotlinx.coroutines.launch

/**
 * BaseViewModel for the [BaseFragment]. Handles events that can unlock achievements.
 *
 * @constructor
 * Adds sources to [MediatorLiveData](https://developer.android.com/reference/androidx/lifecycle/MediatorLiveData) values.
 *
 * @param application
 *
 * @author Lucas Held
 */
class BaseViewModel(application: Application) : ViewModel() {

    private val achievementsRepository = AchievementsRepository(application)
    private val accountRepository = AccountRepository(application)

    val achievements = achievementsRepository.achievements
    private val balance = accountRepository.latestBalance
    private val depot = accountRepository.depot

    private val _balanceChanged = MediatorLiveData<Boolean>()
    val balanceChanged: LiveData<Boolean> = _balanceChanged

    private val _depotChanged = MediatorLiveData<Boolean>()
    val depotChanged: LiveData<Boolean> = _depotChanged

    init {
        _balanceChanged.apply {
            addSource(balance) { balance: Balance? ->
                if (balance === null) return@addSource

                viewModelScope.launch {
                    if (balance.value <= 200) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_200dollarLeft_name)
                        insertReachedAchievement(achievement)
                    }
                    if (balance.value <= BuildConfig.NEW_ACCOUNT_BALANCE - 1000) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_1000dollarLost_name)
                        insertReachedAchievement(achievement)
                    }
                    if (balance.value >= BuildConfig.NEW_ACCOUNT_BALANCE + 10) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_10dollarWon_name)
                        insertReachedAchievement(achievement)
                    }
                    if (balance.value >= BuildConfig.NEW_ACCOUNT_BALANCE + 100) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_100dollarWon_name)
                        insertReachedAchievement(achievement)
                    }
                    if (balance.value >= BuildConfig.NEW_ACCOUNT_BALANCE + 500) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_500dollarWon_name)
                        insertReachedAchievement(achievement)
                    }
                    if (balance.value >= BuildConfig.NEW_ACCOUNT_BALANCE + 10000) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_10000dollarWon_name)
                        insertReachedAchievement(achievement)
                    }
                    if (balance.value >= BuildConfig.NEW_ACCOUNT_BALANCE + 20000) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_20000dollarWon_name)
                        insertReachedAchievement(achievement)
                    }
                    if (balance.value >= BuildConfig.NEW_ACCOUNT_BALANCE + 40000) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_40000dollarWon_name)
                        insertReachedAchievement(achievement)
                    }
                }
            }
        }

        _depotChanged.apply {
            addSource(depot) { depot: List<DepotQuote>? ->
                if (depot === null) return@addSource

                var depotAmount = 0.0
                for (depotQuote in depot) {
                    depotAmount += depotQuote.amount
                }

                viewModelScope.launch {
                    if (depot.isNotEmpty()) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_1shareInDepot_name)
                        insertReachedAchievement(achievement)
                    }
                    if (depotAmount >= 5) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_5sharesInDepot_name)
                        insertReachedAchievement(achievement)
                    }
                    if (depotAmount >= 10) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_10sharesInDepot_name)
                        insertReachedAchievement(achievement)
                    }

                    if (depot.size >= 5) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_5differentSharesInDepot_name)
                        insertReachedAchievement(achievement)
                    }
                    if (depot.size >= 10) {
                        val achievement =
                            achievementsRepository.getAchievementsByName(R.string.achievement_10differentSharesInDepot_name)
                        insertReachedAchievement(achievement)
                    }
                }
            }
        }
    }

    /**
     * Marks an achievement as reached, sets the current timestamp and inserts it into the [AchievementsRepository].
     *
     * @param achievement Achievement that will be modified and inserted
     *
     * @author Lucas Held
     */
    private fun insertReachedAchievement(achievement: Achievement?) {
        val newAchievement = achievement!!.copy(
            reached = true,
            timestamp = System.currentTimeMillis()
        )
        insertAchievement(newAchievement)
    }

    /**
     * Inserts it into the [AchievementsRepository].
     *
     * @param achievement Achievement that will be inserted
     *
     * @author Lucas Held
     */
    private fun insertAchievement(achievement: Achievement) {
        viewModelScope.launch {
            achievementsRepository.insertAchievement(achievement)
        }
    }

    /**
     * Marks an achievement in the [AchievementsRepository] as displayed.
     *
     * @param achievement Achievement that will be modified.
     *
     * @author Lucas Held
     */
    fun markAchievementAsDisplayed(achievement: Achievement) {
        viewModelScope.launch {
            val newAchievement = achievement.copy(displayed = true)
            achievementsRepository.insertAchievement(newAchievement)
        }
    }

    /**
     * Factory for the BaseViewModel.
     *
     * @property application
     */
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
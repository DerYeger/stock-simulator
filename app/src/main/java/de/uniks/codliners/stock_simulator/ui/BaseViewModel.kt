package de.uniks.codliners.stock_simulator.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.domain.Achievement
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository
import kotlinx.coroutines.launch

class BaseViewModel(application: Application): ViewModel() {

    private val achievementsRepository = AchievementsRepository(application)
    val latestAchievement = achievementsRepository.latestAchievement

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
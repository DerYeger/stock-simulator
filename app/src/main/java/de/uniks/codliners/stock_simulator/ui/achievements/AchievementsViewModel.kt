package de.uniks.codliners.stock_simulator.ui.achievements

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository
import kotlinx.coroutines.launch


class AchievementsViewModel(application: Application) : ViewModel() {

    private val achievementsRepository = AchievementsRepository(application)
    val achievements = achievementsRepository.achievements

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AchievementsViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    fun refresh() {
        viewModelScope.launch {
            achievementsRepository.resetAchievements()
        }
    }
}

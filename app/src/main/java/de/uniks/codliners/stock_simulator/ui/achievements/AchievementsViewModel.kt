package de.uniks.codliners.stock_simulator.ui.achievements

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.uniks.codliners.stock_simulator.domain.Achievement
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository

/**
 * ViewModel for the achievement ui.
 *
 * @param application The application to create a [AchievementsRepository] for.
 *
 * @property achievements List of all [Achievement]s from the repository.
 *
 * @author Lucas Held
 */
class AchievementsViewModel(application: Application) : ViewModel() {

    private val achievementsRepository = AchievementsRepository(application)
    val achievements = achievementsRepository.achievements

    /**
     * Factory for the AchievementsViewModel.
     *
     * @property application The context used for creating the [AchievementsRepository].
     */
    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        /**
         * Attempts to create a [AchievementsViewModel].
         *
         * @param T The requested type of [ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel).
         * @param modelClass The requested class. [AchievementsViewModel] must be assignable to it.
         *
         * @throws [IllegalArgumentException] if [AchievementsViewModel] is not assignable to [modelClass].
         *
         * @return The created [AchievementsViewModel].
         */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AchievementsViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

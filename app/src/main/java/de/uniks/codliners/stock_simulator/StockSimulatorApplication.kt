package de.uniks.codliners.stock_simulator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import de.uniks.codliners.stock_simulator.repository.AchievementsRepository
import de.uniks.codliners.stock_simulator.repository.SymbolRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

private const val FIRST_RUN_KEY = "first_run"

/**
 * This app's application. Enables dark mode and logging and handles data initialization.
 *
 */
@Suppress("unused")
class StockSimulatorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        Timber.plant(Timber.DebugTree())

        ensureAccountPresence()

        onFirstRun {
            CoroutineScope(Dispatchers.Unconfined).launch {
                SymbolRepository(this@StockSimulatorApplication).refreshSymbols()
                AchievementsRepository(this@StockSimulatorApplication).initAchievements()
            }
        }
    }

    /**
     * Runs the passed block exactly once when the application is first launched.
     *
     * @param block The block that will be run during the app's first launch.
     *
     * @author Jan Müller
     */
    private inline fun onFirstRun(block: () -> Unit) {
        sharedPreferences()
            .getBoolean(FIRST_RUN_KEY, true)
            .takeIf { it }
            ?.let {
                block()
                sharedPreferences().edit(commit = true) {
                    putBoolean(FIRST_RUN_KEY, false)
                }
            }
    }
}

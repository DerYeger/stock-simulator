package de.uniks.codliners.stock_simulator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import timber.log.Timber

/**
 * This app's application. Enables dark mode and logging and handles data initialization.
 *
 * @author Jan Müller
 */
@Suppress("unused")
class StockSimulatorApplication : Application() {

    /**
     * Enables night mode, Timber's debug-logging.
     * Also handles game data initialization.
     *
     */
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        Timber.plant(Timber.DebugTree())

        ensureAccountPresence()
        ensureAchievementPresence()
        ensureSymbolsPresence()
    }
}

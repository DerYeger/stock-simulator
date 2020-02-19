package de.uniks.codliners.stock_simulator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import timber.log.Timber

private const val FIRST_RUN_KEY = "first_run"

class StockSimulatorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        Timber.plant(Timber.DebugTree())

        onFirstRun {
        }
    }

    private inline fun onFirstRun(block: () -> Unit) {
        sharedPreferences()
            .getBoolean(FIRST_RUN_KEY, true)
            .takeIf { it }
            .let {
                block()
                sharedPreferences().edit {
                    putBoolean(FIRST_RUN_KEY, false)
                }
            }
    }
}

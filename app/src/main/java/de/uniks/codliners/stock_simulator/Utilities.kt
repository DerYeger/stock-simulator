package de.uniks.codliners.stock_simulator

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

const val SHARED_PREFERENCES_KEY = "de.uniks.codliners.stock_simulator"

fun ContextWrapper.sharedPreferences() = getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)

fun Any.truly() = true

fun Context.resetAccount() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        AccountRepository(self).resetAccount()
    }
}

fun Context.ensureAccountPresence() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        val accountRepository = AccountRepository(self)
        if (accountRepository.latestBalance.value == null) {
            Timber.i("No balance detected. Resetting account")
            accountRepository.resetAccount()
        }
        val balances = accountRepository.balances
        Timber.i("Created new account with balances: ${balances.value}")
    }
}

fun noNulls(vararg args: Any?): Boolean {
    return listOfNotNull(*args).size == args.size
}

fun String?.toSafeLong(): Long? {
    return try {
        this?.toLong()
    } catch (_: Throwable) {
        null
    }
}

package de.uniks.codliners.stock_simulator

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.HistoryRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


const val SHARED_PREFERENCES_KEY = "de.uniks.codliners.stock_simulator"

fun ContextWrapper.sharedPreferences(): SharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)

fun Context.resetAccount() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        AccountRepository(self).resetAccount()
    }
}

fun Context.resetHistory() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        HistoryRepository(self).resetHistory()
    }
}

fun Context.resetQuotes() {
    val self = this
    CoroutineScope(Dispatchers.Main).launch {
        QuoteRepository(self).resetQuotes()
    }
}

fun Context.ensureAccountPresence(lifecycleOwner: LifecycleOwner) {
    val accountRepository = AccountRepository(this)
    accountRepository.latestBalance.observe(lifecycleOwner, Observer { t ->
        run {
            CoroutineScope(Dispatchers.Main).launch {
                if (t == null) {
                    Timber.i("No balance detected. Resetting account")
                    accountRepository.resetAccount()
                }
            }
        }
    })
    accountRepository.balances.observe(lifecycleOwner, Observer { t ->
        run {
            Timber.i("Created new account with balances: $t")
        }
    })
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

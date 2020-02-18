package de.uniks.codliners.stock_simulator.ui.account

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.uniks.codliners.stock_simulator.repository.AccountRepository

class AccountViewModel(application: Application) : ViewModel() {

    private val accountRepository = AccountRepository(application)

    val depotQuotes = accountRepository.depot

//    val depotShares = listOf(
//        Share("1", "SMA Solar", 45.5, 0.0, -5.3, 0.23),
//        Share("2", "Daimler", 21.0, 0.0, 28.9, 2.45)
//    )

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AccountViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
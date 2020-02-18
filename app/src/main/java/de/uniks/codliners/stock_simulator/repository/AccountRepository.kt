package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.database.sharesAsDomainModel
import de.uniks.codliners.stock_simulator.domain.Account
import de.uniks.codliners.stock_simulator.domain.Share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val account = database.accountDao.getAccount()

    val depotShares: LiveData<List<Share>> = database.depotDao.getShares().map {
        it.sharesAsDomainModel()
    }

    suspend fun resetAccount() {
        withContext(Dispatchers.IO) {
            database.depotDao.deleteAll()
            database.accountDao.insert(Account(balance = BuildConfig.NEW_ACCOUNT_BALANCE))
        }
    }
}
package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.database.DepotQuote
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.TransactionDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val account = database.accountDao.getAccount()

    val depot: LiveData<List<DepotQuote>> = database.accountDao.getDepot()

    fun depotQuoteWithSymbol(symbol: String): LiveData<DepotQuote> = database.accountDao.getDepotQuoteWithSymbol(symbol)

    suspend fun buy(quote: Quote, amount: Int) {
        val account = account.value
        account?.let {
            withContext(Dispatchers.IO) {
                val newBalance = account.balance - quote.latestPrice * amount
                val newAccount = account.copy(balance = newBalance)

                val depotQuote = database.accountDao.getDepotQuoteBySymbol(quote.symbol)
                val newDepotQuote = depotQuote.copy(amount = depotQuote.amount + amount)

                val transaction = TransactionDatabase(
                    shareName = quote.companyName,
                    number = amount,
                    transactionType = TransactionType.BUY,
                    date = System.currentTimeMillis()
                )

                database.accountDao.update(newAccount)
                database.accountDao.insertDepotQuote(newDepotQuote)
                database.transactionDao.insert(transaction)
            }
        }
    }

    suspend fun sell(quote: Quote, amount: Int) {
        val account = account.value
        account?.let {
            withContext(Dispatchers.IO) {
                val newBalance = account.balance + quote.latestPrice * amount
                val newAccount = account.copy(balance = newBalance)

                val depotQuote = database.accountDao.getDepotQuoteBySymbol(quote.symbol)
                val newDepotQuote = depotQuote.copy(amount = depotQuote.amount - amount)

                val transaction = TransactionDatabase(
                    shareName = quote.companyName,
                    number = amount,
                    transactionType = TransactionType.SELL,
                    date = System.currentTimeMillis()
                )

                database.accountDao.update(newAccount)
                if (newDepotQuote.amount > 0) {
                    database.accountDao.insertDepotQuote(newDepotQuote)
                } else {
                    database.accountDao.deleteDepotQuoteBySymbol(newDepotQuote.symbol)
                }

                database.transactionDao.insert(transaction)
            }
        }
    }

    suspend fun resetAccount() {
        withContext(Dispatchers.IO) {
            database.accountDao.deleteDepot()
            database.accountDao.insert(Account(balance = BuildConfig.NEW_ACCOUNT_BALANCE))
        }
    }
}
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

    val latestBalance by lazy {
        database.accountDao.getLatestBalance()
    }

    val balances by lazy {
        database.accountDao.getBalances()
    }

    val depot by lazy {
        database.accountDao.getDepotQuotes()
    }

    fun depotQuoteWithSymbol(symbol: String): LiveData<DepotQuote> =
        database.accountDao.getDepotQuoteWithSymbol(symbol)

    suspend fun buy(quote: Quote, amount: Int) {
        val lastBalance = latestBalance.value
        lastBalance?.let {
            withContext(Dispatchers.IO) {
                val newBalance = Balance(lastBalance.value - quote.latestPrice * amount)

                val depotQuote = database.accountDao.getDepotQuoteBySymbol(quote.symbol)
                    ?: DepotQuote(quote.symbol, 0)
                val newDepotQuote = depotQuote.copy(amount = depotQuote.amount + amount)

                val transaction = TransactionDatabase(
                    shareName = quote.companyName,
                    number = amount,
                    transactionType = TransactionType.BUY,
                    date = System.currentTimeMillis()
                )

                database.accountDao.apply {
                    insertBalance(newBalance)
                    insertDepotQuote(newDepotQuote)
                }
                database.transactionDao.insert(transaction)
            }
        }
    }

    suspend fun sell(quote: Quote, amount: Int) {
        val lastBalance = latestBalance.value
        lastBalance?.let {
            withContext(Dispatchers.IO) {
                val newBalance = Balance(lastBalance.value + quote.latestPrice * amount)

                val depotQuote = database.accountDao.getDepotQuoteBySymbol(quote.symbol)!!
                val newDepotQuote = depotQuote.copy(amount = depotQuote.amount - amount)

                val transaction = TransactionDatabase(
                    shareName = quote.companyName,
                    number = amount,
                    transactionType = TransactionType.SELL,
                    date = System.currentTimeMillis()
                )

                database.accountDao.apply {
                    insertBalance(newBalance)
                    if (newDepotQuote.amount > 0) {
                        insertDepotQuote(newDepotQuote)
                    } else {
                        deleteDepotQuoteBySymbol(newDepotQuote.symbol)
                    }
                }
                database.transactionDao.insert(transaction)
            }
        }
    }

    suspend fun resetAccount() {
        withContext(Dispatchers.IO) {
            database.accountDao.apply {
                deleteDepot()
                deleteBalances()
                val starterBalance = Balance(value = BuildConfig.NEW_ACCOUNT_BALANCE)
                insertBalance(starterBalance)
            }
        }
    }
}

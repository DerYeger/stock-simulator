package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.database.DepotQuote
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.DatabaseTransaction
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val BALANCE_LIMIT: Int = 50

class AccountRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val latestBalance by lazy {
        database.accountDao.getLatestBalance()
    }

    val balances by lazy {
        database.accountDao.getBalances()
    }

    val balancesLimited by lazy {
        database.accountDao.getBalancesLimited(BALANCE_LIMIT)
    }

    val depot by lazy {
        database.accountDao.getDepotQuotes()
    }

    fun depotQuoteWithSymbol(symbol: String): LiveData<DepotQuote> =
        database.accountDao.getDepotQuoteWitId(symbol)

    suspend fun buy(quote: Quote, amount: Double) {
        val lastBalance = latestBalance.value
        lastBalance?.let {
            withContext(Dispatchers.IO) {
                val cashflow = -(quote.latestPrice * amount) - BuildConfig.TRANSACTION_COSTS
                val newBalance = Balance(lastBalance.value + cashflow)

                val depotQuote = database.accountDao.getDepotQuoteById(quote.symbol)
                    ?: DepotQuote(id = quote.id, type = quote.type, amount = 0.0)
                val newDepotQuote = depotQuote.copy(amount = depotQuote.amount + amount)

                val transaction = DatabaseTransaction(
                    id = quote.id,
                    type = quote.type,
                    amount = amount,
                    price = quote.latestPrice,
                    transactionCosts = BuildConfig.TRANSACTION_COSTS,
                    cashflow = cashflow,
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

    suspend fun sell(quote: Quote, amount: Double) {
        val lastBalance = latestBalance.value
        lastBalance?.let {
            withContext(Dispatchers.IO) {
                val cashflow = quote.latestPrice * amount - BuildConfig.TRANSACTION_COSTS
                val newBalance = Balance(lastBalance.value + cashflow)

                val depotQuote = database.accountDao.getDepotQuoteById(quote.symbol)!!
                val newDepotQuote = depotQuote.copy(amount = depotQuote.amount - amount)

                val transaction = DatabaseTransaction(
                    id = quote.id,
                    type = quote.type,
                    amount = amount,
                    price = quote.latestPrice,
                    transactionCosts = BuildConfig.TRANSACTION_COSTS,
                    cashflow = cashflow,
                    transactionType = TransactionType.SELL,
                    date = System.currentTimeMillis()
                )

                database.accountDao.apply {
                    insertBalance(newBalance)
                    if (newDepotQuote.amount > 0) {
                        insertDepotQuote(newDepotQuote)
                    } else {
                        deleteDepotQuoteById(newDepotQuote.id)
                    }
                }
                database.transactionDao.insert(transaction)
            }
        }
    }

    suspend fun hasBalance() = withContext(Dispatchers.IO) { database.accountDao.getBalanceCount() > 0 }

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

package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.database.*
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.TransactionType
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
    // the last 50 account balance values
    val balancesLimited by lazy {
        database.accountDao.getBalancesLimited(BALANCE_LIMIT)
    }

    val depot by lazy {
        database.accountDao.getDepotQuotes()
    }
    // the last 50 account depot values
    val depotValuesLimited by lazy {
        database.accountDao.getDepotValuesLimited(BALANCE_LIMIT)
    }

    val currentDepotValue by lazy {
        database.accountDao.getLatestDepotValues()
    }

    fun depotQuoteWithSymbol(symbol: String): LiveData<DepotQuote> =
        database.accountDao.getDepotQuoteWitId(symbol)

    fun depotQuoteBySymbol(symbol: String): DepotQuote? =
        database.accountDao.getDepotQuoteById(symbol)

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

                // check for reached achievements
                if (newBalance.value <= 99995) {
                    val achievement =
                        database.achievementDao.getAchievementByName(R.string.achievement_99995reached_name)
                    val newAchievement = achievement!!.copy(timestamp = System.currentTimeMillis())
                    database.achievementDao.insert(newAchievement)
                }
                if (newBalance.value >= 10010) {
                    val achievement =
                        database.achievementDao.getAchievementByName(R.string.achievement_10010reached_name)
                    val newAchievement = achievement!!.copy(timestamp = System.currentTimeMillis())
                    database.achievementDao.insert(newAchievement)
                }
                if (newBalance.value >= 20000) {
                    val achievement =
                        database.achievementDao.getAchievementByName(R.string.achievement_20000reached_name)
                    val newAchievement = achievement!!.copy(timestamp = System.currentTimeMillis())
                    database.achievementDao.insert(newAchievement)
                }
            }
        }
    }

    suspend fun fetchCurrentDepotValue() {
        withContext(Dispatchers.IO) {
            val depotQuotes = database.accountDao.getDepotQuotesValues()
            val newValue = depotQuotes.sumByDouble { depotQuote ->
                val quotePrice = database.quoteDao.getQuoteValueBySymbol(depotQuote.id).latestPrice
                val depotQuoteAmount = depotQuote.amount
                quotePrice * depotQuoteAmount
            }
            val newDepotValue = DepotValue(newValue)
            database.accountDao.insertDepotValue(newDepotValue)

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
                deleteAccount()
                setStartBalance()
                setInitialDepotValue()
            }
        }
    }

    private fun AccountDao.setStartBalance() {
        val starterBalance = Balance(value = BuildConfig.NEW_ACCOUNT_BALANCE)
        insertBalance(starterBalance)
    }

    private fun AccountDao.setInitialDepotValue() {
        val starterDepotValue = DepotValue(value = BuildConfig.NEW_DEPOT_VALUE)
        insertDepotValue(starterDepotValue)
    }

    private fun AccountDao.deleteAccount() {
        deleteDepot()
        deleteBalances()
        deleteDepotValues()
    }
}

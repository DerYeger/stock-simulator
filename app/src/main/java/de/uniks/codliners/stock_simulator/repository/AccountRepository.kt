package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.database.AccountDao
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
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
        database.accountDao.getDepotQuoteWithId(symbol)

    fun depotQuoteBySymbol(symbol: String): DepotQuotePurchase? =
        database.accountDao.getDepotQuoteById(symbol)

    suspend fun getLatestBalance() = withContext(Dispatchers.IO) { database.accountDao.getLatestBalanceValue() }

    fun calculateBuyCashflow(quote: Quote?, amount: Double?): Double {
        if (quote == null || amount == null) return 0.0
        return -(quote.latestPrice * amount) - BuildConfig.TRANSACTION_COSTS
    }

    fun calculateSellCashflow(quote: Quote?, amount: Double?): Double {
        if (quote == null || amount == null) return 0.0
        return quote.latestPrice * amount - BuildConfig.TRANSACTION_COSTS
    }

    suspend fun buy(quote: Quote, amount: Double) {
        if (amount <= 0.0) return
        withContext(Dispatchers.IO) {
            val oldBalance = database.accountDao.getLatestBalanceValue()
            val cashflow = calculateBuyCashflow(quote, amount)
            if (-cashflow > oldBalance.value) {
                return@withContext
            }
            val newBalance = Balance(oldBalance.value + cashflow)

            val newDepotQuote = DepotQuotePurchase(
                id = quote.id,
                symbol = quote.symbol,
                type = quote.type,
                buyingPrice = quote.latestPrice,
                amount = amount
            )

            val transaction = Transaction(
                id = quote.id,
                symbol = quote.symbol,
                type = quote.type,
                amount = amount,
                price = quote.latestPrice,
                transactionCosts = BuildConfig.TRANSACTION_COSTS,
                cashflow = cashflow,
                transactionType = TransactionType.BUY,
                date = System.currentTimeMillis(),
                result = null
            )

            database.accountDao.apply {
                insertBalance(newBalance)
                insertDepotQuote(newDepotQuote)
            }
            database.transactionDao.insert(transaction)
        }
    }

    suspend fun sell(quote: Quote, amount: Double) {
        if (amount <= 0.0) return
        withContext(Dispatchers.IO) {
            val oldBalance = database.accountDao.getLatestBalanceValue()
            if (BuildConfig.TRANSACTION_COSTS > oldBalance.value + quote.latestPrice * amount ) {
                return@withContext
            }
            val cashflow = calculateSellCashflow(quote, amount)
            val newBalance = Balance(oldBalance.value + cashflow)

            val transactionResult = calculateResultAndUpdateQuotePurchases(amount, cashflow, quote)

            val transaction = Transaction(
                id = quote.id,
                symbol = quote.symbol,
                type = quote.type,
                amount = amount,
                price = quote.latestPrice,
                transactionCosts = BuildConfig.TRANSACTION_COSTS,
                cashflow = cashflow,
                transactionType = TransactionType.SELL,
                date = System.currentTimeMillis(),
                result = transactionResult
            )

            database.accountDao.apply {
                insertBalance(newBalance)
                // deleteDepotQuotes(*depotQuotesToSell.toTypedArray())
            }
            database.transactionDao.insert(transaction)
        }
    }

    suspend fun fetchCurrentDepotValue() {
        withContext(Dispatchers.IO) {
            val depotQuotes = database.accountDao.getDepotQuotePurchasesValuesOrderedByPrice()
            val newValue = depotQuotes.sumByDouble { depotQuote ->
                val quotePrice = database.quoteDao.getQuoteValueById(depotQuote.id).latestPrice
                val depotQuoteAmount = depotQuote.amount
                quotePrice * depotQuoteAmount
            }
            val newDepotValue = DepotValue(newValue)
            database.accountDao.insertDepotValue(newDepotValue)

        }
    }

    private fun calculateResultAndUpdateQuotePurchases(
        amount: Double,
        cashflow: Double,
        quote: Quote
    ): Double {
        val allQuotesOPurchases = database.accountDao.getDepotQuotePurchasesByIdOrderedByPrice(quote.id)

        val quotesToSell = mutableListOf<DepotQuotePurchase>()
        var amountCount = 0.0
        var count = 0
        var transactionResult = cashflow

        while (amountCount < amount) {
            val amountOfQuotesMissing = amount - amountCount // x1 = 33
            val quotePurchases = allQuotesOPurchases[count] // quotePurchases.amount = 11
            if (quotePurchases.amount <= amountOfQuotesMissing) {
                quotesToSell.add(quotePurchases) // 11 quotes
                amountCount += quotePurchases.amount // amountCount = 11
                database.accountDao.deleteDepotQuotes(quotePurchases)
                transactionResult -= (quotePurchases.buyingPrice * quotePurchases.amount) + BuildConfig.TRANSACTION_COSTS
            } else {
                val newAmount = quotePurchases.amount - amountOfQuotesMissing
                val newDepotQuote =
                    quotePurchases.copy(amount = newAmount)
                quotesToSell.add(newDepotQuote)
                amountCount += quotePurchases.amount
                database.accountDao.insertDepotQuote(newDepotQuote)
                transactionResult -= (quotePurchases.buyingPrice * amountOfQuotesMissing) + BuildConfig.TRANSACTION_COSTS
            }
            ++count
        }
        return transactionResult
    }

    suspend fun hasBalance() =
        withContext(Dispatchers.IO) { database.accountDao.getBalanceCount() > 0 }

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
        val secondStarterBalance = starterBalance.copy(timestamp = starterBalance.timestamp - 60 * 1000)
        insertBalance(starterBalance)
        insertBalance(secondStarterBalance)
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

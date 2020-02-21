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

    fun depotQuoteWithSymbol(symbol: String): LiveData<DepotQuotePurchase> =
        database.accountDao.getDepotQuoteWitId(symbol)

    fun depotQuoteBySymbol(symbol: String): DepotQuotePurchase? =
        database.accountDao.getDepotQuoteById(symbol)

    suspend fun buy(quote: Quote, amount: Double) {
        val lastBalance = latestBalance.value
        lastBalance?.let {
            withContext(Dispatchers.IO) {
                val cashflow = -(quote.latestPrice * amount) - BuildConfig.TRANSACTION_COSTS
                val newBalance = Balance(lastBalance.value + cashflow)


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

    suspend fun sell(quote: Quote, amount: Double) {
        val lastBalance = latestBalance.value
        lastBalance?.let {
            withContext(Dispatchers.IO) {
                val cashflow = quote.latestPrice * amount - BuildConfig.TRANSACTION_COSTS
                val newBalance = Balance(lastBalance.value + cashflow)

                val transactionResult = calculateResultAndUpdateQuotePurchases(amount, cashflow)

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
    }

    private fun calculateResultAndUpdateQuotePurchases(
        amount: Double,
        cashflow: Double
    ): Double {
        val allQuotesOPurchases = database.accountDao.getDepotQuotePurchasesValuesOrderedByPrice()

        val quotesToSell = mutableListOf<DepotQuotePurchase>()
        var transactionResult = 0.0
        var amountCount = 0.0

        while (amountCount < amount) {
            var count = 0
            val amountOfQuotesMissing = amount - quotesToSell.size
            val quotePurchases = allQuotesOPurchases[count]
            if (quotePurchases.amount <= amountOfQuotesMissing) {
                quotesToSell.add(quotePurchases)
                amountCount += quotePurchases.amount
                database.accountDao.deleteDepotQuotes(quotePurchases)
                transactionResult += cashflow - (quotePurchases.buyingPrice * quotePurchases.amount)
            } else {
                val newAmount = quotePurchases.amount - amountOfQuotesMissing
                val newDepotQuote =
                    quotePurchases.copy(amount = newAmount)
                quotesToSell.add(newDepotQuote)
                amountCount += quotePurchases.amount
                database.accountDao.insertDepotQuote(newDepotQuote)
                transactionResult += cashflow - (quotePurchases.buyingPrice * amountOfQuotesMissing)
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

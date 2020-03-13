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

/**
 * Limits the amount of balance data points returned by corresponding database requests.
 */
private const val BALANCE_LIMIT: Int = 50

/**
 * Repository for accessing and updating account information.
 *
 * @property database The database used by this repository.
 * @property latestBalance The latest [Balance] of the [StockAppDatabase].
 *
 * @author Jan Müller
 * @author Juri Lozowoj
 * @author Lucas Held
 * @author Jonas Thelemann
 */
class AccountRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    /**
     * The last account balance value.
     */
    val latestBalance by lazy {
        database.accountDao.getLatestBalance()
    }

    /**
     * The last {BALANCE_LIMIT} account balance values.
     */
    val balancesLimited by lazy {
        database.accountDao.getBalancesLimited(BALANCE_LIMIT)
    }

    /**
     * All [DepotQuotePurchase]s conflated to [DepotQuote]s whereby their amount [Double] is added up and the mean buyingPrice [Double] is bid from the buyingPrices [Double].
     */
    val depot by lazy {
        database.accountDao.getDepotQuotes()
    }

    /**
     * The last {BALANCE_LIMIT} depot values.
     */
    val depotValuesLimited by lazy {
        database.accountDao.getDepotValuesLimited(BALANCE_LIMIT)
    }

    /**
     * The last depot value.
     */
    val currentDepotValue by lazy {
        database.accountDao.getLatestDepotValues()
    }

    /**
     * Returns the [DepotQuote] with the matching id, wrapped in [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).
     * Conflates all [DepotQuotePurchase]s with the matching id and returns them as one [DepotQuote],
     * whereby the amount [Double] is added up and the mean buyingPrice [Double] is bid from the buyingPrices [Double].
     *
     * @param symbol The [DepotQuotePurchase] symbol used for the database query.
     * @return [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData) containing the [DepotQuote].
     */
    fun depotQuoteWithSymbol(symbol: String): LiveData<DepotQuote> =
        database.accountDao.getDepotQuoteWithId(symbol)

    /**
     * Returns the [DepotQuotePurchase] with the matching id.
     *
     * @param symbol The [DepotQuotePurchase] symbol used for the database query.
     * @return The [DepotQuotePurchase] with this symbol or null if no such quote exists.
     */
    fun depotQuoteBySymbol(symbol: String): DepotQuotePurchase? =
        database.accountDao.getDepotQuoteById(symbol)

    /**
     * Returns the latest [Balance] of the [StockAppDatabase].
     *
     * @return The latest [Balance] of the [StockAppDatabase].
     */
    suspend fun getLatestBalance() =
        withContext(Dispatchers.IO) { database.accountDao.getLatestBalanceValue() }

    /**
     * Calculates the cashflow of a buy transaction.
     *
     * @param quote The [Quote] to buy.
     * @param amount The buy amount of the [Quote].
     * @return The calculated cashflow.
     *
     * @author Lucas Held
     */
    fun calculateBuyCashflow(quote: Quote?, amount: Double?): Double {
        if (quote == null || amount == null) return 0.0
        return -(quote.latestPrice * amount) - BuildConfig.TRANSACTION_COSTS
    }

    /**
     * Calculates the cashflow of a sell transaction.
     *
     * @param quote The [Quote] to sell.
     * @param amount The sell amount of the [Quote].
     * @return The calculated cashflow.
     *
     * @author Lucas Held
     */
    fun calculateSellCashflow(quote: Quote?, amount: Double?): Double {
        if (quote == null || amount == null) return 0.0
        return quote.latestPrice * amount - BuildConfig.TRANSACTION_COSTS
    }

    /**
     * Buys an asset if possible and stores the related data in the [StockAppDatabase].
     *
     * @param quote Quote information of the asset.
     * @param amount The amount to buy.
     */
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

    /**
     * Sells an asset if possible and stores the related data in the [StockAppDatabase].
     *
     * @param quote Quote information of the asset.
     * @param amount The amount to sell.
     */
    suspend fun sell(quote: Quote, amount: Double) {
        if (amount <= 0.0) return
        withContext(Dispatchers.IO) {
            val oldBalance = database.accountDao.getLatestBalanceValue()
            if (BuildConfig.TRANSACTION_COSTS > oldBalance.value + quote.latestPrice * amount) {
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

            database.accountDao.insertBalance(newBalance)
            database.transactionDao.insert(transaction)
        }
    }

    suspend fun fetchCurrentDepotValue() {
        withContext(Dispatchers.IO) {
            val depotQuotes = database.accountDao.getDepotQuotePurchasesValuesOrderedByPrice()
            val newValue = depotQuotes.sumByDouble { depotQuote ->
                val quotePrice = database.quoteDao.getQuoteValueById(depotQuote.id)!!.latestPrice
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
        val allQuotesOPurchases =
            database.accountDao.getDepotQuotePurchasesByIdOrderedByPrice(quote.id)

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

    /**
     * Checks if the [StockAppDatabase] has any [Balance].
     *
     * @return true if the [StockAppDatabase] has any [Balance] and false otherwise.
     */
    suspend fun hasBalance() =
        withContext(Dispatchers.IO) { database.accountDao.getBalanceCount() > 0 }

    /**
     * Resets account information by deleting existing data from the [StockAppDatabase] and inserting default [Balance]s and a [DepotValue].
     *
     */
    suspend fun resetAccount() {
        withContext(Dispatchers.IO) {
            database.accountDao.apply {
                deleteAccount()
                setStartBalance()
                setInitialDepotValue()
            }
        }
    }

    /**
     * Inserts two [Balance]s with a value of [BuildConfig.NEW_ACCOUNT_BALANCE] and a time offset of 1 minute to enable graph plotting.
     *
     */
    private fun AccountDao.setStartBalance() {
        val starterBalance = Balance(value = BuildConfig.NEW_ACCOUNT_BALANCE)
        val secondStarterBalance =
            starterBalance.copy(timestamp = starterBalance.timestamp - 60 * 1000)
        insertBalance(starterBalance)
        insertBalance(secondStarterBalance)
    }

    private fun AccountDao.setInitialDepotValue() {
        val starterDepotValue = DepotValue(value = BuildConfig.NEW_DEPOT_VALUE)
        insertDepotValue(starterDepotValue)
    }

    /**
     * Deletes all account-related information from the [StockAppDatabase].
     *
     */
    private fun AccountDao.deleteAccount() {
        deleteDepot()
        deleteBalances()
        deleteDepotValues()
    }
}

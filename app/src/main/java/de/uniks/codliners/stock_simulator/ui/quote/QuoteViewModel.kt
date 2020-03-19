package de.uniks.codliners.stock_simulator.ui.quote

import android.app.Application
import android.text.InputType
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.background.StockbrotWorkRequest
import de.uniks.codliners.stock_simulator.domain.*
import de.uniks.codliners.stock_simulator.noNulls
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository
import de.uniks.codliners.stock_simulator.sourcedLiveData
import de.uniks.codliners.stock_simulator.toSafeDouble
import kotlinx.coroutines.launch
import java.util.*

/**
 * [ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel) for displaying [Quote] information.
 *
 * @param application The context used for creating repositories.
 *
 * @property id The id of the [Quote].
 * @property type The [Symbol.Type] of the [Quote].
 * @property stockbrotQuote The [StockbrotQuote] of the [Quote].
 * @property autoBuyAmount The default buy amount used for [StockbrotQuote].
 * @property stockbrotQuoteAction Gets triggered if the [stockbrotQuote] has been changed.
 * @property canAddRemoveQuoteToStockbrot Indicates if the [stockbrotQuote] can be added or removed.
 * @property buyAction Gets triggered if a buy transaction has been requested.
 * @property sellAction Gets triggered if a sell transaction has been requested.
 * @property sellAllAction Gets triggered if a sell of all quotes has been requested.
 * @property amount The amount in the depot of the current quote.
 * @property cashflow The cashflow of the current transaction request.
 * @property thresholdBuy The buy threshold used for [StockbrotQuote].
 * @property thresholdSell The sell threshold used for [StockbrotQuote].
 * @property autoBuyAmount The buy amount used for [StockbrotQuote].
 * @property stockbrotQuoteAction Gets triggered if the [stockbrotQuote] has been changed.
 * @property canSellAll Indicates if all [DepotQuote]s can be sold.
 * @property quote The [Quote].
 * @property depotQuote The [DepotQuote].
 * @property historicalPrices The historical prices for the [Quote].
 * @property isCrypto Indicates if the type is a crypto currency or not.
 * @property hasChange Indicates if the [Quote] has a change.
 * @property inputType The input type [Int] or [Double] depending on the [Symbol.Type].
 * @property refreshing Indicates that the quote repository is currently being refreshed.
 * @property errorAction Gets triggered if an error has been occurred.
 * @property buyAmount The buy amount used for buy transactions.
 * @property canBuy Indicates if the [Quote] can be bought.
 * @property sellAmount The sell amount used for sell transactions.
 * @property canSell Indicates if the [DepotQuote] can be sold.
 * @property transactionCosts The transaction costs used for each transaction.
 *
 * @constructor Refreshes [QuoteRepository] data and init the timer.
 *
 * @author Jan Müller
 * @author Lucas Held
 * @author Jonas Thelemann
 */
class QuoteViewModel(
    application: Application,
    private val id: String,
    private val type: Symbol.Type
) : AndroidViewModel(application) {

    private lateinit var timer: Timer

    private val quoteRepository = QuoteRepository(application)
    private val accountRepository = AccountRepository(application)
    private val stockbrotRepository = StockbrotRepository(application)

    private val stockbrotWorkRequest = StockbrotWorkRequest(application)

    private val latestBalance = accountRepository.latestBalance

    val quote = quoteRepository.quoteWithId(id)
    val depotQuote = accountRepository.depotQuoteWithSymbol(id)
    val stockbrotQuote = stockbrotRepository.stockbrotQuoteWithId(id)
    val historicalPrices = quoteRepository.historicalPrices(id)

    val isCrypto = type === Symbol.Type.CRYPTO

    val inputType =
        if (isCrypto) (InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_CLASS_NUMBER) else InputType.TYPE_CLASS_NUMBER

    private val state = quoteRepository.state
    val refreshing = state.map { it === QuoteRepository.State.Refreshing }

    val errorAction = sourcedLiveData(state) {
        when (val state = state.value) {
            is QuoteRepository.State.Error -> state.exception
            else -> null
        }
    }

    val buyAmount = MutableLiveData<String>().apply {
        value = if (isCrypto) "0.0" else "0"
    }

    val canBuy = sourcedLiveData(buyAmount, quote, latestBalance, state) {
        canBuy(
            amount = buyAmount.value.toSafeDouble(),
            price = quote.value?.latestPrice,
            balance = latestBalance.value,
            state = state.value
        )
    }

    val sellAmount = MutableLiveData<String>().apply {
        value = if (isCrypto) "0.0" else "0"
    }

    val canSell = sourcedLiveData(sellAmount, quote, depotQuote, latestBalance, state) {
        canSell(
            amount = sellAmount.value.toSafeDouble(),
            depotQuote = depotQuote.value,
            balance = latestBalance.value,
            state = state.value,
            quote = quote.value
        )
    }

    val canSellAll = sourcedLiveData(quote, depotQuote, latestBalance, state) {
        canSellAll(
            depotQuote = depotQuote.value,
            balance = latestBalance.value,
            state = state.value,
            quote = quote.value
        )
    }

    private val _buyAction = MutableLiveData<Boolean>()
    val buyAction: LiveData<Boolean> = _buyAction

    private val _sellAction = MutableLiveData<Boolean>()
    val sellAction: LiveData<Boolean> = _sellAction

    private val _sellAllAction = MutableLiveData<Boolean>()
    val sellAllAction: LiveData<Boolean> = _sellAllAction

    private val _amount = MediatorLiveData<String>()
    val amount: LiveData<String> = _amount

    private val _cashflow = MediatorLiveData<Double>()
    val cashflow: LiveData<Double> = _cashflow

    val transactionCosts: Double = BuildConfig.TRANSACTION_COSTS

    val thresholdBuy = MutableLiveData("0.0")
    val thresholdSell = MutableLiveData("0.0")
    val autoBuyAmount = MutableLiveData<String>().apply {
        value = if (isCrypto) "0.0" else "0"
    }
    val stockbrotQuoteAction = sourcedLiveData(stockbrotQuote) {
        stockbrotQuote.value
    }

    val canAddRemoveQuoteToStockbrot = sourcedLiveData(autoBuyAmount, thresholdBuy, thresholdSell) {
        canAddRemoveQuoteToStockbrot(
            stockbrotQuote.value,
            autoBuyAmount.value.toSafeDouble(),
            thresholdBuy.value.toSafeDouble(),
            thresholdSell.value.toSafeDouble()
        )
    }

    /**
     * Button click indicator for reset button.
     *
     * @author Jonas Thelemann
     */
    val clickNewsStatus = MutableLiveData<Boolean?>()

    init {
        refresh()
        initTimer()
    }

    /**
     * Cancels the timer if the ViewModel is no longer used and will be destroyed.
     *
     * @author Jan Müller
     */
    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    /**
     * Refreshes [QuoteRepository] data for all [Symbol.Type]s.
     *
     * @author Jan Müller
     */
    fun refresh() {
        viewModelScope.launch {
            when (type) {
                Symbol.Type.SHARE -> quoteRepository.fetchIEXQuote(id)
                Symbol.Type.CRYPTO -> quoteRepository.fetchCoinGeckoQuote(id)
            }
        }
    }

    /**
     * Resets the threshold buy indicator.
     *
     * @author Lucas Held
     */
    fun onThresholdBuyActionCompleted() {
        viewModelScope.launch {
            (stockbrotQuoteAction as MutableLiveData).value = null
        }
    }

    /**
     * Set the buy confirm indicator.
     *
     * @author Lucas Held
     */
    fun confirmBuy() {
        _buyAction.value = true
    }

    /**
     * Set the sell confirm indicator.
     *
     * @author Lucas Held
     */
    fun confirmSell() {
        _sellAction.value = true
    }

    /**
     * Set the sell all confirm indicator.
     *
     * @author Lucas Held
     */
    fun confirmSellAll() {
        _sellAllAction.value = true
    }

    /**
     * Buys the current quote using the specified amount.
     *
     * @author Jan Müller
     */
    fun buy() {
        viewModelScope.launch {
            accountRepository.buy(quote.value!!, buyAmount.value!!.toDouble())
        }
    }

    /**
     * Sells the current quote using the specified amount.
     *
     * @author Jan Müller
     */
    fun sell() {
        viewModelScope.launch {
            accountRepository.sell(quote.value!!, sellAmount.value!!.toDouble())
        }
    }

    /**
     * Sells all depot quotes that matched the current quote.
     *
     * @author Lucas Held
     */
    fun sellAll() {
        viewModelScope.launch {
            accountRepository.sell(quote.value!!, depotQuote.value!!.amount)
        }
    }

    /**
     * Decides depending on the [stockbrotQuote] value if a quote can be added to or removed from the stockbrot.
     *
     * @author Lucas Held
     */
    fun addRemoveQuoteToStockbrot() {
        when (stockbrotQuote.value) {
            null -> addQuoteToStockbrot()
            else -> removeQuoteFromStockbrot()
        }
    }

    /**
     * Sets the buy amount and calculates the buy cashflow for displaying this values in the confirmation dialog.
     *
     * @author Lucas Held
     */
    fun onBuyActionStarted() {
        viewModelScope.launch {
            _amount.value = buyAmount.value
            _cashflow.value = accountRepository.calculateBuyCashflow(
                quote.value,
                amount.value.toSafeDouble()
            )
        }
    }

    /**
     * Sets the sell amount and calculates the sell cashflow for displaying this values in the confirmation dialog.
     *
     * @author Lucas Held
     */
    fun onSellActionStarted() {
        viewModelScope.launch {
            _amount.value = sellAmount.value
            _cashflow.value = accountRepository.calculateSellCashflow(
                quote.value,
                amount.value.toSafeDouble()
            )
        }
    }

    /**
     * Sets the sell amount and calculates the sell all cashflow for displaying this values in the confirmation dialog.
     *
     * @author Lucas Held
     */
    fun onSellAllActionStarted() {
        viewModelScope.launch {
            _amount.value =
                if (isCrypto) depotQuote.value!!.amount.toString() else depotQuote.value!!.amount.toLong()
                    .toString()
            _cashflow.value = accountRepository.calculateSellCashflow(
                quote.value,
                depotQuote.value!!.amount
            )
        }
    }

    /**
     * Resets the error indicator.
     *
     * @author Lucas Held
     */
    fun onErrorActionCompleted() {
        viewModelScope.launch {
            (errorAction as MutableLiveData).value = null
        }
    }

    /**
     * Resets the buy indicator.
     *
     * @author Lucas Held
     */
    fun onBuyActionCompleted() {
        viewModelScope.launch {
            _buyAction.value = null
        }
    }

    /**
     * Resets the sell indicator.
     *
     * @author Lucas Held
     */
    fun onSellActionCompleted() {
        viewModelScope.launch {
            _sellAction.value = null
        }
    }

    /**
     * Sets the flag that indicates that news are to be shown.
     *
     * @author Jonas Thelemann
     */
    fun showNews() {
        clickNewsStatus.value = true
    }

    /**
     * Resets the sell all indicator.
     *
     * @author Lucas Held
     */
    fun onSellAllActionCompleted() {
        viewModelScope.launch {
            _sellAllAction.value = null
        }
    }

    private fun initTimer() {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                refresh()
            }
        }, 10000, 10000)
    }

    /**
     * Adds the current quote to the stockbrot.
     *
     * @author Lucas Held
     */
    private fun addQuoteToStockbrot() {
        viewModelScope.launch {
            val autoBuyAmount = autoBuyAmount.value.toSafeDouble() ?: 0.0
            val thresholdBuyDouble = thresholdBuy.value.toSafeDouble() ?: 0.0
            val thresholdSellDouble = thresholdSell.value.toSafeDouble() ?: 0.0
            val newStockbrotQuote = StockbrotQuote(
                id = id,
                symbol = quote.value!!.symbol,
                type = type,
                limitedBuying = autoBuyAmount > 0.0,
                buyLimit = autoBuyAmount,
                maximumBuyPrice = thresholdBuyDouble,
                minimumSellPrice = thresholdSellDouble
            )
            stockbrotWorkRequest.addQuote(newStockbrotQuote)
            stockbrotRepository.addStockbrotQuote(newStockbrotQuote)
        }
    }

    /**
     * Removed the current quote from the stockbrot.
     *
     * @author Lucas Held
     */
    private fun removeQuoteFromStockbrot() {
        viewModelScope.launch {
            stockbrotWorkRequest.removeQuote(stockbrotQuote.value!!)
            stockbrotRepository.removeStockbrotQuote(stockbrotQuote.value!!)
        }
    }

    /**
     * Indicates if the [Quote] can be bought.
     *
     * @param amount The buy amount.
     * @param price The current quote price.
     * @param balance The account balance.
     * @param state The quote repository state.
     *
     * @author Jan Müller
     */
    private fun canBuy(
        amount: Double?,
        price: Double?,
        balance: Balance?,
        state: QuoteRepository.State?
    ) = noNulls(amount, price, depotQuote, state)
            && state === QuoteRepository.State.Done
            && 0 < amount!!
            && amount * price!! + BuildConfig.TRANSACTION_COSTS <= balance!!.value

    /**
     * Indicates if the [DepotQuote] can be sold.
     *
     * @param amount The sell amount.
     * @param depotQuote The depot quote.
     * @param balance The account balance.
     * @param state The quote repository state.
     * @param quote The quote.
     *
     * @author Jan Müller
     */
    private fun canSell(
        amount: Double?,
        depotQuote: DepotQuote?,
        balance: Balance?,
        state: QuoteRepository.State?,
        quote: Quote?
    ) = noNulls(amount, depotQuote, balance, state, quote)
            && state === QuoteRepository.State.Done
            && 0 < amount!!
            && amount <= depotQuote!!.amount
            && (BuildConfig.TRANSACTION_COSTS <= balance!!.value + quote!!.latestPrice * amount)

    /**
     * Indicates if all [DepotQuote]s can be sold.
     *
     * @param depotQuote
     * @param balance
     * @param state
     * @param quote
     *
     * @author Lucas Held
     */
    private fun canSellAll(
        depotQuote: DepotQuote?,
        balance: Balance?,
        state: QuoteRepository.State?,
        quote: Quote?
    ) = noNulls(depotQuote, state, quote)
            && state === QuoteRepository.State.Done
            && 0 < depotQuote!!.amount
            && (BuildConfig.TRANSACTION_COSTS <= balance!!.value + quote!!.latestPrice * depotQuote.amount)

    /**
     * Indicates if the [stockbrotQuote] can be added or removed.
     *
     * @param stockbrotQuote
     * @param buyAmount
     * @param thresholdBuy
     * @param thresholdSell
     *
     * @author Lucas Held
     */
    private fun canAddRemoveQuoteToStockbrot(
        stockbrotQuote: StockbrotQuote?,
        buyAmount: Double?,
        thresholdBuy: Double?,
        thresholdSell: Double?
    ) = when (stockbrotQuote) {
        null -> buyAmountIsValid(buyAmount) && thresholdIsValid(thresholdBuy) || thresholdIsValid(
            thresholdSell
        )
        else -> true
    }

    private fun buyAmountIsValid(buyAmount: Double?) = buyAmount != null && buyAmount >= 0

    private fun thresholdIsValid(threshold: Double?) = threshold != null && threshold > 0

    /**
     * Factory for the QuoteViewModel.
     *
     * @property application The context used for creating the repositories.
     * @property id The id of the quote.
     * @property type The [Symbol.Type] of the quote.
     */
    class Factory(
        private val application: Application,
        private val id: String,
        private val type: Symbol.Type
    ) : ViewModelProvider.Factory {

        /**
         * The factory's construction method.
         *
         * @param T The class's type.
         * @param modelClass The class to create.
         *
         * @throws [IllegalArgumentException] if [QuoteViewModel] is not assignable to [modelClass].
         *
         * @return A [QuoteViewModel] instance.
         */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuoteViewModel(application, id, type) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

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
import de.uniks.codliners.stock_simulator.toSafeDouble
import kotlinx.coroutines.launch
import java.util.*


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
    // the last 50 historical prices
    val historicalPricesLimited = quoteRepository.historicalPricesLimited(id)

    val isCrypto = type === Symbol.Type.CRYPTO
    val hasChange = quote.map { quote: Quote? -> quote !== null && !isCrypto }

    val inputType =
        if (isCrypto) InputType.TYPE_NUMBER_FLAG_DECIMAL else InputType.TYPE_CLASS_NUMBER

    private val state = quoteRepository.state
    val refreshing = state.map { it === QuoteRepository.State.Refreshing }

    private val _errorAction = MediatorLiveData<String>()
    val errorAction: LiveData<String> = _errorAction

    val buyAmount = MutableLiveData<String>().apply {
        value = if (isCrypto) "0.0" else "0"
    }
    private val _canBuy = MediatorLiveData<Boolean>()
    val canBuy: LiveData<Boolean> = _canBuy

    val sellAmount = MutableLiveData<String>().apply {
        value = if (isCrypto) "0.0" else "0"
    }
    private val _canSell = MediatorLiveData<Boolean>()
    val canSell: LiveData<Boolean> = _canSell

    private val _buyAction = MutableLiveData<Boolean>()
    val buyAction: LiveData<Boolean> = _buyAction

    private val _sellAction = MutableLiveData<Boolean>()
    val sellAction: LiveData<Boolean> = _sellAction

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

    private val _stockbrotQuoteAction = MediatorLiveData<StockbrotQuote>()
    val stockbrotQuoteAction: LiveData<StockbrotQuote> = _stockbrotQuoteAction

    private val _canAddRemoveQuoteToStockbrot = MediatorLiveData<Boolean>()
    val canAddRemoveQuoteToStockbrot: LiveData<Boolean> = _canAddRemoveQuoteToStockbrot

    // Button click indicator for reset button.
    val clickNewsStatus = MutableLiveData<Boolean?>()

    init {
        _errorAction.apply {
            addSource(state) { state ->
                value = when (state) {
                    is QuoteRepository.State.Error -> state.message
                    else -> null
                }
            }
        }

        _amount.apply {
            addSource(buyAmount) { amount ->
                _amount.value= amount
            }

            addSource(sellAmount) { amount ->
                _amount.value= amount
            }
        }

        _cashflow.apply {
            addSource(amount) {
                _cashflow.value = accountRepository.calculateCashflow(
                    quote.value,
                    it.toSafeDouble()
                )
            }

            addSource(quote) {
                _cashflow.value = accountRepository.calculateCashflow(
                    it,
                    amount.value.toSafeDouble()
                )
            }
        }

        _stockbrotQuoteAction.addSource(stockbrotQuote) { stockbrotQuote ->
            _stockbrotQuoteAction.value = stockbrotQuote
        }

        _canBuy.apply {
            addSource(buyAmount) {
                value = canBuy(
                    amount = it.toSafeDouble(),
                    price = quote.value?.latestPrice,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(quote) {
                value = canBuy(
                    amount = buyAmount.value.toSafeDouble(),
                    price = it?.latestPrice,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(latestBalance) {
                value = canBuy(
                    amount = buyAmount.value.toSafeDouble(),
                    price = quote.value?.latestPrice,
                    balance = it,
                    state = state.value
                )
            }

            addSource(state) {
                value = canBuy(
                    amount = buyAmount.value.toSafeDouble(),
                    price = quote.value?.latestPrice,
                    balance = latestBalance.value,
                    state = it
                )
            }
        }

        _canSell.apply {
            addSource(sellAmount) {
                value = canSell(
                    amount = it.toSafeDouble(),
                    depotQuote = depotQuote.value,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(depotQuote) {
                value = canSell(
                    amount = sellAmount.value.toSafeDouble(),
                    depotQuote = it,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(latestBalance) {
                value = canSell(
                    amount = sellAmount.value.toSafeDouble(),
                    depotQuote = depotQuote.value,
                    balance = it,
                    state = state.value
                )
            }

            addSource(state) {
                value = canSell(
                    amount = sellAmount.value.toSafeDouble(),
                    depotQuote = depotQuote.value,
                    balance = latestBalance.value,
                    state = it
                )
            }
        }

        _canAddRemoveQuoteToStockbrot.apply {
            addSource(autoBuyAmount) {
                value = canAddRemoveQuoteToStockbrot(
                    stockbrotQuote.value,
                    autoBuyAmount.value.toSafeDouble(),
                    thresholdBuy.value.toSafeDouble(),
                    thresholdSell.value.toSafeDouble()
                )
            }

            addSource(thresholdBuy) {
                value = canAddRemoveQuoteToStockbrot(
                    stockbrotQuote.value,
                    autoBuyAmount.value.toSafeDouble(),
                    thresholdBuy.value.toSafeDouble(),
                    thresholdSell.value.toSafeDouble()
                )
            }

            addSource(thresholdSell) {
                value = canAddRemoveQuoteToStockbrot(
                    stockbrotQuote.value,
                    autoBuyAmount.value.toSafeDouble(),
                    thresholdBuy.value.toSafeDouble(),
                    thresholdSell.value.toSafeDouble()
                )
            }
        }

        refresh()
        initTimer()
    }

    fun onThresholdBuyActionCompleted() {
        viewModelScope.launch {
            _stockbrotQuoteAction.value = null
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

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    fun confirmBuy() {
        _buyAction.value = true
    }

    fun confirmSell() {
        _sellAction.value = true
    }

    fun buy() {
        viewModelScope.launch {
            accountRepository.buy(quote.value!!, buyAmount.value!!.toDouble())
        }
    }

    fun sell() {
        viewModelScope.launch {
            accountRepository.sell(quote.value!!, sellAmount.value!!.toDouble())
        }
    }

    fun addRemoveQuoteToStockbrot() {
        when (stockbrotQuote.value) {
            null -> addQuoteToStockbrot()
            else -> removeQuoteFromStockbrot()
        }
    }

    private fun addQuoteToStockbrot() {
        viewModelScope.launch {
            val autoBuyAmount = autoBuyAmount.value.toSafeDouble() ?: 0.0
            val thresholdBuyDouble = thresholdBuy.value.toSafeDouble() ?: 0.0
            val thresholdSellDouble = thresholdSell.value.toSafeDouble() ?: 0.0
            val newStockbrotQuote = StockbrotQuote(id, quote.value!!.symbol, type, autoBuyAmount, thresholdBuyDouble, thresholdSellDouble)
            stockbrotWorkRequest.addQuote(newStockbrotQuote)
            stockbrotRepository.addStockbrotQuote(newStockbrotQuote)
        }
    }

    private fun removeQuoteFromStockbrot() {
        viewModelScope.launch {
            stockbrotWorkRequest.removeQuote(stockbrotQuote.value!!)
            stockbrotRepository.removeStockbrotQuote(stockbrotQuote.value!!)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            when (type) {
                Symbol.Type.SHARE -> quoteRepository.fetchIEXQuote(id)
                Symbol.Type.CRYPTO -> quoteRepository.fetchCoinGeckoQuote(id)
            }
        }
    }

    fun onErrorActionCompleted() {
        viewModelScope.launch {
            _errorAction.value = null
        }
    }

    fun onBuyActionCompleted() {
        viewModelScope.launch {
            _buyAction.value = null
        }
    }

    fun onSellActionCompleted() {
        viewModelScope.launch {
            _sellAction.value = null
        }
    }

    private fun canBuy(
        amount: Double?,
        price: Double?,
        balance: Balance?,
        state: QuoteRepository.State?
    ) = noNulls(amount, price, depotQuote, state)
            && state === QuoteRepository.State.Done
            && 0 < amount!!
            && amount * price!! + BuildConfig.TRANSACTION_COSTS <= balance!!.value

    private fun canSell(
        amount: Double?,
        depotQuote: DepotQuote?,
        balance: Balance?,
        state: QuoteRepository.State?
    ) = noNulls(amount, depotQuote, balance, state)
            && state === QuoteRepository.State.Done
            && 0 < amount!!
            && amount <= depotQuote!!.amount
            && BuildConfig.TRANSACTION_COSTS <= balance!!.value

    private fun canAddRemoveQuoteToStockbrot(
        stockbrotQuote: StockbrotQuote?,
        buyAmount: Double?,
        thresholdBuy: Double?,
        thresholdSell: Double?
    ) = when (stockbrotQuote) {
        null -> buyAmountIsValid(buyAmount) && thresholdIsValid(thresholdBuy) || thresholdIsValid(thresholdSell)
        else -> true
    }

    private fun buyAmountIsValid(buyAmount: Double?) = buyAmount != null && buyAmount >= 0

    private fun thresholdIsValid(threshold: Double?) = threshold != null && threshold > 0

    class Factory(
        private val application: Application,
        private val id: String,
        private val type: Symbol.Type
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuoteViewModel(application, id, type) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

    fun showNews() {
        clickNewsStatus.value = true
    }
}

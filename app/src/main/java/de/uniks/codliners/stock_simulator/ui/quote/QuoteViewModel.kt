package de.uniks.codliners.stock_simulator.ui.quote

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.*
import de.uniks.codliners.stock_simulator.background.Constants
import de.uniks.codliners.stock_simulator.background.StockbrotWorkRequest
import de.uniks.codliners.stock_simulator.database.DepotQuote
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository
import kotlinx.coroutines.launch
import java.util.*


class QuoteViewModel(application: Application, private val symbol: String) : AndroidViewModel(application) {


    private lateinit var timer: Timer

    private val quoteRepository = QuoteRepository(application)
    private val accountRepository = AccountRepository(application)
    private val stockbrotRepository = StockbrotRepository(application)

    private val stockbrotWorkRequest = StockbrotWorkRequest(application)

    private val latestBalance = accountRepository.latestBalance

    val quote = quoteRepository.quoteWithSymbol(symbol)
    val depotQuote = accountRepository.depotQuoteWithSymbol(symbol)
    val stockbrotQuote = stockbrotRepository.stockbrotQuoteWithSymbol(symbol)
    val historicalPrices = quoteRepository.historicalPrices(symbol)

    private val state = quoteRepository.state
    val refreshing = state.map { it === QuoteRepository.State.Refreshing }

    private val _errorAction = MediatorLiveData<String>()
    val errorAction: LiveData<String> = _errorAction

    val buyAmount = MutableLiveData("0")
    private val _canBuy = MediatorLiveData<Boolean>()
    val canBuy: LiveData<Boolean> = _canBuy

    val sellAmount = MutableLiveData("0")
    private val _canSell = MediatorLiveData<Boolean>()
    val canSell: LiveData<Boolean> = _canSell

    val thresholdBuy = MutableLiveData("0.0")
    val thresholdSell = MutableLiveData("0.0")
    val autoBuyAmount = MutableLiveData("0")

    private val _stockbrotQuoteAction = MediatorLiveData<StockbrotQuote>()
    val stockbrotQuoteAction: LiveData<StockbrotQuote> = _stockbrotQuoteAction

    private val _canAddRemoveQuoteToStockbrot = MediatorLiveData<Boolean>()
    val canAddRemoveQuoteToStockbrot: LiveData<Boolean> = _canAddRemoveQuoteToStockbrot

    init {
        _errorAction.apply {
            addSource(state) { state ->
                value = when (state) {
                    is QuoteRepository.State.Error -> state.message
                    else -> null
                }
            }
        }

        _stockbrotQuoteAction.addSource(stockbrotQuote) { stockbrotQuote ->
            _stockbrotQuoteAction.value = stockbrotQuote
        }

        _canBuy.apply {
            addSource(buyAmount) {
                value = canBuy(
                    amount = it?.toSafeLong(),
                    price = quote.value?.latestPrice,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(quote) {
                value = canBuy(
                    amount = buyAmount.value?.toSafeLong(),
                    price = it?.latestPrice,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(latestBalance) {
                value = canBuy(
                    amount = buyAmount.value?.toSafeLong(),
                    price = quote.value?.latestPrice,
                    balance = it,
                    state = state.value
                )
            }

            addSource(state) {
                value = canBuy(
                    amount = buyAmount.value?.toSafeLong(),
                    price = quote.value?.latestPrice,
                    balance = latestBalance.value,
                    state = it
                )
            }
        }

        _canSell.apply {
            addSource(sellAmount) {
                value = canSell(
                    amount = it.toSafeLong(),
                    depotQuote = depotQuote.value,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(depotQuote) {
                value = canSell(
                    amount = sellAmount.value?.toSafeLong(),
                    depotQuote = it,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(latestBalance) {
                value = canSell(
                    amount = sellAmount.value?.toSafeLong(),
                    depotQuote = depotQuote.value,
                    balance = it,
                    state = state.value
                )
            }

            addSource(state) {
                value = canSell(
                    amount = sellAmount.value?.toSafeLong(),
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
                    autoBuyAmount.value?.toSafeInt(),
                    thresholdBuy.value?.toSafeDouble(),
                    thresholdSell.value?.toSafeDouble()
                )
            }

            addSource(thresholdBuy) {
                value = canAddRemoveQuoteToStockbrot(
                    stockbrotQuote.value,
                    autoBuyAmount.value?.toSafeInt(),
                    thresholdBuy.value?.toSafeDouble(),
                    thresholdSell.value?.toSafeDouble()
                )
            }

            addSource(thresholdSell) {
                value = canAddRemoveQuoteToStockbrot(
                    stockbrotQuote.value,
                    autoBuyAmount.value?.toSafeInt(),
                    thresholdBuy.value?.toSafeDouble(),
                    thresholdSell.value?.toSafeDouble()
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

    fun buy() {
        viewModelScope.launch {
            accountRepository.buy(quote.value!!, buyAmount.value!!.toInt())
        }
    }

    fun sell() {
        viewModelScope.launch {
            accountRepository.sell(quote.value!!, sellAmount.value!!.toInt())
        }
    }

    fun addRemoveQuoteToStockbrot() {
        when(stockbrotQuote.value) {
            null -> addQuoteToStockbrot()
            else -> removeQuoteFromStockbrot()
        }
    }

    private fun addQuoteToStockbrot() {
        viewModelScope.launch {
            val autoBuyAmount = when(autoBuyAmount.value) {
                null -> Constants.BUY_AMOUNT_DEFAULT
                else -> autoBuyAmount.value.toSafeInt() ?: Constants.BUY_AMOUNT_DEFAULT
            }
            val thresholdBuyDouble = when(thresholdBuy.value) {
                null -> Constants.THRESHOLD_DEFAULT
                else -> thresholdBuy.value.toSafeDouble() ?: Constants.THRESHOLD_DEFAULT
            }
            val thresholdSellDouble = when(thresholdSell.value) {
                null -> Constants.THRESHOLD_DEFAULT
                else -> thresholdSell.value.toSafeDouble() ?: Constants.THRESHOLD_DEFAULT
            }
            val newStockbrotQuote = StockbrotQuote(symbol, autoBuyAmount, thresholdBuyDouble, thresholdSellDouble)
            stockbrotWorkRequest.addQuote(newStockbrotQuote)
            stockbrotRepository.saveAddStockbrotControl(newStockbrotQuote)
        }
    }

    private fun removeQuoteFromStockbrot() {
        viewModelScope.launch {
            stockbrotWorkRequest.removeQuote(stockbrotQuote.value!!)
            stockbrotRepository.saveRemoveStockbrotControl(stockbrotQuote.value!!)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            quoteRepository.fetchQuoteWithSymbol(symbol)
        }
    }

    fun onErrorActionCompleted() {
        viewModelScope.launch {
            _errorAction.value = null
        }
    }

    private fun canBuy(
        amount: Long?,
        price: Double?,
        balance: Balance?,
        state: QuoteRepository.State?
    ) = noNulls(amount, price, depotQuote, state)
            && state === QuoteRepository.State.Done
            && 0 < amount!!
            && amount * price!! + BuildConfig.TRANSACTION_COSTS <= balance!!.value

    private fun canSell(
        amount: Long?,
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
        autoBuyAmount: Int?,
        thresholdBuy: Double?,
        thresholdSell: Double?
    ) = when(stockbrotQuote) {
        null -> ( autoBuyAmountValid(autoBuyAmount) && thresholdValid(thresholdBuy) ) ||
                    thresholdValid(thresholdSell)
        else -> true
    }

    private fun thresholdValid(threshold: Double?) = noNulls(threshold) && 0 < threshold!!

    private fun autoBuyAmountValid(autoBuyAmount: Int?) = noNulls(autoBuyAmount) && 0 < autoBuyAmount!!

    class Factory(
        private val application: Application,
        private val shareId: String
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuoteViewModel(application, shareId) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}

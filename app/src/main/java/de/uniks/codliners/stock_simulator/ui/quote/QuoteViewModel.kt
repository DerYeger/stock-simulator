package de.uniks.codliners.stock_simulator.ui.quote

import android.app.Application
import android.text.InputType
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.background.StockbrotWorkRequest
import de.uniks.codliners.stock_simulator.database.DepotQuote
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.noNulls
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository
import de.uniks.codliners.stock_simulator.toSafeDouble
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*


class QuoteViewModel(
    application: Application,
    private val symbol: String,
    private val type: Symbol.Type
) : AndroidViewModel(application) {

    private lateinit var timer: Timer

    private val quoteRepository = QuoteRepository(application)
    private val accountRepository = AccountRepository(application)
    private val stockbrotRepository = StockbrotRepository(application)

    private val stockbrotWorkRequest = StockbrotWorkRequest(application)

    private val latestBalance = accountRepository.latestBalance

    val quote = quoteRepository.quoteWithSymbol(symbol)
    val depotQuote = accountRepository.depotQuoteWithSymbol(symbol)
    lateinit var stockbrotQuote: MutableLiveData<StockbrotQuote>
    val historicalPrices = quoteRepository.historicalPrices(symbol)

    private val isCrypto = type === Symbol.Type.CRYPTO

    val inputType = if (isCrypto) InputType.TYPE_NUMBER_FLAG_DECIMAL else InputType.TYPE_CLASS_NUMBER

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

    val thresholdBuy = MutableLiveData("0.0")
    val thresholdSell = MutableLiveData("0.0")
    private val _canAddQuoteToStockbrot = MediatorLiveData<Boolean>()
    val canAddQuoteToStockbrot: LiveData<Boolean> = _canAddQuoteToStockbrot

    init {
        _errorAction.apply {
            addSource(state) { state ->
                value = when (state) {
                    is QuoteRepository.State.Error -> state.message
                    else -> null
                }
            }
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

        _canAddQuoteToStockbrot.apply {
            addSource(thresholdBuy) {
                value = canAddQuoteToStockbrot(
                    thresholdBuy.value.toSafeDouble(),
                    thresholdSell.value.toSafeDouble()
                )
            }

            addSource(thresholdSell) {
                value = canAddQuoteToStockbrot(
                    thresholdBuy.value.toSafeDouble(),
                    thresholdSell.value.toSafeDouble()
                )
            }
        }

        viewModelScope.launch {
            stockbrotQuote = stockbrotRepository.stockbrotQuoteWithSymbol(symbol, type)
        }

        refresh()
        initTimer()
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
            accountRepository.buy(quote.value!!, buyAmount.value!!.toDouble())
        }
    }

    fun sell() {
        viewModelScope.launch {
            accountRepository.sell(quote.value!!, sellAmount.value!!.toDouble())
        }
    }

    fun addQuoteToStockbrot() {
        viewModelScope.launch {
            val thresholdBuyDouble = thresholdBuy.value?.toDouble()!!
            val thresholdSellDouble = thresholdSell.value?.toDouble()!!
            val newStockbrotQuote =
                StockbrotQuote(symbol, type, thresholdBuyDouble, thresholdSellDouble)
            stockbrotWorkRequest.addQuote(newStockbrotQuote)
            stockbrotRepository.saveAddStockbrotControl(newStockbrotQuote)
        }
    }

    fun removeQuoteFromStockbrot() {
        viewModelScope.launch {
            stockbrotWorkRequest.removeQuote(stockbrotQuote.value!!)
            stockbrotRepository.saveRemoveStockbrotControl(stockbrotQuote.value!!)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            quoteRepository.fetchQuoteWithSymbol(symbol, type)
        }
    }

    fun onErrorActionCompleted() {
        viewModelScope.launch {
            _errorAction.value = null
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

    private fun canAddQuoteToStockbrot(
        thresholdBuy: Double?,
        thresholdSell: Double?
    ) = thresholdValid(thresholdBuy) && thresholdValid(thresholdSell)

    private fun thresholdValid(threshold: Double?) = noNulls(threshold) && 0 < threshold!!

    class Factory(
        private val application: Application,
        private val symbol: String,
        private val type: Symbol.Type
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return QuoteViewModel(application, symbol, type) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

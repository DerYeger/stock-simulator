package de.uniks.codliners.stock_simulator.ui.quote

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.background.StockbrotWorkRequest
import de.uniks.codliners.stock_simulator.database.DepotQuote
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.domain.StockbrotQuote
import de.uniks.codliners.stock_simulator.noNulls
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.repository.StockbrotRepository
import de.uniks.codliners.stock_simulator.toSafeDouble
import de.uniks.codliners.stock_simulator.toSafeLong
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
    lateinit var stockbrotQuote: MutableLiveData<StockbrotQuote>
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

        _canAddQuoteToStockbrot.apply {
            addSource(thresholdBuy) {
                value = canAddQuoteToStockbrot(
                    thresholdBuy.value?.toSafeDouble(),
                    thresholdSell.value?.toSafeDouble()
                )
            }

            addSource(thresholdSell) {
                value = canAddQuoteToStockbrot(
                    thresholdBuy.value?.toSafeDouble(),
                    thresholdSell.value?.toSafeDouble()
                )
            }
        }

        viewModelScope.launch {
            stockbrotQuote = stockbrotRepository.stockbrotQuoteWithSymbol(symbol)
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
            accountRepository.buy(quote.value!!, buyAmount.value!!.toInt())
        }
    }

    fun sell() {
        viewModelScope.launch {
            accountRepository.sell(quote.value!!, sellAmount.value!!.toInt())
        }
    }

    fun addQuoteToStockbrot() {
        viewModelScope.launch {
            val thresholdBuyDouble = thresholdBuy.value?.toDouble()!!
            val thresholdSellDouble = thresholdSell.value?.toDouble()!!
            val newStockbrotQuote = StockbrotQuote(symbol, thresholdBuyDouble, thresholdSellDouble)
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

    private fun canAddQuoteToStockbrot(
        thresholdBuy: Double?,
        thresholdSell: Double?
    ) = thresholdValid(thresholdBuy) && thresholdValid(thresholdSell)

    private fun thresholdValid(threshold: Double?) = noNulls(threshold) && 0 < threshold!!

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

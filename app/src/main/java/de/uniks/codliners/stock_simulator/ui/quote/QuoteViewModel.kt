package de.uniks.codliners.stock_simulator.ui.quote

import android.app.Application
import androidx.lifecycle.*
import de.uniks.codliners.stock_simulator.database.DepotQuote
import de.uniks.codliners.stock_simulator.domain.Balance
import de.uniks.codliners.stock_simulator.noNulls
import de.uniks.codliners.stock_simulator.repository.AccountRepository
import de.uniks.codliners.stock_simulator.repository.QuoteRepository
import de.uniks.codliners.stock_simulator.toSafeLong
import kotlinx.coroutines.launch
import timber.log.Timber

class QuoteViewModel(application: Application, private val symbol: String) : ViewModel() {

    private val quoteRepository = QuoteRepository(application)
    private val accountRepository = AccountRepository(application)

    private val latestBalance = accountRepository.latestBalance

    val quote = quoteRepository.quoteWithSymbol(symbol)
    val depotQuote = accountRepository.depotQuoteWithSymbol(symbol)

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
                    amount = it.toSafeLong(),
                    price = quote.value?.latestPrice,
                    balance = latestBalance.value,
                    state = state.value
                )
            }

            addSource(quote) {
                value = canBuy(
                    amount = buyAmount.value?.toSafeLong(),
                    price = it.latestPrice,
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
                    state = state.value
                )
            }

            addSource(depotQuote) {
                value = canSell(
                    amount = sellAmount.value?.toSafeLong(),
                    depotQuote = it,
                    state = state.value
                )
            }

            addSource(state) {
                value = canSell(
                    amount = sellAmount.value?.toSafeLong(),
                    depotQuote = depotQuote.value,
                    state = it
                )
            }
        }

        refresh()
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
            && amount * price!! <= balance!!.value

    private fun canSell(
        amount: Long?,
        depotQuote: DepotQuote?,
        state: QuoteRepository.State?
    ) = noNulls(amount, depotQuote, state)
            && state === QuoteRepository.State.Done
            && 0 < amount!!
            && amount <= depotQuote!!.amount

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

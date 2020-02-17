package de.uniks.codliners.stock_simulator.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.network.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(private val database: StockAppDatabase) {

    constructor(application: Application) : this(getDatabase(application))


    private val _refreshing = MutableLiveData<Boolean>()
    val refreshing: LiveData<Boolean> = _refreshing

    fun quoteWithSymbol(symbol: String): LiveData<Quote> = database.quoteDao.getQuoteWithSymbol(symbol)

    suspend fun fetchQuoteWithSymbol(symbol: String) {
        withContext(Dispatchers.IO) {
            _refreshing.postValue(true)
            val response = NetworkService.IEX_API.quote(symbol)
            database.quoteDao.insert(response)
            _refreshing.postValue(false)
        }
    }
}

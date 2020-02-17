package de.uniks.codliners.stock_simulator.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.sharesAsDomainModel
import de.uniks.codliners.stock_simulator.domain.Share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(private val stockAppDatabase: StockAppDatabase) {

    val shares: LiveData<List<Share>> = Transformations.map(stockAppDatabase.shareDao.getShares()) {
        it.sharesAsDomainModel()
    }

    suspend fun refreshShares() {
        withContext(Dispatchers.IO) {
            // val shareList = Network.shareService.getShares().await()
            // stockDatabase.shareDao.insertAll(*shareList.asDatabaseModel())
        }
    }

}


package de.uniks.codliners.stock_simulator.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import de.uniks.codliners.stock_simulator.database.StockDatabase
import de.uniks.codliners.stock_simulator.database.asDomainModel
import de.uniks.codliners.stock_simulator.domain.Share

class SearchRepository(private val stockDatabase: StockDatabase) {

    val shares: LiveData<List<Share>> = Transformations.map(stockDatabase.shareDao.getShares()) {
        it.asDomainModel()
    }
    
}


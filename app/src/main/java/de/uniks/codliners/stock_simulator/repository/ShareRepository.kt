package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.asDomainModel
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.database.sharesAsDomainModel
import de.uniks.codliners.stock_simulator.domain.Share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShareRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val shares: LiveData<List<Share>> = Transformations.map(database.shareDao.getShares()) {
        it.sharesAsDomainModel()
    }

    fun shareWithId(shareId: String): LiveData<Share> = Transformations.map(database.shareDao.getShareById(shareId)) {
        it?.asDomainModel()
    }

    suspend fun refreshShares() {
        withContext(Dispatchers.IO) {
            // val shareList = Network.shareService.getShares().await()
            // stockDatabase.shareDao.insertAll(*shareList.asDatabaseModel())
        }
    }

    suspend fun resetShares() {
        withContext(Dispatchers.IO) {
            database.shareDao.apply {
                deleteShares()
            }
        }
    }
}


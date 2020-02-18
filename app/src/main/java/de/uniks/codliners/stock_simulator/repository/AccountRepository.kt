package de.uniks.codliners.stock_simulator.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.database.sharesAsDomainModel
import de.uniks.codliners.stock_simulator.domain.Share

class AccountRepository(private val stockAppDatabase: StockAppDatabase) {

    constructor(application: Application): this(getDatabase(application))

    val depotShares: LiveData<List<Share>> = Transformations.map(stockAppDatabase.depotDao.getShares()) {
        it.sharesAsDomainModel()
    }
}
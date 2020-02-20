package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase

class AchievementRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))
}

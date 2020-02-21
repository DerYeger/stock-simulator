package de.uniks.codliners.stock_simulator.repository

import android.content.Context
import de.uniks.codliners.stock_simulator.R
import de.uniks.codliners.stock_simulator.database.StockAppDatabase
import de.uniks.codliners.stock_simulator.database.getDatabase
import de.uniks.codliners.stock_simulator.domain.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AchievementsRepository(private val database: StockAppDatabase) {

    constructor(context: Context) : this(getDatabase(context))

    val achievements by lazy {
        database.achievementDao.getAchievements()
    }

    val latestAchievement by lazy {
        database.achievementDao.getLatestAchievement()
    }

    suspend fun initAchievements() {
        insertAchievement(
            Achievement(
                R.string.achievement_99995reached_name,
                R.string.achievement_99995reached_description
            )
        )
        insertAchievement(
            Achievement(
                R.string.achievement_10010reached_name,
                R.string.achievement_10010reached_description
            )
        )
        insertAchievement(
            Achievement(
                R.string.achievement_20000reached_name,
                R.string.achievement_20000reached_description
            )
        )
    }

    suspend fun getAchievementsByName(name: Int): Achievement? {
        return withContext(Dispatchers.IO) {
            database.achievementDao.getAchievementByName(name)
        }
    }

    suspend fun insertAchievement(achievement: Achievement) {
        withContext(Dispatchers.IO) {
            database.achievementDao.insert(achievement)
        }
    }

    suspend fun resetAchievements() {
        withContext(Dispatchers.IO) {
            database.achievementDao.apply {
                deleteAchievements()
            }
        }
        initAchievements()
    }
}

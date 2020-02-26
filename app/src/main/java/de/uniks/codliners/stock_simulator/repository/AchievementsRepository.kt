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

    suspend fun initAchievements() {
        val achievements = listOf(
            Achievement(
                R.string.achievement_200dollarLeft_name,
                R.string.achievement_200dollarLeft_description
            ),
            Achievement(
                R.string.achievement_1000dollarLost_name,
                R.string.achievement_1000dollarLost_description
            ),
            Achievement(
                R.string.achievement_10dollarWon_name,
                R.string.achievement_10dollarWon_description
            ),
            Achievement(
                R.string.achievement_100dollarWon_name,
                R.string.achievement_100dollarWon_description
            ),
            Achievement(
                R.string.achievement_500dollarWon_name,
                R.string.achievement_500dollarWon_description
            ),
            Achievement(
                R.string.achievement_10000dollarWon_name,
                R.string.achievement_10000dollarWon_description
            ),
            Achievement(
                R.string.achievement_20000dollarWon_name,
                R.string.achievement_20000dollarWon_description
            ),
            Achievement(
                R.string.achievement_40000dollarWon_name,
                R.string.achievement_40000dollarWon_description
            ),
            Achievement(
                R.string.achievement_1shareInDepot_name,
                R.string.achievement_1shareInDepot_description
            ),
            Achievement(
                R.string.achievement_5sharesInDepot_name,
                R.string.achievement_5sharesInDepot_description
            ),
            Achievement(
                R.string.achievement_10sharesInDepot_name,
                R.string.achievement_10sharesInDepot_description
            ),
            Achievement(
                R.string.achievement_5differentSharesInDepot_name,
                R.string.achievement_5differentSharesInDepot_description
            ),
            Achievement(
                R.string.achievement_10differentSharesInDepot_name,
                R.string.achievement_10differentSharesInDepot_description
            )
        )
        for (achievement in achievements) {
            if (getAchievementsByName(achievement.name) == null) {
                insertAchievement(Achievement(achievement.name, achievement.description))
            }
        }
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

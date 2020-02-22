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
        val achievements = listOf(
            Achievement(
                R.string.achievement_5dollarlost_name,
                R.string.achievement_5dollarlost_description
            ),
            Achievement(
                R.string.achievement_10dollarwon_name,
                R.string.achievement_10dollarwon_description
            ),
            Achievement(
                R.string.achievement_10000dollarwon_name,
                R.string.achievement_10000dollarwon_description
            ),
            Achievement(
                R.string.achievement_1shareindepot_name,
                R.string.achievement_1shareindepot_description
            ),
            Achievement(
                R.string.achievement_5sharesindepot_name,
                R.string.achievement_5sharesindepot_description
            ),
            Achievement(
                R.string.achievement_10sharesindepot_name,
                R.string.achievement_10sharesindepot_description
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

package de.uniks.codliners.stock_simulator.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.uniks.codliners.stock_simulator.domain.Share

@Entity
data class DatabaseShare constructor(
    @PrimaryKey
    val id: String,
    val title: String,
    val value: Int,
    val runningCost: Double
)

fun List<DatabaseShare>.asDomainModel(): List<Share> {
    return map {
        Share(
            id = it.id,
            title = it.title,
            value = it.value,
            runningCost = it.runningCost)
    }
}
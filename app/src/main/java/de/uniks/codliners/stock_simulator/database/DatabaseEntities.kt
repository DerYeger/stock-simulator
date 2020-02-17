package de.uniks.codliners.stock_simulator.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.uniks.codliners.stock_simulator.domain.Share

@Entity
data class ShareDatabase constructor(
    @PrimaryKey
    val id: String,
    val name: String,
    val value: Double,
    val runningCost: Double
)

@Entity
data class DepotShare constructor(
    @PrimaryKey
    val id: String
)

fun ShareDatabase.asDomainModel() = Share(
    id = this.id,
    name = this.name,
    value = this.value,
    runningCost = this.runningCost
)

fun List<ShareDatabase>.sharesAsDomainModel(): List<Share> {
    return map {
        it.asDomainModel()
    }
}
package de.uniks.codliners.stock_simulator.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.uniks.codliners.stock_simulator.domain.Share

@Entity
data class ShareDatabase constructor(
    @PrimaryKey
    val id: String,
    val name: String,
    val price: Double,
    val runningCost: Double,
    val gap: Double,
    val gapPercent: Double
)

@Entity
data class DepotShare constructor(
    @PrimaryKey
    val id: String
)

fun ShareDatabase.asDomainModel() = Share(
    id = this.id,
    name = this.name,
    price = this.price,
    runningCost = this.runningCost,
    gap = this.gap,
    gapPercent = this.gap
)

fun List<ShareDatabase>.sharesAsDomainModel(): List<Share> {
    return map {
        Share(
            id = it.id,
            name = it.name,
            price = it.price,
            runningCost = it.runningCost,
            gap = it.gap,
            gapPercent = it.gap)
        it.asDomainModel()
    }
}
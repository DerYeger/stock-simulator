package de.uniks.codliners.stock_simulator.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.uniks.codliners.stock_simulator.domain.Share
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.domain.TransactionType

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
data class DepotQuote(
    @PrimaryKey
    val symbol: String,
    val amount: Long
)

@Entity
data class TransactionDatabase constructor(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val shareName: String,
    val number: Int,
    val transactionType: TransactionType,
    val date: Long
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
        it.asDomainModel()
    }
}

fun TransactionDatabase.transactionAsDomainModel() = Transaction(
    shareName = this.shareName,
    number = this.number,
    transactionType = this.transactionType,
    date = this.date
)

fun List<TransactionDatabase>.transactionsAsDomainModel(): List<Transaction> {
    return map {
        it.transactionAsDomainModel()
    }
}
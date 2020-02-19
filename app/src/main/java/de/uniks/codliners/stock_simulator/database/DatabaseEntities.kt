package de.uniks.codliners.stock_simulator.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.uniks.codliners.stock_simulator.domain.HistoricalPriceFromApi
import de.uniks.codliners.stock_simulator.domain.Share
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.domain.TransactionType

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
    val symbol: String,
    val companyName: String,
    val amount: Int,
    val price: Double,
    val transactionType: TransactionType,
    val date: Long
)

@Entity
data class HistoricalPrice constructor(
    @PrimaryKey
    val symbol: String,
    val date: String,
    val high: Double,
    val low: Double,
    val volume: Long,
    val open: Double,
    val close: Double,
    val uHigh: Long,
    val uLow: Long,
    val uVolume: Long,
    val uOpen: Long,
    val uClose: Long,
    val changeOverTime: Long,
    val label: Long,
    val change: Double,
    val changePercent: Double
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
    symbol = this.symbol,
    companyName = this.companyName,
    amount = this.amount,
    price = this.price,
    transactionType = this.transactionType,
    date = this.date
)

fun List<TransactionDatabase>.transactionsAsDomainModel(): List<Transaction> {
    return map {
        it.transactionAsDomainModel()
    }
}

fun HistoricalPriceFromApi.apiPriceAsPriceWithSymbol(symbol: String) = HistoricalPrice(
    symbol = symbol,
    date = this.date,
    high = this.high,
    low = this.low,
    volume = this.volume,
    open = this.open,
    close = this.close,
    uHigh = this.uHigh,
    uLow = this.uLow,
    uVolume = this.uVolume,
    uOpen = this.uOpen,
    uClose = this.uClose,
    changeOverTime = this.changeOverTime,
    label = this.label,
    change = this.change,
    changePercent = this.changePercent
)

fun List<HistoricalPriceFromApi>.apiPricesAsPricesWithSymbol(symbol: String): List<HistoricalPrice> {
    return map {
        it.apiPriceAsPriceWithSymbol(symbol)
    }
}
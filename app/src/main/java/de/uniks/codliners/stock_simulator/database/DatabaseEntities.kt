package de.uniks.codliners.stock_simulator.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.uniks.codliners.stock_simulator.domain.HistoricalPriceFromApi
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.domain.TransactionType

@Entity
data class DepotQuote(
    @PrimaryKey
    val symbol: String,
    val amount: Long
)

@Entity
data class DepotValue(
    val value: Double,
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis()
)

@Entity
data class TransactionDatabase constructor(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val symbol: String,
    val companyName: String,
    val amount: Int,
    val price: Double,
    val transactionCosts: Double,
    val cashflow: Double,
    val transactionType: TransactionType,
    val date: Long
)

@Entity
data class HistoricalPrice constructor(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val symbol: String,
    val date: String,
    val close: Double,
    val change: Double,
    val changeOverTime: Double,
    val changePercent: Double
)

fun TransactionDatabase.transactionAsDomainModel() = Transaction(
    symbol = this.symbol,
    companyName = this.companyName,
    amount = this.amount,
    price = this.price,
    transactionCosts = this.transactionCosts,
    cashflow = this.cashflow,
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
    close = this.close,
    changeOverTime = this.changeOverTime,
    change = this.change,
    changePercent = this.changePercent
)

fun List<HistoricalPriceFromApi>.apiPricesAsPricesWithSymbol(symbol: String): List<HistoricalPrice> {
    return map {
        it.apiPriceAsPriceWithSymbol(symbol)
    }
}
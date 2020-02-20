package de.uniks.codliners.stock_simulator.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.uniks.codliners.stock_simulator.domain.HistoricalPriceFromApi
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.domain.TransactionType

@Entity
data class DepotQuote(
    @PrimaryKey
    val symbol: String,
    val amount: Double
)

@Entity
data class TransactionDatabase(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val symbol: String,
    val type: Symbol.Type,
    val amount: Double,
    val price: Double,
    val transactionCosts: Double,
    val cashflow: Double,
    val transactionType: TransactionType,
    val date: Long
)

@Entity
data class HistoricalPrice(
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
    type = this.type,
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
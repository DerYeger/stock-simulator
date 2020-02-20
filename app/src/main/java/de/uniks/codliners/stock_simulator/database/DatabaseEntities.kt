package de.uniks.codliners.stock_simulator.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.uniks.codliners.stock_simulator.domain.HistoricalPriceFromApi
import de.uniks.codliners.stock_simulator.domain.Symbol
import de.uniks.codliners.stock_simulator.domain.Transaction
import de.uniks.codliners.stock_simulator.domain.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class DepotQuote(
    @PrimaryKey
    val id: String,
    val type: Symbol.Type,
    val amount: Double
)

@Entity
data class DatabaseTransaction(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val id: String,
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
    val id: String,
    val date: Long,
    val price: Double
)

fun DatabaseTransaction.transactionAsDomainModel() = Transaction(
    id = this.id,
    type = this.type,
    amount = this.amount,
    price = this.price,
    transactionCosts = this.transactionCosts,
    cashflow = this.cashflow,
    transactionType = this.transactionType,
    date = this.date
)

fun List<DatabaseTransaction>.transactionsAsDomainModel(): List<Transaction> {
    return map {
        it.transactionAsDomainModel()
    }
}

fun HistoricalPriceFromApi.apiPriceAsPriceWithSymbol(symbol: String) = HistoricalPrice(
    id = symbol,
    date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(date)!!.time,
    price = this.close
)

fun List<HistoricalPriceFromApi>.apiPricesAsPricesWithSymbol(symbol: String): List<HistoricalPrice> {
    return map {
        it.apiPriceAsPriceWithSymbol(symbol)
    }
}

package de.uniks.codliners.stock_simulator.database

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val transactionCosts: Double,
    val cashflow: Double,
    val transactionType: TransactionType,
    val date: Long
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

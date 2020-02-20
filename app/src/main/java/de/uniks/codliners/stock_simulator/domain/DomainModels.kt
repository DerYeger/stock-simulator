package de.uniks.codliners.stock_simulator.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Balance(
    val value: Double,
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType { BUY, SELL }

data class Transaction(
    val symbol: String,
    val type: Symbol.Type,
    val amount: Double,
    val price: Double,
    val transactionCosts: Double,
    val cashflow: Double,
    val transactionType: TransactionType,
    val date: Long
)

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
    val companyName: String,
    val amount: Int,
    val price: Double,
    val transactionType: TransactionType,
    val date: Long
)

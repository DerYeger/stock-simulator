package de.uniks.codliners.stock_simulator.domain

import androidx.annotation.IntegerRes
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

@Entity
data class Achievement(
    @PrimaryKey
    @IntegerRes
    val name: Int,
    @IntegerRes
    val unlockRequirement: Int
)

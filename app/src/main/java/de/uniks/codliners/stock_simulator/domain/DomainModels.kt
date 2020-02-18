package de.uniks.codliners.stock_simulator.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(
    val balance: Double,
    @PrimaryKey
    val id: Long = 0L
)

data class Share(
    val id: String,
    val name: String,
    val price: Double,
    val runningCost: Double,
    val gap: Double,
    val gapPercent: Double
)

enum class TransactionType { BUY, SELL }

data class Transaction(
    val shareName: String,
    val number: Int,
    val transactionType: TransactionType,
    val date: String
)
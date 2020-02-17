package de.uniks.codliners.stock_simulator.domain

data class Share(
    val id: String,
    val name: String,
    val price: Double,
    val runningCost: Double,
    val gap: Double,
    val gapPercent: Double
)

enum class TransactionType{BUY, SELL}

data class Transaction(
    val share: Share,
    val number: Int,
    val transactionType: TransactionType,
    val date: String
)
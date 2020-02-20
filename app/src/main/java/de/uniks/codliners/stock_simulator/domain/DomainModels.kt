package de.uniks.codliners.stock_simulator.domain

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Entity
data class Balance(
    val value: Double,
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType { BUY, SELL }

data class Transaction(
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
data class Symbol(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val name: String,
    val type: Type
) {
    @Parcelize
    enum class Type : Parcelable { SHARE, CRYPTO }
}

@Entity
data class Quote(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val name: String,
    val latestPrice: Double,
    val change: Double?
)

@Entity
data class StockbrotQuote(
    @PrimaryKey
    val id: String,
    val type: Symbol.Type,
    val buyAmount: Double,
    val thresholdBuy: Double,
    val thresholdSell: Double
)

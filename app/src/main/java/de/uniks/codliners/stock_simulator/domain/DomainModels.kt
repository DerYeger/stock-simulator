package de.uniks.codliners.stock_simulator.domain

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Entity
data class Achievement(
    @PrimaryKey
    @StringRes
    val name: Int,
    @StringRes
    val description: Int,
    val reached: Boolean = false,
    val timestamp: Long? = null,  // null if achievement not reached otherwise the reached timestamp
    val displayed: Boolean = false
)

@Entity
data class Balance(
    val value: Double,
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis()
)

data class DepotQuote(
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val amount: Double,
    val buyingPrice: Double
)

@Entity
data class DepotQuotePurchase(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val amount: Double,
    val buyingPrice: Double
)

@Entity
data class DepotValue(
    val value: Double,
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis()
)

@Entity
data class HistoricalPrice(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val id: String,
    val date: Long,
    val price: Double
)

@Entity
@JsonClass(generateAdapter = true)
data class News(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val datetime: Long,
    val headline: String,
    val source: String,
    val url: String,
    val summary: String,
    val related: String,
    val image: String,
    val lang: String,
    val hasPaywall: Boolean
)

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
    val symbol: String,
    val type: Symbol.Type,
    val limitedBuying: Boolean,
    val buyLimit: Double,
    val maximumBuyPrice: Double,
    val minimumSellPrice: Double
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
data class DepotValue(
    val value: Double,
    @PrimaryKey
    val timestamp: Long = System.currentTimeMillis()
)

data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Long = 0,
    val id: String,
    val symbol: String,
    val type: Symbol.Type,
    val amount: Double,
    val price: Double,
    val transactionCosts: Double,
    val cashflow: Double,
    val transactionType: TransactionType,
    val date: Long,
    val result: Double?
)

enum class TransactionType { BUY, SELL }

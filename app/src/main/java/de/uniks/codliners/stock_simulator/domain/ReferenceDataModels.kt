package de.uniks.codliners.stock_simulator.domain

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

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
@JsonClass(generateAdapter = true)
data class StockbrotQuote(
    @PrimaryKey
    val id: String,
    val type: Symbol.Type,
    val thresholdBuy: Double,
    val thresholdSell: Double
)

@Entity
@JsonClass(generateAdapter = true)
data class HistoricalPriceFromApi(
    @PrimaryKey
    val date: String,
    val close: Double,
    val changeOverTime: Double,
    val change: Double,
    val changePercent: Double
)

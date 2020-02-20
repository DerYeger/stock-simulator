package de.uniks.codliners.stock_simulator.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass



@Entity
data class Symbol(
    @PrimaryKey
    val symbol: String,
    val name: String,
    val date: String,
    val isEnabled: Boolean,
    val type: Type,
    val region: String,
    val currency: String
) {
    enum class Type { SHARE, CRYPTO }
}

@Entity
data class Quote(
    @PrimaryKey
    val symbol: String,
    val type: Symbol.Type,
    val companyName: String?,
    val sector: String?,
    val latestPrice: Double,
    val change: Double?
)

@Entity
@JsonClass(generateAdapter = true)
data class StockbrotQuote(
    @PrimaryKey
    val symbol: String,
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

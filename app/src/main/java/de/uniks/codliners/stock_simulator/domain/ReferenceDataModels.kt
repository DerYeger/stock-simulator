package de.uniks.codliners.stock_simulator.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class Symbol(
    @PrimaryKey
    val symbol: String,
    val exchange: String?,
    val name: String,
    val date: String,
    val isEnabled: Boolean,
    val type: String,
    val region: String,
    val currency: String
)

enum class IssueType {
    ad, re, ce, si, lp, cs, et, wt, rt, oef, cef, ps, ut, struct, temp, crypto
}

@Entity
@JsonClass(generateAdapter = true)
data class Quote(
    @PrimaryKey
    val symbol: String,
    val companyName: String,
    val calculationPrice: String,
    val open: Double?,
    val openTime: Long?,
    val close: Double?,
    val closeTime: Long?,
    val high: Double?,
    val low: Double?,
    val latestPrice: Double,
    val latestSource: String,
    val latestTime: String,
    val latestUpdate: Long,
    val latestVolume: Long?,
    val volume: Long?,
    val previousClose: Double?,
    val previousVolume: Long?,
    val change: Double,
    val changePercent: Double,
    val avgTotalVolume: Long,
    val marketCap: Long,
    val week52High: Double,
    val week52Low: Double
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

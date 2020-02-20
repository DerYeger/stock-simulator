package de.uniks.codliners.stock_simulator.network

import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import de.uniks.codliners.stock_simulator.database.HistoricalPrice
import de.uniks.codliners.stock_simulator.domain.Quote
import de.uniks.codliners.stock_simulator.domain.Symbol
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
data class IEXSymbol(
    val symbol: String,
    val name: String,
    val type: String,
    val currency: String
)

fun IEXSymbol.asDomainSymbol() = Symbol(
    id = symbol.toUpperCase(Locale.ROOT),
    symbol = symbol,
    name = name,
    type = if (type == "crypto") Symbol.Type.CRYPTO else Symbol.Type.SHARE
)

fun List<IEXSymbol>.asDomainSymbols() = map { it.asDomainSymbol() }.toTypedArray()

@JsonClass(generateAdapter = true)
data class IEXQuote(
    val symbol: String,
    val companyName: String,
    val latestPrice: Double,
    val change: Double?
)

fun IEXQuote.asDomainQuote() = Quote(
    id = symbol,
    symbol = symbol,
    type = Symbol.Type.SHARE,
    name = companyName,
    latestPrice = latestPrice,
    change = change
)

@JsonClass(generateAdapter = true)
data class IEXHistoricalPrice(
    @PrimaryKey
    val date: String,
    val close: Double,
    val changeOverTime: Double,
    val change: Double,
    val changePercent: Double
)

fun IEXHistoricalPrice.asDomainHistoricalPrice(symbol: String) = HistoricalPrice(
    id = symbol,
    date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(date)!!.time,
    price = this.close
)

fun List<IEXHistoricalPrice>.asDomainHistoricalPrices(symbol: String): List<HistoricalPrice> {
    return map {
        it.asDomainHistoricalPrice(symbol)
    }
}
